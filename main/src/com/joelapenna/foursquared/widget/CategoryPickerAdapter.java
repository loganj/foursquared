package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Category;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.util.RemoteResourceManager;
import com.joelapenna.foursquared.widget.FriendRequestsAdapter.ButtonRowClickHandler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class CategoryPickerAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private int mLayoutToInflate;
    private ButtonRowClickHandler mClickListener;
    private RemoteResourceManager mRrm;
    private RemoteResourceManagerObserver mResourcesObserver;
    private Handler mHandler = new Handler();
    private Category mCategory;

    public CategoryPickerAdapter(Context context, RemoteResourceManager rrm, Category category) {
        super(); 
        mCategory = category;
        mInflater = LayoutInflater.from(context);
        mLayoutToInflate = R.layout.category_picker_list_item;
        mRrm = rrm;
        mResourcesObserver = new RemoteResourceManagerObserver();

        mRrm.addObserver(mResourcesObserver);
        
        for (Category it : mCategory.getChildCategories()) {
            Uri photoUri = Uri.parse(it.getIconUrl());
            if (!mRrm.exists(photoUri)) {
                mRrm.request(photoUri);
            }
        }
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
            holder.indent = (LinearLayout) convertView.findViewById(R.id.categoryPickerPadding);
            holder.icon = (ImageView) convertView.findViewById(R.id.categoryPickerIcon);
            holder.name = (TextView) convertView.findViewById(R.id.categoryPickerName);

            convertView.setTag(holder);

//            holder.clickable.setOnClickListener(mOnClickListenerInfo);
//            holder.add.setOnClickListener(mOnClickListenerApprove);
//            holder.ignore.setOnClickListener(mOnClickListenerDeny);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        Category category = (Category) getItem(position);

        final Uri photoUri = Uri.parse(category.getIconUrl());
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(mRrm.getInputStream(photoUri));
            holder.icon.setImageBitmap(bitmap);
        } catch (IOException e) {
//            holder.photo.setImageResource(R.drawable.blank_boy);
        }
 
        holder.name.setText(category.getNodeName());
    //    holder.clickable.setTag(new Integer(position));
    //    holder.add.setTag(new Integer(position));
    //    holder.ignore.setTag(new Integer(position));

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
        LinearLayout indent;
        ImageView icon;
        TextView name;
    }

    @Override
    public int getCount() {
        return mCategory.getChildCategories().size();
    }

    @Override
    public Object getItem(int position) {
        return mCategory.getChildCategories().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public boolean hasStableIds() {
        return true;
    }
    
    public static class CategoryFlat {
        private Category mCategory;
        private int mDepth;
        
        public CategoryFlat(Category category, int depth) {
            mCategory = category;
            mDepth = depth;
        }
        
        public Category getCategory() {
            return mCategory;
        }
        
        public int getDepth() {
            return mDepth;
        }
    }
}