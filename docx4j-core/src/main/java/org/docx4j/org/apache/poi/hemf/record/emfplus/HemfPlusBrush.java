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

import static org.docx4j.org.apache.poi.hemf.record.emf.HemfFill.readXForm;
import static org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusDraw.readARGB;
import static org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusDraw.readPointF;
import static org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusDraw.readRectF;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.hemf.draw.HemfDrawProperties;
import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusHeader.EmfPlusGraphicsVersion;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusImage.EmfPlusImage;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusImage.EmfPlusWrapMode;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectData;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectType;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusPath.EmfPlusPath;
import org.docx4j.org.apache.poi.hwmf.record.HwmfBrushStyle;
import org.docx4j.org.apache.poi.hwmf.record.HwmfColorRef;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.docx4j.org.apache.poi.sl.draw.DrawPaint;
import org.docx4j.org.apache.poi.util.BitField;
import org.docx4j.org.apache.poi.util.BitFieldFactory;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

public class HemfPlusBrush {
    /** The BrushType enumeration defines types of graphics brushes, which are used to fill graphics regions. */
    public enum EmfPlusBrushType {
        SOLID_COLOR(0X00000000, EmfPlusSolidBrushData::new),
        HATCH_FILL(0X00000001, EmfPlusHatchBrushData::new),
        TEXTURE_FILL(0X00000002, EmfPlusTextureBrushData::new),
        PATH_GRADIENT(0X00000003, EmfPlusPathGradientBrushData::new),
        LINEAR_GRADIENT(0X00000004, EmfPlusLinearGradientBrushData::new)
        ;

        public final int id;
        public final Supplier<? extends EmfPlusBrushData> constructor;

        EmfPlusBrushType(int id, Supplier<? extends EmfPlusBrushData> constructor) {
            this.id = id;
            this.constructor = constructor;
        }

