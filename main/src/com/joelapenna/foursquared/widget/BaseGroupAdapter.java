/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Group;

import android.content.Context;
import android.widget.BaseAdapter;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
abstract class BaseGroupAdapter<T> extends BaseAdapter {

    Group group;

    public BaseGroupAdapter(Context context, Group g) {
        setGroup(g);
    }

    @Override
    public int getCount() {
        return group.size();
    }

    @Override
    public Object getItem(int position) {
        return group.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return group.isEmpty();
    }

    public void clear() {
        group.clear();
        notifyDataSetInvalidated();
    }

    public void setGroup(Group g) {
        group = g;
        notifyDataSetInvalidated();
    }
}
