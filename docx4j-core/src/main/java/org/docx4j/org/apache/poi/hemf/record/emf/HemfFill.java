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
import static org.docx4j.org.apache.poi.hemf.record.emf.HemfDraw.readRectL;
import static org.docx4j.org.apache.poi.hemf.record.emf.HemfRecordIterator.HEADER_SIZE;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.docx4j.org.apache.poi.hemf.draw.HemfDrawProperties;
import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfGraphics;
import org.docx4j.org.apache.poi.hwmf.record.HwmfBitmapDib;
import org.docx4j.org.apache.poi.hwmf.record.HwmfColorRef;
import org.docx4j.org.apache.poi.hwmf.record.HwmfDraw;
import org.docx4j.org.apache.poi.hwmf.record.HwmfFill;
import org.docx4j.org.apache.poi.hwmf.record.HwmfFill.ColorUsage;
import org.docx4j.org.apache.poi.hwmf.record.HwmfRegionMode;
import org.docx4j.org.apache.poi.hwmf.record.HwmfTernaryRasterOp;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

public final class HemfFill {
    private HemfFill() {}

    /**
     * The EMR_SETPOLYFILLMODE record defines polygon fill mode.
     */
    public static class EmfSetPolyfillMode extends HwmfFill.WmfSetPolyfillMode implements HemfRecord {

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setPolyfillMode;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer that specifies the polygon fill mode and
            // MUST be in the PolygonFillMode enumeration.
            polyFillMode = HwmfPolyfillMode.valueOf((int)leis.readUInt());
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    public static class EmfExtFloodFill extends HwmfFill.WmfExtFloodFill implements HemfRecord {

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.extFloodFill;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readPointL(leis, start);
            size += colorRef.init(leis);
            // A 32-bit unsigned integer that specifies how to use the Color value to determine the area for
            // the flood fill operation. The value MUST be in the FloodFill enumeration
            mode = HwmfFloodFillMode.values()[(int)leis.readUInt()];
            return size + LittleEndianConsts.INT_SIZE;
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_STRETCHBLT record specifies a block transfer of pixels from a source bitmap to a destination rectangle,
     * optionally in combination with a brush pattern, according to a specified raster operation, stretching or
     * compressing the output to fit the dimensions of the destination, if necessary.
     */
    public static class EmfStretchBlt extends HwmfFill.WmfStretchDib implements HemfRecord {
        protected final Rectangle2D bounds = new Rectangle2D.Double();

        /** An XForm object that specifies a world-space to page-space transform to apply to the source bitmap. */
        protected final AffineTransform xFormSrc = new AffineTransform();

        /** A WMF ColorRef object that specifies the background color of the source bitmap. */
        protected final HwmfColorRef bkColorSrc = new HwmfColorRef();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.stretchBlt;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            int startIdx = leis.getReadIndex();

            long size = readRectL(leis, bounds);

            size += readBounds2(leis, this.dstBounds);

            // A 32-bit unsigned integer that specifies the raster operation code. This code defines how the
            // color data of the source rectangle is to be combined with the color data of the destination
            // rectangle and optionally a brush pattern, to achieve the final color.
            int rasterOpIndex = (int)leis.readUInt();

            rasterOperation = HwmfTernaryRasterOp.valueOf(rasterOpIndex >>> 16);

            size += LittleEndianConsts.INT_SIZE;

            final Point2D srcPnt = new Point2D.Double();
            size += readPointL(leis, srcPnt);

            size += readXForm(leis, xFormSrc);

            size += bkColorSrc.init(leis);

            colorUsage = ColorUsage.valueOf((int)leis.readUInt());

            // A 32-bit unsigned integer that specifies the offset, in bytes, from the
            // start of this record to the source bitmap header in the BitmapBuffer field.
            final int offBmiSrc = (int)leis.readUInt();

            // A 32-bit unsigned integer that specifies the size, in bytes, of the source bitmap header.
            final int cbBmiSrc = (int)leis.readUInt();
            size += 3*LittleEndianConsts.INT_SIZE;
            if (size >= recordSize) {
                return size;
            }

            // A 32-bit unsigned integer that specifies the offset, in bytes, from the
            // start of this record to the source bitmap bits in the BitmapBuffer field.
            final int offBitsSrc = (int)leis.readUInt();

            // A 32-bit unsigned integer that specifies the size, in bytes, of the source bitmap bits.
            final int cbBitsSrc = (int)leis.readUInt();
            size += 2*LittleEndianConsts.INT_SIZE;

            if (size >= recordSize) {
                return size;
            }

            if (srcEqualsDstDimension()) {
                srcBounds.setRect(srcPnt.getX(), srcPnt.getY(), dstBounds.getWidth(), dstBounds.getHeight());
            } else {
                int srcWidth = leis.readInt();
                int srcHeight = leis.readInt();
                size += 2 * LittleEndianConsts.INT_SIZE;
                srcBounds.setRect(srcPnt.getX(), srcPnt.getY(), srcWidth, srcHeight);
            }

            size += readBitmap(leis, bitmap, startIdx, offBmiSrc, cbBmiSrc, offBitsSrc, cbBitsSrc);

            return size;
        }

        protected boolean srcEqualsDstDimension() {
            return false;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            HemfDrawProperties prop = ctx.getProperties();
            prop.setBackgroundColor(this.bkColorSrc);
            super.draw(ctx);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public Rectangle2D getBounds() {
            return bounds;
        }

        public AffineTransform getXFormSrc() {
            return xFormSrc;
        }

        public HwmfColorRef getBkColorSrc() {
            return bkColorSrc;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "base", super::getGenericProperties,
                "bounds", this::getBounds,
                "xFormSrc", this::getXFormSrc,
                "bkColorSrc", this::getBkColorSrc
            );
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_STRETCHDIBITS record specifies a block transfer of pixels from a source bitmap to a
     * destination rectangle, optionally in combination with a brush pattern, according to a specified raster
     * operation, stretching or compressing the output to fit the dimensions of the destination, if necessary.
     */
    public static class EmfStretchDiBits extends HwmfFill.WmfStretchDib implements HemfRecord {
        protected final Rectangle2D bounds = new Rectangle2D.Double();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.stretchDiBits;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            final int startIdx = leis.getReadIndex();

            long size = readRectL(leis, bounds);

            // A 32-bit signed integer that specifies the logical x-coordinate of the upper-left
            // corner of the destination rectangle.
            int xDest = leis.readInt();
            int yDest = leis.readInt();
            size += 2*LittleEndianConsts.INT_SIZE;

            size += readBounds2(leis, srcBounds);

            // A 32-bit unsigned integer that specifies the offset, in bytes from the start
            // of this record to the source bitmap header.
            int offBmiSrc = (int)leis.readUInt();

            // A 32-bit unsigned integer that specifies the size, in bytes, of the source bitmap header.
            int cbBmiSrc = (int)leis.readUInt();

            // A 32-bit unsigned integer that specifies the offset, in bytes, from the
            // start of this record to the source bitmap bits.
            int offBitsSrc = (int)leis.readUInt();

            // A 32-bit unsigned integer that specifies the size, in bytes, of the source bitmap bits.
            int cbBitsSrc = (int)leis.readUInt();

            // A 32-bit unsigned integer that specifies how to interpret values in the color table
            // in the source bitmap header. This value MUST be in the DIBColors enumeration
            colorUsage = ColorUsage.valueOf(leis.readInt());

            // A 32-bit unsigned integer that specifies a raster operation code.
            // These codes define how the color data of the source rectangle is to be combined with the color data
            // of the destination rectangle and optionally a brush pattern, to achieve the final color.
            // The value MUST be in the WMF Ternary Raster Operation enumeration
            int rasterOpIndex = (int)leis.readUInt();
            rasterOperation = HwmfTernaryRasterOp.valueOf(rasterOpIndex >>> 16);

            // A 32-bit signed integer that specifies the logical width of the destination rectangle.
            int cxDest = leis.readInt();

            // A 32-bit signed integer that specifies the logical height of the destination rectangle.
            int cyDest = leis.readInt();

            dstBounds.setRect(xDest, yDest, cxDest, cyDest);

            size += 8*LittleEndianConsts.INT_SIZE;

            size += readBitmap(leis, bitmap, startIdx, offBmiSrc, cbBmiSrc, offBitsSrc, cbBitsSrc);

            return size;
        }

        public Rectangle2D getBounds() {
            return bounds;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "base", super::getGenericProperties,
                "bounds", this::getBounds
            );
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_BITBLT record specifies a block transfer of pixels from a source bitmap to a destination rectangle,
     * optionally in combination with a brush pattern, according to a specified raster operation.
     */
    public static class EmfBitBlt extends EmfStretchBlt {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.bitBlt;
        }

        @Override
        protected boolean srcEqualsDstDimension() {
            return false;
        }
    }


    /** The EMR_FRAMERGN record draws a border around the specified region using the specified brush. */
    public static class EmfFrameRgn extends HwmfDraw.WmfFrameRegion implements HemfRecord {
        private final Rectangle2D bounds = new Rectangle2D.Double();
        private final List<Rectangle2D> rgnRects = new ArrayList<>();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.frameRgn;
        }

        @SuppressWarnings("unused")
        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            // A 32-bit unsigned integer that specifies the size of region data, in bytes.
            long rgnDataSize = leis.readUInt();
            // A 32-bit unsigned integer that specifies the brush EMF Object Table index.
            brushIndex = (int)leis.readUInt();
            // A 32-bit signed integer that specifies the width of the vertical brush stroke, in logical units.
            int width = leis.readInt();
            // A 32-bit signed integer that specifies the height of the horizontal brush stroke, in logical units.
            int height = leis.readInt();
            frame.setSize(width,height);
            size += 4*LittleEndianConsts.INT_SIZE;
            size += readRgnData(leis, rgnRects);
            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.applyObjectTableEntry(brushIndex);
            ctx.fill(getShape());
        }

