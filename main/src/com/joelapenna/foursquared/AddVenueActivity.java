/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.Foursquared.LocationListener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class AddVenueActivity extends Activity {
    private static final String TAG = "AddVenueActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final int MENU_SUBMIT = 1;

    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    FieldsHolder mFieldsHolder = new FieldsHolder();

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
            setFields((FieldsHolder)getLastNonConfigurationInstance());
        } else {
            new AddressLookupTask().execute();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedInReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        // We should probably dynamically connect to any location provider we can find and not just
        // the gps/network providers.
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                LocationListener.LOCATION_UPDATE_MIN_TIME,
                LocationListener.LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                LocationListener.LOCATION_UPDATE_MIN_TIME,
                LocationListener.LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mLocationManager.removeUpdates(mLocationListener);
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

    private void setFields(FieldsHolder fields) {
        mFieldsHolder = fields;
        mCityEditText.setText(fields.foursquareCity.getName());

        if (fields.geocodedAddress != null) {
            String address = fields.geocodedAddress.getAddressLine(0);
            if (address != null) {
                mAddressEditText.setText(address);
            }

            String zip = fields.geocodedAddress.getPostalCode();
            if (zip != null) {
                mZipEditText.setText(zip);
            }

            String state = fields.geocodedAddress.getAdminArea();
            if (state != null) {
                mStateEditText.setText(state);
            }

            String phone = fields.geocodedAddress.getPhone();
            if (phone != null) {
                mPhoneEditText.setText(phone);
            }
        }
    }

    class AddVenueTask extends AsyncTask<Void, Void, Venue> {

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Venue doInBackground(Void... params) {
            // name, address, crossstreet, city, state, zip, cityid, phone
            try {
                return Foursquared.getFoursquare().addVenue( //
                        mNameEditText.getText().toString(), //
                        mAddressEditText.getText().toString(), //
                        mCrossstreetEditText.getText().toString(), //
                        mCityEditText.getText().toString(), //
                        mStateEditText.getText().toString(), //
                        mZipEditText.getText().toString(), //
                        mFieldsHolder.foursquareCity.getId(), //
                        mPhoneEditText.getText().toString());
            } catch (FoursquareException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareException", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Venue venue) {
            setProgressBarIndeterminateVisibility(false);
            if (venue == null) {
                Toast.makeText(AddVenueActivity.this, "Unable to add venue!", Toast.LENGTH_LONG)
                        .show();
            } else {
                Intent intent = new Intent(AddVenueActivity.this, VenueActivity.class);
                intent.putExtra(VenueActivity.EXTRA_VENUE, venue.getId());
                startActivity(intent);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            setProgressBarIndeterminateVisibility(false);
        }
    }

    class AddressLookupTask extends AsyncTask<Void, Void, FieldsHolder> {

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected FieldsHolder doInBackground(Void... params) {
            FieldsHolder fieldsHolder = new FieldsHolder();

            Location location = mLocationListener.getLastKnownLocation();
            try {
                if (DEBUG) Log.d(TAG, location.toString());
                fieldsHolder.foursquareCity = Foursquared.getFoursquare().checkCity(
                        String.valueOf(location.getLatitude()),
                        String.valueOf(location.getLongitude()));

                Geocoder geocoder = new Geocoder(AddVenueActivity.this);
                fieldsHolder.geocodedAddress = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1).get(0);

            } catch (FoursquareException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareException", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
            return fieldsHolder;
        }

        @Override
        protected void onPostExecute(FieldsHolder fields) {
            setProgressBarIndeterminateVisibility(false);

            try {
                if (fields == null) {
                    Toast.makeText(AddVenueActivity.this,
                            "Unable to lookup venue city. Try again later.", Toast.LENGTH_LONG)
                            .show();
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

    private static class FieldsHolder {
        City foursquareCity = null;
        Address geocodedAddress = null;
    }
}
