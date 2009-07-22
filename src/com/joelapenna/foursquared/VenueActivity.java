/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueActivity extends TabActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_activity);

        final TabHost tabHost = this.getTabHost();
        String tag;

        tag = (String)this.getText(R.string.venue_checkin_activity_name);
        tabHost.addTab(tabHost.newTabSpec(tag) // Checkin Tab
                .setIndicator(tag) // The Name
                .setContent(new Intent(this, VenueCheckinActivity.class)) // The contained activity
                );

        tag = (String)this.getText(R.string.venue_info_activity_name);
        tabHost.addTab(tabHost.newTabSpec(tag) // Info Tab
                .setIndicator(tag) // The Name
                .setContent(new Intent(this, VenueInfoActivity.class)) // The contained activity
                );
    }

}
