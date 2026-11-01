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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.junit.Test;

/**
 * PDF output of tables nested 30 deep (17.3.6). Each table nests its FO about four elements
 * deeper, so this FO is deeper than Java 25's default jdk.xml.maxElementDepth of 100, which
 * failed the layout fixups' parse and then FOP's; docx4j's own limit (XmlParserLimits, 256) holds
 * for both, and for the area tree.
 */
public class DeepNestingPdfTest {

    @Test
    public void tablesThirtyDeepRenderToPdf() throws Exception {
        int n = 30;
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) b.append("<w:tbl><w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/></w:tblPr>"
                + "<w:tblGrid><w:gridCol w:w=\"2000\"/></w:tblGrid><w:tr><w:tc><w:tcPr><w:tcW w:w=\"2000\" w:type=\"dxa\"/></w:tcPr>");
        b.append("<w:p><w:r><w:t>DEEP_").append(n).append("</w:t></w:r></w:p>");
        for (int i = 0; i < n; i++) b.append("</w:tc></w:tr></w:tbl><w:p/>");
        Body body = (Body) XmlUtils.unwrap(XmlUtils.unmarshalString(
                "<w:body xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">" + b + "</w:body>"));
        WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
        pkg.getMainDocumentPart().getContent().addAll(body.getContent());

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        Docx4J.toPDF(pkg, pdf);
        try (PDDocument doc = Loader.loadPDF(pdf.toByteArray())) {
            String text = new PDFTextStripper().getText(doc);
            assertTrue(text, text.contains("DEEP_" + n));
        }
    }
}
