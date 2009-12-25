/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import com.joelapenna.foursquare.error.FoursquareCredentialsException;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquared.FoursquaredSettings;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.SocketException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class NotificationsUtil {
    private static final String TAG = "NotificationsUtil";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static void ToastReasonForFailure(Context context, Exception e) {
        if (DEBUG) Log.d(TAG, "Toasting for exception: ", e);

        if (e instanceof SocketException) {
            Toast.makeText(context, "Foursquare server not responding", Toast.LENGTH_SHORT).show();

        } else if (e instanceof IOException) {
            Toast.makeText(context, "Network unavailable", Toast.LENGTH_SHORT).show();

        } else if (e instanceof FoursquareCredentialsException) {
            Toast.makeText(context, "Authorization failed.", Toast.LENGTH_SHORT).show();

        } else if (e instanceof FoursquareException /* FoursquareError is one of these */) {
            String message;
            int toastLength = Toast.LENGTH_SHORT;
            if (e.getMessage() == null) {
                message = "Invalid Request";
            } else {
                message = e.getMessage();
                toastLength = Toast.LENGTH_LONG;
            }
            Toast.makeText(context, message, toastLength).show();

        } else {
            Toast.makeText(context, "A surprising new problem has occured. Try again!",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
