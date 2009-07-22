/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Badge;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.Foursquared;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.util.RemoteResourceManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class BadgeWithIconListAdapter extends BadgeListAdapter {
    private static final String TAG = "BadgeWithIconListAdapter";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private RemoteResourceManager mRrm;
    private Handler mHandler = new Handler();

    /**
     * @param context
     * @param venues
     */
    public BadgeWithIconListAdapter(Context context, Group badges, RemoteResourceManager rrm) {
        super(context, badges);
        mRrm = rrm;
        mRrm.addObserver(new RemoteResourceManagerObserver());

        for (int i = 0; i < badges.size(); i++) {
            Uri uri = Uri.parse(((Badge)badges.get(i)).getIcon());
            if (!rrm.getFile(uri).exists()) {
                if (DEBUG) Log.d(TAG, "Requesting: " + uri);
                rrm.request(uri);
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        Badge badge = (Badge)getItem(position);
        ImageView icon = ((BadgeWithIconListAdapter.ViewHolder)view.getTag()).icon;
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(//
                    mRrm.getInputStream(Uri.parse(badge.getIcon())));
            icon.setImageBitmap(bitmap);
        } catch (IOException e) {
            if (DEBUG) Log.d(TAG, "Could not load bitmap. We don't have it yet.");
            icon.setImageResource(R.drawable.default_on);
        }

        return view;
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
}
