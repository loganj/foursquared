/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquare.types;

/**
 * @date April 28, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class Response implements FoursquareType {

    private String mValue;

    public Response() {
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }
}
