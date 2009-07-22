/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.maps.VenueItemizedOverlay;

import android.content.Intent;
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
    private VenueItemizedOverlay mOverlay;
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
                // TODO(jlapenna): Construct a useful Yelp URI!
                intent.setData(Uri.parse( //
                        "http://yelp.com/biz/" + mVenue.getName() + "-" + mVenue.getCity()));
                startActivity(intent);

            }
        });

        initMap();

        setVenue((Venue)getIntent().getExtras().get(VenueActivity.EXTRA_VENUE));
        updateMap();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMyLocationOverlay.enableMyLocation();
        // mMyLocationOverlay.enableCompass(); // Disabled due to a sdk 1.5 emulator bug
    }

    @Override
    public void onPause() {
        super.onPause();
        mMyLocationOverlay.disableMyLocation();
        mMyLocationOverlay.disableCompass();
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

        mOverlay = new VenueItemizedOverlay(this.getResources().getDrawable(R.drawable.reddot));
    }

    private boolean isVenueMappable(Venue venue) {
        if ((venue.getGeolat() == null || venue.getGeolong() == null) //
                || venue.getGeolat().equals("0") || venue.getGeolong().equals("0")) {
            return false;
        }
        return true;
    }

    private void setVenue(Venue venue) {
        Group venueGroup = new Group();
        venueGroup.setType("Current Venue");
        mVenue = venue;
        if (isVenueMappable(venue)) {
            mOverlay.setGroup(venueGroup);
            mMapView.getOverlays().add(mOverlay);
        }
    }

    private void updateMap() {
        GeoPoint center;
        if (mOverlay.size() > 0) {
            center = mOverlay.getCenter();
        } else {
            return;
        }
        mMapController.animateTo(center);
        mMapController.setZoom(12);
    }
}
