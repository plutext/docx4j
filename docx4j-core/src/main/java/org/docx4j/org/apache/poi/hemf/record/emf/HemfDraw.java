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

import static org.docx4j.org.apache.poi.hwmf.record.HwmfDraw.normalizeBounds;
import static org.docx4j.org.apache.poi.util.GenericRecordUtil.getEnumBitsAsString;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.docx4j.org.apache.poi.hemf.draw.HemfDrawProperties;
import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfGraphics.FillDrawStyle;
import org.docx4j.org.apache.poi.hwmf.record.HwmfDraw;
import org.docx4j.org.apache.poi.hwmf.record.HwmfDraw.WmfSelectObject;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

public final class HemfDraw {
    // arbitrary limit to avoid OOM on malformed files. This may need increasing if "normal" files have more than this
    public static final int MAX_NUMBER_OF_POLYGONS = 100_000;

    private HemfDraw() {}

    /**
     * The EMR_SELECTOBJECT record adds a graphics object to the current metafile playback device
     * context. The object is specified either by its index in the EMF Object Table or by its
     * value from the StockObject enumeration.
     */
    public static class EmfSelectObject extends WmfSelectObject implements HemfRecord {

        private static final int[] IDX_MASKS = IntStream.rangeClosed(0x80000000,0x80000013).toArray();

