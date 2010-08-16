package com.joelapenna.foursquared.appwidget.stats;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.Foursquared;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.app.FoursquaredService;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.util.DumpcatcherHelper;

import java.io.IOException;

public abstract class StatsWidgetUpdater {
    private static final String LEADER_BOARD_URL = "http://foursquare.com/iphone/me?uid=";
    private static final String LEADER_BOARD_SCOPE = "&view=all&scope=friends";
    
    protected static final String TAG = "StatsWidgetUpdater";

    final private Foursquared mFoursquared;

    protected int mLayoutResource;
	protected int mLayoutId;

    final private String mRefreshToastExtra;

    protected StatsWidgetUpdater(Foursquared foursquared) {
        setLayoutResources();
        mFoursquared = foursquared;
        mRefreshToastExtra = foursquared.getString(R.string.stats_widget_show_toast_extra);
    }

    protected abstract void setLayoutResources();
    protected abstract void updateUserStats(RemoteViews views,UserStats userStats);
    protected abstract void updateUserRank(RemoteViews views,UserRank userRank);
    protected abstract void addOnClickIntents(RemoteViews updateViews, Context context, User user);


    final public void update(Context context, AppWidgetManager am, int widgetId){
        RemoteViews updateViews = buildUpdate(context);
        if (updateViews != null) {
            try {
                am.updateAppWidget(widgetId, updateViews);
            } catch (Exception e) {
                if (FoursquaredSettings.DEBUG) Log.d(TAG, "StatsWidgetUpdater.update crashed: ", e);
                DumpcatcherHelper.sendException(e);
            }
        }
	}

    final private RemoteViews buildUpdate(Context context) {
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), mLayoutResource);
        UserStats userStats = null;
        User user = null;
        if ( mFoursquared.isReady() ) {
            try {
                FoursquareHelper.prepareUserAgent(context);
                Foursquare.Location loc = LocationUtils.createFoursquareLocation(mFoursquared.getLastKnownLocation());
                user = mFoursquared.getFoursquare().user(null, true, true, loc);

                String lastInitial = null;
                if ( user.getLastname() != null && !"".equals(user.getLastname()) ) {
                    lastInitial = user.getLastname().charAt(0) + ".";
                }
                String username = user.getFirstname() + (lastInitial == null ? "" : " "+lastInitial);

                userStats = new UserStats(String.valueOf(user.getMayorCount()),
                                          String.valueOf(user.getBadges().size()),
                                          user.getCheckin().getVenue().getName(),
                                          user.getId(),
                                          username);
                //Update userStats performed in subclass, based on relevant view id.
                updateUserStats(updateViews,userStats);
            } catch (FoursquareException e) {
                Log.e(TAG, "Foursquare problem encountered", e);
            } catch (IOException e) {
                Log.e(TAG, "Couldn't contact API", e);
            }
        }

        if (user != null && userStats != null) {
            try {
                HTMLParser parser = new HTMLParser(context,userStats.getUserName());
                String url = LEADER_BOARD_URL+user.getId()+LEADER_BOARD_SCOPE;
                parser.parse(url);
                UserRank userRank = parser.getUserRank();
                updateUserRank(updateViews,userRank);

            } catch (FoursquareHelper.ParseException e) {
                Log.e(TAG, "Could not parse HTML response", e);
            }
        }

        if ( user != null ) {
            addOnClickIntents(updateViews,context, user);
        }

        return updateViews;
    }

}
