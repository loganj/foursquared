/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import com.joelapenna.foursquare.util.ParcelUtils;

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
    private int mMayorCount;
    private String mPhone;
    private String mPhoto;
    private Settings mSettings;
    private Types mTypes;
    private String mTwitter;
    private Group<Venue> mMayorships;

    public User() {
    }

    private User(Parcel in) {
        mCreated = ParcelUtils.readStringFromParcel(in);
        mEmail = ParcelUtils.readStringFromParcel(in);
        mFacebook = ParcelUtils.readStringFromParcel(in);
        mFirstname = ParcelUtils.readStringFromParcel(in);
        mFriendstatus = ParcelUtils.readStringFromParcel(in);
        mGender = ParcelUtils.readStringFromParcel(in);
        mId = ParcelUtils.readStringFromParcel(in);
        mLastname = ParcelUtils.readStringFromParcel(in);
        mPhone = ParcelUtils.readStringFromParcel(in);
        mPhoto = ParcelUtils.readStringFromParcel(in);
        mTwitter = ParcelUtils.readStringFromParcel(in);
        
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
        
        mTypes = new Types();
        int numTypes = in.readInt();
        for (int i = 0; i < numTypes; i++) {
            String type = in.readString();
            mTypes.add(type);
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
    
    public int getMayorCount() {
        return mMayorCount;
    }
    
    public void setMayorCount(int mayorCount) {
        mMayorCount = mayorCount;    
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
    
    public Types getTypes() {
        return mTypes;
    }

    public void setTypes(Types types) {
        mTypes = types;
    }

    public String getTwitter() {
        return mTwitter;
    }

    public void setTwitter(String twitter) {
        mTwitter = twitter;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        ParcelUtils.writeStringToParcel(out, mCreated);
        ParcelUtils.writeStringToParcel(out, mEmail);
        ParcelUtils.writeStringToParcel(out, mFacebook);
        ParcelUtils.writeStringToParcel(out, mFirstname);
        ParcelUtils.writeStringToParcel(out, mFriendstatus);
        ParcelUtils.writeStringToParcel(out, mGender);
        ParcelUtils.writeStringToParcel(out, mId);
        ParcelUtils.writeStringToParcel(out, mLastname);
        ParcelUtils.writeStringToParcel(out, mPhone);
        ParcelUtils.writeStringToParcel(out, mPhoto);
        ParcelUtils.writeStringToParcel(out, mTwitter);
        
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

        if (mTypes != null) {
            out.writeInt(mTypes.size());
            for (int i = 0; i < mTypes.size(); i++) {
                out.writeString(mTypes.get(i));
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
