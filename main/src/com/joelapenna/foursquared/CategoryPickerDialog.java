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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

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
    

    public CategoryPickerDialog(Context context, Group<Category> categories, Foursquared application) { 
        super(context); 
        mApplication = application;
        mCategories = categories;
    }
    
    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
    
        setContentView(R.layout.category_picker_dialog);
        setTitle("Pick a Category");
        
        mViewFlipper = (ViewFlipper)findViewById(R.id.categoryPickerViewFlipper);
        
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
        page.ensureUI(view, mPageListItemSelected, category, mApplication.getRemoteResourceManager());
        view.setTag(page);
        
        return view;
    }
    
    private static class CategoryPickerPage {
        private CategoryPickerAdapter mAdapter;
        
        private Category mCategory;
        private PageListItemSelected mClickListener;
        
        public void ensureUI(View view, PageListItemSelected clickListener, Category category, RemoteResourceManager rrm) {
            mCategory = category;
            mClickListener = clickListener;
            mAdapter = new CategoryPickerAdapter(
                view.getContext(), 
                rrm, 
                category);
            
            ListView listview = (ListView)view.findViewById(R.id.categoryPickerListView);
            listview.setAdapter(mAdapter);
            listview.setOnItemClickListener(mOnItemClickListener);
        }
        
        private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                mClickListener.onPageListItemSelcected((Category)mAdapter.getItem(arg2));
            }
        };
    }
    
    private PageListItemSelected mPageListItemSelected = new PageListItemSelected() {
        @Override
        public void onPageListItemSelcected(Category category) {
            if (category.getChildCategories() != null && category.getChildCategories().size() > 0) {
                mViewFlipper.addView(makePage(category));
                mViewFlipper.showNext();
            }
        }
    };
    
    private interface PageListItemSelected {
        public void onPageListItemSelcected(Category category);
    }
}
