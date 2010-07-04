package com.joelapenna.foursquared.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.util.CompatibilityHelp;
import com.joelapenna.foursquared.Sync;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Use with SometimesQuickContactBadge for backward-compatibility hijinks
 */
final class SometimesQuickContactBadgeHelp {

    SometimesQuickContactBadgeHelp() {}

    static Constructor<?> badgeConstructor;
    static Method assignContactUri;
    static Method setExcludeMimes;

    static {
        if ( CompatibilityHelp.API_LEVEL_AT_LEAST_ECLAIR) {
            try {
                Class quickContactBadge = Class.forName("android.widget.QuickContactBadge");
                badgeConstructor = quickContactBadge.getConstructor(Context.class, AttributeSet.class, int.class);
                assignContactUri = quickContactBadge.getMethod("assignContactUri", Uri.class);
                setExcludeMimes = quickContactBadge.getMethod("setExcludeMimes", String[].class);
            } catch (Exception e) {
            }
        }
    }

    static ImageView setPhotoView(ContentResolver resolver, User user, View convertView, int photoFrameId) {
        final FrameLayout frame = (FrameLayout)convertView.findViewById(photoFrameId);
        final SometimesQuickContactBadge photo = (SometimesQuickContactBadge)convertView.findViewById(R.id.sometimes_quickcontact);

        if (!CompatibilityHelp.API_LEVEL_AT_LEAST_ECLAIR) {
            return photo;
        }

        try {
            ImageView badge = null;
            if ( frame.getChildCount() > 1 ) {
                badge = (ImageView)frame.getChildAt(1);
                badge.setVisibility(View.GONE);
            } else {
                badge = (ImageView) badgeConstructor.newInstance(photo.getContext(), photo.getAttrs(),  android.R.attr.quickContactBadgeStyleWindowSmall);
                setExcludeMimes.invoke(badge, (Object)new String[] {"vnd.android.cursor.item/com.joelapenna.foursquared.profile"});
                badge.setVisibility(View.GONE);
                frame.addView(badge);
            }

            Uri lookupUri = Sync.getContactLookupUri(resolver, user);

            if ( lookupUri != null ) {
                    assignContactUri.invoke(badge, lookupUri);
                    photo.setVisibility(View.GONE);
                    badge.setVisibility(View.VISIBLE);
                    return badge;
            } else {

            }
        } catch (Exception e) {
            // we'll fall through and return the original photo view
        }
        photo.setVisibility(View.VISIBLE);
        return photo;
    }
}
