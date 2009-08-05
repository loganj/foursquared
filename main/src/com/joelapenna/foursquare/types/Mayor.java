/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

;

/**
 * Auto-generated: 2009-08-05 21:30:25.359802
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Mayor implements FoursquareType {

    private String mCheckins;
    private String mCount;
    private String mMessage;
    private String mType;
    private User mUser;

    public Mayor() {
    }

    public String getCheckins() {
        return mCheckins;
    }

    public void setCheckins(String checkins) {
        mCheckins = checkins;
    }

    public String getCount() {
        return mCount;
    }

    public void setCount(String count) {
        mCount = count;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

}
