/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-07-26 20:59:19.247028
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Venue implements Parcelable, FoursquareType {

    private String mAddress;
    private String mCity;
    private String mCrossstreet;
    private String mDistance;
    private String mGeolat;
    private String mGeolong;
    private String mId;
    private String mName;
    private String mPhone;
    private String mState;
    private Stats mStats;
    private Group mTips;
    private Group mTodos;
    private String mZip;

    public Venue() {
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String city) {
        mCity = city;
    }

    public String getCrossstreet() {
        return mCrossstreet;
    }

    public void setCrossstreet(String crossstreet) {
        mCrossstreet = crossstreet;
    }

    public String getDistance() {
        return mDistance;
    }

    public void setDistance(String distance) {
        mDistance = distance;
    }

    public String getGeolat() {
        return mGeolat;
    }

    public void setGeolat(String geolat) {
        mGeolat = geolat;
    }

    public String getGeolong() {
        return mGeolong;
    }

    public void setGeolong(String geolong) {
        mGeolong = geolong;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getState() {
        return mState;
    }

    public void setState(String state) {
        mState = state;
    }

    public Stats getStats() {
        return mStats;
    }

    public void setStats(Stats stats) {
        mStats = stats;
    }

    public Group getTips() {
        return mTips;
    }

    public void setTips(Group tips) {
        mTips = tips;
    }

    public Group getTodos() {
        return mTodos;
    }

    public void setTodos(Group todos) {
        mTodos = todos;
    }

    public String getZip() {
        return mZip;
    }

    public void setZip(String zip) {
        mZip = zip;
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
        dest.writeString(this.mAddress);
        dest.writeString(this.mCity);
        dest.writeString(this.mCrossstreet);
        dest.writeString(this.mDistance);
        dest.writeString(this.mGeolat);
        dest.writeString(this.mGeolong);
        dest.writeString(this.mId);
        dest.writeString(this.mName);
        dest.writeString(this.mPhone);
        dest.writeString(this.mState);
        dest.writeParcelable(this.mStats, 0);
        dest.writeParcelable((Parcelable)this.mTips, flags);
        dest.writeParcelable((Parcelable)this.mTodos, flags);
        dest.writeString(this.mZip);
    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[0];
        source.readBooleanArray(booleanArray);
        this.mAddress = source.readString();
        this.mCity = source.readString();
        this.mCrossstreet = source.readString();
        this.mDistance = source.readString();
        this.mGeolat = source.readString();
        this.mGeolong = source.readString();
        this.mId = source.readString();
        this.mName = source.readString();
        this.mPhone = source.readString();
        this.mState = source.readString();
        this.mStats = source.readParcelable(null);
        this.mTips = (Group)source.readParcelable(null);
        this.mTodos = (Group)source.readParcelable(null);
        this.mZip = source.readString();
    }

    public static final Parcelable.Creator<Venue> CREATOR = new Parcelable.Creator<Venue>() {

        @Override
        public Venue createFromParcel(Parcel source) {
            Venue instance = new Venue();
            instance.readFromParcel(source);
            return instance;
        }

        @Override
        public Venue[] newArray(int size) {
            return new Venue[size];
        }

    };

}
