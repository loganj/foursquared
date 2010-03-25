/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.types;

import com.joelapenna.foursquare.util.ParcelUtils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Auto-generated: 2009-11-17 09:33:51.550183
 *
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com), implemented Parcelable.
 */
public class Venue implements FoursquareType, Parcelable {

    private String mAddress;
    private Group<Checkin> mCheckins;
    private String mCity;
    private String mCityid;
    private String mCrossstreet;
    private String mDistance;
    private String mGeolat;
    private String mGeolong;
    private String mId;
    private String mName;
    private String mPhone;
    private String mState;
    private Stats mStats;
    private Tags mTags;
    private Group<Tip> mTips;
    private Group<Tip> mTodos;
    private String mTwitter;
    private String mZip;
    private Category mCategory;

    public Venue() {
    }

    private Venue(Parcel in) {
        mAddress = ParcelUtils.readStringFromParcel(in);
        mCity = ParcelUtils.readStringFromParcel(in);
        mCityid = ParcelUtils.readStringFromParcel(in);
        mCrossstreet = ParcelUtils.readStringFromParcel(in);
        mDistance = ParcelUtils.readStringFromParcel(in);
        mGeolat = ParcelUtils.readStringFromParcel(in);
        mGeolong = ParcelUtils.readStringFromParcel(in);
        mId = ParcelUtils.readStringFromParcel(in);
        mName = ParcelUtils.readStringFromParcel(in);
        mPhone = ParcelUtils.readStringFromParcel(in);
        mState = ParcelUtils.readStringFromParcel(in);
        mTwitter = ParcelUtils.readStringFromParcel(in);
        mZip = ParcelUtils.readStringFromParcel(in);

        mCheckins = new Group<Checkin>();
        int numCheckins = in.readInt();
        for (int i = 0; i < numCheckins; i++) {
            Checkin checkin = in.readParcelable(Checkin.class.getClassLoader());
            mCheckins.add(checkin); 
        }
        
        if (in.readInt() == 1) {
            mStats = in.readParcelable(Stats.class.getClassLoader());
        }
        
        mTags = new Tags();
        int numTags = in.readInt();
        for (int i = 0; i < numTags; i++) {
            String tag = in.readString();
            mTags.add(tag);
        }
  
        mTips = new Group<Tip>();
        int numTips = in.readInt();
        for (int i = 0; i < numTips; i++) {
            Tip tip = in.readParcelable(Tip.class.getClassLoader());
            mTips.add(tip);
        }

        mTodos = new Group<Tip>();
        int numTodos = in.readInt();
        for (int i = 0; i < numTodos; i++) {
            Tip todo = in.readParcelable(Tip.class.getClassLoader());
            mTodos.add(todo);
        }

        if (in.readInt() == 1) {
            mCategory = in.readParcelable(Category.class.getClassLoader());
        }
    }
    
    public static final Parcelable.Creator<Venue> CREATOR = new Parcelable.Creator<Venue>() {
        public Venue createFromParcel(Parcel in) {
            return new Venue(in);
        }

        @Override
        public Venue[] newArray(int size) {
            return new Venue[size];
        }
    };
    
    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public Group<Checkin> getCheckins() {
        return mCheckins;
    }

    public void setCheckins(Group<Checkin> checkins) {
        mCheckins = checkins;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String city) {
        mCity = city;
    }

    public String getCityid() {
        return mCityid;
    }

    public void setCityid(String cityid) {
        mCityid = cityid;
    }

    public String getCrossstreet() {
        return mCrossstreet;
    }

    public void setCrossstreet(String crossstreet) {
        mCrossstreet = crossstreet;
    }

    public String getDistance() {
        return mDistance;
    }

    public void setDistance(String distance) {
        mDistance = distance;
    }

    public String getGeolat() {
        return mGeolat;
    }

    public void setGeolat(String geolat) {
        mGeolat = geolat;
    }

    public String getGeolong() {
        return mGeolong;
    }

    public void setGeolong(String geolong) {
        mGeolong = geolong;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getState() {
        return mState;
    }

    public void setState(String state) {
        mState = state;
    }

    public Stats getStats() {
        return mStats;
    }

    public void setStats(Stats stats) {
        mStats = stats;
    }

    public Tags getTags() {
        return mTags;
    }

    public void setTags(Tags tags) {
        mTags = tags;
    }

    public Group<Tip> getTips() {
        return mTips;
    }

    public void setTips(Group<Tip> tips) {
        mTips = tips;
    }

    public Group<Tip> getTodos() {
        return mTodos;
    }

    public void setTodos(Group<Tip> todos) {
        mTodos = todos;
    }

    public String getTwitter() {
        return mTwitter;
    }

    public void setTwitter(String twitter) {
        mTwitter = twitter;
    }

    public String getZip() {
        return mZip;
    }

    public void setZip(String zip) {
        mZip = zip;
    }

    public Category getCategory() {
        return mCategory;
    }
    
    public void setCategory(Category category) {
        mCategory = category;
    }
    
    
    @Override
    public void writeToParcel(Parcel out, int flags) {
        ParcelUtils.writeStringToParcel(out, mAddress);
        ParcelUtils.writeStringToParcel(out, mCity);
        ParcelUtils.writeStringToParcel(out, mCityid);
        ParcelUtils.writeStringToParcel(out, mCrossstreet);
        ParcelUtils.writeStringToParcel(out, mDistance);
        ParcelUtils.writeStringToParcel(out, mGeolat);
        ParcelUtils.writeStringToParcel(out, mGeolong);
        ParcelUtils.writeStringToParcel(out, mId);
        ParcelUtils.writeStringToParcel(out, mName);
        ParcelUtils.writeStringToParcel(out, mPhone);
        ParcelUtils.writeStringToParcel(out, mState);
        ParcelUtils.writeStringToParcel(out, mTwitter);
        ParcelUtils.writeStringToParcel(out, mZip);

        if (mCheckins != null) {
            out.writeInt(mCheckins.size());
            for (int i = 0; i < mCheckins.size(); i++) {
                out.writeParcelable(mCheckins.get(i), flags);
            }
        } else {
            out.writeInt(0);
        }
        
        if (mStats != null) {
            out.writeInt(1);
            out.writeParcelable(mStats, flags);
        } else {
            out.writeInt(0);    
        }
        
        if (mTags != null) {
            out.writeInt(mTags.size());
            for (int i = 0; i < mTags.size(); i++) {
                out.writeString(mTags.get(i));
            }
        } else {
            out.writeInt(0);
        }
  
        if (mTips != null) {
            out.writeInt(mTips.size());
            for (int i = 0; i < mTips.size(); i++) {
                out.writeParcelable((Tip)mTips.get(i), flags);
            }
        } else {
            out.writeInt(0);
        }

        if (mTodos != null) {
            out.writeInt(mTodos.size());
            for (int i = 0; i < mTodos.size(); i++) {
                out.writeParcelable((Tip)mTodos.get(i), flags);
            }
        } else {
            out.writeInt(0);
        }
            
        if (mCategory != null) {
            out.writeInt(1);
            out.writeParcelable(mCategory, flags);
        } else {
            out.writeInt(0);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
