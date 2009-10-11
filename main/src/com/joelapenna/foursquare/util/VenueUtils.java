/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.util;

import com.joelapenna.foursquare.types.Venue;

import android.text.TextUtils;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueUtils {

    public static final boolean isValid(Venue venue) {
        return (venue != null && !TextUtils.isEmpty(venue.getId()));
    }

    public static final boolean hasValidLocation(Venue venue) {
        boolean valid = false;
        if (venue != null) {
            String geoLat = venue.getGeolat();
            String geoLong = venue.getGeolong();
            if (!TextUtils.isEmpty(geoLat) && !TextUtils.isEmpty(geoLong)) {
                if (geoLat != "0" || geoLong != "0") {
                    valid = true;
                }
            }
        }
        return valid;
    }
}
