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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
    private FriendRequestsAdapter mListAdapter;
    private TextView mTextViewNoRequests;

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

        mListAdapter.setGroup(mStateHolder.getFoundFriendRequests());
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
   // zebra     intent.putExtra(UserActivity.EXTRA_USER, user.getId());
     //   intent.setData(Uri.parse("http://foursquare.com/user/" + user.getId()));
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
            mStateHolder.getFoundFriendRequests().size() < 1) {
            mTextViewNoRequests.setVisibility(View.VISIBLE);
        }
        else {
            mTextViewNoRequests.setVisibility(View.GONE);
        }
    }
    
    private void onFriendRequestsTaskComplete(Group<User> users, Exception ex) {

        // Recreate the adapter, cleanup beforehand.
        mListAdapter.removeObserver();
        mListAdapter = new FriendRequestsAdapter(this, mButtonRowClickHandler,
                ((Foursquared) getApplication()).getRemoteResourceManager());

        try {
            // Populate the list control below now.
            if (users != null) {
                mStateHolder.setFoundFriendRequests(users);
                mListAdapter.setGroup(mStateHolder.getFoundFriendRequests());
            } else {
                // If error, feed list adapter empty user group.
                Group<User> usersNone = new Group<User>();
                mStateHolder.setFoundFriendRequests(usersNone);
                mListAdapter.setGroup(usersNone);
                NotificationsUtil.ToastReasonForFailure(FriendRequestsActivity.this, ex);
            }
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
            // that user from the
            // list adapter. We do a linear search to find the matching row.
            if (user != null) {
                int position = 0;
                for (User it : mStateHolder.getFoundFriendRequests()) {
                    if (it.getId().equals(user.getId())) {
                        mListAdapter.removeItem(position);
                        break;
                    }
                    position++;
                }

                // This should generate the message: "You're now friends with [name]!" if
                // the user chose to approve the request, otherwise we show no toast, just
                // remove from the list.
                if (isApproving) {
                    Toast.makeText(this,
                            getResources().getString(R.string.friend_requests_approved) + " " +
                            user.getFirstname() + "!",
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

        public GetFriendRequestsTask(FriendRequestsActivity activity) {
            mActivity = activity;
        }

        public void setActivity(FriendRequestsActivity activity) {
            mActivity = activity;
        }

        @Override
        protected Group<User> doInBackground(Void... params) {
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();

                return foursquare.friendRequests();
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
                mActivity.onFriendRequestsTaskComplete(users, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onFriendRequestsTaskComplete(null, new Exception(
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
        Group<User> mFoundFriends;
        boolean mIsRunningFriendRequests;
        boolean mIsRunningApproval;
        boolean mIsRunningIgnore;
        boolean mRanFetchOnce;

        public StateHolder() {
            mFoundFriends = new Group<User>();
            mIsRunningFriendRequests = false;
            mIsRunningApproval = false;
            mIsRunningIgnore = false;
            mRanFetchOnce = false;
        }

        public void setFoundFriendRequests(Group<User> foundFriends) {
            mFoundFriends = foundFriends;
        }

        public Group<User> getFoundFriendRequests() {
            return mFoundFriends;
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
}
