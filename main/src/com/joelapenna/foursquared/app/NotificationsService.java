/**
 * Copyright 2010 Mark Wyszomierski
 */
package com.joelapenna.foursquared.app;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
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
import android.widget.RemoteViews;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * This service will run every N minutes (specified by the user in settings). An alarm 
 * handles running the service. 
 * 
 * When the service runs, we call /checkins. From the list of checkins, we cut down the
 * list of relevant checkins as follows:
 * <ul>
 *   <li>Not one of our own checkins.</li>
 *   <li>We haven't turned pings off for the user. This can be toggled on/off in the
 *       UserDetailsActivity activity, per user.</li>
 *   <li>The checkin is younger than the last time we ran this service.</li>
 *   <li>The checkin is younger than a static threshold in minutes (currently 20 minutes).</li>
 * </ul>
 * 
 * The last criteria (the 20 minute threshold) exists for the higher refresh intervals. If
 * the user is running notifications every 2 hours, they probably don't care about a checkin
 * that is 1.9 hours old.
 * 
 * Note that the server might override the pings attribute to 'off' for certain checkins, 
 * usually if the checkin is far away from our current location.
 * 
 * Notifications will not be cleared from the notification bar until a subsequent run can
 * generate at least one new notification. A new batch of notifications will clear all 
 * previous notifications so as to not clutter the notification bar.
 * 
 * @date May 21, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class NotificationsService extends WakefulIntentService { 
    public static final String TAG = "NotificationsService";
    private static final boolean DEBUG = true;
    private static final String SHARED_PREFS_NAME = "SharedPrefsNotificationsService";
    private static final String SHARED_PREFS_KEY_LAST_RUN_TIME = "SharedPrefsKeyLastRunTime";
    private static final int MAX_AGE_CHECKINS_IN_MINUTES = 20;

    private SharedPreferences mSharedPrefs;
  
    
    public NotificationsService() { 
        super("NotificationsService"); 
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    @Override
    protected void doWakefulWork(Intent intent) {
        
        // The user must have logged in once previously for this to work,
        // and not leave the app in a logged-out state.
        Foursquared foursquared = (Foursquared) getApplication();
        Foursquare foursquare = foursquared.getFoursquare();
        if (!foursquared.isReady()) {
            if (DEBUG) Log.d(TAG, "User not logged in, cannot proceed.");
            return;
        }
        
        // Before running, make sure the user still wants notifications on.
        // For example, the user could have turned notifications on from
        // this device, but then turned it off on a second device. This 
        // service would continue running then, continuing to notify the
        // user.
        if (!checkUserStillWantsNotifications(foursquared.getUserId(), foursquare)) {
            // Turn off locally.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean(Preferences.PREFERENCE_NOTIFICATIONS, false).commit();
            cancelNotifications(this);
            return;
        }

        // Get the users current location and then request nearby checkins.
        Group<Checkin> checkins = null;
        Location location = getLastGeolocation();
        if (location != null) {
            try {
                checkins = foursquare.checkins(
                    LocationUtils.createFoursquareLocation(location));
            } catch (Exception ex) {
                Log.e(TAG, "Error getting checkins in notifications service.", ex);
            }
        } else {
            Log.e(TAG, "Could not find location in notifications service, cannot proceed.");
        }
        
        if (checkins != null) {
            
            if (DEBUG) Log.e(TAG, "Checking " + checkins.size() + " checkins for notifications.");
            
            // Don't accept any checkins that are older than the last time we ran.
            long lastRunTime = mSharedPrefs.getLong(SHARED_PREFS_KEY_LAST_RUN_TIME, 0L);
            Date dateLast = new Date(lastRunTime);
            
            // Don't accept any checkins that are older than some reasonable threshold.
            // For example, if our interval is 2 hours, we may not really want to show
            // checkins that are 1.9 hours hold, it just doesn't really make sense. 
            // We'll have to refine this.
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());        
            cal.add(Calendar.MINUTE, -(MAX_AGE_CHECKINS_IN_MINUTES));
            Date dateRecent = cal.getTime();                  

            // Now build the list of 'new' checkins.
            List<Checkin> newCheckins = new ArrayList<Checkin>();
            for (Checkin it : checkins) {
                
                if (DEBUG) Log.d(TAG, "Checking checkin of " + it.getUser().getFirstname());
                
                // Ignore ourselves.
                if (it.getUser() != null && it.getUser().getId().equals(foursquared.getUserId())) {
                    if (DEBUG) Log.d(TAG, "  Ignoring checkin of ourselves.");
                    continue;
                }
                
                // Check that our user wanted to see notifications from this user.
                if (!it.getPing()) {
                    if (DEBUG) Log.d(TAG, "  Pings are off for this user.");
                    continue;
                }
                
                // Check against date times.
                try {
                    Date date = StringFormatters.DATE_FORMAT.parse(it.getCreated()); 

                    if (DEBUG) {
                        Log.d(TAG, "  Comaring date times for checkin.");
                        Log.d(TAG, "    Last run time: " + dateLast.toLocaleString());
                        Log.d(TAG, "    Recent time:   " + dateRecent.toLocaleString());
                        Log.d(TAG, "    Checkin time:  " + date.toLocaleString());
                    }
                    
                    if (date.after(dateLast)) {
                        if (DEBUG) Log.d(TAG, "  Checkin is younger than our last run time...");
                        if (date.after(dateRecent)) {
                            if (DEBUG) Log.d(TAG, "  Checkin is younger than 'recent' threshold, passes all tests!");
                            newCheckins.add(it);
                        } else {
                            if (DEBUG) Log.d(TAG, "  Checkin is older than 'recent' threshold.");
                        }
                    } else {
                        if (DEBUG) Log.d(TAG, "  Checkin is older than last run time.");
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
            if (location != null) {
                if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = location;
                }
            }
        }
        
        return bestLocation;
    }
    
    private void notifyUser(List<Checkin> newCheckins) {
        
        // If we have no new checkins to show, nothing to do. We would also be leaving the
        // previous batch of notifications alive (if any) which is ok.
        if (newCheckins.size() < 1) {
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean vibrate = prefs.getBoolean(Preferences.PREFERENCE_NOTIFICATIONS_VIBRATE, false);
        boolean vibratedOnce = false;
        
        // Clear all previous notifications before showing new ones.
        NotificationManager mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE); 
        mgr.cancelAll();
        
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, FriendsActivity.class), 0); 
        int nextNotificationId = 0;
        for (Checkin it : newCheckins) {
            RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_list_item);
            contentView.setTextViewText(R.id.text1, StringFormatters.getNotificationMessageTitle(it));
            contentView.setTextViewText(R.id.text2, StringFormatters.getNotificationMessageInfo(it));
            
            Notification notification = new Notification(
                    R.drawable.icon, 
                    "Foursquare Checkin", 
                    System.currentTimeMillis()); 
            notification.contentView = contentView;
            notification.contentIntent = pi;
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            if (vibrate && !vibratedOnce) {
                notification.defaults |= Notification.DEFAULT_VIBRATE;
                vibratedOnce = true;
            }
            mgr.notify(nextNotificationId++, notification);
        }
    }
    
    private boolean checkUserStillWantsNotifications(String userId, Foursquare foursquare) {
        try {
            User user = foursquare.user(userId, false, false, null);
            if (user != null) {
                return user.getSettings().getPings().equals("on");
            }
        } catch (Exception ex) {
            // Assume they still want it on.
        }
        
        return true;
    }
    
    public static void setupNotifications(Context context) {

        // If the user has notifications on, set an alarm every N minutes, where N is their
        // requested refresh rate. We default to 30 if some problem reading set interval.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean(Preferences.PREFERENCE_NOTIFICATIONS, false)) {

            int refreshRateInMinutes = 30;
            try {
                refreshRateInMinutes = Integer.parseInt(prefs.getString(
                        Preferences.PREFERENCE_NOTIFICATIONS_INTERVAL, String.valueOf(refreshRateInMinutes)));
            } catch (NumberFormatException ex) {
                Log.e(TAG, "Error parsing notification interval time, defaulting to: " + refreshRateInMinutes);
            }

            if (DEBUG) {
                Log.d(TAG, "User has notifications on, attempting to setup alarm with interval: " 
                        + refreshRateInMinutes + "..");
            }
            
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
    
    public static void generateNotificationTest(Context context) {
        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, FriendsActivity.class), 0); 
        
        NotificationManager mgr = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE); 
        
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_list_item);
        contentView.setTextViewText(R.id.text1, "Notification title line");
        contentView.setTextViewText(R.id.text2, "Notification message line");
        
        Notification notification = new Notification(
                R.drawable.icon, 
                "Foursquare Checkin", 
                System.currentTimeMillis()); 
        notification.contentView = contentView;
        notification.contentIntent = pi;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        mgr.notify(-1, notification);
    }
}
