/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Venue;

import android.text.TextUtils;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class StringFormatters {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "EEE, dd MMM yy HH:mm:ss Z");

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
        String venueName = (checkin.getVenue() != null) ? ". @ " + checkin.getVenue().getName()
                : ".";
        String name = checkin.getUser().getFirstname() + " "
                + checkin.getUser().getLastname().substring(0, 1);
        return name + venueName;
    }

    /**
     * @param created
     * @return
     */
    public static CharSequence getRelativeTimeSpanString(String created) {
        try {
            return DateUtils.getRelativeTimeSpanString(DATE_FORMAT.parse(created).getTime());
        } catch (ParseException e) {
            return created;
        }
    }

}
