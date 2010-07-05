/**
 * Copyright 2010 Mark Wyszomierski
 */
package com.joelapenna.foursquared.app;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.ContactsSyncAdapter;
import com.joelapenna.foursquared.Foursquared;
import com.joelapenna.foursquared.FriendsActivity;
import com.joelapenna.foursquared.MainActivity;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.Sync;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.StringFormatters;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.ParseException;
import java.util.ArrayList;
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
 * </ul>
 * 
 * Note that the server might override the pings attribute to 'off' for certain checkins, 
 * usually if the checkin is far away from our current location.
 * 
 * Pings will not be cleared from the notification bar until a subsequent run can
 * generate at least one new ping. A new batch of pings will clear all 
 * previous pings so as to not clutter the notification bar.
 * 
 * @date May 21, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class PingsService extends WakefulIntentService { 
    public static final String TAG = "PingsService";
    private static final boolean DEBUG = false;
    public static final int NOTIFICATION_ID_CHECKINS = 15;

    
    public PingsService() { 
        super("PingsService"); 
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
    }
    
    @Override
    protected void doWakefulWork(Intent intent) {
        
        Log.i(TAG, "Foursquare pings service running...");
        
        // The user must have logged in once previously for this to work,
        // and not leave the app in a logged-out state.
        Foursquared foursquared = (Foursquared) getApplication();
        Foursquare foursquare = foursquared.getFoursquare();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!foursquared.isReady()) {
            Log.i(TAG, "User not logged in, cannot proceed.");
            return;
        }

        // Before running, make sure the user still wants pings on.
        // For example, the user could have turned pings on from
        // this device, but then turned it off on a second device. This 
        // service would continue running then, continuing to notify the
        // user.
        if (!checkUserStillWantsPings(foursquared.getUserId(), foursquare)) {
            // Turn off locally.
            Log.i(TAG, "Pings have been turned off for user, cancelling service.");
            prefs.edit().putBoolean(Preferences.PREFERENCE_PINGS, false).commit();
            cancelPings(this);
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
                Log.e(TAG, "Error getting checkins in pings service.", ex);
            }
        } else {
            Log.e(TAG, "Could not find location in pings service, cannot proceed.");
        }
        
        if (checkins != null) {
            
            Log.i(TAG, "Checking " + checkins.size() + " checkins for pings.");
            
            // Don't accept any checkins that are older than the last time we ran.
            long lastRunTime = prefs.getLong(
                    Preferences.PREFERENCE_PINGS_SERVICE_LAST_RUN_TIME, System.currentTimeMillis());
            Date dateLast = new Date(lastRunTime);
            
            Log.i(TAG, "Last service run time: " + dateLast.toLocaleString() + " (" + lastRunTime + ").");
              
            // Now build the list of 'new' checkins.
            List<Checkin> newCheckins = new ArrayList<Checkin>();
            for (Checkin it : checkins) {
                 
                if (DEBUG) Log.d(TAG, "Checking checkin of " + it.getUser().getFirstname());
                 
                // Ignore ourselves. The server should handle this by setting the pings flag off but..
                if (it.getUser() != null && it.getUser().getId().equals(foursquared.getUserId())) {
                    if (DEBUG) Log.d(TAG, "  Ignoring checkin of ourselves.");
                    continue;
                }
                
                // Check that our user wanted to see pings from this user.
                if (!it.getPing()) {
                    if (DEBUG) Log.d(TAG, "  Pings are off for this user.");
                    continue;
                }
                
                // Check against date times.
                try {
                    Date dateCheckin = StringFormatters.DATE_FORMAT.parse(it.getCreated()); 

                    if (DEBUG) {
                        Log.d(TAG, "  Comaring date times for checkin.");
                        Log.d(TAG, "    Last run time: " + dateLast.toLocaleString());
                        Log.d(TAG, "    Checkin time:  " + dateCheckin.toLocaleString());
                    }
                    
                    if (dateCheckin.after(dateLast)) {
                        if (DEBUG) Log.d(TAG, "  Checkin is younger than our last run time, passes all tests!!");
                        newCheckins.add(it);
                    } else {
                        if (DEBUG) Log.d(TAG, "  Checkin is older than last run time.");
                    }
                } catch (ParseException ex) {
                    if (DEBUG) Log.e(TAG, "  Error parsing checkin timestamp: " + it.getCreated(), ex);
                }
            }

            Log.i(TAG, "Found " + newCheckins.size() + " new checkins.");
            
            if ( newCheckins.size() > 0 && 
                 PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Preferences.PREFERENCE_SYNC_CONTACTS, false)) {
                ContentResolver resolver = getApplication().getContentResolver();
                ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>(newCheckins.size());
                for ( Checkin checkin : newCheckins) {
                    ops.addAll(foursquared.getSync().updateStatus(resolver, checkin.getUser(), checkin));
                }
                try {
                    resolver.applyBatch(ContactsContract.AUTHORITY, ops);
                } catch (Exception e) {
                    Log.w(TAG, "updating contact statuses failed", e);
                }
            }
            
            notifyUser(newCheckins);
        }
        
        // Record this as the last time we ran.
        prefs.edit().putLong(
                Preferences.PREFERENCE_PINGS_SERVICE_LAST_RUN_TIME, System.currentTimeMillis()).commit();
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
        // previous batch of pings alive (if any) which is ok.
        if (newCheckins.size() < 1) {
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Clear all previous pings notifications before showing new ones.
        NotificationManager mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE); 
        mgr.cancelAll();

        // We'll only ever show a single entry, so we collapse data depending on how many
        // new checkins we received on this refresh.
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.pings_list_item);
        if (newCheckins.size() == 1) {
            // A single checkin, show full checkin preview.
            Checkin checkin = newCheckins.get(0);
            String checkinMsgLine1 = StringFormatters.getCheckinMessageLine1(checkin, true);
            String checkinMsgLine2 = StringFormatters.getCheckinMessageLine2(checkin);
            String checkinMsgLine3 = StringFormatters.getCheckinMessageLine3(checkin);
            
            contentView.setTextViewText(R.id.text1, checkinMsgLine1);
            if (!TextUtils.isEmpty(checkinMsgLine2)) {
                contentView.setTextViewText(R.id.text2, checkinMsgLine2);
                contentView.setTextViewText(R.id.text3, checkinMsgLine3);
            } else {
                contentView.setTextViewText(R.id.text2, checkinMsgLine3);
            } 
        } else {
            // More than one new checkin, collapse them.
            String checkinMsgLine1 = newCheckins.size() + " new Foursquare checkins!";
            StringBuilder sbCheckinMsgLine2 = new StringBuilder(1024);
            for (Checkin it : newCheckins) {
                sbCheckinMsgLine2.append(StringFormatters.getUserAbbreviatedName(it.getUser()));
                sbCheckinMsgLine2.append(", ");
            }
            if (sbCheckinMsgLine2.length() > 0) {
                sbCheckinMsgLine2.delete(sbCheckinMsgLine2.length()-2, sbCheckinMsgLine2.length());
            } 
            String checkinMsgLine3 = "at " + StringFormatters.DATE_FORMAT_TODAY.format(new Date());

            contentView.setTextViewText(R.id.text1, checkinMsgLine1);
            contentView.setTextViewText(R.id.text2, sbCheckinMsgLine2.toString());
            contentView.setTextViewText(R.id.text3, checkinMsgLine3);
        }
        
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, FriendsActivity.class), 0); 
        Notification notification = new Notification(
                R.drawable.notification_icon, 
                "Foursquare Checkin", 
                System.currentTimeMillis()); 
        notification.contentView = contentView;
        notification.contentIntent = pi;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        if (prefs.getBoolean(Preferences.PREFERENCE_PINGS_VIBRATE, false)) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (newCheckins.size() > 1) {
            notification.number = newCheckins.size();
        }
        
        mgr.notify(NOTIFICATION_ID_CHECKINS, notification);
    }
    
    private boolean checkUserStillWantsPings(String userId, Foursquare foursquare) {
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
    
    public static void setupPings(Context context) {

        // If the user has pings on, set an alarm every N minutes, where N is their
        // requested refresh rate. We default to 30 if some problem reading set interval.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean(Preferences.PREFERENCE_PINGS, false)) {

            int refreshRateInMinutes = getRefreshIntervalInMinutes(prefs);
            if (DEBUG) {
                Log.d(TAG, "User has pings on, attempting to setup alarm with interval: " 
                        + refreshRateInMinutes + "..");
            }
            
            // We want to mark this as the last run time so we don't get any notifications
            // before the service is started.
            prefs.edit().putLong(
                    Preferences.PREFERENCE_PINGS_SERVICE_LAST_RUN_TIME, System.currentTimeMillis()).commit();
                 
            // Schedule the alarm.
            AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE); 
            mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
                             SystemClock.elapsedRealtime() + (refreshRateInMinutes * 60 * 1000), 
                             refreshRateInMinutes * 60 * 1000, 
                             makePendingIntentAlarm(context)); 
        }
    }
    
    public static void cancelPings(Context context) {
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.cancel(makePendingIntentAlarm(context));
    }
     
    private static PendingIntent makePendingIntentAlarm(Context context) {
        return PendingIntent.getBroadcast(context, 0, new Intent(context, PingsOnAlarmReceiver.class), 0); 
    }
     
    public static void clearAllNotifications(Context context) {
        NotificationManager mgr = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE); 
        mgr.cancelAll();
    }
    
    public static void generatePingsTest(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0); 
        
        NotificationManager mgr = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE); 
         
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.pings_list_item);
        contentView.setTextViewText(R.id.text1, "Ping title line");
        contentView.setTextViewText(R.id.text2, "Ping message line 2");
        contentView.setTextViewText(R.id.text3, "Ping message line 3");
        
        Notification notification = new Notification(
                R.drawable.notification_icon, 
                "Foursquare Checkin", 
                System.currentTimeMillis()); 
        notification.contentView = contentView;
        notification.contentIntent = pi;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        mgr.notify(-1, notification); 
    }
    
    private static int getRefreshIntervalInMinutes(SharedPreferences prefs) {
        int refreshRateInMinutes = 30;
        try {
            refreshRateInMinutes = Integer.parseInt(prefs.getString(
                    Preferences.PREFERENCE_PINGS_INTERVAL, String.valueOf(refreshRateInMinutes)));
        } catch (NumberFormatException ex) {
            Log.e(TAG, "Error parsing pings interval time, defaulting to: " + refreshRateInMinutes);
        }
        
        return refreshRateInMinutes;
    }
}
