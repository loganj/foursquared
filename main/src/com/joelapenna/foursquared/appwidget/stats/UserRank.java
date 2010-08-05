package com.joelapenna.foursquared.appwidget.stats;

/**
 * @author Nick Burton (charlesnicholasburton [at] gmail.com)
 */
public class UserRank {

	private String mRank;
	private String mCheckins;
	
	public UserRank(String rank,String checkins){
		mRank = rank;
		mCheckins = checkins;
	}
	
	public String getUserRank(){
		return mRank;
	}
	
	public String getCheckins(){
		return mCheckins;
	}
}
