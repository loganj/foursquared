/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Scoring;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-09-17 19:58:37.298701
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class ScoringParser extends AbstractParser<Scoring> {
    private static final String TAG = "ScoringParser";
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public Scoring parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, null);

        Scoring scoring = new Scoring();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("rank".equals(name)) {
                scoring.setRank(new GroupParser(new RankParser()).parse(parser));

            } else if ("score".equals(name)) {
                scoring.setScore(new ScoreParser().parse(parser));

            } else if ("total".equals(name)) {
                scoring.setTotal(new ScoreParser().parse(parser));

            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return scoring;
    }
}
