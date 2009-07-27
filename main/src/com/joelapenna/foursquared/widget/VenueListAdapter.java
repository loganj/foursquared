/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Stats;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.util.StringFormatters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueListAdapter extends BaseVenueAdapter {
    private static final String TAG = "VenueListAdapter";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private LayoutInflater mInflater;

    public VenueListAdapter(Context context, Group venues) {
        super(context, venues);
        mInflater = LayoutInflater.from(context);
    }

    /**
     * Make a view to hold each row.
     *
     * @see android.widget.ListAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (DEBUG) Log.d(TAG, "getView() called for position: " + position);
        // A ViewHolder keeps references to children views to avoid unnecessary
        // calls to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no
        // need to re-inflate it. We only inflate a new View when the
        // convertView supplied by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.venue_list_item, null);

            // Creates a ViewHolder and store references to the two children
            // views we want to bind data to.
            holder = new ViewHolder();
            holder.icon = (ImageView)convertView.findViewById(R.id.icon);
            holder.venueName = (TextView)convertView.findViewById(R.id.venueName);
            holder.locationLine1 = (TextView)convertView.findViewById(R.id.venueLocationLine1);
            holder.locationLine2 = (TextView)convertView.findViewById(R.id.venueLocationLine2);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder)convertView.getTag();
        }

        Venue venue = (Venue)getItem(position);
        Stats stats = venue.getStats();
        if (stats != null && stats.getBeenhere() != null && stats.getBeenhere().me()) {
            if (DEBUG) Log.d(TAG, "Using been here icon");
            holder.icon.setImageResource(R.drawable.map_marker_blue);
        } else {
            if (DEBUG) Log.d(TAG, "Using never been here icon");
            holder.icon.setImageResource(R.drawable.map_marker_blue_muted);
        }

        holder.venueName.setText(venue.getName());
        holder.locationLine1.setText(venue.getAddress());

        String line2 = StringFormatters.getVenueLocationCrossStreetOrCity(venue);
        if (line2 == null) {
            holder.locationLine2.setVisibility(View.GONE);
        } else {
            holder.locationLine2.setText(line2);
        }
        if (DEBUG) Log.d(TAG, "Returning: " + convertView);
        return convertView;
    }

    private static class ViewHolder {
        ImageView icon;
        TextView venueName;
        TextView locationLine1;
        TextView locationLine2;
    }
}
