/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-07-25 12:30:19.140729
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class User implements Parcelable, FoursquareType {

    private Group mBadges;
    private Checkin mCheckin;
    private City mCity;
    private String mFirstname;
    private String mGender;
    private String mId;
    private String mLastname;
    private String mPhoto;
    private Settings mSettings;
    
    public User() {
    }
    
    public Group getBadges() {
        return mBadges;
    }
    
    public void setBadges(Group badges) {
        mBadges = badges;
    }
    
    public Checkin getCheckin() {
        return mCheckin;
    }
    
    public void setCheckin(Checkin checkin) {
        mCheckin = checkin;
    }
    
    public City getCity() {
        return mCity;
    }
    
    public void setCity(City city) {
        mCity = city;
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
    
    public Settings getSettings() {
        return mSettings;
    }
    
    public void setSettings(Settings settings) {
        mSettings = settings;
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
        dest.writeParcelable(this.mCheckin, 0);    
        dest.writeParcelable(this.mCity, 0);    
        dest.writeString(this.mFirstname);    
        dest.writeString(this.mGender);    
        dest.writeString(this.mId);    
        dest.writeString(this.mLastname);    
        dest.writeString(this.mPhoto);    
        dest.writeParcelable(this.mSettings, 0);
    }
    
    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[0];
        source.readBooleanArray(booleanArray);
        this.mBadges = (Group)source.readParcelable(null);    
        this.mCheckin = source.readParcelable(null);    
        this.mCity = source.readParcelable(null);    
        this.mFirstname = source.readString();    
        this.mGender = source.readString();    
        this.mId = source.readString();    
        this.mLastname = source.readString();    
        this.mPhoto = source.readString();    
        this.mSettings = source.readParcelable(null);
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
