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

import static org.docx4j.org.apache.poi.hemf.record.emf.HemfDraw.readDimensionInt;
import static org.docx4j.org.apache.poi.hemf.record.emf.HemfDraw.readPointL;
import static org.docx4j.org.apache.poi.hwmf.record.HwmfDraw.normalizeBounds;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.hemf.draw.HemfDrawProperties;
import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hwmf.record.HwmfRegionMode;
import org.docx4j.org.apache.poi.hwmf.record.HwmfWindowing;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

@SuppressWarnings("WeakerAccess")
public class HemfWindowing {

    /**
     * The EMR_SETWINDOWEXTEX record defines the window extent.
     */
    public static class EmfSetWindowExtEx extends HwmfWindowing.WmfSetWindowExt implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setWindowExtEx;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readDimensionInt(leis, size);
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }

        @Override
        public void calcBounds(RenderBounds holder) {
            Rectangle2D window = holder.getWindow();
            double x = window.getX();
            double y = window.getY();
            window.setRect(x,y,size.getWidth(),size.getHeight());
        }
    }

    /**
     * The EMR_SETWINDOWORGEX record defines the window origin.
     */
    public static class EmfSetWindowOrgEx extends HwmfWindowing.WmfSetWindowOrg implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setWindowOrgEx;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readPointL(leis, origin);
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }

        @Override
        public void calcBounds(RenderBounds holder) {
            Rectangle2D window = holder.getWindow();
            double w = window.getWidth();
            double h = window.getHeight();
            window.setRect(origin.getX(),origin.getY(),w,h);
        }
    }

    /**
     * The EMR_SETVIEWPORTEXTEX record defines the viewport extent.
     */
    public static class EmfSetViewportExtEx extends HwmfWindowing.WmfSetViewportExt implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setViewportExtEx;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readDimensionInt(leis, extents);
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }

        @Override
        public void calcBounds(RenderBounds holder) {
            Rectangle2D viewport = holder.getViewport();
            double x = viewport.getX();
            double y = viewport.getY();
            viewport.setRect(x,y,extents.getWidth(),extents.getHeight());
        }
    }

    /**
     * The EMR_SETVIEWPORTORGEX record defines the viewport origin.
     */
    public static class EmfSetViewportOrgEx extends HwmfWindowing.WmfSetViewportOrg implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setViewportOrgEx;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readPointL(leis, origin);
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }

        @Override
        public void calcBounds(RenderBounds holder) {
            Rectangle2D viewport = holder.getViewport();
            double w = viewport.getWidth();
            double h = viewport.getHeight();
            viewport.setRect(origin.getX(), origin.getY(), w, h);
        }
    }

    /**
     * The EMR_OFFSETCLIPRGN record moves the current clipping region in the playback device context
     * by the specified offsets.
     */
    public static class EmfSetOffsetClipRgn extends HwmfWindowing.WmfOffsetClipRgn implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setOffsetClipRgn;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readPointL(leis, offset);
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_EXCLUDECLIPRECT record specifies a new clipping region that consists of the existing
     * clipping region minus the specified rectangle.
     */
    public static class EmfSetExcludeClipRect extends HwmfWindowing.WmfExcludeClipRect implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setExcludeClipRect;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return HemfDraw.readRectL(leis, bounds);
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_INTERSECTCLIPRECT record specifies a new clipping region from the intersection of the
     * current clipping region and the specified rectangle.
     */
    public static class EmfSetIntersectClipRect extends HwmfWindowing.WmfIntersectClipRect implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setIntersectClipRect;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return HemfDraw.readRectL(leis, normalizeBounds(bounds));
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }

        @Override
        public void calcBounds(RenderBounds holder) {
            Rectangle2D b = holder.getBounds();
            if (b.isEmpty()) {
                b.setRect(bounds);
            } else {
                b.add(bounds);
            }
        }
    }

    /**
     * The EMR_SCALEVIEWPORTEXTEX record respecifies the viewport for a device context by using the
     * ratios formed by the specified multiplicands and divisors.
     */
    public static class EmfScaleViewportExtEx extends HwmfWindowing.WmfScaleViewportExt implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.scaleViewportExtEx;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readScale(leis, scale);
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }

        @Override
        public void calcBounds(RenderBounds holder) {
            Rectangle2D viewport = holder.getViewport();
            double x = viewport.getX();
            double y = viewport.getY();
            double w = viewport.getWidth();
            double h = viewport.getHeight();
            viewport.setRect(x,y,w * scale.getWidth(),h * scale.getHeight());
        }

    }

    /**
     * The EMR_SCALEWINDOWEXTEX record respecifies the window for a playback device context by
     * using the ratios formed by the specified multiplicands and divisors.
     */
    public static class EmfScaleWindowExtEx extends HwmfWindowing.WmfScaleWindowExt implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.scaleWindowExtEx;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readScale(leis, scale);
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }

        @Override
        public void calcBounds(RenderBounds holder) {
            Rectangle2D window = holder.getWindow();
            double x = window.getX();
            double y = window.getY();
            double w = window.getWidth();
            double h = window.getHeight();
            window.setRect(x,y,w * scale.getWidth(),h * scale.getHeight());
        }
    }

    /**
     * The EMR_SELECTCLIPPATH record specifies the current path as a clipping region for a playback
     * device context, combining the new region with any existing clipping region using the specified mode.
     */
    public static class EmfSelectClipPath implements HemfRecord {
        protected HwmfRegionMode regionMode;

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.selectClipPath;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer that specifies the way to use the path.
            // The value MUST be in the RegionMode enumeration
            regionMode = HwmfRegionMode.valueOf(leis.readInt());

            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            HemfDrawProperties prop = ctx.getProperties();
            ctx.setClip(prop.getPath(), regionMode, false);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public HwmfRegionMode getRegionMode() {
            return regionMode;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("regionMode", this::getRegionMode);
        }
    }

    private static int readScale(LittleEndianInputStream leis, Dimension2D scale) {
        double xNum = leis.readInt();
        double xDenom = leis.readInt();
        double yNum = leis.readInt();
        double yDenom = leis.readInt();
        scale.setSize(xNum / xDenom, yNum / yDenom);
        return 4*LittleEndianConsts.INT_SIZE;
    }
}