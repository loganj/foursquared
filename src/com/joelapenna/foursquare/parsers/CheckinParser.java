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
 * Auto-generated: 2009-06-09 23:09:39.038041
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class CheckinParser extends AbstractParser<Checkin> {
    private static final String TAG = "CheckinParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    @Override
    public Checkin parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, "checkin");

        Checkin checkin = new Checkin();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("created".equals(name)) {
                checkin.setCreated(parser.nextText());

            } else if ("id".equals(name)) {
                checkin.setId(parser.nextText());

            } else if ("message".equals(name)) {
                checkin.setMessage(parser.nextText());

            } else if ("user".equals(name)) {
                checkin.setUser(parser.nextText());

            } else if ("venue".equals(name)) {
                checkin.setVenue(parser.nextText());

            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return checkin;
    }
}
