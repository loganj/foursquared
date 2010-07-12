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
        this(new BaseDiskCache("foursquare", cacheName));
    }

    public RemoteResourceManager(DiskCache cache) {
        mDiskCache = cache;
        mRemoteResourceFetcher = new RemoteResourceFetcher(mDiskCache);
        mRemoteResourceFetcher.addObserver(mFetcherObserver);
    }

    public boolean exists(Uri uri) {
        return mDiskCache.exists(Uri.encode(uri.toString()));
    }

    /**
     * If IOException is thrown, we don't have the resource available.
     */
    public File getFile(Uri uri) {
        if (DEBUG) Log.d(TAG, "getInputStream(): " + uri);
        return mDiskCache.getFile(Uri.encode(uri.toString()));
    }

    /**
     * If IOException is thrown, we don't have the resource available.
     */
    public InputStream getInputStream(Uri uri) throws IOException {
        if (DEBUG) Log.d(TAG, "getInputStream(): " + uri);
        return mDiskCache.getInputStream(Uri.encode(uri.toString()));
    }

    /**
     * Request a resource be downloaded. Useful to call after a IOException from getInputStream.
     */
    public void request(Uri uri) {
        if (DEBUG) Log.d(TAG, "request(): " + uri);
        mRemoteResourceFetcher.fetch(uri, Uri.encode(uri.toString()));
    }
    
    /**
     * Explicitly expire an individual item.
     */
    public void invalidate(Uri uri) {
        mDiskCache.invalidate(Uri.encode(uri.toString()));
    }

    public void shutdown() {
        mRemoteResourceFetcher.shutdown();
        mDiskCache.cleanup();
    }

    public void clear() {
        mRemoteResourceFetcher.shutdown();
        mDiskCache.clear();
    }

    public static abstract class ResourceRequestObserver implements Observer {

        private Uri mRequestUri;

        abstract public void requestReceived(Observable observable, Uri uri);

        public ResourceRequestObserver(Uri requestUri) {
            mRequestUri = requestUri;
        }

        @Override
        public void update(Observable observable, Object data) {
            if (DEBUG) Log.d(TAG, "Recieved update: " + data);
            Uri dataUri = (Uri)data;
            if (dataUri == mRequestUri) {
                if (DEBUG) Log.d(TAG, "requestReceived: " + dataUri);
                requestReceived(observable, dataUri);
            }
        }
    }

    /**
     * Relay the observed download to this controlling class.
     */
    private class FetcherObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            setChanged();
            notifyObservers(data);
        }
    }
}
