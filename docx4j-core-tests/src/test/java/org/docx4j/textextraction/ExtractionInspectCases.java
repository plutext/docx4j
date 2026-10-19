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
import static org.docx4j.textextraction.ExtractionProbeBuilder.NS;
import static org.docx4j.textextraction.ExtractionProbeBuilder.PAGE;
import static org.docx4j.textextraction.ExtractionProbeBuilder.WPS_URI;
import static org.docx4j.textextraction.ExtractionProbeBuilder.ac;
import static org.docx4j.textextraction.ExtractionProbeBuilder.choice;
import static org.docx4j.textextraction.ExtractionProbeBuilder.comment;
import static org.docx4j.textextraction.ExtractionProbeBuilder.commentRef;
import static org.docx4j.textextraction.ExtractionProbeBuilder.fallback;
import static org.docx4j.textextraction.ExtractionProbeBuilder.field;
import static org.docx4j.textextraction.ExtractionProbeBuilder.note;
import static org.docx4j.textextraction.ExtractionProbeBuilder.noteRef;
import static org.docx4j.textextraction.ExtractionProbeBuilder.p;
import static org.docx4j.textextraction.ExtractionProbeBuilder.png;
import static org.docx4j.textextraction.ExtractionProbeBuilder.pt;
import static org.docx4j.textextraction.ExtractionProbeBuilder.r;
import static org.docx4j.textextraction.ExtractionProbeBuilder.separators;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.docx4j.Docx4J;
import org.docx4j.customXmlProperties.DatastoreItem;
import org.docx4j.fonts.FontEmbedder;
import org.docx4j.model.datastorage.CustomXmlDataStorageImpl;
import org.docx4j.model.structure.PageSizePaper;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.PeoplePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTFootnotes;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.Comments;
import org.docx4j.wml.Hdr;

/**
 * Cases 58 onward, in their own fixture, {@value #FIXTURE} (CR-050 phase 0, 19 October 2026, Jason:
 * "implement CR-050 phase 0"): the inspector's boundary cases (CR-050 section 3), so that the main probe,
 * which Word checked and the goldens are cut from, is not rebuilt for them. docx4j makes the package
 * (relationships, content types, the embedded fonts through {@link FontEmbedder}, the embedded packages
 * and the chunk); the story parts are written here as XML text, as {@link ExtractionProbeBuilder} writes
 * the main probe's. The fonts are made by {@code support/inspect-fonts/make_fonts.py} from Liberation
 * Sans (SIL OFL 1.1), renamed and altered.
 *
 * <p>Every external target names {@code CR050-MISSING} or a host under {@code .invalid}, which never
 * resolves: nothing the fixture refers to exists, and nothing it holds runs (the chunk's script is a
 * comment; its event handler is {@code void 0}).</p>
 */
final class ExtractionInspectCases {

    static final String FIXTURE = "extraction-probe-inspect.docx";
    static final Path FONTS = Path.of("docx4j-core-tests/src/test/resources/text-extraction/support/inspect-fonts");

    /** The page colour, w:background (case 62): every run without a nearer background is drawn on it */
    static final String PAGE_COLOUR = "EAF1DD";
    static final String STORE_ITEM_67 = "{3F1C0E5A-0C05-4E50-8A67-CA5E67000067}";
    static final String WPI_URI = "http://schemas.microsoft.com/office/word/2010/wordprocessingInk";
    /**
     * The relationship type of case 63's part of an unknown kind. A broken relationship (an internal target
     * missing) is not in the fixture: docx4j's loader refuses such a package (Load3: "cannot find part"), so
     * CR-050 phase 1 builds that case in its test.
     */
    static final String UNKNOWN_REL = "http://schemas.docx4j.org/2026/relationships/inspect-probe";

    /** A4 in EMU (PAGE's 11906 x 16838 twips) */
    static final long PAGE_W = 11906L * 635, PAGE_H = 16838L * 635;

    /** The faces case 65 embeds: family name, file, the run's text, and the run's marker */
    static final String[][] FACES = {
        { "Inspect Probe Word", "inspect-probe-word.ttf", "CAT", "FNT65_WORD" },
        { "Inspect Probe Twin", "inspect-probe-twin.ttf", "CAT", "FNT65_TWIN" },
        { "Inspect Probe Blank", "inspect-probe-blank.ttf", "INVISIBLE INK", "FNT65_BLANK" },
        { "Inspect Probe Zero", "inspect-probe-zero.ttf", "MIXED", "FNT65_ZERO" },
        { "Inspect Probe Named", "inspect-probe-named.ttf", "MAT", "FNT65_NAMED" },
        { "Inspect Probe Shared", "inspect-probe-shared.ttf", "FEE", "FNT65_SHARED" },
        { "Inspect Probe Careful", "inspect-probe-careful.ttf", "SIZE", "FNT65_CAREFUL" },
        { "Inspect Probe Plain", "inspect-probe-plain.ttf", "PLAIN", "FNT65_PLAIN" },
        { "Inspect Probe Symbol", "inspect-probe-symbol.ttf", "\uf041\uf061\uf06b", "FNT65_SYMBOL" },
        { "Inspect Probe Substitution", "inspect-probe-substitution.ttf", "BOB", "FNT65_CCMP" },
    };

    private ExtractionInspectCases() {}

