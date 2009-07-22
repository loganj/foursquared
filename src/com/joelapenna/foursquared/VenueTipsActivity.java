/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.SeparatedListAdapter;

import android.app.ListActivity;
import android.content.Intent;
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
import java.util.ArrayList;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueTipsActivity extends ListActivity {
    public static final String TAG = "VenueTipsActivity";
    public static final boolean DEBUG = Foursquared.DEBUG;

    private Venue mVenue;

    private TipsAsyncTask mTipsTask;
    private TextView mEmpty;
    private Group mTipGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_tips_activity);

        setListAdapter(new SeparatedListAdapter(this));
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // fireVenueActivityIntent(venue);
            }
        });

        mEmpty = (TextView)findViewById(android.R.id.empty);

        setVenue((Venue)getIntent().getExtras().get(Foursquared.EXTRAS_VENUE_KEY));

        if (getLastNonConfigurationInstance() != null) {
            setTipGroups((Group)getLastNonConfigurationInstance());
        } else {
            mTipsTask = (TipsAsyncTask)new TipsAsyncTask().execute();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mTipsTask != null) {
            mTipsTask.cancel(true);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mTipGroups;
    }

    /**
     * If a new tips lookup comes in cancel the old one and start a new one.
     */
    private void lookupTipGroups() {
        if (DEBUG) Log.d(TAG, "lookupTips()");

        // If a task is already running, don't start a new one.
        if (mTipsTask != null && mTipsTask.getStatus() != AsyncTask.Status.FINISHED) {
            if (DEBUG) Log.d(TAG, "Query already running attempting to cancel: " + mTipsTask);
            if (!mTipsTask.cancel(true) && !mTipsTask.isCancelled()) {
                if (DEBUG) Log.d(TAG, "Unable to cancel tips? That should not have happened!");
                Toast.makeText(this, "Unable to re-query tips.", Toast.LENGTH_SHORT);
                return;
            }
        }
        mTipsTask = (TipsAsyncTask)new TipsAsyncTask().execute();
    }

    private void setTipGroups(Group groups) {
        if (groups == null) {
            Toast.makeText(getApplicationContext(), "Could not complete TODO lookup!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mTipGroups = filterTipGroups(groups);
        putGroupsInAdapter(mTipGroups);
    }

    private Group filterTipGroups(Group groups) {
        Group filteredGroup = new Group();
        filteredGroup.setType(groups.getType());
        int groupCount = groups.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group group = (Group)groups.get(groupsIndex);
            if (mVenue.getVenueid() != null) {
                filterTipGroupByVenueid(mVenue.getVenueid(), group);
                if (group.size() > 0) {
                    filteredGroup.add(group);
                }
            }
        }
        return filteredGroup;
    }

    private void filterTipGroupByVenueid(String venueid, Group group) {
        ArrayList<Tip> venueTips = new ArrayList<Tip>();
        int tipCount = group.size();
        for (int tipIndex = 0; tipIndex < tipCount; tipIndex++) {
            Tip tip = (Tip)group.get(tipIndex);
            if (venueid.equals(tip.getVenueid())) {
                venueTips.add(tip);
            }
        }
        group.clear();
        group.addAll(venueTips);
    }

    private void putGroupsInAdapter(Group groups) {
        SeparatedListAdapter mainAdapter = (SeparatedListAdapter)getListAdapter();
        mainAdapter.clear();
        int groupCount = groups.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group group = (Group)groups.get(groupsIndex);
            TipsListAdapter groupAdapter = new TipsListAdapter(this, group);
            if (DEBUG) Log.d(TAG, "Adding Section: " + group.getType());
            mainAdapter.addSection(group.getType(), groupAdapter);
        }
        mainAdapter.notifyDataSetInvalidated();
    }

    private void setVenue(Venue venue) {
        mVenue = venue;
    }

    private class TipsAsyncTask extends AsyncTask<Void, Void, Group> {

        private static final String VENUE_ACTIVITY_PROGRESS_BAR_TASK_ID = TAG + "TipsAsyncTask";

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "TipsTask: onPreExecute()");
            startProgressBar();
        }

        @Override
        public Group doInBackground(Void... params) {
            try {
                Location location = ((Foursquared)getApplication()).getLocation();
                if (location == null) {
                    if (DEBUG) Log.d(TAG, "Getting Todos without Location");
                    return ((Foursquared)getApplication()).getFoursquare().todos(null, null, null);
                } else {
                    if (DEBUG) Log.d(TAG, "Getting Todos with Location: " + location);
                    return ((Foursquared)getApplication()).getFoursquare().todos(null,
                            String.valueOf(location.getLatitude()),
                            String.valueOf(location.getLongitude()));

                }
            } catch (FoursquareError e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareError", e);
            } catch (FoursquareParseException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareParseException", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
            return null;
        }

        @Override
        public void onPostExecute(Group groups) {
            try {
                setTipGroups(groups);
            } finally {
                if (DEBUG) Log.d(TAG, "TipsTask: onPostExecute()");
                stopProgressBar();
                if (getListAdapter().getCount() <= 0) {
                    mEmpty.setText("No tips for this venue! Add one!");
                }
            }
        }

        @Override
        public void onCancelled() {
            stopProgressBar();
        }

        private void startProgressBar() {
            Intent intent = new Intent(VenueActivity.ACTION_PROGRESS_BAR_START);
            intent.putExtra(VenueActivity.EXTRA_TASK_ID, VENUE_ACTIVITY_PROGRESS_BAR_TASK_ID);
            sendBroadcast(intent);
        }

        private void stopProgressBar() {
            Intent intent = new Intent(VenueActivity.ACTION_PROGRESS_BAR_STOP);
            intent.putExtra(VenueActivity.EXTRA_TASK_ID, VENUE_ACTIVITY_PROGRESS_BAR_TASK_ID);
            sendBroadcast(intent);
        }
    }
}
