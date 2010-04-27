/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.app;

import com.joelapenna.foursquared.R;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This is pretty much a direct copy of LoadableListActivity. It just gives the caller
 * a chance to set their own view for the empty state. This is used by FriendsActivity
 * to show a button like 'Find some friends!' when the list is empty (in the case that
 * they are a new user and have no friends initially).
 * 
 * By default, loadable_list_activity_with_view is used as the intial empty view with
 * a progress bar and textview description. The owner can then call setEmptyView()
 * with their own view to show if there are no results.
 *  
 * @date April 25, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class LoadableListActivityWithView extends ListActivity {

    private ProgressBar mEmptyProgress;
    private TextView mEmptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.loadable_list_activity_with_view);
        mEmptyProgress = (ProgressBar)findViewById(R.id.emptyProgress);
        mEmptyText = (TextView)findViewById(R.id.emptyText);

        setLoadingView(); 
    }
 
    public void setEmptyView(View view) {
        LinearLayout parent = (LinearLayout)findViewById(R.id.loadableListHolder);
        parent.removeAllViews();
        parent.addView(view);
    }
 
    public void setLoadingView() {
        mEmptyProgress.setVisibility(ViewGroup.VISIBLE);
        mEmptyText.setText(R.string.loading);
    }

    public int getNoSearchResultsStringId() {
        return R.string.no_search_results;
    }
}
