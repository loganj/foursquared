/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.joelapenna.foursquare.types.Special;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.R;

/**
 * @author avolovoy
 */
public class SpecialListAdapter extends BaseGroupAdapter<Special> {

    private static final String TAG = "SpecialListAdapter";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private LayoutInflater mInflater;
    private int mLayoutToInflate;

    public SpecialListAdapter(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        mLayoutToInflate = R.layout.special_list_item;
    }

    public SpecialListAdapter(Context context, int layoutResource) {

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
            // holder.icon = (ImageView)convertView.findViewById(R.id.icon);
            holder.message = (TextView)convertView.findViewById(R.id.message);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder)convertView.getTag();
        }

        Special special = (Special)getItem(position);
        holder.message.setText(special.getMessage());

        return convertView;
    }

    static class ViewHolder {

        // ImageView icon;
        TextView message;
    }
}
