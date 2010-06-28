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
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquare.util.VenueUtils;
import com.joelapenna.foursquared.maps.CheckinGroup;
import com.joelapenna.foursquared.maps.CheckinGroupItemizedOverlay;
import com.joelapenna.foursquared.maps.CrashFixMyLocationOverlay;
import com.joelapenna.foursquared.maps.CheckinGroupItemizedOverlay.CheckingGroupOverlayTapListener;
import com.joelapenna.foursquared.util.CheckinTimestampSort;
import com.joelapenna.foursquared.widget.MapCalloutView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 * @author Mark Wyszomierski (markww@gmail.com)
 *   -Added support for checkingroup items, also stopped recreation
 *    of overlay group in onResume(). [2010-06-21]
 */
public class FriendsMapActivity extends MapActivity {
    public static final String TAG = "FriendsMapActivity";
    public static final boolean DEBUG = true;//FoursquaredSettings.DEBUG;

    private String mTappedVenueId;

    private Observer mSearchResultsObserver;
    private MapCalloutView mCallout;
    
    private MapView mMapView;
    private MapController mMapController;
    private ArrayList<CheckinGroupItemizedOverlay> mCheckinsGroupOverlays = 
        new ArrayList<CheckinGroupItemizedOverlay>();
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
                Intent intent = new Intent(FriendsMapActivity.this, VenueActivity.class);
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
        //mMyLocationOverlay.enableCompass(); // Disabled due to a sdk 1.5
        // emulator bug
        
        // If this is the first time we're showing the activity, try generating the overlay
        // immediately. If the results are already loaded, the observer would not be called.
        if (!mConstructedPinsOnce) {
            clearMap();
            loadSearchResults(FriendsActivity.searchResultsObservable.getSearchResults());
            recenterMap();
            mConstructedPinsOnce = true;
        }

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
        CheckinGroupItemizedOverlay mappableCheckinsOverlay = createMappableCheckinsOverlay(checkins);

        if (mappableCheckinsOverlay != null) {
            if (DEBUG) Log.d(TAG, "adding a map view checkin overlay.");
            mCheckinsGroupOverlays.add(mappableCheckinsOverlay);
        }
        // Only add the list of checkin group overlays if it contains any
        // overlays.
        if (mCheckinsGroupOverlays.size() > 0) {
            mMapView.getOverlays().addAll(mCheckinsGroupOverlays);
        } else {
            Toast.makeText(this, getResources().getString(
                    R.string.friendsmapactivity_no_checkins), Toast.LENGTH_LONG).show();
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
     */
    private CheckinGroupItemizedOverlay createMappableCheckinsOverlay(Group<Checkin> group) {
        // We want to group checkins by venue. Do max three checkins per venue, a total
        // of 100 venues total. We should only also display checkins that are within a
        // city radius, and are at most three hours old.
        CheckinTimestampSort timestamps = new CheckinTimestampSort();
        
        Map<String, CheckinGroup> checkinMap = new HashMap<String, CheckinGroup>();
        for (int i = 0, m = group.size(); i < m; i++) {
            Checkin checkin = (Checkin)group.get(i);
            Venue venue = checkin.getVenue();
            if (VenueUtils.hasValidLocation(venue)) {
                // Make sure the venue is within city radius.
                try {
                    int distance = Integer.parseInt(checkin.getDistance());
                    if (distance > FriendsActivity.CITY_RADIUS_IN_METERS) {
                        continue;
                    }
                } catch (NumberFormatException ex) {
                    // Distance was invalid, ignore this checkin.
                    continue;
                }
                
                // Make sure the checkin happened within the last three hours.
                try { 
                    Date date = new Date(checkin.getCreated());
                    if (date.before(timestamps.getBoundaryRecent())) {
                        continue;
                    }
                } catch (Exception ex) {
                    // Timestamps was invalid, ignore this checkin.
                    continue;
                }
                
                String venueId = venue.getId();
                CheckinGroup cg = checkinMap.get(venueId);
                if (cg == null) {
                    cg = new CheckinGroup();
                    checkinMap.put(venueId, cg);
                }
                
                // Stop appending if we already have three checkins here.
                if (cg.getCheckinCount() < 3) {
                    cg.appendCheckin(checkin);
                }
            }
            
            // We can't have too many pins on the map.
            if (checkinMap.size() > 99) {
                break;
            }
        }

        Group<CheckinGroup> mappableCheckins = new Group<CheckinGroup>(checkinMap.values());
        if (mappableCheckins.size() > 0) {
            CheckinGroupItemizedOverlay mappableCheckinsGroupOverlay = new CheckinGroupItemizedOverlay(
                    this,
                    ((Foursquared) getApplication()).getRemoteResourceManager(),
                    this.getResources().getDrawable(R.drawable.map_marker_blue),
                    mCheckingGroupOverlayTapListener);
            mappableCheckinsGroupOverlay.setGroup(mappableCheckins);
            return mappableCheckinsGroupOverlay;
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
            CheckinGroupItemizedOverlay newestOverlay = mCheckinsGroupOverlays.get(0);
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
        } else {
            if (DEBUG) Log.d(TAG, "Could not re-center; No known user location.");
        }
    }
 
    /** Handle taps on one of the pins. */
    private CheckingGroupOverlayTapListener mCheckingGroupOverlayTapListener = 
        new CheckingGroupOverlayTapListener() {
        @Override
        public void onTap(OverlayItem itemSelected, OverlayItem itemLastSelected, CheckinGroup cg) {
            mTappedVenueId = cg.getVenueId();
            mCallout.setTitle(cg.getVenueName());
            mCallout.setMessage(cg.getDescription());
            mCallout.setVisibility(View.VISIBLE);

            mMapController.animateTo(new GeoPoint(cg.getLatE6(), cg.getLonE6()));
        }

        @Override
        public void onTap(GeoPoint p, MapView mapView) {
            mCallout.setVisibility(View.GONE);
        }
    };
}
