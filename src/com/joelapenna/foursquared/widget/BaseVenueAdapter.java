/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.Foursquared;

import android.content.Context;
import android.util.Log;
import android.widget.BaseAdapter;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
abstract public class BaseVenueAdapter extends BaseAdapter {
    private static final String TAG = "BaseVenueAdapter";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private Group mVenues;

    public BaseVenueAdapter(Context context, Group venues) {
        mVenues = venues;
    }

    @Override
    public int getCount() {
        if (DEBUG) Log.d(TAG, "getCount() called");
        return mVenues.size();
    }

    @Override
    public Object getItem(int position) {
        if (DEBUG) Log.d(TAG, "getItem() called: " + String.valueOf(position));
        return mVenues.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) Log.d(TAG, "getItemId() called: " + String.valueOf(position));
        return Long.valueOf(((Venue)mVenues.get(position)).getVenueid());
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        if (DEBUG) Log.d(TAG, "isEmpty() called");
        return mVenues.isEmpty();
    }
}
