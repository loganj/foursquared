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
 * @author Mark Wyszomierski (markww@gmail.com)
 *   -Updated getVenueDistanceComparator() to do numeric comparison. (2010-03-23)
 */
public class Comparators {

    private static Comparator<Venue> sVenueDistanceComparator = null;
    private static Comparator<User> sUserRecencyComparator = null;
    private static Comparator<Checkin> sCheckinRecencyComparator = null;
    private static Comparator<Checkin> sCheckinDistanceComparator = null;
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
                    // TODO: In practice we're pretty much guaranteed to get valid locations
                    // from foursquare, but.. what if we don't, what's a good fail behavior
                    // here?
                    try {
                        int d1 = Integer.parseInt(object1.getDistance());
                        int d2 = Integer.parseInt(object2.getDistance());
                        
                        if (d1 < d2) {
                            return -1;
                        } else if (d1 > d2) {
                            return 1;
                        } else {
                            return 0;
                        }
                    } catch (NumberFormatException ex) {
                        return object1.getDistance().compareTo(object2.getDistance());
                    }
                }
            };
        }
        return sVenueDistanceComparator;
    }

    public static Comparator<Venue> getVenueNameComparator() {
        if (sVenueDistanceComparator == null) {
            sVenueDistanceComparator = new Comparator<Venue>() {
                /*
                 * (non-Javadoc)
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                @Override
                public int compare(Venue object1, Venue object2) {
                    return object1.getName().toLowerCase().compareTo(
                            object2.getName().toLowerCase());
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
    
    public static Comparator<Checkin> getCheckinDistanceComparator() {
        if (sCheckinDistanceComparator == null) {
            sCheckinDistanceComparator = new Comparator<Checkin>() {
                /*
                 * (non-Javadoc)
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                @Override
                public int compare(Checkin object1, Checkin object2) {
                    try {
                        int d1 = Integer.parseInt(object1.getDistance());
                        int d2 = Integer.parseInt(object2.getDistance());
                        if (d1 > d2) {
                            return 1;
                        } else if (d2 > d1) {
                            return -1;
                        } else {
                            return 0;
                        }
                    } catch (NumberFormatException ex) {
                        return 0;
                    }
                }
            };
        }
        return sCheckinDistanceComparator;
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
