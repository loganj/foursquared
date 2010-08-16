/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.app.LoadableListActivity;
import com.joelapenna.foursquared.location.BestLocationListener;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.MenuUtils;
import com.joelapenna.foursquared.util.NotificationsUtil;
import com.joelapenna.foursquared.util.UserUtils;
import com.joelapenna.foursquared.widget.SeparatedListAdapter;
import com.joelapenna.foursquared.widget.VenueListAdapter;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com)
 *   -Refactored to allow NearbyVenuesMapActivity to list to search results.
 */
public class NearbyVenuesActivity extends LoadableListActivity {
    static final String TAG = "NearbyVenuesActivity";
    static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String INTENT_EXTRA_STARTUP_GEOLOC_DELAY = Foursquared.PACKAGE_NAME
            + ".NearbyVenuesActivity.INTENT_EXTRA_STARTUP_GEOLOC_DELAY";
 
    private static final int MENU_REFRESH = 0;
    private static final int MENU_ADD_VENUE = 1;
    private static final int MENU_SEARCH = 2;
    private static final int MENU_MYINFO = 3;
    private static final int MENU_MAP = 4;

    private StateHolder mStateHolder = new StateHolder();
    private SearchLocationObserver mSearchLocationObserver = new SearchLocationObserver();
    
    public static SearchResultsObservable searchResultsObservable; 

    private ListView mListView;
    private SeparatedListAdapter mListAdapter;
    private LinearLayout mFooterView;
    private TextView mTextViewFooter;
    private Handler mHandler;
    
    
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
        
        searchResultsObservable = new SearchResultsObservable();

