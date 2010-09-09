/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.CheckinResult;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.util.NotificationsUtil;
import com.joelapenna.foursquared.util.RemoteResourceManager;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

/**
 * Can be called to execute a checkin. Should be presented with the transparent
 * dialog theme to appear only as a progress bar. When execution is complete, a
 * successful checkin will show an instance of CheckinResultDialog to handle
 * rendering the CheckinResult response object. A failed checkin will show a 
 * toast with the error message. Ideally we could launch another activity for
 * rendering the result, but passing the CheckinResult between activities using
 * the extras data will have to be done when we have more time.
 * 
 * For the location paramters of the checkin method, this activity will grab the
 * global last-known best location.
 * 
 * The activity will setResult(RESULT_OK) if the checkin worked, and will 
 * setResult(RESULT_CANCELED) if it did not work.
 * 
 * @date March 2, 2010
 * @author Mark Wyszomierski (markww@gmail.com).
 */
public class CheckinExecuteActivity extends Activity {
    public static final String TAG = "CheckinExecuteActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String INTENT_EXTRA_VENUE_ID = Foursquared.PACKAGE_NAME
            + ".CheckinExecuteActivity.INTENT_EXTRA_VENUE_ID";
    public static final String INTENT_EXTRA_SHOUT = Foursquared.PACKAGE_NAME
            + ".CheckinExecuteActivity.INTENT_EXTRA_SHOUT";
    public static final String INTENT_EXTRA_TELL_FRIENDS = Foursquared.PACKAGE_NAME
            + ".CheckinExecuteActivity.INTENT_EXTRA_TELL_FRIENDS";
    public static final String INTENT_EXTRA_TELL_FOLLOWERS = Foursquared.PACKAGE_NAME
            + ".CheckinExecuteActivity.INTENT_EXTRA_TELL_FOLLOWERS";
    public static final String INTENT_EXTRA_TELL_TWITTER = Foursquared.PACKAGE_NAME
            + ".CheckinExecuteActivity.INTENT_EXTRA_TELL_TWITTER";
    public static final String INTENT_EXTRA_TELL_FACEBOOK = Foursquared.PACKAGE_NAME
            + ".CheckinExecuteActivity.INTENT_EXTRA_TELL_FACEBOOK";
    
