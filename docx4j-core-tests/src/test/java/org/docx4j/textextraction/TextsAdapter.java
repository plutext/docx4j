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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.model.text.Group;
import org.docx4j.model.text.HtmlChunkImport;
import org.docx4j.model.text.Inspect;
import org.docx4j.model.text.InspectFinding;
import org.docx4j.model.text.Inspection;
import org.docx4j.model.text.PlainText;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextResult;
import org.docx4j.model.text.TextView;
import org.docx4j.model.text.TextWarning;
import org.docx4j.model.text.Texts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * The phase 0 harness's engine over docx4j's text model (GOLDENS.md, "adapter contract"): a scenario
 * becomes typed options, and only the statements CR-046 has implemented are advertised, so that
 * every other row stays pending.
 */
public class TextsAdapter implements ExtractionConformance.Engine {

	/** The statements CR-046 has implemented, phases 1 to 3 (its section 4), and capability CR-047. */
	static final Set<String> IMPLEMENTED = Collections.unmodifiableSet(new LinkedHashSet<String>(java.util.Arrays.asList(
			"REQ-201", "REQ-202", "REQ-203", "REQ-204",
			"REQ-301", "REQ-302", "REQ-303", "REQ-304", "REQ-305", "REQ-306", "REQ-307",
			"REQ-401", "REQ-402", "REQ-403", "REQ-404", "REQ-405", "REQ-406",
			"REQ-501", "REQ-502", "REQ-503", "REQ-504", "REQ-505", "REQ-506", "REQ-507", "REQ-508", "REQ-509", "REQ-510",
			"REQ-511", "REQ-512", "REQ-513", "REQ-514", "REQ-515", "REQ-516", "REQ-517", "REQ-518", "REQ-520",
			"REQ-601", "REQ-602", "REQ-603", "REQ-604", "REQ-605", "REQ-606",
			"REQ-701", "REQ-702", "REQ-703", "REQ-704", "REQ-705", "REQ-706", "REQ-707", "REQ-708", "REQ-709",
			"REQ-801", "REQ-802", "REQ-1001", "REQ-1202", "REQ-1203",
			// the chart's description, which case 34's axis title needs
			"CR-047",
			// the inspector (CR-050 phase 1; REQ-903's counts in every result, phase 2), and the suite's check of it
			"REQ-901", "REQ-902", "REQ-903", "REQ-904", "REQ-905", "REQ-906", "REQ-907", "REQ-1204")));

	private final Map<Path, WordprocessingMLPackage> packages = new HashMap<Path, WordprocessingMLPackage>();
	private final Map<Path, Inspection> inspections = new HashMap<Path, Inspection>();

	@Override
	public Set<String> implementedStatements() {
		Set<String> implemented = new LinkedHashSet<String>(IMPLEMENTED);
		// the "html reader" scenarios are the class path's: this run can check one of them
		implemented.add(HtmlChunkImport.readerPresent() ? ExtractionConformance.HTML_READER_PRESENT : ExtractionConformance.HTML_READER_ABSENT);
		return implemented;
	}

	@Override
	public ExtractionConformance.Output extract(Path fixture, String view, String scenario, String format) throws Exception {
		if (!"txt".equals(format) && !"md".equals(format)) throw new UnsupportedOperationException("format " + format);
		WordprocessingMLPackage pkg = packages.get(fixture);
		if (pkg == null) packages.put(fixture, pkg = WordprocessingMLPackage.load(fixture.toFile()));
		TextOptions options = options(view, scenario);
		TextResult result = Texts.of(pkg, options);
		Map<String, String> findings = new LinkedHashMap<String, String>();
		for (TextWarning w : result.warnings()) {
			String selector = w.getSelector() == null ? "" : w.getSelector();
			String key = "warning\t" + w.getKind() + "\t" + selector;
			findings.merge(key, "1", (a, b) -> String.valueOf(Integer.parseInt(a) + 1));
		}
		// the inspection's findings by kind and selector, for the inspect rows (an inspection is the package's, whatever the view)
		Map<String, byte[]> entries = ExtractionFixtureChecks.entries(Files.readAllBytes(fixture));
		for (InspectFinding f : inspection(fixture).findings()) {
			findings.merge("inspect\t" + f.getKind() + "\t" + selector(entries, f), "1", (a, b) -> String.valueOf(Integer.parseInt(a) + 1));
		}
		// the properties (core, app, custom) by their own names, as the findings table names them; the inspection
		// counts are checked apart (counts())
		for (Map.Entry<String, Object> e : result.metadata().entrySet()) {
			if (e.getKey().equals(TextResult.INSPECT)) continue;
			if (e.getValue() instanceof Map) {
				for (Map.Entry<?, ?> p : ((Map<?, ?>) e.getValue()).entrySet()) findings.put("metadata\t" + p.getKey() + "\t", String.valueOf(p.getValue()));
			} else {
				findings.put("metadata\t" + e.getKey() + "\t", String.valueOf(e.getValue()));
			}
		}
		String text = "md".equals(format) ? new org.docx4j.markdown.MarkdownExporter().export(result, pkg) : PlainText.of(result);
		return new ExtractionConformance.Output(text, findings);
	}

