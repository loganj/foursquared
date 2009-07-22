/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare.parsers;

import com.joelapenna.foursquared.foursquare.Foursquare;
import com.joelapenna.foursquared.foursquare.error.FoursquareError;
import com.joelapenna.foursquared.foursquare.types.Checkin;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class CheckinResponseParser extends AbstractParser<Checkin> {
    private static final String TAG = "CheckinResponseParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    @Override
    public Checkin parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError {
        Checkin checkin = new Checkin();
        int eventType = parser.nextToken();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));
                    String name = parser.getName();

                    if ("error".equals(name)) {
                        throw new FoursquareError(parser.getText());
                    } else if ("auth".equals(name)) {
                        parseCheckinTag(parser, checkin);
                    } else if ("checkincomplete".equals(name)) {
                        parseCheckinCompleteTag(parser, checkin);
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
            if ("checkinid".equals(name)) {
                checkin.setId(parser.nextText());
            } else if ("venueid".equals(name)) {
                checkin.setVenueId(parser.nextText());
            } else if ("aliasid".equals(name)) {
                checkin.setAliasId(parser.nextText());
            } else if ("venuename".equals(name)) {
                checkin.setAliasId(parser.nextText());
            } else if ("address".equals(name)) {
                checkin.setAliasId(parser.nextText());
            } else if ("crossstreet".equals(name)) {
                checkin.setCrossStreet(parser.nextText());
            } else if ("shout".equals(name)) {
                checkin.setShout(parser.nextText());
            } else if ("geolat".equals(name)) {
                checkin.setGeoLat(parser.nextText());
            } else if ("geolong".equals(name)) {
                checkin.setGeoLong(parser.nextText());
            } else if ("xdatetime".equals(name)) {
                checkin.setXDateTime(parser.nextText());
            } else if ("relativetime".equals(name)) {
                checkin.setRelativeTime(parser.nextText());
            }
        }
    }

    private void parseCheckinCompleteTag(XmlPullParser parser, Checkin checkin)
            throws XmlPullParserException, IOException {
        assert parser.getName() == "checkincomplete";
        checkin.setCheckinComplete(true);
        while (parser.nextTag() != XmlPullParser.END_TAG) {
            String name = parser.getName();
            if ("message".equals(name)) {
                checkin.setCheckinCompleteMessage(parser.nextText());
            }
        }
    }

}
