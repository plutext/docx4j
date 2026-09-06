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

import static org.docx4j.org.apache.poi.hemf.record.emf.HemfDraw.readPointL;
import static org.docx4j.org.apache.poi.hemf.record.emf.HemfFill.readBitmap;
import static org.docx4j.org.apache.poi.hemf.record.emf.HemfFill.readXForm;
import static org.docx4j.org.apache.poi.hemf.record.emf.HemfRecordIterator.HEADER_SIZE;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.hemf.draw.HemfDrawProperties;
import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfDrawProperties;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfGraphics;
import org.docx4j.org.apache.poi.hwmf.record.HwmfBinaryRasterOp;
import org.docx4j.org.apache.poi.hwmf.record.HwmfBitmapDib;
import org.docx4j.org.apache.poi.hwmf.record.HwmfBrushStyle;
import org.docx4j.org.apache.poi.hwmf.record.HwmfColorRef;
import org.docx4j.org.apache.poi.hwmf.record.HwmfFill;
import org.docx4j.org.apache.poi.hwmf.record.HwmfHatchStyle;
import org.docx4j.org.apache.poi.hwmf.record.HwmfMapMode;
import org.docx4j.org.apache.poi.hwmf.record.HwmfMisc;
import org.docx4j.org.apache.poi.hwmf.record.HwmfMisc.WmfSetBkMode;
import org.docx4j.org.apache.poi.hwmf.record.HwmfObjectTableEntry;
import org.docx4j.org.apache.poi.hwmf.record.HwmfPalette.PaletteEntry;
import org.docx4j.org.apache.poi.hwmf.record.HwmfPenStyle;
import org.docx4j.org.apache.poi.hwmf.record.HwmfPenStyle.HwmfLineDash;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

@SuppressWarnings("WeakerAccess")
public class HemfMisc {

    public enum HemfModifyWorldTransformMode {
        /**
         * Reset the current transform using the identity matrix.
         * In this mode, the specified transform data is ignored.
         */
        MWT_IDENTITY(1),
        /**
         * Multiply the current transform. In this mode, the specified transform data is the left multiplicand,
         * and the transform that is currently defined in the playback device context is the right multiplicand.
         */
        MWT_LEFTMULTIPLY(2),
        /**
         * Multiply the current transform. In this mode, the specified transform data is the right multiplicand,
         * and the transform that is currently defined in the playback device context is the left multiplicand.
         */
        MWT_RIGHTMULTIPLY(3),
        /**
         * Perform the function of an EMR_SETWORLDTRANSFORM record
         */
        MWT_SET(4)
        ;

        public final int id;

        HemfModifyWorldTransformMode(int id) {
            this.id = id;
        }

        public static HemfModifyWorldTransformMode valueOf(int id) {
            for (HemfModifyWorldTransformMode wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }


    public static class EmfEof implements HemfRecord {
        protected final List<PaletteEntry> palette = new ArrayList<>();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.eof;
        }

        @SuppressWarnings("unused")
        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            final int startIdx = leis.getReadIndex();

            // A 32-bit unsigned integer that specifies the number of palette entries.
            final int nPalEntries = (int) leis.readUInt();
            // A 32-bit unsigned integer that specifies the offset to the palette entries from the start of this record.
            final int offPalEntries = (int) leis.readUInt();

            int size = 2 * LittleEndianConsts.INT_SIZE;

            if (nPalEntries > 0 && offPalEntries > 0) {
                int undefinedSpace1 = offPalEntries - (size + HEADER_SIZE);
                assert (undefinedSpace1 >= 0);
                leis.skipFully(undefinedSpace1);
                size += undefinedSpace1;

                for (int i = 0; i < nPalEntries; i++) {
                    PaletteEntry pe = new PaletteEntry();
                    size += pe.init(leis);
                }

                int undefinedSpace2 = (int) (recordSize - size - LittleEndianConsts.INT_SIZE);
                assert (undefinedSpace2 >= 0);
                leis.skipFully(undefinedSpace2);
                size += undefinedSpace2;
            }

            // A 32-bit unsigned integer that MUST be the same as Size and MUST be the
            // last field of the record and hence the metafile.
            // LogPaletteEntry objects, if they exist, MUST precede this field.
            long sizeLast = leis.readUInt();
            size += LittleEndianConsts.INT_SIZE;
            // some files store the whole file size in sizeLast, other just the last record size
            // assert (sizeLast == size+HEADER_SIZE);
            assert (recordSize == size);

            return size;
        }

