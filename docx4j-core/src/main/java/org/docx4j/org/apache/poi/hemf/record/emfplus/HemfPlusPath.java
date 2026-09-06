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

import static org.docx4j.org.apache.poi.util.GenericRecordUtil.getBitsAsString;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.hemf.draw.HemfDrawProperties;
import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusDraw.EmfPlusCompressed;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusDraw.EmfPlusRelativePosition;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusHeader.EmfPlusGraphicsVersion;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectData;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectType;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.docx4j.org.apache.poi.util.BitField;
import org.docx4j.org.apache.poi.util.BitFieldFactory;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

public class HemfPlusPath {

    /** The PathPointType enumeration defines types of points on a graphics path. */
    public enum EmfPlusPathPointType {
        /** Specifies that the point is the starting point of a path. */
        START,
        /** Specifies that the point is one of the two endpoints of a line. */
        LINE,
        // not defined
        UNUSED,
        /** Specifies that the point is an endpoint or control point of a cubic Bezier curve */
        BEZIER
    }

    @SuppressWarnings("unused")
    public static class EmfPlusPath implements EmfPlusObjectData, EmfPlusCompressed, EmfPlusRelativePosition {
        /**
         * If set, the point types in the PathPointTypes array are specified by EmfPlusPathPointTypeRLE objects,
         * which use run-length encoding (RLE) compression, and/or EmfPlusPathPointType objects.
         * If clear, the point types in the PathPointTypes array are specified by EmfPlusPathPointType objects.
         */
        private static final BitField RLE_COMPRESSED = BitFieldFactory.getInstance(0x00001000);

        /** Specifies that a line segment that passes through the point is dashed. */
        private static final BitField POINT_TYPE_DASHED = BitFieldFactory.getInstance(0x10);

        /** Specifies that the point is a position marker. */
        private static final BitField POINT_TYPE_MARKER = BitFieldFactory.getInstance(0x20);

        /** Specifies that the point is the endpoint of a subpath. */
        private static final BitField POINT_TYPE_CLOSE = BitFieldFactory.getInstance(0x80);

        private static final BitField POINT_TYPE_ENUM = BitFieldFactory.getInstance(0x0F);


        private static final BitField POINT_RLE_BEZIER = BitFieldFactory.getInstance(0x80);

        private static final BitField POINT_RLE_COUNT = BitFieldFactory.getInstance(0x3F);

        private static final int[] FLAGS_MASKS = { 0x0800, 0x1000, 0x4000 };
        private static final String[] FLAGS_NAMES = { "RELATIVE_POSITION", "RLE_COMPRESSED", "FORMAT_COMPRESSED" };

        private static final int[] TYPE_MASKS = { 0x10, 0x20, 0x80 };
        private static final String[] TYPE_NAMES = { "DASHED", "MARKER", "CLOSE" };

        private final EmfPlusGraphicsVersion graphicsVersion = new EmfPlusGraphicsVersion();
        private int pointFlags;
        private Point2D[] pathPoints;
        private byte[] pointTypes;

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, EmfPlusObjectType objectType, int flags) throws IOException {
            long size = graphicsVersion.init(leis);

            // A 32-bit unsigned integer that specifies the number of points and associated point types that
            // are defined by this object.
            int pointCount = leis.readInt();

            // A 16-bit unsigned integer that specifies how to interpret the points
            // and associated point types that are defined by this object.
            pointFlags = leis.readShort();

            leis.skipFully(LittleEndianConsts.SHORT_SIZE);
            size += 2* LittleEndianConsts.INT_SIZE;

            BiFunction<LittleEndianInputStream,Point2D,Integer> readPoint;

            if (isRelativePosition()) {
                readPoint = HemfPlusDraw::readPointR;
            } else if (isCompressed()) {
                readPoint = HemfPlusDraw::readPointS;
            } else {
                readPoint = HemfPlusDraw::readPointF;
            }

            // pointCount is an untrusted 32-bit field used as the length of both arrays below;
            // reject negative and oversized values before allocating
            IOUtils.safelyAllocateCheck(pointCount, HwmfPicture.getMaxRecordLength());
            pathPoints = new Point2D[pointCount];
            for (int i=0; i<pointCount; i++) {
                pathPoints[i] = new Point2D.Double();
                size += readPoint.apply(leis,pathPoints[i]);
            }

            pointTypes = new byte[pointCount];
            final boolean isRLE = RLE_COMPRESSED.isSet(pointFlags);
            if (isRLE) {
                for (int i=0, rleCount; i<pointCount; i+=rleCount, size+=2) {
                    rleCount = POINT_RLE_COUNT.getValue(leis.readByte());
                    Arrays.fill(pointTypes, i, Math.min(i+rleCount, pointCount), leis.readByte());
                }
            } else {
                leis.readFully(pointTypes);
                size += pointCount;
            }

            int padding = (int)((4 - (size % 4)) % 4);
            leis.skipFully(padding);
            size += padding;

            return size;
        }

        @Override
        public EmfPlusGraphicsVersion getGraphicsVersion() {
            return graphicsVersion;
        }

        public boolean isPointDashed(int index) {
            return POINT_TYPE_DASHED.isSet(pointTypes[index]);
        }

        public boolean isPointMarker(int index) {
            return POINT_TYPE_MARKER.isSet(pointTypes[index]);
        }

        public boolean isPointClosed(int index) {
            return POINT_TYPE_CLOSE.isSet(pointTypes[index]);
        }

        public EmfPlusPathPointType getPointType(int index) {
            return EmfPlusPathPointType.values()[POINT_TYPE_ENUM.getValue(pointTypes[index])];
        }

        @Override
        public int getFlags() {
            return pointFlags;
        }

        public Point2D getPoint(int index) {
            return pathPoints[index];
        }

        @Override
        public void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {
            HemfDrawProperties prop = ctx.getProperties();
            prop.setPath(getPath());
        }

        public Path2D getPath() {
            return getPath(Path2D.WIND_NON_ZERO);
        }

        public Path2D getPath(int windingRule) {
            Path2D path = new Path2D.Double(windingRule);
            for (int idx=0; idx < pathPoints.length; idx++) {
                Point2D p1 = pathPoints[idx];
                switch (getPointType(idx)) {
                    case START:
                        path.moveTo(p1.getX(), p1.getY());
                        break;
                    case LINE:
                        path.lineTo(p1.getX(), p1.getY());
                        break;
                    case BEZIER: {
                        Point2D p2 = pathPoints[++idx];
                        Point2D p3 = pathPoints[++idx];
                        path.curveTo(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
                        break;
                    }
                }
                if (isPointClosed(idx)) {
                    path.closePath();
                }
            }
            return path;
        }

        @Override
        public Enum getGenericRecordType() {
            return EmfPlusObjectType.PATH;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "graphicsVersion", this::getGraphicsVersion,
                "flags", getBitsAsString(this::getFlags, FLAGS_MASKS, FLAGS_NAMES),
                "points", this::getGenericPoints
            );
        }

        private List<GenericRecord> getGenericPoints() {
            return IntStream.range(0, pathPoints.length).
                mapToObj(this::getGenericPoint).
                collect(Collectors.toList());
        }

        private GenericRecord getGenericPoint(final int idx) {
            return () -> GenericRecordUtil.getGenericProperties(
                "flags", getBitsAsString(() -> pointTypes[idx], TYPE_MASKS, TYPE_NAMES),
                "type", () -> getPointType(idx),
                "point", () -> getPoint(idx)
            );
        }
    }
}
