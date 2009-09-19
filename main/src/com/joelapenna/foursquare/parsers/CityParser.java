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

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-09-17 19:58:36.485488
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class CityParser extends AbstractParser<City> {
    private static final String TAG = "CityParser";
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public City parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, null);

        City city = new City();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

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

            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return city;
    }
}
