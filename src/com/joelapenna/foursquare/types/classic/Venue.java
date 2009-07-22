/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types.classic;

import android.os.Parcel;
import android.os.Parcelable;
import com.joelapenna.foursquare.filters.VenueFilterable;
import com.joelapenna.foursquare.types.FoursquareType;

/**
 * Auto-generated: 2009-06-02 23:02:36.903781
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
@Deprecated
public class Venue implements Parcelable, FoursquareType, VenueFilterable {

    private String mAddress;
    private boolean mBeenhereFriends;
    private boolean mBeenhereMe;
    private String mCity;
    private String mCrossstreet;
    private String mDistance;
    private String mExtra;
    private String mGeolat;
    private String mGeolong;
    private String mMap;
    private String mMapurl;
    private boolean mNewVenue;
    private String mNumCheckins;
    private String mState;
    private String mTag;
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
    
    public boolean beenhereFriends() {
        return mBeenhereFriends;
    }
    
    public void setBeenhereFriends(boolean beenhereFriends) {
        mBeenhereFriends = beenhereFriends;
    }
    
    public boolean beenhereMe() {
        return mBeenhereMe;
    }
    
    public void setBeenhereMe(boolean beenhereMe) {
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
    
    public String getTag() {
        return mTag;
    }
    
    public void setTag(String tag) {
        mTag = tag;
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
            mBeenhereFriends,    
        mBeenhereMe,    
        mNewVenue,
        };
        dest.writeBooleanArray(booleanArray);
        dest.writeString(this.mAddress);    
        dest.writeString(this.mCity);    
        dest.writeString(this.mCrossstreet);    
        dest.writeString(this.mDistance);    
        dest.writeString(this.mExtra);    
        dest.writeString(this.mGeolat);    
        dest.writeString(this.mGeolong);    
        dest.writeString(this.mMap);    
        dest.writeString(this.mMapurl);    
        dest.writeString(this.mNumCheckins);    
        dest.writeString(this.mState);    
        dest.writeString(this.mTag);    
        dest.writeString(this.mVenueid);    
        dest.writeString(this.mVenuename);    
        dest.writeString(this.mYelp);    
        dest.writeString(this.mZip);
    }
    
    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[3];
        source.readBooleanArray(booleanArray);
        this.mBeenhereFriends = booleanArray[0];    
        this.mBeenhereMe = booleanArray[1];    
        this.mNewVenue = booleanArray[2];    
        this.mAddress = source.readString();    
        this.mCity = source.readString();    
        this.mCrossstreet = source.readString();    
        this.mDistance = source.readString();    
        this.mExtra = source.readString();    
        this.mGeolat = source.readString();    
        this.mGeolong = source.readString();    
        this.mMap = source.readString();    
        this.mMapurl = source.readString();    
        this.mNumCheckins = source.readString();    
        this.mState = source.readString();    
        this.mTag = source.readString();    
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
