package com.joelapenna.foursquared;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.User;

import java.util.Collections;
import java.util.List;

final class PreEclairSyncImpl implements Sync {

    @Override
    public List<ContentProviderOperation> updateStatus(ContentResolver resolver, User friend, Checkin checkin) {
        return Collections.emptyList();
    }

    @Override
    public Uri getContactLookupUri(ContentResolver resolver, User user) {
        return null;
    }

    @Override
    public AsyncTask<?, ?, ?> startBackgroundSync(ContentResolver resolver, List<Checkin> checkins) {
        return null;
    }
}
