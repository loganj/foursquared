/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

/**
 * Auto-generated: 2009-11-12 21:45:35.920477
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Data implements FoursquareType {

    private String mCityid;
    private String mMessage;
    private boolean mStatus;

    public Data() {
    }

    public String getCityid() {
        return mCityid;
    }

    public void setCityid(String cityid) {
        mCityid = cityid;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public boolean status() {
        return mStatus;
    }

    public void setStatus(boolean status) {
        mStatus = status;
    }

}
