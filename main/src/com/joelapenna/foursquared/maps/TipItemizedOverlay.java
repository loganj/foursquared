/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.maps;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquared.FoursquaredSettings;

import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class TipItemizedOverlay extends BaseGroupItemizedOverlay<Tip> {
    public static final String TAG = "TipItemizedOverlay";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public TipItemizedOverlay(Drawable defaultMarker) {
        super(defaultMarker);
    }

    @Override
    protected OverlayItem createItem(int i) {
        Tip tip = (Tip)group.get(i);
        if (DEBUG) Log.d(TAG, "creating tip overlayItem: " + tip.getVenue().getName());
        int lat = (int)(Double.parseDouble(tip.getVenue().getGeolat()) * 1E6);
        int lng = (int)(Double.parseDouble(tip.getVenue().getGeolong()) * 1E6);
        GeoPoint point = new GeoPoint(lat, lng);
        return new TipOverlayItem(point, tip);
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
        if (DEBUG) Log.d(TAG, "onTap: " + p);
        mapView.getController().animateTo(p);
        return super.onTap(p, mapView);
    }

    public static class TipOverlayItem extends OverlayItem {

        private Tip mTip;

        public TipOverlayItem(GeoPoint point, Tip tip) {
            super(point, tip.getVenue().getName(), tip.getVenue().getAddress());
            mTip = tip;
        }

        public Tip getTip() {
            return mTip;
        }
    }

}
