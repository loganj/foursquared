/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joelapenna.foursquared.appwidget.stats;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import com.joelapenna.foursquared.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FoursquareHelper {
	
	private static int REQUEST_TIMEOUT = 20;//seconds
	private static int MILLISECONDS = 1000;
	
    private static final String TAG = "FoursquareHelper";

    private static final String FOURSQUARE_PAGE =
    	"http://api.foursquare.com/v1/user.json?badges=1&mayor=1";

    /**
     * {@link org.apache.http.StatusLine} HTTP status code when no server error has occurred.
     */
    private static final int HTTP_STATUS_OK = 200;

    /**
     * Shared buffer used by {@link #getUrlContent(String)} when reading results
     * from an API request.
     */
    private static byte[] sBuffer = new byte[512];

    /**
     * User-agent string to use when making requests. Should be filled using
     * {@link #prepareUserAgent(android.content.Context)} before making any other calls.
     */
    private static String sUserAgent = null;

    /**
     * Thrown when there were problems contacting the remote API server, either
     * because of a network error, or the server returned a bad status code.
     */
    @SuppressWarnings("serial")
	public static class ApiException extends Exception {
        public ApiException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public ApiException(String detailMessage) {
            super(detailMessage);
        }
    }

    /**
     * Thrown when there were problems parsing the response to an API call,
     * either because the response was empty, or it was malformed.
     */
    @SuppressWarnings("serial")
    public static class ParseException extends Exception {
        public ParseException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }
    }

    /**
     * Prepare the internal User-Agent string for use. This requires a
     * {@link android.content.Context} to pull the package name and version number for this
     * application.
     */
    public static void prepareUserAgent(Context context) {
        try {
            // Read package name and version number from manifest
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            sUserAgent = String.format(context.getString(R.string.stats_widget_template_user_agent),
            		info.versionName,info.packageName);
        } catch(NameNotFoundException e) {
            Log.e(TAG, "Couldn't find package information in PackageManager", e);
        }
    }

    /**
     * @throws ApiException If any connection or server error occurs.
     * @throws ParseException If there are problems parsing the response.
     */
    public static UserStats getContent(String email,String pword)
            throws ApiException, ParseException {
        // Query the API for content
        String content = getUrlContent(FOURSQUARE_PAGE,email,pword);
        try
        {
        	//Get JSON objects
        	JSONObject obj = new JSONObject(content);
        	JSONObject userObj = obj.getJSONObject("user");
        	JSONObject checkinObj = userObj.getJSONObject("checkin");
        	JSONObject venueObj = checkinObj.getJSONObject("venue");
        	JSONArray badges = userObj.getJSONArray("badges");

        	//Get String content
        	String mayorCount = userObj.getString("mayorcount");
        	String badgeCount = String.valueOf(badges.length());
        	String venue = venueObj.getString("name");
        	String userID = userObj.getString(("id"));
        	String firstName = userObj.getString("firstname");
        	String lastInitial = userObj.getString("lastname").charAt(0) + ".";
        	String userName = firstName + " " +lastInitial;

        	return new UserStats(String.valueOf(mayorCount),badgeCount,venue,userID,userName);
        } catch (JSONException e) {
            throw new ParseException("Problem parsing API response", e);
        }
    }

    /**
     * Pull the raw text content of the given URL. This call blocks until the
     * operation has completed, and is synchronized because it uses a shared
     * buffer {@link #sBuffer}.
     *
     * @param url The exact URL to request.
     * @return The raw content returned by the server.
     * @throws ApiException If any connection or server error occurs.
     * @author Sections of this code contributed by jTribe (http://jtribe.com.au)
     */
    protected static synchronized String getUrlContent(
    		String sUrl,String email,String pword) throws ApiException {
        if (sUserAgent == null) {
            throw new ApiException("User-Agent string must be prepared");
        }
        
        String userPassword = email + ":" + pword;
		String encoding = Base64Coder.encodeString(userPassword);
		try {
			URL url = new URL(sUrl);
			
			System.setProperty("http.keepAlive", "false");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", "Basic " + encoding);
			connection.setReadTimeout(REQUEST_TIMEOUT * MILLISECONDS);
			connection.setConnectTimeout(REQUEST_TIMEOUT * MILLISECONDS);
			connection.setRequestProperty("User-Agent", sUserAgent);
			connection.setRequestMethod("GET");
			
			//Get response code
			int responseCode = connection.getResponseCode();

			if (responseCode != HTTP_STATUS_OK) {
                throw new ApiException("Invalid response from server: " +
                		connection.getResponseMessage());
            }

            // Pull content stream from response
			InputStream inputStream = connection.getInputStream();
            ByteArrayOutputStream content = new ByteArrayOutputStream();

            // Read response into a buffered stream
            int readBytes = 0;
            while ((readBytes = inputStream.read(sBuffer)) != -1) {
                content.write(sBuffer, 0, readBytes);
            }

            // Return result from buffered stream
            return new String(content.toByteArray());
        } catch (IOException e) {
            throw new ApiException("Problem communicating with API", e);
        }
    }
}
