package com.joelapenna.foursquared.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.util.CompatibilityHelp;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class MaybeContactView extends FrameLayout {

    final private static String TAG = "MaybeContactView";

    private static Constructor<?> badgeConstructor;
    private static Method badgeAssignContactUri;
    private static Method badgeSetExcludeMimes;
    private static Method badgeSetMode;
    private static int modeSmall;
    private static Method badgeSetLayoutParams;
    private static Method badgeSetScaleType;
    private static Method badgeSetImageResource;

    private ImageView photo;
    private ImageView quickContactBadge;

    static {
        if ( CompatibilityHelp.API_LEVEL_AT_LEAST_ECLAIR) {
            try {
                Class badge = Class.forName("android.widget.QuickContactBadge");
                badgeConstructor = badge.getConstructor(Context.class, AttributeSet.class, int.class);
                badgeAssignContactUri = badge.getMethod("assignContactUri", Uri.class);
                badgeSetExcludeMimes = badge.getMethod("setExcludeMimes", String[].class);
                badgeSetMode = badge.getMethod("setMode", int.class);
                Field modeSmallField = Class.forName("android.provider.ContactsContract$QuickContact").getDeclaredField("MODE_SMALL");
                modeSmall = modeSmallField.getInt(null);
                badgeSetLayoutParams = badge.getMethod("setLayoutParams", ViewGroup.LayoutParams.class);
                badgeSetScaleType = badge.getMethod("setScaleType", ImageView.ScaleType.class);
                badgeSetImageResource = badge.getMethod("setImageResource", int.class);
            } catch (Exception e) {
                Log.w(TAG, "reflection setup failed", e);
            }
        }
    }


    public MaybeContactView(Context context) {
        super(context);
        if ( getChildCount() < 1) addPhotoView(context);
        if ( getChildCount() < 2) addQuickContactBadgeIfPossible(context);
    }

    public MaybeContactView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if ( getChildCount() < 1) addPhotoView(context);
        if ( getChildCount() < 2) addQuickContactBadgeIfPossible(context);
    }

    public MaybeContactView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if ( getChildCount() < 1) addPhotoView(context);
        if ( getChildCount() < 2) addQuickContactBadgeIfPossible(context);
    }

    private void addPhotoView(Context context) {
        ImageView photo = new ImageView(context);
        photo.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, Gravity.CENTER_VERTICAL));
        photo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        photo.setImageResource(R.drawable.blank_boy);
        photo.setBackgroundResource(R.drawable.photo_background);
        addView(photo, 0);
        photo.setVisibility(View.VISIBLE);
        this.photo = photo;
    }

    private void addQuickContactBadgeIfPossible(Context context) {
        if (!CompatibilityHelp.API_LEVEL_AT_LEAST_ECLAIR) {
            return;
        }
        try {
            ImageView badge = (ImageView) badgeConstructor.newInstance(context, null, android.R.attr.quickContactBadgeStyleWindowSmall);
            badgeSetMode.invoke(badge, modeSmall);
            badgeSetImageResource.invoke(badge, R.drawable.blank_boy);
            badgeSetScaleType.invoke(badge, ImageView.ScaleType.CENTER_INSIDE);
            badgeSetLayoutParams.invoke(badge, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, Gravity.CENTER_VERTICAL));
            badgeSetExcludeMimes.invoke(badge, (Object)new String[] {"vnd.android.cursor.item/com.joelapenna.foursquared.profile"});
            badge.setVisibility(View.GONE);
            addView(badge, 1);
            this.quickContactBadge = badge;
        } catch (Exception e) {
            Log.w(TAG, "Couldn't add QuickContactBadge", e);
        }
    }

    void setImageBitmap(Bitmap bitmap) {
        photo.setImageBitmap(bitmap);
        if ( quickContactBadge != null ) {
            quickContactBadge.setImageBitmap(bitmap);
        }
    }

    void setImageResource(int resId) {
        photo.setImageResource(resId);
        if (quickContactBadge != null) {
            quickContactBadge.setImageResource(resId);
        }
    }

    void setContactLookupUri(Uri contactLookupUri) {
        Log.i(TAG, "setting contactUri to " + contactLookupUri);
        if ( contactLookupUri == null) {
            if ( quickContactBadge != null ) {
                quickContactBadge.setVisibility(GONE);
            }
            photo.setVisibility(View.VISIBLE);
            postInvalidate();
            return;
        }

        photo.setVisibility(View.GONE);
        try {
            if (quickContactBadge != null) {
                badgeAssignContactUri.invoke(quickContactBadge, contactLookupUri);
                quickContactBadge.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            photo.setVisibility(View.VISIBLE);
        }
        postInvalidate();
    }
    
}
