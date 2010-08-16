/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.preferences;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareCredentialsException;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.util.UserUtils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Preferences {
    private static final String TAG = "Preferences";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    // Visible Preferences (sync with preferences.xml)
    public static final String PREFERENCE_SHARE_CHECKIN = "share_checkin";
    public static final String PREFERENCE_IMMEDIATE_CHECKIN = "immediate_checkin";
    public static final String PREFERENCE_STARTUP_TAB = "startup_tab";
    public static final String PREFERENCE_VENUES_SORT = "venues_sort";
 
    // Hacks for preference activity extra UI elements.
    public static final String PREFERENCE_ADVANCED_SETTINGS = "advanced_settings";
    public static final String PREFERENCE_TWITTER_CHECKIN = "twitter_checkin";
    public static final String PREFERENCE_FACEBOOK_CHECKIN = "facebook_checkin";
    public static final String PREFERENCE_TWITTER_HANDLE = "twitter_handle";
    public static final String PREFERENCE_FACEBOOK_HANDLE = "facebook_handle";
    public static final String PREFERENCE_FRIEND_REQUESTS = "friend_requests";
    public static final String PREFERENCE_FRIEND_ADD = "friend_add";
    public static final String PREFERENCE_CHANGELOG = "changelog";
    public static final String PREFERENCE_CITY_NAME = "city_name";
    public static final String PREFERENCE_LOGOUT = "logout";
    public static final String PREFERENCE_HELP = "help";
    public static final String PREFERENCE_SEND_FEEDBACK = "send_feedback";
    public static final String PREFERENCE_PINGS = "pings_on";
    public static final String PREFERENCE_PINGS_INTERVAL = "pings_refresh_interval_in_minutes";
    public static final String PREFERENCE_PINGS_VIBRATE = "pings_vibrate";

    // Credentials related preferences
    public static final String PREFERENCE_LOGIN = "phone";
    public static final String PREFERENCE_PASSWORD = "password";

    // Extra info for getUserCity
    private static final String PREFERENCE_CITY_ID = "city_id";
    private static final String PREFERENCE_CITY_GEOLAT = "city_geolat";
    private static final String PREFERENCE_CITY_GEOLONG = "city_geolong";
    private static final String PREFERENCE_CITY_SHORTNAME = "city_shortname";

    // Extra info for getUserId
    private static final String PREFERENCE_ID = "id";
    
    // Extra info about the user, their gender, to control icon used for 'me' in the UI.
    private static final String PREFERENCE_GENDER = "gender";
    
    // Extra info, can the user have followers or not.
    public static final String PREFERENCE_CAN_HAVE_FOLLOWERS = "can_have_followers";

    // Not-in-XML preferences for dumpcatcher
    public static final String PREFERENCE_DUMPCATCHER_CLIENT = "dumpcatcher_client";

    // Keeps track of the last changelog version shown to the user at startup.
    private static final String PREFERENCE_LAST_SEEN_CHANGELOG_VERSION 
        = "last_seen_changelog_version";

    // User can choose to clear geolocation on each search.
    public static final String PREFERENCE_CACHE_GEOLOCATION_FOR_SEARCHES
        = "cache_geolocation_for_searches";
 
    // If we're compiled to show the prelaunch activity, flag stating whether to skip
    // showing it on startup.
    public static final String PREFERENCE_SHOW_PRELAUNCH_ACTIVITY = "show_prelaunch_activity";
    
    // Last time pings service ran.
    public static final String PREFERENCE_PINGS_SERVICE_LAST_RUN_TIME = "pings_service_last_run_time";
    
    // Broadcast an intent to show single full-screen images, or use our own poor image viewer.
    public static final String PREFERENCE_NATIVE_IMAGE_VIEWER
        = "native_full_size_image_viewer";
    
    
    /**
     * Gives us a chance to set some default preferences if this is the first install
     * of the application.
     */
    public static void setupDefaults(SharedPreferences preferences, Resources resources) {
        Editor editor = preferences.edit();
        if (!preferences.contains(PREFERENCE_STARTUP_TAB)) {
            String[] startupTabValues = resources.getStringArray(R.array.startup_tabs_values);
            editor.putString(PREFERENCE_STARTUP_TAB, startupTabValues[0]);
        }
        if (!preferences.contains(PREFERENCE_CACHE_GEOLOCATION_FOR_SEARCHES)) {
            editor.putBoolean(PREFERENCE_CACHE_GEOLOCATION_FOR_SEARCHES, false);
        }
        if (!preferences.contains(PREFERENCE_SHOW_PRELAUNCH_ACTIVITY)) {
            editor.putBoolean(PREFERENCE_SHOW_PRELAUNCH_ACTIVITY, true);
        }
        if (!preferences.contains(PREFERENCE_PINGS_INTERVAL)) {
            editor.putString(PREFERENCE_PINGS_INTERVAL, "30");
        }
        if (!preferences.contains(PREFERENCE_VENUES_SORT)) {
            String[] venuesSortValues = resources.getStringArray(R.array.venues_sort_values);
            editor.putString(PREFERENCE_VENUES_SORT, venuesSortValues[0]);
        }
        if (!preferences.contains(PREFERENCE_NATIVE_IMAGE_VIEWER)) {
            editor.putBoolean(PREFERENCE_NATIVE_IMAGE_VIEWER, true);
        }
        editor.commit();
    }
    
    public static String createUniqueId(SharedPreferences preferences) {
        String uniqueId = preferences.getString(PREFERENCE_DUMPCATCHER_CLIENT, null);
        if (uniqueId == null) {
            uniqueId = UUID.randomUUID().toString();
            Editor editor = preferences.edit();
            editor.putString(PREFERENCE_DUMPCATCHER_CLIENT, uniqueId);
            editor.commit();
        }
        return uniqueId;
    }

    public static boolean loginUser(Foursquare foursquare, String login, String password,
            Foursquare.Location location, Editor editor) throws FoursquareCredentialsException,
            FoursquareException, IOException {
        if (DEBUG) Log.d(Preferences.TAG, "Trying to log in.");

        foursquare.setCredentials(login, password);
        storeLoginAndPassword(editor, login, password);
        if (!editor.commit()) {
            if (DEBUG) Log.d(TAG, "storeLoginAndPassword commit failed");
            return false;
        }
        
        User user = foursquare.user(null, false, false, location);
        storeUser(editor, user);
        if (!editor.commit()) {
            if (DEBUG) Log.d(TAG, "storeUser commit failed");
            return false;
        }

        return true;
    }

    public static boolean logoutUser(Foursquare foursquare, Editor editor) {
        if (DEBUG) Log.d(Preferences.TAG, "Trying to log out.");
        // TODO: If we re-implement oAuth, we'll have to call clearAllCrendentials here.
        foursquare.setCredentials(null, null);
        return editor.clear().commit();
    }

    public static City getUserCity(SharedPreferences prefs) {
        City city = new City();
        city.setId(prefs.getString(Preferences.PREFERENCE_CITY_ID, null));
        city.setName(prefs.getString(Preferences.PREFERENCE_CITY_NAME, null));
        city.setShortname(prefs.getString(Preferences.PREFERENCE_CITY_SHORTNAME, null));
        city.setGeolat(prefs.getString(Preferences.PREFERENCE_CITY_GEOLAT, null));
        city.setGeolong(prefs.getString(Preferences.PREFERENCE_CITY_GEOLONG, null));
        return city;
    }

    public static String getUserId(SharedPreferences prefs) {
        return prefs.getString(PREFERENCE_ID, null);
    }

    public static String getUserGender(SharedPreferences prefs) {
        return prefs.getString(PREFERENCE_GENDER, null);
    }
    
    public static String getLastSeenChangelogVersion(SharedPreferences prefs) {
        return prefs.getString(PREFERENCE_LAST_SEEN_CHANGELOG_VERSION, null);
    }
    
    public static void storeCity(final Editor editor, City city) {
        if (city != null) {
            editor.putString(PREFERENCE_CITY_ID, city.getId());
            editor.putString(PREFERENCE_CITY_GEOLAT, city.getGeolat());
            editor.putString(PREFERENCE_CITY_GEOLONG, city.getGeolong());
            editor.putString(PREFERENCE_CITY_NAME, city.getName());
            editor.putString(PREFERENCE_CITY_SHORTNAME, city.getShortname());
        }
    }

    public static void storeLoginAndPassword(final Editor editor, String login, String password) {
        editor.putString(PREFERENCE_LOGIN, login);
        editor.putString(PREFERENCE_PASSWORD, password);
    }

    public static void storeUser(final Editor editor, User user) {
        if (user != null && user.getId() != null) {
            editor.putString(PREFERENCE_ID, user.getId());
            editor.putBoolean(PREFERENCE_TWITTER_CHECKIN, user.getSettings().sendtotwitter());
            editor.putBoolean(PREFERENCE_FACEBOOK_CHECKIN, user.getSettings().sendtofacebook());
            editor.putString(PREFERENCE_TWITTER_HANDLE, user.getTwitter() != null ? user.getTwitter() : "");
            editor.putString(PREFERENCE_FACEBOOK_HANDLE, user.getFacebook() != null ? user.getFacebook() : "");
            editor.putString(PREFERENCE_GENDER, user.getGender());
            editor.putBoolean(PREFERENCE_CAN_HAVE_FOLLOWERS, UserUtils.getCanHaveFollowers(user));
            if (DEBUG) Log.d(TAG, "Setting user info");
        } else {
            if (Preferences.DEBUG) Log.d(Preferences.TAG, "Unable to lookup user.");
        }
    }
    
    public static void storeLastSeenChangelogVersion(final Editor editor, String version) {
        editor.putString(PREFERENCE_LAST_SEEN_CHANGELOG_VERSION, version);
        if (!editor.commit()) {
            Log.e(TAG, "storeLastSeenChangelogVersion commit failed");
        }
    }
    
    public static boolean getUseNativeImageViewerForFullScreenImages(SharedPreferences prefs) {
        return prefs.getBoolean(PREFERENCE_NATIVE_IMAGE_VIEWER, true);
    }
}
