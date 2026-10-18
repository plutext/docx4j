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

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/** Phase 0's table/golden protocol; {@link TextsAdapter} supplies the engine (CR-046 phase 1). */
public final class ExtractionConformance {
    /** The adapter must advertise only statements it implements. Unsupported rows remain visible. */
    public interface Engine {
        Set<String> implementedStatements();
        Output extract(Path fixture, String view, String scenario, String format) throws Exception;
    }
    public static final class Output {
        public final String text;
        /** Entries are channel + TAB + kind + TAB + selector, values are counts or metadata values. */
        public final Map<String, String> findings;
        public Output(String text, Map<String, String> findings) {
            this.text = Objects.requireNonNull(text);
            this.findings = Map.copyOf(findings);
        }
    }

    static List<Map<String, String>> table(Path file) throws Exception {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        String[] header = lines.get(0).split("\t", -1);
        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isEmpty()) continue;
            String[] values = lines.get(i).split("\t", -1);
            if (values.length != header.length) throw new IllegalArgumentException(file + ":" + (i + 1) + ": column count");
            Map<String, String> row = new LinkedHashMap<>();
            for (int j = 0; j < header.length; j++) row.put(header[j], values[j]);
            rows.add(row);
        }
        return rows;
    }

    /** REQ-1201 only. Zero-width spaces, tag characters, hyphens and Unicode composition are retained. */
    public static String normalize(String value) {
        return value.replaceAll("\\{\\+\\+|\\+\\+}|\\{--|--}|\\{>>|<<}", "")
                .replaceAll("[\\s\\p{Z}*_\\\\{}$]", "");
    }
    public static int count(String text, String marker) {
        return count(text, marker, Set.of());
    }
    /**
     * Occurrences of a marker, not counting those inside a longer marker token of the suite: after
     * REQ-1201's normalisation removes underscores and whitespace, FLD19_RESULT is also found inside
     * FLD19_RESULT_TWO, and MCE8_OUTER inside MCE8_OUTER_FALLBACK.
     */
    public static int count(String text, String marker, Collection<String> suite) {
        String haystack = normalize(text), needle = normalize(marker);
        if (needle.isEmpty()) throw new IllegalArgumentException("Empty marker");
        boolean[] covered = new boolean[haystack.length()];
        for (String other : suite) {
            String longer = normalize(other);
            if (longer.length() <= needle.length() || !longer.contains(needle)) continue;
            for (int i = 0; (i = haystack.indexOf(longer, i)) >= 0; i += longer.length())
                java.util.Arrays.fill(covered, i, i + longer.length(), true);
        }
        int result = 0;
        for (int i = 0; (i = haystack.indexOf(needle, i)) >= 0; i += needle.length()) if (!covered[i]) result++;
        return result;
    }
    static void equal(Object expected, Object actual, String context) {
        if (!Objects.equals(expected, actual)) throw new AssertionError(context + ": expected " + expected + ", got " + actual);
    }
    static void verify(String text, String token, String expected, String context) {
        verify(text, token, expected, context, Set.of());
    }
    static void verify(String text, String token, String expected, String context, Collection<String> suite) {
        if (expected.matches("\\d+")) {
            equal(Integer.parseInt(expected), count(text, token, suite), context); return;
        }
        String[] kinds = expected.split("\\+");
        equal(kinds.length, count(text, token, suite), context + " total");
        for (String kind : kinds) {
            String opening = kind.equals("ins") ? "{++" : kind.equals("del") ? "{--" : kind.equals("cmt") ? "{>>" : null;
            if (opening == null) throw new IllegalArgumentException("Unknown marking: " + kind);
            String closing = kind.equals("ins") ? "++}" : kind.equals("del") ? "--}" : "<<}";
            Matcher matches = Pattern.compile(Pattern.quote(opening) + "(.*?)" + Pattern.quote(closing), Pattern.DOTALL).matcher(text);
            int marked = 0;
            while (matches.find()) marked += count(matches.group(1), token, suite);
            equal(1, marked, context + " marking " + kind);
        }
    }
    static Set<String> requirements(String value) { return new LinkedHashSet<>(Arrays.asList(value.split(",\\s*"))); }
    static Set<String> textRequirements(Map<String, String> row, String format) {
        Set<String> req = requirements(row.get("statements"));
        req.remove("REQ-902"); // Extractor class does not claim inspector conformance.
        if (row.get("token").equals("CHT34_AXIS_TITLE")) req.add("CR-047");
        if (row.get("case").equals("21") && format.equals("txt")) req.remove("REQ-703");
        // CR-046 section 4: case 25's text boxes are phase 1; only its legacy WordArt needs REQ-511
        if (row.get("case").equals("25") && !row.get("token").startsWith("WA25")) req.remove("REQ-511");
        return req;
    }
    /** Capabilities of the class path, not of an engine: whether docx4j-ImportXHTML is on it (REQ-513). */
    static final String XHTML_IMPORTER_PRESENT = "xhtml importer present", XHTML_IMPORTER_ABSENT = "xhtml importer absent";

    /** The statement a scenario's option belongs to (WD section 10), which the engine must implement too. */
    static Set<String> scenarioRequirements(String scenario) {
        if (scenario == null || scenario.equals("default")) return Set.of();
        if (scenario.equals(XHTML_IMPORTER_PRESENT)) return Set.of("REQ-513", XHTML_IMPORTER_PRESENT);
        if (scenario.equals(XHTML_IMPORTER_ABSENT)) return Set.of("REQ-513", XHTML_IMPORTER_ABSENT);
        if (scenario.equals("markdown")) return Set.of("REQ-703");
        if (scenario.contains(";")) {
            // several options, "; " apart: each one's statement
            Set<String> all = new LinkedHashSet<>();
            for (String option : scenario.split(";")) all.addAll(scenarioRequirements(option.trim()));
            return all;
        }
        String name = scenario.split(":", 2)[0].trim(), value = scenario.contains(":") ? scenario.split(":", 2)[1].trim() : "";
        switch (name) {
            case "include": return value.contains("glossary") ? Set.of("REQ-516") : value.contains("comments") ? Set.of("REQ-504") : Set.of("REQ-502");
            case "text_boxes": return Set.of("REQ-518");
            case "notes": case "note_references": return Set.of("REQ-503");
            case "comments": return Set.of("REQ-504");
            case "hidden": return Set.of("REQ-507");
            case "field_codes": return Set.of("REQ-505");
            case "fields": return Set.of("REQ-506");
            case "placeholders": return Set.of("REQ-508");
            case "bindings": return Set.of("REQ-509");
            case "list_labels": return Set.of("REQ-510");
            case "charts": case "smartart": return Set.of("REQ-511");
            case "embedded": return Set.of("REQ-512");
            case "altchunks": return Set.of("REQ-513");
            case "alt_text": return Set.of("REQ-514");
            case "metadata": return Set.of("REQ-515");
            case "symbols": return Set.of("REQ-601");
            case "math": return Set.of("REQ-604");
            case "tables": return Set.of("REQ-605");
            case "markup_detail": return Set.of("REQ-702");
            case "page_markers": return Set.of("REQ-708");
            case "layout": return Set.of("REQ-709");
            default: throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }
    }
    static int phase(Set<String> requirements) {
        int result = 1;
        for (String req : requirements) {
            // a dependency (CR-047), a class path, or a ruling still to come
            if (!req.startsWith("REQ-")) { result = Math.max(result, 2); continue; }
            int n = Integer.parseInt(req.substring(4));
            if (n >= 901 && n <= 904) return 9;
            if (Set.of(506,509,511,512,513,514,515,516,604,605,708,709).contains(n)) result = Math.max(result, 2);
            if (n == 703 || n == 704 || n == 705) result = Math.max(result, 3);
        }
        return result;
    }

    static final class Run {
        final Engine engine;
        final Set<String> tokens = new LinkedHashSet<>();
        final Map<String, Output> outputs = new HashMap<>();
        final List<String> report = new ArrayList<>();
        int passed, pending;
        Run(Engine engine) { this.engine = engine; report.add("fixture\tcase\titem\tview\tscenario\tstatus\treason"); }
        boolean ready(Map<String, String> row, String item, String view, String scenario, Set<String> requirements) {
            Set<String> missing = new LinkedHashSet<>(requirements);
            if (engine != null) missing.removeAll(engine.implementedStatements());
            boolean ready = engine != null && missing.isEmpty();
            if (!ready) {
                pending++;
                report.add(row.get("fixture") + "\t" + row.get("case") + "\t" + item + "\t" + view + "\t" + scenario
                        + "\tPENDING\t" + (phase(requirements) == 9 ? "later inspector CR" : "phase " + phase(requirements))
                        + "; " + String.join(", ", missing) + (engine == null ? "; Texts adapter not installed" : ""));
            }
            return ready;
        }
        Output output(Path dir, Map<String, String> row, String view, String scenario, String format) throws Exception {
            String key = row.get("fixture") + "\t" + view + "\t" + scenario + "\t" + format;
            if (!outputs.containsKey(key)) outputs.put(key, engine.extract(dir.resolve(row.get("fixture")), view, scenario, format));
            return outputs.get(key);
        }
        void pass(Map<String, String> row, String item, String view, String scenario) {
            passed++;
            report.add(row.get("fixture") + "\t" + row.get("case") + "\t" + item + "\t" + view + "\t" + scenario + "\tPASS\t");
        }
    }

    public static void run(Path dir, Path report, Engine engine) throws Exception {
        Run run = new Run(engine);
        for (Map<String, String> row : table(dir.resolve("expected-tokens.tsv"))) run.tokens.add(row.get("token"));
        for (Map<String, String> row : table(dir.resolve("expected-tokens.tsv"))) {
            for (String view : List.of("accepted", "original", "markup")) {
                // MHT's default scenario explicitly runs without the optional importer.
                String scenario = row.get("case").equals("44") ? "xhtml importer absent" : "default";
                checkToken(run, dir, row, view, scenario, "txt", row.get(view));
                String option = row.get("option");
                if (!option.isEmpty() && (!option.equals("comments: true") || view.equals("markup"))) {
                    String expected = row.get("with_option");
                    checkToken(run, dir, row, view, option, option.equals("markdown") ? "md" : "txt", expected);
                }
            }
        }
        // Supplemental scenarios pin combined capabilities, no-fetch fields, and character retention.
        for (String scenario : List.of("embedded: true", "altchunks: false", "fields: update", "xhtml importer present", "metadata: true", "field_codes: true")) {
            for (Map<String, String> row : table(dir.resolve("expected-tokens.tsv"))) {
                int n = Integer.parseInt(row.get("case"));
                boolean applies = scenario.equals("embedded: true") && (n >= 39 && n <= 42 || n == 53)
                        || scenario.equals("altchunks: false") && n >= 43 && n <= 47
                        || scenario.equals("fields: update") && n >= 49 && n <= 51
                        || scenario.equals("xhtml importer present") && n == 47
                        || (scenario.equals("metadata: true") || scenario.equals("field_codes: true") || scenario.equals("embedded: true")) && n >= 50 && n <= 51;
                if (!applies || row.get("option").equals(scenario)) continue;
                for (String view : List.of("accepted", "original", "markup"))
                    checkToken(run, dir, row, view, scenario, "txt", scenario.equals("altchunks: false") ? "0" : row.get(view));
            }
        }
        for (Map<String, String> row : table(dir.resolve("expected-findings.tsv"))) {
            String item = row.get("channel") + ":" + row.get("kind") + ":" + row.get("selector");
            for (String view : List.of("accepted", "original", "markup")) {
                if (!run.ready(row, item, view, row.get("scenario"), requirements(row.get("statements")))) continue;
                Output output = run.output(dir, row, view, row.get("scenario"), "txt");
                String key = row.get("channel") + "\t" + row.get("kind") + "\t" + row.get("selector");
                equal(row.get("expected"), output.findings.get(key), item);
                run.pass(row, item, view, row.get("scenario"));
            }
        }
        Path orders = dir.resolve("expected-order.tsv");
        if (Files.exists(orders)) for (Map<String, String> row : table(orders)) {
            for (String view : List.of("accepted", "original", "markup")) {
                if (!run.ready(row, "order", view, row.get("scenario"), requirements(row.get("statements")))) continue;
                Output output = run.output(dir, row, view, row.get("scenario"), "txt");
                verifyOrder(output.text, row.get("tokens"), row.get("case") + ":" + view);
                run.pass(row, "order", view, row.get("scenario"));
            }
        }
        // Empty manifest is an explicit phase 0 state. Every published entry must be checked.
        for (Map<String, String> row : table(dir.resolve("goldens.tsv"))) {
            String item = "golden:" + row.get("golden");
            if (!run.ready(row, item, row.get("view"), row.get("scenario"), requirements(row.get("statements")))) continue;
            Output output = run.output(dir, row, row.get("view"), row.get("scenario"), row.get("format"));
            verifyGolden(dir.resolve(row.get("golden")), output.text);
            run.pass(row, item, row.get("view"), row.get("scenario"));
        }
        Files.createDirectories(report.toAbsolutePath().getParent());
        Files.write(report, run.report, StandardCharsets.UTF_8);
        System.out.println("CR-046: " + run.passed + " assertions passed, " + run.pending + " pending; " + report);
    }
    static void checkToken(Run run, Path dir, Map<String, String> row, String view, String scenario, String format, String expected) throws Exception {
        Set<String> requirements = textRequirements(row, format);
        requirements.addAll(scenarioRequirements(scenario));
        if (!run.ready(row, row.get("token"), view, scenario, requirements)) return;
        Output output = run.output(dir, row, view, scenario, format);
        verify(output.text, row.get("token"), expected, row.get("case") + ":" + row.get("token") + ":" + view + ":" + scenario, run.tokens);
        if (row.get("token").equals("TAG48_MARK"))
            equal(true, output.text.contains("TAG48_MARK\udb40\udc41\udb40\udc42"), "REQ-602 tag characters retained");
        run.pass(row, row.get("token"), view, scenario);
    }
    static void verifyOrder(String text, String markers, String context) {
        String normalized = normalize(text);
        int after = 0;
        for (String token : markers.split("\\s*>\\s*")) {
            String marker = normalize(token);
            equal(1, count(text, token), context + ": order marker " + token);
            int position = normalized.indexOf(marker);
            if (position < after) throw new AssertionError(context + ": wrong order at " + token);
            after = position + marker.length();
        }
    }
    public static void verifyGolden(Path file, String actual) throws Exception {
        String golden = Files.readString(file, StandardCharsets.UTF_8);
        if (file.getFileName().toString().endsWith(".md")) {
            golden = imageDestinations(golden);
            actual = imageDestinations(actual);
        }
        equal(normalize(golden), normalize(actual), file.toString());
    }
    private static final Pattern IMAGE = Pattern.compile("!\\[((?:\\\\.|[^\\]\\\\])*)\\]\\([^)]*\\)");
    /**
     * Markdown with each image's destination written {@code …}: WD REQ-703 writes {@code ![alt](…)},
     * leaving the destination to the implementation (docx4j writes a data URI, or a file's name under
     * {@code imageDirPath}), so a golden's isn't compared (CR-046 phase 3).
     */
    public static String imageDestinations(String markdown) {
        return IMAGE.matcher(markdown).replaceAll(m -> Matcher.quoteReplacement("![" + m.group(1) + "](\u2026)"));
    }
    /** The engine: {@link TextsAdapter} unless {@code -Dtextextraction.adapter} names another, or {@code none}. */
    public static Engine engine() throws Exception {
        String name = System.getProperty("textextraction.adapter", TextsAdapter.class.getName());
        return name.isEmpty() || name.equals("none") ? null : (Engine) Class.forName(name).getConstructor().newInstance();
    }
    public static void main(String[] args) throws Exception {
        run(Path.of(args[0]), Path.of(args[1]), engine());
    }
}
