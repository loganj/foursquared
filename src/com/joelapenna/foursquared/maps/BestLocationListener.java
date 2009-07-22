
package com.joelapenna.foursquared.maps;

import com.joelapenna.foursquared.Foursquared;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.util.Date;

public abstract class BestLocationListener implements LocationListener {
    public static final String TAG = "BestLocationListener";
    public static final boolean DEBUG = Foursquared.DEBUG;

    public static final long LOCATION_UPDATE_MIN_TIME = 1000 * 60;
    public static final long LOCATION_UPDATE_MIN_DISTANCE = 100;
    static final long MAX_LOCATION_UPDATE_DELTA_THRESHOLD = 1000 * 60 * 5;

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

    public Location getBetterLocation(Location location) {
        if (mLastLocation == null) {
            mLastLocation = location;
            return location;
        }
        long now = new Date().getTime();
        long locationUpdateDelta = now - location.getTime();
        long lastLocationUpdateDelta = now - mLastLocation.getTime();

        boolean locationIsMostRecent = locationUpdateDelta <= lastLocationUpdateDelta;

        boolean accuracyComparable = location.hasAccuracy() && mLastLocation.hasAccuracy();
        boolean locationIsMoreAccurate = location.getAccuracy() <= mLastLocation.getAccuracy();

        boolean locationIsInTimeThreshold = locationUpdateDelta <= MAX_LOCATION_UPDATE_DELTA_THRESHOLD;
        boolean lastLocationIsInTimeThreshold = lastLocationUpdateDelta <= MAX_LOCATION_UPDATE_DELTA_THRESHOLD;

        if (accuracyComparable && locationIsMoreAccurate && locationIsMostRecent) {
            if (DEBUG) Log.d(TAG, "New +Accuracy, +Time, using new: " + location);
            mLastLocation = location;
            return location;
        } else if (accuracyComparable && locationIsMoreAccurate && !locationIsInTimeThreshold) {
            if (DEBUG) Log.d(TAG, "New +Accuracy, -Time. Using old:" + mLastLocation);
            return null;
        } else if (accuracyComparable && !locationIsMoreAccurate && !locationIsInTimeThreshold) {
            if (DEBUG) Log.d(TAG, "New -Accuracy -Time. Using old: " + mLastLocation);
            return null;
        } else if (locationIsMostRecent) {
            if (DEBUG) Log.d(TAG, "New ?Accuracy, +Time. Using new: " + location);
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

}
