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
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A metafile's text in the SVG the FO and HTML writers embed (17.3.6). The text extraction
 * probe's case 72 (extraction-probe-inspect.docx) failed Docx4J.toPDF: its Shift-JIS record
 * "年度①" is padded to an even length, decoding it kept the pad byte, a NUL, and the SVG
 * carried it into the FO as {@code &#0;}, which no XML parser reads; ① came out as U+FFFD
 * and "@", Java's Shift_JIS lacking the NEC circled digits cp932 has.
 */
public class MetafileSvgTextTest {

    private static final Charset CP932 = Charset.forName("windows-31j");

    @Test
    public void shiftJisRecordDrawsItsTextAndNoNul() throws Exception {
        String svg = svg(wmf(0x80, "SJIS 年度①".getBytes(CP932)));   // 11 bytes: padded
        assertFalse(svg, svg.contains("&#0;"));
        assertFalse(svg, svg.contains("\u0000"));
        assertTrue(svg, svg.contains("SJIS 年度①"));
    }

    @Test
    public void ansiRecordUnchanged() throws Exception {
        String svg = svg(wmf(0x00, "ANSI_TEXT".getBytes(StandardCharsets.US_ASCII)));
        assertTrue(svg, svg.contains(">ANSI_TEXT<"));
    }

    /** What the SVG can still get from a metafile: any character XML cannot carry is dropped. */
    @Test
    public void nonXmlCharactersAreDropped() throws Exception {
        Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element svg = doc.createElementNS("http://www.w3.org/2000/svg", "svg");
        doc.appendChild(svg);
        Element text = doc.createElementNS("http://www.w3.org/2000/svg", "text");
        text.setAttribute("font-family", "A\u0001B");
        text.appendChild(doc.createTextNode("a\u0000b\tc￾d\uD800e😀"));
        svg.appendChild(text);
        BatikMetafileSvgProvider.dropNonXmlCharacters(svg);
        assertEquals("AB", text.getAttribute("font-family"));
        assertEquals("ab\tcde😀", text.getTextContent());
    }

    private static String svg(byte[] wmf) throws Exception {
        Document doc = new BatikMetafileSvgProvider().toSvgDocument(wmf, 144, 72);
        assertNotNull("no SVG", doc);
        StringWriter out = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(out));
        return out.toString();
    }

    /** A placeable WMF, 1920 x 960 units at 1440 per inch: a font of the charset, selected, then one META_EXTTEXTOUT. */
    private static byte[] wmf(int charset, byte[] text) {
        ByteArrayOutputStream records = new ByteArrayOutputStream();
        records.writeBytes(record(0x020B, le(4).putShort((short) 0).putShort((short) 0)));        // SETWINDOWORG
        records.writeBytes(record(0x020C, le(4).putShort((short) 960).putShort((short) 1920)));   // SETWINDOWEXT
        ByteBuffer font = le(50);                                                                  // LOGFONT
        font.putShort((short) -240).putShort((short) 0).putShort((short) 0).putShort((short) 0).putShort((short) 400);
        font.put((byte) 0).put((byte) 0).put((byte) 0).put((byte) charset).put((byte) 0).put((byte) 0).put((byte) 0).put((byte) 0);
        font.put("Arial".getBytes(StandardCharsets.US_ASCII));
        records.writeBytes(record(0x02FB, font));                                                 // CREATEFONTINDIRECT
        records.writeBytes(record(0x012D, le(2).putShort((short) 0)));                            // SELECTOBJECT 0
        int padded = text.length + (text.length & 1);
        ByteBuffer ext = le(8 + padded).putShort((short) 400).putShort((short) 40).putShort((short) text.length).putShort((short) 0);
        ext.put(text);
        records.writeBytes(record(0x0A32, ext));                                                  // EXTTEXTOUT
        records.writeBytes(record(0x0000, le(0)));                                                // EOF
        byte[] body = records.toByteArray();

        ByteBuffer out = le(22 + 18 + body.length);
        out.putInt(0x9AC6CDD7).putShort((short) 0).putShort((short) 0).putShort((short) 0)
                .putShort((short) 1920).putShort((short) 960).putShort((short) 1440).putInt(0);
        short checksum = 0;
        for (int i = 0; i < 20; i += 2) checksum ^= out.getShort(i);
        out.putShort(checksum);
        out.putShort((short) 1).putShort((short) 9).putShort((short) 0x0300).putInt((18 + body.length) / 2)
                .putShort((short) 1).putInt(64).putShort((short) 0);
        out.put(body);
        return out.array();
    }

    private static ByteBuffer le(int n) {
        return ByteBuffer.allocate(n).order(ByteOrder.LITTLE_ENDIAN);
    }

    /** A WMF record: its size in words (size and function included), the function, the parameters. */
    private static byte[] record(int function, ByteBuffer params) {
        byte[] p = params.array();
        ByteBuffer r = le(6 + p.length).putInt((6 + p.length) / 2).putShort((short) function);
        r.put(p);
        return r.array();
    }
}
