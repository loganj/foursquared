/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.User;

import android.content.Context;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public abstract class BaseUserAdapter extends BaseGroupAdapter<User> {

    public BaseUserAdapter(Context context) {
        super(context);
    }
}
