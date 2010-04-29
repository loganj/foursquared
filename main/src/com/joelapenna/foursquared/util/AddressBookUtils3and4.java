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
    
    @Override
    public String getAllContactsEmailAddresses(Activity activity) {
        StringBuilder sb = new StringBuilder(1024);
        
        String[] PROJECTION = new String[] { 
            Contacts.ContactMethods.DATA
        }; 

        Cursor c = activity.managedQuery(
                Contacts.ContactMethods.CONTENT_EMAIL_URI, 
                PROJECTION, null, null, 
                Contacts.ContactMethods.DEFAULT_SORT_ORDER); 
        
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
    
    @Override
    public AddressBookEmailBuilder getAllContactsEmailAddressesInfo(Activity activity) {

        String[] PROJECTION = new String[] { 
            Contacts.PeopleColumns.NAME,
            Contacts.ContactMethods.DATA
        }; 

        Cursor c = activity.managedQuery(
                Contacts.ContactMethods.CONTENT_EMAIL_URI, 
                PROJECTION, null, null, 
                Contacts.ContactMethods.DEFAULT_SORT_ORDER); 
        
        // We give a list of emails: markww@gmail.com,johndoe@gmail.com,janedoe@gmail.com
        // We get back only a list of emails of users that exist on the system (johndoe@gmail.com)
        // Iterate over all those returned users, on each iteration, remove from our hashmap.
        // Can now use the left over hashmap, which is still in correct order to display invites.
        
        AddressBookEmailBuilder bld = new AddressBookEmailBuilder();
        if (c.moveToFirst()) {
            bld.addContact(c.getString(0), c.getString(1));
            while (c.moveToNext()) {
                bld.addContact(c.getString(0), c.getString(1));
            }
        }
        c.close();
        
        return bld;
    }
}
