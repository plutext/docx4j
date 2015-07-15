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

import org.docx4j.org.apache.poi.util.LittleEndianConsts;

/**
 * <p>A class describing attributes of the Big Block Size</p>
 */
public final class POIFSBigBlockSize
{
   private int bigBlockSize;
   private short headerValue;
   
   protected POIFSBigBlockSize(int bigBlockSize, short headerValue) {
      this.bigBlockSize = bigBlockSize;
      this.headerValue = headerValue;
   }
   
   public int getBigBlockSize() {
      return bigBlockSize;
   }
   
   /**
    * Returns the value that gets written into the 
    *  header.
    * Is the power of two that corresponds to the
    *  size of the block, eg 512 => 9
    */
   public short getHeaderValue() {
      return headerValue;
   }
   
   public int getPropertiesPerBlock() {
      return bigBlockSize / POIFSConstants.PROPERTY_SIZE;
   }
   
   public int getBATEntriesPerBlock() {
      return bigBlockSize / LittleEndianConsts.INT_SIZE;
   }
   public int getXBATEntriesPerBlock() {
      return getBATEntriesPerBlock() - 1;
   }
   public int getNextXBATChainOffset() {
      return getXBATEntriesPerBlock() * LittleEndianConsts.INT_SIZE;
   }
}
