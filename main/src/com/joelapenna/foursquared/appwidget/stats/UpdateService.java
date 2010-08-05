/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joelapenna.foursquared.appwidget.stats;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.parsers.StatsParser;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.Foursquared;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.location.LocationUtils;

import java.io.IOException;

/**
 * UpdateService performs the AppWidget refresh and defines the views
 * which are updated in the RemoteViews object. The actual resources 
 * are set within the UpdateService subclass for each widget. This 
 * class is based on the Wiktionary Simple example provided the AOSP.
 * 
 * @author Nick Burton (charlesnicholasburton [at] gmail.com)
 */
public abstract class UpdateService extends Service {
	
	protected static final String TAG = "UpdateService";
	
	protected int mLayoutResource; 
	protected int mLayoutId;
	
	protected Class<? extends AppWidgetProvider> mWidgetProviderClass; 
	protected Class<? extends UpdateService> mUpdateServiceClass;
	
	private String mRefreshToastExtra;
	private boolean mShowRefreshToast = false;
	
	private static final String LEADER_BOARD_URL = "http://foursquare.com/iphone/me?uid=";
	private static final String LEADER_BOARD_SCOPE = "&view=all&scope=friends";

    private User mUser = null;
    private UserStats mUserStats = null;
    
    private Context mContext;
    private Foursquared mFoursquared;
	
    @Override
    public void onStart(Intent intent, int startId) {
    	super.onStart(intent, startId);

    	mContext = getApplicationContext();
    	
    	//Toast 'refreshing widget' if user clicked widget, otherwise nothing.
    	mRefreshToastExtra = getString(R.string.stats_widget_show_toast_extra);
    	mShowRefreshToast = intent.getBooleanExtra(mRefreshToastExtra, mShowRefreshToast);
    	if(mShowRefreshToast){
    		Toast.makeText(mContext, R.string.stats_widget_refresh_text, Toast.LENGTH_LONG).show();
    	}
    	
    	//These would actually come from SharedPrefs inside the Foursquare app.
        // TODO: hook in preferences
//    	mEmail = getString(R.string.stats_widget_email);
//    	mPword = getString(R.string.stats_widget_pword);
    	
    	//Set layout resource ids, specific to the widget provider size.
    	setLayoutResources();

        mFoursquared = (Foursquared)getApplication();
        if (mFoursquared.isReady()) {
            //Build the removeViews objects then stop.
            RemoteViews updateViews = buildUpdate(mContext);
            pushUpdate(updateViews);
        }
        stopSelf();
    }
    
    /**
     * Ids for each view are set in each subclass. 
     */
    protected abstract void setLayoutResources();
    protected abstract void updateUserStats(RemoteViews views,UserStats userStats);
    protected abstract void updateUserRank(RemoteViews views,UserRank userRank);
    
    private void pushUpdate(RemoteViews updateViews){
        ComponentName thisWidget = new ComponentName(this, mWidgetProviderClass);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, updateViews);
	}
    
    public RemoteViews buildUpdate(Context context) {
        RemoteViews updateViews = null;
        updateViews = new RemoteViews(context.getPackageName(), mLayoutResource);
        
        try {
            FoursquareHelper.prepareUserAgent(context);
            Foursquare.Location loc = LocationUtils.createFoursquareLocation(mFoursquared.getLastKnownLocation());
            mUser = mFoursquared.getFoursquare().user(null, true, true, loc);

            String lastInitial = null;
            if ( mUser.getLastname() != null && !"".equals(mUser.getLastname()) ) {
                lastInitial = mUser.getLastname().charAt(0) + ".";
            }
            String username = mUser.getFirstname() + (lastInitial == null ? "" : " "+lastInitial);

            mUserStats = new UserStats(String.valueOf(mUser.getMayorCount()),
                                       String.valueOf(mUser.getBadges().size()),
                                       mUser.getCheckin().getVenue().getName(),
                                       mUser.getId(),
                                       username);
            //Update userStats performed in subclass, based on relevant view id.
            updateUserStats(updateViews,mUserStats);
        } catch (FoursquareException e) {
            Log.e(TAG, "Foursquare problem encountered", e);
        } catch (IOException e) {
            Log.e(TAG, "Couldn't contact API", e);
        }
        
        try {
        	HTMLParser parser = new HTMLParser(this,mUserStats.getUserName());
        	String url = LEADER_BOARD_URL+mUser.getId()+LEADER_BOARD_SCOPE;
			parser.parse(url);
			UserRank userRank = parser.getUserRank();
			updateUserRank(updateViews,userRank);
			
		} catch (FoursquareHelper.ParseException e) {
			Log.e(TAG, "Could not parse HTML response", e);
		}
        	
//      When user clicks on widget, re-run the service
		Intent updateIntent = new Intent(this, mUpdateServiceClass);
		updateIntent.putExtra(mRefreshToastExtra, true);
		
		PendingIntent pendingIntent;
		int flag = PendingIntent.FLAG_UPDATE_CURRENT;
		pendingIntent = PendingIntent.getService(this, 0, updateIntent, flag);
        updateViews.setOnClickPendingIntent(mLayoutId, pendingIntent);
        return updateViews;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // We don't need to bind to this service
        return null;
    }
}
