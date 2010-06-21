/**
 * Copyright 2010 Mark Wyszomierski
 */
package com.joelapenna.foursquared.maps;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.FoursquareType;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Venue;


/**
 * 
 * @date June 18, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class CheckinGroup implements FoursquareType {
    
    private String mVenueId;
    private String mVenueName;
    private String mVenueAddress;
    private String mDescription;
    private String mPhotoUrl;
    private String mGender;
    private int mCheckinCount;
    private int mLatE6;
    private int mLonE6;
    
    public CheckinGroup() {
        mDescription = "";
        mPhotoUrl = "";
        mCheckinCount = 0;
        mGender = Foursquare.MALE;
    }
    
    public void appendCheckin(Checkin checkin) {
        User user = checkin.getUser();
        if (mCheckinCount == 0) {
            mPhotoUrl = user.getPhoto();
            mGender = user.getGender();
            mDescription += user.getFirstname();
            
            Venue venue = checkin.getVenue();
            mVenueId = venue.getId();
            mVenueName = venue.getName();
            mVenueAddress = venue.getAddress();
            mLatE6 = (int)(Double.parseDouble(venue.getGeolat()) * 1E6);
            mLonE6 = (int)(Double.parseDouble(venue.getGeolong()) * 1E6);
        } else {
            mDescription += ", " + user.getFirstname();
        }
        mCheckinCount++;
    }
    
    public String getVenueId() {
        return mVenueId;
    }
    
    public String getVenueName() {
        return mVenueName;
    }
    
    public String getVenueAddress() {
        return mVenueAddress;
    }
    
    public int getLatE6() {
        return mLatE6;
    }
    
    public int getLonE6() {
        return mLonE6;
    }
    
    public String getDescription() {
        return mDescription;
    }
    
    public String getPhotoUrl() {
        return mPhotoUrl;
    }
    
    public String getGender() {
        return mGender;
    }
    
    public int getCheckinCount() { 
        return mCheckinCount;
    }
}
    