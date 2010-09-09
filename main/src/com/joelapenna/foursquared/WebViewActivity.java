/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;


/**
 * @date August 2, 2010.
 * @author Mark Wyszomierski (markww@gmail.com).
 *
 */
public class WebViewActivity extends Activity {
    
    private static final String TAG = "WebViewActivity";
    
    public static final String INTENT_EXTRA_URL = Foursquared.PACKAGE_NAME
        + ".WebViewActivity.INTENT_EXTRA_URL";
    
    private WebView mWebView;
    

    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        mWebView = new WebView(this);
        mWebView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new EmbeddedWebViewClient());
        if (getIntent().getStringExtra(INTENT_EXTRA_URL) != null) {
            mWebView.loadUrl(getIntent().getStringExtra(INTENT_EXTRA_URL));   
        } else {
            Log.e(TAG, "Missing url in intent extras.");
            finish();
            return;
        }
        
        setContentView(mWebView);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private class EmbeddedWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            setProgressBarIndeterminateVisibility(false);
        }
    }
}
