/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types.classic;

import com.joelapenna.foursquare.types.FoursquareType;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-06-02 23:02:35.553807
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */

public class Auth implements Parcelable, FoursquareType {

    private String mEmail;
    private String mFirstname;
    private String mId;
    private String mLastname;
    private String mMessage;
    private String mPhone;
    private String mPhoto;
    private boolean mStatus;

    public Auth() {
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getFirstname() {
        return mFirstname;
    }

    public void setFirstname(String firstname) {
        mFirstname = firstname;
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

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getPhoto() {
        return mPhoto;
    }

    public void setPhoto(String photo) {
        mPhoto = photo;
    }

    public boolean status() {
        return mStatus;
    }

    public void setStatus(boolean status) {
        mStatus = status;
    }

    /* For Parcelable */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean[] booleanArray = {
            mStatus,
        };
        dest.writeBooleanArray(booleanArray);
        dest.writeString(this.mEmail);
        dest.writeString(this.mFirstname);
        dest.writeString(this.mId);
        dest.writeString(this.mLastname);
        dest.writeString(this.mMessage);
        dest.writeString(this.mPhone);
        dest.writeString(this.mPhoto);
    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[1];
        source.readBooleanArray(booleanArray);
        this.mStatus = booleanArray[0];
        this.mEmail = source.readString();
        this.mFirstname = source.readString();
        this.mId = source.readString();
        this.mLastname = source.readString();
        this.mMessage = source.readString();
        this.mPhone = source.readString();
        this.mPhoto = source.readString();
    }

    public static final Parcelable.Creator<Auth> CREATOR = new Parcelable.Creator<Auth>() {

        @Override
        public Auth createFromParcel(Parcel source) {
            Auth instance = new Auth();
            instance.readFromParcel(source);
            return instance;
        }

        @Override
        public Auth[] newArray(int size) {
            return new Auth[size];
        }

    };

}
