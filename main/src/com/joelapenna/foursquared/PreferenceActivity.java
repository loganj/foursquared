/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareCredentialsError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.City;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {
    static final String TAG = "PreferenceActivity";
    static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private SharedPreferences mPrefs;

    // XXX: This is super hack. The correct way would be to make a custom preference subclass to
    // disply this information.
    private Preference mUpdateCityPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate");

        this.addPreferencesFromResource(R.xml.preferences);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mUpdateCityPreference = findPreference(Preferences.PREFERENCE_UPDATE_CITY);
        mUpdateCityPreference.setSummary("In "
                + mPrefs.getString(Preferences.PREFERENCE_CITY_NAME, "unknown"));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (DEBUG) Log.d(TAG, "onPreferenceTreeClick");
        String key = preference.getKey();
        if (Preferences.PREFERENCE_LOGOUT.equals(key)) {
            mPrefs.edit().clear().commit();
            Foursquared.getFoursquare().clearAllCredentials();

            startActivityForResult(new Intent(PreferenceActivity.this, LoginActivity.class),
                    LoginActivity.ACTIVITY_REQUEST_LOGIN);
            sendBroadcast(new Intent(Foursquared.INTENT_ACTION_LOGGED_OUT));

        } else if (Preferences.PREFERENCE_FRIEND_ADD.equals(key)) {
            startActivity(new Intent( //
                    Intent.ACTION_VIEW, Uri.parse("http://m.playfoursquare.com/addfriends")));

        } else if (Preferences.PREFERENCE_FRIEND_REQUESTS.equals(key)) {
            startActivity(new Intent( //
                    Intent.ACTION_VIEW, Uri.parse("http://m.playfoursquare.com/friends")));

        } else if (Preferences.PREFERENCE_UPDATE_CITY.equals(key)) {
            new UpdateCityTask().execute();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            startActivity(new Intent(this, SearchVenuesActivity.class));
        }
        finish();
    }

    private class UpdateCityTask extends AsyncTask<Void, Void, City> {
        private static final String TAG = "UpdateUserTask";
        private static final boolean DEBUG = FoursquaredSettings.DEBUG;

        @Override
        protected void onPreExecute() {
            if (DEBUG) Log.d(TAG, "onPreExecute()");
            Toast.makeText(PreferenceActivity.this, "Updating your city.", Toast.LENGTH_LONG)
                    .show();
        }

        @Override
        protected City doInBackground(Void... params) {
            if (DEBUG) Log.d(TAG, "doInBackground()");
            try {
                Location location = ((Foursquared)getApplication()).getLastKnownLocation();
                if (location == null) {
                    return null;
                }

                Foursquare foursquare = Foursquared.getFoursquare();

                City newCity = foursquare.checkCity(//
                        String.valueOf(location.getLatitude()), //
                        String.valueOf(location.getLongitude()));
                foursquare.switchCity(newCity.getId());

                Editor editor = mPrefs.edit();
                Preferences.storeCity(editor, newCity);
                editor.commit();

                return newCity;
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
            return null;
        }

        @Override
        protected void onPostExecute(City city) {
            if (city != null) {
                mUpdateCityPreference.setSummary("In " + city.getName());
                Toast.makeText(PreferenceActivity.this, "Welcome to " + city.getName() + "!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(PreferenceActivity.this, "Unable to determine your city!",
                        Toast.LENGTH_LONG).show();
            }

        }

        @Override
        protected void onCancelled() {
            Toast.makeText(PreferenceActivity.this, "Unable to update your city.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
