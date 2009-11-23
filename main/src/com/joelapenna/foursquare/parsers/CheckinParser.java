/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Checkin;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Auto-generated: 2009-11-22 20:21:34.387137
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class CheckinParser extends AbstractParser<Checkin> {
    private static final Logger LOG = Logger.getLogger(CheckinParser.class.getCanonicalName());
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public Checkin parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, null);

        Checkin checkin = new Checkin();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) LOG.log(Level.FINE, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("created".equals(name)) {
                checkin.setCreated(parser.nextText());

            } else if ("display".equals(name)) {
                checkin.setDisplay(parser.nextText());

            } else if ("id".equals(name)) {
                checkin.setId(parser.nextText());

            } else if ("ismayor".equals(name)) {
                checkin.setIsmayor(Boolean.valueOf(parser.nextText()));

            } else if ("shout".equals(name)) {
                checkin.setShout(parser.nextText());

            } else if ("user".equals(name)) {
                checkin.setUser(new UserParser().parse(parser));

            } else if ("venue".equals(name)) {
                checkin.setVenue(new VenueParser().parse(parser));

            } else {
                // Consume something we don't understand.
                if (DEBUG) LOG.log(Level.FINE, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return checkin;
    }
}
