/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare;

import com.joelapenna.foursquare.error.FoursquareCredentialsException;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.http.AbstractHttpApi;
import com.joelapenna.foursquare.http.HttpApi;
import com.joelapenna.foursquare.http.HttpApiWithBasicAuth;
import com.joelapenna.foursquare.http.HttpApiWithOAuth;
import com.joelapenna.foursquare.parsers.CategoryParser;
import com.joelapenna.foursquare.parsers.CheckinParser;
import com.joelapenna.foursquare.parsers.CheckinResultParser;
import com.joelapenna.foursquare.parsers.CityParser;
import com.joelapenna.foursquare.parsers.CredentialsParser;
import com.joelapenna.foursquare.parsers.GroupParser;
import com.joelapenna.foursquare.parsers.TipParser;
import com.joelapenna.foursquare.parsers.UserParser;
import com.joelapenna.foursquare.parsers.VenueParser;
import com.joelapenna.foursquare.types.Category;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.CheckinResult;
import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquare.types.Credentials;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Venue;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
class FoursquareHttpApiV1 {
    private static final Logger LOG = Logger
            .getLogger(FoursquareHttpApiV1.class.getCanonicalName());
    private static final boolean DEBUG = Foursquare.DEBUG;

    private static final String URL_API_AUTHEXCHANGE = "/authexchange";

    private static final String URL_API_ADDVENUE = "/addvenue";
    private static final String URL_API_ADDTIP = "/addtip";
    private static final String URL_API_CITIES = "/cities";
    private static final String URL_API_CHECKINS = "/checkins";
    private static final String URL_API_CHECKIN = "/checkin";
    private static final String URL_API_USER = "/user";
    private static final String URL_API_VENUE = "/venue";
    private static final String URL_API_VENUES = "/venues";
    private static final String URL_API_TIPS = "/tips";
    private static final String URL_API_FRIEND_REQUESTS = "/friend/requests";
    private static final String URL_API_FRIEND_APPROVE = "/friend/approve";
    private static final String URL_API_FRIEND_DENY = "/friend/deny";
    private static final String URL_API_FRIEND_SENDREQUEST = "/friend/sendrequest";
    private static final String URL_API_FRIENDS = "/friends";
    private static final String URL_API_FIND_FRIENDS_BY_NAME = "/findfriends/byname";
    private static final String URL_API_FIND_FRIENDS_BY_PHONE = "/findfriends/byphone";
    private static final String URL_API_FIND_FRIENDS_BY_TWITTER = "/findfriends/bytwitter";
    private static final String URL_API_CATEGORIES = "/categories";

    private final DefaultHttpClient mHttpClient = AbstractHttpApi.createHttpClient();
    private HttpApi mHttpApi;

    private final String mApiBaseUrl;
    private final AuthScope mAuthScope;

    public FoursquareHttpApiV1(String domain, String clientVersion, boolean useOAuth) {
        mApiBaseUrl = "http://" + domain + "/v1";
        mAuthScope = new AuthScope(domain, 80);

        if (useOAuth) {
            mHttpApi = new HttpApiWithOAuth(mHttpClient, clientVersion);
        } else {
            mHttpApi = new HttpApiWithBasicAuth(mHttpClient, clientVersion);
        }
    }

    void setCredentials(String phone, String password) {
        if (phone == null || phone.length() == 0 || password == null || password.length() == 0) {
            if (DEBUG) LOG.log(Level.FINE, "Clearing Credentials");
            mHttpClient.getCredentialsProvider().clear();
        } else {
            if (DEBUG) LOG.log(Level.FINE, "Setting Phone/Password: " + phone + "/******");
            mHttpClient.getCredentialsProvider().setCredentials(mAuthScope,
                    new UsernamePasswordCredentials(phone, password));
        }
    }

    public boolean hasCredentials() {
        return mHttpClient.getCredentialsProvider().getCredentials(mAuthScope) != null;
    }

    public void setOAuthConsumerCredentials(String oAuthConsumerKey, String oAuthConsumerSecret) {
        if (DEBUG) {
            LOG.log(Level.FINE, "Setting consumer key/secret: " + oAuthConsumerKey + " "
                    + oAuthConsumerSecret);
        }
        ((HttpApiWithOAuth) mHttpApi).setOAuthConsumerCredentials(oAuthConsumerKey,
                oAuthConsumerSecret);
    }

