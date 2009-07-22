/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Venue;

import android.content.Context;
import android.text.TextUtils;

public class StringFormatters {

    public static String getVenueLocationCrossStreetOrCity(Venue venue) {
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

    public static String getCheckinMessage(Checkin checkin) {
        String name = checkin.getUser().getFirstname() + " "
                + checkin.getUser().getLastname().substring(0, 1);
        return name + ". @" + checkin.getVenue().getName();
    }

    /**
     * @param created
     * @return
     */
    public static CharSequence getRelativeDate(Context context, String created) {
        // TODO(jlapenna): Write a formatter given: Mon, 08 Jun 09 01:53:09 +0000
        // or a different format if that one turns out to be nonsense.
        return created;
    }

}
