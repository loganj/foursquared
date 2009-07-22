/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.filters;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.Venue;

import java.util.ArrayList;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class TipGroupFilterByVenue {
    public static final String TAG = "TipGroupFilterByVenue";
    public static final boolean DEBUG = Foursquare.DEBUG;

    public static Group filter(Group groups, Venue venue) {
        Group filteredGroup = new Group();
        filteredGroup.setType(groups.getType());

        String venueId = venue.getVenueid();

        int groupCount = groups.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group group = (Group)groups.get(groupsIndex);
            filterGroupByVenueid(venueId, group);
            if (group.size() > 0) {
                filteredGroup.add(group);
            }
        }
        return filteredGroup;
    }

    private static void filterGroupByVenueid(String venueid, Group group) {
        ArrayList<Tip> venueTips = new ArrayList<Tip>();
        int tipCount = group.size();
        for (int tipIndex = 0; tipIndex < tipCount; tipIndex++) {
            Tip tip = (Tip)group.get(tipIndex);
            if (venueid.equals(tip.getVenueid())) {
                venueTips.add(tip);
            }
        }
        group.clear();
        group.addAll(venueTips);
    }
}
