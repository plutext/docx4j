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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.common.usermodel.fonts.FontCharset;
import org.docx4j.org.apache.poi.hwmf.record.HwmfFont;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

public class HemfFont extends HwmfFont {
    private static final int LOGFONT_SIZE = 92;
    private static final int LOGFONTPANOSE_SIZE = 320;

    protected interface LogFontDetails {}

    protected static class LogFontExDv implements LogFontDetails {
        protected int[] designVector;

        @Override
        public String toString() {
            return "{ designVectorLen: " + (designVector == null ? 0 : designVector.length) + " }";
        }
    }

    protected static class LogFontPanose implements LogFontDetails, GenericRecord {
        enum FamilyType {
            PAN_ANY,
            PAN_NO_FIT,
            PAN_FAMILY_TEXT_DISPLAY,
            PAN_FAMILY_SCRIPT,
            PAN_FAMILY_DECORATIVE,
            PAN_FAMILY_PICTORIAL
        }

        enum SerifType {
            PAN_ANY,
            PAN_NO_FIT,
            PAN_SERIF_COVE,
            PAN_SERIF_OBTUSE_COVE,
            PAN_SERIF_SQUARE_COVE,
            PAN_SERIF_OBTUSE_SQUARE_COVE,
            PAN_SERIF_SQUARE,
            PAN_SERIF_THIN,
            PAN_SERIF_BONE,
            PAN_SERIF_EXAGGERATED,
            PAN_SERIF_TRIANGLE,
            PAN_SERIF_NORMAL_SANS,
            PAN_SERIF_OBTUSE_SANS,
            PAN_SERIF_PERP_SANS,
            PAN_SERIF_FLARED,
            PAN_SERIF_ROUNDED
        }

        enum FontWeight {
            PAN_ANY,
            PAN_NO_FIT,
            PAN_WEIGHT_VERY_LIGHT,
            PAN_WEIGHT_LIGHT,
            PAN_WEIGHT_THIN,
            PAN_WEIGHT_BOOK,
            PAN_WEIGHT_MEDIUM,
            PAN_WEIGHT_DEMI,
            PAN_WEIGHT_BOLD,
            PAN_WEIGHT_HEAVY,
            PAN_WEIGHT_BLACK,
            PAN_WEIGHT_NORD
        }

        enum Proportion {
            PAN_ANY,
            PAN_NO_FIT,
            PAN_PROP_OLD_STYLE,
            PAN_PROP_MODERN,
            PAN_PROP_EVEN_WIDTH,
            PAN_PROP_EXPANDED,
            PAN_PROP_CONDENSED,
            PAN_PROP_VERY_EXPANDED,
            PAN_PROP_VERY_CONDENSED,
            PAN_PROP_MONOSPACED
        }

        enum Contrast {
            PAN_ANY,
            PAN_NO_FIT,
            PAN_CONTRAST_NONE,
            PAN_CONTRAST_VERY_LOW,
            PAN_CONTRAST_LOW,
            PAN_CONTRAST_MEDIUM_LOW,
            PAN_CONTRAST_MEDIUM,
            PAN_CONTRAST_MEDIUM_HIGH,
            PAN_CONTRAST_HIGH,
            PAN_CONTRAST_VERY_HIGH
        }

        enum StrokeVariation {
            PAN_ANY,
            PAN_NO_FIT,
            PAN_STROKE_GRADUAL_DIAG,
            PAN_STROKE_GRADUAL_TRAN,
            PAN_STROKE_GRADUAL_VERT,
            PAN_STROKE_GRADUAL_HORZ,
            PAN_STROKE_RAPID_VERT,
            PAN_STROKE_RAPID_HORZ,
            PAN_STROKE_INSTANT_VERT
        }

        enum ArmStyle {
            PAN_ANY,
            PAN_NO_FIT,
            PAN_STRAIGHT_ARMS_HORZ,
            PAN_STRAIGHT_ARMS_WEDGE,
            PAN_STRAIGHT_ARMS_VERT,
            PAN_STRAIGHT_ARMS_SINGLE_SERIF,
            PAN_STRAIGHT_ARMS_DOUBLE_SERIF,
            PAN_BENT_ARMS_HORZ,
            PAN_BENT_ARMS_WEDGE,
            PAN_BENT_ARMS_VERT,
            PAN_BENT_ARMS_SINGLE_SERIF,
            PAN_BENT_ARMS_DOUBLE_SERIF
        }

