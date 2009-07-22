/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquared.error.FoursquaredCredentialsError;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * @author jlapenna
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {
    private static final String TAG = "PreferenceActivity";
    private static final boolean DEBUG = Foursquared.DEBUG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate");

        // Load the preferences from an XML resource
        this.addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Look up the phone number if its not set.
        setPhoneNumber();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (DEBUG) Log.d(TAG, "onStop()");
        try {
            ((Foursquared)getApplication()).loadCredentials();
        } catch (FoursquaredCredentialsError e) {
            if (DEBUG) Log.d(TAG, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setPhoneNumber() {
        if (DEBUG) Log.d(TAG, "Setting phone number if not set.");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String phoneNumber = settings.getString(Foursquared.PREFERENCE_PHONE, null);

        if (phoneNumber == null || TextUtils.isEmpty(phoneNumber)) {
            TelephonyManager telephony = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            phoneNumber = telephony.getLine1Number();
            if (phoneNumber.startsWith("1")) {
                phoneNumber = phoneNumber.substring(1);
            }

            if (DEBUG) Log.d(TAG, "Phone number not found. Setting it: " + phoneNumber);
            Editor editor = settings.edit();
            editor.putString(Foursquared.PREFERENCE_PHONE, phoneNumber);
            editor.commit();
        }
    }
}
