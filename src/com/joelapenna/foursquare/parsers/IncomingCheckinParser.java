/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.types.Checkin;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class IncomingCheckinParser extends AbstractParser<Checkin> {
    private static final String TAG = "CheckinResponseParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    @Override
    public Checkin parseInner(XmlPullParser parser) throws XmlPullParserException,
            IOException, FoursquareError {
        Checkin checkin = new Checkin();
        int eventType = parser.nextToken();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));
                    String name = parser.getName();

                    if ("error".equals(name)) {
                        throw new FoursquareError(parser.getText());
                    } else if ("checkin".equals(name)) {
                        parseCheckinTag(parser, checkin);
                    }

                default:
                    if (DEBUG) Log.d(TAG, "Unhandled Event");
            }
            eventType = parser.nextToken();
        }
        return checkin;
    }

    public void parseCheckinTag(XmlPullParser parser, Checkin checkin)
            throws XmlPullParserException, IOException {
        assert parser.getName() == "checkin";
        if (DEBUG) Log.d(TAG, "parse()ing checkin stanza");

        while (parser.nextTag() != XmlPullParser.END_TAG) {
            String name = parser.getName();
            if ("userid".equals(name)) {
                checkin.setUserid(parser.nextText());
            } else if ("message".equals(name)) {
                checkin.setMessage(parser.nextText());
            } else if ("status".equals(name)) {
                checkin.setStatus(parser.nextText().equals("1") ? true : false);
            } else if ("url".equals(name)) {
                // Looks like an escapld string, with & = &amp;
                // http://playfoursquare.com/incoming/breakdown?cid=67889&uid=9232&client=iphone
                String urlString = parser.nextText();
                checkin.setUrl(urlString);
                checkin.setCheckinid(Uri.parse(urlString).getQueryParameter("cid"));
            }
        }
    }
}
