/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.maps.BestLocationListener;
import com.joelapenna.foursquared.preferences.Preferences;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private AsyncTask<Void, Void, Boolean> mLoginTask;

    private TextView mNewAccountTextView;
    private EditText mPhoneUsernameEditText;
    private EditText mPasswordEditText;

    private ProgressDialog mProgressDialog;

    private BestLocationListener mLocationListener = new BestLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");
        setContentView(R.layout.login_activity);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().clear().commit();

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
    public void onResume() {
        super.onResume();
        mLocationListener.register((LocationManager)getSystemService(Context.LOCATION_SERVICE));
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationListener.unregister();
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
        final Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginTask = new LoginTask().execute();
            }
        });

        mNewAccountTextView = (TextView)findViewById(R.id.newAccountTextView);
        mNewAccountTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent( //
                        Intent.ACTION_VIEW, Uri.parse("http://m.foursquare.com./signup")));
            }
        });

        mPhoneUsernameEditText = ((EditText)findViewById(R.id.phoneEditText));
        mPasswordEditText = ((EditText)findViewById(R.id.passwordEditText));

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
                // This can be either a phone number or username so we don't care too much about the
                // format.
                return !TextUtils.isEmpty(mPhoneUsernameEditText.getText());
            }

            private boolean passwordEditTextFieldIsValid() {
                return !TextUtils.isEmpty(mPasswordEditText.getText());
            }
        };

        mPhoneUsernameEditText.addTextChangedListener(fieldValidatorTextWatcher);
        mPasswordEditText.addTextChangedListener(fieldValidatorTextWatcher);

        ensurePhoneNumber();
    }

    private void ensurePhoneNumber() {
        if (TextUtils.isEmpty(mPhoneUsernameEditText.getText().toString())) {
            if (LoginActivity.DEBUG) Log.d(TAG, "Looking up phone number.");

            TelephonyManager telephony = (TelephonyManager)getSystemService(Activity.TELEPHONY_SERVICE);
            String lookup = telephony.getLine1Number();

            if (!TextUtils.isEmpty(lookup) && lookup.startsWith("1")) {
                lookup = lookup.substring(1);
                if (LoginActivity.DEBUG) Log.d(LoginActivity.TAG,
                        "Phone number not found. Setting it: " + lookup);
                mPhoneUsernameEditText.setText(lookup);
            }
        }
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
            try {
                String phoneNumber = mPhoneUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(LoginActivity.this);
                Editor editor = prefs.edit();

                User user = Preferences.loginUser(((Foursquared)getApplication()).getFoursquare(),
                        phoneNumber, password, editor);
                if (user == null) {
                    return false;
                }

                // Use a location to switch the user's foursquare location.
                City city = Preferences.switchCity(((Foursquared)getApplication()).getFoursquare(),
                        mLocationListener.getLastKnownLocation());

                // Fallback to the foursquare server's understanding of the user's city.
                if (city == null) {
                    city = user.getCity();
                }
                Preferences.storeCity(editor, city);

                editor.commit();
                return true;

            } catch (Exception e) {
                mReason = e;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean loggedIn) {
            if (DEBUG) Log.d(TAG, "onPostExecute(): " + loggedIn);

            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(LoginActivity.this);

            if (loggedIn) {
                String city = prefs.getString(Preferences.PREFERENCE_CITY_NAME, null);
                Toast.makeText(
                        //
                        LoginActivity.this, getString(R.string.login_welcome_toast, city),
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, R.string.login_failed_login_toast,
                        Toast.LENGTH_LONG).show();
                if (DEBUG) Log.d(TAG, "Reason for login failure: ", mReason);

                prefs.edit().clear().commit();
                ((Foursquared)getApplication()).getFoursquare().clearAllCredentials();
                // XXX I don't know if you can call setResult multiple times. If you can't and the
                // first login result fails, then even a subsequent result OK will end up firing a
                // CANCELED result
                setResult(Activity.RESULT_CANCELED);
            }
            dismissProgressDialog();
        }

        @Override
        protected void onCancelled() {
            dismissProgressDialog();
        }
    }
}
