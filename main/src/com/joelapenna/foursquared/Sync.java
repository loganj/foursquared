package com.joelapenna.foursquared;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.User;

import java.util.List;

public interface Sync {

    /**
     * @return operations to update the user's status on Contacts, if such update is possible; otherwise an empty List
     */
    List<ContentProviderOperation> updateStatus(ContentResolver resolver, User friend, Checkin checkin);

    /**
     * @return lookup URI for the given user, if there's a matching contact; otherwise null
     */
    Uri getContactLookupUri(ContentResolver resolver, User user);

    /**
     * @return an already-started task to perform sync, or null if such a task is impossible
     */
    AsyncTask<?,?,?> startBackgroundSync(ContentResolver resolver, List<Checkin> checkins);
}
