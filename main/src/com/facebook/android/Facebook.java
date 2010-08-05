/*
 * Copyright 2010 Mark Wyszomierski
 * Portions Copyright (c) 2008-2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.android;

import java.io.IOException;
import java.net.MalformedURLException;

import android.os.Bundle;
import android.text.TextUtils;

/**
 * Main Facebook object for storing session token and session expiration date 
 * in memory, as well as generating urls to access different facebook endpoints.
 * 
 * @author Steven Soneff (ssoneff@facebook.com):
 *    -original author.
 * @author Mark Wyszomierski (markww@gmail.com):
 *    -modified to remove network operation calls, and dialog creation, 
 *     focused on making this class only generate urls for external use
 *     and storage of a session token and expiration date.
 */
public class Facebook {

    /** Strings used in the OAuth flow */
    public static final String REDIRECT_URI = "fbconnect://success";
    public static final String CANCEL_URI = "fbconnect:cancel";
    public static final String TOKEN = "access_token";
    public static final String EXPIRES = "expires_in";
    
    /** Login action requires a few extra steps for setup and completion. */
    public static final String LOGIN = "login";

    /** Facebook server endpoints: may be modified in a subclass for testing */
    protected static String OAUTH_ENDPOINT = 
        "http://graph.facebook.com/oauth/authorize"; // https
    protected static String UI_SERVER = 
        "http://www.facebook.com/connect/uiserver.php"; // http
    protected static String GRAPH_BASE_URL = 
        "https://graph.facebook.com/";
    protected static String RESTSERVER_URL = 
        "https://api.facebook.com/restserver.php";

    private String mAccessToken = null;
    private long mAccessExpires = 0;
    
    
    public Facebook() {
    }
    
    /**
     * Invalidates the current access token in memory, and generates a 
     * prepared URL that can be used to log the user out.
     * 
     * @throws MalformedURLException 
     * @return PreparedUrl instance reflecting the full url of the service.
     */
    public PreparedUrl logout() 
        throws MalformedURLException, IOException 
    {
        setAccessToken(null);
        setAccessExpires(0);
        Bundle b = new Bundle();
        b.putString("method", "auth.expireSession");
        return requestUrl(b);
    }

    /**
     * Build a url to Facebook's old (pre-graph) API with the given 
     * parameters. One of the parameter keys must be "method" and its value 
     * should be a valid REST server API method.  
     * 
     * See http://developers.facebook.com/docs/reference/rest/
     *  
     * Example: 
     * <code>
     *  Bundle parameters = new Bundle();
     *  parameters.putString("method", "auth.expireSession");
     *  PreparedUrl preparedUrl = requestUrl(parameters);
     * </code>
     * 
     * @param parameters
     *            Key-value pairs of parameters to the request. Refer to the
     *            documentation: one of the parameters must be "method".
     * @throws MalformedURLException 
     *            if accessing an invalid endpoint
     * @throws IllegalArgumentException
     *            if one of the parameters is not "method"
     * @return PreparedUrl instance reflecting the full url of the service.
     */
    public PreparedUrl requestUrl(Bundle parameters) 
          throws MalformedURLException {
        if (!parameters.containsKey("method")) {
            throw new IllegalArgumentException("API method must be specified. "
                    + "(parameters must contain key \"method\" and value). See"
                    + " http://developers.facebook.com/docs/reference/rest/");
        }
        return requestUrl(null, parameters, "GET");
    }
    
    /**
     * Build a url to the Facebook Graph API without any parameters.
     * 
     * See http://developers.facebook.com/docs/api
     *  
     * @param graphPath
     *            Path to resource in the Facebook graph, e.g., to fetch data
     *            about the currently logged authenticated user, provide "me",
     *            which will fetch http://graph.facebook.com/me
     * @throws MalformedURLException
     * @return PreparedUrl instance reflecting the full url of the service.
     */
    public PreparedUrl requestUrl(String graphPath) 
          throws MalformedURLException {
        return requestUrl(graphPath, new Bundle(), "GET");
    }
    
