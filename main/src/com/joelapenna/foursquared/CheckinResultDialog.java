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
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.util.RemoteResourceManager;
import com.joelapenna.foursquared.widget.BadgeWithIconListAdapter;
import com.joelapenna.foursquared.widget.ScoreListAdapter;
import com.joelapenna.foursquared.widget.SeparatedListAdapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
    private boolean mPopulatedMayorImage;
    private RemoteResourceManagerObserver mObserverMayorPhoto;
    private Foursquared mApplication;


    public CheckinResultDialog(Context context, CheckinResult result, Foursquared application) { 
        super(context, R.style.ThemeCustomDlgBase_ThemeCustomDlg); 
        mCheckinResult = result;
        mApplication = application;
        mHandler = new Handler();
        mPopulatedMayorImage = false;
        mObserverMayorPhoto = null;
    } 

    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
    
        setContentView(R.layout.checkin_result_dialog);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle("Checked in!");
        
        
        TextView tvMessage = (TextView)findViewById(R.id.textViewCheckinMessage);
        tvMessage.setText(mCheckinResult.getMessage());
        
        SeparatedListAdapter adapter = new SeparatedListAdapter(getContext());
        
        // Add any badges the user unlocked as a result of this checkin.
        addBadges(mCheckinResult.getBadges(), adapter, mApplication.getRemoteResourceManager());
        
        // Add whatever points they got as a result of this checkin.
        addScores(mCheckinResult.getScoring(), adapter, mApplication.getRemoteResourceManager());

        // List items construction complete.
        ListView listview = (ListView)findViewById(R.id.listViewCheckinBadgesAndScores);
        listview.setAdapter(adapter);

        // Show mayor info if any.
        addMayor(mCheckinResult.getMayor(), mApplication.getRemoteResourceManager());
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
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

        ScoreListAdapter adapter = new ScoreListAdapter(getContext(), rrm);
        adapter.setGroup(scores);
        adapterMain.addSection(getContext().getResources().getString(R.string.checkin_score), adapter);

        // Total up the scoring.
        int total = 0;
        for (Score score : scores) {
            total += Integer.parseInt(score.getPoints());
        }
        
        // Show total score section if any points received for this checkin.
        if (total > 0) {
            // TODO: Decide if we want the score section fixed in place, or just make it
            // another element in the ScoreListAdapter, either is possible.
            
            LinearLayout llScoreTotal = (LinearLayout)findViewById(R.id.llCheckinScoreTotal);
            llScoreTotal.setVisibility(View.VISIBLE);
            
            TextView tvScoreTotal = (TextView)findViewById(R.id.textViewCheckinScoreTotal);
            tvScoreTotal.setText(String.valueOf(total));
        }
    }
    
    private void addMayor(Mayor mayor, RemoteResourceManager rrm) {
        if (mayor == null) {
            return;
        }
        
        // The mayor section should be revealed in this case.
        LinearLayout llMayor = (LinearLayout)findViewById(R.id.llCheckinMayorInfo);
        llMayor.setVisibility(View.VISIBLE);
        
        // Set the mayor message.
        TextView tvMayorMessage = (TextView)findViewById(R.id.textViewCheckinMayorMessage);
        tvMayorMessage.setText(mayor.getMessage());
        
        // If the user associated with the mayor object is null, it means we're the 
        // mayor. So point this user object to ourselves.
        User user = mayor.getUser();
        if (user == null) {
            try { 
                Location location = mApplication.getLastKnownLocation();
                user = mApplication.getFoursquare().user(null, false, false, LocationUtils.createFoursquareLocation(location));

            } catch (Exception ex) {
                // TODO: Better error reporting here than just dumping to log?
                Log.e(TAG, "Error creating user object for mayor checkin section.", ex);
                return;
            }
        }
        
        // If we don't have the mayor's photo on disk, try to fetch it from the net.
        boolean populatedMayorImage = populateMayorImage();
        if (!populatedMayorImage) {
            mObserverMayorPhoto = new RemoteResourceManagerObserver();
            rrm.addObserver(mObserverMayorPhoto);
        }
    }
    
    private boolean populateMayorImage() {
        if (mPopulatedMayorImage) {
            return true;
        }
        
        if (mCheckinResult.getMayor() == null || mCheckinResult.getMayor().getUser() == null) {
            return true;
        }
        
        ImageView ivMayor = (ImageView)findViewById(R.id.imageViewCheckinMayor);
        
        User user = mCheckinResult.getMayor().getUser();
        Uri photoUri = Uri.parse(user.getPhoto());
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(mApplication.getRemoteResourceManager().getInputStream(photoUri));
            ivMayor.setImageBitmap(bitmap);
            return true;
        } catch (IOException e) {
            if (Foursquare.MALE.equals(user.getGender())) {
                ivMayor.setImageResource(R.drawable.blank_boy);
            } else {
                ivMayor.setImageResource(R.drawable.blank_girl);
            }
        }

        return false;
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
                    populateMayorImage(); 
                }
            });
        }
    }
}
