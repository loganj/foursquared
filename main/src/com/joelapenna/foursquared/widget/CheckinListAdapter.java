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

    private boolean mDisplayAtVenue;

    public CheckinListAdapter(Context context, Group checkins, RemoteResourceManager rrm,
            boolean displayAtVenue) {
        super(context, checkins);
        mInflater = LayoutInflater.from(context);
        mRrm = rrm;
        mDisplayAtVenue = displayAtVenue;

        // Immediately start trying to grab the user photos. All of them!
        for (int i = 0; i < checkins.size(); i++) {
            Uri photoUri = Uri.parse(((Checkin)checkins.get(i)).getUser().getPhoto());
            if (!mRrm.getFile(photoUri).exists()) {
                mRrm.request(photoUri);
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (DEBUG) Log.d(TAG, "getView() called for position: " + position);
        // A ViewHolder keeps references to children views to avoid unnecessary
        // calls to findViewById() on each row.
        final ViewHolder holder;

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
        final String photo = user.getPhoto();
        final Uri photoUri = Uri.parse(photo);

        if (mRrm.getFile(photoUri).exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(mRrm.getInputStream(photoUri));
                holder.photo.setImageBitmap(bitmap);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
        } else {
            mRrm.request(photoUri);
            // XXX This solution won't scale when we fix the "convertView" bug in
            // SeparatedListAdapter that forces us to render new list items for all list items
            // instead of re-using them.
            // Because mRrm is "static"-like, we're probably going to leak observers... if we run
            // into memory issues, this is a good place to start looking. Otherwise, I'm just going
            // to leave this as is because I'm not super smart and dont' know how to deal with this
            // issue in a smart way.
            mRrm.addObserver(new Observer() {
                @Override
                public void update(Observable observable, Object data) {
                    if (photoUri.equals((Uri)data)) {
                        // Stop observing once we get the correct image.
                        observable.deleteObserver(this);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Bitmap bitmap = BitmapFactory.decodeStream(mRrm
                                            .getInputStream(photoUri));
                                    holder.photo.setImageBitmap(bitmap);
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    if (DEBUG) Log.d(TAG, "IOException", e);
                                }
                            }
                        });
                    }
                }
            });
        }

        holder.firstLine.setText(StringFormatters.getCheckinMessage(checkin, mDisplayAtVenue));
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

    private static class ViewHolder {
        ImageView photo;
        TextView firstLine;
        TextView shoutTextView;
        TextView timeTextView;
    }
}
