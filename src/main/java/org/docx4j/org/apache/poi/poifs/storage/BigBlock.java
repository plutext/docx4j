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

/**
 * Abstract base class of all POIFS block storage classes. All
 * extensions of BigBlock should write 512 or 4096 bytes of data when
 * requested to write their data (as per their BigBlockSize).
 *
 * This class has package scope, as there is no reason at this time to
 * make the class public.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */

import java.io.IOException;
import java.io.OutputStream;

import org.docx4j.org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;

abstract class BigBlock
    implements BlockWritable
{
    /** 
     * Either 512 bytes ({@link POIFSConstants#SMALLER_BIG_BLOCK_SIZE}) 
     *  or 4096 bytes ({@link POIFSConstants#LARGER_BIG_BLOCK_SIZE})
     */
    protected POIFSBigBlockSize bigBlockSize;
    
    protected BigBlock(POIFSBigBlockSize bigBlockSize) {
       this.bigBlockSize = bigBlockSize;
    }

    /**
     * Default implementation of write for extending classes that
     * contain their data in a simple array of bytes.
     *
     * @param stream the OutputStream to which the data should be
     *               written.
     * @param data the byte array of to be written.
     *
     * @exception IOException on problems writing to the specified
     *            stream.
     */

    protected void doWriteData(final OutputStream stream, final byte [] data)
        throws IOException
    {
        stream.write(data);
    }

    /**
     * Write the block's data to an OutputStream
     *
     * @param stream the OutputStream to which the stored data should
     *               be written
     *
     * @exception IOException on problems writing to the specified
     *            stream
     */

    abstract void writeData(final OutputStream stream)
        throws IOException;

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
        writeData(stream);
    }

    /* **********  END  implementation of BlockWritable ********** */
}   // end abstract class BigBlock

