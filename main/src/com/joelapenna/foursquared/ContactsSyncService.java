package com.joelapenna.foursquared;

import android.accounts.Account;
import android.app.Service;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;

public class ContactsSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static ContactsSyncAdapter sSyncAdapter = null;


    @Override
    public void onCreate() {
        synchronized(sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new ContactsSyncAdapter((Foursquared)getApplication(), getApplicationContext(), true);
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

    final private class ContactsSyncAdapter extends AbstractThreadedSyncAdapter {

        final private Foursquared mFoursquared;

        public ContactsSyncAdapter(Foursquared foursquared, Context context, boolean autoInitialize) {
            super(context, autoInitialize);
            mFoursquared = foursquared;
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
                SyncResult syncResult) {
            mFoursquared.getSync().syncFriends(account);
        }
    }
}
