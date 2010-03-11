/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Badge;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquared.widget.BadgeWithIconListAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Shows a listing of all the badges the user has earned. Right not it shows only
 * the earned badges, we can add an additional display flag to also display badges
 * the user has yet to unlock as well. This will show them what they're missing
 * which would be fun to see.
 * 
 * @date March 10, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class BadgesActivity extends Activity {
    private static final String TAG = "BadgesActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    public static final String EXTRA_BADGE_ARRAY_LIST_PARCEL = Foursquared.PACKAGE_NAME
        + ".BadgesActivity.EXTRA_BADGE_ARRAY_LIST_PARCEL";
    
    private static final int DIALOG_ID_INFO = 1;

    private GridView mBadgesGrid;
    private BadgeWithIconListAdapter mListAdapter;
    
    private StateHolder mStateHolder;
    

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.badges_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
        } else {
            mStateHolder = new StateHolder();
            if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(
                    EXTRA_BADGE_ARRAY_LIST_PARCEL)) {
                
                // Can't jump from ArrayList to Group, argh.
                ArrayList<Badge> badges = getIntent().getExtras().getParcelableArrayList(
                        EXTRA_BADGE_ARRAY_LIST_PARCEL);
                Group<Badge> group = new Group<Badge>();
                for (Badge it : badges) {
                    group.add(it);
                }
                mStateHolder.setBadges(group);
            } else {
                Log.e(TAG, "BadgesActivity requires a badge ArrayList pareclable in its intent extras.");
                finish();
                return;
            }
        }
        
        ensureUi();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (isFinishing()) {
            mListAdapter.removeObserver();
            unregisterReceiver(mLoggedOutReceiver);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mStateHolder;
    }

    private void ensureUi() {
        mBadgesGrid = (GridView)findViewById(R.id.badgesGrid);
        mListAdapter = new BadgeWithIconListAdapter(this,
                ((Foursquared)getApplication()).getRemoteResourceManager());
        mListAdapter.setGroup(mStateHolder.getBadges());
        mBadgesGrid.setAdapter(mListAdapter);
        mBadgesGrid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                Badge badge = (Badge)mListAdapter.getItem(position);
                showDialogInfo(badge.getName(), badge.getDescription(), badge.getIcon());
            }
        });
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
        private Group<Badge> mBadges;
        private String mDlgInfoTitle;
        private String mDlgInfoMessage;
        private String mDlgInfoBadgeIconUrl;
        
        public StateHolder() {
            mBadges = new Group<Badge>();
        }
        
        public void setBadges(Group<Badge> badges) { 
            mBadges = badges;
        }
        
        public Group<Badge> getBadges() {
            return mBadges;
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
}
