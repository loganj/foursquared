/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.util;

import android.app.Activity;
import android.database.Cursor;
import android.provider.Contacts;
import android.provider.Contacts.PhonesColumns;

/**
 * Implementation of address book functions for sdk levels 3 and 4.
 * 
 * @date February 14, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public class AddressBookUtils3and4 extends AddressBookUtils {
    public AddressBookUtils3and4() {
    }

    @Override
    public String getAllContactsPhoneNumbers(Activity activity) {
        StringBuilder sb = new StringBuilder(1024);

        String[] PROJECTION = new String[] {
            PhonesColumns.NUMBER
        };

        Cursor c = activity.managedQuery(Contacts.Phones.CONTENT_URI, PROJECTION, null, null,
                Contacts.Phones.DEFAULT_SORT_ORDER);

        if (c.moveToFirst()) {
            sb.append(c.getString(0));
            while (c.moveToNext()) {
                sb.append(",");
                sb.append(c.getString(0));
            }
        }
        c.close();

        return sb.toString();
    }
}
