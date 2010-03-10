/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
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

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class CheckinListAdapter extends BaseCheckinAdapter 
    implements ObservableAdapter {
    
    private static final String TAG = "CheckinListAdapter";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private LayoutInflater mInflater;

    private RemoteResourceManager mRrm;
    private RemoteResourceManagerObserver mResourcesObserver;
    private Handler mHandler = new Handler();

    public CheckinListAdapter(Context context, RemoteResourceManager rrm) {
        super(context);
        mInflater = LayoutInflater.from(context);
        mRrm = rrm;
        mResourcesObserver = new RemoteResourceManagerObserver();

        mRrm.addObserver(mResourcesObserver);
    }

    public void removeObserver() {
        mRrm.deleteObserver(mResourcesObserver);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
            holder.secondLine = (TextView)convertView.findViewById(R.id.secondLine);
            holder.shoutTextView = (TextView)convertView.findViewById(R.id.shoutTextView);
            holder.timeTextView = (TextView)convertView.findViewById(R.id.timeTextView);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder)convertView.getTag();
        }

        Checkin checkin = (Checkin)getItem(position);
        final User user = checkin.getUser();
        final Uri photoUri = Uri.parse(user.getPhoto());

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(mRrm.getInputStream(photoUri));
            holder.photo.setImageBitmap(bitmap);
        } catch (IOException e) {
            if (Foursquare.MALE.equals(user.getGender())) {
                holder.photo.setImageResource(R.drawable.blank_boy);
            } else {
                holder.photo.setImageResource(R.drawable.blank_girl);
            }
        }

        // Always show the checkin message itself, this is server-generated.
        holder.firstLine.setText(StringFormatters.getCheckinMessage(checkin, true));
        
        // If we have a shout, we show that in place of the venue address.
        if (TextUtils.isEmpty(checkin.getShout()) == false) {
            holder.shoutTextView.setText(checkin.getShout());
            holder.shoutTextView.setVisibility(View.VISIBLE);
            holder.secondLine.setVisibility(View.GONE);
        } else {
            // No shout, show address instead.
            holder.shoutTextView.setText("");
            holder.shoutTextView.setVisibility(View.GONE);
            
            if (checkin.getVenue() != null && checkin.getVenue().getAddress() != null) {
                String address = checkin.getVenue().getAddress();
                if (checkin.getVenue().getCrossstreet() != null && 
                    checkin.getVenue().getCrossstreet().length() > 0) {
                    address += " (" + checkin.getVenue().getCrossstreet() + ")";
                }
                holder.secondLine.setText(address);
                holder.secondLine.setVisibility(View.VISIBLE);
            }
            else {
                holder.secondLine.setVisibility(View.GONE);
            }
        }

        holder.timeTextView.setText(StringFormatters
                .getRelativeTimeSpanString(checkin.getCreated()));

        return convertView;
    }

    @Override
    public void setGroup(Group<Checkin> g) {
        super.setGroup(g);
        for (int i = 0; i < g.size(); i++) {
            Uri photoUri = Uri.parse((g.get(i)).getUser().getPhoto());
            if (!mRrm.exists(photoUri)) {
                mRrm.request(photoUri);
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
        ImageView photo;
        TextView firstLine;
        TextView secondLine;
        TextView shoutTextView;
        TextView timeTextView;
    }
}
