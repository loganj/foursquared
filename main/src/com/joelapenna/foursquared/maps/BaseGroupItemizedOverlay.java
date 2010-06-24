/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.maps;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.joelapenna.foursquare.types.FoursquareType;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.FoursquaredSettings;

import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
abstract class BaseGroupItemizedOverlay<T extends FoursquareType> extends ItemizedOverlay<OverlayItem> {
    public static final String TAG = "BaseGroupItemizedOverlay";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    Group<T> group = null;

    public BaseGroupItemizedOverlay(Drawable defaultMarker) {
        super(boundCenterBottom(defaultMarker));
    }

    @Override
    public int size() {
        if (group == null) {
            return 0;
        }
        return group.size();
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
        if (DEBUG) Log.d(TAG, "onTap: " + group.getType() + " " + p);
        return super.onTap(p, mapView);
    }

    @Override
    protected boolean onTap(int i) {
        if (DEBUG) Log.d(TAG, "onTap: " + group.getType() + " " + i);
        return super.onTap(i);
    }

    public void setGroup(Group<T> g) {
        assert g.getType() != null;
        group = g;
        super.populate();
    }
}
