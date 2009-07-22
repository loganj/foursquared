/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.maps.SimpleItemizedOverlay;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueInfoActivity extends MapActivity {
    public static final String TAG = "VenueInfoActivity";
    public static final boolean DEBUG = Foursquared.DEBUG;

    private MapView mMapView;
    private MapController mMapController;
    private SimpleItemizedOverlay mUserOverlay;
    private SimpleItemizedOverlay mVenueOverlay;

    private Venue mVenue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_info_activity);

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

        setUser();
        setVenue((Venue)getIntent().getExtras().get(Foursquared.EXTRAS_VENUE_KEY));
        setMap();
    }
    
    private void setUser() {
        Location location = ((Foursquared)getApplication()).getLocation();
        int lat = (int)(location.getLatitude() * 1E6);
        int lng = (int)(location.getLongitude() * 1E6);
        GeoPoint point = new GeoPoint(lat, lng);

        mUserOverlay = new SimpleItemizedOverlay(this.getResources().getDrawable(R.drawable.blueman));
        mUserOverlay.addOverlay(new OverlayItem(point, "You are here!", ""));
        mMapView.getOverlays().add(mUserOverlay);
        
    }

    private void setVenue(Venue venue) {
        mVenue = venue;

        int lat = (int)(Double.parseDouble(venue.getGeolat()) * 1E6);
        int lng = (int)(Double.parseDouble(venue.getGeolong()) * 1E6);
        GeoPoint point = new GeoPoint(lat, lng);

        mVenueOverlay = new SimpleItemizedOverlay(this.getResources().getDrawable(R.drawable.reddot));
        mVenueOverlay.addOverlay(new OverlayItem(point, venue.getVenuename(), ""));
        mMapView.getOverlays().add(mVenueOverlay);
    }

    private void setMap() {
        mMapController.animateTo(mVenueOverlay.getCenter());
        mMapController.setZoom(14);
    }

    /**
     * Setup the map.
     */
    private void setupMapView() {
        mMapView = (MapView)findViewById(R.id.mapView);
        mMapController = mMapView.getController();
    }

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }

}
