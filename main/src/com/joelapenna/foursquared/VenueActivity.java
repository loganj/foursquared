/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquare.util.VenueUtils;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.util.MenuUtils;
import com.joelapenna.foursquared.util.NotificationsUtil;
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
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
 */
public class VenueActivity extends TabActivity {
    private static final String TAG = "VenueActivity";

    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final int DIALOG_TIPADD = 1;

    private static final int MENU_SHOUT = 1;
    private static final int MENU_TIPADD = 2;
    private static final int MENU_CALL = 3;
    private static final int MENU_MYINFO = 4;

    private static final int RESULT_SHOUT = 1;

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
                Intent intent = new Intent(VenueActivity.this, ShoutActivity.class);
                ShoutActivity.venueIntoIntentExtras(mStateHolder.venue, intent);
                startActivityForResult(intent, RESULT_SHOUT);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(Menu.NONE, MENU_SHOUT, 1, R.string.checkin_action_label) //
                .setIcon(R.drawable.ic_menu_checkin);

        menu.add(Menu.NONE, MENU_TIPADD, 2, R.string.add_a_tip).setIcon(
                android.R.drawable.ic_menu_set_as);

        menu.add(Menu.NONE, MENU_CALL, 3, R.string.call).setIcon(android.R.drawable.ic_menu_call);

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
        menu.findItem(MENU_SHOUT).setEnabled(checkinEnabled);

        boolean callEnabled = mStateHolder.venue != null
                && !TextUtils.isEmpty(mStateHolder.venue.getPhone());
        menu.findItem(MENU_CALL).setEnabled(callEnabled);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SHOUT:
                Intent intent = new Intent(VenueActivity.this, ShoutActivity.class);
                ShoutActivity.venueIntoIntentExtras(mStateHolder.venue, intent);
                // No matter what the immediate checkin user preference is,
                // never auto-checkin
                // (always present the shout prompt).
                intent.putExtra(ShoutActivity.EXTRA_IMMEDIATE_CHECKIN, false);
                startActivityForResult(intent, RESULT_SHOUT);
                return true;
            case MENU_TIPADD:
                showDialog(DIALOG_TIPADD);
                return true;
            case MENU_CALL:
                Uri phoneUri = Uri.fromParts("tel", mStateHolder.venue.getPhone(), null);
                startActivity(new Intent(Intent.ACTION_CALL, phoneUri));
                // nothing sucka.
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
        if (resultCode == Activity.RESULT_OK) {
            mCheckedInSuccessfully = true;
            mVenueView.setCheckinButtonEnabled(false);
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
        Resources resources = getResources();
        String tag;
        Intent intent;

        tag = (String) this.getText(R.string.venue_checkin_activity_label);
        intent = new Intent(this, VenueCheckinsActivity.class);
        tabHost.addTab(tabHost.newTabSpec(tag) //
                .setIndicator(getString(R.string.venue_checkins_tab),
                        resources.getDrawable(R.drawable.friends_tab)).setContent(intent));

        tag = (String) this.getText(R.string.venue_info_activity_label);
        intent = new Intent(this, VenueMapActivity.class);
        tabHost.addTab(tabHost.newTabSpec(tag) //
                .setIndicator(getString(R.string.map_label),
                        resources.getDrawable(R.drawable.map_tab)).setContent(intent));

        tag = (String) this.getText(R.string.venue_tips_activity_label);
        intent = new Intent(this, VenueTipsActivity.class);
        tabHost.addTab(tabHost.newTabSpec(tag) //
                .setIndicator(getString(R.string.venue_info_tab),
                        resources.getDrawable(R.drawable.tips_tab)).setContent(intent));
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
