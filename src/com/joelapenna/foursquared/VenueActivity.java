/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Venue;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.HashSet;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueActivity extends TabActivity {
    private static final String TAG = "VenueActivity";
    private static final boolean DEBUG = Foursquared.DEBUG;

    public static final String ACTION_PROGRESS_BAR_START = "com.joelapenna.foursquared.VenueActivity.PROGRESS_BAR_START";
    public static final String ACTION_PROGRESS_BAR_STOP = "com.joelapenna.foursquared.VenueActivity.PROGRESS_BAR_STOP";
    public static final String EXTRA_TASK_ID = "task_id";
    Venue mVenue;

    ProgressBarHandler mProgressBarHandler = new ProgressBarHandler();
    BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.venue_activity);

        // setVenue((Venue)getIntent().getExtras().get(Foursquared.EXTRAS_VENUE_KEY));
        setVenue(FoursquaredTest.createTestVenue("Test"));
        // Venue venue = FoursquaredTest.createTestVenue("A");
        // setVenue(venue);

        // We register this early (not in onStart) because our children might end up calling out to
        // this.
        initBroadcastReceiver();

        initTabHost();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG) Log.d(TAG, "onStart()");
    }

    @Override
    public void onStop() {
        super.onStart();
        if (DEBUG) Log.d(TAG, "onStop()");
        unregisterReceiver(mBroadcastReceiver);
    }

    public static void startProgressBar(Context context, String taskId) {
        Intent intent = new Intent(ACTION_PROGRESS_BAR_START);
        intent.putExtra(VenueActivity.EXTRA_TASK_ID, taskId);
        context.sendBroadcast(intent);
    }

    public static void stopProgressBar(Context context, String taskId) {
        Intent intent = new Intent(ACTION_PROGRESS_BAR_STOP);
        intent.putExtra(VenueActivity.EXTRA_TASK_ID, taskId);
        context.sendBroadcast(intent);
    }

    private void initBroadcastReceiver() {
        if (DEBUG) Log.d(TAG, "initBroadcastReceiver()");
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (DEBUG) Log.d(TAG, "BrodcastReceiver: onReceive() " + intent);
                if (ACTION_PROGRESS_BAR_START.equals(intent.getAction())) {
                    Message msg = mProgressBarHandler
                            .obtainMessage(ProgressBarHandler.MESSAGE_PROGRESS_BAR_START);
                    msg.obj = intent.getStringExtra(EXTRA_TASK_ID);
                    msg.sendToTarget();
                } else if (ACTION_PROGRESS_BAR_STOP.equals(intent.getAction())) {
                    Message msg = mProgressBarHandler
                            .obtainMessage(ProgressBarHandler.MESSAGE_PROGRESS_BAR_STOP);
                    msg.obj = intent.getStringExtra(EXTRA_TASK_ID);
                    msg.sendToTarget();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PROGRESS_BAR_STOP);
        filter.addAction(ACTION_PROGRESS_BAR_START);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void initTabHost() {
        final TabHost tabHost = this.getTabHost();
        String tag;
        Intent intent;

        tag = (String)this.getText(R.string.venue_checkin_activity_name);
        intent = new Intent(this, VenueCheckinActivity.class);
        intent.putExtra(Foursquared.EXTRAS_VENUE_KEY, mVenue);
        tabHost.addTab(tabHost.newTabSpec(tag)
                // Checkin Tab
                .setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_add))
                .setContent(intent) // The contained activity
                );

        tag = (String)this.getText(R.string.venue_info_activity_name);
        intent = new Intent(this, VenueMapActivity.class);
        intent.putExtra(Foursquared.EXTRAS_VENUE_KEY, mVenue);
        tabHost.addTab(tabHost.newTabSpec(tag)
                // Info Tab
                .setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_compass))
                .setContent(intent) // The contained activity
                );

        tag = (String)this.getText(R.string.venue_tips_activity_name);
        intent = new Intent(this, VenueTipsActivity.class);
        intent.putExtra(Foursquared.EXTRAS_VENUE_KEY, mVenue);
        tabHost.addTab(tabHost.newTabSpec(tag)
                // Info Tab
                .setIndicator("",
                        getResources().getDrawable(android.R.drawable.ic_menu_info_details))
                .setContent(intent) // The contained activity
                );
    }

    private void setVenue(Venue venue) {
        if (DEBUG) Log.d(TAG, "loading venue:" + venue.getVenuename());
        TextView name = (TextView)findViewById(R.id.venueName);
        TextView locationLine1 = (TextView)findViewById(R.id.venueLocationLine1);
        TextView locationLine2 = (TextView)findViewById(R.id.venueLocationLine2);

        name.setText(venue.getVenuename());
        locationLine1.setText(venue.getAddress());

        String line2 = Foursquared.getVenueLocationLine2(venue);
        if (line2 != null) {
            locationLine2.setText(line2);
        }

        mVenue = venue;
    }

    private class ProgressBarHandler extends Handler {

        static final int MESSAGE_PROGRESS_BAR_START = 0;
        static final int MESSAGE_PROGRESS_BAR_STOP = 1;

        private HashSet<Object> mTasks = new HashSet<Object>();

        @Override
        public void handleMessage(Message msg) {
            if (DEBUG) Log.d(TAG, "Recieved message:" + msg.toString());
            switch (msg.what) {
                case MESSAGE_PROGRESS_BAR_START:
                    boolean added = mTasks.add(msg.obj);
                    if (!added) {
                        throw new IllegalStateException("Task already being tracked: " + msg.obj);
                    }
                    setProgressBarIndeterminateVisibility(true);
                    break;
                case MESSAGE_PROGRESS_BAR_STOP:
                    boolean removed = mTasks.remove(msg.obj);
                    if (!removed) {
                        throw new IllegalStateException("Task not being tracked: " + msg.obj);
                    } else if (mTasks.isEmpty()) {
                        setProgressBarIndeterminateVisibility(false);
                    }
                    break;
            }
        }
    }
}
