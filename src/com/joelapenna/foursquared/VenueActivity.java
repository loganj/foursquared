/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Venue;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueActivity extends TabActivity {

    Venue mVenue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_activity);

        loadVenue(Foursquared.createTestVenue());
        setupTabHost();
    }

    private void setupTabHost() {
        final TabHost tabHost = this.getTabHost();
        String tag;

        tag = (String)this.getText(R.string.venue_checkin_activity_name);
        tabHost.addTab(tabHost.newTabSpec(tag)
                // Checkin Tab
                .setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_add))
                .setContent(new Intent(this, VenueCheckinActivity.class)) // The contained activity
                );

        tag = (String)this.getText(R.string.venue_info_activity_name);
        tabHost.addTab(tabHost.newTabSpec(tag)
                // Info Tab
                .setIndicator("", getResources().getDrawable(android.R.drawable.ic_menu_compass))
                .setContent(new Intent(this, VenueInfoActivity.class)) // The contained activity
                );
    }

    private void loadVenue(Venue venue) {
        TextView name = (TextView)findViewById(R.id.venueName);
        TextView locationLine1 = (TextView)findViewById(R.id.venueLocationLine1);
        TextView locationLine2 = (TextView)findViewById(R.id.venueLocationLine2);

        name.setText(venue.getVenuename());
        locationLine1.setText(venue.getAddress());
        locationLine2.setText(venue.getCity() + ", " + venue.getState() + " " + venue.getZip());
        mVenue = venue;
    }

}
