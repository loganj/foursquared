/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.util;

import android.app.Activity;

/**
 * Implementation of address book functions for sdk level 5 and above.
 * 
 * @date February 14, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public class AddressBookUtils5 extends AddressBookUtils {
    public AddressBookUtils5() {
    }

    @Override
    public String getAllContactsPhoneNumbers(Activity activity) {

        throw new IllegalStateException(
                "AddressBookUtils5.getAllContactsPhoneNumbers() not implemented.");

        // TODO: Need to update project to API 5 to use new contacts api.
        /*
         * StringBuilder sb = new StringBuilder(1024); String[] PROJECTION = new
         * String[] { Contacts._ID, Contacts.DISPLAY_NAME, Phone.NUMBER };
         * Cursor c = activity.managedQuery(Phone.CONTENT_URI, PROJECTION, null,
         * null, null); if (c.moveToFirst()) { while (c.moveToNext()) { } }
         * return sb.toString();
         */
    }
}
