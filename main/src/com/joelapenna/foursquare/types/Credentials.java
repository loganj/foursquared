/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

/**
 * Auto-generated: 2009-11-12 21:45:35.491532
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Credentials implements FoursquareType {

    private String mOauthToken;
    private String mOauthTokenSecret;

    public Credentials() {
    }

    public String getOauthToken() {
        return mOauthToken;
    }

    public void setOauthToken(String oauthToken) {
        mOauthToken = oauthToken;
    }

    public String getOauthTokenSecret() {
        return mOauthTokenSecret;
    }

    public void setOauthTokenSecret(String oauthTokenSecret) {
        mOauthTokenSecret = oauthTokenSecret;
    }

}