    static byte[] build(ExtractionProbeBuilder b) throws Exception {
        // a builder of its own for the raw parts, the drawing helpers and their counters
        ExtractionProbeBuilder h = new ExtractionProbeBuilder();
        WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage(PageSizePaper.A4, false);
        MainDocumentPart mdp = pkg.getMainDocumentPart();

        HeaderPart unused = new HeaderPart(new PartName("/word/header1.xml"));   // no section references it (case 59)
        unused.setJaxbElement(new Hdr());
        mdp.addTargetPart(unused);
        HeaderPart used = new HeaderPart(new PartName("/word/header2.xml"));    // the section's, with a watermark (case 63)
        used.setJaxbElement(new Hdr());
        String usedId = mdp.addTargetPart(used).getId();
        FootnotesPart footnotes = new FootnotesPart();
        footnotes.setJaxbElement(new CTFootnotes());
        mdp.addTargetPart(footnotes);
        CommentsPart comments = new CommentsPart();
        comments.setJaxbElement(new Comments());
        mdp.addTargetPart(comments);
        DocumentSettingsPart settings = mdp.getDocumentSettingsPart();
        if (settings == null) {
            settings = new DocumentSettingsPart();
            settings.setJaxbElement(new CTSettings());
            mdp.addTargetPart(settings);
        }

        StringBuilder body = new StringBuilder();
        body.append(p("<w:pPr><w:pStyle w:val='Title'/></w:pPr>" + r("Text extraction probe, inspection cases (58 to 67)")));
        body.append(pt("Built by docx4j (ExtractionInspectCases, in docx4j-core-tests), beside extraction-probe.docx, for"
                + " CR-050. Each case starts with a heading, Case and its number. The page colour is pale green."));

        formatting(b, body);
        geometry(b, h, body);
        characters(b, body);
        fields(b, body);
        background(b, body);
        String fns = inventory(b, h, pkg, mdp, body);
        links(b, mdp, body);
        fonts(b, pkg, body);
        pictures(b, h, pkg, mdp, body);
        stored(b, mdp, body);

        body.append("<w:sectPr><w:headerReference w:type='default' r:id='" + usedId + "'/>" + PAGE + "</w:sectPr>");
        String root = NS.replace(" mc:Ignorable", " xmlns:wpi='" + WPI_URI + "' mc:Ignorable");
        h.raw.put(mdp.getPartName().getName(), "<w:document" + root + "><w:background w:color='" + PAGE_COLOUR + "'/><w:body>"
                + body + "</w:body></w:document>");
        h.raw.put(unused.getPartName().getName(), "<w:hdr" + NS + ">" + pt("HDR59_UNUSED")
                + p("<w:r>" + ac("", choice("wps", anchorBox(h, "page", 8_000_000L, "page", 914_400L, 1_828_800L, 457_200L, "",
                        pt("HDR59_BOX")))) + "</w:r>") + "</w:hdr>");
        h.raw.put(used.getPartName().getName(), "<w:hdr" + NS + ">" + pt("HDR63_USED") + watermark(h, "WMK63_WATERMARK") + "</w:hdr>");
        h.raw.put(footnotes.getPartName().getName(), "<w:footnotes" + NS + ">" + separators("footnote") + fns + "</w:footnotes>");
        h.raw.put(comments.getPartName().getName(), "<w:comments" + NS + ">" + comment(0, "10000063", "CMT63_ANCHORED")
                + comment(1, "10000064", "CMT63_UNANCHORED") + "</w:comments>");
        h.raw.put(pkg.getMainDocumentPart().getStyleDefinitionsPart().getPartName().getName(), STYLES);
        h.raw.put(settings.getPartName().getName(), SETTINGS);

        ByteArrayOutputStream saved = new ByteArrayOutputStream();
        Docx4J.save(pkg, saved, Docx4J.FLAG_NONE);
        return h.rewrite(saved.toByteArray());
    }

    // ------------------------------------------------------------------ 58: formatting

    static void formatting(ExtractionProbeBuilder b, StringBuilder body) {
        b.begin(58, "formatting: hidden text, tiny text, colours at the threshold, a collapsed heading");
        body.append(caseHeading(58, b.current.title));
        body.append(p(r("<w:rStyle w:val='ProbeHidden'/><w:vanish w:val='false'/>", "VAN58_SHOWN")));
        body.append(p(r("<w:rStyle w:val='ProbeHidden'/>", "VAN58_INHERITED")));
        body.append(p(r("PAR58_VISIBLE ") + r("<w:vanish/>", "PAR58_PART_HIDDEN")));
        body.append(p("<w:pPr><w:rPr><w:vanish/></w:rPr></w:pPr>" + r("<w:vanish/>", "PAR58_WHOLE")));
        body.append(p(r("<w:sz w:val='4'/><w:szCs w:val='4'/>", "SZ58_TWO") + r(" ")
                + r("<w:sz w:val='3'/><w:szCs w:val='3'/>", "SZ58_ONE_HALF")));
        body.append(p(r("<w:sz w:val='24'/><w:szCs w:val='3'/><w:cs/>", "SZ58_COMPLEX") + r(" ")
                + r("<w:sz w:val='24'/><w:szCs w:val='3'/>", "SZ58_NOT_COMPLEX")));
        // on white paragraph shading, so that the page colour is not their background
        body.append(p("<w:pPr><w:shd w:val='clear' w:color='auto' w:fill='FFFFFF'/></w:pPr>"
                + r("<w:color w:val='D3D3D3'/>", "COL58_NEAR") + r(" ") + r("<w:color w:val='D2D2D2'/>", "COL58_FAR")));
        body.append(p("<w:pPr><w:pStyle w:val='Heading3'/><w15:collapsed/></w:pPr>" + r("HEAD58_COLLAPSED")));
        body.append(pt("BODY58_FIRST"));
        body.append(p("<w:pPr><w:pStyle w:val='Heading4'/><w15:collapsed/></w:pPr>" + r("HEAD58_NESTED")));
        body.append(pt("BODY58_LAST"));
        body.append(p("<w:pPr><w:pStyle w:val='Heading3'/></w:pPr>" + r("HEAD58_AFTER")));
        b.always("VAN58_SHOWN", "w:vanish false over a hidden character style: shown; no inspection finding");
        b.gated("VAN58_INHERITED", "hidden: true", "hidden through its character style; inspection: text.hidden");
        b.always("PAR58_VISIBLE", "the shown part of a partly hidden paragraph");
        b.gated("PAR58_PART_HIDDEN", "hidden: true", "inspection: text.hidden (its paragraph mark is not hidden)");
        b.gated("PAR58_WHOLE", "hidden: true", "runs and mark hidden; inspection: paragraph.hidden, one finding");
        b.always("SZ58_TWO", "exactly 2pt: no inspection finding");
        b.always("SZ58_ONE_HALF", "1.5pt; inspection: text.tiny");
        b.always("SZ58_COMPLEX", "w:cs: drawn at w:szCs, 1.5pt; inspection: text.tiny");
        b.always("SZ58_NOT_COMPLEX", "drawn at w:sz, 12pt (w:szCs 1.5pt unused): no inspection finding");
        b.always("COL58_NEAR", "D3D3D3 on white, contrast 1.497:1; inspection: text.near_background");
        b.always("COL58_FAR", "D2D2D2 on white, contrast 1.512:1: no inspection finding");
        b.always("HEAD58_COLLAPSED", "a heading 3 collapsed by default (w15:collapsed)");
        b.always("BODY58_FIRST", "inspection: paragraph.collapsed, from here to BODY58_LAST");
        b.always("HEAD58_NESTED", "a collapsed heading inside the collapsed body: no finding of its own");
        b.always("BODY58_LAST", "the collapsed body ends here, at the next heading 3");
        b.always("HEAD58_AFTER", "");
    }

    // ------------------------------------------------------------------ 59: geometry

