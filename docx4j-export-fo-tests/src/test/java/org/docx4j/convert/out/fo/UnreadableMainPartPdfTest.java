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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * PDF output of a document whose main part could not be read (17.3.6): a Docx4JException naming the
 * part and the reason, where toPDF threw a NullPointerException from FOSettings.setOpcPackage.
 */
public class UnreadableMainPartPdfTest {

    @Test
    public void toPdfSaysWhichPartCouldNotBeReadAndWhy() throws Exception {
        WordprocessingMLPackage pkg = WordprocessingMLPackage.load(new ByteArrayInputStream(docx(
                "<w:p><w:r><w:t>ORIGINAL</w:t></w:r></w:p></w:body")));   // the closing tag cut short
        try {
            Docx4J.toPDF(pkg, new ByteArrayOutputStream());
            fail("expected a Docx4JException");
        } catch (Docx4JException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().startsWith(
                    "The content of /word/document.xml could not be read, so it cannot be exported: "));
            assertNotNull("the read failure is the cause", expected.getCause());
        }
    }

    private static byte[] docx(String body) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream z = new ZipOutputStream(bos)) {
            put(z, "[Content_Types].xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/word/document.xml\" "
                    + "ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/></Types>");
            put(z, "_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>");
            put(z, "word/document.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>" + body + "</w:document>");
        }
        return bos.toByteArray();
    }

    private static void put(ZipOutputStream z, String name, String s) throws Exception {
        z.putNextEntry(new ZipEntry(name));
        z.write(s.getBytes(StandardCharsets.UTF_8));
        z.closeEntry();
    }
}
