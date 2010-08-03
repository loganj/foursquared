package com.joelapenna.foursquared.appwidget.stats;

/**
 * @author Nick Burton (charlesnicholasburton [at] gmail.com)
 */
public class RankParser {

	private UserRank mUserRank;
	
	public RankParser(String rankString,String statString){
		String checkins = parseCheckins(statString);
    	rankString = parseUserRank(rankString);
    	mUserRank = new UserRank(rankString,checkins);
	}
	
	public UserRank getUserRank(){
		return mUserRank;
	}
	
	public String parseUserRank(String rankString){
		int num = Integer.valueOf(rankString);
    	if( num >= 100){
    		rankString = ">99";
    	} else if( num < 10){
    		rankString = " #"+rankString;
    	} else {
    		rankString = "#"+rankString;
    	}
    	return rankString;
	}
	
	public String parseCheckins(String statString){
		int commaPos = statString.indexOf(",")+1;
		String checkins = "";
		for(int i = commaPos+1; i < statString.length(); i++){
			char c = statString.charAt(i);
			if( c == 'x'){
				break;
			}
			checkins += c; 
		}
		//Ensure formatted correctly
		int numCheckins = Integer.valueOf(checkins);
		if(numCheckins < 10){
			checkins = " "+checkins;
		} 
		if (numCheckins < 100){
			checkins += "x";
		}
    	return checkins;
    }
}