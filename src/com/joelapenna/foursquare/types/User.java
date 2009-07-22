/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-06-09 22:40:22.077967
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class User implements Parcelable, FoursquareType {

    private Group mBadges;
    private Checkin mCheckin;
    private City mCity;
    private String mFirstname;
    private String mId;
    private String mLastname;
    
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
        dest.writeString(this.mId);    
        dest.writeString(this.mLastname);
    }
    
    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[0];
        source.readBooleanArray(booleanArray);
        this.mBadges = (Group)source.readParcelable(null);    
        this.mCheckin = source.readParcelable(null);    
        this.mCity = source.readParcelable(null);    
        this.mFirstname = source.readString();    
        this.mId = source.readString();    
        this.mLastname = source.readString();
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