        private static final String[] IDX_NAMES = {
            "WHITE_BRUSH",
            "LTGRAY_BRUSH",
            "GRAY_BRUSH",
            "DKGRAY_BRUSH",
            "BLACK_BRUSH",
            "NULL_BRUSH",
            "WHITE_PEN",
            "BLACK_PEN",
            "NULL_PEN",
            // 0x80000009 is not a valid stock object
            "INVALID",
            "OEM_FIXED_FONT",
            "ANSI_FIXED_FONT",
            "ANSI_VAR_FONT",
            "SYSTEM_FONT",
            "DEVICE_DEFAULT_FONT",
            "DEFAULT_PALETTE",
            "SYSTEM_FIXED_FONT",
            "DEFAULT_GUI_FONT",
            "DC_BRUSH",
            "DC_PEN"
        };

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.selectObject;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer that specifies either the index of a graphics object in the
            // EMF Object Table or the index of a stock object from the StockObject enumeration.
            objectIndex = leis.readInt();
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "objectIndex", getEnumBitsAsString(this::getObjectIndex, IDX_MASKS, IDX_NAMES)
            );
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }


    /** The EMR_POLYBEZIER record specifies one or more Bezier curves. */
    public static class EmfPolyBezier extends HwmfDraw.WmfPolygon implements HemfRecord {
        private final Rectangle2D bounds = new Rectangle2D.Double();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyBezier;
        }

        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointL(leis, point);
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);

            /* A 32-bit unsigned integer that specifies the number of points in the points
             * array. This value MUST be one more than three times the number of curves to
             * be drawn, because each Bezier curve requires two control points and an
             * endpoint, and the initial curve requires an additional starting point.
             *
             * Line width | Device supports wideline | Maximum points allowed
             *    1       |            n/a           |          16K
             *   > 1      |            yes           |          16K
             *   > 1      |             no           |         1360
             *
             * Any extra points MUST be ignored.
             */
            final int count = (int)leis.readUInt();
            final int points = Math.min(count, 16384);
            size += LittleEndianConsts.INT_SIZE;

            poly = new Path2D.Double(Path2D.WIND_EVEN_ODD, points+2);

            /* Cubic Bezier curves are defined using the endpoints and control points
             * specified by the points field. The first curve is drawn from the first
             * point to the fourth point, using the second and third points as control
             * points. Each subsequent curve in the sequence needs exactly three more points:
             * the ending point of the previous curve is used as the starting point,
             * the next two points in the sequence are control points,
             * and the third is the ending point.
             * The cubic Bezier curves SHOULD be drawn using the current pen.
             */

            Point2D[] pnt = {new Point2D.Double(), new Point2D.Double(), new Point2D.Double()};

            int i=0;
            if (hasStartPoint()) {
                if (i < points) {
                    size += readPoint(leis, pnt[0]);
                    poly.moveTo(pnt[0].getX(), pnt[0].getY());
                    i++;
                }
            } else {
                poly.moveTo(0, 0);
            }

            for (; i+2<points; i+=3) {
                size += readPoint(leis, pnt[0]);
                size += readPoint(leis, pnt[1]);
                size += readPoint(leis, pnt[2]);

                poly.curveTo(
                    pnt[0].getX(),pnt[0].getY(),
                    pnt[1].getX(),pnt[1].getY(),
                    pnt[2].getX(),pnt[2].getY()
                );
            }

            return size;
        }

        /**
         * @return true, if start point is in the list of points. false, if start point is taken from the context
         */
        protected boolean hasStartPoint() {
            return true;
        }

        @Override
        protected FillDrawStyle getFillDrawStyle() {
            // The cubic Bezier curves SHOULD be drawn using the current pen.
            return FillDrawStyle.DRAW;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.draw(path -> path.append(poly, !hasStartPoint()), getFillDrawStyle());
        }

        public Rectangle2D getBounds() {
            return bounds;
        }

        @Override
        protected boolean addClose() {
            return false;
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
     * The EMR_POLYBEZIER16 record specifies one or more Bezier curves.
     * The curves are drawn using the current pen.
     */
    public static class EmfPolyBezier16 extends EmfPolyBezier {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyBezier16;
        }

        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }


    /**
     * The EMR_POLYGON record specifies a polygon consisting of two or more vertexes connected by
     * straight lines.
     */
    public static class EmfPolygon extends HwmfDraw.WmfPolygon implements HemfRecord {
        private final Rectangle2D bounds = new Rectangle2D.Double();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polygon;
        }

        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointL(leis, point);
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);

            // see PolyBezier about limits
            final int count = (int)leis.readUInt();
            final int points = Math.min(count, 16384);
            size += LittleEndianConsts.INT_SIZE;

            poly = new Path2D.Double(Path2D.WIND_EVEN_ODD, points);

            Point2D pnt = new Point2D.Double();
            for (int i=0; i<points; i++) {
                size += readPoint(leis, pnt);
                if (i==0) {
                    if (hasStartPoint()) {
                        poly.moveTo(pnt.getX(), pnt.getY());
                    } else {
                        // if this path is connected to the current position (= has no start point)
                        // the first entry is a dummy entry and will be skipped later
                        poly.moveTo(0,0);
                        poly.lineTo(pnt.getX(), pnt.getY());
                    }
                } else {
                    poly.lineTo(pnt.getX(), pnt.getY());
                }
            }

            return size;
        }

        /**
         * @return true, if start point is in the list of points. false, if start point is taken from the context
         */
        protected boolean hasStartPoint() {
            return true;
        }

        @Override
        protected FillDrawStyle getFillDrawStyle() {
            // The polygon SHOULD be outlined using the current pen and filled using the current brush and
            // polygon fill mode. The polygon SHOULD be closed automatically by drawing a line from the last
            // vertex to the first.
            return FillDrawStyle.FILL_DRAW;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.draw(path -> path.append(poly, false), getFillDrawStyle());
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
     * The EMR_POLYGON16 record specifies a polygon consisting of two or more vertexes connected by straight lines.
     * The polygon is outlined by using the current pen and filled by using the current brush and polygon fill mode.
     * The polygon is closed automatically by drawing a line from the last vertex to the first
     */
    public static class EmfPolygon16 extends EmfPolygon {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polygon16;
        }

        @Override
        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }

    /**
     * The EMR_POLYLINE record specifies a series of line segments by connecting the points in the
     * specified array.
     */
    public static class EmfPolyline extends EmfPolygon {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyline;
        }

        @Override
        protected FillDrawStyle getFillDrawStyle() {
            // The line segments SHOULD be drawn using the current pen.
            return FillDrawStyle.DRAW;
        }

        @Override
        protected boolean addClose() {
            return false;
        }
    }

    /**
     * The EMR_POLYLINE16 record specifies a series of line segments by connecting the points in the
     * specified array.
     */
    public static class EmfPolyline16 extends EmfPolyline {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyline16;
        }

        @Override
        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }

    /**
     * The EMR_POLYBEZIERTO record specifies one or more Bezier curves based upon the current
     * position.
     */
    public static class EmfPolyBezierTo extends EmfPolyBezier {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyBezierTo;
        }

        @Override
        protected boolean hasStartPoint() {
            return false;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            polyTo(ctx, poly, getFillDrawStyle());
        }
    }

    /**
     * The EMR_POLYBEZIERTO16 record specifies one or more Bezier curves based on the current
     * position.
     */
    public static class EmfPolyBezierTo16 extends EmfPolyBezierTo {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyBezierTo16;
        }

        @Override
        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }

    /** The EMR_POLYLINETO record specifies one or more straight lines based upon the current position. */
    public static class EmfPolylineTo extends EmfPolyline {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polylineTo;
        }

        @Override
        protected boolean hasStartPoint() {
            return false;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            polyTo(ctx, poly, getFillDrawStyle());
        }
    }

    /**
     * The EMR_POLYLINETO16 record specifies one or more straight lines based upon the current position.
     * A line is drawn from the current position to the first point specified by the points field by using the
     * current pen. For each additional line, drawing is performed from the ending point of the previous
     * line to the next point specified by points.
     */
    public static class EmfPolylineTo16 extends EmfPolylineTo {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polylineTo16;
        }

        @Override
        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }

    /**
     * The EMR_POLYPOLYGON record specifies a series of closed polygons.
     */
    public static class EmfPolyPolygon extends HwmfDraw.WmfPolyPolygon implements HemfRecord {
        private final Rectangle2D bounds = new Rectangle2D.Double();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyPolygon;
        }

        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointL(leis, point);
        }

        @SuppressWarnings("unused")
        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);

            // A 32-bit unsigned integer that specifies the number of polygons.
            long numberOfPolygons = leis.readUInt();
            // A 32-bit unsigned integer that specifies the total number of points in all polygons.
            long count = Math.min(16384, leis.readUInt());

            size += 2 * LittleEndianConsts.INT_SIZE;

            // An array of 32-bit unsigned integers that specifies the point count for each polygon.
            IOUtils.safelyAllocateCheck(numberOfPolygons, MAX_NUMBER_OF_POLYGONS);
            long[] polygonPointCount = new long[(int)numberOfPolygons];

            size += numberOfPolygons * LittleEndianConsts.INT_SIZE;

            for (int i=0; i<numberOfPolygons; i++) {
                polygonPointCount[i] = leis.readUInt();
            }

            Point2D pnt = new Point2D.Double();
            for (long nPoints : polygonPointCount) {
                // An array of WMF PointL objects that specifies the points for all polygons in logical units.
                // The number of points is specified by the Count field value.
                Path2D poly = new Path2D.Double(Path2D.WIND_EVEN_ODD, (int)nPoints);
                for (int i=0; i<nPoints; i++) {
                    size += readPoint(leis, pnt);
                    if (i == 0) {
                        poly.moveTo(pnt.getX(), pnt.getY());
                    } else {
                        poly.lineTo(pnt.getX(), pnt.getY());
                    }
                }
                if (isClosed()) {
                    poly.closePath();
                }
                polyList.add(poly);
            }
            return size;
        }


        @Override
        public void draw(HemfGraphics ctx) {
            Shape shape = getShape(ctx);
            if (shape == null) {
                return;
            }

            ctx.draw(path -> path.append(shape, false), getFillDrawStyle());
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
     * The EMR_POLYPOLYGON16 record specifies a series of closed polygons. Each polygon is outlined
     * using the current pen, and filled using the current brush and polygon fill mode.
     * The polygons drawn by this record can overlap.
     */
    public static class EmfPolyPolygon16 extends EmfPolyPolygon {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyPolygon16;
        }

        @Override
        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }

    /**
     * The EMR_POLYPOLYLINE record specifies multiple series of connected line segments.
     */
    public static class EmfPolyPolyline extends EmfPolyPolygon {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyPolyline;
        }

        @Override
        protected boolean isClosed() {
            return false;
        }

        @Override
        protected FillDrawStyle getFillDrawStyle() {
            return FillDrawStyle.DRAW;
        }
    }

    /** The EMR_POLYPOLYLINE16 record specifies multiple series of connected line segments. */
    public static class EmfPolyPolyline16 extends EmfPolyPolyline {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyPolyline16;
        }

        @Override
        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }

    /**
     * The EMR_SETPIXELV record defines the color of the pixel at the specified logical coordinates.
     */
    public static class EmfSetPixelV extends HwmfDraw.WmfSetPixel implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setPixelV;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readPointL(leis, point);
            size += colorRef.init(leis);
            return size;
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_MOVETOEX record specifies coordinates of the new current position, in logical units.
     */
    public static class EmfSetMoveToEx extends HwmfDraw.WmfMoveTo implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setMoveToEx;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readPointL(leis, point);
        }

        @Override
        public void draw(final HemfGraphics ctx) {
            ctx.draw((path) -> path.moveTo(point.getX(), point.getY()), FillDrawStyle.NONE);
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_ARC record specifies an elliptical arc.
     * It resets the current position to the end point of the arc.
     */
    public static class EmfArc extends HwmfDraw.WmfArc implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.arc;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            size += readPointL(leis, startPoint);
            size += readPointL(leis, endPoint);
            return size;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.draw(path -> path.append(getShape(), false), getFillDrawStyle());
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_CHORD record specifies a chord, which is a region bounded by the intersection of an
     * ellipse and a line segment, called a secant. The chord is outlined by using the current pen
     * and filled by using the current brush.
     */
    public static class EmfChord extends HwmfDraw.WmfChord implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.chord;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            size += readPointL(leis, startPoint);
            size += readPointL(leis, endPoint);
            return size;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.draw(path -> path.append(getShape(), false), getFillDrawStyle());
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_PIE record specifies a pie-shaped wedge bounded by the intersection of an ellipse and two
     * radials. The pie is outlined by using the current pen and filled by using the current brush.
     */
    public static class EmfPie extends HwmfDraw.WmfPie implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.pie;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            size += readPointL(leis, startPoint);
            size += readPointL(leis, endPoint);
            return size;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.draw(path -> path.append(getShape(), false), getFillDrawStyle());
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_ELLIPSE record specifies an ellipse. The center of the ellipse is the center of the specified
     * bounding rectangle. The ellipse is outlined by using the current pen and is filled by using the current
     * brush.
     */
    public static class EmfEllipse extends HwmfDraw.WmfEllipse implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.ellipse;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readRectL(leis, bounds);
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.draw(path -> path.append(getShape(), false), FillDrawStyle.FILL_DRAW);
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_RECTANGLE record draws a rectangle. The rectangle is outlined by using the current pen
     * and filled by using the current brush.
     */
    public static class EmfRectangle extends HwmfDraw.WmfRectangle implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.rectangle;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readRectL(leis, bounds);
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.draw(path -> path.append(normalizeBounds(bounds), false), FillDrawStyle.FILL_DRAW);
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
     * The EMR_ROUNDRECT record specifies a rectangle with rounded corners. The rectangle is outlined
     * by using the current pen and filled by using the current brush.
     */
    public static class EmfRoundRect extends HwmfDraw.WmfRoundRect implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.roundRect;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);

            // A 32-bit unsigned integer that defines the x-coordinate of the point.
            int width = (int)leis.readUInt();
            int height = (int)leis.readUInt();
            corners.setSize(width, height);

            return size + 2*LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.draw(path -> path.append(getShape(), false), FillDrawStyle.FILL_DRAW);
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
     * The EMR_LINETO record specifies a line from the current position up to, but not including, the
     * specified point. It resets the current position to the specified point.
     */
    public static class EmfLineTo extends HwmfDraw.WmfLineTo implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.lineTo;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return readPointL(leis, point);
        }

        @Override
        public void draw(final HemfGraphics ctx) {
            ctx.draw((path) -> path.lineTo(point.getX(), point.getY()), FillDrawStyle.DRAW);
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }

        @Override
        public void calcBounds(RenderBounds holder) {
            Rectangle2D b = holder.getBounds();
            if (!b.isEmpty()) {
                b.add(point);
            }
        }
    }

    /**
     * The EMR_ARCTO record specifies an elliptical arc.
     * It resets the current position to the end point of the arc.
     */
    public static class EmfArcTo extends HwmfDraw.WmfArc implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.arcTo;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            size += readPointL(leis, startPoint);
            size += readPointL(leis, endPoint);
            return size;
        }

        @Override
        public void draw(final HemfGraphics ctx) {
            final Arc2D arc = getShape();
            ctx.draw((path) -> path.append(arc, true), getFillDrawStyle());
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

    /** The EMR_POLYDRAW record specifies a set of line segments and Bezier curves. */
    public static class EmfPolyDraw extends HwmfDraw.WmfPolygon implements HemfRecord {
        private final Rectangle2D bounds = new Rectangle2D.Double();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyDraw;
        }

        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointL(leis, point);
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            long size = readRectL(leis, bounds);
            int count = (int)leis.readUInt();
            size += LittleEndianConsts.INT_SIZE;
            IOUtils.safelyAllocateCheck(count, HwmfPicture.getMaxRecordLength());
            Point2D[] points = new Point2D[count];
            for (int i=0; i<count; i++) {
                points[i] = new Point2D.Double();
                size += readPoint(leis, points[i]);
            }

            poly = new Path2D.Double(Path2D.WIND_EVEN_ODD, count);

            for (int i=0; i<count; i++) {
                int mode = leis.readUByte();
                switch (mode & 0x06) {
                    // PT_LINETO
                    // Specifies that a line is to be drawn from the current position to this point, which
                    // then becomes the new current position.
                    case 0x02:
                        poly.lineTo(points[i].getX(), points[i].getY());
                        break;
                    // PT_BEZIERTO
                    // Specifies that this point is a control point or ending point for a Bezier curve.
                    // PT_BEZIERTO types always occur in sets of three.
                    // The current position defines the starting point for the Bezier curve.
                    // The first two PT_BEZIERTO points are the control points,
                    // and the third PT_BEZIERTO point is the ending point.
                    // The ending point becomes the new current position.
                    // If there are not three consecutive PT_BEZIERTO points, an error results.
                    case 0x04:
                        int mode2 = leis.readUByte();
                        int mode3 = leis.readUByte();
                        assert(mode2 == 0x04 && (mode3 == 0x04 || mode3 == 0x05));
                        if ((i + 2) >= points.length) {
                            throw new IllegalStateException("Points index causes index out of bounds");
                        }
                        poly.curveTo(
                            points[i].getX(), points[i].getY(),
                            points[i+1].getX(), points[i+1].getY(),
                            points[i+2].getX(), points[i+2].getY()
                        );
                        // update mode for closePath handling below
                        mode = mode3;
                        i+=2;
                        break;
                    // PT_MOVETO
                    // Specifies that this point starts a disjoint figure. This point becomes the new current position.
                    case 0x06:
                        poly.moveTo(points[i].getX(), points[i].getY());
                        break;
                    default:
                        // TODO: log error
                        break;
                }

                // PT_CLOSEFIGURE
                // A PT_LINETO or PT_BEZIERTO type can be combined with this value by using the bitwise operator OR
                // to indicate that the corresponding point is the last point in a figure and the figure is closed.
                // The current position is set to the ending point of the closing line.
                if ((mode & 0x01) == 0x01) {
                    this.poly.closePath();
                }
            }
            size += count;
            return size;
        }

        @Override
        protected FillDrawStyle getFillDrawStyle() {
            // Draws a set of line segments and Bezier curves.
            return FillDrawStyle.DRAW;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.draw(path -> path.append(poly, false), getFillDrawStyle());
        }

        public Rectangle2D getBounds() {
            return bounds;
        }

        @Override
        protected boolean addClose() {
            return false;
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

    public static class EmfPolyDraw16 extends EmfPolyDraw {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.polyDraw16;
        }

        protected long readPoint(LittleEndianInputStream leis, Point2D point) {
            return readPointS(leis, point);
        }
    }

    /**
     * This record opens a path bracket in the current playback device context.
     *
     * After a path bracket is open, an application can begin processing records to define
     * the points that lie in the path. An application MUST close an open path bracket by
     * processing the EMR_ENDPATH record.
     *
     * When an application processes the EMR_BEGINPATH record, all previous paths
     * MUST be discarded from the playback device context.
     */
    public static class EmfBeginPath implements HemfRecordWithoutProperties {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.beginPath;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return 0;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            final HemfDrawProperties prop = ctx.getProperties();
            prop.setPath(new Path2D.Double());
            prop.setUsePathBracket(true);
        }

        @Override
        public String toString() {
            return "{}";
        }
    }

    /**
     * This record closes a path bracket and selects the path defined by the bracket into
     * the playback device context.
     */
    public static class EmfEndPath implements HemfRecordWithoutProperties {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.endPath;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return 0;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            final HemfDrawProperties prop = ctx.getProperties();
            prop.setUsePathBracket(false);
        }

        @Override
        public String toString() {
            return "{}";
        }
    }

    /**
     * This record aborts a path bracket or discards the path from a closed path bracket.
     */
    public static class EmfAbortPath implements HemfRecordWithoutProperties {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.abortPath;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return 0;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            final HemfDrawProperties prop = ctx.getProperties();
            prop.setPath(null);
            prop.setUsePathBracket(false);
        }

        @Override
        public String toString() {
            return "{}";
        }
    }

    /**
     * This record closes an open figure in a path.
     *
     * Processing the EMR_CLOSEFIGURE record MUST close the figure by drawing a line
     * from the current position to the first point of the figure, and then it MUST connect
     * the lines by using the line join style. If a figure is closed by processing the
     * EMR_LINETO record instead of the EMR_CLOSEFIGURE record, end caps are
     * used to create the corner instead of a join.
     *
     * The EMR_CLOSEFIGURE record SHOULD only be used if there is an open path
     * bracket in the playback device context.
     *
     * A figure in a path is open unless it is explicitly closed by processing this record.
     * Note: A figure can be open even if the current point and the starting point of the
     * figure are the same.
     *
     * After processing the EMR_CLOSEFIGURE record, adding a line or curve to the path
     * MUST start a new figure.
     */
    public static class EmfCloseFigure implements HemfRecordWithoutProperties {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.closeFigure;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return 0;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            final HemfDrawProperties prop = ctx.getProperties();
            final Path2D path = prop.getPath();
            if (path != null && path.getCurrentPoint() != null) {
                path.closePath();
                prop.setLocation(path.getCurrentPoint());
            }
        }

        @Override
        public String toString() {
            return "{}";
        }
    }

    /**
     * This record transforms any curves in the selected path into the playback device
     * context; each curve MUST be turned into a sequence of lines.
     */
    public static class EmfFlattenPath implements HemfRecordWithoutProperties {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.flattenPath;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return 0;
        }
    }

    /**
     * This record redefines the current path as the area that would be painted if the path
     * were drawn using the pen currently selected into the playback device context.
     */
    public static class EmfWidenPath implements HemfRecordWithoutProperties {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.widenPath;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return 0;
        }

        @Override
        public String toString() {
            return "{}";
        }
    }

    /**
     * The EMR_STROKEPATH record renders the specified path by using the current pen.
     */
    public static class EmfStrokePath implements HemfRecord {
        protected final Rectangle2D bounds = new Rectangle2D.Double();

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.strokePath;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 128-bit WMF RectL object, which specifies bounding rectangle, in device units
            return (recordSize == 0) ? 0 : readRectL(leis, bounds);
        }

        @Override
        public void draw(HemfGraphics ctx) {
            HemfDrawProperties props = ctx.getProperties();
            Path2D path = props.getPath();
            path.setWindingRule(ctx.getProperties().getWindingRule());
            ctx.draw(path);
        }

        @Override
        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        public Rectangle2D getBounds() {
            return bounds;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("bounds", this::getBounds);
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
     * The EMR_FILLPATH record closes any open figures in the current path and fills the path's interior by
     * using the current brush and polygon-filling mode.
     */
    public static class EmfFillPath extends EmfStrokePath {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.fillPath;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            final HemfDrawProperties prop = ctx.getProperties();
            final Path2D origPath = prop.getPath();
            if (origPath.getCurrentPoint() == null) {
                return;
            }
            final Path2D path = (Path2D)origPath.clone();
            path.closePath();
            path.setWindingRule(ctx.getProperties().getWindingRule());
            ctx.fill(path);
        }
    }

    /**
     * The EMR_STROKEANDFILLPATH record closes any open figures in a path, strokes the outline of the
     * path by using the current pen, and fills its interior by using the current brush.
     */
    public static class EmfStrokeAndFillPath extends EmfStrokePath {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.strokeAndFillPath;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            HemfDrawProperties props = ctx.getProperties();
            Path2D path = props.getPath();
            path.closePath();
            path.setWindingRule(ctx.getProperties().getWindingRule());
            ctx.fill(path);
            ctx.draw(path);
        }
    }

    static long readRectL(LittleEndianInputStream leis, Rectangle2D bounds) {
        /* A 32-bit signed integer that defines the x coordinate, in logical coordinates,
         * of the ... corner of the rectangle.
         */
        final double left = leis.readInt();
        final double top = leis.readInt();
        final double right = leis.readInt();
        final double bottom = leis.readInt();
        bounds.setRect(left, top, right-left, bottom-top);

        return 4L * LittleEndianConsts.INT_SIZE;
    }

    static long readPointS(LittleEndianInputStream leis, Point2D point) {
        // x (2 bytes): A 16-bit signed integer that defines the horizontal (x) coordinate of the point.
        final int x = leis.readShort();
        // y (2 bytes): A 16-bit signed integer that defines the vertical (y) coordinate of the point.
        final int y = leis.readShort();
        point.setLocation(x, y);

        return 2L*LittleEndianConsts.SHORT_SIZE;

    }
    static long readPointL(LittleEndianInputStream leis, Point2D point) {
        // x (4 bytes): A 32-bit signed integer that defines the horizontal (x) coordinate of the point.
        final int x = leis.readInt();
        // y (4 bytes): A 32-bit signed integer that defines the vertical (y) coordinate of the point.
        final int y = leis.readInt();
        point.setLocation(x, y);

        return 2L*LittleEndianConsts.INT_SIZE;

    }

    static long readDimensionFloat(LittleEndianInputStream leis, Dimension2D dimension) {
        final double width = leis.readFloat();
        final double height = leis.readFloat();
        dimension.setSize(width, height);
        return 2L*LittleEndianConsts.INT_SIZE;
    }

    static long readDimensionInt(LittleEndianInputStream leis, Dimension2D dimension) {
        // although the spec says "use unsigned ints", there are examples out there using signed ints
        final double width = leis.readInt();
        final double height = leis.readInt();
        dimension.setSize(width, height);
        return 2L*LittleEndianConsts.INT_SIZE;
    }

    private static void polyTo(final HemfGraphics ctx, final Path2D poly, FillDrawStyle fillDrawStyle) {
        if (poly.getCurrentPoint() == null) {
            return;
        }

        final PathIterator pi = poly.getPathIterator(null);
        // ignore empty polys and dummy start point (moveTo)
        pi.next();
        if (pi.isDone()) {
            return;
        }

        ctx.draw((path) -> path.append(pi, true), fillDrawStyle);
    }
}
