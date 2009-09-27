/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.test;

import com.joelapenna.foursquared.Foursquared;

import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquaredAppTestCase extends ApplicationTestCase<Foursquared> {

    public FoursquaredAppTestCase() {
        super(Foursquared.class);
    }

    @MediumTest
    public void testLocationMethods() {
        createApplication();
        getApplication().getLastKnownLocation();
        getApplication().getLocationListener();
    }

    @SmallTest
    public void testPreferences() {
        createApplication();
    }
}
