/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.maps;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquared.Foursquared;

import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class CheckinItemizedOverlay extends BaseGroupItemizedOverlay {
    public static final String TAG = "CheckinItemizedOverlay";
    public static final boolean DEBUG = Foursquared.DEBUG;

    public CheckinItemizedOverlay(Drawable defaultMarker) {
        super(defaultMarker);
    }

    @Override
    protected OverlayItem createItem(int i) {
        Checkin checkin = (Checkin)group.get(i);
        if (DEBUG) Log.d(TAG, "creating checkin overlayItem: " + checkin.getVenuename());
        int lat = (int)(Double.parseDouble(checkin.getGeolat()) * 1E6);
        int lng = (int)(Double.parseDouble(checkin.getGeolong()) * 1E6);
        GeoPoint point = new GeoPoint(lat, lng);
        return new CheckinOverlayItem(point, checkin);
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
        if (DEBUG) Log.d(TAG, "onTap: " + p);
        mapView.getController().animateTo(p);
        return super.onTap(p, mapView);
    }

    public static boolean isCheckinMappable(Checkin checkin) {
        if ((checkin.getGeolat() == null //
                || checkin.getGeolong() == null) //
                || checkin.getGeolat().equals("0") //
                || checkin.getGeolong().equals("0")) {
            return false;
        }
        return true;
    }

    public static class CheckinOverlayItem extends OverlayItem {

        private Checkin mCheckin;

        public CheckinOverlayItem(GeoPoint point, Checkin checkin) {
            super(point, checkin.getVenuename(), checkin.getAddress());
            mCheckin = checkin;
        }

        public Checkin getCheckin() {
            return mCheckin;
        }
    }

}
