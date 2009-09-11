/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.NotificationsUtil;
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

    private static final int RESULT_SHOUT = 1;

    final VenueObservable venueObservable = new VenueObservable();
    final CheckinsObservable checkinsObservable = new CheckinsObservable();

    private final StateHolder mStateHolder = new StateHolder();

    private final HashSet<Object> mProgressBarTasks = new HashSet<Object>();

    private VenueView mVenueView;

    private boolean mCheckedInSuccessfully = false;

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
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.venue_activity);
        registerReceiver(mLoggedInReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        initTabHost();

        StateHolder holder = (StateHolder)getLastNonConfigurationInstance();

        mVenueView = (VenueView)findViewById(R.id.venue);
        mVenueView.setCheckinButtonOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VenueActivity.this, ShoutActivity.class);
                ShoutActivity.venueIntoIntentExtras(mStateHolder.venue, intent);
                startActivityForResult(intent, RESULT_SHOUT);
            }
        });

        if (holder != null) {
            if (holder.venue == null) {
                new VenueTask().execute(mStateHolder.venueId);
            } else {
                if (DEBUG) Log.d(TAG, "Restoring Venue: " + holder.venue);
                setVenue(holder.venue);
            }

            if (holder.checkins == null) {
                new CheckinsTask().execute();
            } else {
                if (DEBUG) Log.d(TAG, "Restoring checkins: " + holder.checkins);
                setCheckins(holder.checkins);
            }
        } else {
            mStateHolder.venueId = getIntent().getExtras().getString(Foursquared.EXTRA_VENUE_ID);
            new VenueTask().execute(mStateHolder.venueId);
            new CheckinsTask().execute();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedInReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(Menu.NONE, MENU_SHOUT, 1, R.string.checkin_action_label) //
                .setIcon(R.drawable.ic_menu_checkin);

        menu.add(Menu.NONE, MENU_TIPADD, 2, R.string.add_a_tip).setIcon(
                android.R.drawable.ic_menu_set_as);

        menu.add(Menu.NONE, MENU_CALL, 3, R.string.call).setIcon(android.R.drawable.ic_menu_call);

        Foursquared.addPreferencesToMenu(this, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean checkinEnabled = (mStateHolder.venue != null) && !mCheckedInSuccessfully;
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
                // No matter what the immediate checkin user preference is, never auto-checkin
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
            default:
                return false;
        }
    }

    @Override
    public Dialog onCreateDialog(int id) {
        LayoutInflater inflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout;

        switch (id) {
            case DIALOG_TIPADD:
                layout = inflater.inflate(R.layout.tip_add_dialog,
                        (ViewGroup)findViewById(R.id.layout_root));

                final EditText editText = (EditText)layout.findViewById(R.id.editText);
                final Spinner spinner = (Spinner)layout.findViewById(R.id.spinner);

                return new AlertDialog.Builder(this) //
                        .setView(layout) //
                        .setIcon(android.R.drawable.ic_dialog_alert) // icon
                        .setTitle("Add a Tip") // title
                        .setPositiveButton("Add", new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String tip = editText.getText().toString();
                                String type = ((String)spinner.getSelectedItem()).toLowerCase();
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

        tag = (String)this.getText(R.string.venue_tips_activity_label);
        intent = new Intent(this, VenueTipsActivity.class);
        tabHost.addTab(tabHost.newTabSpec(tag) //
                .setIndicator(resources.getString(R.string.venue_info_tab),
                        resources.getDrawable(R.drawable.venue_info_tab)).setContent(intent));

        tag = (String)this.getText(R.string.venue_info_activity_label);
        intent = new Intent(this, VenueMapActivity.class);
        tabHost.addTab(tabHost.newTabSpec(tag) //
                .setIndicator(resources.getString(R.string.map_label),
                        resources.getDrawable(R.drawable.map_tab)).setContent(intent));

        tag = (String)this.getText(R.string.venue_checkin_activity_label);
        intent = new Intent(this, VenueCheckinsActivity.class);
        tabHost.addTab(tabHost.newTabSpec(tag) //
                .setIndicator(resources.getString(R.string.venue_checkins_tab),
                        resources.getDrawable(R.drawable.recent_checkins_tab)).setContent(intent));

    }

    private void onVenueSet(Venue venue) {
        if (DEBUG) Log.d(TAG, "onVenueSet:" + venue.getName());
        setTitle(venue.getName() + " - Foursquare");
        mVenueView.setVenue(venue);
        mVenueView.setCheckinButtonEnabled(true);
    }

    private void setVenue(Venue venue) {
        mStateHolder.venue = venue;
        venueObservable.notifyObservers(venue);
        onVenueSet(mStateHolder.venue);

    }

    private void setCheckins(Group checkins) {
        mStateHolder.checkins = checkins;
        checkinsObservable.notifyObservers(checkins);
    }

    class VenueObservable extends Observable {
        public void notifyObservers(Object data) {
            setChanged();
            super.notifyObservers(data);
        }

        public Venue getVenue() {
            return mStateHolder.venue;
        }
    }

    class CheckinsObservable extends Observable {
        public void notifyObservers(Object data) {
            setChanged();
            super.notifyObservers(data);
        }

        public Group getCheckins() {
            return mStateHolder.checkins;
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
                return ((Foursquared)getApplication()).getFoursquare().venue(params[0]);
            } catch (Exception e) {
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Venue venue) {
            try {
                if (venue == null) {
                    NotificationsUtil.ToastReasonForFailure(VenueActivity.this, mReason);
                    finish();
                } else {
                    setVenue(venue);
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
                return ((Foursquared)getApplication()).getFoursquare().addTip(mStateHolder.venueId,
                        tip, type);
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
            }
        }

        @Override
        public void onCancelled() {
            stopProgressBar(PROGRESS_BAR_TASK_ID);
        }
    }

    private class CheckinsTask extends AsyncTask<Void, Void, Group> {
        private static final String PROGRESS_BAR_TASK_ID = TAG + "CheckinsTask";

        private Exception mReason;

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "CheckinsTask: onPreExecute()");
            startProgressBar(PROGRESS_BAR_TASK_ID);
        }

        @Override
        public Group doInBackground(Void... params) {
            if (DEBUG) Log.d(TAG, "CheckinsTask: doInBackground()");
            try {
                return ((Foursquared)getApplication()).getFoursquare().checkins(null);
            } catch (Exception e) {
                mReason = e;
            }
            return null;
        }

        @Override
        public void onPostExecute(Group checkins) {
            if (DEBUG) Log.d(TAG, "CheckinTask: onPostExecute()");
            try {
                setCheckins(filterCheckins(checkins));
            } catch (Exception e) {
                NotificationsUtil.ToastReasonForFailure(VenueActivity.this, mReason);
            } finally {
                stopProgressBar(PROGRESS_BAR_TASK_ID);
            }
        }

        @Override
        public void onCancelled() {
            stopProgressBar(PROGRESS_BAR_TASK_ID);
        }

        private Group filterCheckins(Group checkins) {
            Group filteredCheckins = new Group();
            if (checkins == null) {
                Log.d(TAG, "setCheckins provided null, faking it.");
                filteredCheckins.setType("Recent Checkins");
            } else {
                filteredCheckins.setType(checkins.getType());
                for (int i = 0; i < checkins.size(); i++) {
                    Checkin checkin = (Checkin)checkins.get(i);
                    if (checkin.getVenue() != null
                            && checkin.getVenue().getId().equals(mStateHolder.venueId)) {
                        filteredCheckins.add(checkin);
                    }
                }
            }
            return filteredCheckins;
        }

    }

    private static final class StateHolder {
        Venue venue = null;
        String venueId = null;
        Group checkins = null;
    }
}
