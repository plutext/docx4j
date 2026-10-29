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

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.org.apache.poi.poifs.filesystem.*;

/** Structural checks independent of the extraction engine and Word's open/repair check. */
final class ExtractionFixtureChecks {
    static void check(byte[] main, byte[] external, byte[] structure, byte[] inspect, List<ExtractionProbeBuilder.Case> cases) throws Exception {
        if (cases.size() != 82) throw new IllegalStateException("Expected 82 cases");
        Map<String, byte[]> a = entries(main), b = entries(external), s = entries(structure), n = entries(inspect);
        validatePackage(a); validatePackage(b); validatePackage(s); validatePackage(n);
        String mainText = payloadText(a), externalText = payloadText(b), structureText = payloadText(s), inspectText = payloadText(n);
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < cases.size(); i++) {
            ExtractionProbeBuilder.Case c = cases.get(i);
            require(c.n == i + 1, "Case sequence");
            require(!ExtractionPhase0Cases.statements(c.n).isEmpty(), "Missing requirements");
            String fixture = ExtractionPhase0Cases.fixture(c.n);
            String payload = fixture.endsWith(".docm") ? externalText : fixture.equals(ExtractionStructureCases.FIXTURE) ? structureText
                    : fixture.equals(ExtractionInspectCases.FIXTURE) ? inspectText : mainText;
            for (ExtractionProbeBuilder.Token t : c.tokens) {
                require(tokens.add(c.n + ":" + t.name), "Duplicate expectation " + t.name);
                // Case 30's hyphens are elements separating the two token fragments.
                String marker = t.name.replace("\u2011", "").replace("\u00ad", "");
                String text = payload.replaceAll("<[^>]*>", "");
                require(payload.contains(marker) || text.contains(marker), "Marker missing from intended fixture: " + t.name);
            }
        }
        for (Map<String, byte[]> pkg : List.of(a, b, s, n)) {
            NodeList children = xml(pkg.get("word/document.xml")).getElementsByTagNameNS(
                    "http://schemas.openxmlformats.org/wordprocessingml/2006/main", "body").item(0).getChildNodes();
            for (int i = 0; i < children.getLength(); i++) if (children.item(i) instanceof Element)
                require(!children.item(i).getLocalName().equals("r"), "Bare run in document body");
        }
        String story = text(a, "word/document.xml");
        require(story.contains("ZW48_SP\u200bLIT"), "Lost zero-width space");
        require(story.contains("TAG48_MARK\udb40\udc41\udb40\udc42"), "Lost tag characters");
        require(story.contains("margin-left:-5000pt") && story.contains("width:0pt;height:0pt"), "Box geometry");
        require(story.contains("<w:specVanish"), "Style separator");
        require(!story.contains("EMB39_XLSX_CELL") && !story.contains("EMB40_DOCX_TEXT"), "Embedded token leaked to body");
        require(!story.contains("ACX42_CAPTION"), "Control token leaked to body");
        require(!story.contains("EMB53_") && !story.contains("GLS54_"), "New payload marker leaked to body");
        checkAdditionalCases(a);
        checkStructureCases(s);
        checkInspectCases(n);
        require(text(a, "word/activeX/activeX42.xml").contains("persistStorage"), "ActiveX persistence");
        ProbeActiveXControl.check(a.get("word/activeX/activeX42.bin"));
        require(text(a, "word/chunk46.xml").startsWith("<?xml version=\"1.0\""), "Flat OPC declaration");
        require(text(a, "word/chunk46.xml").contains("<?mso-application progid=\"Word.Document\"?>"), "Flat OPC Word identification");
        Map<String, byte[]> embedded = entries(a.get("word/embeddings/probe40.docx"));
        require(text(embedded, "word/document.xml").contains("EMB40_DOCX_TEXT"), "Outer DOCX marker");
        require(text(entries(embedded.get("word/embeddings/nested.docx")), "word/document.xml").contains("EMB40_NESTED_TEXT"), "Inner DOCX marker");
        for (String file : new String[] {"word/embeddings/probe39.xlsx", "word/embeddings/probe40.docx", "word/chunk47.docx"})
            OpcPackage.load(new ByteArrayInputStream(a.get(file)));
        require(text(b, "word/settings.xml").contains("<w:updateFields w:val=\"true\""), "Update-on-open metadata");
        ProbeVbaProject.check(b.get("word/vbaProject.bin"));
        require(!text(b, "word/document.xml").contains("VBA51_MODULE_MARK"), "VBA marker leaked to body");
    }

    /** Cases 55-57: the TOC field and its entries' bookmarks, the lists' numbering, the workbook's sheet states. */
    static void checkStructureCases(Map<String, byte[]> structure) throws Exception {
        String story = text(structure, "word/document.xml");
        require(story.contains("TOC \\o"), "TOC field code");
        for (String bookmark : new String[] {"_Toc55001", "_Toc55002"})
            require(story.contains("w:anchor=\"" + bookmark + "\"") && story.contains("w:name=\"" + bookmark + "\""), "TOC entry and its heading's bookmark " + bookmark);
        require(text(structure, "word/numbering.xml").contains("w:numId=\"2\""), "Second list");
        require(!story.contains("SHT57_"), "Workbook token leaked to body");
        String workbook = text(entries(structure.get("word/embeddings/structure57.xlsx")), "xl/workbook.xml");
        require(workbook.contains("state=\"hidden\"") && workbook.contains("state=\"veryHidden\""), "Hidden sheet states");
    }

    /** Cases 58-67: what the inspector's boundary cases rest on, which a rebuild could lose silently */
    static void checkInspectCases(Map<String, byte[]> inspect) throws Exception {
        String story = text(inspect, "word/document.xml");
        require(story.contains("<w:background w:color=\"" + ExtractionInspectCases.PAGE_COLOUR + "\""), "Page colour");
        require(story.contains("<w15:collapsed/>"), "Collapsed heading");
        require(story.contains("PUA60_SUPPLEMENTARY \udb80\udc41") && story.contains("\u202e</w:t>") && story.contains(">\u202c"), "Characters");
        require(story.contains("hidden=\"1\"") && story.contains("<wp:align>right</wp:align>"), "Shape geometry");
        require(story.contains("SDT67_SURFACE\udb40\udc41\udb40\udc42"), "Surface tag characters");
        require(!story.contains("HDR59_") && !story.contains("NOTE63_UNREFERENCED") && !story.contains("CMT63_UNANCHORED"), "Unused story leaked to body");
        require(!story.contains("EMB63_VISIBLE") && !story.contains("EMB63_HIDDEN"), "Embedded token leaked to body");
        require(text(inspect, "word/settings.xml").contains("<w:docVars>") && text(inspect, "word/settings.xml").contains("<w:embedTrueTypeFonts/>"),
                "docVars and embedTrueTypeFonts");
        require(text(inspect, "word/chunk63.htm").contains("<script>") && text(inspect, "word/chunk63.htm").contains(".invalid/"), "Scripted chunk");
        int fonts = 0;
        for (String name : inspect.keySet()) if (name.startsWith("word/fonts/") && name.endsWith(".odttf")) fonts++;
        require(fonts == ExtractionInspectCases.FACES.length, "Embedded fonts: " + fonts);
        String workbook = text(entries(inspect.get("word/embeddings/probe66.xlsx")), "xl/worksheets/sheet1.xml");
        require(workbook.contains("hidden=\"1\" customWidth") && workbook.contains("<row r=\"2\" hidden=\"1\">"), "Hidden row and column");
    }

    static void checkAdditionalCases(Map<String, byte[]> main) throws Exception {
        Map<String, byte[]> pptx = entries(main.get("word/embeddings/probe53.pptx"));
        OpcPackage loaded = OpcPackage.load(new ByteArrayInputStream(main.get("word/embeddings/probe53.pptx")));
        require(loaded instanceof org.docx4j.openpackaging.packages.PresentationMLPackage, "Embedded presentation type");
        String p = ExtractionAdditionalCases.P, a = ExtractionAdditionalCases.A;
        Document presentation = xml(pptx.get("ppt/presentation.xml"));
        NodeList slideIds = presentation.getElementsByTagNameNS(p, "sldId");
        require(slideIds.getLength() == 3, "Expected three presentation slides");
        Map<String, String> relationships = new HashMap<>();
        NodeList rels = xml(pptx.get("ppt/_rels/presentation.xml.rels")).getDocumentElement().getChildNodes();
        for (int i = 0; i < rels.getLength(); i++) if (rels.item(i) instanceof Element) {
            Element r = (Element) rels.item(i); relationships.put(r.getAttribute("Id"), r.getAttribute("Target"));
        }
        List<String> visible = new ArrayList<>();
        for (int i = 0; i < slideIds.getLength(); i++) {
            String id = ((Element) slideIds.item(i)).getAttributeNS(ExtractionPhase0Cases.REL.substring(0, ExtractionPhase0Cases.REL.length() - 1), "id");
            String target = URI.create("/ppt/presentation.xml").resolve(relationships.get(id)).normalize().getPath().substring(1);
            require(target.equals("ppt/slides/slide" + new int[] {9, 4, 1}[i] + ".xml"), "Slide order must differ from filename order");
            Document slide = xml(pptx.get(target));
            String show = slide.getDocumentElement().getAttribute("show");
            NodeList runs = slide.getElementsByTagNameNS(a, "t");
            if (i == 1) {
                require(show.equals("false") || show.equals("0"), "Expected hidden middle slide");
                require(slide.getDocumentElement().getTextContent().contains("EMB53_HIDDEN_SLIDE"), "Missing hidden-slide marker");
            } else for (int j = 0; j < runs.getLength(); j++) visible.add(runs.item(j).getTextContent());
            if (i == 0) {
                require(slide.getElementsByTagNameNS(p, "grpSp").getLength() == 2, "Nested group structure");
                require(slide.getElementsByTagNameNS(a, "tr").getLength() == 2
                        && slide.getElementsByTagNameNS(a, "tc").getLength() == 4, "Expected 2x2 slide table");
            }
        }
        require(visible.equals(Arrays.asList(ExtractionAdditionalCases.VISIBLE)), "Visible-slide shape/table order");
        require(text(pptx, "ppt/notesSlides/notesSlide1.xml").contains("EMB53_SPEAKER_NOTES"), "Missing speaker-note marker");
        Document glossary = xml(main.get("word/glossary/document.xml"));
        String w = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
        NodeList entries = glossary.getElementsByTagNameNS(w, "docPart");
        require(entries.getLength() == 2, "Expected two glossary entries");
        require(entries.item(0).getTextContent().equals("GLS54_ENTRY_ONE")
                && entries.item(1).getTextContent().equals("GLS54_ENTRY_TWO"), "Glossary entry order and payload");
    }

    static Map<String, byte[]> entries(byte[] zipBytes) throws Exception {
        require(zipBytes != null, "Missing package");
        Map<String, byte[]> out = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry e;
            while ((e = zip.getNextEntry()) != null) {
                require(!out.containsKey(e.getName()), "Duplicate ZIP member");
                require(e.getTimeLocal().equals(java.time.LocalDateTime.of(1980, 1, 1, 0, 0)), "ZIP timestamp " + e.getName());
                out.put(e.getName(), zip.readAllBytes());
            }
        }
        return out;
    }

    static Document xml(byte[] bytes) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
    }

    static void validatePackage(Map<String, byte[]> entries) throws Exception {
        require(entries.containsKey("[Content_Types].xml"), "Missing content types");
        Set<String> types = new HashSet<>(), defaults = new HashSet<>();
        NodeList overrides = xml(entries.get("[Content_Types].xml")).getDocumentElement().getChildNodes();
        for (int i = 0; i < overrides.getLength(); i++) if (overrides.item(i) instanceof Element) {
            Element e = (Element) overrides.item(i);
            if (e.getLocalName().equals("Override")) {
                require(entries.containsKey(e.getAttribute("PartName").substring(1)), "Dangling content-type override");
                types.add(e.getAttribute("PartName").substring(1));
            } else defaults.add(e.getAttribute("Extension"));
        }
        for (Map.Entry<String, byte[]> part : entries.entrySet()) {
            String name = part.getKey();
            if (!name.equals("[Content_Types].xml")) require(types.contains(name)
                    || defaults.contains(name.substring(name.lastIndexOf('.') + 1)), "Missing content type " + name);
            if (name.endsWith(".xml") || name.endsWith(".rels")) xml(part.getValue());
            if (name.endsWith(".docx") || name.endsWith(".xlsx") || name.endsWith(".pptx")) validatePackage(entries(part.getValue()));
            if (!name.endsWith(".rels")) continue;
            String owner = name.equals("_rels/.rels") ? "" : name.replace("/_rels/", "/").substring(0, name.replace("/_rels/", "/").length()-5);
            URI base = URI.create("/" + owner);
            Set<String> ids = new HashSet<>();
            NodeList relationships = xml(part.getValue()).getDocumentElement().getChildNodes();
            for (int i = 0; i < relationships.getLength(); i++) if (relationships.item(i) instanceof Element) {
                Element r = (Element) relationships.item(i);
                require(ids.add(r.getAttribute("Id")), "Duplicate relationship ID");
                if (r.getAttribute("TargetMode").equals("External")) continue;
                String target = base.resolve(r.getAttribute("Target")).normalize().getPath().substring(1);
                require(entries.containsKey(target), "Dangling relationship " + name + " -> " + target);
                if (r.getAttribute("Type").equals(ExtractionPhase0Cases.REL + "aFChunk") && target.endsWith(".docx")) {
                    // An AFI DOCX uses the main-part MIME type, unlike an embedded package.
                    require(ExtractionPhase0Cases.DOCX_CHUNK.equals(contentType(entries, target)),
                            "DOCX altChunk content type must be " + ExtractionPhase0Cases.DOCX_CHUNK + ": " + target);
                }
            }
        }
    }

    static String contentType(Map<String, byte[]> entries, String target) throws Exception {
        NodeList declarations = xml(entries.get("[Content_Types].xml")).getDocumentElement().getChildNodes();
        String fallback = null;
        for (int i = 0; i < declarations.getLength(); i++) if (declarations.item(i) instanceof Element) {
            Element e = (Element) declarations.item(i);
            if (e.getLocalName().equals("Override") && e.getAttribute("PartName").equals("/" + target))
                return e.getAttribute("ContentType");
            if (e.getLocalName().equals("Default") && target.endsWith("." + e.getAttribute("Extension")))
                fallback = e.getAttribute("ContentType");
        }
        return fallback;
    }

    static String payloadText(Map<String, byte[]> entries) throws Exception {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, byte[]> part : entries.entrySet()) {
            String name = part.getKey(); byte[] bytes = part.getValue();
            if (name.endsWith(".docx") || name.endsWith(".xlsx") || name.endsWith(".pptx")) out.append(payloadText(entries(bytes)));
            else if (name.endsWith(".wmf") || name.endsWith(".emf")) {
                // a metafile's text records, single-byte and UTF-16 (cases 72 and 73)
                out.append(new String(bytes, StandardCharsets.ISO_8859_1)).append('\n').append(new String(bytes, StandardCharsets.UTF_16LE)).append('\n');
            } else if (name.equals("word/embeddings/probe41.bin")) {
                try (POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(bytes))) {
                    out.append(new String(Ole10Native.createFromEmbeddedOleObject(fs).getDataBuffer(), StandardCharsets.UTF_8));
                }
            } else if (name.equals("word/activeX/activeX42.bin")) {
                try (POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(bytes));
                     InputStream in = fs.getRoot().createDocumentInputStream("contents")) {
                    byte[] contents = in.readAllBytes();
                    out.append(new String(contents, 20, contents[16] & 255, StandardCharsets.US_ASCII));
                }
            } else if (name.endsWith("vbaProject.bin")) {
                try (org.docx4j.org.apache.poi.poifs.macros.VBAMacroReader reader = new org.docx4j.org.apache.poi.poifs.macros.VBAMacroReader(new ByteArrayInputStream(bytes))) {
                    out.append(reader.readMacros().values());
                }
            } else out.append(new String(bytes, StandardCharsets.UTF_8));
            out.append('\n');
        }
        return out.toString();
    }
    static String text(Map<String, byte[]> entries, String name) { return new String(entries.get(name), StandardCharsets.UTF_8); }
    static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }

    /**
     * The warning and metadata rows of {@code expected-findings.tsv}. Its inspect rows are derived from
     * {@code expected-inspection.tsv} ({@link ExtractionInspection#findingsTable}, CR-050 phase 0).
     */
    static String findings() {
        StringBuilder out = new StringBuilder("case\tfixture\tchannel\tkind\tselector\tscenario\texpected\tstatements\n");
        finding(out, 50, "metadata", "fields_update_on_open", "", "default", "true", "REQ-506");
        finding(out, 52, "metadata", "ProbeCustom", "", "default", "CPR52_CUSTOM", "REQ-515");
        finding(out, 52, "metadata", "Company", "", "default", "APP52_COMPANY", "REQ-515");
        for (int n : new int[] {36, 43, 44, 45, 46, 47, 63}) {
            String type = n == 36 || n == 47 || n == 63 ? "text/html" : n == 43 ? "text/plain" : n == 44 ? "message/rfc822" : n == 45 ? "application/rtf" : "application/xml";
            String absent = ExtractionConformance.HTML_READER_ABSENT, present = ExtractionConformance.HTML_READER_PRESENT;
            // without an HTML reader, case 36's chunk and case 47's nested one, in the same fixture, are not
            // expanded (CR-053 D7, WD 0.27); with one, only case 47's, one level down
            if (n == 36) finding(out, n, "warning", "altchunk.not_expanded", type, absent, "2", "REQ-513");
            if (n == 44 || n == 63) finding(out, n, "warning", "altchunk.not_expanded", type, absent, "1", "REQ-513");
            if (n == 45) finding(out, n, "warning", "altchunk.not_expanded", type, "default", "1", "REQ-513");
            if (n == 47) finding(out, n, "warning", "altchunk.not_expanded", type, present, "1", "REQ-513");
            finding(out, n, "warning", "altchunk.not_expanded", n == 47 ? ExtractionPhase0Cases.DOCX_CHUNK : type, "altchunks: false", "1", "REQ-513");
        }
        return out.toString();
    }
    static void finding(StringBuilder out, int n, String channel, String kind, String selector, String scenario, String expected, String req) {
        out.append(n).append('\t').append(ExtractionPhase0Cases.fixture(n)).append('\t').append(channel).append('\t')
            .append(kind).append('\t').append(selector).append('\t').append(scenario).append('\t').append(expected).append('\t').append(req).append('\n');
    }
}
