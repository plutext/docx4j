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


package org.docx4j.org.apache.poi.poifs.filesystem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.property.RootProperty;
import org.docx4j.org.apache.poi.poifs.storage.BATBlock;
import org.docx4j.org.apache.poi.poifs.storage.BATBlock.BATBlockAndIndex;
import org.docx4j.org.apache.poi.poifs.storage.HeaderBlock;
import org.docx4j.org.apache.poi.util.RecordFormatException;

/**
 * This class handles the MiniStream (small block store)
 * in the NIO case for {@link POIFSFileSystem}
 */
public class POIFSMiniStore extends BlockStore {
    private final POIFSFileSystem _filesystem;
    private POIFSStream _mini_stream;
    private final List<BATBlock> _sbat_blocks;
    private final HeaderBlock _header;
    private final RootProperty _root;

    POIFSMiniStore(POIFSFileSystem filesystem, RootProperty root,
                   List<BATBlock> sbats, HeaderBlock header) {
        if (root == null) {
            throw new RecordFormatException("Invalid argument to POIFSMiniStore: root is null");
        }
        this._filesystem = filesystem;
        this._sbat_blocks = sbats;
        this._header = header;
        this._root = root;

        this._mini_stream = new POIFSStream(filesystem, root.getStartBlock());
    }

    /**
     * Load the block at the given offset.
     */
    protected ByteBuffer getBlockAt(final int offset) throws IOException {
        // Which big block is this?
        int byteOffset = offset * POIFSConstants.SMALL_BLOCK_SIZE;
        int bigBlockNumber = byteOffset / _filesystem.getBigBlockSize();
        int bigBlockOffset = byteOffset % _filesystem.getBigBlockSize();

        // Now locate the data block for it
        Iterator<Integer> it = _mini_stream.getBlockOffsetIterator();
        for (int i = 0; i < bigBlockNumber; i++) {
            it.next();
        }

        ByteBuffer dataBlock = _filesystem.getBlockAt(it.next());
        assert(dataBlock != null);

        // Position ourselves, and take a slice
        dataBlock.position(
                dataBlock.position() + bigBlockOffset
        );
        ByteBuffer miniBuffer = dataBlock.slice();
        miniBuffer.limit(POIFSConstants.SMALL_BLOCK_SIZE);
        return miniBuffer;
    }

    /**
     * Load the block, extending the underlying stream if needed
     */
    protected ByteBuffer createBlockIfNeeded(final int offset) throws IOException {
        boolean firstInStore = false;
        if (_mini_stream.getStartBlock() == POIFSConstants.END_OF_CHAIN) {
            firstInStore = true;
        }

        // Try to get it without extending the stream
        if (!firstInStore) {
            try {
                return getBlockAt(offset);
            } catch (NoSuchElementException ignored) {
            }
        }

        // Need to extend the stream
        // TODO Replace this with proper append support
        // For now, do the extending by hand...

        // Ask for another block
        int newBigBlock = _filesystem.getFreeBlock();
        _filesystem.createBlockIfNeeded(newBigBlock);

        // If we are the first block to be allocated, initialise the stream
        if (firstInStore) {
            _filesystem._get_property_table().getRoot().setStartBlock(newBigBlock);
            _mini_stream = new POIFSStream(_filesystem, newBigBlock);
        } else {
            // Tack it onto the end of our chain
            ChainLoopDetector loopDetector = _filesystem.getChainLoopDetector();
            int block = _mini_stream.getStartBlock();
            while (true) {
                loopDetector.claim(block);
                int next = _filesystem.getNextBlock(block);
                if (next == POIFSConstants.END_OF_CHAIN) {
                    break;
                }
                block = next;
            }
            _filesystem.setNextBlock(block, newBigBlock);
        }

        // This is now the new end
        _filesystem.setNextBlock(newBigBlock, POIFSConstants.END_OF_CHAIN);

        // Now try again, to get the real small block
        return createBlockIfNeeded(offset);
    }

    /**
     * Returns the BATBlock that handles the specified offset,
     * and the relative index within it
     */
    protected BATBlockAndIndex getBATBlockAndIndex(final int offset) {
        return BATBlock.getSBATBlockAndIndex(
                offset, _header, _sbat_blocks
        );
    }

    /**
     * Works out what block follows the specified one.
     */
    protected int getNextBlock(final int offset) {
        BATBlockAndIndex bai = getBATBlockAndIndex(offset);
        return bai.getBlock().getValueAt(bai.getIndex());
    }

