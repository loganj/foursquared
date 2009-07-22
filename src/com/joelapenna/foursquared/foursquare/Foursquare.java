/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare;

import com.joelapenna.foursquared.foursquare.types.Auth;
import com.joelapenna.foursquared.foursquare.types.IncomingCheckin;

import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Foursquare {
    private static final String TAG = "Foursquare";
    public static final boolean DEBUG = true;

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
    }

    public boolean login() {
        if (DEBUG) Log.d(TAG, "login()");
        Auth auth = mFoursquare.login(mPhone, mPassword);
        return (auth != null && auth.status());

    }

    public IncomingCheckin checkin(String venue, boolean silent, boolean twitter, String lat, String lng) {
        return mFoursquare.checkin(mPhone, venue, silent, twitter, lat, lng, "23");
    }
}
