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
public class StatsWidgetProviderTiny extends AppWidgetProvider {
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
    	Intent intent = new Intent(context, UpdateServiceTiny.class);
        context.startService(intent);
    }
	
	public static class UpdateServiceTiny extends UpdateService {
		
		@Override
		protected void setLayoutResources() {
			mLayoutResource = R.layout.stats_widget_layout_tiny;
			mLayoutId = R.id.widget_tiny;
			mWidgetProviderClass = StatsWidgetProviderTiny.class;
			mUpdateServiceClass = UpdateServiceTiny.class;
		}

		@Override
		protected void updateUserStats(RemoteViews updateViews, UserStats userStats) {
		    updateViews.setTextViewText(R.id.venue_tiny, userStats.getVenue());
		}
		
		@Override
		protected void updateUserRank(RemoteViews updateViews, UserRank userRank) {
			updateViews.setTextViewText(R.id.user_rank_tiny, userRank.getUserRank());
	        updateViews.setTextViewText(R.id.checkins_tiny, userRank.getCheckins());
		}
	}
}


