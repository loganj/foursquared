/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import com.joelapenna.foursquared.Foursquared;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

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

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
class RemoteResourceFetcher extends Observable {
    public static final String TAG = "RemoteResourceFetcher";
    public static final boolean DEBUG = Foursquared.DEBUG;

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

    public void fetch(Uri uri, String hash) {
        Message msg = mFetcherHandler.obtainMessage(FetcherHandler.MESSAGE_FETCH);
        msg.obj = uri;
        msg.obj = new Request(uri, hash);
        mFetcherHandler.sendMessage(msg);
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
                    Request request = (Request)msg.obj;
                    // We'll still notify observers that we tried to download the file but when the
                    // client fetches it and gets an IOException the'll know the request failed.
                    InputStream is = fetch(request.uri);
                    if (is != null) {
                        mResourceCache.store(request.hash, is);
                    }
                    notifyObservers(request.uri);
                default:
                    break;
            }
        }

        private InputStream fetch(Uri uri) {
            try {
                HttpGet httpGet = new HttpGet(uri.toString());
                // Now process the response we get
                HttpResponse response;
                response = mHttpClient.execute(httpGet);

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return entity.getContent();
                }
            } catch (IOException e) {
                if (DEBUG) Log.d(TAG, "IOException!", e);
            }
            return null;
        }
    }

    public void notifyObservers(Object data) {
        setChanged();
        super.notifyObservers(data);
    }

    /**
     * Create a thread-safe client. This client does not do redirecting, to allow us to capture
     * correct "error" codes.
     *
     * @return HttpClient
     */
    public static final DefaultHttpClient createHttpClient() {
        // Sets up the http part of the service.
        final SchemeRegistry supportedSchemes = new SchemeRegistry();

        // Register the "http" protocol scheme, it is required
        // by the default operator to look up socket factories.
        final SocketFactory sf = PlainSocketFactory.getSocketFactory();
        supportedSchemes.register(new Scheme("http", sf, 80));

        // Set some client http client parameter defaults.
        final HttpParams httpParams = createHttpParams();
        HttpClientParams.setRedirecting(httpParams, false);

        final ClientConnectionManager ccm = new ThreadSafeClientConnManager(httpParams,
                supportedSchemes);
        return new DefaultHttpClient(ccm, httpParams);
    }

    /**
     * Create the default HTTP protocol parameters.
     */
    private static final HttpParams createHttpParams() {
        // prepare parameters
        final HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUseExpectContinue(params, true);
        return params;
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
