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

package org.docx4j.org.apache.poi.hemf.record.emf;

import static org.docx4j.org.apache.poi.hemf.record.emf.HemfDraw.readDimensionInt;
import static org.docx4j.org.apache.poi.hemf.record.emf.HemfDraw.readRectL;
import static org.docx4j.org.apache.poi.hemf.record.emf.HemfRecordIterator.HEADER_SIZE;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.util.Dimension2DDouble;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;
import org.docx4j.org.apache.poi.util.RecordFormatException;

/**
 * Extracts the full header from EMF files.
 * @see org.docx4j.org.apache.poi.sl.image.ImageHeaderEMF
 */
@Internal
public class HemfHeader implements HemfRecord {

    private final Rectangle2D boundsRectangle = new Rectangle2D.Double();
    private final Rectangle2D frameRectangle = new Rectangle2D.Double();
    private long bytes;
    private long records;
    private int handles;
    private String description;
    private long nPalEntries;
    private boolean hasExtension1;
    private long cbPixelFormat;
    private long offPixelFormat;
    private long bOpenGL;
    private boolean hasExtension2;
    private final Dimension2D deviceDimension = new Dimension2DDouble();
    private final Dimension2D milliDimension = new Dimension2DDouble();
    private final Dimension2D microDimension = new Dimension2DDouble();


    public Rectangle2D getBoundsRectangle() {
        return boundsRectangle;
    }

    public Rectangle2D getFrameRectangle() {
        return frameRectangle;
    }

    public long getBytes() {
        return bytes;
    }

    public long getRecords() {
        return records;
    }

    public int getHandles() {
        return handles;
    }

    public String getDescription() { return description; }

    public long getNPalEntries() {
        return nPalEntries;
    }

    public boolean isHasExtension1() {
        return hasExtension1;
    }

    public long getCbPixelFormat() {
        return cbPixelFormat;
    }

    public long getOffPixelFormat() {
        return offPixelFormat;
    }

    public long getbOpenGL() {
        return bOpenGL;
    }

    public boolean isHasExtension2() {
        return hasExtension2;
    }

    public Dimension2D getDeviceDimension() {
        return deviceDimension;
    }

    public Dimension2D getMilliDimension() {
        return milliDimension;
    }

    public Dimension2D getMicroDimension() {
        return microDimension;
    }

    @Override
    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    @Override
    public HemfRecordType getEmfRecordType() {
        return HemfRecordType.header;
    }

    @SuppressWarnings("unused")
    @Override
    public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
        if (recordId != HemfRecordType.header.id) {
            throw new IOException("Not a valid EMF header. Record type:"+recordId);
        }

        int startIdx = leis.getReadIndex();

        //bounds
        long size = readRectL(leis, boundsRectangle);
        size += readRectL(leis, frameRectangle);

        int recordSignature = leis.readInt();
        if (recordSignature != 0x464D4520) {
            throw new IOException("bad record signature: " + recordSignature);
        }

        long version = leis.readInt();
        //According to the spec, MSOffice doesn't pay attention to this value.
        //It _should_ be 0x00010000
        bytes = leis.readUInt();
        records = leis.readUInt();
        handles = leis.readUShort();
        //reserved
        leis.skipFully(LittleEndianConsts.SHORT_SIZE);

        long nDescription = leis.readUInt();
        long offDescription = leis.readUInt();
        nPalEntries = leis.readUInt();

        size += 8*LittleEndianConsts.INT_SIZE;

        size += readDimensionInt(leis, deviceDimension);
        size += readDimensionInt(leis, milliDimension);

        if (nDescription > 0 && offDescription > 0) {
            long skip = offDescription - (size + HEADER_SIZE);
            long descriptionBytes = (nDescription - 1) * LittleEndianConsts.SHORT_SIZE;
            long descriptionEnd = offDescription + nDescription * LittleEndianConsts.SHORT_SIZE;
            if (skip < 0 || descriptionEnd > recordSize + HEADER_SIZE || skip + descriptionBytes > Integer.MAX_VALUE) {
                throw new RecordFormatException("Invalid EMF header description bounds");
            }
            int maxDescriptionLength = (int)Math.min(recordSize, Integer.MAX_VALUE);
            IOUtils.safelyAllocateCheck(descriptionBytes, maxDescriptionLength);
            leis.mark((int)(skip + descriptionBytes));
            leis.skipFully((int)skip);
            byte[] buf = IOUtils.safelyAllocate(descriptionBytes, maxDescriptionLength);
            leis.readFully(buf);
            description = new String(buf, StandardCharsets.UTF_16LE).replace((char)0, ' ').trim();
            leis.reset();
        }

        if (size+12 <= recordSize) {
            hasExtension1 = true;
            cbPixelFormat =  leis.readUInt();
            offPixelFormat = leis.readUInt();
            bOpenGL = leis.readUInt();
            size += 3*LittleEndianConsts.INT_SIZE;
        }

        if (size+8 <= recordSize) {
            hasExtension2 = true;
            size += readDimensionInt(leis, microDimension);
        }

        return size;
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        final Map<String,Supplier<?>> m = new LinkedHashMap<>();
        m.put("boundsRectangle", this::getBoundsRectangle);
        m.put("frameRectangle", this::getFrameRectangle);
        m.put("bytes", this::getBytes);
        m.put("records", this::getRecords);
        m.put("handles", this::getHandles);
        m.put("description", this::getDescription);
        m.put("nPalEntries", this::getNPalEntries);
        m.put("hasExtension1", this::isHasExtension1);
        m.put("cbPixelFormat", this::getCbPixelFormat);
        m.put("offPixelFormat", this::getOffPixelFormat);
        m.put("bOpenGL", this::getbOpenGL);
        m.put("hasExtension2", this::isHasExtension2);
        m.put("deviceDimension", this::getDeviceDimension);
        m.put("milliDimension", this::getMilliDimension);
        m.put("microDimension", this::getMicroDimension);
        return Collections.unmodifiableMap(m);
    }
}
