/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers.classic;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.parsers.AbstractParser;
import com.joelapenna.foursquare.types.classic.Venue;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-06-02 23:02:36.820816
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
@SuppressWarnings("deprecation")
public class VenueParser extends AbstractParser<Venue> {
    private static final String TAG = "VenueParser";
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public Venue parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareParseException {
        try {
        parser.require(XmlPullParser.START_TAG, null, "venue");
        } catch (XmlPullParserException e) {
            if ("error".equals(parser.getName())) {
                throw new FoursquareParseException(e.getMessage());
            }
        }

        Venue venue = new Venue();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
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

            } else if ("extra".equals(name)) {
                venue.setExtra(parser.nextText());

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

            } else if ("tag".equals(name)) {
                venue.setTag(parser.nextText());

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
                skipSubTree(parser);
            }
        }
        return venue;
    }
}
