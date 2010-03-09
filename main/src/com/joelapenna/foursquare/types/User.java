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
    private String mFirstname;
    private String mFriendstatus;
    private String mGender;
    private String mId;
    private String mLastname;
    private String mPhone;
    private String mPhoto;
    private Settings mSettings;
    private String mTwitter;

    public User() {
    }

    private User(Parcel in) {
        mCreated = in.readString();
        mEmail = in.readString();
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
            Badge badge = Badge.CREATOR.createFromParcel(in);
            mBadges.add(badge);
        }
        
        //mCheckin = Checkin.class;
        
        mSettings = Settings.CREATOR.createFromParcel(in);
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
        out.writeString(mCreated);
        out.writeString(mEmail);
        out.writeString(mFirstname);
        out.writeString(mFriendstatus);
        out.writeString(mGender);
        out.writeString(mId);
        out.writeString(mLastname);
        out.writeString(mPhone);
        out.writeString(mPhoto);
        out.writeString(mTwitter);

        if (mBadges != null) {
            out.writeInt(mBadges.size());
            for (int i = 0; i < mBadges.size(); i++) {
                out.writeParcelable((Badge)mBadges.get(i), flags);
            }
        }
        else {
            out.writeInt(0);
        }
        
        out.writeParcelable(mCheckin, flags);
        out.writeParcelable(mSettings, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
