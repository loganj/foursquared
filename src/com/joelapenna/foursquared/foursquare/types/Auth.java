/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare.types;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Auth extends FoursquareType {

    private boolean mStatus;
    private String mMessage;
    private String mId;
    private String mFirstname;
    private String mLastname;
    private String mEmail;
    private String mPhone;
    private String mPhoto;

    public Auth() {
    }

    /**
     * @param mStatus the mStatus to set
     */
    public void setSuccessful(boolean status) {
        this.mStatus = status;
    }

    /**
     * @return the mStatus
     */
    public boolean isSuccessful() {
        return mStatus;
    }

    /**
     * @param mMesmage the mMesmage to set
     */
    public void setMessage(String message) {
        this.mMessage = message;
    }

    /**
     * @return the mMesmage
     */
    public String getMessage() {
        return mMessage;
    }

    /**
     * @param mId the mId to set
     */
    public void setId(String id) {
        this.mId = id;
    }

    /**
     * @return the mId
     */
    public String getId() {
        return mId;
    }

    /**
     * @param mFirstname the mFirstname to set
     */
    public void setFirstname(String firstname) {
        this.mFirstname = firstname;
    }

    /**
     * @return the mFirstname
     */
    public String getFirstname() {
        return mFirstname;
    }

    /**
     * @param mLastname the mLastname to set
     */
    public void setLastname(String lastname) {
        this.mLastname = lastname;
    }

    /**
     * @return the mLastname
     */
    public String getLastname() {
        return mLastname;
    }

    /**
     * @param mEmail the mEmail to set
     */
    public void setEmail(String email) {
        this.mEmail = email;
    }

    /**
     * @return the mEmail
     */
    public String getEmail() {
        return mEmail;
    }

    /**
     * @param mPhone the mPhone to set
     */
    public void setPhone(String phone) {
        this.mPhone = phone;
    }

    /**
     * @return the mPhone
     */
    public String getPhone() {
        return mPhone;
    }

    /**
     * @param mPhoto the mPhoto to set
     */
    public void setPhoto(String photo) {
        this.mPhoto = photo;
    }

    /**
     * @return the mPhoto
     */
    public String getPhoto() {
        return mPhoto;
    }

}
