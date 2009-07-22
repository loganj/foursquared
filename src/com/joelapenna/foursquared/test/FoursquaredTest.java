/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.test;

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
    public static final String TAG = "FoursquaredTest";
    public static final boolean DEBUG = true;

    private static final Random mRandom = new Random();

    public static Venue createTestVenue(String id) {
        Venue venue = new Venue();
        if (id != null) {
            venue.setVenuename("Bobby's Place " + id);
        } else {
            venue.setVenuename("Bobby's Place");
        }
        venue.setVenueid("44794");
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

    public static Venue createRandomTestVenue(String id) {
        Venue venue = createTestVenue(id);

        venue.setGeolat(String.valueOf(Float.valueOf(venue.getGeolat()) + mRandom.nextFloat()));
        venue.setGeolong(String.valueOf(Float.valueOf(venue.getGeolong()) + mRandom.nextFloat()));
        venue.setBeenhereMe(mRandom.nextBoolean());

        return venue;
    }

    public static Tip createTestTip() {
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

    public static List<Tip> createTestTips() {
        List<Tip> tips = new ArrayList<Tip>();
        tips.add(createTestTip());
        tips.add(createTestTip());
        tips.add(createTestTip());
        tips.add(createTestTip());
        tips.add(createTestTip());
        tips.add(createTestTip());
        tips.add(createTestTip());
        tips.add(createTestTip());
        tips.add(createTestTip());
        tips.add(createTestTip());
        tips.add(createTestTip());
        tips.add(createTestTip());
        tips.add(createTestTip());
        tips.add(createTestTip());
        return tips;
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

    public static Group createVenueGroup(String type) {
        Group tlg = new Group();
        tlg.setType(type);
        tlg.add(createRandomTestVenue(type + " 1"));
        tlg.add(createRandomTestVenue(type + " 2"));
        tlg.add(createRandomTestVenue(type + " 3"));
        tlg.add(createRandomTestVenue(type + " 4"));
        tlg.add(createRandomTestVenue(type + " 5"));
        tlg.add(createRandomTestVenue(type + " 6"));
        tlg.add(createRandomTestVenue(type + " 7"));
        tlg.add(createRandomTestVenue(type + " 8"));
        tlg.add(createRandomTestVenue(type + " 9"));
        return tlg;
    }
}
