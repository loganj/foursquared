/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Mayor;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.util.RemoteResourceManager;
import com.joelapenna.foursquared.util.StringFormatters;
import com.joelapenna.foursquared.widget.CheckinListAdapter;
import com.joelapenna.foursquared.widget.SeparatedListAdapter;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueCheckinActivity extends ListActivity {
    public static final String TAG = "VenueCheckinActivity";
    public static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private Observer mParentDataObserver = new ParentDataObserver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_checkin_activity);

        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Checkin checkin = (Checkin)parent.getAdapter().getItem(position);
                startItemActivity(checkin.getUser());
            }
        });

        VenueActivity parent = (VenueActivity)getParent();

        if (parent.venueObservable.getVenue() != null) {
            mParentDataObserver.update(parent.venueObservable, parent.venueObservable.getVenue());
        } else {
            ((VenueActivity)getParent()).venueObservable.addObserver(mParentDataObserver);
        }

        if (parent.checkinsObservable.getCheckins() != null) {
            mParentDataObserver.update(parent.checkinsObservable, parent.checkinsObservable
                    .getCheckins());
        } else {
            ((VenueActivity)getParent()).checkinsObservable.addObserver(mParentDataObserver);
        }
    }

    private void setCheckins(Group checkins) {
        if (DEBUG) Log.d(TAG, "Putting checkins in adapter.");

        setListAdapter(new SeparatedListAdapter(this));

        SeparatedListAdapter mainAdapter = (SeparatedListAdapter)getListAdapter();
        mainAdapter.clear();
        CheckinListAdapter groupAdapter = new CheckinListAdapter(//
                this, checkins, Foursquared.getUserPhotosManager(), false);
        mainAdapter.addSection("Recent Checkins", groupAdapter);
        mainAdapter.notifyDataSetInvalidated();
    }

    private void setMayor(Mayor mayor) {
        if (DEBUG) Log.d(TAG, "Setting mayor.");
        if (mayor == null) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater)getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View mayorLayout = inflater.inflate(R.layout.mayor, null);
        ((TextView)mayorLayout.findViewById(R.id.nameTextView)).setText(StringFormatters
                .getUserAbbreviatedName(mayor.getUser()));
        ((TextView)mayorLayout.findViewById(R.id.countTextView)).setText( //
                mayor.getCount() + " Checkins");

        final ImageView photo = (ImageView)mayorLayout.findViewById(R.id.photoImageView);
        final RemoteResourceManager rrm = Foursquared.getUserPhotosManager();
        final Uri photoUri = Uri.parse(mayor.getUser().getPhoto());

        if (rrm.getFile(photoUri).exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(rrm.getInputStream(photoUri));
                photo.setImageBitmap(bitmap);
            } catch (IOException e) {
                // its okay to do nothing if we can't handle loading the image.
            }
        } else {
            rrm.addObserver(new Observer() {
                @Override
                public void update(Observable observable, Object data) {
                    // XXX Not sure this will work, make it sure it does before the next
                    // release.
                    if ((Uri)data == photoUri) {
                        observable.deleteObserver(this);

                        try {
                            Bitmap bitmap = BitmapFactory
                                    .decodeStream(rrm.getInputStream(photoUri));
                            photo.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            // its okay to do nothing if we can't handle loading the image.
                        }
                    }
                }
            });
        }
        getListView().addHeaderView(mayorLayout);
    }

    private void startItemActivity(User user) {
        if (DEBUG) Log.d(TAG, "firing venue activity for venue");
        Intent intent = new Intent(VenueCheckinActivity.this, UserActivity.class);
        intent.putExtra(UserActivity.EXTRA_USER, user.getId());
        startActivity(intent);
    }

    private final class ParentDataObserver implements Observer {
        private boolean observed = false;

        @Override
        public void update(Observable observable, Object data) {
            if (DEBUG) Log.d(TAG, "Received update from: " + observable.toString());
            VenueActivity parent = (VenueActivity)getParent();
            Venue venue = parent.venueObservable.getVenue();
            Group checkins = parent.checkinsObservable.getCheckins();

            if (!observed && venue != null && checkins != null) {
                observed = true;

                if (venue.getStats() != null) {
                    setMayor(venue.getStats().getMayor());
                }

                setCheckins(checkins);
            }
        }
    }
}
