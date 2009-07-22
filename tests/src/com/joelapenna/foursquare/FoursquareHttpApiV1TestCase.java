/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare;

import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Credentials;

import android.test.suitebuilder.annotation.LargeTest;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquareHttpApiV1TestCase extends TestCase {

    @LargeTest
    public void test_authExchange() throws FoursquareException, IOException {
        FoursquareHttpApiV1 foursquare = new FoursquareHttpApiV1(TestCredentials.oAuthConsumerKey,
                TestCredentials.oAuthConsumerSecret);
        Credentials credentials = foursquare.authExchange(TestCredentials.testFoursquarePhone,
                TestCredentials.testFoursquarePassword);
        assertNotNull(credentials.getOauthToken());
        assertNotNull(credentials.getOauthTokenSecret());
    }
}
