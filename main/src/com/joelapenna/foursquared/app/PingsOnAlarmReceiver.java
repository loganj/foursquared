/**
 * Copyright 2010 Mark Wyszomierski
 * Portions Copyright (c) 2008-2010 CommonsWare, LLC
 */
package com.joelapenna.foursquared.app; 

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This is based off the chapter 13 sample in Advanced Android Development, by Mark Murphy.
 * 
 * @date May 21, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class PingsOnAlarmReceiver extends BroadcastReceiver { 
    
    @Override 
    public void onReceive(Context context, Intent intent) { 
        WakefulIntentService.acquireStaticLock(context); 
        context.startService(new Intent(context, PingsService.class)); 
    }
} 
