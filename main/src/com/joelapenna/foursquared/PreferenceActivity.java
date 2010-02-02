/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.FeedbackUtils;
import com.joelapenna.foursquared.util.NotificationsUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {
    private static final String TAG = "PreferenceActivity";

    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private SharedPreferences mPrefs;

    private BroadcastReceiver mLoggedOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive: " + intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        this.addPreferencesFromResource(R.xml.preferences);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        Preference advanceSettingsPreference = getPreferenceScreen().findPreference(
                Preferences.PREFERENCE_ADVANCED_SETTINGS);
        advanceSettingsPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ((Foursquared) getApplication()).requestUpdateUser();
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedOutReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        new UpdateUserTask().execute();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (DEBUG) Log.d(TAG, "onPreferenceTreeClick");
        String key = preference.getKey();
        if (Preferences.PREFERENCE_LOGOUT.equals(key)) {
            mPrefs.edit().clear().commit();
            // TODO: If we re-implement oAuth, we'll have to call
            // clearAllCrendentials here.
            ((Foursquared) getApplication()).getFoursquare().setCredentials(null, null);

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            sendBroadcast(new Intent(Foursquared.INTENT_ACTION_LOGGED_OUT));

        } else if (Preferences.PREFERENCE_ADVANCED_SETTINGS.equals(key)) {
            startActivity(new Intent( //
                    Intent.ACTION_VIEW, Uri.parse(Foursquare.FOURSQUARE_PREFERENCES)));

        } else if (Preferences.PREFERENCE_SEND_FEEDBACK.equals(key)) {
            FeedbackUtils.SendFeedBack(this, (Foursquared) getApplication());

        } else if (Preferences.PREFERENCE_FRIEND_ADD.equals(key)) {
            startActivity(new Intent( //
                    Intent.ACTION_VIEW, Uri.parse(Foursquare.FOURSQUARE_MOBILE_ADDFRIENDS)));

        } else if (Preferences.PREFERENCE_FRIEND_REQUESTS.equals(key)) {
            startActivity(new Intent( //
                    Intent.ACTION_VIEW, Uri.parse(Foursquare.FOURSQUARE_MOBILE_FRIENDS)));

        }
        return true;
    }

    private class UpdateUserTask extends AsyncTask<Void, Void, User> {
        private static final String TAG = "UpdateUserTask";

        private static final boolean DEBUG = FoursquaredSettings.DEBUG;

        private Exception mReason;

        @Override
        protected User doInBackground(Void... params) {
            if (DEBUG) Log.d(TAG, "doInBackground()");
            try {
                Foursquared foursquared = (Foursquared) getApplication();
                Location location = foursquared.getLastKnownLocationOrNull();

                Foursquare foursquare = foursquared.getFoursquare();
                return foursquare.user(null, false, false, LocationUtils
                        .createFoursquareLocation(location));

            } catch (Exception e) {
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            if (user == null) {
                NotificationsUtil.ToastReasonForFailure(PreferenceActivity.this, mReason);
            } else {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(PreferenceActivity.this);
                Editor editor = prefs.edit();
                Preferences.storeUser(editor, user);
                if (!editor.commit()) {
                    if (DEBUG) Log.d(TAG, "storeUser commit failed");

                }
            }
        }
    }
}
