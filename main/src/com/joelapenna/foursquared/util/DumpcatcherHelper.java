/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import com.googlecode.dumpcatcher.logging.Dumpcatcher;
import com.googlecode.dumpcatcher.logging.DumpcatcherUncaughtExceptionHandler;
import com.googlecode.dumpcatcher.logging.StackFormattingUtil;
import com.joelapenna.foursquared.FoursquaredSettings;
import com.joelapenna.foursquared.R;

import org.apache.http.HttpResponse;

import android.content.res.Resources;
import android.util.Log;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class DumpcatcherHelper {
    private static final String TAG = "DumpcatcherHelper";

    private static final boolean DEBUG = FoursquaredSettings.DEBUG;

    private static final ExecutorService mExecutor = Executors.newFixedThreadPool(2);

    private static Dumpcatcher sDumpcatcher;

    private static String sClient;

    public DumpcatcherHelper(String client, Resources resources) {
        sClient = client;
        setupDumpcatcher(resources);
    }

    public static void setupDumpcatcher(Resources resources) {
        if (FoursquaredSettings.DUMPCATCHER_TEST) {
            if (FoursquaredSettings.DEBUG)
                Log.d(TAG, "Loading Dumpcatcher TEST");
            sDumpcatcher = new Dumpcatcher( //
                    resources.getString(R.string.test_dumpcatcher_product_key), //
                    resources.getString(R.string.test_dumpcatcher_secret), //
                    resources.getString(R.string.test_dumpcatcher_url), sClient, 5);
        } else {
            if (FoursquaredSettings.DEBUG)
                Log.d(TAG, "Loading Dumpcatcher Live");
            sDumpcatcher = new Dumpcatcher( //
                    resources.getString(R.string.dumpcatcher_product_key), //
                    resources.getString(R.string.dumpcatcher_secret), //
                    resources.getString(R.string.dumpcatcher_url), sClient, 5);
        }

        UncaughtExceptionHandler handler = new DefaultUnhandledExceptionHandler(sDumpcatcher);
        // This can hang the app starving android of its ability to properly
        // kill threads... maybe.
        Thread.setDefaultUncaughtExceptionHandler(handler);
        Thread.currentThread().setUncaughtExceptionHandler(handler);
    }

    public static void sendCrash(final String shortMessage, final String longMessage,
            final String level, final String tag) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpResponse response = sDumpcatcher.sendCrash(shortMessage, longMessage,
                            level, "usage");
                    response.getEntity().consumeContent();
                } catch (Exception e) {
                    if (DEBUG)
                        Log.d(TAG, "Unable to sendCrash");
                }
            }
        });
    }

    public static void sendException(Throwable e) {
        sendCrash(//
                StackFormattingUtil.getStackMessageString(e), //
                StackFormattingUtil.getStackTraceString(e), //
                String.valueOf(Level.INFO.intValue()), //
                "exception");
    }

    public static void sendUsage(final String usage) {
        sendCrash(usage, null, null, "usage");
    }

    private static final class DefaultUnhandledExceptionHandler extends
            DumpcatcherUncaughtExceptionHandler {

        private static final UncaughtExceptionHandler mOriginalExceptionHandler = Thread
                .getDefaultUncaughtExceptionHandler();

        DefaultUnhandledExceptionHandler(Dumpcatcher dumpcatcher) {
            super(dumpcatcher);
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            super.uncaughtException(t, e);
            mOriginalExceptionHandler.uncaughtException(t, e);
        }
    }

}
