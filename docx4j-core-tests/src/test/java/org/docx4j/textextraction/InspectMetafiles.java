/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.textextraction;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Hand-built WMF and EMF files for the inspect fixture's cases 72 and 73 (CR-050 D21): text records
 * in the forms the inspector reads and the ones it declares it leaves out, written byte by byte from
 * MS-WMF and MS-EMF so that each record's charset, options and object-table index are exactly known.
 */
final class InspectMetafiles {

    private InspectMetafiles() {}

    // ---------------------------------------------------------------- little-endian writing

    static final class Out {
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        Out u8(int v) { b.write(v & 0xff); return this; }
        Out i16(int v) { return u8(v).u8(v >> 8); }
        Out i32(long v) { return i16((int) v).i16((int) (v >> 16)); }
        Out bytes(byte[] v) { b.write(v, 0, v.length); return this; }
        Out zeros(int n) { for (int i = 0; i < n; i++) u8(0); return this; }
        int size() { return b.size(); }
        byte[] toBytes() { return b.toByteArray(); }
    }

    // ---------------------------------------------------------------- WMF (case 72)

    static final int SHIFTJIS = 0x80, ANSI = 0x00, SYMBOL = 0x02;
    static final Charset CP932 = Charset.forName("windows-31j");

    /** A WMF record: its size in words (the size and function included), the function, the parameters. */
    static byte[] wmfRecord(int function, byte[] params) {
        Out o = new Out();
        o.i32((params.length + 6) / 2).i16(function).bytes(params);
        return o.toBytes();
    }

    /** META_CREATEFONTINDIRECT: a LOGFONT of the charset, 32-byte face name. */
    static byte[] wmfFont(int charset, String face) {
        Out o = new Out();
        o.i16(-120).i16(0).i16(0).i16(0).i16(400).u8(0).u8(0).u8(0).u8(charset).u8(0).u8(0).u8(0).u8(0);
        byte[] f = face.getBytes(StandardCharsets.US_ASCII);
        o.bytes(f).zeros(32 - f.length);
        return wmfRecord(0x02FB, o.toBytes());
    }

    static byte[] padEven(byte[] s) {
        if ((s.length & 1) == 0) return s;
        byte[] p = new byte[s.length + 1];
        System.arraycopy(s, 0, p, 0, s.length);
        return p;
    }

    /** META_TEXTOUT: count, string (padded to even), y, x. */
    static byte[] wmfTextOut(byte[] text, int x, int y) {
        Out o = new Out();
        o.i16(text.length).bytes(padEven(text)).i16(y).i16(x);
        return wmfRecord(0x0521, o.toBytes());
    }

    /** META_EXTTEXTOUT: y, x, count, options, string (padded to even), no dx. */
    static byte[] wmfExtTextOut(byte[] text, int x, int y, int options) {
        Out o = new Out();
        o.i16(y).i16(x).i16(text.length).i16(options).bytes(padEven(text));
        return wmfRecord(0x0A32, o.toBytes());
    }

    static byte[] wmfSelect(int index) { return wmfRecord(0x012D, new Out().i16(index).toBytes()); }
    static byte[] wmfDelete(int index) { return wmfRecord(0x01F0, new Out().i16(index).toBytes()); }

    /**
     * Case 72: four fonts, selected in turn through the object table (a deleted slot reused), with
     * text in the ANSI and Shift-JIS character sets, a TEXTOUT and EXTTEXTOUT records, one of glyph
     * indices and one with a SYMBOL font, which the inspector leaves out. 1920 x 960 units, 1440 per inch,
     * five lines 180 units apart (a 120-unit font).
     */
    static byte[] wmf72() {
        ByteArrayOutputStream records = new ByteArrayOutputStream();
        byte[][] rs = {
            wmfRecord(0x020B, new Out().i16(0).i16(0).toBytes()),          // SETWINDOWORG y, x
            wmfRecord(0x020C, new Out().i16(960).i16(1920).toBytes()),     // SETWINDOWEXT y, x
            wmfFont(SHIFTJIS, "MS Gothic"),                                 // object 0
            wmfFont(ANSI, "Arial"),                                         // object 1
            wmfSelect(1),
            wmfTextOut("WMF72_TEXTOUT".getBytes(StandardCharsets.US_ASCII), 40, 40),
            wmfSelect(0),
            wmfExtTextOut("WMF72_SJIS 年度①".getBytes(CP932), 40, 220, 0),
            wmfExtTextOut("WMF72_GLYPH_INDEX".getBytes(StandardCharsets.US_ASCII), 40, 400, 0x0010),
            wmfDelete(0),
            wmfFont(ANSI, "Arial"),                                         // reuses slot 0
            wmfSelect(0),
            wmfTextOut("WMF72_SLOT_REUSED".getBytes(StandardCharsets.US_ASCII), 40, 580),
            wmfFont(SYMBOL, "Symbol"),                                      // object 2
            wmfSelect(2),
            wmfTextOut("WMF72_SYMBOL".getBytes(StandardCharsets.US_ASCII), 40, 760),
            wmfRecord(0x0000, new byte[0]),                                 // EOF
        };
        int max = 0;
        for (byte[] r : rs) { records.write(r, 0, r.length); max = Math.max(max, r.length / 2); }
        Out header = new Out();
        int sizeWords = (18 + records.size()) / 2;
        header.i16(2).i16(9).i16(0x0300).i32(sizeWords).i16(3).i32(max).i16(0);
        Out placeable = new Out();
        placeable.i32(0x9AC6CDD7L).i16(0).i16(0).i16(0).i16(1920).i16(960).i16(1440).i32(0);
        byte[] pb = placeable.toBytes();
        int checksum = 0;
        for (int i = 0; i < 20; i += 2) checksum ^= (pb[i] & 0xff) | ((pb[i + 1] & 0xff) << 8);
        placeable.i16(checksum);
        Out all = new Out();
        all.bytes(placeable.toBytes()).bytes(header.toBytes()).bytes(records.toByteArray());
        return all.toBytes();
    }

