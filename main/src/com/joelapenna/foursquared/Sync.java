package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.util.StringFormatters;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

final public class Sync {
    
    final private static String TAG = "Sync";
    static class RawContactDataQuery {
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

    private static final class SyncCheckinsTask extends AsyncTask<Checkin[], Void, Void> {
    
        final private ContentResolver resolver;
        
        SyncCheckinsTask(ContentResolver resolver) {
            this.resolver = resolver;
        }
        
        @Override
        protected Void doInBackground(Checkin[]... checkins) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>(checkins[0].length);
            for ( Checkin checkin : checkins[0]) {
                ops.addAll(updateStatus(resolver, checkin.getUser(), checkin));
            }
            try {
                resolver.applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (RemoteException e) {
               Log.w(UserFriendsActivity.TAG, "failed to sync to Contacts", e);
            } catch (OperationApplicationException e) {
                Log.w(UserFriendsActivity.TAG, "failed to sync to Contacts", e);
            }
            return null;
        }
        
    }
    
    static class RawContactIdQuery {
        static final String[] PROJECTION = new String[] { RawContacts._ID, RawContacts.CONTACT_ID };
        static final String SELECTION = RawContacts.ACCOUNT_TYPE+"='"+AuthenticatorService.ACCOUNT_TYPE+"'"
                                        + " AND " + RawContacts.SOURCE_ID+"=?";
        public final static int COLUMN_ID = 0;
        public final static int COLUMN_CONTACT_ID = 1;
    }

    static AsyncTask<?,?,?> startBackgroundSync(ContentResolver resolver, List<Checkin> checkins) {
        SyncCheckinsTask task = new SyncCheckinsTask(resolver);
        task.execute(checkins.toArray(new Checkin[checkins.size()]));
        return task;
    }

    private Sync() {}

    static String createStatus(Checkin checkin) {
       if ( checkin.getVenue() != null ) {
           return " @ " + checkin.getVenue().getName();
       }
       if ( checkin.getShout() != null ) {
           return "\"" + checkin.getShout() + "\"";
       }
       return StringFormatters.getCheckinMessageLine1(checkin, true);
    }

    public static List<ContentProviderOperation> updateStatus(ContentResolver resolver, User friend, Checkin checkin) {
        if ( friend == null || checkin == null ) {
            return Collections.emptyList();
        }
        long rawContactId = getRawContactId(resolver, friend);
        if ( rawContactId == 0 ) {
            return Collections.emptyList();
        }
        ArrayList<ContentProviderOperation> optionOp = new ArrayList<ContentProviderOperation>(1);
        Cursor c = resolver.query(ContactsContract.Data.CONTENT_URI, 
                Sync.RawContactDataQuery.PROJECTION, 
                Sync.RawContactDataQuery.SELECTION, 
                new String[] { String.valueOf(rawContactId) }, 
                null);
        try {
            while (c.moveToNext()) {
                long id = c.getLong(Sync.RawContactDataQuery.COLUMN_ID);
                ContentProviderOperation.Builder updateStatus = ContentProviderOperation.newInsert(ContactsContract.StatusUpdates.CONTENT_URI);
                updateStatus.withValue(ContactsContract.StatusUpdates.DATA_ID, id);
                String status = createStatus(checkin);
                updateStatus.withValue(ContactsContract.StatusUpdates.STATUS, status);
                long created = new Date(checkin.getCreated()).getTime();
                updateStatus.withValue(ContactsContract.StatusUpdates.STATUS_TIMESTAMP, created);
                optionOp.add(updateStatus.build());
            }
        } finally {
            c.close();
        }
        return optionOp;
    }

    /**
     * 
     * @return raw contact id, or 0 if not found
     */
    static long getRawContactId(ContentResolver resolver, User friend) {
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
    
    static Intent getViewContactIntent(ContentResolver resolver, User friend) {
        long contactId = 0;
        Cursor c = resolver.query(RawContacts.CONTENT_URI, 
                RawContactIdQuery.PROJECTION, 
                RawContactIdQuery.SELECTION, 
                new String[] { friend.getId() }, null);
        try {
            if (c.moveToFirst()) {
                contactId = c.getLong(RawContactIdQuery.COLUMN_CONTACT_ID);
            }
        } finally {
            if ( c != null) {
                c.close();
            }
        }
        if ( contactId == 0 ) {
            return null;
        }
        return new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, ""+contactId));
    }
    

}
