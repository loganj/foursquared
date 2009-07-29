/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.CheckinsActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class CheckinsActivityInstrumentationTestCase extends
        ActivityInstrumentationTestCase2<CheckinsActivity> {

    public CheckinsActivityInstrumentationTestCase() {
        super("com.joelapenna.foursquared", CheckinsActivity.class);
    }

    @SmallTest
    @UiThreadTest
    public void testSetSearchResults() {
        Group fakeResults = FoursquaredTest.createRandomCheckinGroups("Root");
        getActivity().setSearchResults(fakeResults);
        getActivity().putSearchResultsInAdapter(fakeResults);
    }
}
