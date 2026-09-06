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
import static org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusDraw.readPointF;
import static org.docx4j.org.apache.poi.util.GenericRecordUtil.getBitsAsString;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.hemf.draw.HemfDrawProperties;
import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfPenStyle;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusBrush.EmfPlusBrush;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusDraw.EmfPlusUnitType;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusHeader.EmfPlusGraphicsVersion;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectData;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectType;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusPath.EmfPlusPath;
import org.docx4j.org.apache.poi.hwmf.record.HwmfPenStyle.HwmfLineCap;
import org.docx4j.org.apache.poi.hwmf.record.HwmfPenStyle.HwmfLineDash;
import org.docx4j.org.apache.poi.hwmf.record.HwmfPenStyle.HwmfLineJoin;
import org.docx4j.org.apache.poi.util.BitField;
import org.docx4j.org.apache.poi.util.BitFieldFactory;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

@SuppressWarnings("WeakerAccess")
public class HemfPlusPen {
    /**
     * The LineCapType enumeration defines types of line caps to use at the ends of lines that are drawn
     * with graphics pens.
     */
    public enum EmfPlusLineCapType {
        /** Specifies a squared-off line cap. The end of the line MUST be the last point in the line. */
        FLAT(0X00000000),
        /**
         * Specifies a square line cap. The center of the square MUST be located at
         * the last point in the line. The width of the square is the line width.
         */
        SQUARE(0X00000001),
        /**
         * Specifies a circular line cap. The center of the circle MUST be located at
         * the last point in the line. The diameter of the circle is the line width.
         */
        ROUND(0X00000002),
        /**
         * Specifies a triangular line cap. The base of the triangle MUST be located
         * at the last point in the line. The base of the triangle is the line width.
         */
        TRIANGLE(0X00000003),
        /** Specifies that the line end is not anchored. */
        NO_ANCHOR(0X00000010),
        /**
         * Specifies that the line end is anchored with a square line cap. The center of the square MUST be located
         * at the last point in the line. The height and width of the square are the line width.
         */
        SQUARE_ANCHOR(0X00000011),
        /**
         * Specifies that the line end is anchored with a circular line cap. The center of the circle MUST be located
         * at the last point in the line. The circle SHOULD be wider than the line.
         */
        ROUND_ANCHOR(0X00000012),
        /**
         * Specifies that the line end is anchored with a diamond-shaped line cap, which is a square turned at
         * 45 degrees. The center of the diamond MUST be located at the last point in the line.
         * The diamond SHOULD be wider than the line.
         */
        DIAMOND_ANCHOR(0X00000013),
        /**
         * Specifies that the line end is anchored with an arrowhead shape. The arrowhead point MUST be located at
         * the last point in the line. The arrowhead SHOULD be wider than the line.
         */
        ARROW_ANCHOR(0X00000014),
        /** Mask used to check whether a line cap is an anchor cap. */
        ANCHOR_MASK(0X000000F0),
        /** Specifies a custom line cap. */
        CUSTOM(0X000000FF)
        ;

        public final int id;

        EmfPlusLineCapType(int id) {
            this.id = id;
        }

