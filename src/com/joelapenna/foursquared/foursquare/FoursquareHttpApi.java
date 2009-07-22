/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare;

import com.joelapenna.foursquared.foursquare.error.FoursquareError;
import com.joelapenna.foursquared.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquared.foursquare.parsers.AuthParser;
import com.joelapenna.foursquared.foursquare.parsers.CheckinResponseParser;
import com.joelapenna.foursquared.foursquare.parsers.IncomingCheckinResponseParser;
import com.joelapenna.foursquared.foursquare.parsers.Parser;
import com.joelapenna.foursquared.foursquare.types.Auth;
import com.joelapenna.foursquared.foursquare.types.Checkin;
import com.joelapenna.foursquared.foursquare.types.FoursquareType;
import com.joelapenna.foursquared.foursquare.types.IncomingCheckin;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
class FoursquareHttpApi {
    private static final String TAG = "FoursquareHttpApi";
    private static final boolean DEBUG = Foursquare.DEBUG;

    private static final String CLIENT_VERSION = "iPhone 20090301";
    private static final String CLIENT_VERSION_HEADER = "X_foursquare_client_version";

    private static final String DOMAIN = "playfoursquare.com";
    private static final String URL_API_BASE = "http://" + DOMAIN + "/api";
    private static final String URL_API_CHECKIN = URL_API_BASE + "/checkin";
    private static final String URL_API_LOGIN = URL_API_BASE + "/login";

    // Not the normal URL because, well, it doesn't have a normal URL!
    private static final String URL_API_INCOMING = "http://" + DOMAIN + "/incoming/incoming.php";

    private DefaultHttpClient mHttpClient;
    private String mPhone;

    FoursquareHttpApi(DefaultHttpClient httpClient) {
        mHttpClient = httpClient;
    }

    Auth login(String phone, String password) {
        if (DEBUG) Log.d(TAG, "login()");
        mPhone = phone;

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("phone", phone));
        params.add(new BasicNameValuePair("pass", password));
        HttpPost httpPost = createHttpPost(URL_API_LOGIN, params);

        HttpResponse response = executeHttpPost(httpPost);
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

        try {
            Auth auth = new AuthParser().parse(response.getEntity().getContent());
            mHttpClient.getCredentialsProvider().setCredentials(new AuthScope(DOMAIN, 80),
                    new UsernamePasswordCredentials(phone, password));
            return auth;
        } catch (FoursquareError e) {
            // TODO Auto-generated catch block
            if (DEBUG) Log.d(TAG, "FoursquareError", e);
        } catch (FoursquareParseException e) {
            // TODO Auto-generated catch block
            if (DEBUG) Log.d(TAG, "FoursquareParseException", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            if (DEBUG) Log.d(TAG, "IOException", e);
        }
        return null;
    }

    /**
     * Update Foursquare with a user's new location!
     *
     * @param venue
     * @param privacy
     * @param twitter
     * @return
     */
    @Deprecated
    Checkin checkin(String venue, boolean privacy, boolean twitter) {
        if (DEBUG) Log.d(TAG, "checkin: " + venue + ", " + privacy + ", " + twitter);
        return (Checkin)doHttpPost(URL_API_CHECKIN, new CheckinResponseParser(),
                new BasicNameValuePair("venue", venue), new BasicNameValuePair("privacy",
                        (privacy) ? "1" : "0"), new BasicNameValuePair("twitter", (twitter) ? "1"
                        : "0"));
    }

    IncomingCheckin checkin(String phone, String venue, boolean silent, boolean twitter, String lat, String lng, String cityid) {
        return (IncomingCheckin)doHttpPost(
                URL_API_INCOMING,
                new IncomingCheckinResponseParser(),
                new BasicNameValuePair("number", phone),
                new BasicNameValuePair("message", "@" + venue),
                new BasicNameValuePair("silent", (silent) ? "1" : "0"),
                new BasicNameValuePair("twitter", (twitter) ? "1" : "0"),
                new BasicNameValuePair("lat", lat),
                new BasicNameValuePair("lng", lng),
                new BasicNameValuePair("cityid", cityid),
                new BasicNameValuePair("phone", mPhone));
    }

    private FoursquareType doHttpPost(String url, Parser<? extends FoursquareType> abstractParser,
            NameValuePair... nameValuePairs) {
        if (DEBUG) Log.d(TAG, "doHttpPost()");
        HttpPost httpPost = createHttpPost(url, Arrays.asList(nameValuePairs));

        HttpResponse response = executeHttpPost(httpPost);
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

        try {
            return abstractParser.parse(response.getEntity().getContent());
        } catch (FoursquareError e) {
            // TODO Auto-generated catch block
            if (DEBUG) Log.d(TAG, "FoursquareError", e);
        } catch (FoursquareParseException e) {
            // TODO Auto-generated catch block
            if (DEBUG) Log.d(TAG, "FoursquareParseException", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            if (DEBUG) Log.d(TAG, "IOException", e);
        }
        return null;
    }

    /**
     * execute() an httpPost catching exceptions and returning null instead.
     *
     * @param httpPost
     * @return
     */
    private HttpResponse executeHttpPost(HttpPost httpPost) {
        if (DEBUG) Log.d(TAG, "executing HttpPost for: " + httpPost.getURI().toString());
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
    public static final DefaultHttpClient createHttpClient() {
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

    private static final HttpPost createHttpPost(String url, List<NameValuePair> params) {
        if (DEBUG) Log.d(TAG, "creating HttpPost for: " + url);
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(CLIENT_VERSION_HEADER, CLIENT_VERSION);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e1) {
            throw new IllegalArgumentException("Unable to encode http parameters.");
        }
        return httpPost;
    }
}
