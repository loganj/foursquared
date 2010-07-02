/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquare.util;


/**
 * This is not ideal.
 * 
 * @date July 1, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class IconUtils {
    
    private static IconUtils mInstance;
    private boolean mRequestHighDensityIcons;
    
    private IconUtils() {
        mRequestHighDensityIcons = false;
    }
    
    public static IconUtils get() {
        if (mInstance == null) {
            mInstance = new IconUtils();
        }
        return mInstance;
    }

    public boolean getRequestHighDensityIcons() {
        return mRequestHighDensityIcons;
    }
    
    public void setRequestHighDensityIcons(boolean requestHighDensityIcons) {
        mRequestHighDensityIcons = requestHighDensityIcons;
    }
}