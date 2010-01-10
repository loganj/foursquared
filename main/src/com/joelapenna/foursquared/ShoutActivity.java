/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Badge;
import com.joelapenna.foursquare.types.CheckinResult;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Mayor;
import com.joelapenna.foursquare.types.Score;
import com.joelapenna.foursquare.types.Special;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquare.util.VenueUtils;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.NotificationsUtil;
import com.joelapenna.foursquared.widget.BadgeWithIconListAdapter;
import com.joelapenna.foursquared.widget.ScoreListAdapter;
import com.joelapenna.foursquared.widget.SeparatedListAdapter;
import com.joelapenna.foursquared.widget.SpecialListAdapter;

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
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Alex Volovoy (avolovoy@gmail.com)
 */
public class ShoutActivity extends Activity {
    public static final String TAG = "ShoutActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String EXTRA_VENUE = "com.joelapenna.foursquared.VenueId";
    public static final String EXTRA_VENUE_NAME = "com.joelapenna.foursquared.ShoutActivity.VENUE_NAME";
    public static final String EXTRA_VENUE_ADDRESS = "com.joelapenna.foursquared.ShoutActivity.VENUE_ADDRESS";
    public static final String EXTRA_VENUE_CROSSSTREET = "com.joelapenna.foursquared.ShoutActivity.VENUE_CROSSSTREET";
    public static final String EXTRA_VENUE_CITY = "com.joelapenna.foursquared.ShoutActivity.VENUE_CITY";
    public static final String EXTRA_VENUE_ZIP = "com.joelapenna.foursquared.ShoutActivity.VENUE_ZIP";
    public static final String EXTRA_VENUE_STATE = "com.joelapenna.foursquared.ShoutActivity.VENUE_STATE";
    public static final String EXTRA_IMMEDIATE_CHECKIN = "com.joelapenna.foursquared.ShoutActivity.IMMEDIATE_CHECKIN";
    public static final String EXTRA_SHOUT = "com.joelapenna.foursquared.ShoutActivity.SHOUT";
    public static final String CHECKIN_IN_PROGRESS = "com.joelapenna.foursquared.ShoutActivity.CHECKIN_IN_PROGRESS";

    private Dialog mProgressDialog;
    private boolean mIsShouting = true;
    private boolean mTellFriends = true;
    private boolean mTellTwitter = false;
    private boolean mImmediateCheckin = true;

    private String mShout = null;
    private ScoreListAdapter mScoreListAdapter;
    private BadgeWithIconListAdapter mBadgeListAdapter;
    private Button mCheckinButton;
    private CheckBox mTwitterCheckBox;
    private CheckBox mFriendsCheckBox;
    private EditText mShoutEditText;
    private Venue mVenue;
    private SpecialListAdapter mSpecialListAdapter;

    AsyncTask<Void, Void, CheckinResult> mCheckinTask = null;

    private CheckinResultObservable mCheckinResultObservable = new CheckinResultObservable();
    private CheckinResultObserver mCheckinResultObserver = new CheckinResultObserver();

