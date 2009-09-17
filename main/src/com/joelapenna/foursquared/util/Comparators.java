/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Venue;

import java.text.ParseException;
import java.util.Comparator;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Comparators {

    private static Comparator<Venue> sVenueDistanceComparator = null;
    private static Comparator<User> sUserRecencyComparator = null;
    private static Comparator<Checkin> sCheckinRecencyComparator = null;
    private static Comparator<Tip> sTipRecencyComparator = null;

    public static Comparator<Venue> getVenueDistanceComparator() {
        if (sVenueDistanceComparator == null) {
            sVenueDistanceComparator = new Comparator<Venue>() {
                /*
                 * (non-Javadoc)
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                @Override
                public int compare(Venue object1, Venue object2) {
                    return object1.getDistance().compareTo(object2.getDistance());
                }
            };
        }
        return sVenueDistanceComparator;
    }

    public static Comparator<User> getUserRecencyComparator() {
        if (sUserRecencyComparator == null) {
            sUserRecencyComparator = new Comparator<User>() {
                /*
                 * (non-Javadoc)
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                @Override
                public int compare(User object1, User object2) {
                    try {
                        return StringFormatters.DATE_FORMAT.parse(object2.getCreated()).compareTo(
                                StringFormatters.DATE_FORMAT.parse(object1.getCreated()));
                    } catch (ParseException e) {
                        return 0;
                    }
                }
            };
        }
        return sUserRecencyComparator;
    }

    public static Comparator<Checkin> getCheckinRecencyComparator() {
        if (sCheckinRecencyComparator == null) {
            sCheckinRecencyComparator = new Comparator<Checkin>() {
                /*
                 * (non-Javadoc)
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                @Override
                public int compare(Checkin object1, Checkin object2) {
                    try {
                        return StringFormatters.DATE_FORMAT.parse(object2.getCreated()).compareTo(
                                StringFormatters.DATE_FORMAT.parse(object1.getCreated()));
                    } catch (ParseException e) {
                        return 0;
                    }
                }
            };
        }
        return sCheckinRecencyComparator;
    }

    public static Comparator<Tip> getTipRecencyComparator() {
        if (sTipRecencyComparator == null) {
            sTipRecencyComparator = new Comparator<Tip>() {
                /*
                 * (non-Javadoc)
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                @Override
                public int compare(Tip object1, Tip object2) {
                    try {
                        return StringFormatters.DATE_FORMAT.parse(object2.getCreated()).compareTo(
                                StringFormatters.DATE_FORMAT.parse(object1.getCreated()));
                    } catch (ParseException e) {
                        return 0;
                    }
                }
            };
        }
        return sTipRecencyComparator;
    }

}
