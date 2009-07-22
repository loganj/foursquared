/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.http;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareCredentialsError;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.parsers.AbstractParser;
import com.joelapenna.foursquare.parsers.Parser;
import com.joelapenna.foursquare.types.FoursquareType;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class HttpApi {
    protected static final String TAG = "HttpApi";
    protected static final boolean DEBUG = Foursquare.DEBUG;

    private static final String CLIENT_VERSION = "iPhone 20090301";
    private static final String CLIENT_VERSION_HEADER = "X_foursquare_client_version";
    private static final int TIMEOUT = 10;

    DefaultHttpClient mHttpClient;

    public HttpApi(DefaultHttpClient httpClient) {
        mHttpClient = httpClient;
    }

<<<<<<< HEAD:src/com/joelapenna/foursquare/http/HttpApi.java
<<<<<<< HEAD:src/com/joelapenna/foursquare/http/HttpApi.java
<<<<<<< HEAD:src/com/joelapenna/foursquare/http/HttpApi.java
<<<<<<< HEAD:src/com/joelapenna/foursquare/http/HttpApi.java
<<<<<<< HEAD:src/com/joelapenna/foursquare/http/HttpApi.java
    public FoursquareType doHttpPost(String url,
            Parser<? extends FoursquareType> abstractParser, NameValuePair... nameValuePairs)
            throws FoursquareError, FoursquareParseException, IOException {
=======
    public FoursquareType doHttpPost(String url, Parser<? extends FoursquareType> parser,
            NameValuePair... nameValuePairs) throws FoursquareException, IOException {
>>>>>>> 0b7d5f0... Change around Exception/Error raising, add unittests:src/com/joelapenna/foursquare/http/HttpApi.java
        if (DEBUG) Log.d(TAG, "doHttpPost: " + url);
        HttpPost httpPost = createHttpPost(url, nameValuePairs);

=======
    public FoursquareType doHttpPost(HttpPost httpPost, Parser<? extends FoursquareType> parser)
            throws FoursquareException, IOException {
        if (DEBUG) Log.d(TAG, "doHttpPost: " + httpPost.getURI());
>>>>>>> c72392a... Refactor doHttpPost to support soon to land HttpGet support. Add what:src/com/joelapenna/foursquare/http/HttpApi.java
        HttpResponse response = executeHttpPost(httpPost);
=======
    public FoursquareType doHttpRequest(HttpRequestBase httpRequest, Parser<? extends FoursquareType> parser)
            throws FoursquareException, IOException {
=======
    public FoursquareType doHttpRequest(HttpRequestBase httpRequest,
            Parser<? extends FoursquareType> parser) throws FoursquareException, IOException {
>>>>>>> 9041be0... Some random http client optimizations that may or may not help.:src/com/joelapenna/foursquare/http/HttpApi.java
=======
    public FoursquareType doHttpRequest(HttpRequestBase httpRequest,
            Parser<? extends FoursquareType> parser) throws FoursquareException, IOException {
>>>>>>> ce6538a... Improve content consumption (so we don't leak threads) in HttpApis.:src/com/joelapenna/foursquare/http/HttpApi.java
        if (DEBUG) Log.d(TAG, "doHttpRequest: " + httpRequest.getURI());
        HttpResponse response = executeHttpRequest(httpRequest);
>>>>>>> 69171d9... tips() works after realizing that authexchange causes tokens to expire!:src/com/joelapenna/foursquare/http/HttpApi.java
        if (response == null) {
            if (DEBUG) Log.d(TAG, "execute() call for the httpRequest generated an exception;");
            return null;
        }

        switch (response.getStatusLine().getStatusCode()) {
            case 200:
                return parser.parse(AbstractParser.createXmlPullParser( //
                        response.getEntity().getContent()));
            case 401:
<<<<<<< HEAD:src/com/joelapenna/foursquare/http/HttpApi.java
                bestEffortConsumeContent(response);
                throw new FoursquareCredentialsError(response.getStatusLine().toString());
            default:
                if (DEBUG) Log.d(TAG, "Default case for status code reached: "
                        + response.getStatusLine().toString());
                bestEffortConsumeContent(response);
                return null;
        }

<<<<<<< HEAD:src/com/joelapenna/foursquare/http/HttpApi.java
        return abstractParser.parse(AuthParser.createParser(response.getEntity().getContent()));
=======
        return parser.parse(AbstractParser.createXmlPullParser(response.getEntity().getContent()));
>>>>>>> 0b7d5f0... Change around Exception/Error raising, add unittests:src/com/joelapenna/foursquare/http/HttpApi.java
=======
                response.getEntity().consumeContent();
                throw new FoursquareCredentialsError(response.getStatusLine().toString());
            default:
                if (DEBUG) {
                    Log.d(TAG, "Default case for status code reached: "
                            + response.getStatusLine().toString());
                }
                response.getEntity().consumeContent();
                return null;
        }
>>>>>>> ce6538a... Improve content consumption (so we don't leak threads) in HttpApis.:src/com/joelapenna/foursquare/http/HttpApi.java
    }

    public String doHttpPost(String url, NameValuePair... nameValuePairs) throws FoursquareError,
            FoursquareParseException, IOException, FoursquareCredentialsError {
        if (DEBUG) Log.d(TAG, "doHttpPost: " + url);
        HttpPost httpPost = createHttpPost(url, nameValuePairs);

        HttpResponse response = executeHttpRequest(httpPost);
        if (response == null) {
            if (DEBUG) Log.d(TAG, "execute() call for the httpPost generated an exception;");
            throw new FoursquareError("breakdown request unsuccessful.");
        }

        switch (response.getStatusLine().getStatusCode()) {
            case 200:
                try {
                    return EntityUtils.toString(response.getEntity());
                } catch (ParseException e) {
                    throw new FoursquareParseException(e.getMessage());
                }
            case 401:
<<<<<<< HEAD:src/com/joelapenna/foursquare/http/HttpApi.java
                bestEffortConsumeContent(response);
                throw new FoursquareCredentialsError(response.getStatusLine().toString());
            default:
                bestEffortConsumeContent(response);
=======
                response.getEntity().consumeContent();
                throw new FoursquareCredentialsError(response.getStatusLine().toString());
            default:
                response.getEntity().consumeContent();
>>>>>>> ce6538a... Improve content consumption (so we don't leak threads) in HttpApis.:src/com/joelapenna/foursquare/http/HttpApi.java
                throw new FoursquareError(response.getStatusLine().toString());
        }
    }

    /**
     * execute() an httpRequest catching exceptions and returning null instead.
     * 
     * @param httpRequest
     * @return
     */
    public HttpResponse executeHttpRequest(HttpRequestBase httpRequest) {
        if (DEBUG) Log.d(TAG, "executing HttpRequest for: " + httpRequest.getURI().toString());
        HttpResponse response;
        try {
            response = mHttpClient.execute(httpRequest);
        } catch (ClientProtocolException e) {
            Log.d(TAG, "ClientProtocolException for " + httpRequest, e);
            return null;
        } catch (IOException e) {
            Log.d(TAG, "IOException for " + httpRequest, e);
            return null;
        }
        return response;
    }

    public HttpGet createHttpGet(String url, NameValuePair... nameValuePairs) {
        if (DEBUG) Log.d(TAG, "creating HttpGet for: " + url);
        String query = URLEncodedUtils.format(Arrays.asList(nameValuePairs), HTTP.UTF_8);
        HttpGet httpGet = new HttpGet(url + "?" + query);
        if (DEBUG) Log.d(TAG, "Created: " + httpGet.getURI());
        return httpGet;
    }

    public HttpPost createHttpPost(String url, NameValuePair... nameValuePairs) {
        if (DEBUG) Log.d(TAG, "creating HttpPost for: " + url);
        List<NameValuePair> params = Arrays.asList(nameValuePairs);
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(CLIENT_VERSION_HEADER, CLIENT_VERSION);
        try {
            for (int i = 0; i < params.size(); i++) {
                if (DEBUG) Log.d(TAG, "Param: " + params.get(i));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e1) {
            throw new IllegalArgumentException("Unable to encode http parameters.");
        }
        if (DEBUG) Log.d(TAG, "Created: " + httpPost);
        return httpPost;
    }

    /**
     * Create a thread-safe client. This client does not do redirecting, to allow us to capture
     * correct "error" codes.
     * 
     * @return HttpClient
     */
    public static final DefaultHttpClient createHttpClient() {

        // Shamelessly cribbed from AndroidHttpClient
        HttpParams params = new BasicHttpParams();

        // Turn off stale checking. Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.
        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        // Default connection and socket timeout of 10 seconds. Tweak to taste.
        HttpConnectionParams.setConnectionTimeout(params, 10 * 1000);
        HttpConnectionParams.setSoTimeout(params, 10 * 1000);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        // Sets up the http part of the service.
        final SchemeRegistry supportedSchemes = new SchemeRegistry();

        // Register the "http" protocol scheme, it is required
        // by the default operator to look up socket factories.
        final SocketFactory sf = PlainSocketFactory.getSocketFactory();
        supportedSchemes.register(new Scheme("http", sf, 80));

        final ClientConnectionManager ccm = new ThreadSafeClientConnManager(params,
                supportedSchemes);
        return new DefaultHttpClient(ccm, params);
    }

<<<<<<< HEAD:src/com/joelapenna/foursquare/http/HttpApi.java
    public static void bestEffortConsumeContent(HttpResponse response) {
        try {
            response.getEntity().consumeContent();
        } catch (IOException e) {
            // This is a-okay.
        } catch (NullPointerException e) {
            // This is a-okay too!
        }
=======
    /**
     * Create the default HTTP protocol parameters.
     */
    private static final HttpParams createHttpParams() {
        final HttpParams params = new BasicHttpParams();

        // Turn off stale checking. Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.
        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT * 1000);
        HttpConnectionParams.setSoTimeout(params, TIMEOUT * 1000);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        return params;
>>>>>>> ce6538a... Improve content consumption (so we don't leak threads) in HttpApis.:src/com/joelapenna/foursquare/http/HttpApi.java
    }
}
