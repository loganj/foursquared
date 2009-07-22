/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Stats;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-06-19 00:18:41.973373
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class StatsParser extends AbstractParser<Stats> {
    private static final String TAG = "StatsParser";
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public Stats parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        try {
            parser.require(XmlPullParser.START_TAG, null, "stats");
        } catch (XmlPullParserException e) {
            if (parser.getName().equals("error")) {
                throw new FoursquareError(parser.getText());
            }
        }

        Stats stats = new Stats();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("beenhere".equals(name)) {
                stats.setBeenhere(new BeenhereParser().parse(parser));

            } else if ("checkins".equals(name)) {
                stats.setCheckins(parser.nextText());

            } else if ("mayor".equals(name)) {
                stats.setMayor(new MayorParser().parse(parser));

            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return stats;
    }
}
