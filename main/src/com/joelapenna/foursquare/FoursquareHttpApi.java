/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare;

import com.joelapenna.foursquare.error.FoursquareCredentialsException;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.http.HttpApi;
import com.joelapenna.foursquare.parsers.AbstractParser;
import com.joelapenna.foursquare.parsers.DataParser;
import com.joelapenna.foursquare.parsers.GroupParser;
import com.joelapenna.foursquare.types.Data;
import com.joelapenna.foursquare.types.Group;

import org.apache.http.HttpResponse;
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
@SuppressWarnings("deprecation")
class FoursquareHttpApi {
    private static final String TAG = "FoursquareHttpApi";
    private static final boolean DEBUG = Foursquare.DEBUG;

    private static final String HTTP_SCHEME = "http://";
    private static final String DOMAIN = "playfoursquare.com";

    private static final String URL_DOMAIN = HTTP_SCHEME + DOMAIN;

    private static final String URL_API_BASE = URL_DOMAIN + "/api";
    private static final String URL_API_ADD = URL_API_BASE + "/add";
    private static final String URL_API_CHECKINS = URL_API_BASE + "/checkins";
    private static final String URL_API_LOGIN = URL_API_BASE + "/login";
    private static final String URL_API_TODO = URL_API_BASE + "/todo";
    private static final String URL_API_UPDATE = URL_API_BASE + "/update";
    private static final String URL_API_USER = URL_API_BASE + "/user";
    private static final String URL_API_VENUE = URL_API_BASE + "/venue";
    private static final String URL_API_VENUES = URL_API_BASE + "/venues";

    // Not the normal URL because, well, it doesn't have a normal URL!
    private static final String URL_API_INCOMING = URL_DOMAIN + "/incoming/incoming.php";

    // Gets the html description of a checkinResult.
    private static final String URL_BREAKDOWN = URL_DOMAIN + "/incoming/breakdown";

    // Get the html achievements page.
    private static final String URL_ACHIEVEMENTS = URL_DOMAIN + "/web/iphone/achievements";

    // Get the html me page.
    private static final String URL_ME = URL_DOMAIN + "/web/iphone/me";

    // Used for stuff.
    private final HttpApi mHttpApi;
    private final DefaultHttpClient mHttpClient;

    private String mPhone = null;
    private String mPassword = null;

    public FoursquareHttpApi() {
        mHttpClient = HttpApi.createHttpClient();
        mHttpApi = new HttpApi(mHttpClient);
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
     * /api/add?type=XXX&text=add%20a%20tip&vid=44794&lat=37.770741&lng=-122.436854&cityid=23
     */
    @Deprecated
    Data add(String type, String text, String vid, String lat, String lng, String cityid)
            throws FoursquareException, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(URL_API_ADD, //
                new BasicNameValuePair("type", type), //
                new BasicNameValuePair("text", text), //
                new BasicNameValuePair("vid", vid), //
                new BasicNameValuePair("lat", lat), //
                new BasicNameValuePair("lng", lng), //
                new BasicNameValuePair("cityid", cityid));
        return (Data)mHttpApi.doHttpRequest(httpPost, new DataParser());
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

    /**
     * /web/iphone/achievements?task=unlocked&uid=1818&cityid=23
     */
    @Deprecated
    String achievements(String cityid, String task, String userId)
            throws FoursquareCredentialsException, FoursquareParseException, FoursquareException,
            IOException {
        return mHttpApi.doHttpPost(URL_ACHIEVEMENTS, //
                new BasicNameValuePair("cityid", cityid), //
                new BasicNameValuePair("task", task), //
                new BasicNameValuePair("uid", userId) //
                );
    }

    /**
     * /incoming/breakdown?cid=67889&uid=9232&client=iphone
     */
    @Deprecated
    String breakdown(String userId, String checkinId) throws FoursquareCredentialsException,
            FoursquareParseException, FoursquareException, IOException {
        return mHttpApi.doHttpPost(URL_BREAKDOWN, //
                new BasicNameValuePair("uid", userId), //
                new BasicNameValuePair("cid", checkinId), //
                new BasicNameValuePair("client", "android") //
                );
    }

    /**
     * /web/iphone/me?uid=9232&view=mini&cityid=23
     */
    @Deprecated
    String me(String cityid, String userId) throws FoursquareCredentialsException,
            FoursquareParseException, FoursquareException, IOException {
        return mHttpApi.doHttpPost(URL_ME, // url
                new BasicNameValuePair("cityid", cityid), //
                new BasicNameValuePair("view", "mini"), //
                new BasicNameValuePair("uid", userId) //
                );
    }
}
