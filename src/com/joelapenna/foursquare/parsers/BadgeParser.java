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
 * Auto-generated: 2009-06-19 00:18:40.381204
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class BadgeParser extends AbstractParser<Badge> {
    private static final String TAG = "BadgeParser";
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public Badge parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        try {
            parser.require(XmlPullParser.START_TAG, null, "badge");
        } catch (XmlPullParserException e) {
            if (parser.getName().equals("error")) {
                throw new FoursquareError(parser.getText());
            }
        }

        Badge badge = new Badge();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
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
                skipSubTree(parser);
            }
        }
        return badge;
    }
}
