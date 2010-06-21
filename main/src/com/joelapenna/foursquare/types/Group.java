/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Group<T extends FoursquareType> extends ArrayList<T> implements FoursquareType {

    private static final long serialVersionUID = 1L;

    private String mType;
    
    public Group() {
        super();
    }
    
    public Group(Collection<T> collection) {
        super(collection);
    }

    public void setType(String type) {
        mType = type;
    }

    public String getType() {
        return mType;
    }
}
