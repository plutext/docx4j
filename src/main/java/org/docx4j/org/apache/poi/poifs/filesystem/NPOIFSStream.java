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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.docx4j.org.apache.poi.poifs.common.POIFSConstants;
import org.docx4j.org.apache.poi.poifs.filesystem.BlockStore.ChainLoopDetector;
import org.docx4j.org.apache.poi.poifs.property.Property;
import org.docx4j.org.apache.poi.poifs.storage.HeaderBlock;

/**
 * This handles reading and writing a stream within a
 *  {@link NPOIFSFileSystem}. It can supply an iterator
 *  to read blocks, and way to write out to existing and
 *  new blocks.
 * Most users will want a higher level version of this, 
 *  which deals with properties to track which stream
 *  this is.
 * This only works on big block streams, it doesn't
 *  handle small block ones.
 * This uses the new NIO code
 * 
 * TODO Implement a streaming write method, and append
 */

public class NPOIFSStream implements Iterable<ByteBuffer>
{
	private BlockStore blockStore;
	private int startBlock;
	private OutputStream outStream;
	
	/**
	 * Constructor for an existing stream. It's up to you
	 *  to know how to get the start block (eg from a 
	 *  {@link HeaderBlock} or a {@link Property}) 
	 */
	public NPOIFSStream(BlockStore blockStore, int startBlock) {
	   this.blockStore = blockStore;
	   this.startBlock = startBlock;
	}
	
	/**
	 * Constructor for a new stream. A start block won't
	 *  be allocated until you begin writing to it.
	 */
	public NPOIFSStream(BlockStore blockStore) {
      this.blockStore = blockStore;
	   this.startBlock = POIFSConstants.END_OF_CHAIN;
	}
	
	/**
	 * What block does this stream start at?
	 * Will be {@link POIFSConstants#END_OF_CHAIN} for a
	 *  new stream that hasn't been written to yet.
	 */
	public int getStartBlock() {
	   return startBlock;
	}

	/**
	 * Returns an iterator that'll supply one {@link ByteBuffer}
	 *  per block in the stream.
	 */
   public Iterator<ByteBuffer> iterator() {
      return getBlockIterator();
   }
	
   public Iterator<ByteBuffer> getBlockIterator() {
      if(startBlock == POIFSConstants.END_OF_CHAIN) {
         throw new IllegalStateException(
               "Can't read from a new stream before it has been written to"
         );
      }
      return new StreamBlockByteBufferIterator(startBlock);
   }

   /**
    * Updates the contents of the stream to the new
    *  set of bytes.
    * Note - if this is property based, you'll still
    *  need to update the size in the property yourself
    */
   public void updateContents(byte[] contents) throws IOException {
       OutputStream os = getOutputStream();
       os.write(contents);
       os.close();
   }

   public OutputStream getOutputStream() throws IOException {
       if (outStream == null) {
           outStream = new StreamBlockByteBuffer();
       }
       return outStream;
   }
   
   // TODO Streaming write support
   // TODO  then convert fixed sized write to use streaming internally
   // TODO Append write support (probably streaming)
   
   /**
    * Frees all blocks in the stream
    */
   public void free() throws IOException {
      ChainLoopDetector loopDetector = blockStore.getChainLoopDetector();
      free(loopDetector);
   }
   private void free(ChainLoopDetector loopDetector) {
      int nextBlock = startBlock;
      while(nextBlock != POIFSConstants.END_OF_CHAIN) {
         int thisBlock = nextBlock;
         loopDetector.claim(thisBlock);
         nextBlock = blockStore.getNextBlock(thisBlock);
         blockStore.setNextBlock(thisBlock, POIFSConstants.UNUSED_BLOCK);
      }
      this.startBlock = POIFSConstants.END_OF_CHAIN;
   }
   
   /**
    * Class that handles a streaming read of one stream
    */
   protected class StreamBlockByteBufferIterator implements Iterator<ByteBuffer> {
      private ChainLoopDetector loopDetector;
      private int nextBlock;
      
      protected StreamBlockByteBufferIterator(int firstBlock) {
         this.nextBlock = firstBlock;
         try {
            this.loopDetector = blockStore.getChainLoopDetector();
         } catch(IOException e) {
            throw new RuntimeException(e);
         }
      }

      public boolean hasNext() {
         if(nextBlock == POIFSConstants.END_OF_CHAIN) {
            return false;
         }
         return true;
      }

      public ByteBuffer next() {
         if(nextBlock == POIFSConstants.END_OF_CHAIN) {
            throw new IndexOutOfBoundsException("Can't read past the end of the stream");
         }
         
         try {
            loopDetector.claim(nextBlock);
            ByteBuffer data = blockStore.getBlockAt(nextBlock);
            nextBlock = blockStore.getNextBlock(nextBlock);
            return data;
         } catch(IOException e) {
            throw new RuntimeException(e);
         }
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }
   }
   
   protected class StreamBlockByteBuffer extends OutputStream {
       byte oneByte[] = new byte[1];
       ByteBuffer buffer;
       // Make sure we don't encounter a loop whilst overwriting
       // the existing blocks
       ChainLoopDetector loopDetector;
       int prevBlock, nextBlock;

       protected StreamBlockByteBuffer() throws IOException {
           loopDetector = blockStore.getChainLoopDetector();
           prevBlock = POIFSConstants.END_OF_CHAIN;
           nextBlock = startBlock;
       }

       protected void createBlockIfNeeded() throws IOException {
           if (buffer != null && buffer.hasRemaining()) return;
           
           int thisBlock = nextBlock;
           
           // Allocate a block if needed, otherwise figure
           //  out what the next block will be
           if(thisBlock == POIFSConstants.END_OF_CHAIN) {
              thisBlock = blockStore.getFreeBlock();
              loopDetector.claim(thisBlock);
              
              // We're on the end of the chain
              nextBlock = POIFSConstants.END_OF_CHAIN;
              
              // Mark the previous block as carrying on to us if needed
              if(prevBlock != POIFSConstants.END_OF_CHAIN) {
                 blockStore.setNextBlock(prevBlock, thisBlock);
              }
              blockStore.setNextBlock(thisBlock, POIFSConstants.END_OF_CHAIN);
              
              // If we've just written the first block on a 
              //  new stream, save the start block offset
              if(startBlock == POIFSConstants.END_OF_CHAIN) {
                 startBlock = thisBlock;
              }
           } else {
              loopDetector.claim(thisBlock);
              nextBlock = blockStore.getNextBlock(thisBlock);
           }

           buffer = blockStore.createBlockIfNeeded(thisBlock);
           
           // Update pointers
           prevBlock = thisBlock;
       }
       
       public void write(int b) throws IOException {
            oneByte[0] = (byte)(b & 0xFF);
            write(oneByte);
       }
    
        public void write(byte[] b, int off, int len) throws IOException {
            if ((off < 0) || (off > b.length) || (len < 0) ||
                    ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }

            do {
                createBlockIfNeeded();
                int writeBytes = Math.min(buffer.remaining(), len);
                buffer.put(b, off, writeBytes);
                off += writeBytes;
                len -= writeBytes;
            } while (len > 0);
        }
    
        public void close() throws IOException {
            // If we're overwriting, free any remaining blocks
            NPOIFSStream toFree = new NPOIFSStream(blockStore, nextBlock);
            toFree.free(loopDetector);
            
            // Mark the end of the stream, if we have any data
            if (prevBlock != POIFSConstants.END_OF_CHAIN) {
                blockStore.setNextBlock(prevBlock, POIFSConstants.END_OF_CHAIN);
            }
        }
   }
}

