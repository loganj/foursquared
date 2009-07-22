/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.types.Checkin;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class CheckinParser extends AbstractParser<Checkin> {
    private static final String TAG = "CheckinParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    @Override
    public Checkin parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError {
        Checkin checkin = new Checkin();
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

                    String name = parser.getName();
                    if ("error".equals(name)) {
                        throw new FoursquareError(parser.getText());
                    } else if ("checkin".equals(name)) {
                        parseCheckinTag(parser, checkin);
                        return checkin;
                    }

                default:
                    if (DEBUG) Log.d(TAG, "Unhandled Event");
            }
            eventType = parser.nextToken();
        }
        return null;
    }

    public void parseCheckinTag(XmlPullParser parser, Checkin checkin) throws XmlPullParserException,
            IOException {
        assert parser.getName() == "checkin";
        if (DEBUG) Log.d(TAG, "parsing checkin stanza");

        while (parser.nextTag() != XmlPullParser.END_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("address".equals(name)) {
                checkin.setAddress(parser.nextText());

            } else if ("alert".equals(name)) {
                checkin.setAlert(parser.nextText());

            } else if ("aliasid".equals(name)) {
                checkin.setAliasid(parser.nextText());

            } else if ("checkinid".equals(name)) {
                checkin.setCheckinid(parser.nextText());

            } else if ("city_name".equals(name)) {
                checkin.setCityName(parser.nextText());

            } else if ("cityid".equals(name)) {
                checkin.setCityid(parser.nextText());

            } else if ("crossstreet".equals(name)) {
                checkin.setCrossstreet(parser.nextText());

            } else if ("dball_default".equals(name)) {
                checkin.setDballDefault(parser.nextText().equals("1"));

            } else if ("email".equals(name)) {
                checkin.setEmail(parser.nextText());

            } else if ("firstname".equals(name)) {
                checkin.setFirstname(parser.nextText());

            } else if ("gender".equals(name)) {
                checkin.setGender(parser.nextText());

            } else if ("geolat".equals(name)) {
                checkin.setGeolat(parser.nextText());

            } else if ("geolong".equals(name)) {
                checkin.setGeolong(parser.nextText());

            } else if ("lastname".equals(name)) {
                checkin.setLastname(parser.nextText());

            } else if ("message".equals(name)) {
                checkin.setMessage(parser.nextText());

            } else if ("phone".equals(name)) {
                checkin.setPhone(parser.nextText());

            } else if ("photo".equals(name)) {
                checkin.setPhoto(parser.nextText());

            } else if ("relative_time".equals(name)) {
                checkin.setRelativeTime(parser.nextText());

            } else if ("shout".equals(name)) {
                checkin.setShout(parser.nextText());

            } else if ("show_dball".equals(name)) {
                checkin.setShowDball(parser.nextText().equals("1"));

            } else if ("show_twitter".equals(name)) {
                checkin.setShowTwitter(parser.nextText().equals("1"));

            } else if ("stats".equals(name)) {
                checkin.setStats(parser.nextText());

            } else if ("status".equals(name)) {
                checkin.setStatus(parser.nextText().equals("1"));

            } else if ("twitter_default".equals(name)) {
                checkin.setTwitterDefault(parser.nextText().equals("1"));

            } else if ("url".equals(name)) {
                checkin.setUrl(parser.nextText());

            } else if ("userid".equals(name)) {
                checkin.setUserid(parser.nextText());

            } else if ("venueid".equals(name)) {
                checkin.setVenueid(parser.nextText());

            } else if ("venuename".equals(name)) {
                checkin.setVenuename(parser.nextText());

            } else if ("xdatetime".equals(name)) {
                checkin.setXdatetime(parser.nextText());
            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                parser.nextText();
            }
        }
        parser.nextToken();
    }
}

