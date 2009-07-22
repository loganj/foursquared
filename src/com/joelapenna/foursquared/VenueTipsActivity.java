/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Data;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.SeparatedListAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueTipsActivity extends ListActivity {
    public static final String TAG = "VenueTipsActivity";
    public static final boolean DEBUG = Foursquared.DEBUG;

    public static final int DIALOG_TODO = 0;
    private static final int DIALOG_TIP = 1;
    private static final int DIALOG_FAIL_MESSAGE = 2;
    private static final int DIALOG_SHOW_MESSAGE = 3;

    private Venue mVenue;
    private Data mResult;

    private TipsAsyncTask mTipsTask;
    private AddTipAsyncTask mAddTipAsyncTask;

    private TextView mEmpty;
    private Group mTipGroups;
    private Button mTipButton;
    private Button mTodoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_tips_activity);

        setListAdapter(new SeparatedListAdapter(this));
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // fireVenueActivityIntent(venue);
            }
        });

        mEmpty = (TextView)findViewById(android.R.id.empty);

        mTipButton = (Button)findViewById(R.id.tipButton);
        mTipButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_TIP);
            }
        });
        mTodoButton = (Button)findViewById(R.id.todoButton);
        mTodoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_TODO);
            }
        });

        setVenue((Venue)getIntent().getExtras().get(Foursquared.EXTRAS_VENUE_KEY));

        if (getLastNonConfigurationInstance() != null) {
            setTipGroups((Group)getLastNonConfigurationInstance());
        } else {
            lookupTipGroups();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mTipsTask != null) {
            mTipsTask.cancel(true);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mTipGroups;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (DEBUG) Log.d(TAG, "onCreateDialog: " + String.valueOf(id));
        String title = null;
        String message = null;
        DialogInterface.OnClickListener listener = null;
        final EditText editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));

        switch (id) {
            case DIALOG_FAIL_MESSAGE:
                return new AlertDialog.Builder(VenueTipsActivity.this) //
                        .setTitle("Sorry!") //
                        .setIcon(android.R.drawable.ic_dialog_alert) //
                        .setMessage("Failed to add your tip.") //
                        .create();

            case DIALOG_SHOW_MESSAGE:
                return new AlertDialog.Builder(VenueTipsActivity.this) //
                        .setTitle("Added!").setIcon(android.R.drawable.ic_dialog_info) //
                        .setMessage(mResult.getMessage()) //
                        .create();

            case DIALOG_TODO:
                title = "Add a Todo!";
                message = "I want to...";
                listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = editText.getText().toString();
                        if (!TextUtils.isEmpty(text)) {
                            addTodo(text);
                        }
                    }
                };
                break;

            case DIALOG_TIP:
                title = "Add a Tip!";
                message = "I did this...";
                listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String text = editText.getText().toString();
                        if (!TextUtils.isEmpty(text)) {
                            addTip(text);
                        }
                    }
                };
                break;
        }

        if (title == null || message == null || listener == null) {
            return null;
        } else {
            return new AlertDialog.Builder(VenueTipsActivity.this) //
                    .setView(editText) //
                    .setTitle(title) //
                    .setIcon(android.R.drawable.ic_dialog_info) //
                    .setMessage(message) //
                    .setPositiveButton("Add", listener) //
                    .create();
        }
    }

    private void addTip(String tip) {
        mAddTipAsyncTask = (AddTipAsyncTask)new AddTipAsyncTask().execute(tip);
    }

    private void addTodo(String todo) {
    }

    /**
     * If a new tips lookup comes in cancel the old one and start a new one.
     */
    private void lookupTipGroups() {
        if (DEBUG) Log.d(TAG, "lookupTips()");

        // If a task is already running, don't start a new one.
        if (mTipsTask != null && mTipsTask.getStatus() != AsyncTask.Status.FINISHED) {
            if (DEBUG) Log.d(TAG, "Query already running attempting to cancel: " + mTipsTask);
            if (!mTipsTask.cancel(true) && !mTipsTask.isCancelled()) {
                if (DEBUG) Log.d(TAG, "Unable to cancel tips? That should not have happened!");
                Toast.makeText(this, "Unable to re-query tips.", Toast.LENGTH_SHORT);
                return;
            }
        }
        mTipsTask = (TipsAsyncTask)new TipsAsyncTask().execute();
    }

    private void setTipGroups(Group groups) {
        if (groups == null) {
            Toast.makeText(getApplicationContext(), "Could not complete TODO lookup!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mTipGroups = filterTipGroups(groups);
        putGroupsInAdapter(mTipGroups);
    }

    private Group filterTipGroups(Group groups) {
        Group filteredGroup = new Group();
        filteredGroup.setType(groups.getType());
        int groupCount = groups.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group group = (Group)groups.get(groupsIndex);
            if (mVenue.getVenueid() != null) {
                filterTipGroupByVenueid(mVenue.getVenueid(), group);
                if (group.size() > 0) {
                    filteredGroup.add(group);
                }
            }
        }
        return filteredGroup;
    }

    private void filterTipGroupByVenueid(String venueid, Group group) {
        ArrayList<Tip> venueTips = new ArrayList<Tip>();
        int tipCount = group.size();
        for (int tipIndex = 0; tipIndex < tipCount; tipIndex++) {
            Tip tip = (Tip)group.get(tipIndex);
            if (venueid.equals(tip.getVenueid())) {
                venueTips.add(tip);
            }
        }
        group.clear();
        group.addAll(venueTips);
    }

    private void putGroupsInAdapter(Group groups) {
        SeparatedListAdapter mainAdapter = (SeparatedListAdapter)getListAdapter();
        mainAdapter.clear();
        int groupCount = groups.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group group = (Group)groups.get(groupsIndex);
            TipsListAdapter groupAdapter = new TipsListAdapter(this, group);
            if (DEBUG) Log.d(TAG, "Adding Section: " + group.getType());
            mainAdapter.addSection(group.getType(), groupAdapter);
        }
        mainAdapter.notifyDataSetInvalidated();
    }

    private void setVenue(Venue venue) {
        mVenue = venue;
    }

    private void startProgressBar(String taskId) {
        Intent intent = new Intent(VenueActivity.ACTION_PROGRESS_BAR_START);
        intent.putExtra(VenueActivity.EXTRA_TASK_ID, taskId);
        sendBroadcast(intent);
    }

    private void stopProgressBar(String taskId) {
        Intent intent = new Intent(VenueActivity.ACTION_PROGRESS_BAR_STOP);
        intent.putExtra(VenueActivity.EXTRA_TASK_ID, taskId);
        sendBroadcast(intent);
    }

    private class TipsAsyncTask extends AsyncTask<Void, Void, Group> {

        private static final String PROGRESS_BAR_TASK_ID = TAG + "TipsAsyncTask";

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "TipsTask: onPreExecute()");
            startProgressBar(PROGRESS_BAR_TASK_ID);
        }

        @Override
        public Group doInBackground(Void... params) {
            try {
                Location location = ((Foursquared)getApplication()).getLocation();
                if (location == null) {
                    if (DEBUG) Log.d(TAG, "Getting Todos without Location");
                    return ((Foursquared)getApplication()).getFoursquare().todos(null, null, null);
                } else {
                    if (DEBUG) Log.d(TAG, "Getting Todos with Location: " + location);
                    return ((Foursquared)getApplication()).getFoursquare().todos(null,
                            String.valueOf(location.getLatitude()),
                            String.valueOf(location.getLongitude()));

                }
            } catch (FoursquareError e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareError", e);
            } catch (FoursquareParseException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareParseException", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
            return null;
        }

        @Override
        public void onPostExecute(Group groups) {
            try {
                setTipGroups(groups);
            } finally {
                if (DEBUG) Log.d(TAG, "TipsTask: onPostExecute()");
                stopProgressBar(PROGRESS_BAR_TASK_ID);
                if (getListAdapter().getCount() <= 0) {
                    mEmpty.setText("No tips for this venue! Add one!");
                }
            }
        }

        @Override
        public void onCancelled() {
            stopProgressBar(PROGRESS_BAR_TASK_ID);
        }

    }

    private class AddTipAsyncTask extends AsyncTask<String, Void, Data> {

        private static final String PROGRESS_BAR_TASK_ID = TAG + "AddTipAsyncTask";

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "AddTipTask: onPreExecute()");
            startProgressBar(PROGRESS_BAR_TASK_ID);
        }

        @Override
        public Data doInBackground(String... params) {
            try {
                Location location = ((Foursquared)getApplication()).getLocation();
                Foursquare foursquare = ((Foursquared)getApplication()).getFoursquare();
                if (location == null) {
                    if (DEBUG) Log.d(TAG, "Adding Tip without Location");
                    return foursquare.addTip((String)params[0], mVenue.getVenueid(), null, null, null);
                } else {
                    if (DEBUG) Log.d(TAG, "Adding Tip with Location: " + location);
                    String lat = String.valueOf(location.getLatitude());
                    String lng = String.valueOf(location.getLongitude());
                    return foursquare.addTip((String)params[0], mVenue.getVenueid(), lat, lng, null);
                }
            } catch (FoursquareError e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareError", e);
            } catch (FoursquareParseException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareParseException", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
            return null;
        }

        @Override
        public void onPostExecute(Data result) {
            if (DEBUG) Log.d(TAG, "AddTipTask: onPostExecute: " + result);
            mResult = result;
            if (result == null) {
                showDialog(DIALOG_FAIL_MESSAGE);
            } else {
                showDialog(DIALOG_SHOW_MESSAGE);
            }
            stopProgressBar(PROGRESS_BAR_TASK_ID);
        }

        @Override
        public void onCancelled() {
            stopProgressBar(PROGRESS_BAR_TASK_ID);
        }
    }
}