    static void geometry(ExtractionProbeBuilder b, ExtractionProbeBuilder h, StringBuilder body) {
        b.begin(59, "geometry: off the page, partly on it, flat, indeterminate, aligned, hidden, in an unused header");
        body.append(caseHeading(59, b.current.title));
        body.append(p(r("Off the page: ") + box(anchorBox(h, "page", PAGE_W + 400_000L, "page", 914_400L, 1_828_800L, 457_200L, "",
                pt("GEO59_OFF_PAGE")))));
        body.append(p(r("Partly on the page: ") + box(anchorBox(h, "page", PAGE_W - 600_000L, "page", 2_743_200L, 1_828_800L, 457_200L, "",
                pt("GEO59_PARTLY")))));
        body.append(p(r("Flat: ") + box(inlineBox(h, 1_828_800L, 0L, "", pt("GEO59_FLAT")))));
        body.append(p(r("Paragraph-relative, below wherever the paragraph falls: ")
                + box(anchorBox(h, "column", 0L, "paragraph", 9_000_000L, 1_828_800L, 457_200L, "", pt("GEO59_INDETERMINATE")))));
        body.append(p(r("Aligned to the page: ") + box(alignedBox(h, pt("GEO59_ALIGNED")))));
        body.append(p(r("Hidden: ") + box(inlineBox(h, 1_828_800L, 457_200L, " hidden='1'", pt("GEO59_HIDDEN")))));
        b.box("GEO59_OFF_PAGE", "page-relative, wholly right of the page; inspection: shape.off_page");
        b.box("GEO59_PARTLY", "page-relative, partly on the page: no inspection finding");
        b.box("GEO59_FLAT", "height zero; inspection: shape.zero_size");
        b.box("GEO59_INDETERMINATE", "9000000 EMU below its paragraph: on the page or not as the paragraph falls; no finding, undecided");
        b.box("GEO59_ALIGNED", "wp:align right and top, page-relative: on the page by construction, no finding");
        b.box("GEO59_HIDDEN", "wp:docPr hidden on an inline box: Word 365 shows it (19 October 2026), so no inspection finding");
        b.never("HDR59_UNUSED", "a header no section uses; inspection: story.unused");
        b.never("HDR59_BOX", "a page-relative box in the unused header: no section, no page size, undecided");
    }

    static String box(String drawing) {
        return "<w:r>" + ac("", choice("wps", drawing)) + "</w:r>";
    }

    static String wsp(String paragraphs, long cx, long cy) {
        return "<wps:wsp><wps:cNvSpPr txBox='1'/><wps:spPr><a:xfrm><a:off x='0' y='0'/><a:ext cx='" + cx + "' cy='" + cy
                + "'/></a:xfrm><a:prstGeom prst='rect'><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val='FFFFFF'/></a:solidFill>"
                + "<a:ln w='6350'><a:solidFill><a:srgbClr val='000000'/></a:solidFill></a:ln></wps:spPr>"
                + "<wps:txbx><w:txbxContent>" + paragraphs + "</w:txbxContent></wps:txbx>"
                + "<wps:bodyPr rot='0' vert='horz' wrap='square' lIns='91440' tIns='45720' rIns='91440' bIns='45720' anchor='t' "
                + "anchorCtr='0'><a:noAutofit/></wps:bodyPr></wps:wsp>";
    }

    static String graphic(String paragraphs, long cx, long cy) {
        return "<wp:cNvGraphicFramePr/><a:graphic><a:graphicData uri='" + WPS_URI + "'>" + wsp(paragraphs, cx, cy)
                + "</a:graphicData></a:graphic>";
    }

    static String anchorBox(ExtractionProbeBuilder h, String fromH, long x, String fromV, long y, long cx, long cy, String docPr,
            String paragraphs) {
        return anchor(h, "<wp:positionH relativeFrom='" + fromH + "'><wp:posOffset>" + x + "</wp:posOffset></wp:positionH>"
                + "<wp:positionV relativeFrom='" + fromV + "'><wp:posOffset>" + y + "</wp:posOffset></wp:positionV>", cx, cy, docPr, paragraphs);
    }

    static String alignedBox(ExtractionProbeBuilder h, String paragraphs) {
        return anchor(h, "<wp:positionH relativeFrom='page'><wp:align>right</wp:align></wp:positionH>"
                + "<wp:positionV relativeFrom='page'><wp:align>top</wp:align></wp:positionV>", 1_828_800L, 457_200L, "", paragraphs);
    }

    static String anchor(ExtractionProbeBuilder h, String position, long cx, long cy, String docPr, String paragraphs) {
        int id = h.docPrId++;
        return "<w:drawing><wp:anchor distT='0' distB='0' distL='114300' distR='114300' simplePos='0' relativeHeight='"
                + (251658240 + id) + "' behindDoc='0' locked='0' layoutInCell='1' allowOverlap='1'><wp:simplePos x='0' y='0'/>"
                + position + "<wp:extent cx='" + cx + "' cy='" + cy + "'/><wp:effectExtent l='0' t='0' r='0' b='0'/><wp:wrapNone/>"
                + "<wp:docPr id='" + id + "' name='Text Box " + id + "'" + docPr + "/>" + graphic(paragraphs, cx, cy)
                + "</wp:anchor></w:drawing>";
    }

    static String inlineBox(ExtractionProbeBuilder h, long cx, long cy, String docPr, String paragraphs) {
        int id = h.docPrId++;
        return "<w:drawing><wp:inline distT='0' distB='0' distL='0' distR='0'><wp:extent cx='" + cx + "' cy='" + cy + "'/>"
                + "<wp:effectExtent l='0' t='0' r='0' b='0'/><wp:docPr id='" + id + "' name='Text Box " + id + "'" + docPr + "/>"
                + graphic(paragraphs, cx, cy) + "</wp:inline></w:drawing>";
    }

    // ------------------------------------------------------------------ 60: characters

    static void characters(ExtractionProbeBuilder b, StringBuilder body) {
        b.begin(60, "characters: supplementary private use, a bidi override split across runs, symbol and icon fonts");
        body.append(caseHeading(60, b.current.title));
        body.append(p(r("PUA60_SUPPLEMENTARY \udb80\udc41")));
        body.append(p(r("<w:b/>", "BIDI60_SPLIT \u202e") + r("\u202c BIDI60_AFTER")));
        body.append(p(r("SYM60_SYMBOL_FONTS ") + "<w:r><w:sym w:font='Symbol' w:char='F0E5'/></w:r>" + r(" ")
                + "<w:r><w:sym w:font='Wingdings' w:char='F04A'/></w:r>" + r(" ")
                + r("<w:rFonts w:ascii='Wingdings' w:hAnsi='Wingdings'/>", "\uf04a")));
        body.append(p(r("SYM60_TEXT_FONT ") + "<w:r><w:sym w:font='Arial' w:char='F041'/></w:r>"));
        body.append(p(r("ICN60_ICON_FONT ") + r("<w:rFonts w:ascii='Segoe MDL2 Assets' w:hAnsi='Segoe MDL2 Assets'/>", "\ue700")));
        b.always("PUA60_SUPPLEMENTARY", "followed by U+F0041 (plane 15) in a text font; inspection: chars.private_use");
        b.always("BIDI60_SPLIT", "U+202E ends this bold run, U+202C starts the next; inspection: chars.bidi, one finding over both runs");
        b.always("BIDI60_AFTER", "");
        b.always("SYM60_SYMBOL_FONTS", "w:sym in Symbol and Wingdings, and U+F04A in a Wingdings run: no inspection finding");
        b.always("SYM60_TEXT_FONT", "w:sym U+F041 in Arial, a text font; inspection: chars.private_use");
        b.always("ICN60_ICON_FONT", "U+E700 in Segoe MDL2 Assets, an icon font (D17): no inspection finding");
    }

