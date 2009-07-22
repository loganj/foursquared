/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.Foursquared;

import android.content.Context;
import android.util.Log;
import android.widget.BaseAdapter;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
abstract class BaseGroupAdapter<T> extends BaseAdapter {
    private static final String TAG = "BaseGroupAdapter";
    private static final boolean DEBUG = Foursquared.DEBUG;

    Group group;

    public BaseGroupAdapter(Context context, Group g) {
        group = g;
    }

    @Override
    public int getCount() {
        if (DEBUG) Log.d(TAG, "getCount called.");
        return group.size();
    }

    @Override
    public Object getItem(int position) {
        if (DEBUG) Log.d(TAG, "getItem() called: " + String.valueOf(position));
        return group.get(position);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        if (DEBUG) Log.d(TAG, "isEmpty() called");
        return group.isEmpty();
    }
}
