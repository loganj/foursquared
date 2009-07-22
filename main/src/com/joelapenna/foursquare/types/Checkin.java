/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-06-19 00:18:41.114104
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Checkin implements Parcelable, FoursquareType {

    private String mCreated;
    private String mId;
    private String mShout;
    private User mUser;
    private Venue mVenue;

    public Checkin() {
    }

    public String getCreated() {
        return mCreated;
    }

    public void setCreated(String created) {
        mCreated = created;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getShout() {
        return mShout;
    }

    public void setShout(String shout) {
        mShout = shout;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public Venue getVenue() {
        return mVenue;
    }

    public void setVenue(Venue venue) {
        mVenue = venue;
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
        dest.writeString(this.mCreated);
        dest.writeString(this.mId);
        dest.writeString(this.mShout);
        dest.writeParcelable(this.mUser, 0);
        dest.writeParcelable(this.mVenue, 0);
    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[0];
        source.readBooleanArray(booleanArray);
        this.mCreated = source.readString();
        this.mId = source.readString();
        this.mShout = source.readString();
        this.mUser = source.readParcelable(null);
        this.mVenue = source.readParcelable(null);
    }

    public static final Parcelable.Creator<Checkin> CREATOR = new Parcelable.Creator<Checkin>() {

        @Override
        public Checkin createFromParcel(Parcel source) {
            Checkin instance = new Checkin();
            instance.readFromParcel(source);
            return instance;
        }

        @Override
        public Checkin[] newArray(int size) {
            return new Checkin[size];
        }

    };

}
