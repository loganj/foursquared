/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Presents the user with a list of different methods for adding foursquare
 * friends.
 * 
 * @date February 11, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public class AddFriendsActivity extends Activity {
    private static final String TAG = "AddFriendsActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private BroadcastReceiver mLoggedOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive: " + intent);
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");
        setContentView(R.layout.add_friends_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        Button btnAddFriendsByAddressBook = (Button) findViewById(R.id.findFriendsByAddressBook);
        btnAddFriendsByAddressBook.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(AddFriendsActivity.this,
                        AddFriendsByUserInputActivity.class);
                intent.putExtra(AddFriendsByUserInputActivity.INPUT_TYPE,
                        AddFriendsByUserInputActivity.INPUT_TYPE_ADDRESSBOOK);
                startActivity(intent);
            }
        });

        Button btnAddFriendsByTwitter = (Button) findViewById(R.id.findFriendsByTwitter);
        btnAddFriendsByTwitter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(AddFriendsActivity.this,
                        AddFriendsByUserInputActivity.class);
                intent.putExtra(AddFriendsByUserInputActivity.INPUT_TYPE,
                        AddFriendsByUserInputActivity.INPUT_TYPE_TWITTERNAME);
                startActivity(intent);
            }
        });

        Button btnAddFriendsByName = (Button) findViewById(R.id.findFriendsByName);
        btnAddFriendsByName.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(AddFriendsActivity.this,
                        AddFriendsByUserInputActivity.class);
                intent.putExtra(AddFriendsByUserInputActivity.INPUT_TYPE,
                        AddFriendsByUserInputActivity.INPUT_TYPE_USERNAMES);
                startActivity(intent);
            }
        });

        Button btnAddFriendsByPhoneNumber = (Button) findViewById(R.id.findFriendsByPhoneNumber);
        btnAddFriendsByPhoneNumber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(AddFriendsActivity.this,
                        AddFriendsByUserInputActivity.class);
                intent.putExtra(AddFriendsByUserInputActivity.INPUT_TYPE,
                        AddFriendsByUserInputActivity.INPUT_TYPE_PHONENUMBERS);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedOutReceiver);
    }
}
