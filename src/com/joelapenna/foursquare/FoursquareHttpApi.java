/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare;

import com.joelapenna.foursquare.error.FoursquareCredentialsError;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.http.HttpApi;
import com.joelapenna.foursquare.parsers.AbstractParser;
import com.joelapenna.foursquare.parsers.DataParser;
import com.joelapenna.foursquare.parsers.GroupParser;
import com.joelapenna.foursquare.parsers.classic.AuthParser;
import com.joelapenna.foursquare.parsers.classic.CheckinParser;
import com.joelapenna.foursquare.parsers.classic.CheckinResponseParser;
import com.joelapenna.foursquare.parsers.classic.TipParser;
import com.joelapenna.foursquare.parsers.classic.UserParser;
import com.joelapenna.foursquare.parsers.classic.VenueParser;
import com.joelapenna.foursquare.types.Data;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.classic.Auth;
import com.joelapenna.foursquare.types.classic.Checkin;
import com.joelapenna.foursquare.types.classic.User;
import com.joelapenna.foursquare.types.classic.Venue;
import com.joelapenna.foursquared.FoursquaredSettings;

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

    // Gets the html description of a checkin.
    private static final String URL_BREAKDOWN = URL_DOMAIN + "/incoming/breakdown";

    // Get the html achievements page.
    private static final String URL_ACHIEVEMENTS = URL_DOMAIN + "/web/iphone/achievements";

    // Get the html me page.
    private static final String URL_ME = URL_DOMAIN + "/web/iphone/me";

    // Used for stuff.
    private final HttpApi mHttpApi;
    private final DefaultHttpClient mHttpClient;

    public FoursquareHttpApi() {
        mHttpClient = HttpApi.createHttpClient();
        mHttpApi = new HttpApi(mHttpClient);
    }

    void setCredentials(String phone, String password) {
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            if (DEBUG) Log.d(TAG, "Clearing Credentials");
            mHttpClient.getCredentialsProvider().clear();
        } else {
            if (DEBUG) Log.d(TAG, "Setting Phone/Password");
            mHttpClient.getCredentialsProvider().setCredentials(new AuthScope(DOMAIN, 80),
                    new UsernamePasswordCredentials(phone, password));
        }
    }

    Auth login(String phone, String password) throws FoursquareError, FoursquareParseException,
            IllegalStateException, IOException {
        if (DEBUG) Log.d(TAG, "login()");

        HttpPost httpPost = mHttpApi.createHttpPost(URL_API_LOGIN, //
                new BasicNameValuePair("phone", phone), //
                new BasicNameValuePair("pass", password));

        HttpResponse response = mHttpApi.executeHttpRequest(httpPost);
        if (response == null) {
            if (DEBUG) Log.d(TAG, "execute() call for the httpPost generated an exception;");
            return null;
        }

        switch (response.getStatusLine().getStatusCode()) {
            case 200:
                break;
            default:
                if (DEBUG) Log.d(TAG, "Default case for status code reached.");
                return null;
        }
        // Auth results are wrapped in a "data" element. We shouldn't use it, rather we should use
        // the first item in it.
        Auth auth = (Auth)new GroupParser(new AuthParser()).parse(
                AbstractParser.createXmlPullParser(response.getEntity().getContent())).get(0);
        setCredentials(phone, password);
        return auth;
    }

    /*
     * /api/add?type=XXX&text=add%20a%20tip&vid=44794&lat=37.770741&lng=-122.436854&cityid=23
     */
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

    Checkin checkin(String phone, String venue, boolean silent, boolean twitter, String lat,
            String lng, String cityid) throws FoursquareException, FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(URL_API_INCOMING, //
                new BasicNameValuePair("number", phone), //
                new BasicNameValuePair("message", "@" + venue), //
                new BasicNameValuePair("silent", (silent) ? "1" : "0"), //
                new BasicNameValuePair("twitter", (twitter) ? "1" : "0"), //
                new BasicNameValuePair("lat", lat), //
                new BasicNameValuePair("lng", lng), //
                new BasicNameValuePair("cityid", cityid), //
                new BasicNameValuePair("output", "xml"));
        return (Checkin)mHttpApi.doHttpRequest(httpPost, new CheckinResponseParser());
    }

    /**
     * /api/checkins?lat=37.770653&lng=-122.436929&r=1&l=10
     */
    @Deprecated
    Group checkins(String cityid, String lat, String lng) throws FoursquareException,
            FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(URL_API_CHECKINS, //
                new BasicNameValuePair("cityid", cityid), //
                new BasicNameValuePair("lat", lat), //
                new BasicNameValuePair("lng", lng));
        return (Group)mHttpApi.doHttpRequest(httpPost, new GroupParser(new GroupParser(
                new CheckinParser())));
    }

    /**
     * /api/todo?cityid=23&lat=37.770900&lng=-122.436987
     */
    @Deprecated
    Group todos(String cityid, String lat, String lng) throws FoursquareException, FoursquareError,
            IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(URL_API_TODO, //
                new BasicNameValuePair("cityid", cityid), //
                new BasicNameValuePair("lat", lat), //
                new BasicNameValuePair("lng", lng));
        return (Group)mHttpApi.doHttpRequest(httpPost, new GroupParser(new GroupParser(
                new TipParser())));
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

    User user() throws FoursquareException, FoursquareError, IOException {
        return (User)mHttpApi
                .doHttpRequest(mHttpApi.createHttpPost(URL_API_USER), new UserParser());
    }

    /**
     * /api/venue?vid=1234
     */
    @Deprecated
    Venue venue(String id) throws FoursquareException, FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(URL_API_VENUE,
                new BasicNameValuePair("vid", id));
        return (Venue)mHttpApi.doHttpRequest(httpPost, new VenueParser());
    }

    /**
     * /api/venues?lat=37.770653&lng=-122.436929&r=1&l=10
     */
    @Deprecated
    Group venues(String query, String lat, String lng, int radius, int length)
            throws FoursquareException, FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(URL_API_VENUES, //
                new BasicNameValuePair("q", query), //
                new BasicNameValuePair("lat", lat), //
                new BasicNameValuePair("lng", lng), //
                new BasicNameValuePair("r", String.valueOf(radius)), //
                new BasicNameValuePair("l", String.valueOf(length)));
        return (Group)mHttpApi.doHttpRequest(httpPost, new GroupParser(new GroupParser(
                new VenueParser())));
    }

    /**
     * /web/iphone/achievements?task=unlocked&uid=1818&cityid=23
     */
    String achievements(String cityid, String task, String userId) throws FoursquareError,
            FoursquareParseException, IOException, FoursquareCredentialsError {
        return mHttpApi.doHttpPost(URL_ACHIEVEMENTS, //
                new BasicNameValuePair("cityid", cityid), //
                new BasicNameValuePair("task", task), //
                new BasicNameValuePair("uid", userId) //
                );
    }

    /**
     * /incoming/breakdown?cid=67889&uid=9232&client=iphone
     */
    String breakdown(String userId, String checkinId) throws FoursquareError,
            FoursquareParseException, IOException, FoursquareCredentialsError {
        return mHttpApi.doHttpPost(URL_BREAKDOWN, //
                new BasicNameValuePair("uid", userId), //
                new BasicNameValuePair("cid", checkinId), //
                new BasicNameValuePair("client", "android") //
                );
    }

    /**
     * /web/iphone/me?uid=9232&view=mini&cityid=23
     */
    String me(String cityid, String userId) throws FoursquareError, FoursquareParseException,
            IOException, FoursquareCredentialsError {
        return mHttpApi.doHttpPost(URL_ME, // url
                new BasicNameValuePair("cityid", cityid), //
                new BasicNameValuePair("view", "mini"), //
                new BasicNameValuePair("uid", userId) //
                );
    }
}
