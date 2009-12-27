/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.City;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Auto-generated: 2009-11-13 21:59:24.038649
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class CityParser extends AbstractParser<City> {
    private static final Logger LOG = Logger.getLogger(CityParser.class.getCanonicalName());
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public City parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, null);

        City city = new City();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            String name = parser.getName();
            if ("geolat".equals(name)) {
                city.setGeolat(parser.nextText());

            } else if ("geolong".equals(name)) {
                city.setGeolong(parser.nextText());

            } else if ("id".equals(name)) {
                city.setId(parser.nextText());

            } else if ("name".equals(name)) {
                city.setName(parser.nextText());

            } else if ("shortname".equals(name)) {
                city.setShortname(parser.nextText());

            } else if ("timezone".equals(name)) {
                city.setTimezone(parser.nextText());

            } else if ("cityid".equals(name)) {
                city.setId(parser.nextText());

            } else {
                // Consume something we don't understand.
                if (DEBUG) LOG.log(Level.FINE, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return city;
    }
}
