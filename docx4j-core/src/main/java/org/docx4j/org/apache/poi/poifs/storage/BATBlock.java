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
import java.util.Arrays;
import java.util.List;

import org.docx4j.org.apache.poi.poifs.common.POIFSBigBlockSize;
import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.util.LittleEndian;

/**
 * A block of block allocation table entries. BATBlocks are created
 * only through a static factory method: createBATBlocks.
 */
public final class BATBlock extends BigBlock {
    /**
     * For a regular fat block, these are 128 / 1024 
     *  next sector values.
     * For a XFat (DIFat) block, these are 127 / 1023
     *  next sector values, then a chaining value.
     */
    private int[] _values;
    
    /**
     * Does this BATBlock have any free sectors in it?
     */
    private boolean _has_free_sectors;
    
    /**
     * Where in the file are we?
     */
    private int ourBlockIndex;
    
    /**
     * Create a single instance initialized with default values
     */
    private BATBlock(POIFSBigBlockSize bigBlockSize)
    {
        super(bigBlockSize);
        
        int _entries_per_block = bigBlockSize.getBATEntriesPerBlock();
        _values = new int[_entries_per_block];
        _has_free_sectors = true;

        Arrays.fill(_values, POIFSConstants.UNUSED_BLOCK);
    }

    /**
     * Create a single instance initialized (perhaps partially) with entries
     *
     * @param entries the array of block allocation table entries
     * @param start_index the index of the first entry to be written
     *                    to the block
     * @param end_index the index, plus one, of the last entry to be
     *                  written to the block (writing is for all index
     *                  k, start_index <= k < end_index)
     */

    private BATBlock(POIFSBigBlockSize bigBlockSize, final int [] entries,
                     final int start_index, final int end_index)
    {
        this(bigBlockSize);
        for (int k = start_index; k < end_index; k++) {
           _values[k - start_index] = entries[k];
        }
        
        // Do we have any free sectors?
        if(end_index - start_index == _values.length) {
           recomputeFree();
        }
    }
    
    private void recomputeFree() {
       boolean hasFree = false;
       for(int k=0; k<_values.length; k++) {
          if(_values[k] == POIFSConstants.UNUSED_BLOCK) {
             hasFree = true;
             break;
          }
       }
       _has_free_sectors = hasFree;
    }

    /**
     * Create a single BATBlock from the byte buffer, which must hold at least
     *  one big block of data to be read.
     */
    public static BATBlock createBATBlock(final POIFSBigBlockSize bigBlockSize, ByteBuffer data)
    {
       // Create an empty block
       BATBlock block = new BATBlock(bigBlockSize);
       
       // Fill it
       byte[] buffer = new byte[LittleEndian.INT_SIZE];
       for(int i=0; i<block._values.length; i++) {
          data.get(buffer);
          block._values[i] = LittleEndian.getInt(buffer);
       }
       block.recomputeFree();
       
       // All done
       return block;
    }
    
    /**
     * Creates a single BATBlock, with all the values set to empty.
     */
    public static BATBlock createEmptyBATBlock(final POIFSBigBlockSize bigBlockSize, boolean isXBAT) {
       BATBlock block = new BATBlock(bigBlockSize);
       if(isXBAT) {
          block.setXBATChain(bigBlockSize, POIFSConstants.END_OF_CHAIN);
       }
       return block;
    }

    /**
     * Create an array of BATBlocks from an array of int block
     * allocation table entries
     *
     * @param entries the array of int entries
     *
     * @return the newly created array of BATBlocks
     */
    public static BATBlock [] createBATBlocks(final POIFSBigBlockSize bigBlockSize, final int [] entries)
    {
        int        block_count = calculateStorageRequirements(bigBlockSize, entries.length);
        BATBlock[] blocks      = new BATBlock[ block_count ];
        int        index       = 0;
        int        remaining   = entries.length;

        int _entries_per_block = bigBlockSize.getBATEntriesPerBlock();
        for (int j = 0; j < entries.length; j += _entries_per_block)
        {
            blocks[ index++ ] = new BATBlock(bigBlockSize, entries, j,
                                             (remaining > _entries_per_block)
                                             ? j + _entries_per_block
                                             : entries.length);
            remaining         -= _entries_per_block;
        }
        return blocks;
    }
    
