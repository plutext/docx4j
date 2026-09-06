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
import org.docx4j.org.apache.poi.hwmf.draw.HwmfGraphics;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

/**
 * The MetafileEscapes specifies printer driver functionality that
 * might not be directly accessible through WMF records
 */
@SuppressWarnings("WeakerAccess")
public class HwmfEscape implements HwmfRecord {
    private static final int MAX_OBJECT_SIZE = 0xFFFF;

    public enum EscapeFunction {
        /** Notifies the printer driver that the application has finished writing to a page. */
        NEWFRAME(0x0001, WmfEscapeUnknownData::new),
        /** Stops processing the current document. */
        ABORTDOC(0x0002, WmfEscapeUnknownData::new),
        /** Notifies the printer driver that the application has finished writing to a band. */
        NEXTBAND(0x0003, WmfEscapeUnknownData::new),
        /** Sets color table values. */
        SETCOLORTABLE(0x0004, WmfEscapeUnknownData::new),
        /** Gets color table values. */
        GETCOLORTABLE(0x0005, WmfEscapeUnknownData::new),
        /** Causes all pending output to be flushed to the output device. */
        FLUSHOUT(0x0006, WmfEscapeUnknownData::new),
        /** Indicates that the printer driver SHOULD print text only, and no graphics. */
        DRAFTMODE(0x0007, WmfEscapeUnknownData::new),
        /** Queries a printer driver to determine whether a specific escape function is supported on the output device it drives. */
        QUERYESCSUPPORT(0x0008, WmfEscapeUnknownData::new),
        /** Sets the application-defined function that allows a print job to be canceled during printing. */
        SETABORTPROC(0x0009, WmfEscapeUnknownData::new),
        /** Notifies the printer driver that a new print job is starting. */
        STARTDOC(0x000A, WmfEscapeUnknownData::new),
        /** Notifies the printer driver that the current print job is ending. */
        ENDDOC(0x000B, WmfEscapeUnknownData::new),
        /** Retrieves the physical page size currently selected on an output device. */
        GETPHYSPAGESIZE(0x000C, WmfEscapeUnknownData::new),
        /** Retrieves the offset from the upper-left corner of the physical page where the actual printing or drawing begins. */
        GETPRINTINGOFFSET(0x000D, WmfEscapeUnknownData::new),
        /** Retrieves the scaling factors for the x-axis and the y-axis of a printer. */
        GETSCALINGFACTOR(0x000E, WmfEscapeUnknownData::new),
        /** Used to embed an enhanced metafile format (EMF) metafile within a WMF metafile. */
        META_ESCAPE_ENHANCED_METAFILE(0x000F, WmfEscapeEMF::new),
        /** Sets the width of a pen in pixels. */
        SETPENWIDTH(0x0010, WmfEscapeUnknownData::new),
        /** Sets the number of copies. */
        SETCOPYCOUNT(0x0011, WmfEscapeUnknownData::new),
        /** Sets the source, such as a particular paper tray or bin on a printer, for output forms. */
        SETPAPERSOURCE(0x0012, WmfEscapeUnknownData::new),
        /** This record passes through arbitrary data. */
        PASSTHROUGH(0x0013, WmfEscapeUnknownData::new),
        /** Gets information concerning graphics technology that is supported on a device. */
        GETTECHNOLOGY(0x0014, WmfEscapeUnknownData::new),
        /** Specifies the line-drawing mode to use in output to a device. */
        SETLINECAP(0x0015, WmfEscapeUnknownData::new),
        /** Specifies the line-joining mode to use in output to a device. */
        SETLINEJOIN(0x0016, WmfEscapeUnknownData::new),
        /** Sets the limit for the length of miter joins to use in output to a device. */
        SETMITERLIMIT(0x0017, WmfEscapeUnknownData::new),
        /** Retrieves or specifies settings concerning banding on a device, such as the number of bands. */
        BANDINFO(0x0018, WmfEscapeUnknownData::new),
        /** Draws a rectangle with a defined pattern. */
        DRAWPATTERNRECT(0x0019, WmfEscapeUnknownData::new),
        /** Retrieves the physical pen size currently defined on a device. */
        GETVECTORPENSIZE(0x001A, WmfEscapeUnknownData::new),
        /** Retrieves the physical brush size currently defined on a device. */
        GETVECTORBRUSHSIZE(0x001B, WmfEscapeUnknownData::new),
        /** Enables or disables double-sided (duplex) printing on a device. */
        ENABLEDUPLEX(0x001C, WmfEscapeUnknownData::new),
        /** Retrieves or specifies the source of output forms on a device. */
        GETSETPAPERBINS(0x001D, WmfEscapeUnknownData::new),
        /** Retrieves or specifies the paper orientation on a device. */
        GETSETPRINTORIENT(0x001E, WmfEscapeUnknownData::new),
        /** Retrieves information concerning the sources of different forms on an output device. */
        ENUMPAPERBINS(0x001F, WmfEscapeUnknownData::new),
        /** Specifies the scaling of device-independent bitmaps (DIBs). */
        SETDIBSCALING(0x0020, WmfEscapeUnknownData::new),
        /** Indicates the start and end of an encapsulated PostScript (EPS) section. */
        EPSPRINTING(0x0021, WmfEscapeUnknownData::new),
        /** Queries a printer driver for paper dimensions and other forms data. */
        ENUMPAPERMETRICS(0x0022, WmfEscapeUnknownData::new),
        /** Retrieves or specifies paper dimensions and other forms data on an output device. */
        GETSETPAPERMETRICS(0x0023, WmfEscapeUnknownData::new),
        /** Sends arbitrary PostScript data to an output device. */
        POSTSCRIPT_DATA(0x0025, WmfEscapeUnknownData::new),
        /** Notifies an output device to ignore PostScript data. */
        POSTSCRIPT_IGNORE(0x0026, WmfEscapeUnknownData::new),
        /** Gets the device units currently configured on an output device. */
        GETDEVICEUNITS(0x002A, WmfEscapeUnknownData::new),
        /** Gets extended text metrics currently configured on an output device. */
        GETEXTENDEDTEXTMETRICS(0x0100, WmfEscapeUnknownData::new),
        /** Gets the font kern table currently defined on an output device. */
        GETPAIRKERNTABLE(0x0102, WmfEscapeUnknownData::new),
        /** Draws text using the currently selected font, background color, and text color. */
        EXTTEXTOUT(0x0200, WmfEscapeUnknownData::new),
        /** Gets the font face name currently configured on a device. */
        GETFACENAME(0x0201, WmfEscapeUnknownData::new),
        /** Sets the font face name on a device. */
        DOWNLOADFACE(0x0202, WmfEscapeUnknownData::new),
        /** Queries a printer driver about the support for metafiles on an output device. */
        METAFILE_DRIVER(0x0801, WmfEscapeUnknownData::new),
        /** Queries the printer driver about its support for DIBs on an output device. */
        QUERYDIBSUPPORT(0x0C01, WmfEscapeUnknownData::new),
        /** Opens a path. */
        BEGIN_PATH(0x1000, WmfEscapeUnknownData::new),
        /** Defines a clip region that is bounded by a path. The input MUST be a 16-bit quantity that defines the action to take. */
        CLIP_TO_PATH(0x1001, WmfEscapeUnknownData::new),
        /** Ends a path. */
        END_PATH(0x1002, WmfEscapeUnknownData::new),
        /** The same as STARTDOC specified with a NULL document and output filename, data in raw mode, and a type of zero. */
        OPEN_CHANNEL(0x100E, WmfEscapeUnknownData::new),
        /** Instructs the printer driver to download sets of PostScript procedures. */
        DOWNLOADHEADER(0x100F, WmfEscapeUnknownData::new),
        /** The same as ENDDOC. See OPEN_CHANNEL. */
        CLOSE_CHANNEL(0x1010, WmfEscapeUnknownData::new),
        /** Sends arbitrary data directly to a printer driver, which is expected to process this data only when in PostScript mode. */
        POSTSCRIPT_PASSTHROUGH(0x1013, WmfEscapeUnknownData::new),
        /** Sends arbitrary data directly to the printer driver. */
        ENCAPSULATED_POSTSCRIPT(0x1014, WmfEscapeUnknownData::new),
        /** Sets the printer driver to either PostScript or GDI mode. */
        POSTSCRIPT_IDENTIFY(0x1015, WmfEscapeUnknownData::new),
        /** Inserts a block of raw data into a PostScript stream. The input MUST be
        a 32-bit quantity specifying the number of bytes to inject, a 16-bit quantity specifying the
        injection point, and a 16-bit quantity specifying the page number, followed by the bytes to
        inject. */
        POSTSCRIPT_INJECTION(0x1016, WmfEscapeUnknownData::new),
        /** Checks whether the printer supports a JPEG image. */
        CHECKJPEGFORMAT(0x1017, WmfEscapeUnknownData::new),
        /** Checks whether the printer supports a PNG image */
        CHECKPNGFORMAT(0x1018, WmfEscapeUnknownData::new),
        /** Gets information on a specified feature setting for a PostScript printer driver. */
        GET_PS_FEATURESETTING(0x1019, WmfEscapeUnknownData::new),
        /** Enables applications to write documents to a file or to a printer in XML Paper Specification (XPS) format. */
        MXDC_ESCAPE(0x101A, WmfEscapeUnknownData::new),
        /** Enables applications to include private procedures and other arbitrary data in documents. */
        SPCLPASSTHROUGH2(0x11D8, WmfEscapeUnknownData::new);

