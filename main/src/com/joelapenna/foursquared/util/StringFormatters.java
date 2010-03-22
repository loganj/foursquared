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

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com)
 *   -Added date formats for today/yesterday/older contexts.
 */
public class StringFormatters {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "EEE, dd MMM yy HH:mm:ss Z");
    
    /** Should look like "9:09 AM". */
    public static final SimpleDateFormat DATE_FORMAT_TODAY = new SimpleDateFormat(
            "h:mm a");

    /** Should look like "Sun 1:56 PM". */
    public static final SimpleDateFormat DATE_FORMAT_YESTERDAY = new SimpleDateFormat(
            "E h:mm a");

    /** Should look like "Sat Mar 20". */
    public static final SimpleDateFormat DATE_FORMAT_OLDER = new SimpleDateFormat(
            "E MMM d");
    

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
        if (checkin.getDisplay() != null) {
            return checkin.getDisplay();

        } else {
            StringBuffer sb = new StringBuffer();
            sb.append(getUserAbbreviatedName(checkin.getUser()));
            if (checkin.getVenue() != null && displayAtVenue) {
                sb.append(" @ " + checkin.getVenue().getName());
            }
            return sb.toString();
        }
    }

    public static String getUserFullName(User user) {
        StringBuffer sb = new StringBuffer();
        sb.append(user.getFirstname());

        String lastName = user.getLastname();
        if (lastName != null && lastName.length() > 0) {
            sb.append(" ");
            sb.append(lastName);
        }
        return sb.toString();
    }
    
    public static String getUserAbbreviatedName(User user) {
        StringBuffer sb = new StringBuffer();
        sb.append(user.getFirstname());

        String lastName = user.getLastname();
        if (lastName != null && lastName.length() > 0) {
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

    /**
     * Returns a format that will look like: "9:09 AM".
     */
    public static String getTodayTimeString(String created) {
        return DATE_FORMAT_TODAY.format(new Date(created));
    }
    
    /**
     * Returns a format that will look like: "Sun 1:56 PM".
     */
    public static String getYesterdayTimeString(String created) {
        return DATE_FORMAT_YESTERDAY.format(new Date(created));
    }
    
    /**
     * Returns a format that will look like: "Sat Mar 20".
     */
    public static String getOlderTimeString(String created) {
        return DATE_FORMAT_OLDER.format(new Date(created));
    }
}
