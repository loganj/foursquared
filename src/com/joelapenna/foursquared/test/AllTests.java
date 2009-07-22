/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.test;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class AllTests extends TestSuite {

    public static Test suite() {
        return new TestSuiteBuilder(AllTests.class).includeAllPackagesUnderHere().build();
    }

}
