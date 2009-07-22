/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";
    public static final boolean DEBUG = Foursquared.DEBUG;

    private boolean mStartedPreferences;

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) Log.d(TAG, "onStart()");

        if (((Foursquared)getApplication()).getFoursquare().hasCredentials()) {
            startDefaultActivity();
            finish();
        } else if (!mStartedPreferences) {
            if (DEBUG) Log.d(TAG, "No credentials set, starting preferences.");
            startPreferencesActivity();
        } else {
            if (DEBUG) Log.d(TAG, "Already forced the user to preferences, not trying again.");
            ensureViews();
            Toast.makeText(this, "Set your phone and password!", Toast.LENGTH_SHORT).show();
        }
    }

    private void ensureViews() {
        setContentView(R.layout.main_activity);
        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startPreferencesActivity();
            }
        });
    }

    private void startDefaultActivity() {
        startActivity(new Intent(MainActivity.this, SearchVenueActivity.class));
    }

    private void startPreferencesActivity() {
        mStartedPreferences = true;
        startActivity(new Intent(MainActivity.this, PreferenceActivity.class));
    }
}
