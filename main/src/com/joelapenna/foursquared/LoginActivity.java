/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareCredentialsError;
import com.joelapenna.foursquare.error.FoursquareException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class LoginActivity extends Activity {
    public static final String TAG = "LoginActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private SharedPreferences mPrefs;
    private AsyncTask<Void, Void, Boolean> mLoginTask;
    private EditText mPhoneEditText;
    private EditText mPasswordEditText;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.edit().clear().commit();

        // Set up the UI.
        ensureUi();

        // Re-task if the request was cancelled.
        mLoginTask = (LoginTask)getLastNonConfigurationInstance();
        if (mLoginTask != null && mLoginTask.isCancelled()) {
            if (DEBUG) Log.d(TAG, "LoginTask previously cancelled, trying again.");
            mLoginTask = new LoginTask().execute();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        if (DEBUG) Log.d(TAG, "onRetainNonConfigurationInstance()");
        if (mLoginTask != null) {
            mLoginTask.cancel(true);
        }
        return mLoginTask;
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

    private void ensureUi() {
        setContentView(R.layout.main_activity);
        mPhoneEditText = ((EditText)findViewById(R.id.phoneEditText));
        mPasswordEditText = ((EditText)findViewById(R.id.passwordEditText));

        ensurePhoneNumber();

        String password = mPrefs.getString(Preferences.PREFERENCE_PASSWORD, null);
        if (!TextUtils.isEmpty(password)) {
            mPasswordEditText.setText(password);
        }

        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginTask = new LoginTask().execute();
            }
        });
    }

    private void ensurePhoneNumber() {
        if (TextUtils.isEmpty(mPhoneEditText.getText().toString())) {
            if (PreferenceActivity.DEBUG) Log.d(TAG, "Looking up phone number.");

            TelephonyManager telephony = (TelephonyManager)getSystemService(Activity.TELEPHONY_SERVICE);
            String lookup = telephony.getLine1Number();

            if (!TextUtils.isEmpty(lookup) && lookup.startsWith("1")) {
                lookup = lookup.substring(1);
                if (PreferenceActivity.DEBUG) Log.d(PreferenceActivity.TAG,
                        "Phone number not found. Setting it: " + lookup);
                mPhoneEditText.setText(lookup);
            }
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
                String phoneNumber = mPhoneEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                Editor editor = mPrefs.edit();
                if (Preferences.loginUser( //
                        Foursquared.getFoursquare(), phoneNumber, password, editor)) {
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
                Toast
                        .makeText(LoginActivity.this, "Welcome back to Foursquare.",
                                Toast.LENGTH_LONG).show();
                if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
                    startActivity(new Intent(LoginActivity.this, SearchVenuesActivity.class));
                }
                finish();
            } else {
                Toast.makeText(LoginActivity.this,
                        "Unable to log in. Please check your phone number and password.",
                        Toast.LENGTH_LONG).show();

                mPrefs.edit().clear().commit();
                Foursquared.getFoursquare().clearAllCredentials();
            }
            dismissProgressDialog();
        }

        @Override
        protected void onCancelled() {
            dismissProgressDialog();
        }
    }
}
