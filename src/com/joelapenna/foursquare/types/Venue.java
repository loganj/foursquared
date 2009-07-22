/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Venue implements FoursquareType, Parcelable {

    private String mAddress;
    private String mBeenhereFriends;
    private String mBeenhereMe;
    private String mCity;
    private String mCrossstreet;
    private String mDistance;
    private String mExtra;
    private String mGeolat;
    private String mGeolong;
    private String mHere;
    private String mMap;
    private String mMapurl;
    private boolean mNewVenue;
    private String mNumCheckins;
    private String mState;
    private String mVenueid;
    private String mVenuename;
    private String mYelp;
    private String mZip;

    public Venue() {
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getBeenhereFriends() {
        return mBeenhereFriends;
    }

    public void setBeenhereFriends(String beenhereFriends) {
        mBeenhereFriends = beenhereFriends;
    }

    public String getBeenhereMe() {
        return mBeenhereMe;
    }

    public void setBeenhereMe(String beenhereMe) {
        mBeenhereMe = beenhereMe;
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

    public String getExtra() {
        return mExtra;
    }

    public void setExtra(String extra) {
        mExtra = extra;
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

    public String getHere() {
        return mHere;
    }

    public void setHere(String here) {
        mHere = here;
    }

    public String getMap() {
        return mMap;
    }

    public void setMap(String map) {
        mMap = map;
    }

    public String getMapurl() {
        return mMapurl;
    }

    public void setMapurl(String mapurl) {
        mMapurl = mapurl;
    }

    public boolean newVenue() {
        return mNewVenue;
    }

    public void setNewVenue(boolean newVenue) {
        mNewVenue = newVenue;
    }

    public String getNumCheckins() {
        return mNumCheckins;
    }

    public void setNumCheckins(String numCheckins) {
        mNumCheckins = numCheckins;
    }

    public String getState() {
        return mState;
    }

    public void setState(String state) {
        mState = state;
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

    public String getYelp() {
        return mYelp;
    }

    public void setYelp(String yelp) {
        mYelp = yelp;
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
            mNewVenue,
        };
        dest.writeBooleanArray(booleanArray);
        dest.writeString(this.mAddress);
        dest.writeString(this.mBeenhereFriends);
        dest.writeString(this.mBeenhereMe);
        dest.writeString(this.mCity);
        dest.writeString(this.mCrossstreet);
        dest.writeString(this.mDistance);
        dest.writeString(this.mExtra);
        dest.writeString(this.mGeolat);
        dest.writeString(this.mGeolong);
        dest.writeString(this.mHere);
        dest.writeString(this.mMap);
        dest.writeString(this.mMapurl);
        dest.writeString(this.mNumCheckins);
        dest.writeString(this.mState);
        dest.writeString(this.mVenueid);
        dest.writeString(this.mVenuename);
        dest.writeString(this.mYelp);
        dest.writeString(this.mZip);
    }

    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[1];
        source.readBooleanArray(booleanArray);
        this.mNewVenue = booleanArray[0];
        this.mAddress = source.readString();
        this.mBeenhereFriends = source.readString();
        this.mBeenhereMe = source.readString();
        this.mCity = source.readString();
        this.mCrossstreet = source.readString();
        this.mDistance = source.readString();
        this.mExtra = source.readString();
        this.mGeolat = source.readString();
        this.mGeolong = source.readString();
        this.mHere = source.readString();
        this.mMap = source.readString();
        this.mMapurl = source.readString();
        this.mNumCheckins = source.readString();
        this.mState = source.readString();
        this.mVenueid = source.readString();
        this.mVenuename = source.readString();
        this.mYelp = source.readString();
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
