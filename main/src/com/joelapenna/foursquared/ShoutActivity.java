/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.CheckinResult;
import com.joelapenna.foursquare.types.City;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquare.util.VenueUtils;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.NotificationsUtil;
import com.joelapenna.foursquared.widget.VenueView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

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

    public static final String STATE_DIALOG_STATE = "com.joelapenna.foursquared.ShoutActivity.DIALOG_STATE";

    private static final int DIALOG_CHECKIN_PROGRESS = 1;
    private static final int DIALOG_CHECKIN_RESULT = 2;

    private StateHolder mStateHolder = new StateHolder();
    private DialogStateHolder mDialogStateHolder = null;

    private boolean mIsShouting = true;
    private boolean mImmediateCheckin = true;
    private boolean mTellFriends = true;
    private boolean mTellTwitter = false;
    private String mShout = null;

    private Button mCheckinButton;
    private CheckBox mTwitterCheckBox;
    private CheckBox mFriendsCheckBox;
    private EditText mShoutEditText;
    private VenueView mVenueView;

    private BroadcastReceiver mLoggedInReceiver = new BroadcastReceiver() {
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
        registerReceiver(mLoggedInReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

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
            mStateHolder = (StateHolder)getLastNonConfigurationInstance();
        } else if (!mIsShouting) {
            // Translate the extras received in this intent into a venue, then attach it to the
            // venue view.
            mStateHolder.venue = new Venue();
            intentExtrasIntoVenue(getIntent(), mStateHolder.venue);
        }

        // Try to restore the dialog state holder
        if (savedInstanceState != null) {
            mDialogStateHolder = savedInstanceState.getParcelable(STATE_DIALOG_STATE);
        }

        // If we can restore dialog state, we've already checked in.
        boolean checkinCompleted = (mDialogStateHolder != null);

        if (mImmediateCheckin) {
            setVisible(false);
            if (!checkinCompleted) {
                if (DEBUG) Log.d(TAG, "Immediate checkin is set.");
                mStateHolder.checkinTask = new CheckinTask().execute();
            } else {
                if (DEBUG) Log.d(TAG, "We have an existing checkin, not launching checkin task.");
            }
        } else {
            initializeUi();
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
        unregisterReceiver(mLoggedInReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_DIALOG_STATE, mDialogStateHolder);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mStateHolder;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CHECKIN_PROGRESS:
                String title = (mIsShouting) ? "Shouting!" : "Checking in!";
                String messageAction = (mIsShouting) ? "shout!" : "check-in!";
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setCancelable(true);
                dialog.setIndeterminate(true);
                dialog.setTitle(title);
                dialog.setIcon(android.R.drawable.ic_dialog_info);
                dialog.setMessage("Please wait while we " + messageAction);
                dialog.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mStateHolder.checkinTask.cancel(true);
                    }
                });
                return dialog;

            case DIALOG_CHECKIN_RESULT:
                Builder dialogBuilder = new AlertDialog.Builder(this) //
                        .setIcon(android.R.drawable.ic_dialog_info) // icon
                        .setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        }); //

                // Set up the custom view for it.
                LayoutInflater inflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.checkin_result_dialog,
                        (ViewGroup)findViewById(R.id.layout_root));
                dialogBuilder.setView(layout);

                // Set the text message of the result.
                TextView messageView = (TextView)layout.findViewById(R.id.messageTextView);
                messageView.setText(mDialogStateHolder.message);

                // Set the title and web view which vary based on if the user is shouting.

                if (mDialogStateHolder.shouting) {
                    dialogBuilder.setTitle("Shouted!");

                } else {
                    if (mDialogStateHolder.venueName != null) {
                        dialogBuilder.setTitle("Checked in @ " + mDialogStateHolder.venueName);
                    } else {
                        dialogBuilder.setTitle("Checked in!");
                    }
                    WebView webView = (WebView)layout.findViewById(R.id.webView);

                    String userId = PreferenceManager.getDefaultSharedPreferences(this).getString(
                            Preferences.PREFERENCE_ID, "");
                    webView.loadUrl(((Foursquared)getApplication()).getFoursquare()
                            .checkinResultUrl(userId, mDialogStateHolder.checkinId));

                }
                return dialogBuilder.create();
        }
        return null;
    }

    /**
     * Because we cannot parcel venues properly yet (issue #5) we have to mutate a series of intent
     * extras into a venue so that we can code to this future possibility.
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

        mCheckinButton = (Button)findViewById(R.id.checkinButton);
        mFriendsCheckBox = (CheckBox)findViewById(R.id.tellFriendsCheckBox);
        mTwitterCheckBox = (CheckBox)findViewById(R.id.tellTwitterCheckBox);
        mShoutEditText = (EditText)findViewById(R.id.shoutEditText);
        mVenueView = (VenueView)findViewById(R.id.venue);

        mCheckinButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckinButton.setEnabled(false);
                String shout = mShoutEditText.getText().toString();
                if (!TextUtils.isEmpty(shout)) {
                    mShout = shout;
                }
                mStateHolder.checkinTask = new CheckinTask().execute();
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
            mVenueView.setVenue(mStateHolder.venue);
        }
    }

    class CheckinTask extends AsyncTask<Void, Void, CheckinResult> {

        private Exception mReason;

        @Override
        public void onPreExecute() {
            showDialog(DIALOG_CHECKIN_PROGRESS);
        }

        @Override
        public CheckinResult doInBackground(Void... params) {
            String venueId = null;
            if (VenueUtils.isValid(mStateHolder.venue)) {
                venueId = mStateHolder.venue.getId();
            }

            boolean isPrivate = !mTellFriends;

            try {
                String geolat = null;
                String geolong = null;
                Location location = ((Foursquared)getApplication()).getLastKnownLocation();
                if (location != null) {
                    geolat = String.valueOf(location.getLatitude());
                    geolong = String.valueOf(location.getLongitude());
                }

                // Its a couple more lookups, but make sure the server knows what city we're in
                // before we check-in
                City city = Preferences.switchCity(((Foursquared)getApplication()).getFoursquare(),
                        location);
                Editor editor = PreferenceManager.getDefaultSharedPreferences(ShoutActivity.this)
                        .edit();
                Preferences.storeCity(editor, city);
                editor.commit();

                return ((Foursquared)getApplication()).getFoursquare().checkin(venueId, null,
                        geolat, geolong, mShout, isPrivate, mTellTwitter);
            } catch (Exception e) {
                Log.d(TAG, "Storing reason: ", e);
                mReason = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(CheckinResult checkinResult) {
            if (DEBUG) Log.d(TAG, "CheckinTask: onPostExecute()");

            dismissDialog(DIALOG_CHECKIN_PROGRESS);

            if (checkinResult == null) {
                NotificationsUtil.ToastReasonForFailure(ShoutActivity.this, mReason);
                if (mImmediateCheckin) {
                    finish();
                } else {
                    mCheckinButton.setEnabled(true);
                }
                return;

            } else {
                // Show the dialog that will dismiss this activity.
                mDialogStateHolder = new DialogStateHolder(checkinResult, mIsShouting);
                showDialog(DIALOG_CHECKIN_RESULT);

                // Make sure the caller knows things worked out alright.
                setResult(Activity.RESULT_OK);
            }
        }

        @Override
        public void onCancelled() {
            dismissDialog(DIALOG_CHECKIN_PROGRESS);
            if (mImmediateCheckin) {
                finish();
            } else {
                mCheckinButton.setEnabled(true);
            }
        }
    }

    private static class StateHolder {
        // These are all enumerated because we currently cannot handle parceling venues! How sad!
        Venue venue = null;
        AsyncTask<Void, Void, CheckinResult> checkinTask = null;
    }

    private static class DialogStateHolder implements Parcelable {
        String venueName = null;
        String message = null;
        String checkinId = null;
        boolean shouting;

        public DialogStateHolder(CheckinResult checkinResult, boolean isShouting) {
            if (checkinResult == null) {
                throw new IllegalArgumentException("checkinResult cannot be null");
            }
            if (checkinResult.getVenue() != null) {
                venueName = checkinResult.getVenue().getName();
            }
            message = checkinResult.getMessage();
            checkinId = checkinResult.getId();
            shouting = isShouting;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(message);
            out.writeString(venueName);
            out.writeString(checkinId);
            out.writeInt((shouting) ? 1 : 0);
        }

        // Used by the android system.
        @SuppressWarnings("unused")
        public static final Parcelable.Creator<DialogStateHolder> CREATOR = new Parcelable.Creator<DialogStateHolder>() {
            public DialogStateHolder createFromParcel(Parcel in) {
                return new DialogStateHolder(in);
            }

            public DialogStateHolder[] newArray(int size) {
                return new DialogStateHolder[size];
            }
        };

        private DialogStateHolder(Parcel in) {
            message = in.readString();
            venueName = in.readString();
            checkinId = in.readString();
            shouting = (in.readInt() == 1) ? true : false;
        }
    }

}
