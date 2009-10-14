
package com.joelapenna.foursquared.maps;

import com.joelapenna.foursquared.FoursquaredSettings;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

public class BestLocationListener implements LocationListener {
    private static final String TAG = "BestLocationListener";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final long LOCATION_UPDATE_MIN_TIME = 1000;
    public static final long LOCATION_UPDATE_MIN_DISTANCE = 100;

    private static final long LOCATION_UPDATE_MAX_DELTA_THRESHOLD = 1000 * 60 * 5;

    private WeakReference<LocationManager> mLocationManagerWeakReference;

    private Location mLastLocation;

    public BestLocationListener() {
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

    @Override
    public void onLocationChanged(Location location) {
        if (DEBUG) Log.d(TAG, "onLocationChanged: " + location);
        getBetterLocation(location);
    }

    public Location getLastKnownLocation() {
        return mLastLocation;
    }

    public Location getBetterLocation(Location location) {
        if (DEBUG) {
            Log.d(TAG, "getBetterLocation: Old: " + mLastLocation);
            Log.d(TAG, "getBetterLocation: New: " + location);
        }
        if (mLastLocation == null) {
            if (DEBUG) Log.d(TAG, "unknown last location, using new location");
            mLastLocation = location;
            return location;
        } else if (location == null) {
            if (DEBUG) Log.d(TAG, "updated location is unknown but returning new location.");
            return location;
        }
        long now = new Date().getTime();
        long locationUpdateDelta = now - location.getTime();
        long lastLocationUpdateDelta = now - mLastLocation.getTime();

        boolean locationIsMostRecent = locationUpdateDelta < lastLocationUpdateDelta;

        boolean accuracyComparable = location.hasAccuracy() && mLastLocation.hasAccuracy();
        boolean locationIsMoreAccurate = location.getAccuracy() < mLastLocation.getAccuracy();

        boolean locationIsInTimeThreshold = locationUpdateDelta <= LOCATION_UPDATE_MAX_DELTA_THRESHOLD;
        boolean lastLocationIsInTimeThreshold = lastLocationUpdateDelta <= LOCATION_UPDATE_MAX_DELTA_THRESHOLD;

        if (DEBUG) {
            Log.d(TAG, "locationUpdateDelta:\t\t\t" + locationUpdateDelta);
            Log.d(TAG, "lastLocationUpdateDelta:\t\t" + lastLocationUpdateDelta);
            Log.d(TAG, "locationIsMostRecent:\t\t\t" + locationIsMostRecent);
            Log.d(TAG, "accuracyComparable:\t\t\t" + accuracyComparable);
            Log.d(TAG, "locationIsMoreAccurate:\t\t" + locationIsMoreAccurate);
            Log.d(TAG, "locationIsInTimeThreshold:\t\t" + locationIsInTimeThreshold);
            Log.d(TAG, "lastLocationIsInTimeThreshold:\t" + lastLocationIsInTimeThreshold);
        }

        if (accuracyComparable && locationIsMoreAccurate && locationIsMostRecent) {
            if (DEBUG) Log.d(TAG, "+Accuracy, +Time, using new: " + location);
            mLastLocation = location;
            return location;

        } else if (accuracyComparable && locationIsMoreAccurate && !locationIsInTimeThreshold) {
            if (DEBUG) Log.d(TAG, "+Accuracy, -Time. Using old:" + mLastLocation);
            return null;

        } else if (accuracyComparable && !locationIsMoreAccurate && lastLocationIsInTimeThreshold) {
            if (DEBUG) Log.d(TAG, "-Accuracy -Time. Using old: " + mLastLocation);
            return null;

        } else if (locationIsMostRecent) {
            if (DEBUG) Log.d(TAG, "?Accuracy, +Time. Using new: " + location);
            mLastLocation = location;
            return location;

        } else if (!lastLocationIsInTimeThreshold) {
            if (DEBUG) Log.d(TAG, "Old location too old. Using new: " + location);
            mLastLocation = location;
            return location;

        } else {
            if (DEBUG) Log.d(TAG, "Poor comparitive data. Using old: " + mLastLocation);
            return null;
        }
    }

    public void register(LocationManager locationManager) {
        if (DEBUG) Log.d(TAG, "Registering this location listener: " + this.toString());
        List<String> providers = locationManager.getProviders(true);
        int providersCount = providers.size();
        for (int i = 0; i < providersCount; i++) {
            String providerName = providers.get(i);
            getBetterLocation(locationManager.getLastKnownLocation(providerName));
            locationManager.requestLocationUpdates(providerName,
                    BestLocationListener.LOCATION_UPDATE_MIN_TIME,
                    BestLocationListener.LOCATION_UPDATE_MIN_DISTANCE, this);
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
