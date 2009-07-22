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
public class GroupParser extends AbstractParser<Group> {
    private static final String TAG = "GroupParser";
    private static final boolean DEBUG = Foursquare.DEBUG;

    private Parser<? extends FoursquareType> mSubParser;

    public GroupParser(Parser<? extends FoursquareType> subParser) {
        this.mSubParser = subParser;
    }

    @Override
    public Group parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError {
        // We're likely to have to parse multiple groups.
        Group groups = new Group();

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));
                    String name = parser.getName();

                    if ("error".equals(name)) {
                        throw new FoursquareError(parser.getText());
                    } else if ("group".equals(name)) {
                        Group items = new Group();
                        parseGroupTag(parser, items);
                        groups.add(items);
                    }
                    break;

                default:
                    if (DEBUG) Log.d(TAG, "Unhandled Event");
            }
            eventType = parser.nextToken();
        }
        return groups;
    }

    public void parseGroupTag(XmlPullParser parser, Group group) throws XmlPullParserException,
            IOException {
        assert parser.getName() == "group";

        group.setType(parser.getAttributeValue(null, "type"));

        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (DEBUG) Log.d(TAG, "Group : " + String.valueOf(parser.getName()));
                    try {
                        FoursquareType item = this.mSubParser.parse(parser);
                        if (item != null) {
                            if (DEBUG) Log.d(TAG, "adding item: " + item);
                            group.add(item);
                        }
                    } catch (FoursquareError e) {
                        // TODO Auto-generated catch block
                        if (DEBUG) Log.d(TAG, "FoursquaredCredentialsError", e);
                    } catch (FoursquareParseException e) {
                        // TODO Auto-generated catch block
                        if (DEBUG) Log.d(TAG, "FoursquareParseException", e);
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("group")) {
                        return;
                    }

                default:
                    if (DEBUG) Log.d(TAG, "Unhandled Event");
            }
            eventType = parser.nextToken();
        }
    }
}
