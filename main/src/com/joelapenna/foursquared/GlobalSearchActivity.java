package com.joelapenna.foursquared;

import com.joelapenna.foursquared.providers.GlobalSearchProvider;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

/**
 * Activity that gets intents from the Quick Search Box and starts the correct
 * Activity depending on the type of the data.
 * 
 * @author Tauno Talimaa (tauntz@gmail.com)
 */
public class GlobalSearchActivity extends Activity {

    private static final String TAG = GlobalSearchProvider.class.getSimpleName();
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String dataString = intent.getDataString();
        if (!TextUtils.isEmpty(dataString)) {
            Uri uri = Uri.parse(intent.getDataString());
            String directory = uri.getPathSegments().get(0);
            if (directory.equals(GlobalSearchProvider.VENUE_DIRECTORY)) {
                if (DEBUG) {
                    Log.d(TAG, "Viewing venue details for venue id:" + uri.getLastPathSegment());
                }
                Intent i = new Intent(this, VenueActivity.class);
                i.setAction(Intent.ACTION_VIEW);
                i.putExtra(Foursquared.EXTRA_VENUE_ID, uri.getLastPathSegment());
                startActivity(i);
                finish();
            } else if (directory.equals(GlobalSearchProvider.FRIEND_DIRECTORY)) {
                if (DEBUG) {
                    Log.d(TAG, "Viewing friend details for friend id:" + uri.getLastPathSegment());
                    // TODO: Implement
                }
            }
        } else {
            // For now just launch search activity and assume a venue search.
            Intent intentSearch = new Intent(this, SearchVenuesActivity.class);
            intentSearch.setAction(Intent.ACTION_SEARCH);
            if (intent.hasExtra(SearchManager.QUERY)) {
                intentSearch.putExtra(SearchManager.QUERY, intent.getStringExtra(SearchManager.QUERY));
            }
            startActivity(intentSearch);
            finish();
        }
    }
}
