/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare;

import com.joelapenna.foursquared.foursquare.types.Auth;
import com.joelapenna.foursquared.foursquare.types.Checkin;

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

    public void setCredentials(String email, String password) {
        mPhone = email;
        mPassword = password;
    }

    public boolean login() {
        Auth auth = mFoursquare.login(mPhone, mPassword);
        return (auth != null && auth.isSuccessful());

    }

    public Checkin checkin(String venue, boolean privacy, boolean twitter) {
        return mFoursquare.checkin(venue, privacy, twitter);
    }
}
