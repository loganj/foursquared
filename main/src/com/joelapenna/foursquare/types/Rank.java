/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-08-01 10:38:32.725953
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Rank implements Parcelable, FoursquareType {

    private String mCity;
    private String mMessage;
    private String mPosition;

    public Rank() {
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String city) {
        mCity = city;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getPosition() {
        return mPosition;
    }

    public void setPosition(String position) {
        mPosition = position;
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
        dest.writeString(this.mCity);
        dest.writeString(this.mMessage);
        dest.writeString(this.mPosition);
    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[0];
        source.readBooleanArray(booleanArray);
        this.mCity = source.readString();
        this.mMessage = source.readString();
        this.mPosition = source.readString();
    }

    public static final Parcelable.Creator<Rank> CREATOR = new Parcelable.Creator<Rank>() {

        @Override
        public Rank createFromParcel(Parcel source) {
            Rank instance = new Rank();
            instance.readFromParcel(source);
            return instance;
        }

        @Override
        public Rank[] newArray(int size) {
            return new Rank[size];
        }

    };

}
