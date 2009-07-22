/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.types.Data;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class DataParser extends AbstractParser<Data> {
    private static final String TAG = "DataParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    @Override
    public Data parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError {
        Data data = new Data();
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

                    String name = parser.getName();
                    if ("error".equals(name)) {
                        throw new FoursquareError(parser.getText());
                    } else if ("data".equals(name)) {
                        parseDataTag(parser, data);
                        return data;
                    }

                default:
                    if (DEBUG) Log.d(TAG, "Unhandled Event");
            }
            eventType = parser.nextToken();
        }
        return null;
    }

    public void parseDataTag(XmlPullParser parser, Data data) throws XmlPullParserException,
            IOException {
        assert parser.getName() == "data";
        if (DEBUG) Log.d(TAG, "parsing data stanza");

        while (parser.nextTag() != XmlPullParser.END_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("message".equals(name)) {
                data.setMessage(parser.nextText());

            } else if ("status".equals(name)) {
                data.setStatus(parser.nextText().equals("1"));
            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                parser.nextText();
            }
        }
        parser.nextToken();
    }
}

