/**
 * Copyright 2010 Mark Wyszomierski
 */

package com.joelapenna.foursquare.util;

import android.os.Parcel;

/**
 * @date March 25, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class ParcelUtils {

    public static void writeStringToParcel(Parcel out, String str) {
        if (str != null) {
            out.writeInt(1);
            out.writeString(str);
        } else {
            out.writeInt(0);
        }
    }
    
    public static String readStringFromParcel(Parcel in) {
        int flag = in.readInt();
        if (flag == 1) {
            return in.readString();
        } else {
            return null;
        }
    }
}