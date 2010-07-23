/**
 * Copyright 2010 Mark Wyszomierski
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
import com.joelapenna.foursquared.maps.CrashFixMyLocationOverlay;
import com.joelapenna.foursquared.maps.VenueItemizedOverlayWithIcons;
import com.joelapenna.foursquared.maps.VenueItemizedOverlayWithIcons.VenueItemizedOverlayTapListener;
import com.joelapenna.foursquared.util.GeoUtils;
import com.joelapenna.foursquared.widget.MapCalloutView;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * @date June 30, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class NearbyVenuesMapActivity extends MapActivity {
    public static final String TAG = "PlacesMapActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private String mTappedVenueId;

    private Observer mSearchResultsObserver;
    private MapCalloutView mCallout;
    
    private MapView mMapView;
    private MapController mMapController;
    private ArrayList<VenueItemizedOverlayWithIcons> mVenueGroupOverlays = 
        new ArrayList<VenueItemizedOverlayWithIcons>();
    private MyLocationOverlay mMyLocationOverlay;
    private boolean mConstructedPinsOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_map_activity);
        
        mConstructedPinsOnce = false;

        mCallout = (MapCalloutView) findViewById(R.id.map_callout);
        mCallout.setVisibility(View.GONE);
        mCallout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NearbyVenuesMapActivity.this, VenueActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra(Foursquared.EXTRA_VENUE_ID, mTappedVenueId);
                startActivity(intent);
            }
        });
        
        initMap();

        mSearchResultsObserver = new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                if (DEBUG) Log.d(TAG, "Observed search results change.");
                clearMap();
                loadSearchResults(NearbyVenuesActivity.searchResultsObservable.getSearchResults());
                recenterMap();
            } 
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume()");
        mMyLocationOverlay.enableMyLocation();
        //mMyLocationOverlay.enableCompass(); // Disabled due to a sdk 1.5
        // emulator bug
         
        // If this is the first time we're showing the activity, try generating the overlay
        // immediately. If the results are already loaded, the observer would not be called.
        if (!mConstructedPinsOnce) {
            clearMap();
            loadSearchResults(NearbyVenuesActivity.searchResultsObservable.getSearchResults());
            recenterMap(); 
            mConstructedPinsOnce = true;
        }

        //NearbyVenuesActivity.
        NearbyVenuesActivity.searchResultsObservable.addObserver(mSearchResultsObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) Log.d(TAG, "onPause()");
        mMyLocationOverlay.disableMyLocation();
        mMyLocationOverlay.disableCompass();
        NearbyVenuesActivity.searchResultsObservable.deleteObserver(mSearchResultsObserver);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private void initMap() {
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.setBuiltInZoomControls(true);
        mMapController = mMapView.getController();

        mMyLocationOverlay = new CrashFixMyLocationOverlay(this, mMapView);
        mMapView.getOverlays().add(mMyLocationOverlay);
    }

    private void loadSearchResults(Group<Group<Venue>> venues) {
        if (venues == null) {
            if (DEBUG) Log.d(TAG, "no search results. Not loading.");
            return;
        }
        if (DEBUG) Log.d(TAG, "Loading search results");

        // One CheckinItemizedOverlay per group!
        VenueItemizedOverlayWithIcons mappableVenuesOverlay = createMappableVenuesOverlay(venues);

        if (mappableVenuesOverlay != null) {
            mVenueGroupOverlays.add(mappableVenuesOverlay);
        }
        // Only add the list of checkin group overlays if it contains any
        // overlays.
        if (mVenueGroupOverlays.size() > 0) {
            mMapView.getOverlays().addAll(mVenueGroupOverlays);
        } else {
            Toast.makeText(this, getResources().getString(
                    R.string.friendsmapactivity_no_checkins), Toast.LENGTH_LONG).show();
        }
    }

    private void clearMap() {
        if (DEBUG) Log.d(TAG, "clearMap()");
        mVenueGroupOverlays.clear();
        mMapView.getOverlays().clear();
        mMapView.getOverlays().add(mMyLocationOverlay);
        mMapView.postInvalidate();
    }

    /**
     * We can do something more fun here like create an overlay per category, so the user
     * can hide parks and show only bars, for example.
     */
    private VenueItemizedOverlayWithIcons createMappableVenuesOverlay(Group<Group<Venue>> group) {
        
        Group<Venue> mappableVenues = new Group<Venue>();
        for (Group<Venue> it : group) {
            for (Venue jt : it) {
                mappableVenues.add(jt);
            }
        }
        
        if (mappableVenues.size() > 0) {
            VenueItemizedOverlayWithIcons overlay = new VenueItemizedOverlayWithIcons(
                    this,
                    ((Foursquared) getApplication()).getRemoteResourceManager(),
                    getResources().getDrawable(R.drawable.pin_checkin_multiple),
                    mVenueOverlayTapListener);
            overlay.setGroup(mappableVenues);
            return overlay;
        } else {
            return null;
        }
    }

    private void recenterMap() {
        // Previously we'd try to zoom to span, but this gives us odd results a lot of times,
        // so falling back to zoom at a fixed level.
        GeoPoint center = mMyLocationOverlay.getMyLocation();
        if (center != null) {
            if (DEBUG) Log.d(TAG, "Using MyLocaionOverlay as center point for recenterMap().");
            mMapController.animateTo(center);
            mMapController.setZoom(16);
        } else {
            // Location overlay wasn't ready yet, try using last known geolocation from manager.
            Location bestLocation = GeoUtils.getBestLastGeolocation(this);
            if (bestLocation != null) {
                if (DEBUG) Log.d(TAG, "Using last known location as center point for recenterMap().");
                mMapController.animateTo(GeoUtils.locationToGeoPoint(bestLocation));
                mMapController.setZoom(16);
            } else {
                // We have no location information at all, so we'll just show the map at a high
                // zoom level and the user can zoom in as they wish.
                if (DEBUG) Log.d(TAG, "No location info available for recenterMap().");
                mMapController.setZoom(8);
            }
        }
    }
 
    /** Handle taps on one of the pins. */
    private VenueItemizedOverlayTapListener mVenueOverlayTapListener = 
        new VenueItemizedOverlayTapListener() {
        @Override
        public void onTap(OverlayItem itemSelected, OverlayItem itemLastSelected, Venue venue) {
            mTappedVenueId = venue.getId();
            mCallout.setTitle(venue.getName());
            mCallout.setMessage(venue.getAddress());
            mCallout.setVisibility(View.VISIBLE);

            mMapController.animateTo(GeoUtils.stringLocationToGeoPoint(
                    venue.getGeolat(), venue.getGeolong()));
        }

        @Override
        public void onTap(GeoPoint p, MapView mapView) {
            mCallout.setVisibility(View.GONE);
        }
    };
}
