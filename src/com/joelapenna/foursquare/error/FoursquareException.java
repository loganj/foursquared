/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.error;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquareException extends Exception {
    private static final long serialVersionUID = 1L;

    public FoursquareException(String message) {
        super(message);
    }

}
