/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.CheckinResult;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Auto-generated: 2009-12-06 10:51:54.301896
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class CheckinResultParser extends AbstractParser<CheckinResult> {
    private static final Logger LOG = Logger.getLogger(CheckinResultParser.class.getCanonicalName());
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public CheckinResult parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, null);

        CheckinResult checkin_result = new CheckinResult();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) LOG.log(Level.FINE, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("badges".equals(name)) {
                checkin_result.setBadges(new GroupParser(new BadgeParser()).parse(parser));

            } else if ("created".equals(name)) {
                checkin_result.setCreated(parser.nextText());

            } else if ("id".equals(name)) {
                checkin_result.setId(parser.nextText());

            } else if ("mayor".equals(name)) {
                checkin_result.setMayor(new MayorParser().parse(parser));

            } else if ("message".equals(name)) {
                checkin_result.setMessage(parser.nextText());

            } else if ("scoring".equals(name)) {
                checkin_result.setScoring(new GroupParser(new ScoreParser()).parse(parser));

            } else if ("specials".equals(name)) {
                checkin_result.setSpecials(new GroupParser(new SpecialParser()).parse(parser));

            } else if ("venue".equals(name)) {
                checkin_result.setVenue(new VenueParser().parse(parser));

            } else {
                // Consume something we don't understand.
                if (DEBUG) LOG.log(Level.FINE, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return checkin_result;
    }
}
