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
import java.nio.charset.StandardCharsets;

import org.docx4j.Docx4J;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTAltChunk;

/**
 * The suite's fixture for an inspector with an HTML reader (CR-053 phase 3; text-extraction/INSPECTION.md):
 * {@value #FIXTURE}, four {@code altChunk}s whose {@code display: none} an inspector that runs the chunk's
 * import (WD 0.26's REQ-901) reports as {@code text.hidden}. Its expected inspection is
 * {@code expected-inspection-html.tsv} and {@code expected-inspection-html-status.tsv}, written by hand and
 * checked against Word 365's save of the fixture, read only under "html reader present". An inspector
 * without a reader skips each chunk as {@code not_decoded}.
 *
 * <ul>
 * <li>A: inline and class {@code display: none}, one inside another (one finding, the outermost
 *     element), one with no text (no finding), a hidden span;</li>
 * <li>B: what WD 0.27 added to the rule: a block shown inside a hidden {@code div}, a hidden cell, row
 *     and list item;</li>
 * <li>C: d9's well-formed XHTML with a DOCTYPE, its empty {@code div} self-closed: Word reads it as HTML, so
 *     the {@code div} holds what follows and hides it (D9 (iii));</li>
 * <li>D: an MHT chunk, its second HTML part never shown (WD 0.27: the first {@code text/html} part's tree).</li>
 * </ul>
 */
final class ExtractionHtmlCases {

    static final String FIXTURE = "extraction-probe-html.docx";

    static final String A = "<html><head><style>.h{display:none}</style></head><body>"
            + "<p>HTM_A_VISIBLE</p>"
            + "<p style=\"display:none\">HTM_A_INLINE</p>"
            + "<div class=\"h\"><p>HTM_A_CLASS</p></div>"
            + "<div style=\"display:none\"><p>HTM_A_OUTER</p><p style=\"display:none\">HTM_A_NESTED</p></div>"
            + "<p style=\"display:none\"></p>"
            + "<p>HTM_A_SPAN_<span style=\"display:none\">HTM_A_SPAN_HIDDEN</span>_AFTER</p>"
            + "<p>HTM_A_LAST</p>"
            + "</body></html>";

    static final String B = "<html><head></head><body>"
            + "<p>HTM_B_VISIBLE</p>"
            + "<div style=\"display:none\"><p style=\"display:block\">HTM_B_REVEALED</p><p>HTM_B_STAYS_HIDDEN</p></div>"
            + "<table border=\"1\"><tr><td style=\"display:none\">HTM_B_CELL_HIDDEN</td><td>HTM_B_CELL_SHOWN</td></tr>"
            + "<tr style=\"display:none\"><td>HTM_B_ROW_HIDDEN</td><td>HTM_B_ROW_HIDDEN_TOO</td></tr></table>"
            + "<ol><li>HTM_B_ITEM_ONE</li><li style=\"display:none\">HTM_B_ITEM_HIDDEN</li><li>HTM_B_ITEM_THREE</li></ol>"
            + "<p>HTM_B_LAST</p>"
            + "</body></html>";

    static final String C = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
            + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
            + "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>c</title></head><body>"
            + "<p>HTM_C_BEFORE</p>"
            + "<p style=\"display:none\">HTM_C_INLINE</p>"
            + "<div style=\"display:none\"/>"
            + "<p>HTM_C_AFTER</p>"
            + "</body></html>";

    static final String D = "MIME-Version: 1.0\r\n"
            + "Content-Type: multipart/related; boundary=\"HTM_BOUNDARY\"; type=\"text/html\"\r\n\r\n"
            + "--HTM_BOUNDARY\r\nContent-Type: text/html; charset=\"utf-8\"\r\nContent-Location: file:///C:/d.htm\r\n\r\n"
            + "<html><body><p>HTM_D_VISIBLE</p><p style=\"display:none\">HTM_D_FIRST_PART</p></body></html>\r\n"
            + "--HTM_BOUNDARY\r\nContent-Type: text/html; charset=\"utf-8\"\r\nContent-Location: file:///C:/second.htm\r\n\r\n"
            + "<html><body><p style=\"display:none\">HTM_D_SECOND_PART</p></body></html>\r\n"
            + "--HTM_BOUNDARY--\r\n";

    private ExtractionHtmlCases() {}

    static byte[] build() throws Exception {
        WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
        MainDocumentPart mdp = pkg.getMainDocumentPart();
        mdp.addStyledParagraphOfText("Title", "Text extraction probe: HTML chunks");
        mdp.addParagraphOfText("Built by docx4j (ExtractionHtmlCases, in docx4j-core-tests) for CR-053 phase 3. Four altChunks"
                + " follow, A to D, each between marker paragraphs; marker tokens start HTM_. With formatting marks on, hidden"
                + " text shows dotted.");
        mdp.addParagraphOfText("HTM_MAIN_BEFORE_A");
        chunk(mdp, "/word/chunk-a.htm", "text/html", A);
        mdp.addParagraphOfText("HTM_MAIN_BEFORE_B");
        chunk(mdp, "/word/chunk-b.htm", "text/html", B);
        mdp.addParagraphOfText("HTM_MAIN_BEFORE_C");
        chunk(mdp, "/word/chunk-c.xhtml", "application/xhtml+xml", C);
        mdp.addParagraphOfText("HTM_MAIN_BEFORE_D");
        chunk(mdp, "/word/chunk-d.mht", "message/rfc822", D);
        mdp.addParagraphOfText("HTM_MAIN_AFTER");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Docx4J.save(pkg, out, Docx4J.FLAG_NONE);
        return ExtractionProbeBuilder.redate(out.toByteArray());
    }

    private static void chunk(MainDocumentPart mdp, String name, String type, String markup) throws Exception {
        AlternativeFormatInputPart part = new AlternativeFormatInputPart(new PartName(name));
        part.setContentType(new ContentType(type));
        part.setBinaryData(markup.getBytes(StandardCharsets.UTF_8));
        CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk();
        ac.setId(mdp.addTargetPart(part).getId());
        mdp.getContent().add(ac);
    }
}
