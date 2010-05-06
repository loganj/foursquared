/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquare.types;



/**
 * @date 2010-05-05
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class FriendInvitesResult implements FoursquareType {

    /**
     * Users that are in our contact book by email or phone, are already on foursquare,
     * but are not our friends.
     */
    private Group<User> mContactsOnFoursquare;
    
    /**
     * Users not on foursquare, but in our contact book by email or phone. These are 
     * users we have not already sent an email invite to.
     */
    private Emails mContactEmailsNotOnFoursquare;
    
    /**
     * A list of email addresses we've already sent email invites to.
     */
    private Emails mContactEmailsNotOnFoursquareAlreadyInvited;
    

    public FriendInvitesResult() {
        mContactsOnFoursquare = new Group<User>();
        mContactEmailsNotOnFoursquare = new Emails();
        mContactEmailsNotOnFoursquareAlreadyInvited = new Emails();
    }

    public Group<User> getContactsOnFoursquare() {
        return mContactsOnFoursquare;
    }

    public void setContactsOnFoursquare(Group<User> contactsOnFoursquare) {
        mContactsOnFoursquare = contactsOnFoursquare;
    }
    
    public Emails getContactEmailsNotOnFoursquare() {
        return mContactEmailsNotOnFoursquare;
    }

    public void setContactEmailsOnNotOnFoursquare(Emails emails) {
        mContactEmailsNotOnFoursquare = emails;
    }
    
    public Emails getContactEmailsNotOnFoursquareAlreadyInvited() {
        return mContactEmailsNotOnFoursquareAlreadyInvited;
    }

    public void setContactEmailsOnNotOnFoursquareAlreadyInvited(Emails emails) {
        mContactEmailsNotOnFoursquareAlreadyInvited = emails;
    }
}
