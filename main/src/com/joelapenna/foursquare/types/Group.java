/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import java.util.ArrayList;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Group<T extends FoursquareType> extends ArrayList<T> implements FoursquareType {

    private static final long serialVersionUID = 1L;

    private String mType;

    public void setType(String type) {
        mType = type;
    }

    public String getType() {
        return mType;
    }
}
