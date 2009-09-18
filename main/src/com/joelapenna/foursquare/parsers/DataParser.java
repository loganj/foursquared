/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Data;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-09-17 19:58:36.757346
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class DataParser extends AbstractParser<Data> {
    private static final String TAG = "DataParser";
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public Data parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, "data");

        Data data = new Data();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("cityid".equals(name)) {
                data.setCityid(parser.nextText());

            } else if ("message".equals(name)) {
                data.setMessage(parser.nextText());

            } else if ("status".equals(name)) {
                data.setStatus(Boolean.valueOf(parser.nextText()));

            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return data;
    }
}
