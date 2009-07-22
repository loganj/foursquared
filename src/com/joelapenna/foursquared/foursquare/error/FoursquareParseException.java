/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare.error;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquareParseException extends FoursquareException {
    private static final long serialVersionUID = 1L;

    public FoursquareParseException(String message) {
        super(message);
    }
}
