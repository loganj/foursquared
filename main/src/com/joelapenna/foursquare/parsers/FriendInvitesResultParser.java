/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.FriendInvitesResult;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @date 2010-05-05
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class FriendInvitesResultParser extends AbstractParser<FriendInvitesResult> {
    private static final Logger LOG = Logger.getLogger(UserParser.class.getCanonicalName());
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public FriendInvitesResult parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, null);

        FriendInvitesResult result = new FriendInvitesResult();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            String name = parser.getName();
            if ("users".equals(name)) {
                result.setContactsOnFoursquare(new GroupParser(new UserParser()).parse(parser));

            } else if ("emails".equals(name)) {
                result.setContactEmailsOnNotOnFoursquare(new EmailsParser().parse(parser));

            } else if ("invited".equals(name)) {
                result.setContactEmailsOnNotOnFoursquareAlreadyInvited(new EmailsParser().parse(parser));

            } else {
                // Consume something we don't understand.
                if (DEBUG) LOG.log(Level.FINE, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        
        return result;
    }
}
