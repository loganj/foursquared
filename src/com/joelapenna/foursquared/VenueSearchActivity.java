/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.UserTask;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueSearchActivity extends ListActivity {
    private static final String TAG = "VenueSearchActivity";
    private static final boolean DEBUG = FoursquaredTest.DEBUG;

    private EditText mSearchEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.venue_search_activity);

        setListAdapter(new VenueSearchListAdapter(this));

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
    }

    protected void startQuery() {
        if (DEBUG) Log.d(TAG, "sendQuery()");
        String query = mSearchEdit.getText().toString();
        new SearchUserTask().execute(new String[] { query});
    }

    protected void fireVenueActivityIntent(Venue venue) {
        if (DEBUG) Log.d(TAG, "firing venue activity for venue");
        Intent intent = new Intent(VenueSearchActivity.this, VenueActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra("venue", venue);
        startActivity(intent);
    }

    class SearchUserTask extends UserTask<String, Void, Group> {

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
                return ((Foursquared)getApplication()).getFoursquare().venues(params[0], null,
                        null, 10, 1);
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
                VenueSearchListAdapter adapter = (VenueSearchListAdapter)getListAdapter();
                adapter.clear();
                int groupCount = groups.size();
                for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
                    Group group = (Group)groups.get(groupsIndex);
                    int venuesCount = group.size();
                    for (int venuesIndex = 0; venuesIndex < venuesCount; venuesIndex++) {
                        Venue venue = (Venue)group.get(venuesIndex);
                        adapter.add(venue);
                    }
                }
            } finally {
                setProgressBarIndeterminateVisibility(false);
                String query = mSearchEdit.getText().toString();
                setTitle("Searching: " + query);
                mSearchEdit.setEnabled(true);
            }
        }
    }
}
