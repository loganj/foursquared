/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2010-01-14 11:02:51.892579
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com), implemented Parcelable.
 */
public class User implements FoursquareType, Parcelable {

    private Group<Badge> mBadges;
    private Checkin mCheckin;
    private String mCreated;
    private String mEmail;
    private String mFacebook;
    private String mFirstname;
    private String mFriendstatus;
    private String mGender;
    private String mId;
    private String mLastname;
    private String mPhone;
    private String mPhoto;
    private Settings mSettings;
    private String mTwitter;
    private Group<Venue> mMayorships;

    public User() {
    }

    private User(Parcel in) {
        mCreated = in.readString();
        mEmail = in.readString();
        mFacebook = in.readString();
        mFirstname = in.readString();
        mFriendstatus = in.readString();
        mGender = in.readString();
        mId = in.readString();
        mLastname = in.readString();
        mPhone = in.readString();
        mPhoto = in.readString();
        mTwitter = in.readString();
        
        mBadges = new Group<Badge>();
        int numBadges = in.readInt();
        for (int i = 0; i < numBadges; i++) {
            Badge badge = in.readParcelable(Badge.class.getClassLoader());
            mBadges.add(badge);
        }
        
        if (in.readInt() == 1) {
            mCheckin = in.readParcelable(Checkin.class.getClassLoader());
        }
        
        if (in.readInt() == 1) {
            mSettings = in.readParcelable(Settings.class.getClassLoader());
        }
        
        mMayorships = new Group<Venue>();
        int numMayorships = in.readInt();
        for (int i = 0; i < numMayorships; i++) {
            Venue venue = in.readParcelable(Venue.class.getClassLoader());
            mMayorships.add(venue);
        }
    }
    
    public static final User.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public Group<Badge> getBadges() {
        return mBadges;
    }

    public void setBadges(Group<Badge> badges) {
        mBadges = badges;
    }

    public Checkin getCheckin() {
        return mCheckin;
    }

    public void setCheckin(Checkin checkin) {
        mCheckin = checkin;
    }

    public String getCreated() {
        return mCreated;
    }

    public void setCreated(String created) {
        mCreated = created;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getFacebook() {
        return mFacebook;
    }

    public void setFacebook(String facebook) {
        mFacebook = facebook;
    }
    
    public String getFirstname() {
        return mFirstname;
    }

    public void setFirstname(String firstname) {
        mFirstname = firstname;
    }

    public String getFriendstatus() {
        return mFriendstatus;
    }

    public void setFriendstatus(String friendstatus) {
        mFriendstatus = friendstatus;
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String gender) {
        mGender = gender;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getLastname() {
        return mLastname;
    }

    public void setLastname(String lastname) {
        mLastname = lastname;
    }

    public Group<Venue> getMayorships() {
        return mMayorships;
    }

    public void setMayorships(Group<Venue> mayorships) {
        mMayorships = mayorships;
    }
    
    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getPhoto() {
        return mPhoto;
    }

    public void setPhoto(String photo) {
        mPhoto = photo;
    }

    public Settings getSettings() {
        return mSettings;
    }

    public void setSettings(Settings settings) {
        mSettings = settings;
    }

    public String getTwitter() {
        return mTwitter;
    }

    public void setTwitter(String twitter) {
        mTwitter = twitter;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mCreated != null ? mCreated : ""); 
        out.writeString(mEmail != null ? mEmail : "");
        out.writeString(mFacebook != null ? mFacebook : "");
        out.writeString(mFirstname != null ? mFirstname : "");
        out.writeString(mFriendstatus != null ? mFriendstatus : "");
        out.writeString(mGender != null ? mGender : "");
        out.writeString(mId != null ? mId : "");
        out.writeString(mLastname != null ? mLastname : "");
        out.writeString(mPhone != null ? mPhone : "");
        out.writeString(mPhoto != null ? mPhoto : "");
        out.writeString(mTwitter != null ? mTwitter : "");

        if (mBadges != null) {
            out.writeInt(mBadges.size());
            for (int i = 0; i < mBadges.size(); i++) {
                out.writeParcelable(mBadges.get(i), flags);
            }
        } else {
            out.writeInt(0);
        }
        
        if (mCheckin != null) {
            out.writeInt(1);
            out.writeParcelable(mCheckin, flags);
        } else {
            out.writeInt(0);
        }
        
        if (mSettings != null) {
            out.writeInt(1);
            out.writeParcelable(mSettings, flags);
        } else {
            out.writeInt(0);
        }
        
        if (mMayorships != null) {
            out.writeInt(mMayorships.size());
            for (int i = 0; i < mMayorships.size(); i++) {
                out.writeParcelable(mMayorships.get(i), flags);
            }
        } else {
            out.writeInt(0);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
