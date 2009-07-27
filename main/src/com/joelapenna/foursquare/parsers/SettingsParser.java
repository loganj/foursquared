/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Settings;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-07-26 20:59:18.539846
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class SettingsParser extends AbstractParser<Settings> {
    private static final String TAG = "SettingsParser";
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public Settings parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        try {
            parser.require(XmlPullParser.START_TAG, null, "settings");
        } catch (XmlPullParserException e) {
            if (parser.getName().equals("error")) {
                throw new FoursquareError(parser.getText());
            }
        }

        Settings settings = new Settings();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("feeds_key".equals(name)) {
                settings.setFeedsKey(parser.nextText());

            } else if ("sendtotwitter".equals(name)) {
                settings.setSendtotwitter(Boolean.valueOf(parser.nextText()));

            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return settings;
    }
}
