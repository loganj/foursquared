/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-06-10 00:51:03.020803
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Credentials implements Parcelable, FoursquareType {

    private String mOauthToken;
    private String mOauthTokenSecret;
    
    public Credentials() {
    }
    
    public String getOauthToken() {
        return mOauthToken;
    }
    
    public void setOauthToken(String oauthToken) {
        mOauthToken = oauthToken;
    }
    
    public String getOauthTokenSecret() {
        return mOauthTokenSecret;
    }
    
    public void setOauthTokenSecret(String oauthTokenSecret) {
        mOauthTokenSecret = oauthTokenSecret;
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
        dest.writeString(this.mOauthToken);    
        dest.writeString(this.mOauthTokenSecret);
    }
    
    private void readFromParcel(Parcel source) {
        boolean[] booleanArray = new boolean[0];
        source.readBooleanArray(booleanArray);
        this.mOauthToken = source.readString();    
        this.mOauthTokenSecret = source.readString();
    }
    
    public static final Parcelable.Creator<Credentials> CREATOR = new Parcelable.Creator<Credentials>() {
    
        @Override
        public Credentials createFromParcel(Parcel source) {
            Credentials instance = new Credentials();
            instance.readFromParcel(source);
            return instance;
        }
    
        @Override
        public Credentials[] newArray(int size) {
            return new Credentials[size];
        }
    
    };
    
}
