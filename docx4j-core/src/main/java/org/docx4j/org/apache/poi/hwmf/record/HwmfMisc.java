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

import java.awt.Color;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.hwmf.draw.HwmfDrawProperties;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfGraphics;
import org.docx4j.org.apache.poi.hwmf.record.HwmfFill.ColorUsage;
import org.docx4j.org.apache.poi.hwmf.record.HwmfFill.HwmfImageRecord;
import org.docx4j.org.apache.poi.hwmf.record.HwmfMisc.WmfSetBkMode.HwmfBkMode;
import org.docx4j.org.apache.poi.util.Dimension2DDouble;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

@SuppressWarnings("WeakerAccess")
public class HwmfMisc {

    /**
     * The META_SAVEDC record saves the playback device context for later retrieval.
     */
    public static class WmfSaveDc implements HwmfRecord {
        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.saveDc;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return 0;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.saveProperties();
        }

        @Override
        public String toString() {
            return "{}";
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return null;
        }
    }

    /**
     * The META_SETRELABS record is reserved and not supported.
     */
    public static class WmfSetRelabs implements HwmfRecord {
        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setRelabs;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return 0;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return null;
        }
    }

    /**
     * The META_RESTOREDC record restores the playback device context from a previously saved device
     * context.
     */
    public static class WmfRestoreDc implements HwmfRecord {

        /**
         * nSavedDC (2 bytes):  A 16-bit signed integer that defines the saved state to be restored. If this
         * member is positive, nSavedDC represents a specific instance of the state to be restored. If
         * this member is negative, nSavedDC represents an instance relative to the current state.
         */
        protected int nSavedDC;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.restoreDc;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            nSavedDC = leis.readShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.restoreProperties(nSavedDC);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public int getNSavedDC() {
            return nSavedDC;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("nSavedDC", this::getNSavedDC);
        }
    }

    /**
     * The META_SETBKCOLOR record sets the background color in the playback device context to a
     * specified color, or to the nearest physical color if the device cannot represent the specified color.
     */
    public static class WmfSetBkColor implements HwmfRecord {

        protected final HwmfColorRef colorRef = new HwmfColorRef();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setBkColor;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return colorRef.init(leis);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setBackgroundColor(colorRef);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public HwmfColorRef getColorRef() {
            return colorRef;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("colorRef", this::getColorRef);
        }
    }

    /**
     * The META_SETBKMODE record defines the background raster operation mix mode in the playback
     * device context. The background mix mode is the mode for combining pens, text, hatched brushes,
     * and interiors of filled objects with background colors on the output surface.
     */
    public static class WmfSetBkMode implements HwmfRecord {

        /**
         * A 16-bit unsigned integer that defines background mix mode.
         */
        public enum HwmfBkMode {
            TRANSPARENT(0x0001), OPAQUE(0x0002);

            final int flag;
            HwmfBkMode(int flag) {
                this.flag = flag;
            }

            public static HwmfBkMode valueOf(int flag) {
                for (HwmfBkMode bs : values()) {
                    if (bs.flag == flag) return bs;
                }
                return null;
            }
        }

        protected HwmfBkMode bkMode;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setBkMode;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            bkMode = HwmfBkMode.valueOf(leis.readUShort());
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setBkMode(bkMode);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public HwmfBkMode getBkMode() {
            return bkMode;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("bkMode", this::getBkMode);
        }
    }

    /**
     * The META_SETLAYOUT record defines the layout orientation in the playback device context.
     * The layout orientation determines the direction in which text and graphics are drawn
     */
    public static class WmfSetLayout implements HwmfRecord {

        /**
         * A 16-bit unsigned integer that defines the layout of text and graphics.
         * LAYOUT_LTR = 0x0000
         * LAYOUT_RTL = 0x0001
         * LAYOUT_BITMAPORIENTATIONPRESERVED = 0x0008
         */
        private int layout;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setLayout;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            layout = leis.readUShort();
            // A 16-bit field that MUST be ignored.
            /*int reserved =*/ leis.readShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("layout", () -> layout);
        }
    }

    /**
     * The META_SETMAPMODE record defines the mapping mode in the playback device context.
     * The mapping mode defines the unit of measure used to transform page-space units into
     * device-space units, and also defines the orientation of the device's x and y axes.
     */
    public static class WmfSetMapMode implements HwmfRecord {

        protected HwmfMapMode mapMode;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setMapMode;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            mapMode = HwmfMapMode.valueOf(leis.readUShort());
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setMapMode(mapMode);
            ctx.updateWindowMapMode();
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public HwmfMapMode getMapMode() {
            return mapMode;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("mapMode", this::getMapMode);
        }
    }

    /**
     * The META_SETMAPPERFLAGS record defines the algorithm that the font mapper uses when it maps
     * logical fonts to physical fonts.
     */
    public static class WmfSetMapperFlags implements HwmfRecord {

        /**
         * A 32-bit unsigned integer that defines whether the font mapper should attempt to
         * match a font's aspect ratio to the current device's aspect ratio. If bit 0 is
         * set, the font mapper SHOULD select only fonts that match the aspect ratio of the
         * output device, as it is currently defined in the playback device context.
         */
        private long mapperValues;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setMapperFlags;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            mapperValues = leis.readUInt();
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("mapperValues", () -> mapperValues);
        }
    }

    /**
     * The META_SETROP2 record defines the foreground raster operation mix mode in the playback device
     * context. The foreground mix mode is the mode for combining pens and interiors of filled objects with
     * foreground colors on the output surface.
     */
    public static class WmfSetRop2 implements HwmfRecord {

        /** An unsigned integer that defines the foreground binary raster operation mixing mode */
        protected HwmfBinaryRasterOp drawMode;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setRop2;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            drawMode = HwmfBinaryRasterOp.valueOf(leis.readUShort());
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            HwmfDrawProperties prop = ctx.getProperties();
            prop.setRasterOp2(drawMode);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public HwmfBinaryRasterOp getDrawMode() {
            return drawMode;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("drawMode", this::getDrawMode);
        }
    }

    /**
     * The META_SETSTRETCHBLTMODE record defines the bitmap stretching mode in the playback device
     * context.
     */
    public static class WmfSetStretchBltMode implements HwmfRecord {

        public enum StretchBltMode {
            /**
             * Performs a Boolean AND operation by using the color values for the eliminated and existing pixels.
             * If the bitmap is a monochrome bitmap, this mode preserves black pixels at the expense of white pixels.
             *
             * EMF name: STRETCH_ANDSCANS
             */
            BLACKONWHITE(0x0001),
            /**
             * Performs a Boolean OR operation by using the color values for the eliminated and existing pixels.
             * If the bitmap is a monochrome bitmap, this mode preserves white pixels at the expense of black pixels.
             *
             * EMF name: STRETCH_ORSCANS
             */
            WHITEONBLACK(0x0002),
            /**
             * Deletes the pixels. This mode deletes all eliminated lines of pixels without trying
             * to preserve their information.
             *
             * EMF name: STRETCH_DELETESCANS
             */
            COLORONCOLOR(0x0003),
            /**
             * Maps pixels from the source rectangle into blocks of pixels in the destination rectangle.
             * The average color over the destination block of pixels approximates the color of the source
             * pixels.
             *
             * After setting the HALFTONE stretching mode, the brush origin MUST be set to avoid misalignment
             * artifacts - in EMF this is done via EmfSetBrushOrgEx
             *
             * EMF name: STRETCH_HALFTONE
             */
            HALFTONE(0x0004);

            public final int flag;
            StretchBltMode(int flag) {
                this.flag = flag;
            }

            public static StretchBltMode valueOf(int flag) {
                for (StretchBltMode bs : values()) {
                    if (bs.flag == flag) return bs;
                }
                return null;
            }
        }

        protected StretchBltMode stretchBltMode;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setStretchBltMode;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            stretchBltMode = StretchBltMode.valueOf(leis.readUShort());
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public StretchBltMode getStretchBltMode() {
            return stretchBltMode;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("stretchBltMode", this::getStretchBltMode);
        }
    }

    /**
     * The META_DIBCREATEPATTERNBRUSH record creates a Brush Object with a
     * pattern specified by a DeviceIndependentBitmap (DIB) Object
     */
    public static class WmfDibCreatePatternBrush implements HwmfRecord, HwmfImageRecord, HwmfObjectTableEntry {

        protected HwmfBrushStyle style;

        /**
         * A 16-bit unsigned integer that defines whether the Colors field of a DIB
         * Object contains explicit RGB values, or indexes into a palette.
         *
         * If the Style field specifies BS_PATTERN, a ColorUsage value of DIB_RGB_COLORS MUST be
         * used regardless of the contents of this field.
         *
         * If the Style field specified anything but BS_PATTERN, this field MUST be one of the ColorUsage values.
         */
        protected ColorUsage colorUsage;

        protected HwmfBitmapDib patternDib;
        private HwmfBitmap16 pattern16;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.dibCreatePatternBrush;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            style = HwmfBrushStyle.valueOf(leis.readUShort());
            colorUsage = ColorUsage.valueOf(leis.readUShort());
            int size = 2*LittleEndianConsts.SHORT_SIZE;
            switch (style) {
            case BS_SOLID:
            case BS_NULL:
            case BS_DIBPATTERN:
            case BS_DIBPATTERNPT:
            case BS_HATCHED:
            case BS_PATTERN:
                patternDib = new HwmfBitmapDib();
                size += patternDib.init(leis, (int)(recordSize-6-size));
                break;
            case BS_INDEXED:
            case BS_DIBPATTERN8X8:
            case BS_MONOPATTERN:
            case BS_PATTERN8X8:
                throw new IllegalStateException("pattern not supported");
            }
            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }

        @Override
        public void applyObject(HwmfGraphics ctx) {
            if (patternDib != null && !patternDib.isValid()) {
                return;
            }
            HwmfDrawProperties prop = ctx.getProperties();
            prop.setBrushStyle(style);

            BufferedImage bufImg = getImage(
                prop.getBrushColor().getColor(),
                prop.getBackgroundColor().getColor(),
                prop.getBkMode() == HwmfBkMode.TRANSPARENT);

            prop.setBrushBitmap(bufImg);
        }

        @Override
        public BufferedImage getImage(Color foreground, Color background, boolean hasAlpha) {
            if (patternDib != null && patternDib.isValid()) {
                return patternDib.getImage(foreground, background, hasAlpha);
            } else if (pattern16 != null) {
                return pattern16.getImage();
            } else {
                return null;
            }
        }

        @Override
        public byte[] getBMPData() {
            if (patternDib != null && patternDib.isValid()) {
                return patternDib.getBMPData();
            } else if (pattern16 != null) {
                return null;
            } else {
                return null;
            }
        }

        public HwmfBrushStyle getStyle() {
            return style;
        }

        public ColorUsage getColorUsage() {
            return colorUsage;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "style", this::getStyle,
                "colorUsage", this::getColorUsage,
                "pattern", () -> (patternDib != null && patternDib.isValid()) ? patternDib : pattern16,
                "bmpData", this::getBMPData
            );
        }
    }

    /**
     * The META_DELETEOBJECT record deletes an object, including Bitmap16, Brush,
     * DeviceIndependentBitmap, Font, Palette, Pen, and Region. After the object is deleted,
     * its index in the WMF Object Table is no longer valid but is available to be reused.
     */
    public static class WmfDeleteObject implements HwmfRecord {
        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to
         * get the object to be deleted.
         */
        protected int objectIndex;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.deleteObject;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            objectIndex = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            /* TODO:
             * The object specified by this record MUST be deleted from the EMF Object Table.
             * If the deleted object is currently selected in the playback device context,
             * the default object for that graphics property MUST be restored.
             */

            ctx.unsetObjectTableEntry(objectIndex);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public int getObjectIndex() {
            return objectIndex;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("objectIndex", this::getObjectIndex);
        }
    }

    public static class WmfCreatePatternBrush implements HwmfRecord, HwmfObjectTableEntry {

        private HwmfBitmap16 pattern;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.createPatternBrush;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            pattern = new HwmfBitmap16(true);
            return pattern.init(leis);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }

        @Override
        public void applyObject(HwmfGraphics ctx) {
            HwmfDrawProperties dp = ctx.getProperties();
            dp.setBrushBitmap(pattern.getImage());
            dp.setBrushStyle(HwmfBrushStyle.BS_PATTERN);
        }

        public HwmfBitmap16 getPattern() {
            return pattern;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("pattern", this::getPattern);
        }
    }

    public static class WmfCreatePenIndirect implements HwmfRecord, HwmfObjectTableEntry {

        protected HwmfPenStyle penStyle;

        protected final Dimension2D dimension = new Dimension2DDouble();
        /**
         * A 32-bit ColorRef Object that specifies the pen color value.
         */
        protected final HwmfColorRef colorRef = new HwmfColorRef();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.createPenIndirect;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            penStyle = HwmfPenStyle.valueOf(leis.readUShort());
            // A 32-bit PointS Object that specifies a point for the object dimensions.
            // The x-coordinate is the pen width. The y-coordinate is ignored.
            int xWidth = leis.readShort();
            int yWidth = leis.readShort();
            dimension.setSize(xWidth, yWidth);

            int size = colorRef.init(leis);
            return size+3*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }

        @Override
        public void applyObject(HwmfGraphics ctx) {
            HwmfDrawProperties p = ctx.getProperties();
            p.setPenStyle(penStyle);
            p.setPenColor(colorRef);
            p.setPenWidth(dimension.getWidth());
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public HwmfPenStyle getPenStyle() {
            return penStyle;
        }

        public Dimension2D getDimension() {
            return dimension;
        }

        public HwmfColorRef getColorRef() {
            return colorRef;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "penStyle", this::getPenStyle,
                "dimension", this::getDimension,
                "colorRef", this::getColorRef
            );
        }
    }

    /**
     * The META_CREATEBRUSHINDIRECT record creates a Brush Object
     * from a LogBrush Object.
     *
     * The following table shows the relationship between values in the BrushStyle,
     * ColorRef and BrushHatch fields in a LogBrush Object. Only supported brush styles are listed.
     *
     * <table>
     * <caption>Relationship between values in the BrushStyle, ColorRef and BrushHatch fields</caption>
     * <tr>
     *   <th>BrushStyle</th>
     *   <th>ColorRef</th>
     *   <th>BrushHatch</th>
     * </tr>
     * <tr>
     *   <td>BS_SOLID</td>
     *   <td>SHOULD be a ColorRef Object, which determines the color of the brush.</td>
     *   <td>Not used, and SHOULD be ignored.</td>
     * </tr>
     * <tr>
     *   <td>BS_NULL</td>
     *   <td>Not used, and SHOULD be ignored.</td>
     *   <td>Not used, and SHOULD be ignored.</td>
     * </tr>
     * <tr>
     *   <td>BS_PATTERN</td>
     *   <td>Not used, and SHOULD be ignored.</td>
     *   <td>Not used. A default object, such as a solidcolor black Brush Object, MAY be created.</td>
     * </tr>
     * <tr>
     *   <td>BS_DIBPATTERN</td>
     *   <td>Not used, and SHOULD be ignored.</td>
     *   <td>Not used. A default object, such as a solidcolor black Brush Object, MAY be created</td>
     * </tr>
     * <tr>
     *   <td>BS_DIBPATTERNPT</td>
     *   <td>Not used, and SHOULD be ignored.</td>
     *   <td>Not used. A default object, such as a solidcolor black Brush Object, MAY be created.</td>
     * </tr>
     * <tr>
     *   <td>BS_HATCHED</td>
     *   <td>SHOULD be a ColorRef Object, which determines the foreground color of the hatch pattern.</td>
     *   <td>A value from the {@link HwmfHatchStyle} Enumeration that specifies the orientation of lines used to create the hatch.</td>
     * </tr>
     * </table>
     */
    public static class WmfCreateBrushIndirect implements HwmfRecord, HwmfObjectTableEntry {
        protected HwmfBrushStyle brushStyle;

        protected HwmfColorRef colorRef;

        protected HwmfHatchStyle brushHatch;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.createBrushIndirect;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            brushStyle = HwmfBrushStyle.valueOf(leis.readUShort());
            colorRef = new HwmfColorRef();
            int size = colorRef.init(leis);
            brushHatch = HwmfHatchStyle.valueOf(leis.readUShort());
            return size+2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }

        @Override
        public void applyObject(HwmfGraphics ctx) {
            HwmfDrawProperties p = ctx.getProperties();
            p.setBrushStyle(brushStyle);
            p.setBrushColor(colorRef);
            p.setBrushHatch(brushHatch);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public HwmfBrushStyle getBrushStyle() {
            return brushStyle;
        }

        public HwmfColorRef getColorRef() {
            return colorRef;
        }

        public HwmfHatchStyle getBrushHatch() {
            return brushHatch;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "brushStyle", this::getBrushStyle,
                "colorRef", this::getColorRef,
                "brushHatch", this::getBrushHatch
            );
        }
    }
}