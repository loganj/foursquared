/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquare.types;

import com.joelapenna.foursquare.util.ParcelUtils;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * @date March 6, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class Category implements FoursquareType, Parcelable {

    /** The category's id. */
    private String mId;

    /** Full category path name, like Nightlife:Bars. */
    private String mFullPathName;

    /** Simple name of the category. */
    private String mNodeName;

    /** Url of the icon associated with this category. */
    private String mIconUrl;
    
    /** Categories can be nested within one another too. */
    private Group<Category> mChildCategories;

    
    public Category() {
        mChildCategories = new Group<Category>();
    }
    
    private Category(Parcel in) {
        mChildCategories = new Group<Category>();
        
        mId = ParcelUtils.readStringFromParcel(in);
        mFullPathName = ParcelUtils.readStringFromParcel(in);
        mNodeName = ParcelUtils.readStringFromParcel(in);
        mIconUrl = ParcelUtils.readStringFromParcel(in);
        int numCategories = in.readInt();
        for (int i = 0; i < numCategories; i++) {
            Category category = in.readParcelable(Category.class.getClassLoader());
            mChildCategories.add(category);
        }
    }
    
    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override 
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getFullPathName() {
        return mFullPathName;
    }

    public void setFullPathName(String fullPathName) {
        mFullPathName = fullPathName;
    }

    public String getNodeName() {
        return mNodeName;
    }

    public void setNodeName(String nodeName) {
        mNodeName = nodeName;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public void setIconUrl(String iconUrl) {
        mIconUrl = iconUrl;
    }
    
    public Group<Category> getChildCategories() {
        return mChildCategories;
    }
    
    public void setChildCategories(Group<Category> categories) {
        mChildCategories = categories;
    }
    
    @Override
    public void writeToParcel(Parcel out, int flags) {
        ParcelUtils.writeStringToParcel(out, mId);
        ParcelUtils.writeStringToParcel(out, mFullPathName);
        ParcelUtils.writeStringToParcel(out, mNodeName);
        ParcelUtils.writeStringToParcel(out, mIconUrl);

        out.writeInt(mChildCategories.size());
        for (Category it : mChildCategories) {
            out.writeParcelable(it, flags);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
