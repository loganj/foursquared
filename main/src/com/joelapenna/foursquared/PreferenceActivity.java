/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {
    static final String TAG = "PreferenceActivity";
    static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate");

        this.addPreferencesFromResource(R.xml.preferences);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (DEBUG) Log.d(TAG, "onPreferenceTreeClick");
        String key = preference.getKey();
        if (key.equals(Preferences.PREFERENCE_LOGOUT)) {
            mPrefs.edit().clear().commit();
            Foursquared.getFoursquare().clearAllCredentials();

            startActivityForResult(new Intent(PreferenceActivity.this, LoginActivity.class),
                    LoginActivity.ACTIVITY_REQUEST_LOGIN);
            sendBroadcast(new Intent(Foursquared.INTENT_ACTION_LOGGED_OUT));
        } else if (key.equals(Preferences.PREFERENCE_FRIEND_ADD)) {
            startActivity(new Intent( //
                    Intent.ACTION_VIEW, Uri.parse("http://m.playfoursquare.com/addfriends")));
        } else if (key.equals(Preferences.PREFERENCE_FRIEND_REQUESTS)) {
            startActivity(new Intent( //
                    Intent.ACTION_VIEW, Uri.parse("http://m.playfoursquare.com/friends")));
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            startActivity(new Intent(this, SearchVenuesActivity.class));
        }
        finish();
    }
}
