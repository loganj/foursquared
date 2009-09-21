/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class NullDiskCache implements DiskCache {

    /*
     * (non-Javadoc)
     * @see com.joelapenna.foursquared.util.DiskCache#exists(java.lang.String)
     */
    @Override
    public boolean exists(String key) {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.joelapenna.foursquared.util.DiskCache#getFile(java.lang.String)
     */
    @Override
    public File getFile(String key) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.joelapenna.foursquared.util.DiskCache#getInputStream(java.lang.String)
     */
    @Override
    public InputStream getInputStream(String key) throws IOException {
        throw new FileNotFoundException();
    }

    /*
     * (non-Javadoc)
     * @see com.joelapenna.foursquared.util.DiskCache#store(java.lang.String, java.io.InputStream)
     */
    @Override
    public void store(String key, InputStream is) {
    }

    /*
     * (non-Javadoc)
     * @see com.joelapenna.foursquared.util.DiskCache#cleanup()
     */
    @Override
    public void cleanup() {
    }

    /*
     * (non-Javadoc)
     * @see com.joelapenna.foursquared.util.DiskCache#invalidate(java.lang.String)
     */
    @Override
    public void invalidate(String key) {
    }

    /*
     * (non-Javadoc)
     * @see com.joelapenna.foursquared.util.DiskCache#clear()
     */
    @Override
    public void clear() {
    }

}