        enum Letterform {
            PAN_ANY,
            PAN_NO_FIT,
            PAN_LETT_NORMAL_CONTACT,
            PAN_LETT_NORMAL_WEIGHTED,
            PAN_LETT_NORMAL_BOXED,
            PAN_LETT_NORMAL_FLATTENED,
            PAN_LETT_NORMAL_ROUNDED,
            PAN_LETT_NORMAL_OFF_CENTER,
            PAN_LETT_NORMAL_SQUARE,
            PAN_LETT_OBLIQUE_CONTACT,
            PAN_LETT_OBLIQUE_WEIGHTED,
            PAN_LETT_OBLIQUE_BOXED,
            PAN_LETT_OBLIQUE_FLATTENED,
            PAN_LETT_OBLIQUE_ROUNDED,
            PAN_LETT_OBLIQUE_OFF_CENTER,
            PAN_LETT_OBLIQUE_SQUARE
        }

        enum MidLine {
            PAN_ANY,
            PAN_NO_FIT,
            PAN_MIDLINE_STANDARD_TRIMMED,
            PAN_MIDLINE_STANDARD_POINTED,
            PAN_MIDLINE_STANDARD_SERIFED,
            PAN_MIDLINE_HIGH_TRIMMED,
            PAN_MIDLINE_HIGH_POINTED,
            PAN_MIDLINE_HIGH_SERIFED,
            PAN_MIDLINE_CONSTANT_TRIMMED,
            PAN_MIDLINE_CONSTANT_POINTED,
            PAN_MIDLINE_CONSTANT_SERIFED,
            PAN_MIDLINE_LOW_TRIMMED,
            PAN_MIDLINE_LOW_POINTED,
            PAN_MIDLINE_LOW_SERIFED
        }

        enum XHeight {
            PAN_ANY,
            PAN_NO_FIT,
            PAN_XHEIGHT_CONSTANT_SMALL,
            PAN_XHEIGHT_CONSTANT_STD,
            PAN_XHEIGHT_CONSTANT_LARGE,
            PAN_XHEIGHT_DUCKING_SMALL,
            PAN_XHEIGHT_DUCKING_STD,
            PAN_XHEIGHT_DUCKING_LARGE
        }

