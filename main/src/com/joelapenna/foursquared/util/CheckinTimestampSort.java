/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.util;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/**
 * Initializes a few Date objects to act as boundaries for sorting checkin lists
 * by the following time categories:
 * 
 * <ul>
 *   <li>Within the last three hours.</li>
 *   <li>Today</li>
 *   <li>Yesterday</li>
 * </ul>
 * 
 * Create an instance of this class, then call one of the three getBoundary() methods
 * and compare against a Date object to see if it falls before or after.
 * 
 * @date March 22, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class CheckinTimestampSort {
    
    private static final String TAG = "CheckinTimestampSort";
    private static final boolean DEBUG = false;
    
    private static final int IDX_RECENT    = 0;
    private static final int IDX_TODAY     = 1;
    private static final int IDX_YESTERDAY = 2;
    
    private Date[] mBoundaries;
    
    
    public CheckinTimestampSort() {
        mBoundaries = getDateObjects();
    }
    
    public Date getBoundaryRecent() {
        return mBoundaries[IDX_RECENT];
    }

    public Date getBoundaryToday() {
        return mBoundaries[IDX_TODAY];
    }
    
    public Date getBoundaryYesterday() {
        return mBoundaries[IDX_YESTERDAY];
    }
    
    private static Date[] getDateObjects() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        if (DEBUG) Log.d(TAG, "Now: " + cal.getTime().toGMTString());            
        
        // Three hours ago or newer.
        cal.add(Calendar.HOUR, -3);
        Date dateRecent = cal.getTime();
        if (DEBUG) Log.d(TAG, "Recent: " + cal.getTime().toGMTString());                        

        // Today.
        cal.clear(Calendar.HOUR_OF_DAY);
        cal.clear(Calendar.HOUR);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        Date dateToday = cal.getTime();
        if (DEBUG) Log.d(TAG, "Today: " + cal.getTime().toGMTString());

        // Yesterday.
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date dateYesterday = cal.getTime();
        if (DEBUG) Log.d(TAG, "Yesterday: " + cal.getTime().toGMTString());  

        return new Date[] { dateRecent, dateToday, dateYesterday };
    }
}
