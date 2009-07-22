/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Badge;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.RemoteResourceManager;
import com.joelapenna.foursquared.util.StringFormatters;
import com.joelapenna.foursquared.widget.BadgeWithIconListAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class UserActivity extends Activity {
    private static final String TAG = "TestUserActivity";
    private static final boolean DEBUG = Foursquared.DEBUG;

    public static final String EXTRA_USER = "com.joelapenna.foursquared.UserId";

    private RemoteResourceManager mUserPhotoManager = new RemoteResourceManager("user_photo");
    private RemoteResourceManager mBadgeIconManager = new RemoteResourceManager("badges");

    private Dialog mProgressDialog;
    private AlertDialog mBadgeDialog;

    private String mUserId = null;
    private User mUser = null;
    private UserObservable mUserObservable = new UserObservable();
    private UserObserver mUserObserver = new UserObserver();

    private GridView mBadgesGrid;
    private LinearLayout mVenueLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.user_activity);

        mBadgesGrid = (GridView)findViewById(R.id.badgesGrid);
        mVenueLayout = (LinearLayout)findViewById(R.id.venue);

        if (getIntent().hasExtra(EXTRA_USER)) {
            mUserId = getIntent().getExtras().getString(EXTRA_USER);
        } else {
            mUserId = null;
        }

        mUserObservable.addObserver(mUserObserver);
        if (getLastNonConfigurationInstance() == null) {
            new UserTask().execute();
        } else {
            User user = (User)getLastNonConfigurationInstance();
            setUser(user);
        }
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
            mBadgeDialog.setIcon(new BitmapDrawable(mBadgeIconManager.getInputStream(icon)));
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
                    mUserPhotoManager.getInputStream(photo));
            ((ImageView)findViewById(R.id.photo)).setImageBitmap(bitmap);
        } catch (IOException e) {
            if (DEBUG) Log.d(TAG, "Could not load bitmap. we don't have it yet.", e);
        }
    }

    private void setUser(User user) {
        mUser = user;
        mUserObservable.notifyObservers(user);
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

    private void ensureUserPhoto(User user) {
        if (user.getPhoto() == null) {
            ((ImageView)findViewById(R.id.photo)).setImageResource(R.drawable.blank_boy);
            return;
        }
        Uri photo = Uri.parse(user.getPhoto());
        if (photo != null) {
            if (mUserPhotoManager.getFile(photo).exists()) {
                setPhotoImageUri(photo);
            } else {
                new UserPhotoTask().execute(Uri.parse(user.getPhoto()));
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
                mUserPhotoManager.requestBlocking(uri);
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
                String uid;
                Checkin checkin = null;
                if (mUserId == null) {
                    // TODO(jlapenna): Fix someday.
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(UserActivity.this);
                    uid = prefs.getString(Preferences.PREFERENCE_ID, null);
                    // We have to check a second time if we're doing a user lookup because of a
                    // weird data return issue from the foursquare server.
                    if (DEBUG) Log.d(TAG, "Making stupid second user request.");
                    checkin = Foursquared.getFoursquare().user(null, false, false).getCheckin();
                } else {
                    uid = mUserId;
                }
                User user = Foursquared.getFoursquare().user(uid, false, true);
                // See above where we do a second query for the currently logged in user. If we
                // request badges, we don't get checkins.
                if (user != null && checkin != null) {
                    if (DEBUG) Log.d(TAG, "Overloading checkin");
                    user.setCheckin(checkin);
                }
                return user;
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
            setUser(user);
            dismissProgressDialog();
        }

        @Override
        protected void onCancelled() {
            setProgressBarIndeterminateVisibility(false);
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
            if (user.getBadges() != null) {
                mBadgesGrid.setAdapter(new BadgeWithIconListAdapter(UserActivity.this, user
                        .getBadges(), mBadgeIconManager));
                mBadgesGrid.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Badge badge = (Badge)parent.getAdapter().getItem(position);
                        showBadgeDialog(badge);
                    }
                });
            }
            if (user.getCheckin() != null) {
                Venue venue = user.getCheckin().getVenue();
                ((TextView)mVenueLayout.findViewById(R.id.name)).setText(venue.getName());
                ((TextView)mVenueLayout.findViewById(R.id.locationLine1)).setText(venue
                        .getAddress());
                ((TextView)mVenueLayout.findViewById(R.id.locationLine2)).setText(StringFormatters
                        .getVenueLocationCrossStreetOrCity(venue));
            } else {
                // If we don't have a checkin location, clear it from the UI so it doesn't take up
                // space.
                LayoutParams params = mVenueLayout.getLayoutParams();
                params.height = 0;
                mVenueLayout.setLayoutParams(params);
            }
        }
    }
}
