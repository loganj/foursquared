/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Badge;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Auto-generated: 2009-11-13 21:59:24.829338
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class BadgeParser extends AbstractParser<Badge> {
    private static final Logger LOG = Logger.getLogger(BadgeParser.class.getCanonicalName());
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public Badge parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, null);

        Badge badge = new Badge();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) LOG.log(Level.FINE, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("description".equals(name)) {
                badge.setDescription(parser.nextText());

            } else if ("icon".equals(name)) {
                badge.setIcon(parser.nextText());

            } else if ("id".equals(name)) {
                badge.setId(parser.nextText());

            } else if ("name".equals(name)) {
                badge.setName(parser.nextText());

            } else {
                // Consume something we don't understand.
                if (DEBUG) LOG.log(Level.FINE, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return badge;
    }
}