    private static final int DIALOG_CHECKIN_RESULT = 1;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.checkin_execute_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));
        
        // We start the checkin immediately on creation.
        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
            mStateHolder.setActivity(this);
        } else {
            mStateHolder = new StateHolder();

            String venueId = null;
            if (getIntent().getExtras().containsKey(INTENT_EXTRA_VENUE_ID)) {
                venueId = getIntent().getExtras().getString(INTENT_EXTRA_VENUE_ID);
            } else {
                Log.e(TAG, "CheckinExecuteActivity requires intent extra 'INTENT_EXTRA_VENUE_ID'.");
                finish();
                return;
            }
            
            Foursquared foursquared = (Foursquared) getApplication();
            Location location = foursquared.getLastKnownLocation();
            
            mStateHolder.startTask( 
                CheckinExecuteActivity.this,
                venueId,
                location,
                getIntent().getExtras().getString(INTENT_EXTRA_SHOUT),
                getIntent().getExtras().getBoolean(INTENT_EXTRA_TELL_FRIENDS, false),
                getIntent().getExtras().getBoolean(INTENT_EXTRA_TELL_FOLLOWERS, false),
                getIntent().getExtras().getBoolean(INTENT_EXTRA_TELL_TWITTER, false),
                getIntent().getExtras().getBoolean(INTENT_EXTRA_TELL_FACEBOOK, false)
            );
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        mStateHolder.setActivity(null);
        return mStateHolder;
    }
    
    @Override
    public void onResume() {
        super.onResume();

        if (mStateHolder.getIsRunning()) {
            startProgressBar(getResources().getString(R.string.checkin_action_label),
                    getResources().getString(R.string.checkin_execute_activity_progress_bar_message));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopProgressBar();
        
        if (isFinishing()) {
            mStateHolder.cancelTasks();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedOutReceiver);
    }

    private void startProgressBar(String title, String message) {
        if (mDlgProgress == null) {
            mDlgProgress = ProgressDialog.show(this, title, message);
        }
        mDlgProgress.show();
    }

    private void stopProgressBar() {
        if (mDlgProgress != null) {
            mDlgProgress.dismiss();
            mDlgProgress = null;
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CHECKIN_RESULT:
                // When the user cancels the dialog (by hitting the 'back' key), we
                // finish this activity. We don't listen to onDismiss() for this
                // action, because a device rotation will fire onDismiss(), and our
                // dialog would not be re-displayed after the rotation is complete.
                CheckinResultDialog dlg = new CheckinResultDialog(
                    this, 
                    mStateHolder.getCheckinResult(), 
                    ((Foursquared)getApplication()));
                dlg.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        removeDialog(DIALOG_CHECKIN_RESULT);
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                });
                return dlg;
        }
        return null;
    }
    
    private void onCheckinComplete(CheckinResult result, Exception ex) {
        mStateHolder.setIsRunning(false);
        stopProgressBar();

        if (result != null) {
            mStateHolder.setCheckinResult(result);
            showDialog(DIALOG_CHECKIN_RESULT);
        } else {
            NotificationsUtil.ToastReasonForFailure(this, ex);
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }
    
    private static class CheckinTask extends AsyncTask<Void, Void, CheckinResult> {

        private CheckinExecuteActivity mActivity;
        private String mVenueId;
        private Location mLocation;
        private String mShout;
        private boolean mTellFriends;
        private boolean mTellFollowers;
        private boolean mTellTwitter;
        private boolean mTellFacebook;
        private Exception mReason;

        public CheckinTask(CheckinExecuteActivity activity,
                           String venueId,
                           Location location,
                           String shout,
                           boolean tellFriends, 
                           boolean tellFollowers,
                           boolean tellTwitter,
                           boolean tellFacebook) {
            mActivity = activity;
            mVenueId = venueId;
            mLocation = location;
            mShout = shout;
            mTellFriends = tellFriends;
            mTellFollowers = tellFollowers;
            mTellTwitter = tellTwitter;
            mTellFacebook = tellFacebook;
        }

        public void setActivity(CheckinExecuteActivity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            mActivity.startProgressBar(mActivity.getResources().getString(
                    R.string.checkin_action_label), mActivity.getResources().getString(
                    R.string.checkin_execute_activity_progress_bar_message));
        }

        @Override
        protected CheckinResult doInBackground(Void... params) {
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();

                CheckinResult result = 
                  foursquare.checkin(
                    mVenueId, 
                    null, // passing in the real venue name causes a 400 response from the server. 
                    LocationUtils.createFoursquareLocation(mLocation), 
                    mShout, 
                   !mTellFriends, // (isPrivate) 
                    mTellFollowers,
                    mTellTwitter, 
                    mTellFacebook);
                
                // Here we should really be downloading the mayor's photo serially, so that this
                // work is done in the background while the progress bar is already spinning.
                // When the checkin result dialog pops up, the photo would already be loaded.
                // We can at least start the request if necessary here in the background thread.
                if (result != null && result.getMayor() != null && result.getMayor().getUser() != null) {
                    if (result.getMayor() != null && result.getMayor().getUser() != null) {
                        Uri photoUri = Uri.parse(result.getMayor().getUser().getPhoto()); 
                        RemoteResourceManager rrm = foursquared.getRemoteResourceManager();
                        if (rrm.exists(photoUri) == false) {
                            rrm.request(photoUri);
                        }
                    }
                }
                return result;
            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, "CheckinTask: Exception checking in.", e);
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(CheckinResult result) {
            if (DEBUG) Log.d(TAG, "CheckinTask: onPostExecute()");
            if (mActivity != null) {
                mActivity.onCheckinComplete(result, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onCheckinComplete(null, new FoursquareException(
                        "Check-in cancelled."));
            }
        }
    }
    
    private static class StateHolder {
        private CheckinTask mTask;
        private CheckinResult mCheckinResult;
        private boolean mIsRunning;

        public StateHolder() {
            mCheckinResult = null;
            mIsRunning = false;
        }

        public void startTask(CheckinExecuteActivity activity,
                              String venueId,
                              Location location,
                              String shout,
                              boolean tellFriends, 
                              boolean tellFollowers,
                              boolean tellTwitter,
                              boolean tellFacebook) {
            mIsRunning = true;
            mTask = new CheckinTask(
                    activity, venueId, location, shout, tellFriends, 
                    tellFollowers, tellTwitter, tellFacebook);
            mTask.execute();
        }

        public void setActivity(CheckinExecuteActivity activity) {
            if (mTask != null) {
                mTask.setActivity(activity);
            }
        }

        public boolean getIsRunning() {
            return mIsRunning;
        }
        
        public void setIsRunning(boolean isRunning) {
            mIsRunning = isRunning;
        }
        
        public CheckinResult getCheckinResult() {
            return mCheckinResult;
        }
        
        public void setCheckinResult(CheckinResult result) {
            mCheckinResult = result;
        }

        public void cancelTasks() {
            if (mTask != null && mIsRunning) {
                mTask.setActivity(null);
                mTask.cancel(true);
            }
        }
    }
}
