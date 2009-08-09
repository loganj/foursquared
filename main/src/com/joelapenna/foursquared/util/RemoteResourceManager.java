/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import com.joelapenna.foursquared.FoursquaredSettings;

import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class RemoteResourceManager extends Observable {
    private static final String TAG = "RemoteResourceManager";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private DiskCache mDiskCache;
    private RemoteResourceFetcher mRemoteResourceFetcher;
    private FetcherObserver mFetcherObserver = new FetcherObserver();

    public RemoteResourceManager(String cacheName) {
        mDiskCache = new BaseDiskCache("foursquare", cacheName);

        mRemoteResourceFetcher = new RemoteResourceFetcher(mDiskCache);
        mRemoteResourceFetcher.addObserver(mFetcherObserver);
    }

    public void shutdown() {
        mRemoteResourceFetcher.shutdown();
    }

    /**
     * Request a resource be downloaded. Useful to call after a IOException from getInputStream.
     *
     * @param uri
     */
    public void request(Uri uri) {
        if (DEBUG) Log.d(TAG, "request(): " + uri);
        mRemoteResourceFetcher.fetch(uri, Uri.encode(uri.toString()));
    }

    /**
     * Request a resource be downloaded. Useful to call after a IOException from getInputStream.
     *
     * @param uri
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    public void requestBlocking(Uri uri) throws InterruptedException, ExecutionException {
        if (DEBUG) Log.d(TAG, "request(): " + uri);
        mRemoteResourceFetcher.fetchBlocking(uri, Uri.encode(uri.toString()));
    }

    /**
     * If IOException is thrown, we don't have the resource available.
     *
     * @param uri
     * @return
     * @throws IOException
     */
    public File getFile(Uri uri) {
        if (DEBUG) Log.d(TAG, "getInputStream(): " + uri);
        return mDiskCache.getFile(Uri.encode(uri.toString()));
    }

    /**
     * If IOException is thrown, we don't have the resource available.
     *
     * @param uri
     * @return
     * @throws IOException
     */
    public InputStream getInputStream(Uri uri) throws IOException {
        if (DEBUG) Log.d(TAG, "getInputStream(): " + uri);
        return mDiskCache.getInputStream(Uri.encode(uri.toString()));
    }

    /**
     * Relay the observed download to the controlling class.
     *
     * @author Joe LaPenna (joe@joelapenna.com)
     */
    private class FetcherObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            setChanged();
            notifyObservers(data);
        }
    }
}
