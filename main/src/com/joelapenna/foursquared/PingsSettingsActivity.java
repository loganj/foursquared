/**
 * Copyright 2010 Mark Wyszomierski
 */
package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Settings;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.app.NotificationsService;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.NotificationsUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * @date June 2, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class PingsSettingsActivity extends Activity {
    private static final String TAG = "NotificationSettingsActivity";

    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private SharedPreferences mPrefs;
    private StateHolder mStateHolder;
    private ProgressDialog mDlgProgress;
    private CheckBox mCheckBoxNotifications;
    private Spinner mSpinnerInterval;
    private CheckBox mCheckBoxVibrate;
    

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

        setContentView(R.layout.pings_settings_activity);
        setTitle(getResources().getString(R.string.notification_settings_title));
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        ensureUi();
        
        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
            mStateHolder.setActivity(this);
            
            if (mStateHolder.getIsRunningTaskNotifications()) {
                startProgressBar(
                    getResources().getString(
                            R.string.notification_settings_progressbar_title_notifications), 
                    getResources().getString(
                            R.string.notification_settings_progressbar_message_notifications));
            }
        } else {
            // Get a fresh copy of the user object in an attempt to keep the notification
            // setting in sync.
            mStateHolder = new StateHolder(this);
            mStateHolder.startTaskUpdateUser(this);
        }
    }
    
    private void ensureUi() {
        mCheckBoxNotifications = (CheckBox)findViewById(R.id.notifications_on);
        mCheckBoxNotifications.setChecked(mPrefs.getBoolean(Preferences.PREFERENCE_NOTIFICATIONS, false));
        mCheckBoxNotifications.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mStateHolder.startTaskNotifications(
                        PingsSettingsActivity.this, mCheckBoxNotifications.isChecked());
            }
        });
        
        mSpinnerInterval = (Spinner)findViewById(R.id.notifications_interval);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.notifications_refresh_interval_in_minutes, 
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerInterval.setAdapter(adapter);
        mSpinnerInterval.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
                String[] values = PingsSettingsActivity.this.getResources().getStringArray(
                        R.array.notifications_refresh_interval_in_minutes_values);
                mPrefs.edit().putString(
                        Preferences.PREFERENCE_NOTIFICATIONS_INTERVAL, values[position]).commit();

                restartNotifications();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        setIntervalSpinnerFromSettings();
        
        mCheckBoxVibrate = (CheckBox)findViewById(R.id.notifications_vibrate);
        mCheckBoxVibrate.setChecked(mPrefs.getBoolean(Preferences.PREFERENCE_NOTIFICATIONS_VIBRATE, false));
        mCheckBoxVibrate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mPrefs.edit().putBoolean(
                        Preferences.PREFERENCE_NOTIFICATIONS_VIBRATE, mCheckBoxVibrate.isChecked()).commit();
            }
        });
    }
    
    private void setIntervalSpinnerFromSettings() {
        String selected = mPrefs.getString(Preferences.PREFERENCE_NOTIFICATIONS_INTERVAL, "30");
        String[] values = getResources().getStringArray(
                R.array.notifications_refresh_interval_in_minutes_values);
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(selected)) {
                mSpinnerInterval.setSelection(i);
                return;
            }
        }
        mSpinnerInterval.setSelection(1);
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
        
        if (mStateHolder.getIsRunningTaskNotifications()) {
            startProgressBar(
                getResources().getString(
                    R.string.notification_settings_progressbar_title_notifications), 
                getResources().getString(
                    R.string.notification_settings_progressbar_message_notifications));
        } else if (mStateHolder.getIsRunningTaskUpdateUser()) {
            startProgressBar(
                    getResources().getString(
                        R.string.notification_settings_progressbar_title_updateuser), 
                    getResources().getString(
                        R.string.notification_settings_progressbar_message_updateuser));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
     
        if (isFinishing()) {
            stopProgressBar();
            mStateHolder.cancelTasks();
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

    private void onPostTaskNotification(Settings settings, boolean changeToState, Exception reason) {
        if (settings == null) {
            NotificationsUtil.ToastReasonForFailure(this, reason);
            
            // Reset the checkbox.
            mCheckBoxNotifications.setChecked(!changeToState);
            
        } else {
            Toast.makeText(
                    this, 
                    "Notifications have been turned " + settings.getPings() + "!", 
                    Toast.LENGTH_SHORT).show();
            
            if (settings.getPings().equals("on")) {
                NotificationsService.setupNotifications(this);
                mPrefs.edit().putBoolean(Preferences.PREFERENCE_NOTIFICATIONS, true).commit();
            } else {
                NotificationsService.cancelNotifications(this);
                mPrefs.edit().putBoolean(Preferences.PREFERENCE_NOTIFICATIONS, false).commit();
            }
            restartNotifications();
        }
        mStateHolder.setIsRunningTaskNotifications(false);
        stopProgressBar();
    }
    
    private void onPostTaskUserUpdate(User user, Exception reason) {
        if (user == null) {
            NotificationsUtil.ToastReasonForFailure(this, reason);
            finish();
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                    PingsSettingsActivity.this);
            Editor editor = prefs.edit();
            Preferences.storeUser(editor, user);
            if (!editor.commit()) {
                Log.e(TAG, "Error storing user object.");
            }
    
            if (user.getSettings().getPings().equals("on")) {
                mCheckBoxNotifications.setChecked(true);
                mPrefs.edit().putBoolean(Preferences.PREFERENCE_NOTIFICATIONS, true).commit();
            } else {
                mCheckBoxNotifications.setChecked(false);
                mPrefs.edit().putBoolean(Preferences.PREFERENCE_NOTIFICATIONS, false).commit();
            }
            restartNotifications();
        }
        mStateHolder.setIsRunningTaskUpdateUser(false);
        stopProgressBar();
    }
    
    private static class UpdateNotificationsTask extends AsyncTask<Void, Void, Settings> {
        private static final String TAG = "UpdateNotificationsTask";
        private static final boolean DEBUG = FoursquaredSettings.DEBUG;
        private Exception mReason;
        private boolean mNotificationsOn;
        private PingsSettingsActivity mActivity;

        
        public UpdateNotificationsTask(PingsSettingsActivity activity, boolean on) {
            mActivity = activity;
            mNotificationsOn = on;
        }
        
        @Override
        protected void onPreExecute() {
            mActivity.startProgressBar(
                    mActivity.getResources().getString(
                            R.string.notification_settings_progressbar_title_notifications), 
                    mActivity.getResources().getString(
                            R.string.notification_settings_progressbar_message_notifications));
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

        public void setActivity(PingsSettingsActivity activity) {
            mActivity = activity;
        }
    }
    
    private static class UpdateUserTask extends AsyncTask<Void, Void, User> {
        private PingsSettingsActivity mActivity;
        private Exception mReason;

        public UpdateUserTask(PingsSettingsActivity activity) {
            mActivity = activity;
        }
        
        @Override
        protected User doInBackground(Void... params) {
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();
                Location location = foursquared.getLastKnownLocation();
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
            if (mActivity != null) {
                mActivity.onPostTaskUserUpdate(user, mReason);
            }
        }
        
        public void setActivity(PingsSettingsActivity activity) {
            mActivity = activity;
        }
    }
    
    private void restartNotifications() {
        NotificationsService.cancelNotifications(this);
        NotificationsService.setupNotifications(this);
    }
    
    private static class StateHolder {
        private UpdateNotificationsTask mTaskNotifications;
        private UpdateUserTask mTaskUpdateUser;
        private boolean mIsRunningTaskNotifications;
        private boolean mIsRunningTaskUpdateUser;

        public StateHolder(Context context) {
            mTaskNotifications = null;
            mTaskUpdateUser = null;
            mIsRunningTaskNotifications = false;
            mIsRunningTaskUpdateUser = false;
        }

        public void startTaskNotifications(PingsSettingsActivity activity, boolean on) {
            mIsRunningTaskNotifications = true;
            mTaskNotifications = new UpdateNotificationsTask(activity, on);
            mTaskNotifications.execute();
        }

        public void startTaskUpdateUser(PingsSettingsActivity activity) {
            mIsRunningTaskUpdateUser = true;
            mTaskUpdateUser = new UpdateUserTask(activity);
            mTaskUpdateUser.execute();
        }
        
        public void setActivity(PingsSettingsActivity activity) {
            if (mTaskNotifications != null) {
                mTaskNotifications.setActivity(activity);
            }
            if (mTaskUpdateUser != null) {
                mTaskUpdateUser.setActivity(activity);
            }
        }

        public boolean getIsRunningTaskNotifications() {
            return mIsRunningTaskNotifications;
        }

        public boolean getIsRunningTaskUpdateUser() {
            return mIsRunningTaskUpdateUser;
        }
        
        public void setIsRunningTaskNotifications(boolean isRunningTaskNotifications) {
            mIsRunningTaskNotifications = isRunningTaskNotifications;
        }
        
        public void setIsRunningTaskUpdateUser(boolean isRunningTaskUpdateUser) {
            mIsRunningTaskUpdateUser = isRunningTaskUpdateUser;
        }
        
        public void cancelTasks() {
            if (mTaskNotifications != null && mIsRunningTaskNotifications) {
                mTaskNotifications.setActivity(null);
                mTaskNotifications.cancel(true);
            }
            if (mTaskUpdateUser != null && mIsRunningTaskUpdateUser) {
                mTaskUpdateUser.setActivity(null);
                mTaskUpdateUser.cancel(true);
            }
        }
    }
}
