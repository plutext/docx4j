/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */

 /*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: FontCache.java 743273 2009-02-11 08:41:04Z jeremias $ */

package org.docx4j.fonts.fop.fonts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.docx4j.fonts.fop.apps.FOPException;
import org.docx4j.fonts.fop.util.LogUtil;

/**
 * Fop cache (currently only used for font info caching)
 */
public final class FontCache implements Serializable {

    /**
     * Serialization Version UID. Change this value if you want to make sure the user's cache
     * file is purged after an update.
     */
    private static final long serialVersionUID = 605232520271754719L;

    /** logging instance */
    private static Logger log = LoggerFactory.getLogger(FontCache.class);

    /** FOP's user directory name */
    private static final String FOP_USER_DIR = ".docx4j";

    /** font cache file path */
    private static final String DEFAULT_CACHE_FILENAME = "fop-fonts.cache";


    /** has this cache been changed since it was last read? */
    private transient boolean changed = false;

    /** change lock */
    private final boolean[] changeLock = new boolean[1];

    /** master mapping of font url -> font info.  This needs to be
     *  a list, since a TTC file may contain more than 1 font. */
    private Map/*<String, CachedFontFile>*/ fontfileMap = null;

    /** mapping of font url -> file modified date (for all fonts that have failed to load) */
    private Map failedFontMap/*<String, Long>*/ = null;

    /**
     * Default constructor
     */
    public FontCache() {
        //nop
    }

    private static File getUserHome() {
        return toDirectory(System.getProperty("user.home"));
    }

    private static File getTempDirectory() {
        return toDirectory(System.getProperty("java.io.tmpdir"));
    }

    private static File toDirectory(String path) {
        if (path != null) {
            File dir = new File(path);
            if (dir.exists()) {
                return dir;
            }
        }
        return null;
    }

    /**
     * Returns the default font cache file.
     * @param forWriting true if the user directory should be created
     * @return the default font cache file
     */
    public static File getDefaultCacheFile(boolean forWriting) {
        File userHome = getUserHome();
        if (userHome != null) {
            File fopUserDir = new File(userHome, FOP_USER_DIR);
            if (forWriting) {
                boolean writable = fopUserDir.canWrite();
                if (!fopUserDir.exists()) {
                    writable = fopUserDir.mkdir();
                }
                if (!writable) {
                    userHome = getTempDirectory();
                    fopUserDir = new File(userHome, FOP_USER_DIR);
                    fopUserDir.mkdir();
                }
            }
            return new File(fopUserDir, DEFAULT_CACHE_FILENAME);
        }
        return new File(FOP_USER_DIR);
    }

    /**
     * Reads the default font cache file and returns its contents.
     * @return the font cache deserialized from the file (or null if no cache file exists or if
     *         it could not be read)
     */
    public static FontCache load() {
        return loadFrom(getDefaultCacheFile(false));
    }

    /**
     * Reads a font cache file and returns its contents.
     * @param cacheFile the cache file
     * @return the font cache deserialized from the file (or null if no cache file exists or if
     *         it could not be read)
     */
    public static FontCache loadFrom(File cacheFile) {
        if (cacheFile.exists()) {
            try {
                if (log.isTraceEnabled()) {
                    log.trace("Loading font cache from " + cacheFile.getCanonicalPath());
                }
                InputStream in = new java.io.FileInputStream(cacheFile);
                in = new java.io.BufferedInputStream(in);
                ObjectInputStream oin = new ObjectInputStream(in);
                try {
                    return (FontCache)oin.readObject();
                } finally {
                    IOUtils.closeQuietly(oin);
                }
            } catch (ClassNotFoundException e) {
                //We don't really care about the exception since it's just a cache file
                log.warn("Could not read font cache. Discarding font cache file. Reason: "
                        + e.getMessage());
            } catch (IOException ioe) {
                //We don't really care about the exception since it's just a cache file
                log.warn("I/O exception while reading font cache (" + ioe.getMessage()
                        + "). Discarding font cache file.");
                try {
                    cacheFile.delete();
                } catch (SecurityException ex) {
                    log.warn("Failed to delete font cache file: " + cacheFile.getAbsolutePath());
                }
            }
        }
        return null;
    }

    /**
     * Writes the font cache to disk.
     * @throws FOPException fop exception
     */
    public void save() throws FOPException {
        saveTo(getDefaultCacheFile(true));
    }