    // ------------------------------------------------------------------ 61: fields

    static void fields(ExtractionProbeBuilder b, StringBuilder body) {
        b.begin(61, "fields: INCLUDEPICTURE and LINK in both forms, and HYPERLINK to a bookmark, a file and a UNC path");
        body.append(caseHeading(61, b.current.title));
        body.append(p(simple(" includePicture &quot;file:///C:/CR050-MISSING/FLD61_INCPIC_SIMPLE.png&quot; \\d ", "FLD61_SIMPLE_RESULT")
                + r(" ") + field(" IncludePicture file:///C:/CR050-MISSING/FLD61_INCPIC_COMPLEX.png ", "FLD61_COMPLEX_RESULT", "")));
        body.append(p(simple(" link Word.Document.12 FLD61_LINK_BARE.docx ", "FLD61_LINK_SIMPLE_RESULT") + r(" ")
                + field(" LINK Excel.Sheet.12 &quot;C:\\\\CR050-MISSING\\\\FLD61_LINK_QUOTED.xlsx&quot; &quot;Sheet1!R1C1&quot; ",
                        "FLD61_LINK_COMPLEX_RESULT", "")));
        body.append(p("<w:bookmarkStart w:id='61' w:name='fld61target'/>" + r("FLD61_TARGET") + "<w:bookmarkEnd w:id='61'/>" + r(" ")
                + field(" HYPERLINK \\l &quot;fld61target&quot; ", "FLD61_BOOKMARK_RESULT", "") + r(" ")
                + field(" HYPERLINK &quot;file:///C:/CR050-MISSING/FLD61_FILE.docx&quot; ", "FLD61_FILE_RESULT", "") + r(" ")
                + field(" HYPERLINK &quot;\\\\\\\\cr050-missing\\\\share\\\\FLD61_UNC.docx&quot; ", "FLD61_UNC_RESULT", "")));
        b.gated("FLD61_INCPIC_SIMPLE", "field_codes: true", "a simple field, keyword in mixed case, argument quoted; inspection: field.code, field.external");
        b.always("FLD61_SIMPLE_RESULT", "its cached result");
        b.gated("FLD61_INCPIC_COMPLEX", "field_codes: true", "a complex field, argument bare; inspection: field.code, field.external");
        b.always("FLD61_COMPLEX_RESULT", "");
        b.gated("FLD61_LINK_BARE", "field_codes: true", "LINK, simple, keyword lower case, a bare relative path; inspection: field.code, field.external");
        b.always("FLD61_LINK_SIMPLE_RESULT", "");
        b.gated("FLD61_LINK_QUOTED", "field_codes: true", "LINK, complex, a quoted path; inspection: field.code, field.external");
        b.always("FLD61_LINK_COMPLEX_RESULT", "");
        b.always("FLD61_TARGET", "the bookmark the \\l link goes to");
        b.always("FLD61_BOOKMARK_RESULT", "HYPERLINK \\l: inspection: field.code only, neither external nor web");
        b.token("FLD61_FILE", "0", "0", "0", "field_codes: true", "1", "HYPERLINK to a file: inspection: field.code, field.external; "
                + "Markdown also carries the link target");
        b.always("FLD61_FILE_RESULT", "");
        b.token("FLD61_UNC", "0", "0", "0", "field_codes: true", "1", "HYPERLINK to a UNC path: inspection: field.code, field.external; "
                + "Markdown also carries the link target");
        b.always("FLD61_UNC_RESULT", "");
    }

    static String simple(String instr, String result) {
        return "<w:fldSimple w:instr='" + instr + "'>" + r(result) + "</w:fldSimple>";
    }

    // ------------------------------------------------------------------ 62: background

    static void background(ExtractionProbeBuilder b, StringBuilder body) {
        b.begin(62, "backgrounds: highlight, shading, auto, a table style, the page colour, a pattern");
        body.append(caseHeading(62, b.current.title));
        body.append(p(r("<w:color w:val='FFFF00'/><w:highlight w:val='yellow'/>", "BG62_HIGHLIGHT") + r(" ")
                + r("<w:color w:val='FFFFFF'/><w:highlight w:val='white'/><w:shd w:val='clear' w:color='auto' w:fill='000000'/>",
                        "BG62_HIGHLIGHT_OVER_SHADING") + r(" ")
                + r("<w:color w:val='FFFFFF'/><w:shd w:val='clear' w:color='auto' w:fill='000000'/>", "BG62_SHADED_CONTRAST") + r(" ")
                + r("<w:color w:val='auto'/><w:shd w:val='clear' w:color='auto' w:fill='000000'/>", "BG62_AUTO_COLOUR")));
        body.append(p("<w:pPr><w:shd w:val='clear' w:color='auto' w:fill='C00000'/></w:pPr>"
                + r("<w:color w:val='C00000'/><w:shd w:val='clear' w:color='auto' w:fill='auto'/>", "BG62_AUTO_FILL")));
        body.append("<w:tbl><w:tblPr><w:tblStyle w:val='ProbeShaded'/><w:tblW w:w='0' w:type='auto'/><w:tblLook w:val='04A0' "
                + "w:firstRow='0' w:lastRow='0' w:firstColumn='0' w:lastColumn='0' w:noHBand='1' w:noVBand='1'/></w:tblPr>"
                + "<w:tblGrid><w:gridCol w:w='4508'/></w:tblGrid><w:tr><w:tc><w:tcPr><w:tcW w:w='4508' w:type='dxa'/></w:tcPr>"
                + p(r("<w:color w:val='1F3864'/>", "BG62_TABLE_STYLE")) + "</w:tc></w:tr></w:tbl>");
        body.append(p(r("<w:color w:val='" + PAGE_COLOUR + "'/>", "BG62_PAGE") + r(" ")
                + r("<w:color w:val='C8C8C8'/>", "BG62_PAGE_NOT_WHITE")));
        body.append(p(r("<w:color w:val='FFFFFF'/><w:shd w:val='pct50' w:color='000000' w:fill='FFFFFF'/>", "BG62_PATTERN")));
        b.always("BG62_HIGHLIGHT", "yellow on its yellow highlight; inspection: text.near_background");
        b.always("BG62_HIGHLIGHT_OVER_SHADING", "white on a white highlight over black shading (the highlight is drawn); inspection: text.near_background");
        b.always("BG62_SHADED_CONTRAST", "white on black shading: no inspection finding");
        b.always("BG62_AUTO_COLOUR", "an auto colour on black shading (Word draws it in contrast): no inspection finding");
        b.always("BG62_AUTO_FILL", "C00000 on a run fill of auto over its paragraph's C00000; inspection: text.near_background");
        b.always("BG62_TABLE_STYLE", "1F3864 on its table style's 1F3864 cell shading; inspection: text.near_background");
        b.always("BG62_PAGE", "the page colour on the page colour (Word 365 shows the page colour); inspection: text.near_background");
        b.always("BG62_PAGE_NOT_WHITE", "C8C8C8: 1.67:1 on white, 1.44:1 on the page colour; inspection: text.near_background");
        b.always("BG62_PATTERN", "white on a 50% pattern: a background docx4j cannot determine; no finding, undecided");
    }

