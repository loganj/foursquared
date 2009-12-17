/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Badge;
import com.joelapenna.foursquare.types.CheckinResult;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Mayor;
import com.joelapenna.foursquare.types.Score;
import com.joelapenna.foursquare.types.Special;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.NotificationsUtil;
import com.joelapenna.foursquared.widget.BadgeWithIconListAdapter;
import com.joelapenna.foursquared.widget.ScoreListAdapter;
import com.joelapenna.foursquared.widget.SpecialListAdapter;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class CheckinResultActivity extends Activity {

    public static final String TAG = "CheckinResultActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;
    public static final String EXTRA_VENUE = "com.joelapenna.foursquared.VenueId";
    private Dialog mProgressDialog;
    private boolean mIsShouting = true;
    private boolean mTellFriends = true;
    private boolean mTellTwitter = false;
    private String mShout = null;
    private CheckinResult mCheckinResult = null;
    private String mVenueId = null;
    private ScoreListAdapter mScoreListAdapter;
    private BadgeWithIconListAdapter mBadgeListAdapter;
    private SpecialListAdapter mSpecialListAdapter;
    private AsyncTask<Void, Void, CheckinResult> mCheckinTask = null;
    private CheckinResultObservable mCheckinResultObservable = new CheckinResultObservable();
    private CheckinResultObserver mCheckinResultObserver = new CheckinResultObserver();
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
        if (DEBUG) Log.d(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.checkin_result_dialog);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(CheckinResultActivity.this);
        mTellFriends = settings.getBoolean(Preferences.PREFERENCE_SHARE_CHECKIN, mTellFriends);
        mTellTwitter = settings.getBoolean(Preferences.PREFERENCE_TWITTER_CHECKIN, mTellTwitter);
        if (getIntent().hasExtra(Foursquared.EXTRA_VENUE_ID)) {
            mVenueId = getIntent().getStringExtra(Foursquared.EXTRA_VENUE_ID);
            if (DEBUG) Log.d(TAG, "Venue (from extra): " + mVenueId);
        }
        mIsShouting = getIntent().getBooleanExtra(ShoutActivity.EXTRA_SHOUT, false);
        initListViewAdapters();
        mCheckinResultObservable.addObserver(mCheckinResultObserver);
        if (getLastNonConfigurationInstance() == null) {
            mCheckinTask = new CheckinTask().execute();
        } else {
            if (DEBUG) Log.d(TAG, "We have an existing checkin, not launching checkin task.");
            CheckinResult checkinResult = (CheckinResult)getLastNonConfigurationInstance();
            setCheckinResult(checkinResult);
        }
    }

    @Override
    public void onResume() {

        super.onResume();
        ((Foursquared)getApplication()).requestLocationUpdates();
    }

    @Override
    public void onPause() {

        super.onPause();
        ((Foursquared)getApplication()).removeLocationUpdates();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        unregisterReceiver(mLoggedOutReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {

        return mCheckinResult;
    }

    private void initListViewAdapters() {

        mScoreListAdapter = new ScoreListAdapter(this, ((Foursquared)getApplication()).getRemoteResourceManager());
        ((ListView)findViewById(R.id.scoring)).setAdapter(mScoreListAdapter);
        mSpecialListAdapter = new SpecialListAdapter(this);
        ((ListView)findViewById(R.id.specials)).setAdapter(mSpecialListAdapter);
        mBadgeListAdapter = new BadgeWithIconListAdapter(this, ((Foursquared)getApplication()).getRemoteResourceManager(), R.layout.badge_list_item);
        ((ListView)findViewById(R.id.badges)).setAdapter(mBadgeListAdapter);
    }

    private Dialog showProgressDialog() {

        if (mProgressDialog == null) {
            String title = (mIsShouting) ? "Shouting!" : "Checking in!";
            String messageAction = (mIsShouting) ? "shout!" : "check-in!";
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setCancelable(true);
            dialog.setIndeterminate(true);
            dialog.setTitle(title);
            dialog.setIcon(android.R.drawable.ic_dialog_info);
            dialog.setMessage("Please wait while we " + messageAction);
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

    private void dismissProgressDialog() {

        try {
            mProgressDialog.dismiss();
        } catch (IllegalArgumentException e) {
            // We don't mind. android cleared it for us.
        }
    }

    class CheckinTask extends AsyncTask<Void, Void, CheckinResult> {

        private Exception mReason;

        @Override
        public void onPreExecute() {

            setProgressBarIndeterminateVisibility(true);
            showProgressDialog();
        }

        @Override
        public CheckinResult doInBackground(Void... params) {

            // TODO - do we need to check venue here ?
            boolean isPrivate = !mTellFriends;
            try {
                Location location = ((Foursquared)getApplication()).getLastKnownLocation();
                if (location == null) {
                    if (DEBUG) Log.d(TAG, "unable to determine location");
                    throw new FoursquareException(getResources().getString(R.string.no_location_providers));
                }
                ((Foursquared)getApplication()).requestSwitchCity(location);
                return ((Foursquared)getApplication()).getFoursquare().checkin(mVenueId, null, LocationUtils.createFoursquareLocation(location), mShout, isPrivate, mTellTwitter);
            } catch (Exception e) {
                Log.d(TAG, "Storing reason: ", e);
                mReason = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(CheckinResult checkinResult) {

            if (DEBUG) Log.d(TAG, "CheckinTask: onPostExecute()");
            setProgressBarIndeterminateVisibility(false);
            if (checkinResult == null) {
                NotificationsUtil.ToastReasonForFailure(CheckinResultActivity.this, mReason);
                finish();
            } else {
                setCheckinResult(checkinResult);
            }
            dismissProgressDialog();
        }

        @Override
        protected void onCancelled() {

            setProgressBarIndeterminateVisibility(false);
            dismissProgressDialog();
        }
    }

    private void setCheckinResult(CheckinResult checkinrResult) {

        mCheckinResult = checkinrResult;
        mCheckinResultObservable.notifyObservers(checkinrResult);
    }

    private class CheckinResultObservable extends Observable {

        public void notifyObservers(Object data) {

            setChanged();
            super.notifyObservers(data);
        }
    }

    private class CheckinResultObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {

            CheckinResult checkinResult = (CheckinResult)data;
            displayMain(checkinResult);
            displayBadges(checkinResult);
            displayScores(checkinResult);
            displaySpecials(checkinResult);
            // TODO Call displayBlock(s) for different parts of the response.
        }

        private void displayMain(CheckinResult checkinResult) {

            // TODO Populate title and message correctly
            // String checkinId = checkinResult.getId();
            String venueName = checkinResult.getVenue().getName();
            String title = (mIsShouting) ? "Shouted!" : "Checked in @ " + venueName;
            String message = checkinResult.getMessage();
            // Set the text message of the result.
            ((TextView)findViewById(R.id.title_text)).setText(title);
            ((TextView)findViewById(R.id.score_message)).setText(message);
            Mayor mayor = checkinResult.getMayor();
            if (mayor != null) {
                findViewById(R.id.mayor).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.mayor_message)).setText(mayor.getMessage());
            }
        }

        @SuppressWarnings("unchecked")
        private void displayBadges(CheckinResult checkinResult) {

            Group<Badge> badges = checkinResult.getBadges();
            if (badges != null) {
                findViewById(R.id.badges).setVisibility(View.VISIBLE);
                mBadgeListAdapter.setGroup(badges);
            }
        }

        @SuppressWarnings("unchecked")
        private void displayScores(CheckinResult checkinResult) {

            Group<Score> scores = checkinResult.getScoring();
            if (scores != null) {
                mScoreListAdapter.setGroup(scores);
            }
            int total = 0;
            for (Score score : scores) {
                total += Integer.parseInt(score.getPoints());
            }
            ((TextView)findViewById(R.id.totals)).setText("Total: " + total + " Points");
        }

        @SuppressWarnings("unchecked")
        private void displaySpecials(CheckinResult checkinResult) {

            Group<Special> specials = checkinResult.getSpecials();
            if (specials != null) {
                findViewById(R.id.specials_title).setVisibility(View.VISIBLE);
                findViewById(R.id.specials).setVisibility(View.VISIBLE);
                mSpecialListAdapter.setGroup(specials);
            }
        }
    }
}
