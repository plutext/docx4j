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

import static org.docx4j.textextraction.ExtractionPhase0Cases.*;
import static org.docx4j.textextraction.ExtractionProbeBuilder.p;
import static org.docx4j.textextraction.ExtractionProbeBuilder.png;
import static org.docx4j.textextraction.ExtractionProbeBuilder.pt;
import static org.docx4j.textextraction.ExtractionProbeBuilder.r;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.docx4j.org.apache.poi.hpsf.ClassID;
import org.docx4j.org.apache.poi.poifs.filesystem.Ole10Native;
import org.docx4j.org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Cases 83 to 85, in their own fixture, {@value #FIXTURE} (2 November 2026, for the docx-tool-identifier's
 * CR-001 phase 0, Jason: "go ahead"): content the text model never reads, which some readers do. A PDF in
 * an OLE 1.0 package (REQ-512 opens a package's file only when it is {@code .txt}), a PDF as Acrobat embeds
 * it, an OLE2 storage whose {@code CONTENTS} stream is the PDF (REQ-512 opens no other OLE binary), and a
 * raster picture whose only text is its pixels (REQ-514: an image's text is its description; nothing in
 * the package holds the token, so only OCR reads it). Each PDF's page holds its token as text, uncompressed.
 * Every part is written as raw XML, as {@link ExtractionPhase0Cases#document} writes a package.
 */
final class ExtractionOpaqueCases {

    static final String FIXTURE = "extraction-probe-opaque.docx";

    /** Case 85's token, drawn in the picture's pixels and written nowhere in the package */
    static final String DRAWN = "PIX85_DRAWN_TEXT";

    /** The CLSID of Acrobat's PDF document (AcroExch.Document), the storage's class as Acrobat writes it */
    static final String ACROBAT_CLSID = "{B801CA65-A1FC-11D0-85AD-444553540000}";
    static final String ACROBAT_PROGID = "AcroExch.Document.DC";

    private ExtractionOpaqueCases() {}

    static byte[] build(ExtractionProbeBuilder b) throws Exception {
        StringBuilder body = new StringBuilder();
        body.append(p("<w:pPr><w:pStyle w:val='Heading1'/></w:pPr>" + r("Text extraction probe, opaque content (83 to 85)")));
        body.append(pt("Built by docx4j (ExtractionOpaqueCases, in docx4j-core-tests), beside extraction-probe.docx."
                + " Each case starts with a heading, Case and its number."));

        // 83: an OLE 1.0 package (ProgID Package) whose file is a PDF
        b.begin(83, "an OLE package containing a PDF");
        body.append(caseHeading(83, b.current.title));
        body.append(p(object(b, "rId3", "rId2", "Package", false)));
        b.token("PDF83_PACKAGE_TEXT", "0", "0", "0", "embedded: true", "0",
                "the page text of probe.pdf in an Ole10Native stream: REQ-512 opens a package's file only when its name ends .txt");

        // 84: the PDF as Acrobat embeds it - an OLE2 storage of Acrobat's class whose CONTENTS stream is the file
        b.begin(84, "a PDF embedded as Acrobat embeds it");
        body.append(caseHeading(84, b.current.title));
        body.append(p(object(b, "rId4", "rId2", ACROBAT_PROGID, false)));
        b.token("PDF84_ACROBAT_TEXT", "0", "0", "0", "embedded: true", "0",
                "the page text of the PDF in the storage's CONTENTS stream (ProgID " + ACROBAT_PROGID + "): REQ-512 opens no OLE binary"
                + " but a .txt package");

        // 85: a picture whose only text is its pixels: no descr, no title, no SVG, the token nowhere in the package
        b.begin(85, "a raster picture whose only text is drawn");
        body.append(caseHeading(85, b.current.title));
        byte[] drawn = drawn(DRAWN);
        body.append(p(picture("rId5", drawn)));
        b.token(DRAWN, "0", "0", "0", "alt_text: true", "0", "drawn in the PNG's pixels (black on white, 64 px, no anti-aliasing), in no text, attribute or"
                + " property: REQ-514's description is the picture's descr, else its name (Picture 1, under alt_text: true); only OCR reads it");

        Map<String, byte[]> extra = new LinkedHashMap<>();
        extra.put("word/styles.xml", utf8(STYLES));
        extra.put("word/media/preview83.png", png(64, 32, 0x7799bb));
        extra.put("word/embeddings/opaque83.bin", packaged(pdf("PDF83_PACKAGE_TEXT")));
        extra.put("word/embeddings/opaque84.bin", acrobat(pdf("PDF84_ACROBAT_TEXT")));
        extra.put("word/media/drawn85.png", drawn);
        String relationships = relationship("rId1", "styles", "styles.xml", false)
                + relationship("rId2", "image", "media/preview83.png", false)
                + relationship("rId3", "oleObject", "embeddings/opaque83.bin", false)
                + relationship("rId4", "oleObject", "embeddings/opaque84.bin", false)
                + relationship("rId5", "image", "media/drawn85.png", false);
        String overrides = override("/word/styles.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml")
                + override("/word/embeddings/opaque83.bin", OLE_OBJECT)
                + override("/word/embeddings/opaque84.bin", OLE_OBJECT);
        return document(body.toString(), extra, relationships, overrides);
    }

    static final String OLE_OBJECT = "application/vnd.openxmlformats-officedocument.oleObject";

    private static String caseHeading(int n, String title) {
        return p("<w:pPr><w:pStyle w:val='Heading2'/></w:pPr>" + r("Case " + n + ": " + title));
    }

    private static final String STYLES = "<w:styles" + ExtractionProbeBuilder.NS + ">"
            + "<w:style w:type='paragraph' w:default='1' w:styleId='Normal'><w:name w:val='Normal'/><w:qFormat/></w:style>"
            + "<w:style w:type='paragraph' w:styleId='Heading1'><w:name w:val='heading 1'/><w:basedOn w:val='Normal'/>"
            + "<w:next w:val='Normal'/><w:qFormat/><w:pPr><w:keepNext/><w:outlineLvl w:val='0'/></w:pPr><w:rPr><w:b/><w:sz w:val='32'/></w:rPr></w:style>"
            + "<w:style w:type='paragraph' w:styleId='Heading2'><w:name w:val='heading 2'/><w:basedOn w:val='Normal'/>"
            + "<w:next w:val='Normal'/><w:qFormat/><w:pPr><w:keepNext/><w:outlineLvl w:val='1'/></w:pPr><w:rPr><w:b/><w:sz w:val='26'/></w:rPr></w:style>"
            + "</w:styles>";

    /** An inline picture of the PNG at 96 dpi, named as Word names the first (Picture 1), with no descr or title. */
    static String picture(String image, byte[] png) throws IOException {
        return picture(image, png, 1);                      // the fixture's only drawing
    }

    /** As {@link #picture(String, byte[])}, its drawing id (and name) given: the compact probe holds other drawings. */
    static String picture(String image, byte[] png, int id) throws IOException {
        BufferedImage img = javax.imageio.ImageIO.read(new ByteArrayInputStream(png));
        long cx = img.getWidth() * 9525L, cy = img.getHeight() * 9525L;
        return "<w:r><w:drawing><wp:inline distT='0' distB='0' distL='0' distR='0'><wp:extent cx='" + cx + "' cy='" + cy + "'/>"
                + "<wp:effectExtent l='0' t='0' r='0' b='0'/><wp:docPr id='" + id + "' name='Picture " + id + "'/>"
                + "<wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect='1'/></wp:cNvGraphicFramePr>"
                + "<a:graphic><a:graphicData uri='http://schemas.openxmlformats.org/drawingml/2006/picture'><pic:pic><pic:nvPicPr>"
                + "<pic:cNvPr id='0' name='Picture " + id + "'/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed='" + image + "'/>"
                + "<a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x='0' y='0'/>"
                + "<a:ext cx='" + cx + "' cy='" + cy + "'/></a:xfrm><a:prstGeom prst='rect'><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
                + "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r>";
    }

    /** The token drawn black on white in a 64 px sans-serif face, anti-aliasing off, for OCR to read. */
    static byte[] drawn(String text) throws IOException {
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 64);
        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D measure = probe.createGraphics();
        FontMetrics m = measure.getFontMetrics(font);
        int w = m.stringWidth(text) + 96, h = m.getHeight() + 64;
        measure.dispose();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);
        g.setFont(font);
        g.drawString(text, 48, 32 + m.getAscent());
        g.dispose();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "png", bytes);
        return bytes.toByteArray();
    }

    /** A one-page PDF whose page shows the token in Helvetica, its content stream uncompressed. */
    static byte[] pdf(String token) {
        String content = "BT /F1 24 Tf 72 760 Td (" + token + ") Tj ET\n";
        String[] objects = {
            "<< /Type /Catalog /Pages 2 0 R >>",
            "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
            "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>",
            "<< /Length " + content.length() + " >>\nstream\n" + content + "endstream" };
        StringBuilder out = new StringBuilder("%PDF-1.4\n");
        int[] offsets = new int[objects.length];
        for (int i = 0; i < objects.length; i++) {
            offsets[i] = out.length();
            out.append(i + 1).append(" 0 obj\n").append(objects[i]).append("\nendobj\n");
        }
        int xref = out.length();
        out.append("xref\n0 ").append(objects.length + 1).append("\n0000000000 65535 f \n");
        for (int offset : offsets) out.append(String.format("%010d 00000 n \n", offset));
        out.append("trailer\n<< /Size ").append(objects.length + 1).append(" /Root 1 0 R >>\nstartxref\n").append(xref).append("\n%%EOF\n");
        return out.toString().getBytes(StandardCharsets.US_ASCII);
    }

    /** An OLE 1.0 package (case 41's form) whose file is probe.pdf */
    static byte[] packaged(byte[] pdf) throws Exception {
        Ole10Native nativeData = new Ole10Native("probe.pdf", "probe.pdf", "probe.pdf", pdf);
        ByteArrayOutputStream nativeBytes = new ByteArrayOutputStream(); nativeData.writeOut(nativeBytes);
        try (POIFSFileSystem fs = new POIFSFileSystem()) {
            Ole10Native.createOleMarkerEntry(fs);
            fs.createDocument(new ByteArrayInputStream(nativeBytes.toByteArray()), Ole10Native.OLE10_NATIVE);
            fs.getRoot().setStorageClsid(new ClassID("{0003000C-0000-0000-C000-000000000046}"));
            ByteArrayOutputStream out = new ByteArrayOutputStream(); fs.writeFilesystem(out);
            byte[] bytes = out.toByteArray();
            try (POIFSFileSystem check = new POIFSFileSystem(new ByteArrayInputStream(bytes))) {
                if (!Arrays.equals(pdf, Ole10Native.createFromEmbeddedOleObject(check).getDataBuffer()))
                    throw new IOException("Ole10Native round trip failed");
            }
            return bytes;
        }
    }

    /** An OLE2 storage as Acrobat writes one: Acrobat's class, \u0001Ole, \u0001CompObj and the PDF as CONTENTS */
    static byte[] acrobat(byte[] pdf) throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem()) {
            Ole10Native.createOleMarkerEntry(fs);
            fs.createDocument(new ByteArrayInputStream(compObj(ACROBAT_CLSID, "Acrobat Document", ACROBAT_PROGID)), "\u0001CompObj");
            fs.createDocument(new ByteArrayInputStream(pdf), "CONTENTS");
            fs.getRoot().setStorageClsid(new ClassID(ACROBAT_CLSID));
            ByteArrayOutputStream out = new ByteArrayOutputStream(); fs.writeFilesystem(out);
            return out.toByteArray();
        }
    }

    /** A CompObj stream (MS-OLEDS 2.3.8): the header with the class, the user type, no clipboard format, the ProgID. */
    static byte[] compObj(String clsid, String userType, String progId) {
        ByteBuffer out = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);
        out.putInt(0xFFFE0001).putInt(0x00000A03).putInt(0xFFFFFFFF);
        byte[] id = new byte[16];
        new ClassID(clsid).write(id, 0);
        out.put(id);
        ansi(out, userType);
        out.putInt(0);                       // AnsiClipboardFormat: none
        ansi(out, progId);                   // Reserved1: the ProgID
        out.putInt(0x71B239F4);              // UnicodeMarker
        unicode(out, userType);
        out.putInt(0);                       // UnicodeClipboardFormat: none
        out.putInt(0);                       // Reserved2: empty
        return Arrays.copyOf(out.array(), out.position());
    }

    private static void ansi(ByteBuffer out, String s) {
        out.putInt(s.length() + 1).put(s.getBytes(StandardCharsets.US_ASCII)).put((byte) 0);
    }

    private static void unicode(ByteBuffer out, String s) {
        out.putInt(s.length() + 1).put(s.getBytes(StandardCharsets.UTF_16LE)).putShort((short) 0);
    }
}
