/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.widget.TabHost;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class MainActivity extends TabActivity {
    public static final String TAG = "MainActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private TabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultKeyMode(Activity.DEFAULT_KEYS_SEARCH_LOCAL);

        // Don't start the main activity if we don't have credentials
        if (!((Foursquared)getApplication()).getFoursquare().hasCredentials()) {
            setVisible(false);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            startActivityForResult(intent, LoginActivity.ACTIVITY_REQUEST_LOGIN);
            return;
        }

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main_activity);
        initTabHost();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    private void initTabHost() {
        if (mTabHost != null) {
            throw new IllegalStateException("Trying to intialize already initializd TabHost");
        }

        Resources resources = getResources();

        mTabHost = getTabHost();

        // Places tab
        mTabHost.addTab(mTabHost.newTabSpec("places") //
                .setIndicator(resources.getString(R.string.nearby_label),
                        getResources().getDrawable(R.drawable.places_tab)) // the tab icon
                .setContent(new Intent(this, NearbyVenuesActivity.class)) // The contained activity
                );

        // Friends tab
        mTabHost.addTab(mTabHost.newTabSpec("friends") //
                .setIndicator(resources.getString(R.string.checkins_label),
                        getResources().getDrawable(R.drawable.friends_tab)) // the tab
                // icon
                .setContent(new Intent(this, FriendsActivity.class)) // The contained activity
                );
        mTabHost.setCurrentTab(0);
    }
}
