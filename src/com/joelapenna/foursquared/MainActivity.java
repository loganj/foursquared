/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquared.error.FoursquaredCredentialsError;

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

    public void startDefaultActivity() {
        startActivity(new Intent(MainActivity.this, SearchVenueActivity.class));
    }

    public void startPreferencesActivity() {
        mStartedPreferences = true;
        startActivity(new Intent(MainActivity.this, PreferenceActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) Log.d(TAG, "onStart()");
        try {
            ((Foursquared)getApplication()).loadCredentials();
            startDefaultActivity();
            finish();
        } catch (FoursquaredCredentialsError e) {
            if (!mStartedPreferences) {
                startPreferencesActivity();
            } else {
                ensureViews();
                Toast.makeText(this, "Go set your settings!", Toast.LENGTH_SHORT);
            }
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

}
