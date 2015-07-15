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
 * Class POIFSWriterEvent
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 * @version %I%, %G%
 */

public class POIFSWriterEvent
{
    private DocumentOutputStream stream;
    private POIFSDocumentPath    path;
    private String               documentName;
    private int                  limit;

    /**
     * package scoped constructor
     *
     * @param stream the DocumentOutputStream, freshly opened
     * @param path the path of the document
     * @param documentName the name of the document
     * @param limit the limit, in bytes, that can be written to the
     *              stream
     */

    POIFSWriterEvent(final DocumentOutputStream stream,
                     final POIFSDocumentPath path, final String documentName,
                     final int limit)
    {
        this.stream       = stream;
        this.path         = path;
        this.documentName = documentName;
        this.limit        = limit;
    }

    /**
     * @return the DocumentOutputStream, freshly opened
     */

    public DocumentOutputStream getStream()
    {
        return stream;
    }

    /**
     * @return the document's path
     */

    public POIFSDocumentPath getPath()
    {
        return path;
    }

    /**
     * @return the document's name
     */

    public String getName()
    {
        return documentName;
    }

    /**
     * @return the limit on writing, in bytes
     */

    public int getLimit()
    {
        return limit;
    }
}   // end public class POIFSWriterEvent

