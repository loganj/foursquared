/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public interface DiskCache {

    public void store(String key, InputStream is);

    public File getFile(String key);

    public InputStream getInputStream(String key) throws IOException;

}
