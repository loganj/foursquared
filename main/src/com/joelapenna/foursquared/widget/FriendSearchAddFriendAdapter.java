/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.util.RemoteResourceManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * @date February 14, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public class FriendSearchAddFriendAdapter extends BaseGroupAdapter<User> {

    private static final String TAG = "";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private LayoutInflater mInflater;
    private int mLayoutToInflate;
    private ButtonRowClickHandler mClickListener;
    private RemoteResourceManager mRrm;
    private Handler mHandler = new Handler();

    public FriendSearchAddFriendAdapter(Context context, ButtonRowClickHandler clickListener,
            RemoteResourceManager rrm) {
        super(context);
        mInflater = LayoutInflater.from(context);
        mLayoutToInflate = R.layout.add_friends_list_item;
        mClickListener = clickListener;
        mRrm = rrm;

        mRrm.addObserver(new RemoteResourceManagerObserver());
    }

    public FriendSearchAddFriendAdapter(Context context, int layoutResource) {
        super(context);
        mInflater = LayoutInflater.from(context);
        mLayoutToInflate = layoutResource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary
        // calls to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no
        // need to re-inflate it. We only inflate a new View when the
        // convertView supplied by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutToInflate, null);

            // Creates a ViewHolder and store references to the two children
            // views we want to bind data to.
            holder = new ViewHolder();
            holder.main = (LinearLayout) convertView.findViewById(R.id.addFriendListItemBackground);
            holder.photo = (ImageView) convertView.findViewById(R.id.addFriendListItemPhoto);
            holder.name = (TextView) convertView.findViewById(R.id.addFriendListItemName);
            holder.add = (Button) convertView.findViewById(R.id.addFriendListItemAddButton);

            convertView.setTag(holder);

            holder.photo.setOnClickListener(mOnClickListenerInfo);
            holder.add.setOnClickListener(mOnClickListenerAdd);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        User user = (User) getItem(position);

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

        holder.photo.setTag(new Integer(position));
        holder.name.setText(user.getFirstname() + " "
                + (user.getLastname() != null ? user.getLastname() : ""));
        holder.add.setTag(new Integer(position));

        return convertView;
    }

    private OnClickListener mOnClickListenerAdd = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer position = (Integer) v.getTag();
            mClickListener.onBtnClickAdd((User) getItem(position));
        }
    };

    private OnClickListener mOnClickListenerInfo = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mClickListener != null) {
                Integer position = (Integer) v.getTag();
                mClickListener.onPhotoClick((User) getItem(position));
            }
        }
    };

    public void removeItem(int position) throws IndexOutOfBoundsException {
        group.remove(position);
        notifyDataSetInvalidated();
    }

    @Override
    public void setGroup(Group<User> g) {
        super.setGroup(g);
        for (int i = 0; i < g.size(); i++) {
            Uri photoUri = Uri.parse(g.get(i).getPhoto());
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

    static class ViewHolder {
        LinearLayout main;
        ImageView photo;
        TextView name;
        Button add;
    }

    public interface ButtonRowClickHandler {
        public void onBtnClickAdd(User user);

        public void onPhotoClick(User user);
    }
}
