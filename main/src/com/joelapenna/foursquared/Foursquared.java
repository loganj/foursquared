/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareCredentialsException;
import com.joelapenna.foursquared.maps.BestLocationListener;
import com.joelapenna.foursquared.util.DumpcatcherHelper;
import com.joelapenna.foursquared.util.RemoteResourceManager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;

import java.util.List;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Foursquared extends Application {
    private static final String TAG = "Foursquared";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String INTENT_ACTION_LOGGED_OUT = "com.joelapenna.foursquared.intent.action.LOGGED_OUT";
    public static final String EXTRA_VENUE_ID = "com.joelapenna.foursquared.VENUE_ID";

    // Common menu items
    private static final int MENU_PREFERENCES = -1;
    private static final int MENU_GROUP_SYSTEM = 20;

    private LocationListener mLocationListener = new LocationListener();

    private SharedPreferences mPrefs;

    private Foursquare mFoursquare;
    private RemoteResourceManager sUserPhotosManager = new RemoteResourceManager("user_photos");
    private RemoteResourceManager sBadgeIconManager = new RemoteResourceManager("badges");

    @Override
    public void onCreate() {
        Log.i(TAG, "Using Debug Server:\t" + FoursquaredSettings.USE_DEBUG_SERVER);
        Log.i(TAG, "Using Dumpcatcher:\t" + FoursquaredSettings.USE_DUMPCATCHER);
        Log.i(TAG, "Using Debug Log:\t" + DEBUG);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (FoursquaredSettings.USE_DUMPCATCHER) {
            Resources resources = getResources();
            new DumpcatcherHelper(Preferences.createUniqueId(mPrefs), resources);
            DumpcatcherHelper.sendUsage("Started");
        }

        mFoursquare = new Foursquare(FoursquaredSettings.USE_DEBUG_SERVER);

        // Set the oauth credentials.
        mFoursquare.setOAuthConsumerCredentials( //
                getResources().getString(R.string.oauth_consumer_key), //
                getResources().getString(R.string.oauth_consumer_secret));

        try {
            loadCredentials();
        } catch (FoursquareCredentialsException e) {
            // We're not doing anything because hopefully our related activities
            // will handle the failure. This is simply convenience.
        }
    }

    @Override
    public void onTerminate() {
        mFoursquare = null;

        sUserPhotosManager.shutdown();
        sUserPhotosManager = null;

        sBadgeIconManager.shutdown();
        sBadgeIconManager = null;
    }

    public Foursquare getFoursquare() {
        return mFoursquare;
    }

    public Location getLastKnownLocation() {
        primeLocationListener();
        return mLocationListener.getLastKnownLocation();
    }

    public LocationListener getLocationListener() {
        primeLocationListener();
        return mLocationListener;
    }

    private void loadCredentials() throws FoursquareCredentialsException {
        if (FoursquaredSettings.DEBUG) Log.d(TAG, "loadCredentials()");
        String phoneNumber = mPrefs.getString(Preferences.PREFERENCE_PHONE, null);
        String password = mPrefs.getString(Preferences.PREFERENCE_PASSWORD, null);

        String oauthToken = mPrefs.getString(Preferences.PREFERENCE_OAUTH_TOKEN, null);
        String oauthTokenSecret = mPrefs.getString(Preferences.PREFERENCE_OAUTH_TOKEN_SECRET, null);

        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(oauthToken) || TextUtils.isEmpty(oauthTokenSecret)) {
            throw new FoursquareCredentialsException(
                    "Phone number or password not set in preferences.");
        }
        mFoursquare.setCredentials(phoneNumber, password);
        mFoursquare.setOAuthToken(oauthToken, oauthTokenSecret);
    }

    private void primeLocationListener() {
        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        Location location = null;
        List<String> providers = manager.getProviders(true);
        int providersCount = providers.size();
        for (int i = 0; i < providersCount; i++) {
            location = manager.getLastKnownLocation(providers.get(i));
            mLocationListener.getBetterLocation(location);
        }
    }

    public static void addPreferencesToMenu(Context context, Menu menu) {
        Intent intent = new Intent(context, PreferenceActivity.class);
        menu.add(MENU_GROUP_SYSTEM, MENU_PREFERENCES, Menu.CATEGORY_SECONDARY,
                R.string.preferences_label) //
                .setIcon(android.R.drawable.ic_menu_preferences).setIntent(intent);
    }

    public RemoteResourceManager getUserPhotosManager() {
        return sUserPhotosManager;
    }

    public RemoteResourceManager getBadgeIconManager() {
        return sBadgeIconManager;
    }

    /**
     * Used for the accuracy algorithm getBetterLocation.
     */
    public static class LocationListener extends BestLocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (DEBUG) Log.d(TAG, "onLocationChanged: " + location);
            getBetterLocation(location);
        }
    }
}
