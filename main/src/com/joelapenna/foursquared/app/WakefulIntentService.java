/**
 * Copyright 2010 Mark Wyszomierski
 * Portions Copyright (c) 2008-2010 CommonsWare, LLC
 */
package com.joelapenna.foursquared.app;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
 * This is based off the chapter 13 sample in Advanced Android Development, by Mark Murphy.
 * 
 * @date May 21, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public abstract class WakefulIntentService extends IntentService { 
    public static final String TAG = "WakefulIntentService";
    public static final String LOCK_NAME_STATIC = "com.joelapenna.foursquared.app.WakefulintentService.Static"; 
    
    private static PowerManager.WakeLock lockStatic = null; 
    
    abstract void doWakefulWork(Intent intent); 
    
    public static void acquireStaticLock(Context context) { 
        getLock(context).acquire(); 
    }
  
    private synchronized static PowerManager.WakeLock getLock(Context context) { 
        if (lockStatic == null) { 
            PowerManager mgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE); 
            lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
            lockStatic.setReferenceCounted(true); 
        }
    
        return(lockStatic); 
    } 
  
    public WakefulIntentService(String name) { 
        super(name); 
    } 
  
    @Override 
    final protected void onHandleIntent(Intent intent) { 
        doWakefulWork(intent); 
        getLock(this).release(); 
    } 
}
