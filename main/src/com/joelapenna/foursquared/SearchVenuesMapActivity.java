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
import com.joelapenna.foursquare.types.Stats;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquare.util.VenueUtils;
import com.joelapenna.foursquared.maps.VenueItemizedOverlay;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class SearchVenuesMapActivity extends MapActivity {
    public static final String TAG = "SearchVenuesMapActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private Venue mTappedVenue;

    private Observer mSearchResultsObserver;
    private Button mVenueButton;

    private MapView mMapView;
    private MapController mMapController;
    private VenueItemizedOverlay mVenueItemizedOverlay;
    private MyLocationOverlay mMyLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_map_activity);

        initMap();

        mVenueButton = (Button)findViewById(R.id.venueButton);
        mVenueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEBUG) Log.d(TAG, "firing venue activity for venue");
                Intent intent = new Intent(SearchVenuesMapActivity.this, VenueActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra(Foursquared.EXTRA_VENUE_ID, mTappedVenue.getId());
                startActivity(intent);
            }
        });

        mSearchResultsObserver = new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                if (DEBUG) Log.d(TAG, "Observed search results change.");
                clearMap();
                loadSearchResults(SearchVenuesActivity.searchResultsObservable.getSearchResults());
                recenterMap();
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume()");
        mMyLocationOverlay.enableMyLocation();
        // mMyLocationOverlay.enableCompass(); // Disabled due to a sdk 1.5 emulator bug

        clearMap();
        loadSearchResults(SearchVenuesActivity.searchResultsObservable.getSearchResults());
        recenterMap();

        SearchVenuesActivity.searchResultsObservable.addObserver(mSearchResultsObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) Log.d(TAG, "onPause()");
        mMyLocationOverlay.disableMyLocation();
        mMyLocationOverlay.disableCompass();
        SearchVenuesActivity.searchResultsObservable.deleteObserver(mSearchResultsObserver);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private void initMap() {
        mMapView = (MapView)findViewById(R.id.mapView);
        mMapView.setBuiltInZoomControls(true);
        mMapController = mMapView.getController();

        mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
        mMapView.getOverlays().add(mMyLocationOverlay);
        mMyLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                if (DEBUG) Log.d(TAG, "runOnFirstFix()");
                mMapView.getController().animateTo(mMyLocationOverlay.getMyLocation());
                mMapView.getController().setZoom(16);
            }
        });
    }

    private void loadSearchResults(Group<Group<Venue>> searchResults) {
        if (searchResults == null) {
            if (DEBUG) Log.d(TAG, "no search results. Not loading.");
            return;
        }
        if (DEBUG) Log.d(TAG, "Loading search results");

        // Put our location on the map.
        mMapView.getOverlays().add(mMyLocationOverlay);

        Group<Venue> mappableVenues = new Group<Venue>();
        mappableVenues.setType("Mappable Venues");

        // For each group of venues.
        final int groupCount = searchResults.size();
        for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
            Group<Venue> group = searchResults.get(groupIndex);

            // For each venue group
            final int venueCount = group.size();
            for (int venueIndex = 0; venueIndex < venueCount; venueIndex++) {
                Venue venue = group.get(venueIndex);
                if (VenueUtils.hasValidLocation(venue)) {
                    mappableVenues.add(venue);
                }
            }
        }

        // Construct the venues overlay and attach it if we have a mappable set of venues.
        if (mappableVenues.size() > 0) {
            mVenueItemizedOverlay = new VenueItemizedOverlayWithButton( //
                    this.getResources().getDrawable(R.drawable.map_marker_blue), //
                    this.getResources().getDrawable(R.drawable.map_marker_blue_muted));
            mVenueItemizedOverlay.setGroup(mappableVenues);
            mMapView.getOverlays().add(mVenueItemizedOverlay);
        }
    }

    private void clearMap() {
        if (DEBUG) Log.d(TAG, "clearMap()");
        mMapView.getOverlays().clear();
        mMapView.postInvalidate();
    }

    private void recenterMap() {
        if (DEBUG) Log.d(TAG, "Recentering map.");
        GeoPoint center = mMyLocationOverlay.getMyLocation();

        // if we have venues in a search result, focus on those.
        if (mVenueItemizedOverlay != null && mVenueItemizedOverlay.size() > 0) {
            if (DEBUG) Log.d(TAG, "Centering on venues: "
                    + String.valueOf(mVenueItemizedOverlay.getLatSpanE6()) + " "
                    + String.valueOf(mVenueItemizedOverlay.getLonSpanE6()));
            mMapController.setCenter(mVenueItemizedOverlay.getCenter());
            mMapController.zoomToSpan(mVenueItemizedOverlay.getLatSpanE6(), mVenueItemizedOverlay
                    .getLonSpanE6());
        } else if (center != null
                && SearchVenuesActivity.searchResultsObservable.getQuery() == SearchVenuesActivity.QUERY_NEARBY) {
            if (DEBUG) Log.d(TAG, "recenterMap via MyLocation as we are doing a nearby search");
            mMapController.animateTo(center);
            mMapController.zoomToSpan(center.getLatitudeE6(), center.getLongitudeE6());
        } else if (center != null) {
            if (DEBUG) Log.d(TAG, "Fallback, recenterMap via MyLocation overlay");
            mMapController.animateTo(center);
            mMapController.setZoom(16);
            return;
        }
    }

    private class VenueItemizedOverlayWithButton extends VenueItemizedOverlay {
        public static final String TAG = "VenueItemizedOverlayWithButton";
        public static final boolean DEBUG = FoursquaredSettings.DEBUG;

        private Drawable mBeenThereMarker;

        public VenueItemizedOverlayWithButton(Drawable defaultMarker, Drawable beenThereMarker) {
            super(defaultMarker);
            mBeenThereMarker = boundCenterBottom(beenThereMarker);
        }

        @Override
        public OverlayItem createItem(int i) {
            VenueOverlayItem item = (VenueOverlayItem)super.createItem(i);
            Stats stats = item.getVenue().getStats();
            if (stats != null && stats.getBeenhere() != null && stats.getBeenhere().me()) {
                if (DEBUG) Log.d(TAG, "using the beenThereMarker for: " + item.getVenue());
                item.setMarker(mBeenThereMarker);
            }
            return item;
        }

        @Override
        public boolean onTap(GeoPoint p, MapView mapView) {
            if (DEBUG) Log.d(TAG, "onTap: " + this + " " + p + " " + mapView);
            mVenueButton.setVisibility(View.GONE);
            return super.onTap(p, mapView);
        }

        @Override
        protected boolean onTap(int i) {
            if (DEBUG) Log.d(TAG, "onTap: " + this + " " + i);
            VenueOverlayItem item = (VenueOverlayItem)getItem(i);
            mTappedVenue = item.getVenue();
            if (DEBUG) Log.d(TAG, "onTap: " + item.getVenue().getName());
            mVenueButton.setText(item.getVenue().getName());
            mVenueButton.setVisibility(View.VISIBLE);
            return true;
        }
    }
}
