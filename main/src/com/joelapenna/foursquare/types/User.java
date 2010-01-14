/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

/**
 * Auto-generated: 2010-01-14 11:02:51.892579
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class User implements FoursquareType {

    private Group mBadges;
    private Checkin mCheckin;
    private String mCreated;
    private String mEmail;
    private String mFirstname;
    private String mFriendstatus;
    private String mGender;
    private String mId;
    private String mLastname;
    private String mPhone;
    private String mPhoto;
    private Settings mSettings;
    private String mTwitter;

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

    public String getCreated() {
        return mCreated;
    }

    public void setCreated(String created) {
        mCreated = created;
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

    public String getFriendstatus() {
        return mFriendstatus;
    }

    public void setFriendstatus(String friendstatus) {
        mFriendstatus = friendstatus;
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

    public Settings getSettings() {
        return mSettings;
    }

    public void setSettings(Settings settings) {
        mSettings = settings;
    }

    public String getTwitter() {
        return mTwitter;
    }

    public void setTwitter(String twitter) {
        mTwitter = twitter;
    }

}
