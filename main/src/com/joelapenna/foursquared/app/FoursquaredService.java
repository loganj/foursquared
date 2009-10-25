/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.app;

import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.appwidget.SpecialDealsAppWidgetProvider;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquaredService extends Service {
    private static final String TAG = "FoursquaredService";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    @Override
    public void onStart(Intent intent, int startId) {
        if (DEBUG) Log.d(TAG, "onStart: " + intent.toString() + ", " + String.valueOf(startId));

        AppWidgetManager am = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = am.getAppWidgetIds(new ComponentName(this,
                SpecialDealsAppWidgetProvider.class));

        for (int i = 0; i < appWidgetIds.length; i++) {
            SpecialDealsAppWidgetProvider.updateAppWidget((Context)this, am, appWidgetIds[i],
                    "You sucka! " + String.valueOf(appWidgetIds[i]));
        }
        stopSelfResult(startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
