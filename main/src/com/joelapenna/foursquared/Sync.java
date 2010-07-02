package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.ContactsSyncAdapter.RawContactIdQuery;
import com.joelapenna.foursquared.util.StringFormatters;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
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

    private static final class SyncContactsTask extends AsyncTask<User[], Void, Void> {
    
        final private ContentResolver resolver;
        
        SyncContactsTask(ContentResolver resolver) {
            this.resolver = resolver;
        }
        
        @Override
        protected Void doInBackground(User[]... friends) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>(friends[0].length);
            for ( User friend : friends[0]) {
                Log.i(UserFriendsActivity.TAG, "updating status for friend " + friend.getFirstname() + " " + friend.getLastname());
                ops.addAll(updateStatus(resolver, friend));
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
    
    static AsyncTask<?,?,?> startBackgroundSync(ContentResolver resolver, Group<User> friends) {
        SyncContactsTask task = new SyncContactsTask(resolver);
        task.execute(friends.toArray(new User[friends.size()]));
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

    public static List<ContentProviderOperation> updateStatus(ContentResolver resolver, User friend) {
        if ( friend.getCheckin() == null ) {
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
                String status = createStatus(friend.getCheckin());
                updateStatus.withValue(ContactsContract.StatusUpdates.STATUS, status);
                long created = new Date(friend.getCheckin().getCreated()).getTime();
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
                                  ContactsSyncAdapter.RawContactIdQuery.PROJECTION, 
                                  ContactsSyncAdapter.RawContactIdQuery.SELECTION, 
                                  new String[] { friend.getId() }, null);
        try {
            if (c.moveToFirst()) {
                rawContactId = c.getLong(ContactsSyncAdapter.RawContactIdQuery.COLUMN_ID);
            }
        } finally {
            if ( c != null) {
                c.close();
            }
        }
        return rawContactId;
    }
    
    

}
