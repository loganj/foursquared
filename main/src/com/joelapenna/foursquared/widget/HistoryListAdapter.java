/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquared.R;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @date March 9, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class HistoryListAdapter extends BaseCheckinAdapter {

    private LayoutInflater mInflater;

    public HistoryListAdapter(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.history_list_item, null);
            holder = new ViewHolder();
            holder.firstLine = (TextView) convertView.findViewById(R.id.firstLine);
            holder.shoutTextView = (TextView) convertView.findViewById(R.id.shoutTextView);
            holder.timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        Checkin checkin = (Checkin) getItem(position);

        if (checkin.getVenue() != null) {
            holder.firstLine.setText(checkin.getVenue().getName());
            holder.firstLine.setVisibility(View.VISIBLE);
        } else {
            holder.firstLine.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(checkin.getShout()) == false) {
            holder.shoutTextView.setText(checkin.getShout());
            holder.shoutTextView.setVisibility(View.VISIBLE);
        } else {
            holder.shoutTextView.setVisibility(View.GONE);
        }

        holder.timeTextView.setText(checkin.getCreated());

        return convertView;
    }

    private static class ViewHolder {
        TextView firstLine;
        TextView shoutTextView;
        TextView timeTextView;
    }
}
