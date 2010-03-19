/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Badge;
import com.joelapenna.foursquared.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author jlapenna
 */
public class BadgeListAdapter extends BaseBadgeAdapter {

    private LayoutInflater mInflater;
    private int mLayoutToInflate;

    public BadgeListAdapter(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        mLayoutToInflate = R.layout.badge_item;
    }

    public BadgeListAdapter(Context context, int layoutResource) {

        super(context);
        mInflater = LayoutInflater.from(context);
        mLayoutToInflate = layoutResource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary
        // calls to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no
        // need to re-inflate it. We only inflate a new View when the
        // convertView supplied by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutToInflate, null);

            // Creates a ViewHolder and store references to the two children
            // views we want to bind data to.
            holder = new ViewHolder();
            holder.icon = (ImageView)convertView.findViewById(R.id.icon);
            holder.name = (TextView)convertView.findViewById(R.id.name);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder)convertView.getTag();
        }

        Badge badge = (Badge)getItem(position);
        holder.name.setText(badge.getName());

        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView name;
    }
}
