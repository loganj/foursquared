/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.Foursquared;

import android.content.Context;
import android.util.Log;
import android.widget.BaseAdapter;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public abstract class BaseCheckinAdapter extends BaseAdapter {
    private static final String TAG = "CheckinsListAdapter";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private Group mCheckins;

    public BaseCheckinAdapter(Context context, Group checkins) {
        mCheckins = checkins;
    }

    @Override
    public int getCount() {
        if (DEBUG) Log.d(TAG, "getCount called.");
        return mCheckins.size();
    }

    @Override
    public Object getItem(int position) {
        if (DEBUG) Log.d(TAG, "getItem() called: " + String.valueOf(position));
        return mCheckins.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) Log.d(TAG, "getItemId() called: " + String.valueOf(position));
        return Long.valueOf(((Checkin)mCheckins.get(position)).getCheckinid());
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        if (DEBUG) Log.d(TAG, "isEmpty() called");
        return mCheckins.isEmpty();
    }
}