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
public class StatsWidgetProviderMedium extends AppWidgetProvider {
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
    	Intent intent = new Intent(context, UpdateServiceMedium.class);
        context.startService(intent);
    }
	
	public static class UpdateServiceMedium extends UpdateService {
		
		@Override
		protected void setLayoutResources() {
			mLayoutResource = R.layout.stats_widget_layout_medium;
			mLayoutId = R.id.widget_medium;
			mWidgetProviderClass = StatsWidgetProviderMedium.class;
			mUpdateServiceClass = UpdateServiceMedium.class;
		}

		@Override
		protected void updateUserStats(RemoteViews updateViews, UserStats userStats) {
			updateViews.setTextViewText(R.id.badge_count_medium, userStats.getBadgeCount());
		    updateViews.setTextViewText(R.id.mayor_count_medium, userStats.getMayorCount());
		    updateViews.setTextViewText(R.id.venue_medium, userStats.getVenue());
		}
		
		@Override
		protected void updateUserRank(RemoteViews updateViews, UserRank userRank) {
			updateViews.setTextViewText(R.id.user_rank_medium, userRank.getUserRank());
	        updateViews.setTextViewText(R.id.checkins_medium, userRank.getCheckins());
		}
	}
}
