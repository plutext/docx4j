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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusBrush.EmfPlusBrush;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusFont.EmfPlusFont;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusHeader.EmfPlusGraphicsVersion;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusImage.EmfPlusImage;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusImage.EmfPlusImageAttributes;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusMisc.EmfPlusObjectId;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusPath.EmfPlusPath;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusPen.EmfPlusPen;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusRegion.EmfPlusRegion;
import org.docx4j.org.apache.poi.hwmf.draw.HwmfGraphics;
import org.docx4j.org.apache.poi.hwmf.record.HwmfObjectTableEntry;
import org.docx4j.org.apache.poi.util.BitField;
import org.docx4j.org.apache.poi.util.BitFieldFactory;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.IOUtils;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

public class HemfPlusObject {
    private static final int MAX_OBJECT_SIZE = 50_000_000;

    /**
     * The ObjectType enumeration defines types of graphics objects that can be created and used in graphics operations.
     */
    public enum EmfPlusObjectType {
        /**
         * The object is not a valid object.
         */
        INVALID(0x00000000, EmfPlusUnknownData::new),
        /**
         * Brush objects fill graphics regions.
         */
        BRUSH(0x00000001, EmfPlusBrush::new),
        /**
         * Pen objects draw graphics lines.
         */
        PEN(0x00000002, EmfPlusPen::new),
        /**
         * Path objects specify sequences of lines, curves, and shapes.
         */
        PATH(0x00000003, EmfPlusPath::new),
        /**
         * Region objects specify areas of the output surface.
         */
        REGION(0x00000004, EmfPlusRegion::new),
        /**
         * Image objects encapsulate bitmaps and metafiles.
         */
        IMAGE(0x00000005, EmfPlusImage::new),
        /**
         * Font objects specify font properties, including typeface style, em size, and font family.
         */
        FONT(0x00000006, EmfPlusFont::new),
        /**
         * String format objects specify text layout, including alignment, orientation, tab stops, clipping,
         * and digit substitution for languages that do not use Western European digits.
         */
        STRING_FORMAT(0x00000007, EmfPlusUnknownData::new),
        /**
         * Image attribute objects specify operations on pixels during image rendering, including color
         * adjustment, grayscale adjustment, gamma correction, and color mapping.
         */
        IMAGE_ATTRIBUTES(0x00000008, EmfPlusImageAttributes::new),
        /**
         * Custom line cap objects specify shapes to draw at the ends of a graphics line, including
         * squares, circles, and diamonds.
         */
        CUSTOM_LINE_CAP(0x00000009, EmfPlusUnknownData::new);

        public final int id;
        public final Supplier<? extends EmfPlusObjectData> constructor;

        EmfPlusObjectType(int id, Supplier<? extends EmfPlusObjectData> constructor) {
            this.id = id;
            this.constructor = constructor;
        }

