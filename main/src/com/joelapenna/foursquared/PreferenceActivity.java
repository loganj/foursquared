/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

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

        // This here sir, is a nasty hack that will allow me to add a login button to view!
        RelativeLayout l = (RelativeLayout)LayoutInflater.from(this).inflate(
                R.layout.preference_activity, null);

        ((Button)l.findViewById(R.id.loginButton)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPrefs.edit().clear().commit();
                Foursquared.getFoursquare().clearAllCredentials();

                startActivityForResult(new Intent(PreferenceActivity.this, LoginActivity.class),
                        LoginActivity.ACTIVITY_REQUEST_LOGIN);
                sendBroadcast(new Intent(Foursquared.INTENT_ACTION_LOGGED_OUT));
                finish();
            }
        });

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(//
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        this.addContentView(l, lp);
    }
}
