/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.test;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.CheckinsActivity;

import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class TestCheckinsActivity extends CheckinsActivity {
    static final String TAG = "TestCheckinsActivity";
    static final boolean DEBUG = true;

    @Override
    public void handleOnCreateIntent() {
        if (DEBUG) Log.d(TAG, "Running new intent.");
        Group fakeResults = FoursquaredTest.createRandomCheckinGroups("Root");
        setSearchResults(fakeResults);
        putSearchResultsInAdapter(fakeResults);
    }
}
