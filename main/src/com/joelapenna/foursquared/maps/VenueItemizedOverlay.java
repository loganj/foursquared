/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.maps;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquare.util.VenueUtils;
import com.joelapenna.foursquared.FoursquaredSettings;

import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueItemizedOverlay extends BaseGroupItemizedOverlay<Venue> {
    public static final String TAG = "VenueItemizedOverlay";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private boolean mPopulatedSpans = false;
    private SpanHolder mSpanHolder = new SpanHolder();

    public VenueItemizedOverlay(Drawable defaultMarker) {
        super(defaultMarker);
    }

    @Override
    protected OverlayItem createItem(int i) {
        Venue venue = (Venue)group.get(i);
        if (DEBUG) Log.d(TAG, "creating venue overlayItem: " + venue.getName());
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

    @Override
    public int getLatSpanE6() {
        if (!mPopulatedSpans) {
            populateSpans();
        }
        return mSpanHolder.latSpanE6;
    }

    @Override
    public int getLonSpanE6() {
        if (!mPopulatedSpans) {
            populateSpans();
        }
        return mSpanHolder.lonSpanE6;
    }

    private void populateSpans() {
        int maxLat = 0;
        int minLat = 0;
        int maxLon = 0;
        int minLon = 0;
        for (int i = 0; i < group.size(); i++) {
            Venue venue = (Venue)group.get(i);
            if (VenueUtils.hasValidLocation(venue)) {
                int lat = (int)(Double.parseDouble(venue.getGeolat()) * 1E6);
                int lon = (int)(Double.parseDouble(venue.getGeolong()) * 1E6);

                // LatSpan
                if (lat > maxLat || maxLat == 0) {
                    maxLat = lat;
                }
                if (lat < minLat || minLat == 0) {
                    minLat = lat;
                }

                // LonSpan
                if (lon  < minLon || minLon == 0) {
                    minLon = lon;
                }
                if (lon > maxLon || maxLon == 0) {
                    maxLon = lon;
                }
            }
        }
        mSpanHolder.latSpanE6 = maxLat - minLat;
        mSpanHolder.lonSpanE6 = maxLon - minLon;
    }

    public static class VenueOverlayItem extends OverlayItem {

        private Venue mVenue;

        public VenueOverlayItem(GeoPoint point, Venue venue) {
            super(point, venue.getName(), venue.getAddress());
            mVenue = venue;
        }

        public Venue getVenue() {
            return mVenue;
        }
    }

    public static final class SpanHolder {
        int latSpanE6 = 0;
        int lonSpanE6 = 0;
    }
}
