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

    private Group<Badge> mBadges;
    private String mCreated;
    private String mId;
    private String mMarkup;
    private Mayor mMayor;
    private String mMessage;
    private Group<Score> mScoring;
    private Group<Special> mSpecials;
    private Venue mVenue;

    public CheckinResult() {
    }

    public Group<Badge> getBadges() {
        return mBadges;
    }

    public void setBadges(Group<Badge> badges) {
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

    public String getMarkup() {
        return mMarkup;
    }
    
    public void setMarkup(String markup) {
        mMarkup = markup;
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

    public Group<Score> getScoring() {
        return mScoring;
    }

    public void setScoring(Group<Score> scoring) {
        mScoring = scoring;
    }

    public Group<Special> getSpecials() {
        return mSpecials;
    }

    public void setSpecials(Group<Special> specials) {
        mSpecials = specials;
    }

    public Venue getVenue() {
        return mVenue;
    }

    public void setVenue(Venue venue) {
        mVenue = venue;
    }
}
