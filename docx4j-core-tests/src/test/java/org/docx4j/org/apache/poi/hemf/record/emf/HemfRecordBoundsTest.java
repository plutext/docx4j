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

import static org.docx4j.org.apache.poi.hemf.record.emf.HemfRecordIterator.HEADER_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.docx4j.org.apache.poi.hemf.record.emf.HemfComment.EmfComment;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfComment.EmfCommentDataWMF;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfMisc.EmfEof;
import org.docx4j.org.apache.poi.hemf.record.emf.HemfMisc.EmfExtCreatePen;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusDraw.EmfPlusDrawDriverString;
import org.docx4j.org.apache.poi.hemf.record.emfplus.HemfPlusMisc;
import org.docx4j.org.apache.poi.hwmf.record.HwmfBitmapDib;
import org.docx4j.org.apache.poi.hwmf.usermodel.HwmfPicture;
import org.docx4j.org.apache.poi.util.LittleEndian;
import org.docx4j.org.apache.poi.util.LittleEndianInputStream;
import org.docx4j.org.apache.poi.util.RecordFormatException;
import org.junit.Test;

/**
 * The record-level tests of the POI fixes back-ported in November 2026 (the package README of
 * {@code org.docx4j.org.apache.poi.hwmf} lists them), from POI's {@code TestHemfFill},
 * {@code TestHemfComment}, {@code TestHemfMisc}, {@code TestHemfPlusDraw} and
 * {@code TestHemfPlusMisc}, adapted to JUnit 4. In this package because
 * {@link HemfFill#readBitmap} is package-private, as upstream's tests are. Two depart from
 * upstream, as the copy does: the comment record's limit is {@link HwmfPicture}'s (this copy has
 * no {@code HemfComment.setMaxRecordLength}), and an EOF record whose size disagrees is logged,
 * not rejected.
 */
public class HemfRecordBoundsTest {

    // ------------------------------------------------------------------ TestHemfFill (440834229f)

    /**
     * cbBmi and cbBits come straight from the record, so a bitmap that claims to extend past the
     * end of the record must be rejected before anything is buffered: here they add up to almost
     * 2 GB while the record only holds 8 bytes.
     */
    @Test
    public void bitmapExceedingRecordIsRejected() throws Exception {
        assertReadBitmapRejects(8, 1, 0x40000000, 0x7FFFFFF0, 0x3FFFFF00);
    }

    /** cbBmi + cbBits must not overflow into a negative size, even where the record claims room for them. */
    @Test
    public void bitmapSizeOverflowIsRejected() throws Exception {
        assertReadBitmapRejects(0xFFFFFFFFL, 1, 0x40000000, 0x40000001, 0x40000000);
    }

