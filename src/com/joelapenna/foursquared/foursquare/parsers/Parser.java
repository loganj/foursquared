/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare.parsers;

import com.joelapenna.foursquared.foursquare.error.FoursquareError;
import com.joelapenna.foursquared.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquared.foursquare.types.FoursquareType;

import java.io.InputStream;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public interface Parser<T extends FoursquareType> {

    public abstract T parse(InputStream is) throws FoursquareError, FoursquareParseException;

}