	@Override
	public Map<String, String> counts(Path fixture, String output) throws Exception {
		WordprocessingMLPackage pkg = packages.get(fixture);
		if (pkg == null) packages.put(fixture, pkg = WordprocessingMLPackage.load(fixture.toFile()));
		TextResult result = Texts.of(pkg, outputOptions(output));
		Inspection.Counts c = result.counts(outputFormat(output));
		Map<String, String> out = new LinkedHashMap<String, String>();
		for (Group g : Group.values()) out.put(g.id(), String.valueOf(c.get(g)));
		out.put("status", String.valueOf(((Map<?, ?>) result.metadata().get(TextResult.INSPECT)).get("status")));
		return out;
	}

	/** The options of one of {@code expected-inspection.tsv}'s outputs (INSPECTION.md), with docx4j's inspect: true. */
	static TextOptions outputOptions(String output) {
		TextOptions o = new TextOptions().inspect(true);
		switch (output) {
		case "hidden": return o.hidden(true);
		case "embedded": return o.embedded(true);
		case "comments": return o.comments(true);
		default: return o;
		}
	}

	/** The format of one of {@code expected-inspection.tsv}'s outputs. */
	static Inspection.Format outputFormat(String output) {
		return output.equals("markdown") ? Inspection.Format.MARKDOWN : output.equals("blocks") ? Inspection.Format.BLOCKS : Inspection.Format.PLAIN;
	}

	@Override
	public ExtractionConformance.Inspection inspect(Path fixture) throws Exception {
		Inspection i = inspection(fixture);
		List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
		for (InspectFinding f : i.findings()) rows.add(row(f));
		List<String> skipped = new ArrayList<String>(), undecided = new ArrayList<String>();
		for (Inspection.Entry e : i.skipped()) skipped.add(e.toString());
		for (Inspection.Entry e : i.undecided()) undecided.add(e.toString());
		return new ExtractionConformance.Inspection(rows, i.status().name(), skipped, undecided, i.limitations());
	}

	private Inspection inspection(Path fixture) throws Exception {
		Inspection i = inspections.get(fixture);
		if (i == null) inspections.put(fixture, i = Inspect.of(WordprocessingMLPackage.load(fixture.toFile())));
		return i;
	}

	/** A finding as {@code expected-inspection.tsv} writes it: its location, address, visibility, reveal, its group under each output, its excerpt's length. */
	static Map<String, String> row(InspectFinding f) {
		Map<String, String> row = new LinkedHashMap<String, String>();
		InspectFinding.Location l = f.getLocation();
		row.put("kind", f.getKind());
		row.put("chain", String.join(">", l.getChain()));
		row.put("part", l.getPart());
		row.put("path", l.getPath());
		row.put("start", l.getStart() == null ? "" : l.getStart().toString());
		row.put("end_path", l.getEndPath());
		row.put("end", l.getEnd() == null ? "" : l.getEnd().toString());
		row.put("address", f.getAddress() == null ? "" : f.getAddress());
		row.put("visible", f.getVisible().id());
		row.put("reveal", f.getReveal().toString());
		// the class path's: a chunk's hidden text is in an output only with an HTML reader (CR-053 phase 3)
		Set<Inspection.Capability> capabilities = HtmlChunkImport.readerPresent() ? EnumSet.of(Inspection.Capability.HTML_READER)
				: EnumSet.noneOf(Inspection.Capability.class);
		for (String output : ExtractionInspection.OUTPUTS) row.put(output, f.group(outputOptions(output), outputFormat(output), capabilities).id());
		row.put("excerpt_length", String.valueOf(f.getExcerptLength()));
		row.put("excerpt_cut", String.valueOf(f.isExcerptCut()));
		row.put("parsed", f.getParsed() == null ? "" : f.getParsed());
		return row;
	}

	private static final Pattern TOKEN = Pattern.compile("[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)+");

