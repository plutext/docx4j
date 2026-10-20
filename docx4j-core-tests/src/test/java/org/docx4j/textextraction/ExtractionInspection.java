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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.model.text.TextBlock;
import org.docx4j.model.text.TextCell;
import org.docx4j.model.text.TextComment;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextResult;
import org.docx4j.model.text.TextRow;
import org.docx4j.model.text.TextRun;
import org.docx4j.model.text.TextView;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The inspection oracle's Java side (CR-050 phase 0; {@code text-extraction/INSPECTION.md}):
 * {@code support/inspect_oracle.py} writes {@value #TABLE} and {@value #STATUS}; this fills the address
 * column from docx4j's text model, derives the inspect rows of {@code expected-findings.tsv}, and checks
 * every row against its fixture (the path resolves, the range lies in the run's text, a range's excerpt
 * is as long as the text it covers).
 *
 * <p>Usage, after {@link ExtractionProbeBuilder} and {@code inspect_oracle.py}:
 * {@code ExtractionInspection <phase0-dir>} rewrites {@value #TABLE} with its addresses and
 * {@code expected-findings.tsv} with its inspect rows.</p>
 */
public final class ExtractionInspection {

    static final String TABLE = "expected-inspection.tsv", STATUS = "expected-inspection-status.tsv";

    /**
     * An inspector with an HTML reader (CR-053 phase 3): {@link ExtractionHtmlCases#FIXTURE}'s findings, written
     * by hand and checked against Word 365's save of the fixture, with {@code parsed}, the statements and the
     * probe each rests on, and {@code pending} where it rests on a rule not yet ruled on (WD 0.27)
     */
    static final String HTML_TABLE = "expected-inspection-html.tsv", HTML_STATUS = "expected-inspection-html-status.tsv";

    /** A finding's identity: its kind and its stored location (CR-050 section 2.4) */
    static final List<String> KEY = List.of("kind", "chain", "part", "path", "start", "end_path", "end");

    /** The fields compared for a finding whose identity matched */
    static final List<String> FIELDS = List.of("address", "visible", "reveal", "plain", "markdown", "blocks", "hidden",
            "embedded", "comments", "excerpt_length", "excerpt_cut");

    /**
     * The statements a row of the inspector needs: WD 0.17's section 9 (f79bf8b), but REQ-903's counts in a result,
     * which have their own check (CR-050 phase 2: each output's counts against the table's groups)
     */
    static final String STATEMENTS = "REQ-901, REQ-902, REQ-904, REQ-905, REQ-906, REQ-907";

    /**
     * The six outputs the table gives each finding's group under (INSPECTION.md): plain text, Markdown and the block
     * model under the defaults; plain text under {@code hidden: true}, {@code embedded: true} and {@code comments: true}
     */
    static final List<String> OUTPUTS = List.of("plain", "markdown", "blocks", "hidden", "embedded", "comments");

    /**
     * What every inspector says it does not detect (REQ-904, CR-050 section 2.6), by the id each limitation
     * starts with: objects covered by others; positions only a layout knows; text clipped by its frame; a
     * background that is an image; an embedded font swapped with care; content of a payload not decoded
     * (ActiveX, OLE binaries, VBA, parts of unknown kind); a chart's embedded workbook; intent.
     */
    static final List<String> LIMITATIONS = List.of("layout.covered", "layout.position", "layout.clipped",
            "background.image", "font.careful_swap", "payload.opaque", "chart.workbook", "intent");

    /** The canonical prefixes of INSPECTION.md */
    static final Map<String, String> NAMESPACES = new LinkedHashMap<>();
    static {
        String[][] ns = {
            { "w", "http://schemas.openxmlformats.org/wordprocessingml/2006/main" },
            { "r", "http://schemas.openxmlformats.org/officeDocument/2006/relationships" },
            { "rel", "http://schemas.openxmlformats.org/package/2006/relationships" },
            { "wp", "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" },
            { "a", "http://schemas.openxmlformats.org/drawingml/2006/main" },
            { "pic", "http://schemas.openxmlformats.org/drawingml/2006/picture" },
            { "c", "http://schemas.openxmlformats.org/drawingml/2006/chart" },
            { "dgm", "http://schemas.openxmlformats.org/drawingml/2006/diagram" },
            { "wps", "http://schemas.microsoft.com/office/word/2010/wordprocessingShape" },
            { "wpg", "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup" },
            { "mc", "http://schemas.openxmlformats.org/markup-compatibility/2006" },
            { "v", "urn:schemas-microsoft-com:vml" },
            { "o", "urn:schemas-microsoft-com:office:office" },
            { "w10", "urn:schemas-microsoft-com:office:word" },
            { "w14", "http://schemas.microsoft.com/office/word/2010/wordml" },
            { "w15", "http://schemas.microsoft.com/office/word/2012/wordml" },
            { "m", "http://schemas.openxmlformats.org/officeDocument/2006/math" },
            { "x", "http://schemas.openxmlformats.org/spreadsheetml/2006/main" },
            { "p", "http://schemas.openxmlformats.org/presentationml/2006/main" },
            { "cp", "http://schemas.openxmlformats.org/package/2006/metadata/core-properties" },
            { "dc", "http://purl.org/dc/elements/1.1/" },
            { "dcterms", "http://purl.org/dc/terms/" },
            { "ep", "http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" },
            { "op", "http://schemas.openxmlformats.org/officeDocument/2006/custom-properties" },
            { "vt", "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes" },
            { "pkg", "http://schemas.microsoft.com/office/2006/xmlPackage" } };
        for (String[] n : ns) NAMESPACES.put(n[0], n[1]);
    }
    static final String W = NAMESPACES.get("w"), M = NAMESPACES.get("m");

    private ExtractionInspection() {}

    public static void main(String[] args) throws Exception {
        Path dir = Path.of(args[0]);
        List<Map<String, String>> rows = ExtractionConformance.table(dir.resolve(TABLE));
        Map<String, List<TextBlock>> outputs = new HashMap<>();
        for (Map<String, String> row : rows) row.put("address", address(outputs, dir, row));
        write(dir.resolve(TABLE), rows);
        Files.writeString(dir.resolve("expected-findings.tsv"), findingsTable(dir), StandardCharsets.UTF_8);
        System.out.println("wrote addresses for " + rows.size() + " findings, and expected-findings.tsv");
    }

    static void write(Path file, List<Map<String, String>> rows) throws Exception {
        StringBuilder out = new StringBuilder(String.join("\t", rows.get(0).keySet())).append('\n');
        for (Map<String, String> row : rows) out.append(String.join("\t", row.values())).append('\n');
        Files.writeString(file, out.toString(), StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------ expected-findings.tsv

    /**
     * {@code expected-findings.tsv}: a row for every (kind, selector) the inspection table holds, its count
     * the number of findings with that key (for an adapter that cannot report locations), then the
     * warning and metadata rows {@link ExtractionFixtureChecks#findings()} writes.
     */
    static String findingsTable(Path dir) throws Exception {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Map<String, String> row : ExtractionConformance.table(dir.resolve(TABLE))) {
            String key = row.get("case") + "\t" + row.get("fixture") + "\tinspect\t" + row.get("kind") + "\t" + row.get("selector");
            counts.merge(key, 1, Integer::sum);
        }
        String others = ExtractionFixtureChecks.findings();
        StringBuilder out = new StringBuilder(others.substring(0, others.indexOf('\n') + 1));
        for (Map.Entry<String, Integer> e : counts.entrySet())
            out.append(e.getKey()).append("\tdefault\t").append(e.getValue()).append('\t').append(STATEMENTS).append('\n');
        return out.append(others.substring(others.indexOf('\n') + 1)).toString();
    }

    // ------------------------------------------------------------ addresses

    /**
     * The REQ-518/519 address of the block holding a finding in the output its reveal names, as docx4j's
     * text model gives it; empty where the finding is unextractable or no block holds its selector.
     */
    static String address(Map<String, List<TextBlock>> outputs, Path dir, Map<String, String> row) throws Exception {
        String reveal = row.get("reveal");
        if (!reveal.equals("included") && !reveal.startsWith("options: ")) return "";
        // an author and the properties are in no block (metadata, markup's author detail)
        if (row.get("kind").equals("revision.author") || row.get("kind").equals("properties")) return "";
        if (reveal.contains("html reader")) return "";   // a capability the default run has not
        String key = row.get("fixture") + "\t" + reveal;
        List<TextBlock> blocks = outputs.get(key);
        if (blocks == null) {
            WordprocessingMLPackage pkg = WordprocessingMLPackage.load(dir.resolve(row.get("fixture")).toFile());
            TextOptions options = TextOptions.of(TextView.of(reveal.contains("view: original") ? "original"
                    : reveal.contains("view: markup") ? "markup" : "accepted"));
            if (reveal.startsWith("options: ")) {
                for (String option : reveal.substring("options: ".length()).split("; ")) {
                    String name = option.substring(0, option.indexOf(':')).trim(), value = option.substring(option.indexOf(':') + 1).trim();
                    if (!name.equals("view")) options.set(name, value);
                }
            }
            TextResult result = Texts.of(pkg, options);
            blocks = new ArrayList<>();
            for (TextBlock b : result.blocks()) flatten(b, blocks);
            for (TextComment c : result.comments().values()) for (TextBlock b : c.getBlocks()) flatten(b, blocks);
            outputs.put(key, blocks);
        }
        String selector = row.get("selector");
        boolean image = row.get("kind").startsWith("alt_text.");
        for (TextBlock b : blocks) {
            if (image) {
                for (TextRun r : b.getRuns()) {
                    if (r.getImage() == null) continue;
                    if (selector.equals(r.getImage().getDescr()) || selector.equals(r.getImage().getName())) return b.getAddress();
                }
            } else if (b.getKind() != TextBlock.Kind.TABLE && b.getText().contains(selector)) {
                return b.getAddress();
            }
        }
        return "";
    }

    private static void flatten(TextBlock b, List<TextBlock> out) {
        out.add(b);
        for (TextRow row : b.getRows()) for (TextCell cell : row.getCells()) for (TextBlock inner : cell.getBlocks()) flatten(inner, out);
    }

    // ------------------------------------------------------------ the structural check

    /** Every row's location exists in its fixture; a range lies in its run's text and is as long as its excerpt */
    static void check(Path dir) throws Exception {
        Map<String, Map<String, byte[]>> fixtures = new HashMap<>();
        Map<String, Document> parsed = new HashMap<>();
        int n = 0;
        for (Map<String, String> row : ExtractionConformance.table(dir.resolve(TABLE))) {
            n++;
            String context = TABLE + ":" + (n + 1) + " " + row.get("kind") + " " + row.get("selector");
            Map<String, byte[]> entries = fixtures.get(row.get("fixture"));
            if (entries == null) fixtures.put(row.get("fixture"), entries = ExtractionFixtureChecks.entries(Files.readAllBytes(dir.resolve(row.get("fixture")))));
            if (!row.get("chain").isEmpty()) for (String link : row.get("chain").split(">")) entries = open(entries.get(link.substring(1)), context);
            byte[] part = entries.get(row.get("part").substring(1));
            require(part != null, context + ": no part " + row.get("part"));
            if (row.get("path").isEmpty()) {
                require(row.get("start").isEmpty() && row.get("end_path").isEmpty(), context + ": a range without a path");
                continue;
            }
            String key = row.get("fixture") + "|" + row.get("chain") + "|" + row.get("part");
            Document doc = parsed.get(key);
            if (doc == null) parsed.put(key, doc = ExtractionFixtureChecks.xml(part));
            Element first = resolve(doc, row.get("path"));
            require(first != null, context + ": no element " + row.get("path"));
            Element last = row.get("end_path").isEmpty() ? first : resolve(doc, row.get("end_path"));
            require(last != null, context + ": no element " + row.get("end_path"));
            if (row.get("start").isEmpty()) continue;
            int start = Integer.parseInt(row.get("start")), end = Integer.parseInt(row.get("end"));
            int firstLength = codePoints(runText(first)), lastLength = codePoints(runText(last));
            require(start >= 0 && start < firstLength && end > 0 && end <= lastLength, context + ": range " + start + "-" + end);
            int length = Integer.parseInt(row.get("excerpt_length"));
            if (row.get("kind").equals("hyperlink.web")) continue;   // its excerpt is the field's target, not its range
            if (first == last) equal(end - start, length, context + ": excerpt length");
            else require(length >= firstLength - start + end, context + ": excerpt length " + length);
        }
        // the address is the text model's: a change there that moves a block shows here
        Map<String, List<TextBlock>> outputs = new HashMap<>();
        for (Map<String, String> row : ExtractionConformance.table(dir.resolve(TABLE)))
            equal(row.get("address"), address(outputs, dir, row), TABLE + ": address of " + row.get("kind") + " " + row.get("selector"));
        List<Map<String, String>> status = ExtractionConformance.table(dir.resolve(STATUS));
        require(status.size() == fixtures.size(), "a status row for every fixture with findings");
        for (Map<String, String> row : status) require(row.get("status").equals(row.get("skipped").isEmpty() ? "COMPLETE" : "PARTIAL"),
                STATUS + ": " + row.get("fixture") + " is PARTIAL exactly when something was skipped");
        checkHtml(dir);
    }

    /**
     * Every row of {@value #HTML_TABLE}: its part exists; a path resolves in the tree jsoup builds from the
     * chunk (an MHT chunk's first HTML part), its elements unprefixed, and holds the selector; the excerpt is
     * as long as the text the element hides, a block a line feed apart; the reveal names the reader, and a
     * pending names the WD it waits for.
     */
    static void checkHtml(Path dir) throws Exception {
        Path table = dir.resolve(HTML_TABLE);
        if (!Files.exists(table)) return;
        Map<String, org.jsoup.nodes.Document> trees = new HashMap<>();
        int n = 0;
        for (Map<String, String> row : ExtractionConformance.table(table)) {
            n++;
            String context = HTML_TABLE + ":" + (n + 1) + " " + row.get("kind") + " " + row.get("selector");
            byte[] part = ExtractionFixtureChecks.entries(Files.readAllBytes(dir.resolve(row.get("fixture")))).get(row.get("part").substring(1));
            require(part != null, context + ": no part " + row.get("part"));
            require(row.get("pending").isEmpty() || row.get("pending").startsWith("WD 0.27"), context + ": pending " + row.get("pending"));
            if (row.get("path").isEmpty()) continue;
            String key = row.get("fixture") + "|" + row.get("part");
            org.jsoup.nodes.Document tree = trees.get(key);
            if (tree == null) trees.put(key, tree = org.jsoup.Jsoup.parse(html(row.get("part"), part)));
            org.jsoup.nodes.Element e = tree;
            for (String step : row.get("path").substring(1).split("/")) {
                String name = step.substring(0, step.indexOf('['));
                int index = Integer.parseInt(step.substring(step.indexOf('[') + 1, step.length() - 1)), seen = 0;
                org.jsoup.nodes.Element hit = null;
                for (org.jsoup.nodes.Element c : e.children()) if (c.normalName().equals(name) && ++seen == index) { hit = c; break; }
                require(hit != null, context + ": no element " + row.get("path"));
                e = hit;
            }
            require(e.text().contains(row.get("selector")), context + ": the element's text is " + e.text());
            require(row.get("reveal").equals("options: hidden: true; html reader: present"), context + ": reveal " + row.get("reveal"));
            require(Integer.parseInt(row.get("excerpt_length")) == codePoints(String.join("\n", texts(e))), context + ": excerpt length");
        }
    }

    /** A chunk's HTML: an MHT chunk's first text/html part, else the bytes (UTF-8, as the fixture writes them) */
    private static String html(String part, byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8);
        if (!part.endsWith(".mht")) return s;
        int start = s.indexOf("<html"), end = s.indexOf("</html>", start);
        return s.substring(start, end + "</html>".length());
    }

    /**
     * The text an element hides, a block apart: each child block's, or its own where it has none; a child whose
     * own {@code display} is other than {@code none} or {@code inherit} shows its content (WD 0.27's REQ-513)
     */
    private static List<String> texts(org.jsoup.nodes.Element e) {
        List<String> out = new ArrayList<>();
        for (org.jsoup.nodes.Element c : e.children()) {
            if (c.attr("style").matches("(?i).*display\\s*:\\s*(?!none|inherit)[a-z-]+.*")) continue;
            if (c.isBlock() && !c.text().isEmpty()) out.addAll(texts(c));
        }
        if (out.isEmpty()) out.add(e.text());
        return out;
    }

    private static Map<String, byte[]> open(byte[] payload, String context) throws Exception {
        require(payload != null, context + ": no chained part");
        if (payload.length > 1 && payload[0] == 'P' && payload[1] == 'K') return ExtractionFixtureChecks.entries(payload);
        // Flat OPC: each pkg:part's XML
        Map<String, byte[]> out = new LinkedHashMap<>();
        Document flat = ExtractionFixtureChecks.xml(payload);
        org.w3c.dom.NodeList parts = flat.getElementsByTagNameNS(NAMESPACES.get("pkg"), "part");
        for (int i = 0; i < parts.getLength(); i++) {
            Element part = (Element) parts.item(i);
            org.w3c.dom.NodeList data = part.getElementsByTagNameNS(NAMESPACES.get("pkg"), "xmlData");
            if (data.getLength() == 0) continue;
            Node root = data.item(0).getFirstChild();
            while (root != null && root.getNodeType() != Node.ELEMENT_NODE) root = root.getNextSibling();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            javax.xml.transform.TransformerFactory.newInstance().newTransformer().transform(
                    new javax.xml.transform.dom.DOMSource(root), new javax.xml.transform.stream.StreamResult(bytes));
            out.put(part.getAttributeNS(NAMESPACES.get("pkg"), "name").substring(1), bytes.toByteArray());
        }
        return out;
    }

    /** The element at a canonical path ({@code /w:document[1]/w:body[1]/w:p[12]}), or null */
    static Element resolve(Document doc, String path) {
        Element el = null;
        for (String step : path.substring(1).split("/")) {
            int bracket = step.indexOf('[');
            String name = step.substring(0, bracket);
            int index = Integer.parseInt(step.substring(bracket + 1, step.length() - 1));
            String ns = NAMESPACES.get(name.substring(0, name.indexOf(':'))), local = name.substring(name.indexOf(':') + 1);
            if (ns == null) return null;
            Node child = el == null ? doc.getDocumentElement() : el.getFirstChild();
            Element hit = null;
            int seen = 0;
            for (; child != null; child = el == null ? null : child.getNextSibling()) {
                if (child.getNodeType() != Node.ELEMENT_NODE) continue;
                if (ns.equals(child.getNamespaceURI()) && local.equals(child.getLocalName()) && ++seen == index) { hit = (Element) child; break; }
            }
            if (hit == null) return null;
            el = hit;
        }
        return el;
    }

    /** A run's text (INSPECTION.md): w:t, w:delText, w:instrText, w:delInstrText, a code point per w:sym; m:t */
    static String runText(Element r) {
        StringBuilder out = new StringBuilder();
        for (Node c = r.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNodeType() != Node.ELEMENT_NODE) continue;
            String ns = c.getNamespaceURI(), local = c.getLocalName();
            if (W.equals(ns) && (local.equals("t") || local.equals("delText") || local.equals("instrText") || local.equals("delInstrText"))
                    || M.equals(ns) && local.equals("t")) out.append(c.getTextContent());
            else if (W.equals(ns) && local.equals("sym")) out.appendCodePoint(Integer.parseInt(((Element) c).getAttributeNS(W, "char"), 16));
        }
        return out.toString();
    }

    static int codePoints(String s) { return s.codePointCount(0, s.length()); }

    static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }

    static void equal(Object expected, Object actual, String context) {
        if (!expected.equals(actual)) throw new AssertionError(context + ": expected " + expected + ", got " + actual);
    }
}
