/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-11-22 20:21:34.324313
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com), implemented Parcelable.
 */
public class Checkin implements FoursquareType, Parcelable {

    private String mCreated;
    private String mDisplay;
    private String mId;
    private boolean mIsmayor;
    private String mShout;
    private User mUser;
    private Venue mVenue;

    public Checkin() {
    }
    
    private Checkin(Parcel in) {
        mCreated = in.readString();
        mDisplay = in.readString();
        mId = in.readString();
        mIsmayor = in.readInt() == 1;
        mShout = in.readString();
        mUser= User.CREATOR.createFromParcel(in);
        mVenue= Venue.CREATOR.createFromParcel(in);
    }
    
    public static final Parcelable.Creator<Checkin> CREATOR = new Parcelable.Creator<Checkin>() {
        public Checkin createFromParcel(Parcel in) {
            return new Checkin(in);
        }

        @Override
        public Checkin[] newArray(int size) {
            return new Checkin[size];
        }
    };

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

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mCreated);
        out.writeString(mDisplay);
        out.writeString(mId);
        out.writeInt(mIsmayor ? 1 : 0);
        out.writeString(mShout);
        out.writeParcelable(mUser, flags);
        out.writeParcelable(mVenue, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
