/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

;

/**
 * Auto-generated: 2009-08-05 21:30:24.820586
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Checkin implements FoursquareType {

    private String mCreated;
    private String mId;
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

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
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
