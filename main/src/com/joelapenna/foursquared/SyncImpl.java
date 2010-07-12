package com.joelapenna.foursquared;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.*;
import android.preference.PreferenceManager;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.preferences.Preferences;
import com.joelapenna.foursquared.util.StringFormatters;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import java.util.*;

final class SyncImpl implements Sync {
    
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

    private final static class SyncSettingObservable extends Observable {
        @Override
        public void setChanged() {
            super.setChanged();
        }
    }

    private final class SyncCheckinsTask extends AsyncTask<Checkin[], Void, Void> {


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
    
    private static class RawContactIdQuery {
        static final String[] PROJECTION = new String[] { RawContacts._ID, RawContacts.CONTACT_ID };
        static final String SELECTION = RawContacts.ACCOUNT_TYPE+"='"+AuthenticatorService.ACCOUNT_TYPE+"'"
                                        + " AND " + RawContacts.SOURCE_ID+"=?";
        public final static int COLUMN_ID = 0;
        public final static int COLUMN_CONTACT_ID = 1;
    }
    private static class ContactLookupKeyQuery {
        static final String[] PROJECTION = new String[] { ContactsContract.Contacts.LOOKUP_KEY };
        static final String SELECTION = ContactsContract.Contacts._ID + "=?";
        public final static int COLUMN_LOOKUP_KEY = 0;
    }

    final private Context mContext;
    final private SyncSettingObservable mObservable = new SyncSettingObservable();
    private Boolean isEnabled = null;

    SyncImpl(Context context) {
        this.mContext = context;
    }

    @Override
    public AsyncTask<?,?,?> startBackgroundSync(ContentResolver resolver, List<Checkin> checkins) {
        SyncCheckinsTask task = new SyncCheckinsTask(resolver);
        task.execute(checkins.toArray(new Checkin[checkins.size()]));
        return task;
    }

    String createStatus(Checkin checkin) {
       if ( checkin.getVenue() != null ) {
           return " @ " + checkin.getVenue().getName();
       }
       if ( checkin.getShout() != null ) {
           return "\"" + checkin.getShout() + "\"";
       }
       return StringFormatters.getCheckinMessageLine1(checkin, true);
    }


    @Override
    public void validate() {
        boolean isEnabledNow = isEnabled();
        if ( (isEnabled == null) || (isEnabled != isEnabledNow) ) {
            isEnabled = isEnabledNow;
            mObservable.setChanged();
            mObservable.notifyObservers();
        }
    }

    @Override
    public boolean isEnabled() {
        String login = PreferenceManager.getDefaultSharedPreferences(mContext).getString(Preferences.PREFERENCE_LOGIN, "");     
        Account account = new Account(login, AuthenticatorService.ACCOUNT_TYPE);
        return ContentResolver.getSyncAutomatically(account, ContactsContract.AUTHORITY);
    }

    @Override
    public boolean setEnabled(boolean enabled) {
        String login = PreferenceManager.getDefaultSharedPreferences(mContext).getString(Preferences.PREFERENCE_LOGIN, "");
        Account account = new Account(login, AuthenticatorService.ACCOUNT_TYPE);
        if (enabled) {
        String password = PreferenceManager.getDefaultSharedPreferences(mContext).getString(Preferences.PREFERENCE_PASSWORD, "");
        if ("".equals(password)) {
            return false;
        }
        AccountManager.get(mContext).addAccountExplicitly(account, password, null);
        ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
        ContentProviderClient client = mContext.getContentResolver().acquireContentProviderClient(ContactsContract.AUTHORITY_URI);
        ContentValues cv = new ContentValues();
        cv.put(ContactsContract.Groups.ACCOUNT_NAME, account.name);
        cv.put(ContactsContract.Groups.ACCOUNT_TYPE, account.type);
        cv.put(ContactsContract.Settings.UNGROUPED_VISIBLE, true);
        try {
            client.insert(ContactsContract.Settings.CONTENT_URI, cv);
        } catch (RemoteException e) {
            return false;
        }
        } else {
            // TODO: callback and handler should not be null; if something goes wrong, we should not set the pref
            AccountManager.get(mContext).removeAccount(account, null, null);
        }

        if ( (isEnabled == null) || (isEnabled != enabled) ) {
            mObservable.setChanged();
            mObservable.notifyObservers();
        }
        return true;
    }

    @Override
    public Observable getObservable() {
        Log.i(TAG, "observable requested");
        return mObservable;
    }

    @Override
    public List<ContentProviderOperation> updateStatus(ContentResolver resolver, User friend, Checkin checkin) {
        if ( friend == null || checkin == null ) {
            return Collections.emptyList();
        }
        long rawContactId = getRawContactId(resolver, friend);
        if ( rawContactId == 0 ) {
            return Collections.emptyList();
        }
        ArrayList<ContentProviderOperation> optionOp = new ArrayList<ContentProviderOperation>(1);
        Cursor c = resolver.query(ContactsContract.Data.CONTENT_URI, 
                SyncImpl.RawContactDataQuery.PROJECTION,
                SyncImpl.RawContactDataQuery.SELECTION,
                new String[] { String.valueOf(rawContactId) }, 
                null);
        try {
            while (c.moveToNext()) {
                long id = c.getLong(SyncImpl.RawContactDataQuery.COLUMN_ID);
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
    long getRawContactId(ContentResolver resolver, User friend) {
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
    
    long getContactId(ContentResolver resolver, User user) {
        long contactId = 0;
        Cursor c = resolver.query(RawContacts.CONTENT_URI, 
                                  RawContactIdQuery.PROJECTION, 
                                  RawContactIdQuery.SELECTION, 
                                  new String[] { user.getId() }, null);
        try {
            if (c.moveToFirst()) {
                contactId = c.getLong(RawContactIdQuery.COLUMN_CONTACT_ID);
            }
        } finally {
            if ( c != null) {
                c.close();
            }
        }
        return contactId;
    }

    
    @Override
    public Uri getContactLookupUri(ContentResolver resolver, User user) {

        long contactId = getContactId(resolver, user);
        if ( contactId == 0 ) {
            return null;
        }
        Cursor c = resolver.query(ContactsContract.Contacts.CONTENT_URI,
                ContactLookupKeyQuery.PROJECTION,
                ContactLookupKeyQuery.SELECTION,
                new String[] { String.valueOf(contactId) }, null);
        String lookupKey = null;
        try {
            if ( c.moveToFirst() ) {
                lookupKey = c.getString(ContactLookupKeyQuery.COLUMN_LOOKUP_KEY);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        if (lookupKey == null) {
            return null;
        }
        return Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey+"/"+contactId);
    }

}
