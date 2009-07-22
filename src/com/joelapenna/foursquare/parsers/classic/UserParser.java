/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.parsers.classic;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.parsers.AbstractParser;
import com.joelapenna.foursquare.parsers.BadgeParser;
import com.joelapenna.foursquare.parsers.GroupParser;
import com.joelapenna.foursquare.types.classic.User;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import java.io.IOException;

/**
 * Auto-generated: 2009-06-02 23:02:36.637107
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @param <T>
 */
public class UserParser extends AbstractParser<User> {
    private static final String TAG = "UserParser";
    private static final boolean DEBUG = Foursquare.PARSER_DEBUG;

    @Override
    public User parseInner(XmlPullParser parser) throws XmlPullParserException, IOException,
            FoursquareParseException, FoursquareError {
        parser.require(XmlPullParser.START_TAG, null, "user");

        User user = new User();

        while (parser.nextTag() == XmlPullParser.START_TAG) {
            if (DEBUG) Log.d(TAG, "Tag Name: " + String.valueOf(parser.getName()));

            String name = parser.getName();
            if ("badges".equals(name)) {
                user.setBadges(new GroupParser(new BadgeParser()).parse(parser));

            } else if ("cityid".equals(name)) {
                user.setCityid(parser.nextText());

            } else if ("cityshortname".equals(name)) {
                user.setCityshortname(parser.nextText());

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

            } else if ("sendtwitter".equals(name)) {
                user.setSendtwitter(parser.nextText().equals("1"));
            } else {
                // Consume something we don't understand.
                if (DEBUG) Log.d(TAG, "Found tag that we don't recognize: " + name);
                skipSubTree(parser);
            }
        }
        return user;
    }
}