    /**
     * Build a url to the Facebook Graph API with the given string 
     * parameters using an HTTP GET (default method).
     * 
     * See http://developers.facebook.com/docs/api
     *  
     * @param graphPath
     *            Path to resource in the Facebook graph, e.g., to fetch data
     *            about the currently logged authenticated user, provide "me",
     *            which will fetch http://graph.facebook.com/me
     * @param parameters
     *            key-value string parameters, e.g. the path "search" with
     *            parameters "q" : "facebook" would produce a query for the
     *            following graph resource:
     *            https://graph.facebook.com/search?q=facebook
     * @throws MalformedURLException 
     * @return PreparedUrl instance reflecting the full url of the service.
     */
    public PreparedUrl requestUrl(String graphPath, Bundle parameters) 
          throws MalformedURLException {
        return requestUrl(graphPath, parameters, "GET");
    }
    
    /**
     * Build a PreparedUrl object which can be used with Util.openUrl().
     * You can also use the returned PreparedUrl.getUrl() to run the
     * network operation yourself.
     * 
     * Note that binary data parameters 
     * (e.g. pictures) are not yet supported by this helper function.
     * 
     * See http://developers.facebook.com/docs/api
     *  
     * @param graphPath
     *            Path to resource in the Facebook graph, e.g., to fetch data
     *            about the currently logged authenticated user, provide "me",
     *            which will fetch http://graph.facebook.com/me
     * @param parameters
     *            key-value string parameters, e.g. the path "search" with
     *            parameters {"q" : "facebook"} would produce a query for the
     *            following graph resource:
     *            https://graph.facebook.com/search?q=facebook
     * @param httpMethod
     *            http verb, e.g. "GET", "POST", "DELETE"
     * @throws MalformedURLException 
     * @return PreparedUrl instance reflecting the full url of the service.
     */
    public PreparedUrl requestUrl(String graphPath,
                                  Bundle parameters, 
                                  String httpMethod) 
          throws MalformedURLException 
    {
        parameters.putString("format", "json");
        if (isSessionValid()) {
            parameters.putString(TOKEN, getAccessToken());
        }
        String url = graphPath != null ?
            GRAPH_BASE_URL + graphPath : 
            RESTSERVER_URL;

        return new PreparedUrl(url, parameters, httpMethod);
    }
    
    public boolean isSessionValid() {
        return (getAccessToken() != null) && ((getAccessExpires() == 0) || 
            (System.currentTimeMillis() < getAccessExpires()));
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public long getAccessExpires() {
        return mAccessExpires;
    }

    public void setAccessToken(String token) {
        mAccessToken = token;
    }
    
    public void setAccessExpires(long time) {
        mAccessExpires = time;
    }

    public void setAccessExpiresIn(String expiresIn) {
        if (expiresIn != null) {
            setAccessExpires(System.currentTimeMillis()
                    + Integer.parseInt(expiresIn) * 1000);
        }
    }
    
    public String generateUrl(String action, String appId, String[] permissions) {
        Bundle params = new Bundle();
        String endpoint;
        if (action.equals(LOGIN)) {
            params.putString("client_id", appId);
            if (permissions != null && permissions.length > 0) {
                params.putString("scope", TextUtils.join(",", permissions));
            }       
            endpoint = OAUTH_ENDPOINT;
            params.putString("type", "user_agent");
            params.putString("redirect_uri", REDIRECT_URI);
        } else {
            endpoint = UI_SERVER;
            params.putString("method", action);
            params.putString("next", REDIRECT_URI);
        }
        params.putString("display", "touch");
        if (isSessionValid()) {
            params.putString(TOKEN, getAccessToken());
        }
        
        String url = endpoint + "?" + FacebookUtil.encodeUrl(params);
        return url;
    }
    
    /**
     * Stores a prepared url and parameters bundle from one of the <code>requestUrl</code>
     * methods. It can then be used with Util.openUrl() to run the network operation.
     * The Util.openUrl() requires the original params bundle and http method, so this
     * is just a convenience wrapper around it.
     * 
     * @author Mark Wyszomierski (markww@gmail.com)
     */
    public static class PreparedUrl
    {
        private String mUrl;
        private Bundle mParameters;
        private String mHttpMethod;
        
        public PreparedUrl(String url, Bundle parameters, String httpMethod) {
            mUrl = url;
            mParameters = parameters;
            mHttpMethod = httpMethod;
        }
        
        public String getUrl() {
            return mUrl;
        }
        
        public Bundle getParameters() {
            return mParameters;
        }
        
        public String getHttpMethod() {
            return mHttpMethod;
        }
    }
}