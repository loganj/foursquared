/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.util.StringFormatters;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author jlapenna
 */
public class TipListAdapter extends BaseTipAdapter {
    private static final String TAG = "TipListAdapter";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private LayoutInflater mInflater;

    public TipListAdapter(Context context, Group venues) {
        super(context, venues);
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
            convertView = mInflater.inflate(R.layout.tip_list_item, null);

            // Creates a ViewHolder and store references to the two children
            // views we want to bind data to.
            holder = new ViewHolder();
            holder.tipTextView = (TextView)convertView.findViewById(R.id.tipTextView);
            holder.userTextView = (TextView)convertView.findViewById(R.id.userTextView);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder)convertView.getTag();
        }

        Tip tip = (Tip)getItem(position);
        // Popping from string->html fixes things like "&amp;" converting it back to a string
        // prevents a stack overflow in cupcake.
        holder.tipTextView.setText(Html.fromHtml(tip.getText()).toString());
        holder.userTextView.setText("- " + StringFormatters.getUserAbbreviatedName(tip.getUser()));

        return convertView;
    }

    private static class ViewHolder {
        TextView tipTextView;
        TextView userTextView;
    }
}
