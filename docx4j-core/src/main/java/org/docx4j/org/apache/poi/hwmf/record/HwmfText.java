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
import static org.docx4j.org.apache.poi.hwmf.record.HwmfDraw.readRectS;
import static org.docx4j.org.apache.poi.util.GenericRecordUtil.getBitsAsString;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfDrawProperties;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfGraphics;
import org.docx4j.org.apache.poi.hwmf.record.HwmfMisc.WmfSetMapMode;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfCharsetAware;
import org.docx4j.org.apache.poi.util.BitField;
import org.docx4j.org.apache.poi.util.BitFieldFactory;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;
import org.docx4j.org.apache.poi.util.LocaleUtil;

public class HwmfText {
    private static final Logger LOG = LoggerFactory.getLogger(HwmfText.class);
    private static final int DEFAULT_MAX_RECORD_LENGTH = 1_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    /**
     * @param length the max record length allowed for HwmfText
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for HwmfText
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    /**
     * The META_SETTEXTCHAREXTRA record defines inter-character spacing for text justification in the
     * playback device context. Spacing is added to the white space between each character, including
     * break characters, when a line of justified text is output.
     */
    public static class WmfSetTextCharExtra implements HwmfRecord {

        /**
         * A 16-bit unsigned integer that defines the amount of extra space, in
         * logical units, to be added to each character. If the current mapping mode is not MM_TEXT,
         * this value is transformed and rounded to the nearest pixel. For details about setting the
         * mapping mode, see META_SETMAPMODE
         */
        private int charExtra;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setTextCharExtra;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            charExtra = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("charExtra", () -> charExtra);
        }
    }

    /**
     * The META_SETTEXTCOLOR record defines the text foreground color in the playback device context.
     */
    public static class WmfSetTextColor implements HwmfRecord {

        protected final HwmfColorRef colorRef = new HwmfColorRef();

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setTextColor;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return colorRef.init(leis);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.getProperties().setTextColor(colorRef);
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
     * The META_SETTEXTJUSTIFICATION record defines the amount of space to add to break characters
     * in a string of justified text.
     */
    public static class WmfSetTextJustification implements HwmfRecord {

        /**
         * A 16-bit unsigned integer that specifies the number of space characters in the line.
         */
        private int breakCount;

        /**
         * A 16-bit unsigned integer that specifies the total extra space, in logical
         * units, to be added to the line of text. If the current mapping mode is not MM_TEXT, the value
         * identified by the BreakExtra member is transformed and rounded to the nearest pixel. For
         * details about setting the mapping mode, see {@link WmfSetMapMode}.
         */
        private int breakExtra;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setTextJustification;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            breakCount = leis.readUShort();
            breakExtra = leis.readUShort();
            return 2*LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {

        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "breakCount", () -> breakCount,
                "breakExtra", () -> breakExtra
            );
        }
    }

    /**
     * The META_TEXTOUT record outputs a character string at the specified location by using the font,
     * background color, and text color that are defined in the playback device context.
     */
    public static class WmfTextOut implements HwmfRecord, HwmfCharsetAware {
        /**
         * A 16-bit signed integer that defines the length of the string, in bytes, pointed to by String.
         */
        private int stringLength;
        /**
         * The size of this field MUST be a multiple of two. If StringLength is an odd
         * number, then this field MUST be of a size greater than or equal to StringLength + 1.
         * A variable-length string that specifies the text to be drawn.
         * The string does not need to be null-terminated, because StringLength specifies the
         * length of the string.
         * The string is written at the location specified by the XStart and YStart fields.
         */
        private byte[] rawTextBytes;

        protected Point2D reference = new Point2D.Double();

        protected Supplier<Charset> charsetProvider = () -> LocaleUtil.CHARSET_1252;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.textOut;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            stringLength = leis.readShort();
            rawTextBytes = IOUtils.safelyAllocate(stringLength+(long)(stringLength&1), MAX_RECORD_LENGTH);
            leis.readFully(rawTextBytes);
            // A 16-bit signed integer that defines the vertical (y-axis) coordinate, in logical
            // units, of the point where drawing is to start.
            int yStart = leis.readShort();
            // A 16-bit signed integer that defines the horizontal (x-axis) coordinate, in
            // logical units, of the point where drawing is to start.
            int xStart = leis.readShort();
            reference.setLocation(xStart, yStart);
            return 3*LittleEndianConsts.SHORT_SIZE+rawTextBytes.length;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.setCharsetProvider(charsetProvider);
            ctx.drawString(getTextBytes(), stringLength, reference);
        }

        public String getText(Charset charset) {
            return new String(getTextBytes(), charset);
        }

        /**
         *
         * @return a copy of a trimmed byte array of rawTextBytes bytes.
         * This includes only the bytes from 0..stringLength.
         * This does not include the extra optional padding on the byte array.
         */
        private byte[] getTextBytes() {
            return IOUtils.safelyClone(rawTextBytes, 0, stringLength, MAX_RECORD_LENGTH);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "text", () -> getText(charsetProvider.get()),
                "reference", () -> reference
            );
        }

        @Override
        public void setCharsetProvider(Supplier<Charset> provider) {
            charsetProvider = provider;
        }
    }

    @SuppressWarnings("unused")
    public static class WmfExtTextOutOptions implements GenericRecord {
        /**
         * Indicates that the background color that is defined in the playback device context
         * SHOULD be used to fill the rectangle.
         */
        private static final BitField ETO_OPAQUE = BitFieldFactory.getInstance(0x0002);

        /**
         * Indicates that the text SHOULD be clipped to the rectangle.
         */
        private static final BitField ETO_CLIPPED = BitFieldFactory.getInstance(0x0004);

        /**
         * Indicates that the string to be output SHOULD NOT require further processing
         * with respect to the placement of the characters, and an array of character
         * placement values SHOULD be provided. This character placement process is
         * useful for fonts in which diacritical characters affect character spacing.
         */
        private static final BitField ETO_GLYPH_INDEX = BitFieldFactory.getInstance(0x0010);

        /**
         * Indicates that the text MUST be laid out in right-to-left reading order, instead of
         * the default left-to-right order. This SHOULD be applied only when the font that is
         * defined in the playback device context is either Hebrew or Arabic.
         */
        private static final BitField ETO_RTLREADING = BitFieldFactory.getInstance(0x0080);

        /**
         * This bit indicates that the record does not specify a bounding rectangle for the
         * text output.
         */
        private static final BitField ETO_NO_RECT = BitFieldFactory.getInstance(0x0100);

        /**
         * This bit indicates that the codes for characters in an output text string are 8 bits,
         * derived from the low bytes of 16-bit Unicode UTF16-LE character codes, in which
         * the high byte is assumed to be 0.
         */
        private static final BitField ETO_SMALL_CHARS = BitFieldFactory.getInstance(0x0200);

        /**
         * Indicates that to display numbers, digits appropriate to the locale SHOULD be used.
         */
        private static final BitField ETO_NUMERICSLOCAL = BitFieldFactory.getInstance(0x0400);

        /**
         * Indicates that to display numbers, European digits SHOULD be used.
         */
        private static final BitField ETO_NUMERICSLATIN = BitFieldFactory.getInstance(0x0800);

        /**
         * This bit indicates that no special operating system processing for glyph placement
         * should be performed on right-to-left strings; that is, all glyph positioning
         * SHOULD be taken care of by drawing and state records in the metafile
         */
        private static final BitField ETO_IGNORELANGUAGE = BitFieldFactory.getInstance(0x1000);

        /**
         * Indicates that both horizontal and vertical character displacement values
         * SHOULD be provided.
         */
        private static final BitField ETO_PDY = BitFieldFactory.getInstance(0x2000);

        /** This bit is reserved and SHOULD NOT be used. */
        private static final BitField ETO_REVERSE_INDEX_MAP = BitFieldFactory.getInstance(0x10000);

        private static final int[] FLAGS_MASKS = {
            0x0002, 0x0004, 0x0010, 0x0080, 0x0100, 0x0200, 0x0400, 0x0800, 0x1000, 0x2000, 0x10000
        };

        private static final String[] FLAGS_NAMES = {
            "OPAQUE", "CLIPPED", "GLYPH_INDEX", "RTLREADING", "NO_RECT", "SMALL_CHARS", "NUMERICSLOCAL",
            "NUMERICSLATIN", "IGNORELANGUAGE", "PDY", "REVERSE_INDEX_MAP"
        };

        protected int flags;

        public int init(LittleEndianInputStream leis) {
            flags = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        public boolean isOpaque() {
            return ETO_OPAQUE.isSet(flags);
        }

        public boolean isClipped() {
            return ETO_CLIPPED.isSet(flags);
        }

        public boolean isYDisplaced() {
            return ETO_PDY.isSet(flags);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("flags", getBitsAsString(() -> flags, FLAGS_MASKS, FLAGS_NAMES));
        }
    }

    /**
     * The META_EXTTEXTOUT record outputs text by using the font, background color, and text color that
     * are defined in the playback device context. Optionally, dimensions can be provided for clipping,
     * opaquing, or both.
     */
    public static class WmfExtTextOut implements HwmfRecord, HwmfCharsetAware {
        /**
         * The location, in logical units, where the text string is to be placed.
         */
        protected final Point2D reference = new Point2D.Double();

        /**
         * A 16-bit signed integer that defines the length of the string.
         */
        protected int stringLength;
        /**
         * A 16-bit unsigned integer that defines the use of the application-defined
         * rectangle. This member can be a combination of one or more values in the
         * ExtTextOutOptions Flags (ETO_*)
         */
        protected final WmfExtTextOutOptions options;
        /**
         * An optional 8-byte Rect Object (section 2.2.2.18) that defines the
         * dimensions, in logical coordinates, of a rectangle that is used for clipping, opaquing, or both.
         *
         * The corners are given in the order left, top, right, bottom.
         * Each value is a 16-bit signed integer that defines the coordinate, in logical coordinates, of
         * the upper-left corner of the rectangle
         */
        protected final Rectangle2D bounds = new Rectangle2D.Double();
        /**
         * A variable-length string that specifies the text to be drawn. The string does
         * not need to be null-terminated, because StringLength specifies the length of the string. If
         * the length is odd, an extra byte is placed after it so that the following member (optional Dx) is
         * aligned on a 16-bit boundary.
         */
        protected byte[] rawTextBytes;
        /**
         * An optional array of 16-bit signed integers that indicate the distance between
         * origins of adjacent character cells. For example, Dx[i] logical units separate the origins of
         * character cell i and character cell i + 1. If this field is present, there MUST be the same
         * number of values as there are characters in the string.
         */
        protected final List<Integer> dx = new ArrayList<>();

        protected Supplier<Charset> charsetProvider = () -> LocaleUtil.CHARSET_1252;

        public WmfExtTextOut() {
            this(new WmfExtTextOutOptions());
        }

        protected WmfExtTextOut(WmfExtTextOutOptions options) {
            this.options = options;
        }

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.extTextOut;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            // -6 bytes of record function and length header
            final int remainingRecordSize = (int)(recordSize-6);

            int size = readPointS(leis, reference);

            stringLength = leis.readShort();
            size += LittleEndianConsts.SHORT_SIZE;
            size += options.init(leis);

            // Check if we have a rectangle
            if ((options.isOpaque() || options.isClipped()) && size+8<=remainingRecordSize) {
                // the bounding rectangle is optional and only read when options are given
                size += readRectS(leis, bounds);
            }

            rawTextBytes = IOUtils.safelyAllocate(stringLength+(long)(stringLength&1), MAX_RECORD_LENGTH);
            leis.readFully(rawTextBytes);
            size += rawTextBytes.length;

            if (size >= remainingRecordSize) {
                LOG.atInfo().log("META_EXTTEXTOUT doesn't contain character tracking info");
                return size;
            }

            int dxLen = Math.min(stringLength, (remainingRecordSize-size)/LittleEndianConsts.SHORT_SIZE);
            if (dxLen < stringLength) {
                LOG.atWarn().log("META_EXTTEXTOUT tracking info doesn't cover all characters");
            }

            for (int i=0; i<dxLen; i++) {
                dx.add((int)leis.readShort());
                size += LittleEndianConsts.SHORT_SIZE;
            }

            return size;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.setCharsetProvider(charsetProvider);
            ctx.drawString(rawTextBytes, stringLength, reference, null, bounds, options, dx, false);
        }

        public String getText(Charset charset) throws IOException {
            if (rawTextBytes == null) {
                return "";
            }
            String ret = new String(rawTextBytes, charset);
            return ret.substring(0, Math.min(ret.length(), stringLength));
        }

        public Point2D getReference() {
            return reference;
        }

        public Rectangle2D getBounds() {
            return bounds;
        }

        public WmfExtTextOutOptions getOptions() {
            return options;
        }

        protected boolean isUnicode() {
            return false;
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        private String getGenericText() {
            try {
                return getText(isUnicode() ? StandardCharsets.UTF_16LE : charsetProvider.get());
            } catch (IOException e) {
                return "";
            }
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "reference", this::getReference,
                "bounds", this::getBounds,
                "options", this::getOptions,
                "text", this::getGenericText,
                "dx", () -> dx
            );
        }

        @Override
        public void setCharsetProvider(Supplier<Charset> provider) {
            charsetProvider = provider;
        }
    }

    public enum HwmfTextAlignment {
        LEFT,
        RIGHT,
        CENTER
    }

    public enum HwmfTextVerticalAlignment {
        TOP,
        BOTTOM,
        BASELINE
    }

    /**
     * The META_SETTEXTALIGN record defines text-alignment values in the playback device context.
     */
    public static class WmfSetTextAlign implements HwmfRecord {

        /**
         * The drawing position in the playback device context MUST be updated after each text
         * output call. It MUST be used as the reference point.<p>
         *
         * If the flag is not set, the option TA_NOUPDATECP is active, i.e. the drawing position
         * in the playback device context MUST NOT be updated after each text output call.
         * The reference point MUST be passed to the text output function.
         */
        @SuppressWarnings("unused")
        private static final BitField TA_UPDATECP = BitFieldFactory.getInstance(0x0001);

        /**
         * The text MUST be laid out in right-to-left reading order, instead of the default
         * left-to-right order. This SHOULD be applied only when the font that is defined in the
         * playback device context is either Hebrew or Arabic.
         */
        @SuppressWarnings("unused")
        private static final BitField TA_RTLREADING = BitFieldFactory.getInstance(0x0100);


        private static final BitField ALIGN_MASK = BitFieldFactory.getInstance(0x0006);

        /**
         * Flag TA_LEFT (0x0000):
         * The reference point MUST be on the left edge of the bounding rectangle,
         * if all bits of the align mask (latin mode) are unset.
         *
         * Flag VTA_TOP (0x0000):
         * The reference point MUST be on the top edge of the bounding rectangle,
         * if all bits of the valign mask are unset.
         */
        private static final int ALIGN_LEFT = 0;

        /**
         * Flag TA_RIGHT (0x0002):
         * The reference point MUST be on the right edge of the bounding rectangle.
         *
         * Flag VTA_BOTTOM (0x0002):
         * The reference point MUST be on the bottom edge of the bounding rectangle.
         */
        private static final int ALIGN_RIGHT = 1;

        /**
         * Flag TA_CENTER (0x0006) / VTA_CENTER (0x0006):
         * The reference point MUST be aligned horizontally with the center of the bounding
         * rectangle.
         */
        private static final int ALIGN_CENTER = 3;

        private static final BitField VALIGN_MASK = BitFieldFactory.getInstance(0x0018);

        /**
         * Flag TA_TOP (0x0000):
         * The reference point MUST be on the top edge of the bounding rectangle,
         * if all bits of the valign mask are unset.
         *
         * Flag VTA_RIGHT (0x0000):
         * The reference point MUST be on the right edge of the bounding rectangle,
         * if all bits of the align mask (asian mode) are unset.
         */
        private static final int VALIGN_TOP = 0;

        /**
         * Flag TA_BOTTOM (0x0008):
         * The reference point MUST be on the bottom edge of the bounding rectangle.
         *
         * Flag VTA_LEFT (0x0008):
         * The reference point MUST be on the left edge of the bounding rectangle.
         */
        private static final int VALIGN_BOTTOM = 1;

        /**
         * Flag TA_BASELINE (0x0018) / VTA_BASELINE (0x0018):
         * The reference point MUST be on the baseline of the text.
         */
        private static final int VALIGN_BASELINE = 3;

        /**
         * A 16-bit unsigned integer that defines text alignment.
         * This value MUST be a combination of one or more TextAlignmentMode Flags
         * for text with a horizontal baseline, and VerticalTextAlignmentMode Flags
         * for text with a vertical baseline.
         */
        protected int textAlignmentMode;

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.setTextAlign;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            textAlignmentMode = leis.readUShort();
            return LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            HwmfDrawProperties props = ctx.getProperties();
            props.setTextAlignLatin(getAlignLatin());
            props.setTextVAlignLatin(getVAlignLatin());
            props.setTextAlignAsian(getAlignAsian());
            props.setTextVAlignAsian(getVAlignAsian());
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "align", this::getAlignLatin,
                "valign", this::getVAlignLatin,
                "alignAsian", this::getAlignAsian,
                "valignAsian", this::getVAlignAsian
            );
        }

        private HwmfTextAlignment getAlignLatin() {
            switch (ALIGN_MASK.getValue(textAlignmentMode)) {
                default:
                case ALIGN_LEFT:
                    return HwmfTextAlignment.LEFT;
                case ALIGN_CENTER:
                    return HwmfTextAlignment.CENTER;
                case ALIGN_RIGHT:
                    return HwmfTextAlignment.RIGHT;
            }
        }

        private HwmfTextVerticalAlignment getVAlignLatin() {
            switch (VALIGN_MASK.getValue(textAlignmentMode)) {
                default:
                case VALIGN_TOP:
                    return HwmfTextVerticalAlignment.TOP;
                case VALIGN_BASELINE:
                    return HwmfTextVerticalAlignment.BASELINE;
                case VALIGN_BOTTOM:
                    return HwmfTextVerticalAlignment.BOTTOM;
            }
        }

        private HwmfTextAlignment getAlignAsian() {
            switch (getVAlignLatin()) {
                default:
                case TOP:
                    return HwmfTextAlignment.RIGHT;
                case BASELINE:
                    return HwmfTextAlignment.CENTER;
                case BOTTOM:
                    return HwmfTextAlignment.LEFT;
            }
        }

        private HwmfTextVerticalAlignment getVAlignAsian() {
            switch (getAlignLatin()) {
                default:
                case LEFT:
                    return HwmfTextVerticalAlignment.TOP;
                case CENTER:
                    return HwmfTextVerticalAlignment.BASELINE;
                case RIGHT:
                    return HwmfTextVerticalAlignment.BOTTOM;
            }
        }
    }

    public static class WmfCreateFontIndirect implements HwmfRecord, HwmfObjectTableEntry {
        protected final HwmfFont font;

        public WmfCreateFontIndirect() {
            this(new HwmfFont());
        }

        protected WmfCreateFontIndirect(HwmfFont font) {
            this.font = font;
        }

        @Override
        public HwmfRecordType getWmfRecordType() {
            return HwmfRecordType.createFontIndirect;
        }

        @Override
        public int init(LittleEndianInputStream leis, long recordSize, int recordFunction) throws IOException {
            return font.init(leis, recordSize);
        }

        @Override
        public void draw(HwmfGraphics ctx) {
            ctx.addObjectTableEntry(this);
        }

        @Override
        public void applyObject(HwmfGraphics ctx) {
            ctx.getProperties().setFont(font);
        }

        public HwmfFont getFont() {
            return font;
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("font", this::getFont);
        }
    }
}
