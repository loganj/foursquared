/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.User;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Auto-generated: 2009-11-22 20:22:15.986656
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class UserParser extends AbstractParser<User> {
    private static final Logger LOG = Logger.getLogger(UserParser.class.getCanonicalName());
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public User parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, null);

        User user = new User();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) LOG.log(Level.FINE, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("badges".equals(name)) {
                user.setBadges(new GroupParser(new BadgeParser()).parse(parser));

            } else if ("checkin".equals(name)) {
                user.setCheckin(new CheckinParser().parse(parser));

            } else if ("city".equals(name)) {
                user.setCity(new CityParser().parse(parser));

            } else if ("created".equals(name)) {
                user.setCreated(parser.nextText());

            } else if ("email".equals(name)) {
                user.setEmail(parser.nextText());

            } else if ("firstname".equals(name)) {
                user.setFirstname(parser.nextText());

            } else if ("friendstatus".equals(name)) {
                user.setFriendstatus(parser.nextText());

            } else if ("gender".equals(name)) {
                user.setGender(parser.nextText());

            } else if ("id".equals(name)) {
                user.setId(parser.nextText());

            } else if ("lastname".equals(name)) {
                user.setLastname(parser.nextText());

            } else if ("phone".equals(name)) {
                user.setPhone(parser.nextText());

            } else if ("photo".equals(name)) {
                user.setPhoto(parser.nextText());

            } else if ("settings".equals(name)) {
                user.setSettings(new SettingsParser().parse(parser));

            } else if ("twitter".equals(name)) {
                user.setTwitter(parser.nextText());

            } else {
                // Consume something we don't understand.
                if (DEBUG) LOG.log(Level.FINE, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return user;
    }
}
