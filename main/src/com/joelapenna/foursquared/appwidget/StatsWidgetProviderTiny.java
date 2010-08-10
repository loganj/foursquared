package com.joelapenna.foursquared.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.Foursquared;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.StatsActivity;
import com.joelapenna.foursquared.VenueActivity;
import com.joelapenna.foursquared.app.FoursquaredService;
import com.joelapenna.foursquared.appwidget.stats.StatsWidgetUpdater;
import com.joelapenna.foursquared.appwidget.stats.UserRank;
import com.joelapenna.foursquared.appwidget.stats.UserStats;

/**
 * @author Nick Burton (charlesnicholasburton [at] gmail.com)
 */
public final class StatsWidgetProviderTiny extends AppWidgetProvider {

    final private static String TAG = "StatsWidgetProviderTiny";
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
    	Intent intent = new Intent(context, FoursquaredService.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        context.startService(intent);
        Log.i(TAG, "fired " + intent + " from onUpdate()");
    }

    public static StatsWidgetUpdater updater(Foursquared foursquared) {
        return new TinyUpdater(foursquared);
    }

	private static class TinyUpdater extends StatsWidgetUpdater {

        TinyUpdater(Foursquared foursquared) {
            super(foursquared);
        }

        @Override
		protected void setLayoutResources() {
            Log.i(TAG, "setLayoutResources()");
			mLayoutResource = R.layout.stats_widget_layout_tiny;
			mLayoutId = R.id.widget_tiny;
		}

		@Override
		protected void updateUserStats(RemoteViews updateViews, UserStats userStats) {
            Log.i(TAG, "updateUserStats()");
		    updateViews.setTextViewText(R.id.venue_tiny, userStats.getVenue());
		}
		
		@Override
		protected void updateUserRank(RemoteViews updateViews, UserRank userRank) {
            Log.i(TAG, "updateUserRank()");
			updateViews.setTextViewText(R.id.user_rank_tiny, userRank.getUserRank());
	        updateViews.setTextViewText(R.id.checkins_tiny, userRank.getCheckins());
		}

        @Override
        protected void addOnClickIntents(RemoteViews updateViews, Context context, User user) {
            // leaderboard
            Intent intent = new Intent(context, StatsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            updateViews.setOnClickPendingIntent(R.id.user_rank_tiny, pendingIntent);

            // venue
            intent = new Intent(context, VenueActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(Foursquared.EXTRA_VENUE_ID, user.getCheckin().getVenue().getId());
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            updateViews.setOnClickPendingIntent(R.id.checkins_medium, pendingIntent);
        }
    }
}


