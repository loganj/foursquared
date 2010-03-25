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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueTipsActivity extends LoadableListActivity {
    public static final String TAG = "VenueTipsActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final int DIALOG_TIP = 0;
    private static final int DIALOG_ID_LONG_CLICK_ITEM = 1;
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
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tip tip = (Tip)parent.getAdapter().getItem(position);
                Intent intent = new Intent(VenueTipsActivity.this, TipActivity.class);
                intent.putExtra(TipActivity.EXTRA_TIP_PARCEL, tip);
                intent.putExtra(TipActivity.EXTRA_VENUE_NAME, "Venue Name");
                startActivity(intent);
            }
        });
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                showDialog(DIALOG_ID_LONG_CLICK_ITEM); 
                return false;
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
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_TIP:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Tip") // If not called with a value, the title isn't rendered.
                        .setIcon(android.R.drawable.ic_dialog_info) //
                        .setMessage("") // If not called, the textview isn't rendered.
                        .setCancelable(true);
                return builder.create();
            case DIALOG_ID_LONG_CLICK_ITEM:
                String[] options = { 
                    "Add to my To-Do list", 
                    "I've done this!" };

                AlertDialog dlgLongClick = new AlertDialog.Builder(this)
                  .setTitle("Options")
                  .setIcon(0)
                  .setCancelable(true)
                  .setItems(options, new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int which) {
                          /*
                          switch (which) {
                              case 0:
                                  Intent intent = new Intent(FriendsActivity.this, UserDetailsActivity.class);
                                  intent.putExtra(UserDetailsActivity.EXTRA_USER_ID,
                                          mSearchHolder.mLongClickedUserId);
                                  intent.putExtra(UserDetailsActivity.EXTRA_SHOW_ADD_FRIEND_OPTIONS, true);
                                  startActivity(intent);
                                  break;
                              case 1:
                                  Intent intentVenue = new Intent(FriendsActivity.this, VenueActivity.class);
                                  intentVenue.setAction(Intent.ACTION_VIEW);
                                  intentVenue.putExtra(Foursquared.EXTRA_VENUE_ID, 
                                          mSearchHolder.mLongClickedVenueId);
                                  startActivity(intentVenue);
                                  break;
                          }
                          */
                      }
                  }).create();
                dlgLongClick.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dlg) {
                        removeDialog(DIALOG_ID_LONG_CLICK_ITEM);
                    }
                });
                return dlgLongClick;
        }
        return null;
    }

    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_TIP:
                ((AlertDialog)dialog).setMessage(mClickedTip);
                dialog.setTitle(getString(R.string.tip_says, mClickedTipAuthor));
        }
    }

    @Override
    public int getNoSearchResultsStringId() {
        return R.string.no_tips_be_the_first;
    }

    @SuppressWarnings("unchecked")
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
        mListAdapter.clear();
        setEmptyView();

        int groupCount = groups.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group<Tip> group = groups.get(groupsIndex);
            TipListAdapter groupAdapter = new TipListAdapter(this);
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
