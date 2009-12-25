/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.error;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class LocationException extends FoursquaredException {

    public LocationException() {
        super("Unable to determine your location.");
    }

    public LocationException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 1L;

}
