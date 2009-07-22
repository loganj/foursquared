/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquared.Foursquared;

import android.content.Context;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public abstract class BaseCheckinAdapter extends BaseGroupAdapter<Checkin> {
    private static final String TAG = "BaseCheckinAdapter";
    private static final boolean DEBUG = Foursquared.DEBUG;

    public BaseCheckinAdapter(Context context, Group g) {
        super(context, g);
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) Log.d(TAG, "getItemId() called: " + String.valueOf(position));
        return Long.valueOf(((Checkin)group.get(position)).getId());
    }
}
