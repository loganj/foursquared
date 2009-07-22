/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Group;
<<<<<<< HEAD:src/com/joelapenna/foursquared/widget/VenueListAdapter.java
import com.joelapenna.foursquare.types.Venue;
<<<<<<< HEAD:src/com/joelapenna/foursquared/VenueListAdapter.java
import com.joelapenna.foursquared.util.StringFormatters;
=======
=======
import com.joelapenna.foursquare.types.classic.Venue;
>>>>>>> 66b622c... Move "classic" data types and parsers to subdirectories.:src/com/joelapenna/foursquared/widget/VenueListAdapter.java
import com.joelapenna.foursquared.Foursquared;
import com.joelapenna.foursquared.R;
>>>>>>> d021be6... Extract out a base adapter for venues and checkins:src/com/joelapenna/foursquared/widget/VenueListAdapter.java

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueListAdapter extends BaseVenueAdapter {
    private static final String TAG = "VenueListAdapter";
    private static final boolean DEBUG = Foursquared.DEBUG;

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

<<<<<<< HEAD:src/com/joelapenna/foursquared/VenueListAdapter.java
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
=======
    private class ViewHolder {
>>>>>>> d021be6... Extract out a base adapter for venues and checkins:src/com/joelapenna/foursquared/widget/VenueListAdapter.java
        TextView name;
        TextView locationLine1;
        TextView locationLine2;
    }
}
