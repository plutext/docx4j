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

import static org.docx4j.org.apache.poi.hwmf.record.HwmfDraw.readPointS;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.hwmf.draw.HwmfDrawProperties;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfGraphics;
import org.docx4j.org.apache.poi.hwmf.record.HwmfMisc.WmfSetBkMode.HwmfBkMode;
import org.docx4j.org.apache.poi.sl.draw.ImageRenderer;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

@SuppressWarnings("WeakerAccess")
public class HwmfFill {
    /** A record which contains an image (to be extracted) */
    public interface HwmfImageRecord {

        default BufferedImage getImage() {
            return getImage(Color.BLACK, new Color(0x00FFFFFF, true), true);
        }

        /**
         * Provide an image using the fore-/background color, in case of a 1-bit pattern
         * @param foreground the foreground color
         * @param background the background color
         * @param hasAlpha if true, the background color is rendered transparent - see {@link HwmfMisc.WmfSetBkMode.HwmfBkMode}
         * @return the image
         *
         * @since POI 4.1.1
         */
        BufferedImage getImage(Color foreground, Color background, boolean hasAlpha);

        /**
         * @return the raw BMP data
         *
         * @see <a href="https://en.wikipedia.org/wiki/BMP_file_format">BMP format</a>
         * @since POI 4.1.1
         */
        byte[] getBMPData();
    }

    /**
     * The ColorUsage Enumeration (a 16-bit unsigned integer) specifies whether a color table
     * exists in a device-independent bitmap (DIB) and how to interpret its values,
     * i.e. if contains explicit RGB values or indexes into a palette.
     */
    public enum ColorUsage {
        /** The color table contains RGB values */
        DIB_RGB_COLORS(0x0000),
        /**
         * The color table contains 16-bit indices into the current logical palette in
         * the playback device context.
         */
        DIB_PAL_COLORS(0x0001),
        /**
         * No color table exists. The pixels in the DIB are indices into the current
         * logical palette in the playback device context.
         */
        DIB_PAL_INDICES(0x0002)
        ;


        public final int flag;
        ColorUsage(int flag) {
            this.flag = flag;
        }

        public static ColorUsage valueOf(int flag) {
            for (ColorUsage bs : values()) {
                if (bs.flag == flag) return bs;
            }
            return null;
        }
    }


    /**
     * The META_FILLREGION record fills a region using a specified brush.
     */
    public static class WmfFillRegion implements HwmfRecord {

        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the region to be filled.
         */
        protected int regionIndex;

        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get the
         * brush to use for filling the region.
         */
        protected int brushIndex;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.fillRegion;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            regionIndex = leis.readUShort();
            brushIndex = leis.readUShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.applyObjectTableEntry(regionIndex);
            ctx.applyObjectTableEntry(brushIndex);

            Shape region = ctx.getProperties().getRegion();
            if (region != null) {
                ctx.fill(region);
            }
        }

        public int getRegionIndex() {
            return regionIndex;
        }

