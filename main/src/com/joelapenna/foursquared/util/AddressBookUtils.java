/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.util;

import android.app.Activity;

/**
 * Acts as an interface to the contacts API which has changed between SDK 4 to
 * 5.
 * 
 * @date February 14, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public abstract class AddressBookUtils {
    public abstract String getAllContactsPhoneNumbers(Activity activity);

    public static AddressBookUtils addressBookUtils() {

        // TODO: Reenable use of AddressBookUtils5 when project level updated.
        return new AddressBookUtils3and4();

        /*
         * int sdk = new Integer(Build.VERSION.SDK).intValue(); if (sdk < 5) {
         * return new AddressBookUtils3and4(); } else { return new
         * AddressBookUtils5(); }
         */
    }
}
