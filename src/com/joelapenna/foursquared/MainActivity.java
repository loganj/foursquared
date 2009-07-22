/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquared.foursquare.Foursquare;
import com.joelapenna.foursquared.foursquare.error.FoursquareError;
import com.joelapenna.foursquared.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquared.foursquare.types.Venue;
import com.joelapenna.foursquared.foursquare.types.VenueGroup;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private Foursquare mFoursquare;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (DEBUG) Log.d(TAG, "onCreate");

        mFoursquare = new Foursquare("4158303607", "ci9ahXa9");
        try {
            Log.d(TAG, String.valueOf(mFoursquare.login()));
            testVenues();
            // Log.d(TAG, mFoursquare.checkin("Bobby's place", false, false, "", "").toString());
        } catch (FoursquareError e) {
            // TODO Auto-generated catch block
            if (DEBUG) Log.d(TAG, "FoursquareError", e);
        } catch (FoursquareParseException e) {
            // TODO Auto-generated catch block
            if (DEBUG) Log.d(TAG, "FoursquareParseException", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            if (DEBUG) Log.d(TAG, "IOException", e);
        }
    }

    private void testVenues() throws FoursquareError, FoursquareParseException, IOException {
        VenueGroup venues = mFoursquare.venues("37.770900", "-122.436987", 1, 10);
        Log.d(TAG, "VenueGroup:" + venues.getType());
        for (int i = 0; i < venues.size(); i++) {
            Log.d(TAG, "Venue: " + ((Venue)venues.get(i)).getVenuename());
        }
        Venue lastOne = (Venue)venues.get(venues.size()-1);
        Log.d(TAG, "LastOne:" + lastOne.getVenuename());
        Log.d(TAG, "LastOne:" + lastOne.getVenueid());
    }
}