        private final int flag;
        private final Supplier<? extends HwmfEscape.HwmfEscapeData> constructor;


        EscapeFunction(int flag, Supplier<? extends HwmfEscape.HwmfEscapeData> constructor) {
            this.flag = flag;
            this.constructor = constructor;
        }

        public int getFlag() {
            return flag;
        }

        public Supplier<? extends HwmfEscapeData> getConstructor() {
            return constructor;
        }

        static EscapeFunction valueOf(int flag) {
            for (EscapeFunction hs : values()) {
                if (hs.flag == flag) return hs;
            }
            return null;
        }
    }

    public interface HwmfEscapeData {
        int init(LittleEndianInputStream leis, long recordSize, EscapeFunction escapeFunction) throws IOException;
    }


    /**
     * A 16-bit unsigned integer that defines the escape function. The
     * value MUST be from the MetafileEscapes enumeration.
     */
    private EscapeFunction escapeFunction;
    private HwmfEscapeData escapeData;

    @Override
    public HwmfRecordType getWmfRecordType() {
        return HwmfRecordType.escape;
    }

    @Override
    public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
        escapeFunction = EscapeFunction.valueOf(leis.readUShort());
        // A 16-bit unsigned integer that specifies the size, in bytes, of the EscapeData field.
        int byteCount = leis.readUShort();
        int size = 2*LittleEndianConsts.SHORT_SIZE;

