/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Rank;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-07-26 20:59:18.118130
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class RankParser extends AbstractParser<Rank> {
    private static final String TAG = "RankParser";
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public Rank parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        try {
            parser.require(XmlPullParser.START_TAG, null, "rank");
        } catch (XmlPullParserException e) {
            if (parser.getName().equals("error")) {
                throw new FoursquareError(parser.getText());
            }
        }

        Rank rank = new Rank();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("city".equals(name)) {
                rank.setCity(parser.nextText());

            } else if ("message".equals(name)) {
                rank.setMessage(parser.nextText());

            } else if ("position".equals(name)) {
                rank.setPosition(parser.nextText());

            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return rank;
    }
}
