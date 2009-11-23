/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Mayor;

import android.content.Context;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public abstract class BaseMayorAdapter extends BaseGroupAdapter<Mayor> {

    public BaseMayorAdapter(Context context) {
        super(context);
    }
}
