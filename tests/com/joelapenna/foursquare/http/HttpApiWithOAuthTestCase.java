/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.http;

import com.joelapenna.foursquare.TestCredentials;
import com.joelapenna.foursquare.http.HttpApiWithOAuth;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;

import android.test.suitebuilder.annotation.LargeTest;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class HttpApiWithOAuthTestCase extends TestCase {

    public HttpApiWithOAuthTestCase() {
        super();
    }

    @LargeTest
    public void test_createHttpPost_SignsRequest() throws IOException {
        HttpApiWithOAuth httpApi = new HttpApiWithOAuth(HttpApiWithOAuth.createHttpClient());
        httpApi.setOAuthConsumerCredentials(TestCredentials.oAuthConsumerKey,
                TestCredentials.oAuthConsumerSecret);

        HttpPost request = httpApi.createHttpPost("http://someurl");
        Header authHeader = request.getFirstHeader("Authorization");
        assertNotNull(authHeader);

        String oauthHeader = authHeader.getValue();
        assertTrue(oauthHeader.startsWith("OAuth "));
    }
}
