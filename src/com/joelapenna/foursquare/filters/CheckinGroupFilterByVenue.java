/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.filters;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;

import java.util.ArrayList;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class CheckinGroupFilterByVenue {
    public static final String TAG = "CheckinGroupFilterByVenue";
    public static final boolean DEBUG = Foursquare.DEBUG;

    public static Group filter(Group groups, Venue venue) {
        Group filteredGroup = new Group();
        filteredGroup.setType(groups.getType());

        String venueId = venue.getVenueid();

        int groupCount = groups.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group group = (Group)groups.get(groupsIndex);
            filterCheckinGroupByVenueid(venueId, group);
            if (group.size() > 0) {
                filteredGroup.add(group);
            }
        }
        return filteredGroup;
    }

    public static void filterCheckinGroupByVenueid(String venueid, Group group) {
        ArrayList<Checkin> venueCheckins = new ArrayList<Checkin>();
        int checkinCount = group.size();
        for (int checkinIndex = 0; checkinIndex < checkinCount; checkinIndex++) {
            Checkin checkin = (Checkin)group.get(checkinIndex);
            if (venueid.equals(checkin.getVenueid())) {
                venueCheckins.add(checkin);
            }
        }
        group.clear();
        group.addAll(venueCheckins);
    }
}
