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
     * Check for any changes that may have occurred.  Probably want to call in onResume() in your Activity.
     *
     * This isn't necessary from Froyo on, because we can register a listener on the ContentResolver for sync setting
     * changes.  Pre-Froyo devices would still have to call this though.
     */
    void validate();

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
     * @return an already-started task to perform sync, or null if such a task is impossible
     */
    AsyncTask<?,?,?> syncCheckins(ContentResolver resolver, List<Checkin> checkins);

    /**
     * Perform an immediate, synchronous sync.
     *
     * TODO: return an AsyncTask
     */
    void syncFriends(Account account);
}
