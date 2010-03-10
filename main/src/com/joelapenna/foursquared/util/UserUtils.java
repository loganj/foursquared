/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.Foursquared;
import com.joelapenna.foursquared.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.util.Observable;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class UserUtils {

    public static void ensureUserPhoto(final Context context, final User user, final boolean DEBUG,
            final String TAG) {
        Activity activity = ((Activity) context);
        final ImageView photo = (ImageView) activity.findViewById(R.id.photo);
        if (user.getPhoto() == null) {
            photo.setImageResource(R.drawable.blank_boy);
            return;
        }
        final Uri photoUri = Uri.parse(user.getPhoto());
        if (photoUri != null) {
            RemoteResourceManager userPhotosManager = ((Foursquared) activity.getApplication())
                    .getRemoteResourceManager();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(userPhotosManager
                        .getInputStream(photoUri));
                photo.setImageBitmap(bitmap);
            } catch (IOException e) {
                if (DEBUG) Log.d(TAG, "photo not already retrieved, requesting: " + photoUri);
                userPhotosManager.addObserver(new RemoteResourceManager.ResourceRequestObserver(
                        photoUri) {
                    @Override
                    public void requestReceived(Observable observable, Uri uri) {
                        observable.deleteObserver(this);
                        updateUserPhoto(context, photo, uri, user, DEBUG, TAG);
                    }
                });
                userPhotosManager.request(photoUri);
            }
        }
    }

    private static void updateUserPhoto(Context context, final ImageView photo, final Uri uri,
            final User user, final boolean DEBUG, final String TAG) {
        final Activity activity = ((Activity) context);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (DEBUG) Log.d(TAG, "Loading user photo: " + uri);
                    RemoteResourceManager userPhotosManager = ((Foursquared) activity
                            .getApplication()).getRemoteResourceManager();
                    Bitmap bitmap = BitmapFactory.decodeStream(userPhotosManager
                            .getInputStream(uri));
                    photo.setImageBitmap(bitmap);
                    if (DEBUG) Log.d(TAG, "Loaded user photo: " + uri);
                } catch (IOException e) {
                    if (DEBUG) Log.d(TAG, "Unable to load user photo: " + uri);
                    if (Foursquare.MALE.equals(user.getGender())) {
                        photo.setImageResource(R.drawable.blank_boy);
                    } else {
                        photo.setImageResource(R.drawable.blank_girl);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Ummm............", e);
                }
            }
        });
    }
    
    public static boolean isFriend(User user) {
        if (user == null) {
            return false;
        } else if (TextUtils.isEmpty(user.getFriendstatus())) {
            return false;
        } else if (user.getFriendstatus().equals("friend")) {
            return true;
        } else {
            return false;
        }
    }
}
