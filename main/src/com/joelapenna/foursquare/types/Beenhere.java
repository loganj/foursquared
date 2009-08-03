/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-08-01 10:38:31.736218
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Beenhere implements Parcelable, FoursquareType {

    private boolean mFriends;
    private boolean mMe;

    public Beenhere() {
    }

    public boolean friends() {
        return mFriends;
    }

    public void setFriends(boolean friends) {
        mFriends = friends;
    }

    public boolean me() {
        return mMe;
    }

    public void setMe(boolean me) {
        mMe = me;
    }

    /* For Parcelable */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean[] booleanArray = {
                mFriends, mMe,
        };
        dest.writeBooleanArray(booleanArray);

    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[2];
        source.readBooleanArray(booleanArray);
        this.mFriends = booleanArray[0];
        this.mMe = booleanArray[1];
    }

    public static final Parcelable.Creator<Beenhere> CREATOR = new Parcelable.Creator<Beenhere>() {

        @Override
        public Beenhere createFromParcel(Parcel source) {
            Beenhere instance = new Beenhere();
            instance.readFromParcel(source);
            return instance;
        }

        @Override
        public Beenhere[] newArray(int size) {
            return new Beenhere[size];
        }

    };

}
