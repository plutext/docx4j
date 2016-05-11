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
        

package org.docx4j.org.apache.poi.poifs.storage;

import org.docx4j.org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.filesystem.BATManaged;
import org.docx4j.org.apache.poi.poifs.filesystem.OPOIFSDocument;
import org.docx4j.org.apache.poi.poifs.property.RootProperty;

import java.util.*;
import java.io.*;

/**
 * This class implements storage for writing the small blocks used by
 * small documents.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

public class SmallBlockTableWriter
    implements BlockWritable, BATManaged
{
    private BlockAllocationTableWriter _sbat;
    private List<SmallDocumentBlock>   _small_blocks;
    private int                        _big_block_count;
    private RootProperty               _root;

    /**
     * Creates new SmallBlockTable
     *
     * @param documents a List of POIFSDocument instances
     * @param root the Filesystem's root property
     */
    public SmallBlockTableWriter(final POIFSBigBlockSize bigBlockSize,
                                 final List<OPOIFSDocument> documents,
                                 final RootProperty root)
    {
        _sbat         = new BlockAllocationTableWriter(bigBlockSize);
        _small_blocks = new ArrayList<SmallDocumentBlock>();
        _root         = root;

        for (OPOIFSDocument doc : documents)
        {
            SmallDocumentBlock[] blocks = doc.getSmallBlocks();

            if (blocks.length != 0)
            {
                doc.setStartBlock(_sbat.allocateSpace(blocks.length));
                for (int j = 0; j < blocks.length; j++)
                {
                    _small_blocks.add(blocks[ j ]);
                }
            } else {
            	doc.setStartBlock(POIFSConstants.END_OF_CHAIN);
            }
        }
        _sbat.simpleCreateBlocks();
        _root.setSize(_small_blocks.size());
        _big_block_count = SmallDocumentBlock.fill(bigBlockSize,_small_blocks);
    }

    /**
     * Get the number of SBAT blocks
     *
     * @return number of SBAT big blocks
     */
    
    public int getSBATBlockCount()
    {
	return (_big_block_count + 15) / 16;
    }

    /**
     * Get the SBAT
     *
     * @return the Small Block Allocation Table
     */

    public BlockAllocationTableWriter getSBAT()
    {
        return _sbat;
    }

    /* ********** START implementation of BATManaged ********** */

    /**
     * Return the number of BigBlock's this instance uses
     *
     * @return count of BigBlock instances
     */

    public int countBlocks()
    {
        return _big_block_count;
    }

    /**
     * Set the start block for this instance
     *
     * @param start_block
     */

    public void setStartBlock(int start_block)
    {
        _root.setStartBlock(start_block);
    }

    /* **********  END  implementation of BATManaged ********** */
    /* ********** START implementation of BlockWritable ********** */

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
        for (BlockWritable block : _small_blocks) {
            block.writeBlocks(stream);
        }
    }

    /* **********  END  implementation of BlockWritable ********** */
}