        public int getBrushIndex() {
            return brushIndex;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "regionIndex", this::getRegionIndex,
                "brushIndex", this::getBrushIndex
            );
        }
    }

    /**
     * The META_PAINTREGION record paints the specified region by using the brush that is
     * defined in the playback device context.
     */
    public static class WmfPaintRegion implements HwmfRecord {

        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the region to be painted.
         */
        int regionIndex;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.paintRegion;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            regionIndex = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.applyObjectTableEntry(regionIndex);

            Shape region = ctx.getProperties().getRegion();
            if (region != null) {
                ctx.fill(region);
            }
        }

        public int getRegionIndex() {
            return regionIndex;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("regionIndex", this::getRegionIndex);
        }
    }


    /**
     * The META_FLOODFILL record fills an area of the output surface with the brush that
     * is defined in the playback device context.
     */
    public static class WmfFloodFill implements HwmfRecord {

        /** A 32-bit ColorRef Object that defines the color value. */
        protected final HwmfColorRef colorRef = new HwmfColorRef();

        /** the point where filling is to start. */
        protected final Point2D start = new Point2D.Double();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.floodFill;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            int size = colorRef.init(leis);
            size += readPointS(leis, start);
            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }

        public HwmfColorRef getColorRef() {
            return colorRef;
        }

        public Point2D getStart() {
            return start;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "colorRef", this::getColorRef,
                "start", this::getStart
            );
        }
    }

    /**
     * The META_SETPOLYFILLMODE record sets polygon fill mode in the playback device context for
     * graphics operations that fill polygons.
     */
    public static class WmfSetPolyfillMode implements HwmfRecord {
        /**
         * A 16-bit unsigned integer that defines polygon fill mode.
         * This MUST be one of the values: ALTERNATE = 0x0001, WINDING = 0x0002
         */
        public enum HwmfPolyfillMode {
            /**
             * Selects alternate mode (fills the area between odd-numbered and
             * even-numbered polygon sides on each scan line).
             */
            ALTERNATE(0x0001, Path2D.WIND_EVEN_ODD),
            /**
             * Selects winding mode (fills any region with a nonzero winding value).
             */
            WINDING(0x0002, Path2D.WIND_NON_ZERO);

            public final int wmfFlag;
            public final int awtFlag;
            HwmfPolyfillMode(int wmfFlag, int awtFlag) {
                this.wmfFlag = wmfFlag;
                this.awtFlag = awtFlag;
            }

            public static HwmfPolyfillMode valueOf(int wmfFlag) {
                for (HwmfPolyfillMode pm : values()) {
                    if (pm.wmfFlag == wmfFlag) return pm;
                }
                return null;
            }
        }

        /**
         * An unsigned integer that defines polygon fill mode.
         * This MUST be one of the values: ALTERNATE = 0x0001, WINDING = 0x0002
         */
        protected HwmfPolyfillMode polyFillMode;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setPolyFillMode;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            polyFillMode = HwmfPolyfillMode.valueOf(leis.readUShort() & 3);
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setPolyfillMode(polyFillMode);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public HwmfPolyfillMode getPolyFillMode() {
            return polyFillMode;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("polyFillMode", this::getPolyFillMode);
        }
    }


    /**
     * The META_EXTFLOODFILL record fills an area with the brush that is defined in
     * the playback device context.
     */
    public static class WmfExtFloodFill extends WmfFloodFill {

        public enum HwmfFloodFillMode {
            /**
             * The fill area is bounded by the color specified by the Color member.
             * This style is identical to the filling performed by the META_FLOODFILL record.
             */
            FLOOD_FILL_BORDER,
            /**
             * The fill area is bounded by the color that is specified by the Color member.
             * Filling continues outward in all directions as long as the color is encountered.
             * This style is useful for filling areas with multicolored boundaries.
             */
            FLOOD_FILL_SURFACE
        }

        protected HwmfFloodFillMode mode;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.extFloodFill;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            // A 16-bit unsigned integer that defines the fill operation to be performed. This
            // member MUST be one of the values in the FloodFill Enumeration table:
            mode = HwmfFloodFillMode.values()[leis.readUShort()];
            return super.init(leis, recordSize, recordFunction)+LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }

        public HwmfFloodFillMode getMode() {
            return mode;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("mode", this::getMode);
        }
    }

    /**
     * The META_INVERTREGION record draws a region in which the colors are inverted.
     */
    public static class WmfInvertRegion implements HwmfRecord {

        /**
         * A 16-bit unsigned integer used to index into the WMF Object Table to get
         * the region to be inverted.
         */
        private int regionIndex;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.invertRegion;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            regionIndex = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }

        public int getRegionIndex() {
            return regionIndex;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("regionIndex", this::getRegionIndex);
        }
    }


    /**
     * The META_PATBLT record paints a specified rectangle using the brush that is defined in the playback
     * device context. The brush color and the surface color or colors are combined using the specified
     * raster operation.
     */
    public static class WmfPatBlt implements HwmfRecord {

        /**
         * A 32-bit unsigned integer that defines the raster operation code.
         * This code MUST be one of the values in the Ternary Raster Operation enumeration table.
         */
        private HwmfTernaryRasterOp rasterOperation;

        private final Rectangle2D bounds = new Rectangle2D.Double();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.patBlt;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            rasterOperation = readRasterOperation(leis);
            return readBounds2(leis, bounds)+2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }

        public HwmfTernaryRasterOp getRasterOperation() {
            return rasterOperation;
        }

        public Rectangle2D getBounds() {
            return bounds;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "rasterOperation", this::getRasterOperation,
                "bounds", this::getBounds);
        }
    }

    /**
     * The META_STRETCHBLT record specifies the transfer of a block of pixels according to a raster
     * operation, with possible expansion or contraction.
     * The destination of the transfer is the current output region in the playback device context.
     * There are two forms of META_STRETCHBLT, one which specifies a bitmap as the source, and the other
     * which uses the playback device context as the source. Definitions follow for the fields that are the
     * same in the two forms of META_STRETCHBLT are defined below. The subsections that follow specify
     * the packet structures of the two forms of META_STRETCHBLT.
     * The expansion or contraction is performed according to the stretching mode currently set in the
     * playback device context, which MUST be a value from the StretchMode.
     */
    public static class WmfStretchBlt implements HwmfRecord {
        /**
         * A 32-bit unsigned integer that defines how the source pixels, the current brush
         * in the playback device context, and the destination pixels are to be combined to form the new
         * image. This code MUST be one of the values in the Ternary Raster Operation Enumeration
         */
        protected HwmfTernaryRasterOp rasterOperation;

        /** the source rectangle */
        protected final Rectangle2D srcBounds = new Rectangle2D.Double();

        /** the destination rectangle */
        protected final Rectangle2D dstBounds = new Rectangle2D.Double();

        /**
         * A variable-sized Bitmap16 Object that defines source image content.
         * This object MUST be specified, even if the raster operation does not require a source.
         */
        protected HwmfBitmap16 target;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.stretchBlt;
        }


        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            final boolean hasBitmap = hasBitmap(recordSize, recordFunction);

            rasterOperation = readRasterOperation(leis);

            int size = 2*LittleEndianConsts.SHORT_SIZE;

            size += readBounds2(leis, srcBounds);

            if (!hasBitmap) {
                /*int reserved =*/ leis.readShort();
                size += LittleEndianConsts.SHORT_SIZE;
            }

            size += readBounds2(leis, dstBounds);

            if (hasBitmap) {
                target = new HwmfBitmap16();
                size += target.init(leis);
            }

            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public HwmfTernaryRasterOp getRasterOperation() {
            return rasterOperation;
        }

        public Rectangle2D getSrcBounds() {
            return srcBounds;
        }

        public Rectangle2D getDstBounds() {
            return dstBounds;
        }

        public HwmfBitmap16 getTarget() {
            return target;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "rasterOperation", this::getRasterOperation,
                "srcBounds", this::getSrcBounds,
                "dstBounds", this::getDstBounds,
                "target", this::getTarget
            );
        }
    }

    /**
     * The META_STRETCHDIB record specifies the transfer of color data from a
     * block of pixels in device independent format according to a raster operation,
     * with possible expansion or contraction.
     * The source of the color data is a DIB, and the destination of the transfer is
     * the current output region in the playback device context.
     */
    public static class WmfStretchDib implements HwmfRecord, HwmfImageRecord {
        /**
         * A 32-bit unsigned integer that defines how the source pixels, the current brush in
         * the playback device context, and the destination pixels are to be combined to
         * form the new image.
         */
        protected HwmfTernaryRasterOp rasterOperation;

        /**
         * A 16-bit unsigned integer that defines whether the Colors field of the
         * DIB contains explicit RGB values or indexes into a palette.
         */
        protected ColorUsage colorUsage;

        /** the source rectangle. */
        protected final Rectangle2D srcBounds = new Rectangle2D.Double();

        /** the destination rectangle. */
        protected final Rectangle2D dstBounds = new Rectangle2D.Double();

        /**
         * A variable-sized DeviceIndependentBitmap Object (section 2.2.2.9) that is the
         * source of the color data.
         */
        protected final HwmfBitmapDib bitmap = new HwmfBitmapDib();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.stretchDib;
        }


        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            rasterOperation = readRasterOperation(leis);
            colorUsage = ColorUsage.valueOf(leis.readUShort());

            int size = 3*LittleEndianConsts.SHORT_SIZE;

            size += readBounds2(leis, srcBounds);
            size += readBounds2(leis, dstBounds);

            size += bitmap.init(leis, (int)(recordSize-6-size));

            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            HwmfDrawProperties prop = ctx.getProperties();
            prop.setRasterOp3(rasterOperation);
            if (bitmap.isValid()) {
                BufferedImage bi = bitmap.getImage(prop.getPenColor().getColor(), prop.getBackgroundColor().getColor(),
                                                   prop.getBkMode() == HwmfBkMode.TRANSPARENT);
                ctx.drawImage(bi, srcBounds, dstBounds);
            } else if (!dstBounds.isEmpty()) {
                ctx.drawImage((ImageRenderer)null, new Rectangle2D.Double(0,0,1,1), dstBounds);
            }
        }

        @Override
        public BufferedImage getImage(Color foreground, Color background, boolean hasAlpha) {
            return bitmap.getImage(foreground,background,hasAlpha);
        }

        public HwmfBitmapDib getBitmap() {
            return bitmap;
        }

        @Override
        public byte[] getBMPData() {
            return bitmap.getBMPData();
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public HwmfTernaryRasterOp getRasterOperation() {
            return rasterOperation;
        }

        public ColorUsage getColorUsage() {
            return colorUsage;
        }

        public Rectangle2D getSrcBounds() {
            return srcBounds;
        }

        public Rectangle2D getDstBounds() {
            return dstBounds;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "rasterOperation", this::getRasterOperation,
                "colorUsage", this::getColorUsage,
                "srcBounds", this::getSrcBounds,
                "dstBounds", this::getDstBounds
            );
        }
    }

    public static class WmfBitBlt extends WmfStretchBlt {

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.bitBlt;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            final boolean hasBitmap = hasBitmap(recordSize/2, recordFunction);

            rasterOperation = readRasterOperation(leis);

            int size = 2*LittleEndianConsts.SHORT_SIZE;

            final Point2D srcPnt = new Point2D.Double();
            size += readPointS(leis, srcPnt);

            if (!hasBitmap) {
                /*int reserved =*/ leis.readShort();
                size += LittleEndianConsts.SHORT_SIZE;
            }

            size += readBounds2(leis, dstBounds);

            if (hasBitmap) {
                target = new HwmfBitmap16();
                size += target.init(leis);
            }

            srcBounds.setRect(srcPnt.getX(), srcPnt.getY(), dstBounds.getWidth(), dstBounds.getHeight());

            return size;
        }
    }


    /**
     * The META_SETDIBTODEV record sets a block of pixels in the playback device context
     * using deviceindependent color data.
     * The source of the color data is a DIB
     */
    public static class WmfSetDibToDev implements HwmfRecord, HwmfImageRecord, HwmfObjectTableEntry {

        /**
         * A 16-bit unsigned integer that defines whether the Colors field of the
         * DIB contains explicit RGB values or indexes into a palette.
         */
        private ColorUsage colorUsage;
        /**
         * A 16-bit unsigned integer that defines the number of scan lines in the source.
         */
        private int scanCount;
        /**
         * A 16-bit unsigned integer that defines the starting scan line in the source.
         */
        private int startScan;

        /** the source rectangle */
        protected final Rectangle2D srcBounds = new Rectangle2D.Double();

        /** the destination rectangle, having the same dimension as the source rectangle */
        protected final Rectangle2D dstBounds = new Rectangle2D.Double();

        /**
         * A variable-sized DeviceIndependentBitmap Object that is the source of the color data.
         */
        private HwmfBitmapDib dib;


        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setDibToDev;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            colorUsage = ColorUsage.valueOf(leis.readUShort());
            scanCount = leis.readUShort();
            startScan = leis.readUShort();

            int size = 3*LittleEndianConsts.SHORT_SIZE;

            final Point2D srcPnt = new Point2D.Double();
            size += readPointS(leis, srcPnt);

            size += readBounds2(leis, dstBounds);

            dib = new HwmfBitmapDib();
            size += dib.init(leis, (int)(recordSize-6-size));

            srcBounds.setRect(srcPnt.getX(), srcPnt.getY(), dstBounds.getWidth(), dstBounds.getHeight());

            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }

        @Override
        public void applyObject(HwmfGraphics ctx) {

        }

        @Override
        public BufferedImage getImage(Color foreground, Color background, boolean hasAlpha) {
            return dib.getImage(foreground,background,hasAlpha);
        }

        @Override
        public byte[] getBMPData() {
            return dib.getBMPData();
        }

        public ColorUsage getColorUsage() {
            return colorUsage;
        }

        public int getScanCount() {
            return scanCount;
        }

        public int getStartScan() {
            return startScan;
        }

        public Rectangle2D getSrcBounds() {
            return srcBounds;
        }

        public Rectangle2D getDstBounds() {
            return dstBounds;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "colorUsage", this::getColorUsage,
                "scanCount", this::getScanCount,
                "startScan", this::getStartScan,
                "srcBounds", this::getSrcBounds,
                "dstBounds", this::getDstBounds,
                "dib", () -> dib
            );
        }
    }


    public static class WmfDibBitBlt extends WmfDibStretchBlt {
        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.dibBitBlt;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            final boolean hasBitmap = hasBitmap(recordSize/2, recordFunction);

            rasterOperation = readRasterOperation(leis);

            int size = 2*LittleEndianConsts.SHORT_SIZE;

            final Point2D srcPnt = new Point2D.Double();
            size += readPointS(leis, srcPnt);
            if (!hasBitmap) {
                /*int reserved =*/ leis.readShort();
                size += LittleEndianConsts.SHORT_SIZE;
            }

            size += readBounds2(leis, dstBounds);
            if (hasBitmap) {
                target = new HwmfBitmapDib();
                size += target.init(leis, (int)(recordSize-6-size));
            }

            // the destination rectangle, having the same dimension as the source rectangle
            srcBounds.setRect(srcPnt.getX(), srcPnt.getY(), dstBounds.getWidth(), dstBounds.getHeight());

            return size;
        }
    }

    /**
     * The META_DIBSTRETCHBLT record specifies the transfer of a block of pixels in device-independent format
     * according to a raster operation, with possible expansion or contraction.
     *
     * The destination of the transfer is the current output region in the playback device context.
     * There are two forms of META_DIBSTRETCHBLT, one which specifies a device-independent bitmap (DIB) as the source,
     * and the other which uses the playback device context as the source. Definitions follow for the fields that are
     * the same in the two forms of META_DIBSTRETCHBLT. The subsections that follow specify the packet structures of
     * the two forms of META_DIBSTRETCHBLT.
     */
    public static class WmfDibStretchBlt implements HwmfRecord, HwmfImageRecord {
        /**
         * A 32-bit unsigned integer that defines how the source pixels, the current brush
         * in the playback device context, and the destination pixels are to be combined to form the
         * new image. This code MUST be one of the values in the Ternary Raster Operation Enumeration.
         */
        protected HwmfTernaryRasterOp rasterOperation;

        /** the source rectangle */
        protected final Rectangle2D srcBounds = new Rectangle2D.Double();

        /** the destination rectangle */
        protected final Rectangle2D dstBounds = new Rectangle2D.Double();

        /**
         * A variable-sized DeviceIndependentBitmap Object that defines image content.
         * This object MUST be specified, even if the raster operation does not require a source.
         */
        protected HwmfBitmapDib target;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.dibStretchBlt;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            final boolean hasBitmap = hasBitmap(recordSize, recordFunction);

            rasterOperation = readRasterOperation(leis);

            int size = 2*LittleEndianConsts.SHORT_SIZE;

            size += readBounds2(leis, srcBounds);
            if (!hasBitmap) {
                /*int reserved =*/ leis.readShort();
                size += LittleEndianConsts.SHORT_SIZE;
            }

            size += readBounds2(leis, dstBounds);
            if (hasBitmap) {
                target = new HwmfBitmapDib();
                size += target.init(leis, (int)(recordSize-6-size));
            }

            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            HwmfDrawProperties prop = ctx.getProperties();
            prop.setRasterOp3(rasterOperation);
            // TODO: implement second operation based on playback device context
            if (target != null) {
                HwmfBkMode oldMode = prop.getBkMode();
                prop.setBkMode(HwmfBkMode.TRANSPARENT);
                Color fgColor = prop.getPenColor().getColor();
                Color bgColor = prop.getBackgroundColor().getColor();
                BufferedImage bi = target.getImage(fgColor, bgColor, true);
                ctx.drawImage(bi, srcBounds, dstBounds);
                prop.setBkMode(oldMode);
            }
        }

        @Override
        public BufferedImage getImage(Color foreground, Color background, boolean hasAlpha) {
            return (target != null && target.isValid()) ? target.getImage(foreground,background,hasAlpha) : null;
        }

        @Override
        public byte[] getBMPData() {
            return (target != null && target.isValid()) ? target.getBMPData() : null;
        }

        public HwmfTernaryRasterOp getRasterOperation() {
            return rasterOperation;
        }

        public Rectangle2D getSrcBounds() {
            return srcBounds;
        }

        public Rectangle2D getDstBounds() {
            return dstBounds;
        }

        public HwmfBitmapDib getTarget() {
            return target;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "rasterOperation", this::getRasterOperation,
                "srcBounds", this::getSrcBounds,
                "dstBounds", this::getDstBounds,
                "target", this::getTarget
            );
        }
    }

    static int readBounds2(LittleEndianInputStream leis, Rectangle2D bounds) {
        // The 16-bit signed integers that defines the corners of the bounding rectangle.
        int h = leis.readShort();
        int w = leis.readShort();
        int y = leis.readShort();
        int x = leis.readShort();

        bounds.setRect(x, y, w, h);

        return 4 * LittleEndianConsts.SHORT_SIZE;
    }

    private static boolean hasBitmap(long recordSize, int recordFunction) {
        return (recordSize > ((recordFunction >> 8) + 3));
    }

    private static HwmfTernaryRasterOp readRasterOperation(LittleEndianInputStream leis) {
        int rasterOpCode = leis.readUShort();
        int rasterOpIndex = leis.readUShort();

        HwmfTernaryRasterOp rasterOperation = HwmfTernaryRasterOp.valueOf(rasterOpIndex);
        assert(rasterOperation != null && rasterOpCode == rasterOperation.getOpCode());
        return rasterOperation;
    }
}
