/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.Foursquared.LocationListener;

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

    public static final String EXTRA_FIRST_LOGIN = "com.joelapenna.foursquare.LoginActivity.FirstLogin";

    public static final int ACTIVITY_REQUEST_LOGIN = 1;

    private SharedPreferences mPrefs;
    private AsyncTask<Void, Void, Boolean> mLoginTask;

    private TextView mNewAccountTextView;
    private EditText mPhoneEditText;
    private EditText mPasswordEditText;

    private ProgressDialog mProgressDialog;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");
        setContentView(R.layout.login_activity);

        mLocationListener = ((Foursquared)getApplication()).getLocationListener();
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

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
    public void onResume() {
        super.onResume();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                LocationListener.LOCATION_UPDATE_MIN_TIME,
                LocationListener.LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                LocationListener.LOCATION_UPDATE_MIN_TIME,
                LocationListener.LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(mLocationListener);
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
                        Intent.ACTION_VIEW, Uri.parse("http://m.playfoursquare.com/signup")));
            }
        });

        mPhoneEditText = ((EditText)findViewById(R.id.phoneEditText));
        mPasswordEditText = ((EditText)findViewById(R.id.passwordEditText));

        TextWatcher FieldValidatorTextWatcher = new TextWatcher() {
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
                CharSequence phone = mPhoneEditText.getText();
                return !TextUtils.isEmpty(phone) && TextUtils.isDigitsOnly(phone);
            }

            private boolean passwordEditTextFieldIsValid() {
                CharSequence password = mPasswordEditText.getText();
                return !TextUtils.isEmpty(password);
            }
        };

        mPhoneEditText.addTextChangedListener(FieldValidatorTextWatcher);
        mPasswordEditText.addTextChangedListener(FieldValidatorTextWatcher);

        ensurePhoneNumber();
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
                String phoneNumber = mPhoneEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                Editor editor = mPrefs.edit();

                User user = Preferences.loginUser( //
                        Foursquared.getFoursquare(), phoneNumber, password, editor);
                if (user == null) {
                    return false;
                }

                City city = Preferences.switchCityIfChanged(Foursquared.getFoursquare(), user,
                        mLocationListener.getLastKnownLocation());
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

            if (loggedIn) {
                String city = mPrefs.getString(Preferences.PREFERENCE_CITY_NAME, null);
                Toast.makeText( //
                        LoginActivity.this, "Welcome to " + city + "!", Toast.LENGTH_LONG).show();
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                Toast.makeText(LoginActivity.this,
                        "Unable to log in. Please check your phone number and password.",
                        Toast.LENGTH_LONG).show();
                if (DEBUG) Log.d(TAG, "Reason for login failure: ", mReason);

                mPrefs.edit().clear().commit();
                Foursquared.getFoursquare().clearAllCredentials();
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