    // ------------------------------------------------------------------ 63: inventory

    /** Returns the footnotes' XML (after the separators) */
    static String inventory(ExtractionProbeBuilder b, ExtractionProbeBuilder h, WordprocessingMLPackage pkg, MainDocumentPart mdp,
            StringBuilder body) throws Exception {
        b.begin(63, "inventory: an unknown part, notes, comments, docVars, people, ink, a watermark, a scripted chunk, "
                + "an embedded document holding hidden text");
        body.append(caseHeading(63, b.current.title));

        BinaryPart unknown = new BinaryPart(new PartName("/word/probe63.xml"));
        unknown.setContentType(new ContentType("application/vnd.docx4j.inspect-probe+xml"));
        unknown.setRelationshipType(UNKNOWN_REL);
        unknown.setBinaryData(utf8("<probe xmlns='urn:docx4j:inspect-probe'>a part of a kind no reader knows</probe>"));
        mdp.addTargetPart(unknown);

        PeoplePart people = new PeoplePart();
        people.setJaxbElement(new org.docx4j.w15.CTPeople());
        mdp.addTargetPart(people);
        h.raw.put(people.getPartName().getName(), "<w15:people xmlns:mc='http://schemas.openxmlformats.org/markup-compatibility/2006' "
                + "xmlns:w15='http://schemas.microsoft.com/office/word/2012/wordml' mc:Ignorable='w15'>"
                + "<w15:person w15:author='Probe'><w15:presenceInfo w15:providerId='None' w15:userId='Probe'/></w15:person>"
                + "</w15:people>");

        BinaryPart ink = new BinaryPart(new PartName("/word/ink/ink1.xml"));
        ink.setContentType(new ContentType("application/inkml+xml"));
        ink.setRelationshipType(Namespaces.CUSTOM_XML_DATA_STORAGE);
        ink.setBinaryData(utf8(INK));
        String inkId = mdp.addTargetPart(ink).getId();
        String inkPreview = mdp.addTargetPart(BinaryPartAbstractImage.createImagePart(pkg, png(96, 48, 0xffffff))).getId();

        AlternativeFormatInputPart chunk = new AlternativeFormatInputPart(new PartName("/word/chunk63.htm"));
        chunk.setContentType(new ContentType("text/html"));
        chunk.setBinaryData(CHUNK63.getBytes(StandardCharsets.UTF_8));
        String chunkId = mdp.addTargetPart(chunk).getId();

        String preview = mdp.addTargetPart(BinaryPartAbstractImage.createImagePart(pkg, png(64, 32, 0x669977))).getId();
        byte[] inner = document(pt("EMB63_VISIBLE") + p(r("<w:vanish/>", "EMB63_HIDDEN")), Map.of(), "", "");
        String embedded = part(mdp, "/word/embeddings/probe63.docx", DOCX, "package", inner);

        body.append(p(r("NOTE63_HERE") + noteRef("footnote", 1)));
        body.append(p("<w:commentRangeStart w:id='0'/>" + r("CMT63_HERE") + "<w:commentRangeEnd w:id='0'/>" + commentRef(0)));
        body.append(p(r("INK63_HERE ") + "<w:r>" + ac("", choice("wpi", ink(h, inkId)) + fallback(inkPicture(h, inkPreview))) + "</w:r>"));
        body.append("<w:altChunk r:id='" + chunkId + "'/>");
        body.append(pt("After the chunk."));
        body.append(p(r("EMB63_HERE ") + object(h, embedded, preview, "Word.Document.12", false)));
        b.always("NOTE63_HERE", "");
        b.noteText("NOTE63_REFERENCED", "a footnote; inspection: note");
        b.never("NOTE63_UNREFERENCED", "a footnote no reference reaches; inspection: story.unused");
        b.always("CMT63_HERE", "");
        b.token("CMT63_ANCHORED", "0", "0", "cmt", "comments: true", "cmt", "inspection: comment");
        b.never("CMT63_UNANCHORED", "a comment with no anchor; inspection: story.unused");
        b.always("INK63_HERE", "an ink part (w14:contentPart) after it; inspection: ink");
        b.token("CHK63_SCRIPTED", "1", "1", "1", "altchunks: false", "0",
                "an HTML chunk holding a script, an event handler and two external references; inspection: altchunk, "
                + "altchunk.script, altchunk.external_ref; nothing fetched");
        b.always("EMB63_HERE", "");
        b.gated("EMB63_VISIBLE", "embedded: true", "the embedded document's shown text");
        b.token("EMB63_HIDDEN", "0", "0", "0", "embedded: true; hidden: true", "1",
                "hidden text inside the embedded document; inspection: text.hidden in its chain");
        b.never("VAR63_VALUE", "w:docVars; inspection: doc_vars");
        b.gated("HDR63_USED", "include: headers", "the header the section uses; inspection: header_footer");
        b.gated("WMK63_WATERMARK", "include: headers", "its watermark (PowerPlusWaterMarkObject, v:textpath); inspection: watermark");
        return note("footnote", 1, r("NOTE63_REFERENCED")) + note("footnote", 2, r("NOTE63_UNREFERENCED"));
    }

