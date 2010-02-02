/**
 * Copyright 2010 Tauno Talimaa
 */

package com.joelapenna.foursquared.providers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.Foursquared;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.error.LocationException;
import com.joelapenna.foursquared.location.BestLocationListener;
import com.joelapenna.foursquared.location.LocationUtils;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

/**
 * A ContentProvider for Foursquare search results.
 * 
 * @author Tauno Talimaa (tauntz@gmail.com)
 */
public class GlobalSearchProvider extends ContentProvider {

    // TODO: Implement search for friends by name/phone number/twitter ID when
    // API is implemented in Foursquare.java

    private static final String TAG = GlobalSearchProvider.class.getSimpleName();
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final String[] QSB_COLUMNS = {
            "_id", SearchManager.SUGGEST_COLUMN_ICON_1, SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_QUERY,
            SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
            SearchManager.SUGGEST_COLUMN_SPINNER_WHILE_REFRESHING,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
    };

    private static final int URI_TYPE_QUERY = 1;
    private static final int URI_TYPE_SHORTCUT = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(Foursquared.PACKAGE_NAME, SearchManager.SUGGEST_URI_PATH_QUERY + "/*",
                URI_TYPE_QUERY);
        sUriMatcher.addURI(Foursquared.PACKAGE_NAME,
                SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", URI_TYPE_SHORTCUT);
    }

    public static final String VENUE_DIRECTORY = "venue";
    public static final String FRIEND_DIRECTORY = "friend";

    // TODO: Use the argument from SUGGEST_PARAMETER_LIMIT from the Uri passed
    // to query() instead of the hardcoded value (this is available starting
    // from API level 5)
    private static final int VENUE_QUERY_LIMIT = 30;

    private Foursquare mFoursquare;

    @Override
    public boolean onCreate() {
        synchronized (this) {
            if (mFoursquare == null) mFoursquare = Foursquared.createFoursquare(getContext());
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        String query = uri.getLastPathSegment();
        MatrixCursor cursor = new MatrixCursor(QSB_COLUMNS);

        switch (sUriMatcher.match(uri)) {
            case URI_TYPE_QUERY:
                if (DEBUG) {
                    Log.d(TAG, "Global search for venue name: " + query);
                }
                Group<Group<Venue>> venueGroups;
                try {
                    venueGroups = mFoursquare.venues(LocationUtils
                            .createFoursquareLocation(getBestRecentLocation()), query,
                            VENUE_QUERY_LIMIT);
                } catch (FoursquareError e) {
                    if (DEBUG) Log.e(TAG, "Could not get venue list for query: " + query, e);
                    return cursor;
                } catch (FoursquareException e) {
                    if (DEBUG) Log.w(TAG, "Could not get venue list for query: " + query, e);
                    return cursor;
                } catch (LocationException e) {
                    if (DEBUG) Log.w(TAG, "Could not retrieve a recent location", e);
                    return cursor;
                } catch (IOException e) {
                    if (DEBUG) Log.w(TAG, "Could not get venue list for query: " + query, e);
                    return cursor;
                }
                for (int groupIndex = 0; groupIndex < venueGroups.size(); groupIndex++) {
                    Group<Venue> venueGroup = venueGroups.get(groupIndex);

                    if (DEBUG) {
                        Log.d(TAG, venueGroup.size() + " results for group: "
                                + venueGroup.getType());
                    }
                    for (int venueIndex = 0; venueIndex < venueGroup.size(); venueIndex++) {
                        Venue venue = venueGroup.get(venueIndex);
                        if (DEBUG) {
                            Log.d(TAG, "Venue " + venueIndex + ": " + venue.getName() + " ("
                                    + venue.getAddress() + ")");
                        }
                        cursor.addRow(new Object[] {
                                venue.getId(),
                                com.joelapenna.foursquared.R.drawable.venue_shortcut_icon,
                                venue.getName(), venue.getAddress(), venue.getName(),
                                venue.getId(), "true", VENUE_DIRECTORY, venue.getId()
                        });
                    }
                }

                break;
            case URI_TYPE_SHORTCUT:
                if (DEBUG) {
                    Log.d(TAG, "Global search for venue ID: " + query);
                }
                Venue venue;
                try {
                    venue = mFoursquare.venue(query, LocationUtils
                            .createFoursquareLocation(getBestRecentLocation()));
                } catch (FoursquareError e) {
                    if (DEBUG) Log.e(TAG, "Could not get venue details for venue ID: " + query, e);
                    return cursor;
                } catch (LocationException e) {
                    if (DEBUG) Log.w(TAG, "Could not retrieve a recent location", e);
                    return cursor;
                } catch (FoursquareException e) {
                    if (DEBUG) Log.w(TAG, "Could not get venue details for venue ID: " + query, e);
                    return cursor;
                } catch (IOException e) {
                    if (DEBUG) Log.w(TAG, "Could not get venue details for venue ID: " + query, e);
                    return cursor;
                }
                if (DEBUG) {
                    Log.d(TAG, "Updated venue details: " + venue.getName() + " ("
                            + venue.getAddress() + ")");
                }
                cursor.addRow(new Object[] {
                        venue.getId(), com.joelapenna.foursquared.R.drawable.venue_shortcut_icon,
                        venue.getName(), venue.getAddress(), venue.getName(), venue.getId(),
                        "true", VENUE_DIRECTORY, venue.getId()
                });

                break;
            case UriMatcher.NO_MATCH:
                if (DEBUG) {
                    Log.d(TAG, "No matching URI for: " + uri);
                }
                break;
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return SearchManager.SUGGEST_MIME_TYPE;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    /**
     * Convenience method for getting the most recent Location
     * 
     * @return the most recent Locations
     * @throws LocationException when no recent Location could be determined
     */
    private Location getBestRecentLocation() throws LocationException {
        BestLocationListener locationListener = new BestLocationListener();
        locationListener.updateLastKnownLocation((LocationManager) getContext().getSystemService(
                Context.LOCATION_SERVICE));
        Location location = locationListener.getLastKnownLocation();
        if (location != null) {
            return location;
        }
        throw new LocationException();
    }
}
