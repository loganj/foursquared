/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.error.FoursquaredCredentialsError;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;

import java.util.Date;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Foursquared extends Application {
    public static final String TAG = "Foursquared";

    public static final boolean DEBUG = true;

    public static final boolean API_DEBUG = false;

    public static final int LAST_LOCATION_UPDATE_THRESHOLD = 1000 * 60 * 60;

    public static final String EXTRAS_VENUE_KEY = "venue";

    public static final String PREFERENCE_PHONE = "phone";

    public static final String PREFERENCE_PASSWORD = "password";

    public static final String PREFERENCE_TWITTER_CHECKIN = "twitter_checkin";

    public static final String PREFERENCE_SILENT_CHECKIN = "silent_checkin";

    // Hidden preferences
    public static final String PREFERENCE_EMAIL = "email";

    // Common menu items
    private static final int MENU_PREFERENCES = -1;

    private Foursquare mFoursquare;

    public void onCreate() {
        try {
            loadCredentials();
        } catch (FoursquaredCredentialsError e) {
            // We're not doing anything because hopefully our related activities
            // will handle the
            // failure. This is simply convenience.
        }
    }

    public Foursquare getFoursquare() {
        return mFoursquare;
    }

    public Location getLocation() {
        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setCostAllowed(true);

        String providerName = manager.getBestProvider(criteria, true);
        LocationProvider provider = manager.getProvider(providerName);
        if (DEBUG) Log.d(TAG, "Have Provider: " + provider.getName());
        Location location = manager.getLastKnownLocation(providerName);
        if (location != null) {
            long timeDelta = new Date().getTime() - location.getTime();
            if (timeDelta > LAST_LOCATION_UPDATE_THRESHOLD) {
                if (DEBUG) {
                    Log.d(TAG, "Last known position is too old! " + String.valueOf(timeDelta));
                }
                return null;
            }
            if (DEBUG) Log.d(TAG, "got Location: " + location);
        } else {
            if (DEBUG) Log.d(TAG, "No known location.");
        }
        return location;
    }

    public void loadCredentials() throws FoursquaredCredentialsError {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String phoneNumber = settings.getString(Foursquared.PREFERENCE_PHONE, null);
        String password = settings.getString(Foursquared.PREFERENCE_PASSWORD, null);

        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password)) {
            throw new FoursquaredCredentialsError("Phone number or password not set in preferences.");
        }
        mFoursquare = new Foursquare(phoneNumber, password);
    }

    public static String getVenueLocationLine2(Venue venue) {
        if (!TextUtils.isEmpty(venue.getCrossstreet())) {
            if (venue.getCrossstreet().startsWith("at")) {
                return "(" + venue.getCrossstreet() + ")";
            } else {
                return "(at " + venue.getCrossstreet() + ")";
            }
        } else if (!TextUtils.isEmpty(venue.getCity()) && !TextUtils.isEmpty(venue.getState())
                && !TextUtils.isEmpty(venue.getZip())) {
            return venue.getCity() + ", " + venue.getState() + " " + venue.getZip();
        } else {
            return null;
        }
    }

    public static void addPreferencesToMenu(Context context, Menu menu) {
        Intent intent = new Intent(context, PreferenceActivity.class);
        menu.add(Menu.NONE, MENU_PREFERENCES, Menu.NONE, R.string.preferences_label) //
                .setIcon(android.R.drawable.ic_menu_preferences).setIntent(intent);
    }
}