        public List<PaletteEntry> getPalette() {
            return palette;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("palette", this::getPalette);
        }
    }

    /**
     * The EMF_SAVEDC record saves the playback device context for later retrieval.
     */
    public static class EmfSaveDc extends HwmfMisc.WmfSaveDc implements HemfRecordWithoutProperties {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.saveDc;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return 0;
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMF_RESTOREDC record restores the playback device context from a previously saved device
     * context.
     */
    public static class EmfRestoreDc extends HwmfMisc.WmfRestoreDc implements HemfRecord {

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.restoreDc;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit signed integer that specifies the saved state to restore relative to
            // the current state. This value MUST be negative; –1 represents the state that was most
            // recently saved on the stack, –2 the one before that, etc.
            nSavedDC = leis.readInt();
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The META_SETBKCOLOR record sets the background color in the playback device context to a
     * specified color, or to the nearest physical color if the device cannot represent the specified color.
     */
    public static class EmfSetBkColor extends HwmfMisc.WmfSetBkColor implements HemfRecord {

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setBkColor;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return colorRef.init(leis);
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }


    /**
     * The EMR_SETBKMODE record specifies the background mix mode of the playback device context.
     * The background mix mode is used with text, hatched brushes, and pen styles that are not solid
     * lines.
     */
    public static class EmfSetBkMode extends WmfSetBkMode implements HemfRecord {
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setBkMode;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            /*
             * A 32-bit unsigned integer that specifies the background mode
             * and MUST be in the BackgroundMode (section 2.1.4) enumeration
             */
            bkMode = HwmfBkMode.valueOf((int) leis.readUInt());
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_SETMAPPERFLAGS record specifies parameters of the process of matching logical fonts to
     * physical fonts, which is performed by the font mapper.
     */
    public static class EmfSetMapperFlags extends HwmfMisc.WmfSetMapperFlags implements HemfRecord {
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setMapperFlags;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return super.init(leis, recordSize, (int) recordId);
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_SETMAPMODE record specifies the mapping mode of the playback device context. The
     * mapping mode specifies the unit of measure used to transform page space units into device space
     * units, and also specifies the orientation of the device's x-axis and y-axis.
     */
    public static class EmfSetMapMode extends HwmfMisc.WmfSetMapMode implements HemfRecord {
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setMapMode;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer whose definition MUST be in the MapMode enumeration
            mapMode = HwmfMapMode.valueOf((int) leis.readUInt());
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_SETROP2 record defines a binary raster operation mode.
     */
    public static class EmfSetRop2 extends HwmfMisc.WmfSetRop2 implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setRop2;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer that specifies the raster operation mode and
            // MUST be in the WMF Binary Raster Op enumeration
            drawMode = HwmfBinaryRasterOp.valueOf((int) leis.readUInt());
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }


    /**
     * The EMR_SETSTRETCHBLTMODE record specifies bitmap stretch mode.
     */
    public static class EmfSetStretchBltMode extends HwmfMisc.WmfSetStretchBltMode implements HemfRecord {

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setStretchBltMode;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer that specifies the stretch mode and MAY be
            // in the StretchMode enumeration.
            stretchBltMode = StretchBltMode.valueOf((int) leis.readUInt());
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_CREATEBRUSHINDIRECT record defines a logical brush for graphics operations.
     */
    public static class EmfCreateBrushIndirect extends HwmfMisc.WmfCreateBrushIndirect implements HemfRecord {
        /**
         * A 32-bit unsigned integer that specifies the index of the logical brush object in the
         * EMF Object Table. This index MUST be saved so that this object can be reused or modified.
         */
        private int brushIdx;

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.createBrushIndirect;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            brushIdx = (int) leis.readUInt();

            brushStyle = HwmfBrushStyle.valueOf((int) leis.readUInt());
            colorRef = new HwmfColorRef();
            int size = colorRef.init(leis);
            brushHatch = HwmfHatchStyle.valueOf((int) leis.readUInt());
            return size + 3L * LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.addObjectTableEntry(this, brushIdx);
        }