    /** A text watermark as Word writes one: WordArt (spt 136) named PowerPlusWaterMarkObject, behind the text */
    static String watermark(ExtractionProbeBuilder h, String text) {
        int id = h.shapeId++;
        return "<w:p><w:r><w:rPr><w:noProof/></w:rPr><w:pict><v:shapetype id='_x0000_t136' coordsize='21600,21600' o:spt='136' "
                + "adj='10800' path='m@7,l@8,m@5,21600l@6,21600e'><v:formulas><v:f eqn='sum #0 0 10800'/><v:f eqn='prod #0 2 1'/>"
                + "<v:f eqn='sum 21600 0 @1'/><v:f eqn='sum 0 0 @2'/><v:f eqn='sum 21600 0 @3'/><v:f eqn='if @0 @3 0'/>"
                + "<v:f eqn='if @0 21600 @1'/><v:f eqn='if @0 0 @2'/><v:f eqn='if @0 @4 21600'/><v:f eqn='mid @5 @6'/>"
                + "<v:f eqn='mid @8 @5'/><v:f eqn='mid @7 @8'/><v:f eqn='mid @6 @7'/><v:f eqn='sum @6 0 @5'/></v:formulas>"
                + "<v:path textpathok='t' o:connecttype='custom' o:connectlocs='@9,0;@10,10800;@11,21600;@12,10800' "
                + "o:connectangles='270,180,90,0'/><v:textpath on='t' fitshape='t'/><v:handles><v:h position='#0,bottomRight' "
                + "xrange='6629,14971'/></v:handles><o:lock v:ext='edit' text='t' shapetype='t'/></v:shapetype>"
                + "<v:shape id='PowerPlusWaterMarkObject" + id + "' o:spid='_x0000_s" + id + "' type='#_x0000_t136' "
                + "style='position:absolute;margin-left:0;margin-top:0;width:412pt;height:103pt;rotation:315;z-index:-251657216;"
                + "mso-position-horizontal:center;mso-position-horizontal-relative:margin;mso-position-vertical:center;"
                + "mso-position-vertical-relative:margin' o:allowincell='f' fillcolor='silver' stroked='f'><v:fill opacity='.5'/>"
                + "<v:textpath style='font-family:&quot;Calibri&quot;;font-size:1pt' string='" + text + "'/>"
                + "<w10:wrap anchorx='margin' anchory='margin'/></v:shape></w:pict></w:r></w:p>";
    }

    static String ink(ExtractionProbeBuilder h, String inkId) {
        int id = h.docPrId++;
        return "<w:drawing><wp:inline distT='0' distB='0' distL='0' distR='0'><wp:extent cx='914400' cy='457200'/>"
                + "<wp:effectExtent l='0' t='0' r='0' b='0'/><wp:docPr id='" + id + "' name='Ink " + id + "'/><wp:cNvGraphicFramePr/>"
                + "<a:graphic><a:graphicData uri='" + WPI_URI + "'><w14:contentPart bwMode='auto' r:id='" + inkId + "'>"
                + "<w14:nvContentPartPr><w14:cNvContentPartPr/></w14:nvContentPartPr><w14:xfrm><a:off x='0' y='0'/>"
                + "<a:ext cx='914400' cy='457200'/></w14:xfrm></w14:contentPart></a:graphicData></a:graphic></wp:inline></w:drawing>";
    }

    /** The ink's picture, for an older reader: a w:drawing, as the Fallback is inside the run */
    static String inkPicture(ExtractionProbeBuilder h, String image) {
        String run = picture(h, image, "Ink " + h.docPrId, "", "");
        return run.substring("<w:r>".length(), run.length() - "</w:r>".length());
    }

    static final String INK = "<inkml:ink xmlns:inkml='http://www.w3.org/2003/InkML'><inkml:definitions>"
            + "<inkml:context xml:id='ctx0'><inkml:inkSource xml:id='inkSrc0'><inkml:traceFormat>"
            + "<inkml:channel name='X' type='integer' max='32767' units='cm'/><inkml:channel name='Y' type='integer' max='32767' units='cm'/>"
            + "</inkml:traceFormat><inkml:channelProperties>"
            + "<inkml:channelProperty channel='X' name='resolution' value='1000' units='1/cm'/>"
            + "<inkml:channelProperty channel='Y' name='resolution' value='1000' units='1/cm'/>"
            + "</inkml:channelProperties></inkml:inkSource><inkml:timestamp xml:id='ts0' timeString='2026-10-17T00:00:00.000'/>"
            + "</inkml:context><inkml:brush xml:id='br0'><inkml:brushProperty name='width' value='0.05' units='cm'/>"
            + "<inkml:brushProperty name='height' value='0.05' units='cm'/></inkml:brush></inkml:definitions>"
            + "<inkml:trace contextRef='#ctx0' brushRef='#br0'>0 0, 400 300, 800 0, 1200 300, 1600 0</inkml:trace></inkml:ink>";

    /**
     * An HTML chunk with a script (a comment, no code), an event handler that does nothing, and an image and a
     * stylesheet on a host under .invalid, which never resolves (RFC 6761)
     */
    static final String CHUNK63 = "<html><head><meta charset=\"utf-8\"><title>chunk 63</title>"
            + "<link rel=\"stylesheet\" href=\"http://cr050-missing.invalid/probe63.css\">"
            + "<script>/* CR-050 inspection probe: a script element with no code */</script></head>"
            + "<body onload=\"void 0\"><p>CHK63_SCRIPTED</p><p><img src=\"http://cr050-missing.invalid/probe63.png\" alt=\"\"></p>"
            + "</body></html>";

    // ------------------------------------------------------------------ 64: links

    static void links(ExtractionProbeBuilder b, MainDocumentPart mdp, StringBuilder body) {
        b.begin(64, "links: w:hyperlink to a file, a UNC path, the web, mail, and a bookmark");
        body.append(caseHeading(64, b.current.title));
        body.append(p(hyperlink(mdp, "file:///C:/CR050-MISSING/LNK64_FILE.docx", "LNK64_FILE_TEXT") + r(" ")
                + hyperlink(mdp, "file:///\\\\cr050-missing\\share\\LNK64_UNC.docx", "LNK64_UNC_TEXT") + r(" ")
                + hyperlink(mdp, "https://example.com/LNK64_WEB", "LNK64_WEB_TEXT") + r(" ")
                + hyperlink(mdp, "mailto:probe@example.com?subject=LNK64_MAIL", "LNK64_MAIL_TEXT") + r(" ")
                + "<w:hyperlink w:anchor='fld61target' w:history='1'>" + r("<w:rStyle w:val='Hyperlink'/>", "LNK64_BOOKMARK_TEXT")
                + "</w:hyperlink>"));
        b.always("LNK64_FILE_TEXT", "a w:hyperlink to a file; inspection: relationship.external");
        b.always("LNK64_UNC_TEXT", "a w:hyperlink to a UNC path; inspection: relationship.external");
        b.always("LNK64_WEB_TEXT", "https; inspection: hyperlink.web");
        b.always("LNK64_MAIL_TEXT", "mailto; inspection: hyperlink.web");
        b.always("LNK64_BOOKMARK_TEXT", "w:anchor: no inspection finding");
    }

    static String hyperlink(MainDocumentPart mdp, String target, String text) {
        Relationship rel = new org.docx4j.relationships.ObjectFactory().createRelationship();
        rel.setType(Namespaces.HYPERLINK);
        rel.setTarget(target);
        rel.setTargetMode("External");
        mdp.getRelationshipsPart().addRelationship(rel);
        return "<w:hyperlink r:id='" + rel.getId() + "' w:history='1'>" + r("<w:rStyle w:val='Hyperlink'/>", text) + "</w:hyperlink>";
    }

