/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.util.NotificationsUtil;
import com.joelapenna.foursquared.widget.FriendRequestsAdapter;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Presents the user with a list of pending friend requests that they can
 * approve or deny.
 * 
 * @date February 12, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public class FriendRequestsActivity extends ListActivity {
    private static final String TAG = "FriendRequestsActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final int MENU_REFRESH = 0;

    private StateHolder mStateHolder;
    private ProgressDialog mDlgProgress;
    private EditText mEditTextFilter;
    private FriendRequestsAdapter mListAdapter;
    private TextView mTextViewNoRequests;
    private Handler mHandler;

    
    private BroadcastReceiver mLoggedOutReceiver = new BroadcastReceiver() {
        @Override 
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive: " + intent);
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");
        setContentView(R.layout.friend_requests_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));
        
        mHandler = new Handler();

        mEditTextFilter = (EditText)findViewById(R.id.editTextFilter);
        mEditTextFilter.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Run the filter operation after a brief waiting period in case the user
                // is typing real fast.
                mHandler.removeCallbacks(mRunnableFilter);
                mHandler.postDelayed(mRunnableFilter, 700L);
            }
        });
        
        mListAdapter = new FriendRequestsAdapter(this, mButtonRowClickHandler,
                ((Foursquared) getApplication()).getRemoteResourceManager());
        getListView().setAdapter(mListAdapter);
        getListView().setItemsCanFocus(true);
        
        mTextViewNoRequests = (TextView)findViewById(R.id.textViewNoRequests);
 
        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
            mStateHolder.setActivityForTaskFriendRequests(this);
            mStateHolder.setActivityForTaskSendDecision(this);
            
            decideShowNoFriendRequestsTextView();
        } else {
            mStateHolder = new StateHolder();

            // Start searching for friend requests immediately on activity creation.
            startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                    getResources().getString(R.string.friend_requests_progress_bar_find_requests));
            mStateHolder.startTaskFriendRequests(FriendRequestsActivity.this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mStateHolder.getIsRunningFriendRequest()) {
            startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                    getResources().getString(R.string.friend_requests_progress_bar_find_requests));
        } else if (mStateHolder.getIsRunningApproval()) {
            startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                    getResources()
                            .getString(R.string.friend_requests_progress_bar_approve_request));
        } else if (mStateHolder.getIsRunningIgnore()) {
            startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                    getResources()
                            .getString(R.string.friend_requests_progress_bar_ignore_request));
        }

        mListAdapter.setGroup(mStateHolder.getFoundFriendsFiltered());
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopProgressBar();
        
        if (isFinishing()) {
            mListAdapter.removeObserver();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedOutReceiver);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        mStateHolder.setActivityForTaskFriendRequests(null);
        mStateHolder.setActivityForTaskSendDecision(null);
        return mStateHolder;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_REFRESH, Menu.NONE, R.string.refresh_label).setIcon(
                R.drawable.ic_menu_refresh);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_REFRESH:
                startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                        getResources().getString(R.string.friend_requests_progress_bar_find_requests));
                mStateHolder.setRanFetchOnce(false);
                mStateHolder.startTaskFriendRequests(FriendRequestsActivity.this);
                decideShowNoFriendRequestsTextView();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startProgressBar(String title, String message) {
        if (mDlgProgress == null) {
            mDlgProgress = ProgressDialog.show(this, title, message);
        }
        mDlgProgress.show();
    }

    private void stopProgressBar() {
        if (mDlgProgress != null) {
            mDlgProgress.dismiss();
            mDlgProgress = null;
        }
    }

    private void infoFriendRequest(User user) {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra(UserDetailsActivity.EXTRA_USER_PARCEL, user);
        startActivity(intent);
    }

    private void approveFriendRequest(User user) {
        startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                getResources().getString(R.string.friend_requests_progress_bar_approve_request));
        mStateHolder.startTaskSendDecision(FriendRequestsActivity.this, user.getId(), true);
    }

    private void denyFriendRequest(User user) {
        startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                getResources().getString(R.string.friend_requests_progress_bar_ignore_request));
        mStateHolder.startTaskSendDecision(FriendRequestsActivity.this, user.getId(), false);
    }

    private void decideShowNoFriendRequestsTextView() {
        if (mStateHolder.getRanFetchOnce() && 
            mStateHolder.getFoundFriendsCount() < 1) {
            mTextViewNoRequests.setVisibility(View.VISIBLE);
        }
        else {
            mTextViewNoRequests.setVisibility(View.GONE);
        }
    }
    
    private void onFriendRequestsTaskComplete(Group<User> users, HashMap<String, Group<User>> usersAlpha, Exception ex) {

        // Recreate the adapter, cleanup beforehand.
        mListAdapter.removeObserver();
        mListAdapter = new FriendRequestsAdapter(this, mButtonRowClickHandler,
                ((Foursquared) getApplication()).getRemoteResourceManager());

        try {
            // Populate the list control below now.
            if (users != null) {
                mStateHolder.setFoundFriends(users, usersAlpha);
                if (DEBUG) {
                    Log.e(TAG, "Alpha-sorted requests map:");
                    for (Map.Entry<String, Group<User>> it : usersAlpha.entrySet()) {
                        Log.e(TAG, it.getKey());
                        for (User jt : it.getValue()) {
                            Log.e(TAG, "   " + getUsersDisplayName(jt));
                        }
                    }
                }
            } else {
                // If error, feed list adapter empty user group.
                mStateHolder.setFoundFriends(null, null);
                NotificationsUtil.ToastReasonForFailure(FriendRequestsActivity.this, ex);
            }
            mListAdapter.setGroup(mStateHolder.getFoundFriendsFiltered());
        } finally {
            getListView().setAdapter(mListAdapter);
            mStateHolder.setIsRunningFriendRequest(false);
            mStateHolder.setRanFetchOnce(true);
            decideShowNoFriendRequestsTextView();
            stopProgressBar();
        }
    }

    private void onFriendRequestDecisionTaskComplete(User user, boolean isApproving, Exception ex) {
        try {
            // If sending the request was successful, then we need to remove
            // that user from the list adapter. We do a linear search to find the 
            // matching row.
            if (user != null) {
                mStateHolder.removeUser(user);
                mListAdapter.setGroup(mStateHolder.getFoundFriendsFiltered());
                mListAdapter.notifyDataSetChanged();

                // This should generate the message: "You're now friends with [name]!" if
                // the user chose to approve the request, otherwise we show no toast, just
                // remove from the list.
                if (isApproving) {
                    Toast.makeText(this,
                            getResources().getString(R.string.friend_requests_approved) + " " +
                            getUsersDisplayName(user) + "!",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                NotificationsUtil.ToastReasonForFailure(this, ex);
            }
        } finally {
            decideShowNoFriendRequestsTextView();
            mStateHolder.setIsRunningApprval(false);
            mStateHolder.setIsRunningIgnore(false);
            stopProgressBar();
        }
    }

    private static class GetFriendRequestsTask extends AsyncTask<Void, Void, Group<User>> {

        private FriendRequestsActivity mActivity;
        private Exception mReason;
        private HashMap<String, Group<User>> mRequestsAlpha;

        public GetFriendRequestsTask(FriendRequestsActivity activity) {
            mActivity = activity;
            mRequestsAlpha = new LinkedHashMap<String, Group<User>>();
        }

        public void setActivity(FriendRequestsActivity activity) {
            mActivity = activity;
        }

        @Override
        protected Group<User> doInBackground(Void... params) {
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();
                Group<User> requests = foursquare.friendRequests();
                
                for (User it : requests) {
                    String name = getUsersDisplayName(it).toUpperCase();
                    String first = name.substring(0, 1);
                    
                    Group<User> block = mRequestsAlpha.get(first);
                    if (block == null) {
                        block = new Group<User>();
                        mRequestsAlpha.put(first, block);
                    }
                    block.add(it);
                }

                return requests;
                
            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, "FindFriendsTask: Exception doing add friends by name", e);
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Group<User> users) {
            if (DEBUG) Log.d(TAG, "FindFriendsTask: onPostExecute()");
            if (mActivity != null) {
                mActivity.onFriendRequestsTaskComplete(users, mRequestsAlpha, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onFriendRequestsTaskComplete(null, null, new Exception(
                        "Friend search cancelled."));
            }
        }
    }

    private static class SendFriendRequestDecisionTask extends AsyncTask<Void, Void, User> {

        private FriendRequestsActivity mActivity;
        private boolean mIsApproving;
        private String mUserId;
        private Exception mReason;

        public SendFriendRequestDecisionTask(FriendRequestsActivity activity,
                                             String userId,
                                             boolean isApproving) {
            mActivity = activity;
            mUserId = userId;
            mIsApproving = isApproving;
        }

        public void setActivity(FriendRequestsActivity activity) {
            mActivity = activity;
        }

        @Override
        protected User doInBackground(Void... params) {
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();

                User user = null;
                if (mIsApproving) {
                    user = foursquare.friendApprove(mUserId);
                } else {
                    user = foursquare.friendDeny(mUserId);
                }
                return user;
            } catch (Exception e) {
                if (DEBUG)
                    Log.d(TAG, "SendFriendRequestTask: Exception doing send friend request.", e);
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            if (DEBUG) Log.d(TAG, "SendFriendRequestTask: onPostExecute()");
            if (mActivity != null) {
                mActivity.onFriendRequestDecisionTaskComplete(user, mIsApproving, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onFriendRequestDecisionTaskComplete(null, mIsApproving,
                        new Exception("Friend request cancelled."));
            }
        }
    }

    
    
    private static class StateHolder {
        GetFriendRequestsTask mTaskFriendRequests;
        SendFriendRequestDecisionTask mTaskSendDecision;
        boolean mIsRunningFriendRequests;
        boolean mIsRunningApproval;
        boolean mIsRunningIgnore;
        boolean mRanFetchOnce;
        private Group<User> mFoundFriends;
        private Group<User> mFoundFriendsFiltered;
        private HashMap<String, Group<User>> mFoundFriendsAlpha;

        public StateHolder() {
            mFoundFriends = new Group<User>();
            mFoundFriendsFiltered = null;
            mFoundFriendsAlpha = null;
            mIsRunningFriendRequests = false;
            mIsRunningApproval = false;
            mIsRunningIgnore = false;
            mRanFetchOnce = false;
        }

        public void startTaskFriendRequests(FriendRequestsActivity activity) {
            mIsRunningFriendRequests = true;
            mTaskFriendRequests = new GetFriendRequestsTask(activity);
            mTaskFriendRequests.execute();
        }

        public void startTaskSendDecision(FriendRequestsActivity activity, String userId,
                boolean approve) {
            mIsRunningApproval = approve;
            mIsRunningIgnore = !approve;
            mTaskSendDecision = new SendFriendRequestDecisionTask(activity, userId, approve);
            mTaskSendDecision.execute();
        }

        public void setActivityForTaskFriendRequests(FriendRequestsActivity activity) {
            if (mTaskFriendRequests != null) {
                mTaskFriendRequests.setActivity(activity);
            }
        }

        public void setActivityForTaskSendDecision(FriendRequestsActivity activity) {
            if (mTaskSendDecision != null) {
                mTaskSendDecision.setActivity(activity);
            }
        }

        public void setIsRunningFriendRequest(boolean isRunning) {
            mIsRunningFriendRequests = isRunning;
        }

        public boolean getIsRunningFriendRequest() {
            return mIsRunningFriendRequests;
        }

        public boolean getIsRunningApproval() {
            return mIsRunningApproval;
        }
        
        public void setIsRunningApprval(boolean isRunning) {
            mIsRunningApproval = isRunning;
        }

        public boolean getIsRunningIgnore() {
            return mIsRunningIgnore;
        }
        
        public void setIsRunningIgnore(boolean isRunning) {
            mIsRunningIgnore = isRunning;
        }

        public boolean getRanFetchOnce() {
            return mRanFetchOnce;
        }
        
        public void setRanFetchOnce(boolean ranFetchOnce) {
            mRanFetchOnce = ranFetchOnce;
        }
        
        public int getFoundFriendsCount() {
            return mFoundFriends.size();
        }
        
        public Group<User> getFoundFriendsFiltered() {
            if (mFoundFriendsFiltered == null) {
                return mFoundFriends;
            }
            return mFoundFriendsFiltered;
        }
        
        public void setFoundFriends(Group<User> requests, HashMap<String, Group<User>> alpha) {
            if (requests != null) {
                mFoundFriends = requests;
                mFoundFriendsFiltered = null;
                mFoundFriendsAlpha = alpha;
            } else {
                mFoundFriends = new Group<User>();
                mFoundFriendsFiltered = null;
                mFoundFriendsAlpha = null;
            }
        }
        
        public void filterFriendRequests(String filterString) {
            // If no filter, just keep using the original found friends group.
            // If a filter is supplied, reconstruct the group using the alpha
            // map so we don't have to go through the entire list.
            mFoundFriendsFiltered = null;
            if (!TextUtils.isEmpty(filterString)) {
                filterString = filterString.toUpperCase();
                
                Group<User> alpha = mFoundFriendsAlpha.get(filterString.substring(0, 1));
                mFoundFriendsFiltered = new Group<User>();
                if (alpha != null) {
                    for (User it : alpha) {
                        String name = getUsersDisplayName(it).toUpperCase();
                        if (name.startsWith(filterString)) {
                            mFoundFriendsFiltered.add(it);
                        }
                    }
                }
            }
        }
        
        public void removeUser(User user) {
            for (User it : mFoundFriends) {
                if (it.getId().equals(user.getId())) {
                    mFoundFriends.remove(it);
                    break;
                }
            }
            if (mFoundFriendsFiltered != null) {
                for (User it : mFoundFriendsFiltered) {
                    if (it.getId().equals(user.getId())) {
                        mFoundFriendsFiltered.remove(it);
                        break;
                    }
                }
            }
            
            String name = getUsersDisplayName(user).toUpperCase();
            String first = name.substring(0, 1);
            Group<User> alpha = mFoundFriendsAlpha.get(first);
            for (User it : alpha) {
                if (it.getId().equals(user.getId())) {
                    alpha.remove(it);
                    break;
                }
            }
        }
    }
    
    private static String getUsersDisplayName(User user) {
        StringBuilder sb = new StringBuilder(64);
        if (!TextUtils.isEmpty(user.getFirstname())) {
            sb.append(user.getFirstname());
            sb.append(" ");
        }
        if (!TextUtils.isEmpty(user.getLastname())) {
            sb.append(user.getLastname());
        }
        return sb.toString();
    }

    private FriendRequestsAdapter.ButtonRowClickHandler mButtonRowClickHandler = 
        new FriendRequestsAdapter.ButtonRowClickHandler() {
        
        @Override
        public void onBtnClickIgnore(User user) {
            denyFriendRequest(user);
        }

        @Override
        public void onBtnClickAdd(User user) {
            approveFriendRequest(user);
        }

        @Override
        public void onInfoAreaClick(User user) {
            infoFriendRequest(user);
        }
    };
    
    private Runnable mRunnableFilter = new Runnable() {
        public void run() {
            mStateHolder.filterFriendRequests(mEditTextFilter.getText().toString());
            mListAdapter.setGroup(mStateHolder.getFoundFriendsFiltered());
            mListAdapter.notifyDataSetChanged();
        }
    };
}
