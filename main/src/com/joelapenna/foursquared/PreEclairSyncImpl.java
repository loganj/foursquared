package com.joelapenna.foursquared;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.User;

import java.util.Collections;
import java.util.List;
import java.util.Observable;

final class PreEclairSyncImpl implements Sync {

    final private Observable observable = new Observable();

    @Override
    public void validate() {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean setEnabled(boolean enabled) {
        return (!enabled);
    }

    @Override
    public Observable getObservable() {
        return observable;
    }

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
