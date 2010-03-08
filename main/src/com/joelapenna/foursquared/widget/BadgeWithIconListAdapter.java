/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Badge;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.FoursquaredSettings;
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
public class BadgeWithIconListAdapter extends BadgeListAdapter 
    implements ObservableAdapter {
    
    private static final String TAG = "BadgeWithIconListAdapter";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private RemoteResourceManager mRrm;
    private Handler mHandler = new Handler();
    private RemoteResourceManagerObserver mResourcesObserver;

    /**
     * @param context
     * @param venues
     */
    public BadgeWithIconListAdapter(Context context, RemoteResourceManager rrm) {
        super(context);
        mRrm = rrm;
        mResourcesObserver = new RemoteResourceManagerObserver();

        mRrm.addObserver(mResourcesObserver);
    }

    public BadgeWithIconListAdapter(Context context, RemoteResourceManager rrm, int layoutResource) {

        super(context, layoutResource);
        mRrm = rrm;
        mResourcesObserver = new RemoteResourceManagerObserver();

        mRrm.addObserver(mResourcesObserver);
    }

    public void removeObserver() {
        mRrm.deleteObserver(mResourcesObserver);
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

    @Override
    public void setGroup(Group<Badge> g) {
        super.setGroup(g);
        for (int i = 0; i < group.size(); i++) {
            Uri photoUri = Uri.parse((group.get(i)).getIcon());
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
}