    /**
     * Create an array of XBATBlocks from an array of int block
     * allocation table entries
     *
     * @param entries the array of int entries
     * @param startBlock the start block of the array of XBAT blocks
     *
     * @return the newly created array of BATBlocks
     */

    public static BATBlock [] createXBATBlocks(final POIFSBigBlockSize bigBlockSize,
                                               final int [] entries,
                                               final int startBlock)
    {
        int        block_count =
            calculateXBATStorageRequirements(bigBlockSize, entries.length);
        BATBlock[] blocks      = new BATBlock[ block_count ];
        int        index       = 0;
        int        remaining   = entries.length;

        int _entries_per_xbat_block = bigBlockSize.getXBATEntriesPerBlock();
        if (block_count != 0)
        {
            for (int j = 0; j < entries.length; j += _entries_per_xbat_block)
            {
                blocks[ index++ ] =
                    new BATBlock(bigBlockSize, entries, j,
                                 (remaining > _entries_per_xbat_block)
                                 ? j + _entries_per_xbat_block
                                 : entries.length);
                remaining         -= _entries_per_xbat_block;
            }
            for (index = 0; index < blocks.length - 1; index++)
            {
                blocks[ index ].setXBATChain(bigBlockSize, startBlock + index + 1);
            }
            blocks[ index ].setXBATChain(bigBlockSize, POIFSConstants.END_OF_CHAIN);
        }
        return blocks;
    }

    /**
     * Calculate how many BATBlocks are needed to hold a specified
     * number of BAT entries.
     *
     * @param entryCount the number of entries
     *
     * @return the number of BATBlocks needed
     */
    public static int calculateStorageRequirements(final POIFSBigBlockSize bigBlockSize, final int entryCount)
    {
        int _entries_per_block = bigBlockSize.getBATEntriesPerBlock();
        return (entryCount + _entries_per_block - 1) / _entries_per_block;
    }

    /**
     * Calculate how many XBATBlocks are needed to hold a specified
     * number of BAT entries.
     *
     * @param entryCount the number of entries
     *
     * @return the number of XBATBlocks needed
     */
    public static int calculateXBATStorageRequirements(final POIFSBigBlockSize bigBlockSize, final int entryCount)
    {
        int _entries_per_xbat_block = bigBlockSize.getXBATEntriesPerBlock();
        return (entryCount + _entries_per_xbat_block - 1)
               / _entries_per_xbat_block;
    }
    
    /**
     * Calculates the maximum size of a file which is addressable given the
     *  number of FAT (BAT) sectors specified. (We don't care if those BAT
     *  blocks come from the 109 in the header, or from header + XBATS, it
     *  won't affect the calculation)
     *  
     * The actual file size will be between [size of fatCount-1 blocks] and
     *   [size of fatCount blocks].
     *  For 512 byte block sizes, this means we may over-estimate by up to 65kb.
     *  For 4096 byte block sizes, this means we may over-estimate by up to 4mb
     */
    public static long calculateMaximumSize(final POIFSBigBlockSize bigBlockSize,
          final int numBATs) {
       long size = 1; // Header isn't FAT addressed
       
       // The header has up to 109 BATs, and extra ones are referenced
       //  from XBATs
       // However, all BATs can contain 128/1024 blocks
       size += (numBATs * bigBlockSize.getBATEntriesPerBlock());
       
       // So far we've been in sector counts, turn into bytes
       return size * bigBlockSize.getBigBlockSize();
    }
    public static long calculateMaximumSize(final HeaderBlock header)
    {
       return calculateMaximumSize(header.getBigBlockSize(), header.getBATCount());
    }

