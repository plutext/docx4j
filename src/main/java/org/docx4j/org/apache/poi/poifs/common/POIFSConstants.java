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
        

package org.docx4j.org.apache.poi.poifs.common;

/**
 * <p>A repository for constants shared by POI classes.</p>
 */
public interface POIFSConstants
{
    /** Most files use 512 bytes as their big block size */
    public static final int SMALLER_BIG_BLOCK_SIZE = 0x0200;
    public static final POIFSBigBlockSize SMALLER_BIG_BLOCK_SIZE_DETAILS = 
       new POIFSBigBlockSize(SMALLER_BIG_BLOCK_SIZE, (short)9);
    /** Some use 4096 bytes */
    public static final int LARGER_BIG_BLOCK_SIZE = 0x1000;
    public static final POIFSBigBlockSize LARGER_BIG_BLOCK_SIZE_DETAILS = 
       new POIFSBigBlockSize(LARGER_BIG_BLOCK_SIZE, (short)12);
    
    /** How big a block in the small block stream is. Fixed size */
    public static final int SMALL_BLOCK_SIZE = 0x0040; 
    
    /** How big a single property is */
    public static final int PROPERTY_SIZE  = 0x0080;
    
    /** 
     * The minimum size of a document before it's stored using 
     *  Big Blocks (normal streams). Smaller documents go in the 
     *  Mini Stream (SBAT / Small Blocks)
     */
    public static final int BIG_BLOCK_MINIMUM_DOCUMENT_SIZE = 0x1000;
    
    /** The highest sector number you're allowed, 0xFFFFFFFA */
    public static final int LARGEST_REGULAR_SECTOR_NUMBER = -5;
    
    /** Indicates the sector holds a DIFAT block (0xFFFFFFFC) */
    public static final int DIFAT_SECTOR_BLOCK   = -4;
    /** Indicates the sector holds a FAT block (0xFFFFFFFD) */
    public static final int FAT_SECTOR_BLOCK   = -3;
    /** Indicates the sector is the end of a chain (0xFFFFFFFE) */
    public static final int END_OF_CHAIN   = -2;
    /** Indicates the sector is not used (0xFFFFFFFF) */
    public static final int UNUSED_BLOCK   = -1;
    
    /** The first 4 bytes of an OOXML file, used in detection */
    public static final byte[] OOXML_FILE_HEADER = 
    	new byte[] { 0x50, 0x4b, 0x03, 0x04 };
}   // end public interface POIFSConstants;
