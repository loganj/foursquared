/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-11-12 21:45:35.815975
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com), implemented Parcelable.
 */
public class Beenhere implements FoursquareType, Parcelable {

    private boolean mFriends;
    private boolean mMe;

    public Beenhere() {
    }
    
    private Beenhere(Parcel in) {
        mFriends = in.readInt() == 1;
        mMe = in.readInt() == 1; 
    }
    
    public static final Beenhere.Creator<Beenhere> CREATOR = new Parcelable.Creator<Beenhere>() {
        public Beenhere createFromParcel(Parcel in) {
            return new Beenhere(in);
        }

        @Override
        public Beenhere[] newArray(int size) {
            return new Beenhere[size];
        }
    };

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
    
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mFriends ? 1 : 0);
        out.writeInt(mMe ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
