/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Auth;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.error.FoursquaredCredentialsError;
import com.joelapenna.foursquared.maps.BestLocationListener;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Foursquared extends Application {
    public static final String TAG = "Foursquared";
    public static final boolean DEBUG = true;
    public static final boolean API_DEBUG = true;

    public static final int LAST_LOCATION_UPDATE_THRESHOLD = 1000 * 60 * 60;

    public static final String PREFERENCE_PHONE = "phone";
    public static final String PREFERENCE_PASSWORD = "password";
    public static final String PREFERENCE_TWITTER_CHECKIN = "twitter_checkin";
    public static final String PREFERENCE_SILENT_CHECKIN = "silent_checkin";

    // Hidden preferences
    public static final String PREFERENCE_CITY_ID = "city_id";
    public static final String PREFERENCE_EMAIL = "email";
    public static final String PREFERENCE_FIRST = "first_name";
    public static final String PREFERENCE_GENDER = "gender";
    public static final String PREFERENCE_ID = "id";
    public static final String PREFERENCE_LAST = "last_name";
    public static final String PREFERENCE_PHOTO = "photo";

    // Common menu items
    private static final int MENU_PREFERENCES = -1;
    private static final int MENU_GROUP_SYSTEM = -1;

    private LocationListener mLocationListener = new LocationListener();

    private SharedPreferences mSharedPrefs;
    private OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener;

    private static Foursquare sFoursquare = new Foursquare();

    public void onCreate() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mOnSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(PREFERENCE_PHONE) || key.equals(PREFERENCE_PASSWORD)) {
                    Log.d(TAG, key + " preference was changed");
                    try {
                        loadCredentials();
                    } catch (FoursquaredCredentialsError e) {
                        if (DEBUG) Log.d(TAG, "Clearing credentials", e);
                        sFoursquare.setCredentials(null, null);
                    }
                }
            }
        };
        mSharedPrefs.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);

        try {
            loadCredentials();
        } catch (FoursquaredCredentialsError e) {
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

    private void loadCredentials() throws FoursquaredCredentialsError {
        if (DEBUG) Log.d(TAG, "loadCredentials()");
        String phoneNumber = mSharedPrefs.getString(Foursquared.PREFERENCE_PHONE, null);
        String password = mSharedPrefs.getString(Foursquared.PREFERENCE_PASSWORD, null);

        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password)) {
            throw new FoursquaredCredentialsError(
                    "Phone number or password not set in preferences.");
        }
        sFoursquare.setCredentials(phoneNumber, password);
        new Thread() {
            public void run() {
                try {
                    Foursquared.this.getUserInfo();
                } catch (FoursquaredCredentialsError e) {
                    if (DEBUG) Log.d(TAG, "Could not log in: ", e);
                    Toast.makeText(Foursquared.this,
                            "Unable to log in. Please check your phone number and password.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }.start();
    }

    private void getUserInfo() throws FoursquaredCredentialsError {
        try {
            if (DEBUG) Log.d(TAG, "Trying to log in.");
            Auth auth = sFoursquare.login();
            // We don't call user because its broken for authenticated user lookups.
            // User user = sFoursquare.user();
            if (auth != null && auth.status() /* && user != null */) {
                Editor editor = mSharedPrefs.edit();

                editor.putString(PREFERENCE_EMAIL, auth.getEmail());
                editor.putString(PREFERENCE_FIRST, auth.getFirstname());
                editor.putString(PREFERENCE_LAST, auth.getLastname());
                editor.putString(PREFERENCE_PHOTO, auth.getPhoto());

                // editor.putString(PREFERENCE_CITY_ID, user.getCityid());
                // editor.putString(PREFERENCE_ID, user.getId());
                // editor.putString(PREFERENCE_GENDER, user.getGender());

                editor.commit();
            }
        } catch (FoursquareError e) {
            if (DEBUG) Log.d(TAG, "FoursquareError: ", e);
            throw new FoursquaredCredentialsError(e.getMessage());
        } catch (FoursquareParseException e) {
            if (DEBUG) Log.d(TAG, "FoursquareCredentialsError: ", e);
            throw new FoursquaredCredentialsError(e.getMessage());
        } catch (IOException e) {
        }
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
}
