/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.test;

import com.joelapenna.foursquare.FoursquareHttpApiV1;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Credentials;

import android.test.suitebuilder.annotation.LargeTest;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquareHttpApiV1TestCase extends TestCase {

    @LargeTest
    public void test_authExchange() throws FoursquareError, FoursquareParseException, IOException {
        FoursquareHttpApiV1 foursquare = new FoursquareHttpApiV1(TestCredentials.oAuthConsumerKey,
                TestCredentials.oAuthConsumerSecret);
        Credentials credentials = foursquare.authExchange(TestCredentials.testFoursquareUsername,
                TestCredentials.testFoursquarePassword);
        assertNotNull(credentials.getOauthToken());
        assertNotNull(credentials.getOauthTokenSecret());
    }
}
