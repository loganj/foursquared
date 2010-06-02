/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.util;

import com.joelapenna.foursquared.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

/**
 * Acts as an interface to the TabSpec class for setting the content view. The
 * level 3 SDK doesn't support setting a View for the content sections of the
 * tab, so we can only use the big native tab style. The level 4 SDK and up
 * support specifying a custom view for the tab.
 * 
 * @date March 9, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */ 
public abstract class TabsUtil {

    public static void setTabIndicator(TabSpec spec, String title, Drawable drawable, View view) {
        int sdk = new Integer(Build.VERSION.SDK).intValue();
        if (sdk < 4) {
            TabsUtil3.setTabIndicator(spec, title, drawable);
        } else {
            TabsUtil4.setTabIndicator(spec, view);
        }
    }
    
    private static TabHost.TabSpec addNativeLookingTab(Context context, final TabHost tabHost, String specName, 
            String label, int iconId) {
        View view = LayoutInflater.from(context).inflate(R.layout.fake_native_tab, null);
        ImageView iv = (ImageView) view.findViewById(R.id.fakeNativeTabImageView);
        iv.setImageResource(iconId);
        TextView tv = (TextView) view.findViewById(R.id.fakeNativeTabTextView);
        tv.setText(label);
        
        int sdk = new Integer(Build.VERSION.SDK).intValue();
        if (sdk > 3) {
            TabsUtil4.addNativeLookingTab(context, tabHost, view);
        }
        
        TabHost.TabSpec spec = tabHost.newTabSpec(specName);
        TabsUtil.setTabIndicator(spec, label, context.getResources().getDrawable(iconId), view);
        return spec;
    }
    
    public static void addNativeLookingTab(Context context, final TabHost tabHost, String specName, 
            String label, int iconId, int layout) {
        TabHost.TabSpec spec = addNativeLookingTab(context, tabHost, specName, label, iconId);
        
        spec.setContent(layout);
        tabHost.addTab(spec);
    }
    
    public static void addNativeLookingTab(Context context, final TabHost tabHost, String specName, 
            String label, int iconId, Intent intent) {
        TabHost.TabSpec spec = addNativeLookingTab(context, tabHost, specName, label, iconId);
        
        spec.setContent(intent);
        tabHost.addTab(spec);
    }
}
