/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

/**
 * Auto-generated: 2009-11-22 20:21:34.324313
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Checkin implements FoursquareType {

    private String mCreated;
    private String mDisplay;
    private String mId;
    private boolean mIsmayor;
    private String mShout;
    private User mUser;
    private Venue mVenue;

    public Checkin() {
    }

    public String getCreated() {
        return mCreated;
    }

    public void setCreated(String created) {
        mCreated = created;
    }

    public String getDisplay() {
        return mDisplay;
    }

    public void setDisplay(String display) {
        mDisplay = display;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public boolean ismayor() {
        return mIsmayor;
    }

    public void setIsmayor(boolean ismayor) {
        mIsmayor = ismayor;
    }

    public String getShout() {
        return mShout;
    }

    public void setShout(String shout) {
        mShout = shout;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public Venue getVenue() {
        return mVenue;
    }

    public void setVenue(Venue venue) {
        mVenue = venue;
    }

}
