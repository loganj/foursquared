/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare;

import com.joelapenna.foursquare.error.FoursquareCredentialsError;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.http.HttpApi;
import com.joelapenna.foursquare.http.HttpApiWithOAuth;
import com.joelapenna.foursquare.parsers.CheckinParser;
import com.joelapenna.foursquare.parsers.CheckinResultParser;
import com.joelapenna.foursquare.parsers.CityParser;
import com.joelapenna.foursquare.parsers.CredentialsParser;
import com.joelapenna.foursquare.parsers.DataParser;
import com.joelapenna.foursquare.parsers.GroupParser;
import com.joelapenna.foursquare.parsers.TipParser;
import com.joelapenna.foursquare.parsers.UserParser;
import com.joelapenna.foursquare.parsers.VenueParser;
import com.joelapenna.foursquare.types.CheckinResult;
import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquare.types.Credentials;
import com.joelapenna.foursquare.types.Data;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Venue;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquareHttpApiV1 {
    private static final String TAG = "FoursquareHttpApiV1";
    private static final boolean DEBUG = Foursquare.DEBUG;

    private static final String URL_API_AUTHEXCHANGE = "/authexchange";

    private static final String URL_API_ADDVENUE = "/addvenue";
    private static final String URL_API_ADDTIP = "/addtip";
    private static final String URL_API_CITIES = "/cities";
    private static final String URL_API_CHECKCITY = "/checkcity";
    private static final String URL_API_SWITCHCITY = "/switchcity";
    private static final String URL_API_CHECKINS = "/checkins";
    private static final String URL_API_CHECKIN = "/checkin";
    private static final String URL_API_USER = "/user";
    private static final String URL_API_VENUE = "/venue";
    private static final String URL_API_VENUES = "/venues";
    private static final String URL_API_TIPS = "/tips";

    private final DefaultHttpClient mHttpClient = HttpApi.createHttpClient();
    private HttpApiWithOAuth mHttpApi = new HttpApiWithOAuth(mHttpClient);

    private final String mApiBaseUrl;
    private final AuthScope mAuthScope;

    // XXX Foursquare requires "pre-emptive" basic auth, it won't do the normal challenge, response
    // stuff.
    HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {

        @Override
        public void process(final HttpRequest request, final HttpContext context)
                throws HttpException, IOException {

            AuthState authState = (AuthState)context.getAttribute(ClientContext.TARGET_AUTH_STATE);
            CredentialsProvider credsProvider = (CredentialsProvider)context
                    .getAttribute(ClientContext.CREDS_PROVIDER);
            HttpHost targetHost = (HttpHost)context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

            // If not auth scheme has been initialized yet
            if (authState.getAuthScheme() == null) {
                AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
                // Obtain credentials matching the target host
                org.apache.http.auth.Credentials creds = credsProvider.getCredentials(authScope);
                // If found, generate BasicScheme preemptively
                if (creds != null) {
                    authState.setAuthScheme(new BasicScheme());
                    authState.setCredentials(creds);
                }
            }
        }

    };

    public FoursquareHttpApiV1() {
        this("api.playfoursquare.com");
    }

    public FoursquareHttpApiV1(String domain) {
        mApiBaseUrl = "http://" + domain + "/v1";
        mAuthScope = new AuthScope(domain, 80);

        mHttpClient.addRequestInterceptor(preemptiveAuth, 0);
    }

    void setCredentials(String phone, String password) {
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            if (DEBUG) Log.d(TAG, "Clearing Credentials");
            mHttpClient.getCredentialsProvider().clear();
        } else {
            if (DEBUG) Log.d(TAG, "Setting Phone/Password: " + phone + " " + password);
            mHttpClient.getCredentialsProvider().setCredentials(mAuthScope,
                    new UsernamePasswordCredentials(phone, password));
        }
    }

    public boolean hasCredentials() {
        return mHttpClient.getCredentialsProvider().getCredentials(mAuthScope) != null;
    }

    public void setOAuthConsumerCredentials(String oAuthConsumerKey, String oAuthConsumerSecret) {
        if (DEBUG) Log.d(TAG, "Setting consumer key/secret: " + oAuthConsumerKey + " "
                + oAuthConsumerSecret);
        mHttpApi.setOAuthConsumerCredentials(oAuthConsumerKey, oAuthConsumerSecret);
    }

    public void setOAuthTokenWithSecret(String token, String secret) {
        if (DEBUG) Log.d(TAG, "Setting oauth token/secret: " + token + " " + secret);
        mHttpApi.setOAuthTokenWithSecret(token, secret);
    }

    public boolean hasOAuthTokenWithSecret() {
        return mHttpApi.hasOAuthTokenWithSecret();
    }

    /*
     * /authexchange?oauth_consumer_key=d123...a1bffb5&oauth_consumer_secret=fec...18
     */
    public Credentials authExchange(String phone, String password) throws FoursquareException,
            FoursquareCredentialsError, FoursquareError, IOException {
        // XXX Don't do a real lookup.
        if (false) {
            if (mHttpApi.hasOAuthTokenWithSecret()) {
                throw new IllegalStateException(
                        "Cannot do authExchange with OAuthToken already set");
            }
            HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_AUTHEXCHANGE), //
                    new BasicNameValuePair("fs_username", phone), //
                    new BasicNameValuePair("fs_password", password));
            return (Credentials)mHttpApi.doHttpRequest(httpPost, new CredentialsParser());
        }
        // Instead return a hack.
        Credentials credentials = new Credentials();
        credentials.setOauthToken("XXX");
        credentials.setOauthTokenSecret("XXX");
        return credentials;
    }

    /*
     * /addtip?vid=1234&text=I%20added%20a%20tip&type=todo (type defaults "tip")
     */
    Tip addtip(String vid, String text, String type) throws FoursquareException,
            FoursquareCredentialsError, FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_ADDTIP), //
                new BasicNameValuePair("vid", vid), //
                new BasicNameValuePair("text", text), //
                new BasicNameValuePair("type", type));
        return (Tip)mHttpApi.doHttpRequest(httpPost, new TipParser());
    }

    /**
     * @param name the name of the venue
     * @param address the address of the venue (e.g., "202 1st Avenue")
     * @param crossstreet the cross streets (e.g., "btw Grand & Broome")
     * @param city the city name where this venue is
     * @param state the state where the city is
     * @param zip (optional) the ZIP code for the venue
     * @param cityid (required) the foursquare cityid where the venue is
     * @param phone (optional) the phone number for the venue
     * @return
     * @throws FoursquareException
     * @throws FoursquareCredentialsError
     * @throws FoursquareError
     * @throws IOException
     */
    Venue addvenue(String name, String address, String crossstreet, String city, String state,
            String zip, String cityid, String phone) throws FoursquareException,
            FoursquareCredentialsError, FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_ADDVENUE), //
                new BasicNameValuePair("name", name), //
                new BasicNameValuePair("address", address), //
                new BasicNameValuePair("crossstreet", crossstreet), //
                new BasicNameValuePair("city", city), //
                new BasicNameValuePair("state", state), //
                new BasicNameValuePair("zip", zip), //
                new BasicNameValuePair("cityid", cityid), //
                new BasicNameValuePair("phone", phone) //
                );
        return (Venue)mHttpApi.doHttpRequest(httpPost, new VenueParser());
    }

    /*
     * /cities
     */
    Group cities() throws FoursquareException, FoursquareCredentialsError, FoursquareError,
            IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_CITIES));
        return (Group)mHttpApi.doHttpRequest(httpGet, new GroupParser(new CityParser()));
    }

    /*
     * /checkcity?geolat=37.770900&geolong=-122.436987
     */
    City checkcity(String geolat, String geolong) throws FoursquareException,
            FoursquareCredentialsError, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_CHECKCITY), //
                new BasicNameValuePair("geolat", geolat), //
                new BasicNameValuePair("geolong", geolong));
        return (City)mHttpApi.doHttpRequest(httpGet, new CityParser());
    }

    /*
     * /switchcity?cityid=24
     */
    Data switchcity(String cityid) throws FoursquareException, FoursquareCredentialsError,
            FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_SWITCHCITY), //
                new BasicNameValuePair("cityid", cityid));
        return (Data)mHttpApi.doHttpRequest(httpPost, new DataParser());
    }

    /*
     * /checkins?cityid=23
     */
    Group checkins(String cityid) throws FoursquareException, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_CHECKINS), //
                new BasicNameValuePair("cityid", cityid));
        return (Group)mHttpApi.doHttpRequest(httpGet, new GroupParser(new CheckinParser()));
    }

    /*
     * /checkin?vid=1234&venue=Noc%20Noc&shout=Come%20here&private=0&twitter=1
     */
    CheckinResult checkin(String vid, String venue, String shout, boolean isPrivate, boolean twitter)
            throws FoursquareException, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_CHECKIN), //
                new BasicNameValuePair("vid", vid), //
                new BasicNameValuePair("venue", venue), //
                new BasicNameValuePair("shout", shout), //
                new BasicNameValuePair("private", (isPrivate) ? "1" : "0"), //
                new BasicNameValuePair("twitter", (twitter) ? "1" : "0"));
        return (CheckinResult)mHttpApi.doHttpRequest(httpGet, new CheckinResultParser());
    }

    /**
     * /user?uid=9937
     */
    User user(String uid, boolean mayor, boolean badges) throws FoursquareException,
            FoursquareCredentialsError, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_USER), //
                new BasicNameValuePair("uid", uid), //
                new BasicNameValuePair("mayor", (mayor) ? "1" : "0"), //
                new BasicNameValuePair("badges", (badges) ? "1" : "0"));
        return (User)mHttpApi.doHttpRequest(httpGet, new UserParser());
    }

    /**
     * /venues?geolat=37.770900&geolong=-122.43698
     */
    Group venues(String geolat, String geolong, String query, int radius, int limit)
            throws FoursquareException, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_VENUES), //
                new BasicNameValuePair("geolat", geolat), //
                new BasicNameValuePair("geolong", geolong), //
                new BasicNameValuePair("q", query), //
                new BasicNameValuePair("r", String.valueOf(radius)), //
                new BasicNameValuePair("l", String.valueOf(limit)));
        return (Group)mHttpApi.doHttpRequest(httpGet, new GroupParser(new GroupParser(
                new VenueParser())));
    }

    /**
     * /venue?vid=1234
     */
    Venue venue(String vid) throws FoursquareException, FoursquareCredentialsError,
            FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_VENUE), //
                new BasicNameValuePair("vid", vid));
        return (Venue)mHttpApi.doHttpRequest(httpGet, new VenueParser());
    }

    /**
     * /tips?geolat=37.770900&geolong=-122.436987&l=1
     */
    Group tips(String geolat, String geolong, int limit) throws FoursquareException,
            FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_TIPS), //
                new BasicNameValuePair("geolat", geolat), //
                new BasicNameValuePair("geolong", geolong), //
                new BasicNameValuePair("l", String.valueOf(limit)));
        return (Group)mHttpApi.doHttpRequest(httpGet, new GroupParser(new GroupParser(
                new TipParser())));
    }

    private String fullUrl(String url) {
        return mApiBaseUrl + url;
    }
}
