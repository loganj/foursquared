/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquared.foursquare.Foursquare;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private Foursquare mFoursquare;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mFoursquare = new Foursquare("testuser@joelapenna.com", "ci9ahXa9");
        mFoursquare.login();
    }
}
