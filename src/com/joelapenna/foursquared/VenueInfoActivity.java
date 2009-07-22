/**
 * Copyright 2009 Joe LaPenna
 */
package com.joelapenna.foursquared;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.os.Bundle;
import android.view.View;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 *
 */
public class VenueInfoActivity extends MapActivity {
    public static final String TAG = "VenueInfoActivity";
    public static final boolean DEBUG = Foursquared.DEBUG;

    private MapView mMapView;
    private MapController mMapController;

    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_info_activity);

        setupMapView();
    }

    /* (non-Javadoc)
     * @see com.google.android.maps.MapActivity#isRouteDisplayed()
     */
    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Setup the map.
     */
    private void setupMapView() {
        mMapView = (MapView)findViewById(R.id.mapView);
        mMapView.getController();
    }

}