        public static EmfPlusBrushType valueOf(int id) {
            for (EmfPlusBrushType wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }

    public enum EmfPlusHatchStyle {
        /** Specifies equally spaced horizontal lines. */
        HORIZONTAL(0X00000000),
        /** Specifies equally spaced vertical lines. */
        VERTICAL(0X00000001),
        /** Specifies lines on a diagonal from upper left to lower right. */
        FORWARD_DIAGONAL(0X00000002),
        /** Specifies lines on a diagonal from upper right to lower left. */
        BACKWARD_DIAGONAL(0X00000003),
        /** Specifies crossing horizontal and vertical lines. */
        LARGE_GRID(0X00000004),
        /** Specifies crossing forward diagonal and backward diagonal lines with anti-aliasing. */
        DIAGONAL_CROSS(0X00000005),
        /** Specifies a 5-percent hatch, which is the ratio of foreground color to background color equal to 5:100. */
        PERCENT_05(0X00000006),
        /** Specifies a 10-percent hatch, which is the ratio of foreground color to background color equal to 10:100. */
        PERCENT_10(0X00000007),
        /** Specifies a 20-percent hatch, which is the ratio of foreground color to background color equal to 20:100. */
        PERCENT_20(0X00000008),
        /** Specifies a 25-percent hatch, which is the ratio of foreground color to background color equal to 25:100. */
        PERCENT_25(0X00000009),
        /** Specifies a 30-percent hatch, which is the ratio of foreground color to background color equal to 30:100. */
        PERCENT_30(0X0000000A),
        /** Specifies a 40-percent hatch, which is the ratio of foreground color to background color equal to 40:100. */
        PERCENT_40(0X0000000B),
        /** Specifies a 50-percent hatch, which is the ratio of foreground color to background color equal to 50:100. */
        PERCENT_50(0X0000000C),
        /** Specifies a 60-percent hatch, which is the ratio of foreground color to background color equal to 60:100. */
        PERCENT_60(0X0000000D),
        /** Specifies a 70-percent hatch, which is the ratio of foreground color to background color equal to 70:100. */
        PERCENT_70(0X0000000E),
        /** Specifies a 75-percent hatch, which is the ratio of foreground color to background color equal to 75:100. */
        PERCENT_75(0X0000000F),
        /** Specifies an 80-percent hatch, which is the ratio of foreground color to background color equal to 80:100. */
        PERCENT_80(0X00000010),
        /** Specifies a 90-percent hatch, which is the ratio of foreground color to background color equal to 90:100. */
        PERCENT_90(0X00000011),
        /**
         * Specifies diagonal lines that slant to the right from top to bottom points with no anti-aliasing.
         * They are spaced 50 percent further apart than lines in the FORWARD_DIAGONAL pattern
         */
        LIGHT_DOWNWARD_DIAGONAL(0X00000012),
        /**
         * Specifies diagonal lines that slant to the left from top to bottom points with no anti-aliasing.
         * They are spaced 50 percent further apart than lines in the BACKWARD_DIAGONAL pattern.
         */
        LIGHT_UPWARD_DIAGONAL(0X00000013),
        /**
         * Specifies diagonal lines that slant to the right from top to bottom points with no anti-aliasing.
         * They are spaced 50 percent closer and are twice the width of lines in the FORWARD_DIAGONAL pattern.
         */
        DARK_DOWNWARD_DIAGONAL(0X00000014),
        /**
         * Specifies diagonal lines that slant to the left from top to bottom points with no anti-aliasing.
         * They are spaced 50 percent closer and are twice the width of lines in the BACKWARD_DIAGONAL pattern.
         */
        DARK_UPWARD_DIAGONAL(0X00000015),
        /**
         * Specifies diagonal lines that slant to the right from top to bottom points with no anti-aliasing.
         * They have the same spacing between lines in WIDE_DOWNWARD_DIAGONAL pattern and FORWARD_DIAGONAL pattern,
         * but WIDE_DOWNWARD_DIAGONAL has the triple line width of FORWARD_DIAGONAL.
         */
        WIDE_DOWNWARD_DIAGONAL(0X00000016),
        /**
         * Specifies diagonal lines that slant to the left from top to bottom points with no anti-aliasing.
         * They have the same spacing between lines in WIDE_UPWARD_DIAGONAL pattern and BACKWARD_DIAGONAL pattern,
         * but WIDE_UPWARD_DIAGONAL has the triple line width of WIDE_UPWARD_DIAGONAL.
         */
        WIDE_UPWARD_DIAGONAL(0X00000017),
        /** Specifies vertical lines that are spaced 50 percent closer together than lines in the VERTICAL pattern. */
        LIGHT_VERTICAL(0X00000018),
        /** Specifies horizontal lines that are spaced 50 percent closer than lines in the HORIZONTAL pattern. */
        LIGHT_HORIZONTAL(0X00000019),
        /**
         * Specifies vertical lines that are spaced 75 percent closer than lines in the VERTICAL pattern;
         * or 25 percent closer than lines in the LIGHT_VERTICAL pattern.
         */
        NARROW_VERTICAL(0X0000001A),
        /**
         * Specifies horizontal lines that are spaced 75 percent closer than lines in the HORIZONTAL pattern;
         * or 25 percent closer than lines in the LIGHT_HORIZONTAL pattern.
         */
        NARROW_HORIZONTAL(0X0000001B),
        /** Specifies lines that are spaced 50 percent closer than lines in the VERTICAL pattern. */
        DARK_VERTICAL(0X0000001C),
        /** Specifies lines that are spaced 50 percent closer than lines in the HORIZONTAL pattern. */
        DARK_HORIZONTAL(0X0000001D),
        /** Specifies dashed diagonal lines that slant to the right from top to bottom points. */
        DASHED_DOWNWARD_DIAGONAL(0X0000001E),
        /** Specifies dashed diagonal lines that slant to the left from top to bottom points. */
        DASHED_UPWARD_DIAGONAL(0X0000001F),
        /** Specifies dashed horizontal lines. */
        DASHED_HORIZONTAL(0X00000020),
        /** Specifies dashed vertical lines. */
        DASHED_VERTICAL(0X00000021),
        /** Specifies a pattern of lines that has the appearance of confetti. */
        SMALL_CONFETTI(0X00000022),
        /**
         * Specifies a pattern of lines that has the appearance of confetti, and is composed of larger pieces
         * than the SMALL_CONFETTI pattern.
         */
        LARGE_CONFETTI(0X00000023),
        /** Specifies horizontal lines that are composed of zigzags. */
        ZIGZAG(0X00000024),
        /** Specifies horizontal lines that are composed of tildes. */
        WAVE(0X00000025),
        /**
         * Specifies a pattern of lines that has the appearance of layered bricks that slant to the left from
         * top to bottom points.
         */
        DIAGONAL_BRICK(0X00000026),
        /** Specifies a pattern of lines that has the appearance of horizontally layered bricks. */
        HORIZONTAL_BRICK(0X00000027),
        /** Specifies a pattern of lines that has the appearance of a woven material. */
        WEAVE(0X00000028),
        /** Specifies a pattern of lines that has the appearance of a plaid material. */
        PLAID(0X00000029),
        /** Specifies a pattern of lines that has the appearance of divots. */
        DIVOT(0X0000002A),
        /** Specifies crossing horizontal and vertical lines, each of which is composed of dots. */
        DOTTED_GRID(0X0000002B),
        /** Specifies crossing forward and backward diagonal lines, each of which is composed of dots. */
        DOTTED_DIAMOND(0X0000002C),
        /**
         * Specifies a pattern of lines that has the appearance of diagonally layered
         * shingles that slant to the right from top to bottom points.
         */
        SHINGLE(0X0000002D),
        /** Specifies a pattern of lines that has the appearance of a trellis. */
        TRELLIS(0X0000002E),
        /** Specifies a pattern of lines that has the appearance of spheres laid adjacent to each other. */
        SPHERE(0X0000002F),
        /** Specifies crossing horizontal and vertical lines that are spaced 50 percent closer together than LARGE_GRID. */
        SMALL_GRID(0X00000030),
        /** Specifies a pattern of lines that has the appearance of a checkerboard. */
        SMALL_CHECKER_BOARD(0X00000031),
        /**
         * Specifies a pattern of lines that has the appearance of a checkerboard, with squares that are twice the
         * size of the squares in the SMALL_CHECKER_BOARD pattern.
         */
        LARGE_CHECKER_BOARD(0X00000032),
        /** Specifies crossing forward and backward diagonal lines; the lines are not anti-aliased. */
        OUTLINED_DIAMOND(0X00000033),
        /** Specifies a pattern of lines that has the appearance of a checkerboard placed diagonally. */
        SOLID_DIAMOND(0X00000034)
        ;


        public final int id;

        EmfPlusHatchStyle(int id) {
            this.id = id;
        }

        public static EmfPlusHatchStyle valueOf(int id) {
            for (EmfPlusHatchStyle wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }

    }

    @SuppressWarnings("unused")
    public interface EmfPlusBrushData extends GenericRecord {
        /**
         * This flag is meaningful in EmfPlusPathGradientBrushData objects.
         *
         * If set, an EmfPlusBoundaryPathData object MUST be specified in the BoundaryData field of the brush data object.
         * If clear, an EmfPlusBoundaryPointData object MUST be specified in the BoundaryData field of the brush data object.
         */
        BitField PATH = BitFieldFactory.getInstance(0x00000001);

        /**
         * This flag is meaningful in EmfPlusLinearGradientBrushData objects , EmfPlusPathGradientBrushData objects,
         * and EmfPlusTextureBrushData objects.
         *
         * If set, a 2x3 world space to device space transform matrix MUST be specified in the OptionalData field of
         * the brush data object.
         */
        BitField TRANSFORM = BitFieldFactory.getInstance(0x00000002);

        /**
         * This flag is meaningful in EmfPlusLinearGradientBrushData and EmfPlusPathGradientBrushData objects.
         *
         * If set, an EmfPlusBlendColors object MUST be specified in the OptionalData field of the brush data object.
         */
        BitField PRESET_COLORS = BitFieldFactory.getInstance(0x00000004);

        /**
         * This flag is meaningful in EmfPlusLinearGradientBrushData and EmfPlusPathGradientBrushData objects.
         *
         * If set, an EmfPlusBlendFactors object that specifies a blend pattern along a horizontal gradient MUST be
         * specified in the OptionalData field of the brush data object.
         */
        BitField BLEND_FACTORS_H = BitFieldFactory.getInstance(0x00000008);

        /**
         * This flag is meaningful in EmfPlusLinearGradientBrushData objects.
         *
         * If set, an EmfPlusBlendFactors object that specifies a blend pattern along a vertical gradient MUST be
         * specified in the OptionalData field of the brush data object.
         */
        BitField BLEND_FACTORS_V = BitFieldFactory.getInstance(0x00000010);

        /**
         * This flag is meaningful in EmfPlusPathGradientBrushData objects.
         *
         * If set, an EmfPlusFocusScaleData object MUST be specified in the OptionalData field of the brush data object.
         */
        BitField FOCUS_SCALES = BitFieldFactory.getInstance(0x00000040);

        /**
         * This flag is meaningful in EmfPlusLinearGradientBrushData, EmfPlusPathGradientBrushData, and
         * EmfPlusTextureBrushData objects.
         *
         * If set, the brush MUST already be gamma corrected; that is, output brightness and intensity have been
         * corrected to match the input image.
         */
        BitField IS_GAMMA_CORRECTED = BitFieldFactory.getInstance(0x00000080);

        /**
         * This flag is meaningful in EmfPlusTextureBrushData objects.
         *
         * If set, a world space to device space transform SHOULD NOT be applied to the texture brush.
         */
        BitField DO_NOT_TRANSFORM = BitFieldFactory.getInstance(0x00000100);

        long init(LittleEndianInputStream leis, long dataSize) throws IOException;

        /**
         * Apply brush data to graphics properties
         * @param ctx the graphics context
         * @param continuedObjectData the list continued object data
         */
        void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData);

        /**
         * Apply brush data to pen properties
         * @param ctx the graphics context
         * @param continuedObjectData the list continued object data
         */
        void applyPen(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData);
    }

    /** The EmfPlusBrush object specifies a graphics brush for filling regions. */
    public static class EmfPlusBrush implements EmfPlusObjectData {
        private static final int MAX_OBJECT_SIZE = 1_000_000;

        private final EmfPlusGraphicsVersion graphicsVersion = new EmfPlusGraphicsVersion();
        private EmfPlusBrushType brushType;
        private byte[] brushBytes;

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, EmfPlusObjectType objectType, int flags) throws IOException {
            leis.mark(LittleEndianConsts.INT_SIZE);
            long size = graphicsVersion.init(leis);

            if (isContinuedRecord()) {
                leis.reset();
                size = 0;
            } else {
                int brushInt = leis.readInt();
                brushType = EmfPlusBrushType.valueOf(brushInt);
                assert(brushType != null);
                size += LittleEndianConsts.INT_SIZE;
            }

            brushBytes = IOUtils.toByteArray(leis, Math.toIntExact(dataSize-size), MAX_OBJECT_SIZE);

            return dataSize;
        }

        @Override
        public void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {
            EmfPlusBrushData brushData = getBrushData(continuedObjectData);
            brushData.applyObject(ctx, continuedObjectData);
        }


        public void applyPen(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {
            EmfPlusBrushData brushData = getBrushData(continuedObjectData);
            brushData.applyPen(ctx, continuedObjectData);
        }


        @Override
        public EmfPlusGraphicsVersion getGraphicsVersion() {
            return graphicsVersion;
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public byte[] getBrushBytes() {
            return brushBytes;
        }

        /**
         * @param continuedObjectData list of object data
         * @return {@link EmfPlusBrushData}
         * @throws IllegalStateException if the data cannot be processed
         */
        public EmfPlusBrushData getBrushData(List<? extends EmfPlusObjectData> continuedObjectData) {
            EmfPlusBrushData brushData = brushType.constructor.get();
            byte[] buf = getRawData(continuedObjectData);
            try (UnsynchronizedByteArrayInputStream bis = UnsynchronizedByteArrayInputStream.builder().setByteArray(buf).get()){
                brushData.init(new LittleEndianInputStream(bis), buf.length);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return brushData;
        }

        /**
         * @param continuedObjectData list of object data
         * @return byte array
         * @throws IllegalStateException if the data cannot be processed
         */
        public byte[] getRawData(List<? extends EmfPlusObjectData> continuedObjectData) {
            try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
                bos.write(getBrushBytes());
                if (continuedObjectData != null) {
                    for (EmfPlusObjectData od : continuedObjectData) {
                        bos.write(((EmfPlusBrush)od).getBrushBytes());
                    }
                }
                return bos.toByteArray();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }


        @Override
        public EmfPlusBrushType getGenericRecordType() {
            return brushType;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "graphicsVersion", this::getGraphicsVersion,
                /* only return the first object data ... enough for now */
                "brushData", () -> getBrushData(null)
            );
        }
    }

    /** The EmfPlusSolidBrushData object specifies a solid color for a graphics brush. */
    public static class EmfPlusSolidBrushData implements EmfPlusBrushData {
        private Color solidColor;
        @Override
        public long init(LittleEndianInputStream leis, long dataSize) throws IOException {
            solidColor = readARGB(leis.readInt());
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {
            HemfDrawProperties prop = ctx.getProperties();
            prop.setBrushColor(new HwmfColorRef(solidColor));
            prop.setBrushTransform(null);
            prop.setBrushStyle(HwmfBrushStyle.BS_SOLID);
        }

        @Override
        public void applyPen(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {
            HemfDrawProperties prop = ctx.getProperties();
            prop.setPenColor(new HwmfColorRef(solidColor));
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public EmfPlusBrushType getGenericRecordType() {
            return EmfPlusBrushType.SOLID_COLOR;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("solidColor", () -> solidColor);
        }
    }


    /** The EmfPlusHatchBrushData object specifies a hatch pattern for a graphics brush. */
    public static class EmfPlusHatchBrushData implements EmfPlusBrushData {
        private EmfPlusHatchStyle style;
        private Color foreColor, backColor;
        @Override
        public long init(LittleEndianInputStream leis, long dataSize) {
            style = EmfPlusHatchStyle.valueOf(leis.readInt());
            foreColor = readARGB(leis.readInt());
            backColor = readARGB(leis.readInt());
            return 3L*LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {
            HemfDrawProperties prop = ctx.getProperties();
            prop.setBrushColor(new HwmfColorRef(foreColor));
            prop.setBackgroundColor(new HwmfColorRef(backColor));
            prop.setEmfPlusBrushHatch(style);
        }

        @Override
        public void applyPen(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {
            HemfDrawProperties prop = ctx.getProperties();
            prop.setPenColor(new HwmfColorRef(foreColor));
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public EmfPlusBrushType getGenericRecordType() {
            return EmfPlusBrushType.HATCH_FILL;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "style", () -> style,
                "foreColor", () -> foreColor,
                "backColor", () -> backColor
            );
        }
    }

    /** The EmfPlusLinearGradientBrushData object specifies a linear gradient for a graphics brush. */
    public static class EmfPlusLinearGradientBrushData implements EmfPlusBrushData {
        private int dataFlags;
        private EmfPlusWrapMode wrapMode;
        private final Rectangle2D rect = new Rectangle2D.Double();
        private Color startColor, endColor;
        private AffineTransform blendTransform;
        private float[] positions;
        private Color[] blendColors;
        private float[] positionsV;
        private float[] blendFactorsV;
        private float[] positionsH;
        private float[] blendFactorsH;

        private static final int[] FLAG_MASKS = {0x02, 0x04, 0x08, 0x10, 0x80};
        private static final String[] FLAG_NAMES = {"TRANSFORM", "PRESET_COLORS", "BLEND_FACTORS_H", "BLEND_FACTORS_V", "BRUSH_DATA_IS_GAMMA_CORRECTED"};

        @Override
        public long init(LittleEndianInputStream leis, long dataSize) throws IOException {
            // A 32-bit unsigned integer that specifies the data in the OptionalData field.
            // This value MUST be composed of BrushData flags
            dataFlags = leis.readInt();

            // A 32-bit signed integer from the WrapMode enumeration that specifies whether to paint the area outside
            // the boundary of the brush. When painting outside the boundary, the wrap mode specifies how the color
            // gradient is repeated.
            wrapMode = EmfPlusWrapMode.valueOf(leis.readInt());

            long size = 2L * LittleEndianConsts.INT_SIZE;
            size += readRectF(leis, rect);

            // An EmfPlusARGB object that specifies the color at the starting/ending boundary point of the linear gradient brush.
            startColor = readARGB(leis.readInt());
            endColor = readARGB(leis.readInt());

            // skip reserved1/2 fields
            leis.skipFully(2 * LittleEndianConsts.INT_SIZE);

            size += 4L * LittleEndianConsts.INT_SIZE;

            if (TRANSFORM.isSet(dataFlags)) {
                blendTransform = new AffineTransform();
                size += readXForm(leis, blendTransform);
            }

            if (isPreset() && (isBlendH() || isBlendV())) {
                throw new IOException("invalid combination of preset colors and blend factors v/h");
            }

            size += (isPreset()) ? readColors(leis, d -> positions = d, c -> blendColors = c) : 0;
            size += (isBlendV()) ? readFactors(leis, d -> positionsV = d, f -> blendFactorsV = f) : 0;
            size += (isBlendH()) ? readFactors(leis, d -> positionsH = d, f -> blendFactorsH = f) : 0;

            return size;
        }

        @Override
        public void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {
            HemfDrawProperties prop = ctx.getProperties();
            prop.setBrushStyle(HwmfBrushStyle.BS_LINEAR_GRADIENT);
            prop.setBrushRect(rect);
            prop.setBrushTransform(blendTransform);

            // Preset colors and BlendH/V are mutual exclusive
            if (isPreset()) {
                setColorProps(prop::setBrushColorsH, positions, this::getBlendColorAt);
            } else {
                setColorProps(prop::setBrushColorsH, positionsH, this::getBlendHColorAt);
            }
            setColorProps(prop::setBrushColorsV, positionsV, this::getBlendVColorAt);

            if (!(isPreset() || isBlendH() || isBlendV())) {
                prop.setBrushColorsH(Arrays.asList(kv(0f, startColor), kv(1f, endColor)));
            }
        }

        @Override
        public void applyPen(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {

        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public EmfPlusBrushType getGenericRecordType() {
            return EmfPlusBrushType.LINEAR_GRADIENT;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            final Map<String, Supplier<?>> m = new LinkedHashMap<>();
            m.put("flags", GenericRecordUtil.getBitsAsString(() -> dataFlags, FLAG_MASKS, FLAG_NAMES));
            m.put("wrapMode", () -> wrapMode);
            m.put("rect", () -> rect);
            m.put("startColor", () -> startColor);
            m.put("endColor", () -> endColor);
            m.put("blendTransform", () -> blendTransform);
            m.put("positions", () -> positions);
            m.put("blendColors", () -> blendColors);
            m.put("positionsV", () -> positionsV);
            m.put("blendFactorsV", () -> blendFactorsV);
            m.put("positionsH", () -> positionsH);
            m.put("blendFactorsH", () -> blendFactorsH);
            return Collections.unmodifiableMap(m);
        }

        private boolean isPreset() {
            return PRESET_COLORS.isSet(dataFlags);
        }

        private boolean isBlendH() {
            return BLEND_FACTORS_H.isSet(dataFlags);
        }

        private boolean isBlendV() {
            return BLEND_FACTORS_V.isSet(dataFlags);
        }

        private Map.Entry<Float, Color> getBlendColorAt(int index) {
            return kv(positions[index], blendColors[index]);
        }

        private Map.Entry<Float, Color> getBlendHColorAt(int index) {
            return kv(positionsH[index], interpolateColors(blendFactorsH[index]));
        }

        private Map.Entry<Float, Color> getBlendVColorAt(int index) {
            return kv(positionsV[index], interpolateColors(blendFactorsV[index]));
        }

        private static Map.Entry<Float, Color> kv(Float position, Color color) {
            return new AbstractMap.SimpleEntry<>(position, color);
        }

        private static void setColorProps(
                Consumer<List<? extends Map.Entry<Float, Color>>> setter, float[] positions, Function<Integer, ? extends Map.Entry<Float, Color>> sup) {
            if (positions == null) {
                setter.accept(null);
            } else {
                setter.accept(IntStream.range(0, positions.length).boxed().map(sup).collect(Collectors.toList()));
            }
        }

        private Color interpolateColors(final double factor) {
            return interpolateColorsRGB(factor);
        }

        private Color interpolateColorsRGB(final double factor) {
            // TODO: check IS_GAMMA_CORRECTED flag and maybe don't convert into scRGB
            double[] start = DrawPaint.RGB2SCRGB(startColor);
            double[] end = DrawPaint.RGB2SCRGB(endColor);

            // compute the interpolated color in linear space
            int a = (int)Math.round(startColor.getAlpha() + factor * (endColor.getAlpha() - startColor.getAlpha()));
            double r = start[0] + factor * (end[0] - start[0]);
            double g = start[1] + factor * (end[1] - start[1]);
            double b = start[2] + factor * (end[2] - start[2]);

            Color inter = DrawPaint.SCRGB2RGB(r,g,b);
            return new Color(inter.getRed(), inter.getGreen(), inter.getBlue(), a);
        }
    }

    /** The EmfPlusPathGradientBrushData object specifies a path gradient for a graphics brush. */
    public static class EmfPlusPathGradientBrushData implements EmfPlusBrushData {
        private int dataFlags;
        private EmfPlusWrapMode wrapMode;
        private Color centerColor;
        private final Point2D centerPoint = new Point2D.Double();
        private Color[] surroundingColor;
        private EmfPlusPath boundaryPath;
        private Point2D[] boundaryPoints;
        private AffineTransform blendTransform;
        private float[] positions;
        private Color[] blendColors;
        private float[] blendFactorsH;
        private Double focusScaleX, focusScaleY;

        @Override
        public long init(LittleEndianInputStream leis, long dataSize) throws IOException {
            // A 32-bit unsigned integer that specifies the data in the OptionalData field.
            // This value MUST be composed of BrushData flags
            dataFlags = leis.readInt();

            // A 32-bit signed integer from the WrapMode enumeration that specifies whether to paint the area outside
            // the boundary of the brush. When painting outside the boundary, the wrap mode specifies how the color
            // gradient is repeated.
            wrapMode = EmfPlusWrapMode.valueOf(leis.readInt());

            // An EmfPlusARGB object that specifies the center color of the path gradient brush, which is the color
            // that appears at the center point of the brush. The color of the brush changes gradually from the
            // boundary color to the center color as it moves from the boundary to the center point.
            centerColor = readARGB(leis.readInt());
            long size = 3L * LittleEndianConsts.INT_SIZE;

            if (wrapMode == null) {
                return size;
            }

            size += readPointF(leis, centerPoint);

            // An unsigned 32-bit integer that specifies the number of colors specified in the SurroundingColor field.
            // The surrounding colors are colors specified for discrete points on the boundary of the brush.
            final int colorCount = leis.readInt();

            // An array of SurroundingColorCount EmfPlusARGB objects that specify the colors for discrete points on the
            // boundary of the brush.
            IOUtils.safelyAllocateCheck(colorCount, HwmfPicture.getMaxRecordLength());
            surroundingColor = new Color[colorCount];
            for (int i = 0; i < colorCount; i++) {
                surroundingColor[i] = readARGB(leis.readInt());
            }
            size += (colorCount + 1L) * LittleEndianConsts.INT_SIZE;

            // The boundary of the path gradient brush, which is specified by either a path or a closed cardinal spline.
            // If the BrushDataPath flag is set in the BrushDataFlags field, this field MUST contain an
            // EmfPlusBoundaryPathData object; otherwise, this field MUST contain an EmfPlusBoundaryPointData object.
            if (PATH.isSet(dataFlags)) {
                // A 32-bit signed integer that specifies the size in bytes of the BoundaryPathData field.
                int pathDataSize = leis.readInt();
                size += LittleEndianConsts.INT_SIZE;

                // An EmfPlusPath object that specifies the boundary of the brush.
                size += (boundaryPath = new EmfPlusPath()).init(leis, pathDataSize, EmfPlusObjectType.PATH, 0);
            } else {
                // A 32-bit signed integer that specifies the number of points in the BoundaryPointData field.
                int pointCount = leis.readInt();
                size += LittleEndianConsts.INT_SIZE;

                // An array of BoundaryPointCount EmfPlusPointF objects that specify the boundary of the brush.
                IOUtils.safelyAllocateCheck(pointCount, HwmfPicture.getMaxRecordLength());
                boundaryPoints = new Point2D[pointCount];
                for (int i=0; i<pointCount; i++) {
                    size += readPointF(leis, boundaryPoints[i] = new Point2D.Double());
                }
            }

            // An optional EmfPlusTransformMatrix object that specifies a world space to device space transform for
            // the path gradient brush. This field MUST be present if the BrushDataTransform flag is set in the
            // BrushDataFlags field of the EmfPlusPathGradientBrushData object.
            if (TRANSFORM.isSet(dataFlags)) {
                size += readXForm(leis, (blendTransform = new AffineTransform()));
            }

            // An optional blend pattern for the path gradient brush. If this field is present, it MUST contain either
            // an EmfPlusBlendColors object, or an EmfPlusBlendFactors object, but it MUST NOT contain both.
            final boolean isPreset = PRESET_COLORS.isSet(dataFlags);
            final boolean blendH = BLEND_FACTORS_H.isSet(dataFlags);
            if (isPreset && blendH) {
                throw new IOException("invalid combination of preset colors and blend factors h");
            }

            size += (isPreset) ? readColors(leis, d -> positions = d, c -> blendColors = c) : 0;
            size += (blendH) ? readFactors(leis, d -> positions = d, f -> blendFactorsH = f) : 0;

            // An optional EmfPlusFocusScaleData object that specifies focus scales for the path gradient brush.
            // This field MUST be present if the BrushDataFocusScales flag is set in the BrushDataFlags field of the
            // EmfPlusPathGradientBrushData object.
            if (FOCUS_SCALES.isSet(dataFlags)) {
                // A 32-bit unsigned integer that specifies the number of focus scales. This value MUST be 2.
                int focusScaleCount = leis.readInt();
                if (focusScaleCount != 2) {
                    throw new IOException("invalid focus scale count");
                }
                // A floating-point value that defines the horizontal/vertical focus scale.
                // The focus scale MUST be a value between 0.0 and 1.0, exclusive.
                focusScaleX = (double)leis.readFloat();
                focusScaleY = (double)leis.readFloat();
                size += 3*LittleEndianConsts.INT_SIZE;
            }

            return size;
        }

        @Override
        public void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {

        }

        @Override
        public void applyPen(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {

        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }


        @Override
        public EmfPlusBrushType getGenericRecordType() {
            return EmfPlusBrushType.PATH_GRADIENT;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            final Map<String,Supplier<?>> m = new LinkedHashMap<>();
            m.put("flags", () -> dataFlags);
            m.put("wrapMode", () -> wrapMode);
            m.put("centerColor", () -> centerColor);
            m.put("centerPoint", () -> centerPoint);
            m.put("surroundingColor", () -> surroundingColor);
            m.put("boundaryPath", () -> boundaryPath);
            m.put("boundaryPoints", () -> boundaryPoints);
            m.put("blendTransform", () -> blendTransform);
            m.put("positions", () -> positions);
            m.put("blendColors", () -> blendColors);
            m.put("blendFactorsH", () -> blendFactorsH);
            m.put("focusScaleX", () -> focusScaleX);
            m.put("focusScaleY", () -> focusScaleY);
            return Collections.unmodifiableMap(m);
        }
    }

    /** The EmfPlusTextureBrushData object specifies a texture image for a graphics brush. */
    public static class EmfPlusTextureBrushData implements EmfPlusBrushData {
        private int dataFlags;
        private EmfPlusWrapMode wrapMode;
        private AffineTransform brushTransform;
        private EmfPlusImage image;

        @Override
        public long init(LittleEndianInputStream leis, long dataSize) throws IOException {
            // A 32-bit unsigned integer that specifies the data in the OptionalData field.
            // This value MUST be composed of BrushData flags.
            dataFlags = leis.readInt();

            // A 32-bit signed integer from the WrapMode enumeration that specifies how to repeat the texture image
            // across a shape, when the image is smaller than the area being filled.
            wrapMode = EmfPlusWrapMode.valueOf(leis.readInt());

            long size = 2L * LittleEndianConsts.INT_SIZE;

            if (TRANSFORM.isSet(dataFlags)) {
                brushTransform = new AffineTransform();
                size += readXForm(leis, brushTransform);
            }

            if (dataSize > size) {
                image = new EmfPlusImage();
                size += image.init(leis, dataSize-size, EmfPlusObjectType.IMAGE, 0);
            }

            return Math.toIntExact(size);
        }

        @Override
        public void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {
            HemfDrawProperties prop = ctx.getProperties();
            image.applyObject(ctx, null);
            prop.setBrushBitmap(prop.getEmfPlusImage());
            prop.setBrushStyle(HwmfBrushStyle.BS_PATTERN);
            prop.setBrushTransform(brushTransform);
        }

        @Override
        public void applyPen(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {

        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public EmfPlusBrushType getGenericRecordType() {
            return EmfPlusBrushType.TEXTURE_FILL;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "dataFlags", () -> dataFlags,
                "wrapMode", () -> wrapMode,
                "brushTransform", () -> brushTransform,
                "image", () -> image
            );
        }
    }

    private static int readPositions(LittleEndianInputStream leis, Consumer<float[]> pos) {
        final int count = leis.readInt();
        int size = LittleEndianConsts.INT_SIZE;

        IOUtils.safelyAllocateCheck(count, HwmfPicture.getMaxRecordLength());
        float[] positions = new float[count];
        for (int i=0; i<count; i++) {
            positions[i] = leis.readFloat();
            size += LittleEndianConsts.INT_SIZE;
        }

        pos.accept(positions);
        return size;
    }

    private static int readColors(LittleEndianInputStream leis, Consumer<float[]> pos, Consumer<Color[]>  cols) {
        int[] count = { 0 };
        int size = readPositions(leis, p -> { count[0] = p.length; pos.accept(p); });
        Color[] colors = new Color[count[0]];
        for (int i=0; i<colors.length; i++) {
            colors[i] = readARGB(leis.readInt());
        }
        cols.accept(colors);
        return size + colors.length * LittleEndianConsts.INT_SIZE;
    }

    private static int readFactors(LittleEndianInputStream leis, Consumer<float[]> pos, Consumer<float[]> facs) {
        int[] count = { 0 };
        int size = readPositions(leis, p -> { count[0] = p.length; pos.accept(p); });
        float[] factors = new float[count[0]];
        for (int i=0; i<factors.length; i++) {
            factors[i] = leis.readFloat();
        }
        facs.accept(factors);
        return size + factors.length * LittleEndianConsts.INT_SIZE;
    }
}
