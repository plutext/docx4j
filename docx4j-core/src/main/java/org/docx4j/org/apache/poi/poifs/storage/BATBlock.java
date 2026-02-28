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
import org.docx4j.org.apache.poi.util.LittleEndianConsts;

/**
 * A block of block allocation table entries. BATBlocks are created
 * only through a static factory method: createBATBlocks.
 */
public final class BATBlock implements BlockWritable {
    /**
     * Either 512 bytes ({@link POIFSConstants#SMALLER_BIG_BLOCK_SIZE})
     *  or 4096 bytes ({@link POIFSConstants#LARGER_BIG_BLOCK_SIZE})
     */
    private POIFSBigBlockSize bigBlockSize;

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
        this.bigBlockSize = bigBlockSize;

        int _entries_per_block = bigBlockSize.getBATEntriesPerBlock();
        _values = new int[_entries_per_block];
        _has_free_sectors = true;

        Arrays.fill(_values, POIFSConstants.UNUSED_BLOCK);
    }

    private void recomputeFree() {
       boolean hasFree = false;
        for (int _value : _values) {
            if (_value == POIFSConstants.UNUSED_BLOCK) {
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
       byte[] buffer = new byte[LittleEndianConsts.INT_SIZE];
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
           final int _entries_per_xbat_block = bigBlockSize.getXBATEntriesPerBlock();
           block._values[ _entries_per_xbat_block ] = POIFSConstants.END_OF_CHAIN;
       }
       return block;
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
       // Header isn't FAT addressed
       long size = 1;

       // The header has up to 109 BATs, and extra ones are referenced
       //  from XBATs
       // However, all BATs can contain 128/1024 blocks
       size += (((long)numBATs) * bigBlockSize.getBATEntriesPerBlock());

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
       int entriesPerBlock = bigBlockSize.getBATEntriesPerBlock();

       int whichBAT = offset / entriesPerBlock;
       int index = offset % entriesPerBlock;
       return new BATBlockAndIndex( index, bats.get(whichBAT) );
    }

    /**
     * Returns the BATBlock that handles the specified offset,
     *  and the relative index within it, for the mini stream.
     * The List of BATBlocks must be in sequential order
     */
    public static BATBlockAndIndex getSBATBlockAndIndex(final int offset,
          final HeaderBlock header, final List<BATBlock> sbats) {
        return getBATBlockAndIndex(offset, header, sbats);
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

    /**
     * How much of this block is occupied?.
     * This counts the number of sectors up and including the last used sector.
     * Note that this is different from {@link #getUsedSectors(boolean)} which
     * could be smaller as it does not count unused sectors where there are
     * used ones after it (i.e. fragmentation).
     *
     * @since 5.0.0
     */
    public int getOccupiedSize() {
        for (int k = _values.length - 1; k >= 0; k--) {
            if (_values[k] != POIFSConstants.UNUSED_BLOCK) {
                return k + 1;
            }
        }
        return 0;
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

    /**
     * Write the block's data to an OutputStream
     *
     * @param stream the OutputStream to which the stored data should
     *               be written
     *
     * @throws IOException on problems writing to the specified
     *            stream
     */
    public void writeBlocks(final OutputStream stream) throws IOException {
        // Save it out
        stream.write( serialize() );
    }

    public void writeData(final ByteBuffer block) {
       // Save it out
       block.put( serialize() );
    }

    private byte[] serialize() {
       // Create the empty array
       byte[] data = new byte[ bigBlockSize.getBigBlockSize() ];

        // Fill in the values
        int offset = 0;
        for (int _value : _values) {
            LittleEndian.putInt(data, offset, _value);
            offset += LittleEndianConsts.INT_SIZE;
        }

       // Done
       return data;
    }

    public static final class BATBlockAndIndex {
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

