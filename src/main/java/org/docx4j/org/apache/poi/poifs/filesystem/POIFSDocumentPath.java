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

//import org.docx4j.org.apache.poi.util.POILogFactory;
//import org.docx4j.org.apache.poi.util.POILogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class POIFSDocumentPath
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 * @version %I%, %G%
 */

public class POIFSDocumentPath
{
//    private static final POILogger log = POILogFactory.getLogger(POIFSDocumentPath.class);
	private static Logger log = LoggerFactory.getLogger(POIFSDocumentPath.class);
          
    private String[] components;
    private int      hashcode = 0;

    /**
     * constructor for the path of a document that is not in the root
     * of the POIFSFileSystem
     *
     * @param components the Strings making up the path to a document.
     *                   The Strings must be ordered as they appear in
     *                   the directory hierarchy of the the document
     *                   -- the first string must be the name of a
     *                   directory in the root of the POIFSFileSystem,
     *                   and every Nth (for N > 1) string thereafter
     *                   must be the name of a directory in the
     *                   directory identified by the (N-1)th string.
     *                   <p>
     *                   If the components parameter is null or has
     *                   zero length, the POIFSDocumentPath is
     *                   appropriate for a document that is in the
     *                   root of a POIFSFileSystem
     *
     * @exception IllegalArgumentException if any of the elements in
     *                                     the components parameter
     *                                     are null or have zero
     *                                     length
     */

    public POIFSDocumentPath(final String [] components)
        throws IllegalArgumentException
    {
        if (components == null)
        {
            this.components = new String[ 0 ];
        }
        else
        {
            this.components = new String[ components.length ];
            for (int j = 0; j < components.length; j++)
            {
                if ((components[ j ] == null)
                        || (components[ j ].length() == 0))
                {
                    throw new IllegalArgumentException(
                        "components cannot contain null or empty strings");
                }
                this.components[ j ] = components[ j ];
            }
        }
    }

    /**
     * simple constructor for the path of a document that is in the
     * root of the POIFSFileSystem. The constructor that takes an
     * array of Strings can also be used to create such a
     * POIFSDocumentPath by passing it a null or empty String array
     */

    public POIFSDocumentPath()
    {
        this.components = new String[ 0 ];
    }

    /**
     * constructor that adds additional subdirectories to an existing
     * path
     *
     * @param path the existing path
     * @param components the additional subdirectory names to be added
     *
     * @exception IllegalArgumentException if any of the Strings in
     *                                     components is null or zero
     *                                     length
     */

    public POIFSDocumentPath(final POIFSDocumentPath path,
                             final String [] components)
        throws IllegalArgumentException
    {
        if (components == null)
        {
            this.components = new String[ path.components.length ];
        }
        else
        {
            this.components =
                new String[ path.components.length + components.length ];
        }
        for (int j = 0; j < path.components.length; j++)
        {
            this.components[ j ] = path.components[ j ];
        }
        if (components != null)
        {
            for (int j = 0; j < components.length; j++)
            {
                if (components[ j ] == null)
                {
                    throw new IllegalArgumentException(
                        "components cannot contain null");
                }
                if (components[ j ].length() == 0)
                {
                    log.warn("Directory under " + path + " has an empty name, " +
                            "not all OLE2 readers will handle this file correctly!");
                }
                
                this.components[ j + path.components.length ] =
                    components[ j ];
            }
        }
    }

    /**
     * equality. Two POIFSDocumentPath instances are equal if they
     * have the same number of component Strings, and if each
     * component String is equal to its coresponding component String
     *
     * @param o the object we're checking equality for
     *
     * @return true if the object is equal to this object
     */

    public boolean equals(final Object o)
    {
        boolean rval = false;

        if ((o != null) && (o.getClass() == this.getClass()))
        {
            if (this == o)
            {
                rval = true;
            }
            else
            {
                POIFSDocumentPath path = ( POIFSDocumentPath ) o;

                if (path.components.length == this.components.length)
                {
                    rval = true;
                    for (int j = 0; j < this.components.length; j++)
                    {
                        if (!path.components[ j ]
                                .equals(this.components[ j ]))
                        {
                            rval = false;
                            break;
                        }
                    }
                }
            }
        }
        return rval;
    }

    /**
     * calculate and return the hashcode
     *
     * @return hashcode
     */

    public int hashCode()
    {
        if (hashcode == 0)
        {
            for (int j = 0; j < components.length; j++)
            {
                hashcode += components[ j ].hashCode();
            }
        }
        return hashcode;
    }

    /**
     * @return the number of components
     */

    public int length()
    {
        return components.length;
    }

    /**
     * get the specified component
     *
     * @param n which component (0 ... length() - 1)
     *
     * @return the nth component;
     *
     * @exception ArrayIndexOutOfBoundsException if n < 0 or n >=
     *                                           length()
     */

    public String getComponent(int n)
        throws ArrayIndexOutOfBoundsException
    {
        return components[ n ];
    }

    /**
     * <p>Returns the path's parent or <code>null</code> if this path
     * is the root path.</p>
     *
     * @since 2002-01-24
     * @return path of parent, or null if this path is the root path
     */

    public POIFSDocumentPath getParent()
    {
        final int length = components.length - 1;

        if (length < 0)
        {
            return null;
        }
        POIFSDocumentPath parent = new POIFSDocumentPath(null);

        parent.components = new String[ length ];
        System.arraycopy(components, 0, parent.components, 0, length);
        return parent;
    }

    /**
     * <p>Returns a string representation of the path. Components are
     * separated by the platform-specific file separator.</p>
     *
     * @return string representation
     *
     * @since 2002-01-24
     */

    public String toString()
    {
        final StringBuffer b = new StringBuffer();
        final int          l = length();

        b.append(File.separatorChar);
        for (int i = 0; i < l; i++)
        {
            b.append(getComponent(i));
            if (i < l - 1)
            {
                b.append(File.separatorChar);
            }
        }
        return b.toString();
    }
}   // end public class POIFSDocumentPath

