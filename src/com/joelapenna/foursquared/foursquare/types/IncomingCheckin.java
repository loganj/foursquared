/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare.types;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class IncomingCheckin implements FoursquareType {

    private String mCheckinId;
    private String mMessage;
    private String mUrl;
    private String mUserId;
    private boolean mSuccessful;

    public IncomingCheckin() {
    }

    /**
     * @param Id the Id to set
     */
    public void setId(String mCheckinId) {
        this.mCheckinId = mCheckinId;
    }

    /**
     * @return the Id
     */
    public String getId() {
        return mCheckinId;
    }

    /**
     * @param Message the Message to set
     */
    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    /**
     * @return the Message
     */
    public String getMessage() {
        return mMessage;
    }

    /**
     * @param Id the UserId to set
     */
    public void setUserId(String mUserId) {
        this.mCheckinId = mUserId;
    }

    /**
     * @return the UserId
     */
    public String getUserId() {
        return mUserId;
    }

    /**
     * @param Id the Url to set
     */
    public void setUrl(String mUrl) {
        this.mCheckinId = mUrl;
    }

    /**
     * @return the Url
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * @return Successful
     */
    public boolean successful() {
        return mSuccessful;
    }

    /**
     * @param Successful the mSuccessful to set
     */
    public void setSuccessful(boolean successful) {
        this.mSuccessful = successful;
    }

}
