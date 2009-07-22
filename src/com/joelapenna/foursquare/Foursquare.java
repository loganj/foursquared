/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare;

import com.joelapenna.foursquare.error.FoursquareCredentialsError;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Credentials;
import com.joelapenna.foursquare.types.Data;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.classic.Auth;
import com.joelapenna.foursquare.types.classic.Checkin;
import com.joelapenna.foursquare.types.classic.User;
import com.joelapenna.foursquare.types.classic.Venue;
import com.joelapenna.foursquared.Foursquared;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Foursquare {
    private static final String TAG = "Foursquare";
    public static final boolean DEBUG = Foursquared.API_DEBUG;
    public static final boolean PARSER_DEBUG = Foursquared.PARSER_DEBUG;

    private String mPhone;
    private String mPassword;
    private FoursquareHttpApi mFoursquare;
    private FoursquareHttpApiV1 mFoursquareV1;

    public Foursquare() {
        mFoursquare = new FoursquareHttpApi();
        mFoursquareV1 = new FoursquareHttpApiV1();
    }

    public Foursquare(String oAuthConsumerKey, String oAuthConsumerSecret) {
        this();
        mFoursquareV1.setOAuthConsumerCredentials(oAuthConsumerKey, oAuthConsumerSecret);
    }

    public void setCredentials(String phone, String password) {
        mPhone = phone;
        mPassword = password;
        mFoursquare.setCredentials(phone, password);
    }

    public void setCredentials(String phone, String password, String token, String secret) {
        setCredentials(phone, password);
        mFoursquareV1.setOAuthTokenWithSecret(token, secret);
    }

    public void setOAuthConsumerCredentials(String oAuthConsumerKey, String oAuthConsumerSecret) {
        mFoursquareV1.setOAuthConsumerCredentials(oAuthConsumerKey, oAuthConsumerSecret);
    }

    public boolean hasCredentials() {
        return !(TextUtils.isEmpty(mPhone) && TextUtils.isEmpty(mPassword));
    }

    public boolean hasCredentials(boolean v1) {
        return hasCredentials() && ((v1) ? mFoursquareV1.hasCredentials() : true);
    }

    @V1
    public Credentials authExchange() throws FoursquareException, FoursquareError,
            FoursquareCredentialsError, IOException {
        if (mFoursquareV1 == null) {
            throw new NoSuchMethodError(
                    "authExchange is unavailable without a consumer key/secret.");
        }
        return mFoursquareV1.authExchange(mPhone, mPassword);
    }

    @Classic
    public Auth login() throws FoursquareException, FoursquareError, FoursquareCredentialsError,
            IOException {
        if (DEBUG) Log.d(TAG, "login()");
        return mFoursquare.login(mPhone, mPassword);

    }

    @Classic
    public Data addTip(String text, String vid, String lat, String lng, String cityId)
            throws FoursquareException, FoursquareError, IOException {
        return mFoursquare.add("top", text, vid, lat, lng, cityId);
    }

    @Classic
    public Data addTodo(String text, String vid, String lat, String lng, String cityId)
            throws FoursquareException, FoursquareError, IOException {
        return mFoursquare.add("todo", text, vid, lat, lng, cityId);
    }

    @Classic
    public Checkin checkin(String venue, boolean silent, boolean twitter, String lat, String lng)
            throws FoursquareException, FoursquareError, IOException {
        return mFoursquare.checkin(mPhone, venue, silent, twitter, lat, lng, null);
    }

    @Classic
    public Group checkins(String cityId, String lat, String lng) throws FoursquareException,
            FoursquareError, IOException {
        return mFoursquare.checkins(cityId, lat, lng);
    }

    @Deprecated
    @Classic
    public Group todos(String cityId, String lat, String lng) throws FoursquareException,
            FoursquareError, IOException {
        return mFoursquare.todos(cityId, lat, lng);
    }

    @V1
    public Group tips(String geolat, String geolong, int limit) throws FoursquareException,
            FoursquareError, IOException {
        return mFoursquareV1.tips(geolat, geolong, limit);
    }

    @Classic
    public Data update(String status, String tipid) throws FoursquareException, FoursquareError,
            IOException {
        return mFoursquare.update(status, tipid);
    }

    @Classic
    public User user() throws FoursquareException, FoursquareError, IOException {
        return mFoursquare.user();
    }

    @Classic
    public Venue venue(String id) throws FoursquareException, FoursquareError, IOException {
        return mFoursquare.venue(id);
    }

    @Classic
    public Group venues(String query, String lat, String lng, int radius, int length)
            throws FoursquareException, FoursquareError, IOException {
        return mFoursquare.venues(query, lat, lng, radius, length);
    }

    /**
     * This api is supported in the V1 !PI documented at:
     * http://groups.google.com/group/foursquare-api/web/api-documentation
     */
    @interface V1 {
    }

    /**
     * This api was reverse engineered from the iPhone app.
     */
    @interface Classic {
    }

}
