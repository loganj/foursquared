/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

/**
 * Auto-generated: 2010-01-25 20:40:14.399949
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Settings implements FoursquareType {

    private String mFeedsKey;
    private String mPings;
    private boolean mSendtofacebook;
    private boolean mSendtotwitter;

    public Settings() {
    }

    public String getFeedsKey() {
        return mFeedsKey;
    }

    public void setFeedsKey(String feedsKey) {
        mFeedsKey = feedsKey;
    }

    public String getPings() {
        return mPings;
    }

    public void setPings(String pings) {
        mPings = pings;
    }

    public boolean sendtofacebook() {
        return mSendtofacebook;
    }

    public void setSendtofacebook(boolean sendtofacebook) {
        mSendtofacebook = sendtofacebook;
    }

    public boolean sendtotwitter() {
        return mSendtotwitter;
    }

    public void setSendtotwitter(boolean sendtotwitter) {
        mSendtotwitter = sendtotwitter;
    }

}
