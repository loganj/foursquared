/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquared.util;

import android.app.Activity;
import android.database.Cursor;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;

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
        StringBuilder sb = new StringBuilder(1024); 
        String[] PROJECTION = new String[] { Contacts._ID, Phone.NUMBER };
        Cursor c = activity.managedQuery(Phone.CONTENT_URI, PROJECTION, null, null, null); 
        if (c.moveToFirst()) { 
            sb.append(c.getString(1));
            while (c.moveToNext()) { 
                sb.append(",");
                sb.append(c.getString(1));
            } 
        }
        return sb.toString();
    }
    
    @Override
    public String getAllContactsEmailAddresses(Activity activity) {
        StringBuilder sb = new StringBuilder(1024); 
        String[] PROJECTION = new String[] { Email.DATA }; 
        Cursor c = activity.managedQuery(Email.CONTENT_URI, PROJECTION, null, null, null);
        if (c.moveToFirst()) { 
            sb.append(c.getString(0));
            while (c.moveToNext()) { 
                sb.append(",");
                sb.append(c.getString(0));
            }
        }
        return sb.toString();
    }
    
    @Override
    public AddressBookEmailBuilder getAllContactsEmailAddressesInfo(Activity activity) {
        String[] PROJECTION = new String[] { Contacts._ID, Contacts.DISPLAY_NAME, Email.DATA }; 
        Cursor c = activity.managedQuery(Email.CONTENT_URI, PROJECTION, null, null, null);

        // We give a list of emails: markww@gmail.com,johndoe@gmail.com,janedoe@gmail.com
        // We get back only a list of emails of users that exist on the system (johndoe@gmail.com)
        // Iterate over all those returned users, on each iteration, remove from our hashmap.
        // Can now use the left over hashmap, which is still in correct order to display invites.
        AddressBookEmailBuilder bld = new AddressBookEmailBuilder();
        if (c.moveToFirst()) {
            bld.addContact(c.getString(1), c.getString(2));
            while (c.moveToNext()) {
                bld.addContact(c.getString(1), c.getString(2));
            }
        }
        c.close();
        
        return bld;
    }
}
