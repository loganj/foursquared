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
import com.joelapenna.foursquared.maps.VenueItemizedOverlay;
import com.joelapenna.foursquared.util.IconsIterator;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class SearchVenuesMapActivity extends MapActivity {
    public static final String TAG = "SearchVenuesMapActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private final IconsIterator mIconsIterator = new IconsIterator();

    private Venue mTappedVenue;

    private Observer mSearchResultsObserver;
    private Button mVenueButton;

    private MapView mMapView;
    private MapController mMapController;
    private ArrayList<VenueItemizedOverlay> mVenuesGroupOverlays = new ArrayList<VenueItemizedOverlay>();
    private MyLocationOverlay mMyLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_venue_map_activity);

        mVenueButton = (Button)findViewById(R.id.venueButton);
        mVenueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEBUG) Log.d(TAG, "firing venue activity for venue");
                Intent intent = new Intent(SearchVenuesMapActivity.this, VenueActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra(VenueActivity.EXTRA_VENUE, mTappedVenue.getId());
                startActivity(intent);
            }
        });

        initMap();

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

    private void loadSearchResults(Group searchResults) {
        if (searchResults == null) {
            if (DEBUG) Log.d(TAG, "no search results. Not loading.");
            return;
        }
        if (DEBUG) Log.d(TAG, "Loading search results");

        mIconsIterator.icons.reset();
        mIconsIterator.beenthereIcons.reset();

        final int groupCount = searchResults.size();
        for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
            Group group = (Group)searchResults.get(groupIndex);

            // One VenueItemizedOverlay per group!
            VenueItemizedOverlay mappableVenuesOverlay = createMappableVenuesOverlay(group);

            if (mappableVenuesOverlay != null) {
                if (DEBUG) Log.d(TAG, "adding a map view venue overlay.");
                mVenuesGroupOverlays.add(mappableVenuesOverlay);
            }
        }
        // Only add the list of venue group overlays if it contains any overlays.
        if (mVenuesGroupOverlays.size() > 0) {
            mMapView.getOverlays().addAll(mVenuesGroupOverlays);
        }
    }

    private void clearMap() {
        if (DEBUG) Log.d(TAG, "clearMap()");
        mVenuesGroupOverlays.clear();
        mMapView.getOverlays().clear();
        mMapView.getOverlays().add(mMyLocationOverlay);
        mMapView.postInvalidate();
    }

    /**
     * Create an overlay that contains a specific group's list of mappable venues.
     *
     * @param group
     * @return
     */
    private VenueItemizedOverlay createMappableVenuesOverlay(Group group) {
        Group mappableVenues = new Group();
        mappableVenues.setType(group.getType());
        if (DEBUG) Log.d(TAG, "Adding items in group: " + group.getType());

        final int venueCount = group.size();
        for (int venueIndex = 0; venueIndex < venueCount; venueIndex++) {
            Venue venue = (Venue)group.get(venueIndex);
            if (VenueItemizedOverlay.isVenueMappable(venue)) {
                if (DEBUG) Log.d(TAG, "adding venue: " + venue.getName());
                mappableVenues.add(venue);
            }
        }
        if (mappableVenues.size() > 0) {
            VenueItemizedOverlay mappableVenuesOverlay = new VenueItemizedOverlayWithButton( //
                    this.getResources().getDrawable(
                            ((Integer)mIconsIterator.icons.next()).intValue()), //
                    this.getResources().getDrawable(
                            ((Integer)mIconsIterator.beenthereIcons.next()).intValue()));
            mappableVenuesOverlay.setGroup(mappableVenues);
            return mappableVenuesOverlay;
        } else {
            return null;
        }
    }

    private void recenterMap() {
        GeoPoint center = mMyLocationOverlay.getMyLocation();
        if (center != null
                && SearchVenuesActivity.searchResultsObservable.getQuery() == SearchVenuesActivity.QUERY_NEARBY) {
            if (DEBUG) Log.d(TAG, "recenterMap via MyLocation as we are doing a nearby search");
            mMapController.animateTo(center);
            mMapController.setZoom(16);
        } else if (mVenuesGroupOverlays.size() > 0) {
            if (DEBUG) Log.d(TAG, "recenterMap via venues overlay span.");
            VenueItemizedOverlay newestOverlay = mVenuesGroupOverlays.get(0);
            if (DEBUG) {
                Log.d(TAG, "recenterMap to: " + newestOverlay.getLatSpanE6() + " "
                        + newestOverlay.getLonSpanE6());
            }
            // For some reason, this is zooming us to some weird spot!.
            mMapController.zoomToSpan(newestOverlay.getLatSpanE6(), newestOverlay.getLonSpanE6());
            mMapController.animateTo(newestOverlay.getCenter());
        } else if (center != null) {
            if (DEBUG) Log.d(TAG, "Fallback, recenterMap via MyLocation overlay");
            mMapController.animateTo(center);
            mMapController.setZoom(16);
            return;
        }
        if (DEBUG) Log.d(TAG, "Could not re-center; No known user location.");
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
            if (stats != null && stats.getBeenhere().me()) {
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