    /**
     * Returns the BATBlock that handles the specified offset,
     *  and the relative index within it.
     * The List of BATBlocks must be in sequential order
     */
    public static BATBlockAndIndex getBATBlockAndIndex(final int offset, 
                final HeaderBlock header, final List<BATBlock> bats) {
       POIFSBigBlockSize bigBlockSize = header.getBigBlockSize();
       
       int whichBAT = (int)Math.floor(offset / bigBlockSize.getBATEntriesPerBlock());
       int index = offset % bigBlockSize.getBATEntriesPerBlock();
       return new BATBlockAndIndex( index, bats.get(whichBAT) );
    }
    
    /**
     * Returns the BATBlock that handles the specified offset,
     *  and the relative index within it, for the mini stream.
     * The List of BATBlocks must be in sequential order
     */
    public static BATBlockAndIndex getSBATBlockAndIndex(final int offset, 
          final HeaderBlock header, final List<BATBlock> sbats) {
       POIFSBigBlockSize bigBlockSize = header.getBigBlockSize();
       
       // SBATs are so much easier, as they're chained streams
       int whichSBAT = (int)Math.floor(offset / bigBlockSize.getBATEntriesPerBlock());
       int index = offset % bigBlockSize.getBATEntriesPerBlock();
       return new BATBlockAndIndex( index, sbats.get(whichSBAT) );
    }
    
    private void setXBATChain(final POIFSBigBlockSize bigBlockSize, int chainIndex)
    {
        int _entries_per_xbat_block = bigBlockSize.getXBATEntriesPerBlock();
        _values[ _entries_per_xbat_block ] = chainIndex;
    }
    
    /**
     * Does this BATBlock have any free sectors in it, or
     *  is it full?
     */
    public boolean hasFreeSectors() {
       return _has_free_sectors;
    }
    /**
     * How many sectors in this block are taken?
     * Note that calling {@link #hasFreeSectors()} is much quicker
     */
    public int getUsedSectors(boolean isAnXBAT) {
        int usedSectors = 0;
        int toCheck = _values.length;
        if (isAnXBAT) toCheck--; // Last is a chain location
        for(int k=0; k<toCheck; k++) {
            if(_values[k] != POIFSConstants.UNUSED_BLOCK) {
                usedSectors ++;
            }
        }
        return usedSectors;
    }
    
    public int getValueAt(int relativeOffset) {
       if(relativeOffset >= _values.length) {
          throw new ArrayIndexOutOfBoundsException(
                "Unable to fetch offset " + relativeOffset + " as the " + 
                "BAT only contains " + _values.length + " entries"
          ); 
       }
       return _values[relativeOffset];
    }
    public void setValueAt(int relativeOffset, int value) {
       int oldValue = _values[relativeOffset];
       _values[relativeOffset] = value;
       
       // Do we need to re-compute the free?
       if(value == POIFSConstants.UNUSED_BLOCK) {
          _has_free_sectors = true;
          return;
       }
       if(oldValue == POIFSConstants.UNUSED_BLOCK) {
          recomputeFree();
       }
    }
    
    /**
     * Record where in the file we live
     */
    public void setOurBlockIndex(int index) {
       this.ourBlockIndex = index;
    }
    /**
     * Retrieve where in the file we live 
     */
    public int getOurBlockIndex() {
       return ourBlockIndex;
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
       // Save it out
       stream.write( serialize() );
    }
    
    void writeData(final ByteBuffer block)
        throws IOException
    {
       // Save it out
       block.put( serialize() );
    }
    
    private byte[] serialize() {
       // Create the empty array
       byte[] data = new byte[ bigBlockSize.getBigBlockSize() ];
       
       // Fill in the values
       int offset = 0;
       for(int i=0; i<_values.length; i++) {
          LittleEndian.putInt(data, offset, _values[i]);
          offset += LittleEndian.INT_SIZE;
       }
       
       // Done
       return data;
    }

    /* **********  END  extension of BigBlock ********** */
    
    
    public static class BATBlockAndIndex {
       private final int index;
       private final BATBlock block;
       private BATBlockAndIndex(int index, BATBlock block) {
          this.index = index;
          this.block = block;
       }
       public int getIndex() {
          return index;
       }
       public BATBlock getBlock() {
          return block;
       }
    }
}

