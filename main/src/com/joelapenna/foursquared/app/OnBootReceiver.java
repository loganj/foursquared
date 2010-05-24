/**
 * Copyright 2010 Mark Wyszomierski
 */
package com.joelapenna.foursquared.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @date May 21, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class OnBootReceiver extends BroadcastReceiver { 
    public static final String TAG = "OnBootReceiver";
    
    @Override 
    public void onReceive(Context context, Intent intent) { 
        Log.e(TAG, "OnBootReceiver::onReceive()...");
        
        // If the user has notifications on, set an alarm every N minutes, where N is their
        // requested refresh rate.
        NotificationsService.setupNotifications(context);
    }
} 