/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.SeparatedListAdapter;

import android.app.ListActivity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueSearchActivity extends ListActivity {
    private static final String TAG = "VenueSearchActivity";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private EditText mSearchEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.venue_search_activity);

        setListAdapter(new SeparatedListAdapter(this));

        mSearchEdit = (EditText)findViewById(R.id.searchEdit);
        mSearchEdit.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            startQuery();
                            return true;
                    }
                }
                return false;
            }
        });

        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Venue venue = (Venue)parent.getAdapter().getItem(position);
                fireVenueActivityIntent(venue);
            }
        });

        // testStuff();
    }

    private void testStuff() {
        mSearchEdit.setText("YOUR FACE");
        Group groups = new Group();
        groups.setType("TLG");
        groups.add(FoursquaredTest.createVenueGroup("Group A"));
        groups.add(FoursquaredTest.createVenueGroup("Group B"));
        groups.add(FoursquaredTest.createVenueGroup("Group C"));
        putGroupsInAdapter(groups);
    }

    protected void startQuery() {
        if (DEBUG) Log.d(TAG, "sendQuery()");
        String query = mSearchEdit.getText().toString();
        new SearchAsyncTask().execute(new String[] {
            query
        });
    }

    protected void fireVenueActivityIntent(Venue venue) {
        if (DEBUG) Log.d(TAG, "firing venue activity for venue");
        Intent intent = new Intent(VenueSearchActivity.this, VenueActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra("venue", venue);
        startActivity(intent);
    }

    private void putGroupsInAdapter(Group groups) {
        try {
            if (groups == null) {
                Toast.makeText(getApplicationContext(), "Could not complete search!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            SeparatedListAdapter mainAdapter = (SeparatedListAdapter)getListAdapter();
            mainAdapter.clear();
            int groupCount = groups.size();
            for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
                Group group = (Group)groups.get(groupsIndex);
                VenueListAdapter groupAdapter = new VenueListAdapter(this, group);
                if (DEBUG) Log.d(TAG, "Adding Section: " + group.getType());
                mainAdapter.addSection(group.getType(), groupAdapter);
            }
            mainAdapter.notifyDataSetInvalidated();

        } finally {
            setProgressBarIndeterminateVisibility(false);
            String query = mSearchEdit.getText().toString();
            setTitle("Searching: " + query);
            mSearchEdit.setEnabled(true);
        }
    }

    class SearchAsyncTask extends AsyncTask<String, Void, Group> {

        @Override
        public void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            String query = mSearchEdit.getText().toString();
            setTitle("Searching: " + query);
            mSearchEdit.setEnabled(false);
        }

        @Override
        public Group doInBackground(String... params) {
            try {
                Location location = ((Foursquared)getApplication()).getLocation();
                if (location == null) {
                    return ((Foursquared)getApplication()).getFoursquare().venues(params[0], null,
                            null, 10, 1);
                } else {
                    return ((Foursquared)getApplication()).getFoursquare().venues(params[0],
                            String.valueOf(location.getLatitude()),
                            String.valueOf(location.getLongitude()), 10, 1);
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
            putGroupsInAdapter(groups);
        }
    }
}
