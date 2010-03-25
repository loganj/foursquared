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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
    
    private static final int DIALOG_ID_INFO = 1;

    private TipActivityAdapter mListAdapter;
    private StateHolder mStateHolder;
    private ListView mListView;
    private Handler mHandler;
    private RemoteResourceManager mRrm;
    private RemoteResourceManagerObserver mResourcesObserver;
    

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
    public void onPause() {
        super.onPause();
        
        if (mResourcesObserver != null) {
            mRrm.deleteObserver(mResourcesObserver);
        }
        
        if (isFinishing()) {
            unregisterReceiver(mLoggedOutReceiver);
            mHandler.removeCallbacks(mRunnableUpdateUserPhoto);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
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
        startActivity(intent);
    }
    
    private void showDialogInfo(String title, String message, String badgeIconUrl) {
        mStateHolder.setDlgInfoTitle(title);
        mStateHolder.setDlgInfoMessage(message);
        mStateHolder.setDlgInfoBadgeIconUrl(badgeIconUrl);
        showDialog(DIALOG_ID_INFO);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ID_INFO:
                AlertDialog dlgInfo = new AlertDialog.Builder(this)
                    .setTitle(mStateHolder.getDlgInfoTitle())
                    .setIcon(0)
                    .setMessage(mStateHolder.getDlgInfoMessage()).create();
                dlgInfo.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        removeDialog(DIALOG_ID_INFO);
                    }
                });
                try {
                    Uri icon = Uri.parse(mStateHolder.getDlgInfoBadgeIconUrl());
                    dlgInfo.setIcon(new BitmapDrawable(((Foursquared) getApplication())
                            .getRemoteResourceManager().getInputStream(icon)));
                } catch (IOException e) {
                    Log.e(TAG, "Error loading badge dialog!", e);
                    dlgInfo.setIcon(R.drawable.default_on);
                }
                return dlgInfo;
        }
        return null;
    }
    
    private static class StateHolder {
        private Tip mTip;
        private String mDlgInfoTitle;
        private String mDlgInfoMessage;
        private String mDlgInfoBadgeIconUrl;
        
        public StateHolder() {
        }
        
        public void setTip(Tip tip) { 
            mTip = tip;
        }
        
        public Tip getTip() {
            return mTip;
        }
        
        public String getDlgInfoTitle() {
            return mDlgInfoTitle;
        }
        
        public void setDlgInfoTitle(String text) {
            mDlgInfoTitle = text;
        }

        public String getDlgInfoMessage() {
            return mDlgInfoMessage;
        }
        
        public void setDlgInfoMessage(String text) {
            mDlgInfoMessage = text;
        }
        
        public String getDlgInfoBadgeIconUrl() {
            return mDlgInfoBadgeIconUrl;
        }
        
        public void setDlgInfoBadgeIconUrl(String url) {
            mDlgInfoBadgeIconUrl = url;
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
