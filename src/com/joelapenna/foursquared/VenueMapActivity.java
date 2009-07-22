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
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.maps.SimpleItemizedOverlay;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueMapActivity extends MapActivity {
    public static final String TAG = "VenueMapActivity";
    public static final boolean DEBUG = Foursquared.DEBUG;

    private MapView mMapView;
    private MapController mMapController;
    private SimpleItemizedOverlay mOverlay;
    private MyLocationOverlay mMyLocationOverlay;

    private Venue mVenue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_map_activity);

        Button yelpButton = (Button)findViewById(R.id.yelpButton);
        yelpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mVenue.getYelp()));
                startActivity(intent);

            }
        });

        setupMapView();

        setVenue((Venue)getIntent().getExtras().get(Foursquared.EXTRAS_VENUE_KEY));
        setMap();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMyLocationOverlay.enableMyLocation();
        mMyLocationOverlay.enableCompass();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMyLocationOverlay.disableMyLocation();
        mMyLocationOverlay.disableCompass();
    }

    private void setVenue(Venue venue) {
        mVenue = venue;

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
            mOverlay.addItem(new OverlayItem(point, venue.getVenuename(), ""));
            mMapView.getOverlays().add(mOverlay);
        }
    }

    private void setMap() {
        GeoPoint center;
        if (mOverlay.size() > 0) {
            center = mOverlay.getCenter();
        } else {
            return;
        }
        mMapController.animateTo(center);
        mMapController.setZoom(12);
    }

    /**
     * Setup the map.
     */
    private void setupMapView() {
        mMapView = (MapView)findViewById(R.id.mapView);
        mMapView.setBuiltInZoomControls(true);
        mMapController = mMapView.getController();

        mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
        mMapView.getOverlays().add(mMyLocationOverlay);

        mOverlay = new SimpleItemizedOverlay(this.getResources().getDrawable(R.drawable.reddot));
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

}
