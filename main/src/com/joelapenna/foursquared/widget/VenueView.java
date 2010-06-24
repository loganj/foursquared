/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.StringFormatters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueView extends RelativeLayout {

    private boolean mCheckinButtonVisible = false;
    private boolean mCollapsible = false;

    private Button mCheckinButton;
    private TextView mVenueName;
    private TextView mVenueLocationLine1;
    private TextView mVenueLocationLine2;
    private ImageView mVenueSpecialIcon;
    

    public VenueView(Context context) {
        super(context);
    }

    public VenueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VenueView, 0, 0);
        mCheckinButtonVisible = a.getBoolean(R.styleable.VenueView_checkinButton, false);
        mCollapsible = a.getBoolean(R.styleable.VenueView_collapsible, false);
        a.recycle();
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        ((Activity)getContext()).getLayoutInflater().inflate(R.layout.venue_view, this);
        mCheckinButton = (Button)findViewById(R.id.internal_checkinButton);
        mVenueName = (TextView)findViewById(R.id.internal_venueName);
        mVenueLocationLine1 = (TextView)findViewById(R.id.internal_venueLocationLine1);
        mVenueLocationLine2 = (TextView)findViewById(R.id.internal_venueLocationLine2);
        mVenueSpecialIcon = (ImageView)findViewById(R.id.internal_specialImageView);
    }

    public void setCheckinButtonEnabled(boolean enabled) {
        mCheckinButton.setEnabled(enabled);
    }

    public void setCheckinButtonVisibility(int visibility) {
        mCheckinButton.setVisibility(visibility);
    }

    public void setVenue(Venue venue) {
        mVenueName.setText(venue.getName());
        mVenueLocationLine1.setText(venue.getAddress());

        String line2 = StringFormatters.getVenueLocationCrossStreetOrCity(venue);
        if (TextUtils.isEmpty(line2)) {
            mVenueLocationLine2.setText(line2);
            mVenueLocationLine2.setVisibility(View.VISIBLE);
        } else if (mCollapsible) {
            mVenueLocationLine2.setVisibility(View.GONE);
        }

        if (mCheckinButtonVisible) {
            mCheckinButton.setVisibility(View.VISIBLE);
        }
        
        // Don't show the special unless it's linked to this venue!
        if (venue.getSpecials() != null && venue.getSpecials().size() > 0) {
            Venue specialVenue = venue.getSpecials().get(0).getVenue();
            if (specialVenue == null || specialVenue.getId().equals(venue.getId())) {
                mVenueSpecialIcon.setVisibility(View.VISIBLE);
            }
        }
    };

    public void setCheckinButtonOnClickListener(OnClickListener l) {
        mCheckinButton.setOnClickListener(l);
    }
    
    public void setSpecialOnClickListener(OnClickListener l) {
        mVenueSpecialIcon.setOnClickListener(l);
    }
    
    public void updateCheckinButtonText() {
        // If the user has enabled 'quick check-in', then update the text of the button
        // to reflect this.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(
                getContext());
        if (settings.getBoolean(Preferences.PREFERENCE_IMMEDIATE_CHECKIN, false)) {
            mCheckinButton.setText(getContext().getString(R.string.venue_activity_checkin_button_quick));
        } else {
            mCheckinButton.setText(getContext().getString(R.string.venue_activity_checkin_button));
        }
    }
}
