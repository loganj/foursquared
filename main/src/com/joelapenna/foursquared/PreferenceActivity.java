/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.FeedbackUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com)
 *    -added notifications settings (May 21, 2010).
 *    -removed user update, moved to NotificationSettingsActivity (June 2, 2010)
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
        	startActivity(new Intent(this, AddFriendsActivity.class));

        } else if (Preferences.PREFERENCE_FRIEND_REQUESTS.equals(key)) {
        	startActivity(new Intent(this, FriendRequestsActivity.class));
        
        } else if (Preferences.PREFERENCE_CHANGELOG.equals(key)) {
            startActivity(new Intent(this, ChangelogActivity.class));
            
        } else if (Preferences.PREFERENCE_PINGS.equals(key)) {
            startActivity(new Intent(this, PingsSettingsActivity.class));
        }
        
        return true;
    }
}
