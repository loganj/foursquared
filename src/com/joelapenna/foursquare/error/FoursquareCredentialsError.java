/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.error;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquareCredentialsError extends FoursquareException {
    private static final long serialVersionUID = 1L;

    public FoursquareCredentialsError(String message) {
        super(message);
    }

}
