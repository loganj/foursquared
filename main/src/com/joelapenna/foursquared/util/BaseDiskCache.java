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

    private static final String NOMEDIA = ".nomedia";
    private static final int MIN_FILE_SIZE_IN_BYTES = 100;

    private File mStorageDirectory;

    BaseDiskCache(String dirPath, String name) {
        // Lets make sure we can actually cache things!
        File baseDirectory = new File(Environment.getExternalStorageDirectory(), dirPath);
        File storageDirectory = new File(baseDirectory, name);
        createDirectory(storageDirectory);
        mStorageDirectory = storageDirectory;
        cleanup(); // Remove invalid files that may have shown up.
    }

    /*
     * (non-Javadoc)
     * @see com.joelapenna.foursquared.util.DiskCache#exists(java.lang.String)
     */
    @Override
    public boolean exists(String key) {
        return getFile(key).exists();
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

    public void invalidate(String key) {
        getFile(key).delete();
    }

    public void cleanup() {
        // removes files that are too small to be valid. Cheap and cheater way to remove files that
        // were corrupted during download.
        String[] children = mStorageDirectory.list();
        if (children != null) { // children will be null if hte directyr does not exist.
            for (int i = 0; i < children.length; i++) {
                File child = new File(mStorageDirectory, children[i]);
                if (!child.equals(new File(mStorageDirectory, NOMEDIA))
                        && child.length() <= MIN_FILE_SIZE_IN_BYTES) {
                    if (DEBUG) Log.d(TAG, "Deleting: " + child);
                    child.delete();
                }
            }
        }
    }

    public void clear() {
        // Clear the whole cache. Coolness.
        String[] children = mStorageDirectory.list();
        if (children != null) { // children will be null if hte directyr does not exist.
            for (int i = 0; i < children.length; i++) {
                File child = new File(mStorageDirectory, children[i]);
                if (!child.equals(new File(mStorageDirectory, NOMEDIA))) {
                    if (DEBUG) Log.d(TAG, "Deleting: " + child);
                    child.delete();
                }
            }
        }
        mStorageDirectory.delete();
    }

    private static final void createDirectory(File storageDirectory) {
        if (!storageDirectory.exists()) {
            Log.d(TAG, "Trying to create storageDirectory: "
                    + String.valueOf(storageDirectory.mkdirs()));

            Log.d(TAG, "Exists: " + storageDirectory + " "
                    + String.valueOf(storageDirectory.exists()));
            Log.d(TAG, "State: " + Environment.getExternalStorageState());
            Log.d(TAG, "Isdir: " + storageDirectory + " "
                    + String.valueOf(storageDirectory.isDirectory()));
            Log.d(TAG, "Readable: " + storageDirectory + " "
                    + String.valueOf(storageDirectory.canRead()));
            Log.d(TAG, "Writable: " + storageDirectory + " "
                    + String.valueOf(storageDirectory.canWrite()));
            File tmp = storageDirectory.getParentFile();
            Log.d(TAG, "Exists: " + tmp + " " + String.valueOf(tmp.exists()));
            Log.d(TAG, "Isdir: " + tmp + " " + String.valueOf(tmp.isDirectory()));
            Log.d(TAG, "Readable: " + tmp + " " + String.valueOf(tmp.canRead()));
            Log.d(TAG, "Writable: " + tmp + " " + String.valueOf(tmp.canWrite()));
            tmp = tmp.getParentFile();
            Log.d(TAG, "Exists: " + tmp + " " + String.valueOf(tmp.exists()));
            Log.d(TAG, "Isdir: " + tmp + " " + String.valueOf(tmp.isDirectory()));
            Log.d(TAG, "Readable: " + tmp + " " + String.valueOf(tmp.canRead()));
            Log.d(TAG, "Writable: " + tmp + " " + String.valueOf(tmp.canWrite()));
        }

        File nomediaFile = new File(storageDirectory, NOMEDIA);
        if (!nomediaFile.exists()) {
            try {
                Log.d(TAG, "Created file: " + nomediaFile + " "
                        + String.valueOf(nomediaFile.createNewFile()));
            } catch (IOException e) {
                Log.d(TAG, "Unable to create .nomedia file for some reason.", e);
                throw new IllegalStateException("Unable to create nomedia file.");
            }
        }

        // After we best-effort try to create the file-structure we need,
        // lets make sure it worked.
        if (!(storageDirectory.isDirectory() && nomediaFile.exists())) {
            throw new RuntimeException("Unable to create storage directory and nomedia file.");
        }
    }
}
