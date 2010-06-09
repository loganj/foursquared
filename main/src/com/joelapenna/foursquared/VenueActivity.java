/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquare.util.VenueUtils;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.MenuUtils;
import com.joelapenna.foursquared.util.NotificationsUtil;
import com.joelapenna.foursquared.util.TabsUtil;
import com.joelapenna.foursquared.util.UserUtils;
import com.joelapenna.foursquared.widget.VenueView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Observable;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com)
 *         -Replaced shout activity with CheckinGatherInfoActivity (3/10/2010).
 */
public class VenueActivity extends TabActivity {
    private static final String TAG = "VenueActivity";

    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final int DIALOG_TIPADD = 1;

    private static final int MENU_CHECKIN = 1;
    private static final int MENU_TIPADD = 2;
    private static final int MENU_CALL = 3;
    private static final int MENU_EDIT_VENUE = 4;
    private static final int MENU_MYINFO = 5;

    private static final int RESULT_CODE_ACTIVITY_CHECKIN_EXECUTE = 1;

    final VenueObservable venueObservable = new VenueObservable();
    private final StateHolder mStateHolder = new StateHolder();
    private final HashSet<Object> mProgressBarTasks = new HashSet<Object>();
    private VenueView mVenueView;
    private boolean mCheckedInSuccessfully = false;

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
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.venue_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        initTabHost();

        StateHolder holder = (StateHolder) getLastNonConfigurationInstance();

