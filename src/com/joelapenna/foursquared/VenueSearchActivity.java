/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Venue;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueSearchActivity extends ListActivity {
    private static final String TAG = "VenueSearchActivity";
    private static final boolean DEBUG = FoursquaredTest.DEBUG;

    private SearchHandler mSearchHandler = new SearchHandler();
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
        Message msg = mSearchHandler.obtainMessage(SearchHandler.MESSAGE_QUERY_START);
        msg.sendToTarget();
    }

    protected void finishQuery() {
        if (DEBUG) Log.d(TAG, "finishQuery()");
        Message msg = mSearchHandler.obtainMessage(SearchHandler.MESSAGE_QUERY_FINISH);
        msg.sendToTarget();
    }

    protected void fireVenueActivityIntent(Venue venue) {
        if (DEBUG) Log.d(TAG, "firing venue activity for venue");
        Intent intent = new Intent(VenueSearchActivity.this, VenueActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra("venue", venue);
        startActivity(intent);
    }

    class SearchHandler extends Handler {

        public static final int MESSAGE_QUERY_START = 0;
        public static final int MESSAGE_QUERY_FINISH = 1;

        public void handleMessage(Message msg) {
            String query = mSearchEdit.getText().toString();
            switch (msg.what) {
                case MESSAGE_QUERY_START:
                    setProgressBarIndeterminateVisibility(true);
                    setTitle("Searching: " + query);
                    mSearchEdit.setEnabled(false);
                    break;
                case MESSAGE_QUERY_FINISH:
                    setProgressBarIndeterminateVisibility(false);
                    setTitle("Searched for: " + query);
                    mSearchEdit.setEnabled(true);
                    break;
            }
        }
    }
}
