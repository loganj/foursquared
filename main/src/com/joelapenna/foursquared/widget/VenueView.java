/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.widget;

import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.R;
import com.joelapenna.foursquared.util.StringFormatters;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueView extends RelativeLayout {

    private boolean mCheckinButtonVisible;

    private Button mCheckinButton;
    private TextView mVenueName;
    private TextView mVenueLocationLine1;
    private TextView mVenueLocationLine2;

    public VenueView(Context context) {
        super(context);
    }

    public VenueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VenueView, 0, 0);
        mCheckinButtonVisible = a.getBoolean(R.styleable.VenueView_checkinButton, false);
        a.recycle();
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        ((Activity)getContext()).getLayoutInflater().inflate(R.layout.venue, this);
        mCheckinButton = (Button)findViewById(R.id.internal_checkinButton);
        mVenueName = (TextView)findViewById(R.id.internal_venueName);
        mVenueLocationLine1 = (TextView)findViewById(R.id.internal_venueLocationLine1);
        mVenueLocationLine2 = (TextView)findViewById(R.id.internal_venueLocationLine2);
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
        if (line2 != null) {
            mVenueLocationLine2.setText(line2);
        }
        if (mCheckinButtonVisible) {
            mCheckinButton.setVisibility(View.VISIBLE);
        }
    };

    public void setCheckinButtonOnClickListener(OnClickListener l) {
        mCheckinButton.setOnClickListener(l);
    }
}
