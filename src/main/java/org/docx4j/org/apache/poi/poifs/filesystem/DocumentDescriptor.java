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

/**
 * Class DocumentDescriptor
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 * @version %I%, %G%
 */

public class DocumentDescriptor
{
    private POIFSDocumentPath path;
    private String            name;
    private int               hashcode = 0;

    /**
     * Trivial constructor
     *
     * @param path the Document path
     * @param name the Document name
     */

    public DocumentDescriptor(final POIFSDocumentPath path, final String name)
    {
        if (path == null)
        {
            throw new NullPointerException("path must not be null");
        }
        if (name == null)
        {
            throw new NullPointerException("name must not be null");
        }
        if (name.length() == 0)
        {
            throw new IllegalArgumentException("name cannot be empty");
        }
        this.path = path;
        this.name = name;
    }

    /**
     * equality. Two DocumentDescriptor instances are equal if they
     * have equal paths and names
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
                DocumentDescriptor descriptor = ( DocumentDescriptor ) o;

                rval = this.path.equals(descriptor.path)
                       && this.name.equals(descriptor.name);
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
            hashcode = path.hashCode() ^ name.hashCode();
        }
        return hashcode;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer(40 * (path.length() + 1));

        for (int j = 0; j < path.length(); j++)
        {
            buffer.append(path.getComponent(j)).append("/");
        }
        buffer.append(name);
        return buffer.toString();
    }
}   // end public class DocumentDescriptor