    // ---------------------------------------------------------------- EMF (case 73)

    static final Charset UTF_16LE = StandardCharsets.UTF_16LE, CP1252 = Charset.forName("windows-1252");

    static byte[] emfRecord(int type, byte[] params) {
        Out o = new Out();
        o.i32(type).i32(params.length + 8).bytes(params);
        return o.toBytes();
    }

    static byte[] utf16(String s, int chars) {
        Out o = new Out();
        byte[] b = s.getBytes(UTF_16LE);
        o.bytes(b).zeros(chars * 2 - b.length);
        return o.toBytes();
    }

    /** EMR_EXTCREATEFONTINDIRECTW: the index, then a LogFontPanose (320 bytes). */
    static byte[] emfFont(int index, int charset, String face) {
        Out o = new Out();
        o.i32(index);
        o.i32(-120).i32(0).i32(0).i32(0).i32(400).u8(0).u8(0).u8(0).u8(charset).u8(0).u8(0).u8(0).u8(0);
        o.bytes(utf16(face, 32));                       // FaceName, 32 WCHARs
        o.bytes(utf16(face, 64)).bytes(utf16("Regular", 32)); // FullName, Style
        o.i32(0).i32(0).i32(0).i32(0).i32(0).i32(0).zeros(10).zeros(2); // Version .. Panose, padding
        return emfRecord(82, o.toBytes());
    }

    /** EMR_EXTTEXTOUTA (83) or W (84): bounds, GM_COMPATIBLE, scales, then the EmrText with offDx 0. */
    static byte[] emfExtTextOut(boolean unicode, byte[] text, int chars, int x, int y, int options) {
        Out o = new Out();
        o.i32(0).i32(0).i32(1920).i32(960);             // Bounds
        o.i32(1).i32(Float.floatToIntBits(1f)).i32(Float.floatToIntBits(1f));
        o.i32(x).i32(y).i32(chars).i32(8 + 28 + 40);    // Reference, Chars, offString: header 8, bounds to scale 28, EmrText 40
        o.i32(options).i32(0).i32(0).i32(0).i32(0).i32(0); // Options, Rectangle, offDx = 0 (no dx array)
        o.bytes(text).zeros((4 - text.length % 4) % 4);
        return emfRecord(unicode ? 84 : 83, o.toBytes());
    }

    static byte[] emfSelect(long index) { return emfRecord(37, new Out().i32(index).toBytes()); }

    /**
     * Case 73: a font at object-table index 1 (ANSI), a UTF-16 record, an ANSI record, a glyph-index
     * record the inspector leaves out, and a record drawn with the stock SYSTEM_FONT. 1920 x 960 device
     * pixels at 96 per inch, four lines 220 apart (a 120-pixel font).
     */
    static byte[] emf73() {
        byte[][] rs = {
            emfFont(1, ANSI, "Arial"),
            emfSelect(1),
            emfExtTextOut(true, "EMF73_UTF16 ünï".getBytes(UTF_16LE), 15, 40, 80, 0),
            emfExtTextOut(false, "EMF73_ANSI é".getBytes(CP1252), 12, 40, 300, 0),
            emfExtTextOut(true, "EMF73_GLYPH_INDEX".getBytes(UTF_16LE), 17, 40, 520, 0x0010),
            emfSelect(0x8000000DL),                     // SYSTEM_FONT
            emfExtTextOut(false, "EMF73_STOCK_FONT".getBytes(CP1252), 16, 40, 740, 0),
            emfRecord(14, new Out().i32(0).i32(16).i32(20).toBytes()), // EOF
        };
        ByteArrayOutputStream records = new ByteArrayOutputStream();
        for (byte[] r : rs) records.write(r, 0, r.length);
        Out h = new Out();
        h.i32(0).i32(0).i32(1920).i32(960);             // Bounds (device units)
        h.i32(0).i32(0).i32(50800).i32(25400);          // Frame (0.01 mm): 508 x 254 mm, the whole device; a tenth of it drew nothing in Word 365
        h.i32(0x464D4520L).i32(0x00010000L);
        h.i32(88 + records.size()).i32(rs.length + 1).i16(2).i16(0); // Bytes, Records, Handles, Reserved
        h.i32(0).i32(0).i32(0);                         // nDescription, offDescription, nPalEntries
        h.i32(1920).i32(960).i32(508).i32(254);         // Device (pixels), Millimeters
        Out all = new Out();
        all.bytes(emfRecord(1, h.toBytes())).bytes(records.toByteArray());
        return all.toBytes();
    }
}
