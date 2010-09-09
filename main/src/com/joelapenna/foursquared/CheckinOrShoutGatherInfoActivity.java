/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquared.preferences.Preferences;

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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Can be presented as a dialog theme, collects data from the user for a checkin or a
 * shout. The foursquare api is the same for checkins and shouts. A checkin should just
 * contain a venue id. 
 * 
 * After the user has entered their data, this activity will finish itself and call 
 * either CheckinExecuteActivity or ShoutExecuteActivity. The only real difference
 * between them is what's displayed at the conclusion of the execution.
 * 
 * If doing a checkin, the user can also skip this activity and do a 'quick' checkin
 * by launching CheckinExecuteActivity directly. This will just use their saved preferences
 * to checkin at the specified venue, no optional shout message will be attached to
 * the checkin.
 * 
 * This dialog allows the user to supply the following information:
 * 
 * <ul>
 *  <li>Tell my Friends [yes|no]</li>
 *  <li>Tell Twitter [yes|no]</li>
 *  <li>Tell Facebook [yes|no]</li>
 *  <li>EditField for freeform shout text.</li>
 * </ul>
 * 
 * @date March 2, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public class CheckinOrShoutGatherInfoActivity extends Activity {
    public static final String TAG = "CheckinOrShoutGatherInfoActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;
    
    public static final String INTENT_EXTRA_IS_CHECKIN = Foursquared.PACKAGE_NAME
            + ".CheckinOrShoutGatherInfoActivity.INTENT_EXTRA_IS_CHECKIN";
    public static final String INTENT_EXTRA_IS_SHOUT = Foursquared.PACKAGE_NAME
            + ".CheckinOrShoutGatherInfoActivity.INTENT_EXTRA_IS_SHOUT";
    public static final String INTENT_EXTRA_VENUE_ID = Foursquared.PACKAGE_NAME
            + ".CheckinOrShoutGatherInfoActivity.INTENT_EXTRA_VENUE_ID";
    public static final String INTENT_EXTRA_VENUE_NAME = Foursquared.PACKAGE_NAME
            + ".CheckinOrShoutGatherInfoActivity.INTENT_EXTRA_VENUE_NAME";
    public static final String INTENT_EXTRA_TEXT_PREPOPULATE = Foursquared.PACKAGE_NAME
            + ".CheckinOrShoutGatherInfoActivity.INTENT_EXTRA_TEXT_PREPOPULATE";
    
    private StateHolder mStateHolder;
    
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
        if (DEBUG) Log.d(TAG, "onCreate()");
        setContentView(R.layout.checkin_or_shout_gather_info_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));
        
        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
        } else {

            if (getIntent().getExtras() != null) {
                if (getIntent().getBooleanExtra(INTENT_EXTRA_IS_CHECKIN, false)) {
                    // If a checkin, we require venue id and name.
                    String venueId = null;
                    if (getIntent().getExtras().containsKey(INTENT_EXTRA_VENUE_ID)) {
                        venueId = getIntent().getExtras().getString(INTENT_EXTRA_VENUE_ID);
                    } else {
                        Log.e(TAG, "CheckinOrShoutGatherInfoActivity requires intent extra INTENT_EXTRA_VENUE_ID for action type checkin.");
                        finish();
                        return;
                    }
        
                    String venueName = null;
                    if (getIntent().getExtras().containsKey(INTENT_EXTRA_VENUE_NAME)) {
                        venueName = getIntent().getExtras().getString(INTENT_EXTRA_VENUE_NAME);
                    } else {
                        Log.e(TAG, "CheckinOrShoutGatherInfoActivity requires intent extra INTENT_EXTRA_VENUE_NAME for action type checkin.");
                        finish();
                        return;
                    }
                    
                    mStateHolder = new StateHolder(true, venueId, venueName);
                }
                else if (getIntent().getBooleanExtra(INTENT_EXTRA_IS_SHOUT, false)) {
                    // If a shout, we don't require anything at all.
                    mStateHolder = new StateHolder(false, null, null);
                }
                else {
                    Log.e(TAG, "CheckinOrShoutGatherInfoActivity requires intent extra parameter for action type.");
                    finish();
                    return;
                }
                
                if (getIntent().hasExtra(INTENT_EXTRA_TEXT_PREPOPULATE)) {
                    EditText editShout = (EditText)findViewById(R.id.editTextShout);
                    editShout.setText(getIntent().getStringExtra(INTENT_EXTRA_TEXT_PREPOPULATE));
                }
            }
            else {
                Log.e(TAG, "CheckinOrShoutGatherInfoActivity requires intent extras parameters, none found.");
                finish();
                return;
            }
        }

        ensureUi();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (isFinishing()) {
            unregisterReceiver(mLoggedOutReceiver);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mStateHolder;
    }

    private void ensureUi() {
        if (mStateHolder.getIsCheckin()) {
            setTitle(getResources().getString(R.string.checkin_title_checking_in, 
                    mStateHolder.getVenueName()));
        } else {
            setTitle(getResources().getString(R.string.shout_action_label));    
        }
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        CheckBox cbTellFriends = (CheckBox)findViewById(R.id.checkboxTellFriends);
        cbTellFriends.setChecked(settings.getBoolean(Preferences.PREFERENCE_SHARE_CHECKIN, true));
        
        CheckBox cbTellFollowers = (CheckBox)findViewById(R.id.checkboxTellFollowers);
        if (settings.getBoolean(Preferences.PREFERENCE_CAN_HAVE_FOLLOWERS, false)) {
            cbTellFollowers.setVisibility(View.VISIBLE);
        }
        
        CheckBox cbTellTwitter = (CheckBox)findViewById(R.id.checkboxTellTwitter);
        if (settings.getBoolean(Preferences.PREFERENCE_TWITTER_CHECKIN, false) &&
            !TextUtils.isEmpty(settings.getString(Preferences.PREFERENCE_TWITTER_HANDLE, ""))) {
            cbTellTwitter.setChecked(true);
        }
        
        CheckBox cbTellFacebook = (CheckBox)findViewById(R.id.checkboxTellFacebook);
        if (settings.getBoolean(Preferences.PREFERENCE_FACEBOOK_CHECKIN, false) &&
            !TextUtils.isEmpty(settings.getString(Preferences.PREFERENCE_FACEBOOK_HANDLE, ""))) {
            cbTellFacebook.setChecked(true);
        }
        
        Button btnCheckin = (Button)findViewById(R.id.btnCheckin);
        if (mStateHolder.getIsCheckin()) {
            btnCheckin.setText(getResources().getString(R.string.checkin_action_label));
        } else {
            btnCheckin.setText(getResources().getString(R.string.shout_action_label));
        } 
        btnCheckin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkin();
            }
        });
    }
    
    private void checkin() {
        CheckBox cbTellFriends = (CheckBox)findViewById(R.id.checkboxTellFriends);
        CheckBox cbTellFollowers = (CheckBox)findViewById(R.id.checkboxTellFollowers);
        CheckBox cbTellTwitter = (CheckBox)findViewById(R.id.checkboxTellTwitter);
        CheckBox cbTellFacebook = (CheckBox)findViewById(R.id.checkboxTellFacebook);
        EditText editShout = (EditText)findViewById(R.id.editTextShout);
     
        // After we start the activity, we don't have to stick around any longer.
        // We want to forward the resultCode of CheckinExecuteActivity to our
        // caller though, so add the FLAG_ACTIVITY_FORWARD_RESULT on the intent.
        Intent intent = new Intent();
        if (mStateHolder.getIsCheckin()) {
            intent.setClass(this, CheckinExecuteActivity.class);
            intent.putExtra(CheckinExecuteActivity.INTENT_EXTRA_VENUE_ID, mStateHolder.getVenueId());
            intent.putExtra(CheckinExecuteActivity.INTENT_EXTRA_SHOUT, editShout.getText().toString());
            intent.putExtra(CheckinExecuteActivity.INTENT_EXTRA_TELL_FRIENDS, cbTellFriends.isChecked());
            intent.putExtra(CheckinExecuteActivity.INTENT_EXTRA_TELL_FOLLOWERS, cbTellFollowers.isChecked());
            intent.putExtra(CheckinExecuteActivity.INTENT_EXTRA_TELL_TWITTER, cbTellTwitter.isChecked());
            intent.putExtra(CheckinExecuteActivity.INTENT_EXTRA_TELL_FACEBOOK, cbTellFacebook.isChecked());
        }
        else {
            intent.setClass(this, ShoutExecuteActivity.class);
            intent.putExtra(ShoutExecuteActivity.INTENT_EXTRA_SHOUT, editShout.getText().toString());
            intent.putExtra(ShoutExecuteActivity.INTENT_EXTRA_TELL_FRIENDS, cbTellFriends.isChecked());
            intent.putExtra(ShoutExecuteActivity.INTENT_EXTRA_TELL_FOLLOWERS, cbTellFollowers.isChecked());
            intent.putExtra(ShoutExecuteActivity.INTENT_EXTRA_TELL_TWITTER, cbTellTwitter.isChecked());
            intent.putExtra(ShoutExecuteActivity.INTENT_EXTRA_TELL_FACEBOOK, cbTellFacebook.isChecked());
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(intent);
        finish(); 
    }
    
    private static class StateHolder {
        private boolean mIsCheckin; // either a checkin, or a shout.
        private String mVenueId;
        private String mVenueName;

        public StateHolder(boolean isCheckin, String venueId, String venueName) {
            mIsCheckin = isCheckin;
            mVenueId = venueId;
            mVenueName = venueName;
        }

        public boolean getIsCheckin() {
            return mIsCheckin;
        }
        
        public String getVenueId() {
            return mVenueId;
        }
        
        public String getVenueName() {
            return mVenueName;
        }
    }
}
