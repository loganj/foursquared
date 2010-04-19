/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Mayor;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.app.LoadableListActivity;
import com.joelapenna.foursquared.util.Comparators;
import com.joelapenna.foursquared.widget.CheckinListAdapter;
import com.joelapenna.foursquared.widget.MayorListAdapter;
import com.joelapenna.foursquared.widget.SeparatedListAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueCheckinsActivity extends LoadableListActivity {
    public static final String TAG = "VenueCheckinsActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private Observer mParentDataObserver = new ParentDataObserver();
    private SeparatedListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListAdapter = new SeparatedListAdapter(this);
        getListView().setAdapter(mListAdapter);
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getAdapter().getItem(position);
                if (item instanceof Checkin) {
                    Checkin checkin = (Checkin)item;
                    startItemActivity(checkin.getUser());
                } else if (item instanceof Mayor) {
                    Mayor mayor = (Mayor)item;
                    startItemActivity(mayor.getUser());
                }
            }
        });

        VenueActivity parent = (VenueActivity)getParent();

        if (parent.venueObservable.getVenue() != null) {
            mParentDataObserver.update(parent.venueObservable, parent.venueObservable.getVenue());
        } else {
            ((VenueActivity)getParent()).venueObservable.addObserver(mParentDataObserver);
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (isFinishing()) {
            mListAdapter.removeObserver();
        }
    }

    @Override
    public int getNoSearchResultsStringId() {
        return R.string.no_checkins_be_the_first;
    }

    private void putCheckinsInAdapter(Group<Checkin> checkins) {
        if (DEBUG) Log.d(TAG, "Setting checkins.");
        CheckinListAdapter adapter = new CheckinListAdapter(this, //
                ((Foursquared)getApplication()).getRemoteResourceManager());
        adapter.setGroup(checkins);
        mListAdapter.addSection("Recent Checkins", adapter);
    }

    private void putMayorInAdapter(final Mayor mayor) {
        if (DEBUG) Log.d(TAG, "Setting mayor.");
        Group<Mayor> mayors = new Group<Mayor>();
        mayors.add(mayor);
        MayorListAdapter adapter = new MayorListAdapter(this, //
                ((Foursquared)getApplication()).getRemoteResourceManager());
        adapter.setGroup(mayors);
        mListAdapter.addSection("Mayor", adapter);
    }

    private void startItemActivity(User user) {
        if (DEBUG) Log.d(TAG, "firing venue activity for venue");
        Intent intent = new Intent(VenueCheckinsActivity.this, UserDetailsActivity.class);
  // zebra      intent.putExtra(UserActivity.EXTRA_USER, user.getId());
        intent.putExtra(UserDetailsActivity.EXTRA_USER_PARCEL, user);
        startActivity(intent);
    }

    private final class ParentDataObserver implements Observer {

        @SuppressWarnings("unchecked")
        @Override
        public void update(Observable observable, Object data) {
            if (DEBUG) Log.d(TAG, "Received update from: " + observable.toString());
            VenueActivity parent = (VenueActivity)getParent();
            Venue venue = parent.venueObservable.getVenue();

            mListAdapter.removeObserver();
            mListAdapter = new SeparatedListAdapter(VenueCheckinsActivity.this);

            boolean hasMayor = venue.getStats() != null && venue.getStats().getMayor() != null;
            if (hasMayor) {
                if (DEBUG) Log.d(TAG, "Found mayor, pushing to adapter.");
                putMayorInAdapter(venue.getStats().getMayor());
            }

            Group<Checkin> checkins = venue.getCheckins();
            boolean hasCheckins = venue.getCheckins() != null && venue.getCheckins().size() > 0;
            if (hasCheckins) {
                if (DEBUG) Log.d(TAG, "Found checkins, pushing to adapter.");
                Collections.sort(checkins, Comparators.getCheckinRecencyComparator());
                putCheckinsInAdapter(checkins);
            }
            
            getListView().setAdapter(mListAdapter);

            if (!hasMayor && !hasCheckins) {
                if (DEBUG) Log.d(TAG, "No data. Setting empty");
                setEmptyView();
            }
        }
    }
}
