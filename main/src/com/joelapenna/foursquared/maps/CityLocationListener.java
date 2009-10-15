/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.maps;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.preferences.Preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class CityLocationListener implements LocationListener {
    public static final String TAG = "CityLocationListener";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final long LOCATION_UPDATE_MIN_TIME = 1000 * 60 * 60; // Every hour
    private static final long LOCATION_UPDATE_MIN_DISTANCE = 1000 * 10; // 10 kilometers

    private Foursquare mFoursquare;
    private SharedPreferences mPrefs;

    private WeakReference<LocationManager> mLocationManagerWeakReference;

    public CityLocationListener(Foursquare foursquare, SharedPreferences prefs) {
        mFoursquare = foursquare;
        mPrefs = prefs;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (DEBUG) Log.d(TAG, "onLocationChanged: " + location);
        try {
            City city = Preferences.switchCity(mFoursquare, location);
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

    public void register(LocationManager locationManager) {
        this.register(locationManager, CityLocationListener.LOCATION_UPDATE_MIN_TIME,
                CityLocationListener.LOCATION_UPDATE_MIN_DISTANCE);
    }

    public void register(LocationManager locationManager, long updateMinTime, long updateMinDistance) {
        if (DEBUG) Log.d(TAG, "Registering this location listener: " + this.toString());
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        List<String> providers = locationManager.getProviders(criteria, true);
        int providersCount = providers.size();
        for (int i = 0; i < providersCount; i++) {
            String providerName = providers.get(i);
            locationManager.requestLocationUpdates(providerName, updateMinTime, updateMinDistance,
                    this);
        }
        mLocationManagerWeakReference = new WeakReference<LocationManager>(locationManager);
    }

    public void unregister() {
        LocationManager locationManager = mLocationManagerWeakReference.get();
        if (locationManager != null) {
            if (DEBUG) Log.d(TAG, "Unregistering this location listener: " + this.toString());
            locationManager.removeUpdates(this);
        }
    }
}
