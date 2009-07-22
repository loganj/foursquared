/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.SeparatedListAdapter;
import com.joelapenna.foursquared.widget.CheckinListAdapter;

import android.app.ListActivity;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueCheckinActivity extends ListActivity {
    public static final String TAG = "VenueCheckinActivity";
    public static final boolean DEBUG = Foursquared.DEBUG;

    private Venue mVenue;
    private Group mGroups;

    private LookupCheckinsAsyncTask mCheckinTask;
    private Observer mVenueObserver;

    private TextView mEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_checkin_activity);

        setListAdapter(new SeparatedListAdapter(this));
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Checkin checkin = (Checkin)parent.getAdapter().getItem(position);
                startCheckinActivity(checkin);
            }
        });

        mEmpty = (TextView)findViewById(android.R.id.empty);

        if (getLastNonConfigurationInstance() != null) {
            setCheckins((Group)getLastNonConfigurationInstance());

        } else if (((VenueActivity)getParent()).venueObservable.getVenue() != null) {
            mVenue = ((VenueActivity)getParent()).venueObservable.getVenue();
            lookupCheckinGroups();

        } else {
            mVenueObserver = new Observer() {
                @Override
                public void update(Observable observable, Object data) {
                    mVenue = (Venue)data;
                    lookupCheckinGroups();
                }
            };
            ((VenueActivity)getParent()).venueObservable.addObserver(mVenueObserver);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mGroups;
    }

    private void lookupCheckinGroups() {
        if (DEBUG) Log.d(TAG, "lookupCheckin()");

        // If a task is already running, don't start a new one.
        if (mCheckinTask != null && mCheckinTask.getStatus() != AsyncTask.Status.FINISHED) {
            if (DEBUG) Log.d(TAG, "Query already running attempting to cancel: " + mCheckinTask);
            if (!mCheckinTask.cancel(true) && !mCheckinTask.isCancelled()) {
                if (DEBUG) Log.d(TAG, "Unable to cancel checkins? That should not have happened!");
                Toast.makeText(this, "Unable to re-query checkins.", Toast.LENGTH_SHORT);
                return;
            }
        }
        mCheckinTask = (LookupCheckinsAsyncTask)new LookupCheckinsAsyncTask().execute();
    }

    private void setCheckins(Group checkins) {
        if (checkins == null) {
            Toast.makeText(getApplicationContext(), "Could not complete TODO lookup!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Group filteredCheckins = new Group();
        filteredCheckins.setType("Recent Checkins");
        Checkin checkin;
        for (int i = 0; i < checkins.size(); i++) {
            checkin = (Checkin)checkins.get(i);
            if (checkin.getVenue().getId().equals(mVenue.getId())) {
                filteredCheckins.add(checkin);
            }
        }
        mGroups = filteredCheckins;
        putCheckinsInAdapter(filteredCheckins);
    }

    void startCheckinActivity(Checkin checkin) {
        if (DEBUG) Log.d(TAG, "(not) Firing checkin activity for checkin");
        // Intent intent = new Intent(VenueCheckinActivity.this, CheckinActivity.class);
        // intent.setAction(Intent.ACTION_VIEW);
        // intent.putExtra(VenueActivity.EXTRA_VENUE, checkin.getVenueid());
        // startActivity(intent);
    }

    private void putCheckinsInAdapter(Group checkins) {
        if (DEBUG) Log.d(TAG, "Putting groups in adapter.");
        SeparatedListAdapter mainAdapter = (SeparatedListAdapter)getListAdapter();
        mainAdapter.clear();
        CheckinListAdapter groupAdapter = new CheckinListAdapter(this, checkins);
        mainAdapter.addSection("Recent Checkins", groupAdapter);
        mainAdapter.notifyDataSetInvalidated();
    }

    private class LookupCheckinsAsyncTask extends AsyncTask<Void, Void, Group> {

        private static final String PROGRESS_BAR_TASK_ID = TAG + "LookupCheckinsAsyncTask";

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "CheckinTask: onPreExecute()");
            ((VenueActivity)getParent()).startProgressBar(PROGRESS_BAR_TASK_ID);
        }

        @Override
        public Group doInBackground(Void... params) {
            try {
                Location location = ((Foursquared)getApplication()).getLastKnownLocation();
                if (location == null) {
                    if (DEBUG) Log.d(TAG, "Getting Checkins without Location");
                    return Foursquared.getFoursquare().checkins(null);
                } else {
                    if (DEBUG) Log.d(TAG, "Getting Checkins with Location: " + location);
                    return Foursquared.getFoursquare().checkins(null);

                }
            } catch (FoursquareException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareError", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
            return null;
        }

        @Override
        public void onPostExecute(Group checkins) {
            if (DEBUG) Log.d(TAG, "CheckinTask: onPostExecute()");
            try {
                setCheckins(checkins);
            } finally {
                ((VenueActivity)getParent()).stopProgressBar(PROGRESS_BAR_TASK_ID);
                if (getListAdapter().getCount() <= 0) {
                    mEmpty.setText("No checkins for this venue! Add one!");
                }
            }
        }

        @Override
        public void onCancelled() {
            ((VenueActivity)getParent()).stopProgressBar(PROGRESS_BAR_TASK_ID);
        }

    }
}
