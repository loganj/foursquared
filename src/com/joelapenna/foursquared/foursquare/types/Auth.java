/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare.types;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Auth extends FoursquareType {

    private String mEmail;
    private String mFirstname;
    private String mId;
    private String mLastname;
    private String mMessage;
    private String mPhone;
    private String mPhoto;
    private boolean mStatus;
    
    public Auth() {
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
    
    public String getMessage() {
        return mMessage;
    }
    
    public void setMessage(String message) {
        mMessage = message;
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
    
    public boolean status() {
        return mStatus;
    }
    
    public void setStatus(boolean status) {
        mStatus = status;
    }
    
}
