/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Tip;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * @author jlapenna
 */
class TipsListAdapter extends BaseAdapter {
    private static final String TAG = "TipsListAdapter";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private LayoutInflater mInflater;
    private List<Tip> mTips;

    public TipsListAdapter(Context context, List<Tip> tips) {
        mInflater = LayoutInflater.from(context);
        mTips = tips;
    }

    /**
     * The number of items in the list is determined by the number of tips in our array.
     *
     * @see android.widget.ListAdapter#getCount()
     */
    @Override
    public int getCount() {
        return mTips.size();
    }

    @Override
    public Object getItem(int position) {
        if (DEBUG) Log.d(TAG, "getItem() called");
        return mTips.get(position);
    }

    /**
     * Use the position index as a unique id.
     *
     * @see android.widget.ListAdapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        if (DEBUG) Log.d(TAG, "getItemId() called");
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
            convertView = mInflater.inflate(R.layout.tips_list_item, null);

            // Creates a ViewHolder and store references to the two children
            // views we want to bind data to.
            holder = new ViewHolder();
            holder.firstLine = (TextView)convertView.findViewById(R.id.firstLine);
            holder.secondLine = (TextView)convertView.findViewById(R.id.secondLine);
            holder.checkbox = (CheckBox)convertView.findViewById(R.id.checkbox);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder)convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        // TODO(jlapenna): Make this "relative" when cupcake comes out, using
        // DateUtils.getRelativeTimeSpanString

        Tip tip = (Tip)getItem(position);
        holder.firstLine.setText(tip.getFirstname() + " " + tip.getLastname() + " did this...");
        holder.secondLine.setText(Html.fromHtml(tip.getText()));
        holder.checkbox.setChecked(tip.getUserStatus().equals("done") ? true : false);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        if (DEBUG) Log.d(TAG, "isEmpty() called");
        return (mTips.size() <= 0);
    }

    private static class ViewHolder {
        TextView firstLine;
        TextView secondLine;
        CheckBox checkbox;
    }
}