        public static EmfPlusLineCapType valueOf(int id) {
            for (EmfPlusLineCapType wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }

    /**
     * The LineJoinType enumeration defines ways to join two lines that are drawn by the same graphics
     * pen and whose ends meet.
     */
    public enum EmfPlusLineJoin {
        MITER(0X00000000),
        BEVEL(0X00000001),
        ROUND(0X00000002),
        MITER_CLIPPED(0X00000003)
        ;

        public final int id;

        EmfPlusLineJoin(int id) {
            this.id = id;
        }

        public static EmfPlusLineJoin valueOf(int id) {
            for (EmfPlusLineJoin wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }

    }

    /** The LineStyle enumeration defines styles of lines that are drawn with graphics pens. */
    public enum EmfPlusLineStyle {
        /** Specifies a solid line. */
        SOLID(0X00000000),
        /** Specifies a dashed line. */
        DASH(0X00000001),
        /** Specifies a dotted line. */
        DOT(0X00000002),
        /** Specifies an alternating dash-dot line. */
        DASH_DOT(0X00000003),
        /** Specifies an alternating dash-dot-dot line. */
        DASH_DOT_DOT(0X00000004),
        /** Specifies a user-defined, custom dashed line. */
        CUSTOM(0X00000005)
        ;

        public final int id;

        EmfPlusLineStyle(int id) {
            this.id = id;
        }

        public static EmfPlusLineStyle valueOf(int id) {
            for (EmfPlusLineStyle wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }

    /**
     * The DashedLineCapType enumeration defines types of line caps to use at the ends of dashed lines
     * that are drawn with graphics pens.
     */
    public enum EmfPlusDashedLineCapType {
        /** Specifies a flat dashed line cap. */
        FLAT(0X00000000),
        /** Specifies a round dashed line cap. */
        ROUND(0X00000002),
        /** Specifies a triangular dashed line cap. */
        TRIANGLE(0X00000003)
        ;

        public final int id;

        EmfPlusDashedLineCapType(int id) {
            this.id = id;
        }

        public static EmfPlusDashedLineCapType valueOf(int id) {
            for (EmfPlusDashedLineCapType wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }

    /**
     * The PenAlignment enumeration defines the distribution of the width of the pen with respect to the
     * line being drawn.
     */
    public enum EmfPlusPenAlignment {
        /** Specifies that the EmfPlusPen object is centered over the theoretical line. */
        CENTER(0X00000000),
        /** Specifies that the pen is positioned on the inside of the theoretical line. */
        INSET(0X00000001),
        /** Specifies that the pen is positioned to the left of the theoretical line. */
        LEFT(0X00000002),
        /** Specifies that the pen is positioned on the outside of the theoretical line. */
        OUTSET(0X00000003),
        /** Specifies that the pen is positioned to the right of the theoretical line. */
        RIGHT(0X00000004)
        ;

        public final int id;

        EmfPlusPenAlignment(int id) {
            this.id = id;
        }

        public static EmfPlusPenAlignment valueOf(int id) {
            for (EmfPlusPenAlignment wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }


    @Internal
    public static abstract class EmfPlusCustomLineCap implements GenericRecord {
        private EmfPlusLineCapType startCap;
        private EmfPlusLineCapType endCap;
        private EmfPlusLineJoin join;
        private double miterLimit;
        private double widthScale;
        private final Point2D fillHotSpot = new Point2D.Double();
        private final Point2D lineHotSpot = new Point2D.Double();

        protected long init(LittleEndianInputStream leis) throws IOException {
            // A 32-bit unsigned integer that specifies the value in the LineCap enumeration that indicates the line
            // cap used at the start/end of the line to be drawn.
            startCap = EmfPlusLineCapType.valueOf(leis.readInt());
            endCap = EmfPlusLineCapType.valueOf(leis.readInt());

            // A 32-bit unsigned integer that specifies the value in the LineJoin enumeration, which specifies how
            // to join two lines that are drawn by the same pen and whose ends meet. At the intersection of the two
            // line ends, a line join makes the connection look more continuous.
            join = EmfPlusLineJoin.valueOf(leis.readInt());

            // A 32-bit floating-point value that contains the limit of the thickness of the join on a mitered corner
            // by setting the maximum allowed ratio of miter length to line width.
            miterLimit = leis.readFloat();

            // A 32-bit floating-point value that specifies the amount by which to scale the custom line cap with
            // respect to the width of the EmfPlusPen object that is used to draw the lines.
            widthScale = leis.readFloat();

            long size = 5L*LittleEndianConsts.INT_SIZE;

            // An EmfPlusPointF object that is not currently used. It MUST be set to {0.0, 0.0}.
            size += readPointF(leis, fillHotSpot);
            size += readPointF(leis, lineHotSpot);

            return size;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            final Map<String,Supplier<?>> m = new LinkedHashMap<>();
            m.put("startCap", () -> startCap);
            m.put("endCap", () -> endCap);
            m.put("join", () -> join);
            m.put("miterLimit", () -> miterLimit);
            m.put("widthScale", () -> widthScale);
            m.put("fillHotSpot", () -> fillHotSpot);
            m.put("lineHotSpot", () -> lineHotSpot);
            return m;
        }

        @Override
        public final EmfPlusObjectType getGenericRecordType() {
            return EmfPlusObjectType.CUSTOM_LINE_CAP;
        }
    }


    public static class EmfPlusPen implements EmfPlusObjectData {
        /**
         * If set, a 2x3 transform matrix MUST be specified in the OptionalData field of an EmfPlusPenData object.
         */
        private static final BitField TRANSFORM = BitFieldFactory.getInstance(0x00000001);
        /**
         * If set, the style of a starting line cap MUST be specified in the OptionalData field of an
         * EmfPlusPenData object.
         */
        private static final BitField START_CAP = BitFieldFactory.getInstance(0x00000002);
        /**
         * Indicates whether the style of an ending line cap MUST be specified in the OptionalData field
         * of an EmfPlusPenData object.
         */
        private static final BitField END_CAP = BitFieldFactory.getInstance(0x00000004);
        /**
         * Indicates whether a line join type MUST be specified in the OptionalData
         * field of an EmfPlusPenData object.
         */
        private static final BitField JOIN = BitFieldFactory.getInstance(0x00000008);
        /**
         * Indicates whether a miter limit MUST be specified in the OptionalData field of an EmfPlusPenData object.
         */
        private static final BitField MITER_LIMIT = BitFieldFactory.getInstance(0x00000010);
        /**
         * Indicates whether a line style MUST be specified in the OptionalData field of an EmfPlusPenData object.
         */
        private static final BitField LINE_STYLE = BitFieldFactory.getInstance(0x00000020);
        /**
         * Indicates whether a dashed line cap MUST be specified in the OptionalData field of an EmfPlusPenData object.
         */
        private static final BitField DASHED_LINE_CAP = BitFieldFactory.getInstance(0x00000040);
        /**
         * Indicates whether a dashed line offset MUST be specified in the OptionalData field of an EmfPlusPenData object.
         */
        private static final BitField DASHED_LINE_OFFSET = BitFieldFactory.getInstance(0x00000080);
        /**
         * Indicates whether an EmfPlusDashedLineData object MUST be specified in the
         * OptionalData field of an EmfPlusPenData object.
         */
        private static final BitField DASHED_LINE = BitFieldFactory.getInstance(0x00000100);
        /**
         * Indicates whether a pen alignment MUST be specified in the OptionalData field of an EmfPlusPenData object.
         */
        private static final BitField NON_CENTER = BitFieldFactory.getInstance(0x00000200);
        /**
         * Indicates whether the length and content of a EmfPlusCompoundLineData object are present in the
         * OptionalData field of an EmfPlusPenData object.
         */
        private static final BitField COMPOUND_LINE = BitFieldFactory.getInstance(0x00000400);
        /**
         * Indicates whether an EmfPlusCustomStartCapData object MUST be specified
         * in the OptionalData field of an EmfPlusPenData object.y
         */
        private static final BitField CUSTOM_START_CAP = BitFieldFactory.getInstance(0x00000800);
        /**
         * Indicates whether an EmfPlusCustomEndCapData object MUST be specified in
         * the OptionalData field of an EmfPlusPenData object.
         */
        private static final BitField CUSTOM_END_CAP = BitFieldFactory.getInstance(0x00001000);

        private static final int[] FLAGS_MASKS = {
            0x00000001, 0x00000002, 0x00000004, 0x00000008, 0x00000010,
            0x00000020, 0x00000040, 0x00000080, 0x00000100, 0x00000200,
            0x00000400, 0x00000800, 0x00001000
        };

        private static final String[] FLAGS_NAMES = {
            "TRANSFORM", "START_CAP", "END_CAP", "JOIN", "MITER_LIMIT",
            "LINE_STYLE", "DASHED_LINE_CAP", "DASHED_LINE_OFFSET", "DASHED_LINE", "NON_CENTER",
            "COMPOUND_LINE", "CUSTOM_START_CAP", "CUSTOM_END_CAP"
        };

        private final EmfPlusGraphicsVersion graphicsVersion = new EmfPlusGraphicsVersion();


        private int type;
        private int penDataFlags;
        private EmfPlusUnitType unitType;
        private double penWidth;
        private final AffineTransform trans = new AffineTransform();
        private EmfPlusLineCapType startCap = EmfPlusLineCapType.FLAT;
        private EmfPlusLineCapType endCap = startCap;
        private EmfPlusLineJoin lineJoin = EmfPlusLineJoin.ROUND;
        private Double miterLimit = 1.;
        private EmfPlusLineStyle style = EmfPlusLineStyle.SOLID;
        private EmfPlusDashedLineCapType dashedLineCapType;
        private Double dashOffset;
        private float[] dashedLineData;
        private EmfPlusPenAlignment penAlignment;
        private double[] compoundLineData;
        private EmfPlusCustomLineCap customStartCap;
        private EmfPlusCustomLineCap customEndCap;

        private final EmfPlusBrush brush = new EmfPlusBrush();

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, EmfPlusObjectType objectType, int flags) throws IOException {
            // An EmfPlusGraphicsVersion object that specifies the version of operating system graphics that
            // was used to create this object.
            long size = graphicsVersion.init(leis);
            // This field MUST be set to zero.
            type = leis.readInt();
            // A 32-bit unsigned integer that specifies the data in the OptionalData field.
            // This value MUST be composed of PenData flags
            penDataFlags = leis.readInt();
            // A 32-bit unsigned integer that specifies the measuring units for the pen.
            // The value MUST be from the UnitType enumeration
            unitType = EmfPlusUnitType.valueOf(leis.readInt());
            // A 32-bit floating-point value that specifies the width of the line drawn by the pen in the units specified
            // by the PenUnit field. If a zero width is specified, a minimum value is used, which is determined by the units.
            penWidth = leis.readFloat();
            size += 4*LittleEndianConsts.INT_SIZE;

            if (TRANSFORM.isSet(penDataFlags)) {
                // An optional EmfPlusTransformMatrix object that specifies a world space to device space transform for
                // the pen. This field MUST be present if the PenDataTransform flag is set in the PenDataFlags field of
                // the EmfPlusPenData object.
                size += readXForm(leis, trans);
            }

            if (START_CAP.isSet(penDataFlags)) {
                // An optional 32-bit signed integer that specifies the shape for the start of a line in the
                // CustomStartCapData field. This field MUST be present if the PenDataStartCap flag is set in the
                // PenDataFlags field of the EmfPlusPenData object, and the value MUST be defined in the LineCapType enumeration
                startCap = EmfPlusLineCapType.valueOf(leis.readInt());
                size += LittleEndianConsts.INT_SIZE;
            }

            if (END_CAP.isSet(penDataFlags)) {
                // An optional 32-bit signed integer that specifies the shape for the end of a line in the
                // CustomEndCapData field. This field MUST be present if the PenDataEndCap flag is set in the
                // PenDataFlags field of the EmfPlusPenData object, and the value MUST be defined in the LineCapType enumeration.
                endCap = EmfPlusLineCapType.valueOf(leis.readInt());
                size += LittleEndianConsts.INT_SIZE;
            }

            if (JOIN.isSet(penDataFlags)) {
                // An optional 32-bit signed integer that specifies how to join two lines that are drawn by the same pen
                // and whose ends meet. This field MUST be present if the PenDataJoin flag is set in the PenDataFlags
                // field of the EmfPlusPenData object, and the value MUST be defined in the LineJoinType enumeration
                lineJoin = EmfPlusLineJoin.valueOf(leis.readInt());
                size += LittleEndianConsts.INT_SIZE;
            }

            if (MITER_LIMIT.isSet(penDataFlags)) {
                // An optional 32-bit floating-point value that specifies the miter limit, which is the maximum allowed
                // ratio of miter length to line width. The miter length is the distance from the intersection of the
                // line walls on the inside the join to the intersection of the line walls outside the join. The miter
                // length can be large when the angle between two lines is small. This field MUST be present if the
                // PenDataMiterLimit flag is set in the PenDataFlags field of the EmfPlusPenData object.
                miterLimit = (double)leis.readFloat();
                size += LittleEndianConsts.INT_SIZE;
            }

            if (LINE_STYLE.isSet(penDataFlags)) {
                // An optional 32-bit signed integer that specifies the style used for lines drawn with this pen object.
                // This field MUST be present if the PenDataLineStyle flag is set in the PenDataFlags field of the
                // EmfPlusPenData object, and the value MUST be defined in the LineStyle enumeration
                style = EmfPlusLineStyle.valueOf(leis.readInt());
                size += LittleEndianConsts.INT_SIZE;
            }

            if (DASHED_LINE_CAP.isSet(penDataFlags)) {
                // An optional 32-bit signed integer that specifies the shape for both ends of each dash in a dashed line.
                // This field MUST be present if the PenDataDashedLineCap flag is set in the PenDataFlags field of the
                // EmfPlusPenData object, and the value MUST be defined in the DashedLineCapType enumeration
                dashedLineCapType = EmfPlusDashedLineCapType.valueOf(leis.readInt());
                size += LittleEndianConsts.INT_SIZE;
            }

            if (DASHED_LINE_OFFSET.isSet(penDataFlags)) {
                // An optional 32-bit floating-point value that specifies the distance from the start of a line to the
                // start of the first space in a dashed line pattern. This field MUST be present if the
                // PenDataDashedLineOffset flag is set in the PenDataFlags field of the EmfPlusPenData object.
                dashOffset = (double)leis.readFloat();
                size += LittleEndianConsts.INT_SIZE;
            }

            if (DASHED_LINE.isSet(penDataFlags)) {
                // A 32-bit unsigned integer that specifies the number of elements in the DashedLineData field.
                int dashesSize = leis.readInt();
                if (dashesSize < 0 || dashesSize > 1000) {
                    throw new IllegalStateException("Invalid dash data size");
                }

                // An array of DashedLineDataSize floating-point values that specify the lengths of the dashes and spaces in a dashed line.
                dashedLineData = new float[dashesSize];
                for (int i=0; i<dashesSize; i++) {
                    dashedLineData[i] = leis.readFloat();
                }

                size += LittleEndianConsts.INT_SIZE * (dashesSize+1);
            }

            if (NON_CENTER.isSet(penDataFlags)) {
                // An optional 32-bit signed integer that specifies the distribution of the pen width with respect to
                // the coordinates of the line being drawn. This field MUST be present if the PenDataNonCenter flag is
                // set in the PenDataFlags field of the EmfPlusPenData object, and the value MUST be defined in the
                // PenAlignment enumeration
                penAlignment = EmfPlusPenAlignment.valueOf(leis.readInt());
                size += LittleEndianConsts.INT_SIZE;
            }

            if (COMPOUND_LINE.isSet(penDataFlags)) {
                // A 32-bit unsigned integer that specifies the number of elements in the CompoundLineData field.
                int compoundSize = leis.readInt();
                if (compoundSize < 0 || compoundSize > 1000) {
                    throw new IllegalStateException("Invalid compound line data size");
                }

                // An array of CompoundLineDataSize floating-point values that specify the compound line of a pen.
                // The elements MUST be in increasing order, and their values MUST be between 0.0 and 1.0, inclusive.
                compoundLineData = new double[compoundSize];

                for (int i=0; i<compoundSize; i++) {
                    compoundLineData[i] = leis.readFloat();
                }
                size += LittleEndianConsts.INT_SIZE * (compoundSize+1);
            }

            if (CUSTOM_START_CAP.isSet(penDataFlags)) {
                size += initCustomCap(c -> customStartCap = c, leis);
            }

            if (CUSTOM_END_CAP.isSet(penDataFlags)) {
                size += initCustomCap(c -> customEndCap = c, leis);
            }

            size += brush.init(leis, dataSize-size, EmfPlusObjectType.BRUSH, 0);

            return size;
        }

        @Override
        public EmfPlusGraphicsVersion getGraphicsVersion() {
            return graphicsVersion;
        }

        @SuppressWarnings("unused")
        private long initCustomCap(Consumer<EmfPlusCustomLineCap> setter, LittleEndianInputStream leis) throws IOException {
            int CustomStartCapSize = leis.readInt();
            long size = LittleEndianConsts.INT_SIZE;

            EmfPlusGraphicsVersion version = new EmfPlusGraphicsVersion();
            size += version.init(leis);
            assert(version.getGraphicsVersion() != null);

            boolean adjustableArrow = (leis.readInt() != 0);
            size += LittleEndianConsts.INT_SIZE;

            EmfPlusCustomLineCap cap = (adjustableArrow) ? new EmfPlusAdjustableArrowCap() : new EmfPlusPathArrowCap();
            size += cap.init(leis);

            setter.accept(cap);

            return Math.toIntExact(size);
        }

        @Override
        public void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {
            final HemfDrawProperties prop = ctx.getProperties();
            // TODO:
            // - set width according unit type
            // - provide logic for different start and end cap
            // - provide standard caps like diamond
            // - support custom caps

            brush.applyPen(ctx, continuedObjectData);
            prop.setPenWidth(penWidth);

            HwmfLineCap cap;
            // ignore endCap for now
            switch(startCap) {
                default:
                case FLAT:
                    cap = HwmfLineCap.FLAT;
                    break;
                case ROUND:
                    cap = HwmfLineCap.ROUND;
                    break;
                case SQUARE:
                    cap = HwmfLineCap.SQUARE;
                    break;
            }

            HwmfLineJoin lineJoin;
            switch (this.lineJoin) {
                default:
                case BEVEL:
                    lineJoin = HwmfLineJoin.BEVEL;
                    break;
                case ROUND:
                    lineJoin = HwmfLineJoin.ROUND;
                    break;
                case MITER_CLIPPED:
                case MITER:
                    lineJoin = HwmfLineJoin.MITER;
                    break;
            }

            HwmfLineDash lineDash = (dashedLineData == null) ? HwmfLineDash.SOLID : HwmfLineDash.USERSTYLE;

            boolean isAlternate = (lineDash != HwmfLineDash.SOLID && dashOffset != null && dashOffset == 0);
            boolean isGeometric = (unitType == EmfPlusUnitType.World || unitType == EmfPlusUnitType.Display);
            HemfPenStyle penStyle = HemfPenStyle.valueOf(cap, lineJoin, lineDash, isAlternate, isGeometric);
            penStyle.setLineDashes(dashedLineData);

            prop.setPenStyle(penStyle);
        }

        @Override
        public EmfPlusObjectType getGenericRecordType() {
            return EmfPlusObjectType.PEN;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            final Map<String,Supplier<?>> m = new LinkedHashMap<>();
            m.put("type", () -> type);
            m.put("flags", getBitsAsString(() -> penDataFlags, FLAGS_MASKS, FLAGS_NAMES));
            m.put("unitType", () -> unitType);
            m.put("penWidth", () -> penWidth);
            m.put("trans", () -> trans);
            m.put("startCap", () -> startCap);
            m.put("endCap", () -> endCap);
            m.put("join", () -> lineJoin);
            m.put("miterLimit", () -> miterLimit);
            m.put("style", () -> style);
            m.put("dashedLineCapType", () -> dashedLineCapType);
            m.put("dashOffset", () -> dashOffset);
            m.put("dashedLineData", () -> dashedLineData);
            m.put("penAlignment", () -> penAlignment);
            m.put("compoundLineData", () -> compoundLineData);
            m.put("customStartCap", () -> customStartCap);
            m.put("customEndCap", () -> customEndCap);
            m.put("brush", () -> brush);
            return Collections.unmodifiableMap(m);
        }
    }

    public static class EmfPlusPathArrowCap extends EmfPlusCustomLineCap {
        /**
         * If set, an EmfPlusFillPath object MUST be specified in the OptionalData field of the
         * EmfPlusCustomLineCapData object for filling the custom line cap.
         */
        private static final BitField FILL_PATH = BitFieldFactory.getInstance(0x00000001);
        /**
         * If set, an EmfPlusLinePath object MUST be specified in the OptionalData field of the
         * EmfPlusCustomLineCapData object for outlining the custom line cap.
         */
        private static final BitField LINE_PATH = BitFieldFactory.getInstance(0x00000002);

        private static final int[] FLAGS_MASKS = { 0x00000001, 0x00000002 };

        private static final String[] FLAGS_NAMES = { "FILL_PATH", "LINE_PATH" };

        private int dataFlags;
        private EmfPlusLineCapType baseCap;
        private double baseInset;
        private EmfPlusPath fillPath;
        private EmfPlusPath outlinePath;

        @Override
        public long init(LittleEndianInputStream leis) throws IOException {
            // A 32-bit unsigned integer that specifies the data in the OptionalData field.
            // This value MUST be composed of CustomLineCapData flags
            dataFlags = leis.readInt();

            // A 32-bit unsigned integer that specifies the value from the LineCap enumeration on which
            // the custom line cap is based.
            baseCap = EmfPlusLineCapType.valueOf(leis.readInt());

            // A 32-bit floating-point value that specifies the distance between the
            // beginning of the line cap and the end of the line.
            baseInset = leis.readFloat();

            long size = 3L*LittleEndianConsts.INT_SIZE;

            size += super.init(leis);

            size += initPath(leis, FILL_PATH, p -> fillPath = p);
            size += initPath(leis, LINE_PATH, p -> outlinePath = p);

            return size;
        }

        private long initPath(LittleEndianInputStream leis, BitField bitField, Consumer<EmfPlusPath> setter) throws IOException {
            if (!bitField.isSet(dataFlags)) {
                return 0;
            }
            int pathSize = leis.readInt();
            EmfPlusPath path = new EmfPlusPath();
            setter.accept(path);
            return LittleEndianConsts.INT_SIZE + path.init(leis, pathSize, EmfPlusObjectType.PATH, -1);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "flags", getBitsAsString(() -> dataFlags, FLAGS_MASKS, FLAGS_NAMES),
                "baseCap", () -> baseCap,
                "baseInset", () -> baseInset,
                "base", super::getGenericProperties,
                "fillPath", () -> fillPath,
                "outlinePath", () -> outlinePath
            );
        }
    }

    public static class EmfPlusAdjustableArrowCap extends EmfPlusCustomLineCap {
        private double width;
        private double height;
        private double middleInset;
        private boolean isFilled;

        @Override
        public long init(LittleEndianInputStream leis) throws IOException {
            // A 32-bit floating-point value that specifies the width of the arrow cap.
            // The width of the arrow cap is scaled by the width of the EmfPlusPen object that is used to draw the
            // line being capped. For example, when drawing a capped line with a pen that has a width of 5 pixels,
            // and the adjustable arrow cap object has a width of 3, the actual arrow cap is drawn 15 pixels wide.
            width = leis.readFloat();

            // A 32-bit floating-point value that specifies the height of the arrow cap.
            // The height of the arrow cap is scaled by the width of the EmfPlusPen object that is used to draw the
            // line being capped. For example, when drawing a capped line with a pen that has a width of 5 pixels,
            // and the adjustable arrow cap object has a height of 3, the actual arrow cap is drawn 15 pixels high.
            height = leis.readFloat();

            // A 32-bit floating-point value that specifies the number of pixels between the outline of the arrow
            // cap and the fill of the arrow cap.
            middleInset = leis.readFloat();

            // A 32-bit Boolean value that specifies whether the arrow cap is filled.
            // If the arrow cap is not filled, only the outline is drawn.
            isFilled = (leis.readInt() != 0);

            return 4*LittleEndianConsts.INT_SIZE + super.init(leis);
        }


        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "width", () -> width,
                "height", () -> height,
                "middleInset", () -> middleInset,
                "isFilled", () -> isFilled,
                "base", super::getGenericProperties
            );
        }
    }
}
