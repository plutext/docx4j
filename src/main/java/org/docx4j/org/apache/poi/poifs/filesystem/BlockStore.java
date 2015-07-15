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

package org.docx4j.org.apache.poi.poifs.filesystem;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.docx4j.org.apache.poi.poifs.storage.BATBlock.BATBlockAndIndex;

/**
 * This abstract class describes a way to read, store, chain
 *  and free a series of blocks (be they Big or Small ones)
 */
public abstract class BlockStore {
   /**
    * Returns the size of the blocks managed through the block store.
    */
   protected abstract int getBlockStoreBlockSize();
   
    /**
     * Load the block at the given offset.
     */
    protected abstract ByteBuffer getBlockAt(final int offset) throws IOException;
    
    /**
     * Extends the file if required to hold blocks up to
     *  the specified offset, and return the block from there. 
     */
    protected abstract ByteBuffer createBlockIfNeeded(final int offset) throws IOException;
    
    /**
     * Returns the BATBlock that handles the specified offset,
     *  and the relative index within it
     */
    protected abstract BATBlockAndIndex getBATBlockAndIndex(final int offset);
    
    /**
     * Works out what block follows the specified one.
     */
    protected abstract int getNextBlock(final int offset);
    
    /**
     * Changes the record of what block follows the specified one.
     */
    protected abstract void setNextBlock(final int offset, final int nextBlock);
    
    /**
     * Finds a free block, and returns its offset.
     * This method will extend the file/stream if needed, and if doing
     *  so, allocate new FAT blocks to address the extra space.
     */
    protected abstract int getFreeBlock() throws IOException;
    
    /**
     * Creates a Detector for loops in the chain 
     */
    protected abstract ChainLoopDetector getChainLoopDetector() throws IOException;
    
    /**
     * Used to detect if a chain has a loop in it, so
     *  we can bail out with an error rather than
     *  spinning away for ever... 
     */
    protected class ChainLoopDetector {
       private boolean[] used_blocks;
       protected ChainLoopDetector(long rawSize) {
          int numBlocks = (int)Math.ceil( rawSize / getBlockStoreBlockSize() );
          used_blocks = new boolean[numBlocks];
       }
       protected void claim(int offset) {
          if(offset >= used_blocks.length) {
             // They're writing, and have had new blocks requested
             //  for the write to proceed. That means they're into
             //  blocks we've allocated for them, so are safe
             return;
          }
          
          // Claiming an existing block, ensure there's no loop
          if(used_blocks[offset]) {
             throw new IllegalStateException(
                   "Potential loop detected - Block " + offset + 
                   " was already claimed but was just requested again"
             );
          }
          used_blocks[offset] = true;
       }
    }
}

