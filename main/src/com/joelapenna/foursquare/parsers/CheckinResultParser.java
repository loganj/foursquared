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

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-08-01 10:39:23.426578
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class CheckinResultParser extends AbstractParser<CheckinResult> {
    private static final String TAG = "CheckinResultParser";
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public CheckinResult parseInner(XmlPullParser parser) throws XmlPullParserException,
            IOException, FoursquareError, FoursquareParseException {
        try {
            parser.require(XmlPullParser.START_TAG, null, "checkin");
        } catch (XmlPullParserException e) {
            if (parser.getName().equals("error")) {
                throw new FoursquareError(parser.getText());
            }
        }

        CheckinResult checkin_result = new CheckinResult();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

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
                checkin_result.setScoring(new ScoringParser().parse(parser));

            } else if ("venue".equals(name)) {
                checkin_result.setVenue(new VenueParser().parse(parser));

            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return checkin_result;
    }
}
