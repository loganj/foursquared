/**
 * Copyright 2010 Mark Wyszomierski
 */
package com.joelapenna.foursquared.app; 

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @date May 21, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class NotificationsOnAlarmReceiver extends BroadcastReceiver { 
    
    @Override 
    public void onReceive(Context context, Intent intent) { 
        WakefulIntentService.acquireStaticLock(context); 
        context.startService(new Intent(context, NotificationsService.class)); 
    }
} 
