/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.util.SeparatedListAdapter;
import com.joelapenna.foursquared.widget.CheckinListAdapter;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueCheckinActivity extends ListActivity {
    public static final String TAG = "VenueCheckinActivity";
    public static final boolean DEBUG = Foursquared.DEBUG;

    private Observer mParentDataObserver;
    private TextView mEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_checkin_activity);

        setListAdapter(new SeparatedListAdapter(this));
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Checkin checkin = (Checkin)parent.getAdapter().getItem(position);
                // TODO(jlapenna): Do something useful here.
            }
        });

        mEmpty = (TextView)findViewById(android.R.id.empty);

        mParentDataObserver = new ParentDataObserver();
        ((VenueActivity)getParent()).venueObservable.addObserver(mParentDataObserver);
        ((VenueActivity)getParent()).checkinsObservable.addObserver(mParentDataObserver);
    }

    private void setCheckins(Group checkins) {
        if (DEBUG) Log.d(TAG, "Putting groups in adapter.");
        SeparatedListAdapter mainAdapter = (SeparatedListAdapter)getListAdapter();
        mainAdapter.clear();
        CheckinListAdapter groupAdapter = new CheckinListAdapter(this, checkins);
        mainAdapter.addSection("Recent Checkins", groupAdapter);
        mainAdapter.notifyDataSetInvalidated();
    }

    private final class ParentDataObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            if (DEBUG) Log.d(TAG, "Received update from: " + observable.toString());
            VenueActivity parent = (VenueActivity)getParent();
            if (parent.venueObservable.getVenue() != null
                    && parent.checkinsObservable.getCheckins() != null) {
                setCheckins(parent.checkinsObservable.getCheckins());
            }
        }
    }
}