        mVenueView = (VenueView) findViewById(R.id.venue);
        mVenueView.setCheckinButtonOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is a quick checkin, so we can just execute the checkin directly.
                // There's a setting in preferences which can block this behavior though.
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(
                        VenueActivity.this);
                if (settings.getBoolean(Preferences.PREFERENCE_IMMEDIATE_CHECKIN, false)) {
                    startCheckinQuick();
                } else {
                    startCheckin();   
                }
            }
        });
        mVenueView.setSpecialOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showWebViewForSpecial();
            }
        });

        if (holder != null && VenueUtils.isValid(holder.venue)) {
            if (DEBUG) Log.d(TAG, "Restoring Venue: " + holder.venue);
            setVenue(holder.venue);
        } else {
            new VenueTask().execute(getIntent().getExtras().getString(Foursquared.EXTRA_VENUE_ID));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedOutReceiver);
    }
    
    @Override 
    public void onResume() {
        super.onResume();
        
        mVenueView.updateCheckinButtonText();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(Menu.NONE, MENU_CHECKIN, 1, R.string.checkin_action_label) //
                .setIcon(R.drawable.ic_menu_checkin);

        menu.add(Menu.NONE, MENU_TIPADD, 2, R.string.add_a_tip).setIcon(
                android.R.drawable.ic_menu_set_as);

        menu.add(Menu.NONE, MENU_CALL, 3, R.string.call).setIcon(android.R.drawable.ic_menu_call);

        menu.add(Menu.NONE, MENU_EDIT_VENUE, 4, R.string.edit_venue).setIcon(
                android.R.drawable.ic_menu_edit);
        
        int sdk = new Integer(Build.VERSION.SDK).intValue();
        if (sdk < 4) {
            int menuIcon = UserUtils.getDrawableForMeMenuItemByGender(
                ((Foursquared) getApplication()).getUserGender());
            menu.add(Menu.NONE, MENU_MYINFO, Menu.NONE, R.string.myinfo_label) //
                    .setIcon(menuIcon);
        }
        
        MenuUtils.addPreferencesToMenu(this, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean checkinEnabled = (mStateHolder.venueId != null) && !mCheckedInSuccessfully;
        menu.findItem(MENU_CHECKIN).setEnabled(checkinEnabled);

        boolean callEnabled = mStateHolder.venue != null
                && !TextUtils.isEmpty(mStateHolder.venue.getPhone());
        menu.findItem(MENU_CALL).setEnabled(callEnabled);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CHECKIN:   
                startCheckin();
                return true;
            case MENU_TIPADD:
                showDialog(DIALOG_TIPADD);
                return true;
            case MENU_CALL:
                try {
                    Intent dial = new Intent();
                    dial.setAction(Intent.ACTION_DIAL);
                    dial.setData(Uri.parse("tel:" + mStateHolder.venue.getPhone()));
                    startActivity(dial);
                } catch (Exception ex) {
                    Log.e(TAG, "Error starting phone dialer intent.", ex);
                    Toast.makeText(this, "Sorry, we couldn't find any app to place a phone call!",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            case MENU_EDIT_VENUE:
                Intent intentEditVenue = new Intent(this, EditVenueOptionsActivity.class);
                intentEditVenue.putExtra(EditVenueOptionsActivity.EXTRA_VENUE_ID, mStateHolder.venueId);
                startActivity(intentEditVenue);
                return true;
            case MENU_MYINFO:
                Intent intentUser = new Intent(VenueActivity.this, UserDetailsActivity.class);
                intentUser.putExtra(UserDetailsActivity.EXTRA_USER_ID,
                        ((Foursquared) getApplication()).getUserId());
                startActivity(intentUser);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Dialog onCreateDialog(int id) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout;

        switch (id) {
            case DIALOG_TIPADD:
                layout = inflater.inflate(R.layout.tip_add_dialog,
                        (ViewGroup) findViewById(R.id.layout_root));

                final EditText editText = (EditText) layout.findViewById(R.id.editText);
                final Spinner spinner = (Spinner) layout.findViewById(R.id.spinner);

                return new AlertDialog.Builder(this) //
                        .setView(layout) //
                        .setIcon(android.R.drawable.ic_dialog_alert) // icon
                        .setTitle("Add a Tip") // title
                        .setPositiveButton("Add", new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String tip = editText.getText().toString();
                                String type = ((String) spinner.getSelectedItem()).toLowerCase();
                                editText.setText("");
                                spinner.setSelection(0);
                                new TipAddTask().execute(tip, type);
                            }
                        }) //
                        .setNegativeButton("Cancel", new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                editText.setText("");
                                spinner.setSelection(0);
                                dismissDialog(DIALOG_TIPADD);
                            }
                        }).create();
        }
        return null;
    }

    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        // If the tip add was a success we must have set mStateHolder.tip. If
        // that is the case, then
        // we clear the dialog because clearly they're looking to add a new tip
        // and not post the
        // same one again.
        if (id == DIALOG_TIPADD && mStateHolder.tip != null) {
            ((EditText) dialog.findViewById(R.id.editText)).setText("");
            mStateHolder.tip = null;
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mStateHolder;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_CODE_ACTIVITY_CHECKIN_EXECUTE:
                if (resultCode == Activity.RESULT_OK) {
                    mCheckedInSuccessfully = true;
                    mVenueView.setCheckinButtonEnabled(false);
                }
                break;
        }
    }

    public void startProgressBar(String taskId) {
        boolean added = mProgressBarTasks.add(taskId);
        if (!added) {
            if (DEBUG) Log.d(TAG, "Received start for already tracked task. Ignoring");
        }
        setProgressBarIndeterminateVisibility(true);
    }

    public void stopProgressBar(String taskId) {
        boolean removed = mProgressBarTasks.remove(taskId);
        if (!removed) {
            if (DEBUG) Log.d(TAG, "Received stop for untracked task. Ignoring");
        } else if (mProgressBarTasks.isEmpty()) {
            setProgressBarIndeterminateVisibility(false);
        }
    }

    private void initTabHost() {
        final TabHost tabHost = this.getTabHost();
        String tag;
        Intent intent;

        tag = (String) this.getText(R.string.venue_checkins_tab);
        intent = new Intent(this, VenueCheckinsActivity.class);
        TabsUtil.addNativeLookingTab(this, tabHost, "t1", tag, R.drawable.friends_tab, intent);

        tag = (String) this.getText(R.string.map_label);
        intent = new Intent(this, VenueMapActivity.class);
        TabsUtil.addNativeLookingTab(this, tabHost, "t2", tag, R.drawable.map_tab, intent);
        
        tag = (String) this.getText(R.string.venue_info_tab);
        intent = new Intent(this, VenueTipsActivity.class);
        TabsUtil.addNativeLookingTab(this, tabHost, "t3", tag, R.drawable.tips_tab, intent);
    }

    private void onVenueSet() {
        Venue venue = mStateHolder.venue;
        if (DEBUG) Log.d(TAG, "onVenueSet:" + venue.getName());
        setTitle(venue.getName() + " - Foursquare");
        mVenueView.setVenue(venue);
        mVenueView.setCheckinButtonEnabled(mStateHolder.venueId != null);
    }

    private void setVenue(Venue venue) {
        mStateHolder.venue = venue;
        mStateHolder.venueId = venue.getId();
        venueObservable.notifyObservers(venue);
        onVenueSet();
    }
    
    private void startCheckin() {
        Intent intent = new Intent(this, CheckinOrShoutGatherInfoActivity.class);
        intent.putExtra(CheckinOrShoutGatherInfoActivity.INTENT_EXTRA_IS_CHECKIN, true);
        intent.putExtra(CheckinOrShoutGatherInfoActivity.INTENT_EXTRA_VENUE_ID, mStateHolder.venue.getId());
        intent.putExtra(CheckinOrShoutGatherInfoActivity.INTENT_EXTRA_VENUE_NAME, mStateHolder.venue.getName());
        startActivityForResult(intent, RESULT_CODE_ACTIVITY_CHECKIN_EXECUTE);
    }
    
    private void startCheckinQuick() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean tellFriends = settings.getBoolean(Preferences.PREFERENCE_SHARE_CHECKIN, true);
        boolean tellTwitter = settings.getBoolean(Preferences.PREFERENCE_TWITTER_CHECKIN, false);
        boolean tellFacebook = settings.getBoolean(Preferences.PREFERENCE_FACEBOOK_CHECKIN, false);
        
        Intent intent = new Intent(VenueActivity.this, CheckinExecuteActivity.class);
        intent.putExtra(CheckinExecuteActivity.INTENT_EXTRA_VENUE_ID, mStateHolder.venue.getId());
        intent.putExtra(CheckinExecuteActivity.INTENT_EXTRA_SHOUT, "");
        intent.putExtra(CheckinExecuteActivity.INTENT_EXTRA_TELL_FRIENDS, tellFriends);
        intent.putExtra(CheckinExecuteActivity.INTENT_EXTRA_TELL_TWITTER, tellTwitter);
        intent.putExtra(CheckinExecuteActivity.INTENT_EXTRA_TELL_FACEBOOK, tellFacebook);
        startActivityForResult(intent, RESULT_CODE_ACTIVITY_CHECKIN_EXECUTE);
    }
    
    private void showWebViewForSpecial() {
        
        Intent intent = new Intent(this, SpecialWebViewActivity.class);
        intent.putExtra(SpecialWebViewActivity.EXTRA_CREDENTIALS_USERNAME, 
                PreferenceManager.getDefaultSharedPreferences(this).getString(Preferences.PREFERENCE_LOGIN, ""));
        intent.putExtra(SpecialWebViewActivity.EXTRA_CREDENTIALS_PASSWORD, 
                PreferenceManager.getDefaultSharedPreferences(this).getString(Preferences.PREFERENCE_PASSWORD, ""));
        intent.putExtra(SpecialWebViewActivity.EXTRA_SPECIAL_ID, 
                mStateHolder.venue.getSpecials().get(0).getId());
        startActivity(intent);
    }

    class VenueObservable extends Observable {
        @Override
        public void notifyObservers(Object data) {
            setChanged();
            super.notifyObservers(data);
        }

        public Venue getVenue() {
            return mStateHolder.venue;
        }
    }

    private class VenueTask extends AsyncTask<String, Void, Venue> {
        private static final String PROGRESS_BAR_TASK_ID = TAG + "VenueTask";

        private Exception mReason;

        @Override
        protected void onPreExecute() {
            startProgressBar(PROGRESS_BAR_TASK_ID);
        }

        @Override
        protected Venue doInBackground(String... params) {
            try {
                return ((Foursquared) getApplication()).getFoursquare().venue(
                        params[0],
                        LocationUtils.createFoursquareLocation(((Foursquared) getApplication())
                                .getLastKnownLocation()));
            } catch (Exception e) {
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Venue venue) {
            try {
                if (VenueUtils.isValid(venue)) {
                    setVenue(venue);
                } else {
                    NotificationsUtil.ToastReasonForFailure(VenueActivity.this, mReason);
                    finish();
                }
            } finally {
                stopProgressBar(PROGRESS_BAR_TASK_ID);
            }
        }

        @Override
        protected void onCancelled() {
            stopProgressBar(PROGRESS_BAR_TASK_ID);
        }
    }

    private class TipAddTask extends AsyncTask<String, Void, Tip> {
        private static final String PROGRESS_BAR_TASK_ID = TAG + "TipAddTask";

        private Exception mReason;

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "TipAddTask: onPreExecute()");
            startProgressBar(PROGRESS_BAR_TASK_ID);
        }

        @Override
        public Tip doInBackground(String... params) {
            if (DEBUG) Log.d(TAG, "CheckinsTask: doInBackground()");
            try {
                assert params.length == 2;
                String tip = params[0];
                String type = params[1];
                return ((Foursquared) getApplication()).getFoursquare().addTip(
                        mStateHolder.venueId,
                        tip,
                        type,
                        LocationUtils.createFoursquareLocation(((Foursquared) getApplication())
                                .getLastKnownLocation()));
            } catch (Exception e) {
                mReason = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Tip tip) {
            if (DEBUG) Log.d(TAG, "TipAddTask: onPostExecute()");
            try {
                if (tip == null) {
                    NotificationsUtil.ToastReasonForFailure(VenueActivity.this, mReason);
                } else {
                    String tipToastString = "Added Tip #" + tip.getId() + " " + tip.getText();
                    // Refresh the tips list.
                    Toast.makeText(VenueActivity.this, tipToastString, Toast.LENGTH_LONG).show();
                    new VenueTask().execute(mStateHolder.venueId);
                }
            } finally {
                stopProgressBar(PROGRESS_BAR_TASK_ID);
                mStateHolder.tip = null;
            }
        }

        @Override
        public void onCancelled() {
            stopProgressBar(PROGRESS_BAR_TASK_ID);
        }
    }

    private static final class StateHolder {
        Venue venue = null;

        String venueId = null;

        Tip tip = null;
    }
}