    private BroadcastReceiver mLoggedOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive: " + intent);
            finish();
        }
    };
    private SeparatedListAdapter mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.shout);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(ShoutActivity.this);
        mTellFriends = settings.getBoolean(Preferences.PREFERENCE_SHARE_CHECKIN, mTellFriends);
        mTellTwitter = settings.getBoolean(Preferences.PREFERENCE_TWITTER_CHECKIN, mTellTwitter);
        // Implies there is no UI.
        if (getIntent().hasExtra(EXTRA_IMMEDIATE_CHECKIN)) {
            mImmediateCheckin = getIntent().getBooleanExtra(EXTRA_IMMEDIATE_CHECKIN, true);
            if (DEBUG) Log.d(TAG, "Immediate Checkin (from extra): " + mImmediateCheckin);
        } else {
            mImmediateCheckin = PreferenceManager.getDefaultSharedPreferences(ShoutActivity.this)
                    .getBoolean(Preferences.PREFERENCE_IMMEDIATE_CHECKIN, true);
            if (DEBUG) Log.d(TAG, "Immediate Checkin (from preference): " + mImmediateCheckin);
        }
        mIsShouting = getIntent().getBooleanExtra(ShoutActivity.EXTRA_SHOUT, false);
        if (mIsShouting) {
            if (DEBUG) Log.d(TAG, "Immediate checkin disabled, this is a shout.");
            mImmediateCheckin = false;
        }
        if (DEBUG) Log.d(TAG, "Is Shouting: " + mIsShouting);
        if (DEBUG) Log.d(TAG, "Immediate Checkin: " + mImmediateCheckin);
        initListViewAdapters();
        mCheckinResultObservable.addObserver(mCheckinResultObserver);
        if (!mIsShouting) {
            // Translate the extras received in this intent into a venue, then
            // attach it to the
            // venue view.
            mVenue = new Venue();
            intentExtrasIntoVenue(getIntent(), mVenue);
        }
        if (mImmediateCheckin) {
            if (DEBUG) Log.d(TAG, "Immediate checkin is set.");
            // Check if we already have task running
            if (mCheckinTask == null) new CheckinTask().execute();
        } else {
            initializeMainDialog();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((Foursquared) getApplication()).requestLocationUpdates(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((Foursquared) getApplication()).removeLocationUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCheckinTask = null;
        unregisterReceiver(mLoggedOutReceiver);
    }

    /**
     * Because we cannot parcel venues properly yet (issue #5) we have to mutate
     * a series of intent extras into a venue so that we can code to this future
     * possibility.
     */
    public static final void intentExtrasIntoVenue(Intent intent, Venue venue) {
        Bundle extras = intent.getExtras();
        venue.setId(extras.getString(Foursquared.EXTRA_VENUE_ID));
        venue.setName(extras.getString(EXTRA_VENUE_NAME));
        venue.setAddress(extras.getString(EXTRA_VENUE_ADDRESS));
        venue.setCrossstreet(extras.getString(EXTRA_VENUE_CROSSSTREET));
        venue.setCity(extras.getString(EXTRA_VENUE_CITY));
        venue.setZip(extras.getString(EXTRA_VENUE_ZIP));
        venue.setState(extras.getString(EXTRA_VENUE_STATE));
    }

    public static final void venueIntoIntentExtras(Venue venue, Intent intent) {
        intent.putExtra(Foursquared.EXTRA_VENUE_ID, venue.getId());
        intent.putExtra(ShoutActivity.EXTRA_VENUE_NAME, venue.getName());
        intent.putExtra(ShoutActivity.EXTRA_VENUE_ADDRESS, venue.getAddress());
        intent.putExtra(ShoutActivity.EXTRA_VENUE_CITY, venue.getCity());
        intent.putExtra(ShoutActivity.EXTRA_VENUE_CROSSSTREET, venue.getCrossstreet());
        intent.putExtra(ShoutActivity.EXTRA_VENUE_STATE, venue.getState());
        intent.putExtra(ShoutActivity.EXTRA_VENUE_ZIP, venue.getZip());
    }

    private void initListViewAdapters() {
        mListAdapter = new SeparatedListAdapter(this, R.layout.list_header);
        ((ListView) findViewById(R.id.result_list)).setAdapter(mListAdapter);
        mScoreListAdapter = new ScoreListAdapter(this, ((Foursquared) getApplication())
                .getRemoteResourceManager());
        mSpecialListAdapter = new SpecialListAdapter(this);
        mBadgeListAdapter = new BadgeWithIconListAdapter(this, ((Foursquared) getApplication())
                .getRemoteResourceManager(), R.layout.badge_list_item);
    }

    private void initializeMainDialog() {
        // setting main dialog visible
        findViewById(R.id.shout).setVisibility(View.VISIBLE);
        findViewById(R.id.checkin_result).setVisibility(View.GONE);
        mCheckinButton = (Button) findViewById(R.id.checkinButton);
        mFriendsCheckBox = (CheckBox) findViewById(R.id.tellFriendsCheckBox);
        mTwitterCheckBox = (CheckBox) findViewById(R.id.tellTwitterCheckBox);
        mShoutEditText = (EditText) findViewById(R.id.shoutEditText);
        mCheckinButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckinButton.setEnabled(false);
                String shout = mShoutEditText.getText().toString();
                if (!TextUtils.isEmpty(shout)) {
                    mShout = shout;
                }
                // Check if we already have task running.
                if (mCheckinTask == null) new CheckinTask().execute();
            }
        });
        mTwitterCheckBox.setChecked(mTellTwitter);
        mTwitterCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTellTwitter = isChecked;
                mTwitterCheckBox.setEnabled(isChecked);
            }
        });
        mFriendsCheckBox.setChecked(mTellFriends);
        mFriendsCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTellFriends = isChecked;
                mTwitterCheckBox.setEnabled(isChecked);
                if (!isChecked) {
                    mTellTwitter = false;
                    mTwitterCheckBox.setChecked(false);
                }
            }
        });
        String title = (mIsShouting) ? "Shouting!" : "Checking in @";
        if (mIsShouting) {
            mFriendsCheckBox.setChecked(true);
            mFriendsCheckBox.setEnabled(false);
            mCheckinButton.setText("Shout!");

        } else {
            String venueName = mVenue.getName();
            title += venueName;
        }
        ((TextView) findViewById(R.id.title_text)).setText(title);
    }

    private Dialog showProgressDialog() {
        if (mProgressDialog == null) {
            // findViewById(R.id.shout).setVisibility(View.GONE);
            // findViewById(R.id.checkin_result).setVisibility(View.GONE);
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
                    setVisible(true);
                    finish();
                }
            });
            mProgressDialog = dialog;
        }
        setVisible(false);
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
            String venueId = null;
            if (VenueUtils.isValid(mVenue)) {
                venueId = mVenue.getId();
            }
            boolean isPrivate = !mTellFriends;
            try {
                Location location = ((Foursquared) getApplication()).getLastKnownLocation();
                if (location == null) {
                    if (DEBUG) Log.d(TAG, "unable to determine location");
                    throw new FoursquareException(getResources().getString(
                            R.string.no_location_providers));
                }
                return ((Foursquared) getApplication()).getFoursquare().checkin(venueId, null,
                        LocationUtils.createFoursquareLocation(location), mShout, isPrivate,
                        mTellTwitter);
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
                NotificationsUtil.ToastReasonForFailure(ShoutActivity.this, mReason);
                finish();
            } else {
                setCheckinResult(checkinResult);
            }
            dismissProgressDialog();
            // Make sure the caller knows things worked out alright.
            setResult(Activity.RESULT_OK);
        }

        @Override
        protected void onCancelled() {
            setProgressBarIndeterminateVisibility(false);
            setVisible(true);
            dismissProgressDialog();
        }
    }

    private void setCheckinResult(CheckinResult checkinrResult) {
        mCheckinResultObservable.notifyObservers(checkinrResult);
    }

    private class CheckinResultObservable extends Observable {
        public void notifyObservers(Object data) {
            setChanged();
            super.notifyObservers(data);
        }
    }

    private class CheckinResultObserver implements Observer {
        @SuppressWarnings("unchecked")
        @Override
        public void update(Observable observable, Object data) {
            if (DEBUG) Log.d(TAG, "CheckinResult was observed.");
            CheckinResult checkinResult = (CheckinResult) data;
            displayMain(checkinResult);
            displayBadges(checkinResult.getBadges());
            displaySpecials(checkinResult.getSpecials());

            // Only display the footer if we have a score or a mayor change.
            displayScores(checkinResult.getScoring());
            displayMayor(checkinResult.getMayor());
            findViewById(R.id.footer).setVisibility(View.VISIBLE);
        }

        private void displayMain(CheckinResult checkinResult) {
            // TODO Populate title and message correctly
            // String checkinId = checkinResult.getId();
            setVisible(true);
            findViewById(R.id.shout).setVisibility(View.GONE);
            findViewById(R.id.dialog_title).setVisibility(View.VISIBLE);
            findViewById(R.id.checkin_result).setVisibility(View.VISIBLE);
            String message = checkinResult.getMessage();
            String title = (mIsShouting) ? "Shouted!" : "Checked in!";
            // Set the text message of the result.
            ((TextView) findViewById(R.id.title_text)).setText(title);
            ((TextView) findViewById(R.id.score_message)).setText(message);
        }

        private void displayMayor(Mayor mayor) {
            if (mayor != null) {
                // We're the mayor. Yay!
                ((TextView) findViewById(R.id.mayor_message)).setText(mayor.getMessage());
                findViewById(R.id.mayor_message).setVisibility(View.VISIBLE);
                findViewById(R.id.mayor_crown).setVisibility(View.VISIBLE);
            }
        }

        private void displayBadges(Group<Badge> badges) {
            if (badges != null) {
                mBadgeListAdapter.setGroup(badges);
                mListAdapter.addSection(getResources().getString(R.string.checkin_badges),
                        mBadgeListAdapter);
            }
        }

        private boolean displayScores(final Group<Score> scores) {
            Resources res = getResources();
            int total = 0;
            if (scores != null) {
                mScoreListAdapter.setGroup(scores);
                mListAdapter.addSection(res.getString(R.string.checkin_score), mScoreListAdapter);
                for (Score score : scores) {
                    total += Integer.parseInt(score.getPoints());
                }
            }
            if (total > 0) {
                TextView totals = ((TextView) findViewById(R.id.totals));
                totals.setText(res.getString(R.string.checkin_totals) + " " + total + " "
                        + res.getString(R.string.checkin_points));
                totals.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        }

        private boolean displaySpecials(final Group<Special> specials) {
            Group<Special> localSpecials = new Group<Special>();
            Group<Special> nearbySpecials = new Group<Special>();
            if (specials != null) {
                for (int i = 0, size = specials.size(); i < size; i++) {
                    Special special = specials.get(i);
                    if (special.getVenue() == null) {
                        localSpecials.add(special);
                    } else {
                        nearbySpecials.add(special);
                    }
                }
                mSpecialListAdapter.setGroup(localSpecials);
                mListAdapter.addSection(getResources().getString(R.string.checkin_specials),
                        mSpecialListAdapter);
                return true;
            }
            return false;
        }
    }
}
