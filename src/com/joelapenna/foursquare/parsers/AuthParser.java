/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.types.Auth;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class AuthParser extends AbstractParser<Auth> {
    private static final String TAG = "AuthParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    @Override
    public Auth parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError {
        Auth auth = new Auth();
        int eventType = parser.nextToken();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

                    String name = parser.getName();
                    if ("error".equals(name)) {
                        throw new FoursquareError(parser.getText());
                    } else if ("auth".equals(name)) {
                        parseAuthTag(parser, auth);
                        break;
                    }

                default:
                    if (DEBUG) Log.d(TAG, "Unhandled Event");
            }
            eventType = parser.nextToken();
        }
        return auth;
    }

    public void parseAuthTag(XmlPullParser parser, Auth auth) throws XmlPullParserException,
            IOException {
        assert parser.getName() == "auth";
        if (DEBUG) Log.d(TAG, "parsing auth stanza");

        while (parser.nextTag() != XmlPullParser.END_TAG) {
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
            }
        }
    }
}
