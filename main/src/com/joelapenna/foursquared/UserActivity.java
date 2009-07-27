/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Badge;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.StringFormatters;
import com.joelapenna.foursquared.widget.BadgeWithIconListAdapter;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
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
    private RelativeLayout mVenueLayout;
    private AsyncTask<Void, Void, User> mUserTask = null;
    private AsyncTask<Uri, Void, Uri> mUserPhotoTask = null;

    private BroadcastReceiver mLoggedInReceiver = new BroadcastReceiver() {
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
        registerReceiver(mLoggedInReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        mBadgesGrid = (GridView)findViewById(R.id.badgesGrid);
        mVenueLayout = (RelativeLayout)findViewById(R.id.venue);

        if (getIntent().hasExtra(EXTRA_USER)) {
            mUserId = getIntent().getExtras().getString(EXTRA_USER);
        } else {
            mUserId = null;
        }

        mUserObservable.addObserver(mUserObserver);
        if (getLastNonConfigurationInstance() == null) {
            mUserTask = new UserTask().execute();
        } else {
            User user = (User)getLastNonConfigurationInstance();
            setUser(user);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (DEBUG) Log.d(TAG, "onStop()");

        if (mUserTask != null) {
            mUserTask.cancel(true);
        }
        if (mUserPhotoTask != null) {
            mUserPhotoTask.cancel(true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedInReceiver);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "onNewIntent: " + intent);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mUser;
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
            mBadgeDialog.setIcon(new BitmapDrawable(Foursquared.getBadgeIconManager().getInputStream(icon)));
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

    void setPhotoImageUri(Uri photo) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(//
                    Foursquared.getUserPhotoManager().getInputStream(photo));
            ((ImageView)findViewById(R.id.photo)).setImageBitmap(bitmap);
        } catch (IOException e) {
            if (DEBUG) Log.d(TAG, "Could not load bitmap. we don't have it yet.", e);
        }
    }

    private void setUser(User user) {
        mUser = user;
        mUserObservable.notifyObservers(user);
    }

    private void ensureUserPhoto(User user) {
        if (user.getPhoto() == null) {
            ((ImageView)findViewById(R.id.photo)).setImageResource(R.drawable.blank_boy);
            return;
        }
        Uri photo = Uri.parse(user.getPhoto());
        if (photo != null) {
            if (Foursquared.getUserPhotoManager().getFile(photo).exists()) {
                setPhotoImageUri(photo);
            } else {
                mUserPhotoTask = new UserPhotoTask().execute(Uri.parse(user.getPhoto()));
            }
        }
    }

    private class UserPhotoTask extends AsyncTask<Uri, Void, Uri> {

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Uri doInBackground(Uri... params) {
            Uri uri = (Uri)params[0];
            try {
                Foursquared.getUserPhotoManager().requestBlocking(uri);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
                return null;
            }
            return uri;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            setProgressBarIndeterminateVisibility(false);
            setPhotoImageUri(uri);
        }
    }

    private class UserTask extends AsyncTask<Void, Void, User> {

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            showProgressDialog();
        }

        @Override
        protected User doInBackground(Void... params) {
            try {
                return Foursquared.getFoursquare().user(mUserId, false, true);
            } catch (FoursquareException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareException", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(User user) {
            setProgressBarIndeterminateVisibility(false);
            if (user == null) {
                Toast.makeText(UserActivity.this, "Unable to lookup user information",
                        Toast.LENGTH_SHORT).show();
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

        public User getUser() {
            return mUser;
        }
    }

    private class UserObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            User user = (User)data;
            displayUser(user);
            displayBadges(user);
            displayCheckin(user);
        }

        private void displayBadges(User user) {
            if (user.getBadges() != null) {
                mBadgesGrid.setAdapter(new BadgeWithIconListAdapter(UserActivity.this, user
                        .getBadges(), Foursquared.getBadgeIconManager()));
                ((TextView)findViewById(R.id.badgesHeader)).setVisibility(TextView.VISIBLE);
                mBadgesGrid.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Badge badge = (Badge)parent.getAdapter().getItem(position);
                        showBadgeDialog(badge);
                    }
                });
            }
        }

        private void displayCheckin(User user) {
            Checkin checkin = user.getCheckin();
            if (checkin != null && checkin.getVenue() != null) {
                final Venue venue = user.getCheckin().getVenue();
                ((TextView)mVenueLayout.findViewById(R.id.venueName)).setText(venue.getName());
                ((TextView)mVenueLayout.findViewById(R.id.venueLocationLine1)).setText(venue
                        .getAddress());
                ((TextView)mVenueLayout.findViewById(R.id.venueLocationLine2))
                        .setText(StringFormatters.getVenueLocationCrossStreetOrCity(venue));
                ((TextView)findViewById(R.id.venueHeader)).setVisibility(TextView.VISIBLE);

                // Hell, I'm not even sure if this is the right place to put this... Whatever.
                mVenueLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(UserActivity.this, VenueActivity.class);
                        intent.putExtra(VenueActivity.EXTRA_VENUE, venue.getId());
                        startActivity(intent);
                    }
                });
            } else {
                // If we don't have a checkin location, clear it from the UI so it doesn't take up
                // space.
                LayoutParams params = mVenueLayout.getLayoutParams();
                params.height = 0;
                mVenueLayout.setLayoutParams(params);
            }
        }

        private void displayUser(User user) {
            if (DEBUG) Log.d(TAG, "loading user");
            String fullName = user.getFirstname() + " " + user.getLastname();
            TextView name = (TextView)findViewById(R.id.name);
            TextView city = (TextView)findViewById(R.id.city);

            name.setText(fullName);
            city.setText(user.getCity().getShortname());

            ensureUserPhoto(user);
        }
    }
}
