/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Tip;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-06-10 02:19:22.852979
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class TipParser extends AbstractParser<Tip> {
    private static final String TAG = "TipParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    @Override
    public Tip parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, "tip");

        Tip tip = new Tip();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("created".equals(name)) {
                tip.setCreated(parser.nextText());

            } else if ("distance".equals(name)) {
                tip.setDistance(parser.nextText());

            } else if ("id".equals(name)) {
                tip.setId(parser.nextText());

            } else if ("text".equals(name)) {
                tip.setText(parser.nextText());

            } else if ("user".equals(name)) {
                tip.setUser(new UserParser().parse(parser));

            } else if ("venue".equals(name)) {
                tip.setVenue(new VenueParser().parse(parser));

            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return tip;
    }
}
