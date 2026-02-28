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


package org.docx4j.org.apache.poi.poifs.filesystem;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Class POIFSDocumentPath
 */

public class POIFSDocumentPath {

    private final String[] components;
    private int hashcode; //lazy-compute hashCode

    /**
     * simple constructor for the path of a document that is in the root of the POIFSFileSystem.
     * The constructor that takes an array of Strings can also be used to create such a
     * POIFSDocumentPath by passing it a null or empty String array
     */
    public POIFSDocumentPath() {
        components = new String[0];
    }

    /**
     * constructor for the path of a document that is not in the root of the POIFSFileSystem
     *
     * @param components the Strings making up the path to a document.
     *      The Strings must be ordered as they appear in the directory hierarchy of the document.
     *      The first string must be the name of a directory in the root of the POIFSFileSystem, and
     *      every Nth (for N &gt; 1) string thereafter must be the name of a directory in the directory
     *      identified by the (N-1)th string.<p>
     *      If the components parameter is null or has zero length, the POIFSDocumentPath is appropriate
     *      for a document that is in the root of a POIFSFileSystem
     *
     * @throws IllegalArgumentException
     *      if any of the elements in the components parameter are null or have zero length
     */
    public POIFSDocumentPath(final String [] components) throws IllegalArgumentException {
        this(null, components);
    }

    /**
     * constructor that adds additional subdirectories to an existing path
     *
     * @param path the existing path
     * @param components the additional subdirectory names to be added
     *
     * @throws IllegalArgumentException
     *      if any of the Strings in components is null or zero length
     */
    public POIFSDocumentPath(final POIFSDocumentPath path, final String[] components) throws IllegalArgumentException {
        String[] s1 = (path == null) ? new String[0] : path.components;
        String[] s2 = (components == null) ? new String[0] : components;

        // TODO: Although the Javadoc says empty strings are forbidden, the adapted legacy
        //  implementation allowed it in case a path was specified...
        Predicate<String> p = (path != null) ? Objects::isNull : (s) -> (s == null || s.isEmpty());
        if (Stream.of(s2).anyMatch(p)) {
            throw new IllegalArgumentException("components cannot contain null or empty strings");
        }

        this.components = Stream.concat(Stream.of(s1),Stream.of(s2)).toArray(String[]::new);
    }

    /**
     * Two POIFSDocumentPath instances are equal if they have the same number of component Strings,
     * and if each component String is equal to its corresponding component String
     *
     * @param o the object we're checking equality for
     *
     * @return true if the object is equal to this object
     */

    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if ((o != null) && (o.getClass() == this.getClass())) {
            POIFSDocumentPath path = ( POIFSDocumentPath ) o;
            return Arrays.equals(this.components, path.components);
        }
        return false;
    }

    /**
     * calculate and return the hashcode
     *
     * @return hashcode
     */

    public int hashCode() {
        return (hashcode == 0) ? (hashcode = Arrays.hashCode(components)) : hashcode;
    }

    /**
     * @return the number of components
     */
    public int length() {
        return components.length;
    }

    /**
     * get the specified component
     *
     * @param n which component (0 ... length() - 1)
     *
     * @return the nth component;
     *
     * @throws ArrayIndexOutOfBoundsException if n &lt; 0 or n &gt;= length()
     */
    public String getComponent(int n) throws ArrayIndexOutOfBoundsException {
        return components[ n ];
    }

    /**
     * <p>Returns the path's parent or <code>null</code> if this path
     * is the root path.</p>
     *
     * @since 2002-01-24
     * @return path of parent, or null if this path is the root path
     */
    public POIFSDocumentPath getParent() {
        return (components.length == 0) ? null : new POIFSDocumentPath(Arrays.copyOf(components, components.length - 1));
    }

    /**
     * <p>Returns the last name in the document path's name sequence.
     * If the document path's name sequence is empty, then the empty string is returned.</p>
     *
     * @since 2016-04-09
     * @return The last name in the document path's name sequence, or empty string if this is the root path
     */
    public String getName() {
        return components.length == 0 ? "" : components[components.length - 1];
    }

    /**
     * <p>Returns a string representation of the path. Components are
     * separated by the platform-specific file separator {@link File#separatorChar}</p>
     *
     * @return string representation
     *
     * @since 2002-01-24
     */
    public String toString() {
        return File.separatorChar + String.join(String.valueOf(File.separatorChar), components);
    }
}

