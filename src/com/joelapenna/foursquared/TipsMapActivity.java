/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.maps.TipItemizedOverlay;
import com.joelapenna.foursquared.util.IconsIterator;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class TipsMapActivity extends MapActivity {
    public static final String TAG = "TipsMapActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private final IconsIterator mIconIterator = new IconsIterator();

    private Venue mTappedVenue;

    private Observer mSearchResultsObserver;
    private Button mTipButton;

    private MapView mMapView;
    private MapController mMapController;
    private ArrayList<TipItemizedOverlay> mTipsGroupOverlays = new ArrayList<TipItemizedOverlay>();
    private MyLocationOverlay mMyLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_map_activity);

        mTipButton = (Button)findViewById(R.id.venueButton);
        mTipButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEBUG) Log.d(TAG, "firing tip activity for tip");
                Intent intent = new Intent(TipsMapActivity.this, VenueActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra(VenueActivity.EXTRA_VENUE, mTappedVenue.getId());
                startActivity(intent);
            }
        });

        initMap();

        mSearchResultsObserver = new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                if (DEBUG) Log.d(TAG, "Observed search results change.");
                clearMap();
                loadSearchResults(TipsActivity.searchResultsObservable.getSearchResults());
                recenterMap();
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume()");
        mMyLocationOverlay.enableMyLocation();
        // mMyLocationOverlay.enableCompass(); // Disabled due to a sdk 1.5 emulator bug

        clearMap();
        loadSearchResults(TipsActivity.searchResultsObservable.getSearchResults());
        recenterMap();

        TipsActivity.searchResultsObservable.addObserver(mSearchResultsObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) Log.d(TAG, "onPause()");
        mMyLocationOverlay.disableMyLocation();
        mMyLocationOverlay.disableCompass();
        TipsActivity.searchResultsObservable.deleteObserver(mSearchResultsObserver);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private void initMap() {
        mMapView = (MapView)findViewById(R.id.mapView);
        mMapView.setBuiltInZoomControls(true);
        mMapController = mMapView.getController();

        mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
        mMapView.getOverlays().add(mMyLocationOverlay);
    }

    private void loadSearchResults(Group searchResults) {
        if (searchResults == null) {
            if (DEBUG) Log.d(TAG, "no search results. Not loading.");
            return;
        }
        if (DEBUG) Log.d(TAG, "Loading search results");

        mIconIterator.icons.reset();
        mIconIterator.beenthereIcons.reset();

        final int groupCount = searchResults.size();
        for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
            Group group = (Group)searchResults.get(groupIndex);

            // One VenueItemizedOverlay per group!
            TipItemizedOverlay mappableTipsOverlay = createMappableTipsOverlay(group);

            if (mappableTipsOverlay != null) {
                if (DEBUG) Log.d(TAG, "adding a map view venue overlay.");
                mTipsGroupOverlays.add(mappableTipsOverlay);
            }
        }
        // Only add the list of venue group overlays if it contains any overlays.
        if (mTipsGroupOverlays.size() > 0) {
            mMapView.getOverlays().addAll(mTipsGroupOverlays);
        }
    }

    private void clearMap() {
        if (DEBUG) Log.d(TAG, "clearMap()");
        mTipsGroupOverlays.clear();
        mMapView.getOverlays().clear();
        mMapView.getOverlays().add(mMyLocationOverlay);
        mMapView.postInvalidate();
    }

    /**
     * Create an overlay that contains a specific group's list of mappable tips.
     *
     * @param group
     * @return
     */
    private TipItemizedOverlay createMappableTipsOverlay(Group group) {
        Group mappableTips = new Group();
        mappableTips.setType(group.getType());
        if (DEBUG) Log.d(TAG, "Adding items in group: " + group.getType());

        final int tipCount = group.size();
        for (int tipIndex = 0; tipIndex < tipCount; tipIndex++) {
            Tip tip = (Tip)group.get(tipIndex);
            if (TipItemizedOverlay.isTipMappable(tip)) {
                if (DEBUG) Log.d(TAG, "adding tip: " + tip.getVenue().getName());
                mappableTips.add(tip);
            }
        }
        if (mappableTips.size() > 0) {
            TipItemizedOverlay mappableTipsOverlay = new TipItemizedOverlayWithButton( //
                    this.getResources().getDrawable(
                            ((Integer)mIconIterator.allIcons.next()).intValue()));
            mappableTipsOverlay.setGroup(mappableTips);
            return mappableTipsOverlay;
        } else {
            return null;
        }
    }

    private void recenterMap() {
        GeoPoint center = mMyLocationOverlay.getMyLocation();
        if (center != null
                && SearchVenuesActivity.searchResultsObservable.getQuery() == SearchVenuesActivity.QUERY_NEARBY) {
            if (DEBUG) Log.d(TAG, "recenterMap via MyLocation as we are doing a nearby search");
            mMapController.animateTo(center);
            mMapController.setZoom(16);
        } else if (mTipsGroupOverlays.size() > 0) {
            if (DEBUG) Log.d(TAG, "recenterMap via tips overlay span.");
            TipItemizedOverlay newestOverlay = mTipsGroupOverlays.get(0);
            if (DEBUG) {
                Log.d(TAG, "recenterMap to: " + newestOverlay.getLatSpanE6() + " "
                        + newestOverlay.getLonSpanE6());
            }
            // For some reason, this is zooming us to some weird spot!.
            mMapController.zoomToSpan(newestOverlay.getLatSpanE6(), newestOverlay.getLonSpanE6());
            mMapController.animateTo(newestOverlay.getCenter());
        } else if (center != null) {
            if (DEBUG) Log.d(TAG, "Fallback, recenterMap via MyLocation overlay");
            mMapController.animateTo(center);
            mMapController.setZoom(16);
            return;
        }
        if (DEBUG) Log.d(TAG, "Could not re-center; No known user location.");
    }

    private class TipItemizedOverlayWithButton extends TipItemizedOverlay {
        public static final String TAG = "TipItemizedOverlayWithButton";
        public static final boolean DEBUG = FoursquaredSettings.DEBUG;

        public TipItemizedOverlayWithButton(Drawable defaultMarker) {
            super(defaultMarker);
        }

        @Override
        public boolean onTap(GeoPoint p, MapView mapView) {
            if (DEBUG) Log.d(TAG, "onTap: " + this + " " + p + " " + mapView);
            mTipButton.setVisibility(View.GONE);
            return super.onTap(p, mapView);
        }

        @Override
        protected boolean onTap(int i) {
            if (DEBUG) Log.d(TAG, "onTap: " + this + " " + i);
            TipOverlayItem item = (TipOverlayItem)getItem(i);
            item.getTip();
            Tip tip = item.getTip();
            mTappedVenue = tip.getVenue();
            if (DEBUG) Log.d(TAG, "onTap: " + tip.getVenue().getName());
            mTipButton.setText(tip.getText());
            mTipButton.setVisibility(View.VISIBLE);
            return true;
        }
    }
}
