/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.classic.Venue;
import com.joelapenna.foursquared.Foursquared;

import android.content.Context;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
abstract public class BaseVenueAdapter extends BaseGroupAdapter<Venue> {
    private static final String TAG = "BaseVenueAdapter";
    private static final boolean DEBUG = Foursquared.DEBUG;

    public BaseVenueAdapter(Context context, Group group) {
        super(context, group);
    }

    @Override
    public long getItemId(int position) {
        if (DEBUG) Log.d(TAG, "getItemId() called: " + String.valueOf(position));
        return Long.valueOf(((Venue)group.get(position)).getVenueid());
    }
}
