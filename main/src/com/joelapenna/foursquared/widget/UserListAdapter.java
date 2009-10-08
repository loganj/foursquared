/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.Foursquare;
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
public class UserListAdapter extends BaseUserAdapter {
    private static final String TAG = "UserListAdapter";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private LayoutInflater mInflater;

    private RemoteResourceManager mRrm;
    private Handler mHandler = new Handler();

    public UserListAdapter(Context context, RemoteResourceManager rrm) {
        super(context);
        mInflater = LayoutInflater.from(context);
        mRrm = rrm;

        mRrm.addObserver(new RemoteResourceManagerObserver());
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
            holder.shoutTextView = (TextView)convertView.findViewById(R.id.shoutTextView);
            holder.timeTextView = (TextView)convertView.findViewById(R.id.timeTextView);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder)convertView.getTag();
        }

        User user = (User)getItem(position);
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

        holder.firstLine.setText(StringFormatters.getUserAbbreviatedName(user));
        holder.timeTextView.setText(StringFormatters.getRelativeTimeSpanString(user.getCreated()));
        holder.shoutTextView.setText("");

        return convertView;
    }

    @Override
    public void setGroup(Group<User> g) {
        super.setGroup(g);
        for (int i = 0; i < g.size(); i++) {
            Uri photoUri = Uri.parse((g.get(i)).getPhoto());
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
        TextView shoutTextView;
        TextView timeTextView;
    }
}