    /**
     * Writes the font cache to disk.
     * @param cacheFile the file to write to
     * @throws FOPException fop exception
     */
    public void saveTo(File cacheFile) throws FOPException {
        synchronized (changeLock) {
            if (changed) {
                try {
                    if (log.isTraceEnabled()) {
                        log.trace("Writing font cache to " + cacheFile.getCanonicalPath());
                    }
                    OutputStream out = new java.io.FileOutputStream(cacheFile);
                    out = new java.io.BufferedOutputStream(out);
                    ObjectOutputStream oout = new ObjectOutputStream(out);
                    try {
                        oout.writeObject(this);
                    } finally {
                        IOUtils.closeQuietly(oout);
                    }
                } catch (IOException ioe) {
                    LogUtil.handleException(log, ioe, true);
                }
                changed = false;
                log.trace("Cache file written.");
            }
        }
    }

    /**
     * creates a key given a font info for the font mapping
     * @param fontInfo font info
     * @return font cache key
     */
    protected static String getCacheKey(EmbedFontInfo fontInfo) {
        if (fontInfo != null) {
            String embedFile = fontInfo.getEmbedFile();
            String metricsFile = fontInfo.getMetricsFile();
            return (embedFile != null) ? embedFile : metricsFile;
        }
        return null;
    }

    /**
     * cache has been updated since it was read
     * @return if this cache has changed
     */
    public boolean hasChanged() {
        return this.changed;
    }

    /**
     * is this font in the cache?
     * @param embedUrl font info
     * @return boolean
     */
    public boolean containsFont(String embedUrl) {
        return (embedUrl != null
                && getFontFileMap().containsKey(embedUrl));
    }

    /**
     * is this font info in the cache?
     * @param fontInfo font info
     * @return font
     */
    public boolean containsFont(EmbedFontInfo fontInfo) {
        return (fontInfo != null
                && getFontFileMap().containsKey(getCacheKey(fontInfo)));
    }

    /**
     * Tries to identify a File instance from an array of URLs. If there's no file URL in the
     * array, the method returns null.
     * @param urls array of possible font urls
     * @return file font file
     */
    public static File getFileFromUrls(String[] urls) {
        for (int i = 0; i < urls.length; i++) {
            String urlStr = urls[i];
            if (urlStr != null) {
                File fontFile = null;
                if (urlStr.startsWith("file:")) {
                    try {
                        URL url = new URL(urlStr);
                        fontFile = FileUtils.toFile(url);
                    } catch (MalformedURLException mfue) {
                        // do nothing
                    }
                }
                if (fontFile == null) {
                    fontFile = new File(urlStr);
                }
                if (fontFile.exists() && fontFile.canRead()) {
                    return fontFile;
                }
            }
        }
        return null;
    }

    private Map/*<String, CachedFontFile>*/ getFontFileMap() {
        if (fontfileMap == null) {
            fontfileMap = new java.util.HashMap/*<String, CachedFontFile>*/();
        }
        return fontfileMap;
    }

    /**
     * Adds a font info to cache
     * @param fontInfo font info
     */
    public void addFont(EmbedFontInfo fontInfo) {
        String cacheKey = getCacheKey(fontInfo);
        synchronized (changeLock) {
            CachedFontFile cachedFontFile;
            if (containsFont(cacheKey)) {
                cachedFontFile = (CachedFontFile)getFontFileMap().get(cacheKey);
                if (!cachedFontFile.containsFont(fontInfo)) {
                    cachedFontFile.put(fontInfo);
                }
            } else {
                // try and determine modified date
                File fontFile = getFileFromUrls(new String[]
                                     {fontInfo.getEmbedFile(), fontInfo.getMetricsFile()});
                long lastModified = (fontFile != null ? fontFile.lastModified() : -1);
                cachedFontFile = new CachedFontFile(lastModified);
                if (log.isTraceEnabled()) {
                    log.trace("Font added to cache: " + cacheKey);
                }
                cachedFontFile.put(fontInfo);
                getFontFileMap().put(cacheKey, cachedFontFile);
                changed = true;
            }
        }
    }

    /**
     * Returns a font from the cache.
     * @param embedUrl font info
     * @return CachedFontFile object
     */
    public CachedFontFile getFontFile(String embedUrl) {
        return containsFont(embedUrl) ? (CachedFontFile) getFontFileMap().get(embedUrl) : null;
    }

