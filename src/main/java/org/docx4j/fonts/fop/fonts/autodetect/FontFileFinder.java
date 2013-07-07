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

/* $Id: FontFileFinder.java 679326 2008-07-24 09:35:34Z vhennebert $ */

package org.docx4j.fonts.fop.fonts.autodetect;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps to autodetect/locate available operating system fonts.
 */
public class FontFileFinder extends DirectoryWalker implements FontFinder {

    /** logging instance */
    private final Logger log = LoggerFactory.getLogger(FontFileFinder.class);

    /** default depth limit of recursion when searching for font files **/
    public static final int DEFAULT_DEPTH_LIMIT = -1;

    /**
     * Default constructor
     */
    public FontFileFinder() {
        super(getDirectoryFilter(), getFileFilter(), DEFAULT_DEPTH_LIMIT);
    }

    /**
     * Constructor
     * @param depthLimit recursion depth limit
     */
    public FontFileFinder(int depthLimit) {
        super(getDirectoryFilter(), getFileFilter(), depthLimit);
    }

    /**
     * Font directory filter.  Currently ignores hidden directories.
     * @return IOFileFilter font directory filter
     */
    protected static IOFileFilter getDirectoryFilter() {
        return FileFilterUtils.andFileFilter(
                FileFilterUtils.directoryFileFilter(),
                FileFilterUtils.notFileFilter(FileFilterUtils.prefixFileFilter("."))
        );
    }

    /**
     * Font file filter.  Currently searches for files with .ttf, .ttc, .otf, and .pfb extensions.
     * @return IOFileFilter font file filter
     */
    protected static IOFileFilter getFileFilter() {
        return FileFilterUtils.andFileFilter(
                FileFilterUtils.fileFileFilter(),
                new WildcardFileFilter(
                        new String[] {"*.ttf", "*.otf", "*.pfb", "*.ttc"},
                        IOCase.INSENSITIVE)
        );
    }

    /**
     * @param directory directory to handle
     * @param depth recursion depth
     * @param results collection
     * @return whether directory should be handled
     * {@inheritDoc}
     */
    protected boolean handleDirectory(File directory, int depth, Collection results) {
        return true;
    }

    /**
     * @param file file to handle
     * @param depth recursion depth
     * @param results collection
     * {@inheritDoc}
     */
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
    public List find() throws IOException {
        final FontFinder fontDirFinder;
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
        List fontDirs = fontDirFinder.find();
        List results = new java.util.ArrayList();
        for (Iterator iter = fontDirs.iterator(); iter.hasNext();) {
            final File dir = (File)iter.next();
            super.walk(dir, results);
        }
        return results;
    }

    /**
     * Searches a given directory for font files
     *
     * @param dir directory to search
     * @return list of font files
     * @throws IOException thrown if an I/O exception of some sort has occurred
     */
    public List find(String dir) throws IOException {
        List results = new java.util.ArrayList();
        super.walk(new File(dir), results);
        return results;
    }
}
