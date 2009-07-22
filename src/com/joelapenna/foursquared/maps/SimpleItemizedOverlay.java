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

    /**
     * @param defaultMarker
     */
    public SimpleItemizedOverlay(Drawable defaultMarker) {
        super(boundCenterBottom(defaultMarker));
    }

    ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

    /*
     * (non-Javadoc)
     * @see com.google.android.maps.ItemizedOverlay#createItem(int)
     */
    @Override
    protected OverlayItem createItem(int i) {
        return mOverlays.get(i);
    }

    /*
     * (non-Javadoc)
     * @see com.google.android.maps.ItemizedOverlay#size()
     */
    @Override
    public int size() {
        return mOverlays.size();
    }

    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }

}
