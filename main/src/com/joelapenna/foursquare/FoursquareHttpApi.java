/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare;

import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.http.HttpApi;
import com.joelapenna.foursquare.parsers.DataParser;
import com.joelapenna.foursquare.types.Data;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
class FoursquareHttpApi {
    private static final String TAG = "FoursquareHttpApi";
    private static final boolean DEBUG = Foursquare.DEBUG;

    private static final String HTTP_SCHEME = "http://";
    private static final String DOMAIN = "foursquare.com.";

    private static final String URL_DOMAIN = HTTP_SCHEME + DOMAIN;

    private static final String URL_API_BASE = URL_DOMAIN + "/api";
    private static final String URL_API_UPDATE = URL_API_BASE + "/update";

    // Used for stuff.
    private final HttpApi mHttpApi;
    private final DefaultHttpClient mHttpClient;

    private String mPhone = null;
    private String mPassword = null;

    public FoursquareHttpApi(String clientVersion) {
        mHttpClient = HttpApi.createHttpClient();
        mHttpApi = new HttpApi(mHttpClient, clientVersion);
    }

    void setCredentials(String phone, String password) {
        mPhone = phone;
        mPassword = password;
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            if (DEBUG) Log.d(TAG, "Clearing Credentials");
            mHttpClient.getCredentialsProvider().clear();
        } else {
            if (DEBUG) Log.d(TAG, "Setting Phone/Password");
            mHttpClient.getCredentialsProvider().setCredentials(new AuthScope(DOMAIN, 80),
                    new UsernamePasswordCredentials(phone, password));
        }
    }

    boolean hasCredentials() {
        return !(TextUtils.isEmpty(mPhone) || TextUtils.isEmpty(mPassword));
    }

    /*
     * /api/update?tipid=6777&status=ignore /api/update?tipid=6777&status=done
     */
    Data update(String status, String tipid) throws FoursquareException, FoursquareError,
            IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(URL_API_UPDATE, //
                new BasicNameValuePair("status", status), //
                new BasicNameValuePair("tipid", tipid));
        return (Data)mHttpApi.doHttpRequest(httpPost, new DataParser());
    }
}
