/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Badge;
import com.joelapenna.foursquare.types.Group;

import android.content.Context;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
abstract public class BaseBadgeAdapter extends BaseGroupAdapter<Badge> {

    public BaseBadgeAdapter(Context context, Group badges) {
        super(context, badges);
    }
}
