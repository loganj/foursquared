/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Venue;

import android.text.TextUtils;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringFormatters {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "EEE, dd MMM yy HH:mm:ss Z");

    public static String getVenueLocationCrossStreetOrCity(Venue venue) {
        if (!TextUtils.isEmpty(venue.getCrossstreet())) {
            return "(" + venue.getCrossstreet() + ")";
        } else if (!TextUtils.isEmpty(venue.getCity()) && !TextUtils.isEmpty(venue.getState())
                && !TextUtils.isEmpty(venue.getZip())) {
            return venue.getCity() + ", " + venue.getState() + " " + venue.getZip();
        } else {
            return null;
        }
    }

    public static String getCheckinMessage(Checkin checkin, boolean displayAtVenue) {
        StringBuffer sb = new StringBuffer();
        sb.append(getUserAbbreviatedName(checkin.getUser()));
        if (checkin.getVenue() != null && displayAtVenue) {
            sb.append(" @ " + checkin.getVenue().getName());
        }
        return sb.toString();
    }

    public static String getUserAbbreviatedName(User user) {
        StringBuffer sb = new StringBuffer();
        sb.append(user.getFirstname());

        String lastName = user.getLastname();
        if (lastName.length() > 0) {
            sb.append(" ");
            sb.append(lastName.substring(0, 1) + ".");
        }
        return sb.toString();
    }

    public static CharSequence getRelativeTimeSpanString(String created) {
        try {
            return DateUtils.getRelativeTimeSpanString(DATE_FORMAT.parse(created).getTime(),
                    new Date().getTime(), DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE);
        } catch (ParseException e) {
            return created;
        }
    }

}
