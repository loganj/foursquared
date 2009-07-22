/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare.parsers;

import com.joelapenna.foursquared.foursquare.Foursquare;
import com.joelapenna.foursquared.foursquare.error.FoursquareError;
import com.joelapenna.foursquared.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquared.foursquare.types.Venue;
import com.joelapenna.foursquared.foursquare.types.VenueGroup;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class VenueGroupParser extends AbstractParser<VenueGroup> {
    private static final String TAG = "VenueGroupParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    @Override
    public VenueGroup parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError {
        VenueGroup venues = new VenueGroup();
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));
                    String name = parser.getName();

                    if ("error".equals(name)) {
                        throw new FoursquareError(parser.getText());
                    } else if ("group".equals(name)) {
                        parseVenueGroupTag(parser, venues);
                        break;  // TODO(jlapenna): This will only handle a result that has one group in it.
                    }

                default:
                    if (DEBUG) Log.d(TAG, "Unhandled Event");
            }
            eventType = parser.nextToken();
        }
        return venues;
    }

    public void parseVenueGroupTag(XmlPullParser parser, VenueGroup venues)
            throws XmlPullParserException, IOException {
        assert parser.getName() == "group";

        venues.setType(parser.getAttributeValue(null, "type"));

        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "VenueGroup : " + String.valueOf(parser.getName()));

                    String name = parser.getName();
                    if ("venue".equals(name)) {
                        try {
                            Venue venue = new VenueParser().parse(parser);
                            if (DEBUG) Log.d(TAG, "adding venue: " + venue);
                            venues.add(venue);
                        } catch (FoursquareError e) {
                            // TODO Auto-generated catch block
                            if (DEBUG) Log.d(TAG, "FoursquareError", e);
                        } catch (FoursquareParseException e) {
                            // TODO Auto-generated catch block
                            if (DEBUG) Log.d(TAG, "FoursquareParseException", e);
                        }
                        continue;
                    }

                default:
                    if (DEBUG) Log.d(TAG, "Unhandled Event");
            }
            eventType = parser.nextToken();
        }
    }
}
