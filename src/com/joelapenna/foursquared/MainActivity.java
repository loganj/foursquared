/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquared.foursquare.Foursquare;
import com.joelapenna.foursquared.foursquare.error.FoursquareError;
import com.joelapenna.foursquared.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquared.foursquare.types.Checkin;
import com.joelapenna.foursquared.foursquare.types.Group;
import com.joelapenna.foursquared.foursquare.types.IncomingCheckin;
import com.joelapenna.foursquared.foursquare.types.Venue;

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

        mFoursquare = new Foursquare("4158303607", "ci9ahXa9");
        try {
            // All requests fail if this guy isn't called first. I wonder why...
            testLogin();
            testVenues();
            testVenue();
            testCheckins();
            // testCheckin();
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

    private void testCheckins() throws FoursquareError, FoursquareParseException, IOException {
        Log.d(TAG, "testCheckins");
        Group checkins = mFoursquare.checkins("23");
        Log.d(TAG, "CheckinGroup:" + checkins.getType());
        for (int i = 0; i < checkins.size(); i++) {
            Checkin checkin = (Checkin)checkins.get(i);
            Log.d(TAG, "Checkin at: " + checkin.getVenuename());
        }
    }

    private void testLogin() throws FoursquareError, FoursquareParseException, IOException {
        Log.d(TAG, "testLogin");
        Log.d(TAG, String.valueOf(mFoursquare.login()));
    }

    private void testCheckin() throws FoursquareError, FoursquareParseException, IOException {
        Log.d(TAG, "testCheckin");
        IncomingCheckin checkin = mFoursquare.checkin("Bobby's place", false, false, "", "");
        Log.d(TAG, "IncomingCheckin userid: " + checkin.getUserId());
        Log.d(TAG, "IncomingCheckin message: " + checkin.getMessage());
        Log.d(TAG, "IncomingCheckin url: " + checkin.getUrl());

    }

    private void testVenue() throws FoursquareError, FoursquareParseException, IOException {
        Log.d(TAG, "testVenue");
        Venue venue = mFoursquare.venue("23035");
        Log.d(TAG, "venue name:" + venue.getVenuename());
        Log.d(TAG, "venue id:" + venue.getVenueid());
    }

    private void testVenues() throws FoursquareError, FoursquareParseException, IOException {
        Log.d(TAG, "testVenues");
        Group venues = mFoursquare.venues("37.770900", "-122.436987", 1, 10);
        Log.d(TAG, "VenueGroup:" + venues.getType());
        for (int i = 0; i < venues.size(); i++) {
            Log.d(TAG, "Venue: " + ((Venue)venues.get(i)).getVenuename());
        }
    }
}
