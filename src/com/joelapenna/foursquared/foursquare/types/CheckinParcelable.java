/**
 * Copyright 2009 Joe LaPenna
 */
package com.joelapenna.foursquared.foursquare.types;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 *
 */
public class CheckinParcelable {

    private String mId;
    private int mVenueId;
    private int mAliasId;
    private String mVenueName;
    private String mAddress;
    private String mCrossStreet;
    private String mShout;
    private String mGeoLat;
    private String mGeoLong;
    private String mXDateTime;
    private String mRelativeTime;
    /**
     * @param Id the Id to set
     */
    public void setId(String mId) {
        this.mId = mId;
    }
    /**
     * @return the Id
     */
    public String getId() {
        return mId;
    }
    /**
     * @param VenueId the VenueId to set
     */
    public void setVenueId(int mVenueId) {
        this.mVenueId = mVenueId;
    }
    /**
     * @return the VenueId
     */
    public int getVenueId() {
        return mVenueId;
    }
    /**
     * @param VenueName the VenueName to set
     */
    public void setVenueName(String mVenueName) {
        this.mVenueName = mVenueName;
    }
    /**
     * @return the VenueName
     */
    public String getVenueName() {
        return mVenueName;
    }
    /**
     * @param Address the Address to set
     */
    public void setAddress(String mAddress) {
        this.mAddress = mAddress;
    }
    /**
     * @return the Address
     */
    public String getAddress() {
        return mAddress;
    }
    /**
     * @param CrossStreet the CrossStreet to set
     */
    public void setCrossStreet(String mCrossStreet) {
        this.mCrossStreet = mCrossStreet;
    }
    /**
     * @return the CrossStreet
     */
    public String getCrossStreet() {
        return mCrossStreet;
    }
    /**
     * @param Shout the Shout to set
     */
    public void setShout(String mShout) {
        this.mShout = mShout;
    }
    /**
     * @return the Shout
     */
    public String getShout() {
        return mShout;
    }
    /**
     * @param GeoLat the GeoLat to set
     */
    public void setGeoLat(String mGeoLat) {
        this.mGeoLat = mGeoLat;
    }
    /**
     * @return the GeoLat
     */
    public String getGeoLat() {
        return mGeoLat;
    }
    /**
     * @param GeoLong the GeoLong to set
     */
    public void setGeoLong(String mGeoLong) {
        this.mGeoLong = mGeoLong;
    }
    /**
     * @return the GeoLong
     */
    public String getGeoLong() {
        return mGeoLong;
    }
    /**
     * @param XDateTime the XDateTime to set
     */
    public void setXDateTime(String mXDateTime) {
        this.mXDateTime = mXDateTime;
    }
    /**
     * @return the XDateTime
     */
    public String getXDateTime() {
        return mXDateTime;
    }
    /**
     * @param RelativeTime the RelativeTime to set
     */
    public void setRelativeTime(String mRelativeTime) {
        this.mRelativeTime = mRelativeTime;
    }
    /**
     * @return the RelativeTime
     */
    public String getRelativeTime() {
        return mRelativeTime;
    }
    /**
     * @param mAliasId the mAliasId to set
     */
    public void setAliasId(int mAliasId) {
        this.mAliasId = mAliasId;
    }
    /**
     * @return the mAliasId
     */
    public int getAliasId() {
        return mAliasId;
    }

}
