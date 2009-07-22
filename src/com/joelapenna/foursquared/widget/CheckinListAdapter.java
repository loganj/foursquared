/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.Foursquared;
import com.joelapenna.foursquared.R;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class CheckinListAdapter extends BaseCheckinAdapter {
    private static final String TAG = "CheckinListAdapter";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private LayoutInflater mInflater;

    public CheckinListAdapter(Context context, Group checkins) {
        super(context, checkins);
        mInflater = LayoutInflater.from(context);
    }

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
            convertView = mInflater.inflate(R.layout.checkin_list_item, null);

            // Creates a ViewHolder and store references to the two children
            // views we want to bind data to.
            holder = new ViewHolder();
            holder.firstLine = (TextView)convertView.findViewById(R.id.firstLine);
            holder.secondLine = (TextView)convertView.findViewById(R.id.secondLine);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder)convertView.getTag();
        }

        Checkin checkin = (Checkin)getItem(position);
        String firstLineString;
        if (checkin.getDisplay() != null) {
            firstLineString = checkin.getDisplay();
        } else {
            firstLineString = checkin.getFirstname() + " " + checkin.getLastname().substring(0, 1) + ".";
        }
        // Popping from string->html fixes things like "&amp;" converting it back to a string
        // prevents a stack overflow in cupcake.
        holder.firstLine.setText(firstLineString);
        holder.secondLine.setText(Html.fromHtml(checkin.getRelativeTime()).toString());

        return convertView;
    }

<<<<<<< HEAD:src/com/joelapenna/foursquared/widget/CheckinListAdapter.java
    @Override
    public boolean hasStableIds() {
        return false;
    }

<<<<<<< HEAD:src/com/joelapenna/foursquared/CheckinListAdapter.java
    @Override
    public boolean isEmpty() {
        if (DEBUG) Log.d(TAG, "isEmpty() called");
        return (mCheckins.size() <= 0);
=======
    public static Venue venueFromCheckin(Checkin checkin) {
        Venue venue = new Venue();
        venue.setAddress(checkin.getAddress());
        venue.setCity(checkin.getCityName());
        venue.setCrossstreet(checkin.getCrossstreet());
        venue.setGeolat(checkin.getGeolat());
        venue.setGeolong(checkin.getGeolong());
        venue.setVenueid(checkin.getVenueid());
        venue.setVenuename(checkin.getVenuename());
        return venue;
>>>>>>> 12c68cf... Use display attribute in list items when available.:src/com/joelapenna/foursquared/widget/CheckinListAdapter.java
    }

    private static class ViewHolder {
=======
    private class ViewHolder {
>>>>>>> d021be6... Extract out a base adapter for venues and checkins:src/com/joelapenna/foursquared/widget/CheckinListAdapter.java
        TextView firstLine;
        TextView secondLine;
    }
}
