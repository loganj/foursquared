/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.error;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
class FoursquaredException extends Exception {
    private static final long serialVersionUID = 1L;

    public FoursquaredException(String message) {
        super(message);
    }

}
