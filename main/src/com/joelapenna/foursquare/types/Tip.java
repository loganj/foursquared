/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-08-01 10:38:33.448769
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Tip implements Parcelable, FoursquareType {

    private String mCreated;
    private String mDistance;
    private String mId;
    private String mText;
    private User mUser;
    private Venue mVenue;

    public Tip() {
    }

    public String getCreated() {
        return mCreated;
    }

    public void setCreated(String created) {
        mCreated = created;
    }

    public String getDistance() {
        return mDistance;
    }

    public void setDistance(String distance) {
        mDistance = distance;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
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
        dest.writeString(this.mDistance);
        dest.writeString(this.mId);
        dest.writeString(this.mText);
        dest.writeParcelable(this.mUser, 0);
        dest.writeParcelable(this.mVenue, 0);
    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[0];
        source.readBooleanArray(booleanArray);
        this.mCreated = source.readString();
        this.mDistance = source.readString();
        this.mId = source.readString();
        this.mText = source.readString();
        this.mUser = source.readParcelable(null);
        this.mVenue = source.readParcelable(null);
    }

    public static final Parcelable.Creator<Tip> CREATOR = new Parcelable.Creator<Tip>() {

        @Override
        public Tip createFromParcel(Parcel source) {
            Tip instance = new Tip();
            instance.readFromParcel(source);
            return instance;
        }

        @Override
        public Tip[] newArray(int size) {
            return new Tip[size];
        }

    };

}
