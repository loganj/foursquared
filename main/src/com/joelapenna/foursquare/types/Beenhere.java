/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

;

/**
 * Auto-generated: 2009-08-05 21:30:24.564289
 * 
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Beenhere implements FoursquareType {

    private boolean mFriends;
    private boolean mMe;

    public Beenhere() {
    }

    public boolean friends() {
        return mFriends;
    }

    public void setFriends(boolean friends) {
        mFriends = friends;
    }

    public boolean me() {
        return mMe;
    }

    public void setMe(boolean me) {
        mMe = me;
    }

}
