/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.preferences.Preferences;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class ShoutActivity extends Activity {
    public static final String TAG = "ShoutActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String EXTRA_VENUE_NAME = "com.joelapenna.foursquared.ShoutActivity.VENUE_NAME";
    public static final String EXTRA_VENUE_ADDRESS = "com.joelapenna.foursquared.ShoutActivity.VENUE_ADDRESS";
    public static final String EXTRA_VENUE_CROSSSTREET = "com.joelapenna.foursquared.ShoutActivity.VENUE_CROSSSTREET";
    public static final String EXTRA_VENUE_CITY = "com.joelapenna.foursquared.ShoutActivity.VENUE_CITY";
    public static final String EXTRA_VENUE_ZIP = "com.joelapenna.foursquared.ShoutActivity.VENUE_ZIP";
    public static final String EXTRA_VENUE_STATE = "com.joelapenna.foursquared.ShoutActivity.VENUE_STATE";
    public static final String EXTRA_IMMEDIATE_CHECKIN = "com.joelapenna.foursquared.ShoutActivity.IMMEDIATE_CHECKIN";
    public static final String EXTRA_SHOUT = "com.joelapenna.foursquared.ShoutActivity.SHOUT";

    private StateHolder mStateHolder = new StateHolder();

    private boolean mIsShouting = true;
    private boolean mImmediateCheckin = true;
    private boolean mTellFriends = true;
    private boolean mTellTwitter = false;
    private String mShout = null;

    private Button mCheckinButton;
    private CheckBox mTwitterCheckBox;
    private CheckBox mFriendsCheckBox;
    private EditText mShoutEditText;
    private TextView mVenueView;

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

        // Try to restore the general state holder, from a configuration change.
        if (getLastNonConfigurationInstance() != null) {
            if (DEBUG) Log.d(TAG, "Using last non configuration instance");
            mStateHolder = (StateHolder) getLastNonConfigurationInstance();
        } else if (!mIsShouting) {
            // Translate the extras received in this intent into a venue, then
            // attach it to the
            // venue view.
            mStateHolder.venue = new Venue();
            intentExtrasIntoVenue(getIntent(), mStateHolder.venue);
        }

        if (mImmediateCheckin) {
            startCheckinResult();
        } else {
            initializeUi();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((Foursquared) getApplication()).requestLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((Foursquared) getApplication()).removeLocationUpdates();
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
        return mStateHolder;
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

    private void initializeUi() {
        setTheme(android.R.style.Theme_Dialog);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.shout_activity);

        mCheckinButton = (Button) findViewById(R.id.checkinButton);
        mFriendsCheckBox = (CheckBox) findViewById(R.id.tellFriendsCheckBox);
        mTwitterCheckBox = (CheckBox) findViewById(R.id.tellTwitterCheckBox);
        mShoutEditText = (EditText) findViewById(R.id.shoutEditText);
        mVenueView = (TextView) findViewById(R.id.title_text);

        mCheckinButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckinButton.setEnabled(false);
                String shout = mShoutEditText.getText().toString();
                if (!TextUtils.isEmpty(shout)) {
                    mShout = shout;
                }
                startCheckinResult();

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

        if (mIsShouting) {
            mVenueView.setVisibility(ViewGroup.GONE);
            mFriendsCheckBox.setChecked(true);
            mFriendsCheckBox.setEnabled(false);
            mCheckinButton.setText("Shout!");
        } else {
            mVenueView.setText("Checkin @ " + mStateHolder.venue.getName());
        }
    }

    private void startCheckinResult() {
        String venueId = mStateHolder.venue.getId();
        Intent checkin = new Intent(ShoutActivity.this, CheckinResultActivity.class);
        checkin.putExtra(Foursquared.EXTRA_VENUE_ID, venueId);
        startActivity(checkin);
        finish();
    }

    private static class StateHolder {
        // These are all enumerated because we currently cannot handle parceling
        // venues! How sad!
        Venue venue = null;

    }

}