        protected int styleSize;
        protected int vendorId;
        protected int culture;
        protected FamilyType familyType;
        protected SerifType serifStyle;
        protected FontWeight weight;
        protected Proportion proportion;
        protected Contrast contrast;
        protected StrokeVariation strokeVariation;
        protected ArmStyle armStyle;
        protected Letterform letterform;
        protected MidLine midLine;
        protected XHeight xHeight;

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            final Map<String,Supplier<?>> m = new LinkedHashMap<>();
            m.put("styleSize", () -> styleSize);
            m.put("vendorId", () -> vendorId);
            m.put("culture", () -> culture);
            m.put("familyType", () -> familyType);
            m.put("serifStyle", () -> serifStyle);
            m.put("weight", () -> weight);
            m.put("proportion", () -> proportion);
            m.put("contrast", () -> contrast);
            m.put("strokeVariation", () -> strokeVariation);
            m.put("armStyle", () -> armStyle);
            m.put("letterform", () -> letterform);
            m.put("midLine", () -> midLine);
            m.put("xHeight", () -> xHeight);
            return Collections.unmodifiableMap(m);
        }
    }

    protected String fullname;
    protected String style;
    protected String script;

    protected LogFontDetails details;

    @SuppressWarnings("unused")
    @Override
    public int init(LittleEndianInputStream leis, long recordSize) throws IOException {
        // A 32-bit signed integer that specifies the height, in logical units, of the font's
        // character cell or character. The character height value, also known as the em size, is the
        // character cell height value minus the internal leading value. The font mapper SHOULD
        // interpret the value specified in the Height field in the following manner.
        //
        // 0x00000000 < value:
        // The font mapper transforms this value into device units and matches it against
        // the cell height of the available fonts.
        //
        // 0x00000000
        // The font mapper uses a default height value when it searches for a match.
        //
        // value < 0x00000000:
        // The font mapper transforms this value into device units and matches its
        // absolute value against the character height of the available fonts.
        //
        // For all height comparisons, the font mapper SHOULD look for the largest font that does not
        // exceed the requested size.
        height = leis.readInt();

        // A 32-bit signed integer that specifies the average width, in logical units, of
        // characters in the font. If the Width field value is zero, an appropriate value SHOULD be
        // calculated from other LogFont values to find a font that has the typographer's intended
        // aspect ratio.
        width = leis.readInt();

        // A 32-bit signed integer that specifies the angle, in tenths of degrees,
        // between the escapement vector and the x-axis of the device. The escapement vector is
        // parallel to the baseline of a row of text.
        //
        // When the graphics mode is set to GM_ADVANCED, the escapement angle of the string can
        // be specified independently of the orientation angle of the string's characters.
        escapement = leis.readInt();

        // A 32-bit signed integer that specifies the angle, in tenths of degrees,
        // between each character's baseline and the x-axis of the device.
        orientation = leis.readInt();

        // A 32-bit signed integer that specifies the weight of the font in the range zero through 1000.
        // For example, 400 is normal and 700 is bold. If this value is zero, a default weight can be used.
        weight = leis.readInt();

        // An 8-bit unsigned integer that specifies an italic font if set to 0x01;
        // otherwise, it MUST be set to 0x00.
        italic = (leis.readUByte() != 0x00);

        // An 8-bit unsigned integer that specifies an underlined font if set to 0x01;
        // otherwise, it MUST be set to 0x00.
        underline = (leis.readUByte() != 0x00);

        // An 8-bit unsigned integer that specifies a strikeout font if set to 0x01;
        // otherwise, it MUST be set to 0x00.
        strikeOut = (leis.readUByte() != 0x00);

        // An 8-bit unsigned integer that specifies the set of character glyphs.
        // It MUST be a value in the WMF CharacterSet enumeration.
        // If the character set is unknown, metafile processing SHOULD NOT attempt
        // to translate or interpret strings that are rendered with that font.
        // If a typeface name is specified in the Facename field, the CharSet field
        // value MUST match the character set of that typeface.
        charSet = FontCharset.valueOf(leis.readUByte());

        // An 8-bit unsigned integer that specifies the output precision.
        // The output precision defines how closely the font is required to match the requested height, width,
        // character orientation, escapement, pitch, and font type.
        // It MUST be a value from the WMF OutPrecision enumeration.
        // Applications can use the output precision to control how the font mapper chooses a font when the
        // operating system contains more than one font with a specified name. For example, if an operating
        // system contains a font named Symbol in rasterized and TrueType forms, an output precision value
        // of OUT_TT_PRECIS forces the font mapper to choose the TrueType version.
        // A value of OUT_TT_ONLY_PRECIS forces the font mapper to choose a TrueType font, even if it is
        // necessary to substitute a TrueType font with another name.
        outPrecision = WmfOutPrecision.valueOf(leis.readUByte());

        // An 8-bit unsigned integer that specifies the clipping precision.
        // The clipping precision defines how to clip characters that are partially outside the clipping region.
        // It can be one or more of the WMF ClipPrecision Flags
        clipPrecision.init(leis);

        // An 8-bit unsigned integer that specifies the output quality. The output quality defines how closely
        // to attempt to match the logical-font attributes to those of an actual physical font.
        // It MUST be one of the values in the WMF FontQuality enumeration
        quality = WmfFontQuality.valueOf(leis.readUByte());

        // A WMF PitchAndFamily object that specifies the pitch and family of the font.
        // Font families describe the look of a font in a general way.
        // They are intended for specifying a font when the specified typeface is not available.
        pitchAndFamily = leis.readUByte();

        int size = 5* LittleEndianConsts.INT_SIZE+8*LittleEndianConsts.BYTE_SIZE;

        StringBuilder sb = new StringBuilder();

        // A string of no more than 32 Unicode characters that specifies the typeface name of the font.
        // If the length of this string is less than 32 characters, a terminating NULL MUST be present,
        // after which the remainder of this field MUST be ignored.
        int readBytes = readString(leis, sb, 32);
        if (readBytes == -1) {
            throw new IOException("Font facename can't be determined.");
        }
        facename = sb.toString();
        size += readBytes;

        if (recordSize <= LOGFONT_SIZE) {
            return size;
        }

        // A string of 64 Unicode characters that contains the font's full name.
        // Ifthe length of this string is less than 64 characters, a terminating
        // NULL MUST be present, after which the remainder of this field MUST be ignored.
        readBytes = readString(leis, sb, 64);
        if (readBytes == -1) {
            throw new IOException("Font fullname can't be determined.");
        }
        fullname = sb.toString();
        size += readBytes;

        // A string of 32 Unicode characters that defines the font's style. If the length of
        // this string is less than 32 characters, a terminating NULL MUST be present,
        // after which the remainder of this field MUST be ignored.
        readBytes = readString(leis, sb, 32);
        if (readBytes == -1) {
            throw new IOException("Font style can't be determined.");
        }
        style = sb.toString();
        size += readBytes;

        if (recordSize == LOGFONTPANOSE_SIZE) {
            // LogFontPanose Object

            LogFontPanose logPan = new LogFontPanose();
            details = logPan;

            int version = leis.readInt();

            // A 32-bit unsigned integer that specifies the point size at which font
            //hinting is performed. If set to zero, font hinting is performed at the point size corresponding
            //to the Height field in the LogFont object in the LogFont field.
            logPan.styleSize = (int)leis.readUInt();

            int match = leis.readInt();

            int reserved = leis.readInt();

            logPan.vendorId = leis.readInt();

            logPan.culture = leis.readInt();

            // An 8-bit unsigned integer that specifies the family type.
            // The value MUST be in the FamilyType enumeration table.
            logPan.familyType = LogFontPanose.FamilyType.values()[leis.readUByte()];

            // An 8-bit unsigned integer that specifies the serif style.
            // The value MUST be in the SerifType enumeration table.
            logPan.serifStyle = LogFontPanose.SerifType.values()[leis.readUByte()];

            // An 8-bit unsigned integer that specifies the weight of the font.
            // The value MUST be in the Weight enumeration table.
            logPan.weight = LogFontPanose.FontWeight.values()[leis.readUByte()];

            // An 8-bit unsigned integer that specifies the proportion of the font.
            // The value MUST be in the Proportion enumeration table.
            logPan.proportion = LogFontPanose.Proportion.values()[leis.readUByte()];

            // An 8-bit unsigned integer that specifies the proportion of the font.
            // The value MUST be in the Proportion enumeration table.
            logPan.contrast = LogFontPanose.Contrast.values()[leis.readUByte()];

            // An 8-bit unsigned integer that specifies the stroke variation for the font.
            // The value MUST be in the StrokeVariation enumeration table.
            logPan.strokeVariation = LogFontPanose.StrokeVariation.values()[leis.readUByte()];

            // An 8-bit unsigned integer that specifies the arm style of the font.
            // The value MUST be in the ArmStyle enumeration table.
            logPan.armStyle = LogFontPanose.ArmStyle.values()[leis.readUByte()];

            // An 8-bit unsigned integer that specifies the letterform of the font.
            // The value MUST be in the Letterform enumeration table.
            logPan.letterform = LogFontPanose.Letterform.values()[leis.readUByte()];

            // An 8-bit unsigned integer that specifies the midline of the font.
            // The value MUST be in the MidLine enumeration table.
            logPan.midLine = LogFontPanose.MidLine.values()[leis.readUByte()];

            // An 8-bit unsigned integer that specifies the x height of the font.
            // The value MUST be in the XHeight enumeration table.
            logPan.xHeight = LogFontPanose.XHeight.values()[leis.readUByte()];

            // skip 2 byte to ensure 32-bit alignment of this structure.
            long skipped = IOUtils.skipFully(leis,2);
            if (skipped != 2) {
                throw new IOException("Didn't skip 2: "+skipped);
            }

            size += 6*LittleEndianConsts.INT_SIZE+10* LittleEndianConsts.BYTE_SIZE+2;
        } else {
            // LogFontExDv Object

            LogFontExDv logEx = new LogFontExDv();
            details = logEx;

                    // A string of 32 Unicode characters that defines the character set of the font.
            // If the length of this string is less than 32 characters, a terminating NULL MUST be present,
            // after which the remainder of this field MUST be ignored.
            readBytes = readString(leis, sb, 32);
            if (readBytes == -1) {
                throw new IOException("Font script can't be determined.");
            }
            script = sb.toString();
            size += readBytes;

            // Design Vector

            // A 32-bit unsigned integer that MUST be set to the value 0x08007664.
            int signature = leis.readInt();
            // some non-conformant applications don't write the magic code in
            // assert (signature == 0x08007664);

            // A 32-bit unsigned integer that specifies the number of elements in the
            // Values array. It MUST be in the range 0 to 16, inclusive.
            int numAxes = leis.readInt();
            size += 2*LittleEndianConsts.INT_SIZE;

            // An optional array of 32-bit signed integers that specify the values of the font axes of a
            // multiple master, OpenType font. The maximum number of values in the array is 16.
            if (0 <= numAxes && numAxes <= 16) {
                logEx.designVector = new int[numAxes];
                for (int i=0; i<numAxes; i++) {
                    logEx.designVector[i] = leis.readInt();
                }
                size += numAxes*LittleEndianConsts.INT_SIZE;
            }
        }

        return size;
    }

    @Override
    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "base", super::getGenericProperties,
            "fullname", () -> fullname,
            "style", () -> style,
            "script", () -> script,
            "details", () -> details
        );
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }

    public void setStrikeOut(boolean strikeOut) {
        this.strikeOut = strikeOut;
    }

    public void setTypeface(String typeface) {
        this.facename = typeface;
    }

    @Override
    protected int readString(LittleEndianInputStream leis, StringBuilder sb, int limit) {
        sb.setLength(0);
        byte[] buf = new byte[limit * 2];
        leis.readFully(buf);

        int b1, b2, readBytes = 0;
        do {
            if (readBytes == limit*2) {
                return -1;
            }

            b1 = buf[readBytes++];
            b2 = buf[readBytes++];
        } while ((b1 != 0 || b2 != 0) && b1 != -1 && b2 != -1 && readBytes <= limit*2);
        sb.append(new String(buf, 0, readBytes-2, StandardCharsets.UTF_16LE));

        return limit*2;
    }
}

