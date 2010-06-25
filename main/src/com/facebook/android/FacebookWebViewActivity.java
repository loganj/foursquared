/**
 * Copyright 2010 Mark Wyszomierski
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.android;

import com.joelapenna.foursquared.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This activity can be used to run a facebook url request through a webview.
 * The user must supply these intent extras:
 * <ul>
 * <li>INTENT_EXTRA_ACTION - string, which facebook action to perform, like
 * "login", or "stream.publish".</li>
 * <li>INTENT_EXTRA_KEY_APP_ID - string, facebook developer key.</li>
 * <li>INTENT_EXTRA_KEY_PERMISSIONS - string array, set of facebook permissions
 * you want to use.</li>
 * </ul>
 * or you can supply only INTENT_EXTRA_KEY_CLEAR_COOKIES to just have the
 * activity clear its stored cookies (you can also supply it in combination with
 * the above flags to clear cookies before trying to run a request too). If
 * you've already authenticated the user, you can optionally pass in the token
 * and expiration time as intent extras using:
 * <ul>
 * <li>INTENT_EXTRA_AUTHENTICATED_TOKEN</li>
 * <li>INTENT_EXTRA_AUTHENTICATED_EXPIRES</li>
 * </ul>
 * they will then be used in web requests. You should use
 * <code>startActivityForResult</code> to start the activity. When the activity
 * finishes, it will return status code RESULT_OK. You can then check the
 * returned intent data object for:
 * <ul>
 * <li>INTENT_RESULT_KEY_RESULT_STATUS - boolean, whether the request succeeded
 * or not.</li>
 * <li>INTENT_RESULT_KEY_SUPPLIED_ACTION - string, the action you supplied as an
 * intent extra echoed back as a convenience.</li>
 * <li>INTENT_RESULT_KEY_RESULT_BUNDLE - bundle, present if request succeeded,
 * will have all the returned parameters as supplied by the WebView operation.</li>
 * <li>INTENT_RESULT_KEY_ERROR - string, present if request failed.</li>
 * </ul>
 * If the user canceled this activity, the activity result code will be
 * RESULT_CANCELED and there will be no intent data returned. You need the
 * <code>android.permission.INTERNET</code> permission added to your manifest.
 * You need to add this activity definition to your manifest. You can prevent
 * this activity from restarting on rotation so the network operations are
 * preserved like so: <activity
 * android:name="com.facebook.android.FacebookWebViewActivity"
 * android:configChanges="orientation|keyboardHidden" />
 * 
 * @date June 14, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class FacebookWebViewActivity extends Activity {
    private static final String TAG = "FacebookWebViewActivity";

    public static final String INTENT_EXTRA_ACTION = "com.facebook.android.FacebookWebViewActivity.action";
    public static final String INTENT_EXTRA_KEY_APP_ID = "com.facebook.android.FacebookWebViewActivity.appid";
    public static final String INTENT_EXTRA_KEY_PERMISSIONS = "com.facebook.android.FacebookWebViewActivity.permissions";
    public static final String INTENT_EXTRA_AUTHENTICATED_TOKEN = "com.facebook.android.FacebookWebViewActivity.authenticated_token";
    public static final String INTENT_EXTRA_AUTHENTICATED_EXPIRES = "com.facebook.android.FacebookWebViewActivity.authenticated_expires";
    public static final String INTENT_EXTRA_KEY_CLEAR_COOKIES = "com.facebook.android.FacebookWebViewActivity.clear_cookies";
    public static final String INTENT_EXTRA_KEY_DEBUG = "com.facebook.android.FacebookWebViewActivity.debug";

    public static final String INTENT_RESULT_KEY_RESULT_STATUS = "result_status";
    public static final String INTENT_RESULT_KEY_SUPPLIED_ACTION = "supplied_action";
    public static final String INTENT_RESULT_KEY_RESULT_BUNDLE = "bundle";
    public static final String INTENT_RESULT_KEY_ERROR = "error";

    private static final String DISPLAY_STRING = "display=touch";
    private static final int FB_BLUE = 0xFF6D84B4;
    private static final int MARGIN = 4;
    private static final int PADDING = 2;

    private TextView mTitle;
    private WebView mWebView;
    private ProgressDialog mSpinner;

    private String mAction;
    private String mAppId;
    private String[] mPermissions;
    private boolean mDebug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        CookieSyncManager.createInstance(this);

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        mTitle = new TextView(this);
        mTitle.setText("Facebook");
        mTitle.setTextColor(Color.WHITE);
        mTitle.setTypeface(Typeface.DEFAULT_BOLD);
        mTitle.setBackgroundColor(FB_BLUE);
        mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
        mTitle.setCompoundDrawablePadding(MARGIN + PADDING);
        mTitle.setCompoundDrawablesWithIntrinsicBounds(this.getResources().getDrawable(
                R.drawable.facebook_icon), null, null, null);
        ll.addView(mTitle);

        mWebView = new WebView(this);
        mWebView.setLayoutParams(new LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        mWebView.setWebViewClient(new WebViewClientFacebook());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        ll.addView(mWebView);

        mSpinner = new ProgressDialog(this);
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading...");

        setContentView(ll, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            if (extras.containsKey(INTENT_EXTRA_KEY_DEBUG)) {
                mDebug = extras.getBoolean(INTENT_EXTRA_KEY_DEBUG);
            }

            if (extras.containsKey(INTENT_EXTRA_ACTION)) {

                if (extras.getBoolean(INTENT_EXTRA_KEY_CLEAR_COOKIES, false)) {
                    clearCookies();
                }

                if (extras.containsKey(INTENT_EXTRA_KEY_APP_ID)) {
                    if (extras.containsKey(INTENT_EXTRA_KEY_PERMISSIONS)) {
                        mAction = extras.getString(INTENT_EXTRA_ACTION);
                        mAppId = extras.getString(INTENT_EXTRA_KEY_APP_ID);
                        mPermissions = extras.getStringArray(INTENT_EXTRA_KEY_PERMISSIONS);

                        // If the user supplied a pre-authenticated info, use it
                        // here.
                        Facebook facebook = new Facebook();
                        if (extras.containsKey(INTENT_EXTRA_AUTHENTICATED_TOKEN)
                                && extras.containsKey(INTENT_EXTRA_AUTHENTICATED_EXPIRES)) {
                            facebook.setAccessToken(extras
                                    .getString(INTENT_EXTRA_AUTHENTICATED_TOKEN));
                            facebook.setAccessExpires(extras
                                    .getLong(INTENT_EXTRA_AUTHENTICATED_EXPIRES));
                            if (mDebug) {
                                Log.d(TAG, "onCreate(): authenticated token being used.");
                            }
                        }

                        // Generate the url based on the action.
                        String url = facebook.generateUrl(mAction, mAppId, mPermissions);
                        if (mDebug) {
                            Log.d(TAG, "onCreate(): action: " + mAction + ", appid: " + mAppId
                                    + ", permissions: "
                                    + (mPermissions == null ? "(null)" : mPermissions.toString()));
                            Log.d(TAG, "onCreate(): Loading url: " + url);
                        }

                        // Start the request finally.
                        mWebView.loadUrl(url);

                    } else {
                        Log.e(TAG, "Missing intent extra: INTENT_EXTRA_KEY_PERMISSIONS, finishing immediately.");
                        finish();
                    }
                } else {
                    Log.e(TAG, "Missing intent extra: INTENT_EXTRA_KEY_APP_ID, finishing immediately.");
                    finish();
                }
            } else if (extras.getBoolean(INTENT_EXTRA_KEY_CLEAR_COOKIES)) {
                clearCookies();
            } else {
                Log.e(TAG, "Missing intent extra: INTENT_EXTRA_ACTION or INTENT_EXTRA_KEY_CLEAR_COOKIES, finishing immediately.");
                finish();
            }
        } else {
            Log.e(TAG, "No intent extras supplied, finishing immediately.");
            finish();
        }
    }

    private void clearCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    @Override
    protected void onResume() {
        super.onResume();

        CookieSyncManager.getInstance().startSync();
    }

    @Override
    protected void onPause() {
        super.onPause();

        CookieSyncManager.getInstance().stopSync();
    }

    private class WebViewClientFacebook extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (mDebug) {
                Log.d(TAG, "WebViewClientFacebook:shouldOverrideUrlLoading(): " + url);
            }

            if (url.startsWith(Facebook.REDIRECT_URI)) {
                Bundle values = FacebookUtil.parseUrl(url);
                String error = values.getString("error_reason");

                Intent result = new Intent();
                result.putExtra(INTENT_RESULT_KEY_SUPPLIED_ACTION, mAction);
                if (error == null) {
                    CookieSyncManager.getInstance().sync();

                    result.putExtra(INTENT_RESULT_KEY_RESULT_STATUS, true);
                    result.putExtra(INTENT_RESULT_KEY_RESULT_BUNDLE, values);
                    FacebookWebViewActivity.this.setResult(Activity.RESULT_OK, result);

                } else {
                    result.putExtra(INTENT_RESULT_KEY_RESULT_STATUS, false);
                    result.putExtra(INTENT_RESULT_KEY_SUPPLIED_ACTION, mAction);
                    result.putExtra(INTENT_RESULT_KEY_ERROR, error);
                    FacebookWebViewActivity.this.setResult(Activity.RESULT_OK, result);
                }
                FacebookWebViewActivity.this.finish();
                return true;
            } else if (url.startsWith(Facebook.CANCEL_URI)) {
                FacebookWebViewActivity.this.setResult(Activity.RESULT_CANCELED);
                FacebookWebViewActivity.this.finish();
                return true;
            } else if (url.contains(DISPLAY_STRING)) {
                return false;
            }

            // Launch non-dialog URLs in a full browser.
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            if (mDebug) {
                Log.d(TAG, "WebViewClientFacebook:onReceivedError(): " + errorCode + ", "
                        + description + ", " + failingUrl);
            }

            Intent result = new Intent();
            result.putExtra(INTENT_RESULT_KEY_RESULT_STATUS, false);
            result.putExtra(INTENT_RESULT_KEY_SUPPLIED_ACTION, mAction);
            result.putExtra(INTENT_RESULT_KEY_ERROR, description + ", " + errorCode + ", "
                    + failingUrl);
            FacebookWebViewActivity.this.setResult(Activity.RESULT_OK, result);
            FacebookWebViewActivity.this.finish();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            if (mDebug) {
                Log.d(TAG, "WebViewClientFacebook:onPageStarted(): " + url);
            }

            mSpinner.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (mDebug) {
                Log.d(TAG, "WebViewClientFacebook:onPageFinished(): " + url);
            }

            String title = mWebView.getTitle();
            if (title != null && title.length() > 0) {
                mTitle.setText(title);
            }

            mSpinner.dismiss();
        }
    }
}
