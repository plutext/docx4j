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

package org.docx4j.org.apache.poi.hemf.record.emfplus;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.common.usermodel.fonts.FontHeader;
import org.docx4j.org.apache.poi.hemf.draw.HemfDrawProperties;
import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfFont;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusDraw.EmfPlusUnitType;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusHeader.EmfPlusGraphicsVersion;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectData;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectType;
import org.docx4j.org.apache.poi.util.BitField;
import org.docx4j.org.apache.poi.util.BitFieldFactory;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;
import org.docx4j.org.apache.poi.util.StringUtil;

public class HemfPlusFont {


    public static class EmfPlusFont implements EmfPlusObjectData {
        /**
         * If set, the font typeface MUST be rendered with a heavier weight or thickness.
         * If clear, the font typeface MUST be rendered with a normal thickness.
         */
        private static final BitField BOLD = BitFieldFactory.getInstance(0x00000001);

        /**
         * If set, the font typeface MUST be rendered with the vertical stems of the characters at an increased angle
         * or slant relative to the baseline.
         *
         * If clear, the font typeface MUST be rendered with the vertical stems of the characters at a normal angle.
         */
        private static final BitField ITALIC = BitFieldFactory.getInstance(0x00000002);

        /**
         * If set, the font typeface MUST be rendered with a line underneath the baseline of the characters.
         * If clear, the font typeface MUST be rendered without a line underneath the baseline.
         */
        private static final BitField UNDERLINE = BitFieldFactory.getInstance(0x00000004);

        /**
         * If set, the font typeface MUST be rendered with a line parallel to the baseline drawn through the middle of
         * the characters.
         * If clear, the font typeface MUST be rendered without a line through the characters.
         */
        private static final BitField STRIKEOUT = BitFieldFactory.getInstance(0x00000008);


        private final EmfPlusGraphicsVersion graphicsVersion = new EmfPlusGraphicsVersion();
        private double emSize;
        private EmfPlusUnitType sizeUnit;
        private int styleFlags;
        private String family;

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, EmfPlusObjectType objectType, int flags) throws IOException {
            // An EmfPlusGraphicsVersion object that specifies the version of operating system graphics that was used
            // to create this object.
            long size = graphicsVersion.init(leis);

            // A 32-bit floating-point value that specifies the em size of the font in units specified by the SizeUnit field.
            emSize = leis.readFloat();

            // A 32-bit unsigned integer that specifies the units used for the EmSize field. These are typically the
            // units that were employed when designing the font. The value MUST be in the UnitType enumeration
            sizeUnit = EmfPlusUnitType.valueOf(leis.readInt());

            // A 32-bit signed integer that specifies attributes of the character glyphs that affect the appearance of
            // the font, such as bold and italic. This value MUST be composed of FontStyle flags
            styleFlags = leis.readInt();

            // A 32-bit unsigned integer that is reserved and MUST be ignored.
            leis.skipFully(LittleEndianConsts.INT_SIZE);

            // A 32-bit unsigned integer that specifies the number of characters in the FamilyName field.
            int len = leis.readInt();
            size += 5*LittleEndianConsts.INT_SIZE;

            // A string of Length Unicode characters that contains the name of the font family.
            family = StringUtil.readUnicodeLE(leis, len);
            size += (long) len * LittleEndianConsts.SHORT_SIZE;

            return size;
        }

        @Override
        public void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {
            HemfDrawProperties prop = ctx.getProperties();
            HemfFont font = new HemfFont();
            font.initDefaults();
            font.setTypeface(family);
            // TODO: check how to calculate the font size
            font.setHeight(emSize);
            font.setStrikeOut(STRIKEOUT.isSet(styleFlags));
            font.setUnderline(UNDERLINE.isSet(styleFlags));
            font.setWeight(BOLD.isSet(styleFlags) ? 700 : FontHeader.REGULAR_WEIGHT);
            font.setItalic(ITALIC.isSet(styleFlags));
            prop.setFont(font);
        }

        @Override
        public EmfPlusGraphicsVersion getGraphicsVersion() {
            return graphicsVersion;
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "graphicsVersion", () -> graphicsVersion,
                "emSize", () -> emSize,
                "sizeUnit", () -> sizeUnit,
                "styleFlags", () -> styleFlags,
                "family", () -> family
            );
        }

        @Override
        public EmfPlusObjectType getGenericRecordType() {
            return EmfPlusObjectType.FONT;
        }
    }
}
