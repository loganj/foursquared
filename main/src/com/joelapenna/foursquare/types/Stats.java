/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import com.joelapenna.foursquare.util.ParcelUtils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-11-12 21:45:34.803921
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com), implemented Parcelable.
 */
public class Stats implements FoursquareType, Parcelable {

    private Beenhere mBeenhere;
    private String mCheckins;
    private String mHereNow;
    private Mayor mMayor;

    public Stats() {
    }
    
    private Stats(Parcel in) {
        mBeenhere = in.readParcelable(Beenhere.class.getClassLoader());
        mCheckins = ParcelUtils.readStringFromParcel(in);
        mHereNow = ParcelUtils.readStringFromParcel(in);
        mMayor = in.readParcelable(Mayor.class.getClassLoader());
    }
    
    public static final Parcelable.Creator<Stats> CREATOR = new Parcelable.Creator<Stats>() {
        public Stats createFromParcel(Parcel in) {
            return new Stats(in);
        }

        @Override
        public Stats[] newArray(int size) {
            return new Stats[size];
        }
    };

    public Beenhere getBeenhere() {
        return mBeenhere;
    }

    public void setBeenhere(Beenhere beenhere) {
        mBeenhere = beenhere;
    }

    public String getCheckins() {
        return mCheckins;
    }

    public void setCheckins(String checkins) {
        mCheckins = checkins;
    }
    
    public String getHereNow() {
        return mHereNow;
    }

    public void setHereNow(String herenow) {
        mHereNow = herenow;
    }
    
    public Mayor getMayor() {
        return mMayor;
    }

    public void setMayor(Mayor mayor) {
        mMayor = mayor;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(mBeenhere, flags);
        ParcelUtils.writeStringToParcel(out, mCheckins);
        ParcelUtils.writeStringToParcel(out, mHereNow);
        out.writeParcelable(mMayor, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
