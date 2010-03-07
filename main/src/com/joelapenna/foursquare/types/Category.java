/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquare.types;

/**
 * @date March 6, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class Category implements FoursquareType {

    /** The category's id. */
    private String mId;

    /** Full category path name, like Nightlife:Bars. */
    private String mFullPathName;

    /** Simple name of the category. */
    private String mNodeName;

    /** Url of the icon associated with this category. */
    private String mIconUrl;

    public Category() {
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getFullPathName() {
        return mFullPathName;
    }

    public void setFullPathName(String fullPathName) {
        mFullPathName = fullPathName;
    }

    public String getNodeName() {
        return mNodeName;
    }

    public void setNodeName(String nodeName) {
        mNodeName = nodeName;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public void setIconUrl(String iconUrl) {
        mIconUrl = iconUrl;
    }
}
