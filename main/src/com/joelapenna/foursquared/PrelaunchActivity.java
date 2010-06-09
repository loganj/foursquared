/**
 * Copyright 2010 Mark Wyszomierski
 */
package com.joelapenna.foursquared;

import com.joelapenna.foursquared.preferences.Preferences;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

/**
 * Used to show a prompt to the user before the app runs. Some new marketplace vendors have 
 * been asking this so they can prompt the user and tell them that the app uses internet 
 * connections, which is not unlimited usage under many plans. This really should be handled
 * by the install screen which shows other permissions used.
 * 
 * To enable this activity, just set it as the LAUNCHER in the manifest file. If the tag is
 * not added in the manifest file, this activity is never used.
 * 
 * You can modify these text items in strings.xml to modify the appearance of the activity:
 * <ul>
 *   <li>prelaunch_text</li>
 *   <li>prelaunch_button</li>
 *   <li>prelaunch_checkbox_dont_show_again</li>
 * </ul>
 * 
 * @date May 15, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class PrelaunchActivity extends Activity {
    public static final String TAG = "PrelaunchActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");
        setContentView(R.layout.prelaunch_activity);
        
        // If user doesn't want to be reminded anymore, just go to main activity.
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                Preferences.PREFERENCE_SHOW_PRELAUNCH_ACTIVITY, false) == false) {
            startMainActivity();
        } else {            
            ensureUi();
        }
    }

    private void ensureUi() {
        
        Button buttonOk = (Button) findViewById(R.id.btnOk);
        buttonOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMain(false);
            }
        });
    } 
     
    private void goToMain(boolean dontRemind) {
        // Don't show this startup screen anymore.
        CheckBox checkboxDontShowAgain = (CheckBox)findViewById(R.id.checkboxDontShowAgain);
        if (checkboxDontShowAgain.isChecked()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean(Preferences.PREFERENCE_SHOW_PRELAUNCH_ACTIVITY, false).commit();
        }
        
        startMainActivity();
    }
    
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        
        finish();
    }
}