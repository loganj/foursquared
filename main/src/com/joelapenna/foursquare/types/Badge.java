/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-11-12 21:45:35.596207
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com), implemented Parcelable.
 */
public class Badge implements FoursquareType, Parcelable {

    private String mDescription;
    private String mIcon;
    private String mId;
    private String mName;

    public Badge() {
    }
    
    private Badge(Parcel in) {
        mDescription = in.readString();
        mIcon = in.readString();
        mId = in.readString();
        mName = in.readString();
    }
    
    public static final Parcelable.Creator<Badge> CREATOR = new Parcelable.Creator<Badge>() {
        public Badge createFromParcel(Parcel in) {
            return new Badge(in);
        }

        @Override
        public Badge[] newArray(int size) {
            return new Badge[size];
        }
    };

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mDescription);
        out.writeString(mIcon);
        out.writeString(mId);
        out.writeString(mName);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
