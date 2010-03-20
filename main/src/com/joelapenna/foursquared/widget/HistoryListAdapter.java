/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Category;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.util.RemoteResourceManager;
import com.joelapenna.foursquared.util.StringFormatters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * @date March 9, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class HistoryListAdapter extends BaseCheckinAdapter 
    implements ObservableAdapter {

    private LayoutInflater mInflater;
    private RemoteResourceManager mRrm;
    private Handler mHandler;
    private RemoteResourceManagerObserver mResourcesObserver;
    

    public HistoryListAdapter(Context context, RemoteResourceManager rrm) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.history_list_item, null);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.firstLine = (TextView) convertView.findViewById(R.id.firstLine);
            holder.shoutTextView = (TextView) convertView.findViewById(R.id.shoutTextView);
            holder.timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        Checkin checkin = (Checkin) getItem(position);

        if (checkin.getVenue() != null) {
            holder.firstLine.setText(checkin.getVenue().getName());
            holder.firstLine.setVisibility(View.VISIBLE);
            
            Category category = checkin.getVenue().getCategory();
            if (category != null) {
                Uri photoUri = Uri.parse(category.getIconUrl());
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(mRrm.getInputStream(photoUri));
                    holder.icon.setImageBitmap(bitmap);
                } catch (IOException e) {
                    holder.icon.setImageResource(R.drawable.category_none);
                }
            } else {
                holder.icon.setImageResource(R.drawable.category_none);
            }
            
        } else {
            // This is going to be a shout then.
            holder.icon.setImageResource(R.drawable.ic_menu_shout);
            holder.firstLine.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(checkin.getShout()) == false) {
            holder.shoutTextView.setText(checkin.getShout());
            holder.shoutTextView.setVisibility(View.VISIBLE);
        } else {
            holder.shoutTextView.setVisibility(View.GONE);
        } 

        holder.timeTextView.setText( 
            StringFormatters.getRelativeTimeSpanString(checkin.getCreated()));
        
        return convertView;
    }
    
    private class RemoteResourceManagerObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
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
        TextView firstLine;
        TextView shoutTextView;
        TextView timeTextView;
    }
}
