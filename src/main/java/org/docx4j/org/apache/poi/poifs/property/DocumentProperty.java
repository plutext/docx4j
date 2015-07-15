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
        

package org.docx4j.org.apache.poi.poifs.property;

import org.docx4j.org.apache.poi.poifs.filesystem.OPOIFSDocument;

/**
 * Trivial extension of Property for POIFSDocuments
 */

public class DocumentProperty
    extends Property
{

    // the POIFSDocument this property is associated with
    private OPOIFSDocument _document;

    /**
     * Constructor
     *
     * @param name POIFSDocument name
     * @param size POIFSDocument size
     */

    public DocumentProperty(final String name, final int size)
    {
        super();
        _document = null;
        setName(name);
        setSize(size);
        setNodeColor(_NODE_BLACK);   // simplification
        setPropertyType(PropertyConstants.DOCUMENT_TYPE);
    }

    /**
     * reader constructor
     *
     * @param index index number
     * @param array byte data
     * @param offset offset into byte data
     */

    protected DocumentProperty(final int index, final byte [] array,
                               final int offset)
    {
        super(index, array, offset);
        _document = null;
    }

    /**
     * set the POIFSDocument
     *
     * @param doc the associated POIFSDocument
     */

    public void setDocument(OPOIFSDocument doc)
    {
        _document = doc;
    }

    /**
     * get the POIFSDocument
     *
     * @return the associated document
     */

    public OPOIFSDocument getDocument()
    {
        return _document;
    }

    /* ********** START extension of Property ********** */

    /**
     * give method more visibility
     *
     * @return true if this property should use small blocks
     */

    public boolean shouldUseSmallBlocks()
    {
        return super.shouldUseSmallBlocks();
    }

    /**
     * @return true if a directory type Property
     */

    public boolean isDirectory()
    {
        return false;
    }

    /**
     * Perform whatever activities need to be performed prior to
     * writing
     */

    protected void preWrite()
    {

        // do nothing
    }
    
    /**
     * Update the size of the property's data
     */
    public void updateSize(int size)
    {
        setSize(size);
    }

    /* **********  END  extension of Property ********** */
}   // end public class DocumentProperty

