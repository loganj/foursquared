/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquare.util.VenueUtils;
import com.joelapenna.foursquared.maps.CheckinItemizedOverlay;
import com.joelapenna.foursquared.maps.CrashFixMyLocationOverlay;
import com.joelapenna.foursquared.util.StringFormatters;

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
public class FriendsMapActivity extends MapActivity {
    public static final String TAG = "FriendsMapActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private Venue mTappedVenue;

    private Observer mSearchResultsObserver;
    private Button mCheckinButton;

    private MapView mMapView;
    private MapController mMapController;
    private ArrayList<CheckinItemizedOverlay> mCheckinsGroupOverlays = new ArrayList<CheckinItemizedOverlay>();
    private MyLocationOverlay mMyLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_map_activity);

        mCheckinButton = (Button) findViewById(R.id.venueButton);
        mCheckinButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEBUG) Log.d(TAG, "firing checkin activity for checkin");
                Intent intent = new Intent(FriendsMapActivity.this, VenueActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra(Foursquared.EXTRA_VENUE_ID, mTappedVenue.getId());
                startActivity(intent);
            }
        });

        initMap();

        mSearchResultsObserver = new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                if (DEBUG) Log.d(TAG, "Observed search results change.");
                clearMap();
                loadSearchResults(FriendsActivity.searchResultsObservable.getSearchResults());
                recenterMap();
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume()");
        mMyLocationOverlay.enableMyLocation();
        // mMyLocationOverlay.enableCompass(); // Disabled due to a sdk 1.5
        // emulator bug

        clearMap();
        loadSearchResults(FriendsActivity.searchResultsObservable.getSearchResults());
        recenterMap();

        FriendsActivity.searchResultsObservable.addObserver(mSearchResultsObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) Log.d(TAG, "onPause()");
        mMyLocationOverlay.disableMyLocation();
        mMyLocationOverlay.disableCompass();
        FriendsActivity.searchResultsObservable.deleteObserver(mSearchResultsObserver);
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

    private void loadSearchResults(Group<Checkin> checkins) {
        if (checkins == null) {
            if (DEBUG) Log.d(TAG, "no search results. Not loading.");
            return;
        }
        if (DEBUG) Log.d(TAG, "Loading search results");

        // One CheckinItemizedOverlay per group!
        CheckinItemizedOverlay mappableCheckinsOverlay = createMappableCheckinsOverlay(checkins);

        if (mappableCheckinsOverlay != null) {
            if (DEBUG) Log.d(TAG, "adding a map view checkin overlay.");
            mCheckinsGroupOverlays.add(mappableCheckinsOverlay);
        }
        // Only add the list of checkin group overlays if it contains any
        // overlays.
        if (mCheckinsGroupOverlays.size() > 0) {
            mMapView.getOverlays().addAll(mCheckinsGroupOverlays);
        }
    }

    private void clearMap() {
        if (DEBUG) Log.d(TAG, "clearMap()");
        mCheckinsGroupOverlays.clear();
        mMapView.getOverlays().clear();
        mMapView.getOverlays().add(mMyLocationOverlay);
        mMapView.postInvalidate();
    }

    /**
     * Create an overlay that contains a specific group's list of mappable
     * checkins.
     * 
     * @param group
     * @return
     */
    private CheckinItemizedOverlay createMappableCheckinsOverlay(Group<Checkin> group) {
        Group<Checkin> mappableCheckins = new Group<Checkin>();
        mappableCheckins.setType(group.getType());
        if (DEBUG) Log.d(TAG, "Adding items in group: " + group.getType());

        final int checkinCount = group.size();
        for (int checkinIndex = 0; checkinIndex < checkinCount; checkinIndex++) {
            Checkin checkin = (Checkin) group.get(checkinIndex);
            if (VenueUtils.hasValidLocation(checkin.getVenue())) {
                if (DEBUG) Log.d(TAG, "adding checkin: " + checkin.getVenue().getName());
                mappableCheckins.add(checkin);
            }
        }
        if (mappableCheckins.size() > 0) {
            CheckinItemizedOverlay mappableCheckinsOverlay = new CheckinItemizedOverlayWithButton( //
                    this.getResources().getDrawable(R.drawable.map_marker_blue));
            mappableCheckinsOverlay.setGroup(mappableCheckins);
            return mappableCheckinsOverlay;
        } else {
            return null;
        }
    }

    private void recenterMap() {
        GeoPoint center = mMyLocationOverlay.getMyLocation();
        if (center != null
                && FriendsActivity.searchResultsObservable.getQuery() == FriendsActivity.QUERY_NEARBY) {
            if (DEBUG) Log.d(TAG, "recenterMap via MyLocation as we are doing a nearby search");
            mMapController.animateTo(center);
            mMapController.setZoom(16);
        } else if (mCheckinsGroupOverlays.size() > 0) {
            if (DEBUG) Log.d(TAG, "recenterMap via checkins overlay span.");
            CheckinItemizedOverlay newestOverlay = mCheckinsGroupOverlays.get(0);
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

    private class CheckinItemizedOverlayWithButton extends CheckinItemizedOverlay {
        public static final String TAG = "CheckinItemizedOverlayWithButton";
        public static final boolean DEBUG = FoursquaredSettings.DEBUG;

        public CheckinItemizedOverlayWithButton(Drawable defaultMarker) {
            super(defaultMarker);
        }

        @Override
        public boolean onTap(GeoPoint p, MapView mapView) {
            if (DEBUG) Log.d(TAG, "onTap: " + this + " " + p + " " + mapView);
            mCheckinButton.setVisibility(View.GONE);
            return super.onTap(p, mapView);
        }

        @Override
        protected boolean onTap(int i) {
            if (DEBUG) Log.d(TAG, "onTap: " + this + " " + i);
            CheckinOverlayItem item = (CheckinOverlayItem) getItem(i);
            item.getCheckin();
            Checkin checkin = item.getCheckin();
            mTappedVenue = checkin.getVenue();
            if (DEBUG) Log.d(TAG, "onTap: " + checkin.getVenue().getName());
            mCheckinButton.setText(StringFormatters.getCheckinMessageLine1(checkin, true));
            mCheckinButton.setVisibility(View.VISIBLE);
            return true;
        }
    }
}
