/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareCredentialsException;
import com.joelapenna.foursquared.maps.BestLocationListener;
import com.joelapenna.foursquared.maps.CityLocationListener;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.DumpcatcherHelper;
import com.joelapenna.foursquared.util.NullDiskCache;
import com.joelapenna.foursquared.util.RemoteResourceManager;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Foursquared extends Application {
    private static final String TAG = "Foursquared";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String PACKAGE_NAME = "com.joelapenna.foursquared";

    public static final String INTENT_ACTION_LOGGED_OUT = "com.joelapenna.foursquared.intent.action.LOGGED_OUT";
    public static final String EXTRA_VENUE_ID = "com.joelapenna.foursquared.VENUE_ID";

    // Common menu items
    private static final int MENU_PREFERENCES = -1;
    private static final int MENU_GROUP_SYSTEM = 20;

    private SharedPreferences mPrefs;

    private CityLocationListener mCityLocationListener;
    private BestLocationListener mBestLocationListener = new BestLocationListener();

    private MediaCardStateBroadcastReceiver mMediaCardStateBroadcastReceiver;
    private RemoteResourceManager mRemoteResourceManager;

    private Foursquare mFoursquare;

    @Override
    public void onCreate() {
        Log.i(TAG, "Using Debug Server:\t" + FoursquaredSettings.USE_DEBUG_SERVER);
        Log.i(TAG, "Using Dumpcatcher:\t" + FoursquaredSettings.USE_DUMPCATCHER);
        Log.i(TAG, "Using Debug Log:\t" + DEBUG);

        // Setup Prefs (to load dumpcatcher)
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Setup Dumpcatcher
        if (FoursquaredSettings.USE_DUMPCATCHER) {
            Resources resources = getResources();
            new DumpcatcherHelper(Preferences.createUniqueId(mPrefs), resources);
            DumpcatcherHelper.sendUsage("Started");
        }

        // Log into Foursquare, if we can.
        loadFoursquare();

        // Set up storage cache.
        loadResourceManagers();
        mMediaCardStateBroadcastReceiver = new MediaCardStateBroadcastReceiver();
        mMediaCardStateBroadcastReceiver.register();

        mCityLocationListener = new CityLocationListener(mFoursquare, mPrefs);
        mCityLocationListener.register((LocationManager)getSystemService(Context.LOCATION_SERVICE));
    }

    @Override
    public void onTerminate() {
        mRemoteResourceManager.shutdown();
        mCityLocationListener.unregister();
    }

    public Foursquare getFoursquare() {
        return mFoursquare;
    }

    public RemoteResourceManager getRemoteResourceManager() {
        return mRemoteResourceManager;
    }

    public BestLocationListener requestLocationUpdates() {
        mBestLocationListener.register((LocationManager)getSystemService(Context.LOCATION_SERVICE));
        return mBestLocationListener;
    }

    public void removeLocationUpdates() {
        mBestLocationListener.unregister((LocationManager)getSystemService(Context.LOCATION_SERVICE));
    }

    public Location getLastKnownLocation() {
        return mBestLocationListener.getLastKnownLocation();
    }

    private void loadFoursquare() {
        String version;
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(PACKAGE_NAME, 0);
            version = PACKAGE_NAME + " " + String.valueOf(pi.versionCode);
        } catch (NameNotFoundException e) {
            if (DEBUG) Log.d(TAG, "NameNotFoundException", e);
            throw new RuntimeException(e);
        }

        // Try logging in and setting up foursquare oauth, then user credentials.
        mFoursquare = new Foursquare(FoursquaredSettings.USE_DEBUG_SERVER, version);
        mFoursquare.setOAuthConsumerCredentials( //
                getString(R.string.oauth_consumer_key), //
                getString(R.string.oauth_consumer_secret));
        try {
            if (FoursquaredSettings.DEBUG) Log.d(TAG, "loadCredentials()");
            String phoneNumber = mPrefs.getString(Preferences.PREFERENCE_LOGIN, null);
            String password = mPrefs.getString(Preferences.PREFERENCE_PASSWORD, null);

            String oauthToken = mPrefs.getString(Preferences.PREFERENCE_OAUTH_TOKEN, null);
            String oauthTokenSecret = mPrefs.getString(Preferences.PREFERENCE_OAUTH_TOKEN_SECRET,
                    null);

            if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password)
                    || TextUtils.isEmpty(oauthToken) || TextUtils.isEmpty(oauthTokenSecret)) {
                throw new FoursquareCredentialsException(
                        "Phone number or password not set in preferences.");
            }
            mFoursquare.setCredentials(phoneNumber, password);
            mFoursquare.setOAuthToken(oauthToken, oauthTokenSecret);
        } catch (FoursquareCredentialsException e) {
            // We're not doing anything because hopefully our related activities
            // will handle the failure. This is simply convenience.
        }
    }

    private void loadResourceManagers() {
        // We probably don't have SD card access if we get an IllegalStateException. If it did, lets
        // at least have some sort of disk cache so that things don't npe when trying to access the
        // resource managers.
        try {
            if (DEBUG) Log.d(TAG, "Attempting to load RemoteResourceManager(cache)");
            mRemoteResourceManager = new RemoteResourceManager("cache");
        } catch (IllegalStateException e) {
            if (DEBUG) Log.d(TAG, "Falling back to NullDiskCache for RemoteResourceManager");
            mRemoteResourceManager = new RemoteResourceManager(new NullDiskCache());
        }
    }

    public static void addPreferencesToMenu(Context context, Menu menu) {
        Intent intent = new Intent(context, PreferenceActivity.class);
        menu.add(MENU_GROUP_SYSTEM, MENU_PREFERENCES, Menu.CATEGORY_SECONDARY,
                R.string.preferences_label) //
                .setIcon(android.R.drawable.ic_menu_preferences).setIntent(intent);
    }

    /**
     * Set up resource managers on the application depending on SD card state.
     *
     * @author Joe LaPenna (joe@joelapenna.com)
     */
    public class MediaCardStateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "Media state changed, reloading resource managers:"
                    + intent.getAction());
            if (Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) {
                getRemoteResourceManager().shutdown();
                loadResourceManagers();
            } else if (Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())) {
                loadResourceManagers();
            }
        }

        public void register() {
            // Register our media card broadcast receiver so we can enable/disable the cache as
            // appropriate.
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            // intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
            // intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
            // intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            // intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
            // intentFilter.addAction(Intent.ACTION_MEDIA_NOFS);
            // intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
            // intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
            intentFilter.addDataScheme("file");
            registerReceiver(mMediaCardStateBroadcastReceiver, intentFilter);
        }

    }
}
