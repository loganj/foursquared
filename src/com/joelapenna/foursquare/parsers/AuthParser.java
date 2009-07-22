/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Auth;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-06-02 23:02:35.474137
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class AuthParser extends AbstractParser<Auth> {
    private static final String TAG = "AuthParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    @Override
    public Auth parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, "auth");

        Auth auth = new Auth();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("email".equals(name)) {
                auth.setEmail(parser.nextText());

            } else if ("firstname".equals(name)) {
                auth.setFirstname(parser.nextText());

            } else if ("id".equals(name)) {
                auth.setId(parser.nextText());

            } else if ("lastname".equals(name)) {
                auth.setLastname(parser.nextText());

            } else if ("message".equals(name)) {
                auth.setMessage(parser.nextText());

            } else if ("phone".equals(name)) {
                auth.setPhone(parser.nextText());

            } else if ("photo".equals(name)) {
                auth.setPhoto(parser.nextText());

            } else if ("status".equals(name)) {
                auth.setStatus(parser.nextText().equals("1"));
            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return auth;
    }
}
