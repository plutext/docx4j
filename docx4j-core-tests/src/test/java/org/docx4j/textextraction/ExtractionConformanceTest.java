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

import static org.junit.Assert.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Phase 0 checks the corpus and protocol; extraction assertions run through {@link TextsAdapter} (CR-046 phase 1). */
public class ExtractionConformanceTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    static Path fixtures() {
        for (Path dir : List.of(Path.of("src/test/resources/text-extraction/phase0"),
                Path.of("docx4j-core-tests/src/test/resources/text-extraction/phase0")))
            if (Files.isDirectory(dir)) return dir;
        throw new IllegalStateException("Run from repository root or docx4j-core-tests");
    }
    @Test public void corpusHasEveryCaseAndValidPayloads() throws Exception {
        Path dir = fixtures();
        List<ExtractionProbeBuilder.Case> catalogue = new ArrayList<>();
        for (Map<String, String> row : ExtractionConformance.table(dir.resolve("expected-tokens.tsv"))) {
            int n = Integer.parseInt(row.get("case"));
            while (catalogue.size() < n) catalogue.add(new ExtractionProbeBuilder.Case(catalogue.size() + 1, ""));
            catalogue.get(n - 1).tokens.add(new ExtractionProbeBuilder.Token(row.get("token"), row.get("accepted"), row.get("original"),
                    row.get("markup"), row.get("option"), row.get("with_option"), row.get("note")));
            assertEquals(ExtractionPhase0Cases.fixture(n), row.get("fixture"));
            assertEquals(ExtractionPhase0Cases.statements(n), row.get("statements"));
        }
        ExtractionFixtureChecks.check(Files.readAllBytes(dir.resolve("extraction-probe.docx")),
                Files.readAllBytes(dir.resolve("extraction-probe-external.docm")),
                Files.readAllBytes(dir.resolve(ExtractionStructureCases.FIXTURE)),
                Files.readAllBytes(dir.resolve(ExtractionInspectCases.FIXTURE)), catalogue);
        assertEquals(ExtractionInspection.findingsTable(dir), Files.readString(dir.resolve("expected-findings.tsv")));
        assertEquals(ExtractionAdditionalCases.orders(), Files.readString(dir.resolve("expected-order.tsv")));
    }
    /** CR-050 phase 0: every row of the inspection oracle is where its fixture has it (text-extraction/INSPECTION.md) */
    @Test public void inspectionOracleMatchesTheFixtures() throws Exception {
        ExtractionInspection.check(fixtures());
    }
    /** The inspection is compared by occurrence: a finding missing, unexpected, doubled or with another group fails. */
    @Test public void inspectionComparisonIsExactByOccurrence() throws Exception {
        Path dir = temporary.newFolder("inspected").toPath();
        Files.writeString(dir.resolve("expected-tokens.tsv"), "case\ttoken\taccepted\toriginal\tmarkup\toption\twith_option\tfixture\tstatements\n");
        Files.writeString(dir.resolve("expected-findings.tsv"), "case\tfixture\tchannel\tkind\tselector\tscenario\texpected\tstatements\n");
        Files.writeString(dir.resolve("goldens.tsv"), "fixture\tcase\tview\tscenario\tformat\tgolden\tstatements\n");
        String header = "fixture\tcase\tkind\tchain\tpart\tpath\tstart\tend_path\tend\taddress\tvisible\treveal\tplain\tmarkdown"
                + "\tblocks\thidden\tembedded\tcomments\texcerpt_length\texcerpt_cut\tselector\tsummary\n";
        String one = "f.docx\t1\ttext.hidden\t\t/word/document.xml\t/w:document[1]/w:body[1]/w:p[1]/w:r[1]\t0\t\t4\tbody/0\tno"
                + "\toptions: hidden: true\tomitted\tomitted\tomitted\tunseen\tomitted\tomitted\t4\tfalse\tA_B\t\n";
        String two = one.replace("w:p[1]", "w:p[2]").replace("body/0", "body/1");
        Files.writeString(dir.resolve(ExtractionInspection.TABLE), header + one + two);
        Files.writeString(dir.resolve(ExtractionInspection.STATUS), "fixture\tstatus\tskipped\tundecided\nf.docx\tCOMPLETE\t\t\n");
        List<Map<String, String>> expected = ExtractionConformance.table(dir.resolve(ExtractionInspection.TABLE));
        final List<List<Map<String, String>>> reported = new ArrayList<>();
        ExtractionConformance.Engine engine = new ExtractionConformance.Engine() {
            public Set<String> implementedStatements() { return Set.of("REQ-901", "REQ-902", "REQ-904", "REQ-905", "REQ-906", "REQ-907", "REQ-1204",
                    ExtractionConformance.HTML_READER_ABSENT); }
            public ExtractionConformance.Output extract(Path p, String view, String scenario, String format) { throw new AssertionError(); }
            public ExtractionConformance.Inspection inspect(Path fixture) {
                return new ExtractionConformance.Inspection(reported.get(0), "COMPLETE", List.of(), List.of(),
                        ExtractionInspection.LIMITATIONS);
            }
        };
        Path report = dir.resolve("report.tsv");
        reported.add(expected);
        ExtractionConformance.run(dir, report, engine);
        assertTrue(Files.readString(report).contains("f.docx\t0\tinspection\t-\t" + ExtractionConformance.HTML_READER_ABSENT + "\tPASS"));
        Map<String, String> moved = new LinkedHashMap<>(expected.get(1));
        moved.put("plain", "unseen");
        for (List<Map<String, String>> wrong : List.of(List.of(expected.get(0)), List.of(expected.get(0), expected.get(1), expected.get(1)),
                List.of(expected.get(0), moved))) {
            reported.set(0, wrong);
            try { ExtractionConformance.run(dir, report, engine); fail("A wrong inspection passed: " + wrong.size()); }
            catch (AssertionError e) { assertTrue(e.getMessage(), e.getMessage().contains("inspection differs")); }
        }
    }
    @Test public void docxAltChunkRequiresItsImportContentType() throws Exception {
        Map<String, byte[]> entries = ExtractionFixtureChecks.entries(
                Files.readAllBytes(fixtures().resolve("extraction-probe.docx")));
        assertEquals(ExtractionPhase0Cases.DOCX_CHUNK,
                ExtractionFixtureChecks.contentType(entries, "word/chunk47.docx"));
        assertEquals(ExtractionPhase0Cases.DOCX,
                ExtractionFixtureChecks.contentType(entries, "word/embeddings/probe40.docx"));
        org.w3c.dom.Document types = ExtractionFixtureChecks.xml(entries.get("[Content_Types].xml"));
        org.w3c.dom.NodeList declarations = types.getDocumentElement().getChildNodes();
        for (int i = 0; i < declarations.getLength(); i++) if (declarations.item(i) instanceof org.w3c.dom.Element) {
            org.w3c.dom.Element e = (org.w3c.dom.Element) declarations.item(i);
            if (e.getAttribute("PartName").equals("/word/chunk47.docx"))
                e.setAttribute("ContentType", ExtractionPhase0Cases.DOCX);
        }
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        javax.xml.transform.TransformerFactory.newInstance().newTransformer().transform(
                new javax.xml.transform.dom.DOMSource(types), new javax.xml.transform.stream.StreamResult(out));
        entries.put("[Content_Types].xml", out.toByteArray());
        try {
            ExtractionFixtureChecks.validatePackage(entries);
            fail("Package MIME type incorrectly accepted for a DOCX altChunk");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("DOCX altChunk content type"));
        }
    }
    @Test public void requirementsAndReadmeMatchAppendixA() throws Exception {
        Path dir = fixtures();
        List<Map<String, String>> appendix = ExtractionConformance.table(dir.getParent().resolve("support/appendix-a-statements.tsv"));
        List<Map<String, String>> generated = ExtractionConformance.table(dir.resolve("case-statements.tsv"));
        assertEquals(52, appendix.size());
        List<Map<String, String>> additions = ExtractionConformance.table(dir.getParent().resolve("support/additional-case-statements.tsv"));
        // 53-54 (WD 0.5), 55-57 (extraction-probe-structure.docx, 19 October 2026), 58-67 (extraction-probe-inspect.docx, CR-050),
        // 68-74 (the same fixture, the text a picture draws, CR-050 phase 3), 75-82 (the same fixture, an integer attribute
        // beyond Word's storage width, CR-066 phase 4)
        assertEquals(30, additions.size());
        appendix.addAll(additions);
        assertEquals(82, generated.size());
        String readme = Files.readString(dir.resolve("README.md"));
        for (int i = 0; i < appendix.size(); i++) {
            assertEquals(appendix.get(i).get("case"), generated.get(i).get("case"));
            assertEquals(appendix.get(i).get("statements"), generated.get(i).get("statements"));
            String section = readme.split("### Case " + (i + 1) + ":", 2)[1].split("### Case ", 2)[0];
            for (String line : section.split("\n")) if (line.startsWith("| `"))
                assertTrue(line.endsWith("| " + appendix.get(i).get("statements") + " |"));
        }
    }
    @Test public void normalizationRetainsSignificantCharacters() {
        assertEquals("MTH32A", ExtractionConformance.normalize("{++ $MTH32\\_A$ ++}"));
        assertEquals("ZW48SP\u200bLIT", ExtractionConformance.normalize("ZW48_SP\u200bLIT"));
        assertEquals("TAG48MARK\udb40\udc41\udb40\udc42", ExtractionConformance.normalize("TAG48_MARK\udb40\udc41\udb40\udc42"));
        assertNotEquals(ExtractionConformance.normalize("a\u00adb"), ExtractionConformance.normalize("ab"));
        assertNotEquals(ExtractionConformance.normalize("a\u2011b"), ExtractionConformance.normalize("ab"));
        assertEquals(1, ExtractionConformance.count("MTH32\\_A", "MTH32_A"));
    }
    @Test public void markingMustBeRightBeforeNormalization() {
        ExtractionConformance.verify("{-- TC10_MOVED --}{++ TC10_MOVED ++}", "TC10_MOVED", "del+ins", "move");
        try {
            ExtractionConformance.verify("{-- TC9_NEW --}", "TC9_NEW", "ins", "wrong marking");
            fail("Wrong marking passed");
        } catch (AssertionError expected) { assertTrue(expected.getMessage().contains("marking ins")); }
    }
    @Test public void goldenReaderUsesRequiredNormalizationAndDetectsChanges() throws Exception {
        Path golden = temporary.newFile("example.accepted.txt").toPath();
        Files.writeString(golden, "A_B\n", StandardCharsets.UTF_8);
        ExtractionConformance.verifyGolden(golden, "A\\_B");
        try { ExtractionConformance.verifyGolden(golden, "A_C"); fail("Changed golden passed"); }
        catch (AssertionError expected) { assertTrue(expected.getMessage().contains("expected AB")); }
    }
    /** WD REQ-703 writes ![alt](…): a Markdown golden's image destinations aren't compared, its alt text is (CR-046 phase 3). */
    @Test public void markdownGoldenIgnoresImageDestinations() throws Exception {
        Path golden = temporary.newFile("example.md").toPath();
        Files.writeString(golden, "See ![IMG\\_ALT \\[1\\]](\u2026) here.\n", StandardCharsets.UTF_8);
        ExtractionConformance.verifyGolden(golden, "See ![IMG\\_ALT \\[1\\]](data:image/png;base64,iVBORw0KGgo=) here.\n");
        ExtractionConformance.verifyGolden(golden, "See ![IMG\\_ALT \\[1\\]](images/image1.png) here.\n");
        try { ExtractionConformance.verifyGolden(golden, "See ![IMG\\_OTHER](images/image1.png) here.\n"); fail("Changed alt text passed"); }
        catch (AssertionError expected) { assertTrue(expected.getMessage(), expected.getMessage().contains("IMGALT")); }
        // a plain-text golden compares everything
        Path text = temporary.newFile("example.accepted.txt").toPath();
        Files.writeString(text, "![a](\u2026)\n", StandardCharsets.UTF_8);
        try { ExtractionConformance.verifyGolden(text, "![a](x.png)"); fail("A plain-text golden ignored a destination"); }
        catch (AssertionError expected) { }
    }
    @Test public void enabledAdapterAndPublishedGoldenAreActuallyChecked() throws Exception {
        Path dir = temporary.newFolder("suite").toPath();
        Files.writeString(dir.resolve("expected-tokens.tsv"),
                "case\ttoken\taccepted\toriginal\tmarkup\toption\twith_option\tfixture\tstatements\n"
                + "9\tTC9_NEW\t1\t0\tins\t\t\tfixture.docx\tREQ-402, REQ-403\n");
        Files.writeString(dir.resolve("expected-findings.tsv"), "case\tfixture\tchannel\tkind\tselector\tscenario\texpected\tstatements\n");
        Files.writeString(dir.resolve("goldens.tsv"), "fixture\tcase\tview\tscenario\tformat\tgolden\tstatements\n"
                + "fixture.docx\t0\taccepted\tdefault\ttxt\tfixture.accepted.txt\tREQ-402\n");
        Files.writeString(dir.resolve("fixture.accepted.txt"), "TC9_NEW\n");
        ExtractionConformance.Engine engine = new ExtractionConformance.Engine() {
            // an engine says which of the two HTML-reader capabilities it has; the goldens need "absent"
            public Set<String> implementedStatements() { return Set.of("REQ-402", "REQ-403", ExtractionConformance.HTML_READER_ABSENT); }
            public ExtractionConformance.Output extract(Path p, String view, String scenario, String format) {
                return new ExtractionConformance.Output(view.equals("original") ? "" : view.equals("markup") ? "{++ TC9_NEW ++}" : "TC9_NEW", Map.of());
            }
        };
        Path report = dir.resolve("report.tsv");
        ExtractionConformance.run(dir, report, engine);
        assertEquals(5, Files.readAllLines(report).size()); // header, three views and golden
        assertFalse(Files.readString(report).contains("PENDING"));
        Files.writeString(dir.resolve("fixture.accepted.txt"), "BROKEN\n");
        try { ExtractionConformance.run(dir, report, engine); fail("Golden mismatch passed"); }
        catch (AssertionError expected) { assertTrue(expected.getMessage().contains("fixture.accepted.txt")); }
    }
    @Test public void presentationAndGlossaryOrderAssertionsRunInEveryView() throws Exception {
        Path dir = temporary.newFolder("ordered-suite").toPath();
        Files.writeString(dir.resolve("expected-tokens.tsv"), "case\ttoken\taccepted\toriginal\tmarkup\toption\twith_option\tfixture\tstatements\n");
        Files.writeString(dir.resolve("expected-findings.tsv"), "case\tfixture\tchannel\tkind\tselector\tscenario\texpected\tstatements\n");
        Files.writeString(dir.resolve("goldens.tsv"), "fixture\tcase\tview\tscenario\tformat\tgolden\tstatements\n");
        Files.writeString(dir.resolve("expected-order.tsv"), "case\tfixture\tscenario\ttokens\tstatements\n"
                + "53\tfixture.docx\tembedded: true\tFIRST53_SHAPE > LAST53_SHAPE\tREQ-512\n");
        final boolean[] reversed = {false};
        ExtractionConformance.Engine engine = new ExtractionConformance.Engine() {
            public Set<String> implementedStatements() { return Set.of("REQ-512"); }
            public ExtractionConformance.Output extract(Path p, String view, String scenario, String format) {
                assertEquals("embedded: true", scenario);
                return new ExtractionConformance.Output(reversed[0] ? "LAST53_SHAPE FIRST53_SHAPE" : "FIRST53_SHAPE LAST53_SHAPE", Map.of());
            }
        };
        Path report = dir.resolve("report.tsv");
        ExtractionConformance.run(dir, report, engine);
        assertEquals(4, Files.readAllLines(report).size());
        assertFalse(Files.readString(report).contains("PENDING"));
        reversed[0] = true;
        try { ExtractionConformance.run(dir, report, engine); fail("Reordered shapes passed"); }
        catch (AssertionError expected) { assertTrue(expected.getMessage().contains("wrong order")); }
    }
    @Test public void extractionRequirementsAreCheckedOrReportedPending() throws Exception {
        Path report = Path.of("target/text-extraction/conformance.tsv");
        ExtractionConformance.run(fixtures(), report, ExtractionConformance.engine());
        String results = Files.readString(report);
        assertTrue(results.contains("PENDING"));
        assertTrue(results.contains("VBA51_MODULE_MARK"));
        assertTrue(results.contains("ALT47_NESTED_CHUNK"));
        if (ExtractionConformance.engine() != null) {
            // CR-046 phases 1 and 2, CR-047 and CR-050 phase 1: their rows checked; Markdown's wait on phase 3,
            // the HTML reader's rows on the class path (CR-053)
            assertTrue(results.contains("\tCHOICE_ALPHA_123\tmarkup\tdefault\tPASS"));
            assertTrue(results.contains("\tTBX25_IN_CELL\toriginal\ttext_boxes: false\tPASS"));
            assertTrue(results.contains("\tCHT34_TITLE\taccepted\tdefault\tPASS"));
            assertTrue(results.contains("\tEMB53_TABLE_D\tmarkup\tembedded: true\tPASS"));
            assertTrue(results.contains("\tGLS54_ENTRY_TWO\toriginal\tinclude: glossary\tPASS"));
            assertTrue(results.contains("\tCHT34_AXIS_TITLE\taccepted\tdefault\tPASS"));
            assertTrue(results.contains("\tTBL31_HEADER\taccepted\tdefault\tPASS"));
            // CR-050 phase 1: the inspector's rows and each fixture's whole inspection; since phase 4 the table's
            // capability column lets either environment check every fixture against the rows that apply to it
            boolean reader = org.docx4j.model.text.HtmlChunkImport.readerPresent();
            String environment = reader ? "html reader present" : "html reader absent", other = reader ? "html reader absent" : "html reader present";
            assertTrue(results.contains("extraction-probe-inspect.docx\t0\tinspection\t-\t" + environment + "\tPASS"));
            assertTrue(results.contains("extraction-probe.docx\t0\tinspection\t-\t" + environment + "\tPASS"));
            assertTrue(results.contains("extraction-probe-html.docx\t0\tinspection\t-\t" + environment + "\tPASS"));
            assertTrue(results.contains("extraction-probe-external.docm\t0\tinspection\t-\t" + environment + "\tPASS"));
            assertTrue(results.contains("\tinspect:font.glyph_mismatch:"));
            // the other environment's rows pend by count, per fixture with any (the HTML fixture's eleven text.hidden
            // rows with no reader; the chunks' altchunk rows with one); since WD 0.27 no row pends on a rule
            assertTrue(results.contains("extraction-probe-html.docx\t0\tinspection\t-\t" + other + "\tPENDING\t"));
            // a reader-present row's (kind, selector) count is checked by selector with a reader, pends without one
            assertTrue(results.contains("\tinspect:text.hidden:HTM_A_INLINE\taccepted\thtml reader present\t" + (reader ? "PASS" : "PENDING")));
            assertTrue(results.contains("\tcounts\t-\tplain\tPASS"));
            for (String line : results.split("\n"))
                if (line.contains("\tPENDING\t")) assertTrue(line, line.contains("phase 3; REQ-70") || line.contains("html reader"));
        } else {
            assertTrue(results.contains("the inspector (CR-050)"));
        }
    }
}
