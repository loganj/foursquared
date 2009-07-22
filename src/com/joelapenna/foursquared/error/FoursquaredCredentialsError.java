/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.error;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquaredCredentialsError extends FoursquaredException {
    private static final long serialVersionUID = 1L;

    public FoursquaredCredentialsError(String message) {
        super(message);
    }

}
