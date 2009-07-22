/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.http;

import com.joelapenna.foursquare.Foursquare;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.impl.DefaultOAuthConsumer;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class HttpApiWithOAuth extends HttpApi {
    protected static final String TAG = "HttpApiWithOAuth";
    protected static final boolean DEBUG = Foursquare.DEBUG;

    private OAuthConsumer mConsumer;

    public HttpApiWithOAuth(DefaultHttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public HttpPost createHttpPost(String url, NameValuePair... nameValuePairs) {
        HttpPost httpPost = super.createHttpPost(url, nameValuePairs);
        try {
            mConsumer.sign(httpPost);
        } catch (OAuthMessageSignerException e) {
            if (DEBUG) Log.d(TAG, "OAuthMessageSignerException", e);
            throw new RuntimeException(e);
        }
        return httpPost;
    }

    public void setOAuthConsumerCredentials(String key, String secret) {
        mConsumer = new DefaultOAuthConsumer(key, secret, SignatureMethod.HMAC_SHA1);
    }

    public void setOAuthTokenWithSecret(String token, String secret) {
        mConsumer.setTokenWithSecret(token, secret);
    }

    public boolean hasOAuthTokenWithSecret() {
        return (mConsumer.getToken() != null) && (mConsumer.getTokenSecret() != null);
    }
}
