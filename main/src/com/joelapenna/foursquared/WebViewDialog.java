/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;


/**
 * Renders the result of a checkin using a CheckinResult object. This is called 
 * from CheckinExecuteActivity. It would be nicer to put this in another activity,
 * but right now the CheckinResult is quite large and would require a good amount
 * of work to add serializers for all its inner classes. This wouldn't be a huge
 * problem, but maintaining it as the classes evolve could more trouble than it's
 * worth.
 * 
 * The only way the user can dismiss this dialog is by hitting the 'back' key.
 * CheckingExecuteActivity depends on this so it knows when to finish() itself. 
 * 
 * @date March 3, 2010.
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 *
 */
public class WebViewDialog extends Dialog
{
    private WebView mWebView;
    private String mTitle;
    private String mContent;


    public WebViewDialog(Context context, String title, String content) { 
        super(context, R.style.ThemeCustomDlgBase_ThemeCustomDlg); 
        mTitle = title;
        mContent = content;
    } 

    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
    
        setContentView(R.layout.webview_dialog);
        setTitle(mTitle);
        
        mWebView = (WebView)findViewById(R.id.webView);
        mWebView.loadDataWithBaseURL("--", mContent, "text/html", "utf-8", "");
        
        LinearLayout llMain = (LinearLayout)findViewById(R.id.llMain);
        inflateDialog(llMain);
    }
    
    /**
     * Force-inflates a dialog main linear-layout to take max available screen space even though
     * contents might not occupy full screen size.
     */
    public static void inflateDialog(LinearLayout layout) {
        WindowManager wm = (WindowManager) layout.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        layout.setMinimumWidth(display.getWidth() - 30);
        layout.setMinimumHeight(display.getHeight() - 40);
    }
}
    