        public int getBrushIdx() {
            return brushIdx;
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "base", super::getGenericProperties,
                "brushIdx", this::getBrushIdx
            );
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_CREATEDIBPATTERNBRUSHPT record defines a pattern brush for graphics operations.
     * The pattern is specified by a DIB.
     */
    public static class EmfCreateDibPatternBrushPt extends HwmfMisc.WmfDibCreatePatternBrush implements HemfRecord {
        protected int brushIdx;

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.createDibPatternBrushPt;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            final int startIdx = leis.getReadIndex();

            style = HwmfBrushStyle.BS_DIBPATTERNPT;

            // A 32-bit unsigned integer that specifies the index of the pattern brush
            // object in the EMF Object Table
            brushIdx = (int)leis.readUInt();

            // A 32-bit unsigned integer that specifies how to interpret values in the color
            // table in the DIB header. This value MUST be in the DIBColors enumeration
            colorUsage = HwmfFill.ColorUsage.valueOf((int)leis.readUInt());

            // A 32-bit unsigned integer that specifies the offset from the start of this
            // record to the DIB header.
            final int offBmi = leis.readInt();

            // A 32-bit unsigned integer that specifies the size of the DIB header.
            final int cbBmi = leis.readInt();

            // A 32-bit unsigned integer that specifies the offset from the start of this record to the DIB bits.
            final int offBits = leis.readInt();

            // A 32-bit unsigned integer that specifies the size of the DIB bits.
            final int cbBits = leis.readInt();

            int size = 6*LittleEndianConsts.INT_SIZE;

            patternDib = new HwmfBitmapDib();
            size = Math.toIntExact(size + readBitmap(leis, patternDib, startIdx, offBmi, cbBmi, offBits, cbBits));
            return size;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.addObjectTableEntry(this, brushIdx);
        }

        public int getBrushIdx() {
            return brushIdx;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "base", super::getGenericProperties,
                "brushIdx", this::getBrushIdx
            );
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_DELETEOBJECT record deletes a graphics object, which is specified by its index
     * in the EMF Object Table
     */
    public static class EmfDeleteObject extends HwmfMisc.WmfDeleteObject implements HemfRecord {

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.deleteobject;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            objectIndex = (int) leis.readUInt();
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_CREATEPEN record defines a logical pen for graphics operations.
     */
    public static class EmfCreatePen extends HwmfMisc.WmfCreatePenIndirect implements HemfRecord {
        /**
         * A 32-bit unsigned integer that specifies the index of the logical palette object
         * in the EMF Object Table. This index MUST be saved so that this object can be
         * reused or modified.
         */
        protected int penIndex;

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.createPen;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            penIndex = (int) leis.readUInt();

            // A 32-bit unsigned integer that specifies the PenStyle.
            // The value MUST be defined from the PenStyle enumeration table
            penStyle = HwmfPenStyle.valueOf((int) leis.readUInt());

            int widthX = leis.readInt();
            int widthY = leis.readInt();
            dimension.setSize(widthX, widthY);

            int size = colorRef.init(leis);

