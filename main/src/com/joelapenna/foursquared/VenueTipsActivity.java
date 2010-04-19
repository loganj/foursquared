/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.app.LoadableListActivity;
import com.joelapenna.foursquared.util.Comparators;
import com.joelapenna.foursquared.widget.SeparatedListAdapter;
import com.joelapenna.foursquared.widget.TipListAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com)
 *   -modified to start TipActivity on tip click (2010-03-25)
 *   -added photos for tips (2010-03-25)
 */
public class VenueTipsActivity extends LoadableListActivity {
    public static final String TAG = "VenueTipsActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final String STATE_CLICKED_TIP = "com.joelapenna.foursquared.VenueTipsActivity.CLICKED_TIP";
    private static final String STATE_CLICKED_TIP_AUTHOR = "com.joelapenna.foursquared.VenueTipsActivity.CLICKED_TIP_AUTHOR";

    private Observer mVenueObserver = new VenueObserver();
    private String mClickedTip = null;
    private String mClickedTipAuthor = null;
    private SeparatedListAdapter mListAdapter;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mClickedTip = savedInstanceState.getString(STATE_CLICKED_TIP);
            mClickedTipAuthor = savedInstanceState.getString(STATE_CLICKED_TIP_AUTHOR);
        }

        setListAdapter(new SeparatedListAdapter(this));
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                VenueActivity parent = (VenueActivity)getParent();
                Tip tip = (Tip)adapter.getItemAtPosition(position);
                Intent intent = new Intent(VenueTipsActivity.this, TipActivity.class);
                intent.putExtra(TipActivity.EXTRA_TIP_PARCEL, tip);
                if (parent.venueObservable.getVenue() != null) {
                    intent.putExtra(TipActivity.EXTRA_VENUE_NAME, parent.venueObservable.getVenue().getName());
                }
                startActivity(intent);
            }
        });
        
        VenueActivity parent = (VenueActivity)getParent();
        if (parent.venueObservable.getVenue() != null) {
            mVenueObserver.update(parent.venueObservable, parent.venueObservable.getVenue());
        } else {
            parent.venueObservable.addObserver(mVenueObserver);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_CLICKED_TIP, mClickedTip);
        outState.putString(STATE_CLICKED_TIP_AUTHOR, mClickedTipAuthor);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (isFinishing() && mListAdapter != null) {
            mListAdapter.removeObserver();
        }
    }

    @Override
    public int getNoSearchResultsStringId() {
        return R.string.no_tips_be_the_first;
    }

    private Group<Group<Tip>> getVenueTipsAndTodos(Venue venue) {
        Group<Group<Tip>> tipsAndTodos = new Group<Group<Tip>>();

        Group<Tip> tips = venue.getTips();
        if (tips != null && tips.size() > 0) {
            Collections.sort(tips, Comparators.getTipRecencyComparator());
            tips.setType("Tips");
            tipsAndTodos.add(tips);
        }

        tips = venue.getTodos();
        if (tips != null && tips.size() > 0) {
            Collections.sort(tips, Comparators.getTipRecencyComparator());
            tips.setType("Todos");
            tipsAndTodos.add(tips);
        }
        return tipsAndTodos;
    }

    private void putGroupsInAdapter(Group<Group<Tip>> groups) {
        mListAdapter = (SeparatedListAdapter)getListAdapter();
        mListAdapter.removeObserver();
        mListAdapter.clear();
        setEmptyView();

        int groupCount = groups.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group<Tip> group = groups.get(groupsIndex);
            TipListAdapter groupAdapter = new TipListAdapter(
                this, ((Foursquared)getApplication()).getRemoteResourceManager());
            groupAdapter.setGroup(group);
            mListAdapter.addSection(group.getType(), groupAdapter);
        }
        mListAdapter.notifyDataSetInvalidated();
        getListView().setAdapter(mListAdapter);
    }

    private final class VenueObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            putGroupsInAdapter(getVenueTipsAndTodos((Venue)data));
        }
    }
}
