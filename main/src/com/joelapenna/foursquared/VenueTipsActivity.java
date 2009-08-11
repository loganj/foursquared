/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.widget.SeparatedListAdapter;
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

    private Group mGroups;

    private Observer mVenueObserver = new VenueObserver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_tab_with_list);

        setListAdapter(new SeparatedListAdapter(this));

        VenueActivity parent = (VenueActivity)getParent();
        if (parent.venueObservable.getVenue() != null) {
            mVenueObserver.update(parent.venueObservable, parent.venueObservable.getVenue());
        } else {
            parent.venueObservable.addObserver(mVenueObserver);
        }
    }

    private Group getVenueTipsAndTodos(Venue venue) {
        Group tipsAndTodos = new Group();

        Group tips = venue.getTips();
        if (tips != null && tips.size() > 0) {
            tips.setType("Tips");
            tipsAndTodos.add(tips);
        }

        tips = venue.getTodos();
        if (tips != null && tips.size() > 0) {
            tips.setType("Todos");
            tipsAndTodos.add(tips);
        }

        // We want to still show a header in the list view in case of no results (so that the empty
        // view stops showing) so we'll fill tips and todos with an empty group.
        if (tipsAndTodos.size() == 0) {
            Group emptyGroup = new Group();
            emptyGroup.setType("Tips");
            tipsAndTodos.add(emptyGroup);
        }
        return tipsAndTodos;
    }

    private void setTipGroups(Group groups) {
        mGroups = groups;
        putGroupsInAdapter(mGroups);
    }

    private void putGroupsInAdapter(Group groups) {
        SeparatedListAdapter mainAdapter = (SeparatedListAdapter)getListAdapter();

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
            setTipGroups(getVenueTipsAndTodos((Venue)data));
        }
    }
}
