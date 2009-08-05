/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

;

/**
 * Auto-generated: 2009-08-05 21:30:26.311084
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class User implements FoursquareType {

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

}
