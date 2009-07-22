/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareCredentialsError;
import com.joelapenna.foursquare.error.FoursquareException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class PreferenceActivity extends android.preference.PreferenceActivity implements
        OnSharedPreferenceChangeListener {
    static final String TAG = "PreferenceActivity";
    static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final int MENU_CLEAR = 0;

    private AsyncTask<Void, Void, Boolean> mLoginTask = null;
    private SharedPreferences mPrefs;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate");

        this.addPreferencesFromResource(R.xml.preferences);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (getLastNonConfigurationInstance() != null) {
            if (DEBUG) Log.d(TAG, "Restoring state.");
            mLoginTask = (LoginTask)getLastNonConfigurationInstance();
            // Launch the task again if it was cancelled.
            if (mLoginTask != null && mLoginTask.isCancelled()) {
                if (DEBUG) Log.d(TAG, "LoginTask previously cancelled, trying again.");
                mLoginTask = new LoginTask().execute();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (DEBUG) Log.d(TAG, "onStop");
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        if (DEBUG) Log.d(TAG, "onRetainNonConfigurationInstance()");
        if (mLoginTask != null) {
            mLoginTask.cancel(true);
        }
        return mLoginTask;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_CLEAR, Menu.NONE, R.string.clear_prefs_label) //
                .setIcon(android.R.drawable.ic_menu_revert);
        Foursquared.addPreferencesToMenu(this, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CLEAR:
                mPrefs.edit().clear().commit();
                Foursquared.getFoursquare().setCredentials(null, null);
                Foursquared.getFoursquare().setOAuthToken(null, null);

                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
        }
        return false;
    }

    /**
     * This launches the login task when a user sets a username and password.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Preferences.PREFERENCE_PHONE) || key.equals(Preferences.PREFERENCE_PASSWORD)) {
            Log.d(TAG, key + " preference was changed");

            String phoneNumber = mPrefs.getString(Preferences.PREFERENCE_PHONE, null);
            String password = mPrefs.getString(Preferences.PREFERENCE_PASSWORD, null);

            if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password)) {
                // If the user hasn't set both username and password, no reason to login.
                return;
            }

            String strippedPhoneNumber = PhoneNumberUtils.stripSeparators(phoneNumber);
            if (!phoneNumber.equals(strippedPhoneNumber)) {
                Editor editor = mPrefs.edit();
                editor.putString(Preferences.PREFERENCE_PHONE, strippedPhoneNumber);
                editor.commit();
            }

            Log.d(TAG, "Attempting to log-in.");
            if (mLoginTask == null || mLoginTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
                mLoginTask = new LoginTask().execute();
            }
        }
    }

    private ProgressDialog showProgressDialog() {
        if (mProgressDialog == null) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Logging in");
            dialog.setMessage("Please wait while logging into Foursquare...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            mProgressDialog = dialog;
        }
        mProgressDialog.show();
        return mProgressDialog;
    }

    private void dismissProgressDialog() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException e) {
            // We don't mind. android cleared it for us.
        }
    }

    private class LoginTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "LoginTask";
        private static final boolean DEBUG = FoursquaredSettings.DEBUG;

        @Override
        protected void onPreExecute() {
            if (DEBUG) Log.d(TAG, "onPreExecute()");
            showProgressDialog();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (DEBUG) Log.d(TAG, "doInBackground()");
            try {
                String phoneNumber = mPrefs.getString(Preferences.PREFERENCE_PHONE, null);
                String password = mPrefs.getString(Preferences.PREFERENCE_PASSWORD, null);

                Editor editor = mPrefs.edit();
                if (Preferences.loginUser(Foursquared.getFoursquare(), phoneNumber, password,
                        editor)) {
                    editor.commit();
                    return true;
                }
            } catch (FoursquareCredentialsError e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareCredentialsError", e);
            } catch (FoursquareException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareException", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean loggedIn) {
            if (DEBUG) Log.d(TAG, "onPostExecute(): " + loggedIn);

            if (loggedIn) {
                Toast.makeText(PreferenceActivity.this, "Welcome back to Foursquare.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(PreferenceActivity.this,
                        "Unable to log in. Please check your phone number and password.",
                        Toast.LENGTH_LONG).show();
                mPrefs.edit().clear().commit();
                Foursquared.getFoursquare().setCredentials(null, null);
                Foursquared.getFoursquare().setOAuthToken(null, null);

                startActivity(new Intent(PreferenceActivity.this, MainActivity.class));
                finish();
            }
            dismissProgressDialog();
        }

        @Override
        protected void onCancelled() {
            dismissProgressDialog();
        }
    }
}
