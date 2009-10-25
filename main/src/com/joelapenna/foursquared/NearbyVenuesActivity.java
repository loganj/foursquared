/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.app.LoadableListActivity;
import com.joelapenna.foursquared.maps.BestLocationListener;
import com.joelapenna.foursquared.util.Comparators;
import com.joelapenna.foursquared.util.DumpcatcherHelper;
import com.joelapenna.foursquared.util.NotificationsUtil;
import com.joelapenna.foursquared.widget.SeparatedListAdapter;
import com.joelapenna.foursquared.widget.VenueListAdapter;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class NearbyVenuesActivity extends LoadableListActivity {
    static final String TAG = "NearbyVenuesActivity";
    static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final long DELAY_TIME_IN_MS = 2000;

    private static final int MENU_REFRESH = 0;
    private static final int MENU_ADD_VENUE = 1;
    private static final int MENU_SEARCH = 2;
    private static final int MENU_MYINFO = 3;

    private SearchTask mSearchTask;
    private SearchHolder mSearchHolder = new SearchHolder();
    private SearchHandler mSearchHandler = new SearchHandler();
    private SearchLocationListener mSearchLocationListener = new SearchLocationListener();
    private SearchResultsObservable mSearchResultsObservable = new SearchResultsObservable();

    private ListView mListView;
    private SeparatedListAdapter mListAdapter;

    private BroadcastReceiver mLoggedOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive: " + intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultKeyMode(Activity.DEFAULT_KEYS_SEARCH_LOCAL);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        mListView = getListView();
        mListAdapter = new SeparatedListAdapter(this);

        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Venue venue = (Venue)parent.getAdapter().getItem(position);
                startItemActivity(venue);
            }
        });

        mSearchResultsObservable.addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                putSearchResultsInAdapter(((SearchResultsObservable)observable).getSearchResults());
            }
        });

        if (getLastNonConfigurationInstance() != null) {
            if (DEBUG) Log.d(TAG, "Restoring state.");
            SearchHolder holder = (SearchHolder)getLastNonConfigurationInstance();
            if (holder.results != null) {
                setSearchResults(holder.results);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedOutReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume");
        // Prime location with master listener's info.
        mSearchLocationListener.getBetterLocation(((Foursquared)getApplication())
                .getLastKnownLocation());
        // Register listener
        mSearchLocationListener
                .register((LocationManager)getSystemService(Context.LOCATION_SERVICE));

        if (mSearchHolder.results == null) {
            mSearchHandler.sendEmptyMessageDelayed(SearchHandler.MESSAGE_SEARCH, DELAY_TIME_IN_MS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSearchLocationListener
                .unregister((LocationManager)getSystemService(Context.LOCATION_SERVICE));
    }

    @Override
    public void onStop() {
        super.onStop();
        mSearchHandler.sendEmptyMessage(SearchHandler.MESSAGE_STOP_SEARCH);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_SEARCH, Menu.NONE, R.string.search_label) //
                .setIcon(android.R.drawable.ic_search_category_default) //
                .setAlphabeticShortcut(SearchManager.MENU_KEY);
        menu.add(Menu.NONE, MENU_REFRESH, Menu.NONE, R.string.refresh_label) //
                .setIcon(R.drawable.ic_menu_refresh);
        menu.add(Menu.NONE, MENU_ADD_VENUE, Menu.NONE, R.string.add_venue_label) //
                .setIcon(android.R.drawable.ic_menu_add);
        menu.add(Menu.NONE, MENU_MYINFO, Menu.NONE, R.string.myinfo_label) //
                .setIcon(R.drawable.ic_menu_myinfo);
        Foursquared.addPreferencesToMenu(this, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SEARCH:
                Intent intent = new Intent(NearbyVenuesActivity.this, SearchVenuesActivity.class);
                intent.setAction(Intent.ACTION_SEARCH);
                startActivity(intent);
                return true;
            case MENU_REFRESH:
                mSearchHandler.sendEmptyMessage(SearchHandler.MESSAGE_FORCE_SEARCH);
                return true;
            case MENU_ADD_VENUE:
                startActivity(new Intent(NearbyVenuesActivity.this, AddVenueActivity.class));
                return true;
            case MENU_MYINFO:
                startActivity(new Intent(NearbyVenuesActivity.this, UserActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mSearchHolder;
    }

    @Override
    public int getNoSearchResultsStringId() {
        return R.string.no_nearby_venues;
    }

    public void putSearchResultsInAdapter(Group<Group<Venue>> searchResults) {
        setEmptyView();
        mListAdapter.clear();
        int groupCount = searchResults.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group<Venue> group = searchResults.get(groupsIndex);
            if (group.size() > 0) {
                VenueListAdapter groupAdapter = new VenueListAdapter(this);
                groupAdapter.setGroup(group);
                if (DEBUG) Log.d(TAG, "Adding Section: " + group.getType());
                mListAdapter.addSection(group.getType(), groupAdapter);
            }
        }
        mListAdapter.notifyDataSetInvalidated();
    }

    public void setSearchResults(Group<Group<Venue>> searchResults) {
        if (DEBUG) Log.d(TAG, "Setting search results.");
        mSearchHolder.results = searchResults;
        mSearchResultsObservable.notifyObservers();
    }

    void startItemActivity(Venue venue) {
        Intent intent = new Intent(NearbyVenuesActivity.this, VenueActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(Foursquared.EXTRA_VENUE_ID, venue.getId());
        startActivity(intent);
    }

    private void ensureTitle(boolean finished) {
        if (finished) {
            setTitle(getString(R.string.title_search_finished_noquery));
        } else {
            setTitle(getString(R.string.title_search_inprogress_noquery));
        }
    }

    private class SearchTask extends AsyncTask<Void, Void, Group<Group<Venue>>> {

        private static final int METERS_PER_MILE = 1609;

        private Exception mReason = null;

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "SearchTask: onPreExecute()");
            setProgressBarIndeterminateVisibility(true);
            ensureTitle(false);
        }

        @Override
        public Group<Group<Venue>> doInBackground(Void... params) {
            try {
                return search();
            } catch (Exception e) {
                mReason = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Group<Group<Venue>> groups) {
            try {
                if (groups == null) {
                    NotificationsUtil.ToastReasonForFailure(NearbyVenuesActivity.this, mReason);
                } else {
                    setSearchResults(groups);
                }

            } finally {
                setProgressBarIndeterminateVisibility(false);
                ensureTitle(true);
            }
        }

        public Group<Group<Venue>> search() throws FoursquareException, IOException {
            if (DEBUG) Log.d(TAG, "SearchTask.search()");
            Foursquare foursquare = ((Foursquared)getApplication()).getFoursquare();
            Location location = mSearchLocationListener.getLastKnownLocation();
            String geolat;
            String geolong;
            int radius;
            if (location == null) {
                if (DEBUG) Log.d(TAG, "SearchTask.search(): doing user lookup");
                // Foursquare requires a lat, lng for a venue search, so we have to pull it from the
                // server if we cannot determine it locally.
                City city = foursquare.user(null, false, false).getCity();
                geolat = String.valueOf(city.getGeolat());
                geolong = String.valueOf(city.getGeolong());
                radius = 1;
            } else {
                if (DEBUG) Log.d(TAG, "SearchTask.search(): searching with location: " + location);
                geolat = String.valueOf(location.getLatitude());
                geolong = String.valueOf(location.getLongitude());

                if (location.hasAccuracy()) {
                    radius = (int)Math.round(location.getAccuracy() / (double)METERS_PER_MILE);
                } else {
                    radius = 1;
                }
            }
            if (DEBUG) Log.d(TAG, "SearchTask.search(): executing: " + geolat + ", " + geolong);
            Group<Group<Venue>> groups = foursquare.venues(geolat, geolong, mSearchHolder.query,
                    radius, 30);
            DumpcatcherHelper.sendLocation("/venues#NearbyVenuesActivity", location);
            for (int i = 0; i < groups.size(); i++) {
                Collections.sort(groups.get(i), Comparators.getVenueNameComparator());
            }
            return groups;
        }
    }

    private class SearchHandler extends Handler {

        public static final int MESSAGE_FORCE_SEARCH = 0;
        public static final int MESSAGE_STOP_SEARCH = 1;
        public static final int MESSAGE_SEARCH = 2;

        private boolean mFirstSearchCompleted = false;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (DEBUG) Log.d(TAG, "handleMessage: " + String.valueOf(msg.what));

            switch (msg.what) {
                case MESSAGE_FORCE_SEARCH:
                    mSearchTask = (SearchTask)new SearchTask().execute();
                    return;

                case MESSAGE_STOP_SEARCH:
                    if (mSearchTask != null) {
                        mSearchTask.cancel(true);
                        mSearchTask = null;
                    }
                    return;

                case MESSAGE_SEARCH:
                    if (mSearchTask == null
                            || AsyncTask.Status.FINISHED.equals(mSearchTask.getStatus())
                            && !mFirstSearchCompleted) {
                        mFirstSearchCompleted = true;
                        mSearchTask = (SearchTask)new SearchTask().execute();
                    }
                    return;

                default:
            }
        }

    }

    private class SearchResultsObservable extends Observable {

        public void notifyObservers(Object data) {
            setChanged();
            super.notifyObservers(data);
        }

        public Group<Group<Venue>> getSearchResults() {
            return mSearchHolder.results;
        }

        public String getQuery() {
            return mSearchHolder.query;
        }
    }

    private class SearchLocationListener extends BestLocationListener {

        private boolean mRequestedFirstSearch = false;

        @Override
        public void onBestLocationChanged(Location location) {
            super.onBestLocationChanged(location);
            // Fire a search if we haven't done so yet.
            boolean accurateEnough = isAccurateEnough(getLastKnownLocation());
            if (!mRequestedFirstSearch && accurateEnough) {
                mRequestedFirstSearch = true;
                mSearchHandler.removeMessages(SearchHandler.MESSAGE_SEARCH);
                mSearchHandler.sendEmptyMessage(SearchHandler.MESSAGE_SEARCH);
            }
        }
    }

    private static class SearchHolder {
        Group<Group<Venue>> results;
        String query;
    }
}
