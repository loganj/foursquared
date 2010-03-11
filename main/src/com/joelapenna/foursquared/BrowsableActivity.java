/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class BrowsableActivity extends Activity {
    private static final String TAG = "BrowsableActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final int URI_PATH_CHECKIN = 1;
    private static final int URI_PATH_CHECKINS = 2;
    private static final int URI_PATH_SEARCH = 3;
    private static final int URI_PATH_SHOUT = 4;
    private static final int URI_PATH_USER = 5;
    private static final int URI_PATH_VENUE = 6;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {

        sUriMatcher.addURI("m.foursquare.com", "checkin", URI_PATH_CHECKIN);
        sUriMatcher.addURI("m.foursquare.com", "checkins", URI_PATH_CHECKINS);
        sUriMatcher.addURI("m.foursquare.com", "search", URI_PATH_SEARCH);
        sUriMatcher.addURI("m.foursquare.com", "shout", URI_PATH_SHOUT);
        sUriMatcher.addURI("m.foursquare.com", "user", URI_PATH_USER);
        sUriMatcher.addURI("m.foursquare.com", "venue/#", URI_PATH_VENUE);
    }

    private BroadcastReceiver mLoggedOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive: " + intent);
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        Uri uri = getIntent().getData();
        if (DEBUG) Log.d(TAG, "Intent Data: " + uri);

        Intent intent;

        switch (sUriMatcher.match(uri)) {
            case URI_PATH_CHECKIN:
                if (DEBUG) Log.d(TAG, "Matched: URI_PATH_CHECKIN");
                intent = new Intent(this, VenueActivity.class);
                intent.putExtra(Foursquared.EXTRA_VENUE_ID, uri.getQueryParameter("vid"));
                startActivity(intent);
                break;
            case URI_PATH_CHECKINS:
                if (DEBUG) Log.d(TAG, "Matched: URI_PATH_CHECKINS");
                intent = new Intent(this, FriendsActivity.class);
                startActivity(intent);
                break;
            case URI_PATH_SEARCH:
                if (DEBUG) Log.d(TAG, "Matched: URI_PATH_SEARCH");
                intent = new Intent(this, SearchVenuesActivity.class);
                startActivity(intent);
                break;
            case URI_PATH_SHOUT:
                if (DEBUG) Log.d(TAG, "Matched: URI_PATH_SHOUT");
                intent = new Intent(this, CheckinOrShoutGatherInfoActivity.class);
                intent.putExtra(CheckinOrShoutGatherInfoActivity.INTENT_EXTRA_IS_SHOUT, true);
                startActivity(intent);
                break;
            case URI_PATH_USER:
                if (DEBUG) Log.d(TAG, "Matched: URI_PATH_USER");
                intent = new Intent(this, UserDetailsActivity.class);
                intent.putExtra(UserActivity.EXTRA_USER, uri.getQueryParameter("uid"));
                startActivity(intent);
                break;
            case URI_PATH_VENUE:
                if (DEBUG) Log.d(TAG, "Matched: URI_PATH_VENUE");
                intent = new Intent(this, VenueActivity.class);
                intent.putExtra(Foursquared.EXTRA_VENUE_ID, uri.getLastPathSegment());
                startActivity(intent);
                break;
            default:
                if (DEBUG) Log.d(TAG, "Matched: None");
        }
        finish();
    }

}
