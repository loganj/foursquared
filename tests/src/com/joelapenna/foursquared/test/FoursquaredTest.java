/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.test;

import com.joelapenna.foursquare.types.Beenhere;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Stats;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.Venue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class FoursquaredTest {
    public static final boolean DEBUG = true;
    private static final Random mRandom = new Random();

    public static final String TAG = "FoursquaredTest";

    public static Checkin createCheckin(String id, Venue venue) {
        Checkin checkin = new Checkin();
        checkin.setVenue(venue);
        User user = new User();
        user.setFirstname("2");
        user.setLastname("User2");
        // user.setPhoto("http://photourl.com");
        user.setId("9923");
        checkin.setUser(user);
        checkin.setId(id);

        return checkin;
    }

    public static Group createRandomCheckinGroup(String id) {
        Group tlg = new Group();
        tlg.setType(id);
        tlg.add(createCheckin(id + " 1", createRandomVenue(id + " 1")));
        tlg.add(createCheckin(id + " 2", createRandomVenue(id + " 2")));
        tlg.add(createCheckin(id + " 3", createRandomVenue(id + " 3")));
        tlg.add(createCheckin(id + " 4", createRandomVenue(id + " 4")));
        tlg.add(createCheckin(id + " 5", createRandomVenue(id + " 5")));
        tlg.add(createCheckin(id + " 6", createRandomVenue(id + " 6")));
        tlg.add(createCheckin(id + " 7", createRandomVenue(id + " 7")));
        tlg.add(createCheckin(id + " 8", createRandomVenue(id + " 8")));
        tlg.add(createCheckin(id + " 9", createRandomVenue(id + " 9")));
        tlg.add(createCheckin(id + " 10", createRandomVenue(id + " 10")));
        return tlg;
    }

    public static Group createRandomCheckinGroups(String id) {
        Group group = new Group();
        group.setType(id);
        group.add(FoursquaredTest.createRandomCheckinGroup("Me"));
        group.add(FoursquaredTest.createRandomCheckinGroup("Last 3 hours"));
        group.add(FoursquaredTest.createRandomCheckinGroup("Older"));
        return group;
    }

    public static Venue createRandomVenue(String id) {
        Venue venue = createVenue(id);

        venue.setGeolat(String.valueOf(Float.valueOf(venue.getGeolat()) + mRandom.nextFloat()));
        venue.setGeolong(String.valueOf(Float.valueOf(venue.getGeolong()) + mRandom.nextFloat()));
        Beenhere beenhere = new Beenhere();
        beenhere.setMe(mRandom.nextBoolean());
        venue.getStats().setBeenhere(beenhere);

        return venue;
    }

    public static Group createRandomVenueGroups(String id) {
        Group group = new Group();
        group.setType(id);
        group.add(FoursquaredTest.createVenueGroup("A"));
        group.add(FoursquaredTest.createVenueGroup("B"));
        group.add(FoursquaredTest.createVenueGroup("C"));
        group.add(FoursquaredTest.createVenueGroup("D"));
        group.add(FoursquaredTest.createVenueGroup("E"));
        return group;
    }

    public static Tip createTip() {
        Tip tip = new Tip();
        tip.setCreated("Mon, 12 May 09 06:15:00 GMT");
        tip.setVenue(createRandomVenue("Random"));

        tip.setId("4272");
        tip.setText( //
                "Play games here - both on their games night every Wed but the "
                        + " &quot;advanced&quot;"
                        + " tip is they have a phenomenal library of games you can request"
                        + " to borrow - a serious gamer&apos;s library");
        tip.setDistance("0.1m");

        User user = new User();
        user.setId("1058");
        user.setFirstname("Shannon");
        user.setLastname("Clark");
        user.setPhoto("1058_1237850027.jpg");
        tip.setUser(user);

        return tip;
    }

    public static List<Tip> createTips() {
        List<Tip> tips = new ArrayList<Tip>();
        tips.add(createTip());
        tips.add(createTip());
        tips.add(createTip());
        tips.add(createTip());
        tips.add(createTip());
        tips.add(createTip());
        tips.add(createTip());
        tips.add(createTip());
        tips.add(createTip());
        tips.add(createTip());
        tips.add(createTip());
        tips.add(createTip());
        tips.add(createTip());
        tips.add(createTip());
        return tips;
    }

    public static Venue createVenue(String id) {
        Venue venue = new Venue();
        if (id == null) {
            venue.setId("44794");
            venue.setName("Bobby's Place");
        } else {
            venue.setId(id);
            venue.setName("Bobby's Place " + id);
        }
        venue.setAddress("123 Fake St.");
        venue.setCity("San Francisco");
        venue.setState("CA");
        venue.setZip("94117");
        venue.setCrossstreet("Imaginary");
        venue.setDistance("0.1m");
        venue.setGeolat("37.7722");
        venue.setGeolong("-122.437");
        venue.setStats(new Stats());
        return venue;
    }

    public static Group createVenueGroup(String type) {
        Group tlg = new Group();
        tlg.setType(type);
        tlg.add(createRandomVenue(type + " 1"));
        tlg.add(createRandomVenue(type + " 2"));
        tlg.add(createRandomVenue(type + " 3"));
        tlg.add(createRandomVenue(type + " 4"));
        tlg.add(createRandomVenue(type + " 5"));
        tlg.add(createRandomVenue(type + " 6"));
        tlg.add(createRandomVenue(type + " 7"));
        tlg.add(createRandomVenue(type + " 8"));
        tlg.add(createRandomVenue(type + " 9"));
        tlg.add(createRandomVenue(type + " 10"));
        return tlg;
    }
}
