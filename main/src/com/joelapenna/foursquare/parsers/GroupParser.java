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
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    private Parser<? extends FoursquareType> mSubParser;

    public GroupParser(Parser<? extends FoursquareType> subParser) {
        this.mSubParser = subParser;
    }

    @Override
    public Group parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareParseException, FoursquareError {

        Group group = new Group();
        group.setType(parser.getAttributeValue(null, "type"));

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));
            FoursquareType item = this.mSubParser.parse(parser);
            if (DEBUG) Log.d(TAG, "adding item: " + item);
            group.add(item);
        }
        return group;
    }
}
