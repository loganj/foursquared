package com.joelapenna.foursquared;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import com.joelapenna.foursquared.preferences.Preferences;

class SyncPreferenceChangeListener implements Preference.OnPreferenceChangeListener {

    final private Account mAccount;

    public SyncPreferenceChangeListener(Account account) {
        this.mAccount = account;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Boolean on = (Boolean)newValue;
        Context context = preference.getContext();
        if ( on ) {
            String password = PreferenceManager.getDefaultSharedPreferences(context).getString(Preferences.PREFERENCE_PASSWORD, "");
            if ("".equals(password)) {
                return false;
            }
            AccountManager.get(context).addAccountExplicitly(mAccount, password, null);
            ContentResolver.setSyncAutomatically(mAccount, ContactsContract.AUTHORITY, true);
            ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(ContactsContract.AUTHORITY_URI);
            ContentValues cv = new ContentValues();
            cv.put(ContactsContract.Groups.ACCOUNT_NAME, mAccount.name);
            cv.put(ContactsContract.Groups.ACCOUNT_TYPE, mAccount.type);
            cv.put(ContactsContract.Settings.UNGROUPED_VISIBLE, true);
            try {
                client.insert(ContactsContract.Settings.CONTENT_URI, cv);
            } catch (RemoteException e) {
                return false;
            }
        } else {
            // TODO: callback and handler should not be null; if something goes wrong, we should not set the pref
            AccountManager.get(context).removeAccount(mAccount, null, null);
        }
        return true;
    }
}
