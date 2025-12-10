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

/* $Id$ */

package org.docx4j.fonts.fop.fonts.autodetect;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.fonts.fop.fonts.FontEventListener;

/**
 * Helps to autodetect/locate available operating system fonts.
 */
public class FontFileFinder extends DirectoryWalker implements FontFinder {

    /** logging instance */
    private final  Logger log = LoggerFactory.getLogger(FontFileFinder.class);

    /** default depth limit of recursion when searching for font files **/
    public static final int DEFAULT_DEPTH_LIMIT = -1;
    private final FontEventListener eventListener;

    /**
     * Default constructor
     * @param listener for throwing font related events
     */
    public FontFileFinder(FontEventListener listener) {
        this(DEFAULT_DEPTH_LIMIT, listener);
    }

    /**
     * Constructor
     * @param depthLimit recursion depth limit
     * @param listener for throwing font related events
     */
    public FontFileFinder(int depthLimit, FontEventListener listener) {
        super(getDirectoryFilter(), getFileFilter(), depthLimit);
        eventListener = listener;
    }

    /**
     * Font directory filter.  Currently ignores hidden directories.
     * @return IOFileFilter font directory filter
     */
    protected static IOFileFilter getDirectoryFilter() {
        return FileFilterUtils.and(
                FileFilterUtils.directoryFileFilter(),
                FileFilterUtils.notFileFilter(FileFilterUtils.prefixFileFilter("."))
        );
    }

    /**
     * Font file filter.  Currently searches for files with .ttf, .ttc, .otf, and .pfb extensions.
     * @return IOFileFilter font file filter
     */
    protected static IOFileFilter getFileFilter() {
        return FileFilterUtils.and(
                FileFilterUtils.fileFileFilter(),
                WildcardFileFilter.builder().setIoCase(IOCase.INSENSITIVE)
                        .setWildcards("*.ttf", "*.otf", "*.pfb", "*.ttc").get());
    }
    

    /**
     * @param directory directory to handle
     * @param depth recursion depth
     * @param results collection
     * @return whether directory should be handled
     * {@inheritDoc}
     */
    @Override
    protected boolean handleDirectory(File directory, int depth, Collection results) {
        return true;
    }

    /**
     * @param file file to handle
     * @param depth recursion depth
     * @param results collection
     * {@inheritDoc}
     */
    @Override
    protected void handleFile(File file, int depth, Collection results) {
        try {
            // Looks Strange, but is actually recommended over just .URL()
            results.add(file.toURI().toURL());
        } catch (MalformedURLException e) {
            log.debug("MalformedURLException" + e.getMessage());
        }
    }

    /**
     * @param directory the directory being processed
     * @param depth the current directory level
     * @param results the collection of results objects
     * {@inheritDoc}
     */
    @Override
    protected void handleDirectoryEnd(File directory, int depth, Collection results) {
        if (log.isDebugEnabled()) {
            log.debug(directory + ": found " + results.size() + " font"
                    + ((results.size() == 1) ? "" : "s"));
        }
    }

    /**
     * Automagically finds a list of font files on local system
     *
     * @return List&lt;URL&gt; of font files
     * @throws IOException io exception
     * {@inheritDoc}
     */
    public List<URL> find() throws IOException {
        final FontDirFinder fontDirFinder;
        final String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            fontDirFinder = new WindowsFontDirFinder();
        } else {
            if (osName.startsWith("Mac")) {
                fontDirFinder = new MacFontDirFinder();
            } else {
                fontDirFinder = new UnixFontDirFinder();
            }
        }
        List<File> fontDirs = fontDirFinder.find();
        List<URL> results = new java.util.ArrayList<>();
        for (File dir : fontDirs) {
            walkDirectory(dir, results);
        }
        return results;
    }

    /**
     * Searches a given directory for font files
     *
     * @param directory directory to search
     * @return list of font files
     * @throws IOException thrown if an I/O exception of some sort has occurred
     */
    public List<URL> find(File directory) throws IOException {
        List<URL> results = new java.util.ArrayList<>();
        if (!directory.isDirectory()) {
            if (eventListener != null) {
                eventListener.fontDirectoryNotFound(this, directory.getAbsolutePath());
            } else {
                log.warn("Font directory not found: " + directory.getAbsolutePath());
            }
        } else {
            walkDirectory(directory, results);
        }
        return results;
    }

    private void walkDirectory(File startDirectory, Collection<URL> results) throws IOException {
        Objects.requireNonNull(startDirectory, "startDirectory");

        try {
            walk(startDirectory, 0, results, new HashMap<>());
        } catch (CancelException cancel) {
            handleCancelled(startDirectory, results, cancel);
        }

    }

    private void walk(File directory, int depth, Collection<URL> results,
                      Map<String, String> visitedSymlinks) throws IOException {
        IOFileFilter fileFilter = FileFilterUtils.makeFileOnly(getFileFilter());
        IOFileFilter directoryFilter = FileFilterUtils.makeDirectoryOnly(getDirectoryFilter());
        FileFilter filter = directoryFilter.or(fileFilter);

        checkIfCancelled(directory, depth, results);

        int childDepth = depth + 1;

        File[] childFiles = directory.listFiles(filter);
        if (childFiles != null) {
            for (File childFile : childFiles) {
                if (childFile.isDirectory()) {
                    if (Files.isSymbolicLink(childFile.toPath()) && hasSymlinkBeenWalked(childFile, visitedSymlinks)) {
                        continue;
                    }
                    walk(childFile, childDepth, results, visitedSymlinks);
                } else {
                    checkIfCancelled(childFile, childDepth, results);
                    handleFile(childFile, childDepth, results);
                    checkIfCancelled(childFile, childDepth, results);
                }
            }
        }

        handleDirectoryEnd(directory, depth, results);
        checkIfCancelled(directory, depth, results);
    }

    private boolean hasSymlinkBeenWalked(File symlink, Map<String, String> visitedSymlinks) {
        String symlinkPath;
        try {
            symlinkPath = symlink.toPath().toRealPath().toString();
        } catch (IOException e) {
            log.warn("Failed to get symlink path: " + e.getMessage());
            symlinkPath = symlink.getAbsolutePath();
        }
        for (Map.Entry<String, String> entry : visitedSymlinks.entrySet()) {
            //startsWith being used in the event there's an exception in the call above
            //if there's no exception, startsWith will work as an equals
            if (symlinkPath.startsWith(entry.getKey()) && entry.getValue().equals(symlink.getName())) {
                return true;
            }
        }

        visitedSymlinks.put(symlinkPath, symlink.getName());

        return false;
    }
}