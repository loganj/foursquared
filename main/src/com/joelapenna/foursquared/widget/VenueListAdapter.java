/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Category;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Stats;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.util.RemoteResourceManager;
import com.joelapenna.foursquared.util.StringFormatters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueListAdapter extends BaseVenueAdapter {
    private static final String TAG = "VenueListAdapter";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private LayoutInflater mInflater;
    private RemoteResourceManager mRrm;
    private Handler mHandler;
    private RemoteResourceManagerObserver mResourcesObserver;

    public VenueListAdapter(Context context, RemoteResourceManager rrm) {
        super(context);
        mInflater = LayoutInflater.from(context);
        mHandler = new Handler();
        mRrm = rrm;
        mResourcesObserver = new RemoteResourceManagerObserver();

        mRrm.addObserver(mResourcesObserver);
    }

    public void removeObserver() {
        mRrm.deleteObserver(mResourcesObserver);
    }

    /**
     * Make a view to hold each row.
     * 
     * @see android.widget.ListAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.venueName = (TextView) convertView.findViewById(R.id.venueName);
            holder.locationLine1 = (TextView) convertView.findViewById(R.id.venueLocationLine1);
            holder.locationLine2 = (TextView) convertView.findViewById(R.id.venueLocationLine2);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // Check if the venue category icon exists on disk, if not default to a
        // venue pin icon.
        Venue venue = (Venue) getItem(position);
        Category category = venue.getCategory();
        if (category != null) {
            Uri photoUri = Uri.parse(category.getIconUrl());
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(mRrm.getInputStream(photoUri));
                holder.icon.setImageBitmap(bitmap);
            } catch (IOException e) {
                setDefaultVenueCategoryIcon(venue, holder);
            }
        } else {
            // If there is no category for this venue, fall back to the original
            // method of the
            // blue/grey pin depending on if the user has been there or not.
            setDefaultVenueCategoryIcon(venue, holder);
        }

        holder.venueName.setText(venue.getName());
        holder.locationLine1.setText(venue.getAddress());

        String line2 = StringFormatters.getVenueLocationCrossStreetOrCity(venue);
        if (line2 != null) {
            holder.locationLine2.setText(line2);
        }
        if (DEBUG) Log.d(TAG, "Returning: " + convertView);
        return convertView;
    }

    private void setDefaultVenueCategoryIcon(Venue venue, ViewHolder holder) {
        Stats stats = venue.getStats();
        if (stats != null && stats.getBeenhere() != null && stats.getBeenhere().me()) {
            if (DEBUG) Log.d(TAG, "Using been here icon");
            holder.icon.setImageResource(R.drawable.map_marker_blue);
        } else {
            if (DEBUG) Log.d(TAG, "Using never been here icon");
            holder.icon.setImageResource(R.drawable.map_marker_blue_muted);
        }
    }

    @Override
    public void setGroup(Group<Venue> g) {
        super.setGroup(g);
        for (int i = 0; i < g.size(); i++) {
            Category category = g.get(i).getCategory();
            if (category != null) {
                Uri photoUri = Uri.parse(category.getIconUrl());
                if (!mRrm.exists(photoUri)) {
                    mRrm.request(photoUri);
                }
            }
        }
    }

    private class RemoteResourceManagerObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            if (DEBUG) Log.d(TAG, "Fetcher got: " + data);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    }

    private static class ViewHolder {
        ImageView icon;
        TextView venueName;
        TextView locationLine1;
        TextView locationLine2;
    }
}
