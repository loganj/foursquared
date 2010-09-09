/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.CheckinResult;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.util.NotificationsUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

/**
 * Can be called to execute a shout. Should be presented with the transparent
 * dialog theme to appear only as a progress bar. When execution is complete, a
 * toast will be shown with a success or error message.
 * 
 * For the location paramters of the checkin method, this activity will grab the
 * global last-known best location.
 * 
 * The activity will setResult(RESULT_OK) if the shout worked, and will 
 * setResult(RESULT_CANCELED) if it did not work.
 * 
 * @date March 10, 2010
 * @author Mark Wyszomierski (markww@gmail.com).
 */
public class ShoutExecuteActivity extends Activity {
    public static final String TAG = "ShoutExecuteActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String INTENT_EXTRA_SHOUT = Foursquared.PACKAGE_NAME
            + ".ShoutExecuteActivity.INTENT_EXTRA_SHOUT";
    public static final String INTENT_EXTRA_TELL_FRIENDS = Foursquared.PACKAGE_NAME
            + ".ShoutExecuteActivity.INTENT_EXTRA_TELL_FRIENDS";
    public static final String INTENT_EXTRA_TELL_FOLLOWERS = Foursquared.PACKAGE_NAME
            + ".ShoutExecuteActivity.INTENT_EXTRA_TELL_FOLLOWERS";
    public static final String INTENT_EXTRA_TELL_TWITTER = Foursquared.PACKAGE_NAME
            + ".ShoutExecuteActivity.INTENT_EXTRA_TELL_TWITTER";
    public static final String INTENT_EXTRA_TELL_FACEBOOK = Foursquared.PACKAGE_NAME
            + ".ShoutExecuteActivity.INTENT_EXTRA_TELL_FACEBOOK";
    
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

            Foursquared foursquared = (Foursquared) getApplication();
            Location location = foursquared.getLastKnownLocation();
            
            mStateHolder.startTask( 
                this,
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
            startProgressBar(getResources().getString(R.string.shout_action_label),
                    getResources().getString(R.string.shout_execute_activity_progress_bar_message));
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
    
    private void onShoutComplete(CheckinResult result, Exception ex) {
        mStateHolder.setIsRunning(false);
        stopProgressBar();

        if (result != null) {
            Toast.makeText(this, getResources().getString(R.string.shout_exceute_activity_result), 
                    Toast.LENGTH_LONG).show();
            setResult(Activity.RESULT_OK);
        } else {
            NotificationsUtil.ToastReasonForFailure(this, ex);
            setResult(Activity.RESULT_CANCELED);
        }
        finish();
    }
    
    private static class ShoutTask extends AsyncTask<Void, Void, CheckinResult> {

        private ShoutExecuteActivity mActivity;
        private Location mLocation;
        private String mShout;
        private boolean mTellFriends;
        private boolean mTellFollowers;
        private boolean mTellTwitter;
        private boolean mTellFacebook;
        private Exception mReason;

        public ShoutTask(ShoutExecuteActivity activity,
                         Location location,
                         String shout,
                         boolean tellFriends, 
                         boolean tellFollowers,
                         boolean tellTwitter,
                         boolean tellFacebook) {
            mActivity = activity;
            mLocation = location;
            mShout = shout;
            mTellFriends = tellFriends;
            mTellFollowers = tellFollowers;
            mTellTwitter = tellTwitter;
            mTellFacebook = tellFacebook;
        }

        public void setActivity(ShoutExecuteActivity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            mActivity.startProgressBar(mActivity.getResources().getString(
                    R.string.shout_action_label), mActivity.getResources().getString(
                    R.string.shout_execute_activity_progress_bar_message));
        }

        @Override
        protected CheckinResult doInBackground(Void... params) {
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();

                CheckinResult result = 
                  foursquare.checkin(
                    null, 
                    null,
                    LocationUtils.createFoursquareLocation(mLocation), 
                    mShout, 
                   !mTellFriends, // (isPrivate)
                    mTellFollowers,
                    mTellTwitter, 
                    mTellFacebook);
                return result;
            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, "ShoutTask: Exception checking in.", e);
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(CheckinResult result) {
            if (DEBUG) Log.d(TAG, "ShoutTask: onPostExecute()");
            if (mActivity != null) {
                mActivity.onShoutComplete(result, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onShoutComplete(null, new Exception(
                        "Shout cancelled."));
            }
        }
    }
    
    private static class StateHolder {
        private ShoutTask mTask;
        private boolean mIsRunning;

        public StateHolder() {
            mIsRunning = false;
        }

        public void startTask(ShoutExecuteActivity activity,
                              Location location,
                              String shout,
                              boolean tellFriends, 
                              boolean tellFollowers,
                              boolean tellTwitter,
                              boolean tellFacebook) {
            mIsRunning = true;
            mTask = new ShoutTask(activity, location, shout, tellFriends, tellFollowers,
                    tellTwitter, tellFacebook);
            mTask.execute();
        }

        public void setActivity(ShoutExecuteActivity activity) {
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

        public void cancelTasks() {
            if (mTask != null && mIsRunning) {
                mTask.setActivity(null);
                mTask.cancel(true);
            }
        }
    }
}
