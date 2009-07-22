/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.SeparatedListAdapter;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueSearchActivity extends ListActivity {
    private static final String TAG = "VenueSearchActivity";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private static final int MENU_SEARCH = 0;

    private SearchAsyncTask mSearchTask;
    private String mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate");

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.venue_search_activity);

        setListAdapter(new SeparatedListAdapter(this));
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Venue venue = (Venue)parent.getAdapter().getItem(position);
                fireVenueActivityIntent(venue);
            }
        });

        if (getLastNonConfigurationInstance() != null) {
            if (DEBUG) Log.d(TAG, "Restoring configuration.");
            mQuery = (String)getLastNonConfigurationInstance();
            startQuery(mQuery);
        } else {
            if (DEBUG) Log.d(TAG, "Running new intent.");
            onNewIntent(getIntent());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_SEARCH, Menu.NONE, R.string.search_label) // More stuff.
                .setIcon(android.R.drawable.ic_search_category_default);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SEARCH:
                onSearchRequested();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "New Intent: " + intent);
        if (intent == null) {
            if (DEBUG) Log.d(TAG, "No intent to search, querying default.");
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            if (DEBUG) Log.d(TAG, "onNewIntent received search intent");
        }
        startQuery(intent.getStringExtra(SearchManager.QUERY));
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mQuery;
    }
    
    @Override
    public void onStop() {
        super.onStop();
        if (mSearchTask != null) {
            mSearchTask.cancel(true);
        }
    }

    void testStuff() {
        Group groups = new Group();
        groups.setType("TLG");
        groups.add(FoursquaredTest.createVenueGroup("Group A"));
        groups.add(FoursquaredTest.createVenueGroup("Group B"));
        groups.add(FoursquaredTest.createVenueGroup("Group C"));
        putGroupsInAdapter(groups);
    }

    void startQuery(String query) {
        if (DEBUG) Log.d(TAG, "sendQuery()");
        mQuery = query;

        // If a task is already running, don't start a new one.
        if (mSearchTask != null && mSearchTask.getStatus() != AsyncTask.Status.FINISHED) {
            if (DEBUG) Log.d(TAG, "Query already running attempting to cancel: " + mSearchTask);
            if (!mSearchTask.cancel(true) && !mSearchTask.isCancelled()) {
                if (DEBUG) Log.d(TAG, "Unable to cancel search? Notifying the user.");
                Toast.makeText(this, "A search is already in progress.", Toast.LENGTH_SHORT);
                return;
            }
        }
        mSearchTask = (SearchAsyncTask)new SearchAsyncTask().execute();
    }

    void fireVenueActivityIntent(Venue venue) {
        if (DEBUG) Log.d(TAG, "firing venue activity for venue");
        Intent intent = new Intent(VenueSearchActivity.this, VenueActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra("venue", venue);
        startActivity(intent);
    }

    private void putGroupsInAdapter(Group groups) {
        if (groups == null) {
            Toast.makeText(getApplicationContext(), "Could not complete search!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        SeparatedListAdapter mainAdapter = (SeparatedListAdapter)getListAdapter();
        mainAdapter.clear();
        int groupCount = groups.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group group = (Group)groups.get(groupsIndex);
            VenueListAdapter groupAdapter = new VenueListAdapter(this, group);
            if (DEBUG) Log.d(TAG, "Adding Section: " + group.getType());
            mainAdapter.addSection(group.getType(), groupAdapter);
        }
        mainAdapter.notifyDataSetInvalidated();
    }

    class SearchAsyncTask extends AsyncTask<Void, Void, Group> {

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "SearchTask: onPreExecute()");
            setProgressBarIndeterminateVisibility(true);
            if (mQuery == null) {
                setTitle("Searching Nearby - Foursquared");
            } else {
                setTitle("Searching \"" + mQuery + "\" - Foursquared");
            }
        }

        @Override
        public Group doInBackground(Void... params) {
            try {
                Location location = ((Foursquared)getApplication()).getLocation();
                if (location == null) {
                    return ((Foursquared)getApplication()).getFoursquare().venues(mQuery, null,
                            null, 10, 1);
                } else {
                    return ((Foursquared)getApplication()).getFoursquare().venues(mQuery,
                            String.valueOf(location.getLatitude()),
                            String.valueOf(location.getLongitude()), 10, 1);
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
                putGroupsInAdapter(groups);
            } finally {
                setProgressBarIndeterminateVisibility(false);
                if (mQuery == null) {
                    setTitle("Searching Nearby");
                } else {
                    setTitle(mQuery + " - Foursquared");
                }
            }
        }
    }
}
