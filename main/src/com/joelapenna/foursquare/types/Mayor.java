/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-08-01 10:38:32.574807
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Mayor implements Parcelable, FoursquareType {

    private String mCheckins;
    private String mCount;
    private String mMessage;
    private String mType;
    private User mUser;

    public Mayor() {
    }

    public String getCheckins() {
        return mCheckins;
    }

    public void setCheckins(String checkins) {
        mCheckins = checkins;
    }

    public String getCount() {
        return mCount;
    }

    public void setCount(String count) {
        mCount = count;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
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
        dest.writeString(this.mCheckins);
        dest.writeString(this.mCount);
        dest.writeString(this.mMessage);
        dest.writeString(this.mType);
        dest.writeParcelable(this.mUser, 0);
    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[0];
        source.readBooleanArray(booleanArray);
        this.mCheckins = source.readString();
        this.mCount = source.readString();
        this.mMessage = source.readString();
        this.mType = source.readString();
        this.mUser = source.readParcelable(null);
    }

    public static final Parcelable.Creator<Mayor> CREATOR = new Parcelable.Creator<Mayor>() {

        @Override
        public Mayor createFromParcel(Parcel source) {
            Mayor instance = new Mayor();
            instance.readFromParcel(source);
            return instance;
        }

        @Override
        public Mayor[] newArray(int size) {
            return new Mayor[size];
        }

    };

}
