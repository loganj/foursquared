/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareCredentialsError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Credentials;
import com.joelapenna.foursquare.types.User;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
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
    static final boolean DEBUG = Foursquared.DEBUG;

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
        // Look up the phone number if its not set.
        setPhoneNumber();
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
                Editor editor = mPrefs.edit();
                editor.clear();
                editor.commit();
                Foursquared.getFoursquare().setCredentials(null, null);
                Foursquared.getFoursquare().setOAuthToken(null, null);
                // Lame-o hack to force update all the preference views.
                finish();
                startActivity(getIntent());
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

    private void setPhoneNumber() {
        if (DEBUG) Log.d(TAG, "Setting phone number if not set.");
        String phoneNumber = mPrefs.getString(Preferences.PREFERENCE_PHONE, null);

        if (TextUtils.isEmpty(phoneNumber)) {
            if (DEBUG) Log.d(TAG, "Phone number not found.");
            TelephonyManager telephony = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            phoneNumber = telephony.getLine1Number();
            if (!TextUtils.isEmpty(phoneNumber) && phoneNumber.startsWith("1")) {
                phoneNumber = phoneNumber.substring(1);
                if (DEBUG) Log.d(TAG, "Phone number not found. Setting it: " + phoneNumber);
                Editor editor = mPrefs.edit();
                editor.putString(Preferences.PREFERENCE_PHONE, PhoneNumberUtils
                        .stripSeparators(phoneNumber));
                editor.commit();
            }
        }
    }

    private class LoginTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "LoginTask";
        private static final boolean DEBUG = Foursquared.DEBUG;

        @Override
        protected void onPreExecute() {
            if (DEBUG) Log.d(TAG, "onPreExecute()");
            showProgressDialog();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (DEBUG) Log.d(TAG, "doInBackground()");
            try {
                verifyCredentials(mPrefs, Foursquared.getFoursquare(), true);
                return true;
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
            }
            dismissProgressDialog();
        }

        @Override
        protected void onCancelled() {
            dismissProgressDialog();
        }
    }

    private static void verifyCredentials(SharedPreferences preferences, Foursquare foursquare,
            boolean doAuthExchange) throws FoursquareCredentialsError, FoursquareException,
            IOException {
        if (DEBUG) Log.d(TAG, "verifyCredentials()");

        String phoneNumber = preferences.getString(Preferences.PREFERENCE_PHONE, null);
        String password = preferences.getString(Preferences.PREFERENCE_PASSWORD, null);

        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password)) {
            throw new FoursquareCredentialsError("Phone number or password not set in preferences.");
        }

        final Editor editor = preferences.edit();
        loginUser(editor, foursquare, phoneNumber, password);

        String oauthToken = preferences.getString(Preferences.PREFERENCE_OAUTH_TOKEN, null);
        String oauthTokenSecret = preferences.getString(Preferences.PREFERENCE_OAUTH_TOKEN_SECRET,
                null);
        foursquare.setCredentials(phoneNumber, password);
        foursquare.setOAuthToken(oauthToken, oauthTokenSecret);
    }

    static void loginUser(final Editor editor, final Foursquare foursquare, String phoneNumber,
            String password) throws FoursquareCredentialsError, FoursquareException, IOException {
        if (PreferenceActivity.DEBUG) Log.d(PreferenceActivity.TAG, "Trying to log in.");

        foursquare.setCredentials(phoneNumber, password);

        if (DEBUG) Log.d(TAG, "doAuthExchange specified for loginUser");
        foursquare.setOAuthToken(null, null);
        Credentials credentials = foursquare.authExchange();
        Preferences.storeAuthExchangeCredentials(editor, credentials);

        foursquare.setOAuthToken(credentials.getOauthToken(), credentials.getOauthTokenSecret());

        User user = foursquare.user(null, false, false);
        Preferences.storeUser(editor, user);
    }
}
