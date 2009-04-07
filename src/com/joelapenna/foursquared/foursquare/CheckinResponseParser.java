/**
 * Copyright 2009 Joe LaPenna
 */
package com.joelapenna.foursquared.foursquare;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 *
 */
public class CheckinResponseParser {
    private static final String TAG = "CheckinResponseParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    private XmlPullParser mParser;

    public CheckinResponseParser(InputStream is) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            mParser = factory.newPullParser();
            mParser.setInput(is, null);
        } catch (XmlPullParserException e) {
            throw new IllegalArgumentException();
        }
    }

    public void parse() {
        if (DEBUG) Log.d(TAG, "parse()ing");
        int eventType;
        try {
            eventType = mParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (DEBUG) Log.d(TAG, "Event Type: " + String.valueOf(eventType));
                switch (eventType) {

                    case XmlPullParser.START_DOCUMENT:
                        break;

                    case XmlPullParser.END_DOCUMENT:
                        break;

                    case XmlPullParser.START_TAG:
                        handleStartTag();
                        break;

                    case XmlPullParser.END_TAG:
                        break;

                    case XmlPullParser.TEXT:
                        break;

                    default:
                        if (DEBUG) Log.d(TAG, "Unhandled Event");
                        break;

                }
                eventType = mParser.next();
            }
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            if (DEBUG) Log.d(TAG, "XmlPullParserException in parse", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            if (DEBUG) Log.d(TAG, "IOException in Parse", e);
        }
    }

    /**
     *
     */
    private void handleStartTag() {
        // TODO Auto-generated method stub

    }

}
