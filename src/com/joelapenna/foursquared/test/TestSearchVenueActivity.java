/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.test;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.SearchVenueActivity;

import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class TestSearchVenueActivity extends SearchVenueActivity {
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