    // ------------------------------------------------------------------ 65: fonts

    static void fonts(ExtractionProbeBuilder b, WordprocessingMLPackage pkg, StringBuilder body) throws Exception {
        b.begin(65, "embedded fonts that draw text as other text or as nothing, a careful swap, symbol and plain faces");
        body.append(caseHeading(65, b.current.title));
        for (String[] face : FACES) {
            FontEmbedder.embed(pkg, Files.readAllBytes(FONTS.resolve(face[1])), face[0], FontEmbedder.Style.REGULAR,
                    FontEmbedder.EmbedPolicy.WHOLE);
            body.append(p(r(face[3] + " ") + r(fonts(face[0]), face[2])));
        }
        String sub = FACES[FACES.length - 1][0];
        body.append(p(r("FNT65_LIGA ") + r(fonts(sub) + "<w14:ligatures w14:val='standard'/>", "DOT")));
        body.append(p(r("FNT65_NO_LIGA ") + r(fonts(sub), "ODD")));
        body.append(p(r("FNT65_PUA_PLAIN ") + r(fonts("Inspect Probe Plain"), "\ue123")));
        b.always("FNT65_WORD", "CAT drawn DOG: C an over-wide glyph drawing DOG, A and T blank; inspection: font.glyph_mismatch, font.glyph_blank");
        b.always("FNT65_TWIN", "as FNT65_WORD, in a face named like the unaltered one support/inspect-fonts gives to install");
        b.always("FNT65_BLANK", "INVISIBLE INK, every letter blank; inspection: font.glyph_blank");
        b.always("FNT65_ZERO", "MIXED, X at zero advance with an outline; inspection: font.glyph_mismatch");
        b.always("FNT65_NAMED", "MAT drawn AMT, the glyphs' names kept; inspection: font.glyph_mismatch");
        b.always("FNT65_SHARED", "FEE drawn FFF, E sharing F's glyph; inspection: font.glyph_mismatch");
        b.always("FNT65_CAREFUL", "SIZE drawn ZISE, a careful swap: font.embedded only, a limitation");
        b.always("FNT65_PLAIN", "PLAIN, the glyphs as made: font.embedded only");
        b.always("FNT65_SYMBOL", "U+F041 U+F061 U+F06B in an embedded symbol-encoded face: no chars.private_use");
        b.always("FNT65_CCMP", "BOB drawn POP through ccmp; inspection: font.glyph_mismatch (Word's use of ccmp is the check's to measure)");
        b.always("FNT65_LIGA", "DOT drawn OOT through liga, w14:ligatures standard; inspection: font.glyph_mismatch");
        b.always("FNT65_NO_LIGA", "ODD, liga not applied (no w14:ligatures): no inspection finding");
        b.always("FNT65_PUA_PLAIN", "U+E123 in a face of neither kind, which does not map it; inspection: chars.private_use");
    }

    static String fonts(String name) {
        return "<w:rFonts w:ascii='" + name + "' w:hAnsi='" + name + "' w:cs='" + name + "'/>";
    }

    // ------------------------------------------------------------------ 66: pictures and an embedded workbook

    static void pictures(ExtractionProbeBuilder b, ExtractionProbeBuilder h, WordprocessingMLPackage pkg, MainDocumentPart mdp,
            StringBuilder body) throws Exception {
        b.begin(66, "a picture named and not described, a decorative one, a workbook with a hidden row and column");
        body.append(caseHeading(66, b.current.title));
        String a = mdp.addTargetPart(BinaryPartAbstractImage.createImagePart(pkg, png(96, 48, 0x3377aa))).getId();
        String d = mdp.addTargetPart(BinaryPartAbstractImage.createImagePart(pkg, png(96, 48, 0xaa7733))).getId();
        body.append(p(r("Named: ") + picture(h, a, "PIC66_NAME_ONLY", "", "")));
        body.append(p(r("Decorative: ") + picture(h, d, "PIC66_DECOR_NAME", " descr='PIC66_DECOR_DESCR'",
                "<a:extLst><a:ext uri='{C183D7F6-B498-43B3-948B-1728B52AA6E4}'>"
                + "<adec:decorative xmlns:adec='http://schemas.microsoft.com/office/drawing/2017/decorative' val='1'/>"
                + "</a:ext></a:extLst>")));
        String preview = mdp.addTargetPart(BinaryPartAbstractImage.createImagePart(pkg, png(64, 32, 0x77aa66))).getId();
        String workbook = part(mdp, "/word/embeddings/probe66.xlsx", XLSX, "package", workbook66());
        body.append(p(object(h, workbook, preview, "Excel.Sheet.12", false)));
        b.gated("PIC66_NAME_ONLY", "alt_text: true", "a picture's name, no descr: its description (AltTextDescriber); inspection: alt_text.name");
        b.never("PIC66_DECOR_NAME", "a decorative picture's name; inspection: alt_text.name, unextractable");
        b.never("PIC66_DECOR_DESCR", "a decorative picture's descr; inspection: alt_text.description, unextractable");
        b.gated("ROW66_SHOWN", "embedded: true", "A1");
        b.gated("COL66_HIDDEN_CELL", "embedded: true", "B1, in hidden column B: read (Embedded.sheet checks the sheet's state only); "
                + "inspection: embedded.column_hidden");
        b.gated("ROW66_HIDDEN", "embedded: true", "A2, in hidden row 2: read; inspection: embedded.row_hidden");
    }

    static String picture(ExtractionProbeBuilder h, String image, String name, String docPrAttributes, String docPrChildren) {
        int id = h.docPrId++;
        return "<w:r><w:drawing><wp:inline distT='0' distB='0' distL='0' distR='0'><wp:extent cx='914400' cy='457200'/>"
                + "<wp:effectExtent l='0' t='0' r='0' b='0'/><wp:docPr id='" + id + "' name='" + name + "'" + docPrAttributes
                + (docPrChildren.isEmpty() ? "/>" : ">" + docPrChildren + "</wp:docPr>")
                + "<wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect='1'/></wp:cNvGraphicFramePr>"
                + "<a:graphic><a:graphicData uri='http://schemas.openxmlformats.org/drawingml/2006/picture'><pic:pic><pic:nvPicPr>"
                + "<pic:cNvPr id='0' name='" + name + "'/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed='" + image + "'/>"
                + "<a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x='0' y='0'/>"
                + "<a:ext cx='914400' cy='457200'/></a:xfrm><a:prstGeom prst='rect'><a:avLst/></a:prstGeom></pic:spPr></pic:pic>"
                + "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r>";
    }

