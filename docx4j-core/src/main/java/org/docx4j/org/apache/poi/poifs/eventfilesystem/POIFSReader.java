/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 *
 * This notice is included to meet the condition in clause 4(b) of the License.
 */


/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


package org.docx4j.org.apache.poi.poifs.eventfilesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.docx4j.org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSDocument;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSDocumentPath;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.docx4j.org.apache.poi.poifs.property.DirectoryProperty;
import org.docx4j.org.apache.poi.poifs.property.DocumentProperty;
import org.docx4j.org.apache.poi.poifs.property.Property;
import org.docx4j.org.apache.poi.poifs.property.PropertyTable;
import org.docx4j.org.apache.poi.poifs.property.RootProperty;
import org.docx4j.org.apache.poi.util.IOUtils;

/**
 * An event-driven reader for POIFS file systems. Users of this class
 * first create an instance of it, then use the registerListener
 * methods to register POIFSReaderListener instances for specific
 * documents. Once all the listeners have been registered, the read()
 * method is called, which results in the listeners being notified as
 * their documents are read.
 */

public class POIFSReader
{
    private final POIFSReaderRegistry registry = new POIFSReaderRegistry();
    private boolean registryClosed = false;
    private boolean notifyEmptyDirectories;
//    private NPOIFSFileSystem poifs;

    /**
     * Read from an InputStream and process the documents we get
     *
     * @param stream the InputStream from which to read the data
     *
     * @throws IOException on errors reading, or on invalid data
     */

    public void read(final InputStream stream) throws IOException {
        try (POIFSFileSystem poifs = new POIFSFileSystem(stream)) {
            read(poifs);
        }
    }

    /**
     * Read from a File and process the documents we get
     *
     * @param poifsFile the file from which to read the data
     *
     * @throws IOException on errors reading, or on invalid data
     */
    public void read(final File poifsFile) throws IOException {
        try (POIFSFileSystem poifs = new POIFSFileSystem(poifsFile, true)) {
            read(poifs);
        }
    }

    /**
     * Read from a {@link POIFSFileSystem} and process the documents we get
     *
     * @param poifs the POIFSFileSystem from which to read the data
     *
     * @throws IOException on errors reading, or on invalid data
     */
    public void read(final POIFSFileSystem poifs) throws IOException {
        registryClosed = true;

        // get property table from the document
        PropertyTable properties = poifs.getPropertyTable();

        // process documents
        RootProperty root = properties.getRoot();
        processProperties(poifs, root, new POIFSDocumentPath());
    }

    /**
     * Register a POIFSReaderListener for all documents
     *
     * @param listener the listener to be registered
     *
     * @throws NullPointerException if listener is null
     * @throws IllegalStateException if read() has already been
     *                                  called
     */

    public void registerListener(final POIFSReaderListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        if (registryClosed) {
            throw new IllegalStateException();
        }
        registry.registerListener(listener);
    }

    /**
     * Register a POIFSReaderListener for a document in the root
     * directory
     *
     * @param listener the listener to be registered
     * @param name the document name
     *
     * @throws NullPointerException if listener is null or name is
     *                                 null or empty
     * @throws IllegalStateException if read() has already been
     *                                  called
     */

    public void registerListener(final POIFSReaderListener listener, final String name) {
        registerListener(listener, null, name);
    }

    /**
     * Register a POIFSReaderListener for a document in the specified
     * directory
     *
     * @param listener the listener to be registered
     * @param path the document path; if null, the root directory is
     *             assumed
     * @param name the document name
     *
     * @throws NullPointerException if listener is null or name is null
     * @throws IllegalStateException if read() has already been
     *                                  called or name is empty
     */
    public void registerListener(final POIFSReaderListener listener,
                                 final POIFSDocumentPath path,
                                 final String name) {
        if (listener == null || name == null) {
            throw new NullPointerException("invalid null parameter");
        }
        if (name.isEmpty()) {
            throw new IllegalStateException("name must not be empty");
        }
        if (registryClosed) {
            throw new IllegalStateException("registry closed");
        }
        registry.registerListener(listener, path == null ? new POIFSDocumentPath() : path, name);
    }

    /**
     * Activates the notification of empty directories.<p>
     * If this flag is activated, the {@link POIFSReaderListener listener} receives
     * {@link POIFSReaderEvent POIFSReaderEvents} with nulled {@code name} and {@code stream}
     *
     * @param notifyEmptyDirectories if {@code true}, empty directories will be notified
     */
    public void setNotifyEmptyDirectories(boolean notifyEmptyDirectories) {
        this.notifyEmptyDirectories = notifyEmptyDirectories;
    }


    /**
     * read in files
     *
     * @param args names of the files
     *
     * @throws IOException if the files can't be read or have invalid content
     */

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("at least one argument required: input filename(s)");
            System.exit(1);
        }

        // register for all
        for (String arg : args) {
            POIFSReader reader = new POIFSReader();
            reader.registerListener(POIFSReader::readEntry);
            System.out.println("reading " + arg);

            reader.read(new File(arg));
        }
    }

    private static void readEntry(POIFSReaderEvent event) {
        POIFSDocumentPath path = event.getPath();
        StringBuilder sb = new StringBuilder();

        try (DocumentInputStream istream = event.getStream()) {
            sb.setLength(0);
            int pathLength = path.length();
            for (int k = 0; k < pathLength; k++) {
                sb.append('/').append(path.getComponent(k));
            }
            byte[] data = IOUtils.toByteArray(istream);
            sb.append('/').append(event.getName()).append(": ").append(data.length).append(" bytes read");
            System.out.println(sb);
        } catch (IOException ignored) {
        }
    }

    private void processProperties(final POIFSFileSystem poifs, DirectoryProperty dir, final POIFSDocumentPath path) {
        boolean hasChildren = false;
        for (final Property property : dir) {
            hasChildren = true;
            String name = property.getName();

            if (property.isDirectory()) {
                POIFSDocumentPath new_path = new POIFSDocumentPath(path,new String[]{name});
                processProperties(poifs, (DirectoryProperty) property, new_path);
            } else {
                POIFSDocument document = null;
                for (POIFSReaderListener rl : registry.getListeners(path, name)) {
                    if (document == null) {
                        document = new POIFSDocument((DocumentProperty)property, poifs);
                    }
                    try (DocumentInputStream dis = new DocumentInputStream(document)) {
                        POIFSReaderEvent pe = new POIFSReaderEvent(dis, path, name, dir.getStorageClsid());
                        rl.processPOIFSReaderEvent(pe);
                    }
                }
            }
        }

        if (hasChildren || !notifyEmptyDirectories) {
            return;
        }

        for (POIFSReaderListener rl : registry.getListeners(path, ".")) {
            POIFSReaderEvent pe = new POIFSReaderEvent(null, path, null, dir.getStorageClsid());
            rl.processPOIFSReaderEvent(pe);
        }
    }
}

