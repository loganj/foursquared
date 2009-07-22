/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.filters.TipGroupFilterByVenue;
import com.joelapenna.foursquare.types.Data;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.SeparatedListAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
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
    private AddAsyncTask mAddAsyncTask;

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

        setupUi();
        mVenue = (Venue)getIntent().getExtras().get(Foursquared.EXTRAS_VENUE_KEY);

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
        if (mAddAsyncTask != null) {
            mAddAsyncTask.cancel(true);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mTipGroups;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (DEBUG) Log.d(TAG, "onCreateDialog: " + String.valueOf(id));

        // Handle the simple result dialogs
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
        }

        // Handle the more complex tip/todo dialogs.

        String title = null;
        String message = null;
        DialogInterface.OnClickListener listener = null;

        final EditText editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));

        switch (id) {
            case DIALOG_TODO:
                title = "Add a Todo!";
                message = "I want to . . .";
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
                message = "I did this . . .";
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

    private void setupUi() {
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
    }

    private void addTip(String tip) {
        mAddAsyncTask = (AddAsyncTask)new AddAsyncTask().execute(tip, AddAsyncTask.TIP);
    }

    private void addTodo(String todo) {
        mAddAsyncTask = (AddAsyncTask)new AddAsyncTask().execute(todo, AddAsyncTask.TODO);
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
        if (mVenue != null) {
            mTipGroups = TipGroupFilterByVenue.filter(groups, mVenue);
        } else {
            mTipGroups = groups;
        }
        putGroupsInAdapter(mTipGroups);
    }

    private void putGroupsInAdapter(Group groups) {
        SeparatedListAdapter mainAdapter = (SeparatedListAdapter)getListAdapter();
        mainAdapter.clear();
        int groupCount = groups.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group group = (Group)groups.get(groupsIndex);
            TipListAdapter groupAdapter = new TipListAdapter(this, group);
            if (DEBUG) Log.d(TAG, "Adding Section: " + group.getType());
            mainAdapter.addSection(group.getType(), groupAdapter);
        }
        mainAdapter.notifyDataSetInvalidated();
    }

    private class TipsAsyncTask extends AsyncTask<Void, Void, Group> {

        private static final String PROGRESS_BAR_TASK_ID = TAG + "TipsAsyncTask";

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "TipsTask: onPreExecute()");
            VenueActivity.startProgressBar(VenueTipsActivity.this, PROGRESS_BAR_TASK_ID);
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
                VenueActivity.stopProgressBar(VenueTipsActivity.this, PROGRESS_BAR_TASK_ID);
                if (getListAdapter().getCount() <= 0) {
                    mEmpty.setText("No tips for this venue! Add one!");
                }
            }
        }

        @Override
        public void onCancelled() {
            VenueActivity.stopProgressBar(VenueTipsActivity.this, PROGRESS_BAR_TASK_ID);
        }

    }

    private class AddAsyncTask extends AsyncTask<String, Void, Data> {

        private static final String PROGRESS_BAR_TASK_ID = TAG + "AddAsyncTask";

        public static final String TODO = "todo";
        public static final String TIP = "tip";

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "AddTipTask: onPreExecute()");
            VenueActivity.startProgressBar(VenueTipsActivity.this, PROGRESS_BAR_TASK_ID);
        }

        @Override
        public Data doInBackground(String... params) {
            assert params.length == 2;
            String text = (String)params[0];
            String type = (String)params[1];

            final String lat;
            final String lng;

            Location location = ((Foursquared)getApplication()).getLocation();
            if (location == null) {
                if (DEBUG) Log.d(TAG, "Adding Tip without Location");
                lat = null;
                lng = null;
            } else {
                if (DEBUG) Log.d(TAG, "Adding Tip with Location: " + location);
                lat = String.valueOf(location.getLatitude());
                lng = String.valueOf(location.getLongitude());
            }

            try {
                Foursquare foursquare = ((Foursquared)getApplication()).getFoursquare();
                if (type == TIP) {
                    return foursquare.addTip(text, mVenue.getVenueid(), lat, lng, null);
                } else if (type == TODO) {
                    return foursquare.addTodo(text, mVenue.getVenueid(), lat, lng, null);
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
                lookupTipGroups();
            }
            VenueActivity.stopProgressBar(VenueTipsActivity.this, PROGRESS_BAR_TASK_ID);
        }

        @Override
        public void onCancelled() {
            VenueActivity.stopProgressBar(VenueTipsActivity.this, PROGRESS_BAR_TASK_ID);
        }
    }
}