            return size + 4L * LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.addObjectTableEntry(this, penIndex);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public int getPenIndex() {
            return penIndex;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "base", super::getGenericProperties,
                "penIndex", this::getPenIndex
            );
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    public static class EmfExtCreatePen extends EmfCreatePen {
        protected HwmfBrushStyle brushStyle;
        protected HwmfHatchStyle hatchStyle;

        protected final HwmfBitmapDib bitmap = new HwmfBitmapDib();


        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.extCreatePen;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            final int startIdx = leis.getReadIndex();

            // An unsigned integer that specifies the index of the extended logical pen object in
            // the EMF object table. This index MUST be saved so that this object can be
            // reused or modified.
            penIndex = (int) leis.readUInt();

            // A 32-bit unsigned integer that specifies the offset from the start of this
            // record to the DIB header, if the record contains a DIB.
            int offBmi = (int) leis.readUInt();

            // A 32-bit unsigned integer that specifies the size of the DIB header, if the
            // record contains a DIB.
            int cbBmi = (int) leis.readUInt();

            // A 32-bit unsigned integer that specifies the offset from the start of this
            // record to the DIB bits, if the record contains a DIB.
            int offBits = (int) leis.readUInt();

            // A 32-bit unsigned integer that specifies the size of the DIB bits, if the record
            // contains a DIB.
            int cbBits = (int) leis.readUInt();

            // A 32-bit unsigned integer that specifies the PenStyle.
            // The value MUST be defined from the PenStyle enumeration table
            final HemfPenStyle emfPS = HemfPenStyle.valueOf((int) leis.readUInt());
            penStyle = emfPS;

            // A 32-bit unsigned integer that specifies the width of the line drawn by the pen.
            // If the pen type in the PenStyle field is PS_GEOMETRIC, this value is the width in logical
            // units; otherwise, the width is specified in device units. If the pen type in the PenStyle field is
            // PS_COSMETIC, this value MUST be 0x00000001.
            long width = leis.readUInt();
            dimension.setSize(width, 0);
            int size = 7 * LittleEndianConsts.INT_SIZE;

            // A 32-bit unsigned integer that specifies a brush style for the pen from the WMF BrushStyle enumeration
            //
            // If the pen type in the PenStyle field is PS_GEOMETRIC, this value MUST be either BS_SOLID or BS_HATCHED.
            // The value of this field can be BS_NULL, but only if the line style specified in PenStyle is PS_NULL.
            // The BS_NULL style SHOULD be used to specify a brush that has no effect
            brushStyle = HwmfBrushStyle.valueOf((int) leis.readUInt());

            size += LittleEndianConsts.INT_SIZE;

            size += colorRef.init(leis);

            hatchStyle = HwmfHatchStyle.valueOf(leis.readInt());
            size += LittleEndianConsts.INT_SIZE;

            // The number of elements in the array specified in the StyleEntry
            // field. This value SHOULD be zero if PenStyle does not specify PS_USERSTYLE.
            final int numStyleEntries = (int) leis.readUInt();
            size += LittleEndianConsts.INT_SIZE;

            assert (numStyleEntries == 0 || penStyle.getLineDash() == HwmfLineDash.USERSTYLE);

            // An optional array of 32-bit unsigned integers that defines the lengths of
            // dashes and gaps in the line drawn by this pen, when the value of PenStyle is
            // PS_USERSTYLE line style for the pen. The array contains a number of entries specified by
            // NumStyleEntries, but it is used as if it repeated indefinitely.
            // The first entry in the array specifies the length of the first dash. The second entry specifies
            // the length of the first gap. Thereafter, lengths of dashes and gaps alternate.
            // If the pen type in the PenStyle field is PS_GEOMETRIC, the lengths are specified in logical
            // units; otherwise, the lengths are specified in device units.

            float[] dashPattern = new float[numStyleEntries];

            for (int i = 0; i < numStyleEntries; i++) {
                dashPattern[i] = (int) leis.readUInt();
            }

            if (penStyle.getLineDash() == HwmfLineDash.USERSTYLE) {
                emfPS.setLineDashes(dashPattern);
            }

            size = Math.addExact(size, numStyleEntries * LittleEndianConsts.INT_SIZE);

            size = Math.toIntExact(size + readBitmap(leis, bitmap, startIdx, offBmi, cbBmi, offBits, cbBits));

            return size;
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public HwmfBrushStyle getBrushStyle() {
            return brushStyle;
        }

        public HwmfHatchStyle getHatchStyle() {
            return hatchStyle;
        }

        public HwmfBitmapDib getBitmap() {
            return bitmap;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "base", super::getGenericProperties,
                "brushStyle", this::getBrushStyle,
                "hatchStyle", this::getHatchStyle,
                "bitmap", this::getBitmap
            );
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_SETMITERLIMIT record specifies the limit for the length of miter joins for the playback
     * device context.
     */
    public static class EmfSetMiterLimit implements HemfRecord {
        protected int miterLimit;

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setMiterLimit;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            miterLimit = (int) leis.readUInt();
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.getProperties().setPenMiterLimit(miterLimit);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public int getMiterLimit() {
            return miterLimit;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("miterLimit", this::getMiterLimit);
        }
    }


    public static class EmfSetBrushOrgEx implements HemfRecord {
        protected final Point2D origin = new Point2D.Double();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setBrushOrgEx;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readPointL(leis, origin);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public Point2D getOrigin() {
            return origin;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("origin", this::getOrigin);
        }
    }

    public static class EmfSetWorldTransform implements HemfRecord {
        protected final AffineTransform xForm = new AffineTransform();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setWorldTransform;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readXForm(leis, xForm);
        }

        @Override
        public void draw(HemfGraphics ctx) {
            HemfDrawProperties prop = ctx.getProperties();
            prop.clearTransform();
            prop.addLeftTransform(xForm);
            ctx.updateWindowMapMode();
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public AffineTransform getXForm() {
            return xForm;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("xForm", this::getXForm);
        }
    }

    public static class EmfModifyWorldTransform implements HemfRecord {
        protected final AffineTransform xForm = new AffineTransform();
        protected HemfModifyWorldTransformMode modifyWorldTransformMode;
        protected HemfHeader header;

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.modifyWorldTransform;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // An XForm object that defines a two-dimensional linear transform in logical units.
            // This transform is used according to the ModifyWorldTransformMode to define a new value for
            // the world-space to page-space transform in the playback device context.
            long size = readXForm(leis, xForm);

            // A 32-bit unsigned integer that specifies how the transform specified in Xform is used.
            // This value MUST be in the ModifyWorldTransformMode enumeration
            modifyWorldTransformMode = HemfModifyWorldTransformMode.valueOf((int)leis.readUInt());

            return size + LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void setHeader(HemfHeader header) {
            this.header = header;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            if (modifyWorldTransformMode == null) {
                return;
            }

            final HemfDrawProperties prop = ctx.getProperties();

            switch (modifyWorldTransformMode) {
                case MWT_LEFTMULTIPLY:
                    prop.addLeftTransform(xForm);
                    break;
                case MWT_RIGHTMULTIPLY:
                    prop.addRightTransform(xForm);
                    break;
                case MWT_IDENTITY:
                    prop.clearTransform();
                    break;
                default:
                case MWT_SET:
                    prop.clearTransform();
                    prop.addLeftTransform(xForm);
                    break;
            }
            ctx.updateWindowMapMode();
            ctx.getProperties().setLocation(0,0);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public AffineTransform getXForm() {
            return xForm;
        }

        public HemfModifyWorldTransformMode getModifyWorldTransformMode() {
            return modifyWorldTransformMode;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "xForm", this::getXForm,
                "modifyWorldTransformMode", this::getModifyWorldTransformMode
            );
        }
    }

    public static class EmfCreateMonoBrush implements HemfRecord, HwmfObjectTableEntry {
        /**
         * A 32-bit unsigned integer that specifies the index of the logical palette object
         * in the EMF Object Table. This index MUST be saved so that this object can be
         * reused or modified.
         */
        protected int penIndex;

        protected HwmfFill.ColorUsage colorUsage;
        protected final HwmfBitmapDib bitmap = new HwmfBitmapDib();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.createMonoBrush;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            final int startIdx = leis.getReadIndex();

            penIndex = (int) leis.readUInt();

            // A 32-bit unsigned integer that specifies how to interpret values in the color
            // table in the DIB header. This value MUST be in the DIBColors enumeration
            colorUsage = HwmfFill.ColorUsage.valueOf((int) leis.readUInt());

            // A 32-bit unsigned integer that specifies the offset from the start of this
            // record to the DIB header, if the record contains a DIB.
            int offBmi = (int) leis.readUInt();

            // A 32-bit unsigned integer that specifies the size of the DIB header, if the
            // record contains a DIB.
            int cbBmi = (int) leis.readUInt();

            // A 32-bit unsigned integer that specifies the offset from the start of this
            // record to the DIB bits, if the record contains a DIB.
            int offBits = (int) leis.readUInt();

            // A 32-bit unsigned integer that specifies the size of the DIB bits, if the record
            // contains a DIB.
            int cbBits = Math.toIntExact(leis.readUInt());

            int size = 6 * LittleEndianConsts.INT_SIZE;

            size = Math.toIntExact(size + readBitmap(leis, bitmap, startIdx, offBmi, cbBmi, offBits, cbBits));

            return size;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.addObjectTableEntry(this, penIndex);
        }

        @Override
        public void applyObject(HwmfGraphics ctx) {
            if (!bitmap.isValid()) {
                return;
            }
            HwmfDrawProperties props = ctx.getProperties();
            props.setBrushStyle(HwmfBrushStyle.BS_PATTERN);
            props.setBrushBitmap(bitmap.getImage());
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public int getPenIndex() {
            return penIndex;
        }

        public HwmfFill.ColorUsage getColorUsage() {
            return colorUsage;
        }

        public HwmfBitmapDib getBitmap() {
            return bitmap;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "penIndex", this::getPenIndex,
                "colorUsage", this::getColorUsage,
                "bitmap", this::getBitmap
            );
        }
    }

}
