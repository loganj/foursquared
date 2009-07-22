/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.filters.VenueFilter;
import com.joelapenna.foursquare.types.Data;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.SeparatedListAdapter;
import com.joelapenna.foursquared.widget.TipListAdapter;

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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueTipsActivity extends ListActivity {
    public static final String TAG = "VenueTipsActivity";
    public static final boolean DEBUG = Foursquared.DEBUG;

    private static final int DIALOG_TODO = 0;
    private static final int DIALOG_TIP = 1;
    private static final int DIALOG_ADD_FAIL_MESSAGE = 2;
    private static final int DIALOG_ADD_SHOW_MESSAGE = 3;
    private static final int DIALOG_UPDATE_FAIL_MESSAGE = 4;
    private static final int DIALOG_UPDATE_SHOW_MESSAGE = 5;

    private Venue mVenue;
    private Group mGroups;

    private LookupTipsAsyncTask mTipsTask;
    private AddAsyncTask mAddAsyncTask;
    private UpdateAsyncTask mUpdateAsyncTask;

    private TextView mEmpty;
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
                if (DEBUG) Log.d(TAG, "Click for: " + String.valueOf(position));
                CheckBox checkbox = (CheckBox)view.findViewById(R.id.checkbox);
                checkbox.setChecked(!checkbox.isChecked());
                Tip tip = (Tip)((SeparatedListAdapter)getListAdapter()).getItem(position);
                updateTodo(tip.getTipid());
            }
        });

        setupUi();
        mVenue = (Venue)getIntent().getExtras().get(VenueActivity.EXTRA_VENUE);

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
        if (mUpdateAsyncTask != null) {
            mUpdateAsyncTask.cancel(true);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mGroups;
    }

    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        String title = null;
        String message = null;
        TipTask task = null;
        switch (id) {
            case DIALOG_ADD_FAIL_MESSAGE:
                title = "Sorry!";
                message = "Could not add your " + mAddAsyncTask.type + " Try again!";
                break;
            case DIALOG_ADD_SHOW_MESSAGE:
                title = "Added!";
                task = mAddAsyncTask;
                break;
            case DIALOG_UPDATE_FAIL_MESSAGE:
                title = "Sorry!";
                message = "Could not update your " + mUpdateAsyncTask.type + " Try again!";
                break;
            case DIALOG_UPDATE_SHOW_MESSAGE:
                title = "Completed!";
                task = mUpdateAsyncTask;
                break;
            case DIALOG_TODO:
                title = "Add a Todo!";
                message = "I want to . . .";
                break;
            case DIALOG_TIP:
                title = "Add a Tip!";
                message = "I did this . . .";
                break;
        }

        if (message == null && task == null) {
            throw new RuntimeException("This should never happen no message or task!");
        } else if (message == null) {
            try {
                message = task.get().getMessage();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        AlertDialog alertDialog = (AlertDialog)dialog;
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (DEBUG) Log.d(TAG, "onCreateDialog: " + String.valueOf(id));
        DialogInterface.OnClickListener listener = null;

        final EditText editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));

        // Handle the simple result dialogs
        switch (id) {
            case DIALOG_TODO:
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

        /*
         * if (message == null) { try { message = task.get().getMessage(); } catch
         * (InterruptedException e) { throw new RuntimeException(e); } catch (ExecutionException e)
         * { throw new RuntimeException(e); } }
         */

        if (listener == null) {
            return new AlertDialog.Builder(VenueTipsActivity.this) //
                    .setIcon(android.R.drawable.ic_dialog_info) //
                    .setTitle("This is a dummy title.") // Weird layout issues if this isn't called.
                    .setMessage("This is a dummy message.") // Weird layout issues if this isn't
                                                            // called.
                    .create();
        } else {
            return new AlertDialog.Builder(VenueTipsActivity.this) //
                    .setView(editText) //
                    .setIcon(android.R.drawable.ic_dialog_info) //
                    .setPositiveButton("Add", listener) //
                    .setTitle("This is a dummy title.") // Weird layout issues if this isn't called.
                    .setMessage("This is a dummy message.") // Weird layout issues if this isn't
                                                            // called.
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

    private void addTip(String text) {
        mAddAsyncTask = (AddAsyncTask)new AddAsyncTask().execute(AddAsyncTask.TIP, text);
    }

    private void addTodo(String text) {
        mAddAsyncTask = (AddAsyncTask)new AddAsyncTask().execute(AddAsyncTask.TODO, text);
    }

    private void updateTodo(String todoid) {
        mUpdateAsyncTask = (UpdateAsyncTask)new UpdateAsyncTask().execute(TipTask.TODO, todoid);
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
        mTipsTask = (LookupTipsAsyncTask)new LookupTipsAsyncTask().execute();
    }

    private void setTipGroups(Group groups) {
        if (groups == null) {
            Toast.makeText(getApplicationContext(), "Could not complete TODO lookup!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mGroups = VenueFilter.filter(groups, mVenue);
        putGroupsInAdapter(mGroups);
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

    private class LookupTipsAsyncTask extends AsyncTask<Void, Void, Group> {

        private static final String PROGRESS_BAR_TASK_ID = TAG + "LookupTipsAsyncTask";

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "TipsTask: onPreExecute()");
            VenueActivity.startProgressBar(VenueTipsActivity.this, PROGRESS_BAR_TASK_ID);
        }

        @Override
        public Group doInBackground(Void... params) {
            try {
                Location location = ((Foursquared)getApplication()).getLastKnownLocation();
                if (location == null) {
                    if (DEBUG) Log.d(TAG, "Getting Todos without Location");
                    return Foursquared.getFoursquare().todos(null, null, null);
                } else {
                    if (DEBUG) Log.d(TAG, "Getting Todos with Location: " + location);
                    return Foursquared.getFoursquare().todos(null,
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

    private class TipTask extends AsyncTask<String, Void, Data> {

        public static final String TODO = "todo";
        public static final String TIP = "tip";

        @Override
        protected Data doInBackground(String... params) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private class AddAsyncTask extends TipTask {

        private static final String PROGRESS_BAR_TASK_ID = TAG + "AddAsyncTask";

        String type = null;

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "AddTipTask: onPreExecute()");
            VenueActivity.startProgressBar(VenueTipsActivity.this, PROGRESS_BAR_TASK_ID);
        }

        @Override
        public Data doInBackground(String... params) {
            assert params.length == 2;
            type = (String)params[0];
            String text = (String)params[1];

            final String lat;
            final String lng;

            Location location = ((Foursquared)getApplication()).getLastKnownLocation();
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
                Foursquare foursquare = Foursquared.getFoursquare();
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
            if (result == null) {
                showDialog(DIALOG_ADD_FAIL_MESSAGE);
            } else {
                showDialog(DIALOG_ADD_SHOW_MESSAGE);
                lookupTipGroups();
            }
            VenueActivity.stopProgressBar(VenueTipsActivity.this, PROGRESS_BAR_TASK_ID);
        }

        @Override
        public void onCancelled() {
            VenueActivity.stopProgressBar(VenueTipsActivity.this, PROGRESS_BAR_TASK_ID);
        }
    }

    private class UpdateAsyncTask extends TipTask {

        private static final String PROGRESS_BAR_TASK_ID = TAG + "UpdateAsyncTask";

        String type = null;

        @Override
        public void onPreExecute() {
            if (DEBUG) Log.d(TAG, "UpdateTipTask: onPreExecute()");
            VenueActivity.startProgressBar(VenueTipsActivity.this, PROGRESS_BAR_TASK_ID);
        }

        @Override
        public Data doInBackground(String... params) {
            assert params.length == 1;
            type = (String)params[0];
            String tipid = (String)params[1];

            try {
                Foursquare foursquare = Foursquared.getFoursquare();
                return foursquare.update("done", tipid);
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
            if (DEBUG) Log.d(TAG, "UpdateTipTask: onPostExecute: " + result);
            if (result == null) {
                showDialog(DIALOG_UPDATE_FAIL_MESSAGE);
            } else {
                showDialog(DIALOG_UPDATE_SHOW_MESSAGE);
            }
            // Do this unconditionally so if the update fails the checkbox in the list view will
            // appear to unset itself.
            lookupTipGroups();
            VenueActivity.stopProgressBar(VenueTipsActivity.this, PROGRESS_BAR_TASK_ID);
        }

        @Override
        public void onCancelled() {
            VenueActivity.stopProgressBar(VenueTipsActivity.this, PROGRESS_BAR_TASK_ID);
        }
    }
}
