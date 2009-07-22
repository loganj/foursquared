/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.StringFormatters;

import android.app.TabActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueActivity extends TabActivity {
    private static final String TAG = "VenueActivity";
    private static final boolean DEBUG = Foursquared.DEBUG;

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_VENUE = "com.joelapenna.foursquared.VenueId";

    VenueObservable venueObservable = new VenueObservable();

    private HashSet<Object> mProgressBarTasks = new HashSet<Object>();

    private Observer mVenueObserver = new VenueObserver();
    private Venue mVenue = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.venue_activity);

        initTabHost();

        venueObservable.addObserver(mVenueObserver);
        new VenueTask().execute(getIntent().getExtras().getString(EXTRA_VENUE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Foursquared.addPreferencesToMenu(this, menu);
        return true;

    }

    public void startProgressBar(String taskId) {
        boolean added = mProgressBarTasks.add(taskId);
        if (!added) {
            if (DEBUG) Log.d(TAG, "Received start for already tracked task. Ignoring");
        }
        setProgressBarIndeterminateVisibility(true);
    }

    public void stopProgressBar(String taskId) {
        boolean removed = mProgressBarTasks.remove(taskId);
        if (!removed) {
            if (DEBUG) Log.d(TAG, "Received stop for untracked task. Ignoring");
        } else if (mProgressBarTasks.isEmpty()) {
            setProgressBarIndeterminateVisibility(false);
        }
    }

    private void initTabHost() {
        final TabHost tabHost = this.getTabHost();
        String tag;
        Intent intent;

        tag = (String)this.getText(R.string.venue_checkin_activity_name);
        intent = new Intent(this, VenueCheckinActivity.class);
        tabHost.addTab(tabHost.newTabSpec(tag)
                // Checkin Tab
                .setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_add))
                .setContent(intent) // The contained activity
                );

        tag = (String)this.getText(R.string.venue_info_activity_name);
        intent = new Intent(this, VenueMapActivity.class);
        tabHost.addTab(tabHost.newTabSpec(tag)
                // Info Tab
                .setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_compass))
                .setContent(intent) // The contained activity
                );

        tag = (String)this.getText(R.string.venue_tips_activity_name);
        intent = new Intent(this, VenueTipsActivity.class);
        tabHost.addTab(tabHost.newTabSpec(tag)
                // Info Tab
                .setIndicator("",
                        getResources().getDrawable(android.R.drawable.ic_menu_info_details))
                .setContent(intent) // The contained activity
                );

    }

    private void displayVenue(Venue venue) {
        if (DEBUG) Log.d(TAG, "loading venue:" + venue.getName());
        setTitle(venue.getName() + " - Foursquared");
        TextView name = (TextView)findViewById(R.id.venueName);
        TextView locationLine1 = (TextView)findViewById(R.id.venueLocationLine1);
        TextView locationLine2 = (TextView)findViewById(R.id.venueLocationLine2);

        name.setText(venue.getName());
        locationLine1.setText(venue.getAddress());

        String line2 = StringFormatters.getVenueLocationCrossStreetOrCity(venue);
        if (line2 != null) {
            locationLine2.setText(line2);
        }
    }

    private void setVenue(Venue venue) {
        mVenue = venue;
        venueObservable.notifyObservers(venue);
    }

    private class VenueTask extends AsyncTask<String, Void, Venue> {
        private static final String PROGRESS_BAR_TASK_ID = TAG + "VenueTask";

        @Override
        protected void onPreExecute() {
            startProgressBar(PROGRESS_BAR_TASK_ID);
        }

        @Override
        protected Venue doInBackground(String... params) {
            try {
                return Foursquared.getFoursquare().venue(params[0]);
            } catch (FoursquareException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareException", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Venue venue) {
            stopProgressBar(PROGRESS_BAR_TASK_ID);
            setVenue(venue);
        }

        @Override
        protected void onCancelled() {
            stopProgressBar(PROGRESS_BAR_TASK_ID);
        }
    }

    class VenueObservable extends Observable {
        public void notifyObservers(Object data) {
            setChanged();
            super.notifyObservers(data);
        }

        public Venue getVenue() {
            return mVenue;
        }
    }

    /**
     * This guy will be an attempt at controlling a dialog without relying on AsyncTask...
     *
     * @author Joe LaPenna (joe@joelapenna.com)
     */
    class VenueObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            displayVenue(mVenue);
        }
    }
}
