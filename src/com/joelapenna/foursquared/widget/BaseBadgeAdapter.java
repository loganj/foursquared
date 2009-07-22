/**
 * Copyright 2009 Joe LaPenna
 */
package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Badge;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.Foursquared;

import android.content.Context;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 *
 */
abstract public class BaseBadgeAdapter extends BaseGroupAdapter<Badge> {
    private static final String TAG = "BaseTipAdapter";
    private static final boolean DEBUG = Foursquared.DEBUG;

    public BaseBadgeAdapter(Context context, Group badges) {
        super(context, badges);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
