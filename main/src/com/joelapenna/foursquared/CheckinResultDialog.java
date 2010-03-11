/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Badge;
import com.joelapenna.foursquare.types.CheckinResult;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Mayor;
import com.joelapenna.foursquare.types.Score;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.util.Base64Coder;
import com.joelapenna.foursquared.util.RemoteResourceManager;
import com.joelapenna.foursquared.widget.BadgeWithIconListAdapter;
import com.joelapenna.foursquared.widget.ScoreListAdapter;
import com.joelapenna.foursquared.widget.SeparatedListAdapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;


/**
 * Renders the result of a checkin using a CheckinResult object. This is called 
 * from CheckinExecuteActivity. It would be nicer to put this in another activity,
 * but right now the CheckinResult is quite large and would require a good amount
 * of work to add serializers for all its inner classes. This wouldn't be a huge
 * problem, but maintaining it as the classes evolve could more trouble than it's
 * worth.
 * 
 * The only way the user can dismiss this dialog is by hitting the 'back' key.
 * CheckingExecuteActivity depends on this so it knows when to finish() itself. 
 * 
 * @date March 3, 2010.
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 *
 */
public class CheckinResultDialog extends Dialog
{
    private static final String TAG = "CheckinResultDialog";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;
    
    private CheckinResult mCheckinResult;
    private Handler mHandler;
    private RemoteResourceManagerObserver mObserverMayorPhoto;
    private Foursquared mApplication;
    private String mExtrasDecoded;
    private WebViewDialog mDlgWebViewExtras;

    public CheckinResultDialog(Context context, CheckinResult result, Foursquared application) { 
        super(context, R.style.ThemeCustomDlgBase_ThemeCustomDlg); 
        mCheckinResult = result;
        mApplication = application;
        mHandler = new Handler();
        mObserverMayorPhoto = null;
    } 

    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
    
        setContentView(R.layout.checkin_result_dialog);
        setTitle("Checked in!");
        
        TextView tvMessage = (TextView)findViewById(R.id.textViewCheckinMessage);
        tvMessage.setText(mCheckinResult.getMessage());
        
        SeparatedListAdapter adapter = new SeparatedListAdapter(getContext());
        
        // Add any badges the user unlocked as a result of this checkin.
        addBadges(mCheckinResult.getBadges(), adapter, mApplication.getRemoteResourceManager());
        
        // Add whatever points they got as a result of this checkin.
        addScores(mCheckinResult.getScoring(), adapter, mApplication.getRemoteResourceManager());

        // Add a button below the mayor section which will launch a new webview if
        // we have additional content from the server. This is base64 encoded and
        // is supposed to be just dumped into a webview.
        addExtras(mCheckinResult.getMarkup());
        
        // List items construction complete.
        ListView listview = (ListView)findViewById(R.id.listViewCheckinBadgesAndScores);
        listview.setAdapter(adapter);

        // Show mayor info if any.
        addMayor(mCheckinResult.getMayor(), mApplication.getRemoteResourceManager());
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        if (mDlgWebViewExtras != null && mDlgWebViewExtras.isShowing()) {
            mDlgWebViewExtras.dismiss();
        }
        
