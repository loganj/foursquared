/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare;

import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.parsers.AuthParser;
import com.joelapenna.foursquare.parsers.Parser;
import com.joelapenna.foursquare.types.FoursquareType;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 *
 */
public class HttpApi {

    protected static final String TAG = "FoursquareHttpApi";
    protected static final boolean DEBUG = Foursquare.DEBUG;
    private static final String CLIENT_VERSION = "iPhone 20090301";
    private static final String CLIENT_VERSION_HEADER = "X_foursquare_client_version";
    protected DefaultHttpClient mHttpClient;

    /**
     * 
     */
    public HttpApi() {
        super();
    }

    protected FoursquareType doHttpPost(String url, Parser<? extends FoursquareType> abstractParser, NameValuePair... nameValuePairs) throws FoursquareError,
            FoursquareParseException, IOException {
                if (DEBUG) Log.d(TAG, "doHttpPost: " + url);
                HttpPost httpPost = createHttpPost(url, Arrays.asList(nameValuePairs));
            
                HttpResponse response = executeHttpPost(httpPost);
                if (response == null) {
                    if (DEBUG) Log.d(TAG, "execute() call for the httpPost generated an exception;");
                    return null;
                }
            
                switch (response.getStatusLine().getStatusCode()) {
                    case 200:
                        break;
                    default:
                        if (DEBUG) Log.d(TAG, "Default case for status code reached: "
                                + response.getStatusLine().toString());
                        return null;
                }
            
                return abstractParser.parse(AuthParser.createParser(response.getEntity().getContent()));
            }

    protected String doHttpPost(String url, NameValuePair... nameValuePairs) throws FoursquareError,
            FoursquareParseException, IOException {
                if (DEBUG) Log.d(TAG, "doHttpPost: " + url);
                HttpPost httpPost = createHttpPost(url, Arrays.asList(nameValuePairs));
            
                HttpResponse response = executeHttpPost(httpPost);
                if (response == null) {
                    if (DEBUG) Log.d(TAG, "execute() call for the httpPost generated an exception;");
                    throw new FoursquareError("breakdown request unsuccessful.");
                }
            
                switch (response.getStatusLine().getStatusCode()) {
                    case 200:
                        break;
                    default:
                        throw new FoursquareError(response.getStatusLine().toString());
                }
            
                try {
                    return EntityUtils.toString(response.getEntity());
                } catch (ParseException e) {
                    throw new FoursquareParseException(e.getMessage());
                }
            }

    /**
     * execute() an httpPost catching exceptions and returning null instead.
     *
     * @param httpPost
     * @return
     */
    protected HttpResponse executeHttpPost(HttpPost httpPost) {
        if (DEBUG) Log.d(TAG, "executing HttpPost for: " + httpPost.getURI().toString());
        HttpResponse response;
        try {
            response = mHttpClient.execute(httpPost);
        } catch (ClientProtocolException e) {
            Log.d(TAG, "ClientProtocolException for " + httpPost, e);
            return null;
        } catch (IOException e) {
            Log.d(TAG, "IOException for " + httpPost, e);
            return null;
        }
        return response;
    }

    /**
     * Create a thread-safe client. This client does not do redirecting, to allow us to capture
     * correct "error" codes.
     *
     * @return HttpClient
     */
    public static final DefaultHttpClient createHttpClient() {
        // Sets up the http part of the service.
        final SchemeRegistry supportedSchemes = new SchemeRegistry();
    
        // Register the "http" protocol scheme, it is required
        // by the default operator to look up socket factories.
        final SocketFactory sf = PlainSocketFactory.getSocketFactory();
        supportedSchemes.register(new Scheme("http", sf, 80));
    
        // Set some client http client parameter defaults.
        final HttpParams httpParams = createHttpParams();
        HttpClientParams.setRedirecting(httpParams, false);
    
        final ClientConnectionManager ccm = new ThreadSafeClientConnManager(httpParams,
                supportedSchemes);
        return new DefaultHttpClient(ccm, httpParams);
    }

    /**
     * Create the default HTTP protocol parameters.
     */
    private static final HttpParams createHttpParams() {
        // prepare parameters
        final HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setUseExpectContinue(params, true);
        return params;
    }

    protected static final HttpPost createHttpPost(String url, List<NameValuePair> params) {
        if (DEBUG) Log.d(TAG, "creating HttpPost for: " + url);
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(CLIENT_VERSION_HEADER, CLIENT_VERSION);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e1) {
            throw new IllegalArgumentException("Unable to encode http parameters.");
        }
        return httpPost;
    }

}
