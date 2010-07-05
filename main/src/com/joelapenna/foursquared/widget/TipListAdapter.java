/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.Sync;
import com.joelapenna.foursquared.util.RemoteResourceManager;
import com.joelapenna.foursquared.util.StringFormatters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * @author jlapenna
 * @author Mark Wyszomierski (markww@gmail.com)
 *   -added photo support. (2010-03-25)
 */
public class TipListAdapter extends BaseTipAdapter 
    implements ObservableAdapter {
    
    private static final String TAG = "TipListAdapter";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private LayoutInflater mInflater;
    private RemoteResourceManager mRrm;
    private RemoteResourceManagerObserver mResourcesObserver;
    private Handler mHandler = new Handler();
    private int mLoadedPhotoIndex;
    private Context mContext;
    private Sync mSync;

    public TipListAdapter(Context context, RemoteResourceManager rrm, Sync sync) {
        super(context);
        mContext = context;
        mSync = sync;
        mInflater = LayoutInflater.from(context);
        mRrm = rrm;
        mResourcesObserver = new RemoteResourceManagerObserver();
        mLoadedPhotoIndex = 0;

        mRrm.addObserver(mResourcesObserver);
    }
    
    public void removeObserver() {
        mHandler.removeCallbacks(mRunnableLoadPhotos);
        mRrm.deleteObserver(mResourcesObserver);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary
        // calls to findViewById() on each row.
        ViewHolder holder;
        Tip tip = (Tip)getItem(position);
        User user = tip.getUser();

        // When convertView is not null, we can reuse it directly, there is no
        // need to re-inflate it. We only inflate a new View when the
        // convertView supplied by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.tip_list_item, null);

            // Creates a ViewHolder and store references to the two children
            // views we want to bind data to.
            holder = new ViewHolder();
            holder.photo =  (MaybeContactView)convertView.findViewById(R.id.tipPhoto);
            holder.tipTextView = (TextView)convertView.findViewById(R.id.tipTextView);
            holder.userTextView = (TextView)convertView.findViewById(R.id.userTextView);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder)convertView.getTag();
        }

        // Popping from string->html fixes things like "&amp;" converting it back to a string
        // prevents a stack overflow in cupcake.
        holder.tipTextView.setText(Html.fromHtml(tip.getText()).toString());
        holder.userTextView.setText("- " + StringFormatters.getUserAbbreviatedName(tip.getUser()));
        
        if (user != null) {
            holder.photo.setContactLookupUri(mSync.getContactLookupUri(mContext.getContentResolver(), user));

            Uri photoUri = Uri.parse(user.getPhoto());
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
        }

        return convertView;
    }
    
    @Override
    public void setGroup(Group<Tip> g) {
        super.setGroup(g);
        mLoadedPhotoIndex = 0;
        mHandler.postDelayed(mRunnableLoadPhotos, 10L);
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
    
    private Runnable mRunnableLoadPhotos = new Runnable() {
        @Override
        public void run() {
            if (mLoadedPhotoIndex < getCount()) {
                Tip tip = (Tip)getItem(mLoadedPhotoIndex++);
                User user = tip.getUser();
                if (user != null) {
                    Uri photoUri = Uri.parse(user.getPhoto());
                    if (!mRrm.exists(photoUri)) {
                        mRrm.request(photoUri);
                    }
                    mHandler.postDelayed(mRunnableLoadPhotos, 200L);
                }
            }
        }
    };

    private static class ViewHolder {
        MaybeContactView photo;
        TextView tipTextView;
        TextView userTextView;
    }
}
