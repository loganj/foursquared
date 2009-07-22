/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.maps;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class SimpleItemizedOverlay extends ItemizedOverlay<OverlayItem> {

    ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

    public SimpleItemizedOverlay(Drawable defaultMarker) {
        super(boundCenterBottom(defaultMarker));
    }

    @Override
    protected OverlayItem createItem(int i) {
        return mOverlays.get(i);
    }

    @Override
    public int size() {
        return mOverlays.size();
    }

    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }

}
