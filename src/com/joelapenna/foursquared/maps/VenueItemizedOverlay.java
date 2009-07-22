/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.maps;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.Foursquared;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueItemizedOverlay extends ItemizedOverlay<OverlayItem> {
    public static final String TAG = "VenueItemizedOverlay";
    public static final boolean DEBUG = Foursquared.DEBUG;

    ArrayList<Venue> mVenues = new ArrayList<Venue>();

    public VenueItemizedOverlay(Drawable defaultMarker) {
        super(boundCenterBottom(defaultMarker));
    }

    @Override
    protected OverlayItem createItem(int i) {
        Venue venue = mVenues.get(i);
        if (DEBUG) Log.d(TAG, "creating venue overlayItem");
        int lat = (int)(Double.parseDouble(venue.getGeolat()) * 1E6);
        int lng = (int)(Double.parseDouble(venue.getGeolong()) * 1E6);
        GeoPoint point = new GeoPoint(lat, lng);
        return new OverlayItem(point, venue.getVenuename(), "");
    }

    @Override
    public int size() {
        return mVenues.size();
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
        mapView.getController().animateTo(p);
        return true;
    }

    public void addVenue(Venue venue) {
        mVenues.add(venue);
        populate();
    }

}
