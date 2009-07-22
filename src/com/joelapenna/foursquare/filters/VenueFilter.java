/**
 * Copyright 2009 Joe LaPenna
 */
package com.joelapenna.foursquare.filters;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.classic.Venue;

import java.util.ArrayList;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 *
 */
@Deprecated
public class VenueFilter {

    public static Group filter(Group groups, Venue venue) {
        Group filteredGroup = new Group();
        filteredGroup.setType(groups.getType());

        String venueId = venue.getVenueid();

        int groupCount = groups.size();
        for (int groupsIndex = 0; groupsIndex < groupCount; groupsIndex++) {
            Group group = (Group)groups.get(groupsIndex);
            filterGroupByVenueid(group, venueId);
            if (group.size() > 0) {
                filteredGroup.add(group);
            }
        }
        return filteredGroup;
    }

    private static void filterGroupByVenueid(Group group, String venueid) {
        ArrayList<VenueFilterable> venueFilterables = new ArrayList<VenueFilterable>();
        int itemCount = group.size();
        for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
            VenueFilterable item = (VenueFilterable)group.get(itemIndex);
            if (venueid.equals(item.getVenueid())) {
                venueFilterables.add(item);
            }
        }
        group.clear();
        group.addAll(venueFilterables);
    }
}
