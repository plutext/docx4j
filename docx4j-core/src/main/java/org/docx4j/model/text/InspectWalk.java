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

import static org.docx4j.model.text.InspectXml.A;
import static org.docx4j.model.text.InspectXml.C;
import static org.docx4j.model.text.InspectXml.DGM;
import static org.docx4j.model.text.InspectXml.DSP;
import static org.docx4j.model.text.InspectXml.M;
import static org.docx4j.model.text.InspectXml.MC;
import static org.docx4j.model.text.InspectXml.O;
import static org.docx4j.model.text.InspectXml.P;
import static org.docx4j.model.text.InspectXml.PIC;
import static org.docx4j.model.text.InspectXml.R;
import static org.docx4j.model.text.InspectXml.V;
import static org.docx4j.model.text.InspectXml.W;
import static org.docx4j.model.text.InspectXml.W14;
import static org.docx4j.model.text.InspectXml.W15;
import static org.docx4j.model.text.InspectXml.WP;
import static org.docx4j.model.text.InspectXml.WPS;
import static org.docx4j.model.text.InspectXml.X;
import static org.docx4j.model.text.InspectXml.attr;
import static org.docx4j.model.text.InspectXml.child;
import static org.docx4j.model.text.InspectXml.children;
import static org.docx4j.model.text.InspectXml.is;
import static org.docx4j.model.text.InspectXml.isOn;
import static org.docx4j.model.text.InspectXml.iter;
import static org.docx4j.model.text.InspectXml.on;
import static org.docx4j.model.text.InspectXml.parent;
import static org.docx4j.model.text.InspectXml.w;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.docx4j.jaxb.McPurpose;
import org.docx4j.jaxb.McSelection;
import org.docx4j.mce.AlternateContent;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The walk of one package (CR-050 section 2.1; the rules of {@code text-extraction/INSPECTION.md},
 * whose reference reading is the suite's {@code support/inspect_oracle.py}): the stored stories of a
 * WordprocessingML package before any selection, or an embedded workbook's or presentation's kinds,
 * then the package inventory, its chunks and embedded packages inspected one level down.
 */
final class InspectWalk {

	/** A finding as the walk makes it: mutable while runs merge into it, its element kept for its address. */
	static final class Found {
		final String kind;
		final List<String> chain;
		final String part;
		String path;
		Integer start;
		String endPath = "";
		Integer end;
		String excerpt;
		final Set<String> needs;
		Visible visible;
		String summary = "";
		String subject;
		/** The package and element it was found at, for its address. */
		InspectPackage store;
		Element anchor;
		/** Where a finding has no one element (a hidden column's cells), the elements its content is in. */
		List<Element> scope;
		/** A chunk's finding (CR-053 phase 3): the paragraph or table of the inspector's import that holds its text. */
		Object block;
		/** An HTML, XHTML or MHT chunk's {@code altchunk} finding: how it was parsed ({@code html}), or null. */
		String parsed;
		/** What merging compares, the paragraph and the index of the last run merged, the runs' texts. */
		List<Object> attrs;
		Element para;
		int lastIndex;
		final List<int[]> texts = new ArrayList<int[]>();

		Found(String kind, List<String> chain, String part, String path, String excerpt, Set<String> needs) {
			this.kind = kind;
			this.chain = chain;
			this.part = part;
			this.path = path == null ? "" : path;
			this.excerpt = excerpt == null ? "" : excerpt;
			this.needs = new LinkedHashSet<String>(needs);
			this.visible = VISIBLE.get(kind);
		}
	}

	static final Map<String, Visible> VISIBLE = new HashMap<String, Visible>();
	static {
		String[] no = { "chars.zero_width", "chars.tag", "chars.bidi", "chars.private_use", "font.glyph_blank", "shape.zero_size",
				"shape.off_page", "shape.hidden", "smartart.data", "text.hidden", "paragraph.hidden", "revision.deleted",
				"revision.moved_from", "mce.branch", "story.unused", "sdt.stored_surface", "field.code", "glossary",
				"alt_text.description", "alt_text.title", "alt_text.name", "embedded.sheet_hidden", "embedded.sheet_very_hidden",
				"embedded.slide_hidden", "embedded.row_hidden", "embedded.column_hidden", "embedded.notes", "properties",
				"doc_vars", "custom_xml", "people", "revision.author", "rsids" };
		String[] differently = { "text.near_background", "text.tiny", "font.glyph_mismatch", "paragraph.collapsed" };
		String[] yes = { "hyperlink.web", "comment", "header_footer", "watermark", "note", "sdt.placeholder", "chart.cached",
				"text.web_hidden", "altchunk", "object.embedded", "ink", "image.svg_text", "image.metafile_text" };
		String[] none = { "field.external", "settings.update_fields", "fields.auto_update", "fields.print_update", "relationship.external", "part.macro", "part.activex",
				"altchunk.script", "altchunk.external_ref", "font.embedded", "part.unknown", InspectRanges.KIND };
		for (String k : no) VISIBLE.put(k, Visible.NO);
		for (String k : differently) VISIBLE.put(k, Visible.DIFFERENTLY);
		for (String k : yes) VISIBLE.put(k, Visible.YES);
		for (String k : none) VISIBLE.put(k, Visible.NOT_CONTENT);
	}

	/** Word's symbol fonts and the icon fonts that keep their glyphs in the private-use area (D17, the list alone). */
	private static final Set<String> SYMBOL_FONTS = new HashSet<String>(Arrays.asList("symbol", "webdings", "wingdings",
			"wingdings 2", "wingdings 3", "marlett", "mt extra", "bookshelf symbol 7", "ms outlook", "ms reference specialty",
			"segoe mdl2 assets", "segoe fluent icons"));
	private static final Map<String, String> HIGHLIGHT = new HashMap<String, String>();
	static {
		String[] h = { "black", "000000", "blue", "0000FF", "cyan", "00FFFF", "green", "00FF00", "magenta", "FF00FF", "red",
				"FF0000", "yellow", "FFFF00", "white", "FFFFFF", "darkBlue", "000080", "darkCyan", "008080", "darkGreen", "008000",
				"darkMagenta", "800080", "darkRed", "800000", "darkYellow", "808000", "darkGray", "808080", "lightGray", "C0C0C0" };
		for (int i = 0; i < h.length; i += 2) HIGHLIGHT.put(h[i], h[i + 1]);
	}
	/** The content-type families of a known part (INSPECTION.md, {@code part.unknown}). */
	private static final String[] KNOWN_TYPES = { "application/vnd.openxmlformats-", "application/vnd.ms-", "image/", "audio/",
			"video/", "text/", "message/rfc822", "application/xml", "application/xhtml+xml", "application/rtf", "application/inkml+xml", "font/",
			"application/x-font", "application/pdf", "application/vnd.oasis.opendocument." };
	private static final String DOCX_CHUNK = "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml";
	private static final long EMU_PER_PT = 12700;
	/** D19 (b): a name of this form ("Text Box 3", "Picture 7") is no finding unless extraction carries it. */
	private static final Pattern DEFAULT_NAME = Pattern.compile("[^0-9]+ [0-9]+");
	private static final String WHITE = "FFFFFF";
	private static final String[] REVISION_MARKS = { W, "ins", W, "del", W, "moveFrom", W, "moveTo", W, "rPrChange", W, "pPrChange",
			W, "sectPrChange", W, "tblPrChange", W, "trPrChange", W, "tcPrChange", W, "numberingChange", W, "cellIns", W, "cellDel",
			W, "cellMerge" };
	private static final List<String> EXTERNAL_FIELDS = Arrays.asList("INCLUDETEXT", "INCLUDEPICTURE", "LINK", "IMPORT", "DDE", "DDEAUTO");
	/**
	 * The fields whose presence makes Word's open update the document (CR-067 section 14.7, measured 2026-10-29): Word
	 * refreshes them and recomputes every IF with literal or auto-updating operands, writing the results on save, so a
	 * Word user sees text the stored results do not hold.
	 */
	private static final List<String> OPEN_TRIGGER_FIELDS = Arrays.asList("DATE", "TIME", "SAVEDATE");
	/** The fields Word's PDF export (a print) updates before the same pass, so Word's PDF differs from the stored text. */
	private static final List<String> PRINT_TRIGGER_FIELDS = Arrays.asList("PAGE", "NUMPAGES", "SECTION", "SECTIONPAGES", "PRINTDATE", "REF", "SEQ");

	private final InspectPackage pkg;
	private final InspectRun run;
	private final List<String> chain;
	private final int depth;
	private final Set<String> baseNeeds;
	private final String main;
	private final Styles styles;

	/** The story being walked. */
	private String part;
	private String storyKind;
	private Set<Element> tocRuns = Collections.emptySet();
	/** Bound controls whose surface is not their value, in the main document. */
	private final Set<Element> stale = Collections.newSetFromMap(new IdentityHashMap<Element, Boolean>());
	private final List<Element> sections = new ArrayList<Element>();
	private String pageColour;
	private boolean pageShown;
	/** Embedded faces by font name (lower case) and style, and those some run is drawn in, with the characters drawn. */
	private final Map<String, InspectFace> faces = new HashMap<String, InspectFace>();
	private final Map<InspectFace, String> facePart = new IdentityHashMap<InspectFace, String>();
	private final Map<String, Object[]> usedFaces = new LinkedHashMap<String, Object[]>();
	/** Each revision author's first mark (part, element) and the dates of their marks. */
	private final Map<String, Object[]> authors = new LinkedHashMap<String, Object[]>();
	private final Map<Element, Boolean> selected = new IdentityHashMap<Element, Boolean>();
	private final Map<Element, List<Element>> runsWithText = new IdentityHashMap<Element, List<Element>>();
	/** The image parts met through a picture (D21): the part's finding, or null where it gave none, so each is read once. */
	private final Map<String, Found> images = new HashMap<String, Found>();
	/** For an image finding: the pictures showing the part so far, its lines, and the first picture's raster copy. */
	private final Map<String, Object[]> imageInfo = new HashMap<String, Object[]>();

	InspectWalk(InspectPackage pkg, InspectRun run, int depth, Set<String> baseNeeds) {
		this.pkg = pkg;
		this.run = run;
		this.chain = pkg.chain;
		this.depth = depth;
		this.baseNeeds = new LinkedHashSet<String>(baseNeeds);
		this.main = pkg.main();
		List<String> st = main == null ? Collections.<String>emptyList() : pkg.byType(main, "/styles");
		this.styles = new Styles(st.isEmpty() ? null : pkg.xml(st.get(0)));
	}

	private Found add(Found f) {
		f.needs.addAll(baseNeeds);
		f.store = pkg;
		run.add(f);
		return f;
	}

	private Found found(String kind, String part, String path, String excerpt, Set<String> needs, Element anchor) {
		Found f = new Found(kind, chain, part, path, excerpt, needs);
		f.anchor = anchor;
		return f;
	}

	private static Set<String> needs(String... needs) {
		return new LinkedHashSet<String>(Arrays.asList(needs));
	}

	private static Set<String> plus(Set<String> a, String... more) {
		Set<String> s = new LinkedHashSet<String>(a);
		s.addAll(Arrays.asList(more));
		return s;
	}

	private String path(Element e) {
		return pkg.path(part, e);
	}

	private String path(String inPart, Element e) {
		return pkg.path(inPart, e);
	}

	private String loc(String inPart, String path) {
		return pkg.location(inPart, path);
	}

	// --- the package --------------------------------------------------------------------------------

	void run() {
		// a global stop leaves this package on the stack, so that what it has not inspected is listed
		run.walking.addLast(pkg);
		if (main != null) {
			String ctype = pkg.contentType(main);
			ctype = ctype == null ? "" : ctype;
			boolean word = ctype.contains("wordprocessingml") || ctype.contains("ms-word");
			activeFirst(word);
			if (word) word();
			else if (ctype.contains("spreadsheetml")) workbook(main);
			else if (ctype.contains("presentationml")) presentation(main);
			inventory();
		}
		run.walking.removeLast();
	}

	private void word() {
		Element doc = pkg.xml(main);
		if (doc == null) return;
		List<String> st = pkg.byType(main, "/settings");
		Element settings = st.isEmpty() ? null : pkg.xml(st.get(0));
		if (settings != null && child(settings, W, "displayBackgroundShape") != null) pageShown = isOn(child(settings, W, "displayBackgroundShape"));
		Element bg = child(doc, W, "background");
		if (bg != null && w(bg, "color") != null && !w(bg, "color").equalsIgnoreCase("auto")) pageColour = w(bg, "color").toUpperCase(Locale.ROOT);
		loadFaces();
		loadBindings();
		// the sections, and the header and footer parts they use
		Map<String, String> used = new LinkedHashMap<String, String>();
		for (Element sp : iter(doc, W, "sectPr")) {
			sections.add(sp);
			for (Element ref : refs(sp)) {
				String t = pkg.target(main, attr(ref, R, "id"));
				if (t != null) used.put(t, is(ref, W, "headerReference") ? "header" : "footer");
			}
		}
		Map<String, Set<String>> reach = new HashMap<String, Set<String>>();
		Map<String, int[]> reachRank = new HashMap<String, int[]>();
		Set<String> commentRefs = new HashSet<String>();
		collectReferences(main, doc, "main", reach, reachRank, commentRefs);
		List<String> headersFooters = new ArrayList<String>(pkg.byType(main, "/header"));
		headersFooters.addAll(pkg.byType(main, "/footer"));
		for (String p : headersFooters) {
			Element x = pkg.xml(p);
			if (x != null) collectReferences(p, x, used.get(p), reach, reachRank, commentRefs);
		}
		// a comment anchored in a note is anchored (phase 2)
		for (String p : pkg.byType(main, "/footnotes")) if (pkg.xml(p) != null) collectReferences(p, pkg.xml(p), null, reach, reachRank, commentRefs);
		for (String p : pkg.byType(main, "/endnotes")) if (pkg.xml(p) != null) collectReferences(p, pkg.xml(p), null, reach, reachRank, commentRefs);

		story(main, doc, needs(), "main");
		pkg.done.add(main);
		for (String p : new TreeSet<String>(headersFooters)) {
			Element x = pkg.xml(p);
			if (x == null) continue;
			if (used.containsKey(p)) {
				Found f = add(found("header_footer", p, "", text(x), needs("header".equals(used.get(p)) ? InspectFinding.HEADERS : InspectFinding.FOOTERS), x));
				story(p, x, new LinkedHashSet<String>(f.needs), used.get(p));
			} else {
				add(found("story.unused", p, "", text(x), needs(InspectFinding.NEVER), x));
				story(p, x, needs(InspectFinding.NEVER), "unused");
			}
			pkg.done.add(p);
		}
		for (String kind : new String[] { "footnote", "endnote" }) {
			for (String p : pkg.byType(main, "/" + kind + "s")) {
				Element x = pkg.xml(p);
				if (x == null) continue;
				part = p;
				for (Element note : children(x, W, kind)) {
					String t = w(note, "type");
					if (t != null && !t.equals("normal")) continue;
					String id = w(note, "id");
					Set<String> best = reach.get(kind + " " + id);
					if (best == null) {
						add(found("story.unused", p, path(p, note), text(note), needs(InspectFinding.NEVER), note));
						story(p, note, needs(InspectFinding.NEVER), "unused");
					} else {
						// read through its best reference: what reading that reference needs
						Set<String> n = needs(InspectFinding.NOTES);
						n.addAll(best);
						add(found("note", p, path(p, note), text(note), n, note));
						story(p, note, n, "note");
					}
				}
				pkg.done.add(p);
			}
		}
		for (String p : pkg.byType(main, "/comments")) {
			Element x = pkg.xml(p);
			if (x == null) continue;
			for (Element c : children(x, W, "comment")) {
				if (commentRefs.contains(w(c, "id"))) {
					Found f = add(found("comment", p, path(p, c), text(c), needs(InspectFinding.COMMENTS), c));
					f.summary = w(c, "author") == null ? "" : w(c, "author");
					story(p, c, needs(InspectFinding.COMMENTS), "comment");
				} else {
					add(found("story.unused", p, path(p, c), text(c), needs(InspectFinding.NEVER), c));
					story(p, c, needs(InspectFinding.NEVER), "unused");
				}
			}
			pkg.done.add(p);
		}
		for (String p : pkg.byType(main, "/glossaryDocument")) {
			Element x = pkg.xml(p);
			if (x == null) continue;
			for (Element dp : iter(x, W, "docPart")) {
				Element body = child(dp, W, "docPartBody");
				add(found("glossary", p, path(p, dp), text(body), needs(InspectFinding.GLOSSARY_SCOPE), dp));
				if (body != null) story(p, body, needs(InspectFinding.GLOSSARY_SCOPE), "glossary");
			}
			pkg.done.add(p);
		}
		for (Map.Entry<String, Object[]> a : authors.entrySet()) {
			String p = (String) a.getValue()[0];
			Element first = (Element) a.getValue()[1];
			@SuppressWarnings("unchecked")
			List<String> dates = (List<String>) a.getValue()[2];
			Found f = add(found("revision.author", p, path(p, first), a.getKey(), needs(InspectFinding.VIEW_MARKUP, InspectFinding.MARKUP_DETAIL), first));
			f.subject = a.getKey();
			if (!dates.isEmpty()) f.summary = Collections.min(dates) + " to " + Collections.max(dates);
		}
		if (settings != null) {
			String sp = st.get(0);
			Element dv = child(settings, W, "docVars");
			if (dv != null && !children(dv).isEmpty()) {
				List<String> vals = new ArrayList<String>();
				for (Element d : children(dv, W, "docVar")) vals.add(w(d, "val") == null ? "" : w(d, "val"));
				add(found("doc_vars", sp, path(sp, dv), String.join("\n", vals), needs(InspectFinding.NEVER), dv));
			}
			place(sp + "#updateFields"); // found first (activeFirst)
			Element rs = child(settings, W, "rsids");
			if (rs != null) {
				Found f = add(found("rsids", sp, path(sp, rs), "", needs(InspectFinding.NEVER), rs));
				f.summary = String.valueOf(children(rs).size());
			}
		}
		for (Map.Entry<String, Object[]> e : usedFaces.entrySet()) {
			Found f = add(found("font.embedded", (String) e.getValue()[0], "", "", needs(InspectFinding.NEVER), null));
			f.subject = (String) e.getValue()[2];
			int n = (Integer) e.getValue()[1];
			f.summary = f.subject + ": " + n + (n == 1 ? " character" : " characters");
		}
		walkRanges();
	}

	/**
	 * CR-066 D6 (its section 10): every integer attribute outside the width Word stores it in, in every
	 * WordprocessingML XML part of the package (the main document first, then the rest by part name: the
	 * styles, settings, numbering, headers, footers, notes, comments, the glossary), every branch of an
	 * {@code mc:AlternateContent} included; one {@code value.out_of_range} finding per attribute occurrence (a
	 * run's property merging into the same finding in the run before it, as every run kind does, so that run
	 * splitting does not count), its summary the ports' shared fields, its group info in every output (it is not
	 * content).
	 */
	private void walkRanges() {
		List<String> parts = new ArrayList<String>();
		parts.add(main);
		for (String p : pkg.partNames()) {
			if (p.equals(main)) continue;
			String ctype = pkg.contentType(p);
			if (ctype == null || !ctype.endsWith("+xml")) continue;
			if (!ctype.contains("wordprocessingml") && !ctype.contains("ms-word")) continue;
			parts.add(p);
		}
		for (String p : parts) {
			Element root = pkg.xml(p);
			if (root == null) continue;
			Element lastRun = null;
			String lastSummary = null;
			for (Object[] f : InspectRanges.findings(root)) {
				Element e = (Element) f[0];
				String summary = (String) f[1];
				// a run's property in the run after one with the same finding is the same finding (CR-050 section 2.4:
				// run splitting does not count), as for every run kind; a paragraph's or a style's never merges
				Element run = ancestor(e, W, "r");
				if (run != null && lastRun != null && summary.equals(lastSummary) && adjacentRuns(lastRun, run)) continue;
				lastRun = run;
				lastSummary = run == null ? null : summary;
				Found found = add(found(InspectRanges.KIND, p, path(p, e), "", needs(InspectFinding.NEVER), e));
				found.summary = summary;
				// its subject, the harness's selector: the part, the attribute and the value (unique per fixture case)
				String attribute = summary.substring("attribute=".length(), summary.indexOf(';'));
				int v = summary.indexOf("; value=") + "; value=".length();
				found.subject = (chain.isEmpty() ? "" : String.join(">", chain) + ">") + p + " " + attribute + "=" + summary.substring(v, summary.indexOf(';', v));
			}
		}
	}

	/** The nearest ancestor-or-self of the name, or null. */
	private static Element ancestor(Element e, String ns, String local) {
		for (Element a = e; a != null; a = parent(a)) if (is(a, ns, local)) return a;
		return null;
	}

	/** Two runs of one parent with no run between them. */
	private static boolean adjacentRuns(Element a, Element b) {
		if (a == b || parent(a) == null || parent(a) != parent(b)) return false;
		for (Node n = a.getNextSibling(); n != null; n = n.getNextSibling()) {
			if (n == b) return true;
			if (n.getNodeType() == Node.ELEMENT_NODE && is(n, W, "r")) return false;
		}
		return false;
	}

	private static List<Element> refs(Element sectPr) {
		List<Element> out = new ArrayList<Element>(children(sectPr, W, "headerReference"));
		out.addAll(children(sectPr, W, "footerReference"));
		return out;
	}

	/**
	 * A story's note and comment references (phase 2). The text model reads a note through a reference
	 * it reads: in the main document, or in a header or footer a section uses ({@code story} null for
	 * one none does, whose references reach no note), where the reference itself is read. Each reference
	 * needs what reading it needs: its story's option, its revision's view, a text box, hidden text, a
	 * table of contents, a stale control's surface; one in an unselected branch reaches nothing.
	 * {@code reach} keeps, for each note, its best reference's needs ({@link #reachRank}).
	 */
	private void collectReferences(String p, Element root, String story, Map<String, Set<String>> reach, Map<String, int[]> ranks,
			Set<String> comments) {
		if (story != null) {
			String savedPart = part;
			Set<Element> savedToc = tocRuns;
			part = p;
			tocRuns = tocSpans(root);
			Set<String> base = "header".equals(story) ? needs(InspectFinding.HEADERS) : "footer".equals(story) ? needs(InspectFinding.FOOTERS) : needs();
			for (Element e : iter(root, W, "footnoteReference", W, "endnoteReference")) {
				Set<String> n = contextNeeds(e, root, base);
				if (n.contains(InspectFinding.NEVER)) continue;
				Element r = InspectXml.ancestor(e, W, "r");
				if (r != null && Boolean.TRUE.equals(on(prop(layers(paragraphOf(r), r), "vanish")))) n.add(InspectFinding.HIDDEN);
				String key = (is(e, W, "footnoteReference") ? "footnote" : "endnote") + " " + w(e, "id");
				int[] rank = reachRank(n, story);
				int[] was = ranks.get(key);
				if (was == null || Arrays.compare(rank, was) < 0) {
					ranks.put(key, rank);
					reach.put(key, n);
				}
			}
			part = savedPart;
			tocRuns = savedToc;
		}
		for (Element e : iter(root, W, "commentReference", W, "commentRangeStart")) comments.add(w(e, "id"));
	}

	private static final List<String> STORY_ORDER = Arrays.asList("main", "header", "footer");

	/**
	 * A note's references in order of preference (REQ-905): the fewest options beyond the defaults; then a
	 * live reference before an inserted one, an inserted before a deleted, a deleted before one deleted
	 * inside an insertion; then the main document before a header before a footer; then the fewest needs.
	 * The first met in document order wins a tie.
	 */
	private static int[] reachRank(Set<String> needs, String story) {
		int beyond = 0;
		for (String n : needs) if (!InspectFinding.DEFAULT_ON.contains(n)) beyond++;
		int revision = needs.contains(InspectFinding.VIEW_MARKUP) ? 3 : needs.contains(InspectFinding.VIEW_ORIGINAL) ? 2
				: needs.contains(InspectFinding.NOT_ORIGINAL) ? 1 : 0;
		return new int[] { beyond, revision, STORY_ORDER.indexOf(story), needs.size() };
	}

	// --- text ---------------------------------------------------------------------------------------

	/** A run's stored text (INSPECTION.md): {@code w:t}, {@code w:delText}, {@code w:instrText}, {@code w:delInstrText}, a code point per {@code w:sym}; {@code m:t}. */
	static String runText(Element r) {
		return text(r, true);
	}

	/** A run's display text: its stored text but a field's instruction. */
	static String displayText(Element r) {
		return text(r, false);
	}

	private static String text(Element r, boolean instructions) {
		StringBuilder sb = new StringBuilder();
		for (Node c = r.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (!(c instanceof Element)) continue;
			Element e = (Element) c;
			if (is(e, W, "t") || is(e, W, "delText") || is(e, M, "t") || instructions && (is(e, W, "instrText") || is(e, W, "delInstrText"))) {
				sb.append(e.getTextContent());
			} else if (is(e, W, "sym")) {
				int cp = symbol(e);
				if (cp >= 0) sb.appendCodePoint(cp);
			}
		}
		return sb.toString();
	}

	private static int symbol(Element sym) {
		String c = w(sym, "char");
		try {
			int cp = Integer.parseInt(c.trim(), 16);
			return Character.isValidCodePoint(cp) ? cp : -1;
		} catch (RuntimeException e) {
			return -1;
		}
	}

	private static Element paragraphOf(Element r) {
		return InspectXml.ancestor(r, W, "p", A, "p");
	}

	/** The runs whose nearest paragraph is {@code p}, in document order. */
	private List<Element> ownRunsAll(Element p) {
		List<Element> out = new ArrayList<Element>();
		for (Element r : iter(p, W, "r", M, "r")) if (paragraphOf(r) == p) out.add(r);
		return out;
	}

	private List<Element> ownRuns(Element p) {
		List<Element> out = new ArrayList<Element>();
		for (Element r : ownRunsAll(p)) if (!unselectedBetween(r, p)) out.add(r);
		return out;
	}

	/**
	 * An element's text: each paragraph's display text (selected branches), paragraphs joined by LF; an
	 * element inside a paragraph (a branch, a control's content) its runs' display text.
	 */
	private String text(Element el) {
		if (el == null) return "";
		List<Element> candidates = iter(el, W, "p");
		if (candidates.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (Element t : iter(el, W, "t", W, "delText", M, "t", W, "sym")) {
				if (unselectedBetween(t, el)) continue;
				if (is(t, W, "sym")) {
					int cp = symbol(t);
					if (cp >= 0) sb.appendCodePoint(cp);
				} else {
					sb.append(t.getTextContent());
				}
			}
			return sb.toString();
		}
		List<String> paras = new ArrayList<String>();
		for (Element p : candidates) {
			if (unselectedBetween(p, el)) continue;
			StringBuilder sb = new StringBuilder();
			for (Element r : ownRuns(p)) sb.append(displayText(r));
			paras.add(sb.toString());
		}
		return String.join("\n", paras);
	}

	/** Whether an element lies in a branch its {@code mc:AlternateContent} does not select, below {@code top}. */
	private boolean unselectedBetween(Element el, Element top) {
		for (Element a = parent(el); a != null; a = parent(a)) {
			if (a == top) return false;
			if ((is(a, MC, "Choice") || is(a, MC, "Fallback")) && !selected(a)) return true;
		}
		return false;
	}

	/** Whether the text model selects this branch (REQ-302): its JAXB object's, else by the namespaces in scope. */
	private boolean selected(Element branch) {
		Boolean s = selected.get(branch);
		if (s != null) return s;
		Element ac = parent(branch);
		Object acObject = ac == null ? null : pkg.jaxb(ac);
		Object branchObject = pkg.jaxb(branch);
		if (acObject instanceof AlternateContent && branchObject != null) {
			s = McSelection.selectedBranch(McPurpose.TEXT, (AlternateContent) acObject) == branchObject;
		} else {
			Element chosen = null;
			Set<String> understood = McSelection.understoodNamespaces();
			for (Element c : children(ac, MC, "Choice")) {
				String req = attr(c, null, "Requires");
				if (req == null || req.trim().isEmpty()) continue;
				boolean all = true;
				for (String p : req.trim().split("\\s+")) {
					// the declarations in scope, else docx4j's conventional table, as McSelection reads a Choice
					String uri = c.lookupNamespaceURI(p);
					if (uri == null || uri.isEmpty()) uri = new org.docx4j.jaxb.NamespacePrefixMappings().getNamespaceURI(p);
					if (uri == null || uri.isEmpty() || !understood.contains(uri)) all = false;
				}
				if (all) {
					chosen = c;
					break;
				}
			}
			if (chosen == null) chosen = child(ac, MC, "Fallback");
			if (chosen == null) chosen = child(ac, MC, "Choice");
			s = chosen == branch;
		}
		selected.put(branch, s);
		return s;
	}

	// --- a story ------------------------------------------------------------------------------------

	private void story(String p, Element root, Set<String> needs, String kind) {
		this.part = p;
		this.storyKind = kind;
		this.tocRuns = tocSpans(root);
		walkBranches(p, root, needs);
		walkParagraphs(p, root, needs);
		walkFields(p, root, needs);
		walkDrawings(p, root, needs);
		walkControls(p, root, needs);
		walkRevisions(p, root, needs);
		walkCollapsed(p, root, needs);
	}

	/** The runs of every TOC field, begin to end, its result and nested fields included (REQ-520: {@code toc: true}). */
	private Set<Element> tocSpans(Element root) {
		List<Element> runs = iter(root, W, "r");
		Set<Element> spans = Collections.newSetFromMap(new IdentityHashMap<Element, Boolean>());
		List<Object[]> stack = new ArrayList<Object[]>();
		for (int i = 0; i < runs.size(); i++) {
			for (Element c : children(runs.get(i))) {
				if (is(c, W, "fldChar")) {
					String t = w(c, "fldCharType");
					if ("begin".equals(t)) {
						stack.add(new Object[] { i, new StringBuilder() });
					} else if ("end".equals(t) && !stack.isEmpty()) {
						Object[] f = stack.remove(stack.size() - 1);
						String[] words = f[1].toString().trim().split("\\s+");
						if (words.length > 0 && words[0].toUpperCase(Locale.ROOT).equals("TOC")) spans.addAll(runs.subList((Integer) f[0], i + 1));
					}
				} else if ((is(c, W, "instrText") || is(c, W, "delInstrText")) && !stack.isEmpty()) {
					((StringBuilder) stack.get(stack.size() - 1)[1]).append(c.getTextContent());
				}
			}
		}
		return spans;
	}

	/** What the containers between an element and the story's root need (REQ-905). */
	private Set<String> contextNeeds(Element el, Element top, Set<String> needs) {
		Set<String> n = new LinkedHashSet<String>(needs);
		boolean inserted = false, deleted = false;
		for (Element a = el; a != null; a = parent(a)) {
			if (a == top) break;
			if (is(a, W, "r") && tocRuns.contains(a)) n.add(InspectFinding.TOC);
			if ((is(a, MC, "Choice") || is(a, MC, "Fallback")) && !selected(a)) n.add(InspectFinding.NEVER);
			else if (is(a, W, "ins") || is(a, W, "moveTo")) inserted = true;
			else if (is(a, W, "del") || is(a, W, "moveFrom")) deleted = true;
			else if (is(a, W, "tr")) {
				// a row's own mark: Accept All removes a deleted row, Reject All an inserted one
				Element pr = child(a, W, "trPr");
				if (child(pr, W, "del") != null) deleted = true;
				if (child(pr, W, "ins") != null) inserted = true;
			} else if (is(a, W, "tc")) {
				Element pr = child(a, W, "tcPr");
				if (child(pr, W, "cellDel") != null) deleted = true;
				if (child(pr, W, "cellIns") != null) inserted = true;
			} else if (is(a, W, "sdtContent")) {
				if (stale.contains(parent(a))) n.add(InspectFinding.SURFACE);
				if (isOn(child(child(parent(a), W, "sdtPr"), W, "showingPlcHdr"))) n.add(InspectFinding.PLACEHOLDERS);
			} else if (is(a, W, "txbxContent") || is(a, V, "textpath")) n.add(InspectFinding.TEXT_BOXES);
		}
		// deleted inside an insertion: the markup view only; inserted: not the original view (REQ-905)
		if (deleted) n.add(inserted ? InspectFinding.VIEW_MARKUP : InspectFinding.VIEW_ORIGINAL);
		else if (inserted) n.add(InspectFinding.NOT_ORIGINAL);
		return n;
	}

	private void walkBranches(String p, Element root, Set<String> needs) {
		for (Element ac : iter(root, MC, "AlternateContent")) {
			if (unselectedBetween(ac, root)) continue; // the outermost unselected branch is the finding
			for (Element b : children(ac)) {
				if (!is(b, MC, "Choice") && !is(b, MC, "Fallback") || selected(b)) continue;
				add(found("mce.branch", p, path(b), text(b), plus(contextNeeds(b, root, needs), InspectFinding.NEVER), b));
			}
		}
	}

	// --- paragraphs: hidden, formatting, characters, fonts ---------------------------------------------

	private void walkParagraphs(String p, Element root, Set<String> needs) {
		Set<Element> seen = Collections.newSetFromMap(new IdentityHashMap<Element, Boolean>());
		for (Element para : iter(root, W, "p")) {
			if (!seen.add(para)) continue;
			Set<String> pneeds = contextNeeds(para, root, needs);
			List<Element> runs = new ArrayList<Element>();
			List<String> texts = new ArrayList<String>();
			for (Element r : ownRunsAll(para)) {
				String t = displayText(r);
				if (t.isEmpty()) continue;
				runs.add(r);
				texts.add(t);
			}
			boolean markHidden = Boolean.TRUE.equals(on(prop(markLayers(para), "vanish")));
			Map<Element, Boolean> hiddenRuns = new IdentityHashMap<Element, Boolean>();
			boolean allHidden = !runs.isEmpty();
			for (Element r : runs) {
				boolean h = Boolean.TRUE.equals(on(prop(layers(para, r), "vanish")));
				hiddenRuns.put(r, h);
				allHidden &= h;
			}
			boolean whole = markHidden && allHidden;
			if (whole) add(found("paragraph.hidden", p, path(para), String.join("", texts), plus(pneeds, InspectFinding.HIDDEN), para));
			Map<String, List<Found>> groups = new LinkedHashMap<String, List<Found>>();
			for (int k = 0; k < runs.size(); k++) {
				Element r = runs.get(k);
				int[] t = texts.get(k).codePoints().toArray();
				Set<String> rn = contextNeeds(r, root, needs);
				List<Element> layers = layers(para, r);
				boolean hid = hiddenRuns.get(r);
				Set<String> inner = hid || whole ? plus(rn, InspectFinding.HIDDEN) : rn;
				if (hid && !whole) emitRun(groups, "text.hidden", r, t, 0, t.length, plus(rn, InspectFinding.HIDDEN), Collections.emptyList(), null);
				if (Boolean.TRUE.equals(on(prop(layers, "webHidden")))) emitRun(groups, "text.web_hidden", r, t, 0, t.length, inner, Collections.emptyList(), null);
				if (!allSpace(t)) {
					Object[] nb = nearBackground(para, r, layers);
					if (nb != null) {
						if (!(Boolean) nb[0]) {
							run.undecided(Inspection.BACKGROUND_INDETERMINATE, loc(p, path(r)));
						} else if ((Boolean) nb[1]) {
							emitRun(groups, "text.near_background", r, t, 0, t.length, inner, Arrays.asList(nb[2], nb[3]),
									(Boolean) nb[4] && !pageShown ? Visible.INDETERMINATE : null);
						}
					}
					Integer tiny = tiny(t, layers);
					if (tiny != null) emitRun(groups, "text.tiny", r, t, 0, t.length, inner, Collections.<Object>singletonList(tiny), null);
				}
				charKind(groups, "chars.zero_width", r, t, inner, CharTest.ZERO_WIDTH, layers);
				charKind(groups, "chars.tag", r, t, inner, CharTest.TAG, layers);
				charKind(groups, "chars.bidi", r, t, inner, CharTest.BIDI, layers);
				charKind(groups, "chars.private_use", r, t, inner, CharTest.PRIVATE_USE, layers);
				fontTests(groups, para, r, t, layers, inner);
			}
			for (List<Found> g : groups.values()) for (Found f : g) add(f);
		}
	}

	private enum CharTest { ZERO_WIDTH, TAG, BIDI, PRIVATE_USE }

	private void charKind(Map<String, List<Found>> groups, String kind, Element r, int[] t, Set<String> needs, CharTest test, List<Element> layers) {
		int first = -1, last = -1;
		for (int i = 0; i < t.length; i++) {
			int c = t[i];
			boolean hit;
			switch (test) {
			case ZERO_WIDTH: hit = c >= 0x200B && c <= 0x200F || c == 0x2060 || c == 0xFEFF; break;
			case TAG: hit = c >= 0xE0000 && c <= 0xE007F; break;
			case BIDI: hit = c >= 0x202A && c <= 0x202E || c >= 0x2066 && c <= 0x2069; break;
			default: hit = privateUse(c) && !symbolFont(fontOf(r, c, layers));
			}
			if (!hit) continue;
			if (first < 0) first = i;
			last = i;
		}
		if (first >= 0) emitRun(groups, kind, r, t, first, last + 1, needs, Collections.emptyList(), null);
	}

	static boolean privateUse(int c) {
		return c >= 0xE000 && c <= 0xF8FF || c >= 0xF0000 && c <= 0xFFFFD || c >= 0x100000 && c <= 0x10FFFD;
	}

	private static boolean complexScript(int c) {
		return c >= 0x0590 && c <= 0x08FF || c >= 0x0900 && c <= 0x0DFF || c >= 0x0E00 && c <= 0x0EFF || c >= 0xFB1D && c <= 0xFDFF
				|| c >= 0xFE70 && c <= 0xFEFF;
	}

	/** Python's {@code str.isspace} for one code point. */
	static boolean isSpace(int c) {
		return Character.isSpaceChar(c) || c >= 0x09 && c <= 0x0D || c >= 0x1C && c <= 0x1F || c == 0x85;
	}

	private static boolean allSpace(int[] t) {
		for (int c : t) if (!isSpace(c)) return false;
		return true;
	}

	static boolean symbolFont(String name) {
		if (name == null) return false;
		String n = name.trim().toLowerCase(Locale.ROOT);
		return SYMBOL_FONTS.contains(n) || n.startsWith("font awesome") || n.startsWith("fontawesome");
	}

	/**
	 * A run finding, merged with the previous one of its kind when the runs are adjacent in one paragraph
	 * and every attribute the finding reports is equal (INSPECTION.md, "Identity").
	 */
	private void emitRun(Map<String, List<Found>> groups, String kind, Element r, int[] t, int start, int end, Set<String> needs,
			List<Object> attrs, Visible visible) {
		List<Found> list = groups.computeIfAbsent(kind, k -> new ArrayList<Found>());
		Found f = list.isEmpty() ? null : list.get(list.size() - 1);
		Element para = paragraphOf(r);
		int index = runIndex(para, r);
		List<Object> key = Arrays.<Object>asList(attrs, new HashSet<String>(needs), visible);
		if (f != null && f.attrs.equals(key) && f.para == para && f.lastIndex == index - 1) {
			f.endPath = path(r);
			f.end = end;
			f.texts.add(t);
			StringBuilder sb = new StringBuilder();
			int[] first = f.texts.get(0);
			sb.append(new String(first, f.start, first.length - f.start));
			for (int i = 1; i < f.texts.size() - 1; i++) sb.append(new String(f.texts.get(i), 0, f.texts.get(i).length));
			sb.append(new String(t, 0, end));
			f.excerpt = sb.toString();
			f.lastIndex = index;
			return;
		}
		f = found(kind, part, path(r), new String(t, start, end - start), needs, r);
		f.start = start;
		f.end = end;
		if (visible != null) f.visible = visible;
		f.attrs = key;
		f.para = para;
		f.lastIndex = index;
		f.texts.add(t);
		list.add(f);
	}

	private int runIndex(Element para, Element r) {
		if (para == null) return -10;
		List<Element> runs = runsWithText.get(para);
		if (runs == null) {
			runs = new ArrayList<Element>();
			for (Element x : ownRunsAll(para)) if (!displayText(x).isEmpty()) runs.add(x);
			runsWithText.put(para, runs);
		}
		for (int i = 0; i < runs.size(); i++) if (runs.get(i) == r) return i;
		return -10;
	}

	// --- property resolution ------------------------------------------------------------------------

	/** The styles part's styles, document defaults and default paragraph style. */
	static final class Styles {
		final Map<String, Element> styles = new HashMap<String, Element>();
		Element defaultRPr, defaultPPr;
		String defaultPara;

		Styles(Element root) {
			if (root == null) return;
			Element dd = child(root, W, "docDefaults");
			if (dd != null) {
				defaultRPr = child(child(dd, W, "rPrDefault"), W, "rPr");
				defaultPPr = child(child(dd, W, "pPrDefault"), W, "pPr");
			}
			for (Element s : children(root, W, "style")) {
				styles.put(w(s, "styleId"), s);
				if ("paragraph".equals(w(s, "type")) && w(s, "default") != null && isOn(s, "default")) defaultPara = w(s, "styleId");
			}
		}

		private static boolean isOn(Element s, String attr) {
			return Boolean.TRUE.equals(on(s, W, attr));
		}

		/** A style's basedOn chain, its base first. */
		List<Element> chain(String id) {
			List<Element> out = new ArrayList<Element>();
			Set<String> seen = new HashSet<String>();
			while (id != null && styles.containsKey(id) && seen.add(id)) {
				Element s = styles.get(id);
				out.add(0, s);
				Element b = child(s, W, "basedOn");
				id = b == null ? null : w(b, "val");
			}
			return out;
		}
	}

	private String paragraphStyle(Element p) {
		Element ps = child(child(p, W, "pPr"), W, "pStyle");
		return ps != null ? w(ps, "val") : styles.defaultPara;
	}

	/** The rPr elements that apply to a run, weakest first: defaults, table style, paragraph style, character style, the run's own. */
	private List<Element> layers(Element p, Element r) {
		List<Element> layers = new ArrayList<Element>();
		if (styles.defaultRPr != null) layers.add(styles.defaultRPr);
		String ts = tableStyle(p);
		if (ts != null) for (Element s : styles.chain(ts)) if (child(s, W, "rPr") != null) layers.add(child(s, W, "rPr"));
		for (Element s : styles.chain(paragraphStyle(p))) if (child(s, W, "rPr") != null) layers.add(child(s, W, "rPr"));
		Element rPr = r == null ? null : child(r, W, "rPr");
		if (rPr != null) {
			Element rs = child(rPr, W, "rStyle");
			if (rs != null) for (Element s : styles.chain(w(rs, "val"))) if (child(s, W, "rPr") != null) layers.add(child(s, W, "rPr"));
			layers.add(rPr);
		}
		return layers;
	}

	private List<Element> markLayers(Element p) {
		List<Element> layers = new ArrayList<Element>();
		if (styles.defaultRPr != null) layers.add(styles.defaultRPr);
		for (Element s : styles.chain(paragraphStyle(p))) if (child(s, W, "rPr") != null) layers.add(child(s, W, "rPr"));
		Element ppr = child(p, W, "pPr");
		if (ppr != null && child(ppr, W, "rPr") != null) layers.add(child(ppr, W, "rPr"));
		return layers;
	}

	/** The last layer's element of this name: the effective property, no toggle arithmetic (phase 0). */
	private static Element prop(List<Element> layers, String local) {
		Element found = null;
		for (Element l : layers) {
			Element e = child(l, W, local);
			if (e != null) found = e;
		}
		return found;
	}

	private Element pPrProp(Element p, String local) {
		Element found = null;
		if (styles.defaultPPr != null && child(styles.defaultPPr, W, local) != null) found = child(styles.defaultPPr, W, local);
		for (Element s : styles.chain(paragraphStyle(p))) {
			Element x = child(s, W, "pPr");
			if (x != null && child(x, W, local) != null) found = child(x, W, local);
		}
		Element ppr = child(p, W, "pPr");
		if (ppr != null && child(ppr, W, local) != null) found = child(ppr, W, local);
		return found;
	}

	private String tableStyle(Element p) {
		Element tc = InspectXml.ancestor(p, W, "tc");
		if (tc == null) return null;
		Element tbl = InspectXml.ancestor(tc, W, "tbl");
		Element ts = tbl == null ? null : child(child(tbl, W, "tblPr"), W, "tblStyle");
		return ts == null ? null : w(ts, "val");
	}

	/** A shading's fill and whether it is decided: null fill for none. */
	private static Object[] shading(Element shd) {
		if (shd == null) return new Object[] { null, true };
		String val = w(shd, "val") == null ? "clear" : w(shd, "val");
		String fill = w(shd, "fill");
		if (val.equals("nil")) return new Object[] { null, true };
		if (val.equals("clear")) return fill == null || fill.equalsIgnoreCase("auto") ? new Object[] { null, true } : new Object[] { fill.toUpperCase(Locale.ROOT), true };
		if (val.equals("solid")) {
			String colour = w(shd, "color");
			if (colour == null || colour.equalsIgnoreCase("auto")) return new Object[] { null, false };
			return new Object[] { colour.toUpperCase(Locale.ROOT), true };
		}
		return new Object[] { null, false }; // a pattern: a mix docx4j cannot determine
	}

	/** {decided, near, colour, background, on the page colour}, or null for no colour to compare. */
	private Object[] nearBackground(Element p, Element r, List<Element> layers) {
		Element colourEl = prop(layers, "color");
		if (colourEl == null) return null;
		String val = w(colourEl, "val");
		if (val == null || val.equalsIgnoreCase("auto")) return null;
		String colour = val.toUpperCase(Locale.ROOT);
		String background = null;
		boolean decided = true, page = false;
		Element hl = prop(layers, "highlight");
		if (hl != null && w(hl, "val") != null && !w(hl, "val").equals("none")) background = HIGHLIGHT.get(w(hl, "val"));
		if (background == null) {
			Object[] s = shading(prop(layers, "shd"));
			background = (String) s[0];
			decided = (Boolean) s[1];
		}
		if (background == null && decided) {
			Object[] s = shading(pPrProp(p, "shd"));
			background = (String) s[0];
			decided = (Boolean) s[1];
		}
		if (background == null && decided) {
			Element tc = InspectXml.ancestor(p, W, "tc");
			if (tc != null) {
				Element tcPr = child(tc, W, "tcPr");
				if (tcPr != null && child(tcPr, W, "shd") != null) {
					Object[] s = shading(child(tcPr, W, "shd"));
					background = (String) s[0];
					decided = (Boolean) s[1];
				}
				if (background == null && decided && tableStyle(p) != null) {
					for (Element st : styles.chain(tableStyle(p))) {
						Element x = child(st, W, "tcPr");
						if (x != null && child(x, W, "shd") != null) {
							Object[] s = shading(child(x, W, "shd"));
							background = (String) s[0];
							decided = (Boolean) s[1];
						}
					}
				}
			}
		}
		if (background == null && decided) {
			Object[] box = boxFill(r);
			if (box != null) {
				background = (String) box[0];
				decided = (Boolean) box[1];
			}
		}
		if (background == null && decided) {
			if (pageColour != null) {
				background = pageColour;
				page = true;
			} else {
				background = WHITE;
			}
		}
		if (!decided) return new Object[] { false, false, colour, null, false };
		Double ratio = contrast(colour, background);
		if (ratio == null) return null;
		return new Object[] { true, ratio < 1.5, colour, background, page };
	}

	/** A text box's fill: {fill, decided}, or null where the run is in none (or one with no fill). */
	private static Object[] boxFill(Element r) {
		for (Element a = parent(r); a != null; a = parent(a)) {
			if (is(a, WPS, "wsp")) {
				Element sp = child(a, WPS, "spPr");
				if (sp == null) return null;
				Element solid = child(child(sp, A, "solidFill"), A, "srgbClr");
				if (solid != null) return new Object[] { attr(solid, null, "val") == null ? null : attr(solid, null, "val").toUpperCase(Locale.ROOT), true };
				if (child(sp, A, "noFill") != null) return null;
				if (child(sp, A, "gradFill") != null || child(sp, A, "blipFill") != null || child(sp, A, "pattFill") != null) return new Object[] { null, false };
				return null;
			}
			if (is(a, V, "shape") || is(a, V, "rect")) {
				String filled = attr(a, null, "filled");
				if (filled != null && (filled.equalsIgnoreCase("f") || filled.equalsIgnoreCase("false"))) return null;
				String fc = attr(a, null, "fillcolor");
				if (fc != null && fc.matches("#?[0-9a-fA-F]{6}")) return new Object[] { fc.replace("#", "").toUpperCase(Locale.ROOT), true };
				return new Object[] { WHITE, true };
			}
		}
		return null;
	}

	/** The WCAG 2 contrast ratio of two sRGB colours; null for a colour that is not one. */
	static Double contrast(String a, String b) {
		Double la = luminance(a), lb = luminance(b);
		if (la == null || lb == null) return null;
		double hi = Math.max(la, lb), lo = Math.min(la, lb);
		return (hi + 0.05) / (lo + 0.05);
	}

	private static Double luminance(String hex) {
		if (hex == null || !hex.matches("[0-9A-Fa-f]{6}")) return null;
		double[] c = new double[3];
		for (int i = 0; i < 3; i++) {
			double v = Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16) / 255.0;
			c[i] = v <= 0.04045 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
		}
		return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2];
	}

	/** The size every non-space character of a run is drawn at, in half-points, where all are under 2pt; else null. */
	private static Integer tiny(int[] t, List<Element> layers) {
		boolean cs = Boolean.TRUE.equals(on(prop(layers, "cs"))) || Boolean.TRUE.equals(on(prop(layers, "rtl")));
		int size = halfPoints(prop(layers, "sz")), sizeCs = halfPoints(prop(layers, "szCs"));
		int drawn = 0, small = 0, min = Integer.MAX_VALUE;
		for (int c : t) {
			if (isSpace(c)) continue;
			int s = cs || complexScript(c) ? sizeCs : size;
			drawn++;
			if (s < 4) {
				small++;
				min = Math.min(min, s);
			}
		}
		return small > 0 && small == drawn ? min : null;
	}

	private static int halfPoints(Element sz) {
		if (sz == null) return 20;
		try {
			return Integer.parseInt(w(sz, "val").trim());
		} catch (RuntimeException e) {
			return 20;
		}
	}

	/** The document's font name for a character (D17): a {@code w:sym}'s {@code w:font}, else the run's for the character's slot. */
	private String fontOf(Element r, int c, List<Element> layers) {
		if (is(r, W, "r")) {
			Element sym = child(r, W, "sym");
			if (sym != null && symbol(sym) == c) return w(sym, "font");
		}
		Element f = prop(layers, "rFonts");
		if (f == null) return null;
		if (complexScript(c) || Boolean.TRUE.equals(on(prop(layers, "cs"))) || Boolean.TRUE.equals(on(prop(layers, "rtl")))) return w(f, "cs");
		if (c < 0x80) return w(f, "ascii");
		return w(f, "hAnsi");
	}

	// --- embedded faces (D17, D18) --------------------------------------------------------------------

	private void loadFaces() {
		List<String> ft = pkg.byType(main, "/fontTable");
		if (ft.isEmpty()) return;
		Element root = pkg.xml(ft.get(0));
		if (root == null) return;
		for (Element font : children(root, W, "font")) {
			String name = w(font, "name");
			if (name == null) continue;
			for (String style : new String[] { "embedRegular", "embedBold", "embedItalic", "embedBoldItalic" }) {
				Element e = child(font, W, style);
				if (e == null) continue;
				String p = pkg.target(ft.get(0), attr(e, R, "id"));
				byte[] data = p == null ? null : pkg.bytes(p);
				if (data == null) continue;
				String where = loc(p, "");
				try {
					run.payload().add(data.length);
				} catch (InspectRun.PayloadLimit limit) {
					run.skip(Inspection.LIMIT_PAYLOAD_BYTES, where);
					continue;
				}
				byte[] face = data.clone();
				String key = w(e, "fontKey");
				if (key != null && !key.isEmpty()) {
					String hex = key.replace("{", "").replace("}", "").replace("-", "");
					if (!hex.matches("[0-9A-Fa-f]{32}") || face.length < 32) {
						run.skip(Inspection.UNREADABLE, where);
						continue;
					}
					byte[] k = new byte[16];
					for (int i = 0; i < 16; i++) k[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
					for (int i = 0; i < 32; i++) face[i] ^= k[15 - i % 16];
				}
				try {
					// what the face holds is charged to the total (REQ-907): a small face can map far more than its bytes
					InspectFace f = InspectFace.read(face, run.maxTotalBytes - run.totalBytes);
					run.charge(f.held());
					faces.put(name.toLowerCase(Locale.ROOT) + "|" + style, f);
					facePart.put(f, p);
				} catch (InspectFace.OverBudget over) {
					throw new InspectRun.Stop(Inspection.LIMIT_TOTAL_BYTES);
				} catch (InspectRun.Stop stop) {
					throw stop;
				} catch (InspectFace.NotTrueType nt) {
					run.skip(Inspection.NOT_DECODED, where);
				} catch (java.io.IOException | RuntimeException bad) {
					run.skip(Inspection.UNREADABLE, where);
				}
			}
		}
	}

	private InspectFace face(String name, List<Element> layers) {
		if (name == null) return null;
		boolean b = Boolean.TRUE.equals(on(prop(layers, "b"))), i = Boolean.TRUE.equals(on(prop(layers, "i")));
		String style = b && i ? "embedBoldItalic" : b ? "embedBold" : i ? "embedItalic" : "embedRegular";
		return faces.get(name.toLowerCase(Locale.ROOT) + "|" + style);
	}

	private void fontTests(Map<String, List<Found>> groups, Element para, Element r, int[] t, List<Element> layers, Set<String> needs) {
		if (faces.isEmpty()) return;
		int blankFirst = -1, blankLast = -1, mismatchFirst = -1, mismatchLast = -1;
		Set<String> tests = new LinkedHashSet<String>();
		for (int i = 0; i < t.length; i++) {
			int c = t[i];
			String name = fontOf(r, c, layers);
			InspectFace face = face(name, layers);
			if (face == null) continue;
			String p = facePart.get(face);
			Object[] used = usedFaces.get(p);
			if (used == null) usedFaces.put(p, used = new Object[] { p, 0, name });
			used[1] = (Integer) used[1] + 1;
			Integer glyph = face.glyph(c);
			if (glyph == null || glyph == 0) continue;
			String cat = category(c);
			if (!face.outline(glyph)) {
				if (!(isSpace(c) || cat.equals("Zs") || cat.equals("Cf") || cat.equals("Mn") || cat.equals("Me") || cat.equals("Mc"))) {
					if (blankFirst < 0) blankFirst = i;
					blankLast = i;
				}
				continue;
			}
			if (symbolFont(name)) continue;
			String test = mismatch(face, c, glyph, features(layers));
			if (test != null) {
				if (mismatchFirst < 0) mismatchFirst = i;
				mismatchLast = i;
				tests.add(new String(Character.toChars(c)) + ": " + test);
			}
		}
		if (blankFirst >= 0) emitRun(groups, "font.glyph_blank", r, t, blankFirst, blankLast + 1, needs, Collections.emptyList(), null);
		if (mismatchFirst >= 0) {
			emitRun(groups, "font.glyph_mismatch", r, t, mismatchFirst, mismatchLast + 1, needs, Collections.emptyList(), null);
			List<Found> l = groups.get("font.glyph_mismatch");
			Found f = l.get(l.size() - 1);
			f.summary = f.summary.isEmpty() ? String.join("; ", tests) : f.summary + "; " + String.join("; ", tests);
		}
	}

	/**
	 * Which of D18's tests finds a letter or digit drawn as another, naming what is drawn; null for none.
	 * As calibrated on the layout-fidelity corpora's embedded faces (CR-050 phase 1): a compatibility
	 * character (a presentation form, a ligature, a width variant) is not tested, its glyph being a
	 * variant by design; "drawn as" another character is evidence only where the two are not
	 * {@link #sameLetter the same letter} and the other is no compatibility form; a modifier letter's
	 * advance is not tested, nor a titlecase letter's width; a glyph name names every character the AGL
	 * gives it.
	 */
	static String mismatch(InspectFace face, int c, int glyph, Set<String> features) {
		String cat = category(c);
		if (!letterOrDigit(c) || compatibility(c)) return null;
		for (int d : face.codes(glyph)) {
			if (d == c || !evidence(c, d)) continue;
			return "its glyph is " + new String(Character.toChars(d)) + "'s"; // test 1: a shared glyph
		}
		int adv = face.advance(glyph);
		if (cat.startsWith("L") && !cat.equals("Lm") && !base(c).isEmpty() && (adv == 0 || adv > 1.5 * face.unitsPerEm && !cat.equals("Lt"))) {
			return "its advance is " + adv; // test 2: zero, or over 1.5 em
		}
		if (face.hasNames()) {
			List<Integer> named = InspectFace.named(face.name(glyph));
			boolean other = !named.isEmpty();
			for (int v : named) other &= v != c && evidence(c, v);
			if (other) return "its glyph is named " + face.name(glyph); // test 3
		}
		for (int out : face.substitutions(glyph, features)) {
			for (int d : face.codes(out)) {
				if (d != c && evidence(c, d)) return "a substitution draws " + new String(Character.toChars(d)); // test 4
			}
		}
		return null;
	}

	private static boolean letterOrDigit(int c) {
		String cat = category(c);
		return cat.startsWith("L") || cat.equals("Nd");
	}

	/** Whether {@code c} drawn as {@code d} is evidence: {@code d} a letter or digit, no compatibility form, and another letter. */
	private static boolean evidence(int c, int d) {
		return letterOrDigit(d) && !compatibility(d) && !sameLetter(c, d);
	}

	/** A compatibility character: one NFKC changes (a presentation form, a ligature, a width variant, a superscript). */
	private static boolean compatibility(int c) {
		String s = new String(Character.toChars(c));
		return !Normalizer.normalize(s, Normalizer.Form.NFKC).equals(s);
	}

	/**
	 * Whether two characters are one letter for D18's tests: the same {@link #base}, or either with no base
	 * (a mark-like letter), or in different scripts (Latin A and Greek Alpha, the first word of each
	 * character's Unicode name).
	 */
	static boolean sameLetter(int c, int d) {
		String bc = base(c), bd = base(d);
		return bc.isEmpty() || bd.isEmpty() || bc.equals(bd) || !script(c).equals(script(d));
	}

	/**
	 * A character's base letter: its compatibility decomposition, marks, modifier letters and spaces
	 * dropped, case folded, and the Latin letters a face draws as another by design folded to it (dotless
	 * i and j, which {@code ccmp} puts under an accent; kra, eth and d with stroke, which a capitals face
	 * draws as K and D).
	 */
	static String base(int c) {
		String nfkd = Normalizer.normalize(new String(Character.toChars(c)), Normalizer.Form.NFKD);
		StringBuilder sb = new StringBuilder();
		nfkd.codePoints().forEach(x -> {
			String cat = category(x);
			if (cat.equals("Mn") || cat.equals("Mc") || cat.equals("Me") || cat.equals("Lm") || isSpace(x)) return;
			int f = Character.toLowerCase(Character.toUpperCase(x));
			int i = FOLD.indexOf(f);
			sb.appendCodePoint(i >= 0 && i % 2 == 0 ? FOLD.charAt(i + 1) : f);
		});
		return sb.toString();
	}

	/** Pairs: a Latin letter, and the letter it is drawn as by design. */
	private static final String FOLD = "\u0131i\u0237j\u0138k\u00f0d\u0111d";

	/** The GSUB features Word applies to a run: ccmp, locl and rlig always; ligatures and contextual alternates as the run says. */
	private static Set<String> features(List<Element> layers) {
		Set<String> f = new HashSet<String>(Arrays.asList("ccmp", "locl", "rlig"));
		String lig = null;
		for (Element l : layers) {
			Element e = child(l, W14, "ligatures");
			if (e != null) lig = attr(e, W14, "val");
		}
		if (lig != null) {
			String v = lig.toLowerCase(Locale.ROOT);
			if (v.equals("all") || v.contains("standard")) f.add("liga");
			if (v.equals("all") || v.contains("contextual")) f.add("clig");
			if (v.equals("all") || v.contains("historical")) f.add("hlig");
			if (v.equals("all") || v.contains("discretional")) f.add("dlig");
		}
		for (Element l : layers) {
			Element e = child(l, W14, "cntxtAlts");
			if (e != null && Boolean.TRUE.equals(on(e, W14, "val"))) f.add("calt");
		}
		return f;
	}

	/** A character's script, as the first word of its Unicode name. */
	private static String script(int c) {
		String name = Character.getName(c);
		return name == null ? "" : name.split(" ", 2)[0];
	}

	/** A code point's two-letter general category. */
	static String category(int c) {
		switch (Character.getType(c)) {
		case Character.UPPERCASE_LETTER: return "Lu";
		case Character.LOWERCASE_LETTER: return "Ll";
		case Character.TITLECASE_LETTER: return "Lt";
		case Character.MODIFIER_LETTER: return "Lm";
		case Character.OTHER_LETTER: return "Lo";
		case Character.NON_SPACING_MARK: return "Mn";
		case Character.ENCLOSING_MARK: return "Me";
		case Character.COMBINING_SPACING_MARK: return "Mc";
		case Character.DECIMAL_DIGIT_NUMBER: return "Nd";
		case Character.LETTER_NUMBER: return "Nl";
		case Character.OTHER_NUMBER: return "No";
		case Character.SPACE_SEPARATOR: return "Zs";
		case Character.LINE_SEPARATOR: return "Zl";
		case Character.PARAGRAPH_SEPARATOR: return "Zp";
		case Character.CONTROL: return "Cc";
		case Character.FORMAT: return "Cf";
		case Character.PRIVATE_USE: return "Co";
		case Character.SURROGATE: return "Cs";
		case Character.UNASSIGNED: return "Cn";
		default: return "P"; // punctuation and symbols: neither a letter, a digit, a mark nor a format character
		}
	}

	// --- fields -------------------------------------------------------------------------------------

	private void walkFields(String p, Element root, Set<String> needs) {
		List<Object[]> stack = new ArrayList<Object[]>(); // {runs, code}
		for (Element e : iter(root, W, "fldChar", W, "instrText", W, "delInstrText", W, "fldSimple")) {
			if (is(e, W, "fldSimple")) {
				String instr = w(e, "instr") == null ? "" : w(e, "instr");
				fieldFindings(p, path(e), null, "", null, instr, plus(contextNeeds(e, root, needs), InspectFinding.FIELD_CODES), e);
				continue;
			}
			if (is(e, W, "fldChar")) {
				String t = w(e, "fldCharType");
				if ("begin".equals(t)) {
					stack.add(new Object[] { new ArrayList<Element>(), Boolean.TRUE });
				} else if ("separate".equals(t) && !stack.isEmpty()) {
					stack.get(stack.size() - 1)[1] = Boolean.FALSE;
				} else if ("end".equals(t) && !stack.isEmpty()) {
					@SuppressWarnings("unchecked")
					List<Element> runs = (List<Element>) stack.remove(stack.size() - 1)[0];
					if (!runs.isEmpty()) {
						Element first = runs.get(0), last = runs.get(runs.size() - 1);
						StringBuilder instr = new StringBuilder();
						for (Element r : runs) instr.append(runText(r));
						String lastText = runText(last);
						fieldFindings(p, path(first), 0, last != first ? path(last) : "", lastText.codePointCount(0, lastText.length()),
								instr.toString(), plus(contextNeeds(first, root, needs), InspectFinding.FIELD_CODES), first);
					}
				}
				continue;
			}
			Element r = parent(e);
			for (Object[] f : stack) {
				@SuppressWarnings("unchecked")
				List<Element> runs = (List<Element>) f[0];
				if ((Boolean) f[1] && (runs.isEmpty() || runs.get(runs.size() - 1) != r)) runs.add(r);
			}
		}
	}

	private void fieldFindings(String p, String path, Integer start, String endPath, Integer end, String instr, Set<String> needs, Element el) {
		Found code = found("field.code", p, path, instr, needs, el);
		code.start = start;
		code.endPath = endPath;
		code.end = end;
		add(code);
		String[] parsed = parseField(instr);
		String k = parsed[0].toUpperCase(Locale.ROOT);
		Found f = null;
		if (EXTERNAL_FIELDS.contains(k)) {
			f = found("field.external", p, path, instr, needs, el);
		} else if (k.equals("HYPERLINK") && parsed[1] != null) {
			if (web(parsed[1])) {
				f = found("hyperlink.web", p, path, parsed[1], needs, el);
				f.subject = parsed[1];
			} else {
				f = found("field.external", p, path, instr, needs, el);
			}
		}
		if (f != null) {
			f.start = start;
			f.endPath = endPath;
			f.end = end;
			add(f);
		}
		// a field whose presence makes Word recompute the document's literal IFs on open, or on its PDF export (WD 0.41)
		Found trigger = OPEN_TRIGGER_FIELDS.contains(k) ? found(InspectFinding.FIELDS_AUTO_UPDATE, p, path, instr, needs, el)
				: PRINT_TRIGGER_FIELDS.contains(k) ? found(InspectFinding.FIELDS_PRINT_UPDATE, p, path, instr, needs, el) : null;
		if (trigger != null) {
			trigger.start = start;
			trigger.endPath = endPath;
			trigger.end = end;
			add(trigger);
		}
	}

	/** A web target (the link rule): the scheme http, https or mailto. */
	static boolean web(String target) {
		Matcher m = Pattern.compile("([A-Za-z][A-Za-z0-9+.-]*):").matcher(target == null ? "" : target);
		if (!m.lookingAt()) return false;
		String s = m.group(1).toLowerCase(Locale.ROOT);
		return (s.equals("http") || s.equals("https") || s.equals("mailto")) && s.length() > 1;
	}

	/** A field instruction's keyword, first argument and bookmark ({@code \l}): arguments quoted ({@code \\} and {@code \"} unescaped) or bare. */
	static String[] parseField(String s) {
		List<String[]> tokens = new ArrayList<String[]>();
		int i = 0;
		while (i < s.length()) {
			char c = s.charAt(i);
			if (Character.isWhitespace(c)) {
				i++;
				continue;
			}
			if (c == '"') {
				int j = i + 1;
				StringBuilder out = new StringBuilder();
				while (j < s.length() && s.charAt(j) != '"') {
					if (s.charAt(j) == '\\' && j + 1 < s.length() && (s.charAt(j + 1) == '\\' || s.charAt(j + 1) == '"')) {
						out.append(s.charAt(j + 1));
						j += 2;
						continue;
					}
					out.append(s.charAt(j));
					j++;
				}
				tokens.add(new String[] { "arg", out.toString() });
				i = j + 1;
				continue;
			}
			int j = i;
			while (j < s.length() && !Character.isWhitespace(s.charAt(j))) j++;
			String word = s.substring(i, j);
			tokens.add(new String[] { word.length() == 2 && word.charAt(0) == '\\' && Character.isLetter(word.charAt(1)) ? "switch" : "arg", word });
			i = j;
		}
		if (tokens.isEmpty()) return new String[] { "", null, null };
		String target = null, bookmark = null;
		for (int k = 1; k < tokens.size(); k++) {
			String[] t = tokens.get(k);
			if (t[0].equals("switch")) {
				String v = t[1].toLowerCase(Locale.ROOT);
				if ((v.equals("\\l") || v.equals("\\o") || v.equals("\\t") || v.equals("\\m")) && k + 1 < tokens.size() && tokens.get(k + 1)[0].equals("arg")) {
					if (v.equals("\\l")) bookmark = tokens.get(k + 1)[1];
					k++;
				}
			} else if (target == null) {
				target = t[1];
			}
		}
		return new String[] { tokens.get(0)[1], target, bookmark };
	}

	// --- drawings: shapes and alt text ------------------------------------------------------------------

	/**
	 * SmartArt data its drawing part does not show (REQ-902): where the data part names a drawing part
	 * ({@code dsp:dataModelExt}, its {@code relId} the story part's), each point the text model reads
	 * (Drawings.smartArt's order) with a line no paragraph of the drawing part holds, each drawing line
	 * holding one. No drawing part, no finding: Word lays the diagram out from the data.
	 */
	private void smartArt(String p, Element relIds, Set<String> dn) {
		String data = pkg.target(p, attr(relIds, R, "dm"));
		if (data == null || !pkg.has(data)) return;
		Element model = pkg.xml(data);
		if (model == null) return;
		Element ext = InspectXml.first(model, DSP, "dataModelExt");
		String drawing = ext == null ? null : pkg.target(p, attr(ext, null, "relId"));
		if (drawing == null || !pkg.has(drawing)) return;
		Map<String, Integer> shown = new HashMap<String, Integer>();
		for (Element body : iter(pkg.xml(drawing), DSP, "txBody")) {
			for (Element ap : children(body, A, "p")) shown.merge(dmlLine(ap), 1, Integer::sum);
		}
		for (Element pt : smartArtPoints(model)) {
			List<String> missing = new ArrayList<String>();
			for (Element ap : children(child(pt, DGM, "t"), A, "p")) {
				String line = dmlLine(ap);
				if (line.isEmpty()) continue;
				Integer n = shown.get(line);
				if (n != null && n > 0) shown.put(line, n - 1);
				else missing.add(line);
			}
			if (!missing.isEmpty()) {
				add(found("smartart.data", data, path(data, pt), String.join("\n", missing), plus(dn, InspectFinding.SMARTART), pt));
			}
		}
	}

	/** A DrawingML paragraph's line: its runs' and fields' {@code a:t}, an {@code a:br} a line feed (Drawings.text). */
	private static String dmlLine(Element ap) {
		StringBuilder sb = new StringBuilder();
		for (Element c : children(ap)) {
			if (is(c, A, "r") || is(c, A, "fld")) sb.append(InspectXml.text(child(c, A, "t")));
			else if (is(c, A, "br")) sb.append('\n');
		}
		return sb.toString();
	}

	/**
	 * The points whose text the text model reads (Drawings.smartArt): from the {@code doc} point
	 * depth-first along {@code parOf} connections, each point's children by {@code @srcOrd}, those of
	 * type {@code node} (the default) and {@code asst}.
	 */
	private static List<Element> smartArtPoints(Element model) {
		Map<String, Element> points = new HashMap<String, Element>();
		Element doc = null;
		for (Element pt : children(child(model, DGM, "ptLst"), DGM, "pt")) {
			String id = attr(pt, null, "modelId");
			if (id == null) continue;
			points.put(id, pt);
			if (doc == null && "doc".equals(attr(pt, null, "type"))) doc = pt;
		}
		List<Element> out = new ArrayList<Element>();
		if (doc == null) return out;
		Map<String, List<Element>> kids = new HashMap<String, List<Element>>();
		for (Element c : children(child(model, DGM, "cxnLst"), DGM, "cxn")) {
			String type = attr(c, null, "type");
			if (type != null && !type.equals("parOf") || attr(c, null, "srcId") == null) continue;
			kids.computeIfAbsent(attr(c, null, "srcId"), k -> new ArrayList<Element>()).add(c);
		}
		for (List<Element> l : kids.values()) l.sort(Comparator.comparingLong(c -> Math.max(0, number(attr(c, null, "srcOrd")))));
		Set<String> seen = new HashSet<String>();
		Deque<Element> stack = new ArrayDeque<Element>();
		stack.push(doc);
		while (!stack.isEmpty()) {
			Element pt = stack.pop();
			if (!seen.add(attr(pt, null, "modelId"))) continue; // a cycle
			String type = attr(pt, null, "type");
			if (type == null || type.equals("node") || type.equals("asst")) out.add(pt);
			List<Element> l = kids.get(attr(pt, null, "modelId"));
			if (l == null) continue;
			for (int i = l.size() - 1; i >= 0; i--) {
				Element child = points.get(attr(l.get(i), null, "destId"));
				if (child != null) stack.push(child);
			}
		}
		return out;
	}


	private void walkDrawings(String p, Element root, Set<String> needs) {
		for (Element d : iter(root, WP, "inline", WP, "anchor")) {
			Set<String> dn = contextNeeds(d, root, needs);
			String text = textOfShape(d);
			Set<String> tneeds = plus(dn, text.isEmpty() ? InspectFinding.NEVER : InspectFinding.TEXT_BOXES);
			Element ext = child(d, WP, "extent");
			long cx = number(attr(ext, null, "cx")), cy = number(attr(ext, null, "cy"));
			Element docPr = child(d, WP, "docPr");
			boolean anchored = is(d, WP, "anchor");
			if (ext != null && (cx == 0 || cy == 0)) add(found("shape.zero_size", p, path(d), text, tneeds, d));
			if (anchored && docPr != null && attr(docPr, null, "hidden") != null && Boolean.TRUE.equals(on(docPr, null, "hidden"))) {
				add(found("shape.hidden", p, path(d), text, tneeds, d));
			}
			if (anchored) {
				String verdict = offPageAnchor(d, cx, cy);
				if ("off".equals(verdict)) add(found("shape.off_page", p, path(d), text, tneeds, d));
				else if (verdict != null) run.undecided(verdict, loc(p, path(d)));
			}
			if (docPr != null) altText(p, d, docPr, dn);
			for (Element pic : iter(d, PIC, "pic")) {
				Element blip = InspectXml.first(pic, A, "blip");
				if (blip == null) continue;
				// Word 365 draws the SVG asvg:svgBlip names, an older Office the raster copy a:blip names (D21)
				Element svg = InspectXml.first(blip, ASVG, "svgBlip");
				String raster = pkg.target(p, attr(blip, R, "embed"));
				String shown = svg != null && attr(svg, R, "embed") != null ? pkg.target(p, attr(svg, R, "embed")) : raster;
				imageText(p, shown, shown != null && shown.equals(raster) ? null : raster, dn);
			}
			Element chart = InspectXml.first(d, C, "chart");
			if (chart != null) {
				String target = pkg.target(p, attr(chart, R, "id"));
				if (target != null && pkg.has(target)) {
					Element cx_ = pkg.xml(target);
					List<String> strings = new ArrayList<String>();
					for (Element e : iter(cx_, C, "v", A, "t")) {
						if (is(e, A, "t") || InspectXml.ancestor(e, C, "strCache") != null) strings.add(e.getTextContent());
					}
					Found f = add(found("chart.cached", target, "", String.join("\n", strings), plus(dn, InspectFinding.CHARTS), cx_));
					f.anchor = null; // the chart part's address, which the text model records by part
				}
			}
			Element relIds = child(child(child(d, A, "graphic"), A, "graphicData"), DGM, "relIds");
			if (relIds != null) smartArt(p, relIds, dn);
		}
		for (Element s : iter(root, V, "shape", V, "rect", V, "roundrect", V, "oval")) {
			Set<String> dn = contextNeeds(s, root, needs);
			Map<String, String> style = vmlStyle(s);
			String text = textOfShape(s);
			Set<String> tneeds = plus(dn, text.isEmpty() ? InspectFinding.NEVER : InspectFinding.TEXT_BOXES);
			String id = attr(s, null, "id");
			if (id != null && id.startsWith("PowerPlusWaterMarkObject")) {
				Element tp = child(s, V, "textpath");
				Set<String> wneeds = "header".equals(storyKind) || "footer".equals(storyKind) ? plus(dn, InspectFinding.TEXT_BOXES) : plus(dn, InspectFinding.NEVER);
				add(found("watermark", p, path(s), tp == null || attr(tp, null, "string") == null ? "" : attr(tp, null, "string"), wneeds, s));
			}
			Double wd = lengthUnit(style.get("width")), ht = lengthUnit(style.get("height"));
			if (wd != null && wd == 0 || ht != null && ht == 0) add(found("shape.zero_size", p, path(s), text, tneeds, s));
			if ("hidden".equalsIgnoreCase(style.getOrDefault("visibility", ""))) add(found("shape.hidden", p, path(s), text, tneeds, s));
			if ("absolute".equalsIgnoreCase(style.getOrDefault("position", ""))) {
				String verdict = offPageVml(s, style);
				if ("off".equals(verdict)) add(found("shape.off_page", p, path(s), text, tneeds, s));
				else if (verdict != null) run.undecided(verdict, loc(p, path(s)));
			}
			String alt = attr(s, null, "alt");
			if (alt != null && !alt.isEmpty()) {
				boolean image = child(s, V, "imagedata") != null;
				add(found("alt_text.description", p, path(s), alt, plus(dn, image ? InspectFinding.ALT_TEXT : InspectFinding.NEVER), s));
			}
			Element im = child(s, V, "imagedata");
			String title = attr(im, O, "title");
			if (im != null && title != null && !title.isEmpty()) add(found("alt_text.title", p, path(im), title, plus(dn, InspectFinding.NEVER), im));
			if (im != null) imageText(p, pkg.target(p, attr(im, R, "id")), null, dn);
		}
	}

	private static final String ASVG = "http://schemas.microsoft.com/office/drawing/2016/SVG/main";

	/**
	 * The text an image part a picture shows draws (D21, {@link InspectImages}): one finding per part, where the
	 * walk first meets a picture showing it; a later picture showing the same part is counted in its summary.
	 * The part's bytes are a payload (REQ-907); a metafile the decoder cannot parse is skipped as unreadable.
	 */
	private void imageText(String p, String target, String raster, Set<String> dn) {
		if (target == null || !pkg.has(target)) return;
		if (images.containsKey(target)) {
			Found f = images.get(target);
			if (f != null) {
				Object[] info = imageInfo.get(target);
				info[0] = ((Integer) info[0]) + 1;
				f.summary = imageSummary(info);
			}
			return;
		}
		String ctype = pkg.contentType(target);
		boolean svg = InspectImages.isSvg(ctype);
		if (!svg && !InspectImages.isMetafile(ctype)) return;
		images.put(target, null);
		byte[] data = pkg.bytes(target); // null for an SVG, which docx4j parses at load (a DefaultXmlPart)
		String where = loc(target, "");
		List<String> lines;
		try {
			if (svg) {
				run.payload().add(data != null ? data.length : pkg.storedSize(target));
				Element root = pkg.xml(target);
				if (root == null) {
					run.skip(Inspection.UNREADABLE, where);
					return;
				}
				lines = InspectImages.svgText(root);
			} else {
				if (data == null) return;
				run.payload().add(data.length);
				lines = InspectImages.metafileText(data);
			}
		} catch (InspectRun.PayloadLimit limit) {
			run.skip(Inspection.LIMIT_PAYLOAD_BYTES, where);
			return;
		} catch (InspectRun.Stop stop) {
			throw stop;
		} catch (Exception e) {
			run.skip(Inspection.UNREADABLE, where);
			return;
		}
		if (lines.isEmpty()) return;
		Found f = found(svg ? InspectFinding.IMAGE_SVG_TEXT : InspectFinding.IMAGE_METAFILE_TEXT, target, "",
				String.join("\n", lines), plus(dn, InspectFinding.NEVER), null);
		Object[] info = { Integer.valueOf(1), Integer.valueOf(lines.size()), raster };
		f.summary = imageSummary(info);
		imageInfo.put(target, info);
		images.put(target, f);
		add(f);
	}

	/** {@code pictures=<n> texts=<m>}, and {@code raster=<part>} where the first picture showing the part has a raster copy. */
	private static String imageSummary(Object[] info) {
		return "pictures=" + info[0] + " texts=" + info[1] + (info[2] == null ? "" : " raster=" + info[2]);
	}

	private static long number(String v) {
		try {
			return v == null ? -1 : Long.parseLong(v.trim());
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private String textOfShape(Element d) {
		List<Element> boxes = iter(d, W, "txbxContent");
		if (!boxes.isEmpty()) {
			List<String> out = new ArrayList<String>();
			for (Element b : boxes) out.add(text(b));
			return String.join("\n", out);
		}
		Element tp = child(d, V, "textpath");
		String s = attr(tp, null, "string");
		return s == null ? "" : s;
	}

	private void altText(String p, Element d, Element docPr, Set<String> dn) {
		boolean decorative = false;
		for (Element x : iter(docPr, InspectXml.DECORATIVE, "decorative")) {
			String v = attr(x, null, "val");
			if ("1".equals(v) || "true".equals(v)) decorative = true;
		}
		boolean picture = InspectXml.first(d, PIC, "pic") != null;
		String descr = attr(docPr, null, "descr"), title = attr(docPr, null, "title"), name = attr(docPr, null, "name");
		boolean hasDescr = descr != null && !descr.isEmpty();
		if (hasDescr) add(found("alt_text.description", p, path(docPr), descr, plus(dn, picture && !decorative ? InspectFinding.ALT_TEXT : InspectFinding.NEVER), docPr));
		if (title != null && !title.isEmpty()) add(found("alt_text.title", p, path(docPr), title, plus(dn, InspectFinding.NEVER), docPr));
		boolean described = picture && !decorative && !hasDescr;
		if (name != null && !name.isEmpty() && (described || !DEFAULT_NAME.matcher(name).matches())) {
			add(found("alt_text.name", p, path(docPr), name, plus(dn, described ? InspectFinding.ALT_TEXT : InspectFinding.NEVER), docPr));
		}
	}

	private static Map<String, String> vmlStyle(Element el) {
		Map<String, String> style = new HashMap<String, String>();
		String s = attr(el, null, "style");
		if (s == null) return style;
		for (String item : s.split(";")) {
			int colon = item.indexOf(':');
			if (colon > 0) style.put(item.substring(0, colon).trim().toLowerCase(Locale.ROOT), item.substring(colon + 1).trim());
		}
		return style;
	}

	/** A VML length in points, or null. */
	static Double lengthUnit(String value) {
		Matcher m = Pattern.compile("\\s*(-?[0-9.]+)\\s*(pt|in|cm|mm|px|emu)?\\s*").matcher(value == null ? "" : value);
		if (!m.matches()) return null;
		double n;
		try {
			n = Double.parseDouble(m.group(1));
		} catch (NumberFormatException e) {
			return null;
		}
		String u = m.group(2);
		double f = u == null || u.equals("px") ? 0.75 : u.equals("pt") ? 1 : u.equals("in") ? 72 : u.equals("cm") ? 72 / 2.54
				: u.equals("mm") ? 72 / 25.4 : 1.0 / EMU_PER_PT;
		return n * f;
	}

	// --- geometry (D4) ------------------------------------------------------------------------------

	/** {width, height, top, right, bottom, left, columns} in EMU of the section holding an element, or null. */
	private long[] pageOf(Element el) {
		if ("header".equals(storyKind) || "footer".equals(storyKind)) {
			Set<List<Long>> sizes = new LinkedHashSet<List<Long>>();
			for (Element sp : sections) {
				boolean uses = false;
				for (Element ref : refs(sp)) if (part.equals(pkg.target(main, attr(ref, R, "id")))) uses = true;
				if (uses) {
					List<Long> l = new ArrayList<Long>();
					for (long v : pageSize(sp)) l.add(v);
					sizes.add(l);
				}
			}
			if (sizes.size() != 1) return null;
			List<Long> l = sizes.iterator().next();
			long[] out = new long[l.size()];
			for (int i = 0; i < out.length; i++) out[i] = l.get(i);
			return out;
		}
		if (!"main".equals(storyKind)) return null;
		Element bodyChild = el;
		for (Element a = parent(el); a != null; a = parent(a)) {
			if (is(a, W, "body")) break;
			bodyChild = a;
		}
		for (Node sib = bodyChild; sib != null; sib = sib.getNextSibling()) {
			if (!(sib instanceof Element)) continue;
			Element s = (Element) sib;
			Element sp = is(s, W, "p") ? child(child(s, W, "pPr"), W, "sectPr") : is(s, W, "sectPr") ? s : null;
			if (sp != null) return pageSize(sp);
		}
		return null;
	}

	private static long[] pageSize(Element sp) {
		Element pg = child(sp, W, "pgSz"), mar = child(sp, W, "pgMar"), cols = child(sp, W, "cols");
		long n = 1;
		try {
			if (cols != null && w(cols, "num") != null) n = Long.parseLong(w(cols, "num").trim());
		} catch (NumberFormatException e) {
			n = 1;
		}
		return new long[] { twips(pg, "w", 11906), twips(pg, "h", 16838), twips(mar, "top", 1440), twips(mar, "right", 1440),
				twips(mar, "bottom", 1440), twips(mar, "left", 1440), n };
	}

	private static long twips(Element e, String a, long dflt) {
		String v = e == null ? null : w(e, a);
		try {
			return (v == null || v.isEmpty() ? dflt : Long.parseLong(v.trim())) * 635;
		} catch (NumberFormatException ex) {
			return dflt * 635;
		}
	}

	private String offPageAnchor(Element d, long cx, long cy) {
		long[] page = pageOf(d);
		if (page == null) return Inspection.POSITION_NO_SECTION;
		List<String> verdicts = new ArrayList<String>();
		for (String axis : new String[] { "H", "V" }) {
			Element pos = child(d, WP, "position" + axis);
			if (pos == null || child(pos, WP, "align") != null) {
				verdicts.add("on");
				continue;
			}
			Element offset = child(pos, WP, "posOffset");
			long off;
			try {
				off = Long.parseLong(offset.getTextContent().trim());
			} catch (RuntimeException e) {
				// no offset to read (a percentage offset, say): neither provably on nor off
				verdicts.add(Inspection.POSITION_INDETERMINATE);
				continue;
			}
			verdicts.add(axisVerdict(page, axis, attr(pos, null, "relativeFrom"), off, axis.equals("H") ? cx : cy));
		}
		return combine(verdicts);
	}

	private String offPageVml(Element s, Map<String, String> style) {
		long[] page = pageOf(s);
		if (page == null) return Inspection.POSITION_NO_SECTION;
		List<String> verdicts = new ArrayList<String>();
		for (String axis : new String[] { "H", "V" }) {
			String name = axis.equals("H") ? "horizontal" : "vertical";
			String align = style.get("mso-position-" + name);
			if (align != null && !align.equals("absolute")) {
				verdicts.add("on");
				continue;
			}
			Double off = lengthUnit(style.getOrDefault(axis.equals("H") ? "margin-left" : "margin-top", "0"));
			Double extent = lengthUnit(style.getOrDefault(axis.equals("H") ? "width" : "height", "0"));
			String rel = style.getOrDefault("mso-position-" + name + "-relative", "text");
			if (rel.equals("text")) rel = axis.equals("H") ? "column" : "paragraph";
			verdicts.add(axisVerdict(page, axis, rel, Math.round((off == null ? 0 : off) * EMU_PER_PT),
					Math.round((extent == null ? 0 : extent) * EMU_PER_PT)));
		}
		return combine(verdicts);
	}

	private static String axisVerdict(long[] page, String axis, String rel, long off, long extent) {
		boolean h = axis.equals("H");
		long size = h ? page[0] : page[1], loMargin = h ? page[5] : page[2], hiMargin = h ? page[3] : page[4], columns = page[6];
		long lo, hi;
		String r = rel == null ? "" : rel;
		if (r.equals("page")) {
			lo = hi = off;
		} else if (r.equals("margin") || r.equals("leftMargin") || r.equals("topMargin") || r.equals("insideMargin")) {
			lo = hi = loMargin + off;
		} else if (r.equals("rightMargin") || r.equals("bottomMargin") || r.equals("outsideMargin")) {
			lo = hi = size - hiMargin + off;
		} else if (r.equals("column") && h && columns == 1) {
			lo = hi = loMargin + off; // one column: its left edge is the left margin
		} else {
			// paragraph, line, character, a column of several: anchored anywhere in the text area
			lo = loMargin + off;
			hi = size - hiMargin + off;
		}
		if (hi + extent <= 0 || lo >= size) return "off";
		if (lo + extent > 0 && hi < size) return "on";
		return Inspection.POSITION_INDETERMINATE;
	}

	private static String combine(List<String> verdicts) {
		if (verdicts.contains("off")) return "off";
		if (verdicts.contains(Inspection.POSITION_INDETERMINATE)) return Inspection.POSITION_INDETERMINATE;
		return null;
	}

	// --- content controls ---------------------------------------------------------------------------

	private void loadBindings() {
		Map<String, String> stores = new HashMap<String, String>();
		for (String cx : pkg.byType(main, "/customXml")) {
			String ct = pkg.contentType(cx);
			if (ct != null && !ct.equals("application/xml") && !cx.endsWith(".xml")) continue;
			if ("application/inkml+xml".equals(ct)) continue;
			for (String props : pkg.byType(cx, "/customXmlProps")) {
				Element x = pkg.xml(props);
				if (x != null && attr(x, InspectXml.CUSTOM_XML, "itemID") != null) stores.put(attr(x, InspectXml.CUSTOM_XML, "itemID"), cx);
			}
		}
		if (stores.isEmpty()) return;
		Element doc = pkg.xml(main);
		part = main;
		for (Element sdt : iter(doc, W, "sdt")) {
			Element db = child(child(sdt, W, "sdtPr"), W, "dataBinding");
			if (db == null) continue;
			String store = stores.get(w(db, "storeItemID"));
			if (store == null || pkg.xml(store) == null) continue;
			String value = bound(pkg.xml(store).getOwnerDocument(), w(db, "xpath"), w(db, "prefixMappings"));
			if (value == null) continue;
			String surface = text(child(sdt, W, "sdtContent"));
			if (!surface.equals(value)) stale.add(sdt);
		}
	}

	/** A binding's value: the string value of the first node its XPath selects in the store, or null. */
	private static String bound(Document store, String xpath, String mappings) {
		if (xpath == null) return null;
		try {
			XPath x = org.docx4j.utils.XPathFactoryUtil.newXPath();
			x.setNamespaceContext(new BoundValues.Mappings(mappings));
			NodeList nodes;
			synchronized (store) {
				nodes = (NodeList) x.evaluate(xpath, store, XPathConstants.NODESET);
			}
			if (nodes == null || nodes.getLength() == 0) return null;
			Node n = nodes.item(0);
			return n instanceof Attr ? ((Attr) n).getValue() : n.getTextContent();
		} catch (Exception e) {
			return null;
		}
	}

	private void walkControls(String p, Element root, Set<String> needs) {
		for (Element sdt : iter(root, W, "sdt")) {
			Element content = child(sdt, W, "sdtContent");
			if (content == null) continue;
			Set<String> dn = contextNeeds(sdt, root, needs);
			if (stale.contains(sdt)) {
				add(found("sdt.stored_surface", p, path(content), text(content), plus(dn, InspectFinding.SURFACE), content));
			} else if (isOn(child(child(sdt, W, "sdtPr"), W, "showingPlcHdr"))) {
				add(found("sdt.placeholder", p, path(content), text(content), plus(dn, InspectFinding.PLACEHOLDERS), content));
			}
		}
	}

	// --- revisions ----------------------------------------------------------------------------------

	private void walkRevisions(String p, Element root, Set<String> needs) {
		for (Element e : iter(root, REVISION_MARKS)) {
			String author = w(e, "author");
			if (author == null) continue;
			Object[] a = authors.get(author);
			if (a == null) authors.put(author, a = new Object[] { p, e, new ArrayList<String>() });
			if (w(e, "date") != null && !w(e, "date").isEmpty()) {
				@SuppressWarnings("unchecked")
				List<String> dates = (List<String>) a[2];
				dates.add(w(e, "date"));
			}
		}
		Map<String, List<Found>> groups = new LinkedHashMap<String, List<Found>>();
		for (Element e : iter(root, W, "del", W, "moveFrom")) {
			Element up = parent(e);
			if (up != null && (is(up, W, "rPr") || is(up, W, "trPr"))) continue;
			String kind = is(e, W, "del") ? "revision.deleted" : "revision.moved_from";
			for (Element r : iter(e, W, "r")) {
				String t = displayText(r);
				if (t.isEmpty()) continue;
				if (InspectXml.ancestor(r, W, "del", W, "moveFrom") != e) continue;
				part = p;
				int[] cps = t.codePoints().toArray();
				emitRun(groups, kind, r, cps, 0, cps.length, contextNeeds(r, root, needs), Arrays.<Object>asList(w(e, "author"), w(e, "date")), null);
			}
		}
		for (List<Found> g : groups.values()) for (Found f : g) add(f);
	}

	// --- collapsed headings -------------------------------------------------------------------------

	private Integer outlineLevel(Element p) {
		Element lvl = pPrProp(p, "outlineLvl");
		if (lvl == null) return null;
		try {
			int v = Integer.parseInt(w(lvl, "val").trim());
			return v < 9 ? v : null;
		} catch (RuntimeException e) {
			return null;
		}
	}

	private void walkCollapsed(String p, Element root, Set<String> needs) {
		Element body = is(root, W, "body") || is(root, W, "hdr") || is(root, W, "ftr") ? root : child(root, W, "body");
		if (body == null) return;
		List<Element> blocks = new ArrayList<Element>();
		for (Element b : children(body)) if (is(b, W, "p") || is(b, W, "tbl") || is(b, W, "sdt")) blocks.add(b);
		int i = 0;
		while (i < blocks.size()) {
			Element b = blocks.get(i);
			Integer level = is(b, W, "p") ? outlineLevel(b) : null;
			Element ppr = is(b, W, "p") ? child(b, W, "pPr") : null;
			Boolean direct = ppr == null ? null : on(child(ppr, W15, "collapsed"));
			boolean collapsed = level != null && (direct != null ? direct : styleCollapsed(b));
			if (!collapsed) {
				i++;
				continue;
			}
			int j = i + 1;
			while (j < blocks.size()) {
				Element n = blocks.get(j);
				Integer nl = is(n, W, "p") ? outlineLevel(n) : null;
				if (nl != null && nl <= level) break;
				j++;
			}
			List<Element> bodyBlocks = blocks.subList(i + 1, j);
			if (!bodyBlocks.isEmpty()) {
				List<String> texts = new ArrayList<String>();
				for (Element x : bodyBlocks) texts.add(text(x));
				Element first = bodyBlocks.get(0), last = bodyBlocks.get(bodyBlocks.size() - 1);
				Found f = found("paragraph.collapsed", p, path(first), String.join("\n", texts), contextNeeds(b, root, needs), first);
				f.endPath = bodyBlocks.size() > 1 ? path(last) : "";
				f.summary = String.valueOf(bodyBlocks.size());
				add(f);
			}
			i = j;
		}
	}

	private boolean styleCollapsed(Element p) {
		boolean found = false;
		for (Element s : styles.chain(paragraphStyle(p))) {
			Element x = child(s, W, "pPr");
			if (x != null && child(x, W15, "collapsed") != null) found = isOn(child(x, W15, "collapsed"));
		}
		return found;
	}

	// --- the inventory ------------------------------------------------------------------------------

	// --- what Office would run or fetch, found first (phase 2) -----------------------------------------

	/** The findings made ahead of their place in the report, by the key {@link #place} is called with. */
	private final Map<String, List<Found>> early = new HashMap<String, List<Found>>();

	/**
	 * What Office would run or fetch that the package holds outside its stories, found before them so that
	 * a global limit reached in the stories never drops it (Jason, 20 October 2026): a macro project, an
	 * ActiveX control, every external relationship (a web link's with them), {@code w:updateFields}. Each
	 * is counted at once and placed in the report where the inventory (the settings, for
	 * {@code w:updateFields}) puts it. An external field and a chunk's script are found where they are read.
	 */
	private void activeFirst(boolean word) {
		for (String p : pkg.partNames()) {
			String ctype = pkg.contentType(p) == null ? "" : pkg.contentType(p);
			if (ctype.equals("application/vnd.ms-office.vbaProject")) {
				early(p, found("part.macro", p, "", "", needs(InspectFinding.NEVER), null));
				pkg.done.add(p); // its content is not decoded by design (payload.opaque)
			} else if (ctype.equals("application/vnd.ms-office.activeX+xml")) {
				early(p, found("part.activex", p, "", "", needs(InspectFinding.NEVER), null));
				pkg.done.add(p);
			}
		}
		List<String> sources = new ArrayList<String>();
		sources.add("/");
		sources.addAll(pkg.partNames());
		for (String p : sources) {
			String rp = pkg.relsPart(p);
			for (InspectPackage.Rel r : pkg.rels(p)) {
				if (!r.external) continue;
				boolean webLink = r.type.endsWith("/hyperlink") && web(r.target);
				early(rp, found(webLink ? "hyperlink.web" : "relationship.external", rp, path(rp, r.element), r.target, needs(InspectFinding.NEVER),
						r.element)).subject = r.target;
			}
			pkg.done.add(rp); // a relationships part holds nothing else the inspection reads
		}
		if (word) {
			List<String> st = pkg.byType(main, "/settings");
			Element settings = st.isEmpty() ? null : pkg.xml(st.get(0));
			Element uf = child(settings, W, "updateFields");
			if (isOn(uf)) early(st.get(0) + "#updateFields", found("settings.update_fields", st.get(0), path(st.get(0), uf), "", needs(InspectFinding.NEVER), uf));
		}
	}

	private Found early(String key, Found f) {
		f.needs.addAll(baseNeeds);
		f.store = pkg;
		run.addEarly(f);
		early.computeIfAbsent(key, k -> new ArrayList<Found>()).add(f);
		return f;
	}

	/** The findings made ahead for this key, at their place in the report. */
	private void place(String key) {
		List<Found> l = early.remove(key);
		if (l != null) for (Found f : l) run.place(f);
	}

	private void inventory() {
		List<String> parts = pkg.partNames();
		Map<String, String> objects = oleObjects();
		Set<String> chunks = new HashSet<String>();
		for (String p : parts) for (InspectPackage.Rel r : pkg.rels(p)) if (r.type.endsWith("/aFChunk") && !r.external) chunks.add(r.target);
		for (String p : parts) {
			String ctype = pkg.contentType(p) == null ? "" : pkg.contentType(p);
			if (ctype.equals("application/vnd.ms-office.vbaProject") || ctype.equals("application/vnd.ms-office.activeX+xml")) {
				place(p); // found first (activeFirst)
			} else if (ctype.equals("application/inkml+xml")) {
				add(found("ink", p, "", "", needs(InspectFinding.NEVER), null));
			} else if (ctype.endsWith("wordprocessingml.people+xml")) {
				List<String> people = new ArrayList<String>();
				for (Element e : iter(pkg.xml(p), W15, "person")) if (attr(e, W15, "author") != null && !attr(e, W15, "author").isEmpty()) people.add(attr(e, W15, "author"));
				add(found("people", p, "", String.join("\n", people), needs(InspectFinding.NEVER), null));
			} else if (ctype.equals("application/vnd.openxmlformats-package.core-properties+xml")
					|| ctype.equals("application/vnd.openxmlformats-officedocument.extended-properties+xml")
					|| ctype.equals("application/vnd.openxmlformats-officedocument.custom-properties+xml")) {
				List<String> values = values(pkg.xml(p));
				if (!values.isEmpty()) add(found("properties", p, "", String.join("\n", values), needs(chain.isEmpty() ? InspectFinding.METADATA : InspectFinding.NEVER), null));
			} else if (ctype.equals("application/xml") && main != null && pkg.byType(main, "/customXml").contains(p)) {
				add(found("custom_xml", p, "", String.join("\n", values(pkg.xml(p))), needs(InspectFinding.NEVER), null));
			} else if (!known(ctype)) {
				Found f = add(found("part.unknown", p, "", "", needs(InspectFinding.NEVER), null));
				f.summary = ctype;
			}
			if (chunks.contains(p)) chunk(p, ctype);
			if (objects.containsKey(p)) embedded(p, objects.get(p));
			pkg.done.add(p);
		}
		List<String> sources = new ArrayList<String>();
		sources.add("/");
		sources.addAll(parts);
		for (String p : sources) {
			String rp = pkg.relsPart(p);
			place(rp); // its external relationships, found first (activeFirst)
			pkg.done.add(rp);
		}
	}

	private static boolean known(String ctype) {
		for (String k : KNOWN_TYPES) if (ctype.startsWith(k)) return true;
		return false;
	}

	private static List<String> values(Element root) {
		List<String> out = new ArrayList<String>();
		if (root == null) return out;
		for (String t : InspectXml.texts(root)) if (!t.strip().isEmpty()) out.add(t.strip());
		return out;
	}

	/** Each embedded part an {@code o:OLEObject} of {@code Type="Embed"} refers to, to its ProgID. */
	private Map<String, String> oleObjects() {
		Map<String, String> out = new LinkedHashMap<String, String>();
		if (main == null) return out;
		List<String> parts = new ArrayList<String>();
		parts.add(main);
		parts.addAll(pkg.byType(main, "/header"));
		parts.addAll(pkg.byType(main, "/footer"));
		for (String p : parts) {
			Element x = pkg.xml(p);
			if (x == null) continue;
			for (Element ole : iter(x, O, "OLEObject")) {
				if ("Link".equals(attr(ole, null, "Type"))) continue;
				String target = pkg.target(p, attr(ole, R, "id"));
				if (target != null) out.put(target, attr(ole, null, "ProgID"));
			}
		}
		return out;
	}

	// --- chunks and embedded packages, one level down -------------------------------------------------

	private void chunk(String p, String ctype) {
		Found f = add(found("altchunk", p, "", "", needs(InspectFinding.NEVER), null));
		f.summary = ctype;
		String where = loc(p, "");
		if (depth >= 1) {
			run.skip(Inspection.DEPTH, where);
			return;
		}
		byte[] data = pkg.bytes(p);
		if (data == null) {
			run.skip(Inspection.UNREADABLE, where);
			return;
		}
		List<String> html = new ArrayList<String>();
		try {
			if (ctype.equals("text/html") || ctype.equals("application/xhtml+xml")) {
				run.payload().add(data.length);
				html.add(InspectPayloads.decode(data));
			} else if (ctype.equals("message/rfc822")) {
				run.payload().add(data.length);
				html.addAll(InspectPayloads.mhtHtml(data));
			} else if (ctype.equals(DOCX_CHUNK) || ctype.equals("application/xml") && contains(data, "pkg:package")) {
				nested(p, data, InspectFinding.ALTCHUNKS);
				return;
			} else if (ctype.equals("text/plain")) {
				run.payload().add(data.length);
				return;
			} else {
				run.skip(Inspection.NOT_DECODED, where);
				return;
			}
		} catch (InspectRun.PayloadLimit limit) {
			run.skip(Inspection.LIMIT_PAYLOAD_BYTES, where);
			return;
		}
		boolean script = false;
		List<String> refs = new ArrayList<String>();
		for (String h : html) {
			InspectPayloads.Scan s = InspectPayloads.html(h);
			script |= s.script;
			refs.addAll(s.refs);
		}
		if (script) add(found("altchunk.script", p, "", "", needs(InspectFinding.NEVER), null));
		if (!refs.isEmpty()) {
			Found r = add(found("altchunk.external_ref", p, "", "", needs(InspectFinding.NEVER), null));
			r.summary = String.join(" ", refs);
		}
		hidden(p, ctype, data, f, where);
	}

	/**
	 * What {@code display: none} hides in an HTML, XHTML or MHT chunk (CR-053 phase 3, WD REQ-902): the
	 * chunk read through the import its extraction runs ({@link InspectChunks}), a {@code text.hidden}
	 * finding for each outermost hidden element with text, at its path in the parsed tree. The import is
	 * kept for the addresses, so that the extraction reads the chunk as it was inspected. Without an
	 * HTML reader, and where the chunk cannot be read, it is skipped; its scripts and references are found
	 * all the same.
	 */
	private void hidden(String p, String ctype, byte[] data, Found chunk, String where) {
		InspectChunks.Read read = InspectChunks.read(data, ctype.equals("message/rfc822"), pkg.pkg, p, run);
		if (read.parsed) chunk.parsed = InspectFinding.PARSED_HTML;
		if (read.skipped != null) {
			run.skip(read.skipped, where);
			return;
		}
		Part part = pkg.part(p);
		if (part != null) run.chunks.put(part, read.scratch);
		for (InspectChunks.Hidden h : read.hidden) {
			Found t = add(found(InspectFinding.TEXT_HIDDEN, p, h.path, h.text,
					needs(InspectFinding.ALTCHUNKS, InspectFinding.HIDDEN, InspectFinding.HTML_READER), null));
			t.block = h.block;
		}
	}

	private static boolean contains(byte[] data, String s) {
		return new String(data, 0, Math.min(data.length, 1 << 20), StandardCharsets.ISO_8859_1).contains(s);
	}

	private void embedded(String p, String progId) {
		Found f = add(found("object.embedded", p, "", "", needs(InspectFinding.NEVER), null));
		f.summary = progId == null ? "" : progId;
		if (depth >= 1) {
			run.skip(Inspection.DEPTH, loc(p, ""));
			return;
		}
		byte[] data = pkg.bytes(p);
		if (data != null && data.length > 1 && data[0] == 'P' && data[1] == 'K') nested(p, data, InspectFinding.EMBEDDED);
	}

	/**
	 * A package one level down: its bytes decoded and counted first, then loaded by docx4j (the text
	 * model reads the same instance) and walked with the need its container adds.
	 */
	private void nested(String p, byte[] data, String need) {
		String where = loc(p, "");
		try {
			InspectRun.Payload payload = run.payload();
			if (data.length > 1 && data[0] == 'P' && data[1] == 'K') InspectPayloads.zip(data, payload);
			else payload.add(data.length);
		} catch (InspectRun.PayloadLimit limit) {
			run.skip(Inspection.LIMIT_PAYLOAD_BYTES, where);
			return;
		} catch (java.io.IOException e) {
			run.skip(Inspection.UNREADABLE, where);
			return;
		}
		OpcPackage inner;
		try {
			inner = OpcPackage.load(new ByteArrayInputStream(data));
		} catch (Exception | LinkageError e) {
			run.skip(Inspection.UNREADABLE, where);
			return;
		}
		Part holder = pkg.part(p);
		if (holder != null) run.loaded.put(holder, inner);
		List<String> innerChain = new ArrayList<String>(chain);
		innerChain.add(p);
		Set<String> innerNeeds = new LinkedHashSet<String>(baseNeeds);
		innerNeeds.add(need);
		new InspectWalk(new InspectPackage(inner, innerChain, run), run, depth + 1, innerNeeds).run();
	}

	// --- spreadsheets and presentations (inside a docx) -------------------------------------------------

	private void workbook(String wbPart) {
		Element wb = pkg.xml(wbPart);
		if (wb == null) return;
		List<String> strings = new ArrayList<String>();
		List<String> ss = pkg.byType(wbPart, "/sharedStrings");
		if (!ss.isEmpty() && pkg.xml(ss.get(0)) != null) {
			for (Element si : iter(pkg.xml(ss.get(0)), X, "si")) {
				StringBuilder sb = new StringBuilder();
				for (Element t : iter(si, X, "t")) sb.append(t.getTextContent());
				strings.add(sb.toString());
			}
		}
		for (Element sheet : iter(wb, X, "sheet")) {
			String state = attr(sheet, null, "state");
			String target = pkg.target(wbPart, attr(sheet, R, "id"));
			Element ws = target == null ? null : pkg.xml(target);
			if (ws == null) continue;
			boolean hidden = "hidden".equals(state) || "veryHidden".equals(state);
			if (hidden) {
				Found f = add(found("hidden".equals(state) ? "embedded.sheet_hidden" : "embedded.sheet_very_hidden", wbPart, path(wbPart, sheet),
						sheetText(ws, strings), needs(InspectFinding.HIDDEN), ws));
				f.summary = attr(sheet, null, "name") == null ? "" : attr(sheet, null, "name");
			}
			for (Element row : iter(ws, X, "row")) {
				if (attr(row, null, "hidden") == null || !Boolean.TRUE.equals(on(row, null, "hidden"))) continue;
				List<String> cells = new ArrayList<String>();
				for (Element c : iter(row, X, "c")) cells.add(cellText(c, strings));
				add(found("embedded.row_hidden", target, path(target, row), String.join("\t", cells), hidden ? needs(InspectFinding.HIDDEN) : needs(), row));
			}
			for (Element col : iter(ws, X, "col")) {
				if (attr(col, null, "hidden") == null || !Boolean.TRUE.equals(on(col, null, "hidden"))) continue;
				long lo = number(attr(col, null, "min")), hi = number(attr(col, null, "max"));
				List<String> cells = new ArrayList<String>();
				List<Element> in = new ArrayList<Element>();
				for (Element row : iter(ws, X, "row")) {
					for (Element c : iter(row, X, "c")) {
						int n = columnNumber(attr(c, null, "r"));
						if (n >= lo && n <= hi) {
							cells.add(cellText(c, strings));
							in.add(c);
						}
					}
				}
				Found f = add(found("embedded.column_hidden", target, path(target, col), String.join("\n", cells), hidden ? needs(InspectFinding.HIDDEN) : needs(), col));
				f.scope = in;
			}
		}
	}

	private static int columnNumber(String ref) {
		int n = 0;
		if (ref == null) return 0;
		for (int i = 0; i < ref.length() && Character.isLetter(ref.charAt(i)); i++) n = n * 26 + Character.toUpperCase(ref.charAt(i)) - 'A' + 1;
		return n;
	}

	private static String cellText(Element c, List<String> strings) {
		String t = attr(c, null, "t");
		if ("inlineStr".equals(t)) {
			StringBuilder sb = new StringBuilder();
			for (Element x : iter(c, X, "t")) sb.append(x.getTextContent());
			return sb.toString();
		}
		Element v = child(c, X, "v");
		if (v == null) return "";
		if ("s".equals(t)) {
			try {
				int i = Integer.parseInt(v.getTextContent().trim());
				return i >= 0 && i < strings.size() ? strings.get(i) : "";
			} catch (NumberFormatException e) {
				return "";
			}
		}
		return v.getTextContent();
	}

	private static String sheetText(Element ws, List<String> strings) {
		List<String> rows = new ArrayList<String>();
		for (Element row : iter(ws, X, "row")) {
			List<String> cells = new ArrayList<String>();
			for (Element c : iter(row, X, "c")) cells.add(cellText(c, strings));
			rows.add(String.join("\t", cells));
		}
		return String.join("\n", rows);
	}

	private void presentation(String presPart) {
		Element pres = pkg.xml(presPart);
		if (pres == null) return;
		for (Element sid : iter(pres, P, "sldId")) {
			String target = pkg.target(presPart, attr(sid, R, "id"));
			Element sld = target == null ? null : pkg.xml(target);
			if (sld == null) continue;
			String show = attr(sld, null, "show");
			if ("0".equals(show) || "false".equals(show)) add(found("embedded.slide_hidden", target, path(target, sld), drawingText(sld), needs(InspectFinding.HIDDEN), sld));
			for (String notes : pkg.byType(target, "/notesSlide")) {
				Element n = pkg.xml(notes);
				if (n == null) continue;
				String text = notesText(n);
				if (!text.isEmpty()) add(found("embedded.notes", notes, path(notes, n), text, needs(InspectFinding.NEVER), n));
			}
		}
	}

	private static String drawingText(Element root) {
		List<String> out = new ArrayList<String>();
		for (Element p : iter(root, A, "p")) {
			StringBuilder sb = new StringBuilder();
			for (Element t : iter(p, A, "t")) sb.append(t.getTextContent());
			out.add(sb.toString());
		}
		return String.join("\n", out);
	}

	private static String notesText(Element root) {
		List<String> parts = new ArrayList<String>();
		for (Element sp : iter(root, P, "sp")) {
			Element ph = InspectXml.first(sp, P, "ph");
			String type = attr(ph, null, "type");
			if (ph != null && type != null && Arrays.asList("sldImg", "sldNum", "hdr", "ftr", "dt").contains(type)) continue;
			String t = drawingText(sp);
			if (!t.trim().isEmpty()) parts.add(t);
		}
		return String.join("\n", parts);
	}
}
