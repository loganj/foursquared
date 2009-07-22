/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquared.VenueActivity;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueActivityInstrumentationTestCase extends
        ActivityInstrumentationTestCase2<VenueActivity> {

    public VenueActivityInstrumentationTestCase() {
        super("com.joelapenna.foursquared", VenueActivity.class);
    }

    @SmallTest
    public void testOnCreate() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(VenueActivity.EXTRA_VENUE, "40450");
        setActivityIntent(intent);

        VenueActivity activity = getActivity();
        activity.openOptionsMenu();
        activity.closeOptionsMenu();
    }
}
