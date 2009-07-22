/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.util.SeparatedListAdapter;
import com.joelapenna.foursquared.widget.CheckinListAdapter;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueCheckinActivity extends ListActivity {
    public static final String TAG = "VenueCheckinActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private Observer mParentDataObserver = new ParentDataObserver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_checkin_activity);

        setListAdapter(new SeparatedListAdapter(this));
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Checkin checkin = (Checkin)parent.getAdapter().getItem(position);
                startItemActivity(checkin.getUser());
            }
        });

        VenueActivity parent = (VenueActivity)getParent();
        if (parent.venueObservable.getVenue() != null) {
            mParentDataObserver.update(parent.venueObservable, parent.venueObservable.getVenue());
        } else {
            ((VenueActivity)getParent()).venueObservable.addObserver(mParentDataObserver);
        }

        if (parent.checkinsObservable.getCheckins() != null) {
            mParentDataObserver.update(parent.checkinsObservable, parent.checkinsObservable
                    .getCheckins());
        } else {
            ((VenueActivity)getParent()).checkinsObservable.addObserver(mParentDataObserver);
        }
    }

    private void setCheckins(Group checkins) {
        if (DEBUG) Log.d(TAG, "Putting groups in adapter.");
        SeparatedListAdapter mainAdapter = (SeparatedListAdapter)getListAdapter();
        mainAdapter.clear();
        CheckinListAdapter groupAdapter = new CheckinListAdapter(this, checkins);
        mainAdapter.addSection("Recent Checkins", groupAdapter);
        mainAdapter.notifyDataSetInvalidated();
    }

    void startItemActivity(User user) {
        if (DEBUG) Log.d(TAG, "firing venue activity for venue");
        Intent intent = new Intent(VenueCheckinActivity.this, UserActivity.class);
        intent.putExtra(UserActivity.EXTRA_USER, user.getId());
        startActivity(intent);
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
