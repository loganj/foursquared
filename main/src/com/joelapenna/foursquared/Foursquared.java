/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import android.app.Activity;
import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.util.IconUtils;
import com.joelapenna.foursquared.app.FoursquaredService;
import com.joelapenna.foursquared.error.LocationException;
import com.joelapenna.foursquared.location.BestLocationListener;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.CompatibilityHelp;
import com.joelapenna.foursquared.util.JavaLoggingHandler;
import com.joelapenna.foursquared.util.NullDiskCache;
import com.joelapenna.foursquared.util.RemoteResourceManager;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Foursquared extends Application {
    private static final String TAG = "Foursquared";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;
    static {
        Logger.getLogger("com.joelapenna.foursquare").addHandler(new JavaLoggingHandler());
        Logger.getLogger("com.joelapenna.foursquare").setLevel(Level.ALL);
    }

    public static final String PACKAGE_NAME = "com.joelapenna.foursquared";

    public static final String INTENT_ACTION_LOGGED_OUT = "com.joelapenna.foursquared.intent.action.LOGGED_OUT";
    public static final String INTENT_ACTION_LOGGED_IN = "com.joelapenna.foursquared.intent.action.LOGGED_IN";
    public static final String EXTRA_VENUE_ID = "com.joelapenna.foursquared.VENUE_ID";

    private String mVersion = null;

    private TaskHandler mTaskHandler;
    private HandlerThread mTaskThread;

    private SharedPreferences mPrefs;
    private RemoteResourceManager mRemoteResourceManager;
    private Sync mSync;

    private Foursquare mFoursquare;

    private BestLocationListener mBestLocationListener = new BestLocationListener();
    
    private boolean mIsFirstRun;
    

    @Override
    public void onCreate() {
        Log.i(TAG, "Using Debug Server:\t" + FoursquaredSettings.USE_DEBUG_SERVER);
        Log.i(TAG, "Using Dumpcatcher:\t" + FoursquaredSettings.USE_DUMPCATCHER);
        Log.i(TAG, "Using Debug Log:\t" + DEBUG);

        mVersion = getVersionString(this);
        
        // Check if this is a new install by seeing if our preference file exists on disk.
        mIsFirstRun = checkIfIsFirstRun();

        // Setup Prefs (to load dumpcatcher)
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Setup some defaults in our preferences if not set yet.
        Preferences.setupDefaults(mPrefs, getResources());
        
        // If we're on a high density device, request higher res images. This singleton
        // is picked up by the parsers to replace their icon urls with high res versions.
        float screenDensity = getApplicationContext().getResources().getDisplayMetrics().density;
        IconUtils.get().setRequestHighDensityIcons(screenDensity > 1.0f);
         
        // Setup Dumpcatcher - We've outgrown this infrastructure but we'll
        // leave its calls in place for the day that someone pays for some
        // appengine quota.
        // if (FoursquaredSettings.USE_DUMPCATCHER) {
        // Resources resources = getResources();
        // new DumpcatcherHelper(Preferences.createUniqueId(mPrefs), resources);
        // }
 
        // Sometimes we want the application to do some work on behalf of the
        // Activity. Lets do that
        // asynchronously.
        mTaskThread = new HandlerThread(TAG + "-AsyncThread");
        mTaskThread.start();
        mTaskHandler = new TaskHandler(mTaskThread.getLooper());

        // Set up storage cache.
        loadResourceManagers();

        // Catch sdcard state changes
        new MediaCardStateBroadcastReceiver().register();

        // Catch logins or logouts.
        new LoggedInOutBroadcastReceiver().register();

        // Log into Foursquare, if we can.
        loadFoursquare();

        if ( CompatibilityHelp.API_LEVEL_AT_LEAST_ECLAIR ) {
            try {
                mSync = (SyncImpl)Class.forName("com.joelapenna.foursquared.SyncImpl").getDeclaredConstructor(Foursquared.class).newInstance(this);
            } catch (Exception e) {
                Log.w(TAG, "failed to instantiate SyncImpl for Eclair+", e);
                mSync = new PreEclairSyncImpl();
            }
        } else {
            mSync = new PreEclairSyncImpl();
        }
    }

    public boolean isReady() {
        return getFoursquare().hasLoginAndPassword() && !TextUtils.isEmpty(getUserId());
    }

    public Foursquare getFoursquare() {
        return mFoursquare;
    }

    public String getUserId() {
        return Preferences.getUserId(mPrefs);
    }
    
    public String getUserGender() {
        return Preferences.getUserGender(mPrefs);
    }
    
    public String getVersion() {

        if (mVersion != null) {
            return mVersion;
        } else {
            return "";
        }
    }
    
    public String getLastSeenChangelogVersion() {
        return Preferences.getLastSeenChangelogVersion(mPrefs);
    }
    
    public void storeLastSeenChangelogVersion(String version) {
        Preferences.storeLastSeenChangelogVersion(mPrefs.edit(), version); 
    }
    
    public boolean getUseNativeImageViewerForFullScreenImages() {
        return Preferences.getUseNativeImageViewerForFullScreenImages(mPrefs);
    }
    
    public RemoteResourceManager getRemoteResourceManager() {
        return mRemoteResourceManager;
    }

    public Sync getSync() {
        return mSync;
    }
    
    public BestLocationListener requestLocationUpdates(boolean gps) {
        mBestLocationListener.register(
                (LocationManager) getSystemService(Context.LOCATION_SERVICE), gps);
        return mBestLocationListener;
    }

    public BestLocationListener requestLocationUpdates(Observer observer) {
        mBestLocationListener.addObserver(observer);
        mBestLocationListener.register(
                (LocationManager) getSystemService(Context.LOCATION_SERVICE), true);
        return mBestLocationListener;
    }

    public void removeLocationUpdates() {
        mBestLocationListener
                .unregister((LocationManager) getSystemService(Context.LOCATION_SERVICE));
    }

    public void removeLocationUpdates(Observer observer) {
        mBestLocationListener.deleteObserver(observer);
        this.removeLocationUpdates();
    }

    public Location getLastKnownLocation() {
        return mBestLocationListener.getLastKnownLocation();
    }

    public Location getLastKnownLocationOrThrow() throws LocationException {
        Location location = mBestLocationListener.getLastKnownLocation();
        if (location == null) {
            throw new LocationException();
        }
        return location;
    }
    
    public void clearLastKnownLocation() {
        mBestLocationListener.clearLastKnownLocation();
    }

    public void requestStartService() {
        mTaskHandler.sendMessage( //
                mTaskHandler.obtainMessage(TaskHandler.MESSAGE_START_SERVICE));
    }

    public void requestUpdateUser() {
        mTaskHandler.sendEmptyMessage(TaskHandler.MESSAGE_UPDATE_USER);
    }

    private void loadFoursquare() {
        // Try logging in and setting up foursquare oauth, then user
        // credentials.
        if (FoursquaredSettings.USE_DEBUG_SERVER) {
            mFoursquare = new Foursquare(Foursquare.createHttpApi("10.0.2.2:8080", mVersion, false));
        } else {
            mFoursquare = new Foursquare(Foursquare.createHttpApi(mVersion, false));
        }

        if (FoursquaredSettings.DEBUG) Log.d(TAG, "loadCredentials()");
        String phoneNumber = mPrefs.getString(Preferences.PREFERENCE_LOGIN, null);
        String password = mPrefs.getString(Preferences.PREFERENCE_PASSWORD, null);
        mFoursquare.setCredentials(phoneNumber, password);
        if (mFoursquare.hasLoginAndPassword()) {
            sendBroadcast(new Intent(INTENT_ACTION_LOGGED_IN));
        } else {
            sendBroadcast(new Intent(INTENT_ACTION_LOGGED_OUT));
        }
    }

    /**
     * Provides static access to a Foursquare instance. This instance is
     * initiated without user credentials.
     * 
     * @param context the context to use when constructing the Foursquare
     *            instance
     * @return the Foursquare instace
     */
    public static Foursquare createFoursquare(Context context) {
        String version = getVersionString(context);
        if (FoursquaredSettings.USE_DEBUG_SERVER) {
            return new Foursquare(Foursquare.createHttpApi("10.0.2.2:8080", version, false));
        } else {
            return new Foursquare(Foursquare.createHttpApi(version, false));
        }
    }

    /**
     * Provides static access to the application as a Foursquared.  Mostly here to hide the cast, and in case we can do
     * better later.
     * @return the current Application as a Foursquared instance
     */
    public static Foursquared get(Activity activity) {
        return (Foursquared)activity.getApplication();
    }

    /**
     * Constructs the version string of the application.
     * 
     * @param context the context to use for getting package info
     * @return the versions string of the application
     */
    private static String getVersionString(Context context) {
        // Get a version string for the app.
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(PACKAGE_NAME, 0);
            return PACKAGE_NAME + ":" + String.valueOf(pi.versionCode);
        } catch (NameNotFoundException e) {
            if (DEBUG) Log.d(TAG, "Could not retrieve package info", e);
            throw new RuntimeException(e);
        }
    }

    private void loadResourceManagers() {
        // We probably don't have SD card access if we get an
        // IllegalStateException. If it did, lets
        // at least have some sort of disk cache so that things don't npe when
        // trying to access the
        // resource managers.
        try {
            if (DEBUG) Log.d(TAG, "Attempting to load RemoteResourceManager(cache)");
            mRemoteResourceManager = new RemoteResourceManager("cache");
        } catch (IllegalStateException e) {
            if (DEBUG) Log.d(TAG, "Falling back to NullDiskCache for RemoteResourceManager");
            mRemoteResourceManager = new RemoteResourceManager(new NullDiskCache());
        }
    }

    public boolean getIsFirstRun() {
        return mIsFirstRun;
    }

    private boolean checkIfIsFirstRun() {
        File file = new File(
            "/data/data/com.joelapenna.foursquared/shared_prefs/com.joelapenna.foursquared_preferences.xml");
        return !file.exists();
    }
    

    /**
     * Set up resource managers on the application depending on SD card state.
     * 
     * @author Joe LaPenna (joe@joelapenna.com)
     */
    private class MediaCardStateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG)
                Log
                        .d(TAG, "Media state changed, reloading resource managers:"
                                + intent.getAction());
            if (Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) {
                getRemoteResourceManager().shutdown();
                loadResourceManagers();
            } else if (Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())) {
                loadResourceManagers();
            }
        }

        public void register() {
            // Register our media card broadcast receiver so we can
            // enable/disable the cache as
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
            registerReceiver(this, intentFilter);
        }
    }

    private class LoggedInOutBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (INTENT_ACTION_LOGGED_IN.equals(intent.getAction())) {
                requestUpdateUser();
            }
        }

        public void register() {
            // Register our media card broadcast receiver so we can
            // enable/disable the cache as
            // appropriate.
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(INTENT_ACTION_LOGGED_IN);
            intentFilter.addAction(INTENT_ACTION_LOGGED_OUT);
            registerReceiver(this, intentFilter);
        }
    }

    private class TaskHandler extends Handler {

        private static final int MESSAGE_UPDATE_USER = 1;
        private static final int MESSAGE_START_SERVICE = 2;

        public TaskHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (DEBUG) Log.d(TAG, "handleMessage: " + msg.what);

            switch (msg.what) {
                case MESSAGE_UPDATE_USER:
                    try {
                        // Update user info
                        Log.d(TAG, "Updating user.");
                        
                        // Use location when requesting user information, if we
                        // have it.
                        Foursquare.Location location = LocationUtils
                                .createFoursquareLocation(getLastKnownLocation());
                        User user = getFoursquare().user(
                                null, false, false, location);

                        Editor editor = mPrefs.edit();
                        Preferences.storeUser(editor, user);
                        editor.commit();

                        if (location == null) {
                            // Pump the location listener, we don't have a
                            // location in our listener yet.
                            Log.d(TAG, "Priming Location from user city.");
                            Location primeLocation = new Location("foursquare");
                            // Very inaccurate, right?
                            primeLocation.setTime(System.currentTimeMillis());
                            mBestLocationListener.updateLocation(primeLocation);
                        }

                    } catch (FoursquareError e) {
                        if (DEBUG) Log.d(TAG, "FoursquareError", e);
                        // TODO Auto-generated catch block
                    } catch (FoursquareException e) {
                        if (DEBUG) Log.d(TAG, "FoursquareException", e);
                        // TODO Auto-generated catch block
                    } catch (IOException e) {
                        if (DEBUG) Log.d(TAG, "IOException", e);
                        // TODO Auto-generated catch block
                    }
                    return;

                case MESSAGE_START_SERVICE:
                    Intent serviceIntent = new Intent(Foursquared.this, FoursquaredService.class);
                    serviceIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    startService(serviceIntent);
                    return;
            }
        }
    }
}
