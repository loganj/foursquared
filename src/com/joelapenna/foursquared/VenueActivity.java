/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Venue;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueActivity extends TabActivity {
    private static final String TAG = "VenueActivity";
    private static final boolean DEBUG = Foursquared.DEBUG;

    Venue mVenue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_activity);

        setVenue((Venue)getIntent().getExtras().get(Foursquared.EXTRAS_VENUE_KEY));
        //Venue venue = FoursquaredTest.createTestVenue("A");
        //venue.setGeolat("0");
        //venue.setGeolong("0");
        //setVenue(venue);
        setupTabHost();
    }

    private void setupTabHost() {
        final TabHost tabHost = this.getTabHost();
        String tag;
        Intent intent;

        tag = (String)this.getText(R.string.venue_checkin_activity_name);
        intent = new Intent(this, VenueCheckinActivity.class);
        intent.putExtra(Foursquared.EXTRAS_VENUE_KEY, mVenue);
        tabHost.addTab(tabHost.newTabSpec(tag)
                // Checkin Tab
                .setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_add))
                .setContent(intent) // The contained activity
                );

        tag = (String)this.getText(R.string.venue_info_activity_name);
        intent = new Intent(this, VenueInfoActivity.class);
        intent.putExtra(Foursquared.EXTRAS_VENUE_KEY, mVenue);
        tabHost.addTab(tabHost.newTabSpec(tag)
                // Info Tab
                .setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_compass))
                .setContent(intent) // The contained activity
                );
    }

    private void setVenue(Venue venue) {
        if (DEBUG) Log.d(TAG, "loading venue:" + venue.getVenuename());
        TextView name = (TextView)findViewById(R.id.venueName);
        TextView locationLine1 = (TextView)findViewById(R.id.venueLocationLine1);
        TextView locationLine2 = (TextView)findViewById(R.id.venueLocationLine2);

        name.setText(venue.getVenuename());
        locationLine1.setText(venue.getAddress());

        String line2 = Foursquared.getVenueLocationLine2(venue);
        if (line2 != null) {
            locationLine2.setText(line2);
        }

        mVenue = venue;
    }

}
