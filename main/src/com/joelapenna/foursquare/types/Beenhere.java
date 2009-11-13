/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

/**
 * Auto-generated: 2009-11-12 21:45:35.815975
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
