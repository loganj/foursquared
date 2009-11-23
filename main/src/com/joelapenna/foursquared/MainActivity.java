/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import android.app.Activity;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.TabHost;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class MainActivity extends TabActivity {
    public static final String TAG = "MainActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private TabHost mTabHost;

    private BroadcastReceiver mLoggedOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive: " + intent);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");
        setDefaultKeyMode(Activity.DEFAULT_KEYS_SEARCH_LOCAL);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        // Don't start the main activity if we don't have credentials
        Foursquared foursquared = (Foursquared)getApplication();
        if (!foursquared.getFoursquare().hasLoginAndPassword()) {
            if (DEBUG) Log.d(TAG, "No login and password.");
            redirectToLoginActivity();
        } else if (TextUtils.isEmpty(foursquared.getUserId())
                || TextUtils.isEmpty(foursquared.getUserCity().getId())) {
            if (DEBUG) Log.d(TAG, "Missing required preferences.");
            redirectToLoginActivity();
        }

        if (DEBUG) Log.d(TAG, "Setting up main activity layout.");
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main_activity);
        initTabHost();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedOutReceiver);
    }

    private void initTabHost() {
        if (mTabHost != null) {
            throw new IllegalStateException("Trying to intialize already initializd TabHost");
        }

        mTabHost = getTabHost();

        // Places tab
        mTabHost.addTab(mTabHost.newTabSpec("places") //
                .setIndicator(getString(R.string.nearby_label),
                        getResources().getDrawable(R.drawable.places_tab)) // the tab icon
                .setContent(new Intent(this, NearbyVenuesActivity.class)) // The contained activity
                );

        // Friends tab
        mTabHost.addTab(mTabHost.newTabSpec("friends") //
                .setIndicator(getString(R.string.checkins_label),
                        getResources().getDrawable(R.drawable.friends_tab)) // the tab
                // icon
                .setContent(new Intent(this, FriendsActivity.class)) // The contained activity
                );
        mTabHost.setCurrentTab(0);
    }

    private void redirectToLoginActivity() {
        setVisible(false);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