    /**
     * Changes the record of what block follows the specified one.
     */
    protected void setNextBlock(final int offset, final int nextBlock) {
        BATBlockAndIndex bai = getBATBlockAndIndex(offset);
        bai.getBlock().setValueAt(
                bai.getIndex(), nextBlock
        );
    }

    /**
     * Finds a free block, and returns its offset.
     * This method will extend the file if needed, and if doing
     * so, allocate new FAT blocks to address the extra space.
     */
    protected int getFreeBlock() throws IOException {
        int sectorsPerSBAT = _filesystem.getBigBlockSizeDetails().getBATEntriesPerBlock();

        // First up, do we have any spare ones?
        int offset = 0;
        for (BATBlock sbat : _sbat_blocks) {
            // Check this one
            if (sbat.hasFreeSectors()) {
                // Claim one of them and return it
                for (int j = 0; j < sectorsPerSBAT; j++) {
                    int sbatValue = sbat.getValueAt(j);
                    if (sbatValue == POIFSConstants.UNUSED_BLOCK) {
                        // Bingo
                        return offset + j;
                    }
                }
            }

            // Move onto the next SBAT
            offset += sectorsPerSBAT;
        }

        // If we get here, then there aren't any
        //  free sectors in any of the SBATs
        // So, we need to extend the chain and add another

        // Create a new BATBlock
        BATBlock newSBAT = BATBlock.createEmptyBATBlock(_filesystem.getBigBlockSizeDetails(), false);
        int batForSBAT = _filesystem.getFreeBlock();
        newSBAT.setOurBlockIndex(batForSBAT);

        // Are we the first SBAT?
        if (_header.getSBATCount() == 0) {
            // Tell the header that we've got our first SBAT there
            _header.setSBATStart(batForSBAT);
            _header.setSBATBlockCount(1);
        } else {
            // Find the end of the SBAT stream, and add the sbat in there
            ChainLoopDetector loopDetector = _filesystem.getChainLoopDetector();
            int batOffset = _header.getSBATStart();
            while (true) {
                loopDetector.claim(batOffset);
                int nextBat = _filesystem.getNextBlock(batOffset);
                if (nextBat == POIFSConstants.END_OF_CHAIN) {
                    break;
                }
                batOffset = nextBat;
            }

            // Add it in at the end
            _filesystem.setNextBlock(batOffset, batForSBAT);

            // And update the count
            _header.setSBATBlockCount(
                    _header.getSBATCount() + 1
            );
        }

        // Finish allocating
        _filesystem.setNextBlock(batForSBAT, POIFSConstants.END_OF_CHAIN);
        _sbat_blocks.add(newSBAT);

        // Return our first spot
        return offset;
    }

    @Override
    protected ChainLoopDetector getChainLoopDetector() {
        return new ChainLoopDetector(_root.getSize());
    }

    protected int getBlockStoreBlockSize() {
        return POIFSConstants.SMALL_BLOCK_SIZE;
    }

    /**
     * Writes the SBATs to their backing blocks, and updates
     * the mini-stream size in the properties. Stream size is
     * based on full blocks used, not the data within the streams
     */
    void syncWithDataSource() throws IOException {
        for (BATBlock sbat : _sbat_blocks) {
            ByteBuffer block = _filesystem.getBlockAt(sbat.getOurBlockIndex());
            sbat.writeData(block);
        }

        // Set the size on the root in terms of the number of SBAT blocks
        // RootProperty.setSize does the sbat -> bytes conversion for us
        _filesystem._get_property_table().getRoot().setSize(computeSize());
    }

    /**
     * Computes the size of the mini-stream (in number of mini blocks). The trailing
     * unallocated mini blocks are ignored, the others are counted as allocated. This
     * behaviour was checked with MSI files signed with signtool.
     */
    private int computeSize() {
        int entriesPerBlock = _filesystem.getBigBlockSizeDetails().getBATEntriesPerBlock();
        for (int sbatIndex = _sbat_blocks.size() - 1; sbatIndex >= 0; sbatIndex--) {
            BATBlock sbat = _sbat_blocks.get(sbatIndex);
            int occupiedSize = sbat.getOccupiedSize();
            if (occupiedSize > 0) {
                return (sbatIndex * entriesPerBlock) + occupiedSize;
            }
        }

        return 0;
    }

    @Override
    protected void releaseBuffer(ByteBuffer buffer) {
        _filesystem.releaseBuffer(buffer);
    }
}
