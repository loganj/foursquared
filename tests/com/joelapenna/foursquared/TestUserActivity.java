/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Badge;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.util.RemoteResourceManager;
import com.joelapenna.foursquared.widget.BadgeWithIconListAdapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class TestUserActivity extends Activity {
    private static final String TAG = "TestUserActivity";
    private static final boolean DEBUG = Foursquared.DEBUG;

    private RemoteResourceManager mUserPhotoManager = new RemoteResourceManager("user_photo");
    private RemoteResourceManager mBadgeIconManager = new RemoteResourceManager("badges");

    private GridView mBadgesGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);

        User user = new User();
        user.setPhoto("http://playfoursquare.com/userpix/1818_1239601037.jpg");

        ensureUserPhoto(user);

        mBadgesGrid = (GridView)findViewById(R.id.badgesGrid);
        Group badges = new Group();
        badges.setType("bages");
        Badge badge;

        badge = new Badge();
        badge.setName("Newbie");
        badge.setIcon("http://playfoursquare.com/images/badges/newbie_on.png");

        badges.add(badge);

        badge = new Badge();
        badge.setName("Adventurer");
        badge.setIcon("http://playfoursquare.com/images/badges/adventurer_on.png");

        badges.add(badge);

        badge = new Badge();
        badge.setName("Explorer");
        badge.setIcon("http://playfoursquare.com/images/badges/explorer_on.png");

        badges.add(badge);

        badge = new Badge();
        badge.setName("Superstar");
        badge.setIcon("http://playfoursquare.com/images/badges/superstar_on.png");

        badges.add(badge);

        badge = new Badge();
        badge.setName("Bender");
        badge.setIcon("http://playfoursquare.com/images/badges/bender_on.png");
        badges.add(badge);

        badge = new Badge();
        badge.setName("Crunked");
        badge.setIcon("http://playfoursquare.com/images/badges/crunked_on.png");

        badges.add(badge);

        badge = new Badge();
        badge.setName("Local");
        badge.setIcon("http://playfoursquare.com/images/badges/local_on.png");

        badges.add(badge);

        badge = new Badge();
        badge.setName("Superuser");
        badge.setIcon("http://playfoursquare.com/images/badges/superuser_on.png");

        badges.add(badge);

        mBadgesGrid.setAdapter(new BadgeWithIconListAdapter(this, badges, mBadgeIconManager));
    }

    private void ensureUserPhoto(User user) {
        if (user.getPhoto() == null) {
            return;
        }
        Uri photo = Uri.parse(user.getPhoto());
        if (photo != null) {
            if (!mUserPhotoManager.getFile(photo).exists()) {
                mUserPhotoManager.addObserver(new Observer() {
                    @Override
                    public void update(Observable observable, final Object data) {

                    }
                });
                mUserPhotoManager.request(photo);
            } else {
                setPhotoImageView(photo);
            }
        }
    }

    private void setPhotoImageView(Uri photo) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(//
                    mUserPhotoManager.getInputStream(photo));
            ((ImageView)findViewById(R.id.photo)).setImageBitmap(bitmap);
        } catch (IOException e) {
            if (DEBUG) Log.d(TAG, "Could not load bitmap. we don't have it yet.", e);
        }
    }
}
