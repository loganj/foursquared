package com.joelapenna.foursquared.util;

import android.util.Log;

import java.lang.reflect.Field;

final public class CompatibilityHelp {

    private CompatibilityHelp() {}
    
    public static final boolean API_LEVEL_AT_LEAST_ECLAIR = apiLevelIsAtLeastEclair();

    private static boolean apiLevelIsAtLeastEclair() {
        // we need to at least be running on cupcake for QuickContactBadges.
        boolean isAtLeastEclair = false;
        try {
            Field verField = Class.forName("android.os.Build$VERSION").getField("SDK_INT");
            int sdkInt = verField.getInt(verField);
            isAtLeastEclair = (sdkInt >= 5);
        } catch (Exception e) {
            try {
                Field verField = Class.forName("android.os.Build$VERSION").getField("SDK");
                String sdk = (String)verField.get(verField);
                isAtLeastEclair = (Integer.parseInt(sdk) >= 5);
            } catch (Exception e2) {
                isAtLeastEclair = false;
            }
        }
        Log.i("CompatibilityHelp", "api level at least eclair? " + isAtLeastEclair);
        return isAtLeastEclair;
    }
}
