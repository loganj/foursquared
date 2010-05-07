/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.util.MenuUtils;
import com.joelapenna.foursquared.util.NotificationsUtil;
import com.joelapenna.foursquared.util.RemoteResourceManager;
import com.joelapenna.foursquared.util.StringFormatters;
import com.joelapenna.foursquared.util.TabsUtil;
import com.joelapenna.foursquared.util.UserUtils;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * Displays information on a user. If the user is the logged-in user, we can
 * show our checkin history. If viewing a stranger, we can show info and
 * friends. Should look like this: Self History | Friends Stranger Info |
 * Friends
 * 
 * @date March 8, 2010.
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class UserDetailsActivity extends TabActivity {
    private static final String TAG = "UserDetailsActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String EXTRA_USER_PARCEL = "com.joelapenna.foursquared.UserParcel";
    public static final String EXTRA_USER_ID = "com.joelapenna.foursquared.UserId";

    public static final String EXTRA_SHOW_ADD_FRIEND_OPTIONS = Foursquared.PACKAGE_NAME
            + ".UserDetailsActivity.EXTRA_SHOW_ADD_FRIEND_OPTIONS";
    
    private static final int MENU_FRIEND_REQUESTS    = 0;
    private static final int MENU_SHOUT              = 1;

    
    private ImageView mImageViewPhoto;
    private TextView mTextViewName;
    private LinearLayout mLayoutNumMayorships;
    private LinearLayout mLayoutNumBadges;
    private TextView mTextViewNumMayorships;
    private TextView mTextViewNumBadges;
    private TabHost mTabHost;
    private LinearLayout mLayoutProgressBar;

    private StateHolder mStateHolder;
    private boolean mIsUsersPhotoSet;
    
    private RemoteResourceManager mRrm;
    private RemoteResourceManagerObserver mResourcesObserver;
    

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
        setContentView(R.layout.user_details_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        mIsUsersPhotoSet = false;

        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
            mStateHolder.setActivityForTaskUserDetails(this);
        } else {

            mStateHolder = new StateHolder();

            String userId = null;
            if (getIntent().getExtras() != null) {
                if (getIntent().getExtras().containsKey(EXTRA_USER_PARCEL)) {
                    User user = getIntent().getExtras().getParcelable(EXTRA_USER_PARCEL);
                    userId = user.getId();
                    mStateHolder.setUser(user);
                } else if (getIntent().getExtras().containsKey(EXTRA_USER_ID)) {
                    userId = getIntent().getExtras().getString(EXTRA_USER_ID);
                } else {
                    Log.e(TAG, "UserDetailsActivity requires a userid in its intent extras.");
                    finish();
                    return;
                }

                mStateHolder.setShowAddFriendOptions(getIntent().getBooleanExtra(
                        EXTRA_SHOW_ADD_FRIEND_OPTIONS, false));
            } else {
                Log.e(TAG, "UserDetailsActivity requires a userid in its intent extras.");
                finish();
                return;
            }

            mStateHolder.startTaskUserDetails(this, userId);
        }
        
        mRrm = ((Foursquared) getApplication()).getRemoteResourceManager();
        mResourcesObserver = new RemoteResourceManagerObserver();
        mRrm.addObserver(mResourcesObserver);

        ensureUi();
        populateUi();

        if (mStateHolder.getIsRunningUserDetailsTask() == false) {
            populateUiAfterFullUserObjectFetched();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isFinishing()) {
            mStateHolder.cancelTasks();
            unregisterReceiver(mLoggedOutReceiver);

            RemoteResourceManager rrm = ((Foursquared) getApplication()).getRemoteResourceManager();
            rrm.deleteObserver(mResourcesObserver);
        }
    }

    private void ensureUi() {
        mImageViewPhoto = (ImageView) findViewById(R.id.userDetailsActivityPhoto);
        mImageViewPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStateHolder.getUser() != null) {
                    // If "_thumbs" exists, remove it to get the url of the
                    // full-size image.
                    String photoUrl = mStateHolder.getUser().getPhoto().replace("_thumbs", "");

                    Intent intent = new Intent();
                    intent.setClass(UserDetailsActivity.this, FetchImageForViewIntent.class);
                    intent.putExtra(FetchImageForViewIntent.IMAGE_URL, photoUrl);
                    intent.putExtra(FetchImageForViewIntent.PROGRESS_BAR_TITLE, getResources()
                            .getString(R.string.user_activity_fetch_full_image_title));
                    intent.putExtra(FetchImageForViewIntent.PROGRESS_BAR_MESSAGE, getResources()
                            .getString(R.string.user_activity_fetch_full_image_message));
                    startActivity(intent);
                }
            }
        });

        mTextViewName = (TextView) findViewById(R.id.userDetailsActivityName);
        mTextViewNumMayorships = (TextView) findViewById(R.id.userDetailsActivityNumMayorships);
        mTextViewNumBadges = (TextView) findViewById(R.id.userDetailsActivityNumBadges);

        // When the user clicks the mayorships section, then launch the mayorships activity.
        mLayoutNumMayorships = (LinearLayout) findViewById(R.id.userDetailsActivityNumMayorshipsLayout);
        mLayoutNumMayorships.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startMayorshipsActivity();
            }
        });
        mLayoutNumMayorships.setEnabled(false);
        
        // When the user clicks the badges section, then launch the badges
        // activity.
        mLayoutNumBadges = (LinearLayout) findViewById(R.id.userDetailsActivityNumBadgesLayout);
        mLayoutNumBadges.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startBadgesActivity();
            }
        });
        mLayoutNumBadges.setEnabled(false);

        // At startup, we need to have at least one tab. Once we load the full
        // user object,
        // we can clear all tabs, and add our real tabs once we know what they
        // are.
        mTabHost = getTabHost();
        mTabHost.addTab(mTabHost.newTabSpec("dummy").setIndicator("").setContent(
                R.id.userDetailsActivityTextViewTabDummy));
        mTabHost.getTabWidget().setVisibility(View.GONE);

        mLayoutProgressBar = (LinearLayout) findViewById(R.id.userDetailsActivityLayoutProgressBar);
    }

    private void populateUi() {
        User user = mStateHolder.getUser();

        // User photo.
        if (user != null && mIsUsersPhotoSet == false) {
            if (Foursquare.MALE.equals(user.getGender())) {
                mImageViewPhoto.setImageResource(R.drawable.blank_boy);
            } else {
                mImageViewPhoto.setImageResource(R.drawable.blank_girl);
            }
            if (user != null) {
                Uri uriPhoto = Uri.parse(user.getPhoto());
                if (mRrm.exists(uriPhoto)) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(mRrm.getInputStream(Uri.parse(user
                                .getPhoto())));
                        mImageViewPhoto.setImageBitmap(bitmap);
                        mIsUsersPhotoSet = true;
                    } catch (IOException e) {
                    }
                } else {
                    mRrm.request(uriPhoto);
                }
            }
        }

        // User name.
        if (user != null) {
            if (UserUtils.isFriend(user)
                    || user.getId().equals(((Foursquared) getApplication()).getUserId())) {
                mTextViewName.setText(StringFormatters.getUserFullName(user));
            } else {
                mTextViewName.setText(StringFormatters.getUserAbbreviatedName(user));
            }
        } else {
            mTextViewName.setText("");
        }

        // Number of mayorships.
        if (user != null) {
            if (mStateHolder.getFetchedUserDetails()) {
                mTextViewNumMayorships.setText(String.valueOf(user.getMayorships().size()));
            } else {
                mTextViewNumMayorships.setText("-");
            }
        } else {
            mTextViewNumMayorships.setText("-");
        }

        // Number of badges.
        if (user != null) {
            if (mStateHolder.getFetchedUserDetails()) {
                mTextViewNumBadges.setText(String.valueOf(user.getBadges().size()));
            } else {
                mTextViewNumBadges.setText("-");
            }
        } else {
            mTextViewNumBadges.setText("-");
        }
    }

    private void populateUiAfterFullUserObjectFetched() {
        populateUi();

        mLayoutProgressBar.setVisibility(View.GONE);

        mTabHost.clearAllTabs();
        mTabHost.getTabWidget().setVisibility(View.VISIBLE);

        // Add tab1.
        TabHost.TabSpec specTab1 = mTabHost.newTabSpec("tab1");
        if (mStateHolder.getUser().getId().equals(((Foursquared) getApplication()).getUserId())) {
            // Ourselves, History tab.
            View tabView = prepareTabView(getResources().getString(
                    R.string.user_details_activity_tab_title_history));
            TabsUtil.setTabIndicator(specTab1, getResources().getString(
                    R.string.user_details_activity_tab_title_history), null, tabView);

            Intent intent = new Intent(this, UserHistoryActivity.class);
            specTab1.setContent(intent);
        } else {
            // Friend or stranger, Info tab.
            View tabView = prepareTabView(getResources().getString(
                    R.string.user_details_activity_tab_title_info));
            TabsUtil.setTabIndicator(specTab1, getResources().getString(
                    R.string.user_details_activity_tab_title_info), null, tabView);

            Intent intent = new Intent(this, UserActionsActivity.class);
            intent.putExtra(UserActionsActivity.EXTRA_USER_PARCEL, mStateHolder.getUser());
            intent.putExtra(UserActionsActivity.EXTRA_SHOW_ADD_FRIEND_OPTIONS, mStateHolder
                    .getShowAddFriendOptions());
            specTab1.setContent(intent);
        }
        mTabHost.addTab(specTab1);

        // Add tab2, always Friends tab.
        TabHost.TabSpec specTab2 = mTabHost.newTabSpec("tab2");
        View tabView = prepareTabView(getResources().getString(
                R.string.user_details_activity_tab_title_friends));
        TabsUtil.setTabIndicator(specTab2, getResources().getString(
                R.string.user_details_activity_tab_title_friends), null, tabView);

        Intent intent = new Intent(this, UserFriendsActivity.class);
        intent.putExtra(UserFriendsActivity.EXTRA_USER_ID, mStateHolder.getUser().getId());
        intent.putExtra(UserFriendsActivity.EXTRA_SHOW_ADD_FRIEND_OPTIONS, mStateHolder
                .getShowAddFriendOptions());
        specTab2.setContent(intent);
        mTabHost.addTab(specTab2);

        // User can also now click on the badges / mayorships layouts.
        mLayoutNumBadges.setEnabled(true);
        mLayoutNumMayorships.setEnabled(true);
    }

    private View prepareTabView(String text) {
        View view = LayoutInflater.from(this).inflate(R.layout.user_details_activity_tabs, null);
        TextView tv = (TextView) view.findViewById(R.id.userDetailsActivityTabTextView);
        tv.setText(text);
        return view;
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        mStateHolder.setActivityForTaskUserDetails(null);
        return mStateHolder;
    }

    private void startBadgesActivity() {
        if (mStateHolder.getUser() != null) {
            Intent intent = new Intent(UserDetailsActivity.this, BadgesActivity.class);
            intent.putParcelableArrayListExtra(BadgesActivity.EXTRA_BADGE_ARRAY_LIST_PARCEL,
                    mStateHolder.getUser().getBadges());
            startActivity(intent);
        }
    }
    
    private void startMayorshipsActivity() {
        if (mStateHolder.getUser() != null) {
            Intent intent = new Intent(UserDetailsActivity.this, UserMayorshipsActivity.class);
            intent.putExtra(UserMayorshipsActivity.EXTRA_USER_ID, mStateHolder.getUser().getId());
            startActivity(intent); 
        }
    }

    private void onUserDetailsTaskComplete(User user, Exception ex) {
        setProgressBarIndeterminateVisibility(false);
        mStateHolder.setFetchedUserDetails(true);
        mStateHolder.setIsRunningUserDetailsTask(false);
        if (user != null) {
            mStateHolder.setUser(user);
            populateUiAfterFullUserObjectFetched();
        } else {
            NotificationsUtil.ToastReasonForFailure(this, ex);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        // We have a different set of menu options for the logged-in user vs
        // viewing a friend and potentially a stranger even.
        User user = mStateHolder.getUser();
        if (user != null && user.getId().equals(((Foursquared) getApplication()).getUserId())) {
            menu.add(Menu.NONE, MENU_FRIEND_REQUESTS, Menu.NONE, 
                    R.string.preferences_friend_requests_title).setIcon(R.drawable.ic_menu_friends);
            menu.add(Menu.NONE, MENU_SHOUT, Menu.NONE,  
                    R.string.shout_action_label).setIcon(R.drawable.ic_menu_shout);
            MenuUtils.addPreferencesToMenu(this, menu);
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_FRIEND_REQUESTS:
                startActivity(new Intent(this, FriendRequestsActivity.class));
                return true;
            case MENU_SHOUT:
                Intent intent = new Intent(this, CheckinOrShoutGatherInfoActivity.class);
                intent.putExtra(CheckinOrShoutGatherInfoActivity.INTENT_EXTRA_IS_SHOUT, true);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Even if the caller supplies us with a User object parcelable, it won't
     * have all the badge etc extra info in it. As soon as the activity starts,
     * we launch this task to fetch a full user object, and merge it with
     * whatever is already supplied in mUser.
     */
    private static class UserDetailsTask extends AsyncTask<String, Void, User> {

        private UserDetailsActivity mActivity;
        private Exception mReason;

        public UserDetailsTask(UserDetailsActivity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            mActivity.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected User doInBackground(String... params) {
            try {
                return ((Foursquared) mActivity.getApplication()).getFoursquare().user(
                        params[0],
                        true,
                        true,
                        LocationUtils.createFoursquareLocation(((Foursquared) mActivity
                                .getApplication()).getLastKnownLocation()));
            } catch (Exception e) {
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            if (mActivity != null) {
                mActivity.onUserDetailsTaskComplete(user, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onUserDetailsTaskComplete(null, mReason);
            }
        }

        public void setActivity(UserDetailsActivity activity) {
            mActivity = activity;
        }
    }

    private static class StateHolder {

        /** The user object we are rendering. */
        private User mUser;

        /** Show options to add friends for strangers. */
        private boolean mShowAddFriendOptions;

        private UserDetailsTask mTaskUserDetails;
        private boolean mIsRunningUserDetailsTask;
        private boolean mFetchedUserDetails;

        public StateHolder() {
            mShowAddFriendOptions = false;
            mIsRunningUserDetailsTask = false;
            mFetchedUserDetails = false;
        }

        public void setFetchedUserDetails(boolean fetched) {
            mFetchedUserDetails = fetched;
        }

        public boolean getFetchedUserDetails() {
            return mFetchedUserDetails;
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

        public void startTaskUserDetails(UserDetailsActivity activity, String userId) {
            mIsRunningUserDetailsTask = true;
            mTaskUserDetails = new UserDetailsTask(activity);
            mTaskUserDetails.execute(userId);
        }

        public void setActivityForTaskUserDetails(UserDetailsActivity activity) {
            if (mTaskUserDetails != null) {
                mTaskUserDetails.setActivity(activity);
            }
        }

        public void setIsRunningUserDetailsTask(boolean isRunning) {
            mIsRunningUserDetailsTask = isRunning;
        }

        public boolean getIsRunningUserDetailsTask() {
            return mIsRunningUserDetailsTask;
        }

        public void cancelTasks() {
            if (mTaskUserDetails != null) {
                mTaskUserDetails.setActivity(null);
                mTaskUserDetails.cancel(true);
            }
        }
    }
    
    private class RemoteResourceManagerObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            mImageViewPhoto.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (mStateHolder.getUser() != null) {
                        Uri uriPhoto = Uri.parse(mStateHolder.getUser().getPhoto());
                        if (mRrm.exists(uriPhoto)) {
                            try {
                                Bitmap bitmap = BitmapFactory.decodeStream(mRrm.getInputStream(uriPhoto));
                                mImageViewPhoto.setImageBitmap(bitmap);
                                mIsUsersPhotoSet = true;
                                mImageViewPhoto.setImageBitmap(bitmap);
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            });
        }
    }
}
