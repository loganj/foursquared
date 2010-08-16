/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Category;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.util.RemoteResourceManager;
import com.joelapenna.foursquared.widget.CategoryPickerAdapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;

/**
 * Presents the user with a list of all available categories from foursquare
 * that they can use to label a new venue.
 * 
 * @date March 7, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public class CategoryPickerDialog extends Dialog {
    private static final String TAG = "FriendRequestsActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private Foursquared mApplication;
    private Group<Category> mCategories;
    private ViewFlipper mViewFlipper;
    private Category mChosenCategory;

    private int mFirstDialogHeight;

    public CategoryPickerDialog(Context context, Group<Category> categories, Foursquared application) {
        super(context);
        mApplication = application;
        mCategories = categories;
        mChosenCategory = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.category_picker_dialog);
        setTitle(getContext().getResources().getString(R.string.category_picket_dialog_title));
        
        mViewFlipper = (ViewFlipper) findViewById(R.id.categoryPickerViewFlipper);

        mFirstDialogHeight = -1;

        // By default we always have a top-level page.
        Category root = new Category();
        root.setNodeName("root");
        root.setChildCategories(mCategories);

        mViewFlipper.addView(makePage(root));
    }

    private View makePage(Category category) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.category_picker_page, null);

        CategoryPickerPage page = new CategoryPickerPage();
        page.ensureUI(view, mPageListItemSelected, category, mApplication
                .getRemoteResourceManager());
        view.setTag(page);

        if (mViewFlipper.getChildCount() == 1 && mFirstDialogHeight == -1) {
            mFirstDialogHeight = mViewFlipper.getChildAt(0).getHeight();
        }
        if (mViewFlipper.getChildCount() > 0) {
            view.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT, mFirstDialogHeight));
        }

        return view;
    }

    @Override
    protected void onStop() {
        super.onStop();

        cleanupPageAdapters();
    }

    private void cleanupPageAdapters() {
        for (int i = 0; i < mViewFlipper.getChildCount(); i++) {
            CategoryPickerPage page = (CategoryPickerPage) mViewFlipper.getChildAt(i).getTag();
            page.cleanup();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mViewFlipper.getChildCount() > 1) {
                    mViewFlipper.removeViewAt(mViewFlipper.getChildCount() - 1);
                    return true;
                }
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * After the user has dismissed the dialog, the parent activity can use this
     * to see which category they picked, if any. Will return null if no
     * category was picked.
     */
    public Category getChosenCategory() {
        return mChosenCategory;
    }

    private static class CategoryPickerPage {
        private CategoryPickerAdapter mListAdapter;

        private Category mCategory;
        private PageListItemSelected mClickListener;

        public void ensureUI(View view, PageListItemSelected clickListener, Category category,
                RemoteResourceManager rrm) {
            mCategory = category;
            mClickListener = clickListener;
            mListAdapter = new CategoryPickerAdapter(view.getContext(), rrm, category);

            ListView listview = (ListView) view.findViewById(R.id.categoryPickerListView);
            listview.setAdapter(mListAdapter);
            listview.setOnItemClickListener(mOnItemClickListener);

            LinearLayout llRootCategory = (LinearLayout) view
                    .findViewById(R.id.categoryPickerRootCategoryButton);
            if (category.getNodeName().equals("root") == false) {
                ImageView iv = (ImageView) view.findViewById(R.id.categoryPickerIcon);
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(rrm.getInputStream(Uri
                            .parse(category.getIconUrl())));
                    iv.setImageBitmap(bitmap);
                } catch (IOException e) {
                    if (DEBUG) Log.e(TAG, "Error loading category icon from disk.", e);
                }

                TextView tv = (TextView) view.findViewById(R.id.categoryPickerName);
                tv.setText(category.getNodeName());

                llRootCategory.setClickable(true);
                llRootCategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mClickListener.onCategorySelected(mCategory);
                    }
                });
            } else {
                llRootCategory.setVisibility(View.GONE);
            }
        }

        public void cleanup() {
            mListAdapter.removeObserver();
        }

        private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                mClickListener.onPageListItemSelcected((Category) mListAdapter.getItem(position));
            }
        };
    }

    private PageListItemSelected mPageListItemSelected = new PageListItemSelected() {
        @Override
        public void onPageListItemSelcected(Category category) {
            // If the item has children, create a new page for it.
            if (category.getChildCategories() != null && category.getChildCategories().size() > 0) {
                mViewFlipper.addView(makePage(category));
                mViewFlipper.showNext();
            } else {
                // This is a leaf node, finally the user's selection. Record the
                // category
                // then cancel ourselves, parent activity should pick us up
                // after that.
                mChosenCategory = category;
                cancel();
            }
        }

        @Override
        public void onCategorySelected(Category category) {
            // The user has chosen the category parent listed at the top of the
            // current page.
            mChosenCategory = category;
            cancel();
        }
    };

    private interface PageListItemSelected {
        public void onPageListItemSelcected(Category category);

        public void onCategorySelected(Category category);
    }
}
