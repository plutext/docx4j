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

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

public class HwmfPlaceableHeader {
    public static final int WMF_HEADER_MAGIC = 0x9AC6CDD7;
    
    final Rectangle2D bounds;
    final int unitsPerInch;
    
    protected HwmfPlaceableHeader(LittleEndianInputStream leis) throws IOException {
        /*
         * HWmf (2 bytes):  The resource handle to the metafile, when the metafile is in memory. When
         * the metafile is on disk, this field MUST contain 0x0000. This attribute of the metafile is
         * specified in the Type field of the META_HEADER record.
         */
        leis.readShort(); // ignore
        
        /*
         * BoundingBox (8 bytes):  The destination rectangle, measured in logical units, for displaying
         * the metafile. The size of a logical unit is specified by the Inch field.
         */
        int x1 = leis.readShort();
        int y1 = leis.readShort();
        int x2 = leis.readShort();
        int y2 = leis.readShort();
        bounds = new Rectangle2D.Double(Math.min(x1,x2), Math.min(y1,y2), Math.abs(x2-x1), Math.abs(y2-y1));
        
        /*
         * Inch (2 bytes):  The number of logical units per inch used to represent the image.
         * This value can be used to scale an image.
         * By convention, an image is considered to be recorded at 1440 logical units (twips) per inch.
         * Thus, a value of 720 specifies that the image SHOULD be rendered at twice its normal size,
         * and a value of 2880 specifies that the image SHOULD be rendered at half its normal size.
         */
        unitsPerInch = leis.readShort();
        
        /*
         * Reserved (4 bytes):  A field that is not used and MUST be set to 0x00000000.
         */
        leis.readInt();
        
        /*
         * Checksum (2 bytes):  A checksum for the previous 10 16-bit values in the header.
         * This value can be used to determine whether the metafile has become corrupted.
         */
        leis.readShort();

        // sometimes the placeable header is filled/aligned to dwords.
        // check for padding 0 bytes.
        leis.mark(LittleEndianConsts.INT_SIZE);
        if (leis.readShort() != 0) {
            leis.reset();
        }
    }
    
    public static HwmfPlaceableHeader readHeader(LittleEndianInputStream leis) throws IOException {
        leis.mark(LittleEndianConsts.INT_SIZE);
        int magic = leis.readInt();
        if (magic == WMF_HEADER_MAGIC) {
            return new HwmfPlaceableHeader(leis);
        } else {
            leis.reset();
            return null;
        }
    }

    public Rectangle2D getBounds() {
        return bounds;
    }

    public int getUnitsPerInch() {
        return unitsPerInch;
    }
}
