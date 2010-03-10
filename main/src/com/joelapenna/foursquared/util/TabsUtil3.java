/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.util;

import android.graphics.drawable.Drawable;
import android.widget.TabHost.TabSpec;

/**
 * Acts as an interface to the TabSpec class for setting the content view.
 * The level 3 SDK doesn't support setting a View for the content sections
 * of the tab, so we can only use the big native tab style. The level 4
 * SDK and up support specifying a custom view for the tab.
 * 
 * @date March 9, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public class TabsUtil3 {

    private TabsUtil3() {
    }
    
    public static void setTabIndicator(TabSpec spec, String title, Drawable drawable) {
        if (drawable != null) {
            spec.setIndicator(title, drawable);
        } else {
            spec.setIndicator(title);
        }
    }
}
