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


import static org.docx4j.org.apache.poi.util.GenericRecordUtil.getEnumBitsAsString;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.common.usermodel.GenericRecord;
import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics.EmfRenderState;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfRecord.RenderBounds;
import org.docx4j.org.apache.poi.util.BitField;
import org.docx4j.org.apache.poi.util.BitFieldFactory;
import org.docx4j.org.apache.poi.util.GenericRecordJsonWriter;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.Internal;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

@Internal
public class HemfPlusHeader implements HemfPlusRecord {
    /**
     * The GraphicsVersion enumeration defines versions of operating system graphics that are used to
     * create EMF+ metafiles.
     */
    public enum GraphicsVersion {
        V1(0x0001),
        V1_1(0x0002)
        ;

        public final int id;

        GraphicsVersion(int id) {
            this.id = id;
        }

        public static GraphicsVersion valueOf(int id) {
            for (GraphicsVersion wrt : values()) {
                if (wrt.id == id) return wrt;
            }
            return null;
        }
    }

    private static final int[] FLAGS_MASK = { 0x0000, 0x0001 };
    private static final String[] FLAGS_NAMES = { "EMF_PLUS_MODE", "DUAL_MODE" };

    private static final int[] EMFFLAGS_MASK = { 0x0000, 0x0001 };
    private static final String[] EMFFLAGS_NAMES = { "CONTEXT_PRINTER", "CONTEXT_VIDEO" };



    private int flags;
    private final EmfPlusGraphicsVersion version = new EmfPlusGraphicsVersion();
    private long emfPlusFlags;
    private long logicalDpiX;
    private long logicalDpiY;

    @Override
    public HemfPlusRecordType getEmfPlusRecordType() {
        return HemfPlusRecordType.header;
    }

    public int getFlags() {
        return flags;
    }

    @Override
    public long init(LittleEndianInputStream leis, long dataSize, long recordId, int flags) throws IOException {
        this.flags = flags;
        version.init(leis);

        assert(version.getMetafileSignature() == 0xDBC01 && version.getGraphicsVersion() != null);

        emfPlusFlags = leis.readUInt();

        logicalDpiX = leis.readUInt();
        logicalDpiY = leis.readUInt();
        return 4L*LittleEndianConsts.INT_SIZE;
    }

    public EmfPlusGraphicsVersion getVersion() {
        return version;
    }

    /**
     * If set, this flag indicates that this metafile is "dual-mode", which means that it contains two sets of records,
     * each of which completely specifies the graphics content. If clear, the graphics content is specified by EMF+
     * records, and possibly EMF records that are preceded by an EmfPlusGetDC record. If this flag is set, EMF records
     * alone SHOULD suffice to define the graphics content. Note that whether the "dual-mode" flag is set or not, some
     * EMF records are always present, namely EMF control records and the EMF records that contain EMF+ records.
     *
     * @return {@code true} if dual-mode is enabled
     */
    @SuppressWarnings("unused")
    public boolean isEmfPlusDualMode() {
        return (flags & 1) == 1;
    }

    public long getEmfPlusFlags() {
        return emfPlusFlags;
    }

    public long getLogicalDpiX() {
        return logicalDpiX;
    }

    public long getLogicalDpiY() {
        return logicalDpiY;
    }

    @Override
    public void draw(HemfGraphics ctx) {
        // currently EMF is better supported than EMF+ ... so if there's a complete set of EMF records available,
        // disable EMF+ rendering for now
        ctx.setRenderState(EmfRenderState.EMF_DCONTEXT);
    }

    @Override
    public void calcBounds(RenderBounds holder) {
        holder.setState(EmfRenderState.EMF_DCONTEXT);
    }

    @Override
    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "flags", getEnumBitsAsString(this::getFlags, FLAGS_MASK, FLAGS_NAMES),
            "version", this::getVersion,
            "emfPlusFlags", getEnumBitsAsString(this::getEmfPlusFlags, EMFFLAGS_MASK, EMFFLAGS_NAMES),
            "logicalDpiX", this::getLogicalDpiX,
            "logicalDpiY", this::getLogicalDpiY
        );
    }

    public static class EmfPlusGraphicsVersion implements GenericRecord {
        private static final BitField METAFILE_SIGNATURE = BitFieldFactory.getInstance(0xFFFFF000);

        private static final BitField GRAPHICS_VERSION = BitFieldFactory.getInstance(0x00000FFF);


        private int metafileSignature;
        private GraphicsVersion graphicsVersion;


        public int getMetafileSignature() {
            return metafileSignature;
        }

        public GraphicsVersion getGraphicsVersion() {
            return graphicsVersion;
        }

        public long init(LittleEndianInputStream leis) throws IOException {
            int val = leis.readInt();
            // A value that identifies the type of metafile. The value for an EMF+ metafile is 0xDBC01.
            metafileSignature = METAFILE_SIGNATURE.getValue(val);
            // The version of operating system graphics. This value MUST be defined in the GraphicsVersion enumeration
            graphicsVersion = GraphicsVersion.valueOf(GRAPHICS_VERSION.getValue(val));

            return LittleEndianConsts.INT_SIZE;
        }

        public String toString() {
            return GenericRecordJsonWriter.marshal(this);
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "metafileSignature", this::getMetafileSignature,
                "graphicsVersion", this::getGraphicsVersion
            );
        }
    }
}