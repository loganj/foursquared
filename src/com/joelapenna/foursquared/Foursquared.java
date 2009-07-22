/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Venue;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Foursquared {
    public static final String TAG = "Foursquared";
    public static final boolean DEBUG = true;


    public static Venue createTestVenue() {
        Venue venue = new Venue();
        venue.setVenuename("The Page");
        venue.setAddress("298 Divisadero St.");
        venue.setCity("San Francisco");
        venue.setState("CA");
        venue.setZip("94117");
        venue.setCrossstreet("between Haight and Page");
        venue.setDistance("0.1m");
        venue.setNumCheckins("46");
        venue.setYelp("http://www.yelp.com/biz/the-page-san-francisco");
        venue.setGeolat("37.7722");
        venue.setGeolong("-122.437");
        venue.setMap("http://maps.google.com/staticmap?zoom=15&amp;size=280x100&amp;markers=37.7722,-122.437&amp;maptype=mobile");
        venue.setMapurl("http://maps.google.com/maps?q=+298+Divisadero+St%2CSan+Francisco%2CCA%2C94117");
        venue.setNewVenue(false);
        return venue;
    }
}
