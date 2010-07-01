/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.maps;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.util.GeoUtils;
import com.joelapenna.foursquared.util.RemoteResourceManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.IOException;

/**
 * @date June 30, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class VenueItemizedOverlayWithIcons extends BaseGroupItemizedOverlay<Venue> {
    public static final String TAG = "VenueItemizedOverlay2";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private Context mContext;
    private RemoteResourceManager mRrm;
    private OverlayItem mLastSelected;
    
    private VenueItemizedOverlayTapListener mTapListener;
    
    
    public VenueItemizedOverlayWithIcons(Context context, RemoteResourceManager rrm, 
            Drawable defaultMarker, VenueItemizedOverlayTapListener tapListener) {
        super(defaultMarker);
        mContext = context;
        mRrm = rrm;
        mTapListener = tapListener;
        mLastSelected = null;
    }

    @Override
    protected OverlayItem createItem(int i) {
        Venue venue = (Venue)group.get(i);
        GeoPoint point = GeoUtils.stringLocationToGeoPoint(
                venue.getGeolat(), venue.getGeolong());
        return new VenueOverlayItem(point, venue, mContext, mRrm);
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
        if (mTapListener != null) {
            mTapListener.onTap(p, mapView);
        }
        return super.onTap(p, mapView);
    }
    
    @Override
    public boolean onTap(int i) {
        if (mTapListener != null) {
            mTapListener.onTap(getItem(i), mLastSelected, group.get(i));
        }
        mLastSelected = getItem(i);
        return true;
    }
    
    @Override
    public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow)  {
        super.draw(canvas, mapView, false);
    }
    
    public static class VenueOverlayItem extends OverlayItem {

        private Venue mVenue;

        public VenueOverlayItem(GeoPoint point, Venue venue, Context context, 
                RemoteResourceManager rrm) {
            super(point, venue.getName(), venue.getAddress());
            mVenue = venue;
            
            constructPinDrawable(venue, context, rrm);
        }

        public Venue getVenue() {
            return mVenue;
        }

        private static int dddi(int dd, float screenDensity) {
            return (int)(dd * screenDensity + 0.5f);
        }
        
        private void constructPinDrawable(Venue venue, Context context, RemoteResourceManager rrm) {
            
            float screenDensity = context.getResources().getDisplayMetrics().density;
            int cx  = dddi(32, screenDensity);
            int cy  = dddi(32, screenDensity);
            
            Bitmap bmp = Bitmap.createBitmap(cx, cy, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            Paint paint = new Paint();

            boolean laodedPin = false;
            if (venue.getCategory() != null) {
                Uri photoUri = Uri.parse(venue.getCategory().getIconUrl());
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(rrm.getInputStream(photoUri));
                    canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), 
                            new Rect(0, 0, cx, cy), paint);
                    laodedPin = true;
                } catch (IOException e) {
                }
            }
            
            if (!laodedPin) {
                Drawable drw = context.getResources().getDrawable(R.drawable.category_none);
                drw.draw(canvas);
            }
            
            Drawable bd = new BitmapDrawable(bmp);
            bd.setBounds(-cx / 2, -cy, cx / 2, 0);
            setMarker(bd);
        }
    }
    
    public interface VenueItemizedOverlayTapListener
    {
        public void onTap(OverlayItem itemSelected, OverlayItem itemLastSelected, Venue venue);
        public void onTap(GeoPoint p, MapView mapView);
    }
}
