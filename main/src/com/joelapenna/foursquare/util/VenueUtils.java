/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.util;

import com.joelapenna.foursquare.types.Venue;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueUtils {

    public static final boolean isValid(Venue venue) {
        return !(venue == null || venue.getId() == null || venue.getId().length() == 0);
    }

    public static final boolean hasValidLocation(Venue venue) {
        boolean valid = false;
        if (venue != null) {
            String geoLat = venue.getGeolat();
            String geoLong = venue.getGeolong();
            if (!(geoLat == null || geoLat.length() == 0 || geoLong == null || geoLong.length() == 0)) {
                if (geoLat != "0" || geoLong != "0") {
                    valid = true;
                }
            }
        }
        return valid;
    }
}
