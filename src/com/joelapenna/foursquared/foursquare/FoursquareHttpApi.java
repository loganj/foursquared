/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare;

import com.joelapenna.foursquared.foursquare.error.FoursquareError;
import com.joelapenna.foursquared.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquared.foursquare.parsers.AuthParser;
import com.joelapenna.foursquared.foursquare.parsers.IncomingCheckinResponseParser;
import com.joelapenna.foursquared.foursquare.parsers.Parser;
import com.joelapenna.foursquared.foursquare.parsers.VenueGroupParser;
import com.joelapenna.foursquared.foursquare.types.Auth;
import com.joelapenna.foursquared.foursquare.types.FoursquareType;
import com.joelapenna.foursquared.foursquare.types.IncomingCheckin;
import com.joelapenna.foursquared.foursquare.types.VenueGroup;

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
    private static final String URL_API_LOGIN = URL_API_BASE + "/login";
    private static final String URL_API_VENUES = URL_API_BASE + "/venues";

    // Not the normal URL because, well, it doesn't have a normal URL!
    private static final String URL_API_INCOMING = "http://" + DOMAIN + "/incoming/incoming.php";

    private DefaultHttpClient mHttpClient;

    FoursquareHttpApi(DefaultHttpClient httpClient) {
        mHttpClient = httpClient;
    }

    Auth login(String phone, String password) throws FoursquareError, FoursquareParseException,
            IllegalStateException, IOException {
        if (DEBUG) Log.d(TAG, "login()");

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
        Auth auth = new AuthParser().parse(AuthParser.createParser(response.getEntity()
                .getContent()));
        mHttpClient.getCredentialsProvider().setCredentials(new AuthScope(DOMAIN, 80),
                new UsernamePasswordCredentials(phone, password));
        return auth;
    }

    IncomingCheckin checkin(String phone, String venue, boolean silent, boolean twitter,
            String lat, String lng, String cityid) throws FoursquareError,
            FoursquareParseException, IOException {
        return (IncomingCheckin)doHttpPost(URL_API_INCOMING, new IncomingCheckinResponseParser(),
                new BasicNameValuePair("number", phone), // phone
                new BasicNameValuePair("message", "@" + venue), // venue
                new BasicNameValuePair("silent", (silent) ? "1" : "0"), // silent
                new BasicNameValuePair("twitter", (twitter) ? "1" : "0"), // twitter
                new BasicNameValuePair("lat", (lat != null) ? lat : ""), // lat
                new BasicNameValuePair("lng", (lng != null) ? lng : ""), // lng
                new BasicNameValuePair("cityid", (cityid != null) ? cityid : ""));
    }

    /**
     * /api/venues?lat=37.770653&lng=-122.436929&r=1&l=10
     *
     * @return
     */
    VenueGroup venues(String lat, String lng, int radius, int length) throws FoursquareError,
            FoursquareParseException, IOException {
        return (VenueGroup)doHttpPost(URL_API_VENUES, new VenueGroupParser(),
                new BasicNameValuePair("lat", (lat != null) ? lat : ""), // lat
                new BasicNameValuePair("lng", (lng != null) ? lng : ""), // lng
                new BasicNameValuePair("r", String.valueOf(radius)), // radius in miles?
                new BasicNameValuePair("length", String.valueOf(length)));
    }

    private FoursquareType doHttpPost(String url, Parser<? extends FoursquareType> abstractParser,
            NameValuePair... nameValuePairs) throws FoursquareError, FoursquareParseException,
            IOException {
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

        return abstractParser.parse(AuthParser.createParser(response.getEntity().getContent()));
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
     * Create a thread-safe client. This client does not do redirecting, to allow us to capture
     * correct "error" codes.
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
