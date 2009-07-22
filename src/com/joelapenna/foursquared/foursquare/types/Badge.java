/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.foursquare.types;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class Badge {

    private String mName;
    private String mIcon;
    private String mDescription;

    /**
     * @param Name the Name to set
     */
    public void setName(String mName) {
        this.mName = mName;
    }

    /**
     * @return the Name
     */
    public String getName() {
        return mName;
    }

    /**
     * @param Icon the Icon to set
     */
    public void setIcon(String mIcon) {
        this.mIcon = mIcon;
    }

    /**
     * @return the Icon
     */
    public String getIcon() {
        return mIcon;
    }

    /**
     * @param Description the Description to set
     */
    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    /**
     * @return the Description
     */
    public String getDescription() {
        return mDescription;
    }

}