        mHandler = new Handler();
        mListView = getListView();
        mListAdapter = new SeparatedListAdapter(this);

        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Venue venue = (Venue) parent.getAdapter().getItem(position);
                startItemActivity(venue);
            }
        });
        
        // We can dynamically add a footer to our loadable listview.
        LayoutInflater inflater = LayoutInflater.from(this);
        mFooterView = (LinearLayout)inflater.inflate(R.layout.geo_loc_address_view, null);
        mTextViewFooter = (TextView)mFooterView.findViewById(R.id.footerTextView);
        LinearLayout llMain = (LinearLayout)findViewById(R.id.main);
        llMain.addView(mFooterView);

        // Check if we're returning from a configuration change.
        if (getLastNonConfigurationInstance() != null) {
            if (DEBUG) Log.d(TAG, "Restoring state.");
            mStateHolder = (StateHolder) getLastNonConfigurationInstance();
            mStateHolder.setActivity(this);
        } else {
            mStateHolder = new StateHolder();
            mStateHolder.setQuery("");
        }
        
        // Start a new search if one is not running or we have no results.
        if (mStateHolder.getIsRunningTask()) {
            setProgressBarIndeterminateVisibility(true);
            putSearchResultsInAdapter(mStateHolder.getResults());
            ensureTitle(false);
        } else if (mStateHolder.getResults().size() == 0) {
            long firstLocDelay = 0L;
            if (getIntent().getExtras() != null) {
                firstLocDelay = getIntent().getLongExtra(INTENT_EXTRA_STARTUP_GEOLOC_DELAY, 0L);
            }
            startTask(firstLocDelay);
        } else {
            onTaskComplete(mStateHolder.getResults(), mStateHolder.getReverseGeoLoc(), null);
        }
        populateFooter(mStateHolder.getReverseGeoLoc());
    }
 
    @Override
    public Object onRetainNonConfigurationInstance() {
        mStateHolder.setActivity(null);
        return mStateHolder;
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
        
        ((Foursquared) getApplication()).requestLocationUpdates(mSearchLocationObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
 
        ((Foursquared) getApplication()).removeLocationUpdates(mSearchLocationObserver);

        if (isFinishing()) {
            mStateHolder.cancelAllTasks();
            mListAdapter.removeObserver();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_REFRESH, Menu.NONE, R.string.refresh_label) //
                .setIcon(R.drawable.ic_menu_refresh);
        menu.add(Menu.NONE, MENU_SEARCH, Menu.NONE, R.string.search_label) //
                .setIcon(R.drawable.ic_menu_search) //
                .setAlphabeticShortcut(SearchManager.MENU_KEY);
        menu.add(Menu.NONE, MENU_ADD_VENUE, Menu.NONE, R.string.nearby_menu_add_venue) //
                .setIcon(R.drawable.ic_menu_add);

        int sdk = new Integer(Build.VERSION.SDK).intValue();
        if (sdk < 4) {
            int menuIcon = UserUtils.getDrawableForMeMenuItemByGender(
                ((Foursquared) getApplication()).getUserGender());
            menu.add(Menu.NONE, MENU_MYINFO, Menu.NONE, R.string.myinfo_label) //
                    .setIcon(menuIcon);
        }
        
        // Shows a map of all nearby venues, works but not going into this version.
        //menu.add(Menu.NONE, MENU_MAP, Menu.NONE, "Map")
        //    .setIcon(R.drawable.ic_menu_places);

        MenuUtils.addPreferencesToMenu(this, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_REFRESH:
                if (mStateHolder.getIsRunningTask() == false) {
                    startTask();
                }
                return true;
            case MENU_SEARCH:
                Intent intent = new Intent(NearbyVenuesActivity.this, SearchVenuesActivity.class);
                intent.setAction(Intent.ACTION_SEARCH);
                startActivity(intent);
                return true;
            case MENU_ADD_VENUE:
                startActivity(new Intent(NearbyVenuesActivity.this, AddVenueActivity.class));
                return true;
            case MENU_MYINFO:
                Intent intentUser = new Intent(NearbyVenuesActivity.this, UserDetailsActivity.class);
                intentUser.putExtra(UserDetailsActivity.EXTRA_USER_ID,
                        ((Foursquared) getApplication()).getUserId());
                startActivity(intentUser);
                return true;
            case MENU_MAP:
                startActivity(new Intent(NearbyVenuesActivity.this, NearbyVenuesMapActivity.class));
                return true;
                
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getNoSearchResultsStringId() {
        return R.string.no_nearby_venues;
    }

    public void putSearchResultsInAdapter(Group<Group<Venue>> searchResults) {
        if (DEBUG) Log.d(TAG, "putSearchResultsInAdapter");

        mListAdapter.removeObserver();
        mListAdapter = new SeparatedListAdapter(this);
        if (searchResults != null && searchResults.size() > 0) {
            int groupCount = searchResults.size();
            for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
                Group<Venue> group = searchResults.get(groupsIndex);
                if (group.size() > 0) {
                    VenueListAdapter groupAdapter = new VenueListAdapter(this,
                            ((Foursquared) getApplication()).getRemoteResourceManager());
                    groupAdapter.setGroup(group);
                    if (DEBUG) Log.d(TAG, "Adding Section: " + group.getType());
                    mListAdapter.addSection(group.getType(), groupAdapter);
                }
            }
        } else {
            setEmptyView();
        }
        
        mListView.setAdapter(mListAdapter);
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
    
    private void populateFooter(String reverseGeoLoc) {
        mFooterView.setVisibility(View.VISIBLE);
        mTextViewFooter.setText(reverseGeoLoc);
    }
    
    private long getClearGeolocationOnSearch() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean cacheGeolocation = settings.getBoolean(Preferences.PREFERENCE_CACHE_GEOLOCATION_FOR_SEARCHES, true);
        if (cacheGeolocation) {
            return 0L;
        } else {
            Foursquared foursquared = ((Foursquared) getApplication());
            foursquared.clearLastKnownLocation();
            foursquared.removeLocationUpdates(mSearchLocationObserver);
            foursquared.requestLocationUpdates(mSearchLocationObserver);
            return 2000L;
        }
    }

    class SearchResultsObservable extends Observable {

        @Override
        public void notifyObservers(Object data) {
            setChanged();
            super.notifyObservers(data);
        }

        public Group<Group<Venue>> getSearchResults() {
            return mStateHolder.getResults();
        }
    }

    /** If location changes, auto-start a nearby venues search. */
    private class SearchLocationObserver implements Observer {

        private boolean mRequestedFirstSearch = false;

        @Override
        public void update(Observable observable, Object data) {
            Location location = (Location) data;
            // Fire a search if we haven't done so yet.
            if (!mRequestedFirstSearch
                    && ((BestLocationListener) observable).isAccurateEnough(location)) {
                mRequestedFirstSearch = true;
                if (mStateHolder.getIsRunningTask() == false) {
                    // Since we were told by the system that location has changed, no need to make the
                    // task wait before grabbing the current location.
                    mHandler.post(new Runnable() {
                        public void run() {
                            startTask(0L);
                        }
                    });
                }
            }
        }
    }
    
    private void startTask() {
        startTask(getClearGeolocationOnSearch());
    }
    
    private void startTask(long geoLocDelayTimeInMs) {
        if (mStateHolder.getIsRunningTask() == false) {
            setProgressBarIndeterminateVisibility(true);
            ensureTitle(false);
            if (mStateHolder.getResults().size() == 0) {
                setLoadingView();
            }
            mStateHolder.startTask(this, mStateHolder.getQuery(), geoLocDelayTimeInMs);
        }
    }
    
    private void onTaskComplete(Group<Group<Venue>> result, String reverseGeoLoc, Exception ex) {
        if (result != null) {
            mStateHolder.setResults(result);
            mStateHolder.setReverseGeoLoc(reverseGeoLoc);
        } else {
            mStateHolder.setResults(new Group<Group<Venue>>());   
            NotificationsUtil.ToastReasonForFailure(NearbyVenuesActivity.this, ex);
        }

        populateFooter(mStateHolder.getReverseGeoLoc());
        putSearchResultsInAdapter(mStateHolder.getResults());
        setProgressBarIndeterminateVisibility(false);
        ensureTitle(true);
        searchResultsObservable.notifyObservers();
        
        mStateHolder.cancelAllTasks();
    }
    
    /** Handles the work of finding nearby venues. */
    private static class SearchTask extends AsyncTask<Void, Void, Group<Group<Venue>>> {

        private NearbyVenuesActivity mActivity;
        private Exception mReason = null;
        private String mQuery;
        private long mSleepTimeInMs;
        private Foursquare mFoursquare;
        private String mReverseGeoLoc; // Filled in after execution.
        
        public SearchTask(NearbyVenuesActivity activity, String query, long sleepTimeInMs) {
            super();
            mActivity = activity;
            mQuery = query;
            mSleepTimeInMs = sleepTimeInMs;
            mFoursquare = ((Foursquared)activity.getApplication()).getFoursquare();
        }

        @Override
        public void onPreExecute() {
        }

        @Override
        public Group<Group<Venue>> doInBackground(Void... params) {
            
            try {
                // If the user has chosen to clear their geolocation on each search, wait briefly
                // for a new fix to come in. The two-second wait time is arbitrary and can be 
                // changed to something more intelligent.
                if (mSleepTimeInMs > 0L) {
                    Thread.sleep(mSleepTimeInMs);
                }
                
                // Get last known location.
                Location location = ((Foursquared) mActivity.getApplication()).getLastKnownLocation();
                if (location == null) {
                    throw new FoursquareException("Your location could not be found! Make sure network or GPS location services are turned on, then try refreshing the venues list again!");
                }
                
                // Get the venues.
                Group<Group<Venue>> results = mFoursquare.venues(LocationUtils
                        .createFoursquareLocation(location), mQuery, 30);
                
                // Try to get our reverse geolocation.
                mReverseGeoLoc = getGeocode(mActivity, location);
                
                return results;
                
            } catch (Exception e) {
                mReason = e;
            }
            return null;
        }
        
        @Override
        public void onPostExecute(Group<Group<Venue>> groups) {
            if (mActivity != null) {
                mActivity.onTaskComplete(groups, mReverseGeoLoc, mReason);
            }
        }

        private String getGeocode(Context context, Location location) {
            Geocoder geocoded = new Geocoder(context, Locale.getDefault());   
            try {
                List<Address> addresses = geocoded.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);

                    StringBuilder sb = new StringBuilder(128);
                    sb.append("Near ");
                    sb.append(address.getAddressLine(0));
                    if (addresses.size() > 1) {
                        sb.append(", ");
                        sb.append(address.getAddressLine(1));
                    } 
                    if (addresses.size() > 2) {
                        sb.append(", ");
                        sb.append(address.getAddressLine(2));
                    }
                    
                    if (!TextUtils.isEmpty(address.getLocality())) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }  
                        sb.append(address.getLocality());
                    }

                    return sb.toString();
                }
            } catch (Exception ex) {
                if (DEBUG) Log.d(TAG, "SearchTask: error geocoding current location.", ex);
            }
            
            return null;
        }
        
        public void setActivity(NearbyVenuesActivity activity) {
            mActivity = activity;
        }
    }

    private static class StateHolder {
        private Group<Group<Venue>> mResults;
        private String mQuery;
        private String mReverseGeoLoc;
        private SearchTask mSearchTask;
        
        public StateHolder() {
            mResults = new Group<Group<Venue>>();
            mSearchTask = null;
        }
        
        public String getQuery() {
            return mQuery;
        }
        
        public void setQuery(String query) {
            mQuery = query;
        }
        
        public String getReverseGeoLoc() {
            return mReverseGeoLoc;
        }
        
        public void setReverseGeoLoc(String reverseGeoLoc) {
            mReverseGeoLoc = reverseGeoLoc;
        }
        
        public Group<Group<Venue>> getResults() {
            return mResults;
        }
        
        public void setResults(Group<Group<Venue>> results) {
            mResults = results;
        }
        
        public void startTask(NearbyVenuesActivity activity, String query, long sleepTimeInMs) {
            mSearchTask = new SearchTask(activity, query, sleepTimeInMs);
            mSearchTask.execute();
        }
        
        public boolean getIsRunningTask() {
            return mSearchTask != null;
        }
        
        public void cancelAllTasks() {
            if (mSearchTask != null) {
                mSearchTask.cancel(true);
                mSearchTask = null;
            }
        }
        
        public void setActivity(NearbyVenuesActivity activity) {
            if (mSearchTask != null) {
                mSearchTask.setActivity(activity);
            }
        }
    }
}
