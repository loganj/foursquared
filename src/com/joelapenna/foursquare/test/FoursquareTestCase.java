/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.test;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Credentials;

import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquareTestCase extends TestCase {

    @SmallTest
    public void test_hasCredentials() {
        Foursquare foursquare = new Foursquare(TestCredentials.oAuthConsumerKey,
                TestCredentials.oAuthConsumerSecret);
        assertFalse(foursquare.hasCredentials());

        foursquare.setCredentials("a", "b");
        assertTrue(foursquare.hasCredentials());

        // check for v1/oauth credentials
        assertFalse(foursquare.hasCredentials(true));

        foursquare.setCredentials("a", "b", "c", "d");
        assertTrue(foursquare.hasCredentials(true));

    }

    @LargeTest
    public void test_authExchange() throws FoursquareError, FoursquareParseException, IOException {
        Foursquare foursquare = new Foursquare(TestCredentials.oAuthConsumerKey,
                TestCredentials.oAuthConsumerSecret);
        foursquare.setCredentials(TestCredentials.testFoursquareUsername,
                TestCredentials.testFoursquarePassword);

        Credentials credentials = foursquare.authExchange();

        assertNotNull(credentials.getOauthToken());
        assertNotNull(credentials.getOauthTokenSecret());
    }
}
