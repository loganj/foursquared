/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquared.util.NotificationsUtil;

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

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {
    static final String TAG = "PreferenceActivity";
    static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate");

        this.addPreferencesFromResource(R.xml.preferences);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (DEBUG) Log.d(TAG, "onPreferenceTreeClick");
        String key = preference.getKey();
        if (Preferences.PREFERENCE_LOGOUT.equals(key)) {
            mPrefs.edit().clear().commit();
            ((Foursquared)getApplication()).getFoursquare().clearAllCredentials();

            startActivityForResult(new Intent(PreferenceActivity.this, LoginActivity.class),
                    LoginActivity.ACTIVITY_REQUEST_LOGIN);
            sendBroadcast(new Intent(Foursquared.INTENT_ACTION_LOGGED_OUT));

        } else if (Preferences.PREFERENCE_FRIEND_ADD.equals(key)) {
            startActivity(new Intent( //
                    Intent.ACTION_VIEW, Uri.parse("http://m.playfoursquare.com/addfriends")));

        } else if (Preferences.PREFERENCE_FRIEND_REQUESTS.equals(key)) {
            startActivity(new Intent( //
                    Intent.ACTION_VIEW, Uri.parse("http://m.playfoursquare.com/friends")));

        } else if (Preferences.PREFERENCE_CITY_NAME.equals(key)) {
            new UpdateCityTask().execute();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            startActivity(new Intent(this, NearbyVenuesActivity.class));
        }
        finish();
    }

    private class UpdateCityTask extends AsyncTask<Void, Void, City> {
        private static final String TAG = "UpdateCityTask";
        private static final boolean DEBUG = FoursquaredSettings.DEBUG;

        private Exception mReason;

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
                    if (DEBUG) Log.d(TAG, "unable to determine location");
                    return null;
                }

                Foursquare foursquare = ((Foursquared)getApplication()).getFoursquare();

                City newCity = foursquare.checkCity(//
                        String.valueOf(location.getLatitude()), //
                        String.valueOf(location.getLongitude()));
                foursquare.switchCity(newCity.getId());

                Editor editor = mPrefs.edit();
                Preferences.storeCity(editor, newCity);
                editor.commit();

                return newCity;
            } catch (Exception e) {
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(City city) {
            if (city == null) {
                NotificationsUtil.ToastReasonForFailure(PreferenceActivity.this, mReason);
            } else {
                Toast.makeText(PreferenceActivity.this, "Welcome to " + city.getName() + "!",
                        Toast.LENGTH_LONG).show();
                // Back to the stupid lame-o hack of restarting the activity so it shows the
                // background-updated preference.
                finish();
                startActivity(getIntent());
            }
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(PreferenceActivity.this, "Unable to update your city.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
