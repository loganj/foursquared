/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.NotificationsUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class LoginActivity extends Activity {
    public static final String TAG = "LoginActivity";
    public static final String LAUNCH_MAIN_WHEN_FINISHED = "LAUNCH_MAIN_WHEN_FINISHED";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private AsyncTask<Void, Void, Boolean> mLoginTask;

    private TextView mNewAccountTextView;
    private EditText mPhoneUsernameEditText;
    private EditText mPasswordEditText;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_activity);

        Preferences.logoutUser( //
                ((Foursquared) getApplication()).getFoursquare(), //
                PreferenceManager.getDefaultSharedPreferences(this).edit());

        // Set up the UI.
        ensureUi();

        // Re-task if the request was cancelled.
        mLoginTask = (LoginTask) getLastNonConfigurationInstance();
        if (mLoginTask != null && mLoginTask.isCancelled()) {
            if (DEBUG) Log.d(TAG, "LoginTask previously cancelled, trying again.");
            mLoginTask = new LoginTask().execute();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((Foursquared) getApplication()).requestLocationUpdates(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((Foursquared) getApplication()).removeLocationUpdates();
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
            dialog.setTitle(R.string.login_dialog_title);
            dialog.setMessage(getString(R.string.login_dialog_message));
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
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginTask = new LoginTask().execute();
            }
        });

        mNewAccountTextView = (TextView) findViewById(R.id.newAccountTextView);
        mNewAccountTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(
                    Intent.ACTION_VIEW, Uri.parse(Foursquare.FOURSQUARE_MOBILE_SIGNUP)));
            }
        });

        mPhoneUsernameEditText = ((EditText) findViewById(R.id.phoneEditText));
        mPasswordEditText = ((EditText) findViewById(R.id.passwordEditText));

        TextWatcher fieldValidatorTextWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                button.setEnabled(phoneNumberEditTextFieldIsValid()
                        && passwordEditTextFieldIsValid());
            }

            private boolean phoneNumberEditTextFieldIsValid() {
                // This can be either a phone number or username so we don't
                // care too much about the
                // format.
                return !TextUtils.isEmpty(mPhoneUsernameEditText.getText());
            }

            private boolean passwordEditTextFieldIsValid() {
                return !TextUtils.isEmpty(mPasswordEditText.getText());
            }
        };

        mPhoneUsernameEditText.addTextChangedListener(fieldValidatorTextWatcher);
        mPasswordEditText.addTextChangedListener(fieldValidatorTextWatcher);
    }

    private class LoginTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "LoginTask";
        private static final boolean DEBUG = FoursquaredSettings.DEBUG;

        private Exception mReason;

        @Override
        protected void onPreExecute() {
            if (DEBUG) Log.d(TAG, "onPreExecute()");
            showProgressDialog();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (DEBUG) Log.d(TAG, "doInBackground()");
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(LoginActivity.this);
            Editor editor = prefs.edit();
            Foursquared foursquared = (Foursquared) getApplication();
            Foursquare foursquare = foursquared.getFoursquare();
            try {
                String phoneNumber = mPhoneUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                Foursquare.Location location = null;
                location = LocationUtils.createFoursquareLocation(
                    foursquared.getLastKnownLocation());

                boolean loggedIn = Preferences.loginUser(foursquare, phoneNumber, password,
                        location, editor);

                // Make sure prefs make a round trip.
                String userId = Preferences.getUserId(prefs);
                if (TextUtils.isEmpty(userId)) {
                    if (DEBUG) Log.d(TAG, "Preference store calls failed");
                    throw new FoursquareException(getResources().getString(
                            R.string.login_failed_login_toast));
                }
                return loggedIn;

            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, "Caught Exception logging in.", e);
                mReason = e;
                Preferences.logoutUser(foursquare, editor);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean loggedIn) {
            if (DEBUG) Log.d(TAG, "onPostExecute(): " + loggedIn);
            Foursquared foursquared = (Foursquared) getApplication();

            if (loggedIn) {

                sendBroadcast(new Intent(Foursquared.INTENT_ACTION_LOGGED_IN));
                Toast.makeText(LoginActivity.this, getString(R.string.login_welcome_toast),
                        Toast.LENGTH_LONG).show();

                // Launch the service to update any widgets, etc.
                foursquared.requestStartService();

                if ( getIntent().getBooleanExtra(LAUNCH_MAIN_WHEN_FINISHED, false) ) {
                // Launch the main activity to let the user do anything.
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                // Be done with the activity.
                finish();

            } else {
                sendBroadcast(new Intent(Foursquared.INTENT_ACTION_LOGGED_OUT));
                NotificationsUtil.ToastReasonForFailure(LoginActivity.this, mReason);
            }
            dismissProgressDialog();
        }

        @Override
        protected void onCancelled() {
            dismissProgressDialog();
        }
    }
}
