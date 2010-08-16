/**
 * Copyright 2010 Mark Wyszomierski
 */
package com.joelapenna.foursquared.util;

import android.media.ExifInterface;
import android.text.TextUtils;

/**
 * @date July 24, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 */
public class ExifUtils
{
    private ExifUtils() {
    }
    
    public static int getExifRotation(String imgPath) {
        try {
            ExifInterface exif = new ExifInterface(imgPath);
            String rotationAmount = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            if (!TextUtils.isEmpty(rotationAmount)) {
                int rotationParam = Integer.parseInt(rotationAmount);
                switch (rotationParam) {
                    case ExifInterface.ORIENTATION_NORMAL:
                        return 0;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        return 90;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        return 180;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        return 270;
                    default:
                        return 0;
                }
            } else {
                return 0;
            }
        } catch (Exception ex) {
            return 0;
        }
    }
}