package com.joelapenna.foursquared;

import android.accounts.*;
import android.content.*;
import android.database.ContentObserver;
import android.os.Handler;
import android.preference.PreferenceManager;
import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;
import com.joelapenna.foursquared.location.LocationUtils;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

final class SyncImpl implements Sync {
    
    final private static String TAG = "Sync";

    static void syncFriends(Foursquared mFoursquared, Foursquare mFoursquare, ContentResolver resolver, AccountManager mAccountManager, Account account) {
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

        mFoursquare.setCredentials(account.name, password);
        final HashMap<String,User> friends = new HashMap<String,User>();
        final HashMap<String,Checkin> checkinsByUserId = new HashMap<String,Checkin>();

        Foursquare.Location loc = LocationUtils.createFoursquareLocation(mFoursquared.getLastKnownLocation());

        try {
            User user = mFoursquare.user(null, false, false, loc);
            friends.put(user.getId(), user);
            Group<User> friendsFromServer = mFoursquare.friends(user.getId(), loc);
            for ( User friend : friendsFromServer ) {
                Log.i(TAG, "Stashed friend " + friend.getId());
                friends.put(friend.getId(), friend);
            }
        } catch (FoursquareError e) {
            Log.e(TAG, "error fetching friends", e);
        } catch (FoursquareException e) {
            Log.e(TAG, "exception fetching friends", e);
        } catch (IOException e) {
            Log.e(TAG, "ioexception fetching friends", e);
        }

        Log.i(TAG, "got " + friends.size() + " friends from server");

        final Group<Checkin> checkins = new Group<Checkin>();
        try {
            checkins.addAll(mFoursquare.checkins(loc));
        } catch (FoursquareError e) {
            Log.e(TAG, "error fetching checkins", e);
        } catch (FoursquareException e) {
            Log.e(TAG, "error fetching checkins", e);
        } catch (IOException e) {
            Log.e(TAG, "error fetching checkins", e);
        }
        for ( Checkin checkin : checkins ) {
            checkinsByUserId.put(checkin.getUser().getId(), checkin);
        }
        
        ArrayList<ContentProviderOperation> opList = new ArrayList<ContentProviderOperation>();
        ArrayList<User> justAdded = new ArrayList<User>();
        for ( User friend : friends.values() ) {
            long rawContactId = ((SyncImpl)mFoursquared.getSync()).getRawContactId(resolver, friend);
            if ( rawContactId == 0 ) {
                opList.addAll(addContact(mFoursquared, account, friend, opList.size()));
                justAdded.add(friend);
            } else {
                 opList.addAll(updateContact(mFoursquared, resolver, rawContactId, friend, checkinsByUserId.get(friend.getId())));
            }
        }
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, opList);
        } catch (Exception e) {
            Log.e(TAG, "Something went wrong during creation!", e);
            e.printStackTrace();
        }

        opList.clear();
        for ( User friend : justAdded ) {
            Log.i(TAG, "added friend " + friend.getFirstname() + " with id " + ((SyncImpl)mFoursquared.getSync()).getRawContactId(resolver, friend));
            opList.addAll(mFoursquared.getSync().updateStatus(resolver, friend, checkinsByUserId.get(friend.getId())));
        }

        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, opList);
        } catch (Exception e) {
            Log.e(TAG, "Something went wrong while updating status for new contacts", e);
            e.printStackTrace();
        }
    }

    private static ArrayList<ContentProviderOperation> addContact(Foursquared foursquared, Account account, User friend, int backReference) {
        ArrayList<ContentProviderOperation> opList = new ArrayList<ContentProviderOperation>();
 
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
        builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
        builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
        builder.withValue(RawContacts.SYNC1, friend.getId());
        builder.withValue(RawContacts.SOURCE_ID, friend.getId());
        opList.add(builder.build());
        
        builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, backReference);
        builder.withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, friend.getFirstname());
        String last = friend.getLastname();
        if ( last != null ) {
            builder.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, friend.getLastname());
        }
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, friend.getFirstname() + (last==null ? " "+friend.getLastname() : ""));
        opList.add(builder.build());

        if ( friend.getPhone() != null ) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, backReference);
            builder.withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM);
            builder.withValue(ContactsContract.CommonDataKinds.Phone.LABEL, "foursquare");
            builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, friend.getPhone());
            opList.add(builder.build());
        }

        if ( friend.getEmail() != null ) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, backReference);
            builder.withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
            builder.withValue(ContactsContract.CommonDataKinds.Email.DATA, friend.getEmail());
            opList.add(builder.build());
        }

        if ( friend.getPhoto() != null ) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, backReference);
            builder.withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);

            try {
                Uri photoUri = Uri.parse(friend.getPhoto());
                InputStream photoIn = foursquared.getRemoteResourceManager().getInputStream(photoUri);
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
        }

        // create a Data record with custom type to point at Foursquare profile
        builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        builder.withValueBackReference(Data.RAW_CONTACT_ID, backReference);
        builder.withValue(Data.MIMETYPE, "vnd.android.cursor.item/com.joelapenna.foursquared.profile");
        builder.withValue(Data.DATA1, friend.getId());
        builder.withValue(Data.DATA2, "Foursquare Profile");
        builder.withValue(Data.DATA3, "View profile");
        opList.add(builder.build());
        
        return opList;
        
    }

    private static ArrayList<ContentProviderOperation> updateContact(Foursquared foursquared, ContentResolver resolver, long rawContactId, User friend, Checkin checkin) {
        Cursor c = resolver.query(Data.CONTENT_URI, 
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
                if ( ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    
                    // TODO: will this ever be null?  what if it's null, and we want to clear the column?
                    StringBuilder newDisplayName = new StringBuilder();

                    String contactGivenName = c.getString(RawContactDataQuery.COLUMN_GIVEN_NAME);
                    boolean changeDisplayName = false;
                    if ( friend.getFirstname() != null &&
                         !friend.getFirstname().equals(contactGivenName)) {
                        Log.i(TAG, "updating given name from '" + contactGivenName + "' to '" + friend.getFirstname() + "'");
                        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, friend.getFirstname());
                        newDisplayName.append(friend.getFirstname());
                        changeDisplayName = true;
                    } else {
                        newDisplayName.append(contactGivenName);
                    }

                    String contactFamilyName = c.getString(RawContactDataQuery.COLUMN_FAMILY_NAME);
                    if ( friend.getLastname() != null && !friend.getLastname().equals(contactFamilyName)) {
                        Log.i(TAG, "updating family name from '" + contactFamilyName + "' to '" + friend.getLastname() + "'");
                        values.put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, friend.getLastname());
                        newDisplayName.append(" ");
                        newDisplayName.append(friend.getLastname());
                        changeDisplayName = true;
                    } else if (contactFamilyName != null) {
                        newDisplayName.append(" ");
                        newDisplayName.append(contactFamilyName);
                    }

                    if ( changeDisplayName ) {
                        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, newDisplayName.toString());
                    }
                    

                } else if ( ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mimeType) ) {
                    
                    if ( friend.getPhone() != null && !friend.getPhone().equals(c.getString(RawContactDataQuery.COLUMN_PHONE_NUMBER))) {
                        Log.i(TAG, "updating phone to '" + friend.getPhone() + "'");
                        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM);
                        values.put(ContactsContract.CommonDataKinds.Phone.LABEL, "foursquare");                     
                        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, friend.getPhone());
                    }
                } else if ( ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    if ( friend.getEmail() != null && !friend.getEmail().equals(c.getString(RawContactDataQuery.COLUMN_EMAIL_ADDRESS))) {
                        Log.i(TAG, "updating email to '" + friend.getEmail() + "'");
                        values.put(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, friend.getEmail());
                    }
                }
                
                if ( values.size() > 0) {
                    ContentProviderOperation.Builder op = ContentProviderOperation.newUpdate(uri);
                    op.withValues(values);
                    Log.i(TAG, "updating " + values.size() + " values; building op");
                    ops.add(op.build());
                }
                if ( checkin != null ) {
                    ContentProviderOperation.Builder updateStatus = ContentProviderOperation.newInsert(ContactsContract.StatusUpdates.CONTENT_URI);
                    updateStatus.withValue(ContactsContract.StatusUpdates.DATA_ID, id);
                    String status = ((SyncImpl)foursquared.getSync()).createStatus(checkin);
                    updateStatus.withValue(ContactsContract.StatusUpdates.STATUS, status);
                    long created = new Date(checkin.getCreated()).getTime();
                    updateStatus.withValue(ContactsContract.StatusUpdates.STATUS_TIMESTAMP, created);
                    ops.add(updateStatus.build());
                }
            }
        } finally {
            c.close();
        }
        
        
        
        return ops;
    }

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

    /**
     * Watches for any changes to Contacts or Accounts and notifies its own observers that those changes occurred.
     * Attempts to aggregate events that occur in close temporal proximity.
     */
    private final class SyncObservable extends Observable {

        final private AtomicBoolean observerRegistered = new AtomicBoolean(false);
        final private ContentObserver observer;
        final private ContentResolver resolver;
        final private AccountManager accountManager;
        final private OnAccountsUpdateListener listener;

        final private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        final private Runnable fireAggregate = new Runnable() {
            @Override
            public void run() {

                // invalidate/refresh lookup uri cache
                Set<String> userIds = new HashSet<String>(contactLookupUriCache.keySet());
                contactLookupUriCache.clear();
                for ( String userId : userIds ) {
                    getContactLookupUri(resolver, userId);
                }

                notifyObservers();
            }
        };
        final private AtomicReference<ScheduledFuture<?>> scheduled = new AtomicReference<ScheduledFuture<?>>();

        public SyncObservable(AccountManager accountManager, ContentResolver resolver) {
            this.accountManager = accountManager;
            this.resolver = resolver;
            this.observer = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    scheduleEvent();
                }
            };
            this.listener = new OnAccountsUpdateListener() {
                @Override
                public void onAccountsUpdated(Account[] accounts) {
                    scheduleEvent();
                }
            };
        }

        private void scheduleEvent() {
            ScheduledFuture sf = scheduled.get();
            if ( sf != null ) {
                sf.cancel(false);
            }
            setChanged();
            scheduler.schedule(fireAggregate, 1, TimeUnit.SECONDS);
        }

        @Override
        public void addObserver(Observer observer) {
            super.addObserver(observer);
            ensureContentObserver();
        }

        @Override
        public void deleteObserver(Observer observer) {
            super.deleteObserver(observer);
            if (countObservers() == 0) {
                removeContentObserver();
            }
        }

        @Override
        public void deleteObservers() {
            super.deleteObservers();
            removeContentObserver();
        }

        private void ensureContentObserver() {
            synchronized(observerRegistered) {
                if ( !observerRegistered.get() ) {
                    accountManager.addOnAccountsUpdatedListener(listener, new Handler(), true);
                    Uri observe = ContactsContract.Data.CONTENT_URI;
                    resolver.registerContentObserver(observe, true, observer);
                    observerRegistered.set(true);
                }
            }
        }

        private void removeContentObserver() {
            synchronized(observerRegistered) {
                accountManager.removeOnAccountsUpdatedListener(listener);
                resolver.unregisterContentObserver(observer);
                observerRegistered.set(false);
            }
        }

    }

    private final class SyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            if (isEnabled()) {
                syncFriends(getAccount());
            }
            return (Void)null;
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

    final private Foursquared mFoursquared;
    final private Context mContext;
    final private SyncObservable mObservable;
    private Boolean isEnabled = null;
    final private Map<String,Uri> contactLookupUriCache = new ConcurrentHashMap<String,Uri>();    

    SyncImpl(Foursquared foursquared) {
        this.mFoursquared = foursquared;
        this.mContext = foursquared;
        this.mObservable = new SyncObservable(AccountManager.get(mContext), mContext.getContentResolver());
    }

    @Override
    public AsyncTask<?,?,?> createSyncTask() {
        return new SyncTask();
    }

    @Override
    public void syncFriends(Account account) {
        syncFriends(mFoursquared, mFoursquared.getFoursquare(), mContext.getContentResolver(), AccountManager.get(mContext), account);
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


    private Account getAccount() {
        String login = PreferenceManager.getDefaultSharedPreferences(mContext).getString(Preferences.PREFERENCE_LOGIN, "");
        return new Account(login, AuthenticatorService.ACCOUNT_TYPE);
    }

    @Override
    public boolean isEnabled() {
        return ContentResolver.getSyncAutomatically(getAccount(), ContactsContract.AUTHORITY);
    }

    @Override
    public boolean setEnabled(boolean enabled) {
        Account account = getAccount();
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

//        if ( (isEnabled == null) || (isEnabled != enabled) ) {
//            mObservable.setChanged();
//            mObservable.notifyObservers();
//        }
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
    
    long getContactId(ContentResolver resolver, String userId) {
        long contactId = 0;
        Cursor c = resolver.query(RawContacts.CONTENT_URI, 
                                  RawContactIdQuery.PROJECTION, 
                                  RawContactIdQuery.SELECTION, 
                                  new String[] { userId }, null);
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
    public Uri getContactLookupUri(ContentResolver resolver, String userId) {

        if ( contactLookupUriCache.containsKey(userId) ) {
            return contactLookupUriCache.get(userId);
        }
        
        long contactId = getContactId(resolver, userId);
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
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey+"/"+contactId);
        contactLookupUriCache.put(userId, uri);
        return uri;
    }

}
