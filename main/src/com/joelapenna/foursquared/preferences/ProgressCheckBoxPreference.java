package com.joelapenna.foursquared.preferences;

import android.R;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

final public class ProgressCheckBoxPreference extends CheckBoxPreference {
    private final static String TAG = "ProgressCheckBoxPref";

    public ProgressCheckBoxPreference(Context context) {
        super(context);
    }

    public ProgressCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgressCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected View onCreateView(ViewGroup parent) {
        View checkboxView = super.onCreateView(parent);
        if (isEnabled()) {
            return checkboxView;
        }
        
        RelativeLayout layout = new RelativeLayout(parent.getContext());
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.addView(checkboxView);
        ProgressBar progressBar = new ProgressBar(parent.getContext());
        progressBar.setBackgroundDrawable(parent.getBackground());
        progressBar.setPadding(0, checkboxView.getPaddingTop(), checkboxView.getPaddingRight(), checkboxView.getPaddingBottom());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.FILL_PARENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, checkboxView.getId());
        layout.addView(progressBar, lp);
        return layout;
    }

}
