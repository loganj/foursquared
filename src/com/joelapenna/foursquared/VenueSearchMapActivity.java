/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.maps.SimpleItemizedOverlay;

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
    private SimpleItemizedOverlay mVenuesOverlay;
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

    private void addVenue(Venue venue) {
        // If our venue information is not displayable...
        if ((venue.getGeolat() == null || venue.getGeolong() == null) //
                || venue.getGeolat().equals("0") || venue.getGeolong().equals("0")) {
            return;
        }

        // Otherwise...
        if (("0".equals(venue.getGeolat()) && "0".equals(venue.getGeolong()))) {
            if (DEBUG) Log.d(TAG, "Terrible lat/long coordinates, ignoring.");
        } else {
            if (DEBUG) Log.d(TAG, "Adding venue overlay at: " + venue.getGeolat());
            int lat = (int)(Double.parseDouble(venue.getGeolat()) * 1E6);
            int lng = (int)(Double.parseDouble(venue.getGeolong()) * 1E6);
            GeoPoint point = new GeoPoint(lat, lng);
            mVenuesOverlay.addItem(new OverlayItem(point, venue.getVenuename(), ""));
        }
    }

    private void initMap() {
        mMapView = (MapView)findViewById(R.id.mapView);
        mMapView.setBuiltInZoomControls(true);
        mMapController = mMapView.getController();

        mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
        mMapView.getOverlays().add(mMyLocationOverlay);
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
                addVenue((Venue)group.get(venueIndex));
            }
        }
        if (mVenuesOverlay.size() > 0) {
            if (DEBUG) Log.d(TAG, "adding mVenuesOverlay to mMapView");
            mMapView.getOverlays().add(mVenuesOverlay);
        }
    }

    private void clearMap() {
        mMapView.getOverlays().remove(mVenuesOverlay);
        mVenuesOverlay = new SimpleItemizedOverlay(this.getResources().getDrawable(
                R.drawable.reddot));
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
