/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.maps;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.Preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class CityLocationListener implements LocationListener {
    public static final String TAG = "CityLocationListener";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final long LOCATION_UPDATE_MIN_TIME = 1000 * 60 * 60; // Every hour
    public static final long LOCATION_UPDATE_MIN_DISTANCE = 1000 * 15; // 15 kilometers

    private Foursquare mFoursquare;
    private SharedPreferences mPrefs;

    public CityLocationListener(Foursquare foursquare, SharedPreferences prefs) {
        mFoursquare = foursquare;
        mPrefs = prefs;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (DEBUG) Log.d(TAG, "onLocationChanged: " + location);
        try {
            City city = Preferences.switchCity(mFoursquare, Preferences.getUser(mPrefs), location);
            Editor editor = mPrefs.edit();
            Preferences.storeCity(editor, city);
            editor.commit();
        } catch (Exception e) {
            if (DEBUG) Log.d(TAG, "Could not update city.", e);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

}
