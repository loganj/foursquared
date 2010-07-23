/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.util;

import com.google.android.maps.GeoPoint;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import java.util.List;

/**
 * @date June 30, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class GeoUtils {

    /**
     * To be used if you just want a one-shot best last location, iterates over
     * all providers and returns the most accurate result.
     */
    public static Location getBestLastGeolocation(Context context) {
        LocationManager manager = (LocationManager)context.getSystemService(
                Context.LOCATION_SERVICE);
        List<String> providers = manager.getAllProviders();
         
        Location bestLocation = null;
        for (String it : providers) {
            Location location = manager.getLastKnownLocation(it);
            if (location != null) {
                if (bestLocation == null || 
                        location.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = location;
                }
            }
        }
        
        return bestLocation;
    }
    
    public static GeoPoint locationToGeoPoint(Location location) {
        if (location != null) {
            GeoPoint pt = new GeoPoint(
                (int)(location.getLatitude() * 1E6 + 0.5),
                (int)(location.getLongitude() * 1E6 + 0.5));
            return pt;
        } else {
            return null;
        }
    }
    
    public static GeoPoint stringLocationToGeoPoint(String strlat, String strlon) {
        try {
            double lat = Double.parseDouble(strlat);
            double lon = Double.parseDouble(strlon);
            GeoPoint pt = new GeoPoint(
                    (int)(lat * 1E6 + 0.5),
                    (int)(lon * 1E6 + 0.5));
            return pt;
        } catch (Exception ex) {
            return null;
        }
    }
}