/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.appwidget;

import com.joelapenna.foursquared.Foursquared;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.MainActivity;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.app.FoursquaredService;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class SpecialDealsAppWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "SpecialDealsAppWidgetProvider";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (DEBUG) Log.d(TAG, "onUpdate()");
        context.startService(new Intent(context, FoursquaredService.class));
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            Integer appWidgetId, String text) {
        if (DEBUG) Log.d(TAG, "updateAppWidget: " + String.valueOf(appWidgetId) + " with " + text);
        // Create an Intent to launch ExampleActivity

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Foursquared.EXTRA_VENUE_ID, "1234");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Get the layout for the App Widget and attach an on-click listener to the button
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.specialdeals_appwidget);

        views.setOnClickPendingIntent(R.id.checkinListItemLayout, pendingIntent);
        views.setTextViewText(R.id.firstLine, text);

        // Tell the AppWidgetManager to perform an update on the current App Widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }
}
