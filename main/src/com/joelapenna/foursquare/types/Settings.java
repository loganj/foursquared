/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import com.joelapenna.foursquare.util.ParcelUtils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2010-01-25 20:40:14.399949
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com), implemented Parcelable.
 */
public class Settings implements FoursquareType, Parcelable {

    private String mFeedsKey;
    private String mGetPings;
    private String mPings;
    private boolean mSendtofacebook;
    private boolean mSendtotwitter;

    public Settings() {
    }
    
    private Settings(Parcel in) {
        mFeedsKey = ParcelUtils.readStringFromParcel(in);
        mGetPings = ParcelUtils.readStringFromParcel(in);
        mPings = ParcelUtils.readStringFromParcel(in);
        mSendtofacebook = in.readInt() == 1;
        mSendtotwitter = in.readInt() == 1;
    }
    
    public static final Parcelable.Creator<Settings> CREATOR = new Parcelable.Creator<Settings>() {
        public Settings createFromParcel(Parcel in) {
            return new Settings(in);
        }

        @Override
        public Settings[] newArray(int size) {
            return new Settings[size];
        }
    };

    public String getFeedsKey() {
        return mFeedsKey;
    }

    public void setFeedsKey(String feedsKey) {
        mFeedsKey = feedsKey;
    }
    
    public String getGetPings() {
        return mGetPings;
    }

    public void setGetPings(String getPings) {
        mGetPings = getPings;
    }

    public String getPings() {
        return mPings;
    }

    public void setPings(String pings) {
        mPings = pings;
    }

    public boolean sendtofacebook() {
        return mSendtofacebook;
    }

    public void setSendtofacebook(boolean sendtofacebook) {
        mSendtofacebook = sendtofacebook;
    }

    public boolean sendtotwitter() {
        return mSendtotwitter;
    }

    public void setSendtotwitter(boolean sendtotwitter) {
        mSendtotwitter = sendtotwitter;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        ParcelUtils.writeStringToParcel(out, mFeedsKey);
        ParcelUtils.writeStringToParcel(out, mGetPings);
        ParcelUtils.writeStringToParcel(out, mPings);
        out.writeInt(mSendtofacebook ? 1 : 0);
        out.writeInt(mSendtotwitter ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }
} 