package com.joelapenna.foursquared.appwidget.stats;

import android.content.Context;
import com.joelapenna.foursquared.R;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.net.URL;

/**
 * @author Nick Burton (charlesnicholasburton [at] gmail.com)
 */
public class HTMLParser extends DefaultHandler{ 
      
	private Context mContext;
	private UserRank mUserRank;
    private String mUserName;
    private String mRankString;
	private String mPointsString;
	private String mLastCell;
    private StringBuffer mStatBuffer = new StringBuffer();
    private boolean mIsPoints = false;
    private boolean mIsUser = false;
    private boolean mIsTData = false;
    private boolean mFoundUser = false;
    
    public HTMLParser(Context context,String uName){
    	mContext = context;
    	mUserName = uName;
    }
    
    public UserRank getUserRank() { 
        return mUserRank; 
    }
    
    public void parse(String urlString) throws FoursquareHelper.ParseException {
    	
    	SAXParserFactory spf = SAXParserFactory.newInstance();
    	try {
    		URL url = new URL(urlString);
    		url.openConnection();
    		SAXParser sp = spf.newSAXParser();
    		XMLReader xr = sp.getXMLReader();
    		xr.setContentHandler(this);
    		xr.parse(new InputSource(url.openStream()));
    	} catch (Exception e){
    		 throw new FoursquareHelper.ParseException("Problem parsing API response", e);
    	} 
    }
    
    @Override 
    public void startElement(String namespaceURI, String localName, 
               String qName, Attributes atts) throws SAXException {
    	if (localName.equals("td")){
    		mLastCell = mStatBuffer.toString();
    		mStatBuffer = new StringBuffer();
    		mIsTData = true;
    		String attr = atts.getValue("class");
    		if( attr != null ){
    			if( attr.equals("mini")){
        			mIsPoints = true;
        		}
    		}
    	}
    }
    
    @Override
    public void characters (char[] chars, int start, int length) throws SAXException
	{
	 	if (mIsTData){
	 		mStatBuffer.append(chars,start,length);
	 	}
	}

    @Override 
    public void endElement(String namespaceURI, String localName, String qName) 
              throws SAXException {
    	if(localName.equals("td")){
    		
    		if(mIsPoints){
    			mPointsString = mStatBuffer.toString();
    			if(mIsUser){
        			RankParser parser = new RankParser(mRankString,mPointsString);
    				mUserRank = parser.getUserRank();
    				mIsUser = false;
    				mFoundUser = true;
    			}
		    	mIsPoints = false;
			}
    		if( mStatBuffer.toString().equals(mUserName)){
    			mIsUser = true;
    			mRankString = mLastCell.replace(".", "");
    		}
    		mIsTData = false;
    	}	
    } 
     	
    public void endDocument() throws SAXException {
    	String blankText = mContext.getString(R.string.stats_widget_blank_stats_text);
    	if(!mFoundUser){
    		mUserRank = new UserRank(blankText,blankText);
    	}
    } 
}