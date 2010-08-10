package com.joelapenna.foursquared.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.*;
import com.joelapenna.foursquared.app.FoursquaredService;
import com.joelapenna.foursquared.appwidget.stats.StatsWidgetUpdater;
import com.joelapenna.foursquared.appwidget.stats.UserRank;
import com.joelapenna.foursquared.appwidget.stats.UserStats;

/**
 * @author Nick Burton (charlesnicholasburton [at] gmail.com)
 */
public class StatsWidgetProviderMedium extends AppWidgetProvider {
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
    	Intent intent = new Intent(context, FoursquaredService.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);        
        context.startService(intent);
    }

    public static StatsWidgetUpdater updater(Foursquared foursquared) {
        return new MediumUpdater(foursquared);
    }

	private static class MediumUpdater extends StatsWidgetUpdater {

        protected MediumUpdater(Foursquared foursquared) {
            super(foursquared);
        }

        @Override
		protected void setLayoutResources() {
			mLayoutResource = R.layout.stats_widget_layout_medium;
			mLayoutId = R.id.widget_medium;
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

        @Override
        protected void addOnClickIntents(RemoteViews updateViews, Context context, User user) {

            // leaderboard
            Intent intent = new Intent(context, StatsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            updateViews.setOnClickPendingIntent(R.id.user_rank_medium, pendingIntent);

            // mayorships
            intent = new Intent(context, UserMayorshipsActivity.class);
            intent.putExtra(UserMayorshipsActivity.EXTRA_USER_ID, user.getId());
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            updateViews.setOnClickPendingIntent(R.id.mayor_count_medium, pendingIntent);

            // badges
            intent = new Intent(context, BadgesActivity.class);
            intent.putParcelableArrayListExtra(BadgesActivity.EXTRA_BADGE_ARRAY_LIST_PARCEL, user.getBadges());
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            updateViews.setOnClickPendingIntent(R.id.badge_count_medium, pendingIntent);

            // venue
            intent = new Intent(context, VenueActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(Foursquared.EXTRA_VENUE_ID, user.getCheckin().getVenue().getId());
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            updateViews.setOnClickPendingIntent(R.id.checkins_medium, pendingIntent);

        }
    }
}
