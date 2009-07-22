/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-06-10 00:51:03.238654
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Data implements Parcelable, FoursquareType {

    private String mCityid;
    private String mMessage;
    private boolean mStatus;
    
    public Data() {
    }
    
    public String getCityid() {
        return mCityid;
    }
    
    public void setCityid(String cityid) {
        mCityid = cityid;
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
        dest.writeString(this.mCityid);    
        dest.writeString(this.mMessage);
    }
    
    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[1];
        source.readBooleanArray(booleanArray);
        this.mStatus = booleanArray[0];    
        this.mCityid = source.readString();    
        this.mMessage = source.readString();
    }
    
    public static final Parcelable.Creator<Data> CREATOR = new Parcelable.Creator<Data>() {
    
        @Override
        public Data createFromParcel(Parcel source) {
            Data instance = new Data();
            instance.readFromParcel(source);
            return instance;
        }
    
        @Override
        public Data[] newArray(int size) {
            return new Data[size];
        }
    
    };
    
}
