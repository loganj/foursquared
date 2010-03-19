/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.app.LoadableListActivity;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.util.MeasurementSystems;
import com.joelapenna.foursquared.util.NotificationsUtil;
import com.joelapenna.foursquared.widget.VenueListAdapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;

/**
 * Shows a list of venues that the specified user is mayor of.
 * We can fetch these ourselves given a userId, or work from
 * a venue array parcel.
 * 
 * @date March 15, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class UserMayorshipsActivity extends LoadableListActivity {
    static final String TAG = "UserMayorshipsActivity";
    static final boolean DEBUG = FoursquaredSettings.DEBUG;
    
    public static final String EXTRA_USER_ID = Foursquared.PACKAGE_NAME
        + ".UserMayorshipsActivity.EXTRA_USER_ID";

    public static final String EXTRA_VENUE_LIST_PARCEL = Foursquared.PACKAGE_NAME
        + ".UserMayorshipsActivity.EXTRA_VENUE_LIST_PARCEL";

    private StateHolder mStateHolder;
    private VenueListAdapter mListAdapter;

    
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
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
            mStateHolder.setActivityForTaskVenues(this);
        } else {

            mStateHolder = new StateHolder();
            if (getIntent().getExtras().containsKey(EXTRA_USER_ID) == false) {
                Log.e(TAG, "UserMayorships requires a userid in its intent extras.");
                finish();
                return;
            }
            mStateHolder.setUserId(getIntent().getExtras().getString(EXTRA_USER_ID));
            
            if (getIntent().getExtras().containsKey(EXTRA_VENUE_LIST_PARCEL)) {
                // Can't jump from ArrayList to Group, argh.
                ArrayList<Venue> venues = getIntent().getExtras().getParcelableArrayList(
                        EXTRA_VENUE_LIST_PARCEL);
                Group<Venue> group = new Group<Venue>();
                for (Venue it : venues) {
                    group.add(it);
                }
                mStateHolder.setVenues(group);
                
            } else {
                mStateHolder.startTaskVenues(this, mStateHolder.getUserId());
            }
        }
        
        ensureUi();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (isFinishing()) {
            mStateHolder.cancelTasks();
            mListAdapter.removeObserver();
            unregisterReceiver(mLoggedOutReceiver);
        }
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        mStateHolder.setActivityForTaskVenues(null);
        return mStateHolder;
    }

    private void ensureUi() {
        mListAdapter = new VenueListAdapter(this, 
            ((Foursquared) getApplication()).getRemoteResourceManager(),
            ((Foursquared) getApplication()).getMeasurementSystem().equals(
                    MeasurementSystems.METRIC));
        mListAdapter.setGroup(mStateHolder.getVenues());
        
        ListView listView = getListView();
        listView.setAdapter(mListAdapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Venue venue = (Venue)mListAdapter.getItem(position);
                
                Intent intent = new Intent(UserMayorshipsActivity.this, VenueActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra(Foursquared.EXTRA_VENUE_ID, venue.getId());
                startActivity(intent);
            }
        });
        
        if (mStateHolder.getIsRunningVenuesTask()) {
            setLoadingView();
        } else if (mStateHolder.getFetchedVenuesOnce() && mStateHolder.getVenues().size() == 0) {
            setEmptyView();
        }
    }

    private void onVenuesTaskComplete(User user, Exception ex) {
        mListAdapter.removeObserver();
        mListAdapter = new VenueListAdapter(this, 
            ((Foursquared) getApplication()).getRemoteResourceManager(),
            ((Foursquared) getApplication()).getMeasurementSystem().equals(
                    MeasurementSystems.METRIC));
        if (user != null) {
            mStateHolder.setVenues(user.getMayorships());
            mListAdapter.setGroup(mStateHolder.getVenues());
            getListView().setAdapter(mListAdapter);
        }
        else {
            mStateHolder.setVenues(new Group<Venue>());
            mListAdapter.setGroup(mStateHolder.getVenues());
            getListView().setAdapter(mListAdapter);
            
            NotificationsUtil.ToastReasonForFailure(this, ex);
        }
        mStateHolder.setIsRunningVenuesTask(false);
        mStateHolder.setFetchedVenuesOnce(true);
        
        // TODO: We can probably tighten this up by just calling ensureUI() again.
        if (mStateHolder.getVenues().size() == 0) {
            setEmptyView();
        }
    }
    
    /**
     * Gets venues that the current user is mayor of.
     */
    private static class VenuesTask extends AsyncTask<String, Void, User> {

        private UserMayorshipsActivity mActivity;
        private Exception mReason;

        public VenuesTask(UserMayorshipsActivity activity) {
            mActivity = activity;
        }
        
        @Override
        protected void onPreExecute() {
            mActivity.setLoadingView();
        }

        @Override
        protected User doInBackground(String... params) {
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();
                return foursquare.user(params[0], Foursquare.USER_MAYOR_VENUE_INFO_FULL, false, 
                        LocationUtils.createFoursquareLocation(foursquared.getLastKnownLocation()));
            } catch (Exception e) {
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            if (mActivity != null) {
                mActivity.onVenuesTaskComplete(user, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onVenuesTaskComplete(null, mReason);
            }
        }
        
        public void setActivity(UserMayorshipsActivity activity) {
            mActivity = activity;
        }
    }
    
    
    private static class StateHolder {
        
        /** User id. */
        private String mUserId;
        
        /** Friends of the current user. */
        private Group<Venue> mVenues;
        
        private VenuesTask mTaskVenues;
        private boolean mIsRunningVenuesTask;
        private boolean mFetchedVenuesOnce;
        
        
        public StateHolder() {
            mIsRunningVenuesTask = false;
            mFetchedVenuesOnce = false;
            mVenues = new Group<Venue>();
        }
 
        public String getUserId() {
            return mUserId;
        }
        
        public void setUserId(String userId) {
            mUserId = userId;
        }
        
        public Group<Venue> getVenues() {
            return mVenues;
        }
        
        public void setVenues(Group<Venue> venues) {
            mVenues = venues;
        }
        
        public void startTaskVenues(UserMayorshipsActivity activity,
                                    String userId) {
            mIsRunningVenuesTask = true;
            mTaskVenues = new VenuesTask(activity);
            mTaskVenues.execute(userId);
        }

        public void setActivityForTaskVenues(UserMayorshipsActivity activity) {
            if (mTaskVenues != null) {
                mTaskVenues.setActivity(activity);
            }
        }

        public void setIsRunningVenuesTask(boolean isRunning) {
            mIsRunningVenuesTask = isRunning;
        }

        public boolean getIsRunningVenuesTask() {
            return mIsRunningVenuesTask;
        }
        
        public void setFetchedVenuesOnce(boolean fetchedOnce) {
            mFetchedVenuesOnce = fetchedOnce;
        }
        
        public boolean getFetchedVenuesOnce() {
            return mFetchedVenuesOnce;
        }
        
        public void cancelTasks() {
            if (mTaskVenues != null) {
                mTaskVenues.setActivity(null);
                mTaskVenues.cancel(true);
            }
        }
    }
}
