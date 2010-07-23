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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueListAdapter extends BaseVenueAdapter implements ObservableAdapter {
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
            holder.iconSpecial = (ImageView) convertView.findViewById(R.id.iconSpecialHere);
            holder.venueDistance = (TextView) convertView.findViewById(R.id.venueDistance);
            holder.iconTrending = (ImageView) convertView.findViewById(R.id.iconTrending);
            holder.venueCheckinCount = (TextView) convertView.findViewById(R.id.venueCheckinCount);
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

        // Venue name.
        holder.venueName.setText(venue.getName());

        // Venue street address (cross streets | city, state zip).
        if (!TextUtils.isEmpty(venue.getAddress())) { 
            StringBuilder sb = new StringBuilder(128);
            sb.append(venue.getAddress());
            String crossStreets = StringFormatters.getVenueLocationCrossStreetOrCity(venue);
            if (crossStreets != null) {
                sb.append(" ");
                sb.append(crossStreets);
            }
            holder.locationLine1.setText(sb.toString());
        } else {
            holder.locationLine1.setText("");
        }
        
        // If there's a special here, show the special here icon.
        if (venue.getSpecials() != null && venue.getSpecials().size() > 0) {
            holder.iconSpecial.setVisibility(View.VISIBLE);
        } else {
            holder.iconSpecial.setVisibility(View.GONE);
        }
        
        // Show venue distance.
        if (venue.getDistance() != null) {
            holder.venueDistance.setText(venue.getDistance() + " meters");
        } else {
            holder.venueDistance.setText("");
        }
        
        // If more than two people here, then show trending text.
        Stats stats = venue.getStats();
        if (stats != null && 
           !stats.getHereNow().equals("0") &&
           !stats.getHereNow().equals("1") &&
           !stats.getHereNow().equals("2")) {
            holder.iconTrending.setVisibility(View.VISIBLE);
            holder.venueCheckinCount.setVisibility(View.VISIBLE);
            holder.venueCheckinCount.setText(stats.getHereNow() + " people here");
        } else {
            holder.iconTrending.setVisibility(View.GONE);
            holder.venueCheckinCount.setVisibility(View.GONE);
        }
        
        return convertView;
    }

    private void setDefaultVenueCategoryIcon(Venue venue, ViewHolder holder) {
        holder.icon.setImageResource(R.drawable.category_none);
    }

    @Override
    public void setGroup(Group<Venue> g) {
        super.setGroup(g);
        
        for (Venue it : g) {
            // Start download of category icon if not already in the cache.
            // At the same time, check the age of each of these images, if
            // expired, delete and request a fresh copy. This should be 
            // removed once category icon set urls are versioned.
            Category category = it.getCategory();
            if (category != null) {
                Uri photoUri = Uri.parse(category.getIconUrl());
                
                File file = mRrm.getFile(photoUri);
                if (file != null) {
                    if (System.currentTimeMillis() - file.lastModified() > FoursquaredSettings.CATEGORY_ICON_EXPIRATION) {
                        mRrm.invalidate(photoUri); 
                        file = null;
                    }
                }
                
                if (file == null) {
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
        ImageView iconSpecial;
        TextView venueDistance;
        ImageView iconTrending;
        TextView venueCheckinCount;
    }
}
