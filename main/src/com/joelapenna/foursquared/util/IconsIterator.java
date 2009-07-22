/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import com.joelapenna.foursquared.R;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class IconsIterator {

    private static final Integer[] MAP_NEW_ICONS = {
        R.drawable.map_marker_blue, //
        R.drawable.map_marker_purple, //
    };

    private static final Integer[] MAP_BEEN_THERE_ICONS = {
        R.drawable.map_marker_blue_muted, //
        R.drawable.map_marker_purple_muted, //
    };

    private static final Integer[] ALL_ICONS = {
            R.drawable.reddot, //
            R.drawable.bluedot, //
            R.drawable.greendot, //
            R.drawable.pinkdot, //
            R.drawable.ltbluedot, //
            R.drawable.yellowdot,
    };

    public final InfiniteIterator<Integer> allIcons = new InfiniteIterator<Integer>(ALL_ICONS);
    public final InfiniteIterator<Integer> icons = new InfiniteIterator<Integer>(MAP_NEW_ICONS);
    public final InfiniteIterator<Integer> beenthereIcons = new InfiniteIterator<Integer>(
            MAP_BEEN_THERE_ICONS);
}
