/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.googlecode.dumpcatcher.logging.Dumpcatcher;
import com.googlecode.dumpcatcher.logging.DumpcatcherUncaughtExceptionHandler;
import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareCredentialsError;
import com.joelapenna.foursquared.maps.BestLocationListener;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Foursquared extends Application {
    public static final String TAG = "Foursquared";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final int LAST_LOCATION_UPDATE_THRESHOLD = 1000 * 60 * 60;

    // Common menu items
    private static final int MENU_PREFERENCES = -1;
    private static final int MENU_GROUP_SYSTEM = 20;

    private Dumpcatcher mDumpcatcher;

    private LocationListener mLocationListener = new LocationListener();

    private SharedPreferences mPrefs;
    private OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener;

    private static Foursquare sFoursquare = new Foursquare();

    public void onCreate() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (FoursquaredSettings.USE_DUMPCATCHER) setupDumpcatcher();
        // Set the oauth credentials.
        sFoursquare.setOAuthConsumerCredentials( //
                getResources().getString(R.string.oauth_consumer_key), //
                getResources().getString(R.string.oauth_consumer_secret));
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mOnSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(Preferences.PREFERENCE_PHONE)
                        || key.equals(Preferences.PREFERENCE_PASSWORD)) {
                    Log.d(TAG, key + " preference was changed");
                    try {
                        loadCredentials();
                    } catch (FoursquareCredentialsError e) {
                        // pass
                    }
                }
            }
        };
        mPrefs.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);

        try {
            loadCredentials();
        } catch (FoursquareCredentialsError e) {
            // We're not doing anything because hopefully our related activities
            // will handle the failure. This is simply convenience.
        }
    }

    public Location getLastKnownLocation() {
        return mLocationListener.getLastKnownLocation();
    }

    public LocationListener getLocationListener() {
        primeLocationListener();
        return mLocationListener;
    }

    private void setupDumpcatcher() {
        String client = Preferences.createUniqueId(mPrefs);
        if (FoursquaredSettings.DUMPCATCHER_TEST) {
            if (FoursquaredSettings.DEBUG) Log.d(TAG, "Loading Dumpcatcher TEST");
            mDumpcatcher = new Dumpcatcher( //
                    getResources().getString(R.string.test_dumpcatcher_product_key), //
                    getResources().getString(R.string.test_dumpcatcher_secret), //
                    getResources().getString(R.string.test_dumpcatcher_url), client, 5);
        } else {
            if (FoursquaredSettings.DEBUG) Log.d(TAG, "Loading Dumpcatcher Live");
            mDumpcatcher = new Dumpcatcher( //
                    getResources().getString(R.string.dumpcatcher_product_key), //
                    getResources().getString(R.string.dumpcatcher_secret), //
                    getResources().getString(R.string.dumpcatcher_url), client, 5);
        }

        UncaughtExceptionHandler handler = new DefaultUnhandledExceptionHandler(mDumpcatcher);
        // This can hang the app starving android of its ability to properly kill threads... maybe.
        Thread.setDefaultUncaughtExceptionHandler(handler);
        Thread.currentThread().setUncaughtExceptionHandler(handler);

        // TODO(jlapenna): Usage related, async sendCrashes should be pooled together or something.
        // This is nasty.
        sendUsage("Started");
    }

    private void loadCredentials() throws FoursquareCredentialsError {
        if (FoursquaredSettings.DEBUG) Log.d(TAG, "loadCredentials()");
        String phoneNumber = mPrefs.getString(Preferences.PREFERENCE_PHONE, null);
        String password = mPrefs.getString(Preferences.PREFERENCE_PASSWORD, null);
        String oauthToken = mPrefs.getString(Preferences.PREFERENCE_OAUTH_TOKEN, null);
        String oauthTokenSecret = mPrefs.getString(Preferences.PREFERENCE_OAUTH_TOKEN_SECRET, null);

        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(oauthToken) || TextUtils.isEmpty(oauthTokenSecret)) {
            throw new FoursquareCredentialsError("Phone number or password not set in preferences.");
        }
        sFoursquare.setCredentials(phoneNumber, password);
        sFoursquare.setOAuthToken(oauthToken, oauthTokenSecret);
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

    private void sendUsage(final String usage) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NameValuePair[] parameters = {
                            new BasicNameValuePair("tag", "usage"),
                            new BasicNameValuePair("short", usage),
                    };
                    HttpResponse response = mDumpcatcher.sendCrash(parameters);
                    response.getEntity().consumeContent();
                } catch (Exception e) {
                    // no biggie...
                }
            }
        });
        thread.start();
    }

    public static void addPreferencesToMenu(Context context, Menu menu) {
        Intent intent = new Intent(context, PreferenceActivity.class);
        menu.add(MENU_GROUP_SYSTEM, MENU_PREFERENCES, Menu.NONE, R.string.preferences_label) //
                .setIcon(android.R.drawable.ic_menu_preferences).setIntent(intent);
    }

    public static Foursquare getFoursquare() {
        return sFoursquare;
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

    private static final class DefaultUnhandledExceptionHandler extends
            DumpcatcherUncaughtExceptionHandler {

        private static final UncaughtExceptionHandler mOriginalExceptionHandler = Thread
                .getDefaultUncaughtExceptionHandler();

        DefaultUnhandledExceptionHandler(Dumpcatcher dumpcatcher) {
            super(dumpcatcher);
        }

        public void uncaughtException(Thread t, Throwable e) {
            super.uncaughtException(t, e);
            mOriginalExceptionHandler.uncaughtException(t, e);
        }

    }
}