    /** One sheet: A1 and B1 (column B hidden), A2 (row 2 hidden) */
    static byte[] workbook66() throws Exception {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("[Content_Types].xml", utf8(types(override("/xl/workbook.xml", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml")
                + override("/xl/worksheets/sheet1.xml", "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"))));
        entries.put("_rels/.rels", utf8(rels(relationship("rId1", "officeDocument", "xl/workbook.xml", false))));
        entries.put("xl/workbook.xml", utf8("<workbook xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main' xmlns:r='"
                + REL.substring(0, REL.length() - 1) + "'><sheets><sheet name='Probe' sheetId='1' r:id='rId1'/></sheets></workbook>"));
        entries.put("xl/_rels/workbook.xml.rels", utf8(rels(relationship("rId1", "worksheet", "worksheets/sheet1.xml", false))));
        entries.put("xl/worksheets/sheet1.xml", utf8("<worksheet xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main'>"
                + "<cols><col min='2' max='2' width='9.140625' hidden='1' customWidth='1'/></cols><sheetData>"
                + "<row r='1'><c r='A1' t='inlineStr'><is><t>ROW66_SHOWN</t></is></c><c r='B1' t='inlineStr'><is><t>COL66_HIDDEN_CELL</t></is></c></row>"
                + "<row r='2' hidden='1'><c r='A2' t='inlineStr'><is><t>ROW66_HIDDEN</t></is></c></row>"
                + "</sheetData></worksheet>"));
        return zip(entries);
    }

    // ------------------------------------------------------------------ 67: stored content

    static void stored(ExtractionProbeBuilder b, MainDocumentPart mdp, StringBuilder body) throws Exception {
        b.begin(67, "stored content: a Fallback holding near-background text, the stale surface of a bound control holding tag characters");
        body.append(caseHeading(67, b.current.title));
        CustomXmlDataStoragePart part = new CustomXmlDataStoragePart();
        CustomXmlDataStorageImpl data = new CustomXmlDataStorageImpl();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        data.setDocument(dbf.newDocumentBuilder().parse(new ByteArrayInputStream(("<probe xmlns=\"" + ExtractionProbeBuilder.PROBE_NS
                + "\"><value>SDT67_VALUE</value></probe>").getBytes(StandardCharsets.UTF_8))));
        part.setData(data);
        mdp.addTargetPart(part, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
        CustomXmlDataStoragePropertiesPart props = new CustomXmlDataStoragePropertiesPart();
        DatastoreItem item = new org.docx4j.customXmlProperties.ObjectFactory().createDatastoreItem();
        item.setItemID(STORE_ITEM_67);
        props.setJaxbElement(item);
        part.addTargetPart(props);

        body.append(p(r("MCE67_HERE ") + ac("", choice("w14", r("MCE67_CHOICE "))
                + fallback(r("<w:color w:val='FFFFFF'/>", "MCE67_FALLBACK_WHITE ")))));
        body.append(p(r("SDT67_HERE ") + "<w:sdt><w:sdtPr><w:alias w:val='Probe bound 67'/><w:tag w:val='probe67'/><w:id w:val='767'/>"
                + "<w:dataBinding w:prefixMappings='xmlns:p=&apos;" + ExtractionProbeBuilder.PROBE_NS + "&apos;' "
                + "w:xpath='/p:probe[1]/p:value[1]' w:storeItemID='" + STORE_ITEM_67 + "'/><w:text/></w:sdtPr><w:sdtContent>"
                + r("SDT67_SURFACE\udb40\udc41\udb40\udc42") + "</w:sdtContent></w:sdt>"));
        b.always("MCE67_HERE", "");
        b.always("MCE67_CHOICE", "the selected Choice (w14)");
        b.never("MCE67_FALLBACK_WHITE", "white text in the unselected Fallback; inspection: mce.branch, and text.near_background in it");
        b.always("SDT67_HERE", "");
        b.token("SDT67_VALUE", "1", "1", "1", "bindings: surface", "0", "the bound value, read by default");
        b.gated("SDT67_SURFACE", "bindings: surface", "the stale surface, followed by U+E0041 U+E0042, which Word 365 replaces with "
                + "the bound value on open; inspection: sdt.stored_surface, and chars.tag in it");
    }

    // ------------------------------------------------------------------ the package

    static String caseHeading(int n, String title) {
        return p("<w:pPr><w:pStyle w:val='Heading2'/></w:pPr>" + r("Case " + n + ": " + title));
    }

    static final String SETTINGS = "<w:settings" + NS + "><w:zoom w:percent='100'/><w:displayBackgroundShape/><w:embedTrueTypeFonts/>"
            + "<w:defaultTabStop w:val='720'/><w:characterSpacingControl w:val='doNotCompress'/>"
            + "<w:footnotePr><w:footnote w:id='-1'/><w:footnote w:id='0'/></w:footnotePr>"
            + "<w:compat><w:compatSetting w:name='compatibilityMode' w:uri='http://schemas.microsoft.com/office/word' w:val='15'/>"
            + "<w:compatSetting w:name='enableOpenTypeFeatures' w:uri='http://schemas.microsoft.com/office/word' w:val='1'/></w:compat>"
            + "<w:docVars><w:docVar w:name='ProbeVar' w:val='VAR63_VALUE'/></w:docVars>"
            + "<w:themeFontLang w:val='en-US'/><w:decimalSymbol w:val='.'/><w:listSeparator w:val=','/></w:settings>";

    static final String STYLES = ExtractionProbeBuilder.stylesXml().replace("</w:styles>",
            heading("Heading1", "heading 1", 0, 32) + heading("Heading3", "heading 3", 2, 24) + heading("Heading4", "heading 4", 3, 22)
            + "<w:style w:type='table' w:styleId='ProbeShaded'><w:name w:val='Probe Shaded'/><w:basedOn w:val='TableNormal'/>"
            + "<w:tblPr><w:tblBorders><w:top w:val='single' w:sz='4' w:space='0' w:color='auto'/>"
            + "<w:bottom w:val='single' w:sz='4' w:space='0' w:color='auto'/></w:tblBorders></w:tblPr>"
            + "<w:tcPr><w:shd w:val='clear' w:color='auto' w:fill='1F3864'/></w:tcPr></w:style></w:styles>");

    static String heading(String id, String name, int level, int size) {
        return "<w:style w:type='paragraph' w:styleId='" + id + "'><w:name w:val='" + name + "'/><w:basedOn w:val='Normal'/>"
                + "<w:next w:val='Normal'/><w:qFormat/><w:pPr><w:keepNext/><w:outlineLvl w:val='" + level + "'/></w:pPr>"
                + "<w:rPr><w:b/><w:sz w:val='" + size + "'/><w:szCs w:val='" + size + "'/></w:rPr></w:style>";
    }

}
