/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.util.RemoteResourceManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

/**
 * Displays information on a user. If the user is the logged-in user, we can show
 * our checkin history. If viewing a stranger, we can show info and friends. 
 * Should look like this:
 * 
 * Self
 * History | Friends
 * 
 * Stranger 
 * Info | Friends
 * 
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class UserDetailsActivity extends Activity {
    private static final String TAG = "UserDetailsActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String EXTRA_USER_PARCEL = "com.joelapenna.foursquared.UserParcel";
    public static final String EXTRA_USER_ID = "com.joelapenna.foursquared.UserId";
    
    /** The user object we are rendering. */
    private User mUser;
    
    private ImageView mImageViewPhoto;
    private TextView mTextViewName;
    private TextView mTextViewNumMayorships;
    private TextView mTextViewNumBadges;
    

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.user_details_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));
        
        if (getIntent().getExtras().containsKey(EXTRA_USER_PARCEL)) {
            mUser = getIntent().getExtras().getParcelable(EXTRA_USER_PARCEL);
        }
        else if (getIntent().getExtras().containsKey(EXTRA_USER_ID)) {
            
        }
        
        ensureUi();
    }
    
    private void ensureUi() {
        RemoteResourceManager rrm = ((Foursquared)getApplication()).getRemoteResourceManager();
        
        mImageViewPhoto = (ImageView)findViewById(R.id.userDetailsActivityPhoto);
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(
                rrm.getInputStream(Uri.parse(mUser.getPhoto())));
            mImageViewPhoto.setImageBitmap(bitmap);
        } catch (IOException e) {
            if (Foursquare.MALE.equals(mUser.getGender())) {
                mImageViewPhoto.setImageResource(R.drawable.blank_boy);
            } else {
                mImageViewPhoto.setImageResource(R.drawable.blank_girl);
            }
        }
        
        mTextViewName = (TextView)findViewById(R.id.userDetailsActivityName);
        mTextViewName.setText(mUser.getFirstname());

        mTextViewNumMayorships = (TextView)findViewById(R.id.userDetailsActivityNumMayorships);
        //mTextViewNumMayorships.setText("12");
//
        mTextViewNumBadges = (TextView)findViewById(R.id.userDetailsActivityNumBadges);
        //mTextViewNumBadges.setText("" + mUser.getBadges().size());
//
    }
}
