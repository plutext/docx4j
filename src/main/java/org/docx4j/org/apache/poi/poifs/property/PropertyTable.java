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

import java.io.IOException;
import java.io.OutputStream;

import org.docx4j.org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.docx4j.org.apache.poi.poifs.storage.BlockWritable;
import org.docx4j.org.apache.poi.poifs.storage.HeaderBlock;
import org.docx4j.org.apache.poi.poifs.storage.PropertyBlock;
import org.docx4j.org.apache.poi.poifs.storage.RawDataBlockList;

/**
 * This class embodies the Property Table for the {@link org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem}; 
 *  this is basically the directory for all of the documents in the
 * filesystem.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */
public final class PropertyTable extends PropertyTableBase implements BlockWritable {
    private POIFSBigBlockSize _bigBigBlockSize;
    private BlockWritable[]   _blocks;

    public PropertyTable(HeaderBlock headerBlock)
    {
        super(headerBlock);
        _bigBigBlockSize = headerBlock.getBigBlockSize();
        _blocks = null;
    }

    /**
     * reading constructor (used when we've read in a file and we want
     * to extract the property table from it). Populates the
     * properties thoroughly
     *
     * @param headerBlock the header block of the file
     * @param blockList the list of blocks
     *
     * @exception IOException if anything goes wrong (which should be
     *            a result of the input being NFG)
     */
    public PropertyTable(final HeaderBlock headerBlock,
                         final RawDataBlockList blockList)
        throws IOException
    {
        super(
              headerBlock,
              PropertyFactory.convertToProperties(
                    blockList.fetchBlocks(headerBlock.getPropertyStart(), -1)
              )
        );
        _bigBigBlockSize = headerBlock.getBigBlockSize();
        _blocks      = null;
    }

    /**
     * Prepare to be written
     */
    public void preWrite()
    {
        Property[] properties = _properties.toArray(new Property[_properties.size()]);

        // give each property its index
        for (int k = 0; k < properties.length; k++)
        {
            properties[ k ].setIndex(k);
        }

        // allocate the blocks for the property table
        _blocks = PropertyBlock.createPropertyBlockArray(_bigBigBlockSize, _properties);

        // prepare each property for writing
        for (int k = 0; k < properties.length; k++)
        {
            properties[ k ].preWrite();
        }
    }
    
    /**
     * Return the number of BigBlock's this instance uses
     *
     * @return count of BigBlock instances
     */
    public int countBlocks()
    {
        return (_blocks == null) ? 0
                                 : _blocks.length;
    }

    /**
     * Write the storage to an OutputStream
     *
     * @param stream the OutputStream to which the stored data should
     *               be written
     *
     * @exception IOException on problems writing to the specified
     *            stream
     */
    public void writeBlocks(final OutputStream stream)
        throws IOException
    {
        if (_blocks != null)
        {
            for (int j = 0; j < _blocks.length; j++)
            {
                _blocks[ j ].writeBlocks(stream);
            }
        }
    }
}
