/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.app.LoadableListActivity;
import com.joelapenna.foursquared.util.NotificationsUtil;
import com.joelapenna.foursquared.widget.HistoryListAdapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This only works for the currently authenticated user.
 * 
 * @date March 9, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class UserHistoryActivity extends LoadableListActivity {
    static final String TAG = "UserHistoryActivity";
    static final boolean DEBUG = FoursquaredSettings.DEBUG;
    
    private StateHolder mStateHolder;
    private HistoryListAdapter mListAdapter;

    
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
            mStateHolder.setActivityForTaskFriends(this);
        } else {
            mStateHolder = new StateHolder();
            mStateHolder.startTaskHistory(this);
        }
        
        ensureUi();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (isFinishing()) {
            mStateHolder.cancelTasks();
            unregisterReceiver(mLoggedOutReceiver);
        }
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        mStateHolder.setActivityForTaskFriends(null);
        return mStateHolder;
    }
    
    private void ensureUi() {
        mListAdapter = new HistoryListAdapter(
                this, ((Foursquared) getApplication()).getRemoteResourceManager());
        mListAdapter.setGroup(mStateHolder.getHistory());
        
        ListView listView = getListView();
        listView.setAdapter(mListAdapter);
        listView.setSmoothScrollbarEnabled(true);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
                Object obj = (Object)mListAdapter.getItem(position);
                if (obj != null) {
                    startVenueActivity((Checkin)obj);
                }
            }
        });
        
        if (mStateHolder.getIsRunningHistoryTask()) {
            setLoadingView();
        } else if (mStateHolder.getFetchedOnce() && mStateHolder.getHistory().size() == 0) {
            setEmptyView();
        }
    }
    
    @Override
    public int getNoSearchResultsStringId() {
        return R.string.user_history_activity_no_info;
    }
    
    private void onHistoryTaskComplete(Group<Checkin> group, Exception ex) {
        mListAdapter = new HistoryListAdapter(
                this, ((Foursquared) getApplication()).getRemoteResourceManager());
        if (group != null) {
            mStateHolder.setHistory(group);
            mListAdapter.setGroup(mStateHolder.getHistory());
            getListView().setAdapter(mListAdapter);
        } 
        else {
            mStateHolder.setHistory(new Group<Checkin>());
            mListAdapter.setGroup(mStateHolder.getHistory());
            getListView().setAdapter(mListAdapter);
            
            NotificationsUtil.ToastReasonForFailure(this, ex);
        }
        mStateHolder.setIsRunningHistoryTask(false);
        mStateHolder.setFetchedOnce(true);
        
        // TODO: Can tighten this up by just calling ensureUI() probably.
        if (mStateHolder.getHistory().size() == 0) {
            setEmptyView();
        }
    }
    
    private void startVenueActivity(Checkin checkin) {
        // TODO: Perform some action when user is clicking on a shout history item.
        if (checkin != null) {
            if (checkin.getVenue() != null && !TextUtils.isEmpty(checkin.getVenue().getId())) {
                Venue venue = checkin.getVenue();
                Intent intent = new Intent(this, VenueActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra(Foursquared.EXTRA_VENUE_ID, venue.getId());
                startActivity(intent);
            }
        }
    }
    
    /**
     * Gets friends of the current user we're working for.
     */
    private static class HistoryTask extends AsyncTask<String, Void, Group<Checkin>> {

        private UserHistoryActivity mActivity;
        private Exception mReason;

        public HistoryTask(UserHistoryActivity activity) {
            mActivity = activity;
        }
        
        @Override
        protected void onPreExecute() {
            mActivity.setLoadingView();
        }

        @Override
        protected Group<Checkin> doInBackground(String... params) {
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();
                
                // Prune out shouts for now.
                Group<Checkin> history = foursquare.history(100);
                Group<Checkin> venuesOnly = new Group<Checkin>();
                for (Checkin it : history) {
                    if (it.getVenue() != null) {
                        venuesOnly.add(it);
                    }
                }
                
                return venuesOnly;
            } catch (Exception e) {
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Group<Checkin> checkins) {
            if (mActivity != null) {
                mActivity.onHistoryTaskComplete(checkins, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onHistoryTaskComplete(null, mReason);
            }
        }
        
        public void setActivity(UserHistoryActivity activity) {
            mActivity = activity;
        }
    }
    
    
    private static class StateHolder {
        
        /** History of the logged in user. */
        private Group<Checkin> mHistory;
        
        private HistoryTask mTaskHistory;
        private boolean mIsRunningHistoryTask;
        private boolean mFetchedOnce;
        
        public StateHolder() {
            mIsRunningHistoryTask = false;
            mFetchedOnce = false;
            mHistory = new Group<Checkin>();
        }
        
        public Group<Checkin> getHistory() {
            return mHistory;
        }
        
        public void setHistory(Group<Checkin> history) {
            mHistory = history;
        }

        public void startTaskHistory(UserHistoryActivity activity) {
            mIsRunningHistoryTask = true;
            mTaskHistory = new HistoryTask(activity);
            mTaskHistory.execute();
        }

        public void setActivityForTaskFriends(UserHistoryActivity activity) {
            if (mTaskHistory != null) {
                mTaskHistory.setActivity(activity);
            }
        }

        public void setIsRunningHistoryTask(boolean isRunning) {
            mIsRunningHistoryTask = isRunning;
        }

        public boolean getIsRunningHistoryTask() {
            return mIsRunningHistoryTask;
        }
        
        public void setFetchedOnce(boolean fetchedOnce) {
            mFetchedOnce = fetchedOnce;
        }
        
        public boolean getFetchedOnce() {
            return mFetchedOnce;
        }
        
        public void cancelTasks() {
            if (mTaskHistory != null) {
                mTaskHistory.setActivity(null);
                mTaskHistory.cancel(true);
            }
        }
    }
}