        protected Shape getShape() {
            return getRgnShape(rgnRects);
        }

        public Rectangle2D getBounds() {
            return bounds;
        }

        public List<Rectangle2D> getRgnRects() {
            return rgnRects;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "base", super::getGenericProperties,
                "bounds", this::getBounds,
                "rgnRects", this::getRgnRects
            );
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /** The EMR_INVERTRGN record inverts the colors in the specified region. */
    public static class EmfInvertRgn implements HemfRecord {
        protected final Rectangle2D bounds = new Rectangle2D.Double();
        protected final List<Rectangle2D> rgnRects = new ArrayList<>();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.invertRgn;
        }

        @SuppressWarnings("unused")
        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            // A 32-bit unsigned integer that specifies the size of region data, in bytes.
            long rgnDataSize = leis.readUInt();
            size += LittleEndianConsts.INT_SIZE;
            size += readRgnData(leis, rgnRects);
            return size;
        }

        protected Shape getShape() {
            return getRgnShape(rgnRects);
        }

        public Rectangle2D getBounds() {
            return bounds;
        }

        public List<Rectangle2D> getRgnRects() {
            return rgnRects;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "bounds", this::getBounds,
                "rgnRects", this::getRgnRects
            );
        }
    }

    /**
     * The EMR_PAINTRGN record paints the specified region by using the brush currently selected into the
     * playback device context.
     */
    public static class EmfPaintRgn extends EmfInvertRgn {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.paintRgn;
        }
    }

    /** The EMR_FILLRGN record fills the specified region by using the specified brush. */
    public static class EmfFillRgn extends HwmfFill.WmfFillRegion implements HemfRecord {
        protected final Rectangle2D bounds = new Rectangle2D.Double();
        protected final List<Rectangle2D> rgnRects = new ArrayList<>();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.fillRgn;
        }

        @SuppressWarnings("unused")
        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            // A 32-bit unsigned integer that specifies the size of region data, in bytes.
            long rgnDataSize = leis.readUInt();
            brushIndex = (int)leis.readUInt();
            size += 2*LittleEndianConsts.INT_SIZE;
            size += readRgnData(leis, rgnRects);
            return size;
        }

        protected Shape getShape() {
            return getRgnShape(rgnRects);
        }

        public Rectangle2D getBounds() {
            return bounds;
        }

        public List<Rectangle2D> getRgnRects() {
            return rgnRects;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "base", super::getGenericProperties,
                "bounds", this::getBounds,
                "rgnRects", this::getRgnRects
            );
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    public static class EmfExtSelectClipRgn implements HemfRecord {
        protected HwmfRegionMode regionMode;
        protected final List<Rectangle2D> rgnRects = new ArrayList<>();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.extSelectClipRgn;
        }

        @SuppressWarnings("unused")
        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer that specifies the size of region data in bytes
            long rgnDataSize = leis.readUInt();
            // A 32-bit unsigned integer that specifies the way to use the region.
            regionMode = HwmfRegionMode.valueOf((int)leis.readUInt());
            long size = 2L * LittleEndianConsts.INT_SIZE;

            // If RegionMode is RGN_COPY, this data can be omitted and the clip region
            // SHOULD be set to the default (NULL) clip region.
            if (regionMode != HwmfRegionMode.RGN_COPY) {
                size += readRgnData(leis, rgnRects);
            }
            return size;
        }

        protected Shape getShape() {
            return getRgnShape(rgnRects);
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.setClip(getShape(), regionMode, true);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public HwmfRegionMode getRegionMode() {
            return regionMode;
        }

        public List<Rectangle2D> getRgnRects() {
            return rgnRects;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "regionMode", this::getRegionMode,
                "rgnRects", this::getRgnRects
            );
        }
    }

    public static class EmfAlphaBlend implements HemfRecord {
        /** the destination bounding rectangle in device units */
        protected final Rectangle2D bounds = new Rectangle2D.Double();
        /** the destination rectangle */
        protected final Rectangle2D destRect = new Rectangle2D.Double();
        /** the source rectangle */
        protected final Rectangle2D srcRect = new Rectangle2D.Double();
        /**
         * The blend operation code. The only source and destination blend operation that has been defined
         * is 0x00, which specifies that the source bitmap MUST be combined with the destination bitmap based
         * on the alpha transparency values of the source pixels.
         */
        protected byte blendOperation;
        /** This value MUST be 0x00 and MUST be ignored. */
        protected byte blendFlags;
        /**
         * An 8-bit unsigned integer that specifies alpha transparency, which determines the blend of the source
         * and destination bitmaps. This value MUST be used on the entire source bitmap. The minimum alpha
         * transparency value, zero, corresponds to completely transparent; the maximum value, 0xFF, corresponds
         * to completely opaque. In effect, a value of 0xFF specifies that the per-pixel alpha values determine
         * the blend of the source and destination bitmaps.
         */
        protected int srcConstantAlpha;
        /**
         * A byte that specifies how source and destination pixels are interpreted with respect to alpha transparency.
         *
         * 0x00:
         * The pixels in the source bitmap do not specify alpha transparency.
         * In this case, the SrcConstantAlpha value determines the blend of the source and destination bitmaps.
         * Note that in the following equations SrcConstantAlpha is divided by 255,
         * which produces a value in the range 0 to 1.
         *
         * 0x01: "AC_SRC_ALPHA"
         * Indicates that the source bitmap is 32 bits-per-pixel and specifies an alpha transparency value
         * for each pixel.
         */
        protected byte alphaFormat;
        /** a world-space to page-space transform to apply to the source bitmap. */
        protected final AffineTransform xFormSrc = new AffineTransform();
        /** the background color of the source bitmap. */
        protected final HwmfColorRef bkColorSrc = new HwmfColorRef();
        /**
         * A 32-bit unsigned integer that specifies how to interpret values in the
         * color table in the source bitmap header.
         */
        protected ColorUsage usageSrc;

        protected final HwmfBitmapDib bitmap = new HwmfBitmapDib();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.alphaBlend;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            final int startIdx = leis.getReadIndex();

            long size = readRectL(leis, bounds);
            size += readBounds2(leis, destRect);

            blendOperation = leis.readByte();
            assert (blendOperation == 0);
            blendFlags = leis.readByte();
            assert (blendOperation == 0);
            srcConstantAlpha = leis.readUByte();
            alphaFormat = leis.readByte();

            // A 32-bit signed integer that specifies the logical x-coordinate of the upper-left
            // corner of the source rectangle.
            final int xSrc = leis.readInt();
            // A 32-bit signed integer that specifies the logical y-coordinate of the upper-left
            // corner of the source rectangle.
            final int ySrc = leis.readInt();

            size += 3*LittleEndianConsts.INT_SIZE;
            size += readXForm(leis, xFormSrc);
            size += bkColorSrc.init(leis);

            usageSrc = ColorUsage.valueOf((int)leis.readUInt());


            // A 32-bit unsigned integer that specifies the offset, in bytes, from the
            // start of this record to the source bitmap header in the BitmapBuffer field.
            final int offBmiSrc = (int)leis.readUInt();

            // A 32-bit unsigned integer that specifies the size, in bytes, of the source bitmap header.
            final int cbBmiSrc = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the offset, in bytes, from the
            // start of this record to the source bitmap bits in the BitmapBuffer field.
            final int offBitsSrc = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the size, in bytes, of the source bitmap bits.
            final int cbBitsSrc = (int)leis.readUInt();

            // A 32-bit signed integer that specifies the logical width of the source rectangle.
            // This value MUST be greater than zero.
            final int cxSrc = leis.readInt();
            // A 32-bit signed integer that specifies the logical height of the source rectangle.
            // This value MUST be greater than zero.
            final int cySrc = leis.readInt();

            srcRect.setRect(xSrc, ySrc, cxSrc, cySrc);

            size += 7 * LittleEndianConsts.INT_SIZE;

            size += readBitmap(leis, bitmap, startIdx, offBmiSrc, cbBmiSrc, offBitsSrc, cbBitsSrc);

            return size;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            final Map<String,Supplier<?>> m = new LinkedHashMap<>();
            m.put("bounds", () -> bounds);
            m.put("destRect", () -> destRect);
            m.put("srcRect", () -> srcRect);
            m.put("blendOperation", () -> blendOperation);
            m.put("blendFlags", () -> blendFlags);
            m.put("srcConstantAlpha", () -> srcConstantAlpha);
            m.put("alphaFormat", () -> alphaFormat);
            m.put("xFormSrc", () -> xFormSrc);
            m.put("bkColorSrc", () -> bkColorSrc);
            m.put("usageSrc", () -> usageSrc);
            m.put("bitmap", () -> bitmap);
            return Collections.unmodifiableMap(m);
        }
    }

    /**
     * The EMR_SETDIBITSTODEVICE record specifies a block transfer of pixels from specified scanlines of
     * a source bitmap to a destination rectangle.
     */
    public static class EmfSetDiBitsToDevice implements HemfRecord {
        protected final Rectangle2D bounds = new Rectangle2D.Double();
        protected final Point2D dest = new Point2D.Double();
        protected final Rectangle2D src = new Rectangle2D.Double();
        protected ColorUsage usageSrc;
        protected final HwmfBitmapDib bitmap = new HwmfBitmapDib();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setDiBitsToDevice;
        }

        @SuppressWarnings("unused")
        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            int startIdx = leis.getReadIndex();

            // A WMF RectL object that defines the destination bounding rectangle in device units.
            long size = readRectL(leis, bounds);
            // the logical x/y-coordinate of the upper-left corner of the destination rectangle.
            size += readPointL(leis, dest);
            // the source rectangle
            size += readBounds2(leis, src);
            // A 32-bit unsigned integer that specifies the offset, in bytes, from the
            // start of this record to the source bitmap header in the BitmapBuffer field.
            final int offBmiSrc = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the size, in bytes, of the source bitmap header.
            final int cbBmiSrc = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the offset, in bytes, from the
            // start of this record to the source bitmap bits in the BitmapBuffer field.
            final int offBitsSrc = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the size, in bytes, of the source bitmap bits.
            final int cbBitsSrc = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies how to interpret values in the color table
            // in the source bitmap header. This value MUST be in the DIBColors enumeration
            usageSrc = ColorUsage.valueOf((int)leis.readUInt());
            // A 32-bit unsigned integer that specifies the first scan line in the array.
            final int iStartScan = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the number of scan lines.
            final int cScans = (int)leis.readUInt();
            size += 7*LittleEndianConsts.INT_SIZE;

            size += readBitmap(leis, bitmap, startIdx, offBmiSrc, cbBmiSrc, offBitsSrc, cbBitsSrc);

            return size;
        }

        public Rectangle2D getBounds() {
            return bounds;
        }

        public Point2D getDest() {
            return dest;
        }

        public Rectangle2D getSrc() {
            return src;
        }

        public ColorUsage getUsageSrc() {
            return usageSrc;
        }

        public HwmfBitmapDib getBitmap() {
            return bitmap;
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "bounds", this::getBounds,
                "dest", this::getDest,
                "src", this::getSrc,
                "usageSrc", this::getUsageSrc,
                "bitmap", this::getBitmap
            );
        }
    }

    static long readBitmap(final LittleEndianInputStream leis, final HwmfBitmapDib bitmap,
            final int startIdx, final int offBmi, final int cbBmi, final int offBits, int cbBits)
    throws IOException {
        if (offBmi == 0) {
            return 0;
        }

        final int offCurr = leis.getReadIndex()-(startIdx-HEADER_SIZE);
        final int undefinedSpace1 = offBmi-offCurr;
        if (undefinedSpace1 < 0) {
            return 0;
        }

        final int undefinedSpace2 = offBits-offCurr-cbBmi-undefinedSpace1;
        assert(undefinedSpace2 >= 0);

        leis.skipFully(undefinedSpace1);

        if (cbBmi == 0 || cbBits == 0) {
            return undefinedSpace1;
        }

        final int dibSize = cbBmi+cbBits;
        if (undefinedSpace2 == 0) {
            return (long)undefinedSpace1 + bitmap.init(leis, dibSize);
        }

        final UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().setBufferSize(cbBmi+cbBits).get();
        final long cbBmiSrcAct = IOUtils.copy(leis, bos, cbBmi);
        assert (cbBmiSrcAct == cbBmi);
        leis.skipFully(undefinedSpace2);
        final long cbBitsSrcAct = IOUtils.copy(leis, bos, cbBits);
        assert (cbBitsSrcAct == cbBits);

        final LittleEndianInputStream leisDib = new LittleEndianInputStream(bos.toInputStream());
        final int dibSizeAct = bitmap.init(leisDib, dibSize);
        assert (dibSizeAct <= dibSize);
        return (long)undefinedSpace1 + cbBmi + undefinedSpace2 + cbBits;
    }


    @SuppressWarnings("unused")
    static long readRgnData(final LittleEndianInputStream leis, final List<Rectangle2D> rgnRects) {
        // *** RegionDataHeader ***
        // A 32-bit unsigned integer that specifies the size of this object in bytes. This MUST be 0x00000020.
        long rgnHdrSize = leis.readUInt();
        assert(rgnHdrSize == 0x20);
        // A 32-bit unsigned integer that specifies the region type. This SHOULD be RDH_RECTANGLES (0x00000001)
        long rgnHdrType = leis.readUInt();
        assert(rgnHdrType == 1);
        // A 32-bit unsigned integer that specifies the number of rectangles in this region.
        long rgnCntRect = leis.readUInt();
        // A 32-bit unsigned integer that specifies the size of the buffer of rectangles in bytes.
        long rgnCntBytes = leis.readUInt();
        long size = 4L*LittleEndianConsts.INT_SIZE;
        // A 128-bit WMF RectL object, which specifies the bounds of the region.
        Rectangle2D rgnBounds = new Rectangle2D.Double();
        size += readRectL(leis, rgnBounds);
        for (int i=0; i<rgnCntRect; i++) {
            Rectangle2D rgnRct = new Rectangle2D.Double();
            size += readRectL(leis, rgnRct);
            rgnRects.add(rgnRct);
        }
        return size;
    }


    static int readBounds2(LittleEndianInputStream leis, Rectangle2D bounds) {
        // The 32-bit signed integers that defines the corners of the bounding rectangle.
        int x = leis.readInt();
        int y = leis.readInt();
        int w = leis.readInt();
        int h = leis.readInt();

        bounds.setRect(x, y, w, h);

        return 4 * LittleEndianConsts.INT_SIZE;
    }

    public static int readXForm(LittleEndianInputStream leis, AffineTransform xform) {
        // mapping <java AffineTransform> = <xform>

        // m00 (scaleX) = eM11 (Horizontal scaling component)
        double m00 = leis.readFloat();

        // m01 (shearX) = eM12 (Horizontal proportionality constant)
        double m01 = leis.readFloat();

        // m10 (shearY) = eM21 (Vertical proportionality constant)
        double m10 = leis.readFloat();

        // m11 (scaleY) = eM22 (Vertical scaling component)
        double m11 = leis.readFloat();

        // m02 (translateX) = eDx (The horizontal translation component, in logical units.)
        double m02 = leis.readFloat();

        // m12 (translateY) = eDy (The vertical translation component, in logical units.)
        double m12 = leis.readFloat();

        // TODO: not sure, why the shearing has to be inverted here,
        // probably because of the different world/user space transformation
        xform.setTransform(m00, -m10, -m01, m11, m02, m12);

        if (xform.isIdentity()) {
            xform.setToIdentity();
        }

        return 6 * LittleEndianConsts.INT_SIZE;
    }

    static Shape getRgnShape(List<Rectangle2D> rgnRects) {
        if (rgnRects.size() == 1) {
            return rgnRects.get(0);
        }
        final Area frame = new Area();
        rgnRects.forEach((rct) -> frame.add(new Area(rct)));
        return frame;
    }
}
