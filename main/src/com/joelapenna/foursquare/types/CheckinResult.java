/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-08-01 10:39:25.277407
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class CheckinResult implements Parcelable, FoursquareType {

    private Group mBadges;
    private String mCreated;
    private String mId;
    private Mayor mMayor;
    private String mMessage;
    private Scoring mScoring;
    private Venue mVenue;

    public CheckinResult() {
    }

    public Group getBadges() {
        return mBadges;
    }

    public void setBadges(Group badges) {
        mBadges = badges;
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

    public Mayor getMayor() {
        return mMayor;
    }

    public void setMayor(Mayor mayor) {
        mMayor = mayor;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public Scoring getScoring() {
        return mScoring;
    }

    public void setScoring(Scoring scoring) {
        mScoring = scoring;
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
        dest.writeParcelable((Parcelable)this.mBadges, flags);
        dest.writeString(this.mCreated);
        dest.writeString(this.mId);
        dest.writeParcelable(this.mMayor, 0);
        dest.writeString(this.mMessage);
        dest.writeParcelable(this.mScoring, 0);
        dest.writeParcelable(this.mVenue, 0);
    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[0];
        source.readBooleanArray(booleanArray);
        this.mBadges = (Group)source.readParcelable(null);
        this.mCreated = source.readString();
        this.mId = source.readString();
        this.mMayor = source.readParcelable(null);
        this.mMessage = source.readString();
        this.mScoring = source.readParcelable(null);
        this.mVenue = source.readParcelable(null);
    }

    public static final Parcelable.Creator<CheckinResult> CREATOR = new Parcelable.Creator<CheckinResult>() {

        @Override
        public CheckinResult createFromParcel(Parcel source) {
            CheckinResult instance = new CheckinResult();
            instance.readFromParcel(source);
            return instance;
        }

        @Override
        public CheckinResult[] newArray(int size) {
            return new CheckinResult[size];
        }

    };

}
