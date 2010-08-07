/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Response;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.NotificationsUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * Gives the user the option to correct some info about a venue:
 * <ul>
 *   <li>Edit venue info</li>
 *   <li>Flag as closed</li>
 *   <li>Mislocated (but don't know the right address)</li>
 *   <li>Flag as duplicate</li>
 * </ul>
 * 
 * @date June 7, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class EditVenueOptionsActivity extends Activity {
    public static final String EXTRA_VENUE_PARCELABLE = "com.joelapenna.foursquared.VenueParcelable";
    private static final String TAG = "EditVenueOptionsActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;
    private static final int REQUEST_CODE_ACTIVITY_ADD_VENUE = 15;

    private StateHolder mStateHolder;
    private ProgressDialog mDlgProgress;
    

    private BroadcastReceiver mLoggedOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Log.d(TAG, "onReceive: " + intent);
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.edit_venue_options_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));
        
        ensureUi();
        
        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
            mStateHolder.setActivity(this);
        } else {
            if (getIntent().getExtras() != null) {
                if (getIntent().getExtras().containsKey(EXTRA_VENUE_PARCELABLE)) {
                    Venue venue = (Venue)getIntent().getExtras().getParcelable(EXTRA_VENUE_PARCELABLE);
                    if (venue != null) {
                        mStateHolder = new StateHolder(venue);
                    } else {
                        Log.e(TAG, "EditVenueOptionsActivity supplied with null venue parcelable.");
                        finish();
                    }
                } else {
                    Log.e(TAG, "EditVenueOptionsActivity requires venue parcelable in extras.");
                    finish();
                }
            } else {
                Log.e(TAG, "EditVenueOptionsActivity requires venueid in extras.");
                finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((Foursquared) getApplication()).requestLocationUpdates(true);
        
        if (mStateHolder.getIsRunningTaskVenue()) {
            startProgressBar();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ((Foursquared) getApplication()).removeLocationUpdates();

        stopProgressBar();
        if (isFinishing()) {
            mStateHolder.cancelTasks();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedOutReceiver);
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        mStateHolder.setActivity(null);
        return mStateHolder;
    }
    
    private void ensureUi() {
        Button btnEditVenue = (Button)findViewById(R.id.btnEditVenue);
        btnEditVenue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditVenueOptionsActivity.this, AddVenueActivity.class);
                intent.putExtra(AddVenueActivity.EXTRA_VENUE_TO_EDIT, mStateHolder.getVenue());
                startActivityForResult(intent, REQUEST_CODE_ACTIVITY_ADD_VENUE);
            }
        });
        
        Button btnFlagAsClosed = (Button)findViewById(R.id.btnFlagAsClosed);
        btnFlagAsClosed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mStateHolder.startTaskVenue(
                        EditVenueOptionsActivity.this, VenueTask.ACTION_FLAG_AS_CLOSED);
            }
        });
        
        Button btnFlagAsMislocated = (Button)findViewById(R.id.btnFlagAsMislocated);
        btnFlagAsMislocated.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mStateHolder.startTaskVenue(
                        EditVenueOptionsActivity.this, VenueTask.ACTION_FLAG_AS_MISLOCATED);
            }
        });
        
        Button btnFlagAsDuplicate = (Button)findViewById(R.id.btnFlagAsDuplicate);
        btnFlagAsDuplicate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mStateHolder.startTaskVenue(
                        EditVenueOptionsActivity.this, VenueTask.ACTION_FLAG_AS_DUPLICATE);
            }
        });
    }
    
    private void startProgressBar() {
        if (mDlgProgress == null) {
            mDlgProgress = ProgressDialog.show(this, 
                    getResources().getString(R.string.edit_venue_options_progress_title),
                    getResources().getString(R.string.edit_venue_options_progress_message));
        }
        mDlgProgress.show();
    }

    private void stopProgressBar() {
        if (mDlgProgress != null) {
            mDlgProgress.dismiss();
            mDlgProgress = null;
        }
    }
    
    private void onVenueTaskComplete(Response response, Exception ex) {
        stopProgressBar();
        mStateHolder.setIsRunningTaskVenue(false);
        if (response != null) {
            if (!TextUtils.isEmpty(response.getValue()) && response.getValue().equals("ok")) {
                Toast.makeText(this, getResources().getString(R.string.edit_venue_options_thankyou), 
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, getResources().getString(R.string.edit_venue_options_error), 
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            NotificationsUtil.ToastReasonForFailure(this, ex);
        }
    }
    
    
    private static class VenueTask extends AsyncTask<Void, Void, Response> {
        public static final int ACTION_FLAG_AS_CLOSED     = 0;
        public static final int ACTION_FLAG_AS_MISLOCATED = 1;
        public static final int ACTION_FLAG_AS_DUPLICATE  = 2;
        
        private EditVenueOptionsActivity mActivity;
        private Exception mReason;
        private int mAction;
        private String mVenueId;

        public VenueTask(EditVenueOptionsActivity activity, int action, String venueId) {
            mActivity = activity;
            mAction = action;
            mVenueId = venueId;
        }

        @Override
        protected void onPreExecute() {
            mActivity.startProgressBar();
        }

        @Override
        protected Response doInBackground(Void... params) {
            Foursquared foursquared = (Foursquared) mActivity.getApplication();
            Foursquare foursquare = foursquared.getFoursquare();
            try {
                switch (mAction) {
                    case ACTION_FLAG_AS_CLOSED:
                        return foursquare.flagclosed(mVenueId);
                    case ACTION_FLAG_AS_MISLOCATED:
                        return foursquare.flagmislocated(mVenueId);
                    case ACTION_FLAG_AS_DUPLICATE:
                        return foursquare.flagduplicate(mVenueId);
                }
            } catch (Exception e) {
                mReason = e;
            }
            
            return null;
        }

        @Override
        protected void onPostExecute(Response response) {
            if (mActivity != null) {
                mActivity.onVenueTaskComplete(response, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onVenueTaskComplete(null, mReason);
            }
        }

        public void setActivity(EditVenueOptionsActivity activity) {
            mActivity = activity;
        }
    }

    private static class StateHolder {

        private Venue mVenue;
        private VenueTask mTaskVenue;
        private boolean mIsRunningTaskVenue;
        

        public StateHolder(Venue venue) {
            mVenue = venue;
            mIsRunningTaskVenue = false;
        }
        
        public Venue getVenue() {
            return mVenue;
        }
        
        public void startTaskVenue(EditVenueOptionsActivity activity, int action) {
            mIsRunningTaskVenue = true;
            mTaskVenue = new VenueTask(activity, action, mVenue.getId());
            mTaskVenue.execute();
        }

        public void setActivity(EditVenueOptionsActivity activity) {
            if (mTaskVenue != null) {
                mTaskVenue.setActivity(activity);
            }
        }

        public boolean getIsRunningTaskVenue() {
            return mIsRunningTaskVenue;
        }
        
        public void setIsRunningTaskVenue(boolean isRunning) {
            mIsRunningTaskVenue = isRunning;
        }
        
        public void cancelTasks() {
            if (mTaskVenue != null) {
                mTaskVenue.setActivity(null);
                mTaskVenue.cancel(true);
                mIsRunningTaskVenue = false;
            }
        }
    }
}
