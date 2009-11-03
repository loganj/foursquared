
package com.joelapenna.foursquared.location;

import com.joelapenna.foursquared.FoursquaredSettings;

import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

abstract public class CityLocationListener implements LocationListener {
    private static final String TAG = "CityLocationListener";
    private static final boolean DEBUG = FoursquaredSettings.LOCATION_DEBUG;

    public static final long LOCATION_UPDATE_MIN_TIME = 1000 * 60 * 60; // Every hour
    public static final long LOCATION_UPDATE_MIN_DISTANCE = 1000 * 10; // 10 kilometers

    public CityLocationListener() {
        super();
    }

    @Override
    public void onProviderDisabled(String provider) {
        // do nothing.
    }

    @Override
    public void onProviderEnabled(String provider) {
        // do nothing.
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // do nothing.
    }

    public void register(LocationManager locationManager) {
        this.register(locationManager, LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE);
    }

    public void register(LocationManager locationManager, long updateMinTime, long updateMinDistance) {
        if (DEBUG) Log.d(TAG, "Registering this location listener: " + this.toString());

        // Lets only listen on coarse, cheap location providers.
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
    }

    public void unregister(LocationManager locationManager) {
        if (DEBUG) Log.d(TAG, "Unregistering this location listener: " + this.toString());
        locationManager.removeUpdates(this);
    }
}
