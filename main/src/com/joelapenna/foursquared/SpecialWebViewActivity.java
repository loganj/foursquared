/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.http.AbstractHttpApi;
import com.joelapenna.foursquared.util.NotificationsUtil;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;

import java.io.IOException;


/**
 * Displays a special in a webview. Ideally we could use WebView.setHttpAuthUsernamePassword(),
 * but it is unfortunately not working. Instead we download the html content manually, then 
 * feed it to our webview. Not ideal and we should update this in the future.
 * 
 * @date April 4, 2010.
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 *
 */
public class SpecialWebViewActivity extends Activity
{
    private static final String TAG = "WebViewActivity";
    
    public static final String EXTRA_CREDENTIALS_USERNAME = Foursquared.PACKAGE_NAME
            + ".SpecialWebViewActivity.EXTRA_CREDENTIALS_USERNAME";

    public static final String EXTRA_CREDENTIALS_PASSWORD = Foursquared.PACKAGE_NAME
            + ".SpecialWebViewActivity.EXTRA_CREDENTIALS_PASSWORD";
    
    public static final String EXTRA_SPECIAL_ID = Foursquared.PACKAGE_NAME
            + ".SpecialWebViewActivity.EXTRA_SPECIAL_ID";
    
    private WebView mWebView;
    private StateHolder mStateHolder;


    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
    
        setContentView(R.layout.special_webview_activity);
        
        mWebView = (WebView)findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);

        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
            mStateHolder.setActivityForTask(this);
            
            if (mStateHolder.getIsRunningTask() == false) {
                mWebView.loadDataWithBaseURL("--", mStateHolder.getHtml(), "text/html", "utf-8", "");
            }
        } else {
            mStateHolder = new StateHolder();

            if (getIntent().getExtras() != null && 
                getIntent().getExtras().containsKey(EXTRA_CREDENTIALS_USERNAME) &&
                getIntent().getExtras().containsKey(EXTRA_CREDENTIALS_PASSWORD) &&
                getIntent().getExtras().containsKey(EXTRA_SPECIAL_ID)) 
            {
                String username  = getIntent().getExtras().getString(EXTRA_CREDENTIALS_USERNAME);
                String password  = getIntent().getExtras().getString(EXTRA_CREDENTIALS_PASSWORD);
                String specialid = getIntent().getExtras().getString(EXTRA_SPECIAL_ID);
                
                mStateHolder.startTask(this, username, password, specialid);
            } else {
                Log.e(TAG, TAG + " intent missing required extras parameters.");
                finish();
            }
        }
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        mStateHolder.setActivityForTask(null);
        return mStateHolder;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void onTaskComplete(String html, Exception ex) {
        mStateHolder.setIsRunningTask(false);
        
        if (html != null) {
            mStateHolder.setHtml(html);
            mWebView.loadDataWithBaseURL("--", mStateHolder.getHtml(), "text/html", "utf-8", "");
        } else {
            NotificationsUtil.ToastReasonForFailure(this, ex);
        }
    }
    
    private static class SpecialTask extends AsyncTask<String, Void, String> {
        private SpecialWebViewActivity mActivity;
        private Exception mReason;
        

        public SpecialTask(SpecialWebViewActivity activity) {
            mActivity = activity;
        }

        public void setActivity(SpecialWebViewActivity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
        }
        
        @Override
        protected String doInBackground(String... params) {
            String html = null;
            try {
                String username  = params[0];
                String password  = params[1];
                String specialid = params[2];

                StringBuilder sbUrl = new StringBuilder(128);
                sbUrl.append("https://api.foursquare.com/iphone/special?sid=");
                sbUrl.append(specialid);
                
                AuthScope authScope = new AuthScope("api.foursquare.com", 80);
                DefaultHttpClient httpClient = AbstractHttpApi.createHttpClient();
                httpClient.getCredentialsProvider().setCredentials(authScope,
                    new UsernamePasswordCredentials(username, password));
                httpClient.addRequestInterceptor(preemptiveAuth, 0);
                    
                    
                HttpGet httpGet = new HttpGet(sbUrl.toString());
                try {
                    HttpResponse response = httpClient.execute(httpGet);
                    String responseText = EntityUtils.toString(response.getEntity());
                    
                    html = responseText.replace("('/img", "('http://www.foursquare.com/img");
                } catch (Exception e) {
                    mReason = e;
                } 
            } catch (Exception e) {
                mReason = e;
            }
            return html;
        }

        @Override
        protected void onPostExecute(String html) {
            if (mActivity != null) {
                mActivity.onTaskComplete(html, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onTaskComplete(null, new Exception("Special task cancelled."));
            }
        }
        
        private HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {

            @Override
            public void process(final HttpRequest request, final HttpContext context)
                    throws HttpException, IOException {

                AuthState authState = (AuthState)context.getAttribute(ClientContext.TARGET_AUTH_STATE);
                CredentialsProvider credsProvider = (CredentialsProvider)context
                        .getAttribute(ClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost)context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

                // If not auth scheme has been initialized yet
                if (authState.getAuthScheme() == null) {
                    AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
                    org.apache.http.auth.Credentials creds = credsProvider.getCredentials(authScope);
                    if (creds != null) {
                        authState.setAuthScheme(new BasicScheme());
                        authState.setCredentials(creds);
                    }
                }
            }
        };
    }
    
    private static class StateHolder {
        private String mHtml;
        private boolean mIsRunningTask;
        private SpecialTask mTask;
        
        
        public StateHolder() {
            mIsRunningTask = false;
        }
        
        public void setHtml(String html) { 
            mHtml = html;
        }
        
        public String getHtml() {
            return mHtml;
        }
        
        public void startTask(SpecialWebViewActivity activity,
                              String username,
                              String password,
                              String specialid) 
        {
            mIsRunningTask = true;
            mTask = new SpecialTask(activity);
            mTask.execute(username, password, specialid);
        }

        public void setActivityForTask(SpecialWebViewActivity activity) {
            if (mTask != null) {
                mTask.setActivity(activity);
            }
        }
        
        public void setIsRunningTask(boolean isRunningTipTask) {
            mIsRunningTask = isRunningTipTask;
        }
        
        public boolean getIsRunningTask() {
            return mIsRunningTask;
        }
    }
}
