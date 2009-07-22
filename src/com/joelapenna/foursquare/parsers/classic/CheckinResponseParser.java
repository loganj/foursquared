/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers.classic;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.parsers.AbstractParser;
import com.joelapenna.foursquare.types.classic.Checkin;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class CheckinResponseParser extends AbstractParser<Checkin> {
    private static final String TAG = "CheckinResponseParser";
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public Checkin parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, "checkin");

        Checkin checkin = new Checkin();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

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

            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return checkin;
    }
}
