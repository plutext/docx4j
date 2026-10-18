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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cases 55 to 57, in their own fixture, {@value #FIXTURE} (19 October 2026, Jason: "build the separate
 * fixture"): a table of contents (REQ-520), a list a paragraph interrupts (REQ-703, REQ-510) and an
 * embedded workbook with hidden sheets (REQ-512, REQ-507). They are a fixture of their own so that the
 * main probe, which Word checked and the goldens are cut from, is not rebuilt for them. Every part is
 * written as raw XML, as {@link ExtractionPhase0Cases#document} writes a package.
 */
final class ExtractionStructureCases {

    static final String FIXTURE = "extraction-probe-structure.docx";

    private ExtractionStructureCases() {}

    static byte[] build(ExtractionProbeBuilder b) throws Exception {
        StringBuilder body = new StringBuilder();
        body.append(heading("Heading1", "Text extraction probe, structure cases (55 to 57)", null));
        body.append(pt("Built by docx4j (ExtractionStructureCases, in docx4j-core-tests), beside extraction-probe.docx."
                + " Each case starts with a heading, Case and its number."));

        // 55: a table of contents - the TOC field's cached result, its entries linking the headings' bookmarks
        b.begin(55, "a table of contents");
        body.append(caseHeading(55, "a table of contents"));
        body.append(p("<w:pPr><w:pStyle w:val='TOCHeading'/></w:pPr>" + r("TOC55_TITLE")));
        body.append(p("<w:pPr><w:pStyle w:val='TOC1'/>" + TOC_TABS + "</w:pPr>"
                + "<w:r><w:fldChar w:fldCharType='begin'/></w:r>"
                + "<w:r><w:instrText xml:space='preserve'> TOC \\o '1-3' \\h \\z \\u </w:instrText></w:r>"
                + "<w:r><w:fldChar w:fldCharType='separate'/></w:r>"
                + entry("_Toc55001", "HEAD55_ONE", "1")));
        body.append(p("<w:pPr><w:pStyle w:val='TOC1'/>" + TOC_TABS + "</w:pPr>" + entry("_Toc55002", "HEAD55_TWO", "1")));
        body.append(p("<w:r><w:fldChar w:fldCharType='end'/></w:r>"));
        body.append(heading("Heading1", "HEAD55_ONE", "_Toc55001"));
        body.append(pt("Text under the first heading the contents list."));
        body.append(heading("Heading1", "HEAD55_TWO", "_Toc55002"));
        body.append(pt("Text under the second."));
        b.always("TOC55_TITLE", "the contents heading is not the field's (REQ-520), so it is always read");
        b.token("HEAD55_ONE", "1", "1", "1", "toc: true", "2", "a heading the table of contents lists: its entry is read only under toc: true");
        b.token("HEAD55_TWO", "1", "1", "1", "toc: true", "2", "the same for the second heading");

        // 56: a list a paragraph interrupts, in plain text one line each; its Markdown is the golden's
        b.begin(56, "a list interrupted by paragraphs");
        body.append(caseHeading(56, "a list interrupted by paragraphs"));
        body.append(item(1, 0, "LST56_APPLES"));
        body.append(pt("LST56_MARGIN_NOTE"));                       // at the margin: the list ends, and resumes at 2
        body.append(item(1, 0, "LST56_PEARS"));
        body.append(p("<w:pPr><w:ind w:left='720'/></w:pPr>" + r("LST56_INDENTED_NOTE"))); // at the item's text: a follow-on
        body.append(item(1, 0, "LST56_PLUMS"));
        body.append(p(""));                                          // empty: neither ends the list nor joins it
        body.append(item(1, 0, "LST56_QUINCES"));
        body.append(item(1, 1, "LST56_QUINCE_ONE"));
        body.append(pt("LST56_NESTED_NOTE"));                       // at the margin inside the nested list
        body.append(item(1, 1, "LST56_QUINCE_TWO"));
        body.append(item(2, 0, "LST56_OTHER_LIST"));                // another list, straight after
        for (String token : new String[] { "LST56_APPLES", "LST56_MARGIN_NOTE", "LST56_PEARS", "LST56_INDENTED_NOTE",
                "LST56_PLUMS", "LST56_QUINCES", "LST56_QUINCE_ONE", "LST56_NESTED_NOTE", "LST56_QUINCE_TWO", "LST56_OTHER_LIST" })
            b.always(token, "one line each in plain text; the Markdown golden holds the list markers (REQ-703)");

        // 57: an embedded workbook whose second and third sheets are hidden and very hidden
        b.begin(57, "an embedded workbook with hidden sheets");
        body.append(caseHeading(57, "an embedded workbook with hidden sheets"));
        body.append(p(object(b, "rId4", "rId5", "Excel.Sheet.12", false)));
        b.gated("SHT57_SHOWN", "embedded: true", "the first sheet, visible");
        b.token("SHT57_HIDDEN", "0", "0", "0", "embedded: true; hidden: true", "1",
                "sheet state hidden: absent under embedded: true alone; read under hidden: true, as hidden text is");
        b.token("SHT57_VERY_HIDDEN", "0", "0", "0", "embedded: true; hidden: true", "1",
                "sheet state veryHidden: as the hidden sheet");
        b.gated("SHT57_LAST", "embedded: true", "the last sheet, visible after the hidden two");

        Map<String, byte[]> extra = new LinkedHashMap<>();
        extra.put("word/styles.xml", utf8(STYLES));
        extra.put("word/numbering.xml", utf8(NUMBERING));
        extra.put("word/embeddings/structure57.xlsx", workbook57());
        extra.put("word/media/preview57.png", png(64, 32, 0x88aa66));
        String relationships = relationship("rId1", "styles", "styles.xml", false)
                + relationship("rId2", "numbering", "numbering.xml", false)
                + relationship("rId4", "package", "embeddings/structure57.xlsx", false)
                + relationship("rId5", "image", "media/preview57.png", false);
        String overrides = override("/word/styles.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml")
                + override("/word/numbering.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml")
                + override("/word/embeddings/structure57.xlsx", XLSX);
        return document(body.toString(), extra, relationships, overrides);
    }

    private static final String TOC_TABS = "<w:tabs><w:tab w:val='right' w:leader='dot' w:pos='9016'/></w:tabs>";

    private static String caseHeading(int n, String title) {
        return p("<w:pPr><w:pStyle w:val='Heading2'/></w:pPr>" + r("Case " + n + ": " + title));
    }

    private static String heading(String style, String text, String bookmark) {
        String start = bookmark == null ? "" : "<w:bookmarkStart w:id='" + bookmark.substring(4) + "' w:name='" + bookmark + "'/>";
        String end = bookmark == null ? "" : "<w:bookmarkEnd w:id='" + bookmark.substring(4) + "'/>";
        return p("<w:pPr><w:pStyle w:val='" + style + "'/></w:pPr>" + start + r(text) + end);
    }

    /** A table of contents entry: a link to the heading's bookmark, a tab, and its PAGEREF field's cached page. */
    private static String entry(String bookmark, String text, String page) {
        return "<w:hyperlink w:anchor='" + bookmark + "' w:history='1'>" + r(text) + "<w:r><w:tab/></w:r>"
                + "<w:r><w:fldChar w:fldCharType='begin'/></w:r>"
                + "<w:r><w:instrText xml:space='preserve'> PAGEREF " + bookmark + " \\h </w:instrText></w:r>"
                + "<w:r><w:fldChar w:fldCharType='separate'/></w:r>" + r(page)
                + "<w:r><w:fldChar w:fldCharType='end'/></w:r></w:hyperlink>";
    }

    /** A list item: a paragraph of its own w:numPr. */
    private static String item(int numId, int ilvl, String text) {
        return p("<w:pPr><w:numPr><w:ilvl w:val='" + ilvl + "'/><w:numId w:val='" + numId + "'/></w:numPr></w:pPr>" + r(text));
    }

    private static final String STYLES = "<w:styles" + ExtractionProbeBuilder.NS + ">"
            + "<w:style w:type='paragraph' w:default='1' w:styleId='Normal'><w:name w:val='Normal'/><w:qFormat/></w:style>"
            + "<w:style w:type='paragraph' w:styleId='Heading1'><w:name w:val='heading 1'/><w:basedOn w:val='Normal'/>"
            + "<w:next w:val='Normal'/><w:qFormat/><w:pPr><w:keepNext/><w:outlineLvl w:val='0'/></w:pPr><w:rPr><w:b/><w:sz w:val='32'/></w:rPr></w:style>"
            + "<w:style w:type='paragraph' w:styleId='Heading2'><w:name w:val='heading 2'/><w:basedOn w:val='Normal'/>"
            + "<w:next w:val='Normal'/><w:qFormat/><w:pPr><w:keepNext/><w:outlineLvl w:val='1'/></w:pPr><w:rPr><w:b/><w:sz w:val='26'/></w:rPr></w:style>"
            + "<w:style w:type='paragraph' w:styleId='TOCHeading'><w:name w:val='TOC Heading'/><w:basedOn w:val='Heading1'/>"
            + "<w:next w:val='Normal'/><w:uiPriority w:val='39'/><w:unhideWhenUsed/><w:qFormat/><w:pPr><w:outlineLvl w:val='9'/></w:pPr></w:style>"
            + "<w:style w:type='paragraph' w:styleId='TOC1'><w:name w:val='toc 1'/><w:basedOn w:val='Normal'/><w:next w:val='Normal'/>"
            + "<w:uiPriority w:val='39'/><w:unhideWhenUsed/></w:style>"
            + "</w:styles>";

    /** Two decimal lists, each with levels 0 (%1.) and 1 (%1.%2.) at 720 and 1440, hanging 360. */
    private static final String NUMBERING;
    static {
        StringBuilder x = new StringBuilder("<w:numbering" + ExtractionProbeBuilder.NS + ">");
        for (int a = 1; a <= 2; a++) {
            x.append("<w:abstractNum w:abstractNumId='" + a + "'><w:multiLevelType w:val='multilevel'/>");
            for (int l = 0; l <= 1; l++) {
                x.append("<w:lvl w:ilvl='" + l + "'><w:start w:val='1'/><w:numFmt w:val='decimal'/><w:lvlText w:val='"
                        + (l == 0 ? "%1." : "%1.%2.") + "'/><w:lvlJc w:val='left'/><w:pPr><w:ind w:left='" + (720 + 720 * l)
                        + "' w:hanging='360'/></w:pPr></w:lvl>");
            }
            x.append("</w:abstractNum>");
        }
        x.append("<w:num w:numId='1'><w:abstractNumId w:val='1'/></w:num><w:num w:numId='2'><w:abstractNumId w:val='2'/></w:num>");
        NUMBERING = x.append("</w:numbering>").toString();
    }

    /** Four sheets: Shown, Hidden (state hidden), VeryHidden (state veryHidden), Last; one cell each. */
    static byte[] workbook57() throws Exception {
        String[][] sheets = { { "Shown", null, "SHT57_SHOWN" }, { "Hidden", "hidden", "SHT57_HIDDEN" },
                { "VeryHidden", "veryHidden", "SHT57_VERY_HIDDEN" }, { "Last", null, "SHT57_LAST" } };
        Map<String, byte[]> entries = new LinkedHashMap<>();
        StringBuilder types = new StringBuilder(override("/xl/workbook.xml", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"));
        StringBuilder list = new StringBuilder(), rels = new StringBuilder();
        for (int i = 0; i < sheets.length; i++) {
            types.append(override("/xl/worksheets/sheet" + (i + 1) + ".xml", "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"));
            list.append("<sheet name='" + sheets[i][0] + "' sheetId='" + (i + 1) + "'" + (sheets[i][1] == null ? "" : " state='" + sheets[i][1] + "'")
                    + " r:id='rId" + (i + 1) + "'/>");
            rels.append(relationship("rId" + (i + 1), "worksheet", "worksheets/sheet" + (i + 1) + ".xml", false));
        }
        entries.put("[Content_Types].xml", utf8(types(types.toString())));
        entries.put("_rels/.rels", utf8(rels(relationship("rId1", "officeDocument", "xl/workbook.xml", false))));
        entries.put("xl/workbook.xml", utf8("<workbook xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main' xmlns:r='"
                + REL.substring(0, REL.length() - 1) + "'><sheets>" + list + "</sheets></workbook>"));
        entries.put("xl/_rels/workbook.xml.rels", utf8(rels(rels.toString())));
        for (int i = 0; i < sheets.length; i++) {
            entries.put("xl/worksheets/sheet" + (i + 1) + ".xml", utf8("<worksheet xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main'>"
                    + "<sheetData><row r='1'><c r='A1' t='inlineStr'><is><t>" + sheets[i][2] + "</t></is></c></row></sheetData></worksheet>"));
        }
        return zip(entries);
    }
}
