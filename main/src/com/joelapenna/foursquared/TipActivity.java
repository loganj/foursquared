/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Tip;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.RemoteResourceManager;
import com.joelapenna.foursquared.util.StringFormatters;
import com.joelapenna.foursquared.widget.TipActivityAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * Shows actions available for a tip:
 * 
 * <ul>
 *   <li>Add to my to-do list</li>
 *   <li>I've done this!</li>
 * </ul>
 * 
 * The foursquare API doesn't tell us whether we've already marked a tip as
 * to-do or already done, so we just keep presenting the same options to the
 * user every time they look at this screen.
 * 
 * @date March 24, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 * 
 */
public class TipActivity extends Activity {
    private static final String TAG = "TipActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String EXTRA_TIP_PARCEL = Foursquared.PACKAGE_NAME
        + ".TipActivity.EXTRA_TIP_PARCEL";

    public static final String EXTRA_VENUE_NAME = Foursquared.PACKAGE_NAME
        + ".TipActivity.EXTRA_VENUE_NAME";
    
    public static final int RESULT_TIP_MARKED_TODO = -2;
    public static final int RESULT_TIP_MARKED_DONE = -3;
    
    private TipActivityAdapter mListAdapter;
    private StateHolder mStateHolder;
    private ListView mListView;
    private Handler mHandler;
    private RemoteResourceManager mRrm;
    private RemoteResourceManagerObserver mResourcesObserver;
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
        setContentView(R.layout.tip_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));
        
        mRrm = ((Foursquared) getApplication()).getRemoteResourceManager();
        mHandler = new Handler();
        
        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
            mStateHolder.setActivityForTipTask(this);
        } else {
            mStateHolder = new StateHolder();
            if (getIntent().getExtras() != null && 
                getIntent().getExtras().containsKey(EXTRA_TIP_PARCEL)) {
                
                Tip tip = getIntent().getExtras().getParcelable(EXTRA_TIP_PARCEL);
                mStateHolder.setTip(tip);
                
                if (getIntent().getExtras().containsKey(EXTRA_VENUE_NAME)) {
                    if (mStateHolder.getTip().getVenue() == null) {
                        mStateHolder.getTip().setVenue(new Venue());
                    }
                    mStateHolder.getTip().getVenue().setName(
                        getIntent().getExtras().getString(EXTRA_VENUE_NAME));
                }
                
            } else {
                Log.e(TAG, "TipActivity requires a tip pareclable in its intent extras.");
                finish();
                return;
            }
        }
        
        ensureUi();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        if (mStateHolder.getIsRunningTipTask()) {
            startProgressBar(mStateHolder.getTask());
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (mResourcesObserver != null) {
            mRrm.deleteObserver(mResourcesObserver);
        }
        
        if (isFinishing()) {
            unregisterReceiver(mLoggedOutReceiver);
            mHandler.removeCallbacks(mRunnableUpdateUserPhoto);
            stopProgressBar();
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        mStateHolder.setActivityForTipTask(null);
        return mStateHolder;
    }

    private void ensureUi() {
        LinearLayout llHeader = (LinearLayout)findViewById(R.id.tipActivityHeaderView);
        llHeader.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserDetailsActivity(mStateHolder.getTip().getUser().getId());
            }
        });
        
        TextView tvTitle = (TextView)findViewById(R.id.tipActivityName);
        tvTitle.setText(
            getResources().getString(R.string.tip_activity_by) + " " +
            StringFormatters.getUserFullName(mStateHolder.getTip().getUser()));
        
        setUserPhoto(true);
        
        mListAdapter = new TipActivityAdapter(
            this, 
            mStateHolder.getTip().getVenue() != null ? 
                mStateHolder.getTip().getVenue().getName() : "",
            mStateHolder.getTip().getText());
        
        mListView = (ListView)findViewById(R.id.tipActivityListView);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                switch (position) {
                    case TipActivityAdapter.ACTION_ID_ADD_TODO_LIST:
                        mStateHolder.startTipTask(TipActivity.this, mStateHolder.getTip().getId(), 
                                TipActivityAdapter.ACTION_ID_ADD_TODO_LIST);
                        break;
                    case TipActivityAdapter.ACTION_ID_IVE_DONE_THIS:
                        mStateHolder.startTipTask(TipActivity.this, mStateHolder.getTip().getId(), 
                                TipActivityAdapter.ACTION_ID_IVE_DONE_THIS);
                        break;
                }
            }
        });
    }
    
    private void setUserPhoto(boolean fetchIfMissing) {
        
        ImageView iv = (ImageView)findViewById(R.id.tipActivityPhoto);
        
        User user = mStateHolder.getTip().getUser();
        if (user != null) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(mRrm.getInputStream(Uri.parse(user
                        .getPhoto())));
                iv.setImageBitmap(bitmap);
            } catch (IOException e) {
                setUserPhotoNone(user);
                
                // Try to fetch the photo.
                mResourcesObserver = new RemoteResourceManagerObserver();
                mRrm.addObserver(mResourcesObserver);
                mRrm.request(Uri.parse(user.getPhoto()));
            }
        }
    }
    
    private void setUserPhotoNone(User user) {
        ImageView iv = (ImageView)findViewById(R.id.tipActivityPhoto);
        if (Foursquare.MALE.equals(user.getGender())) {
            iv.setImageResource(R.drawable.blank_boy);
        } else {
            iv.setImageResource(R.drawable.blank_girl);
        }
    }
    
    private void showUserDetailsActivity(String userId) {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra(UserDetailsActivity.EXTRA_USER_ID, userId);
        intent.putExtra(UserDetailsActivity.EXTRA_SHOW_ADD_FRIEND_OPTIONS, true);
        startActivity(intent);
    }
    
    private void startProgressBar(int task) {
        if (mDlgProgress == null) {
            
            String message = "";
            switch (task) {
                case TipActivityAdapter.ACTION_ID_ADD_TODO_LIST:
                    message = getResources().getString(
                        R.string.tip_activity_action_todo);
                    break;
                case TipActivityAdapter.ACTION_ID_IVE_DONE_THIS:
                    message = getResources().getString(
                        R.string.tip_activity_action_done_this);
                    break;
            }

            mDlgProgress = ProgressDialog.show(
                this, getResources().getString(R.string.tip_activity_prgoress_title), message);
        }
        mDlgProgress.show();
    }

    private void stopProgressBar() {
        if (mDlgProgress != null) {
            mDlgProgress.dismiss();
            mDlgProgress = null;
        }
    }
    
    private void onTipTaskComplete(Tip tip, int type, Exception ex) {
        stopProgressBar();
        mStateHolder.setIsRunningTipTask(false);
        if (tip != null) {
            String message = "";
            switch (type) {
                case TipActivityAdapter.ACTION_ID_ADD_TODO_LIST:
                    message = getResources().getString(
                            R.string.tip_activity_prgoress_complete_todo);
                    setResult(RESULT_TIP_MARKED_TODO);
                    break;
                case TipActivityAdapter.ACTION_ID_IVE_DONE_THIS:
                    message = getResources().getString(
                            R.string.tip_activity_prgoress_complete_done);
                    setResult(RESULT_TIP_MARKED_DONE);
                    break;
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
        }
    }
    
    private static class TipTask extends AsyncTask<String, Void, Tip> {
        private TipActivity mActivity;
        private String mTipId;
        private int mTask;
        private Exception mReason;

        public TipTask(TipActivity activity, String tipid, int task) {
            mActivity = activity;
            mTipId = tipid;
            mTask = task;
        }

        public void setActivity(TipActivity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            mActivity.startProgressBar(mTask);
        }
        
        public int getTask() {
            return mTask;
        }

        @Override
        protected Tip doInBackground(String... params) {
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();

                Tip tip = null;
                switch (mTask) {
                    case TipActivityAdapter.ACTION_ID_ADD_TODO_LIST:
                        tip = foursquare.tipMarkTodo(mTipId);
                        break;
                    case TipActivityAdapter.ACTION_ID_IVE_DONE_THIS:
                        tip = foursquare.tipMarkDone(mTipId);
                        break;
                }
                return tip;
                
            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, "TipTask: Exception performing tip task.", e);
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Tip tip) {
            if (DEBUG) Log.d(TAG, "TipTask: onPostExecute()");
            if (mActivity != null) {
                mActivity.onTipTaskComplete(tip, mTask, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onTipTaskComplete(null, mTask, new Exception("Tip task cancelled."));
            }
        }
    }
    
    private static class StateHolder {
        private Tip mTip;
        private TipTask mTipTask;
        private boolean mIsRunningTipTask;
        
        
        public StateHolder() {
            mIsRunningTipTask = false;
        }
        
        public void setTip(Tip tip) { 
            mTip = tip;
        }
        
        public Tip getTip() {
            return mTip;
        }
        
        public int getTask() {
            return mTipTask.getTask();
        }

        public void startTipTask(TipActivity activity, String tipId, int task) {
            mIsRunningTipTask = true;
            mTipTask = new TipTask(activity, tipId, task);
            mTipTask.execute();
        }

        public void setActivityForTipTask(TipActivity activity) {
            if (mTipTask != null) {
                mTipTask.setActivity(activity);
            }
        }
        
        public void setIsRunningTipTask(boolean isRunningTipTask) {
            mIsRunningTipTask = isRunningTipTask;
        }
        
        public boolean getIsRunningTipTask() {
            return mIsRunningTipTask;
        }
    }
    
    private class RemoteResourceManagerObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            mHandler.post(mRunnableUpdateUserPhoto);
        }
    }
    
    private Runnable mRunnableUpdateUserPhoto = new Runnable() {
        @Override
        public void run() {
            setUserPhoto(false);
        }
    };
}
