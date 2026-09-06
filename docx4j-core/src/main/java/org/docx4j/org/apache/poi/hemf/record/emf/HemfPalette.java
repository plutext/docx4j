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

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import org.docx4j.org.apache.poi.hemf.draw.HemfGraphics;
import org.docx4j.org.apache.poi.hwmf.record.HwmfPalette;
import org.docx4j.org.apache.poi.util.GenericRecordUtil;
import org.docx4j.org.apache.poi.util.LittleEndianConsts;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;

@SuppressWarnings("WeakerAccess")
public class HemfPalette {
    /** The EMR_SELECTPALETTE record specifies a logical palette for the playback device context. */
    public static class EmfSelectPalette extends HwmfPalette.WmfSelectPalette implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.selectPalette;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            /*
             * A 32-bit unsigned integer that specifies either the index of a LogPalette object
             * in the EMF Object Table or the value DEFAULT_PALETTE, which is the index
             * of a stock object palette from the StockObject enumeration
             */
            paletteIndex = (int)leis.readUInt();
            return LittleEndianConsts.INT_SIZE;
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /** The EMR_CREATEPALETTE record defines a logical palette for graphics operations. */
    public static class EmfCreatePalette extends HwmfPalette.WmfCreatePalette implements HemfRecord {

        protected int paletteIndex;

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.createPalette;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            start = 0x0300;
            /* A 32-bit unsigned integer that specifies the index of the logical palette object
             * in the EMF Object Table. This index MUST be saved so that this object can be
             * reused or modified.
             */
            paletteIndex = (int)leis.readUInt();
            /* A 16-bit unsigned integer that specifies the version number of the system. This MUST be 0x0300. */
            int version = leis.readUShort();
            assert(version == 0x0300);
            long size = readPaletteEntries(leis, -1);
            return size + LittleEndianConsts.INT_SIZE + LittleEndianConsts.SHORT_SIZE;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.addObjectTableEntry(this, paletteIndex);
        }

        public int getPaletteIndex() {
            return paletteIndex;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "base", super::getGenericProperties,
                "paletteIndex", this::getPaletteIndex
            );
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_SETPALETTEENTRIES record defines RGB color values in a range of entries for an existing
     * LogPalette object.
     */
    public static class EmfSetPaletteEntries extends HwmfPalette.WmfSetPaletteEntries implements HemfRecord {
        /** specifies the palette EMF Object Table index. */
        int paletteIndex;

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.setPaletteEntries;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer that specifies the palette EMF Object Table index.
            paletteIndex = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the index of the first entry to set.
            start = (int)leis.readUInt();
            // A 32-bit unsigned integer that specifies the number of entries.
            int nbrOfEntries = (int)leis.readUInt();
            int size = readPaletteEntries(leis, nbrOfEntries);
            return size + 3L*LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.addObjectTableEntry(this, paletteIndex);
        }

        public int getPaletteIndex() {
            return paletteIndex;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "base", super::getGenericProperties,
                "paletteIndex", this::getPaletteIndex
            );
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_RESIZEPALETTE record increases or decreases the size of an existing LogPalette object
     */
    public static class EmfResizePalette extends HwmfPalette.WmfResizePalette implements HemfRecord {
        /** specifies the palette EMF Object Table index. */
        int paletteIndex;

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.resizePalette;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            // A 32-bit unsigned integer that specifies the index of the palette object in the EMF Object Table
            paletteIndex = (int)leis.readUInt();

            // A 32-bit unsigned integer that specifies the number of entries in the palette after resizing.
            // The value MUST be less than or equal to 0x00000400 and greater than 0x00000000.
            numberOfEntries = (int)leis.readUInt();

            return 2L*LittleEndianConsts.INT_SIZE;
        }

        @Override
        public void draw(HemfGraphics ctx) {
            ctx.addObjectTableEntry(this, paletteIndex);
        }

        public int getPaletteIndex() {
            return paletteIndex;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties(
                "base", super::getGenericProperties,
                "paletteIndex", this::getPaletteIndex
            );
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * This record maps palette entries from the current LogPalette object to the system_palette.
     * This EMF record specifies no parameters.
     */
    public static class EmfRealizePalette extends HwmfPalette.WmfRealizePalette implements HemfRecord {
        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.realizePalette;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            return 0;
        }

        @Override
        public HemfRecordType getGenericRecordType() {
            return getEmfRecordType();
        }
    }

    /**
     * The EMR_SETICMMODE record specifies the mode of Image Color Management (ICM) for graphics operations.
     */
    public static class EmfSetIcmMode implements HemfRecord {
        /** The ICMMode enumeration defines values that specify when to turn on and off ICM. */
        public enum ICMMode {
            /**
             * Turns off Image Color Management (ICM) in the playback device context.
             * Turns on old-style color correction of halftones.
             */
            ICM_OFF(0x01),
            /**
             * Turns on ICM in the playback device context.
             * Turns off old-style color correction of halftones.
             */
            ICM_ON(0x02),
            /**
             * Queries the current state of color management in the playback device context.
             */
            ICM_QUERY(0x03),
            /**
             * Turns off ICM in the playback device context, and turns off old-style color correction of halftones.
             */
            ICM_DONE_OUTSIDEDC(0x04)
            ;

            public final int id;

            ICMMode(int id) {
                this.id = id;
            }

            public static ICMMode valueOf(int id) {
                for (ICMMode wrt : values()) {
                    if (wrt.id == id) return wrt;
                }
                return null;
            }

        }

        private ICMMode icmMode;

        @Override
        public HemfRecordType getEmfRecordType() {
            return HemfRecordType.seticmmode;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordSize, long recordId) throws IOException {
            icmMode = ICMMode.valueOf(leis.readInt());
            return LittleEndianConsts.INT_SIZE;
        }

        public ICMMode getIcmMode() {
            return icmMode;
        }

        @Override
        public Map<String, Supplier<?>> getGenericProperties() {
            return GenericRecordUtil.getGenericProperties("icmMode", this::getIcmMode);
        }
    }
}
