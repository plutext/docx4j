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
package org.docx4j.model.text;

import static org.docx4j.model.text.InspectXml.M;
import static org.docx4j.model.text.InspectXml.V;
import static org.docx4j.model.text.InspectXml.W;
import static org.docx4j.model.text.InspectXml.WP;
import static org.docx4j.model.text.InspectXml.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.w3c.dom.Element;

/**
 * Each finding's extraction address (WD REQ-901): the address of the block that holds its content in
 * the output its reveal names, as docx4j's text model gives it. The package is read through the text
 * model, calling no importer, once for each set of options the findings' reveals name, with a record of the block each
 * paragraph, table, WordArt shape, chart, workbook cell and slide shape went into; a finding's
 * address is then its element's, or its nearest recorded ancestor's (a run's paragraph), or its first
 * recorded descendant's (a text box's first paragraph, a header's first block).
 */
final class InspectAddresses {

	private InspectAddresses() {}

	static List<InspectFinding> findings(WordprocessingMLPackage pkg, InspectRun run, boolean addresses) {
		Map<String, Map<Object, String>> recorded = new HashMap<String, Map<Object, String>>();
		List<InspectFinding> out = new ArrayList<InspectFinding>();
		for (InspectWalk.Found f : run.found) {
			InspectFinding.Location location = new InspectFinding.Location(f.chain, f.part, f.path, f.start, f.endPath, f.end);
			InspectFinding finding = new InspectFinding(f.kind, location, null, f.visible, f.excerpt, f.summary, f.subject, f.needs);
			if (addresses && addressable(f)) {
				// one reading for each reveal: needs the defaults meet ask for no other options
				String key = finding.getReveal().toString();
				Map<Object, String> sources = recorded.get(key);
				if (sources == null) recorded.put(key, sources = record(pkg, options(f.needs), run));
				finding = finding.withAddress(address(f, sources));
			}
			out.add(finding);
		}
		return out;
	}

	/** An extractable finding with a block: not an author or the properties (metadata), nor what needs a capability. */
	private static boolean addressable(InspectWalk.Found f) {
		return !f.needs.contains(InspectFinding.NEVER) && !f.needs.contains(InspectFinding.IMPORTER)
				&& !f.kind.equals(InspectFinding.REVISION_AUTHOR) && !f.kind.equals(InspectFinding.PROPERTIES);
	}

	/** The options a finding's needs name, the defaults otherwise. */
	static TextOptions options(Set<String> needs) {
		TextOptions o = new TextOptions();
		if (needs.contains(InspectFinding.VIEW_MARKUP)) o.view(TextView.MARKUP);
		else if (needs.contains(InspectFinding.VIEW_ORIGINAL)) o.view(TextView.ORIGINAL);
		if (needs.contains(InspectFinding.HEADERS)) o.include(TextOptions.Include.HEADERS);
		if (needs.contains(InspectFinding.FOOTERS)) o.include(TextOptions.Include.FOOTERS);
		if (needs.contains(InspectFinding.GLOSSARY_SCOPE)) o.include(TextOptions.Include.GLOSSARY);
		if (needs.contains(InspectFinding.COMMENTS)) o.comments(true);
		if (needs.contains(InspectFinding.HIDDEN)) o.hidden(true);
		if (needs.contains(InspectFinding.FIELD_CODES)) o.fieldCodes(true);
		if (needs.contains(InspectFinding.TOC)) o.toc(true);
		if (needs.contains(InspectFinding.SURFACE)) o.bindings(TextOptions.Bindings.SURFACE);
		if (needs.contains(InspectFinding.EMBEDDED)) o.embedded(true);
		if (needs.contains(InspectFinding.ALT_TEXT)) o.altText(true);
		if (needs.contains(InspectFinding.METADATA)) o.metadata(true);
		if (needs.contains(InspectFinding.MARKUP_DETAIL)) o.markupDetail(TextOptions.MarkupDetail.AUTHOR_DATE);
		return o;
	}

	/** One reading through the text model, its blocks' sources recorded; the package is not changed (REQ-204). */
	private static Map<Object, String> record(WordprocessingMLPackage pkg, TextOptions options, InspectRun run) {
		Extractor x = new Extractor(pkg, options, Extractor.Mode.VIEW);
		x.sources = new IdentityHashMap<Object, String>();
		x.packages = run.loaded;
		x.noImporter = true;
		try {
			x.extract();
		} catch (RuntimeException e) {
			// what was read before it failed keeps its addresses
		}
		return x.sources;
	}

	private static String address(InspectWalk.Found f, Map<Object, String> sources) {
		if (f.scope != null) {
			for (Element e : f.scope) {
				String a = recorded(f, e, sources);
				if (a != null) return a;
			}
			return null;
		}
		if (f.anchor == null) {
			// a whole part the text model reads as one (a chart)
			Object part = f.store.part(f.part);
			return part == null ? null : sources.get(part);
		}
		String a;
		if (withinParagraph(f)) {
			a = ancestorOrSelf(f, f.anchor, sources);
			if (a == null) a = descendantOrSelf(f, f.anchor, sources);
		} else {
			a = descendantOrSelf(f, f.anchor, sources);
			if (a == null) a = ancestorOrSelf(f, f.anchor, sources);
		}
		return a;
	}

	/** A finding whose block is its paragraph's: a run's, a paragraph's, a field's, a control's content, a picture's alt text. */
	private static boolean withinParagraph(InspectWalk.Found f) {
		Element e = f.anchor;
		return is(e, W, "r") || is(e, M, "r") || is(e, W, "p") || is(e, W, "fldSimple") || is(e, W, "sdtContent")
				|| is(e, WP, "docPr") || is(e, V, "imagedata") || f.kind.startsWith("alt_text.");
	}

	private static String recorded(InspectWalk.Found f, Element e, Map<Object, String> sources) {
		Object o = f.store.jaxb(e);
		return o == null ? null : sources.get(o);
	}

	private static String ancestorOrSelf(InspectWalk.Found f, Element e, Map<Object, String> sources) {
		for (Element a = e; a != null; a = InspectXml.parent(a)) {
			String r = recorded(f, a, sources);
			if (r != null) return r;
		}
		return null;
	}

	private static String descendantOrSelf(InspectWalk.Found f, Element e, Map<Object, String> sources) {
		for (Element d : InspectXml.iter(e)) {
			String r = recorded(f, d, sources);
			if (r != null) return r;
		}
		return null;
	}
}
