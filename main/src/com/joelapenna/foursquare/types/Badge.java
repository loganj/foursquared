/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-08-01 10:38:31.592075
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Badge implements Parcelable, FoursquareType {

    private String mDescription;
    private String mIcon;
    private String mId;
    private String mMessage;
    private String mName;

    public Badge() {
    }

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

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    /* For Parcelable */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean[] booleanArray = {

        };
        dest.writeBooleanArray(booleanArray);
        dest.writeString(this.mDescription);
        dest.writeString(this.mIcon);
        dest.writeString(this.mId);
        dest.writeString(this.mMessage);
        dest.writeString(this.mName);
    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[0];
        source.readBooleanArray(booleanArray);
        this.mDescription = source.readString();
        this.mIcon = source.readString();
        this.mId = source.readString();
        this.mMessage = source.readString();
        this.mName = source.readString();
    }

    public static final Parcelable.Creator<Badge> CREATOR = new Parcelable.Creator<Badge>() {

        @Override
        public Badge createFromParcel(Parcel source) {
            Badge instance = new Badge();
            instance.readFromParcel(source);
            return instance;
        }

        @Override
        public Badge[] newArray(int size) {
            return new Badge[size];
        }

    };

}
