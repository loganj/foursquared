/**
 * Copyright 2009 Joe LaPenna
 */
package com.joelapenna.foursquared;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 *
 */
public class VenueCheckinActivity extends Activity {

    private WebView mWebView;

    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_checkin_activity);

        setupWebView();
    }

    /**
     *
     */
    private void setupWebView() {
        mWebView = (WebView)findViewById(R.id.webView);
        mWebView.loadUrl("http://playfoursquare.com/incoming/breakdown?cid=67889&uid=9232&client=iphone");

    }
}