    private static void assertReadBitmapRejects(long recordSize, int offBmi, int cbBmi, int offBits, int cbBits) throws Exception {
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(new byte[16]))) {
            HemfFill.readBitmap(leis, new HwmfBitmapDib(), HEADER_SIZE, recordSize, offBmi, cbBmi, offBits, cbBits);
            fail("expected RecordFormatException");
        } catch (RecordFormatException expected) {
            // as upstream
        }
    }

    // ------------------------------------------------------------------ TestHemfComment (140db93794)

    /** EMR_COMMENT body (after type and size) holding a public EMR_COMMENT_WINDOWS_METAFILE of wmfSize bytes */
    private static byte[] wmfComment(int wmfSize) {
        byte[] data = new byte[28 + wmfSize];
        LittleEndian.putInt(data, 0, 24 + wmfSize);       // dataSize
        LittleEndian.putInt(data, 4, 0x43494447);         // EMR_COMMENT_PUBLIC
        LittleEndian.putInt(data, 8, 0x80000001);         // EMR_COMMENT_WINDOWS_METAFILE
        LittleEndian.putUInt(data, 24, wmfSize);          // WinMetafileSize
        return data;
    }

    private static EmfComment parse(byte[] data) throws IOException {
        EmfComment comment = new EmfComment();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            comment.init(leis, data.length, HemfRecordType.comment.id);
        }
        return comment;
    }

    /**
     * Comment records carry whole embedded metafiles, often a few MB. Upstream's 1 MB default
     * (b7f1f55a47, never in this copy) dropped them until 140db93794; this copy has always
     * taken HwmfPicture's 100 MB.
     */
    @Test
    public void embeddedWmfOverOneMegabyte() throws IOException {
        int wmfSize = 2_000_000;
        EmfComment comment = parse(wmfComment(wmfSize));
        assertEquals(wmfSize, ((EmfCommentDataWMF) comment.getCommentData()).getWMFData().length);
    }

    @Test
    public void maxRecordLengthIsEnforced() throws IOException {
        int saved = HwmfPicture.getMaxRecordLength();
        try {
            HwmfPicture.setMaxRecordLength(1000);
            parse(wmfComment(2000));
            fail("expected RecordFormatException");
        } catch (RecordFormatException expected) {
            // as upstream, through this copy's limit
        } finally {
            HwmfPicture.setMaxRecordLength(saved);
        }
    }

    // ------------------------------------------------------------------ TestHemfMisc (69f8eb6fb8, 5fb3244453)

    /**
     * EMR_EXTCREATEPEN uses its unsigned NumStyleEntries as the length of the dash-pattern array:
     * a crafted count of 0x40000000 has to be capped before the array is created.
     */
    @Test
    public void numStyleEntriesIsBounded() throws Exception {
        byte[] data = new byte[44];
        LittleEndian.putInt(data, 20, 0x7);         // penStyle with PS_USERSTYLE line dash
        LittleEndian.putInt(data, 40, 0x40000000);  // numStyleEntries
        EmfExtCreatePen penRecord = new EmfExtCreatePen();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            // long arguments: with an int record id the WMF pen's init(leis, long, int) would be chosen
            penRecord.init(leis, (long) data.length, (long) 0x5F);
            fail("expected RecordFormatException");
        } catch (RecordFormatException expected) {
            // as upstream
        }
    }

    @Test
    public void eof() throws IOException {
        assertEquals(12, eof(12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
    }

    @Test
    public void eofWithPalette() throws IOException {
        assertEquals(16, eof(16, 1, 0, 0, 0, 16, 0, 0, 0, 99, 98, 97, 96, 0, 0, 0, 0));
    }

    /** the palette's offset points back into the record's own fields */
    @Test(expected = IllegalArgumentException.class)
    public void eofPaletteOffsetInvalid() throws IOException {
        eof(12, 1, 0, 0, 0, 1, 0, 0, 0, 99, 98, 0, 0, 0, 0);
    }

    /** the palette runs past the record's end */
    @Test(expected = IllegalArgumentException.class)
    public void eofPaletteOverrunsRecord() throws IOException {
        eof(12, 1, 0, 0, 0, 16, 0, 0, 0, 99, 98, 97, 96, 0, 0, 0, 0);
    }

    /**
     * docx4j departs from upstream here: a record size the fields disagree with is logged, not an
     * IllegalArgumentException, since the last record failing would lose a picture that draws whole.
     */
    @Test
    public void eofSizeMismatchIsLogged() throws IOException {
        assertEquals(12, eof(11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
    }

    private static long eof(long recordSize, int... bytes) throws IOException {
        byte[] data = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) data[i] = (byte) bytes[i];
        return new EmfEof().init(new LittleEndianInputStream(new ByteArrayInputStream(data)), recordSize, 0L);
    }

    // ------------------------------------------------------------------ TestHemfPlusDraw (e5d0cad1d5)

    /**
     * EmfPlusDrawDriverString allocates glyphCount*2 bytes: a count of 0x40000000 wrapped that to a
     * negative int, which slipped past the MAX_OBJECT_SIZE cap; the cap must reject it.
     */
    @Test
    public void glyphCountByteLengthDoesNotOverflow() throws Exception {
        byte[] data = new byte[32];
        LittleEndian.putInt(data, 12, 0x40000000);   // glyphCount, after brushId, optionsFlags, matrixPresent
        EmfPlusDrawDriverString record = new EmfPlusDrawDriverString();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            record.init(leis, data.length, 0x4036, 0);
            fail("expected RecordFormatException");
        } catch (RecordFormatException expected) {
            // as upstream
        }
    }

    // ------------------------------------------------------------------ TestHemfPlusMisc (f6f957bb5d)

    @Test
    public void flagOnlyRecord() throws IOException {
        assertEquals(3, flagOnly(0));
    }

    /** a data size other than 0 is logged, not an assertion */
    @Test
    public void flagOnlyRecordWithData() throws IOException {
        assertEquals(3, flagOnly(1));
    }

    private static int flagOnly(long dataSize) throws IOException {
        HemfPlusMisc.EmfPlusFlagOnly rec = new HemfPlusMisc.EmfPlusSetPixelOffsetMode();
        rec.init(new LittleEndianInputStream(new ByteArrayInputStream(new byte[0])), dataSize, 0, 3);
        return rec.getFlags();
    }
}
