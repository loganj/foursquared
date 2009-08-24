/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.maps.BestLocationListener;
import com.joelapenna.foursquared.util.NotificationsUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class AddVenueActivity extends Activity {
    private static final String TAG = "AddVenueActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final double MINIMUM_ACCURACY_FOR_ADDRESS = 100.0;

    private static final int MENU_SUBMIT = 1;

    private BestLocationListener mLocationListener;
    private LocationManager mLocationManager;

    final private StateHolder mFieldsHolder = new StateHolder();

    private EditText mNameEditText;
    private EditText mCityEditText;
    private EditText mAddressEditText;
    private EditText mCrossstreetEditText;
    private EditText mStateEditText;
    private EditText mZipEditText;
    private EditText mPhoneEditText;
    private MenuItem mMenuSubmit;

    private BroadcastReceiver mLoggedInReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive: " + intent);
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_venue_activity);
        registerReceiver(mLoggedInReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        mLocationListener = ((Foursquared)getApplication()).getLocationListener();
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        mNameEditText = (EditText)findViewById(R.id.nameEditText);
        mCityEditText = (EditText)findViewById(R.id.cityEditText);
        mAddressEditText = (EditText)findViewById(R.id.addressEditText);
        mCrossstreetEditText = (EditText)findViewById(R.id.crossstreetEditText);
        mStateEditText = (EditText)findViewById(R.id.stateEditText);
        mZipEditText = (EditText)findViewById(R.id.zipEditText);
        mPhoneEditText = (EditText)findViewById(R.id.phoneEditText);

        if (getLastNonConfigurationInstance() != null) {
            setFields((StateHolder)getLastNonConfigurationInstance());
        } else {
            new AddressLookupTask().execute();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mFieldsHolder.foursquareCity = Preferences.getUser(prefs).getCity();
    }

    @Override
    public void onResume() {
        super.onResume();
        // We should probably dynamically connect to any location provider we can find and not just
        // the gps/network providers.
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                BestLocationListener.LOCATION_UPDATE_MIN_TIME,
                BestLocationListener.LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                BestLocationListener.LOCATION_UPDATE_MIN_TIME,
                BestLocationListener.LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(mLocationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedInReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        mMenuSubmit = menu.add(Menu.NONE, MENU_SUBMIT, Menu.NONE, R.string.add_venue_label) //
                .setIcon(android.R.drawable.ic_menu_add).setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SUBMIT:
                new AddVenueTask().execute();
                return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mMenuSubmit.setEnabled(mFieldsHolder.foursquareCity != null);
        return true;
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mFieldsHolder;
    }

    private void setFields(StateHolder fields) {
        mFieldsHolder.geocodedAddress = fields.geocodedAddress;
        mFieldsHolder.location = fields.location;

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
                        mFieldsHolder.foursquareCity.getId(), //
                        mPhoneEditText.getText().toString());
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
                Intent intent = new Intent(AddVenueActivity.this, VenueActivity.class);
                intent.putExtra(Foursquared.EXTRA_VENUE_ID, venue.getId());
                startActivity(intent);
                finish();
            } catch (Exception e) {
                NotificationsUtil.ToastReasonForFailure(AddVenueActivity.this, mReason);
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

            stateHolder.location = mLocationListener.getLastKnownLocation();
            try {
                if (DEBUG) Log.d(TAG, stateHolder.location.toString());

                stateHolder.foursquareCity = ((Foursquared)getApplication()).getFoursquare()
                        .checkCity(String.valueOf(stateHolder.location.getLatitude()),
                                String.valueOf(stateHolder.location.getLongitude()));

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
