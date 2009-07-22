/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-06-10 02:19:21.823386
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Checkin implements Parcelable, FoursquareType {

    private String mCreated;
    private String mId;
    private String mMessage;
    private String mUser;
    private String mVenue;
    
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
    
    public String getMessage() {
        return mMessage;
    }
    
    public void setMessage(String message) {
        mMessage = message;
    }
    
    public String getUser() {
        return mUser;
    }
    
    public void setUser(String user) {
        mUser = user;
    }
    
    public String getVenue() {
        return mVenue;
    }
    
    public void setVenue(String venue) {
        mVenue = venue;
    }
    
    /* For Parcelable */
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean[] booleanArray = {
        
        };
        dest.writeBooleanArray(booleanArray);
        dest.writeString(this.mCreated);    
        dest.writeString(this.mId);    
        dest.writeString(this.mMessage);    
        dest.writeString(this.mUser);    
        dest.writeString(this.mVenue);
    }
    
    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[0];
        source.readBooleanArray(booleanArray);
        this.mCreated = source.readString();    
        this.mId = source.readString();    
        this.mMessage = source.readString();    
        this.mUser = source.readString();    
        this.mVenue = source.readString();
    }
    
    public static final Parcelable.Creator<Checkin> CREATOR = new Parcelable.Creator<Checkin>() {
    
        @Override
        public Checkin createFromParcel(Parcel source) {
            Checkin instance = new Checkin();
            instance.readFromParcel(source);
            return instance;
        }
    
        @Override
        public Checkin[] newArray(int size) {
            return new Checkin[size];
        }
    
    };
    
}
