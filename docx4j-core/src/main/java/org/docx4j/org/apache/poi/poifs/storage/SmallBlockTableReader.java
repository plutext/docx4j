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

import org.docx4j.org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.docx4j.org.apache.poi.poifs.property.RootProperty;

/**
 * This class implements reading the small document block list from an
 * existing file
 */
public final class SmallBlockTableReader {
    private static BlockList prepareSmallDocumentBlocks(
            final POIFSBigBlockSize bigBlockSize,
            final RawDataBlockList blockList, final RootProperty root,
            final int sbatStart)
        throws IOException
    {
        // Fetch the blocks which hold the Small Blocks stream
        ListManagedBlock [] smallBlockBlocks = 
                blockList.fetchBlocks(root.getStartBlock(), -1);
        
       // Turn that into a list
        BlockList list =new SmallDocumentBlockList(
                SmallDocumentBlock.extract(bigBlockSize, smallBlockBlocks));

        return list;
    }
    private static BlockAllocationTableReader prepareReader(
            final POIFSBigBlockSize bigBlockSize,
            final RawDataBlockList blockList, final BlockList list, 
            final RootProperty root, final int sbatStart)
        throws IOException
    {
        // Process the SBAT and blocks
        return new BlockAllocationTableReader(bigBlockSize,
                blockList.fetchBlocks(sbatStart, -1),
                list);
    }
            
    /**
     * Fetch the small document block reader from an existing file, normally
     *  needed for debugging and low level dumping. You should typically call
     *  {@link #getSmallDocumentBlocks(POIFSBigBlockSize, RawDataBlockList, RootProperty, int)}
     *  instead.
     *
     * @param blockList the raw data from which the small block table
     *                  will be extracted
     * @param root the root property (which contains the start block
     *             and small block table size)
     * @param sbatStart the start block of the SBAT
     *
     * @return the small document block reader
     *
     * @exception IOException
     */
    public static BlockAllocationTableReader _getSmallDocumentBlockReader(
            final POIFSBigBlockSize bigBlockSize,
            final RawDataBlockList blockList, final RootProperty root,
            final int sbatStart)
        throws IOException
    {
       BlockList list = prepareSmallDocumentBlocks(
                bigBlockSize, blockList, root, sbatStart);
        return prepareReader(
                bigBlockSize, blockList, list, root, sbatStart);
    }

    /**
     * Fetch the small document block list from an existing file
     *
     * @param blockList the raw data from which the small block table
     *                  will be extracted
     * @param root the root property (which contains the start block
     *             and small block table size)
     * @param sbatStart the start block of the SBAT
     *
     * @return the small document block list
     *
     * @exception IOException
     */
    public static BlockList getSmallDocumentBlocks(
            final POIFSBigBlockSize bigBlockSize,
            final RawDataBlockList blockList, final RootProperty root,
            final int sbatStart)
        throws IOException
    {
        BlockList list = prepareSmallDocumentBlocks(
                bigBlockSize, blockList, root, sbatStart);
        prepareReader(bigBlockSize, blockList, list, root, sbatStart);
        return list;
    }
}
