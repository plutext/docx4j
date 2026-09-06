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

package org.docx4j.org.apache.poi.hwmf.record;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

public class HwmfHeader implements GenericRecord {
    public enum HwmfHeaderMetaType {
        MEMORY_METAFILE, DISK_METAFILE
    }

    private HwmfHeaderMetaType type;
    private int recordSize;
    private int version;
    private int filesize;
    private int numberOfObjects;
    private long maxRecord;
    private int numberOfMembers;
    
    public HwmfHeader(LittleEndianInputStream leis) throws IOException {
        // Type (2 bytes):  A 16-bit unsigned integer that defines the type of metafile
        // MEMORYMETAFILE = 0x0001, DISKMETAFILE = 0x0002 
        type = HwmfHeaderMetaType.values()[leis.readUShort()-1];

        // HeaderSize (2 bytes):  A 16-bit unsigned integer that defines the number
        // of 16-bit words in the header.
        recordSize = leis.readUShort();
        int bytesLeft = recordSize*LittleEndianConsts.SHORT_SIZE-4;
        
        // Version (2 bytes):  A 16-bit unsigned integer that defines the metafile version.
        // METAVERSION100 = 0x0100, METAVERSION300 = 0x0300
        version = leis.readUShort();
        bytesLeft -= LittleEndianConsts.SHORT_SIZE;
        
        // SizeLow (2 bytes):  A 16-bit unsigned integer that defines the low-order word
        // of the number of 16-bit words in the entire metafile.
        // SizeHigh (2 bytes):  A 16-bit unsigned integer that defines the high-order word
        // of the number of 16-bit words in the entire metafile.
        filesize = leis.readInt();
        bytesLeft -= LittleEndianConsts.INT_SIZE;
        
        // NumberOfObjects (2 bytes):  A 16-bit unsigned integer that specifies the number
        // of graphics objects that are defined in the entire metafile. These objects include
        // brushes, pens, and the other objects
        numberOfObjects = leis.readUShort();
        bytesLeft -= LittleEndianConsts.SHORT_SIZE;
        
        // MaxRecord (4 bytes):  A 32-bit unsigned integer that specifies the size of the
        // largest record used in the metafile (in 16-bit elements).
        maxRecord = leis.readUInt();
        bytesLeft -= LittleEndianConsts.INT_SIZE;
        
        // NumberOfMembers (2 bytes):  A 16-bit unsigned integer that is not used.
        // It SHOULD be 0x0000.
        numberOfMembers = leis.readUShort();
        bytesLeft -= LittleEndianConsts.SHORT_SIZE;
        
        if (bytesLeft > 0) {
            long len = leis.skip(bytesLeft);
            assert(len == bytesLeft);
        }
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("type", () -> type);
        m.put("recordSize", () -> recordSize);
        m.put("version", () -> version);
        m.put("filesize", () -> filesize);
        m.put("numberOfObjects", () -> numberOfObjects);
        m.put("maxRecord", () -> maxRecord);
        m.put("numberOfMembers", () -> numberOfMembers);
        return Collections.unmodifiableMap(m);
    }
}
