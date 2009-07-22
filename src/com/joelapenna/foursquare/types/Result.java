/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Result implements FoursquareType, Parcelable {

    private String mMessage;
    private boolean mStatus;
    
    public Result() {
    }
    
    public String getMessage() {
        return mMessage;
    }
    
    public void setMessage(String message) {
        mMessage = message;
    }
    
    public boolean status() {
        return mStatus;
    }
    
    public void setStatus(boolean status) {
        mStatus = status;
    }
    
    /* For Parcelable */
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean[] booleanArray = {
            mStatus,
        };
        dest.writeBooleanArray(booleanArray);
        dest.writeString(this.mMessage);
    }
    
    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[1];
        source.readBooleanArray(booleanArray);
        this.mStatus = booleanArray[0];    
        this.mMessage = source.readString();
    }
    
    public static final Parcelable.Creator<Result> CREATOR = new Parcelable.Creator<Result>() {
    
        @Override
        public Result createFromParcel(Parcel source) {
            Result instance = new Result();
            instance.readFromParcel(source);
            return instance;
        }
    
        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    
    };
    
}
