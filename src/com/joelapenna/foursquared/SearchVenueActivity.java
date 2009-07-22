/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.providers.VenueQuerySuggestionsProvider;
import com.joelapenna.foursquared.util.SeparatedListAdapter;

import android.app.SearchManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
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
import java.util.Date;
import java.util.Observable;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class SearchVenueActivity extends TabActivity {
    private static final String TAG = "SearchVenueActivity";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private static final int MENU_SEARCH = 0;
    private static final int MENU_REFRESH = 1;
    private static final int MENU_NEARBY = 2;

    private static final String QUERY_NEARBY = null;

    private static final long LOCATION_UPDATE_MIN_TIME = 1000 * 60;
    private static final long LOCATION_UPDATE_MIN_DISTANCE = 100;
    private static final long MAX_LOCATION_UPDATE_DELTA_THRESHOLD = 1000 * 60 * 5;

    private SearchAsyncTask mSearchTask;
    private LocationManager mLocationManager;
    private BestLocationListener mLocationListener;
    private Location mLocation;

    private String mQuery;
    Group mSearchResults;

    private TextView mEmpty;
    private SeparatedListAdapter mListAdapter;
    private ListView mListView;
    private TabHost mTabHost;

    public static SearchResultsObservable searchResultsObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.search_venue_activity);

        searchResultsObservable = new SearchResultsObservable();

        initTabHost();

        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new BestLocationListener();
        mLocation = ((Foursquared)getApplication()).getLocation();

        mEmpty = (TextView)findViewById(R.id.empty);

        mListView = (ListView)findViewById(R.id.list);
        mListAdapter = new SeparatedListAdapter(this);

        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Venue venue = (Venue)parent.getAdapter().getItem(position);
                fireVenueActivityIntent(venue);
            }
        });

        if (getLastNonConfigurationInstance() != null) {
            if (DEBUG) Log.d(TAG, "Restoring state.");
            StateHolder holder = (StateHolder)getLastNonConfigurationInstance();
            if (holder.results == null) {
                startQuery(holder.query);
            } else {
                mQuery = holder.query;
                setSearchResults(holder.results);
                putGroupsInAdapter(holder.results);
            }
        } else {
            if (DEBUG) Log.d(TAG, "Running new intent.");
            onNewIntent(getIntent());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_SEARCH, Menu.NONE, R.string.search_label) //
                .setIcon(android.R.drawable.ic_menu_search);
        menu.add(Menu.NONE, MENU_NEARBY, Menu.NONE, R.string.nearby_label) //
                .setIcon(android.R.drawable.ic_menu_compass);
        menu.add(Menu.NONE, MENU_REFRESH, Menu.NONE, R.string.refresh_label) //
                .setIcon(R.drawable.ic_menu_refresh);
        Foursquared.addPreferencesToMenu(this, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SEARCH:
                onSearchRequested();
                return true;
            case MENU_NEARBY:
                startQuery(null);
                return true;
            case MENU_REFRESH:
                startQuery(mQuery);
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
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    VenueQuerySuggestionsProvider.AUTHORITY, VenueQuerySuggestionsProvider.MODE);
            suggestions.saveRecentQuery(intent.getStringExtra(SearchManager.QUERY), null);

        }
        startQuery(intent.getStringExtra(SearchManager.QUERY));
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        StateHolder holder = new StateHolder();
        holder.query = mQuery;
        holder.results = mSearchResults;
        return holder;
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mLocationManager.removeUpdates(mLocationListener);
        if (mSearchTask != null) {
            mSearchTask.cancel(true);
        }
    }

    private void initTabHost() {
        mTabHost = getTabHost();

        // Results tab
        mTabHost.addTab(mTabHost.newTabSpec("results")
                // Checkin Tab
                .setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_search))
                .setContent(R.id.listviewLayout) //
                );

        // Maps tab
        Intent intent = new Intent(this, SearchVenueMapActivity.class);
        mTabHost.addTab(mTabHost.newTabSpec("map")
                // Map Tab
                .setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_mapmode))
                .setContent(intent) // The
                // contained
                // activity
                );
        mTabHost.setCurrentTab(0);
    }

    void setSearchResults(Group searchResults) {
        if (DEBUG) Log.d(TAG, "Setting search results.");
        mSearchResults = searchResults;
        searchResultsObservable.notifyObservers();
    }

    void startQuery(String query) {
        if (DEBUG) Log.d(TAG, "sendQuery()");
        mQuery = query;
        // not going through set* because we don't want to notify search result
        // observers.
        mSearchResults = null;

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
        Intent intent = new Intent(SearchVenueActivity.this, VenueActivity.class);
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
        mListAdapter.clear();
        int groupCount = groups.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group group = (Group)groups.get(groupsIndex);
            if (group.size() > 0) {
                VenueListAdapter groupAdapter = new VenueListAdapter(this, group);
                if (DEBUG) Log.d(TAG, "Adding Section: " + group.getType());
                mListAdapter.addSection(group.getType(), groupAdapter);
            }
        }
        mListAdapter.notifyDataSetInvalidated();
    }

    private class SearchAsyncTask extends AsyncTask<Void, Void, Group> {

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "SearchTask: onPreExecute()");
            setProgressBarIndeterminateVisibility(true);
            if (mQuery == QUERY_NEARBY) {
                setTitle("Searching Nearby - Foursquared");
            } else {
                setTitle("Searching \"" + mQuery + "\" - Foursquared");
            }
        }

        @Override
        public Group doInBackground(Void... params) {
            try {
                Location location = mLocation;
                Foursquare foursquare = ((Foursquared)getApplication()).getFoursquare();
                if (location == null) {
                    if (DEBUG) Log.d(TAG, "Searching without location.");
                    return foursquare.venues(mQuery, null, null, 10, 1);
                } else {
                    // Try to make the search radius to be the same as our
                    // accuracy.
                    if (DEBUG) Log.d(TAG, "Searching with location: " + location);
                    int radius;
                    if (location.hasAccuracy()) {
                        radius = Float.valueOf(location.getAccuracy()).intValue();
                    } else {
                        radius = 10;
                    }
                    return foursquare.venues(mQuery, String.valueOf(location.getLatitude()), String
                            .valueOf(location.getLongitude()), radius, 1);
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
                setSearchResults(groups);
                putGroupsInAdapter(groups);
            } finally {
                setProgressBarIndeterminateVisibility(false);
                if (mQuery == QUERY_NEARBY) {
                    setTitle("Nearby - Foursquared");
                } else {
                    setTitle(mQuery + " - Foursquared");
                }
                if (mListAdapter.getCount() > 0) {
                    mEmpty.setVisibility(View.GONE);
                } else {
                    mEmpty.setText("No results found! Try another search!");
                    mEmpty.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private class BestLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (DEBUG) Log.d(TAG, "onLocationChanged: " + location.getProvider());
            if (mLocation == null) {
                if (DEBUG) Log.d(TAG, "No previous location. using new: " + location);
                mLocation = location;
                return;
            }

            // If we've decided to use the new location.
            if (updateLocation(location)) {

            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // do nothing.
        }

        @Override
        public void onProviderEnabled(String provider) {
            // do nothing.
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // do nothing.
        }

        private boolean updateLocation(Location location) {
            Location lastLocation = mLocation;

            long now = new Date().getTime();
            long locationUpdateDelta = now - location.getTime();
            long lastLocationUpdateDelta = now - lastLocation.getTime();

            boolean locationIsMostRecent = locationUpdateDelta <= lastLocationUpdateDelta;

            boolean accuracyComparable = location.hasAccuracy() && lastLocation.hasAccuracy();
            boolean locationIsMoreAccurate = location.getAccuracy() <= lastLocation.getAccuracy();

            boolean locationIsInTimeThreshold = locationUpdateDelta <= MAX_LOCATION_UPDATE_DELTA_THRESHOLD;
            boolean lastLocationIsInTimeThreshold = lastLocationUpdateDelta <= MAX_LOCATION_UPDATE_DELTA_THRESHOLD;

            if (accuracyComparable && locationIsMoreAccurate && locationIsMostRecent) {
                if (DEBUG) Log.d(TAG, "New +Accuracy, +Time, using new: " + location);
                mLocation = location;
                return true;
            } else if (accuracyComparable && locationIsMoreAccurate && !locationIsInTimeThreshold) {
                if (DEBUG) Log.d(TAG, "New +Accuracy, -Time. Using old:" + lastLocation);
                return false;
            } else if (accuracyComparable && !locationIsMoreAccurate && !locationIsInTimeThreshold) {
                if (DEBUG) Log.d(TAG, "New -Accuracy -Time. Using old: " + lastLocation);
                return false;
            } else if (locationIsMostRecent) {
                if (DEBUG) Log.d(TAG, "New ?Accuracy, +Time. Using new: " + location);
                mLocation = location;
                return true;
            } else if (!lastLocationIsInTimeThreshold) {
                if (DEBUG) Log.d(TAG, "Old location too old. Using new: " + location);
                mLocation = location;
                return true;
            } else {
                if (DEBUG) Log.d(TAG, "Poor comparitive data. Using old: " + lastLocation);
                return false;
            }
        }
    }

    private class StateHolder {
        Group results;

        String query;
    }

    class SearchResultsObservable extends Observable {

        public void notifyObservers(Object data) {
            setChanged();
            super.notifyObservers(data);
        }

        public Group getSearchResults() {
            return mSearchResults;
        }
    };
}
