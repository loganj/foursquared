/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.http;

import com.joelapenna.foursquare.TestCredentials;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

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
    public void test_oAuthSigning() throws ClientProtocolException, IOException,
            OAuthMessageSignerException, OAuthExpectationFailedException {
        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(TestCredentials.oAuthConsumerKey,
                TestCredentials.oAuthConsumerSecret, SignatureMethod.HMAC_SHA1);
        consumer.setTokenWithSecret(TestCredentials.oAuthToken, TestCredentials.oAuthTokenSecret);

        HttpGet httpGet = new HttpGet(
                "http://api.playfoursquare.com/v1/tips?geolat=37.770900&geolong=-122.436987&l=1");
        consumer.sign(httpGet);
        HttpClient httpClient = HttpApi.createHttpClient();
        HttpResponse response = httpClient.execute(httpGet);
        String responseString = EntityUtils.toString(response.getEntity());
        assertTrue(responseString.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?><tips>"));
    }
}
