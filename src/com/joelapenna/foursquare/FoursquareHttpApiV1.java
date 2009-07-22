/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare;

import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.http.HttpApi;
import com.joelapenna.foursquare.http.HttpApiWithOAuth;
import com.joelapenna.foursquare.parsers.CredentialsParser;
import com.joelapenna.foursquare.types.Credentials;
import com.joelapenna.foursquared.Foursquared;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquareHttpApiV1 {
    private static final String TAG = "FoursquareHttpApiV1";
    private static final boolean DEBUG = Foursquared.API_DEBUG;

    private DefaultHttpClient mHttpClient;
    private HttpApiWithOAuth mHttpApi;

    public FoursquareHttpApiV1(String oAuthConsumerKey, String oAuthConsumerSecret) {
        mHttpClient = HttpApi.createHttpClient();
        mHttpApi = new HttpApiWithOAuth(mHttpClient);
        mHttpApi.setOAuthConsumerCredentials(oAuthConsumerKey, oAuthConsumerSecret);
    }

    public void setCredentials(String token, String secret) {
        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(secret)) {
            if (DEBUG) Log.d(TAG, "Clearing Credentials");
            throw new NoSuchMethodError();
        } else {
            mHttpApi.setOAuthTokenWithSecret(token, secret);
        }
    }
    
    public boolean hasCredentials() {
        return mHttpApi.hasOAuthTokenWithSecret();
    }

    public Credentials authExchange(String phone, String password) throws FoursquareError,
            FoursquareParseException, IOException {
        return (Credentials)mHttpApi.doHttpPost("http://api.playfoursquare.com/v1/authexchange",
                new CredentialsParser(), //
                new BasicNameValuePair("fs_username", phone), //
                new BasicNameValuePair("fs_password", password));
    }
}
