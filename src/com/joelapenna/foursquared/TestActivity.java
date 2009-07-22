/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.Venue;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class TestActivity extends Activity {
    private static final String TAG = "TestActivity";
    private static final boolean DEBUG = FoursquaredTest.DEBUG;

    private Foursquare mFoursquare;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mFoursquare = new Foursquare("4158303607", "ci9ahXa9");
        try {
            // testLogin();
            testVenues();
            testVenue();
            testCheckins();
            testTodos();
            // testBreakdown();
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

    private void testTodos() throws FoursquareError, FoursquareParseException, IOException {
        Log.d(TAG, "testTodos");
        Group tipGroups = mFoursquare.todos("23", "37.770900", "-122.436987");
        Log.d(TAG, "Num Groups:" + tipGroups.size());
        for (int i = 0; i < tipGroups.size(); i++) {
            Group tips = (Group)tipGroups.get(i);
            Log.d(TAG, "TodoGroup:" + tips.getType());
            for (int j = 0; j < tips.size(); j++) {
                Tip tip = (Tip)tips.get(j);
                Log.d(TAG, "Todo at: " + tip.getVenueid() + "(" + tip.getTipid() + ")");
            }
        }

    }

    private void testCheckins() throws FoursquareError, FoursquareParseException, IOException {
        Log.d(TAG, "testCheckins");
        Group checkinGroups = mFoursquare.checkins("23");
        Log.d(TAG, "Num Groups:" + checkinGroups.size());
        for (int i = 0; i < checkinGroups.size(); i++) {
            Group checkins = (Group)checkinGroups.get(i);
            Log.d(TAG, "CheckinGroup:" + checkins.getType());
            for (int j = 0; j < checkins.size(); j++) {
                Checkin checkin = (Checkin)checkins.get(j);
                Log.d(TAG, "Checkin at: " + checkin.getVenuename() + "(" + checkin.getCheckinid() + ")");
            }
        }
    }

    private void testBreakdown() throws FoursquareError, FoursquareParseException, IOException {
        Log.d(TAG, "testBreakdown");
        Log.d(TAG, mFoursquare.breakdown("9232", "67889"));

    }

    private void testLogin() throws FoursquareError, FoursquareParseException, IOException {
        Log.d(TAG, "testLogin");
        Log.d(TAG, String.valueOf(mFoursquare.login()));
    }

    private void testCheckin() throws FoursquareError, FoursquareParseException, IOException {
        Log.d(TAG, "testCheckin");
        Checkin checkin = mFoursquare.checkin("Bobby's place", false, false, "", "");
        Log.d(TAG, "IncomingCheckin userid: " + checkin.getUserid());
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
        Group venueGroups = mFoursquare.venues("37.770900", "-122.436987", 1, 10);
        Log.d(TAG, "Num Groups:" + venueGroups.size());
        for (int i = 0; i < venueGroups.size(); i++) {
            Group venues = (Group)venueGroups.get(i);
            Log.d(TAG, "VenueGroup:" + venues.getType());
            for (int j = 0; j < venues.size(); j++) {
                Venue venue = (Venue)venues.get(j);
                Log.d(TAG, "Venue at: " + venue.getVenuename() + "(" + venue.getVenueid() + ")");
            }
        }
    }
}
