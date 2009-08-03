/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.CheckinResult;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.StringFormatters;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashSet;
import java.util.Observable;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueActivity extends TabActivity {
    private static final String TAG = "VenueActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String EXTRA_VENUE = "com.joelapenna.foursquared.VenueActivity.VenueId";

    private static final int DIALOG_CHECKIN = 0;
    private static final int DIALOG_TIPADD = 1;

    private static final int MENU_GROUP_SHOUT = 1;

    private static final int MENU_SHOUT = 1;
    private static final int MENU_TIPADD = 2;

    final VenueObservable venueObservable = new VenueObservable();
    final CheckinsObservable checkinsObservable = new CheckinsObservable();

    private final StateHolder mStateHolder = new StateHolder();

    private final HashSet<Object> mProgressBarTasks = new HashSet<Object>();

    private MenuItem mShoutMenuItem;
    private MenuItem mAddTipMenuItem;
    private Button mCheckinButton;

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

        mCheckinButton = (Button)findViewById(R.id.checkinButton);
        mCheckinButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new VenueCheckinTask().execute(mStateHolder.venue);
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
            if (holder.checkinResult != null) {
                mStateHolder.checkinResult = holder.checkinResult;
            }
        } else {
            mStateHolder.venueId = getIntent().getExtras().getString(EXTRA_VENUE);
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

        mShoutMenuItem = menu.add(MENU_GROUP_SHOUT, MENU_SHOUT, 1, "Shout!") //
                .setIcon(android.R.drawable.ic_menu_add);

        mAddTipMenuItem = menu.add(Menu.NONE, MENU_TIPADD, 4, "Add Tip") //
                .setIcon(android.R.drawable.ic_menu_set_as);

        Foursquared.addPreferencesToMenu(this, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean checkinEnabled = (mStateHolder.venue != null)
                && (mStateHolder.checkinResult == null);
        menu.setGroupEnabled(mShoutMenuItem.getGroupId(), checkinEnabled);
        mAddTipMenuItem.setEnabled(checkinEnabled);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SHOUT:
                new VenueCheckinTask().execute(mStateHolder.venue);
                return true;
            case MENU_TIPADD:
                showDialog(DIALOG_TIPADD);
            default:
                return false;
        }
    }

    @Override
    public Dialog onCreateDialog(int id) {
        LayoutInflater inflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout;

        switch (id) {
            case DIALOG_CHECKIN:
                CheckinResult checkinResult = mStateHolder.checkinResult;
                layout = inflater.inflate(R.layout.checkin_dialog,
                        (ViewGroup)findViewById(R.id.layout_root));
                String userId = PreferenceManager.getDefaultSharedPreferences(this).getString(
                        Preferences.PREFERENCE_ID, "");

                WebView webView = (WebView)layout.findViewById(R.id.webView);
                webView.setBackgroundColor(0);  // make it transparent... how do we do this in xml?
                String breakdownUrl = "http://playfoursquare.com/incoming/breakdown";
                String breakdownQuery = "?client=iphone&uid=" + userId + "&cid=" + checkinResult.getId();
                webView.loadUrl(breakdownUrl + breakdownQuery);

                TextView messageView = (TextView)layout.findViewById(R.id.messageTextView);
                messageView.setText(checkinResult.getMessage());

                return new AlertDialog.Builder(this) //
                        .setView(layout) //
                        .setIcon(android.R.drawable.ic_dialog_info) // icon
                        .setTitle("Checked in @ " + checkinResult.getVenue().getName()) // title
                        .create();

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

        tag = (String)this.getText(R.string.venue_tips_activity_name);
        intent = new Intent(this, VenueTipsActivity.class);
        tabHost.addTab(tabHost.newTabSpec(tag)
                // Info Tab
                .setIndicator("",
                        getResources().getDrawable(android.R.drawable.ic_menu_info_details))
                .setContent(intent) // The contained activity
                );

        tag = (String)this.getText(R.string.venue_info_activity_name);
        intent = new Intent(this, VenueMapActivity.class);
        tabHost.addTab(tabHost.newTabSpec(tag)
                // Info Tab
                .setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_mapmode))
                .setContent(intent) // The contained activity
                );

        tag = (String)this.getText(R.string.venue_checkin_activity_name);
        intent = new Intent(this, VenueCheckinActivity.class);
        tabHost.addTab(tabHost.newTabSpec(tag)
                // Checkin Tab
                .setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_agenda))
                .setContent(intent) // The contained activity
                );

    }

    private void onVenueSet(Venue venue) {
        if (DEBUG) Log.d(TAG, "onVenueSet:" + venue.getName());
        setTitle(venue.getName() + " - Foursquare");
        TextView name = (TextView)findViewById(R.id.venueName);
        TextView locationLine1 = (TextView)findViewById(R.id.venueLocationLine1);
        TextView locationLine2 = (TextView)findViewById(R.id.venueLocationLine2);

        name.setText(venue.getName());
        locationLine1.setText(venue.getAddress());

        String line2 = StringFormatters.getVenueLocationCrossStreetOrCity(venue);
        if (line2 != null) {
            locationLine2.setText(line2);
        }

        mCheckinButton.setEnabled(true);
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

        @Override
        protected void onPreExecute() {
            startProgressBar(PROGRESS_BAR_TASK_ID);
        }

        @Override
        protected Venue doInBackground(String... params) {
            try {
                return Foursquared.getFoursquare().venue(params[0]);
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
        protected void onPostExecute(Venue venue) {
            try {
                if (venue == null) {
                    Toast.makeText(VenueActivity.this, "Unable to lookup the venue.",
                            Toast.LENGTH_LONG).show();
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

    private class VenueCheckinTask extends AsyncTask<Venue, Void, CheckinResult> {

        private static final String PROGRESS_BAR_TASK_ID = TAG + "AddCheckinAsyncTask";

        @Override
        public void onPreExecute() {
            mCheckinButton.setEnabled(false);
            if (DEBUG) Log.d(TAG, "VenueCheckinTask: onPreExecute()");
            startProgressBar(PROGRESS_BAR_TASK_ID);

        }

        @Override
        public CheckinResult doInBackground(Venue... params) {
            if (DEBUG) Log.d(TAG, "VenueCheckinTask: doInBackground()");
            try {
                final Venue venue = params[0];
                if (DEBUG) Log.d(TAG, "Checking in to: " + venue.getName());

                SharedPreferences settings = PreferenceManager
                        .getDefaultSharedPreferences(VenueActivity.this);
                boolean silent = !settings.getBoolean(Preferences.PREFERENCE_SHARE_CHECKIN, true);
                boolean twitter = settings
                        .getBoolean(Preferences.PREFERENCE_TWITTER_CHECKIN, false);
                return Foursquared.getFoursquare().checkin(venue.getId(), null, null, silent,
                        twitter);
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
            if (DEBUG) Log.d(TAG, "VenueCheckinTask: onPostExecute()");
            try {
                mStateHolder.checkinResult = checkinResult;
                if (checkinResult == null) {
                    mCheckinButton.setEnabled(true);
                    Toast.makeText(VenueActivity.this, "Unable to checkinResult! (FIX THIS!)",
                            Toast.LENGTH_LONG).show();
                    return;
                } else {
                    showDialog(DIALOG_CHECKIN);
                }
                new CheckinsTask().execute();
            } finally {
                if (DEBUG) Log.d(TAG, "CheckinTask: onPostExecute()");
                stopProgressBar(PROGRESS_BAR_TASK_ID);
            }
        }

        @Override
        public void onCancelled() {
            stopProgressBar(PROGRESS_BAR_TASK_ID);
        }
    }

    private class TipAddTask extends AsyncTask<String, Void, Tip> {
        private static final String PROGRESS_BAR_TASK_ID = TAG + "TipAddTask";

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
                return Foursquared.getFoursquare().addTip(mStateHolder.venueId, tip, type);
            } catch (FoursquareException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareError", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
            return null;
        }

        @Override
        public void onPostExecute(Tip tip) {
            if (DEBUG) Log.d(TAG, "TipAddTask: onPostExecute()");
            try {
                if (tip != null) {
                    String tipToastString = "Added Tip #" + tip.getId() + " " + tip.getText();
                    // Refresh the tips list.
                    Toast.makeText(VenueActivity.this, tipToastString, Toast.LENGTH_LONG).show();
                    new VenueTask().execute(mStateHolder.venueId);
                } else {
                    Toast.makeText(VenueActivity.this, "Unable to add your tip! (Sorry!)",
                            Toast.LENGTH_LONG).show();
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

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "CheckinsTask: onPreExecute()");
            startProgressBar(PROGRESS_BAR_TASK_ID);
        }

        @Override
        public Group doInBackground(Void... params) {
            if (DEBUG) Log.d(TAG, "CheckinsTask: doInBackground()");
            try {
                return Foursquared.getFoursquare().checkins(null);
            } catch (FoursquareException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareError", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
            return null;
        }

        @Override
        public void onPostExecute(Group checkins) {
            if (DEBUG) Log.d(TAG, "CheckinTask: onPostExecute()");
            try {
                setCheckins(filterCheckins(checkins));
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
        CheckinResult checkinResult = null;
    }
}
