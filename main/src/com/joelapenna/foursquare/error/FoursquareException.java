/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.error;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquareException extends Exception {
    private static final long serialVersionUID = 1L;
    
    private String mExtra;

    public FoursquareException(String message) {
        super(message);
    }

    public FoursquareException(String message, String extra) {
        super(message);
        mExtra = extra;
    }
    
    public String getExtra() {
        return mExtra;
    }
}
