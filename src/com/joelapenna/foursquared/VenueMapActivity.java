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
public class VenueMapActivity extends MapActivity {
    public static final String TAG = "VenueMapActivity";
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
        if (location != null) {
            int lat = (int)(location.getLatitude() * 1E6);
            int lng = (int)(location.getLongitude() * 1E6);
            GeoPoint point = new GeoPoint(lat, lng);
            mUserOverlay.addOverlay(new OverlayItem(point, "You are here!", ""));
        }

    }

    private void setVenue(Venue venue) {
        mVenue = venue;

        // If our venue information is not displayable...
        if ((venue.getGeolat() == null || venue.getGeolong() == null) //
                || venue.getGeolat().equals("0") || venue.getGeolong().equals("0")) {
            return;
        }

        // Otherwise...
        if (!("0".equals(venue.getGeolat()) && "0".equals(venue.getGeolong()))) {
            int lat = (int)(Double.parseDouble(venue.getGeolat()) * 1E6);
            int lng = (int)(Double.parseDouble(venue.getGeolong()) * 1E6);
            GeoPoint point = new GeoPoint(lat, lng);
            mVenueOverlay.addOverlay(new OverlayItem(point, venue.getVenuename(), ""));
        }
    }

    private void setMap() {
        GeoPoint center;
        if (mVenueOverlay.size() > 0) {
            center = mVenueOverlay.getCenter();
        } else if (mUserOverlay.size() > 0) {
            center = mUserOverlay.getCenter();
        } else {
            return;
        }
        mMapController.animateTo(center);
        mMapController.setZoom(14);
    }

    /**
     * Setup the map.
     */
    private void setupMapView() {
        mMapView = (MapView)findViewById(R.id.mapView);
        mMapController = mMapView.getController();

        mUserOverlay = new SimpleItemizedOverlay(this.getResources()
                .getDrawable(R.drawable.blueman));
        mMapView.getOverlays().add(mUserOverlay);

        mVenueOverlay = new SimpleItemizedOverlay(this.getResources()
                .getDrawable(R.drawable.reddot));
        mMapView.getOverlays().add(mVenueOverlay);
    }

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }

}
