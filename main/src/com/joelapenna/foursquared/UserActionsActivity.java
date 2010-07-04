/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import android.content.*;
import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Settings;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.app.LoadableListActivity;
import com.joelapenna.foursquared.util.CompatibilityHelp;
import com.joelapenna.foursquared.util.NotificationsUtil;
import com.joelapenna.foursquared.util.UserUtils;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Presents the user with a list of ways to interact with the supplied user. If
 * the user is a friend, the following list will be presented:
 * <ul>
 * <li>SMS</li>
 * <li>Email</li>
 * <li>Phone</li>
 * <li>Twitter</li>
 * <li>Facebook</li>
 * </ul>
 * If the user is not a friend, we add a different set of options:
 * <ul>
 * <li>Send Friend Request (if no pending request)</li>
 * <li>Approve Pending Request (if there is a pending request)</li>
 * <li>Just show pending request we've sent user, if any, read-only.</li>
 * <li>Twitter (public anyway so no harm in showing)</li>
 * <li>Facebook (public anyway so no harm in showing)</li>
 * </ul>
 * If we modify the friend relationship during this activity's lifetime, we can
 * reload in a new activity or try to dynamically change ourselves.
 * 
 * @date March 9, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class UserActionsActivity extends LoadableListActivity {
    static final String TAG = "UserActionsActivity";
    static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String EXTRA_USER_PARCEL = Foursquared.PACKAGE_NAME
            + ".UserActionsActivity.EXTRA_USER_PARCEL";

    public static final String EXTRA_SHOW_ADD_FRIEND_OPTIONS = Foursquared.PACKAGE_NAME
            + ".UserActionsActivity.EXTRA_SHOW_ADD_FRIEND_OPTIONS";

    private StateHolder mStateHolder;
    private ActionsAdapter mListAdapter;

    private ProgressDialog mDlgProgress;

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
            mStateHolder.setActivityForTaskFriendRequest(this);
            mStateHolder.setActivityForTaskSendDecision(this);
            mStateHolder.setActivityForTaskPings(this);
        } else {
            mStateHolder = new StateHolder();
            if (verifyIntent(getIntent())) {
                User user = getIntent().getExtras().getParcelable(EXTRA_USER_PARCEL);
                mStateHolder.setUser(user);
                mStateHolder.setShowAddFriendOptions(getIntent().getBooleanExtra(
                        EXTRA_SHOW_ADD_FRIEND_OPTIONS, false));
            } else {
                Log.e(TAG, "UserActionsFriendActivity requires a user pareclable in its intent extras.");
                finish();
                return;
            }
        }

        ensureUi();
    }

    private boolean verifyIntent(Intent intent) {
        if (intent == null || intent.getExtras() == null
                || intent.getExtras().containsKey(EXTRA_USER_PARCEL) == false) {
            return false;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isFinishing()) {
            mStateHolder.cancelTasks();
            unregisterReceiver(mLoggedOutReceiver);
            stopProgressBar();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        mStateHolder.setActivityForTaskFriendRequest(null);
        mStateHolder.setActivityForTaskSendDecision(null);
        return mStateHolder;
    }

    private void ensureUi() {
        mListAdapter = new ActionsAdapter(this, mStateHolder.getUser(), mStateHolder
                .getShowAddFriendOptions());

        ListView listView = getListView();
        listView.setAdapter(mListAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Action action = (Action) mListAdapter.getItem(position);
                switch (action.getActionId()) {
                    case ActionsAdapter.ACTION_ID_SMS:
                        startSmsIntent(mStateHolder.getUser().getPhone());
                        break;
                    case ActionsAdapter.ACTION_ID_EMAIL:
                        startEmailIntent(mStateHolder.getUser().getEmail());
                        break;
                    case ActionsAdapter.ACTION_ID_PHONE:
                        startDialer(mStateHolder.getUser().getPhone());
                        break;
                    case ActionsAdapter.ACTION_ID_TWITTER:
                        startWebIntent("http://www.twitter.com/"
                                + mStateHolder.getUser().getTwitter());
                        break;
                    case ActionsAdapter.ACTION_ID_FACEBOOK:
                        startWebIntent("http://www.facebook.com/profile.php?id="
                                + mStateHolder.getUser().getFacebook());
                        break;
                    case ActionsAdapter.ACTION_ID_LAST_SEEN_AT:
                        Intent intent = new Intent(UserActionsActivity.this, VenueActivity.class);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.putExtra(Foursquared.EXTRA_VENUE_ID, 
                            mStateHolder.getUser().getCheckin().getVenue().getId());
                        startActivity(intent);
                        break;
                    case ActionsAdapter.ACTION_ID_SEND_FRIEND_REQUEST:
                        mStateHolder.startTaskSendFriendRequest(UserActionsActivity.this,
                                mStateHolder.getUser().getId());
                        break;
                    case ActionsAdapter.ACTION_ID_SEND_APPROVE_FRIEND_REQUEST:
                        mStateHolder.startTaskSendDecision(UserActionsActivity.this, mStateHolder
                                .getUser().getId(), true);
                        break;
                    case ActionsAdapter.ACTION_ID_SEND_READONLY_FRIEND_REQUEST:
                        // Nothing to do, we have to wait for the other user to
                        // accept our invitation!
                        break;
                    case ActionsAdapter.ACTION_ID_PINGS_ON:
                        mStateHolder.startTaskPings(UserActionsActivity.this, mStateHolder
                                .getUser().getId(), false);
                        break;
                    case ActionsAdapter.ACTION_ID_PINGS_OFF:
                        mStateHolder.startTaskPings(UserActionsActivity.this, mStateHolder
                                .getUser().getId(), true);
                        break;
                    case ActionsAdapter.ACTION_ID_CONTACTS:
                        if (mListAdapter.contactIntent != null) {
                            startActivity(mListAdapter.contactIntent);
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        if (mStateHolder.getIsRunningApproval()) {
            startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                    getResources().getString(R.string.friend_requests_progress_bar_approve_request));
        } else if (mStateHolder.getIsRunningIgnore()) {
            startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                    getResources().getString(R.string.friend_requests_progress_bar_ignore_request));
        } else if (mStateHolder.getIsRunningTaskSendFriendRequest()) {
            startProgressBar(getResources().getString(R.string.add_friends_activity_label),
                    getResources()
                            .getString(R.string.add_friends_progress_bar_message_send_request));
        } else if (mStateHolder.getIsRunningPings()) {
            startProgressBar(getResources().getString(R.string.friend_requests_activity_label),
                    getResources().getString(R.string.friend_requests_progress_bar_ignore_request));
        }

        if (mListAdapter.getCount() == 0) {
            setEmptyView();
        }
    }

    @Override
    public int getNoSearchResultsStringId() {
        return R.string.user_actions_activity_no_info;
    }

    /**
     * This adapter doesn't implement a holder because we have so few rows, we
     * can improve this in subsequent versions if necessary.
     */
    private static class ActionsAdapter extends BaseAdapter {
        public static final int ACTION_ID_SMS = 0;
        public static final int ACTION_ID_EMAIL = 1;
        public static final int ACTION_ID_PHONE = 2;
        public static final int ACTION_ID_TWITTER = 3;
        public static final int ACTION_ID_FACEBOOK = 4;
        public static final int ACTION_ID_LAST_SEEN_AT = 5; // Going to go away eventually.
        public static final int ACTION_ID_PINGS_ON = 6;
        public static final int ACTION_ID_PINGS_OFF = 7;
        public static final int ACTION_ID_CONTACTS = 8;
        public static final int ACTION_ID_SEND_FRIEND_REQUEST = 100;
        public static final int ACTION_ID_SEND_APPROVE_FRIEND_REQUEST = 101;
        public static final int ACTION_ID_SEND_READONLY_FRIEND_REQUEST = 102;

        private LayoutInflater mInflater;
        private int mLayoutToInflate;
        private User mUser;
        private ArrayList<Action> mActions;
        private Intent contactIntent;

        public ActionsAdapter(Context context, User user, boolean showAddFriendOptions) {
            super();
            mInflater = LayoutInflater.from(context);
            mLayoutToInflate = R.layout.user_actions_list_item;
            mUser = user;

            mActions = new ArrayList<Action>();
            if (user != null) {
                if (user.getCheckin() != null && user.getCheckin().getVenue() != null) {
                    mActions.add(new Action(
                            "Last seen at " + user.getCheckin().getVenue().getName(),
                            R.drawable.map_marker_blue, ACTION_ID_LAST_SEEN_AT, false));
                }
                if (UserUtils.isFriend(user)) {
                    if (Boolean.parseBoolean(mUser.getSettings().getGetPings())) {
                        mActions.add(new Action(context.getResources().getString(
                                R.string.user_actions_activity_action_pings_on),
                                R.drawable.user_action_pings, ACTION_ID_PINGS_ON, false));
                    } else {
                        mActions.add(new Action(context.getResources().getString(
                                R.string.user_actions_activity_action_pings_off),
                                R.drawable.user_action_pings, ACTION_ID_PINGS_OFF, false));
                    }
                    if (TextUtils.isEmpty(mUser.getPhone()) == false) {
                        mActions.add(new Action(context.getResources().getString(
                                R.string.user_actions_activity_action_sms),
                                R.drawable.user_action_text, ACTION_ID_SMS, false));
                    }
                    if (TextUtils.isEmpty(mUser.getEmail()) == false) {
                        mActions.add(new Action(context.getResources().getString(
                                R.string.user_actions_activity_action_email),
                                R.drawable.user_action_email, ACTION_ID_EMAIL, false));
                    }
                    if (TextUtils.isEmpty(mUser.getEmail()) == false) {
                        mActions.add(new Action(context.getResources().getString(
                                R.string.user_actions_activity_action_phone),
                                R.drawable.user_action_phone, ACTION_ID_PHONE, false));
                    }
                } else if (showAddFriendOptions) {
                    // Not a friend, but show add friend options?
                    if (TextUtils.isEmpty(user.getFriendstatus())) {
                        // No friend relationship at all.
                        mActions.add(new Action(context.getResources().getString(
                                R.string.user_actions_activity_action_send_friend_request),
                                R.drawable.user_action_add_friend, ACTION_ID_SEND_FRIEND_REQUEST,
                                false));
                    } else if (user.getFriendstatus().equals("pendingyou")) {
                        mActions
                                .add(new Action(
                                        context
                                                .getResources()
                                                .getString(
                                                        R.string.user_actions_activity_action_approve_pending_friend_request),
                                        R.drawable.user_action_friend_pending,
                                        ACTION_ID_SEND_APPROVE_FRIEND_REQUEST, false));
                    } else if (user.getFriendstatus().equals("pendingthem")) {
                        mActions.add(new Action(context.getResources().getString(
                                R.string.user_actions_activity_action_readonly_friend_request),
                                R.drawable.user_action_friend_pending,
                                ACTION_ID_SEND_READONLY_FRIEND_REQUEST, false));
                    }
                }

                if (TextUtils.isEmpty(mUser.getTwitter()) == false) {
                    mActions.add(new Action(context.getResources().getString(
                            R.string.user_actions_activity_action_twitter),
                            R.drawable.user_action_twitter, ACTION_ID_TWITTER, true));
                }
                if (TextUtils.isEmpty(mUser.getFacebook()) == false) {
                    mActions.add(new Action(context.getResources().getString(
                            R.string.user_actions_activity_action_facebook),
                            R.drawable.user_action_facebook, ACTION_ID_FACEBOOK, true));
                }
                if ( CompatibilityHelp.API_LEVEL_AT_LEAST_ECLAIR) {
                    try {
                        Method getViewContactIntent = Class.forName("com.joelapenna.foursquared.Sync").getDeclaredMethod("getViewContactIntent", ContentResolver.class, User.class);
                        contactIntent = (Intent) getViewContactIntent.invoke(null, context.getContentResolver(), mUser);
                        if ( contactIntent != null ) {
                            mActions.add(new Action(context.getResources().getString(R.string.user_actions_activity_contacts),
                                 android.R.drawable.sym_contact_card, ACTION_ID_CONTACTS, true));
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(mLayoutToInflate, null);
            }

            ImageView iv = (ImageView) convertView.findViewById(R.id.userActionsListItemIcon);
            TextView tv = (TextView) convertView.findViewById(R.id.userActionsListItemLabel);
            ImageView ivExt = (ImageView) convertView
                    .findViewById(R.id.userActionsListItemDisclosure);

            Action action = (Action) getItem(position);
            iv.setImageResource(action.getIconId());
            tv.setText(action.getLabel());

            if (action.getIsExternalAction()) {
                ivExt.setVisibility(View.VISIBLE);
            } else {
                ivExt.setVisibility(View.GONE);
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return mActions.size();
        }

        @Override
        public Object getItem(int position) {
            return mActions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private static class Action {
        private String mLabel;
        private int mIconId;
        private int mActionId;
        private boolean mIsExternalAction;

        public Action(String label, int iconId, int actionId, boolean isExternalAction) {
            mLabel = label;
            mIconId = iconId;
            mActionId = actionId;
            mIsExternalAction = isExternalAction;
        }

        public String getLabel() {
            return mLabel;
        }

        public int getIconId() {
            return mIconId;
        }

        public int getActionId() {
            return mActionId;
        }

        public boolean getIsExternalAction() {
            return mIsExternalAction;
        }
    }

    private void startDialer(String phoneNumber) {
        try {
            Intent dial = new Intent();
            dial.setAction(Intent.ACTION_DIAL);
            dial.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(dial);
        } catch (Exception ex) {
            Log.e(TAG, "Error starting phone dialer intent.", ex);
            Toast.makeText(this, "Sorry, we couldn't find any app to place a phone call!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void startSmsIntent(String phoneNumber) {
        try {
            Uri uri = Uri.parse("sms:" + phoneNumber);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra("address", phoneNumber);
            intent.setType("vnd.android-dir/mms-sms");
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, "Error starting sms intent.", ex);
            Toast.makeText(this, "Sorry, we couldn't find any app to send an SMS!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void startEmailIntent(String emailAddress) {
        try {
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {
                emailAddress
            });
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, "Error starting email intent.", ex);
            Toast.makeText(this, "Sorry, we couldn't find any app for sending emails!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void startWebIntent(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, "Error starting url intent.", ex);
            Toast.makeText(this, "Sorry, we couldn't find any app for viewing this url!",
                    Toast.LENGTH_SHORT).show();
        }
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

    private void onFriendRequestDecisionTaskComplete(User user, boolean isApproving, Exception ex) {
        stopProgressBar();
        mStateHolder.setIsRunningApprval(false);
        mStateHolder.setIsRunningIgnore(false);
        if (user != null) {
            // The returned user object won't contain any updated friend status string in it,
            // so we just modify our own user object to have a friend status of "friends" now.
            // This will have the effect of only still showing public info immediately, the
            // user will have to reload this activity to see the new friend's phone # and all
            // that stuff, will improve in subsequent versions.
            // TODO: Update reload of user actions after approve friend status.
            mStateHolder.getUser().setFriendstatus("friend");
            ensureUi();
        } else {
            // If we failed, friend status remains unchanged.
            NotificationsUtil.ToastReasonForFailure(this, ex);
        }
    }

    private void onSendFriendRequestTaskComplete(User user, Exception ex) {
        stopProgressBar();
        mStateHolder.setIsRunningTaskSendFriendRequest(false);
        if (user != null) {
            // Modify our user's friend status string to reflect the fact that we 
            // have a pending friend request sent to them. Then we recreate the
            // list adapter which will see this new friend state and change the
            // friend relationship item for us. Kinda ugly but works for now.
            mStateHolder.getUser().setFriendstatus("pendingthem");
            ensureUi();
        } else {
            // If we failed, friend status remains unchanged.
            NotificationsUtil.ToastReasonForFailure(this, ex);
        }
    }

    private void onPingsTaskComplete(Settings settings, boolean on, Exception ex) {
        stopProgressBar();
        mStateHolder.setIsRunningPings(false);
        if (settings != null) {
            mStateHolder.getUser().getSettings().setGetPings(on ? "true" : "false");
            ensureUi();
        } else {
            NotificationsUtil.ToastReasonForFailure(this, ex);
        }
    }
    
    private static class SendFriendRequestDecisionTask extends AsyncTask<Void, Void, User> {

        private UserActionsActivity mActivity;
        private boolean mIsApproving;
        private String mUserId;
        private Exception mReason;

        public SendFriendRequestDecisionTask(UserActionsActivity activity, String userId,
                boolean isApproving) {
            mActivity = activity;
            mUserId = userId;
            mIsApproving = isApproving;
        }

        public void setActivity(UserActionsActivity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            if (mIsApproving) {
                mActivity.startProgressBar(mActivity.getResources().getString(
                        R.string.friend_requests_activity_label), mActivity.getResources()
                        .getString(R.string.friend_requests_progress_bar_approve_request));
            } else {
                mActivity.startProgressBar(mActivity.getResources().getString(
                        R.string.friend_requests_activity_label), mActivity.getResources()
                        .getString(R.string.friend_requests_progress_bar_ignore_request));
            }
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
                mActivity.onFriendRequestDecisionTaskComplete(null, mIsApproving, new Exception(
                        "Friend request cancelled."));
            }
        }
    }

    private static class SendFriendRequestTask extends AsyncTask<String, Void, User> {

        private UserActionsActivity mActivity;
        private Exception mReason;

        public SendFriendRequestTask(UserActionsActivity activity) {
            mActivity = activity;
        }

        public void setActivity(UserActionsActivity activity) {
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

                User user = foursquare.friendSendrequest(params[0]);
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
                mActivity.onSendFriendRequestTaskComplete(user, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onSendFriendRequestTaskComplete(null, new Exception(
                        "Friend invitation cancelled."));
            }
        }
    }
    
    private static class PingsTask extends AsyncTask<Void, Void, Settings> {

        private UserActionsActivity mActivity;
        private Exception mReason;
        private String mUserId;
        private boolean mOn;

        public PingsTask(UserActionsActivity activity, String userId, boolean on) {
            mActivity = activity;
            mUserId = userId;
            mOn = on;
        }

        public void setActivity(UserActionsActivity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            mActivity.startProgressBar(mActivity.getResources().getString(
                    R.string.add_friends_progress_bar_title_pings), mActivity.getResources().getString(
                    R.string.add_friends_progress_bar_message_pings));
        }

        @Override
        protected Settings doInBackground(Void... params) {
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();

                Settings settings = foursquare.setpings(mUserId, mOn);
                return settings;
            } catch (Exception e) {
                if (DEBUG)
                    Log.d(TAG, "PingsTask: Exception setting new ping status.", e);
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Settings settings) {
            if (DEBUG) Log.d(TAG, "PingsTask: onPostExecute()");
            if (mActivity != null) {
                mActivity.onPingsTaskComplete(settings, mOn, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onSendFriendRequestTaskComplete(null, new Exception(
                        "Pings update cancelled."));
            }
        }
    }

    private static class StateHolder {

        private User mUser;
        private boolean mShowAddFriendOptions;

        private SendFriendRequestTask mTaskSendFriendRequest;
        private boolean mIsRunningTaskSendFriendRequest;

        private SendFriendRequestDecisionTask mTaskSendDecision;
        private boolean mIsRunningApproval;
        private boolean mIsRunningIgnore;
        
        private PingsTask mTaskPings;
        private boolean mIsRunningTaskPings;

        
        public StateHolder() {
            mShowAddFriendOptions = false;
            mIsRunningTaskSendFriendRequest = false;
            mIsRunningApproval = false;
            mIsRunningIgnore = false;
            mIsRunningTaskPings = false;
        }

        public User getUser() {
            return mUser;
        }

        public void setUser(User user) {
            mUser = user;
        }

        public void setShowAddFriendOptions(boolean showAddFriendOptions) {
            mShowAddFriendOptions = showAddFriendOptions;
        }

        public boolean getShowAddFriendOptions() {
            return mShowAddFriendOptions;
        }

        public void startTaskSendFriendRequest(UserActionsActivity activity, String userId) {
            mIsRunningTaskSendFriendRequest = true;
            mTaskSendFriendRequest = new SendFriendRequestTask(activity);
            mTaskSendFriendRequest.execute(userId);
        }

        public void startTaskSendDecision(UserActionsActivity activity, String userId,
                boolean approve) {
            mIsRunningApproval = approve;
            mIsRunningIgnore = !approve;
            mTaskSendDecision = new SendFriendRequestDecisionTask(activity, userId, approve);
            mTaskSendDecision.execute();
        }
        
        public void startTaskPings(UserActionsActivity activity, String userId, boolean on) {
            mIsRunningTaskPings = true;
            mTaskPings = new PingsTask(activity, userId, on);
            mTaskPings.execute();
        }

        public void setActivityForTaskFriendRequest(UserActionsActivity activity) {
            if (mTaskSendFriendRequest != null) {
                mTaskSendFriendRequest.setActivity(activity);
            }
        }

        public void setActivityForTaskSendDecision(UserActionsActivity activity) {
            if (mTaskSendDecision != null) {
                mTaskSendDecision.setActivity(activity);
            }
        }

        public void setActivityForTaskPings(UserActionsActivity activity) {
            if (mTaskPings != null) {
                mTaskPings.setActivity(activity);
            }
        }
        
        public void setIsRunningTaskSendFriendRequest(boolean isRunning) {
            mIsRunningTaskSendFriendRequest = isRunning;
        }

        public boolean getIsRunningTaskSendFriendRequest() {
            return mIsRunningTaskSendFriendRequest;
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

        public boolean getIsRunningPings() {
            return mIsRunningTaskPings;
        }

        public void setIsRunningPings(boolean isRunning) {
            mIsRunningTaskPings = isRunning;
        }
        
        public void cancelTasks() {
            if (mTaskSendFriendRequest != null) {
                mTaskSendFriendRequest.setActivity(null);
                mTaskSendFriendRequest.cancel(true);
            }
            if (mTaskSendDecision != null) {
                mTaskSendDecision.setActivity(null);
                mTaskSendDecision.cancel(true);
            }
            if (mTaskPings != null) {
                mTaskPings.setActivity(null);
                mTaskPings.cancel(true);
            }
        }
    }
}
