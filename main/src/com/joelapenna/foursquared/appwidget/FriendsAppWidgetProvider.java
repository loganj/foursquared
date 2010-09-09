/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.appwidget;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.util.VenueUtils;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.MainActivity;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.UserDetailsActivity;
import com.joelapenna.foursquared.app.FoursquaredService;
import com.joelapenna.foursquared.util.DumpcatcherHelper;
import com.joelapenna.foursquared.util.RemoteResourceManager;
import com.joelapenna.foursquared.util.StringFormatters;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.Date;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FriendsAppWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "FriendsAppWidgetProvider";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final int[][] WIDGET_VIEW_IDS = {
            {
                    R.id.widgetItem0, R.id.photo0, R.id.user0, R.id.time0, R.id.location0
            }, {
                    R.id.widgetItem1, R.id.photo1, R.id.user1, R.id.time1, R.id.location1
            }, {
                    R.id.widgetItem2, R.id.photo2, R.id.user2, R.id.time2, R.id.location2
            }, {
                    R.id.widgetItem3, R.id.photo3, R.id.user3, R.id.time3, R.id.location3
            }, {
                    R.id.widgetItem4, R.id.photo4, R.id.user4, R.id.time4, R.id.location4
            }
    };

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (DEBUG) Log.d(TAG, "onUpdate()");
        Intent intent = new Intent(context, FoursquaredService.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        context.startService(intent);
    }

    public static void updateAppWidget(Context context, RemoteResourceManager rrm,
            AppWidgetManager appWidgetManager, Integer appWidgetId, Group<Checkin> checkins) {
        if (DEBUG) Log.d(TAG, "updateAppWidget: " + String.valueOf(appWidgetId));

        // Get the layout for the App Widget and attach an on-click listener to the button
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.friends_appwidget);

        if (DEBUG) Log.d(TAG, "adding header intent: " + String.valueOf(appWidgetId));
        Intent baseIntent = new Intent(context, MainActivity.class);
        views.setOnClickPendingIntent(R.id.widgetHeader, PendingIntent.getActivity(context, 0,
                baseIntent, 0));

        if (DEBUG) Log.d(TAG, "adding footer intent: " + String.valueOf(appWidgetId));
        baseIntent = new Intent(context, FoursquaredService.class);
        baseIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        views.setOnClickPendingIntent(R.id.widgetFooter, PendingIntent.getService(context, 0,
                baseIntent, 0));

        // Now hide all views if checkins is null (a sign of not being logged in), or populate the
        // checkins.
        if (checkins == null) {
            if (DEBUG) Log.d(TAG, "checkins is null, hiding UI.");
            views.setViewVisibility(R.id.widgetNotLoggedInTextView, View.VISIBLE);
            for (int i = 0; i < WIDGET_VIEW_IDS.length; i++) {
                hideCheckinView(views, WIDGET_VIEW_IDS[i]);
            }
        } else {
            if (DEBUG) Log.d(TAG, "Displaying checkins");
            views.setViewVisibility(R.id.widgetNotLoggedInTextView, View.GONE);
            int numCheckins = checkins.size();
            for (int i = 0; i < WIDGET_VIEW_IDS.length; i++) {
                if (i < numCheckins) {
                    updateCheckinView(context, rrm, views, checkins.get(i), WIDGET_VIEW_IDS[i]);
                } else {
                    hideCheckinView(views, WIDGET_VIEW_IDS[i]);
                }
            }
        }

        // Lastly, update the refresh timestamp
        CharSequence timestamp = DateUtils.formatDateTime(context, new Date().getTime(),
                DateUtils.FORMAT_SHOW_TIME);
        views.setTextViewText(R.id.widgetFooter, context.getResources().getString(
                R.string.friends_appwidget_footer_text, timestamp));

        // Tell the AppWidgetManager to perform an update on the current App Widget
        try {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (Exception e) {
            if (DEBUG) Log.d(TAG, "updateAppWidget crashed: ", e);
            DumpcatcherHelper.sendException(e);
        }
    }

    private static void updateCheckinView(Context context, RemoteResourceManager rrm,
            RemoteViews views, Checkin checkin, int[] viewIds) {
        int viewId = viewIds[0];
        int photoViewId = viewIds[1];
        int userViewId = viewIds[2];
        int timeViewId = viewIds[3];
        int locationViewId = viewIds[4];

        final User user = checkin.getUser();
        final Uri photoUri = Uri.parse(user.getPhoto());

        views.setViewVisibility(viewId, View.VISIBLE);
        Intent baseIntent;
        baseIntent = new Intent(context, UserDetailsActivity.class);
        baseIntent.putExtra(UserDetailsActivity.EXTRA_USER_ID, checkin.getUser().getId());
        baseIntent.setData(Uri.parse("https://foursquare.com/user/" + checkin.getUser().getId()));
        views.setOnClickPendingIntent(viewId, PendingIntent.getActivity(context, 0, baseIntent, 0));

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(rrm.getInputStream(photoUri));
            views.setImageViewBitmap(photoViewId, bitmap);
        } catch (IOException e) {
            if (Foursquare.MALE.equals(checkin.getUser().getGender())) {
                views.setImageViewResource(photoViewId, R.drawable.blank_boy);
            } else {
                views.setImageViewResource(photoViewId, R.drawable.blank_girl);
            }
        }

        views.setTextViewText(userViewId, StringFormatters.getUserAbbreviatedName(user));
        views.setTextViewText(timeViewId, StringFormatters.getRelativeTimeSpanString(checkin
                .getCreated()));
        if (VenueUtils.isValid(checkin.getVenue())) {
            views.setTextViewText(locationViewId, checkin.getVenue().getName());
        } else {
            views.setTextViewText(locationViewId, "");
        }
    }

    private static void hideCheckinView(RemoteViews views, int[] viewIds) {
        views.setViewVisibility(viewIds[0], View.GONE);
    }
}
