/**
 * Copyright 2008 Joe LaPenna
 */

package com.joelapenna.foursquared;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {
    static final String TAG = "PreferenceActivity";
    static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final int MENU_LOGIN = 0;

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate");

        this.addPreferencesFromResource(R.xml.preferences);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_LOGIN, Menu.NONE, R.string.login_label) //
                .setIcon(android.R.drawable.ic_menu_revert);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_LOGIN:
                mPrefs.edit().clear().commit();
                Foursquared.getFoursquare().setCredentials(null, null);
                Foursquared.getFoursquare().setOAuthToken(null, null);

                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
        }
        return false;
    }
}
