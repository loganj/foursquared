/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare;

import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Auth;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Data;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.Foursquared;

import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Foursquare {
    private static final String TAG = "Foursquare";
    public static final boolean DEBUG = Foursquared.DEBUG;

    private String mPhone;
    private String mPassword;
    private FoursquareHttpApi mFoursquare;

    public Foursquare(String phone, String password) {
        mFoursquare = new FoursquareHttpApi(FoursquareHttpApi.createHttpClient());
        setCredentials(phone, password);
    }

    public void setCredentials(String phone, String password) {
        mPhone = phone;
        mPassword = password;
        mFoursquare.setCredentials(phone, password);
    }

    public boolean login() throws FoursquareError, FoursquareParseException, IOException {
        if (DEBUG) Log.d(TAG, "login()");
        Auth auth = mFoursquare.login(mPhone, mPassword);
        return (auth != null && auth.status());

    }

    public Data addTip(String text, String vid, String lat, String lng, String cityId)
            throws FoursquareError, FoursquareParseException, IOException {
        return mFoursquare.add("top", text, vid, lat, lng, cityId);
    }

    public Data addTodo(String text, String vid, String lat, String lng, String cityId)
            throws FoursquareError, FoursquareParseException, IOException {
        return mFoursquare.add("todo", text, vid, lat, lng, cityId);
    }

    public String breakdown(String userId, String checkinId) throws FoursquareError,
            FoursquareParseException, IOException {
        return mFoursquare.breakdown(userId, checkinId);
    }

    public Checkin checkin(String venue, boolean silent, boolean twitter, String lat, String lng)
            throws FoursquareError, FoursquareParseException, IOException {
        return mFoursquare.checkin(mPhone, venue, silent, twitter, lat, lng, null);
    }

    public Group todos(String cityId, String lat, String lng) throws FoursquareError,
            FoursquareParseException, IOException {
        return mFoursquare.todos(cityId, lat, lng);
    }

    public Group venues(String query, String lat, String lng, int radius, int length)
            throws FoursquareError, FoursquareParseException, IOException {
        return mFoursquare.venues(query, lat, lng, radius, length);
    }

    public Group checkins(String cityId, String lat, String lng) throws FoursquareError, FoursquareParseException,
            IOException {
        return mFoursquare.checkins(cityId, lat, lng);
    }

    public Venue venue(String id) throws FoursquareError, FoursquareParseException, IOException {
        return mFoursquare.venue(id);
    }
}
