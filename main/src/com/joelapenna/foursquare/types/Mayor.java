/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import com.joelapenna.foursquare.util.ParcelUtils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-11-12 21:45:35.140783
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com), implemented Parcelable.
 */
public class Mayor implements FoursquareType, Parcelable {

    private String mCheckins;
    private String mCount;
    private String mMessage;
    private String mType;
    private User mUser;

    public Mayor() {
    }
    
    private Mayor(Parcel in) {
        mCheckins = ParcelUtils.readStringFromParcel(in);
        mCount = ParcelUtils.readStringFromParcel(in);
        mMessage = ParcelUtils.readStringFromParcel(in);
        mType = ParcelUtils.readStringFromParcel(in);
        
        if (in.readInt() == 1) {
            mUser = in.readParcelable(User.class.getClassLoader());
        }
    }
    
    public static final Parcelable.Creator<Mayor> CREATOR = new Parcelable.Creator<Mayor>() {
        public Mayor createFromParcel(Parcel in) {
            return new Mayor(in);
        }

        @Override
        public Mayor[] newArray(int size) {
            return new Mayor[size];
        }
    };

    public String getCheckins() {
        return mCheckins;
    }

    public void setCheckins(String checkins) {
        mCheckins = checkins;
    }

    public String getCount() {
        return mCount;
    }

    public void setCount(String count) {
        mCount = count;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        ParcelUtils.writeStringToParcel(out, mCheckins);
        ParcelUtils.writeStringToParcel(out, mCount);
        ParcelUtils.writeStringToParcel(out, mMessage);
        ParcelUtils.writeStringToParcel(out, mType);
        
        if (mUser != null) {
            out.writeInt(1);
            out.writeParcelable(mUser, flags);
        } else {
            out.writeInt(0);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
