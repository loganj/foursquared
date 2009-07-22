/**
 * Copyright 2009 Joe LaPenna
 */
package com.joelapenna.foursquared;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 *
 */
public class VenueCheckinActivity extends Activity {

    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_checkin_activity);
    }
}
