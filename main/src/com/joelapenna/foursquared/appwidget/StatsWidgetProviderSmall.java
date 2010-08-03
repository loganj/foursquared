package com.joelapenna.foursquared.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.appwidget.stats.UpdateService;
import com.joelapenna.foursquared.appwidget.stats.UserRank;
import com.joelapenna.foursquared.appwidget.stats.UserStats;

/**
 * @author Nick Burton (charlesnicholasburton [at] gmail.com)
 */
public class StatsWidgetProviderSmall extends AppWidgetProvider {
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
    	Intent intent = new Intent(context, UpdateServiceSmall.class);
        context.startService(intent);
    }
	
	public static class UpdateServiceSmall extends UpdateService {
		
		@Override
		protected void setLayoutResources() {
			mLayoutResource = R.layout.stats_widget_layout_small;
			mLayoutId = R.id.widget_small;
			mWidgetProviderClass = StatsWidgetProviderSmall.class;
			mUpdateServiceClass = UpdateServiceSmall.class;
		}

		@Override
		protected void updateUserStats(RemoteViews updateViews, UserStats userStats) {
			updateViews.setTextViewText(R.id.badge_count_small, userStats.getBadgeCount());
		    updateViews.setTextViewText(R.id.mayor_count_small, userStats.getMayorCount());
		    updateViews.setTextViewText(R.id.venue_small, userStats.getVenue());
		}
		
		@Override
		protected void updateUserRank(RemoteViews updateViews, UserRank userRank) {
			updateViews.setTextViewText(R.id.user_rank_small, userRank.getUserRank());
	        updateViews.setTextViewText(R.id.checkins_small, userRank.getCheckins());
		}
	}	
}


