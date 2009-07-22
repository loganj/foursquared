/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Group extends ArrayList<FoursquareType> implements Parcelable, FoursquareType {

    private static final long serialVersionUID = 1L;

    private String mType;

    public void setType(String type) {
        mType = type;
    }

    public String getType() {
        return mType;
    }

    /* For Parcelable */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this);
        dest.writeString(this.mType);
    }

    public static final Parcelable.Creator<Group> CREATOR = new Parcelable.Creator<Group>() {

        @Override
        public Group createFromParcel(Parcel source) {
            Group instance = new Group();
            source.readList(instance, null);
            instance.setType(source.readString());
            return instance;
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }

    };
}
