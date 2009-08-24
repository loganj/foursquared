/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class BaseDiskCache implements DiskCache {
    private static final String TAG = "BaseDiskCache";
    private static final boolean DEBUG = true;

    private File mStorageDirectory;

    BaseDiskCache(String dirname, String basename) {
        // Lets make sure we can actually cache things!
        String dir = Environment.getExternalStorageDirectory() + File.separator + dirname;
        File storageDirectory = new File(dir, basename);
        createDirectory(storageDirectory);
        mStorageDirectory = storageDirectory;
    }

    /**
     * This is silly, but our content provider *has* to serve content: URIs as File/FileDescriptors
     * using ContentProvider.openAssetFile, this is a limitation of the StreamLoader that is used by
     * the WebView. So, we handle this by writing the file to disk, and returning a File pointer to
     * it.
     *
     * @param guid
     * @return
     */
    public File getFile(String hash) {
        return new File(mStorageDirectory.toString() + File.separator + hash);
    }

    public InputStream getInputStream(String hash) throws IOException {
        return (InputStream)new FileInputStream(getFile(hash));
    }

    /*
     * (non-Javadoc)
     * @see com.joelapenna.everdroid.evernote.NoteResourceDataBodyCache#storeResource (com
     * .evernote.edam.type.Resource)
     */
    public void store(String key, InputStream is) {
        if (DEBUG) Log.d(TAG, "store: " + key);
        is = new BufferedInputStream(is);
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(getFile(key)));

            byte[] b = new byte[2048];
            int count;
            int total = 0;

            while ((count = is.read(b)) > 0) {
                os.write(b, 0, count);
                total += count;
            }
            os.close();
            if (DEBUG) Log.d(TAG, "store complete: " + key);
        } catch (IOException e) {
            if (DEBUG) Log.d(TAG, "store failed to store: " + key, e);
            return;
        }
    }

    private static final void createDirectory(File storageDirectory) {
        File nomediaFile = new File(storageDirectory, ".nomedia");

        if (!(storageDirectory.isDirectory() && nomediaFile.exists())) {
            storageDirectory.mkdirs();
            try {
                nomediaFile.createNewFile();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create nomedia file.");
            }
            // After we best-effort try to create the file-structure we need,
            // lets make sure it worked.
            if (!(storageDirectory.isDirectory() && nomediaFile.exists())) {
                throw new RuntimeException("Unable to create storage directory and nomedia file.");
            }
        }
    }
}
