/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Tip implements FoursquareType, Parcelable {

    private String mAddress;
    private String mCreatorStatus;
    private String mCrossstreet;
    private String mDate;
    private String mDistance;
    private String mFirstname;
    private boolean mIstodoable;
    private String mLastname;
    private String mPhoto;
    private String mRelativeDate;
    private String mStatusText;
    private String mText;
    private String mTipid;
    private String mUrl;
    private String mUserStatus;
    private String mUserid;
    private String mVenueid;
    private String mVenuename;

    public Tip() {
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getCreatorStatus() {
        return mCreatorStatus;
    }

    public void setCreatorStatus(String creatorStatus) {
        mCreatorStatus = creatorStatus;
    }

    public String getCrossstreet() {
        return mCrossstreet;
    }

    public void setCrossstreet(String crossstreet) {
        mCrossstreet = crossstreet;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getDistance() {
        return mDistance;
    }

    public void setDistance(String distance) {
        mDistance = distance;
    }

    public String getFirstname() {
        return mFirstname;
    }

    public void setFirstname(String firstname) {
        mFirstname = firstname;
    }

    public boolean istodoable() {
        return mIstodoable;
    }

    public void setIstodoable(boolean istodoable) {
        mIstodoable = istodoable;
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

    public String getRelativeDate() {
        return mRelativeDate;
    }

    public void setRelativeDate(String relativeDate) {
        mRelativeDate = relativeDate;
    }

    public String getStatusText() {
        return mStatusText;
    }

    public void setStatusText(String statusText) {
        mStatusText = statusText;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public String getTipid() {
        return mTipid;
    }

    public void setTipid(String tipid) {
        mTipid = tipid;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUserStatus() {
        return mUserStatus;
    }

    public void setUserStatus(String userStatus) {
        mUserStatus = userStatus;
    }

    public String getUserid() {
        return mUserid;
    }

    public void setUserid(String userid) {
        mUserid = userid;
    }

    public String getVenueid() {
        return mVenueid;
    }

    public void setVenueid(String venueid) {
        mVenueid = venueid;
    }

    public String getVenuename() {
        return mVenuename;
    }

    public void setVenuename(String venuename) {
        mVenuename = venuename;
    }

    /* For Parcelable */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean[] booleanArray = {
            mIstodoable,
        };
        dest.writeBooleanArray(booleanArray);
        dest.writeString(this.mAddress);
        dest.writeString(this.mCreatorStatus);
        dest.writeString(this.mCrossstreet);
        dest.writeString(this.mDate);
        dest.writeString(this.mDistance);
        dest.writeString(this.mFirstname);
        dest.writeString(this.mLastname);
        dest.writeString(this.mPhoto);
        dest.writeString(this.mRelativeDate);
        dest.writeString(this.mStatusText);
        dest.writeString(this.mText);
        dest.writeString(this.mTipid);
        dest.writeString(this.mUrl);
        dest.writeString(this.mUserStatus);
        dest.writeString(this.mUserid);
        dest.writeString(this.mVenueid);
        dest.writeString(this.mVenuename);
    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[1];
        source.readBooleanArray(booleanArray);
        this.mIstodoable = booleanArray[0];
        this.mAddress = source.readString();
        this.mCreatorStatus = source.readString();
        this.mCrossstreet = source.readString();
        this.mDate = source.readString();
        this.mDistance = source.readString();
        this.mFirstname = source.readString();
        this.mLastname = source.readString();
        this.mPhoto = source.readString();
        this.mRelativeDate = source.readString();
        this.mStatusText = source.readString();
        this.mText = source.readString();
        this.mTipid = source.readString();
        this.mUrl = source.readString();
        this.mUserStatus = source.readString();
        this.mUserid = source.readString();
        this.mVenueid = source.readString();
        this.mVenuename = source.readString();
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
