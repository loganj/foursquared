/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.types.Category;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.Venue;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.util.NotificationsUtil;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class AddVenueActivity extends Activity {
    private static final String TAG = "AddVenueActivity";
    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final double MINIMUM_ACCURACY_FOR_ADDRESS = 100.0;
    
    private static final int DIALOG_PICK_CATEGORY = 1;

    private StateHolder mStateHolder = new StateHolder();

    private EditText mNameEditText;
    private EditText mAddressEditText;
    private EditText mCrossstreetEditText;
    private EditText mCityEditText;
    private EditText mStateEditText;
    private EditText mZipEditText;
    private EditText mPhoneEditText;
    private Button mAddVenueButton;
    private LinearLayout mCategoryLayout;
    private ImageView mCategoryImageView;
    private TextView mCategoryTextView;

    private ProgressDialog mDlgProgress;
    
    
    private TextWatcher mNameFieldWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mAddVenueButton.setEnabled(!TextUtils.isEmpty(s));
        }
    };

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
        setContentView(R.layout.add_venue_activity);
        registerReceiver(mLoggedOutReceiver, new IntentFilter(Foursquared.INTENT_ACTION_LOGGED_OUT));

        mAddVenueButton = (Button) findViewById(R.id.addVenueButton);
        mNameEditText = (EditText) findViewById(R.id.nameEditText);
        mAddressEditText = (EditText) findViewById(R.id.addressEditText);
        mCrossstreetEditText = (EditText) findViewById(R.id.crossstreetEditText);
        mCityEditText = (EditText) findViewById(R.id.cityEditText);
        mStateEditText = (EditText) findViewById(R.id.stateEditText);
        mZipEditText = (EditText) findViewById(R.id.zipEditText);
        mPhoneEditText = (EditText) findViewById(R.id.phoneEditText);
        mCategoryLayout = (LinearLayout) findViewById(R.id.addVenueCategoryLayout);
        mCategoryImageView = (ImageView) findViewById(R.id.addVenueCategoryIcon);
        mCategoryTextView = (TextView) findViewById(R.id.addVenueCategoryTextView);
        
        mCategoryTextView.setText("Pick a category");
        mCategoryLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(DIALOG_PICK_CATEGORY);
            }
        });
        mCategoryLayout.setEnabled(false);
        
        mAddVenueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mStateHolder.startTaskAddVenue(
                    AddVenueActivity.this,
                    new String[] {
                        mNameEditText.getText().toString(),
                        mAddressEditText.getText().toString(),
                        mCrossstreetEditText.getText().toString(),
                        mCityEditText.getText().toString(),
                        mStateEditText.getText().toString(),
                        mZipEditText.getText().toString(),
                        mZipEditText.getText().toString(),
                        mPhoneEditText.getText().toString()
                    });
            }
        });
        
        mNameEditText.addTextChangedListener(mNameFieldWatcher);

        Object retained = getLastNonConfigurationInstance();
        if (retained != null && retained instanceof StateHolder) {
            mStateHolder = (StateHolder) retained;
            mStateHolder.setActivityForTaskGetCategories(this);
            mStateHolder.setActivityForTaskAddressLookup(this);
            mStateHolder.setActivityForTaskAddVenue(this);
            
            setFields(mStateHolder.getAddressLookup());
        } else {
            mStateHolder = new StateHolder();
            mStateHolder.startTaskAddressLookup(this);
            mStateHolder.startTaskGetCategories(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((Foursquared) getApplication()).requestLocationUpdates(true);
        
        if (mStateHolder.getIsRunningTaskAddVenue()) {
            startProgressBar(
                getResources().getString(R.string.add_venue_label),
                getResources().getString(R.string.add_venue_progress_bar_message));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ((Foursquared) getApplication()).removeLocationUpdates();
        
        stopProgressBar();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mLoggedOutReceiver);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return mStateHolder;
    }

    private void setFields(AddressLookup addressLookup) {
        if (addressLookup != null && addressLookup.getAddress() != null) {

            // Don't fill in the street unless we're reasonably confident we
            // know where the user is.
            String address = addressLookup.getAddress().getAddressLine(0);
            double accuracy = addressLookup.getLocation().getAccuracy();
            if (address != null && (accuracy > 0.0 && accuracy < MINIMUM_ACCURACY_FOR_ADDRESS)) {
                if (DEBUG) Log.d(TAG, "Accuracy good enough, setting address field.");
                mAddressEditText.setText(address);
            }

            String city = addressLookup.getAddress().getLocality();
            if (city != null) {
                mCityEditText.setText(city);
            }

            String state = addressLookup.getAddress().getAdminArea();
            if (state != null) {
                mStateEditText.setText(state);
            }

            String zip = addressLookup.getAddress().getPostalCode();
            if (zip != null) {
                mZipEditText.setText(zip);
            }

            String phone = addressLookup.getAddress().getPhone();
            if (phone != null) {
                mPhoneEditText.setText(phone);
            }
        }
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
    
    private void onGetCategoriesTaskComplete(Group<Category> categories, Exception ex) {
        mStateHolder.setIsRunningTaskGetCategories(false);
        try {
            // Populate the categories list now.
            if (categories != null) {
                mStateHolder.setCategories(categories);
                mCategoryLayout.setEnabled(true);
            } else {
                // If error, feed list adapter empty user group.
                mStateHolder.setCategories(new Group<Category>());
                NotificationsUtil.ToastReasonForFailure(this, ex);
            }
        } finally {
        }
        stopIndeterminateProgressBar();
    }
    
    private void ooGetAddressLookupTaskComplete(AddressLookup addressLookup, Exception ex) {
        mStateHolder.setIsRunningTaskAddressLookup(false);
        try {
            // We can prepopulate some of the fields for them now.
            if (addressLookup != null) {
                mStateHolder.setAddressLookup(addressLookup);
                setFields(addressLookup);
            } else {
                NotificationsUtil.ToastReasonForFailure(this, ex);
            }
        } finally {
        }
        stopIndeterminateProgressBar();
    }

    private void onAddVenueTaskComplete(Venue venue, Exception ex) {
        mStateHolder.setIsRunningTaskAddVenue(false);
        try {
            // If they added the venue ok, then send them to an activity displaying it
            // so they can play around with it.
            if (venue != null) {
                Intent intent = new Intent(AddVenueActivity.this, VenueActivity.class);
                intent.putExtra(Foursquared.EXTRA_VENUE_ID, venue.getId());
                startActivity(intent);
                finish();
            } else {
                NotificationsUtil.ToastReasonForFailure(this, ex);
            }
        } finally {
        }
        stopProgressBar();
    }
    
    private void stopIndeterminateProgressBar() {
        if (mStateHolder.getIsRunningTaskAddressLookup() == false &&
            mStateHolder.getIsRunningTaskGetCategories() == false) {
            setProgressBarIndeterminateVisibility(false);
        }
    }

    private static class AddVenueTask extends AsyncTask<Void, Void, Venue> {

        private AddVenueActivity mActivity;
        private String[] mParams;
        private Exception mReason;

        public AddVenueTask(AddVenueActivity activity, String[] params) {
            mActivity = activity;
            mParams = params;
        }

        public void setActivity(AddVenueActivity activity) {
            mActivity = activity;
        }
        
        @Override
        protected void onPreExecute() {
            mActivity.startProgressBar(
                mActivity.getResources().getString(R.string.add_venue_label),
                mActivity.getResources().getString(R.string.add_venue_progress_bar_message));
        }

        @Override
        protected Venue doInBackground(Void... params) {
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();
                Location location = foursquared.getLastKnownLocationOrThrow();

                return foursquare.addVenue(
                        mParams[0],
                        mParams[1],
                        mParams[2],
                        mParams[3],
                        mParams[4],
                        mParams[5],
                        mParams[6],
                        LocationUtils.createFoursquareLocation(location));
            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, "Exception doing add venue", e);
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Venue venue) {
            if (DEBUG) Log.d(TAG, "onPostExecute()");
            if (mActivity != null) {
                mActivity.onAddVenueTaskComplete(venue, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onAddVenueTaskComplete(null, mReason);
            }
        }
    }

    private static class AddressLookupTask extends AsyncTask<Void, Void, AddressLookup> {

        private AddVenueActivity mActivity;
        private Exception mReason;

        public AddressLookupTask(AddVenueActivity activity) {
            mActivity = activity;
        }

        public void setActivity(AddVenueActivity activity) {
            mActivity = activity;
        }
        
        @Override
        protected void onPreExecute() {
            mActivity.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected AddressLookup doInBackground(Void... params) {
            try {
                Location location = ((Foursquared)mActivity.getApplication()).getLastKnownLocationOrThrow();
                Geocoder geocoder = new Geocoder(mActivity);
                return new AddressLookup(
                    location,
                    geocoder.getFromLocation(
                        location.getLatitude(), 
                        location.getLongitude(), 1).get(0));

            } catch (Exception e) {
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(AddressLookup address) {
            if (mActivity != null) {
                mActivity.ooGetAddressLookupTaskComplete(address, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.ooGetAddressLookupTaskComplete(null, mReason);
            }
        }
    }

    private static class GetCategoriesTask extends AsyncTask<Void, Void, Group<Category>> {

        private AddVenueActivity mActivity;
        private Exception mReason;

        public GetCategoriesTask(AddVenueActivity activity) {
            mActivity = activity;
        }

        public void setActivity(AddVenueActivity activity) {
            mActivity = activity;
        }
        
        @Override
        protected void onPreExecute() {
            mActivity.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Group<Category> doInBackground(Void... params) {
            try {
                Foursquared foursquared = (Foursquared) mActivity.getApplication();
                Foursquare foursquare = foursquared.getFoursquare();
                return foursquare.categories();
            } catch (Exception e) {
                if (DEBUG)
                    Log.d(TAG, "GetCategoriesTask: Exception doing send friend request.", e);
                mReason = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Group<Category> categories) {
            if (DEBUG) Log.d(TAG, "GetCategoriesTask: onPostExecute()");
            if (mActivity != null) {
                mActivity.onGetCategoriesTaskComplete(categories, mReason);
            }
        }

        @Override
        protected void onCancelled() {
            if (mActivity != null) {
                mActivity.onGetCategoriesTaskComplete(null,
                        new Exception("Get categories task request cancelled."));
            }
        }
    }

    private static class StateHolder {
        
        private AddressLookupTask mTaskGetAddress;
        private AddressLookup mAddressLookup;
        private boolean mIsRunningTaskAddressLookup;
        private GetCategoriesTask mTaskGetCategories;
        private Group<Category> mCategories;
        private boolean mIsRunningTaskGetCategories;
        private AddVenueTask mTaskAddVenue;
        private boolean mIsRunningTaskAddVenue;
        
        
        public StateHolder() {
            mCategories = new Group<Category>();
            mIsRunningTaskAddressLookup = false;
            mIsRunningTaskGetCategories = false;
            mIsRunningTaskAddVenue = false;
        }

        public void setCategories(Group<Category> categories) {
            mCategories = categories;
        }

        public void setAddressLookup(AddressLookup addressLookup) {
            mAddressLookup = addressLookup;
        }
        
        public Group<Category> getCategories() {
            return mCategories;
        }
        
        public AddressLookup getAddressLookup() {
            return mAddressLookup;
        }

        public void startTaskGetCategories(AddVenueActivity activity) {
            mIsRunningTaskGetCategories = true;
            mTaskGetCategories = new GetCategoriesTask(activity);
            mTaskGetCategories.execute();
        }

        public void startTaskAddressLookup(AddVenueActivity activity) {
            mIsRunningTaskAddressLookup = true;
            mTaskGetAddress = new AddressLookupTask(activity);
            mTaskGetAddress.execute();
        }

        public void startTaskAddVenue(AddVenueActivity activity, String[] params) {
            mIsRunningTaskAddVenue = true;
            mTaskAddVenue = new AddVenueTask(activity, params);
            mTaskAddVenue.execute();
        }

        public void setActivityForTaskGetCategories(AddVenueActivity activity) {
            if (mTaskGetCategories != null) {
                mTaskGetCategories.setActivity(activity);
            }
        }

        public void setActivityForTaskAddressLookup(AddVenueActivity activity) {
            if (mTaskGetAddress != null) {
                mTaskGetAddress.setActivity(activity);
            }
        }

        public void setActivityForTaskAddVenue(AddVenueActivity activity) {
            if (mTaskAddVenue != null) {
                mTaskAddVenue.setActivity(activity);
            }
        }
        
        public void setIsRunningTaskAddressLookup(boolean isRunning) {
            mIsRunningTaskAddressLookup = isRunning;
        }
        
        public void setIsRunningTaskGetCategories(boolean isRunning) {
            mIsRunningTaskGetCategories = isRunning;
        }

        public void setIsRunningTaskAddVenue(boolean isRunning) {
            mIsRunningTaskAddVenue = isRunning;
        }
        
        public boolean getIsRunningTaskAddressLookup() {
            return mIsRunningTaskAddressLookup;
        }

        public boolean getIsRunningTaskGetCategories() {
            return mIsRunningTaskGetCategories;
        }
        
        public boolean getIsRunningTaskAddVenue() {
            return mIsRunningTaskAddVenue;
        }
    }
    
    private static class AddressLookup {
        private Location mLocation;
        private Address mAddress;
        
        public AddressLookup(Location location, Address address) {
            mLocation = location;
            mAddress = address;
        }
        
        public Location getLocation() {
            return mLocation;
        }
        
        public Address getAddress() {
            return mAddress;
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PICK_CATEGORY:
                // When the user cancels the dialog (by hitting the 'back' key), we
                // finish this activity. We don't listen to onDismiss() for this
                // action, because a device rotation will fire onDismiss(), and our
                // dialog would not be re-displayed after the rotation is complete.
                CategoryPickerDialog dlg = new CategoryPickerDialog(
                    this, 
                    mStateHolder.getCategories(), 
                    ((Foursquared)getApplication()));
                dlg.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        setChosenCategory((CategoryPickerDialog)dialog);
                    }
                });
                return dlg;
        }
        return null;
    } 
    
    private void setChosenCategory(CategoryPickerDialog dlg) {
        Category category = dlg.getChosenCategory();
        if (category == null) {
            return;
        }
        
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(
                ((Foursquared)getApplication()).getRemoteResourceManager().getInputStream(
                    Uri.parse(category.getIconUrl())));
            mCategoryImageView.setImageBitmap(bitmap);
        } catch (IOException e) {
//            holder.photo.setImageResource(R.drawable.blank_boy);
        }
        
        mCategoryTextView.setText(category.getNodeName());
    }
}
