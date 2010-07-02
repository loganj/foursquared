/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import android.app.Activity;
import android.content.Intent;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");
        setContentView(R.layout.add_friends_activity);

        Button btnAddFriendsByAddressBook = (Button) findViewById(R.id.findFriendsByAddressBook);
        btnAddFriendsByAddressBook.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddFriendsActivity.this,
                        AddFriendsByUserInputActivity.class);
                intent.putExtra(AddFriendsByUserInputActivity.INPUT_TYPE,
                        AddFriendsByUserInputActivity.INPUT_TYPE_ADDRESSBOOK);
                startActivity(intent);
            }
        });

        Button btnAddFriendsByFacebook = (Button) findViewById(R.id.findFriendsByFacebook);
        btnAddFriendsByFacebook.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddFriendsActivity.this,
                        AddFriendsByUserInputActivity.class);
                intent.putExtra(AddFriendsByUserInputActivity.INPUT_TYPE,
                        AddFriendsByUserInputActivity.INPUT_TYPE_FACEBOOK);
                startActivity(intent);
            }
        });
        
        Button btnAddFriendsByTwitter = (Button) findViewById(R.id.findFriendsByTwitter);
        btnAddFriendsByTwitter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddFriendsActivity.this,
                        AddFriendsByUserInputActivity.class);
                intent.putExtra(AddFriendsByUserInputActivity.INPUT_TYPE,
                        AddFriendsByUserInputActivity.INPUT_TYPE_TWITTERNAME);
                startActivity(intent);
            }
        });

        Button btnAddFriendsByName = (Button) findViewById(R.id.findFriendsByNameOrPhoneNumber);
        btnAddFriendsByName.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddFriendsActivity.this,
                        AddFriendsByUserInputActivity.class);
                intent.putExtra(AddFriendsByUserInputActivity.INPUT_TYPE,
                        AddFriendsByUserInputActivity.INPUT_TYPE_NAME_OR_PHONE);
                startActivity(intent);
            }
        });
        
        Button btnInviteFriends = (Button) findViewById(R.id.findFriendsInvite);
        btnInviteFriends.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddFriendsActivity.this,
                        AddFriendsByUserInputActivity.class);
                intent.putExtra(AddFriendsByUserInputActivity.INPUT_TYPE,
                        AddFriendsByUserInputActivity.INPUT_TYPE_ADDRESSBOOK_INVITE);
                startActivity(intent);
            }
        });
    }
}
