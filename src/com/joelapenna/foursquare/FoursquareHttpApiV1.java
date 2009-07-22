/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare;

import com.joelapenna.foursquare.error.FoursquareCredentialsError;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.http.HttpApi;
import com.joelapenna.foursquare.http.HttpApiWithOAuth;
import com.joelapenna.foursquare.parsers.CredentialsParser;
import com.joelapenna.foursquare.parsers.GroupParser;
import com.joelapenna.foursquare.parsers.TipParser;
import com.joelapenna.foursquare.types.Credentials;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.Foursquared;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquareHttpApiV1 {
    private static final String TAG = "FoursquareHttpApiV1";
    private static final boolean DEBUG = Foursquared.API_DEBUG;

    private static final String HTTP_SCHEME = "http://";
    private static final String DOMAIN = "api.playfoursquare.com";

    private static final String URL_DOMAIN = HTTP_SCHEME + DOMAIN;

    private static final String URL_API_BASE = URL_DOMAIN + "/v1";

    private static final String URL_API_AUTHEXCHANGE = URL_API_BASE + "/authexchange";

    private static final String URL_API_TIPS = URL_API_BASE + "/tips";

    private DefaultHttpClient mHttpClient;
    private HttpApiWithOAuth mHttpApi;

    public FoursquareHttpApiV1() {
        mHttpClient = HttpApi.createHttpClient();
        mHttpApi = new HttpApiWithOAuth(mHttpClient);
    }

    public FoursquareHttpApiV1(String oAuthConsumerKey, String oAuthConsumerSecret) {
        this();
        setOAuthConsumerCredentials(oAuthConsumerKey, oAuthConsumerSecret);
    }

    public void setOAuthConsumerCredentials(String oAuthConsumerKey, String oAuthConsumerSecret) {
        mHttpApi.setOAuthConsumerCredentials(oAuthConsumerKey, oAuthConsumerSecret);
    }

    public void setOAuthTokenWithSecret(String token, String secret) {
        mHttpApi.setOAuthTokenWithSecret(token, secret);
    }

    public boolean hasCredentials() {
        return mHttpApi.hasOAuthTokenWithSecret();
    }

    public Credentials authExchange(String phone, String password) throws FoursquareException,
            FoursquareCredentialsError, FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(URL_API_AUTHEXCHANGE, //
                new BasicNameValuePair("fs_username", phone), //
                new BasicNameValuePair("fs_password", password));
        return (Credentials)mHttpApi.doHttpRequest(httpPost, new CredentialsParser());
    }

    Group tips(String geolat, String geolong, int limit) throws FoursquareException,
            FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(URL_API_TIPS, //
                new BasicNameValuePair("geolat", geolat), // geolat
                new BasicNameValuePair("geolong", geolong), // geolong
                new BasicNameValuePair("l", String.valueOf(limit)));
        return (Group)mHttpApi.doHttpRequest(httpGet, new GroupParser(new GroupParser(new TipParser())));
    }
}
