/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.CheckinResult;
import com.joelapenna.foursquare.types.Venue;
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
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.io.IOException;

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

    private static final int DIALOG_CHECKIN_PROGRESS = 1;

    private StateHolder mStateHolder = new StateHolder();

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.shout_activity);
        registerReceiver(mLoggedInReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(ShoutActivity.this);

        mCheckinButton = (Button)findViewById(R.id.checkinButton);
        mFriendsCheckBox = (CheckBox)findViewById(R.id.tellFriendsCheckBox);
        mTwitterCheckBox = (CheckBox)findViewById(R.id.tellTwitterCheckBox);
        mShoutEditText = (EditText)findViewById(R.id.shoutEditText);
        mVenueView = (VenueView)findViewById(R.id.venue);

        mCheckinButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mStateHolder.checkinTask = new CheckinTask().execute();
            }
        });

        mTwitterCheckBox.setChecked(settings.getBoolean(Preferences.PREFERENCE_TWITTER_CHECKIN,
                false));

        mFriendsCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTwitterCheckBox.setEnabled(isChecked);
                if (!isChecked) {
                    mTwitterCheckBox.setChecked(false);
                }
            }
        });
        mFriendsCheckBox
                .setChecked(settings.getBoolean(Preferences.PREFERENCE_SHARE_CHECKIN, true));

        if (getLastNonConfigurationInstance() != null) {
            if (DEBUG) Log.d(TAG, "Using last non configuration instance");
            mStateHolder = (StateHolder)getLastNonConfigurationInstance();
        } else {
            // Translate the extras received in this intent int a venue, then attach it to the venue
            // view.
            mStateHolder.venue = new Venue();
            intentExtrasIntoVenue(getIntent(), mStateHolder.venue);
        }

        if (mStateHolder.venue.getId() != null) {
            mVenueView.setVenue(mStateHolder.venue);

            if (getIntent().getBooleanExtra(EXTRA_IMMEDIATE_CHECKIN, false)) {
                if (mStateHolder.checkinTask == null) {
                    if (DEBUG) Log.d(TAG, "Immediate checkin is set.");
                    mStateHolder.checkinTask = new CheckinTask().execute();
                }
            }

        } else {
            mVenueView.setVisibility(ViewGroup.GONE);
            mCheckinButton.setText("Shout!");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedInReceiver);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mStateHolder;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CHECKIN_PROGRESS:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setCancelable(true);
                dialog.setIndeterminate(true);
                dialog.setTitle("Checking in!");
                dialog.setIcon(android.R.drawable.ic_dialog_info);
                dialog.setMessage("Please wait while we check-in.");
                dialog.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mStateHolder.checkinTask.cancel(true);
                    }
                });
                return dialog;
        }
        return null;
    }

    /**
     * Because we cannot parcel venues properly yet (issue #5) we have to mutate a series of intent
     * extras into a venue so that we can code to this future possibility.
     */
    public static void intentExtrasIntoVenue(Intent intent, Venue venue) {
        venue.setId(intent.getExtras().getString(Foursquared.EXTRA_VENUE_ID));
        venue.setName(intent.getExtras().getString(EXTRA_VENUE_NAME));
        venue.setAddress(intent.getExtras().getString(EXTRA_VENUE_ADDRESS));
        venue.setCrossstreet(intent.getExtras().getString(EXTRA_VENUE_CROSSSTREET));
        venue.setCity(intent.getExtras().getString(EXTRA_VENUE_CITY));
        venue.setZip(intent.getExtras().getString(EXTRA_VENUE_ZIP));
        venue.setState(intent.getExtras().getString(EXTRA_VENUE_STATE));
    }

    public static void venueIntoIntentExtras(Venue venue, Intent intent) {
        intent.putExtra(Foursquared.EXTRA_VENUE_ID, venue.getId());
        intent.putExtra(ShoutActivity.EXTRA_VENUE_NAME, venue.getName());
        intent.putExtra(ShoutActivity.EXTRA_VENUE_ADDRESS, venue.getAddress());
        intent.putExtra(ShoutActivity.EXTRA_VENUE_CITY, venue.getCity());
        intent.putExtra(ShoutActivity.EXTRA_VENUE_CROSSSTREET, venue.getCrossstreet());
        intent.putExtra(ShoutActivity.EXTRA_VENUE_STATE, venue.getState());
        intent.putExtra(ShoutActivity.EXTRA_VENUE_ZIP, venue.getZip());
    }

    private AlertDialog createCheckinResultDialog(CheckinResult checkinResult) {
        LayoutInflater inflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.checkin_result_dialog,
                (ViewGroup)findViewById(R.id.layout_root));

        String userId = PreferenceManager.getDefaultSharedPreferences(this).getString(
                Preferences.PREFERENCE_ID, "");

        WebView webView = (WebView)layout.findViewById(R.id.webView);
        webView.setBackgroundColor(0); // make it transparent... how do we do this in xml?
        String breakdownUrl = "http://playfoursquare.com/incoming/breakdown";
        String breakdownQuery = "?client=iphone&uid=" + userId + "&cid=" + checkinResult.getId();
        webView.loadUrl(breakdownUrl + breakdownQuery);

        TextView messageView = (TextView)layout.findViewById(R.id.messageTextView);
        messageView.setText(checkinResult.getMessage());

        return new AlertDialog.Builder(this) //
                .setView(layout) //
                .setIcon(android.R.drawable.ic_dialog_info) // icon
                .setTitle("Checked in @ " + checkinResult.getVenue().getName()) // title
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }) //
                .create();
    }

    class CheckinTask extends AsyncTask<Void, Void, CheckinResult> {

        @Override
        public void onPreExecute() {
            mCheckinButton.setEnabled(false);
            showDialog(DIALOG_CHECKIN_PROGRESS);
        }

        @Override
        public CheckinResult doInBackground(Void... params) {
            boolean isPrivate = !mFriendsCheckBox.isChecked();
            boolean twitter = mTwitterCheckBox.isChecked();
            String shout = TextUtils.isEmpty(mShoutEditText.getText()) ? null : mShoutEditText
                    .getText().toString();
            try {
                return Foursquared.getFoursquare().checkin(null, mStateHolder.venue.getId(), shout,
                        isPrivate, twitter);
            } catch (FoursquareError e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareError", e);
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
        public void onPostExecute(CheckinResult checkinResult) {
            if (DEBUG) Log.d(TAG, "CheckinTask: onPostExecute()");
            dismissDialog(DIALOG_CHECKIN_PROGRESS);
            if (checkinResult == null) {
                mCheckinButton.setEnabled(true);
                Toast.makeText(ShoutActivity.this, "Unable to checkin!", Toast.LENGTH_LONG).show();
                return;
            } else {
                setResult(Activity.RESULT_OK);
                createCheckinResultDialog(checkinResult).show();
            }
        }

        @Override
        public void onCancelled() {
            mCheckinButton.setEnabled(true);
            dismissDialog(DIALOG_CHECKIN_PROGRESS);
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    private static class StateHolder {
        // These are all enumerated because we currently cannot handle parceling venues! How sad!
        Venue venue = null;
        AsyncTask<Void, Void, CheckinResult> checkinTask = null;
    }

}
