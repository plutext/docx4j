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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * A POIFS {@link DataSource} backed by a byte array.
 */
public class ByteArrayBackedDataSource extends DataSource {
   private byte[] buffer;
   private long size;
   
   public ByteArrayBackedDataSource(byte[] data, int size) {
      this.buffer = data;
      this.size = size;
   }
   public ByteArrayBackedDataSource(byte[] data) {
      this(data, data.length);
   }
                
   @Override
   public ByteBuffer read(int length, long position) {
      if(position >= size) {
         throw new IndexOutOfBoundsException(
               "Unable to read " + length + " bytes from " +
               position + " in stream of length " + size
         );
      }
      
      int toRead = (int)Math.min(length, size - position);
      return ByteBuffer.wrap(buffer, (int)position, toRead);
   }
   
   @Override
   public void write(ByteBuffer src, long position) {
      // Extend if needed
      long endPosition = position + src.capacity(); 
      if(endPosition > buffer.length) {
         extend(endPosition);
      }
      
      // Now copy
      src.get(buffer, (int)position, src.capacity());
      
      // Update size if needed
      if(endPosition > size) {
         size = endPosition;
      }
   }
   
   private void extend(long length) {
      // Consider extending by a bit more than requested
      long difference = length - buffer.length;
      if(difference < buffer.length*0.25) {
         difference = (long)(buffer.length*0.25);
      }
      if(difference < 4096) {
         difference = 4096;
      }

      byte[] nb = new byte[(int)(difference+buffer.length)];
      System.arraycopy(buffer, 0, nb, 0, (int)size);
      buffer = nb;
   }
   
   @Override
   public void copyTo(OutputStream stream) throws IOException {
      stream.write(buffer, 0, (int)size);
   }
   
   @Override
   public long size() {
      return size;
   }
   
   @Override
   public void close() {
      buffer = null;
      size = -1;
   }
}
