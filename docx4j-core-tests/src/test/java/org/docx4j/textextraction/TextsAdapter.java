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

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
			"REQ-511", "REQ-512", "REQ-513", "REQ-514", "REQ-515", "REQ-516", "REQ-517", "REQ-518",
			"REQ-601", "REQ-602", "REQ-603", "REQ-604", "REQ-605", "REQ-606",
			"REQ-701", "REQ-702", "REQ-703", "REQ-704", "REQ-705", "REQ-706", "REQ-707", "REQ-708", "REQ-709",
			"REQ-801", "REQ-802", "REQ-1001", "REQ-1202", "REQ-1203",
			// the chart's description, which case 34's axis title needs
			"CR-047")));

	private final Map<Path, WordprocessingMLPackage> packages = new HashMap<Path, WordprocessingMLPackage>();

	@Override
	public Set<String> implementedStatements() {
		Set<String> implemented = new LinkedHashSet<String>(IMPLEMENTED);
		// the "xhtml importer" scenarios are the class path's: this run can check one of them
		implemented.add(xhtmlImporter() ? ExtractionConformance.XHTML_IMPORTER_PRESENT : ExtractionConformance.XHTML_IMPORTER_ABSENT);
		return implemented;
	}

	private static boolean xhtmlImporter() {
		try {
			Class.forName("org.docx4j.convert.in.xhtml.XHTMLImporterImpl");
			return true;
		} catch (ClassNotFoundException | LinkageError e) {
			return false;
		}
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
		// the properties (core, app, custom) by their own names, as the findings table names them
		for (Map.Entry<String, Object> e : result.metadata().entrySet()) {
			if (e.getValue() instanceof Map) {
				for (Map.Entry<?, ?> p : ((Map<?, ?>) e.getValue()).entrySet()) findings.put("metadata\t" + p.getKey() + "\t", String.valueOf(p.getValue()));
			} else {
				findings.put("metadata\t" + e.getKey() + "\t", String.valueOf(e.getValue()));
			}
		}
		String text = "md".equals(format) ? new org.docx4j.markdown.MarkdownExporter().export(result, pkg) : PlainText.of(result);
		return new ExtractionConformance.Output(text, findings);
	}

	/**
	 * A scenario's options: {@code default} (and {@code markdown}, the default read as Markdown), or WD
	 * options as {@code name: value}, several {@code "; "} apart ({@code embedded: true; hidden: true}).
	 */
	static TextOptions options(String view, String scenario) {
		TextOptions options = TextOptions.of(TextView.of(view));
		if (scenario == null || scenario.equals("default") || scenario.equals("markdown") || scenario.startsWith("xhtml importer")) return options;
		for (String option : scenario.split(";")) {
			int colon = option.indexOf(':');
			if (colon < 0) throw new IllegalArgumentException("Unknown scenario: " + scenario);
			options.set(option.substring(0, colon).trim(), option.substring(colon + 1).trim());
		}
		return options;
	}
}
