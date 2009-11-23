/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquare.util.VenueUtils;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.util.NotificationsUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class AddVenueActivity extends Activity {
    private static final String TAG = "AddVenueActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final double MINIMUM_ACCURACY_FOR_ADDRESS = 100.0;

    final private StateHolder mStateHolder = new StateHolder();

    private EditText mNameEditText;
    private EditText mAddressEditText;
    private EditText mCrossstreetEditText;
    private EditText mCityEditText;
    private EditText mStateEditText;
    private EditText mZipEditText;
    private EditText mPhoneEditText;
    private Button mAddVenueButton;

    private EditText[] mRequiredFields;
    private TextWatcher mRequiredFieldsWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            boolean requiredFieldsValid = true;
            for (int i = 0; i < mRequiredFields.length; i++) {
                if (TextUtils.isEmpty(mRequiredFields[i].getText())) {
                    requiredFieldsValid = false;
                    break;
                }
            }
            mAddVenueButton.setEnabled(requiredFieldsValid && mStateHolder.foursquareCity != null);
        }
    };

    private BroadcastReceiver mLoggedOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive: " + intent);
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.add_venue_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        mAddVenueButton = (Button)findViewById(R.id.addVenueButton);
        mNameEditText = (EditText)findViewById(R.id.nameEditText);
        mAddressEditText = (EditText)findViewById(R.id.addressEditText);
        mCrossstreetEditText = (EditText)findViewById(R.id.crossstreetEditText);
        mCityEditText = (EditText)findViewById(R.id.cityEditText);
        mStateEditText = (EditText)findViewById(R.id.stateEditText);
        mZipEditText = (EditText)findViewById(R.id.zipEditText);
        mPhoneEditText = (EditText)findViewById(R.id.phoneEditText);

        mAddVenueButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new AddVenueTask().execute();
            }
        });

        mRequiredFields = new EditText[] {
                mNameEditText, //
                mAddressEditText, //
                mCityEditText, //
                mStateEditText, //
        };

        for (int i = 0; i < mRequiredFields.length; i++) {
            mRequiredFields[i].addTextChangedListener(mRequiredFieldsWatcher);
        }

        if (getLastNonConfigurationInstance() != null) {
            setFields((StateHolder)getLastNonConfigurationInstance());
        } else {
            new AddressLookupTask().execute();
        }
        mStateHolder.foursquareCity = ((Foursquared)getApplication()).getUserCity();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((Foursquared)getApplication()).requestLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((Foursquared)getApplication()).removeLocationUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedOutReceiver);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mStateHolder;
    }

    private void setFields(StateHolder fields) {
        mStateHolder.geocodedAddress = fields.geocodedAddress;
        mStateHolder.location = fields.location;

        if (fields.geocodedAddress != null) {

            // Don't fill in the street unless we're reasonably confident we know where the user is.
            String address = fields.geocodedAddress.getAddressLine(0);
            double accuracy = fields.location.getAccuracy();
            if (address != null && (accuracy > 0.0 && accuracy < MINIMUM_ACCURACY_FOR_ADDRESS)) {
                if (DEBUG) Log.d(TAG, "Accuracy good enough, setting address field.");
                mAddressEditText.setText(address);
            }

            String city = fields.geocodedAddress.getLocality();
            if (city != null) {
                mCityEditText.setText(city);
            }

            String state = fields.geocodedAddress.getAdminArea();
            if (state != null) {
                mStateEditText.setText(state);
            }

            String zip = fields.geocodedAddress.getPostalCode();
            if (zip != null) {
                mZipEditText.setText(zip);
            }

            String phone = fields.geocodedAddress.getPhone();
            if (phone != null) {
                mPhoneEditText.setText(phone);
            }
        }
    }

    class AddVenueTask extends AsyncTask<Void, Void, Venue> {

        private Exception mReason;

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Venue doInBackground(Void... params) {
            // name, address, crossstreet, city, state, zip, cityid, phone
            try {
                return ((Foursquared)getApplication()).getFoursquare().addVenue( //
                        mNameEditText.getText().toString(), //
                        mAddressEditText.getText().toString(), //
                        mCrossstreetEditText.getText().toString(), //
                        mCityEditText.getText().toString(), //
                        mStateEditText.getText().toString(), //
                        mZipEditText.getText().toString(), //
                        mStateHolder.foursquareCity.getId(), //
                        mPhoneEditText.getText().toString(), null);
            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, "Exception doing add venue", e);
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Venue venue) {
            if (DEBUG) Log.d(TAG, "onPostExecute()");
            try {
                if (VenueUtils.isValid(venue)) {
                    Intent intent = new Intent(AddVenueActivity.this, VenueActivity.class);
                    intent.putExtra(Foursquared.EXTRA_VENUE_ID, venue.getId());
                    startActivity(intent);
                    finish();
                } else {
                    NotificationsUtil.ToastReasonForFailure(AddVenueActivity.this, mReason);
                }
            } finally {
                setProgressBarIndeterminateVisibility(false);
            }
        }

        @Override
        protected void onCancelled() {
            setProgressBarIndeterminateVisibility(false);
        }
    }

    class AddressLookupTask extends AsyncTask<Void, Void, StateHolder> {

        private Exception mReason;

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected StateHolder doInBackground(Void... params) {
            StateHolder stateHolder = new StateHolder();

            stateHolder.location = ((Foursquared)getApplication()).getLastKnownLocation();
            try {
                if (DEBUG) Log.d(TAG, stateHolder.location.toString());

                stateHolder.foursquareCity = ((Foursquared)getApplication()).getFoursquare()
                        .checkCity(LocationUtils.createFoursquareLocation(stateHolder.location));

                Geocoder geocoder = new Geocoder(AddVenueActivity.this);
                stateHolder.geocodedAddress = geocoder.getFromLocation(
                        stateHolder.location.getLatitude(), stateHolder.location.getLongitude(), 1)
                        .get(0);

            } catch (Exception e) {
                mReason = e;
            }
            return stateHolder;
        }

        @Override
        protected void onPostExecute(StateHolder fields) {
            setProgressBarIndeterminateVisibility(false);

            try {
                if (fields == null) {
                    NotificationsUtil.ToastReasonForFailure(AddVenueActivity.this, mReason);
                    finish();
                } else {
                    setFields(fields);
                }
            } finally {
                setProgressBarIndeterminateVisibility(false);

            }
        }

        @Override
        protected void onCancelled() {
            setProgressBarIndeterminateVisibility(false);
        }
    }

    private static class StateHolder {
        City foursquareCity = null;
        Location location = null;
        Address geocodedAddress = null;
    }
}
