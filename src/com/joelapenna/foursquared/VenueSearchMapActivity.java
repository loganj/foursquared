/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.maps.VenueItemizedOverlay;

import android.os.Bundle;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueSearchMapActivity extends MapActivity {
    public static final String TAG = "VenueMapActivity";
    public static final boolean DEBUG = Foursquared.DEBUG;

    private MapView mMapView;
    private MapController mMapController;
    private VenueItemizedOverlay mVenuesOverlay;
    private MyLocationOverlay mMyLocationOverlay;

    private Observer mSearchResultsObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_search_map_activity);

        initMap();

        mSearchResultsObserver = new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                if (DEBUG) Log.d(TAG, "Observed search results change.");
                clearMap();
                loadSearchResults(VenueSearchActivity.searchResultsObservable.getSearchResults());
                updateMap();
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume()");
        mMyLocationOverlay.enableMyLocation();
        mMyLocationOverlay.enableCompass();

        clearMap();
        loadSearchResults(VenueSearchActivity.searchResultsObservable.getSearchResults());
        updateMap();

        VenueSearchActivity.searchResultsObservable.addObserver(mSearchResultsObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) Log.d(TAG, "onPause()");
        mMyLocationOverlay.disableMyLocation();
        mMyLocationOverlay.disableCompass();
        VenueSearchActivity.searchResultsObservable.deleteObserver(mSearchResultsObserver);
    }

    private void initMap() {
        mMapView = (MapView)findViewById(R.id.mapView);
        mMapView.setBuiltInZoomControls(true);
        mMapController = mMapView.getController();

        mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
        mMapView.getOverlays().add(mMyLocationOverlay);
        mMyLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mMapView.getController().animateTo(mMyLocationOverlay.getMyLocation());
            }
        });
    }

    private void loadSearchResults(Group searchResults) {
        if (searchResults == null) {
            if (DEBUG) Log.d(TAG, "no search results. Not loading.");
            return;
        }

        final int groupCount = searchResults.size();
        for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
            Group group = (Group)searchResults.get(groupIndex);
            if (DEBUG) Log.d(TAG, "Adding items in group: " + group.getType());
            final int venueCount = group.size();
            for (int venueIndex = 0; venueIndex < venueCount; venueIndex++) {
                Venue venue = (Venue)group.get(venueIndex);
                if (isVenueMappable(venue)) {
                    mVenuesOverlay.addVenue(venue);
                }
            }
        }
        if (mVenuesOverlay.size() > 0) {
            if (DEBUG) Log.d(TAG, "adding mVenuesOverlay to mMapView");
            mMapView.getOverlays().add(mVenuesOverlay);
        }
    }

    private void clearMap() {
        mMapView.getOverlays().remove(mVenuesOverlay);
        mVenuesOverlay = new VenueItemizedOverlay(this.getResources()
                .getDrawable(R.drawable.reddot));
    }

    private boolean isVenueMappable(Venue venue) {
        if ((venue.getGeolat() == null || venue.getGeolong() == null) //
                || venue.getGeolat().equals("0") || venue.getGeolong().equals("0")) {
            return false;
        }
        return true;
    }

    private void updateMap() {
        GeoPoint center = mMyLocationOverlay.getMyLocation();
        if (center == null && mVenuesOverlay.size() > 0) {
            center = mVenuesOverlay.getCenter();
        }
        if (center != null) {
            mMapController.setZoom(12);
            mMapController.animateTo(center);
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
