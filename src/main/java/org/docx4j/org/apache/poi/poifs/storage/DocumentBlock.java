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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.docx4j.org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.util.IOUtils;

/**
 * A block of document data.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */
public final class DocumentBlock extends BigBlock {
    private static final byte _default_value = ( byte ) 0xFF;
    private byte[]            _data;
    private int               _bytes_read;

    /**
     * create a document block from a raw data block
     *
     * @param block the raw data block
     *
     * @exception IOException
     */

    public DocumentBlock(final RawDataBlock block)
        throws IOException
    {
        super(
              block.getBigBlockSize() == POIFSConstants.SMALLER_BIG_BLOCK_SIZE ?
                    POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS :
                    POIFSConstants.LARGER_BIG_BLOCK_SIZE_DETAILS
        );
        _data       = block.getData();
        _bytes_read = _data.length;
    }

    /**
     * Create a single instance initialized with data.
     *
     * @param stream the InputStream delivering the data.
     *
     * @exception IOException
     */

    public DocumentBlock(final InputStream stream, POIFSBigBlockSize bigBlockSize)
        throws IOException
    {
        this(bigBlockSize);
        int count = IOUtils.readFully(stream, _data);

        _bytes_read = (count == -1) ? 0
                                    : count;
    }

    /**
     * Create a single instance initialized with default values
     */

    private DocumentBlock(POIFSBigBlockSize bigBlockSize)
    {
        super(bigBlockSize);
        _data = new byte[ bigBlockSize.getBigBlockSize() ];
        Arrays.fill(_data, _default_value);
    }

    /**
     * Get the number of bytes read for this block
     *
     * @return bytes read into the block
     */

    public int size()
    {
        return _bytes_read;
    }

    /**
     * Was this a partially read block?
     *
     * @return true if the block was only partially filled with data
     */

    public boolean partiallyRead()
    {
        return _bytes_read != bigBlockSize.getBigBlockSize();
    }

    /**
     * @return the fill byte used
     */

    public static byte getFillByte()
    {
        return _default_value;
    }

    /**
     * convert a single long array into an array of DocumentBlock
     * instances
     *
     * @param array the byte array to be converted
     * @param size the intended size of the array (which may be smaller)
     *
     * @return an array of DocumentBlock instances, filled from the
     *         input array
     */

    public static DocumentBlock [] convert(final POIFSBigBlockSize bigBlockSize,
                                           final byte [] array,
                                           final int size)
    {
        DocumentBlock[] rval   =
            new DocumentBlock[ (size + bigBlockSize.getBigBlockSize() - 1) / bigBlockSize.getBigBlockSize() ];
        int             offset = 0;

        for (int k = 0; k < rval.length; k++)
        {
            rval[ k ] = new DocumentBlock(bigBlockSize);
            if (offset < array.length)
            {
                int length = Math.min(bigBlockSize.getBigBlockSize(),
                                      array.length - offset);

                System.arraycopy(array, offset, rval[ k ]._data, 0, length);
                if (length != bigBlockSize.getBigBlockSize())
                {
                    Arrays.fill(rval[ k ]._data, length,
                          bigBlockSize.getBigBlockSize(),
                                _default_value);
                }
            }
            else
            {
                Arrays.fill(rval[ k ]._data, _default_value);
            }
            offset += bigBlockSize.getBigBlockSize();
        }
        return rval;
    }

    public static DataInputBlock getDataInputBlock(DocumentBlock[] blocks, int offset) {
        if(blocks == null || blocks.length == 0) {
           return null;
        }
        
        // Key things about the size of the block
        POIFSBigBlockSize bigBlockSize = blocks[0].bigBlockSize;
        int BLOCK_SHIFT = bigBlockSize.getHeaderValue();
        int BLOCK_SIZE = bigBlockSize.getBigBlockSize();
        int BLOCK_MASK = BLOCK_SIZE - 1;

        // Now do the offset lookup
        int firstBlockIndex = offset >> BLOCK_SHIFT;
        int firstBlockOffset= offset & BLOCK_MASK;
        return new DataInputBlock(blocks[firstBlockIndex]._data, firstBlockOffset);
    }

    /* ********** START extension of BigBlock ********** */

    /**
     * Write the block's data to an OutputStream
     *
     * @param stream the OutputStream to which the stored data should
     *               be written
     *
     * @exception IOException on problems writing to the specified
     *            stream
     */

    void writeData(final OutputStream stream)
        throws IOException
    {
        doWriteData(stream, _data);
    }

    /* **********  END  extension of BigBlock ********** */
}   // end public class DocumentBlock

