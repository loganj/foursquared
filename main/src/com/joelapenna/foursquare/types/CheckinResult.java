/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

/**
 * Auto-generated: 2009-12-06 10:20:15.288167
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class CheckinResult implements FoursquareType {

    private Group mBadges;
    private String mCreated;
    private String mId;
    private Mayor mMayor;
    private String mMessage;
    private Group mScoring;
    private Group mSpecials;
    private Venue mVenue;

    public CheckinResult() {
    }

    public Group getBadges() {
        return mBadges;
    }

    public void setBadges(Group badges) {
        mBadges = badges;
    }

    public String getCreated() {
        return mCreated;
    }

    public void setCreated(String created) {
        mCreated = created;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public Mayor getMayor() {
        return mMayor;
    }

    public void setMayor(Mayor mayor) {
        mMayor = mayor;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public Group getScoring() {
        return mScoring;
    }

    public void setScoring(Group scoring) {
        mScoring = scoring;
    }

    public Group getSpecials() {
        return mSpecials;
    }

    public void setSpecials(Group specials) {
        mSpecials = specials;
    }

    public Venue getVenue() {
        return mVenue;
    }

    public void setVenue(Venue venue) {
        mVenue = venue;
    }

}
