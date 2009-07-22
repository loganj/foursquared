/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;

import android.content.Context;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public abstract class BaseCheckinAdapter extends BaseGroupAdapter<Checkin> {

    public BaseCheckinAdapter(Context context, Group g) {
        super(context, g);
    }
}
