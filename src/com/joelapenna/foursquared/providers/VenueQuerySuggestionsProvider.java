/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.providers;

import android.content.SearchRecentSuggestionsProvider;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueQuerySuggestionsProvider extends SearchRecentSuggestionsProvider {

    public static final String AUTHORITY = "com.joelapenna.foursquared.providers.VenueQuerySuggestionsProvider";
    public static final int MODE = DATABASE_MODE_QUERIES;

    public VenueQuerySuggestionsProvider() {
        super();
        setupSuggestions(AUTHORITY, MODE);
    }
}
