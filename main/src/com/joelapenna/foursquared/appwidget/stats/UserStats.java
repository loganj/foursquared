package com.joelapenna.foursquared.appwidget.stats;

/**
 * @author Nick Burton (charlesnicholasburton [at] gmail.com)
 */
public class UserStats {

	private String mMayorCount;
	private String mBadgeCount;
	private String mVenue;
	private String mUserID;
	private String mUserName;
	
	public UserStats(String mayorCount,String badgeCount,String venue,
						String userID,String userName){
		mMayorCount = mayorCount;
		mBadgeCount = badgeCount;
		mVenue = venue;
		mUserID = userID;
		mUserName = userName;
	}
	
	public String getMayorCount(){
		return mMayorCount;
	}
	
	public String getBadgeCount(){
		return mBadgeCount;
	}
	
	public String getVenue(){
		return mVenue;
	}
	
	public String getUserID(){
		return mUserID;
	}
	
	public String getUserName(){
		return mUserName;
	}
}
