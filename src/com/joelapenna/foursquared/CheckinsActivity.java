/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.classic.Checkin;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.Foursquared.LocationListener;
import com.joelapenna.foursquared.util.SeparatedListAdapter;
import com.joelapenna.foursquared.widget.CheckinListAdapter;

import android.app.SearchManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.util.Observable;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class CheckinsActivity extends TabActivity {
    static final String TAG = "CheckinsActivity";
    static final boolean DEBUG = Foursquared.DEBUG;

    public static final String QUERY_NEARBY = null;
    public static SearchResultsObservable searchResultsObservable;

    private static final int MENU_REFRESH = 1;

    private static final int MENU_GROUP_SEARCH = 0;

    private SearchTask mSearchTask;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    private SearchHolder mSearchHolder = new SearchHolder();

    private TextView mEmpty;
    private SeparatedListAdapter mListAdapter;
    private ListView mListView;
    private TabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.checkins_activity);

        mLocationListener = ((Foursquared)getApplication()).getLocationListener();
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        searchResultsObservable = new SearchResultsObservable();

        initTabHost();
        initListViewAdapter();

        if (getLastNonConfigurationInstance() != null) {
            if (DEBUG) Log.d(TAG, "Restoring state.");
            SearchHolder holder = (SearchHolder)getLastNonConfigurationInstance();
            if (holder.results == null) {
                executeSearchTask(holder.query);
            } else {
                mSearchHolder.query = holder.query;
                setSearchResults(holder.results);
                putSearchResultsInAdapter(holder.results);
            }
        } else
            handleOnCreateIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(MENU_GROUP_SEARCH, MENU_REFRESH, Menu.NONE, R.string.refresh_label) //
                .setIcon(R.drawable.ic_menu_refresh);
        Foursquared.addPreferencesToMenu(this, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_REFRESH:
                executeSearchTask(mSearchHolder.query);
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
            if (DEBUG) Log.d(TAG, "onNewIntent received search intent and saving.");
        }
        executeSearchTask(intent.getStringExtra(SearchManager.QUERY));
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mSearchHolder;
    }

    @Override
    public void onStart() {
        super.onStart();
        // We should probably dynamically connect to any location provider we can find and not just
        // the gps/network providers.
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                LocationListener.LOCATION_UPDATE_MIN_TIME,
                LocationListener.LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                LocationListener.LOCATION_UPDATE_MIN_TIME,
                LocationListener.LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mLocationManager.removeUpdates(mLocationListener);
        if (mSearchTask != null) {
            mSearchTask.cancel(true);
        }
    }

    public void handleOnCreateIntent() {
        if (DEBUG) Log.d(TAG, "Running new intent.");
        onNewIntent(getIntent());
    }

    public void putSearchResultsInAdapter(Group searchResults) {
        if (searchResults == null) {
            Toast.makeText(getApplicationContext(), "Could not complete search!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mListAdapter.clear();
        int groupCount = searchResults.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group group = (Group)searchResults.get(groupsIndex);
            if (group.getType().equals("Me")) {
                if (DEBUG) Log.d(TAG, "Skipping 'Me' Section.");
            } else if (group.size() > 0 && !group.getType().equals("Me")) {
                CheckinListAdapter groupAdapter = new CheckinListAdapter(this, group);
                if (DEBUG) Log.d(TAG, "Adding Section: " + group.getType());
                mListAdapter.addSection(group.getType(), groupAdapter);
            }
        }
        mListAdapter.notifyDataSetInvalidated();
    }

    public void setSearchResults(Group searchResults) {
        if (DEBUG) Log.d(TAG, "Setting search results.");
        mSearchHolder.results = searchResults;
        searchResultsObservable.notifyObservers();
    }

    void executeSearchTask(String query) {
        if (DEBUG) Log.d(TAG, "sendQuery()");
        mSearchHolder.query = query;
        // not going through set* because we don't want to notify search result
        // observers.
        mSearchHolder.results = null;

        // If a task is already running, don't start a new one.
        if (mSearchTask != null && mSearchTask.getStatus() != AsyncTask.Status.FINISHED) {
            if (DEBUG) Log.d(TAG, "Query already running attempting to cancel: " + mSearchTask);
            if (!mSearchTask.cancel(true) && !mSearchTask.isCancelled()) {
                if (DEBUG) Log.d(TAG, "Unable to cancel search? Notifying the user.");
                Toast.makeText(this, "A search is already in progress.", Toast.LENGTH_SHORT);
                return;
            }
        }
        mSearchTask = (SearchTask)new SearchTask().execute();
    }

    void startItemActivity(Venue venue) {
        if (DEBUG) Log.d(TAG, "firing venue activity for venue");
        Intent intent = new Intent(CheckinsActivity.this, VenueActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra("venue", venue);
        startActivity(intent);
    }

    private void ensureSearchResults() {
        if (mListAdapter.getCount() > 0) {
            mEmpty.setVisibility(View.GONE);
        } else {
            mEmpty.setText("No results found! Try another search!");
            mEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void initListViewAdapter() {
        if (mListView != null) {
            throw new IllegalStateException("Trying to initialize already initialized ListView");
        }
        mEmpty = (TextView)findViewById(R.id.empty);

        mListView = (ListView)findViewById(R.id.list);
        mListAdapter = new SeparatedListAdapter(this);

        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Checkin checkin = (Checkin)parent.getAdapter().getItem(position);
                startItemActivity(CheckinListAdapter.venueFromCheckin(checkin));
            }
        });
    }

    private void initTabHost() {
        if (mTabHost != null) {
            throw new IllegalStateException("Trying to intialize already initializd TabHost");
        }
        mTabHost = getTabHost();

        // Results tab
        mTabHost.addTab(mTabHost.newTabSpec("results")
                // Checkin Tab
                .setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_search))
                .setContent(R.id.listviewLayout) //
                );

        // Maps tab
        Intent intent = new Intent(this, CheckinsMapActivity.class);
        mTabHost.addTab(mTabHost.newTabSpec("map")
                // Map Tab
                .setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_mapmode))
                .setContent(intent) // The
                // contained
                // activity
                );
        mTabHost.setCurrentTab(0);
    }

    private void ensureTitle(boolean finished) {
        if (finished) {
            if (mSearchHolder.query == QUERY_NEARBY) {
                setTitle("Nearby Checkins - Foursquared");
            } else {
                setTitle(mSearchHolder.query + " - Foursquared");
            }
        } else {
            if (mSearchHolder.query == QUERY_NEARBY) {
                setTitle("Searching Nearby Checkins - Foursquared");
            } else {
                setTitle("Searching \"" + mSearchHolder.query + "\" Checkins - Foursquared");
            }
        }

    }

    private class SearchTask extends AsyncTask<Void, Void, Group> {

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "SearchTask: onPreExecute()");
            setProgressBarIndeterminateVisibility(true);
            ensureTitle(false);
        }

        @Override
        public Group doInBackground(Void... params) {
            try {
                return search();
            } catch (FoursquareException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquarException", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
            return null;
        }

        @Override
        public void onPostExecute(Group groups) {
            try {
                setSearchResults(groups);
                putSearchResultsInAdapter(groups);
            } finally {
                setProgressBarIndeterminateVisibility(false);
                ensureTitle(true);
                ensureSearchResults();
            }
        }

        Group search() throws FoursquareException, IOException {
            Location location = mLocationListener.getLastKnownLocation();
            Foursquare foursquare = Foursquared.getFoursquare();
            if (location == null) {
                if (DEBUG) Log.d(TAG, "Searching without location.");
                return foursquare.checkins(null, null, null);
            } else {
                // Try to make the search radius to be the same as our
                // accuracy.
                if (DEBUG) Log.d(TAG, "Searching with location: " + location);
                return foursquare.checkins(null, String.valueOf(location.getLatitude()), String
                        .valueOf(location.getLongitude()));
            }
        }
    }

    private static class SearchHolder {
        Group results;
        String query;
    }

    class SearchResultsObservable extends Observable {

        public void notifyObservers(Object data) {
            setChanged();
            super.notifyObservers(data);
        }

        public Group getSearchResults() {
            return mSearchHolder.results;
        }

        public String getQuery() {
            return mSearchHolder.query;
        }
    };
}
