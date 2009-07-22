/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquared.foursquare.Foursquare;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private Foursquare mFoursquare;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mFoursquare = new Foursquare("4158303607", "ci9ahXa9");
        Log.d(TAG, String.valueOf(mFoursquare.login()));
    }
}
