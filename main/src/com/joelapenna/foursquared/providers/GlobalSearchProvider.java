/**
 * Copyright 2009 Tauno Talimaa
 */

package com.joelapenna.foursquared.providers;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
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
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
    };
    
    private static final String AUTHORITY = "com.joelapenna.foursquared";
    
    private static final int TYPE_QUERY = 1;
    private static final int TYPE_SHORTCUT = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    
    static {
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", TYPE_QUERY);
        sUriMatcher
                .addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", TYPE_SHORTCUT);
    }

    public static final String VENUE_DIRECTORY = "venue";
    public static final String FRIEND_DIRECTORY = "friend";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        String query = uri.getLastPathSegment();
        MatrixCursor cursor = new MatrixCursor(QSB_COLUMNS);
        // TODO: Can this be initialized somewhere else? Doesn't seem like a
        // reasonable thing to do here every time
        Foursquare api = new Foursquare(Foursquare.createHttpApi("", false));

        switch (sUriMatcher.match(uri)) {
            case TYPE_QUERY:
                if (DEBUG) {
                    Log.d(TAG, "Global search for venue name: " + query);
                }
                try {
                    // TODO: use the argument from SUGGEST_PARAMETER_LIMIT
                    // instead of the hardcoded value (this is available
                    // starting from API level 5)
                    Group<Group<Venue>> venues = api.venues(LocationUtils
                            .createFoursquareLocation(getBestRecentLocation()), query, 30);
                    int groupCount = venues.size();
                    Venue venue;
                    for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
                        Group<Venue> group = venues.get(groupsIndex);
                        if (DEBUG) {
                            Log.d(TAG, "Global search returned " + group.size() + " results: ");
                        }
                        for (int j = 0; j < group.size(); j++) {
                            venue = group.get(j);
                            if (DEBUG) {
                                Log.d(TAG, "Result " + j + ": " + venue.getName() + " ("
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
                } catch (FoursquareError e) {
                    if (DEBUG) Log.e(TAG, "Could not get venue list for query: " + query, e);
                } catch (FoursquareException e) {
                    if (DEBUG) Log.w(TAG, "Could not get venue list for query: " + query, e);
                } catch (LocationException e) {
                    if (DEBUG) Log.w(TAG, "Could not retrieve a recent location", e);
                } catch (IOException e) {
                    if (DEBUG) Log.w(TAG, "Could not get venue list for query: " + query, e);
                }
                break;
            case TYPE_SHORTCUT:
                if (DEBUG) {
                    Log.d(TAG, "Global search for venue ID: " + query);
                }
                try {
                    Venue venue = api.venue(query, LocationUtils
                            .createFoursquareLocation(getBestRecentLocation()));
                    if (DEBUG) {
                        Log.d(TAG, "Updated venue details: " + venue.getName() + " ("
                                + venue.getAddress() + ")");
                    }
                    cursor.addRow(new Object[] {
                            venue.getId(),
                            com.joelapenna.foursquared.R.drawable.venue_shortcut_icon,
                            venue.getName(), venue.getAddress(), venue.getName(), venue.getId(),
                            "true", VENUE_DIRECTORY, venue.getId()
                    });
                } catch (FoursquareError e) {
                    if (DEBUG) Log.e(TAG, "Could not get venue details for venue ID: " + query, e);
                } catch (LocationException e) {
                    if (DEBUG) Log.w(TAG, "Could not retrieve a recent location", e);
                } catch (FoursquareException e) {
                    if (DEBUG) Log.w(TAG, "Could not get venue details for venue ID: " + query, e);
                } catch (IOException e) {
                    if (DEBUG) Log.w(TAG, "Could not get venue details for venue ID: " + query, e);
                }
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
     * Gets the best recent Location that is available. TODO: Should this be
     * refractored out from here to a utility class? I imagine that other parts
     * of the app might also want to access it
     * 
     * @return the best recent Location
     * @throws LocationException if no recent location can be determined
     */
    private Location getBestRecentLocation() throws LocationException {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(
                Context.LOCATION_SERVICE);

        // Get last good network location
        Location networkLocation = null;
        try {
            networkLocation = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (networkLocation == null) {
                if (DEBUG) Log.w(TAG, "NETWORK_PROVIDER is disabled");
            }
        } catch (IllegalArgumentException e) {
            if (DEBUG) Log.w(TAG, "NETWORK_PROVIDER does not exist");
        }

        // Get last good GPS location
        Location gpsLocation = null;
        try {
            gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (gpsLocation == null) {
                if (DEBUG) Log.w(TAG, "GPS_PROVIDER is disabled");
            }
        } catch (IllegalArgumentException e) {
            if (DEBUG) Log.w(TAG, "GPS_PROVIDER does not exist");
        }

        // If there is just one location, return it
        if (networkLocation == null && gpsLocation != null) {
            return gpsLocation;
        } else if (gpsLocation == null && networkLocation != null) {
            return networkLocation;
        } else if (gpsLocation == null && networkLocation == null) {
            throw new LocationException();
        }

        long networkLocationAge = System.currentTimeMillis() - networkLocation.getTime();
        if (DEBUG)
            Log.d(TAG, "Network location accuracy: " + networkLocation.getAccuracy() + " age: "
                    + networkLocationAge);
        long gpsLocationAge = System.currentTimeMillis() - gpsLocation.getTime();
        if (DEBUG)
            Log.d(TAG, "GPS location accuracy: " + gpsLocation.getAccuracy() + " age: "
                    + gpsLocationAge);
        
        // Check if the locations are retrieved at about the same time
        if (Math.abs(gpsLocationAge - networkLocationAge) < BestLocationListener.LOCATION_UPDATE_MAX_DELTA_THRESHOLD) {
            // Return the most accurate location
            if (gpsLocation.hasAccuracy() && networkLocation.hasAccuracy()) {
                return (gpsLocation.getAccuracy() - networkLocation.getAccuracy() > 0) ? networkLocation
                        : gpsLocation;
            }
            // Return the location that has an accuracy
            return networkLocation.hasAccuracy() ? networkLocation : gpsLocation;
        }

        // Return the location that is more recent
        return (gpsLocationAge - networkLocationAge > 0) ? networkLocation : gpsLocation;
    }

}
