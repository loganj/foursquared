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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Auto-generated: 2009-11-12 21:45:34.434891
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class ScoringParser extends AbstractParser<Scoring> {
    private static final Logger LOG = Logger.getLogger("ScoringParser");
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public Scoring parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, null);

        Scoring scoring = new Scoring();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) LOG.log(Level.FINE, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("rank".equals(name)) {
                scoring.setRank(new GroupParser(new RankParser()).parse(parser));

            } else if ("score".equals(name)) {
                scoring.setScore(new ScoreParser().parse(parser));

            } else if ("total".equals(name)) {
                scoring.setTotal(new ScoreParser().parse(parser));

            } else {
                // Consume something we don't understand.
                if (DEBUG) LOG.log(Level.FINE, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return scoring;
    }
}
