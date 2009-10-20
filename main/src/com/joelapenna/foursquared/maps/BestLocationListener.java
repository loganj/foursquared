
package com.joelapenna.foursquared.maps;

import com.joelapenna.foursquared.FoursquaredSettings;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.Date;
import java.util.List;

public class BestLocationListener implements LocationListener {
    private static final String TAG = "BestLocationListener";
    private static final boolean DEBUG = FoursquaredSettings.LOCATION_DEBUG;

    public static final long LOCATION_UPDATE_MIN_TIME = 0;
    public static final long LOCATION_UPDATE_MIN_DISTANCE = 0;

    public static final long SLOW_LOCATION_UPDATE_MIN_TIME = 1000 * 60 * 5;
    public static final long SLOW_LOCATION_UPDATE_MIN_DISTANCE = 50;

    public static final float REQUESTED_FIRST_SEARCH_ACCURACY_IN_METERS = 100.0f;
    public static final int REQUESTED_FIRST_SEARCH_ACCURACY_IN_MS = 1000 * 60 * 5;

    private static final long LOCATION_UPDATE_MAX_DELTA_THRESHOLD = 1000 * 60 * 5;

    private Location mLastLocation;

    public BestLocationListener() {
        super();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (DEBUG) Log.d(TAG, "onLocationChanged: " + location);
        getBetterLocation(location);
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

    public void onBestLocationChanged(Location location) {
        mLastLocation = location;
    }

    public Location getLastKnownLocation() {
        return mLastLocation;
    }

    synchronized public void getBetterLocation(Location location) {
        if (DEBUG) {
            Log.d(TAG, "getBetterLocation: Old: " + mLastLocation);
            Log.d(TAG, "getBetterLocation: New: " + location);
        }

        // Cases where we only have one or the other.
        if (location != null && mLastLocation == null) {
            if (DEBUG) Log.d(TAG, "null last location, firing onBestLocationChanged");
            onBestLocationChanged(location);
            return;
        } else if (location == null) {
            if (DEBUG) Log.d(TAG, "updated location is null, doing nothing");
            return;
        }

        long now = new Date().getTime();
        long locationUpdateDelta = now - location.getTime();
        long lastLocationUpdateDelta = now - mLastLocation.getTime();
        boolean locationIsInTimeThreshold = locationUpdateDelta <= LOCATION_UPDATE_MAX_DELTA_THRESHOLD;
        boolean lastLocationIsInTimeThreshold = lastLocationUpdateDelta <= LOCATION_UPDATE_MAX_DELTA_THRESHOLD;

        boolean timeComparable = locationIsInTimeThreshold && lastLocationIsInTimeThreshold;
        boolean locationIsMostRecent = (locationUpdateDelta < lastLocationUpdateDelta);

        boolean accuracyComparable = location.hasAccuracy() || mLastLocation.hasAccuracy();
        boolean locationIsMostAccurate = false;
        if (accuracyComparable) {
            // If we have only one side of the accuracy, that one is more accurate.
            if (location.hasAccuracy() && !mLastLocation.hasAccuracy()) {
                locationIsMostAccurate = true;
            } else if (!location.hasAccuracy() && mLastLocation.hasAccuracy()) {
                locationIsMostAccurate = false;
            } else {
                // If we have both accuracies, do a real comparison.
                locationIsMostAccurate = location.getAccuracy() < mLastLocation.getAccuracy();
            }
        }

        if (DEBUG) {
            Log.d(TAG, "timeComparable:\t\t\t" + timeComparable);
            Log.d(TAG, "locationUpdateDelta:\t\t\t" + locationUpdateDelta);
            Log.d(TAG, "lastLocationUpdateDelta:\t\t" + lastLocationUpdateDelta);
            Log.d(TAG, "locationIsMostRecent:\t\t\t" + locationIsMostRecent);
            Log.d(TAG, "accuracyComparable:\t\t\t" + accuracyComparable);
            Log.d(TAG, "locationIsMoreAccurate:\t\t" + locationIsMostAccurate);
            Log.d(TAG, "locationIsInTimeThreshold:\t\t" + locationIsInTimeThreshold);
            Log.d(TAG, "lastLocationIsInTimeThreshold:\t" + lastLocationIsInTimeThreshold);
        }

        // Favor accuracy over time, unless times are too old.

        if (accuracyComparable && timeComparable) {
            if (locationIsMostAccurate && locationIsMostRecent) {
                if (DEBUG) Log.d(TAG, "+Accuracy, +Time, using new: " + location);
                onBestLocationChanged(location);

            } else if (locationIsMostAccurate && !locationIsMostRecent) {
                if (DEBUG) Log.d(TAG, "+Accuracy, -Time, using new: " + location);
                onBestLocationChanged(location);
            }

        } else if (accuracyComparable) {
            if (locationIsMostAccurate) {
                if (DEBUG) Log.d(TAG, "+Accuracy, +Time, using new: " + location);
                onBestLocationChanged(location);
            }
        }
    }

    public boolean isAccurateEnough(Location location) {
        if (location != null && location.hasAccuracy()
                && location.getAccuracy() <= REQUESTED_FIRST_SEARCH_ACCURACY_IN_METERS) {
            long locationUpdateDelta = new Date().getTime() - location.getTime();
            if (locationUpdateDelta < REQUESTED_FIRST_SEARCH_ACCURACY_IN_MS) {
                if (DEBUG) Log.d(TAG, "Location is accurate: " + location.toString());
                return true;
            }
        }
        if (DEBUG) Log.d(TAG, "Location is not accurate: " + String.valueOf(location));
        return false;
    }

    public void register(LocationManager locationManager) {
        this.register(locationManager, BestLocationListener.LOCATION_UPDATE_MIN_TIME,
                BestLocationListener.LOCATION_UPDATE_MIN_DISTANCE);
    }

    public void register(LocationManager locationManager, long updateMinTime, long updateMinDistance) {
        if (DEBUG) Log.d(TAG, "Registering this location listener: " + this.toString());
        List<String> providers = locationManager.getProviders(true);
        int providersCount = providers.size();
        for (int i = 0; i < providersCount; i++) {
            String providerName = providers.get(i);
            if (locationManager.isProviderEnabled(providerName)) {
                getBetterLocation(locationManager.getLastKnownLocation(providerName));
            }
            locationManager.requestLocationUpdates(providerName, updateMinTime, updateMinDistance,
                    this);
        }
    }

    public void unregister(LocationManager locationManager) {
        if (DEBUG) Log.d(TAG, "Unregistering this location listener: " + this.toString());
        locationManager.removeUpdates(this);
    }
}
