/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.util.AddressBookUtils;
import com.joelapenna.foursquared.util.NotificationsUtil;
import com.joelapenna.foursquared.widget.FriendSearchAddFriendAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

/**
 * Lets the user search for friends via first+last name, phone number, or
 * twitter names. Once a list of matching users is found, the user can click on
 * elements in the list to send a friend request to them. When the request is
 * successfully sent, that user is removed from the list. You can add the
 * INPUT_TYPE key to the intent while launching the activity to control what
 * type of friend search the activity will perform. Pass in one of the following
 * values:
 * <ul>
 * <li>INPUT_TYPE_USERNAMES</li>
 * <li>INPUT_TYPE_PHONENUMBERS</li>
 * <li>INPUT_TYPE_TWITTERNAME</li>
 * <li>INPUT_TYPE_ADDRESSBOOK</li>
 * </ul>
 * 
 * @date February 11, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public class AddFriendsByUserInputActivity extends Activity {
    private static final String TAG = "AddFriendsByUserInputActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String INPUT_TYPE = "com.joelapenna.foursquared.AddFriendsByUserInputActivity.INPUT_TYPE";
    public static final int INPUT_TYPE_NAME_OR_PHONE = 0;
    public static final int INPUT_TYPE_TWITTERNAME = 1;
    public static final int INPUT_TYPE_ADDRESSBOOK = 2;

    private TextView mTextViewInstructions;
    private TextView mTextViewAdditionalInstructions;
    private EditText mEditInput;
    private Button mBtnSearch;
    private TextView mTextViewMatches;
    private ListView mListView;
    private ProgressDialog mDlgProgress;

    private int mInputType;
    private FriendSearchAddFriendAdapter mListAdapter;

    private StateHolder mStateHolder;

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
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.add_friends_by_user_input_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        mTextViewInstructions = (TextView) findViewById(R.id.addFriendInstructionsTextView);
        mTextViewAdditionalInstructions = (TextView) findViewById(R.id.addFriendInstructionsAdditionalTextView);
        mEditInput = (EditText) findViewById(R.id.addFriendInputEditText);
        mBtnSearch = (Button) findViewById(R.id.addFriendSearchButton);
        mTextViewMatches = (TextView) findViewById(R.id.addFriendResultsMatchesTitleTextView);
        mListView = (ListView) findViewById(R.id.addFriendResultsListView);

        mListAdapter = new FriendSearchAddFriendAdapter(
            this,
            mButtonRowClickHandler,
            ((Foursquared)getApplication()).getRemoteResourceManager());
        mListView.setAdapter(mListAdapter);
        mListView.setItemsCanFocus(true);

        mBtnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch(mEditInput.getText().toString());
            }
        });
        mEditInput.addTextChangedListener(mNamesFieldWatcher);
        mEditInput.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL) {
                    startSearch(mEditInput.getText().toString());
                }
                return false;
            }
        });
        mBtnSearch.setEnabled(false);

        mInputType = getIntent().getIntExtra(INPUT_TYPE, INPUT_TYPE_NAME_OR_PHONE);
        switch (mInputType) {
            case INPUT_TYPE_TWITTERNAME:
                mTextViewInstructions.setText(getResources().getString(
                        R.string.add_friends_by_twitter_instructions));
                mEditInput.setHint(getResources().getString(R.string.add_friends_by_twitter_hint));
                mEditInput.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case INPUT_TYPE_ADDRESSBOOK:
                mTextViewInstructions.setText(getResources().getString(
                        R.string.add_friends_by_addressbook_instructions));
                mTextViewAdditionalInstructions.setText(getResources().getString(
                        R.string.add_friends_by_addressbook_additional_instructions));
                mEditInput.setVisibility(View.GONE);
                mBtnSearch.setVisibility(View.GONE);
                break;
            default:
                mTextViewInstructions.setText(getResources().getString(
                        R.string.add_friends_by_name_or_phone_instructions));
                mEditInput.setHint(getResources().getString(R.string.add_friends_by_name_or_phone_hint));
                mEditInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                break;
        }

        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
            mStateHolder.setActivityForTaskFindFriends(this);
            mStateHolder.setActivityForTaskFriendRequest(this);

            // If we have run before, restore matches divider.
            if (mStateHolder.getRanOnce()) {
                mTextViewMatches.setVisibility(View.VISIBLE);
            }
        } else {
            mStateHolder = new StateHolder();

            // If we are scanning the address book, we should kick it off
            // immediately.
            if (mInputType == INPUT_TYPE_ADDRESSBOOK) {
                startSearch("");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mStateHolder.getIsRunningTaskFindFriends()) {
            startProgressBar(getResources().getString(R.string.add_friends_activity_label),
                    getResources().getString(R.string.add_friends_progress_bar_message_find));
        } else if (mStateHolder.getIsRunningTaskSendFriendRequest()) {
            startProgressBar(getResources().getString(R.string.add_friends_activity_label),
                    getResources()
                            .getString(R.string.add_friends_progress_bar_message_send_request));
        }

        mListAdapter.setGroup(mStateHolder.getFoundFriends());
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
        mStateHolder.setActivityForTaskFindFriends(null);
        mStateHolder.setActivityForTaskFriendRequest(null);
        return mStateHolder;
    }

    private void userAdd(User user) {
        startProgressBar(getResources().getString(R.string.add_friends_activity_label),
                getResources().getString(R.string.add_friends_progress_bar_message_send_request));
        mStateHolder.startTaskSendFriendRequest(AddFriendsByUserInputActivity.this, user.getId());
    }

    private void userInfo(User user) {
        Intent intent = new Intent(AddFriendsByUserInputActivity.this, UserDetailsActivity.class);
   // zebra     intent.putExtra(UserActivity.EXTRA_USER, user.getId());
        //intent.setData(Uri.parse("http://foursquare.com/user/" + user.getId()));
        intent.putExtra(UserDetailsActivity.EXTRA_USER_PARCEL, user);
        startActivity(intent);
    }
    
    private void startSearch(String input) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditInput.getWindowToken(), 0);
    
        mEditInput.setEnabled(false);
        mBtnSearch.setEnabled(false);
        startProgressBar(getResources().getString(R.string.add_friends_activity_label),
                getResources().getString(R.string.add_friends_progress_bar_message_find));
        mStateHolder.startTaskFindFriends(AddFriendsByUserInputActivity.this, input);
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

    private void onFindFriendsTaskComplete(Group<User> users, Exception ex) {

        // Recreate the adapter, will also be necessary when we switch to a
        // SeparatedListAdapter for merging results between twitter/name/phone etc.
        mListAdapter.removeObserver();
        mListAdapter = new FriendSearchAddFriendAdapter(
            this,
            mButtonRowClickHandler,
            ((Foursquared)getApplication()).getRemoteResourceManager());
        
        try {
            // Populate the list adapter.
            if (users != null) {
                mStateHolder.setFoundFriends(users);
                mListAdapter.setGroup(mStateHolder.getFoundFriends());
                mTextViewMatches.setVisibility(View.VISIBLE);
                if (users.size() < 1) {
                    mTextViewMatches.setVisibility(View.GONE);
                    Toast.makeText(this, getResources().getString(R.string.add_friends_no_matches),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // If error, feed list adapter empty user group.
                mListAdapter.setGroup(new Group<User>());
                NotificationsUtil.ToastReasonForFailure(AddFriendsByUserInputActivity.this, ex);
            }
        } finally {
            mListView.setAdapter(mListAdapter);
            mEditInput.setEnabled(true);
            mBtnSearch.setEnabled(true);
            mStateHolder.setIsRunningTaskFindFriends(false);
            stopProgressBar();
        }
    }

    private void onSendFriendRequestTaskComplete(User friendRequestRecipient, Exception ex) {
        try {
            // If sending the request was successful, then we need to remove
            // that user from the
            // list adapter. We do a linear search to find the matching row.
            if (friendRequestRecipient != null) {
                int position = 0;
                for (User it : mStateHolder.getFoundFriends()) {
                    if (it.getId().equals(friendRequestRecipient.getId())) {
                        mListAdapter.removeItem(position);
                        break;
                    }
                    position++;
                }

                Toast.makeText(AddFriendsByUserInputActivity.this,
                        getResources().getString(R.string.add_friends_request_sent_ok),
                        Toast.LENGTH_SHORT).show();

            } else {
                // If error, feed adapter empty user group.
                mListAdapter.setGroup(new Group<User>());
                mListAdapter.notifyDataSetChanged();
                NotificationsUtil.ToastReasonForFailure(AddFriendsByUserInputActivity.this, ex);
            }
        } finally {
            mEditInput.setEnabled(true);
            mBtnSearch.setEnabled(true);
            mStateHolder.setIsRunningTaskSendFriendRequest(false);
            stopProgressBar();
        }
    }

    private static class FindFriendsTask extends AsyncTask<String, Void, Group<User>> {

        private AddFriendsByUserInputActivity mActivity;
        private Exception mReason;

        public FindFriendsTask(AddFriendsByUserInputActivity activity) {
            mActivity = activity;
        }

        public void setActivity(AddFriendsByUserInputActivity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            mActivity.startProgressBar(mActivity.getResources().getString(
                    R.string.add_friends_activity_label), mActivity.getResources().getString(
                    R.string.add_friends_progress_bar_message_find));
        }

        @Override
        protected Group<User> doInBackground(String... params) {
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();

                Group<User> users = null;
                switch (mActivity.mInputType) {
                    case INPUT_TYPE_TWITTERNAME:
                        users = foursquare.findFriendsByTwitter(params[0]);
                        break;
                    case INPUT_TYPE_ADDRESSBOOK:
                        AddressBookUtils addr = AddressBookUtils.addressBookUtils();
                        String addresses = addr.getAllContactsPhoneNumbers(mActivity);
                        if (addresses != null && addresses.length() > 0) {
                            users = foursquare.findFriendsByPhone(addresses);
                        } else {
                            // No contacts in their contacts book, just say no
                            // matches by supplying an empty group.
                            users = new Group<User>();
                        }
                        break;
                    default:
                        // Combine searches for name/phone, results returned in one list.
                        users = new Group<User>();
                        users.addAll(foursquare.findFriendsByPhone(params[0]));
                        users.addAll(foursquare.findFriendsByName(params[0]));
                        break;
                }
                return users;
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
                mActivity.onFindFriendsTaskComplete(users, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity
                        .onFindFriendsTaskComplete(null, new Exception("Friend search cancelled."));
            }
        }
    }

    private static class SendFriendRequestTask extends AsyncTask<String, Void, User> {

        private AddFriendsByUserInputActivity mActivity;
        private Exception mReason;

        public SendFriendRequestTask(AddFriendsByUserInputActivity activity) {
            mActivity = activity;
        }

        public void setActivity(AddFriendsByUserInputActivity activity) {
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

    private static class StateHolder {
        FindFriendsTask mTaskFindFriends;
        SendFriendRequestTask mTaskSendFriendRequest;
        Group<User> mFoundFriends;
        boolean mIsRunningTaskFindFriends;
        boolean mIsRunningTaskSendFriendRequest;
        boolean mRanOnce;

        public StateHolder() {
            mFoundFriends = new Group<User>();
            mIsRunningTaskFindFriends = false;
            mIsRunningTaskSendFriendRequest = false;
            mRanOnce = false;
        }

        public void setFoundFriends(Group<User> foundFriends) {
            mFoundFriends = foundFriends;
            mRanOnce = true;
        }

        public Group<User> getFoundFriends() {
            return mFoundFriends;
        }

        public void startTaskFindFriends(AddFriendsByUserInputActivity activity, String input) {
            mIsRunningTaskFindFriends = true;
            mTaskFindFriends = new FindFriendsTask(activity);
            mTaskFindFriends.execute(input);
        }

        public void startTaskSendFriendRequest(AddFriendsByUserInputActivity activity, String userId) {
            mIsRunningTaskSendFriendRequest = true;
            mTaskSendFriendRequest = new SendFriendRequestTask(activity);
            mTaskSendFriendRequest.execute(userId);
        }

        public void setActivityForTaskFindFriends(AddFriendsByUserInputActivity activity) {
            if (mTaskFindFriends != null) {
                mTaskFindFriends.setActivity(activity);
            }
        }

        public void setActivityForTaskFriendRequest(AddFriendsByUserInputActivity activity) {
            if (mTaskSendFriendRequest != null) {
                mTaskSendFriendRequest.setActivity(activity);
            }
        }

        public void setIsRunningTaskFindFriends(boolean isRunning) {
            mIsRunningTaskFindFriends = isRunning;
        }

        public void setIsRunningTaskSendFriendRequest(boolean isRunning) {
            mIsRunningTaskSendFriendRequest = isRunning;
        }

        public boolean getIsRunningTaskFindFriends() {
            return mIsRunningTaskFindFriends;
        }

        public boolean getIsRunningTaskSendFriendRequest() {
            return mIsRunningTaskSendFriendRequest;
        }

        public boolean getRanOnce() {
            return mRanOnce;
        }
    }

    private TextWatcher mNamesFieldWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mBtnSearch.setEnabled(!TextUtils.isEmpty(s));
        }
    };
    
    /** 
     * This handler will be called when the user clicks on buttons in one of the
     * listview's rows.
     */
    private FriendSearchAddFriendAdapter.ButtonRowClickHandler mButtonRowClickHandler = 
        new FriendSearchAddFriendAdapter.ButtonRowClickHandler() {
            @Override
            public void onBtnClickAdd(User user) {
                userAdd(user);
            }
    
            @Override
            public void onInfoAreaClick(User user) {
                userInfo(user);
            }
    };
}
