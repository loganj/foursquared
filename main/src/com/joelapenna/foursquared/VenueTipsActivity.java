/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.SeparatedListAdapter;
import com.joelapenna.foursquared.widget.TipListAdapter;

import android.app.ListActivity;
import android.os.Bundle;

import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueTipsActivity extends ListActivity {
    public static final String TAG = "VenueTipsActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private Venue mVenue;
    private Group mGroups;

    private Observer mVenueObserver = new VenueObserver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_tips_activity);

        setListAdapter(new SeparatedListAdapter(this));
        // TODO(jlapenna): Hey Joe, you need to make this work...
        // TODO(foursquare): Hey Foursquare, you need to support this in the API.
        /*
         * getListView().setOnItemClickListener(new OnItemClickListener() {
         * @Override public void onItemClick(AdapterView<?> parent, View view, int position, long
         * id) { if (DEBUG) Log.d(TAG, "Click for: " + String.valueOf(position)); CheckBox checkbox
         * = (CheckBox)view.findViewById(R.id.checkbox); checkbox.setChecked(!checkbox.isChecked());
         * Tip tip = (Tip)((SeparatedListAdapter)getListAdapter()).getItem(position);
         * updateTodo(tip.getId()); } });
         */

        VenueActivity parent = (VenueActivity)getParent();
        if (parent.venueObservable.getVenue() != null) {
            mVenueObserver.update(parent.venueObservable, parent.venueObservable.getVenue());
        } else {
            parent.venueObservable.addObserver(mVenueObserver);
        }
    }

    private Group onVenueSet(Venue venue) {
        Group tipsAndTodos = new Group();

        Group tips = venue.getTips();
        if (tips != null) {
            tips.setType("Tips");
            tipsAndTodos.add(tips);
        }

        tips = venue.getTodos();
        if (tips != null) {
            tips.setType("Todos");
            tipsAndTodos.add(tips);
        }
        return tipsAndTodos;
    }

    private void setTipGroups(Group groups) {
        mGroups = groups;
        putGroupsInAdapter(mGroups);
    }

    private void putGroupsInAdapter(Group groups) {
        SeparatedListAdapter mainAdapter = (SeparatedListAdapter)getListAdapter();
        mainAdapter.clear();
        int groupCount = groups.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group group = (Group)groups.get(groupsIndex);
            TipListAdapter groupAdapter = new TipListAdapter(this, group);
            mainAdapter.addSection(group.getType(), groupAdapter);
        }
        mainAdapter.notifyDataSetInvalidated();
    }

    private final class VenueObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            mVenue = (Venue)data;
            setTipGroups(onVenueSet(mVenue));
        }
    }
}