        public static EmfPlusObjectType valueOf(int id) {
            for (EmfPlusObjectType wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }


    /**
     * The EmfPlusObject record specifies an object for use in graphics operations. The object definition
     * can span multiple records), which is indicated by the value of the Flags field.
     */
    @SuppressWarnings("unused")
    public static class EmfPlusObject implements HemfPlusRecord, EmfPlusObjectId, HwmfObjectTableEntry {
        /**
         * Indicates that the object definition continues on in the next EmfPlusObject
         * record. This flag is never set in the final record that defines the object.
         */
        private static final BitField CONTINUABLE = BitFieldFactory.getInstance(0x8000);

        /**
         * Specifies the metafileType of object to be created by this record, from the
         * ObjectType enumeration
         */
        private static final BitField OBJECT_TYPE = BitFieldFactory.getInstance(0x7F00);

        private static final int[] FLAGS_MASKS = { 0x7F00, 0x8000 };

        private static final String[] FLAGS_NAMES = { "OBJECT_TYPE", "CONTINUABLE" };

        private int flags;
        // for debugging
        @SuppressWarnings("FieldCanBeLocal")
        private int objectId;
        private EmfPlusObjectData objectData;
        private List<EmfPlusObjectData> continuedObjectData;
        private int totalObjectSize;

        @Override
        public HemfPlusRecordType getEmfPlusRecordType() {
            return HemfPlusRecordType.object;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        public EmfPlusObjectType getObjectType() {
            return EmfPlusObjectType.valueOf(OBJECT_TYPE.getValue(flags));
        }

        @SuppressWarnings("unchecked")
        public <T extends EmfPlusObjectData> T getObjectData() {
            return (T)objectData;
        }

        public int getTotalObjectSize() {
            return totalObjectSize;
        }

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
            this.flags = flags;

            objectId = getObjectId();
            EmfPlusObjectType objectType = getObjectType();
            assert (objectType != null);

            long size = 0;

            totalObjectSize = 0;
            int dataSize2 = (int) dataSize;

            if (CONTINUABLE.isSet(flags)) {
                // If the record is continuable, when the continue bit is set, this field will be present.
                // Continuing objects have multiple EMF+ records starting with EmfPlusContinuedObjectRecord.
                // Each EmfPlusContinuedObjectRecord will contain a TotalObjectSize. Once TotalObjectSize number
                // of bytes has been read, the next EMF+ record will not be treated as part of the continuing object.
                totalObjectSize = leis.readInt();
                size += LittleEndianConsts.INT_SIZE;
                dataSize2 -= LittleEndianConsts.INT_SIZE;
            }

            objectData = objectType.constructor.get();
            size += objectData.init(leis, dataSize2, objectType, flags);

            return Math.toIntExact(size);
        }

        @Override
        public void draw(HemfGraphics ctx) {
            if (objectData.isContinuedRecord()) {
                EmfPlusObject other;
                HwmfObjectTableEntry entry = ctx.getPlusObjectTableEntry(getObjectId());
                if (entry instanceof EmfPlusObject &&
                    objectData.getClass().isInstance((other = (EmfPlusObject)entry).getObjectData())
                ) {
                    other.linkContinuedObject(objectData);
                } else {
                    throw new IllegalStateException("can't find previous record for continued record");
                }
            } else {
                ctx.addPlusObjectTableEntry(this, getObjectId());
            }
        }

        @Override
        public void applyObject(HwmfGraphics ctx) {
            objectData.applyObject((HemfGraphics)ctx, continuedObjectData);
        }

        void linkContinuedObject(EmfPlusObjectData continueObject) {
            if (continuedObjectData == null) {
                continuedObjectData = new ArrayList<>();
            }
            continuedObjectData.add(continueObject);
        }

        List<EmfPlusObjectData> getContinuedObject() {
            return continuedObjectData;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "flags", getBitsAsString(this::getFlags, FLAGS_MASKS, FLAGS_NAMES),
                "objectId", this::getObjectId,
                "objectData", () -> objectData.isContinuedRecord() ? null : getObjectData(),
                "continuedObject", objectData::isContinuedRecord,
                "totalObjectSize", this::getTotalObjectSize
            );
        }

    }

    public interface EmfPlusObjectData extends GenericRecord {
        long init(LittleEndianInputStream leis, long dataSize, EmfPlusObjectType objectType, int flags) throws IOException;

        void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData);

        EmfPlusGraphicsVersion getGraphicsVersion();

        default boolean isContinuedRecord() {
            EmfPlusGraphicsVersion gv = getGraphicsVersion();
            return (gv.getGraphicsVersion() == null || gv.getMetafileSignature() != 0xDBC01);
        }
    }

    public static class EmfPlusUnknownData implements EmfPlusObjectData {
        private EmfPlusObjectType objectType;
        private final EmfPlusGraphicsVersion graphicsVersion = new EmfPlusGraphicsVersion();
        private byte[] objectDataBytes;

        @Override
        public long init(LittleEndianInputStream leis, long dataSize, EmfPlusObjectType objectType, int flags) throws IOException {
            this.objectType = objectType;

            long size = graphicsVersion.init(leis);

            objectDataBytes = IOUtils.toByteArray(leis, (int)(dataSize - size), MAX_OBJECT_SIZE);

            return dataSize;
        }

        @Override
        public void applyObject(HemfGraphics ctx, List<? extends EmfPlusObjectData> continuedObjectData) {

        }

        @Override
        public EmfPlusGraphicsVersion getGraphicsVersion() {
            return graphicsVersion;
        }

        @Override
        public EmfPlusObjectType getGenericRecordType() {
            return objectType;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "graphicsVersion", this::getGraphicsVersion,
                "objectDataBytes", () -> objectDataBytes
            );
        }
    }
}