	/**
	 * A finding's selector in {@code expected-findings.tsv}, as {@code inspect_oracle.py} chooses it: what it
	 * names (a target, an author, a font); for a character, text or font finding in a run, the marker token
	 * its range starts in; else a token in its excerpt, then in its paragraph; its excerpt when short; its part.
	 */
	static String selector(Map<String, byte[]> entries, InspectFinding f) throws Exception {
		if (f.getSubject() != null) return f.getSubject();
		InspectFinding.Location l = f.getLocation();
		String kind = f.getKind();
		boolean chars = kind.startsWith("chars.");
		if (l.getStart() != null && !l.getPath().isEmpty() && (chars || kind.startsWith("text.") || kind.startsWith("font.")) && l.getChain().isEmpty()) {
			org.w3c.dom.Element r = ExtractionInspection.resolve(ExtractionFixtureChecks.xml(entries.get(l.getPart().substring(1))), l.getPath());
			if (r != null) {
				int[] text = ExtractionInspection.runText(r).codePoints().toArray();
				int pos = 0;
				while (pos < text.length) {
					int end = pos;
					boolean space = isSpace(text[pos]);
					while (end < text.length && isSpace(text[end]) == space) end++;
					if (!space && pos <= l.getStart() && l.getStart() < end) {
						int stop = end;
						while (chars && stop > pos && of(kind, text[stop - 1])) stop--;
						String word = new String(text, pos, end - pos);
						String stripped = new String(text, pos, stop - pos);
						String candidate = chars ? stripped : word;
						if (TOKEN.matcher(candidate).lookingAt()) return candidate;
					}
					pos = end;
				}
				Matcher m = TOKEN.matcher(f.getExcerpt());
				if (m.find()) return m.group();
				for (org.w3c.dom.Node a = r.getParentNode(); a instanceof org.w3c.dom.Element; a = a.getParentNode()) {
					if (!"p".equals(a.getLocalName()) || !ExtractionInspection.W.equals(a.getNamespaceURI())) continue;
					StringBuilder sb = new StringBuilder();
					org.w3c.dom.NodeList ts = ((org.w3c.dom.Element) a).getElementsByTagNameNS(ExtractionInspection.W, "t");
					for (int i = 0; i < ts.getLength(); i++) sb.append(ts.item(i).getTextContent());
					m = TOKEN.matcher(sb);
					if (m.find()) return m.group();
					break;
				}
			}
		}
		Matcher m = TOKEN.matcher(f.getExcerpt());
		if (m.find()) return m.group();
		String e = f.getExcerpt();
		if (!e.isEmpty() && e.codePointCount(0, e.length()) <= 80 && !e.contains("\n")) return e.strip();
		return (l.getChain().isEmpty() ? "" : String.join(">", l.getChain()) + ">") + l.getPart();
	}

	/** Python's {@code str.isspace}. */
	private static boolean isSpace(int c) {
		return Character.isSpaceChar(c) || c >= 0x09 && c <= 0x0D || c >= 0x1C && c <= 0x1F || c == 0x85;
	}

	/** Whether a character is of a character kind's class. */
	private static boolean of(String kind, int c) {
		switch (kind) {
		case "chars.zero_width": return c >= 0x200B && c <= 0x200F || c == 0x2060 || c == 0xFEFF;
		case "chars.tag": return c >= 0xE0000 && c <= 0xE007F;
		case "chars.bidi": return c >= 0x202A && c <= 0x202E || c >= 0x2066 && c <= 0x2069;
		default: return c >= 0xE000 && c <= 0xF8FF || c >= 0xF0000 && c <= 0xFFFFD || c >= 0x100000 && c <= 0x10FFFD;
		}
	}

	/**
	 * A scenario's options: {@code default} (and {@code markdown}, the default read as Markdown), or WD
	 * options as {@code name: value}, several {@code "; "} apart ({@code embedded: true; hidden: true}).
	 */
	static TextOptions options(String view, String scenario) {
		// this engine claims the inspector class (REQ-903), so it extracts as one: docx4j's inspect: true (CR-050 D9)
		TextOptions options = TextOptions.of(TextView.of(view)).inspect(true);
		if (scenario == null || scenario.equals("default") || scenario.equals("markdown") || scenario.startsWith("html reader")) return options;
		for (String option : scenario.split(";")) {
			int colon = option.indexOf(':');
			if (colon < 0) throw new IllegalArgumentException("Unknown scenario: " + scenario);
			options.set(option.substring(0, colon).trim(), option.substring(colon + 1).trim());
		}
		return options;
	}
}