    /**
     * Returns the EmbedFontInfo instances belonging to a font file. If the font file was
     * modified since it was cached the entry is removed and null is returned.
     * @param embedUrl the font URL
     * @param lastModified the last modified date/time of the font file
     * @return the EmbedFontInfo instances or null if there's no cached entry or if it is outdated
     */
    public EmbedFontInfo[] getFontInfos(String embedUrl, long lastModified) {
        CachedFontFile cff = getFontFile(embedUrl);
        if (cff.lastModified() == lastModified) {
            return cff.getEmbedFontInfos();
        } else {
            removeFont(embedUrl);
            return null;
        }
    }

    /**
     * removes font from cache
     * @param embedUrl embed url
     */
    public void removeFont(String embedUrl) {
        synchronized (changeLock) {
            if (containsFont(embedUrl)) {
                if (log.isTraceEnabled()) {
                    log.trace("Font removed from cache: " + embedUrl);
                }
                getFontFileMap().remove(embedUrl);
                changed = true;
            }
        }
    }

    /**
     * has this font previously failed to load?
     * @param embedUrl embed url
     * @param lastModified last modified
     * @return whether this is a failed font
     */
    public boolean isFailedFont(String embedUrl, long lastModified) {
        synchronized (changeLock) {
            if (getFailedFontMap().containsKey(embedUrl)) {
                long failedLastModified = ((Long)getFailedFontMap().get(embedUrl)).longValue();
                if (lastModified != failedLastModified) {
                    // this font has been changed so lets remove it
                    // from failed font map for now
                    getFailedFontMap().remove(embedUrl);
                    changed = true;
                }
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Registers a failed font with the cache
     * @param embedUrl embed url
     * @param lastModified time last modified
     */
    public void registerFailedFont(String embedUrl, long lastModified) {
        synchronized (changeLock) {
            if (!getFailedFontMap().containsKey(embedUrl)) {
                getFailedFontMap().put(embedUrl, new Long(lastModified));
                changed = true;
            }
        }
    }

    private Map/*<String, Long>*/ getFailedFontMap() {
        if (failedFontMap == null) {
            failedFontMap = new java.util.HashMap/*<String, Long>*/();
        }
        return failedFontMap;
    }

    /**
     * Clears font cache
     */
    public void clear() {
        synchronized (changeLock) {
            if (log.isTraceEnabled()) {
                log.trace("Font cache cleared.");
            }
            fontfileMap = null;
            failedFontMap = null;
            changed = true;
        }
    }

    /**
     * Retrieve the last modified date/time of a URL.
     * @param url the URL
     * @return the last modified date/time
     */
    public static long getLastModified(URL url) {
        try {
            URLConnection conn = url.openConnection();
            try {
                return conn.getLastModified();
            } finally {
                //An InputStream is created even if it's not accessed, but we need to close it.
                IOUtils.closeQuietly(conn.getInputStream());
            }
        } catch (IOException e) {
            // Should never happen, because URL must be local
            log.debug("IOError: " + e.getMessage());
            return 0;
        }
    }

    private static class CachedFontFile implements Serializable {
        private static final long serialVersionUID = 4524237324330578883L;

        /** file modify date (if available) */
        private long lastModified = -1;

        private Map/*<String, EmbedFontInfo>*/ filefontsMap = null;

        public CachedFontFile(long lastModified) {
            setLastModified(lastModified);
        }

        private Map/*<String, EmbedFontInfo>*/ getFileFontsMap() {
            if (filefontsMap == null) {
                filefontsMap = new java.util.HashMap/*<String, EmbedFontInfo>*/();
            }
            return filefontsMap;
        }

        void put(EmbedFontInfo efi) {
            getFileFontsMap().put(efi.getPostScriptName(), efi);
        }

        public boolean containsFont(EmbedFontInfo efi) {
            return efi.getPostScriptName() != null
                    && getFileFontsMap().containsKey(efi.getPostScriptName());
        }

        public EmbedFontInfo[] getEmbedFontInfos() {
            return (EmbedFontInfo[])getFileFontsMap().values().toArray(
                    new EmbedFontInfo[getFileFontsMap().size()]);
        }

        /**
         * Gets the modified timestamp for font file (not always available)
         * @return modified timestamp
         */
        public long lastModified() {
            return this.lastModified;
        }

        /**
         * Gets the modified timestamp for font file
         * (used for the purposes of font info caching)
         * @param lastModified modified font file timestamp
         */
        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        /**
         * @return string representation of this object
         * {@inheritDoc}
         */
        public String toString() {
            return super.toString() + ", lastModified=" + lastModified;
        }

    }
}
