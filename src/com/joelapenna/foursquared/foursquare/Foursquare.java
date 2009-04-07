/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare;



/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Foursquare {
    private static final String TAG = "Foursquare";
    public static final boolean DEBUG = true;

    private String mEmail;
    private String mPassword;
    private FoursquareHttpApi mFoursquare;

    public Foursquare(String email, String password) {
        mFoursquare = new FoursquareHttpApi(FoursquareHttpApi.createHttpClient());
        setCredentials(email, password);
    }

    public void setCredentials(String email, String password) {
        mEmail = email;
        mPassword = password;
    }

    public boolean isLoggedIn() {
        return mFoursquare.isLoggedIn();
    }

    public boolean login() {
        return mFoursquare.login(mEmail, mPassword);
    }

    public boolean checkin(String venue, boolean privacy, boolean twitter) {
        return mFoursquare.checkin(venue, privacy, twitter);
    }
}
