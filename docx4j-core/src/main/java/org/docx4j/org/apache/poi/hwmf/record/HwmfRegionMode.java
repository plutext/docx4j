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

import java.awt.Shape;
import java.awt.geom.Area;
import java.util.function.BiFunction;

public enum HwmfRegionMode {
    /**
     * The new clipping region includes the intersection (overlapping areas)
     * of the current clipping region and the current path (or new region).
     */
    RGN_AND(0x01, HwmfRegionMode::andOp),
    /**
     * The new clipping region includes the union (combined areas)
     * of the current clipping region and the current path (or new region).
     */
    RGN_OR(0x02, HwmfRegionMode::orOp),
    /**
     * The new clipping region includes the union of the current clipping region
     * and the current path (or new region) but without the overlapping areas
     */
    RGN_XOR(0x03, HwmfRegionMode::xorOp),
    /**
     * The new clipping region includes the areas of the current clipping region
     * with those of the current path (or new region) excluded.
     */
    RGN_DIFF(0x04, HwmfRegionMode::diffOp),
    /**
     * The new clipping region is the current path (or the new region).
     */
    RGN_COPY(0x05, HwmfRegionMode::copyOp),
    /**
     * This is the opposite of {@link #RGN_DIFF}, and only made-up for compatibility with EMF+
     */
    RGN_COMPLEMENT(-1, HwmfRegionMode::complementOp);

    private final int flag;
    private final BiFunction<Shape,Shape,Shape> op;

    HwmfRegionMode(int flag, BiFunction<Shape,Shape,Shape> op) {
        this.flag = flag;
        this.op = op;
    }

    public static HwmfRegionMode valueOf(int flag) {
        for (HwmfRegionMode rm : values()) {
            if (rm.flag == flag) return rm;
        }
        return null;
    }

    public int getFlag() {
        return flag;
    }

    public Shape applyOp(Shape oldClip, Shape newClip) {
        return op.apply(oldClip, newClip);
    }

    private static Shape andOp(final Shape oldClip, final Shape newClip) {
        assert(newClip != null);
        if (newClip.getBounds2D().isEmpty()) {
            return oldClip;
        } else if (oldClip == null) {
            return newClip;
        } else {
            Area newArea = new Area(oldClip);
            newArea.intersect(new Area(newClip));
            return newArea.getBounds2D().isEmpty() ? newClip : newArea;
        }
    }

    private static Shape orOp(final Shape oldClip, final Shape newClip) {
        assert(newClip != null);
        if (newClip.getBounds2D().isEmpty()) {
            return oldClip;
        } else if (oldClip == null) {
            return newClip;
        } else {
            Area newArea = new Area(oldClip);
            newArea.add(new Area(newClip));
            return newArea;
        }
    }

    private static Shape xorOp(final Shape oldClip, final Shape newClip) {
        assert(newClip != null);
        if (newClip.getBounds2D().isEmpty()) {
            return oldClip;
        } else if (oldClip == null) {
            return newClip;
        } else {
            Area newArea = new Area(oldClip);
            newArea.exclusiveOr(new Area(newClip));
            return newArea;
        }
    }

    private static Shape diffOp(final Shape oldClip, final Shape newClip) {
        assert(newClip != null);
        if (newClip.getBounds2D().isEmpty()) {
            return oldClip;
        } else if (oldClip == null) {
            return newClip;
        } else {
            Area newArea = new Area(oldClip);
            newArea.subtract(new Area(newClip));
            return newArea;
        }
    }

    private static Shape copyOp(final Shape oldClip, final Shape newClip) {
        return (newClip == null || newClip.getBounds2D().isEmpty()) ? null : newClip;
    }

    private static Shape complementOp(final Shape oldClip, final Shape newClip) {
        assert(newClip != null);
        if (newClip.getBounds2D().isEmpty()) {
            return oldClip;
        } else if (oldClip == null) {
            return newClip;
        } else {
            Area newArea = new Area(newClip);
            newArea.subtract(new Area(oldClip));
            return newArea;
        }
    }
}
