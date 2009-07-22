/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.StringFormatters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author jlapenna
 */
class VenueListAdapter extends BaseAdapter {
    private static final String TAG = "VenueListAdapter";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private LayoutInflater mInflater;
    private Group mVenues;

    public VenueListAdapter(Context context, Group venues) {
        mInflater = LayoutInflater.from(context);
        mVenues = venues;
    }

    /**
     * The number of items in the list is determined by the number of venues in our array.
     *
     * @see android.widget.ListAdapter#getCount()
     */
    @Override
    public int getCount() {
        return mVenues.size();
    }

    @Override
    public Object getItem(int position) {
        if (DEBUG) Log.d(TAG, "getItem() called: " + String.valueOf(position));
        return mVenues.get(position);
    }

    /**
     * Use the position index as a unique id.
     *
     * @see android.widget.ListAdapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        if (DEBUG) Log.d(TAG, "getItemId() called: " + String.valueOf(position));
        return position;
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
            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.locationLine1 = (TextView)convertView.findViewById(R.id.locationLine1);
            holder.locationLine2 = (TextView)convertView.findViewById(R.id.locationLine2);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder)convertView.getTag();
        }

        Venue venue = (Venue)getItem(position);
        if (DEBUG) Log.d(TAG, "getView() is: " + venue);
        holder.name.setText(venue.getVenuename());
        holder.locationLine1.setText(venue.getAddress());
        String line2 = StringFormatters.getVenueLocationLine2(venue);
        if (line2 == null) {
            holder.locationLine2.setVisibility(View.GONE);
        } else {
            holder.locationLine2.setText(line2);
        }
        if (DEBUG) Log.d(TAG, "Returning: " + convertView);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        if (DEBUG) Log.d(TAG, "isEmpty() called");
        return (mVenues.size() <= 0);
    }

    private static class ViewHolder {
        TextView name;
        TextView locationLine1;
        TextView locationLine2;
    }
}
