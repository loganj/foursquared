/**
 * Copyright 2009 Joe LaPenna
 */
package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquared.Foursquared;

import android.content.Context;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 *
 */
abstract public class BaseTipAdapter extends BaseGroupAdapter<Tip> {
    private static final String TAG = "BaseTipAdapter";
    private static final boolean DEBUG = Foursquared.DEBUG;

    public BaseTipAdapter(Context context, Group group) {
        super(context, group);
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) Log.d(TAG, "getItemId() called: " + String.valueOf(position));
        return Long.valueOf(((Tip)group.get(position)).getId());
    }
}
