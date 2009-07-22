/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.UserTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueCheckinActivity extends ListActivity {
    public static final String TAG = "VenueCheckinActivity";
    public static final boolean DEBUG = Foursquared.DEBUG;

    private static final int DIALOG_CHECKIN = 0;

    private Venue mVenue;
    private Button mCheckinButton;
    public Checkin mCheckin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_checkin_activity);

        setListAdapter(new TipsListAdapter(this));

        mCheckinButton = (Button)findViewById(R.id.checkinButton);
        mCheckinButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sendCheckin();
            }
        });

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        ToggleButton silentToggle = (ToggleButton)findViewById(R.id.silentToggle);
        silentToggle.setChecked(settings.getBoolean(Foursquared.PREFERENCE_SILENT_CHECKIN, false));

        ToggleButton twitterToggle = (ToggleButton)findViewById(R.id.twitterToggle);
        twitterToggle.setChecked(settings.getBoolean(Foursquared.PREFERENCE_TWITTER_CHECKIN, false));

        setVenue((Venue)getIntent().getExtras().get(Foursquared.EXTRAS_VENUE_KEY));
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CHECKIN:
                Checkin checkin = mCheckin;
                if (DEBUG) Log.d(TAG, checkin.getUserid());
                if (DEBUG) Log.d(TAG, checkin.getMessage());
                if (DEBUG) Log.d(TAG, String.valueOf(checkin.status()));
                if (DEBUG) Log.d(TAG, checkin.getUrl());

                WebView webView = new WebView(this);
                webView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                        LayoutParams.FILL_PARENT));
                webView.loadUrl(checkin.getUrl());
                Spanned title = Html.fromHtml(checkin.getMessage());
                return new AlertDialog.Builder(this) // the builder
                        .setView(webView) // use a web view
                        .setIcon(android.R.drawable.ic_dialog_info) // show an icon
                        .setTitle(title).create(); // return it.
        }
        return null;
    }

    private void sendCheckin() {
        new CheckinUserTask().execute(new Venue[] {
            mVenue
        });
    }

    private void setVenue(Venue venue) {
        mVenue = venue;
    }

    class CheckinUserTask extends UserTask<Venue, Void, Checkin> {

        @Override
        public void onPreExecute() {
            mCheckinButton.setEnabled(false);
        }

        @Override
        public Checkin doInBackground(Venue... params) {
            try {
                final Venue venue = params[0];
                if (DEBUG) Log.d(TAG, "Checking in to: " + venue.getVenuename());

                boolean silent = ((ToggleButton)findViewById(R.id.silentToggle)).isChecked();
                boolean twitter = ((ToggleButton)findViewById(R.id.twitterToggle)).isChecked();
                Location location = ((Foursquared)getApplication()).getLocation();
                if (location == null) {
                    return ((Foursquared)getApplication()).getFoursquare().checkin(
                            venue.getVenuename(), silent, twitter, null, null);
                } else {
                    // I wonder if this could result in the backend logic to mis-calculate which
                    // venue you're at because the phone gave too coarse or inaccurate location
                    // information.
                    return ((Foursquared)getApplication()).getFoursquare().checkin(
                            venue.getVenuename(), silent, twitter,
                            String.valueOf(location.getLatitude()),
                            String.valueOf(location.getLongitude()));
                }
            } catch (FoursquareError e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareError", e);
            } catch (FoursquareParseException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "FoursquareParseException", e);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if (DEBUG) Log.d(TAG, "IOException", e);
            }
            return null;
        }

        @Override
        public void onPostExecute(Checkin checkin) {
            try {
                mCheckin = checkin;
                if (checkin == null) {
                    mCheckinButton.setEnabled(true);
                    Toast.makeText(VenueCheckinActivity.this, "Unable to checkin! (FIX THIS!)",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                showDialog(DIALOG_CHECKIN);
            } finally {
                // Anything else?
            }
        }
    }
}
