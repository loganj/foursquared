/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Badge;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.util.NotificationsUtil;
import com.joelapenna.foursquared.util.StringFormatters;
import com.joelapenna.foursquared.util.UserUtils;
import com.joelapenna.foursquared.widget.BadgeWithIconListAdapter;
import com.joelapenna.foursquared.widget.VenueView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class UserActivity extends Activity {
    private static final String TAG = "UserActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String EXTRA_USER = "com.joelapenna.foursquared.UserId";

    private Dialog mProgressDialog;
    private AlertDialog mBadgeDialog;

    private String mUserId = null;
    private User mUser = null;
    private UserObservable mUserObservable = new UserObservable();
    private UserObserver mUserObserver = new UserObserver();

    private GridView mBadgesGrid;
    private BadgeWithIconListAdapter mListAdapter;
    private VenueView mVenueView;
    private AsyncTask<Void, Void, User> mUserTask = null;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.user_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        mVenueView = (VenueView) findViewById(R.id.venue);

        ImageView userPhoto = (ImageView) findViewById(R.id.photo);
        userPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserPhotoLarge();
            }
        });

        initGridViewAdapter();

        if (getIntent().hasExtra(EXTRA_USER)) {
            mUserId = getIntent().getExtras().getString(EXTRA_USER);
            if (DEBUG) Log.d(TAG, "Found User Extra: " + mUserId);
        } else {
            mUserId = null;
            if (DEBUG) Log.d(TAG, "No User Extra, defaulting to self");
        }

        mUserObservable.addObserver(mUserObserver);
        if (getLastNonConfigurationInstance() == null) {
            mUserTask = new UserTask().execute();
        } else {
            User user = (User) getLastNonConfigurationInstance();
            setUser(user);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mListAdapter.removeObserver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedOutReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (DEBUG) Log.d(TAG, "onStop()");

        if (mUserTask != null) {
            mUserTask.cancel(true);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "onNewIntent: " + intent);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mUser;
    }

    private void initGridViewAdapter() {
        mBadgesGrid = (GridView) findViewById(R.id.badgesGrid);
        mListAdapter = new BadgeWithIconListAdapter(UserActivity.this,
                ((Foursquared) getApplication()).getRemoteResourceManager());
        mBadgesGrid.setAdapter(mListAdapter);
    }

    private Dialog showProgressDialog() {
        if (mProgressDialog == null) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Loading");
            dialog.setMessage("Please wait while we retrieve some information");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            mProgressDialog = dialog;
        }
        mProgressDialog.show();
        return mProgressDialog;
    }

    private Dialog showBadgeDialog(Badge badge) {
        if (mBadgeDialog == null) {
            AlertDialog dialog = new AlertDialog.Builder(UserActivity.this) //
                    .setTitle("Loading") //
                    .setMessage("Please wait while retrieve some information") //
                    .setCancelable(true) //
                    .create();
            mBadgeDialog = dialog;
        }
        mBadgeDialog.setTitle(badge.getName());
        mBadgeDialog.setMessage(badge.getDescription());

        try {
            Uri icon = Uri.parse(badge.getIcon());
            if (DEBUG) Log.d(TAG, icon.toString());
            mBadgeDialog.setIcon(new BitmapDrawable(((Foursquared) getApplication())
                    .getRemoteResourceManager().getInputStream(icon)));
        } catch (IOException e) {
            if (DEBUG) Log.d(TAG, "IOException", e);
            mBadgeDialog.setIcon(R.drawable.default_on);
        }
        mBadgeDialog.show();
        return mBadgeDialog;
    }

    private void dismissProgressDialog() {
        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException e) {
            // We don't mind. android cleared it for us.
        }
    }

    private void setUser(User user) {
        mUser = user;
        mUserObservable.notifyObservers(user);
    }

    private class UserTask extends AsyncTask<Void, Void, User> {

        private Exception mReason;

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            showProgressDialog();
        }

        @Override
        protected User doInBackground(Void... params) {
            try {
                return ((Foursquared) getApplication()).getFoursquare().user(
                        mUserId,
                        false,
                        true,
                        LocationUtils.createFoursquareLocation(((Foursquared) getApplication())
                                .getLastKnownLocation()));
            } catch (Exception e) {
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            setProgressBarIndeterminateVisibility(false);
            if (user == null) {
                NotificationsUtil.ToastReasonForFailure(UserActivity.this, mReason);
                finish();
            } else {
                setUser(user);
            }
            dismissProgressDialog();
        }

        @Override
        protected void onCancelled() {
            setProgressBarIndeterminateVisibility(false);
            dismissProgressDialog();
        }
    }

    private class UserObservable extends Observable {
        public void notifyObservers(Object data) {
            setChanged();
            super.notifyObservers(data);
        }
    }

    private class UserObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            User user = (User) data;
            displayUser(user);
            displayBadges(user);
            displayCheckin(user);
        }

        @SuppressWarnings("unchecked")
        private void displayBadges(User user) {
            if (user.getBadges() != null) {
                mListAdapter.setGroup(user.getBadges());
                ((TextView) findViewById(R.id.badgesHeader)).setVisibility(TextView.VISIBLE);
                mBadgesGrid.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Badge badge = (Badge) parent.getAdapter().getItem(position);
                        showBadgeDialog(badge);
                    }
                });
            }
        }

        private void displayCheckin(User user) {
            Checkin checkin = user.getCheckin();
            if (checkin != null && !TextUtils.isEmpty(checkin.getShout())) {
                ((TextView) findViewById(R.id.secondLine)).setText(checkin.getShout());
            }

            if (checkin != null && checkin.getVenue() != null) {
                final Venue venue = user.getCheckin().getVenue();
                mVenueView.setVenue(venue);
                ((TextView) findViewById(R.id.venueHeader)).setVisibility(TextView.VISIBLE);

                // Hell, I'm not even sure if this is the right place to put
                // this... Whatever.
                mVenueView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(UserActivity.this, VenueActivity.class);
                        intent.putExtra(Foursquared.EXTRA_VENUE_ID, venue.getId());
                        startActivity(intent);
                    }
                });
            } else {
                // If we don't have a checkin location, clear it from the UI so
                // it doesn't take up
                // space.
                LayoutParams params = mVenueView.getLayoutParams();
                params.height = 0;
                mVenueView.setLayoutParams(params);
            }
        }

        private void displayUser(User user) {
            if (DEBUG) Log.d(TAG, "loading user");
            String fullName = StringFormatters.getUserFullName(user);
            TextView name = (TextView) findViewById(R.id.name);

            name.setText(fullName);
            UserUtils.ensureUserPhoto(UserActivity.this, user, DEBUG, TAG);
        }
    }

    private void showUserPhotoLarge() {
        if (mUser == null) {
            return;
        }
        String photoUrl = mUser.getPhoto();
        if (photoUrl == null) {
            return;
        }

        // If "_thumbs" exists, remove it to get the url of the full-size image.
        photoUrl = photoUrl.replace("_thumbs", "");

        Intent intent = new Intent();
        intent.setClass(this, FetchImageForViewIntent.class);
        intent.putExtra(FetchImageForViewIntent.IMAGE_URL, photoUrl);
        intent.putExtra(FetchImageForViewIntent.PROGRESS_BAR_TITLE, getResources().getString(
                R.string.user_activity_fetch_full_image_title));
        intent.putExtra(FetchImageForViewIntent.PROGRESS_BAR_MESSAGE, getResources().getString(
                R.string.user_activity_fetch_full_image_message));
        startActivity(intent);
    }
}
