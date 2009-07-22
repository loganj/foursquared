/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.CheckinsActivity;

import android.util.Log;

/**
 * Use this to inject a bit of data into the CheckinsActivity for exploratory testing.
 * 
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
