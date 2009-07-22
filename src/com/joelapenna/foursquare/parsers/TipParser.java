/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.types.Tip;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class TipParser extends AbstractParser<Tip> {
    private static final String TAG = "TipParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    @Override
    public Tip parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError {
        Tip tip = new Tip();
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

                    String name = parser.getName();
                    if ("error".equals(name)) {
                        throw new FoursquareError(parser.getText());
                    } else if ("tip".equals(name)) {
                        parseTipTag(parser, tip);
                        return tip;
                    }

                default:
                    if (DEBUG) Log.d(TAG, "Unhandled Event");
            }
            eventType = parser.nextToken();
        }
        return null;
    }

    public void parseTipTag(XmlPullParser parser, Tip tip) throws XmlPullParserException,
            IOException {
        assert parser.getName() == "tip";
        if (DEBUG) Log.d(TAG, "parsing tip stanza");

        while (parser.nextTag() != XmlPullParser.END_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("address".equals(name)) {
                tip.setAddress(parser.nextText());

            } else if ("creator_status".equals(name)) {
                tip.setCreatorStatus(parser.nextText());

            } else if ("crossstreet".equals(name)) {
                tip.setCrossstreet(parser.nextText());

            } else if ("date".equals(name)) {
                tip.setDate(parser.nextText());

            } else if ("distance".equals(name)) {
                tip.setDistance(parser.nextText());

            } else if ("firstname".equals(name)) {
                tip.setFirstname(parser.nextText());

            } else if ("istodoable".equals(name)) {
                tip.setIstodoable(parser.nextText().equals("1"));

            } else if ("lastname".equals(name)) {
                tip.setLastname(parser.nextText());

            } else if ("photo".equals(name)) {
                tip.setPhoto(parser.nextText());

            } else if ("relative_date".equals(name)) {
                tip.setRelativeDate(parser.nextText());

            } else if ("status_text".equals(name)) {
                tip.setStatusText(parser.nextText());

            } else if ("text".equals(name)) {
                tip.setText(parser.nextText());

            } else if ("tipid".equals(name)) {
                tip.setTipid(parser.nextText());

            } else if ("url".equals(name)) {
                tip.setUrl(parser.nextText());

            } else if ("user_status".equals(name)) {
                tip.setUserStatus(parser.nextText());

            } else if ("userid".equals(name)) {
                tip.setUserid(parser.nextText());

            } else if ("venueid".equals(name)) {
                tip.setVenueid(parser.nextText());

            } else if ("venuename".equals(name)) {
                tip.setVenuename(parser.nextText());
            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                parser.nextText();
            }
        }
        parser.nextToken();
    }
}

