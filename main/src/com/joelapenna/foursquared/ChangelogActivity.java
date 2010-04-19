/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;

/**
 * Shows a listing of what's changed between 
 * 
 * @date March 17, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class ChangelogActivity extends Activity {

    private static final String CHANGELOG_HTML_FILE = 
        "file:///android_asset/changelog-en.html";
    
    private WebView mWebViewChanges;
    
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changelog_activity);
        
        ensureUi();
    }
    
    private void ensureUi() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        
        LinearLayout llMain = (LinearLayout)findViewById(R.id.layoutMain);

        // We'll force the dialog to be a certain percentage height of the screen.
        mWebViewChanges = new WebView(this);
        mWebViewChanges.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                (int)Math.floor(display.getHeight() * 0.5)));
        
        mWebViewChanges.loadUrl(CHANGELOG_HTML_FILE); 
         
        llMain.addView(mWebViewChanges);
    }
}
