/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Credentials;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-06-01 21:12:40.010308
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class CredentialsParser extends AbstractParser<Credentials> {
    private static final String TAG = "CredentialsParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    @Override
    public Credentials parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        Credentials credentials = new Credentials();
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

                    String name = parser.getName();
                    if ("error".equals(name)) {
                        throw new FoursquareError(parser.getText());
                    } else if ("credentials".equals(name)) {
                        parseCredentialsTag(parser, credentials);
                        return credentials;
                    }
                    break;

                default:
                    if (DEBUG) Log.d(TAG, "Unhandled Event");
            }
            eventType = parser.nextToken();
        }
        return null;
    }

    public void parseCredentialsTag(XmlPullParser parser, Credentials credentials)
            throws XmlPullParserException, IOException, FoursquareError, FoursquareParseException {
        assert parser.getName() == "credentials";
        if (DEBUG) Log.d(TAG, "parsing credentials stanza");

        while (parser.nextTag() != XmlPullParser.END_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("oauth_token".equals(name)) {
                credentials.setOauthToken(parser.nextText());

            } else if ("oauth_token_secret".equals(name)) {
                credentials.setOauthTokenSecret(parser.nextText());
            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                parser.nextText();
            }
        }
        parser.nextToken();
    }
}
