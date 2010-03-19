/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.UserUtils;

import android.app.Activity;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
        if (!((Foursquared) getApplication()).isReady()) {
            if (DEBUG) Log.d(TAG, "Not ready for user.");
            redirectToLoginActivity();
            return;
        }

        if (DEBUG) Log.d(TAG, "Setting up main activity layout.");
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main_activity);
        initTabHost();
        
        // Only show the changelog if this isn't the first run of the app.
        // Otherwise the changelog will be a weird distraction to first-
        // time users.
        //showChangelogActivity();
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
        
        // We may want to show the friends tab first, or the places tab first, depending on
        // the user preferences.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 
        // We can add more tabs here eventually, but if "Friends" isn't the startup tab, then
        // we are left with "Places" being the startup tab instead.
        String[] startupTabValues = getResources().getStringArray(R.array.startup_tabs_values);
        String startupTab = settings.getString(
                Preferences.PREFERENCE_STARTUP_TAB, startupTabValues[0]);
        if (startupTab.equals(startupTabValues[0])) {
            addTabFriends();
            addTabPlaces(false);
        } else {
            addTabPlaces(true);
            addTabFriends();
        } 
        
        
        // 1.5 can't display tabs within tabs, so we won't have the 'me' tab for
        // 1.5 users. They can access the 'me' page through the context menu.
        int sdk = new Integer(Build.VERSION.SDK).intValue();
        if (sdk > 3) {
            // 'Me' tab, just shows our own info. At this point we should have a
            // stored user id, and a user gender to control the image which is
            // displayed on the tab.
            String userId = ((Foursquared) getApplication()).getUserId();
            String userGender = ((Foursquared) getApplication()).getUserGender();
            
            Intent intentTabMe = new Intent(this, UserDetailsActivity.class);
            intentTabMe.putExtra(UserDetailsActivity.EXTRA_USER_ID, userId == null ? "unknown"
                    : userId);
            mTabHost.addTab(mTabHost.newTabSpec("me") //
                    .setIndicator(getString(R.string.main_activity_tab_title_me),
                            getResources().getDrawable(
                                    UserUtils.getDrawableForMeTabByGender(
                                            userGender))) // the
                                                          // tab
                                                          // icon
                    .setContent(intentTabMe) // The contained activity
                    );
        } 
        
        mTabHost.setCurrentTab(0);
    }
    
    private void addTabFriends() {
         // Friends tab
        mTabHost.addTab(mTabHost.newTabSpec("friends") //
                .setIndicator(getString(R.string.checkins_label),
                        getResources().getDrawable(R.drawable.friends_tab)) // the
                                                                            // tab
                // icon
                .setContent(new Intent(this, FriendsActivity.class)) // The
                                                                     // contained
                                                                     // activity
                );
    }
    
    private void addTabPlaces(boolean delayLocationFetch) {
        // Places tab
        Intent intent = new Intent(this, NearbyVenuesActivity.class);
        if (delayLocationFetch) {
            intent.putExtra(NearbyVenuesActivity.INTENT_EXTRA_STARTUP_GEOLOC_DELAY, 4000L);
        }
        mTabHost.addTab(mTabHost.newTabSpec("places") //
                .setIndicator(getString(R.string.nearby_label),
                        getResources().getDrawable(R.drawable.places_tab)) // the
                                                                           // tab
                                                                           // icon
                .setContent(intent) // The
                                    // contained
                                    // activity
                );
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
    
    private void showChangelogActivity() {
        Foursquared foursquared = (Foursquared) getApplication();
        
        String lastSeenChangelogVersion = foursquared.getLastSeenChangelogVersion();
        if (TextUtils.isEmpty(lastSeenChangelogVersion) || 
            lastSeenChangelogVersion.compareTo(foursquared.getVersion()) < 0) {
            
            // Save the fact that we're going to show the changelog activity.
            foursquared.storeLastSeenChangelogVersion(foursquared.getVersion());

            // Also show only if this is NOT the first run of the app. We will
            // just skip showing the changelog for first-time installs - the
            // changelog would have no meaning for these users and be annoying
            // for them.
            if (!foursquared.getIsFirstRun()) {
                Intent intent = new Intent(this, ChangelogActivity.class);
                startActivity(intent);
            }
        } else { 
            if (DEBUG) Log.d(TAG, "Changelog already shown for current version, ignoring.");
        }
    }
}