        escapeData = (escapeFunction == null) ? new WmfEscapeUnknownData() : escapeFunction.constructor.get();
        size += escapeData.init(leis, byteCount, escapeFunction);

        return size;
    }

    public EscapeFunction getEscapeFunction() {
        return escapeFunction;
    }

    @SuppressWarnings("unchecked")
    public <T extends HwmfEscapeData> T getEscapeData() {
        return (T)escapeData;
    }

    @Override
    public void draw(HwmfGraphics ctx) {

    }

    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "escapeFunction", this::getEscapeFunction,
            "escapeData", this::getEscapeData
        );
    }

    public static class WmfEscapeUnknownData implements HwmfEscapeData, GenericRecord {
        EscapeFunction escapeFunction;
        private byte[] escapeDataBytes;

        public byte[] getEscapeDataBytes() {
            return escapeDataBytes;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, EscapeFunction escapeFunction) throws IOException {
            this.escapeFunction = escapeFunction;
            escapeDataBytes = IOUtils.toByteArray(leis,(int)recordSize,MAX_OBJECT_SIZE);
            return (int)recordSize;
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("escapeDataBytes", this::getEscapeDataBytes);
        }
    }

    public static class WmfEscapeEMF implements HwmfEscapeData, GenericRecord {
        // The magic for EMF parts, i.e. the byte sequence for "WMFC"
        private static final int EMF_COMMENT_IDENTIFIER = 0x43464D57;

        int commentIdentifier;
        int commentType;
        int version;
        int checksum;
        int flags;
        int commentRecordCount;
        int currentRecordSize;
        int remainingBytes;
        int emfRecordSize;
        byte[] emfData;


        @Override
        public int init(LittleEndianInputStream leis, long recordSize, EscapeFunction escapeFunction) throws IOException {
            if (recordSize < LittleEndianConsts.INT_SIZE) {
                return 0;
            }

            // A 32-bit unsigned integer that defines this record as a WMF Comment record.
            commentIdentifier = leis.readInt();

            if (commentIdentifier != EMF_COMMENT_IDENTIFIER) {
                // there are some WMF implementation using this record as a MFCOMMENT or similar
                // if the commentIdentifier doesn't match, then return immediately
                emfData = IOUtils.toByteArray(leis, (int)(recordSize-LittleEndianConsts.INT_SIZE), MAX_OBJECT_SIZE);
                remainingBytes = emfData.length;
                return (int)recordSize;
            }

            // A 32-bit unsigned integer that identifies the type of comment in this record.
            // This value MUST be 0x00000001.
            commentType = leis.readInt();
            assert(commentType == 0x00000001);

            // A 32-bit unsigned integer that specifies EMF metafile interoperability. This SHOULD be 0x00010000.
            version = leis.readInt();

            // A 16-bit unsigned integer used to validate the correctness of the embedded EMF stream.
            // This value MUST be the one's-complement of the result of applying an XOR operation to all WORDs in the EMF stream.
            checksum = leis.readUShort();

            // This 32-bit unsigned integer is unused and MUST be set to zero.
            flags = leis.readInt();
            assert(flags == 0);

            // A 32-bit unsigned integer that specifies the total number of consecutive META_ESCAPE_ENHANCED_METAFILE
            // records that contain the embedded EMF metafile.
            commentRecordCount = leis.readInt();

            // A 32-bit unsigned integer that specifies the size, in bytes, of the EnhancedMetafileData field.
            // This value MUST be less than or equal to 8,192.
            currentRecordSize = leis.readInt();
            assert(0 <= currentRecordSize && currentRecordSize <= 0x2000);

            // A 32-bit unsigned integer that specifies the number of bytes in the EMF stream that remain to be
            // processed after this record. Those additional EMF bytes MUST follow in the EnhancedMetafileData
            // fields of subsequent META_ESCAPE_ENHANDED_METAFILE escape records.
            remainingBytes = leis.readInt();

            // A 32-bit unsigned integer that specifies the total size of the EMF stream embedded in this
            // sequence of META_ESCAPE_ENHANCED_METAFILE records.
            emfRecordSize = leis.readInt();


            // A segment of an EMF file. The bytes in consecutive META_ESCAPE_ENHANCED_METAFILE records
            // MUST be concatenated to represent the entire embedded EMF file.
            emfData = IOUtils.toByteArray(leis, currentRecordSize, MAX_OBJECT_SIZE);

            return LittleEndianConsts.INT_SIZE*8+ LittleEndianConsts.SHORT_SIZE+emfData.length;
        }

        public boolean isValid() {
            return commentIdentifier == EMF_COMMENT_IDENTIFIER;
        }

        public int getCommentRecordCount() {
            return commentRecordCount;
        }

        public int getCurrentRecordSize() {
            return currentRecordSize;
        }

        public int getRemainingBytes() {
            return remainingBytes;
        }

        public int getEmfRecordSize() {
            return emfRecordSize;
        }

        public byte[] getEmfData() {
            return emfData;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            final Map<String,Supplier<?>> m = new LinkedHashMap<>();
            m.put("commentIdentifier", () -> commentIdentifier);
            m.put("commentType", () -> commentType);
            m.put("version", () -> version);
            m.put("checksum", () -> checksum);
            m.put("flags", () -> flags);
            m.put("commentRecordCount", this::getCommentRecordCount);
            m.put("currentRecordSize", this::getCurrentRecordSize);
            m.put("remainingBytes", this::getRemainingBytes);
            m.put("emfRecordSize", this::getEmfRecordSize);
            m.put("emfData", this::getEmfData);
            return Collections.unmodifiableMap(m);
        }
    }
}
