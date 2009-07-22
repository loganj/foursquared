/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import com.joelapenna.foursquared.FoursquaredSettings;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.zip.GZIPInputStream;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
class RemoteResourceFetcher extends Observable {
    public static final String TAG = "RemoteResourceFetcher";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private DiskCache mResourceCache;
    private Handler mFetcherHandler;
    private HandlerThread mFetcherThread;
    private Looper mFetcherLooper;

    private HttpClient mHttpClient;

    public RemoteResourceFetcher(DiskCache cache) {
        mResourceCache = cache;
        mHttpClient = createHttpClient();

        startFetcher();
    }

    @Override
    public void notifyObservers(Object data) {
        setChanged();
        super.notifyObservers(data);
    }

    public void fetch(Uri uri, String hash) {
        Message msg = mFetcherHandler.obtainMessage(FetcherHandler.MESSAGE_FETCH);
        msg.obj = new Request(uri, hash);
        mFetcherHandler.sendMessage(msg);
    }

    public void fetchBlocking(Uri uri, String hash) throws IOException {
        makeRequest(new Request(uri, hash));
    }

    public void shutdown() {
        mFetcherLooper.quit();
        mFetcherThread = null;
        mFetcherHandler = null;
        mFetcherLooper = null;
    }

    private void startFetcher() {
        if (DEBUG) Log.d(TAG, "Starting Initializer");
        mFetcherThread = new HandlerThread("FetcherThread", Process.THREAD_PRIORITY_BACKGROUND);
        mFetcherThread.start();
        mFetcherLooper = mFetcherThread.getLooper();
        mFetcherHandler = new FetcherHandler(mFetcherLooper);
    }

    private class FetcherHandler extends Handler {

        /**
         * Use to request a thumb fetch. (May contain large amounts of data). msg.obj will be the
         * GUID of the resource to fetch.
         */
        static final int MESSAGE_FETCH = 0;

        /**
         * @param initializerLooper
         */
        FetcherHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (DEBUG) Log.d(TAG, "handle Message" + msg.toString());
            switch (msg.what) {
                case MESSAGE_FETCH:
                    try {
                        makeRequest((Request)msg.obj);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        // There isn't anything we can really do here... that I want to implement.
                    }
                default:
                    break;
            }
        }
    }

    private void makeRequest(Request request) throws IOException {
        try {
            HttpGet httpGet = new HttpGet(request.uri.toString());
            httpGet.addHeader("Accept-Encoding", "gzip");
            HttpResponse response = mHttpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream is = getUngzippedContent(entity);
            mResourceCache.store(request.hash, is);
        } finally {
            notifyObservers(request.uri);
        }
    }

    /**
     * Gets the input stream from a response entity. If the entity is gzipped then this will get a
     * stream over the uncompressed data.
     *
     * @param entity the entity whose content should be read
     * @return the input stream to read from
     * @throws IOException
     */
    public static InputStream getUngzippedContent(HttpEntity entity) throws IOException {
        InputStream responseStream = entity.getContent();
        if (responseStream == null) {
            return responseStream;
        }
        Header header = entity.getContentEncoding();
        if (header == null) {
            return responseStream;
        }
        String contentEncoding = header.getValue();
        if (contentEncoding == null) {
            return responseStream;
        }
        if (contentEncoding.contains("gzip")) {
            responseStream = new GZIPInputStream(responseStream);
        }
        return responseStream;
    }

    /**
     * Create a thread-safe client. This client does not do redirecting, to allow us to capture
     * correct "error" codes.
     *
     * @return HttpClient
     */
    public static final DefaultHttpClient createHttpClient() {

        // Shamelessly cribbed from AndroidHttpClient
        HttpParams params = new BasicHttpParams();

        // Turn off stale checking. Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.
        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        // Default connection and socket timeout of 10 seconds. Tweak to taste.
        HttpConnectionParams.setConnectionTimeout(params, 10 * 1000);
        HttpConnectionParams.setSoTimeout(params, 10 * 1000);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        // Sets up the http part of the service.
        final SchemeRegistry supportedSchemes = new SchemeRegistry();

        // Register the "http" protocol scheme, it is required
        // by the default operator to look up socket factories.
        final SocketFactory sf = PlainSocketFactory.getSocketFactory();
        supportedSchemes.register(new Scheme("http", sf, 80));

        final ClientConnectionManager ccm = new ThreadSafeClientConnManager(params,
                supportedSchemes);
        return new DefaultHttpClient(ccm, params);
    }

    private static class Request {
        Uri uri;
        String hash;

        public Request(Uri requestUri, String requestHash) {
            uri = requestUri;
            hash = requestHash;
        }
    }

}
