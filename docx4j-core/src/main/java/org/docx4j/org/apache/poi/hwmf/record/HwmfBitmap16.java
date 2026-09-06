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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

public class HwmfBitmap16 implements GenericRecord {
    private final boolean isPartial;
    private int type;
    private int width;
    private int height;
    private int widthBytes;
    private int planes;
    private int bitsPixel;
    private byte[] bitmap;

    public HwmfBitmap16() {
        this(false);
    }
    
    public HwmfBitmap16(boolean isPartial) {
        this.isPartial = isPartial;
    }
    
    public int init(LittleEndianInputStream leis) throws IOException {
        // A 16-bit signed integer that defines the bitmap type.
        type = leis.readShort();
        
        // A 16-bit signed integer that defines the width of the bitmap in pixels.
        width = leis.readShort();
        
        // A 16-bit signed integer that defines the height of the bitmap in scan lines.
        height = leis.readShort();
        
        // A 16-bit signed integer that defines the number of bytes per scan line.
        widthBytes = leis.readShort();
        
        // An 8-bit unsigned integer that defines the number of color planes in the 
        // bitmap. The value of this field MUST be 0x01.
        planes = leis.readUByte();
        
        // An 8-bit unsigned integer that defines the number of adjacent color bits on 
        // each plane.
        bitsPixel = leis.readUByte();

        int size = 2*LittleEndianConsts.BYTE_SIZE+4*LittleEndianConsts.SHORT_SIZE;
        if (isPartial) {
            // Bits (4 bytes): This field MUST be ignored.
            long skipSize = leis.skip(LittleEndianConsts.INT_SIZE);
            assert(skipSize == LittleEndianConsts.INT_SIZE);
            // Reserved (18 bytes): This field MUST be ignored.
            skipSize = leis.skip(18);
            assert(skipSize == 18);
            size += 18+LittleEndianConsts.INT_SIZE;
        }

        int length = (((width * bitsPixel + 15) >> 4) << 1) * height;
        bitmap = IOUtils.toByteArray(leis, length);
        
        // TODO: this is not implemented ... please provide a sample, if it
        // ever happens to you, to come here ...
        
        return size;
    }

    public BufferedImage getImage() {
        return new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("isPartial", () -> isPartial);
        m.put("type", () -> type);
        m.put("width", () -> width);
        m.put("height", () -> height);
        m.put("widthBytes", () -> widthBytes);
        m.put("planes", () -> planes);
        m.put("bitsPixel", () -> bitsPixel);
        m.put("bitmap", () -> bitmap);
        return Collections.unmodifiableMap(m);
    }
}
