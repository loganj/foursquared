/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.util;


/**
 * Conversions between different measurement systems.
 * 
 * @date March 18, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class MeasurementSystems {
    public static final String METRIC = "Metric";
    public static final String IMPERIAL = "Imperial";
    
    private static final float YARDS_PER_METER = 1.0936133f;
    
    
    public static float metersToYards(float meters) {
        return meters * YARDS_PER_METER;
    }
}
