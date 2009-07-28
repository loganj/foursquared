/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.preferences;

import com.joelapenna.foursquared.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class CityNamePreference extends Preference {

    private String mCityName;

    // This is the constructor called by the inflater
    public CityNamePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_city_name);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        // Set our custom views inside the layout
        final TextView myTextView = (TextView)view.findViewById(R.id.cityNameTextView);
        if (myTextView != null) {
            myTextView.setText(mCityName);
        }
    }

    @Override
    protected void onClick() {
        super.onClick();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setText(restorePersistedValue ? getPersistedString(mCityName) : (String) defaultValue);
    }

    public void setText(String cityName) {
        mCityName = cityName;
        persistString(cityName);
        notifyChanged();
    }

}
