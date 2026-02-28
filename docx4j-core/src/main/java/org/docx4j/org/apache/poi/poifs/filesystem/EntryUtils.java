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
package org.docx4j.org.apache.poi.poifs.filesystem;

import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.docx4j.org.apache.poi.hpsf.NoPropertySetStreamException;
import org.docx4j.org.apache.poi.hpsf.PropertySet;
import org.docx4j.org.apache.poi.hpsf.PropertySetFactory;
import org.docx4j.org.apache.poi.util.Internal;

@Internal
public final class EntryUtils {
    private EntryUtils() {}

    /**
     * Copies an Entry into a target POIFS directory, recursively
     */
    @Internal
    public static void copyNodeRecursively( Entry entry, DirectoryEntry target )
    throws IOException {
        if ( entry.isDirectoryEntry() ) {
            DirectoryEntry dirEntry = (DirectoryEntry)entry;
            DirectoryEntry newTarget = target.createDirectory( entry.getName() );
            newTarget.setStorageClsid( dirEntry.getStorageClsid() );
            Iterator<Entry> entries = dirEntry.getEntries();

            while ( entries.hasNext() ) {
                copyNodeRecursively( entries.next(), newTarget );
            }
        } else {
            DocumentEntry dentry = (DocumentEntry) entry;
            DocumentInputStream dstream = new DocumentInputStream( dentry );
            target.createDocument( dentry.getName(), dstream );
            dstream.close();
        }
    }

    /**
     * Copies all the nodes from one POIFS Directory to another
     *
     * @param sourceRoot
     *            is the source Directory to copy from
     * @param targetRoot
     *            is the target Directory to copy to
     */
    public static void copyNodes(DirectoryEntry sourceRoot, DirectoryEntry targetRoot)
    throws IOException {
        for (Entry entry : sourceRoot) {
            copyNodeRecursively( entry, targetRoot );
        }
    }

    /**
     * Copies all nodes from one POIFS to the other
     *
     * @param source
     *            is the source POIFS to copy from
     * @param target
     *            is the target POIFS to copy to
     */
    public static void copyNodes(POIFSFileSystem source, POIFSFileSystem target )
    throws IOException {
        copyNodes( source.getRoot(), target.getRoot() );
    }

    /**
     * Copies nodes from one POIFS to the other, minus the excepts.
     * This delegates the filtering work to {@link FilteringDirectoryNode},
     *  so excepts can be of the form "NodeToExclude" or
     *  "FilteringDirectory/ExcludedChildNode"
     *
     * @param source is the source POIFS to copy from
     * @param target is the target POIFS to copy to
     * @param excepts is a list of Entry Names to be excluded from the copy
     */
    public static void copyNodes(POIFSFileSystem source, POIFSFileSystem target, List<String> excepts )
    throws IOException {
        copyNodes(
              new FilteringDirectoryNode(source.getRoot(), excepts),
              new FilteringDirectoryNode(target.getRoot(), excepts)
        );
    }

    /**
     * Checks to see if the two Directories hold the same contents.
     * For this to be true ...
     * <ul>
     *     <li>they must have entries with the same names</li>
     *     <li>no entries in one but not the other</li>
     *     <li>the size+contents of each entry must match</li>
     *     <li>the storage classid of the directories must match</li>
     * </ul>
     * To exclude certain parts of the Directory from being checked,
     *  use a {@link FilteringDirectoryNode}
     */
    public static boolean areDirectoriesIdentical(DirectoryEntry dirA, DirectoryEntry dirB) {
        return new DirectoryDelegate(dirA).equals(new DirectoryDelegate(dirB));
    }

    /**
     * Compares two {@link DocumentEntry} instances of a POI file system.
     * Documents that are not property set streams must be bitwise identical.
     * Property set streams must be logically equal.<p>
     *
     * (Their parent directories are not checked)
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean areDocumentsIdentical(DocumentEntry docA, DocumentEntry docB)
    throws IOException {
        try {
            return new DocumentDelegate(docA).equals(new DocumentDelegate(docB));
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException)e.getCause();
            } else {
                throw e;
            }
        }
    }

    private interface POIDelegate {
    }

    private static class DirectoryDelegate implements POIDelegate {
        final DirectoryEntry dir;

        DirectoryDelegate(DirectoryEntry dir) {
            this.dir = dir;
        }

        private Map<String,POIDelegate> entries() {
            return StreamSupport.stream(dir.spliterator(), false)
                .collect(Collectors.toMap(Entry::getName, DirectoryDelegate::toDelegate));
        }

        private static POIDelegate toDelegate(Entry entry) {
            return (entry.isDirectoryEntry())
                ? new DirectoryDelegate((DirectoryEntry)entry)
                : new DocumentDelegate((DocumentEntry)entry);
        }

        @Override
        public int hashCode() {
            return dir.getName().hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof DirectoryDelegate)) {
                return false;
            }

            DirectoryDelegate dd = (DirectoryDelegate)other;

            if (this == dd) {
                return true;
            }

            // First, check names
            if (!Objects.equals(dir.getName(),dd.dir.getName())) {
                return false;
            }

            // Next up, check they have the same number of children
            if (dir.getEntryCount() != dd.dir.getEntryCount()) {
                return false;
            }

            if (!dir.getStorageClsid().equals(dd.dir.getStorageClsid())) {
                return false;
            }

            return entries().equals(dd.entries());
        }
    }

    private static class DocumentDelegate implements POIDelegate {
        final DocumentEntry doc;

        DocumentDelegate(DocumentEntry doc) {
            this.doc = doc;
        }

        @Override
        public int hashCode() {
            return doc.getName().hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof DocumentDelegate)) {
                return false;
            }

            DocumentDelegate dd = (DocumentDelegate)other;

            if (this == dd) {
                return true;
            }


            if (!Objects.equals(doc.getName(), dd.doc.getName())) {
                // Names don't match, not the same
                return false;
            }

            try (DocumentInputStream inpA = new DocumentInputStream(doc);
                 DocumentInputStream inpB = new DocumentInputStream(dd.doc)) {

                if (PropertySet.isPropertySetStream(inpA) &&
                        PropertySet.isPropertySetStream(inpB)) {
                    final PropertySet ps1 = PropertySetFactory.create(inpA);
                    final PropertySet ps2 = PropertySetFactory.create(inpB);
                    return ps1.equals(ps2);
                } else {
                    return isEqual(inpA, inpB);
                }
            } catch (NoPropertySetStreamException | IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        private static boolean isEqual(DocumentInputStream i1, DocumentInputStream i2)
        throws IOException {
            final byte[] buf1 = new byte[4*1024];
            final byte[] buf2 = new byte[4*1024];
            try {
                int len;
                while ((len = i1.read(buf1)) > 0) {
                    i2.readFully(buf2,0,len);
                    for(int i=0;i<len;i++) {
                        if (buf1[i] != buf2[i]) {
                            return false;
                        }
                    }
                }
                // is the end of the second file also.
                return i2.read() < 0;
            } catch(EOFException | RuntimeException ioe) {
                return false;
            }
        }
    }
}
