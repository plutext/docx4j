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
package org.docx4j.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.junit.After;
import org.junit.Test;

/**
 * docx4j's element-depth limit on its XML parsers (17.3.6): Java 25's jaxp.properties sets
 * jdk.xml.maxElementDepth=100, which a document 32 tables deep exceeds; docx4j's limit is 256.
 */
public class XmlParserLimitsTest {

    @After
    public void clear() {
        Docx4jProperties.getProperties().remove(XmlParserLimits.MAX_ELEMENT_DEPTH_PROPERTY);
        System.clearProperty("jdk.xml.maxElementDepth");
    }

    /** 40 tables in tables, element depth 126: past Java 25's 100, within docx4j's 256 */
    @Test
    public void tablesFortyDeepLoadAndSave() throws Exception {
        WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(nested(40)));
        assertTrue(XmlUtils.marshaltoString(pkg.getMainDocumentPart().getContents()).contains("DEEP_40"));
        ByteArrayOutputStream saved = new ByteArrayOutputStream();
        pkg.save(saved);
        WordprocessingMLPackage again = WordprocessingMLPackage.load(new ByteArrayInputStream(saved.toByteArray()));
        assertTrue(XmlUtils.marshaltoString(again.getMainDocumentPart().getContents()).contains("DEEP_40"));
    }

    /** A part deeper than the limit is unreadable, as CR-065 D1 has it: its content is refused, not emptied. */
    @Test
    public void aPartDeeperThanTheLimitIsUnreadable() throws Exception {
        Docx4jProperties.setProperty(XmlParserLimits.MAX_ELEMENT_DEPTH_PROPERTY, "50");
        assertUnreadable(nested(20));   // depth 66
    }

    /** A hostile depth is refused as unreadable, where docx4j's recursion ran out of stack (about 700) */
    @Test
    public void aHostileDepthIsUnreadableNotAStackOverflow() throws Exception {
        assertUnreadable(nested(3000)); // depth 9006
    }

    private static void assertUnreadable(byte[] docx) throws Exception {
        WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(docx));
        MainDocumentPart main = pkg.getMainDocumentPart();
        try {
            main.getContents();
            fail("expected the part to be unreadable");
        } catch (Docx4JException expected) {
            assertTrue(main.isContentUnreadable());
        }
    }

    @Test
    public void theLimitAndItsSettings() {
        assertEquals(Integer.valueOf(256), XmlParserLimits.maxElementDepth());
        Docx4jProperties.setProperty(XmlParserLimits.MAX_ELEMENT_DEPTH_PROPERTY, "0");
        assertEquals(Integer.valueOf(0), XmlParserLimits.maxElementDepth());   // no limit
        Docx4jProperties.setProperty(XmlParserLimits.MAX_ELEMENT_DEPTH_PROPERTY, "deep");
        assertEquals(Integer.valueOf(256), XmlParserLimits.maxElementDepth());
        Docx4jProperties.getProperties().remove(XmlParserLimits.MAX_ELEMENT_DEPTH_PROPERTY);
        System.setProperty("jdk.xml.maxElementDepth", "64");
        assertNull("the JVM's own setting stands", XmlParserLimits.maxElementDepth());
    }

    /** The FO's Source gives a Transformer the same events a StreamSource does: namespaces, prefixes, attributes. */
    @Test
    public void theSourceParsesAsAStreamSourceDoes() throws Exception {
        String xml = "<fo:root xmlns:fo='http://www.w3.org/1999/XSL/Format' xmlns:fox='http://xmlgraphics.apache.org/fop/extensions'>"
                + "<fo:block fox:x='1' font-size='10pt'>a <fo:inline>b</fo:inline></fo:block><other xmlns='urn:x'><y/></other></fo:root>";
        assertEquals(identity(new StreamSource(new StringReader(xml))), identity(XmlParserLimits.source(new StringReader(xml))));
    }

    private static String identity(Source source) throws Exception {
        StringWriter out = new StringWriter();
        XmlUtils.getTransformerFactory().newTransformer().transform(source, new StreamResult(out));
        return out.toString();
    }

    /** A docx whose body holds tables nested n deep: element depth 2 + 3n + 4 */
    private static byte[] nested(int n) throws Exception {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) b.append("<w:tbl><w:tblGrid><w:gridCol w:w=\"2000\"/></w:tblGrid><w:tr><w:tc>");
        b.append("<w:p><w:r><w:t>DEEP_").append(n).append("</w:t></w:r></w:p>");
        for (int i = 0; i < n; i++) b.append("</w:tc></w:tr></w:tbl><w:p/>");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream z = new ZipOutputStream(bos)) {
            put(z, "[Content_Types].xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/word/document.xml\" "
                    + "ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/></Types>");
            put(z, "_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>");
            put(z, "word/document.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>" + b + "</w:body></w:document>");
        }
        return bos.toByteArray();
    }

    private static void put(ZipOutputStream z, String name, String s) throws Exception {
        z.putNextEntry(new ZipEntry(name));
        z.write(s.getBytes(StandardCharsets.UTF_8));
        z.closeEntry();
    }
}
