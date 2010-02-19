/**
 * Copyright 2009 Joe LaPenna
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

        mListAdapter = new FriendRequestsAdapter(this,
                new FriendRequestsAdapter.ButtonRowClickHandler() {
                    @Override
                    public void onBtnClickDeny(User user) {
                        denyFriendRequest(user);
                    }

                    @Override
                    public void onBtnClickApprove(User user) {
                        approveFriendRequest(user);
                    }

                    @Override
                    public void onBtnClickInfo(User user) {
                        infoFriendRequest(user);
                    }
                });
        getListView().setAdapter(mListAdapter);
        getListView().setItemsCanFocus(true);

        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
            mStateHolder.setActivityForTaskFriendRequests(this);
            mStateHolder.setActivityForTaskSendDecision(this);
        } else {
            mStateHolder = new StateHolder();

            // If we are scanning the address book, we should kick it off
            // immediately.
            startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                    getResources().getString(R.string.add_friends_progress_bar_message_find));
            mStateHolder.startTaskFriendRequests(FriendRequestsActivity.this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mStateHolder.getIsRunningFriendRequest()) {
            startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                    getResources().getString(R.string.add_friends_progress_bar_message_find));
        } else if (mStateHolder.getIsRunningSendDecision()) {
            startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                    getResources()
                            .getString(R.string.add_friends_progress_bar_message_send_request));
        }

        mListAdapter.setGroup(mStateHolder.getFoundFriendRequests());
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopProgressBar();
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
                        getResources().getString(R.string.add_friends_progress_bar_message_find));
                mStateHolder.startTaskFriendRequests(FriendRequestsActivity.this);
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
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra(UserActivity.EXTRA_USER, user.getId());
        intent.setData(Uri.parse("http://foursquare.com/user/" + user.getId()));
        startActivity(intent);
    }

    private void approveFriendRequest(User user) {
        startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                getResources().getString(R.string.add_friends_progress_bar_message_find));
        mStateHolder.startTaskSendDecision(FriendRequestsActivity.this, true, user.getId());
    }

    private void denyFriendRequest(User user) {
        startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                getResources().getString(R.string.add_friends_progress_bar_message_find));
        mStateHolder.startTaskSendDecision(FriendRequestsActivity.this, false, user.getId());
    }

    private void onFriendRequestsTaskComplete(Group<User> users, Exception ex) {
        try {
            // Populate the list control below now.
            if (users != null) {
                mStateHolder.setFoundFriendRequests(users);

                mListAdapter.setGroup(mStateHolder.getFoundFriendRequests());
                mListAdapter.notifyDataSetChanged();
            } else {
                // If error, feed list adapter empty user group.
                mListAdapter.setGroup(new Group<User>());
                mListAdapter.notifyDataSetChanged();
                NotificationsUtil.ToastReasonForFailure(FriendRequestsActivity.this, ex);
            }
        } finally {
            mStateHolder.setIsRunningFriendRequest(false);
            stopProgressBar();
        }
    }

    private void onFriendRequestDecisionTaskComplete(User user, Exception ex) {
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

                Toast.makeText(this,
                        getResources().getString(R.string.friend_requests_decision_sent_ok),
                        Toast.LENGTH_SHORT).show();

            } else {
                NotificationsUtil.ToastReasonForFailure(this, ex);
            }
        } finally {
            mStateHolder.setIsRunningSendDecision(false);
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
        protected void onPreExecute() {
            mActivity.startProgressBar(mActivity.getResources().getString(
                    R.string.add_friends_activity_label), mActivity.getResources().getString(
                    R.string.add_friends_progress_bar_message_find));
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

    private static class SendFriendRequestDecisionTask extends AsyncTask<String, Void, User> {

        private FriendRequestsActivity mActivity;
        private Exception mReason;

        public SendFriendRequestDecisionTask(FriendRequestsActivity activity) {
            mActivity = activity;
        }

        public void setActivity(FriendRequestsActivity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            mActivity.startProgressBar(mActivity.getResources().getString(
                    R.string.add_friends_activity_label), mActivity.getResources().getString(
                    R.string.add_friends_progress_bar_message_send_request));
        }

        @Override
        protected User doInBackground(String... params) {
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();

                User user = null;
                if (Boolean.parseBoolean(params[0]) == true) {
                    user = foursquare.friendApprove(params[1]);
                } else {
                    user = foursquare.friendDeny(params[1]);
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
                mActivity.onFriendRequestDecisionTaskComplete(user, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onFriendRequestDecisionTaskComplete(null, new Exception(
                        "Friend request cancelled."));
            }
        }
    }

    private static class StateHolder {
        GetFriendRequestsTask mTaskFriendRequests;
        SendFriendRequestDecisionTask mTaskSendDecision;
        Group<User> mFoundFriends;
        boolean mIsRunningFriendRequests;
        boolean mIsRunningSendDecision;

        public StateHolder() {
            mFoundFriends = new Group<User>();
            mIsRunningFriendRequests = false;
            mIsRunningSendDecision = false;
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

        public void startTaskSendDecision(FriendRequestsActivity activity, boolean approve,
                String userId) {
            mIsRunningSendDecision = true;
            mTaskSendDecision = new SendFriendRequestDecisionTask(activity);
            mTaskSendDecision.execute(String.valueOf(approve), userId);
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

        public void setIsRunningSendDecision(boolean isRunning) {
            mIsRunningSendDecision = isRunning;
        }

        public boolean getIsRunningSendDecision() {
            return mIsRunningSendDecision;
        }
    }
}
