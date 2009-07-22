/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare;

import com.joelapenna.foursquare.error.FoursquareCredentialsError;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.http.HttpApi;
import com.joelapenna.foursquare.http.HttpApiWithOAuth;
import com.joelapenna.foursquare.parsers.CityParser;
import com.joelapenna.foursquare.parsers.CredentialsParser;
import com.joelapenna.foursquare.parsers.DataParser;
import com.joelapenna.foursquare.parsers.GroupParser;
import com.joelapenna.foursquare.parsers.CheckinParser;
import com.joelapenna.foursquare.parsers.TipParser;
import com.joelapenna.foursquare.parsers.UserParser;
import com.joelapenna.foursquare.parsers.VenueParser;
import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquare.types.Credentials;
import com.joelapenna.foursquare.types.Data;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Venue;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquareHttpApiV1 {
    private static final String TAG = "FoursquareHttpApiV1";
    private static final boolean DEBUG = Foursquare.DEBUG;

    private static final String HTTP_SCHEME = "http://";
    private static final String DOMAIN = "api.playfoursquare.com";

    private static final String URL_DOMAIN = HTTP_SCHEME + DOMAIN;

    private static final String URL_API_BASE = URL_DOMAIN + "/v1";

    private static final String URL_API_AUTHEXCHANGE = URL_API_BASE + "/authexchange";

    private static final String URL_API_ADDVENUE = URL_API_BASE + "/addvenue";
    private static final String URL_API_ADDTIP = URL_API_BASE + "/addtip";
    private static final String URL_API_CITIES = URL_API_BASE + "/cities";
    private static final String URL_API_CHECKCITY = URL_API_BASE + "/checkcity";
    private static final String URL_API_SWITCHCITY = URL_API_BASE + "/switchcity";
    private static final String URL_API_CHECKINS = URL_API_BASE + "/checkins";
    private static final String URL_API_CHECKIN = URL_API_BASE + "/checkin";
    private static final String URL_API_USER = URL_API_BASE + "/user";
    private static final String URL_API_VENUE = URL_API_BASE + "/venue";
    private static final String URL_API_VENUES = URL_API_BASE + "/venues";
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
        if (DEBUG) Log.d(TAG, "Setting consumer key/secret: " + oAuthConsumerKey + " "
                + oAuthConsumerSecret);
        mHttpApi.setOAuthConsumerCredentials(oAuthConsumerKey, oAuthConsumerSecret);
    }

    public void setOAuthTokenWithSecret(String token, String secret) {
        if (DEBUG) Log.d(TAG, "Setting oauth token/secret: " + token + " " + secret);
        mHttpApi.setOAuthTokenWithSecret(token, secret);
    }

    public boolean hasCredentials() {
        return mHttpApi.hasOAuthTokenWithSecret();
    }

    /*
     * /authexchange?oauth_consumer_key=d123...a1bffb5&oauth_consumer_secret=fec...18
     */
    public Credentials authExchange(String phone, String password) throws FoursquareException,
            FoursquareCredentialsError, FoursquareError, IOException {
        if (mHttpApi.hasOAuthTokenWithSecret()) {
            throw new IllegalStateException("Cannot do authExchange with OAuthToken already set");
        }
        HttpPost httpPost = mHttpApi.createHttpPost(URL_API_AUTHEXCHANGE, //
                new BasicNameValuePair("fs_username", phone), //
                new BasicNameValuePair("fs_password", password));
        return (Credentials)mHttpApi.doHttpRequest(httpPost, new CredentialsParser());
    }

    /*
     * /addtip?vid=1234&text=I%20added%20a%20tip&type=todo (type defaults "tip")
     */
    Data addtip(String vid, String text, String type) throws FoursquareException,
            FoursquareCredentialsError, FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(URL_API_ADDTIP, //
                new BasicNameValuePair("vid", vid), //
                new BasicNameValuePair("text", text), //
                new BasicNameValuePair("type", type));
        return (Data)mHttpApi.doHttpRequest(httpPost, new DataParser());
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
        HttpPost httpPost = mHttpApi.createHttpPost(URL_API_ADDVENUE, //
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
        HttpGet httpGet = mHttpApi.createHttpGet(URL_API_CITIES);
        return (Group)mHttpApi.doHttpRequest(httpGet, new GroupParser(new CityParser()));
    }

    /*
     * /checkcity?geolat=37.770900&geolong=-122.436987
     */
    City checkcity(String geolat, String geolong) throws FoursquareException,
            FoursquareCredentialsError, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(URL_API_CHECKCITY, //
                new BasicNameValuePair("geolat", geolat), //
                new BasicNameValuePair("geolong", geolong));
        return (City)mHttpApi.doHttpRequest(httpGet, new CityParser());
    }

    /*
     * /switchcity?cityid=24
     */
    Data switchcity(String cityid) throws FoursquareException, FoursquareCredentialsError,
            FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(URL_API_SWITCHCITY, //
                new BasicNameValuePair("cityid", cityid));
        return (Data)mHttpApi.doHttpRequest(httpGet, new DataParser());
    }

    /*
     * /checkins?cityid=23
     */
    Group checkins(String cityid) throws FoursquareException, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(URL_API_CHECKINS, //
                new BasicNameValuePair("cityid", cityid));
        return (Group)mHttpApi.doHttpRequest(httpGet, new GroupParser(new CheckinParser()));
    }

    /*
     * /checkin?vid=1234&venue=Noc%20Noc&shout=Come%20here&private=0&twitter=1
     */
    Checkin checkin(String vid, String venue, String shout, boolean isPrivate, boolean twitter)
            throws FoursquareException, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(URL_API_CHECKIN, //
                new BasicNameValuePair("vid", vid), //
                new BasicNameValuePair("venue", venue), //
                new BasicNameValuePair("shout", shout), //
                new BasicNameValuePair("private", (isPrivate) ? "1" : "0"), //
                new BasicNameValuePair("twitter", (twitter) ? "1" : "0"));
        return (Checkin)mHttpApi.doHttpRequest(httpGet, new CheckinParser());
    }

    /**
     * /user?uid=9937
     */
    User user(String uid, boolean mayor, boolean badges) throws FoursquareException,
            FoursquareCredentialsError, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(URL_API_USER, //
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
        HttpGet httpGet = mHttpApi.createHttpGet(URL_API_VENUES, //
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
        HttpGet httpGet = mHttpApi.createHttpGet(URL_API_VENUE, //
                new BasicNameValuePair("vid", vid));
        return (Venue)mHttpApi.doHttpRequest(httpGet, new VenueParser());
    }

    /**
     * /tips?geolat=37.770900&geolong=-122.436987&l=1
     */
    Group tips(String geolat, String geolong, int limit) throws FoursquareException,
            FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(URL_API_TIPS, //
                new BasicNameValuePair("geolat", geolat), //
                new BasicNameValuePair("geolong", geolong), //
                new BasicNameValuePair("l", String.valueOf(limit)));
        return (Group)mHttpApi.doHttpRequest(httpGet, new GroupParser(new GroupParser(
                new TipParser())));
    }
}
