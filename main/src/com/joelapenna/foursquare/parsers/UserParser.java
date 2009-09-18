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

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-09-17 19:58:37.888051
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class UserParser extends AbstractParser<User> {
    private static final String TAG = "UserParser";
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public User parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareError, FoursquareParseException {
        parser.require(XmlPullParser.START_TAG, null, "user");

        User user = new User();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("badges".equals(name)) {
                user.setBadges(new GroupParser(new BadgeParser()).parse(parser));

            } else if ("checkin".equals(name)) {
                user.setCheckin(new CheckinParser().parse(parser));

            } else if ("city".equals(name)) {
                user.setCity(new CityParser().parse(parser));

            } else if ("created".equals(name)) {
                user.setCreated(parser.nextText());

            } else if ("firstname".equals(name)) {
                user.setFirstname(parser.nextText());

            } else if ("gender".equals(name)) {
                user.setGender(parser.nextText());

            } else if ("id".equals(name)) {
                user.setId(parser.nextText());

            } else if ("lastname".equals(name)) {
                user.setLastname(parser.nextText());

            } else if ("photo".equals(name)) {
                user.setPhoto(parser.nextText());

            } else if ("settings".equals(name)) {
                user.setSettings(new SettingsParser().parse(parser));

            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return user;
    }
}
