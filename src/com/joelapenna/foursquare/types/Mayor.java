/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-06-10 00:51:03.442348
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Mayor implements Parcelable, FoursquareType {

    private String mCount;
    private User mUser;
    
    public Mayor() {
    }
    
    public String getCount() {
        return mCount;
    }
    
    public void setCount(String count) {
        mCount = count;
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
        dest.writeString(this.mCount);    
        dest.writeParcelable(this.mUser, 0);
    }
    
    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[0];
        source.readBooleanArray(booleanArray);
        this.mCount = source.readString();    
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
