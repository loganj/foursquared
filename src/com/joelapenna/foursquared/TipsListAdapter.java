/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Tip;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jlapenna
 */
class TipsListAdapter extends BaseAdapter {
    private static final String TAG = "TipsListAdapter";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private LayoutInflater mInflater;
    private List<Tip> mTips = new ArrayList<Tip>();

    // Converting a string to an html span is actually pretty resource intensive, use a cache...
    // This causes a startup delay, might not want to do this on the UI thread.
    private List<Spanned> mHtmlCache = new ArrayList<Spanned>();

    public TipsListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
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
            convertView = mInflater.inflate(R.layout.tip_list_item, null);

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

        Tip tip = (Tip)getItem(position);
        holder.firstLine.setText(tip.getFirstname() + " " + tip.getLastname() + " did this...");
        holder.secondLine.setText(mHtmlCache.get(position));
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

    public void add(Tip tip) {
        mTips.add(tip);
        mHtmlCache.add(Html.fromHtml(tip.getText()));
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView firstLine;
        TextView secondLine;
        CheckBox checkbox;
    }
}
