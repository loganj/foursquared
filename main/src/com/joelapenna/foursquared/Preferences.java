/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareCredentialsError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Credentials;
import com.joelapenna.foursquare.types.User;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.text.TextUtils;
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
    static boolean loginUser(Foursquare foursquare, String phoneNumber, String password,
            Editor editor) throws FoursquareCredentialsError, FoursquareException, IOException {
        if (PreferenceActivity.DEBUG) Log.d(PreferenceActivity.TAG, "Trying to log in.");

        foursquare.setCredentials(phoneNumber, password);
        foursquare.setOAuthToken(null, null);

        Credentials credentials = foursquare.authExchange();
        if (credentials == null) {
            return false;
        }
        foursquare.setOAuthToken(credentials.getOauthToken(), credentials.getOauthTokenSecret());
        User user = foursquare.user(null, false, false);

        storePhoneAndPassword(editor, phoneNumber, password);
        storeAuthExchangeCredentials(editor, credentials);
        storeUser(editor, user);
        return true;
    }

    /**
     * Read credentials from preferences and attempt to log in.
     * 
     * @param preferences
     * @param foursquare
     * @param doAuthExchange
     * @return
     * @throws FoursquareCredentialsError
     * @throws FoursquareException
     * @throws IOException
     */
    static User verifyCredentials(Resources resources, SharedPreferences preferences,
            boolean doAuthExchange) throws FoursquareCredentialsError, FoursquareException,
            IOException {
        if (PreferenceActivity.DEBUG) Log.d(PreferenceActivity.TAG, "verifyCredentials()");

        String phoneNumber = preferences.getString(PREFERENCE_PHONE, null);
        String password = preferences.getString(PREFERENCE_PASSWORD, null);

        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password)) {
            throw new FoursquareCredentialsError("Phone number or password not set in preferences.");
        }

        Foursquare foursquare = new Foursquare( //
                resources.getString(R.string.oauth_consumer_key), //
                resources.getString(R.string.oauth_consumer_secret));
        foursquare.setCredentials(phoneNumber, password);

        String oAuthConsumerKey = preferences.getString(PREFERENCE_OAUTH_TOKEN, null);
        String oAuthConsumerSecret = preferences.getString(PREFERENCE_OAUTH_TOKEN, null);

        foursquare.setOAuthConsumerCredentials(oAuthConsumerKey, oAuthConsumerSecret);

        return foursquare.user(null, false, false);
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
            editor.putString(PREFERENCE_CITY_ID, user.getCity().getId());
            editor.putString(PREFERENCE_ID, user.getId());
            if (DEBUG) Log.d(TAG, "Commiting user info: " + String.valueOf(editor.commit()));
        } else {
            if (PreferenceActivity.DEBUG) Log.d(PreferenceActivity.TAG, "Unable to lookup user.");
        }
    }
}
