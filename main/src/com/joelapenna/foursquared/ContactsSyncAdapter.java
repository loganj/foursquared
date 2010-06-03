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
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
        
        ArrayList<ContentProviderOperation> opList = null;
        int backReference = 0;
        for ( User friend : friends ) {
            long rawContactId = getRawContactId(resolver, friend);
            if ( rawContactId == 0 ) {
                Log.i(TAG, "adding friend " + friend.getId() + " (" + friend.getFirstname() + " " + friend.getLastname() + ")");
                opList = addContact(account, friend, backReference);
            } else {
                Log.i(TAG, "updating contact " + rawContactId + " for friend " + friend.getFirstname() + " " + friend.getLastname() + ")");
                 opList = updateContact(resolver, rawContactId, friend);
            }
//            backReference++;
            try {
                mContentResolver.applyBatch(ContactsContract.AUTHORITY, opList);
            } catch (Exception e) {
                Log.e(TAG, "Something went wrong during creation! " + e);
                e.printStackTrace();
            }        
            
        }
        
        // TODO: just need to do deletes now
        

        // can't do each friend as one go because of backreferences.  I think.
       
        

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
    
    private ArrayList<ContentProviderOperation> addContact(Account account, User friend, int backReference) {
        ArrayList<ContentProviderOperation> opList = new ArrayList<ContentProviderOperation>();
 
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
        builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
        builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
        builder.withValue(RawContacts.SYNC1, friend.getId());
        builder.withValue(RawContacts.SOURCE_ID, friend.getId());
        opList.add(builder.build());
        
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, backReference);
        builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, friend.getFirstname()+" "+friend.getLastname());
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, friend.getFirstname());
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, friend.getLastname());
        opList.add(builder.build());
        
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, backReference);
        builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, friend.getPhone());
        opList.add(builder.build());
        
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, backReference);
        builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
        builder.withValue(ContactsContract.CommonDataKinds.Email.DATA, friend.getEmail());
        opList.add(builder.build());

        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, backReference);
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
        builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReference);
        builder.withValue(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/com.joelapenna.foursquared.profile");
        builder.withValue(ContactsContract.Data.DATA1, friend.getId());
        builder.withValue(ContactsContract.Data.DATA2, "Foursquare Profile");
        builder.withValue(ContactsContract.Data.DATA3, "View profile");
        opList.add(builder.build());
        
        return opList;
        
    }
    
    private ArrayList<ContentProviderOperation> updateContact(ContentResolver resolver, long rawContactId, User friend) {
        Cursor c = resolver.query(ContactsContract.Data.CONTENT_URI, 
                                  RawContactDataQuery.PROJECTION, 
                                  RawContactDataQuery.SELECTION, 
                                  new String[] { String.valueOf(rawContactId) }, 
                                  null);
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        Log.i(TAG, "updateContact passed rawContactId=" + rawContactId);
        try {
            while (c.moveToNext()) {
                Log.i(TAG, "processing row with raw_contact_id=" + c.getLong(5));
                Uri uri = ContentUris.withAppendedId(Data.CONTENT_URI, rawContactId);
                long id = c.getLong(RawContactDataQuery.COLUMN_ID);
                String mimeType = c.getString(RawContactDataQuery.COLUMN_MIMETYPE);
                ContentValues values = new ContentValues();
                if ( StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    
                    // TODO: will this ever be null?  what if it's null, and we want to clear the column?
                    String contactFamilyName = c.getString(RawContactDataQuery.COLUMN_FAMILY_NAME);
                    if ( friend.getLastname() != null && !friend.getLastname().equals(contactFamilyName)) {
                        Log.i(TAG, "updating family name from '" + contactFamilyName + "' to '" + friend.getLastname() + "'");
                        values.put(StructuredName.FAMILY_NAME, friend.getLastname());
                    }
                    
                    String contactGivenName = c.getString(RawContactDataQuery.COLUMN_GIVEN_NAME);
                    if ( friend.getFirstname() != null &&
                         !friend.getFirstname().equals(contactGivenName)) {
                        Log.i(TAG, "updating given name from '" + contactGivenName + "' to '" + friend.getFirstname() + "'");
                        values.put(StructuredName.GIVEN_NAME, friend.getFirstname());
                    }
                } else if ( Phone.CONTENT_ITEM_TYPE.equals(mimeType) ) {
                    
                    if ( friend.getPhone() != null && !friend.getPhone().equals(c.getString(RawContactDataQuery.COLUMN_PHONE_NUMBER))) {
                        Log.i(TAG, "updating phone to '" + friend.getPhone() + "'");
                        values.put(Phone.NUMBER, friend.getPhone());
                    }
                } else if ( Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    if ( friend.getEmail() != null && !friend.getEmail().equals(c.getString(RawContactDataQuery.COLUMN_EMAIL_ADDRESS))) {
                        Log.i(TAG, "updating email to '" + friend.getEmail() + "'");
                        values.put(Email.CONTENT_ITEM_TYPE, friend.getEmail());
                    }
                }
                
                if ( values.size() > 0) {
                    ContentProviderOperation.Builder op = ContentProviderOperation.newUpdate(uri);
                    op.withValues(values);
                    Log.i(TAG, "updating " + values.size() + " values; building op");
                    ops.add(op.build());
                }
            }
        } finally {
            c.close();
        }
        return ops;
    }
    
    private static class RawContactDataQuery {
        final static String[] PROJECTION = new String[] { Data._ID, Data.MIMETYPE, Data.DATA1, Data.DATA2, Data.DATA3, Data.RAW_CONTACT_ID };
        final static String SELECTION = Data.RAW_CONTACT_ID + "=?";

        final static int COLUMN_ID = 0;
        final static int COLUMN_MIMETYPE = 1;
        final static int COLUMN_DATA1 = 2;
        final static int COLUMN_DATA2 = 3;
        final static int COLUMN_DATA3 = 4;
        final static int COLUMN_PHONE_NUMBER = COLUMN_DATA1;
        final static int COLUMN_EMAIL_ADDRESS = COLUMN_DATA1;
        final static int COLUMN_GIVEN_NAME = COLUMN_DATA2;
        final static int COLUMN_FAMILY_NAME = COLUMN_DATA3;

    }

}
