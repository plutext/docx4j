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

import static org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusDraw.readRectF;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.hemf.draw.HemfDrawProperties;
import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusHeader.EmfPlusGraphicsVersion;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectData;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectType;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusPath.EmfPlusPath;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

public class HemfPlusRegion {
    public enum EmfPlusRegionNodeDataType {
        /**
         * Specifies a region node with child nodes. A Boolean AND operation SHOULD be applied to the left and right
         * child nodes specified by an EmfPlusRegionNodeChildNodes object
         */
        AND(0X00000001, EmfPlusRegionNode::new, Area::intersect),
        /**
         * Specifies a region node with child nodes. A Boolean OR operation SHOULD be applied to the left and right
         * child nodes specified by an EmfPlusRegionNodeChildNodes object.
         */
        OR(0X00000002, EmfPlusRegionNode::new, Area::add),
        /**
         * Specifies a region node with child nodes. A Boolean XOR operation SHOULD be applied to the left and right
         * child nodes specified by an EmfPlusRegionNodeChildNodes object.
         */
        XOR(0X00000003, EmfPlusRegionNode::new, Area::exclusiveOr),
        /**
         * Specifies a region node with child nodes. A Boolean operation, defined as "the part of region 1 that is excluded
         * from region 2", SHOULD be applied to the left and right child nodes specified by an EmfPlusRegionNodeChildNodes object.
         */
        EXCLUDE(0X00000004, EmfPlusRegionNode::new, Area::subtract),
        /**
         * Specifies a region node with child nodes. A Boolean operation, defined as "the part of region 2 that is excluded
         * from region 1", SHOULD be applied to the left and right child nodes specified by an EmfPlusRegionNodeChildNodes object.
         */
        COMPLEMENT(0X00000005, EmfPlusRegionNode::new, Area::subtract),
        /**
         * Specifies a region node with no child nodes.
         * The RegionNodeData field SHOULD specify a boundary with an EmfPlusRectF object.
         */
        RECT(0X10000000, EmfPlusRegionRect::new, null),
        /**
         * Specifies a region node with no child nodes.
         * The RegionNodeData field SHOULD specify a boundary with an EmfPlusRegionNodePath object
         */
        PATH(0X10000001, EmfPlusRegionPath::new, null),
        /** Specifies a region node with no child nodes. The RegionNodeData field SHOULD NOT be present. */
        EMPTY(0X10000002, EmfPlusRegionEmpty::new, null),
        /** Specifies a region node with no child nodes, and its bounds are not defined. */
        INFINITE(0X10000003, EmfPlusRegionInfinite::new, null)
        ;

        public final int id;
        public final Supplier<EmfPlusRegionNodeData> constructor;
        public final BiConsumer<Area,Area> operation;

        EmfPlusRegionNodeDataType(int id, Supplier<EmfPlusRegionNodeData> constructor, BiConsumer<Area,Area> operation) {
            this.id = id;
            this.constructor = constructor;
            this.operation = operation;
        }

        public static EmfPlusRegionNodeDataType valueOf(int id) {
            for (EmfPlusRegionNodeDataType wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }

    /** The EmfPlusRegion object specifies line and curve segments that define a nonrectilinear shape. */
    public static class EmfPlusRegion implements EmfPlusObjectData {

        private final EmfPlusGraphicsVersion graphicsVersion = new EmfPlusGraphicsVersion();
        private EmfPlusRegionNodeData regionNode;

        @SuppressWarnings("unused")
        @Override
        public long init(LittleEndianInputStream leis, long dataSize, EmfPlusObjectType objectType, int flags) throws IOException {
            long size = graphicsVersion.init(leis);

            // A 32-bit unsigned integer that specifies the number of child nodes in the RegionNode field.
            int nodeCount = leis.readInt();
            size += LittleEndianConsts.INT_SIZE;

            // An array of RegionNodeCount+1 EmfPlusRegionNode objects. Regions are specified as a binary tree of
            // region nodes, and each node MUST either be a terminal node or specify one or two child nodes.
            // RegionNode MUST contain at least one element.
            size += readNode(leis, this::setRegionNode);

            return size;
        }

        @Override
        public void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {
            HemfDrawProperties prop = ctx.getProperties();
            Shape shape = regionNode.getShape();
            prop.setPath(shape == null ? null : new Path2D.Double(shape));
        }

        @Override
        public EmfPlusGraphicsVersion getGraphicsVersion() {
            return graphicsVersion;
        }

        @Override
        public EmfPlusObjectType getGenericRecordType() {
            return EmfPlusObjectType.REGION;
        }

        private void setRegionNode(EmfPlusRegionNodeData regionNode) {
            this.regionNode = regionNode;
        }

        public EmfPlusRegionNodeData getRegionNode() {
            return regionNode;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "graphicsVersion", this::getGraphicsVersion,
                "regionNode", this::getRegionNode
            );
        }
    }


