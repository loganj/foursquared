/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.util;

import com.joelapenna.foursquared.R;

import android.content.Context;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
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
public class TabsUtil4 {

    private TabsUtil4() {
    }

    public static void setTabIndicator(TabSpec spec, View view) {
        spec.setIndicator(view);
    }

    public static int getTabCount(TabHost tabHost) {
        return tabHost.getTabWidget().getTabCount();
    }

    public static void addNativeLookingTab(Context context, final TabHost tabHost, View tabView) {

        // Since we're using a custom view, we need to manually set the colors of the focused
        // and non-focused tab textviews as the user selects different tabs.
        int tabIndex = tabHost.getTabWidget().getTabCount();
        setNativeLookingTabTextColor(tabView, tabIndex, 0);
            
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String arg0) {
                int tabSelected = tabHost.getCurrentTab();
                for (int i = 0, m = TabsUtil4.getTabCount(tabHost); i < m; i++) {
                    View view = tabHost.getTabWidget().getChildAt(i);
                    setNativeLookingTabTextColor(view, i, tabSelected);
                }
            }
        });
    }
    
    private static void setNativeLookingTabTextColor(View view, int ourIndex, int selectedIndex) {
        TextView tv = (TextView) view.findViewById(R.id.fakeNativeTabTextView);
        if (ourIndex == selectedIndex) {
            tv.setTextColor(view.getContext().getResources().getColor(R.color.dgrey_start));
        } else {
            tv.setTextColor(view.getContext().getResources().getColor(R.color.white));
        }
    }
}
