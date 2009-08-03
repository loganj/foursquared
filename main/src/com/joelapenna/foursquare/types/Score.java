/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-08-01 10:38:32.863906
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Score implements Parcelable, FoursquareType {

    private String mIcon;
    private String mMessage;
    private String mPoints;

    public Score() {
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getPoints() {
        return mPoints;
    }

    public void setPoints(String points) {
        mPoints = points;
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
        dest.writeString(this.mIcon);
        dest.writeString(this.mMessage);
        dest.writeString(this.mPoints);
    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[0];
        source.readBooleanArray(booleanArray);
        this.mIcon = source.readString();
        this.mMessage = source.readString();
        this.mPoints = source.readString();
    }

    public static final Parcelable.Creator<Score> CREATOR = new Parcelable.Creator<Score>() {

        @Override
        public Score createFromParcel(Parcel source) {
            Score instance = new Score();
            instance.readFromParcel(source);
            return instance;
        }

        @Override
        public Score[] newArray(int size) {
            return new Score[size];
        }

    };

}
