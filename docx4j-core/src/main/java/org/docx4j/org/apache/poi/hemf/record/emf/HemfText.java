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

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static org.docx4j.org.apache.poi.hemf.record.emf.HemfDraw.readDimensionFloat;
import static org.docx4j.org.apache.poi.hemf.record.emf.HemfDraw.readPointL;
import static org.docx4j.org.apache.poi.hemf.record.emf.HemfDraw.readRectL;
import static org.docx4j.org.apache.poi.hemf.record.emf.HemfRecordIterator.HEADER_SIZE;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfGraphics;
import org.docx4j.org.apache.poi.hwmf.record.HwmfText;
import org.docx4j.org.apache.poi.hwmf.record.HwmfText.WmfSetTextAlign;
import org.docx4j.org.apache.poi.util.Dimension2DDouble;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;
import org.docx4j.org.apache.poi.util.RecordFormatException;

/**
 * Container class to gather all text-related commands
 * This is starting out as read only, and very little is actually
 * implemented at this point!
 */
@Internal
@SuppressWarnings("WeakerAccess")
public class HemfText {

    private static final int DEFAULT_MAX_RECORD_LENGTH = 1_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    /**
     * @param length the max record length allowed for HemfText
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for HemfText
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    public enum EmfGraphicsMode {
        GM_COMPATIBLE, GM_ADVANCED
    }

    public static class EmfExtTextOutA extends HwmfText.WmfExtTextOut implements HemfRecord {

        protected Rectangle2D boundsIgnored = new Rectangle2D.Double();
        protected EmfGraphicsMode graphicsMode;

        /**
         * The scale factor to apply along the X/Y axis to convert from page space units to .01mm units.
         * This SHOULD be used only if the graphics mode specified by iGraphicsMode is GM_COMPATIBLE.
         */
        protected final Dimension2D scale = new Dimension2DDouble();

        public EmfExtTextOutA() {
            super(new EmfExtTextOutOptions());
        }

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.extTextOutA;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            if (recordSize < 0 || Integer.MAX_VALUE <= recordSize) {
                throw new RecordFormatException("recordSize must be a positive integer (0-0x7FFFFFFF)");
            }

            // A WMF RectL object. It is not used and MUST be ignored on receipt.
            long size = readRectL(leis, boundsIgnored);

            // A 32-bit unsigned integer that specifies the graphics mode from the GraphicsMode enumeration
            graphicsMode = EmfGraphicsMode.values()[leis.readInt()-1];
            size += LittleEndianConsts.INT_SIZE;

            size += readDimensionFloat(leis, scale);

            // A WMF PointL object that specifies the coordinates of the reference point used to position the string.
            // The reference point is defined by the last EMR_SETTEXTALIGN record.
            // If no such record has been set, the defaulint alignment is TA_LEFT,TA_TOP.
            size += readPointL(leis, reference);
            // A 32-bit unsigned integer that specifies the number of characters in the string.
            stringLength = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the offset to the output string, in bytes,
            // from the start of the record in which this object is contained.
            // This value MUST be 8- or 16-bit aligned, according to the character format.
            int offString = (int)leis.readUInt();
            size += 2*LittleEndianConsts.INT_SIZE;

            size += options.init(leis);
            // An optional WMF RectL object that defines a clipping and/or opaquing rectangle in logical units.
            // This rectangle is applied to the text output performed by the containing record.
            if (options.isClipped() || options.isOpaque()) {
                size += readRectL(leis, bounds);
            }

            // A 32-bit unsigned integer that specifies the offset to an intercharacter spacing array, in bytes,
            // from the start of the record in which this object is contained. This value MUST be 32-bit aligned.
            int offDx = (int)leis.readUInt();
            size += LittleEndianConsts.INT_SIZE;

            // handle dx before string and other way round
            final String order = (offDx < offString) ? "ds" : "sd";
            // the next byte index after the string ends
            int strEnd = (int)((offDx <= HEADER_SIZE) ? recordSize : offDx-HEADER_SIZE);
            for (char op : order.toCharArray()) {
                switch (op) {
                    case 'd': {
                        dx.clear();
                        int undefinedSpace2 = (int) (offDx - (size + HEADER_SIZE));
                        if (offDx > 0 && undefinedSpace2 >= 0 && offDx-HEADER_SIZE < recordSize) {
                            leis.skipFully(undefinedSpace2);
                            size += undefinedSpace2;

                            // An array of 32-bit unsigned integers that specify the output spacing between the origins of adjacent
                            // character cells in logical units. The location of this field is specified by the value of offDx
                            // in bytes from the start of this record. If spacing is defined, this field contains the same number
                            // of values as characters in the output string.
                            //
                            // If the Options field of the EmrText object contains the ETO_PDY flag, then this buffer
                            // contains twice as many values as there are characters in the output string, one
                            // horizontal and one vertical offset for each, in that order.
                            //
                            // If ETO_RTLREADING is specified, characters are laid right to left instead of left to right.
                            // No other options affect the interpretation of this field.
                            final int maxSize = (int)Math.min((offDx < offString) ? (offString-HEADER_SIZE) : recordSize, recordSize);
                            while (size <= maxSize-LittleEndianConsts.INT_SIZE) {
                                dx.add((int) leis.readUInt());
                                size += LittleEndianConsts.INT_SIZE;
                            }
                        }
                        if (dx.size() < stringLength) {
                            // invalid dx array
                            dx.clear();
                        }
                        strEnd = Math.toIntExact(recordSize);
                        break;
                    }
                    default:
                    case 's': {
                        int undefinedSpace1 = Math.toIntExact(offString - (size + HEADER_SIZE));
                        if (offString > 0 && undefinedSpace1 >= 0 && offString-HEADER_SIZE < recordSize) {
                            leis.skipFully(undefinedSpace1);
                            size += undefinedSpace1;

                            // read all available bytes and not just "stringLength * 1(ansi)/2(unicode)"
                            // in case we need to deal with surrogate pairs
                            final int maxSize = Math.toIntExact(Math.min(recordSize, strEnd)-size);
                            rawTextBytes = IOUtils.safelyAllocate(maxSize, MAX_RECORD_LENGTH);
                            leis.readFully(rawTextBytes);
                            size += maxSize;
                            break;
                        }
                    }
                }
            }

