/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.FoursquareType;
import com.joelapenna.foursquare.types.Group;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class BadgesParser extends AbstractParser<Group> {
    private static final String TAG = "BadgesParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    private Parser<? extends FoursquareType> mSubParser;

    public BadgesParser(Parser<? extends FoursquareType> subParser) {
        this.mSubParser = subParser;
    }

    @Override
    public Group parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        // We're likely to have to parse multiple groups.
        Group badges = new Group();

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));
                    String name = parser.getName();

                    if ("error".equals(name)) {
                        throw new FoursquareError(parser.getText());
                    } else if ("badge".equals(name)) {
                        parseGroupTag(parser, badges);
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("badges")) {
                        return badges;
                    }
                    break;

                default:
                    if (DEBUG) Log.d(TAG, "Unhandled Event");
            }
            eventType = parser.nextToken();
        }
        return badges;
    }

    public void parseGroupTag(XmlPullParser parser, Group badges) throws XmlPullParserException,
            IOException {
        try {
            badges.add(this.mSubParser.parse(parser));
        } catch (FoursquareError e) {
            // TODO Auto-generated catch block
            if (DEBUG) Log.d(TAG, "FoursquareError", e);
        } catch (FoursquareParseException e) {
            // TODO Auto-generated catch block
            if (DEBUG) Log.d(TAG, "FoursquareParseException", e);
        }
    }
}
