/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.app.LoadableListActivity;
import com.joelapenna.foursquared.error.LocationException;
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
import android.os.Message;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class NearbyVenuesActivity extends LoadableListActivity {
    static final String TAG = "NearbyVenuesActivity";
    static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String INTENT_EXTRA_STARTUP_GEOLOC_DELAY = Foursquared.PACKAGE_NAME
            + ".NearbyVenuesActivity.INTENT_EXTRA_STARTUP_GEOLOC_DELAY";

    private long mDelayTimeInMS = 1L;

    private static final int MENU_REFRESH = 0;
    private static final int MENU_ADD_VENUE = 1;
    private static final int MENU_SEARCH = 2;
    private static final int MENU_MYINFO = 3;

    private SearchTask mSearchTask;
    private SearchHolder mSearchHolder = new SearchHolder();
    private SearchHandler mSearchHandler = new SearchHandler();
    private SearchLocationObserver mSearchLocationObserver = new SearchLocationObserver();
    private SearchResultsObservable mSearchResultsObservable = new SearchResultsObservable();

    private ListView mListView;
    private SeparatedListAdapter mListAdapter;
    private LinearLayout mFooterView;
    private TextView mTextViewFooter;

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
        
        mSearchResultsObservable.addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                putSearchResultsInAdapter(((SearchResultsObservable) observable).getSearchResults());
            }
        });

        if (getLastNonConfigurationInstance() != null) {
            if (DEBUG) Log.d(TAG, "Restoring state.");
            SearchHolder holder = (SearchHolder) getLastNonConfigurationInstance();
            if (holder.results != null) {
                setSearchResults(holder.results, holder.reverseGeoLoc);
            }
            populateFooter(holder.reverseGeoLoc);
        }
        
        // We can reparse the delay startup time each onCreate().
        if (getIntent().getExtras() != null) {
            mDelayTimeInMS = getIntent().getLongExtra(
                    INTENT_EXTRA_STARTUP_GEOLOC_DELAY, 1L);
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
        ((Foursquared) getApplication()).requestLocationUpdates(mSearchLocationObserver);

        if (mSearchHolder.results == null) {
            mSearchHandler.sendEmptyMessageDelayed(SearchHandler.MESSAGE_SEARCH, mDelayTimeInMS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ((Foursquared) getApplication()).removeLocationUpdates(mSearchLocationObserver);

        if (isFinishing()) {
            mListAdapter.removeObserver();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mSearchHandler.sendEmptyMessage(SearchHandler.MESSAGE_STOP_SEARCH);
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

        MenuUtils.addPreferencesToMenu(this, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_REFRESH:
                mSearchHandler.sendEmptyMessage(SearchHandler.MESSAGE_FORCE_SEARCH);
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
        if (DEBUG) Log.d(TAG, "putSearchResultsInAdapter");

        mListAdapter.removeObserver();
        mListAdapter = new SeparatedListAdapter(this);
        if (searchResults != null) {
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
        }
        mListView.setAdapter(mListAdapter);
    }

    public void setSearchResults(Group<Group<Venue>> searchResults, String reverseGeoLoc) {
        if (DEBUG) Log.d(TAG, "Setting search results.");
        mSearchHolder.results = searchResults;
        mSearchResultsObservable.notifyObservers();
        mSearchHolder.reverseGeoLoc = reverseGeoLoc;
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
    
    private class SearchTask extends AsyncTask<Void, Void, Group<Group<Venue>>> {

        private Exception mReason = null;
        private String mReverseGeoLoc;
        private boolean mClearGeolocationOnSearch;
        
        public SearchTask(boolean clearGeolocationOnSearch) {
            super();
            mClearGeolocationOnSearch = clearGeolocationOnSearch;
        }

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "SearchTask: onPreExecute()");
            setProgressBarIndeterminateVisibility(true);
            ensureTitle(false);
            setLoadingView();

            if (mClearGeolocationOnSearch) {
                Foursquared foursquared = ((Foursquared) getApplication());
                foursquared.clearLastKnownLocation();
                foursquared.removeLocationUpdates(mSearchLocationObserver);
                foursquared.requestLocationUpdates(mSearchLocationObserver);
            }
        }

        @Override
        public Group<Group<Venue>> doInBackground(Void... params) {
            
            Foursquare foursquare = ((Foursquared) getApplication()).getFoursquare();
            
            try {
                // If the user has chosen to clear their geolocation on each search, wait briefly
                // for a new fix to come in. The two-second wait time is arbitrary and can be 
                // changed to something more intelligent.
                if (mClearGeolocationOnSearch) {
                    Thread.sleep(2000);
                }
                
                Location location = ((Foursquared) getApplication()).getLastKnownLocationOrThrow();
                
                Group<Group<Venue>> results = search(foursquare, location);
                
                // Try to get the geolocation associated with the search.
                mReverseGeoLoc = getGeocode(location);
                
                return results;
                
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
                }
                setSearchResults(groups, mReverseGeoLoc);

            } finally {
                setProgressBarIndeterminateVisibility(false);
                ensureTitle(true);
                setEmptyView();
            }
            
            populateFooter(mReverseGeoLoc);
        }

        public Group<Group<Venue>> search(Foursquare foursquare, Location location) 
            throws FoursquareException, LocationException, IOException {

            Group<Group<Venue>> groups = foursquare.venues(LocationUtils
                    .createFoursquareLocation(location), mSearchHolder.query, 30);
            
            // We can sort the returned venues by distance, but now the foursquare api should
            // do a smart-sort for us by popularity and distance, see here for more info:
            // http://blog.foursquare.com/post/589698188/weve-just-made-the-places-screen-smarter
            //for (int i = 0; i < groups.size(); i++) {
            //      Collections.sort(groups.get(i), Comparators.getVenueDistanceComparator());
            //}
            
            return groups;
        }
        
        private String getGeocode(Location location) {
            Geocoder geocoded = new Geocoder(getApplicationContext(), Locale.getDefault());   
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
                    mSearchTask = (SearchTask) new SearchTask(
                            getClearGeolocationOnSearch()).execute();
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
                        mSearchTask = (SearchTask) new SearchTask(
                                getClearGeolocationOnSearch()).execute();
                    }
                    return;

                default:
            }
        }
    }
    
    private boolean getClearGeolocationOnSearch() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean cacheGeolocation = settings.getBoolean(Preferences.PREFERENCE_CACHE_GEOLOCATION_FOR_SEARCHES, true);
        return !cacheGeolocation;
    } 

    private class SearchResultsObservable extends Observable {

        @Override
        public void notifyObservers(Object data) {
            setChanged();
            super.notifyObservers(data);
        }

        public Group<Group<Venue>> getSearchResults() {
            return mSearchHolder.results;
        }
    }

    private class SearchLocationObserver implements Observer {

        private boolean mRequestedFirstSearch = false;

        @Override
        public void update(Observable observable, Object data) {
            Location location = (Location) data;
            // Fire a search if we haven't done so yet.
            if (!mRequestedFirstSearch
                    && ((BestLocationListener) observable).isAccurateEnough(location)) {
                mRequestedFirstSearch = true;
                mSearchHandler.removeMessages(SearchHandler.MESSAGE_SEARCH);
                mSearchHandler.sendEmptyMessage(SearchHandler.MESSAGE_SEARCH);
            }
        }
    }

    private static class SearchHolder {
        Group<Group<Venue>> results;
        String query;
        String reverseGeoLoc;
    }
}
