/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import android.app.Activity;
import android.content.Intent;
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

    private static final int URI_PATH_LOGIN = 1;
    private static final int URI_PATH_VENUE = 2;
    private static final int URI_PATH_CHECKINS = 3;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {

        sUriMatcher.addURI("foursquare.com.", "venue/#", URI_PATH_VENUE);
        sUriMatcher.addURI("m.foursquare.com.", "venue/#", URI_PATH_VENUE);

        sUriMatcher.addURI("foursquare.com.", "checkins", URI_PATH_CHECKINS);
        sUriMatcher.addURI("m.foursquare.com.", "checkins", URI_PATH_CHECKINS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");

        Uri uri = getIntent().getData();
        if (DEBUG) Log.d(TAG, "Intent Data: " + uri);

        Intent intent;

        switch (sUriMatcher.match(uri)) {
            case URI_PATH_LOGIN:
                if (DEBUG) Log.d(TAG, "Matched: URI_PATH_LOGIN");
                intent = new Intent(this, MainActivity.class);
                intent.setAction(Intent.ACTION_MAIN);
                startActivity(intent);
                break;
            case URI_PATH_VENUE:
                if (DEBUG) Log.d(TAG, "Matched: URI_PATH_VENUE");
                intent = new Intent(this, VenueActivity.class);
                intent.putExtra(Foursquared.EXTRA_VENUE_ID, uri.getLastPathSegment());
                startActivity(intent);
                break;
            case URI_PATH_CHECKINS:
                if (DEBUG) Log.d(TAG, "Matched: URI_PATH_CHECKINS");
                intent = new Intent(this, FriendsActivity.class);
                intent.setAction(Intent.ACTION_MAIN);
                startActivity(intent);
                break;
            default:
                if (DEBUG) Log.d(TAG, "Matched: None");
        }
        finish();
    }

}
