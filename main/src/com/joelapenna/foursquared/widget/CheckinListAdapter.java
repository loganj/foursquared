/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
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
public class CheckinListAdapter extends BaseCheckinAdapter {
    private static final String TAG = "CheckinListAdapter";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private LayoutInflater mInflater;

    private RemoteResourceManager mRrm;
    private Handler mHandler = new Handler();
    RemoteResourceManagerObserver mUserPhotosObserver = new RemoteResourceManagerObserver();

    public CheckinListAdapter(Context context, Group checkins, RemoteResourceManager rrm) {
        super(context, checkins);
        mInflater = LayoutInflater.from(context);
        mRrm = rrm;
        mRrm.addObserver(mUserPhotosObserver);
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
            holder.photo = (ImageView)convertView.findViewById(R.id.photo);
            holder.firstLine = (TextView)convertView.findViewById(R.id.firstLine);
            holder.shoutTextView = (TextView)convertView.findViewById(R.id.shoutTextView);
            holder.timeTextView = (TextView)convertView.findViewById(R.id.timeTextView);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder)convertView.getTag();
        }

        Checkin checkin = (Checkin)getItem(position);
        User user = checkin.getUser();
        Uri photo = Uri.parse(user.getPhoto());

        if (mRrm.getFile(photo).exists()) {
            try {
                Bitmap bitmap;
                bitmap = BitmapFactory.decodeStream(mRrm.getInputStream(photo));
                holder.photo.setImageBitmap(bitmap);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
        } else {
            mRrm.request(photo);
        }

        holder.firstLine.setText(StringFormatters.getCheckinMessage(checkin));
        holder.timeTextView.setText(StringFormatters
                .getRelativeTimeSpanString(checkin.getCreated()));

        if (checkin.getShout() != null) {
            holder.shoutTextView.setText(checkin.getShout());
        } else {
            holder.shoutTextView.setText("");
        }

        return convertView;
    }

    public static Venue venueFromCheckin(Checkin checkin) {
        Venue venue = new Venue();
        venue.setAddress(checkin.getVenue().getAddress());
        venue.setCity(checkin.getVenue().getCity());
        venue.setCrossstreet(checkin.getVenue().getCrossstreet());
        venue.setGeolat(checkin.getVenue().getGeolat());
        venue.setGeolong(checkin.getVenue().getGeolong());
        venue.setId(checkin.getVenue().getId());
        venue.setName(checkin.getVenue().getName());
        return venue;
    }

    private class RemoteResourceManagerObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            if (DEBUG) Log.d(TAG, "Fetcher got: " + data);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) Log.d(TAG, "notifyDataSetChanged()");
                    notifyDataSetChanged();
                }
            });
        }
    }

    private static class ViewHolder {
        ImageView photo;
        TextView firstLine;
        TextView shoutTextView;
        TextView timeTextView;
    }
}
