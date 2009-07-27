/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-07-26 20:59:18.602834
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Settings implements Parcelable, FoursquareType {

    private String mFeedsKey;
    private boolean mSendtotwitter;

    public Settings() {
    }

    public String getFeedsKey() {
        return mFeedsKey;
    }

    public void setFeedsKey(String feedsKey) {
        mFeedsKey = feedsKey;
    }

    public boolean sendtotwitter() {
        return mSendtotwitter;
    }

    public void setSendtotwitter(boolean sendtotwitter) {
        mSendtotwitter = sendtotwitter;
    }

    /* For Parcelable */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean[] booleanArray = {
            mSendtotwitter,
        };
        dest.writeBooleanArray(booleanArray);
        dest.writeString(this.mFeedsKey);
    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[1];
        source.readBooleanArray(booleanArray);
        this.mSendtotwitter = booleanArray[0];
        this.mFeedsKey = source.readString();
    }

    public static final Parcelable.Creator<Settings> CREATOR = new Parcelable.Creator<Settings>() {

        @Override
        public Settings createFromParcel(Parcel source) {
            Settings instance = new Settings();
            instance.readFromParcel(source);
            return instance;
        }

        @Override
        public Settings[] newArray(int size) {
            return new Settings[size];
        }

    };

}
