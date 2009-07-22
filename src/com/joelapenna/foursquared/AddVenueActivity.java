/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.Foursquared.LocationListener;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class AddVenueActivity extends Activity {
    private static final String TAG = "AddVenueActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    FieldsHolder mFieldsHolder;

    private EditText mCityEditText;
    private EditText mAddressEditText;
    private EditText mStateEditText;
    private EditText mZipEditText;
    private EditText mPhoneEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_venue_activity);

        mLocationListener = ((Foursquared)getApplication()).getLocationListener();
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        mCityEditText = (EditText)findViewById(R.id.cityEditText);
        mAddressEditText = (EditText)findViewById(R.id.addressEditText);
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
        protected Venue doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Venue venue) {

        }
    }

    class AddressLookupTask extends AsyncTask<Void, Void, FieldsHolder> {
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
            setFields(fields);
        }
    }

    private static class FieldsHolder {
        City foursquareCity = null;
        Address geocodedAddress = null;
    }
}
