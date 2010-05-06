/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.util;

import android.app.Activity;
import android.os.Build;

/**
 * Acts as an interface to the contacts API which has changed between SDK 4 to
 * 5.
 * 
 * @date February 14, 2010
 * @author Mark Wyszomierski (markww@gmail.com), foursquare.
 */
public abstract class AddressBookUtils {
    public abstract String getAllContactsPhoneNumbers(Activity activity);
    public abstract String getAllContactsEmailAddresses(Activity activity);
    public abstract AddressBookEmailBuilder getAllContactsEmailAddressesInfo(
            Activity activity);
 
    public static AddressBookUtils addressBookUtils() {

        int sdk = new Integer(Build.VERSION.SDK).intValue(); 
        if (sdk < 5) {
            return new AddressBookUtils3and4(); 
        } else { 
            return new AddressBookUtils5(); 
        }
    }
}
