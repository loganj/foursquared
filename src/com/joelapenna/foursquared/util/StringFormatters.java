/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;


import com.joelapenna.foursquare.types.Venue;

import android.text.TextUtils;

public class StringFormatters {

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

}
