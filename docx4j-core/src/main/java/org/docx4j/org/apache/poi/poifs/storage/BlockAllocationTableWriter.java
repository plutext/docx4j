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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.docx4j.org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.filesystem.BATManaged;
import org.docx4j.org.apache.poi.util.IntList;

/**
 * This class manages and creates the Block Allocation Table, which is
 * basically a set of linked lists of block indices.
 * <P>
 * Each block of the filesystem has an index. The first block, the
 * header, is skipped; the first block after the header is index 0,
 * the next is index 1, and so on.
 * <P>
 * A block's index is also its index into the Block Allocation
 * Table. The entry that it finds in the Block Allocation Table is the
 * index of the next block in the linked list of blocks making up a
 * file, or it is set to -2: end of list.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */
public final class BlockAllocationTableWriter implements BlockWritable, BATManaged {
    private IntList    _entries;
    private BATBlock[] _blocks;
    private int        _start_block;
    private POIFSBigBlockSize _bigBlockSize;

    /**
     * create a BlockAllocationTableWriter
     */
    public BlockAllocationTableWriter(POIFSBigBlockSize bigBlockSize)
    {
       _bigBlockSize = bigBlockSize; 
        _start_block  = POIFSConstants.END_OF_CHAIN;
        _entries      = new IntList();
        _blocks       = new BATBlock[ 0 ];
    }

    /**
     * Create the BATBlocks we need
     *
     * @return start block index of BAT blocks
     */
    public int createBlocks()
    {
        int xbat_blocks = 0;
        int bat_blocks  = 0;

        while (true)
        {
            int calculated_bat_blocks  =
                BATBlock.calculateStorageRequirements(_bigBlockSize,
                                                      bat_blocks
                                                      + xbat_blocks
                                                      + _entries.size());
            int calculated_xbat_blocks =
                HeaderBlockWriter.calculateXBATStorageRequirements(
                      _bigBlockSize, calculated_bat_blocks);

            if ((bat_blocks == calculated_bat_blocks)
                    && (xbat_blocks == calculated_xbat_blocks))
            {

                // stable ... we're OK
                break;
            }
            bat_blocks  = calculated_bat_blocks;
            xbat_blocks = calculated_xbat_blocks;
        }
        int startBlock = allocateSpace(bat_blocks);

        allocateSpace(xbat_blocks);
        simpleCreateBlocks();
        return startBlock;
    }

    /**
     * Allocate space for a block of indices
     *
     * @param blockCount the number of blocks to allocate space for
     *
     * @return the starting index of the blocks
     */
    public int allocateSpace(final int blockCount)
    {
        int startBlock = _entries.size();

        if (blockCount > 0)
        {
            int limit = blockCount - 1;
            int index = startBlock + 1;

            for (int k = 0; k < limit; k++)
            {
                _entries.add(index++);
            }
            _entries.add(POIFSConstants.END_OF_CHAIN);
        }
        return startBlock;
    }

    /**
     * get the starting block
     *
     * @return the starting block index
     */
    public int getStartBlock()
    {
        return _start_block;
    }

    /**
     * create the BATBlocks
     */
    void simpleCreateBlocks()
    {
        _blocks = BATBlock.createBATBlocks(_bigBlockSize, _entries.toArray());
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
        for (int j = 0; j < _blocks.length; j++)
        {
            _blocks[ j ].writeBlocks(stream);
        }
    }
    
    /**
     * Write the BAT into its associated block
     */
    public static void writeBlock(final BATBlock bat, final ByteBuffer block) 
        throws IOException
    {
        bat.writeData(block);
    }

    /**
     * Return the number of BigBlock's this instance uses
     *
     * @return count of BigBlock instances
     */
    public int countBlocks()
    {
        return _blocks.length;
    }

    /**
     * Set the start block for this instance
     */
    public void setStartBlock(int start_block)
    {
        _start_block = start_block;
    }
}
