/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Venue;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-05-02 23:59:56.237833
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class VenueParser extends AbstractParser<Venue> {
    private static final String TAG = "VenueParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    @Override
    public Venue parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        Venue venue = new Venue();
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

                    String name = parser.getName();
                    if ("error".equals(name)) {
                        throw new FoursquareError(parser.getText());
                    } else if ("venue".equals(name)) {
                        parseVenueTag(parser, venue);
                        return venue;
                    }
                    break;

                default:
                    if (DEBUG) Log.d(TAG, "Unhandled Event");
            }
            eventType = parser.nextToken();
        }
        return null;
    }

    public void parseVenueTag(XmlPullParser parser, Venue venue) throws XmlPullParserException,
            IOException, FoursquareError, FoursquareParseException {
        assert parser.getName() == "venue";
        if (DEBUG) Log.d(TAG, "parsing venue stanza");

        while (parser.nextTag() != XmlPullParser.END_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("address".equals(name)) {
                venue.setAddress(parser.nextText());

            } else if ("beenhere_friends".equals(name)) {
                venue.setBeenhereFriends(parser.nextText().equals("1"));

            } else if ("beenhere_me".equals(name)) {
                venue.setBeenhereMe(parser.nextText().equals("1"));

            } else if ("city".equals(name)) {
                venue.setCity(parser.nextText());

            } else if ("crossstreet".equals(name)) {
                venue.setCrossstreet(parser.nextText());

            } else if ("distance".equals(name)) {
                venue.setDistance(parser.nextText());

            } else if ("geolat".equals(name)) {
                venue.setGeolat(parser.nextText());

            } else if ("geolong".equals(name)) {
                venue.setGeolong(parser.nextText());

            } else if ("map".equals(name)) {
                venue.setMap(parser.nextText());

            } else if ("mapurl".equals(name)) {
                venue.setMapurl(parser.nextText());

            } else if ("new_venue".equals(name)) {
                venue.setNewVenue(parser.nextText().equals("1"));

            } else if ("num_checkins".equals(name)) {
                venue.setNumCheckins(parser.nextText());

            } else if ("state".equals(name)) {
                venue.setState(parser.nextText());

            } else if ("venueid".equals(name)) {
                venue.setVenueid(parser.nextText());

            } else if ("venuename".equals(name)) {
                venue.setVenuename(parser.nextText());

            } else if ("yelp".equals(name)) {
                venue.setYelp(parser.nextText());

            } else if ("zip".equals(name)) {
                venue.setZip(parser.nextText());
            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                parser.nextText();
            }
        }
        parser.nextToken();
    }
}