        if (mObserverMayorPhoto != null) {
            mApplication.getRemoteResourceManager().deleteObserver(mObserverMayorPhoto);
        }
    }
    
    private void addBadges(Group<Badge> badges, SeparatedListAdapter adapterMain, RemoteResourceManager rrm) {
        if (badges == null || badges.size() < 1) {
            return;
        }

        BadgeWithIconListAdapter adapter = new BadgeWithIconListAdapter(
            getContext(), rrm, R.layout.badge_list_item);
        adapter.setGroup(badges);
        adapterMain.addSection("Badges", adapter);
    }

    private void addScores(Group<Score> scores,
                           SeparatedListAdapter adapterMain,
                           RemoteResourceManager rrm) {
        
        if (scores == null || scores.size() < 1) {
            return; 
        }

        // We make our own local score group because we'll inject the total as 
        // a new dummy score element.
        Group<Score> scoresWithTotal = new Group<Score>();
        
        // Total up the scoring.
        int total = 0;
        for (Score score : scores) {
            total += Integer.parseInt(score.getPoints());
            scoresWithTotal.add(score);
        }
        
        // Add a dummy score element to the group which is just the total.
        Score scoreTotal = new Score();
        scoreTotal.setIcon("");
        scoreTotal.setMessage(getContext().getResources().getString(
                R.string.checkin_result_dialog_score_total));
        scoreTotal.setPoints(String.valueOf(total));
        scoresWithTotal.add(scoreTotal);
        
        // Give it all to the adapter now.
        ScoreListAdapter adapter = new ScoreListAdapter(getContext(), rrm);
        adapter.setGroup(scoresWithTotal);
        adapterMain.addSection(getContext().getResources().getString(R.string.checkin_score), adapter);
    }
    
    private void addMayor(Mayor mayor, RemoteResourceManager rrm) {
        
        LinearLayout llMayor = (LinearLayout)findViewById(R.id.llCheckinMayorInfo);
        if (mayor == null) {
            llMayor.setVisibility(View.GONE);
            return;
        } else {
            llMayor.setVisibility(View.VISIBLE);
        }
        
        // Set the mayor message.
        TextView tvMayorMessage = (TextView)findViewById(R.id.textViewCheckinMayorMessage);
        tvMayorMessage.setText(mayor.getMessage());
        
        // A few cases here for the image to display.
        ImageView ivMayor = (ImageView)findViewById(R.id.imageViewCheckinMayor);
        if (mCheckinResult.getMayor().getUser() == null) {
            // I am still the mayor.
            // Just show the crown icon.
            ivMayor.setImageDrawable(getContext().getResources().getDrawable(R.drawable.crown));
        }
        else if (mCheckinResult.getMayor().getType().equals("nochange")) {
            // Someone else is mayor.
            // Show that user's photo from the network. If not already on disk,
            // we need to start a fetch for it.
            Uri photoUri = populateMayorImageFromNetwork();
            if (photoUri != null) {
                mApplication.getRemoteResourceManager().request(photoUri);
                mObserverMayorPhoto = new RemoteResourceManagerObserver();
                rrm.addObserver(mObserverMayorPhoto);
            }
        }
        else if (mCheckinResult.getMayor().getType().equals("new")) {
            // I just became the new mayor as a result of this checkin.
            // Just show the crown icon.
            ivMayor.setImageDrawable(getContext().getResources().getDrawable(R.drawable.crown));
        }
        else if (mCheckinResult.getMayor().getType().equals("stolen")) {
            // I stole mayorship from someone else as a result of this checkin.
            // Just show the crown icon.
            ivMayor.setImageDrawable(getContext().getResources().getDrawable(R.drawable.crown));
        }
    }
    
    private void addExtras(String extras) {
        
        LinearLayout llExtras = (LinearLayout)findViewById(R.id.llCheckinExtras);
        if (TextUtils.isEmpty(extras)) {
            llExtras.setVisibility(View.GONE);
            return;
        } else {
            llExtras.setVisibility(View.VISIBLE); 
        }

        // The server sent us additional content, it is base64 encoded, so decode it now.
        mExtrasDecoded = Base64Coder.decodeString(extras);
        
        // TODO: Replace with generic extras method.
        // Now when the user clicks this 'button' pop up yet another dialog dedicated
        // to showing just the webview and the decoded content. This is not ideal but
        // having problems putting a webview directly inline with the rest of the 
        // checkin content, we can improve this later.
        llExtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDlgWebViewExtras = new WebViewDialog(getContext(), "SXSW Stats", mExtrasDecoded);
                mDlgWebViewExtras.show();
            }
        });
    }
    
    /**
     * If we have to download the user's photo from the net (wasn't already in cache)
     * will return the uri to launch.
     */
    private Uri populateMayorImageFromNetwork() {
        
        User user = mCheckinResult.getMayor().getUser();
        ImageView ivMayor = (ImageView)findViewById(R.id.imageViewCheckinMayor);
        
        if (user != null) {
            Uri photoUri = Uri.parse(user.getPhoto());
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(
                    mApplication.getRemoteResourceManager().getInputStream(photoUri));
                ivMayor.setImageBitmap(bitmap);
                return null;
            } catch (IOException e) {
                // User's image wasn't already in the cache, have to start a request for it.
                if (Foursquare.MALE.equals(user.getGender())) {
                    ivMayor.setImageResource(R.drawable.blank_boy);
                } else {
                    ivMayor.setImageResource(R.drawable.blank_girl);
                }
                return photoUri;
            } 
        }

        return null;
    }
    
    /**
     * Called if the remote resource manager downloads the mayor's photo.
     * If the photo is already on disk, this observer will never be used.
     */
    private class RemoteResourceManagerObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
            if (DEBUG) Log.d(TAG, "Fetcher got: " + data);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    populateMayorImageFromNetwork(); 
                }
            });
        }
    }
}