    public void setOAuthTokenWithSecret(String token, String secret) {
        if (DEBUG) LOG.log(Level.FINE, "Setting oauth token/secret: " + token + " " + secret);
        ((HttpApiWithOAuth) mHttpApi).setOAuthTokenWithSecret(token, secret);
    }

    public boolean hasOAuthTokenWithSecret() {
        return ((HttpApiWithOAuth) mHttpApi).hasOAuthTokenWithSecret();
    }

    /*
     * /authexchange?oauth_consumer_key=d123...a1bffb5&oauth_consumer_secret=fec...
     * 18
     */
    public Credentials authExchange(String phone, String password) throws FoursquareException,
            FoursquareCredentialsException, FoursquareError, IOException {
        if (((HttpApiWithOAuth) mHttpApi).hasOAuthTokenWithSecret()) {
            throw new IllegalStateException("Cannot do authExchange with OAuthToken already set");
        }
        HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_AUTHEXCHANGE), //
                new BasicNameValuePair("fs_username", phone), //
                new BasicNameValuePair("fs_password", password));
        return (Credentials) mHttpApi.doHttpRequest(httpPost, new CredentialsParser());
    }

    /*
     * /addtip?vid=1234&text=I%20added%20a%20tip&type=todo (type defaults "tip")
     */
    Tip addtip(String vid, String text, String type, String geolat, String geolong, String geohacc,
            String geovacc, String geoalt) throws FoursquareException,
            FoursquareCredentialsException, FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_ADDTIP), //
                new BasicNameValuePair("vid", vid), //
                new BasicNameValuePair("text", text), //
                new BasicNameValuePair("type", type), //
                new BasicNameValuePair("geolat", geolat), //
                new BasicNameValuePair("geolong", geolong), //
                new BasicNameValuePair("geohacc", geohacc), //
                new BasicNameValuePair("geovacc", geovacc), //
                new BasicNameValuePair("geoalt", geoalt));
        return (Tip) mHttpApi.doHttpRequest(httpPost, new TipParser());
    }

    /**
     * @param name the name of the venue
     * @param address the address of the venue (e.g., "202 1st Avenue")
     * @param crossstreet the cross streets (e.g., "btw Grand & Broome")
     * @param city the city name where this venue is
     * @param state the state where the city is
     * @param zip (optional) the ZIP code for the venue
     * @param phone (optional) the phone number for the venue
     * @return
     * @throws FoursquareException
     * @throws FoursquareCredentialsException
     * @throws FoursquareError
     * @throws IOException
     */
    Venue addvenue(String name, String address, String crossstreet, String city, String state,
            String zip, String phone, String categoryId, String geolat, String geolong, String geohacc,
            String geovacc, String geoalt) throws FoursquareException,
            FoursquareCredentialsException, FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_ADDVENUE), //
                new BasicNameValuePair("name", name), //
                new BasicNameValuePair("address", address), //
                new BasicNameValuePair("crossstreet", crossstreet), //
                new BasicNameValuePair("city", city), //
                new BasicNameValuePair("state", state), //
                new BasicNameValuePair("zip", zip), //
                new BasicNameValuePair("phone", phone), //
                new BasicNameValuePair("primarycategoryid", categoryId), //
                new BasicNameValuePair("geolat", geolat), //
                new BasicNameValuePair("geolong", geolong), //
                new BasicNameValuePair("geohacc", geohacc), //
                new BasicNameValuePair("geovacc", geovacc), //
                new BasicNameValuePair("geoalt", geoalt) //
                );
        return (Venue) mHttpApi.doHttpRequest(httpPost, new VenueParser());
    }

    /*
     * /cities
     */
    @SuppressWarnings("unchecked")
    Group<City> cities() throws FoursquareException, FoursquareCredentialsException,
            FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_CITIES));
        return (Group<City>) mHttpApi.doHttpRequest(httpGet, new GroupParser(new CityParser()));
    }

    /*
     * /checkins?
     */
    @SuppressWarnings("unchecked")
    Group<Checkin> checkins(String geolat, String geolong, String geohacc, String geovacc,
            String geoalt) throws FoursquareException, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_CHECKINS), //
                new BasicNameValuePair("geolat", geolat), //
                new BasicNameValuePair("geolong", geolong), //
                new BasicNameValuePair("geohacc", geohacc), //
                new BasicNameValuePair("geovacc", geovacc), //
                new BasicNameValuePair("geoalt", geoalt));
        return (Group<Checkin>) mHttpApi.doHttpRequest(httpGet,
                new GroupParser(new CheckinParser()));
    }

    /*
     * /checkin?vid=1234&venue=Noc%20Noc&shout=Come%20here&private=0&twitter=1
     */
    CheckinResult checkin(String vid, String venue, String geolat, String geolong, String geohacc,
            String geovacc, String geoalt, String shout, boolean isPrivate, boolean twitter,
            boolean facebook) throws FoursquareException, FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_CHECKIN), //
                new BasicNameValuePair("vid", vid), //
                new BasicNameValuePair("venue", venue), //
                new BasicNameValuePair("geolat", geolat), //
                new BasicNameValuePair("geolong", geolong), //
                new BasicNameValuePair("geohacc", geohacc), //
                new BasicNameValuePair("geovacc", geovacc), //
                new BasicNameValuePair("geoalt", geoalt), //
                new BasicNameValuePair("shout", shout), //
                new BasicNameValuePair("private", (isPrivate) ? "1" : "0"), //
                new BasicNameValuePair("twitter", (twitter) ? "1" : "0"), //
                new BasicNameValuePair("facebook", (facebook) ? "1" : "0"));
        return (CheckinResult) mHttpApi.doHttpRequest(httpPost, new CheckinResultParser());
    }

    /**
     * /user?uid=9937
     */
    User user(String uid, boolean mayor, boolean badges, String geolat, String geolong,
            String geohacc, String geovacc, String geoalt) throws FoursquareException,
            FoursquareCredentialsException, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_USER), //
                new BasicNameValuePair("uid", uid), //
                new BasicNameValuePair("mayor", (mayor) ? "1" : "0"), //
                new BasicNameValuePair("badges", (badges) ? "1" : "0"), //
                new BasicNameValuePair("geolat", geolat), //
                new BasicNameValuePair("geolong", geolong), //
                new BasicNameValuePair("geohacc", geohacc), //
                new BasicNameValuePair("geovacc", geovacc), //
                new BasicNameValuePair("geoalt", geoalt) //
                );
        return (User) mHttpApi.doHttpRequest(httpGet, new UserParser());
    }

    /**
     * /venues?geolat=37.770900&geolong=-122.43698
     */
    @SuppressWarnings("unchecked")
    Group<Group<Venue>> venues(String geolat, String geolong, String geohacc, String geovacc,
            String geoalt, String query, int limit) throws FoursquareException, FoursquareError,
            IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_VENUES), //
                new BasicNameValuePair("geolat", geolat), //
                new BasicNameValuePair("geolong", geolong), //
                new BasicNameValuePair("geohacc", geohacc), //
                new BasicNameValuePair("geovacc", geovacc), //
                new BasicNameValuePair("geoalt", geoalt), //
                new BasicNameValuePair("q", query), //
                new BasicNameValuePair("l", String.valueOf(limit)));
        return (Group<Group<Venue>>) mHttpApi.doHttpRequest(httpGet, new GroupParser(
                new GroupParser(new VenueParser())));
    }

    /**
     * /venue?vid=1234
     */
    Venue venue(String vid, String geolat, String geolong, String geohacc, String geovacc,
            String geoalt) throws FoursquareException, FoursquareCredentialsException,
            FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_VENUE), //
                new BasicNameValuePair("vid", vid), //
                new BasicNameValuePair("geolat", geolat), //
                new BasicNameValuePair("geolong", geolong), //
                new BasicNameValuePair("geohacc", geohacc), //
                new BasicNameValuePair("geovacc", geovacc), //
                new BasicNameValuePair("geoalt", geoalt) //
                );
        return (Venue) mHttpApi.doHttpRequest(httpGet, new VenueParser());
    }

    /**
     * /tips?geolat=37.770900&geolong=-122.436987&l=1
     */
    @SuppressWarnings("unchecked")
    Group<Group<Tip>> tips(String geolat, String geolong, String geohacc, String geovacc,
            String geoalt, int limit) throws FoursquareException, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_TIPS), //
                new BasicNameValuePair("geolat", geolat), //
                new BasicNameValuePair("geolong", geolong), //
                new BasicNameValuePair("geohacc", geohacc), //
                new BasicNameValuePair("geovacc", geovacc), //
                new BasicNameValuePair("geoalt", geoalt), //
                new BasicNameValuePair("l", String.valueOf(limit)) //
                );
        return (Group<Group<Tip>>) mHttpApi.doHttpRequest(httpGet, new GroupParser(new GroupParser(
                new TipParser())));
    }

    /*
     * /friends?uid=9937
     */
    @SuppressWarnings("unchecked")
    Group<User> friends(String uid, String geolat, String geolong, String geohacc, String geovacc,
            String geoalt) throws FoursquareException, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_FRIENDS), //
                new BasicNameValuePair("uid", uid), //
                new BasicNameValuePair("geolat", geolat), //
                new BasicNameValuePair("geolong", geolong), //
                new BasicNameValuePair("geohacc", geohacc), //
                new BasicNameValuePair("geovacc", geovacc), //
                new BasicNameValuePair("geoalt", geoalt) //
                );
        return (Group<User>) mHttpApi.doHttpRequest(httpGet, new GroupParser(new UserParser()));
    }

    /*
     * /friend/requests
     */
    @SuppressWarnings("unchecked")
    Group<User> friendRequests() throws FoursquareException, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_FRIEND_REQUESTS));
        return (Group<User>) mHttpApi.doHttpRequest(httpGet, new GroupParser(new UserParser()));
    }

    /*
     * /friend/approve?uid=9937
     */
    User friendApprove(String uid) throws FoursquareException, FoursquareCredentialsException,
            FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_FRIEND_APPROVE), //
                new BasicNameValuePair("uid", uid));
        return (User) mHttpApi.doHttpRequest(httpPost, new UserParser());
    }

    /*
     * /friend/deny?uid=9937
     */
    User friendDeny(String uid) throws FoursquareException, FoursquareCredentialsException,
            FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_FRIEND_DENY), //
                new BasicNameValuePair("uid", uid));
        return (User) mHttpApi.doHttpRequest(httpPost, new UserParser());
    }

    /*
     * /friend/sendrequest?uid=9937
     */
    User friendSendrequest(String uid) throws FoursquareException, FoursquareCredentialsException,
            FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_FRIEND_SENDREQUEST), //
                new BasicNameValuePair("uid", uid));
        return (User) mHttpApi.doHttpRequest(httpPost, new UserParser());
    }

    /**
     * /findfriends/byname?q=john doe, mary smith
     */
    @SuppressWarnings("unchecked")
    public Group<User> findFriendsByName(String text) throws FoursquareException,
            FoursquareCredentialsException, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_FIND_FRIENDS_BY_NAME), //
                new BasicNameValuePair("q", text));
        return (Group<User>) mHttpApi.doHttpRequest(httpGet, new GroupParser(new UserParser()));
    }

    /**
     * /findfriends/byphone?q=555-5555,555-5556
     */
    @SuppressWarnings("unchecked")
    public Group<User> findFriendsByPhone(String text) throws FoursquareException,
            FoursquareCredentialsException, FoursquareError, IOException {
        HttpPost httpPost = mHttpApi.createHttpPost(fullUrl(URL_API_FIND_FRIENDS_BY_PHONE), //
                new BasicNameValuePair("q", text));
        return (Group<User>) mHttpApi.doHttpRequest(httpPost, new GroupParser(new UserParser()));
    }

    /**
     * /findfriends/bytwitter?q=yourtwittername
     */
    @SuppressWarnings("unchecked")
    public Group<User> findFriendsByTwitter(String text) throws FoursquareException,
            FoursquareCredentialsException, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_FIND_FRIENDS_BY_TWITTER), //
                new BasicNameValuePair("q", text));
        return (Group<User>) mHttpApi.doHttpRequest(httpGet, new GroupParser(new UserParser()));
    }
    
    /**
     * /categories
     */
    @SuppressWarnings("unchecked")
    public Group<Category> categories() throws FoursquareException,
            FoursquareCredentialsException, FoursquareError, IOException {
        HttpGet httpGet = mHttpApi.createHttpGet(fullUrl(URL_API_CATEGORIES));
        return (Group<Category>) mHttpApi.doHttpRequest(httpGet, new GroupParser(new CategoryParser()));
    }
        
    private String fullUrl(String url) {
        return mApiBaseUrl + url;
    }
}
