/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.test;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.SearchVenuesActivity;

import android.util.Log;

/**
 * Use this to inject a bit of data into the SearchVenuesActivity for exploratory testing.
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class TestSearchVenueActivity extends SearchVenuesActivity {
    static final String TAG = "TestSearchVenueActivity";
    static final boolean DEBUG = true;

    @Override
    public void handleOnCreateIntent() {
        if (DEBUG) Log.d(TAG, "Running new intent.");
        Group fakeResults = FoursquaredTest.createRandomVenueGroups("Root");
        setSearchResults(fakeResults);
        putSearchResultsInAdapter(fakeResults);
    }
}
