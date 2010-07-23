
package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Category;
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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class CategoryPickerAdapter extends BaseAdapter implements ObservableAdapter {

    private static final String TAG = "CheckinListAdapter";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private LayoutInflater mInflater;
    private int mLayoutToInflate;
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
            
            File file = mRrm.getFile(photoUri);
            if (file != null) {
                if (System.currentTimeMillis() - file.lastModified() > FoursquaredSettings.CATEGORY_ICON_EXPIRATION) {
                    mRrm.invalidate(photoUri); 
                    file = null;
                }
            }
            
            if (file == null) {
                mRrm.request(photoUri);
            }
        }
    }
    
    public void removeObserver() {
        mRrm.deleteObserver(mResourcesObserver);
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
            holder.icon = (ImageView) convertView.findViewById(R.id.categoryPickerIcon);
            holder.name = (TextView) convertView.findViewById(R.id.categoryPickerName);
            holder.disclosure = (ImageView) convertView.findViewById(R.id.categoryPickerIconDisclosure);

            convertView.setTag(holder);
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
            if (DEBUG) Log.e(TAG, "Error loading category icon.", e);
        }
        
        if (category.getChildCategories() != null && category.getChildCategories().size() > 0) {
            holder.disclosure.setVisibility(View.VISIBLE);
        } else {
            holder.disclosure.setVisibility(View.GONE);
        }

        holder.name.setText(category.getNodeName());

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
        TextView name;
        ImageView disclosure;
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
