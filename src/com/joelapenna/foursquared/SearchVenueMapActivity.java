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
import com.joelapenna.foursquared.maps.VenueItemizedOverlay;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class SearchVenueMapActivity extends MapActivity {
    public static final String TAG = "SearchVenueMapActivity";
    public static final boolean DEBUG = Foursquared.DEBUG;

    private MapView mMapView;
    private MapController mMapController;
    private VenueItemizedOverlay mVenuesOverlay;
    private MyLocationOverlay mMyLocationOverlay;
    private Observer mSearchResultsObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_venue_map_activity);

        initMap();

        mSearchResultsObserver = new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                if (DEBUG) Log.d(TAG, "Observed search results change.");
                clearMap();
                loadSearchResults(SearchVenueActivity.searchResultsObservable.getSearchResults());
                updateMap();
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume()");
        mMyLocationOverlay.enableMyLocation();
        // mMyLocationOverlay.enableCompass();  // Disabled due to a sdk 1.5 emulator bug

        clearMap();
        loadSearchResults(SearchVenueActivity.searchResultsObservable.getSearchResults());
        updateMap();

        SearchVenueActivity.searchResultsObservable.addObserver(mSearchResultsObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) Log.d(TAG, "onPause()");
        mMyLocationOverlay.disableMyLocation();
        mMyLocationOverlay.disableCompass();
        SearchVenueActivity.searchResultsObservable.deleteObserver(mSearchResultsObserver);
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

        final int groupCount = searchResults.size();
        for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
            Group group = (Group)searchResults.get(groupIndex);
            if (DEBUG) Log.d(TAG, "Adding items in group: " + group.getType());
            final int venueCount = group.size();
            for (int venueIndex = 0; venueIndex < venueCount; venueIndex++) {
                Venue venue = (Venue)group.get(venueIndex);
                if (isVenueMappable(venue)) {
                    if (DEBUG) Log.d(TAG, "adding venue: " + venue.getVenuename());
                    mVenuesOverlay.addVenue(venue);
                }
            }
        }
        if (mVenuesOverlay.size() > 0) {
            if (DEBUG) Log.d(TAG, "adding mVenuesOverlay to mMapView");
            mVenuesOverlay.finish();
            mMapView.getOverlays().add(mVenuesOverlay);
        }
    }

    private void clearMap() {
        mMapView.getOverlays().remove(mVenuesOverlay);
        mVenuesOverlay = new VenueItemizedOverlay(this.getResources().getDrawable(
                R.drawable.reddot));
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
        if (mVenuesOverlay.size() > 0) {
            if (DEBUG) Log.d(TAG, "updateMap via overlay span");
            center = mVenuesOverlay.getCenter();
            mMapController.animateTo(center);
            mMapController.zoomToSpan(mVenuesOverlay.getLatSpanE6(), mVenuesOverlay.getLonSpanE6());
            return;
        }

        if (center != null) {
            if (DEBUG) Log.d(TAG, "updateMap via location overlay");
            mMapController.animateTo(center);
            mMapController.setZoom(16);
            return;
        }
        if (DEBUG) Log.d(TAG, "Could not re-center no location or venue overlay.");
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    public class TestingVenueItemizedOverlay extends
            com.joelapenna.foursquared.maps.VenueItemizedOverlay {

        private PopupWindow mWindow;

        public TestingVenueItemizedOverlay(Drawable defaultMarker) {
            super(defaultMarker);
        }

        protected boolean onTap(int i) {
            OverlayItem item = getItem(i);
            Toast.makeText(getApplication(), item.getTitle(), Toast.LENGTH_SHORT).show();
            PopupWindow window = getPopupWindow(item);
            setPopupWindow(item);
            return true;
        }

        private void setPopupWindow(OverlayItem item) {
            GeoPoint geoPoint = item.getPoint();
            Point point = new Point();
            mMapView.getProjection().toPixels(geoPoint, point);
            int x = point.x + mMapView.getLeft();
            int y = point.y + 62; // TabHost height.
            mWindow.showAtLocation(mMapView, Gravity.NO_GRAVITY, x, y);
        }

        private PopupWindow getPopupWindow(OverlayItem item) {
            if (mWindow == null) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.venue_list_item, null);
                mWindow = new PopupWindow(view, 128, 128, true);
                mWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon));
            }
            TextView name = (TextView)mWindow.getContentView().findViewById(R.id.name);
            name.setText(item.getTitle());

            TextView locationLine1 = (TextView)mWindow.getContentView().findViewById(R.id.name);
            locationLine1.setText(item.getSnippet());
            return mWindow;
        }
    }
}