    public interface EmfPlusRegionNodeData extends GenericRecord {
        long init(LittleEndianInputStream leis) throws IOException;
        Shape getShape();
        default void setNodeType(EmfPlusRegionNodeDataType type) {}
    }

    public static class EmfPlusRegionPath extends EmfPlusPath implements EmfPlusRegionNodeData {
        public long init(LittleEndianInputStream leis) throws IOException {
            int dataSize = leis.readInt();
            return super.init(leis, dataSize, EmfPlusObjectType.PATH, 0) + LittleEndianConsts.INT_SIZE;
        }

        @Override
        public Shape getShape() {
            return getPath();
        }

        @Override
        public EmfPlusRegionNodeDataType getGenericRecordType() {
            return EmfPlusRegionNodeDataType.PATH;
        }
    }

    public static class EmfPlusRegionInfinite implements EmfPlusRegionNodeData {
        @Override
        public long init(LittleEndianInputStream leis) throws IOException {
            return 0;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return null;
        }

        @Override
        public Shape getShape() {
            return null;
        }

        @Override
        public EmfPlusRegionNodeDataType getGenericRecordType() {
            return EmfPlusRegionNodeDataType.INFINITE;
        }
    }

    public static class EmfPlusRegionEmpty implements EmfPlusRegionNodeData {
        @Override
        public long init(LittleEndianInputStream leis) throws IOException {
            return 0;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return null;
        }

        @Override
        public Shape getShape() {
            return new Rectangle2D.Double(0,0,0,0);
        }

        @Override
        public EmfPlusRegionNodeDataType getGenericRecordType() {
            return EmfPlusRegionNodeDataType.EMPTY;
        }
    }

    public static class EmfPlusRegionRect implements EmfPlusRegionNodeData {
        private final Rectangle2D rect = new Rectangle2D.Double();

        @Override
        public long init(LittleEndianInputStream leis) {
            return readRectF(leis, rect);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("rect", () -> rect);
        }

        @Override
        public Shape getShape() {
            return rect;
        }

        @Override
        public EmfPlusRegionNodeDataType getGenericRecordType() {
            return EmfPlusRegionNodeDataType.RECT;
        }
    }

    /** The EmfPlusRegionNode object specifies nodes of a graphics region. */
    public static class EmfPlusRegionNode implements EmfPlusRegionNodeData {
        private EmfPlusRegionNodeData left, right;
        private EmfPlusRegionNodeDataType nodeType;

        @Override
        public long init(LittleEndianInputStream leis) throws IOException {
            long size = readNode(leis, this::setLeft);
            size += readNode(leis, this::setRight);
            return size;
        }

        private void setLeft(EmfPlusRegionNodeData left) {
            this.left = left;
        }

        private void setRight(EmfPlusRegionNodeData right) {
            this.right = right;
        }

        public EmfPlusRegionNodeData getLeft() {
            return left;
        }

        public EmfPlusRegionNodeData getRight() {
            return right;
        }

        public EmfPlusRegionNodeDataType getNodeType() {
            return nodeType;
        }

        @Override
        public void setNodeType(EmfPlusRegionNodeDataType nodeType) {
            this.nodeType = nodeType;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "nodeType", this::getNodeType,
                "left", this::getLeft,
                "right", this::getRight
            );
        }

        @Override
        public Shape getShape() {
            boolean com = (nodeType == EmfPlusRegionNodeDataType.COMPLEMENT);
            final Shape leftShape =  (com ? right : left).getShape();
            final Shape rightShape = (com ? left : right).getShape();

            if (leftShape == null) {
                return rightShape;
            } else if (rightShape == null) {
                return leftShape;
            }

            // TODO: check Area vs. Path manipulation
            Area leftArea = new Area(leftShape);
            Area rightArea = new Area(rightShape);

            assert(nodeType.operation != null);
            nodeType.operation.accept(leftArea, rightArea);

            return leftArea;
        }

        @Override
        public EmfPlusRegionNodeDataType getGenericRecordType() {
            return nodeType;
        }
    }

    private static long readNode(LittleEndianInputStream leis, Consumer<EmfPlusRegionNodeData> con) throws IOException {
        // A 32-bit unsigned integer that specifies the type of data in the RegionNodeData field.
        // This value MUST be defined in the RegionNodeDataType enumeration
        EmfPlusRegionNodeDataType type = EmfPlusRegionNodeDataType.valueOf(leis.readInt());
        assert(type != null);
        EmfPlusRegionNodeData nd = type.constructor.get();
        con.accept(nd);
        nd.setNodeType(type);
        return LittleEndianConsts.INT_SIZE + nd.init(leis);
    }
}
