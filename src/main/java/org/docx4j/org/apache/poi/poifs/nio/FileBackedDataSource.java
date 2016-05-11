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

package org.docx4j.org.apache.poi.poifs.nio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import org.docx4j.org.apache.poi.util.IOUtils;

/**
 * A POIFS {@link DataSource} backed by a File
 */
public class FileBackedDataSource extends DataSource {
   private FileChannel channel;
   private boolean writable;
   // remember file base, which needs to be closed too
   private RandomAccessFile srcFile;

   public FileBackedDataSource(File file) throws FileNotFoundException {
       this(newSrcFile(file, "r"), true);
   }

   public FileBackedDataSource(File file, boolean readOnly) throws FileNotFoundException {
       this(newSrcFile(file, readOnly ? "r" : "rw"), readOnly);
   }

   public FileBackedDataSource(RandomAccessFile srcFile, boolean readOnly) {
       this(srcFile.getChannel(), readOnly);
       this.srcFile = srcFile;
   }   
   
   public FileBackedDataSource(FileChannel channel, boolean readOnly) {
      this.channel = channel;
      this.writable = !readOnly;
   }
   
   public boolean isWriteable() {
       return this.writable;
   }
   
   public FileChannel getChannel() {
       return this.channel;
   }

   @Override
   public ByteBuffer read(int length, long position) throws IOException {
      if(position >= size()) {
         throw new IllegalArgumentException("Position " + position + " past the end of the file");
      }
      
      // Do we read or map (for read/write?
      ByteBuffer dst;
      int worked = -1;
      if (writable) {
          dst = channel.map(FileChannel.MapMode.READ_WRITE, position, length);
          worked = 0;
      } else {
          // Read
          channel.position(position);
          dst = ByteBuffer.allocate(length);
          worked = IOUtils.readFully(channel, dst);
      }

      // Check
      if(worked == -1) {
         throw new IllegalArgumentException("Position " + position + " past the end of the file");
      }

      // Ready it for reading
      dst.position(0);

      // All done
      return dst;
   }

   @Override
   public void write(ByteBuffer src, long position) throws IOException {
      channel.write(src, position);
   }

   @Override
   public void copyTo(OutputStream stream) throws IOException {
      // Wrap the OutputSteam as a channel
      WritableByteChannel out = Channels.newChannel(stream);
      // Now do the transfer
      channel.transferTo(0, channel.size(), out);
   }

   @Override
   public long size() throws IOException {
      return channel.size();
   }

   @Override
   public void close() throws IOException {
      if (srcFile != null) {
          // see http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4796385
          srcFile.close();
      } else {
          channel.close();
      }
   }

   private static RandomAccessFile newSrcFile(File file, String mode) throws FileNotFoundException {
       if(!file.exists()) {
           throw new FileNotFoundException(file.toString());
        }
        return new RandomAccessFile(file, mode);
   }
}
