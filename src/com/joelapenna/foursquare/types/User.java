/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;
import com.joelapenna.foursquare.types.Group;

/**
 * Auto-generated: 2009-05-28 10:32:35.869115
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class User implements Parcelable, FoursquareType {

    private Group mBadges;
    private String mCityid;
    private String mCityshortname;
    private String mFirstname;
    private String mGender;
    private String mId;
    private String mLastname;
    private String mPhoto;
    private boolean mSendtwitter;

    public User() {
    }

    public Group getBadges() {
        return mBadges;
    }

    public void setBadges(Group badges) {
        mBadges = badges;
    }

    public String getCityid() {
        return mCityid;
    }

    public void setCityid(String cityid) {
        mCityid = cityid;
    }

    public String getCityshortname() {
        return mCityshortname;
    }

    public void setCityshortname(String cityshortname) {
        mCityshortname = cityshortname;
    }

    public String getFirstname() {
        return mFirstname;
    }

    public void setFirstname(String firstname) {
        mFirstname = firstname;
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String gender) {
        mGender = gender;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getLastname() {
        return mLastname;
    }

    public void setLastname(String lastname) {
        mLastname = lastname;
    }

    public String getPhoto() {
        return mPhoto;
    }

    public void setPhoto(String photo) {
        mPhoto = photo;
    }

    public boolean sendtwitter() {
        return mSendtwitter;
    }

    public void setSendtwitter(boolean sendtwitter) {
        mSendtwitter = sendtwitter;
    }

    /* For Parcelable */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean[] booleanArray = {
            mSendtwitter,
        };
        dest.writeBooleanArray(booleanArray);
        dest.writeParcelable((Parcelable)this.mBadges, flags);
        dest.writeString(this.mCityid);
        dest.writeString(this.mCityshortname);
        dest.writeString(this.mFirstname);
        dest.writeString(this.mGender);
        dest.writeString(this.mId);
        dest.writeString(this.mLastname);
        dest.writeString(this.mPhoto);
    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[1];
        source.readBooleanArray(booleanArray);
        this.mSendtwitter = booleanArray[0];
        this.mBadges = (Group)source.readParcelable(null);
        this.mCityid = source.readString();
        this.mCityshortname = source.readString();
        this.mFirstname = source.readString();
        this.mGender = source.readString();
        this.mId = source.readString();
        this.mLastname = source.readString();
        this.mPhoto = source.readString();
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

        @Override
        public User createFromParcel(Parcel source) {
            User instance = new User();
            instance.readFromParcel(source);
            return instance;
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }

    };

}
