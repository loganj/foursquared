/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare;

import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.parsers.AuthParser;
import com.joelapenna.foursquare.parsers.CheckinParser;
import com.joelapenna.foursquare.parsers.CheckinResponseParser;
import com.joelapenna.foursquare.parsers.DataParser;
import com.joelapenna.foursquare.parsers.GroupParser;
import com.joelapenna.foursquare.parsers.TipParser;
import com.joelapenna.foursquare.parsers.VenueParser;
import com.joelapenna.foursquare.types.Auth;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Data;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.error.FoursquaredCredentialsError;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
class FoursquareHttpApi extends HttpApi {
    private static final String HTTP_SCHEME = "http://";
    private static final String DOMAIN = "playfoursquare.com";

    private static final String URL_DOMAIN = HTTP_SCHEME + DOMAIN;

    private static final String URL_API_BASE = URL_DOMAIN + "/api";
    private static final String URL_API_ADD_TODO = URL_API_BASE + "/add";
    private static final String URL_API_CHECKINS = URL_API_BASE + "/checkins";
    private static final String URL_API_LOGIN = URL_API_BASE + "/login";
    private static final String URL_API_TODO = URL_API_BASE + "/todo";
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

    FoursquareHttpApi(DefaultHttpClient httpClient) {
        mHttpClient = httpClient;
    }

    void setCredentials(String phone, String password) {
        mHttpClient.getCredentialsProvider().setCredentials(new AuthScope(DOMAIN, 80),
                new UsernamePasswordCredentials(phone, password));
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
        setCredentials(phone, password);
        return auth;
    }

    /*
     * /api/add?type=XXX&text=add%20a%20tip&vid=44794&lat=37.770741&lng=-122.436854&cityid=23
     * top or todo
     */
    Data add(String type, String text, String vid, String lat, String lng, String cityId)
            throws FoursquareError, FoursquareParseException, IOException {
        return (Data)doHttpPost(URL_API_ADD_TODO, new DataParser(), //
                new BasicNameValuePair("type", type),
                new BasicNameValuePair("text", text),
                new BasicNameValuePair("vid", vid),
                new BasicNameValuePair("lat", (lat != null) ? lat : ""), // lat
                new BasicNameValuePair("lng", (lng != null) ? lng : ""), // lng
                new BasicNameValuePair("cityid", cityId) //
        //
        );
    }

    Checkin checkin(String phone, String venue, boolean silent, boolean twitter, String lat,
            String lng, String cityid) throws FoursquareError, FoursquareParseException,
            IOException {
        return (Checkin)doHttpPost(URL_API_INCOMING,
                new CheckinResponseParser(),
                new BasicNameValuePair("number", phone), // phone
                new BasicNameValuePair("message", "@" + venue), // venue
                new BasicNameValuePair("silent", (silent) ? "1" : "0"), // silent
                new BasicNameValuePair("twitter", (twitter) ? "1" : "0"), // twitter
                new BasicNameValuePair("lat", (lat != null) ? lat : ""), // lat
                new BasicNameValuePair("lng", (lng != null) ? lng : ""), // lng
                new BasicNameValuePair("cityid", (cityid != null) ? cityid : ""),
                new BasicNameValuePair("output", "xml"));
    }

    /**
     * /api/checkins?lat=37.770653&lng=-122.436929&r=1&l=10
     *
     * @return
     */
    Group checkins(String cityId) throws FoursquareError, FoursquareParseException, IOException {
        return (Group)doHttpPost(URL_API_CHECKINS, new GroupParser(new CheckinParser()),
                new BasicNameValuePair("cityid", cityId));
    }

    /**
     * /api/todo?cityid=23&lat=37.770900&lng=-122.436987
     *
     * @throws IOException
     * @throws FoursquareParseException
     * @throws FoursquaredCredentialsError
     */
    Group todos(String cityId, String lat, String lng) throws FoursquareError,
            FoursquareParseException, IOException {
        return (Group)doHttpPost(URL_API_TODO, new GroupParser(new TipParser()),
                new BasicNameValuePair("cityid", cityId), // city id
                new BasicNameValuePair("lat", (lat != null) ? lat : ""), // lat
                new BasicNameValuePair("lng", (lng != null) ? lng : "") // lng
        );
    }

    /**
     * /api/venues?lat=37.770653&lng=-122.436929&r=1&l=10
     *
     * @return
     */
    Group venues(String query, String lat, String lng, int radius, int length)
            throws FoursquareError, FoursquareParseException, IOException {
        return (Group)doHttpPost(URL_API_VENUES, new GroupParser(new VenueParser()),
                new BasicNameValuePair("lat", (lat != null) ? lat : ""), // lat
                new BasicNameValuePair("lng", (lng != null) ? lng : ""), // lng
                new BasicNameValuePair("q", (query != null) ? query : ""), // a query
                new BasicNameValuePair("r", String.valueOf(radius)), // radius in miles?
                new BasicNameValuePair("length", String.valueOf(length)) // number of results.
        );
    }

    /**
     * /api/venue?vid=1234
     *
     * @return
     */
    Venue venue(String id) throws FoursquareError, FoursquareParseException, IOException {
        return (Venue)doHttpPost(URL_API_VENUE, new VenueParser(),
                new BasicNameValuePair("vid", id));
    }

    /**
     * /web/iphone/achievements?task=unlocked&uid=1818&cityid=23
     */
    String achievements(String cityId, String task, String userId) throws FoursquareError,
            FoursquareParseException, IOException {
        return doHttpPost(URL_ACHIEVEMENTS, // url
                new BasicNameValuePair("cityid", cityId), // city matters, I guess.
                new BasicNameValuePair("task", task), // task name?
                new BasicNameValuePair("uid", userId) // user id
        );
    }

    /**
     * /incoming/breakdown?cid=67889&uid=9232&client=iphone
     */
    String breakdown(String userId, String checkinId) throws FoursquareError,
            FoursquareParseException, IOException {
        return doHttpPost(URL_BREAKDOWN, // url
                new BasicNameValuePair("uid", userId), // user id
                new BasicNameValuePair("cid", checkinId), // checkin id
                new BasicNameValuePair("client", "android") // client i guess.
        );
    }

    /**
     * /web/iphone/me?uid=9232&view=mini&cityid=23
     */
    String me(String cityId, String userId) throws FoursquareError, FoursquareParseException,
            IOException {
        return doHttpPost(URL_ME, // url
                new BasicNameValuePair("cityid", cityId), // city matters, I guess.
                new BasicNameValuePair("view", "mini"), // huh?
                new BasicNameValuePair("uid", userId) // user id
        );
    }
}
