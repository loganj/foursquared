/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare.error;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquareError extends FoursquareException {
    private static final long serialVersionUID = 1L;

    public FoursquareError(String message) {
        super(message);
    }

}
