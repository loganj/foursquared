package com.joelapenna.foursquared.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Use with SometimesQuickContactBadgeHelp to build a QuickContactBadge from this ImageView.
 */
final class SometimesQuickContactBadge extends ImageView {
    private AttributeSet mAttrs;

    public SometimesQuickContactBadge(Context context) {
        super(context);
    }

    public SometimesQuickContactBadge(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mAttrs = attrs;
    }

    public SometimesQuickContactBadge(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mAttrs = attrs;
    }

    AttributeSet getAttrs() {
        return mAttrs;
    }
}
