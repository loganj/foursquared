/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
class FoursquareHttpApi {
    private static final String TAG = "FoursquareHttpApi";
    private static final boolean DEBUG = Foursquare.DEBUG;

    private static final String MOBILE_DOMAIN = "m.playfoursquare.com";
    public static final String URL_BASE = "http://" + MOBILE_DOMAIN;
    public static final String URL_CHECKIN = URL_BASE + "/checkin";
    public static final String URL_LOGIN = URL_BASE + "/login";

    private HttpClient mHttpClient;
    private boolean mLoggedIn = false;
    private Cookie mCookieCityId = null;
    private Cookie mCookieSessionId = null;

    FoursquareHttpApi(HttpClient httpClient) {
        mHttpClient = httpClient;
    }

    boolean login(String email, String password) {
        if (DEBUG) Log.d(TAG, "login()");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("password", password));
        HttpPost httpPost = createHttpPost(URL_LOGIN, params);

        HttpResponse response = executeHttpPost(httpPost);
        if (response == null) {
            if (DEBUG) Log.d(TAG, "execute() call for the httpPost generated an exception;");
            return false;
        }

        if (DEBUG) {
            Log.d(TAG, "Status for " + response + ": " + response.getStatusLine().toString());
        }
        switch (response.getStatusLine().getStatusCode()) {
            case 302:
                // This is the success case, we've acquired an authenticated
                // session cookie and are being redirected to the main page.
                break;
            case 200:
                // Oddly, this means login failed, because we're supposed to
                // hit a 302 first!
                // NO BREAK - Default case!
            default:
                if (DEBUG) Log.d(TAG, "Default case for status code reached.");
                mLoggedIn = false;
                return mLoggedIn;
        }

        // Make sure we've received the cookies we need!
        boolean foundCookieCityId = false;
        boolean foundSessionId = false;
        Header[] headers = response.getHeaders("Set-Cookie");
        for (int i = 0; i < headers.length; i++) {
            Header header = headers[i];
            if (DEBUG) Log.d(TAG, "Header: " + header.getName() + ": " + header.getValue());
            if (header.getName() == "SESSIONID") foundSessionId = true;
            if (header.getName() == "cookieCityId") foundCookieCityId = true;
        }
        assert foundCookieCityId;
        assert foundSessionId;

        // Save the cookies we're storing for future use?
        List<Cookie> cookies = ((DefaultHttpClient)mHttpClient).getCookieStore().getCookies();
        int cookieCount = cookies.size();
        for (int i = 0; i < cookieCount; i++) {
            Cookie cookie = cookies.get(i);
            if (cookie.getName() == "SESSIONID") mCookieSessionId = cookie;
            if (cookie.getName() == "cookieCityId") mCookieCityId = cookie;
        }
        assert (mCookieSessionId != null);
        assert (mCookieCityId != null);

        mLoggedIn = true;
        return mLoggedIn;
    }

    /**
     * Update Foursquare with a user's new location!
     *
     * @param venue
     * @param privacy
     * @param twitter
     * @return
     */
    boolean checkin(String venue, boolean privacy, boolean twitter) {
        if (DEBUG) Log.d(TAG, "checkin(" + venue + ", " + privacy + ", " + twitter);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("venue", venue));
        params.add(new BasicNameValuePair("privacy", (privacy) ? "1" : "0"));
        params.add(new BasicNameValuePair("twitter", (twitter) ? "1" : "0"));
        HttpPost httpPost = createHttpPost(URL_CHECKIN, params);

        HttpResponse response = executeHttpPost(httpPost);
        if (response == null) {
            if (DEBUG) Log.d(TAG, "execute() call for the httpPost generated an exception;");
            return false;
        }

        if (DEBUG) {
            Log.d(TAG, "Status for " + response + ": " + response.getStatusLine().toString());
        }
        switch (response.getStatusLine().getStatusCode()) {
            case 302:
                // This is the success case, we've acquired an authenticated
                // session cookie and are being redirected to the main page.
                break;
            case 200:
                // Oddly, this means login failed, because we're supposed to
                // hit a 302 first!
                // NO BREAK - Default case!
            default:
                if (DEBUG) Log.d(TAG, "Default case for status code reached.");
                return false;
        }
        return true;
    }

    private boolean validateCheckinResponse(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return false;
        }
        try {

            InputStream entityStream = entity.getContent();
            CheckinResponseParser checkinParser = new CheckinResponseParser(entityStream);
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Check that this instance is logged in.
     *
     * @return
     */
    boolean isLoggedIn() {
        return mLoggedIn;
    }

    /**
     * execute() an httpPost catching exceptions and returning null instead.
     *
     * @param httpPost
     * @return
     */
    private HttpResponse executeHttpPost(HttpPost httpPost) {
        HttpResponse response;
        try {
            response = mHttpClient.execute(httpPost);
        } catch (ClientProtocolException e) {
            Log.d(TAG, "ClientProtocolException for " + httpPost, e);
            return null;
        } catch (IOException e) {
            Log.d(TAG, "IOException for " + httpPost, e);
            return null;
        }
        return response;
    }

    /**
     * Create a thread-safe client. This client does not do redirecting, to
     * allow us to capture correct "error" codes.
     *
     * @return HttpClient
     */
    public static final HttpClient createHttpClient() {
        // Sets up the http part of the service.
        final SchemeRegistry supportedSchemes = new SchemeRegistry();

        // Register the "http" protocol scheme, it is required
        // by the default operator to look up socket factories.
        final SocketFactory sf = PlainSocketFactory.getSocketFactory();
        supportedSchemes.register(new Scheme("http", sf, 80));

        // Set some client http client parameter defaults.
        final HttpParams httpParams = createHttpParams();
        HttpClientParams.setRedirecting(httpParams, false);

        final ClientConnectionManager ccm = new ThreadSafeClientConnManager(httpParams,
                supportedSchemes);
        return new DefaultHttpClient(ccm, httpParams);
    }

    /**
     * Create the default HTTP protocol parameters.
     */
    private static final HttpParams createHttpParams() {
        // prepare parameters
        final HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUseExpectContinue(params, true);
        return params;
    }

    private static HttpPost createHttpPost(String url, List<NameValuePair> params) {
        HttpPost httpPost = new HttpPost(url);

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e1) {
            throw new IllegalArgumentException("Unable to encode http parameters.");
        }
        return httpPost;
    }
}
