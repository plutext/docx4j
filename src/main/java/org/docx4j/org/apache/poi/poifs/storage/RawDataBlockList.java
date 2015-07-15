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

import java.io.*;
import java.util.*;

import org.docx4j.org.apache.poi.poifs.common.POIFSBigBlockSize;

/**
 * A list of RawDataBlocks instances, and methods to manage the list
 *
 * @author Marc Johnson (mjohnson at apache dot org
 */

public class RawDataBlockList
    extends BlockListImpl
{

    /**
     * Constructor RawDataBlockList
     *
     * @param stream the InputStream from which the data will be read
     * @param bigBlockSize The big block size, either 512 bytes or 4096 bytes
     *
     * @exception IOException on I/O errors, and if an incomplete
     *            block is read
     */

    public RawDataBlockList(final InputStream stream, POIFSBigBlockSize bigBlockSize)
        throws IOException
    {
        List<RawDataBlock> blocks = new ArrayList<RawDataBlock>();

        while (true)
        {
            RawDataBlock block = new RawDataBlock(stream, bigBlockSize.getBigBlockSize());
            
            // If there was data, add the block to the list
            if(block.hasData()) {
            	blocks.add(block);
            }

            // If the stream is now at the End Of File, we're done
            if (block.eof()) {
                break;
            }
        }
        setBlocks( blocks.toArray(new RawDataBlock[ blocks.size() ]) );
    }
}   // end public class RawDataBlockList

