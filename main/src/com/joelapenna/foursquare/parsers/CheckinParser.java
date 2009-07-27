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

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-07-26 20:59:17.430473
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class CheckinParser extends AbstractParser<Checkin> {
    private static final String TAG = "CheckinParser";
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public Checkin parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        try {
            parser.require(XmlPullParser.START_TAG, null, "checkin");
        } catch (XmlPullParserException e) {
            if (parser.getName().equals("error")) {
                throw new FoursquareError(parser.getText());
            }
        }

        Checkin checkin = new Checkin();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("created".equals(name)) {
                checkin.setCreated(parser.nextText());

            } else if ("id".equals(name)) {
                checkin.setId(parser.nextText());

            } else if ("shout".equals(name)) {
                checkin.setShout(parser.nextText());

            } else if ("user".equals(name)) {
                checkin.setUser(new UserParser().parse(parser));

            } else if ("venue".equals(name)) {
                checkin.setVenue(new VenueParser().parse(parser));

            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return checkin;
    }
}
