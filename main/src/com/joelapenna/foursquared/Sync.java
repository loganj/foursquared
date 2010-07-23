package com.joelapenna.foursquared;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.User;

import java.util.List;
import java.util.Observable;

/**
 * Handles all sync-related interaction with the platform.  Note that this interface is sterile-- it contains nothing
 * that will cause compatibility issues on pre-Eclair devices.
 */
public interface Sync {

    /**
     *
     * @return true if sync is turned on; false otherwise
     */
    boolean isEnabled();

    /**
     * @param enabled true to turn on syncing; false to turn it off
     * @return true if setting was updated or request was idempotent
     */
    boolean setEnabled(boolean enabled);

    /**
     *
     * @return an Observable that fires when any sync-related state changes
     */
    Observable getObservable();

    /**
     * @return operations to update the user's status on Contacts, if such update is possible; otherwise an empty List
     */
    List<ContentProviderOperation> updateStatus(ContentResolver resolver, User friend, Checkin checkin);

    /**
     * @return lookup URI for the given user, if there's a matching contact; otherwise null
     */
    Uri getContactLookupUri(ContentResolver resolver, String userId);

    /**
     * @return a task to perform sync; if sync is impossible or disabled in preferences, this task is a noop
     */
    AsyncTask<?,?,?> createSyncTask();
    
    /**
     * Perform an immediate, synchronous sync.
     *
     * TODO: return an AsyncTask
     */
    void syncFriends(Account account);
}
