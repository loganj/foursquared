/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Badge;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-04-30 18:45:18.593342
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class BadgeParser extends AbstractParser<Badge> {
    private static final String TAG = "BadgeParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    @Override
    public Badge parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        Badge badge = new Badge();
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

                    String name = parser.getName();
                    if ("error".equals(name)) {
                        throw new FoursquareError(parser.getText());
                    } else if ("badge".equals(name)) {
                        parseBadgeTag(parser, badge);
                        return badge;
                    }
                    break;

                default:
                    if (DEBUG) Log.d(TAG, "Unhandled Event");
            }
            eventType = parser.nextToken();
        }
        return null;
    }

    public void parseBadgeTag(XmlPullParser parser, Badge badge) throws XmlPullParserException,
            IOException, FoursquareError, FoursquareParseException {
        assert parser.getName() == "badge";
        if (DEBUG) Log.d(TAG, "parsing badge stanza");

        while (parser.nextTag() != XmlPullParser.END_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("description".equals(name)) {
                badge.setDescription(parser.nextText());

            } else if ("icon".equals(name)) {
                badge.setIcon(parser.nextText());

            } else if ("name".equals(name)) {
                badge.setName(parser.nextText());
            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                parser.nextText();
            }
        }
        parser.nextToken();
    }
}

