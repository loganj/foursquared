/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");

        if (Foursquared.getFoursquare().hasCredentials()) {
            startActivity(new Intent(this, SearchVenuesActivity.class));
            finish();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            startActivityForResult(intent, LoginActivity.ACTIVITY_REQUEST_LOGIN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            startActivity(new Intent(this, SearchVenuesActivity.class));
        }
        finish();
    }
}
