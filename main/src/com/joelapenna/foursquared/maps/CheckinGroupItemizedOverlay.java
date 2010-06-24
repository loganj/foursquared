/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.maps;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.R;
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
 * @date June 18, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class CheckinGroupItemizedOverlay extends BaseGroupItemizedOverlay<CheckinGroup> {
    public static final String TAG = "CheckinItemizedGroupOverlay";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private Context mContext;
    private RemoteResourceManager mRrm;
    private Bitmap mBmpPinSingle;
    private Bitmap mBmpPinMultiple;
    
    private OverlayItem mLastSelected;
    
    private CheckingGroupOverlayTapListener mTapListener;
    
    
    public CheckinGroupItemizedOverlay(Context context, RemoteResourceManager rrm, 
            Drawable defaultMarker, CheckingGroupOverlayTapListener tapListener) {
        super(defaultMarker);
        mContext = context;
        mRrm = rrm;
        mTapListener = tapListener;
        mLastSelected = null;
        
        constructScaledPinBackgrounds(context);
    }

    @Override
    protected OverlayItem createItem(int i) {
        CheckinGroup cg = (CheckinGroup)group.get(i);
        GeoPoint point = new GeoPoint(cg.getLatE6(), cg.getLonE6());
        return new CheckinGroupOverlayItem(point, cg, mContext, mRrm, mBmpPinSingle, mBmpPinMultiple);
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
        mapView.getController().animateTo(p);
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
    
    private void constructScaledPinBackgrounds(Context context) {
        Drawable drwSingle = context.getResources().getDrawable(R.drawable.pin_checkin_single);
        Drawable drwMultiple = context.getResources().getDrawable(R.drawable.pin_checkin_multiple);
        drwSingle.setBounds(0, 0, drwSingle.getIntrinsicWidth(), drwSingle.getIntrinsicHeight());
        drwMultiple.setBounds(0, 0, drwMultiple.getIntrinsicWidth(), drwMultiple.getIntrinsicHeight());
        mBmpPinSingle = drawableToBitmap(drwSingle);
        mBmpPinMultiple = drawableToBitmap(drwMultiple);
    }
    
    private Bitmap drawableToBitmap(Drawable drw) {
        Bitmap bmp = Bitmap.createBitmap(
                drw.getIntrinsicWidth(), drw.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        drw.draw(canvas);
        return bmp;
    }
    
    private static int dddi(int dd, float screenDensity) {
        return (int)(dd * screenDensity + 0.5f);
    }

    public static class CheckinGroupOverlayItem extends OverlayItem {

        private CheckinGroup mCheckinGroup;

        public CheckinGroupOverlayItem(GeoPoint point, CheckinGroup cg, Context context, 
                RemoteResourceManager rrm, Bitmap bmpPinSingle, Bitmap bmpPinMultiple) {
            super(point, cg.getVenueName(), cg.getVenueAddress());
            mCheckinGroup = cg;
            
            constructPinDrawable(cg, context, rrm, bmpPinSingle, bmpPinMultiple);
        }

        public CheckinGroup getCheckin() {
            return mCheckinGroup;
        }
        
        
        private void constructPinDrawable(CheckinGroup cg, Context context, RemoteResourceManager rrm, 
                    Bitmap bmpPinSingle, Bitmap bmpPinMultiple) {

            // The mdpi size of the photo background is 52 x 58.
            // The user's photo should begin at origin (9, 12).
            // The user's photo should be 34 x 34.
            float screenDensity = context.getResources().getDisplayMetrics().density;
            int cx  = dddi(52, screenDensity);
            int cy  = dddi(58, screenDensity);
            int pox = dddi(9,  screenDensity);
            int poy = dddi(12, screenDensity);
            int pcx = cx - (pox * 2);//dddi(34, screenDensity);
            int pcy = cy - (poy * 2);//dddi(34, screenDensity); 
            
            Bitmap bmp = Bitmap.createBitmap(cx, cy, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            Paint paint = new Paint();

            // Draw the correct background pin image.
            if (cg.getCheckinCount() < 2) {
                canvas.drawBitmap(bmpPinSingle, 
                        new Rect(0, 0, bmpPinSingle.getWidth(), bmpPinSingle.getHeight()), 
                        new Rect(0, 0, cx, cy), paint);
            } else {
                canvas.drawBitmap(bmpPinMultiple, 
                        new Rect(0, 0, bmpPinMultiple.getWidth(), bmpPinMultiple.getHeight()), 
                        new Rect(0, 0, cx, cy), paint);
            }
            
            // Put the user's photo on top.
            Uri photoUri = Uri.parse(cg.getPhotoUrl());
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(rrm.getInputStream(photoUri));
                canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), 
                        new Rect(pox, poy, pox + pcx, poy + pcy), paint);
            } catch (IOException e) {
                // If the user's photo isn't already in the cache, don't try loading it,
                // use a default user pin.
                Drawable drw2 = null;
                if (Foursquare.MALE.equals(cg.getGender())) {
                    drw2 = context.getResources().getDrawable(R.drawable.blank_boy);
                } else {
                    drw2 = context.getResources().getDrawable(R.drawable.blank_girl);
                }
                drw2.draw(canvas);
            }
            
            BitmapDrawable bd = new BitmapDrawable(bmp);
            bd.setBounds(new Rect(0, 0, cx, cy));
            setMarker(bd);
        }
    }
    
    public interface CheckingGroupOverlayTapListener
    {
        public void onTap(OverlayItem itemSelected, OverlayItem itemLastSelected, CheckinGroup cg);
        public void onTap(GeoPoint p, MapView mapView);
    }
}
