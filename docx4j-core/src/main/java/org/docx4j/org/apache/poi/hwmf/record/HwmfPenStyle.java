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

import java.awt.BasicStroke;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.common.Duplicatable;
import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.util.BitField;
import org.docx4j.org.apache.poi.util.BitFieldFactory;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;

/**
 * The 16-bit PenStyle Enumeration is used to specify different types of pens
 * that can be used in graphics operations.
 *
 * Various styles can be combined by using a logical OR statement, one from
 * each subsection of Style, EndCap, Join, and Type (Cosmetic).
 *
 * The defaults in case the other values of the subsection aren't set are
 * solid, round end caps, round joins and cosmetic type.
 */
public class HwmfPenStyle implements Duplicatable, GenericRecord {
    public enum HwmfLineCap {
        /** Rounded ends */
        ROUND(0, BasicStroke.CAP_ROUND),
        /** Square protrudes by half line width */
        SQUARE(1, BasicStroke.CAP_SQUARE),
        /** Line ends at end point*/
        FLAT(2, BasicStroke.CAP_BUTT);

        public final int wmfFlag;
        public final int awtFlag;
        HwmfLineCap(int wmfFlag, int awtFlag) {
            this.wmfFlag = wmfFlag;
            this.awtFlag = awtFlag;
        }

        static HwmfLineCap valueOf(int wmfFlag) {
            for (HwmfLineCap hs : values()) {
                if (hs.wmfFlag == wmfFlag) return hs;
            }
            return null;
        }
    }

    public enum HwmfLineJoin {
        /**Line joins are round. */
        ROUND(0, BasicStroke.JOIN_ROUND),
        /** Line joins are beveled. */
        BEVEL(1, BasicStroke.JOIN_BEVEL),
        /**
         * Line joins are mitered when they are within the current limit set by the
         * SETMITERLIMIT META_ESCAPE record. A join is beveled when it would exceed the limit
         */
        MITER(2, BasicStroke.JOIN_MITER);

        public final int wmfFlag;
        public final int awtFlag;
        HwmfLineJoin(int wmfFlag, int awtFlag) {
            this.wmfFlag = wmfFlag;
            this.awtFlag = awtFlag;
        }

        static HwmfLineJoin valueOf(int wmfFlag) {
            for (HwmfLineJoin hs : values()) {
                if (hs.wmfFlag == wmfFlag) return hs;
            }
            return null;
        }
    }

    public enum HwmfLineDash {
        /**
         * The pen is solid.
         */
        SOLID(0x0000, null),
        /**
         * The pen is dashed. (-----)
         */
        DASH(0x0001, 10, 8),
        /**
         * The pen is dotted. (.....)
         */
        DOT(0x0002, 2, 4),
        /**
         * The pen has alternating dashes and dots. (_._._._)
         */
        DASHDOT(0x0003, 10, 8, 2, 8),
        /**
         * The pen has dashes and double dots. (_.._.._)
         */
        DASHDOTDOT(0x0004, 10, 4, 2, 4, 2, 4),
        /**
         * The pen is invisible.
         */
        NULL(0x0005, null),
        /**
         * The pen is solid. When this pen is used in any drawing record that takes a
         * bounding rectangle, the dimensions of the figure are shrunk so that it fits
         * entirely in the bounding rectangle, taking into account the width of the pen.
         */
        INSIDEFRAME(0x0006, null),
        /**
         * The pen uses a styling array supplied by the user.
         * (this is currently not supported and drawn as solid ... no idea where the user
         * styling is supposed to come from ...)
         */
        USERSTYLE(0x0007, null);


        public final int wmfFlag;
        public final float[] dashes;

        HwmfLineDash(int wmfFlag, float... dashes) {
            this.wmfFlag = wmfFlag;
            this.dashes = dashes;
        }

        static HwmfLineDash valueOf(int wmfFlag) {
            for (HwmfLineDash hs : values()) {
                if (hs.wmfFlag == wmfFlag) return hs;
            }
            return null;
        }
    }

    protected static final BitField SUBSECTION_DASH      = BitFieldFactory.getInstance(0x00007);
    protected static final BitField SUBSECTION_ALTERNATE = BitFieldFactory.getInstance(0x00008);
    protected static final BitField SUBSECTION_ENDCAP    = BitFieldFactory.getInstance(0x00300);
    protected static final BitField SUBSECTION_JOIN      = BitFieldFactory.getInstance(0x03000);
    protected static final BitField SUBSECTION_GEOMETRIC = BitFieldFactory.getInstance(0x10000);

    protected int flag;

    public HwmfPenStyle(int flag) {
        this.flag = flag;
    }

    public HwmfPenStyle(HwmfPenStyle other) {
        flag = other.flag;
    }

    public static HwmfPenStyle valueOf(int flag) {
        return new HwmfPenStyle(flag);
    }

    public HwmfLineCap getLineCap() {
        return HwmfLineCap.valueOf(SUBSECTION_ENDCAP.getValue(flag));
    }

    public HwmfLineJoin getLineJoin() {
        return HwmfLineJoin.valueOf(SUBSECTION_JOIN.getValue(flag));
    }

    public HwmfLineDash getLineDash() {
        return HwmfLineDash.valueOf(SUBSECTION_DASH.getValue(flag));
    }

    /**
     * Convienence method which should be used instead of accessing {@link HwmfLineDash#dashes}
     * directly, so an subclass can provide user-style dashes
     *
     * @return the dash pattern
     */
    public float[] getLineDashes() {
        return getLineDash().dashes;
    }

    /**
     * The pen sets every other pixel (this style is applicable only for cosmetic pens).
     */
    public boolean isAlternateDash() {
        return SUBSECTION_ALTERNATE.isSet(flag);
    }

    /**
     * A pen type that specifies a line with a width that is measured in logical units
     * and a style that can contain any of the attributes of a brush.
     */
    public boolean isGeometric()  {
        return SUBSECTION_GEOMETRIC.isSet(flag);
    }


    @Override
    public HwmfPenStyle copy() {
        return new HwmfPenStyle(this);
    }

    @Override
    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "lineCap", this::getLineCap,
            "lineJoin", this::getLineJoin,
            "lineDash", this::getLineDash,
            "lineDashes", this::getLineDashes,
            "alternateDash", this::isAlternateDash,
            "geometric", this::isGeometric
        );
    }
}
