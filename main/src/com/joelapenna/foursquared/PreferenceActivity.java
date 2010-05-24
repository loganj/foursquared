/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Settings;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.app.NotificationsService;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.FeedbackUtils;
import com.joelapenna.foursquared.util.NotificationsUtil;

import android.app.ProgressDialog;
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
import android.widget.Toast;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com)
 *    -added notifications settings (May 21, 2010).
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {
    private static final String TAG = "PreferenceActivity";

    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private SharedPreferences mPrefs;
    private StateHolder mStateHolder;
    private ProgressDialog mDlgProgress;
    

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
        
        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
            mStateHolder.setActivity(this);
            
            if (mStateHolder.getIsRunningTaskNotifications()) {
                startProgressBar(
                    getResources().getString(R.string.preferences_progressbar_title_notifications), 
                    getResources().getString(R.string.preferences_progressbar_message_notifications));
            }
        } else {
            mStateHolder = new StateHolder(this);
        }
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        mStateHolder.setActivity(null);
        return mStateHolder;
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
     
        if (isFinishing()) {
            stopProgressBar();
            mStateHolder.cancelTasks();
            
            // When the activity is finishing, we can check if the user has modified
            // their refresh rate interval for notifications. If they have, we can
            // restart the alarm service.
            if (mPrefs.getBoolean(Preferences.PREFERENCE_NOTIFICATIONS, false)) {
                if (!mPrefs.getString(Preferences.PREFERENCE_NOTIFICATIONS_INTERVAL, "30").equals(
                        mStateHolder.getNotificationIntervalAtStartup())) {
                    restartNotificationsWithNewInterval();
                }
            }
        }
    }
    
    private void startProgressBar(String title, String message) {
        if (mDlgProgress == null) {
            mDlgProgress = ProgressDialog.show(this, title, message);
        }
        mDlgProgress.show();
    }

    private void stopProgressBar() {
        if (mDlgProgress != null) {
            mDlgProgress.dismiss();
            mDlgProgress = null;
        }
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
            
        } else if (Preferences.PREFERENCE_NOTIFICATIONS.equals(key)) {
            boolean notificationsOn = preference.getSharedPreferences().getBoolean(
                    Preferences.PREFERENCE_NOTIFICATIONS, false);
            int interval = Integer.parseInt(preference.getSharedPreferences().getString(
                    Preferences.PREFERENCE_NOTIFICATIONS_INTERVAL, "30"));
            updateNotifications(notificationsOn, interval);
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
                Location location = foursquared.getLastKnownLocation();

                Foursquare foursquare = foursquared.getFoursquare();
                return foursquare.user(
                        null, false, false, LocationUtils
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
    
    private void onPostTaskNotification(Settings settings, boolean changeToState, Exception reason) {
        if (settings == null) {
            NotificationsUtil.ToastReasonForFailure(this, reason);
            
            // Reset the preference in case of failure.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean(Preferences.PREFERENCE_NOTIFICATIONS, !changeToState).commit();
            
            // There is currently no way to refresh this activity, we just restart it.
            finish();
            startActivity(new Intent(this, PreferenceActivity.class));
        } else {
            Toast.makeText(
                    this, 
                    "Notifications have been turned " + settings.getPings() + "!", 
                    Toast.LENGTH_SHORT).show();
            
            if (settings.getPings().equals("on")) {
                NotificationsService.setupNotifications(this);
            } else {
                NotificationsService.cancelNotifications(this);
            }
        }
        mStateHolder.setIsRunningTaskNotifications(false);
        stopProgressBar();
    }
    
    private static class UpdateNotificationsTask extends AsyncTask<Void, Void, Settings> {
        private static final String TAG = "UpdateNotificationsTask";
        private static final boolean DEBUG = FoursquaredSettings.DEBUG;
        private Exception mReason;
        private boolean mNotificationsOn;
        private PreferenceActivity mActivity;

        
        public UpdateNotificationsTask(PreferenceActivity activity, boolean on) {
            mActivity = activity;
            mNotificationsOn = on;
        }
        
        @Override
        protected void onPreExecute() {
            mActivity.startProgressBar(
                    mActivity.getResources().getString(
                            R.string.preferences_progressbar_title_notifications), 
                    mActivity.getResources().getString(
                            R.string.preferences_progressbar_message_notifications));
        }
        
        @Override
        protected Settings doInBackground(Void... params) {
            if (DEBUG) Log.d(TAG, "doInBackground()");
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();
                return foursquare.setpings(mNotificationsOn);

            } catch (Exception e) {
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Settings settings) {
            if (mActivity != null) {
                mActivity.onPostTaskNotification(settings, mNotificationsOn, mReason); 
            }
        }

        public void setActivity(PreferenceActivity activity) {
            mActivity = activity;
        }
    }
    
    private void updateNotifications(boolean on, int intervalInMinutes) {
        if (on) {
            // TODO: Warn user that this will use more battery?   
        }

        mStateHolder.startTaskNotifications(this, on);
    }
    
    private void restartNotificationsWithNewInterval() {
        Log.e(TAG, "restartingNotification... cause user changed interval...");
        NotificationsService.cancelNotifications(this);
        NotificationsService.setupNotifications(this);
    }
    
    private static class StateHolder {
        private UpdateNotificationsTask mTaskNotifications;
        private boolean mIsRunningTaskNotifications;
        private String mNotificationIntervalAtStartup;

        public StateHolder(Context context) {
            mTaskNotifications = null;
            mIsRunningTaskNotifications = false;
            
            mNotificationIntervalAtStartup = PreferenceManager.getDefaultSharedPreferences(context).getString(
                    Preferences.PREFERENCE_NOTIFICATIONS_INTERVAL, "30");
        }

        public void startTaskNotifications(PreferenceActivity activity, boolean on) {
            mIsRunningTaskNotifications = true;
            mTaskNotifications = new UpdateNotificationsTask(activity, on);
            mTaskNotifications.execute();
        }

        public void setActivity(PreferenceActivity activity) {
            if (mTaskNotifications != null) {
                mTaskNotifications.setActivity(activity);
            }
        }

        public boolean getIsRunningTaskNotifications() {
            return mIsRunningTaskNotifications;
        }
        
        public void setIsRunningTaskNotifications(boolean isRunningTaskNotifications) {
            mIsRunningTaskNotifications = isRunningTaskNotifications;
        }
        
        public String getNotificationIntervalAtStartup() {
            return mNotificationIntervalAtStartup;
        }
        
        public void cancelTasks() {
            if (mTaskNotifications != null && mIsRunningTaskNotifications) {
                mTaskNotifications.setActivity(null);
                mTaskNotifications.cancel(true);
            }
        }
    }
}
