/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareCredentialsError;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquare.types.Credentials;
import com.joelapenna.foursquare.types.User;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Preferences {
    private static final String TAG = "Preferences";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String PREFERENCE_PHONE = "phone";
    public static final String PREFERENCE_PASSWORD = "password";
    public static final String PREFERENCE_TWITTER_CHECKIN = "twitter_checkin";
    public static final String PREFERENCE_SHARE_CHECKIN = "share_checkin";

    // Not-in-XML preferences
    public static final String PREFERENCE_CITY_ID = "city_id";
    public static final String PREFERENCE_CITY_GEOLAT = "city_geolat";
    public static final String PREFERENCE_CITY_GEOLONG = "city_geolong";
    public static final String PREFERENCE_CITY_NAME = "city_name";
    public static final String PREFERENCE_EMAIL = "email";
    public static final String PREFERENCE_FIRST = "first_name";
    public static final String PREFERENCE_GENDER = "gender";
    public static final String PREFERENCE_ID = "id";
    public static final String PREFERENCE_LAST = "last_name";
    public static final String PREFERENCE_PHOTO = "photo";

    // Not-in-XML preferences for oAuth
    public static final String PREFERENCE_OAUTH_TOKEN = "oauth_token";
    public static final String PREFERENCE_OAUTH_TOKEN_SECRET = "oauth_token_secret";

    // Not-in-XML preferences for dumpcatcher
    public static final String PREFERENCE_DUMPCATCHER_CLIENT = "dumpcatcher_client";

    static String createUniqueId(SharedPreferences preferences) {
        String uniqueId = preferences.getString(PREFERENCE_DUMPCATCHER_CLIENT, null);
        if (uniqueId == null) {
            uniqueId = UUID.randomUUID().toString();
            Editor editor = preferences.edit();
            editor.putString(PREFERENCE_DUMPCATCHER_CLIENT, uniqueId);
            editor.commit();
        }
        return uniqueId;
    }

    /**
     * Log in a user and put credential information into the preferences edit queue.
     *
     * @param foursquare
     * @param phoneNumber
     * @param password
     * @param editor
     * @throws FoursquareCredentialsError
     * @throws FoursquareException
     * @throws IOException
     */
    static User loginUser(Foursquare foursquare, String phoneNumber, String password, Editor editor)
            throws FoursquareCredentialsError, FoursquareException, IOException {
        if (PreferenceActivity.DEBUG) Log.d(PreferenceActivity.TAG, "Trying to log in.");

        foursquare.setCredentials(phoneNumber, password);
        foursquare.setOAuthToken(null, null);

        Credentials credentials = foursquare.authExchange();
        if (credentials == null) {
            return null;
        }
        foursquare.setOAuthToken(credentials.getOauthToken(), credentials.getOauthTokenSecret());
        User user = foursquare.user(null, false, false);

        storePhoneAndPassword(editor, phoneNumber, password);
        storeAuthExchangeCredentials(editor, credentials);
        storeUser(editor, user);
        return user;
    }

    static City switchCityIfChanged(Foursquare foursquare, User user, Location location)
            throws FoursquareException, FoursquareError, IOException {
        City city = null;
        City currentCity = user.getCity();
        if (location != null) {
            City newCity = foursquare.checkCity(//
                    String.valueOf(location.getLatitude()), //
                    String.valueOf(location.getLongitude()));

            if (currentCity != null && newCity != null) {
                if (!currentCity.getId().equals(newCity.getId())) {
                    foursquare.switchCity(newCity.getId());
                    city = newCity;
                } else {
                    city = currentCity;
                }
            } else if (newCity != null) {
                foursquare.switchCity(newCity.getId());
                city = newCity;

            } else if (currentCity != null) {
                city = currentCity;
            }

        } else {
            city = currentCity;
        }
        return city;
    }

    static void storeAuthExchangeCredentials(final Editor editor, Credentials credentials)
            throws FoursquareCredentialsError {
        if (credentials != null && credentials.getOauthToken() != null
                && credentials.getOauthTokenSecret() != null) {
            if (DEBUG) Log.d(TAG, "Storing oauth token");
            editor.putString(PREFERENCE_OAUTH_TOKEN, credentials.getOauthToken());
            editor.putString(PREFERENCE_OAUTH_TOKEN_SECRET, credentials.getOauthTokenSecret());
            if (DEBUG) Log.d(TAG, "Commiting authexchange token: "
                    + String.valueOf(editor.commit()));
        } else {
            throw new FoursquareCredentialsError("Unable to auth exchange.");
        }
    }

    static void storePhoneAndPassword(final Editor editor, String phoneNumber, String password) {
        editor.putString(PREFERENCE_PHONE, phoneNumber);
        editor.putString(PREFERENCE_PASSWORD, password);
    }

    static void storeUser(final Editor editor, User user) {
        if (user != null && user.getId() != null) {
            editor.putString(PREFERENCE_ID, user.getId());
            if (DEBUG) Log.d(TAG, "Commiting user info: " + String.valueOf(editor.commit()));
        } else {
            if (PreferenceActivity.DEBUG) Log.d(PreferenceActivity.TAG, "Unable to lookup user.");
        }
    }

    static void storeCity(final Editor editor, City city) {
        if (city != null) {
            editor.putString(PREFERENCE_CITY_ID, city.getId());
            editor.putString(PREFERENCE_CITY_GEOLAT, city.getGeolat());
            editor.putString(PREFERENCE_CITY_GEOLONG, city.getGeolong());
            editor.putString(PREFERENCE_CITY_NAME, city.getName());
        }
    }
}
