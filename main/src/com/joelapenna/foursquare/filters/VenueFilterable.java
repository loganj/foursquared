/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.filters;

import com.joelapenna.foursquare.types.FoursquareType;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public interface VenueFilterable extends FoursquareType {
    public abstract String getVenueid();
}
