/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
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
        checkin.setCheckinid(id);
        checkin.setDisplay("Test2 U. @ " + venue.getVenuename());
        checkin.setEmail("email@email.com");
        checkin.setFirstname("2");
        checkin.setGender("female");
        checkin.setLastname("User2");
        checkin.setMessage("message");
        checkin.setPhone("5554202");
        checkin.setPhoto("http://photourl.com");
        checkin.setRelativeTime("2 relative minutes");
        checkin.setShout("shout");
        checkin.setUserid("9923");
        checkin.setAddress(venue.getAddress());
        checkin.setCityid(venue.getCity());
        checkin.setCityName(venue.getCity());
        checkin.setCrossstreet(venue.getCrossstreet());
        checkin.setGeolat(venue.getGeolat());
        checkin.setGeolong(venue.getGeolong());
        checkin.setVenueid(venue.getVenueid());
        checkin.setVenuename(venue.getVenuename());

        return checkin;
    }

    public static Checkin createIncomingCheckin() {
        Checkin checkin = new Checkin();
        checkin.setCheckinid("67889");
        checkin.setMessage("OK! We&apos;ve got you @ The Page.");
        checkin.setStatus(true);
        checkin
                .setUrl("http://playfoursquare.com/incoming/breakdown?cid=67889&uid=9232&client=iphone");
        checkin.setUserid("9232");
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
        venue.setBeenhereMe(mRandom.nextBoolean());

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
        tip.setUserid("1058");
        tip.setFirstname("Shannon");
        tip.setLastname("Clark");
        tip.setPhoto("1058_1237850027.jpg");
        tip.setTipid("4272");
        tip
                .setText("Play games here - both on their games night every Wed but the &quot;advanced&quot; tip is they have a phenomenal library of games you can request to borrow - a serious gamer&apos;s library");
        tip.setVenueid("40450");
        tip.setVenuename("On the Corner");
        tip.setAddress("359 Divisadero St.");
        tip.setCrossstreet("Oak St");
        tip.setUrl("");
        tip.setDistance("0.1m");
        tip.setStatusText("From Shanon C&apos;s Top 12");
        tip.setRelativeDate("Mar 25, 2009");
        tip.setIstodoable(true);
        tip.setCreatorStatus("done");
        tip.setUserStatus("");
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
            venue.setVenueid("44794");
            venue.setVenuename("Bobby's Place");
        } else {
            venue.setVenueid(id);
            venue.setVenuename("Bobby's Place " + id);
        }
        venue.setAddress("123 Fake St.");
        venue.setCity("San Francisco");
        venue.setState("CA");
        venue.setZip("94117");
        venue.setCrossstreet("Imaginary");
        venue.setDistance("0.1m");
        venue.setNumCheckins("10");
        venue
                .setYelp("http://mobile.yelp.com/search?find_desc=Bobby%27s+place&amp;find_loc=San+Francisco%2CCA&amp;find_submit=Search");
        venue.setGeolat("37.7722");
        venue.setGeolong("-122.437");
        venue
                .setMap("http://maps.google.com/staticmap?zoom=15&amp;size=280x100&amp;markers=0,0&amp;maptype=mobile");
        venue.setMapurl("http://maps.google.com/maps?q=123+Fake+St.%2CSan+Francisco%2CCA%2C");
        venue.setNewVenue(true);
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
