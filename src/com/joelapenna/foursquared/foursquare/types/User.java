/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare.types;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class User {

    private String mId;
    private String mFirstName;
    private String mLastName;
    private String mCityShortName;
    private int mCityId;
    private Gender mGender;
    private boolean mSendTwitter;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    /**
     * @param Id the mId to set
     */
    public void setId(String mId) {
        this.mId = mId;
    }

    /**
     * @return theId
     */
    public String getId() {
        return mId;
    }

    /**
     * @param FirstName the mFirstName to set
     */
    public void setFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    /**
     * @return theFirstName
     */
    public String getFirstName() {
        return mFirstName;
    }

    /**
     * @param LastName the mLastName to set
     */
    public void setLastName(String mLastName) {
        this.mLastName = mLastName;
    }

    /**
     * @return theLastName
     */
    public String getLastName() {
        return mLastName;
    }

    /**
     * @param CityShortName the mCityShortName to set
     */
    public void setCityShortName(String mCityShortName) {
        this.mCityShortName = mCityShortName;
    }

    /**
     * @return theCityShortName
     */
    public String getCityShortName() {
        return mCityShortName;
    }

    /**
     * @param CityId the mCityId to set
     */
    public void setCityId(int mCityId) {
        this.mCityId = mCityId;
    }

    /**
     * @return theCityId
     */
    public int getCityId() {
        return mCityId;
    }

    /**
     * @param Gender the mGender to set
     */
    public void setGender(Gender mGender) {
        this.mGender = mGender;
    }

    /**
     * @return theGender
     */
    public Gender getGender() {
        return mGender;
    }

    /**
     * @param SendTwitter the mSendTwitter to set
     */
    public void setSendTwitter(boolean mSendTwitter) {
        this.mSendTwitter = mSendTwitter;
    }

    /**
     * @return theSendTwitter
     */
    public boolean isSendTwitter() {
        return mSendTwitter;
    }
}
