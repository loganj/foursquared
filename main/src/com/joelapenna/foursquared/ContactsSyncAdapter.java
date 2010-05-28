package com.joelapenna.foursquared;

import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.location.LocationUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ContactsSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "ContactsSyncAdapter";

    final private Foursquared mFoursquared;
    final private AccountManager mAccountManager;
    final private ContentResolver mContentResolver;
    
    public ContactsSyncAdapter(Foursquared foursquared, Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mFoursquared = foursquared;
        mAccountManager = AccountManager.get(context);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
            SyncResult syncResult) {
        
        String password = null;
        try {
            Log.i(TAG, "getting password from account manager");
            password = mAccountManager.blockingGetAuthToken(account, AuthenticatorService.ACCOUNT_TYPE, true);
            
        } catch (OperationCanceledException e) {
            Log.w(TAG, "operation cancelled while getting auth token", e);
        } catch (AuthenticatorException e) {
            Log.e(TAG, "authenticator exception while getting auth token", e);
        } catch (IOException e) {
            Log.e(TAG, "ioexception while getting auth token", e);
        }
        
        final Group<User> friends = new Group<User>();
        
        try {
            friends.addAll(mFoursquared.getFoursquare().friends(mFoursquared.getUserId(), LocationUtils.createFoursquareLocation(mFoursquared.getLastKnownLocation())));
        } catch (FoursquareError e) {
            Log.e(TAG, "error fetching friends", e);
        } catch (FoursquareException e) {
            Log.e(TAG, "exception fetching friends", e);
        } catch (IOException e) {
            Log.e(TAG, "ioexception fetching friends", e);
        }
        
        Log.i(TAG, "got " + friends.size() + " friends from server");
        
        ContentResolver resolver = getContext().getContentResolver();
        // TODO: sync correctly
        // TODO: double check that we're fetching *all* friends from foursquare
        // partition friend and contact sets into three sets:
        // contacts - friends: need to be deleted
        // intersection: need to be updated
        
        for ( User friend : friends ) {
            long rawContactId = getRawContactId(resolver, friend);
            if ( rawContactId == 0 ) {
                Log.i(TAG, "adding friend " + friend.getId() + " (" + friend.getFirstname() + " " + friend.getLastname() + ")");
                addContact(account, friend);
            } else {
                // updateContact()
            }     
        }
        

        // friends - contacts: need to be added
        
    }
    
    /**
     * 
     * @return raw contact id, or 0 if not found
     */
    private long getRawContactId(ContentResolver resolver, User friend) {
        long rawContactId = 0;
        Cursor c = resolver.query(RawContacts.CONTENT_URI, 
                                  RawContactIdQuery.PROJECTION, 
                                  RawContactIdQuery.SELECTION, 
                                  new String[] { friend.getId() }, null);
        try {
            if (c.moveToFirst()) {
                rawContactId = c.getLong(RawContactIdQuery.COLUMN_ID);
            }
        } finally {
            if ( c != null) {
                c.close();
            }
        }
        return rawContactId;
    }
    
    private static class RawContactIdQuery {
        static final String[] PROJECTION = new String[] { RawContacts._ID };
        static final String SELECTION = RawContacts.ACCOUNT_TYPE+"='"+AuthenticatorService.ACCOUNT_TYPE+"'"
                                        + " AND " + RawContacts.SOURCE_ID+"=?";
        public final static int COLUMN_ID = 0;
    }
    
    private void addContact(Account account, User friend) {
        ArrayList<ContentProviderOperation> opList = new ArrayList<ContentProviderOperation>();
 
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
        builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
        builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
        builder.withValue(RawContacts.SYNC1, friend.getId());
        opList.add(builder.build());
        
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, friend.getFirstname()+" "+friend.getLastname());
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, friend.getFirstname());
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, friend.getLastname());
        opList.add(builder.build());
        
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, friend.getPhone());
        opList.add(builder.build());
        
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
        builder.withValue(ContactsContract.CommonDataKinds.Email.DATA, friend.getEmail());
        opList.add(builder.build());

        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
        
        try {
            Uri photoUri = Uri.parse(friend.getPhoto());
            InputStream photoIn = mFoursquared.getRemoteResourceManager().getInputStream(photoUri);
            ByteArrayOutputStream photoOut = new ByteArrayOutputStream();
            byte[] buf = new byte[64];
            int r = 0;
            while ( (r = photoIn.read(buf)) >= 0) {
                photoOut.write(buf, 0, r);
            }
            byte[] photoBytes = photoOut.toByteArray();
            builder.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBytes);
        } catch (IOException e) {
            Log.w(TAG, "failed to fetch or read friend photo", e);
        }
        opList.add(builder.build());


//        friend.getFacebook();
//        friend.getTwitter();

        // create a Data record with custom type to point at Foursquare profile
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/com.joelapenna.foursquared.profile");
        builder.withValue(ContactsContract.Data.DATA1, friend.getId());
        builder.withValue(ContactsContract.Data.DATA2, "Foursquare Profile");
        builder.withValue(ContactsContract.Data.DATA3, "View profile");
        opList.add(builder.build());
        
        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, opList);
        } catch (Exception e) {
            Log.e(TAG, "Something went wrong during creation! " + e);
            e.printStackTrace();
        }        
        
    }

}
