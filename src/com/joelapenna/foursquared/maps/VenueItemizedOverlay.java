/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.maps;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.joelapenna.foursquare.types.classic.Venue;
import com.joelapenna.foursquared.Foursquared;

import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueItemizedOverlay extends BaseGroupItemizedOverlay {
    public static final String TAG = "VenueItemizedOverlay";
    public static final boolean DEBUG = Foursquared.DEBUG;

    public VenueItemizedOverlay(Drawable defaultMarker) {
        super(defaultMarker);
    }

    @Override
    protected OverlayItem createItem(int i) {
        Venue venue = (Venue)group.get(i);
        if (DEBUG) Log.d(TAG, "creating venue overlayItem: " + venue.getVenuename());
        int lat = (int)(Double.parseDouble(venue.getGeolat()) * 1E6);
        int lng = (int)(Double.parseDouble(venue.getGeolong()) * 1E6);
        GeoPoint point = new GeoPoint(lat, lng);
        return new VenueOverlayItem(point, venue);
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
        if (DEBUG) Log.d(TAG, "onTap: " + p);
        mapView.getController().animateTo(p);
        return super.onTap(p, mapView);
    }

    public static boolean isVenueMappable(Venue venue) {
        if ((venue.getGeolat() == null //
                || venue.getGeolong() == null) //
                || venue.getGeolat().equals("0") //
                || venue.getGeolong().equals("0")) {
            return false;
        }
        return true;
    }

    public static class VenueOverlayItem extends OverlayItem {

        private Venue mVenue;

        public VenueOverlayItem(GeoPoint point, Venue venue) {
            super(point, venue.getVenuename(), venue.getAddress());
            mVenue = venue;
        }

        public Venue getVenue() {
            return mVenue;
        }
    }

}
