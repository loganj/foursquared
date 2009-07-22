/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare.types;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Venue implements FoursquareType {

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
    
}
