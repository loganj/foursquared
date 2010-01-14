/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare;

import com.joelapenna.foursquare.error.FoursquareCredentialsException;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.CheckinResult;
import com.joelapenna.foursquare.types.Credentials;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Venue;

import android.net.Uri;
import android.text.TextUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Foursquare {
    private static final Logger LOG = Logger.getLogger("com.joelapenna.foursquare");
    public static final boolean DEBUG = true;
    public static final boolean PARSER_DEBUG = false;

    public static final String FOURSQUARE_API_DOMAIN = "api.foursquare.com";

    public static final String FOURSQUARE_MOBILE_ADDFRIENDS = "http://m.foursquare.com/addfriends";
    public static final String FOURSQUARE_MOBILE_FRIENDS = "http://m.foursquare.com/friends";
    public static final String FOURSQUARE_MOBILE_SIGNUP = "http://m.foursquare.com/signup";
    public static final String FOURSQUARE_PREFERENCES = "http://foursquare.com/settings";

    public static final String MALE = "male";
    public static final String FEMALE = "female";

    private String mPhone;
    private String mPassword;
    private FoursquareHttpApiV1 mFoursquareV1;

    @V1
    public Foursquare(FoursquareHttpApiV1 httpApi) {
        mFoursquareV1 = httpApi;
    }

    public void setCredentials(String phone, String password) {
        mPhone = phone;
        mPassword = password;
        mFoursquareV1.setCredentials(phone, password);
    }

    @V1
    public void setOAuthToken(String token, String secret) {
        mFoursquareV1.setOAuthTokenWithSecret(token, secret);
    }

    @V1
    public void setOAuthConsumerCredentials(String oAuthConsumerKey, String oAuthConsumerSecret) {
        mFoursquareV1.setOAuthConsumerCredentials(oAuthConsumerKey, oAuthConsumerSecret);
    }

    public void clearAllCredentials() {
        setCredentials(null, null);
        setOAuthToken(null, null);
    }

    @V1
    public boolean hasCredentials() {
        return mFoursquareV1.hasCredentials() && mFoursquareV1.hasOAuthTokenWithSecret();
    }

    @V1
    public boolean hasLoginAndPassword() {
        return mFoursquareV1.hasCredentials();
    }

    @V1
    public Credentials authExchange() throws FoursquareException, FoursquareError,
            FoursquareCredentialsException, IOException {
        if (mFoursquareV1 == null) {
            throw new NoSuchMethodError(
                    "authExchange is unavailable without a consumer key/secret.");
        }
        return mFoursquareV1.authExchange(mPhone, mPassword);
    }

    @V1
    public Tip addTip(String vid, String text, String type, Location location)
            throws FoursquareException, FoursquareError, IOException {
        return mFoursquareV1.addtip(vid, text, type, location.geolat, location.geolong,
                location.geohacc, location.geovacc, location.geoalt);
    }

    @V1
    public Venue addVenue(String name, String address, String crossstreet, String city,
            String state, String zip, String phone, Location location) throws FoursquareException,
            FoursquareError, IOException {
        return mFoursquareV1.addvenue(name, address, crossstreet, city, state, zip, phone,
                location.geolat, location.geolong, location.geohacc, location.geovacc,
                location.geoalt);
    }

    @V1
    public CheckinResult checkin(String venueId, String venueName, Location location, String shout,
            boolean isPrivate, boolean twitter) throws FoursquareException, FoursquareError,
            IOException {
        return mFoursquareV1.checkin(venueId, venueName, location.geolat, location.geolong,
                location.geohacc, location.geovacc, location.geoalt, shout, isPrivate, twitter);
    }

    @V1
    public Group<Checkin> checkins(Location location) throws FoursquareException, FoursquareError,
            IOException {
        return mFoursquareV1.checkins(location.geolat, location.geolong, location.geohacc,
                location.geovacc, location.geoalt);
    }

    @V1
    public Group<User> friends(String userId, Location location) throws FoursquareException,
            FoursquareError, IOException {
        return mFoursquareV1.friends(userId, location.geolat, location.geolong,
                location.geohacc, location.geovacc, location.geoalt);
    }

    @V1
    public Group<User> friendRequests() throws FoursquareException, FoursquareError, IOException {
        return mFoursquareV1.friendRequests();
    }

    @V1
    public User friendApprove(String userId) throws FoursquareException,
            FoursquareCredentialsException, FoursquareError, IOException {
        return mFoursquareV1.friendApprove(userId);
    }

    @V1
    public User friendDeny(String userId) throws FoursquareException,
            FoursquareCredentialsException, FoursquareError, IOException {
        return mFoursquareV1.friendDeny(userId);
    }

    @V1
    public User friendSendrequest(String userId) throws FoursquareException,
            FoursquareCredentialsException, FoursquareError, IOException {
        return mFoursquareV1.friendSendrequest(userId);
    }

    @V1
    public Group<Group<Tip>> tips(Location location, int limit) throws FoursquareException,
            FoursquareError, IOException {
        return mFoursquareV1.tips(location.geolat, location.geolong, location.geohacc,
                location.geovacc, location.geoalt, limit);
    }

    @V1
    public User user(String user, boolean mayor, boolean badges, Location location)
            throws FoursquareException, FoursquareError, IOException {
        if (location != null) {
            return mFoursquareV1.user(user, mayor, badges, location.geolat, location.geolong,
                    location.geohacc, location.geovacc, location.geoalt);
        } else {
            return mFoursquareV1.user(user, mayor, badges, null, null, null, null, null);
        }
    }

    @V1
    public Venue venue(String id, Location location) throws FoursquareException, FoursquareError,
            IOException {
        return mFoursquareV1.venue(id, location.geolat, location.geolong, location.geohacc,
                location.geovacc, location.geoalt);
    }

    @V1
    public Group<Group<Venue>> venues(Location location, String query, int limit)
            throws FoursquareException, FoursquareError, IOException {
        return mFoursquareV1.venues(location.geolat, location.geolong, location.geohacc,
                location.geovacc, location.geoalt, query, limit);
    }

    public static final FoursquareHttpApiV1 createHttpApi(String domain, String clientVersion,
            boolean useOAuth) {
        LOG.log(Level.INFO, "Using foursquare.com for requests.");
        return new FoursquareHttpApiV1(domain, clientVersion, useOAuth);
    }

    public static final FoursquareHttpApiV1 createHttpApi(String clientVersion, boolean useOAuth) {
        return createHttpApi(FOURSQUARE_API_DOMAIN, clientVersion, useOAuth);
    }

    public static final String createLeaderboardUrl(String userId, Location location) {
        Uri.Builder builder = new Uri.Builder() //
                .scheme("http") //
                .authority("foursquare.com") //
                .appendEncodedPath("/iphone/me") //
                .appendQueryParameter("view", "all") //
                .appendQueryParameter("scope", "friends") //
                .appendQueryParameter("uid", userId);
        if (!TextUtils.isEmpty(location.geolat)) {
            builder.appendQueryParameter("geolat", location.geolat);
        }
        if (!TextUtils.isEmpty(location.geolong)) {
            builder.appendQueryParameter("geolong", location.geolong);
        }
        if (!TextUtils.isEmpty(location.geohacc)) {
            builder.appendQueryParameter("geohacc", location.geohacc);
        }
        if (!TextUtils.isEmpty(location.geovacc)) {
            builder.appendQueryParameter("geovacc", location.geovacc);
        }
        return builder.build().toString();
    }

    /**
     * This api is supported in the V1 API documented at:
     * http://groups.google.com/group/foursquare-api/web/api-documentation
     */
    @interface V1 {
    }

    public static class Location {
        String geolat = null;
        String geolong = null;
        String geohacc = null;
        String geovacc = null;
        String geoalt = null;

        public Location(final String geolat, final String geolong, final String geohacc,
                final String geovacc, final String geoalt) {
            this.geolat = geolat;
            this.geolong = geolong;
            this.geohacc = geohacc;
            this.geovacc = geovacc;
            this.geoalt = geovacc;
        }

        public Location(final String geolat, final String geolong) {
            this(geolat, geolong, null, null, null);
        }
    }

}