            return size;
        }

        /**
         *
         * To be implemented!  We need to get the current character set
         * from the current font for {@link EmfExtTextOutA},
         * which has to be tracked in the playback device.
         *
         * For {@link EmfExtTextOutW}, the charset is "UTF-16LE"
         *
         * @param charset the charset to be used to decode the character bytes
         * @return text from this text element
         * @throws IOException if the charset is not compatible to the underlying bytes
         */
        public String getText(Charset charset) throws IOException {
            return super.getText(charset);
        }

        public EmfGraphicsMode getGraphicsMode() {
            return graphicsMode;
        }

        public Dimension2D getScale() {
            return scale;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            // A 32-bit floating-point value that specifies the scale factor to apply along
            // the axis to convert from page space units to .01mm units.
            // This SHOULD be used only if the graphics mode specified by iGraphicsMode is GM_COMPATIBLE.
            Dimension2D scl = graphicsMode == EmfGraphicsMode.GM_COMPATIBLE ? scale : null;
            ctx.setCharsetProvider(charsetProvider);
            ctx.drawString(rawTextBytes, stringLength, reference, scl, bounds, options, dx, isUnicode());
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "base", super::getGenericProperties,
                "boundsIgnored", () -> boundsIgnored,
                "graphicsMode", this::getGraphicsMode,
                "scale", this::getScale
            );
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    public static class EmfExtTextOutW extends EmfExtTextOutA {

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.extTextOutW;
        }

        public String getText() throws IOException {
            return getText(UTF_16LE);
        }

        protected boolean isUnicode() {
            return true;
        }
    }

    /**
     * The EMR_SETTEXTALIGN record specifies text alignment.
     */
    public static class EmfSetTextAlign extends WmfSetTextAlign implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setTextAlign;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer that specifies text alignment by using a mask of text alignment flags.
            // These are either WMF TextAlignmentMode Flags for text with a horizontal baseline,
            // or WMF VerticalTextAlignmentMode Flags for text with a vertical baseline.
            // Only one value can be chosen from those that affect horizontal and vertical alignment.
            textAlignmentMode = (int)leis.readUInt();
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_SETTEXTCOLOR record defines the current text color.
     */
    public static class EmfSetTextColor extends HwmfText.WmfSetTextColor implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setTextColor;
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



    public static class EmfExtCreateFontIndirectW extends HwmfText.WmfCreateFontIndirect
    implements HemfRecord {
        int fontIdx;

        public EmfExtCreateFontIndirectW() {
            super(new HemfFont());
        }

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.extCreateFontIndirectW;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer that specifies the index of the logical font object
            // in the EMF Object Table
            fontIdx = (int)leis.readUInt();
            long size = font.init(leis, (int)(recordSize-LittleEndianConsts.INT_SIZE));
            return size+LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.addObjectTableEntry(this, fontIdx);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public int getFontIdx() {
            return fontIdx;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "base", super::getGenericProperties,
                "fontIdx", this::getFontIdx
            );
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    public static class EmfExtTextOutOptions extends HwmfText.WmfExtTextOutOptions {
        @Override
        public int init(LittleEndianInputStream leis) {
            // A 32-bit unsigned integer that specifies how to use the rectangle specified in the Rectangle field.
            // This field can be a combination of more than one ExtTextOutOptions enumeration
            flags = (int)leis.readUInt();
            return LittleEndianConsts.INT_SIZE;
        }
    }

    public static class SetTextJustification extends UnimplementedHemfRecord {

    }

    /**
     * Needs to be implemented.  Couldn't find example.
     */
    public static class PolyTextOutA extends UnimplementedHemfRecord {

    }

    /**
     * Needs to be implemented.  Couldn't find example.
     */
    public static class PolyTextOutW extends UnimplementedHemfRecord {

    }

}
