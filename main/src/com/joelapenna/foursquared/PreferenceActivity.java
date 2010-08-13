/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquared.preferences.Preferences;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com)
 *    -added notifications settings (May 21, 2010).
 *    -removed user update, moved to NotificationSettingsActivity (June 2, 2010)
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {
    private static final String TAG = "PreferenceActivity";

    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final int DIALOG_TOS_PRIVACY = 1;
    private static final int DIALOG_PROFILE_SETTINGS = 2;
    
    private static final String URL_TOS = "http://foursquare.com/legal/terms";
    private static final String URL_PRIVACY = "http://foursquare.com/legal/privacy";
    
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

        } else if (Preferences.PREFERENCE_HELP.equals(key)) {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.INTENT_EXTRA_URL, "http://foursquare.com/help/android");
            startActivity(intent);
            
        } else if (Preferences.PREFERENCE_SEND_FEEDBACK.equals(key)) {
            startActivity(new Intent(this, SendLogActivity.class));
            
        } else if (Preferences.PREFERENCE_FRIEND_ADD.equals(key)) {
        	startActivity(new Intent(this, AddFriendsActivity.class));

        } else if (Preferences.PREFERENCE_FRIEND_REQUESTS.equals(key)) {
        	startActivity(new Intent(this, FriendRequestsActivity.class));
        
        } else if (Preferences.PREFERENCE_CHANGELOG.equals(key)) {
            startActivity(new Intent(this, ChangelogActivity.class));
            
        } else if (Preferences.PREFERENCE_PINGS.equals(key)) {
            startActivity(new Intent(this, PingsSettingsActivity.class));
        
        } else if (Preferences.PREFERENCE_TOS_PRIVACY.equals(key)) {
            showDialog(DIALOG_TOS_PRIVACY);
        
        } else if (Preferences.PREFERENCE_PROFILE_SETTINGS.equals(key)) {
            showDialog(DIALOG_PROFILE_SETTINGS);
        }
        
        return true;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_TOS_PRIVACY:
                ArrayAdapter<String> adapterTos = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
                adapterTos.add(getResources().getString(R.string.preference_activity_tos));
                adapterTos.add(getResources().getString(R.string.preference_activity_privacy));
                AlertDialog dlgInfo = new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.preference_activity_tos_privacy_dlg_title))
                    .setAdapter(adapterTos, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dlg, int pos) {
                            Intent intent = new Intent(PreferenceActivity.this, WebViewActivity.class);
                            switch (pos) {
                                case 0:
                                    intent.putExtra(WebViewActivity.INTENT_EXTRA_URL, URL_TOS);
                                    break;
                                case 1:
                                    intent.putExtra(WebViewActivity.INTENT_EXTRA_URL, URL_PRIVACY);
                                    break;
                                default:
                                    return;
                            }
                            startActivity(intent);
                        }
                    })
                    .create();
                return dlgInfo;
                
            case DIALOG_PROFILE_SETTINGS:
                String userId = ((Foursquared) getApplication()).getUserId();
                String userName = ((Foursquared) getApplication()).getUserName();
                String userEmail = ((Foursquared) getApplication()).getUserEmail();
                
                ArrayAdapter<String> adapterProfileSettings = new ArrayAdapter<String>(
                        this, android.R.layout.simple_list_item_1);
                adapterProfileSettings.add(
                        getResources().getString(R.string.preference_activity_profile_setting_user_id) + ":\t" + userId);
                adapterProfileSettings.add(
                        getResources().getString(R.string.preference_activity_profile_setting_username) + ":\t" + userName);
                adapterProfileSettings.add(
                        getResources().getString(R.string.preference_activity_profile_setting_email) + ":\t" + userEmail);
                
                AlertDialog dlgProfileSettings = new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.preference_activity_profile_settings_dlg_title))
                    .setAdapter(adapterProfileSettings, null)
                    .set
                    .create();
                return dlgProfileSettings;
        }
        
        //preference_activity_tos_privacy_dlg_title
        return null;
    }
}
