/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.NotificationsUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {
    private static final String TAG = "PreferenceActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private SharedPreferences mPrefs;

    private BroadcastReceiver mLoggedOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive: " + intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        this.addPreferencesFromResource(R.xml.preferences);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedOutReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (DEBUG) Log.d(TAG, "onPreferenceTreeClick");
        String key = preference.getKey();
        if (Preferences.PREFERENCE_LOGOUT.equals(key)) {
            mPrefs.edit().clear().commit();
            ((Foursquared)getApplication()).getFoursquare().clearAllCredentials();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            sendBroadcast(new Intent(Foursquared.INTENT_ACTION_LOGGED_OUT));

        } else if (Preferences.PREFERENCE_FRIEND_ADD.equals(key)) {
            startActivity(new Intent( //
                    Intent.ACTION_VIEW, Uri.parse("http://m.foursquare.com./addfriends")));

        } else if (Preferences.PREFERENCE_FRIEND_REQUESTS.equals(key)) {
            startActivity(new Intent( //
                    Intent.ACTION_VIEW, Uri.parse("http://m.foursquare.com./friends")));

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
            Toast.makeText(PreferenceActivity.this,
                    getString(R.string.preferences_updating_city_toast), Toast.LENGTH_LONG).show();
        }

        @Override
        protected City doInBackground(Void... params) {
            if (DEBUG) Log.d(TAG, "doInBackground()");
            try {
                Location location = getMostRecentLocation();
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

        private Location getMostRecentLocation() {
            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

            Location bestLocation = null;
            List<String> providers = locationManager.getProviders(false);
            for (int i = 0; i < providers.size(); i++) {
                Location location = locationManager.getLastKnownLocation(providers.get(i));
                if (bestLocation == null
                        || (location != null && bestLocation.getTime() <= location.getTime())) {
                    bestLocation = location;
                    continue;
                }
            }
            return bestLocation;
        }

        @Override
        protected void onPostExecute(City city) {
            if (city == null) {
                NotificationsUtil.ToastReasonForFailure(PreferenceActivity.this, mReason);
            } else {
                Toast.makeText(PreferenceActivity.this,
                        getString(R.string.preferences_welcome_city_toast, city.getName()),
                        Toast.LENGTH_LONG).show();
                // Back to the stupid lame-o hack of restarting the activity so it shows the
                // background-updated preference.
                finish();
                startActivity(getIntent());
            }
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(PreferenceActivity.this,
                    getString(R.string.preferences_unable_to_find_city_toast), Toast.LENGTH_LONG)
                    .show();
        }
    }
}
