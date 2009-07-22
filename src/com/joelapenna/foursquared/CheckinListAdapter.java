/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author jlapenna
 */
class CheckinListAdapter extends BaseAdapter {
    private static final String TAG = "CheckinListAdapter";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private LayoutInflater mInflater;
    private Group mCheckins;

    public CheckinListAdapter(Context context, Group checkins) {
        mInflater = LayoutInflater.from(context);
        mCheckins = checkins;
    }

    /**
     * The number of items in the list is determined by the number of checkins in our array.
     *
     * @see android.widget.ListAdapter#getCount()
     */
    @Override
    public int getCount() {
        return mCheckins.size();
    }

    @Override
    public Object getItem(int position) {
        if (DEBUG) Log.d(TAG, "getItem() called: " + String.valueOf(position));
        return mCheckins.get(position);
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
        String firstLineString = checkin.getFirstname() + " " + checkin.getLastname().substring(0, 1) + ".";
        // Popping from string->html fixes things like "&amp;" converting it back to a string
        // prevents a stack overflow in cupcake.
        holder.firstLine.setText(firstLineString);
        holder.secondLine.setText(Html.fromHtml(checkin.getRelativeTime()).toString());

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        if (DEBUG) Log.d(TAG, "isEmpty() called");
        return (mCheckins.size() <= 0);
    }

    private static class ViewHolder {
        TextView firstLine;
        TextView secondLine;
    }
}
