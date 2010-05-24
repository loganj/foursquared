/**
 * Copyright 2010 Mark Wyszomierski
 */
package com.joelapenna.foursquared.app;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.Foursquared;
import com.joelapenna.foursquared.FriendsActivity;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.StringFormatters;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * @date May 21, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class NotificationsService extends WakefulIntentService { 
    public static final String TAG = "NotificationsService";
    private static final String SHARED_PREFS_NAME = "SharedPrefsNotificationsService";
    private static final String SHARED_PREFS_KEY_LAST_RUN_TIME = "SharedPrefsKeyLastRunTime";

    private SharedPreferences mSharedPrefs;
  
    
    public NotificationsService() { 
        super(TAG); 
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    @Override
    protected void doWakefulWork(Intent intent) {
        
        Log.e(TAG, "NotificationsService::doWakefulWork()...");
        
        // The user must have logged in once previously for this to work,
        // and not leave the app in a logged-out state.
        Foursquared foursquared = (Foursquared) getApplication();
        if (!foursquared.isReady()) {
            Log.e(TAG, "  User not logged in, cannot proceed.");
            return;
        }

        Foursquare foursquare = foursquared.getFoursquare();
        Location location = getLastGeolocation();
        Group<Checkin> checkins = null;
        if (location != null) {
            try {
                checkins = foursquare.checkins(
                    LocationUtils.createFoursquareLocation(location));
            } catch (Exception ex) {
                Log.e(TAG, "  Error getting checkins in notifications service.", ex);
            }
        } else {
            Log.e(TAG, "  Could not find location in notifications service, cannot proceed.");
        }
        
        if (checkins != null) {
            Log.e(TAG, "  Got " + checkins.size() + " checkins back, examing all now!");
            
            // Don't accept any checkins that are older than the last time we ran.
            long lastRunTime = mSharedPrefs.getLong(SHARED_PREFS_KEY_LAST_RUN_TIME, 0L);
            Date dateLast = new Date(lastRunTime);
            
            // Don't accept any checkins that are older than some reasonable threshold.
            // For example, if our interval is 2 hours, we may not really want to show
            // checkins that are 1.9 hours hold, it just doesn't really make sense.
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());        
            cal.add(Calendar.MINUTE, -50);
            Date dateRecent = cal.getTime();                  

            // Now build the list of 'new' checkins.
            List<Checkin> newCheckins = new ArrayList<Checkin>();
            for (Checkin it : checkins) {
                Log.d(TAG, "  examining new checkin: " + it.getDisplay());
                
                // We haven't shown this checkin within the lifetime of our service.
                // If this is the first run, also discard checkins that are older
                // than some threshold.
                try {
                    Date date = StringFormatters.DATE_FORMAT.parse(it.getCreated()); 

                    Log.d(TAG, "  " + dateLast.toLocaleString());
                    Log.d(TAG, "  " + dateRecent.toLocaleString());
                    Log.d(TAG, "  " + date.toLocaleString());
                    
                    if (date.after(dateLast)) {
                        Log.d(TAG, "  checkin is younger than last run time...");
                        if (date.after(dateRecent)) {
                            Log.d(TAG, "  checkin is younger than recent threshold...");
                            Log.d(TAG, "  checkin is 'new', adding for notification...");
                            newCheckins.add(it);
                        }
                    }
                } catch (ParseException ex) {
                }
            }

            notifyUser(newCheckins);
        }
        
        // Record this as the last time we ran.
        mSharedPrefs.edit().putLong(SHARED_PREFS_KEY_LAST_RUN_TIME, System.currentTimeMillis()).commit();
    }

    private Location getLastGeolocation() {
        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = manager.getAllProviders();
        
        Location bestLocation = null;
        for (String it : providers) {
            Location location = manager.getLastKnownLocation(it);
            if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = location;
            }
        }
        
        return bestLocation;
    }
    
    private void notifyUser(List<Checkin> newCheckins) {
        NotificationManager mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE); 
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, FriendsActivity.class), 0); 
        
        for (Checkin it : newCheckins) {
            Notification note = new Notification(R.drawable.icon, 
                                                 "Foursquare Checkin", 
                                                 System.currentTimeMillis()); 
            note.setLatestEventInfo(this, "Foursquare Checkin", it.getDisplay(), pi); 
            //note.number = 88; 
            mgr.notify(1337, note); 
        }
    }
    
    public static void setupNotifications(Context context) {
        // If the user has notifications on, set an alarm every N minutes, where N is their
        // requested refresh rate.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean(Preferences.PREFERENCE_NOTIFICATIONS, false)) {

            int refreshRateInMinutes = 30;
            try {
                refreshRateInMinutes = Integer.parseInt(prefs.getString(
                        Preferences.PREFERENCE_NOTIFICATIONS_INTERVAL, String.valueOf(refreshRateInMinutes)));
            } catch (NumberFormatException ex) {
                Log.e(TAG, "Error parsing notification interval time, defaulting to: " + refreshRateInMinutes);
            }

            Log.d(TAG, "User has notifications on, attempting to setup alarm with interval: " + refreshRateInMinutes + "..");
            
            AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE); 
            mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
                             SystemClock.elapsedRealtime() + (refreshRateInMinutes * 60 * 1000), 
                             refreshRateInMinutes * 60 * 1000, 
                             makePendingIntentAlarm(context)); 
        }
    }
    
    public static void cancelNotifications(Context context) {
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE); 
        mgr.cancel(makePendingIntentAlarm(context));
    }
    
    private static PendingIntent makePendingIntentAlarm(Context context) {
        return PendingIntent.getBroadcast(context, 0, new Intent(context, NotificationsOnAlarmReceiver.class), 0); 
    }
}
