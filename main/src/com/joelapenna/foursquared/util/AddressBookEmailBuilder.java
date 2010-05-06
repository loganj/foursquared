package com.joelapenna.foursquared.util;

import com.joelapenna.foursquare.types.Group;
import com.joelapenna.foursquare.types.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles building an internal list of all email addresses as both a comma
 * separated list, and as a linked hash map for use with email invites. The
 * internal map is kept for pruning when we get a list of contacts which are
 * already foursquare users back from the invite api method. Note that after
 * the prune method is called, the internal mEmailsCommaSeparated memeber may
 * be out of sync with the contents of the other maps.
 * 
 * @date April 26, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 *
 */
public class AddressBookEmailBuilder {
    /** 
     * Keeps all emails as a flat comma separated list for use with the 
     * API findFriends method. 
     */
    private StringBuilder mEmailsCommaSeparated;
    
    /**
     * Links a single email address to a single contact name. 
     */
    private LinkedHashMap<String, String> mEmailsToNames;
    
    /**
     * Links a single contact name to multiple email addresses.
     */
    private HashMap<String, HashSet<String>> mNamesToEmails;
    
    
    public AddressBookEmailBuilder() {
        mEmailsCommaSeparated = new StringBuilder();
        mEmailsToNames = new LinkedHashMap<String, String>();
        mNamesToEmails = new HashMap<String, HashSet<String>>();
    }
    
    public void addContact(String contactName, String contactEmail) {
        // Email addresses should be uniquely tied to a single contact name.
        mEmailsToNames.put(contactEmail, contactName);
        
        // Reverse link, a single contact can have multiple email addresses.
        HashSet<String> emailsForContact = mNamesToEmails.get(contactName);
        if (emailsForContact == null) {
            emailsForContact = new HashSet<String>();
            mNamesToEmails.put(contactName, emailsForContact);
        }
        emailsForContact.add(contactEmail);
        
        // Keep building the comma separated flat list of email addresses.
        if (mEmailsCommaSeparated.length() > 0) {
            mEmailsCommaSeparated.append(",");
        }
        mEmailsCommaSeparated.append(contactEmail);
    }
    
    public String getEmailsCommaSeparated() {
        return mEmailsCommaSeparated.toString();
    }
    
    public void pruneEmailsAndNames(Group<User> group) {
        if (group != null) {
            for (User it : group) {
                // Get the contact name this email address belongs to.
                String contactName = mEmailsToNames.get(it.getEmail());
                if (contactName != null) {
                    Set<String> allEmailsForContact = mNamesToEmails.get(contactName);
                    if (allEmailsForContact != null) {
                        for (String jt : allEmailsForContact) {
                            // Get rid of these emails from the master list.
                            mEmailsToNames.remove(jt);
                        }
                    }
                }
            }
        }
    }
    
    /** Returns the map as a list of [email, name] pairs. */
    public List<ContactSimple> getEmailsAndNamesAsList() {
        List<ContactSimple> list = new ArrayList<ContactSimple>();
        for (Map.Entry<String, String> it : mEmailsToNames.entrySet()) {
            ContactSimple contact = new ContactSimple();
            contact.mName = it.getValue();
            contact.mEmail = it.getKey();
            list.add(contact);
        }
        
        return list;
    }
    
    public String getNameForEmail(String email) {
        return mEmailsToNames.get(email);
    }
    
    public String toStringCurrentEmails() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("Current email contents:\n");
        for (Map.Entry<String, String> it : mEmailsToNames.entrySet()) {
            sb.append(it.getValue()); sb.append("   "); sb.append(it.getKey());
            sb.append("\n");
        }
        return sb.toString();
    }
    
    public static void main(String[] args) {
        AddressBookEmailBuilder bld = new AddressBookEmailBuilder();
        bld.addContact("john", "john@google.com");
        bld.addContact("john", "john@hotmail.com");
        bld.addContact("john", "john@yahoo.com");
        bld.addContact("jane", "jane@blah.com");
        bld.addContact("dave", "dave@amazon.com");
        bld.addContact("dave", "dave@earthlink.net");
        bld.addContact("sara", "sara@odwalla.org");
        bld.addContact("sara", "sara@test.com");
        
        System.out.println("Comma separated list of emails addresses:");
        System.out.println(bld.getEmailsCommaSeparated());
        
        Group<User> users = new Group<User>();
        
        User userJohn = new User();
        userJohn.setEmail("john@hotmail.com");
        users.add(userJohn);

        User userSara = new User();
        userSara.setEmail("sara@test.com");
        users.add(userSara);
        
        bld.pruneEmailsAndNames(users);
        System.out.println(bld.toStringCurrentEmails());
    }
    
    public static class ContactSimple {
        public String mName;
        public String mEmail;
    }
}