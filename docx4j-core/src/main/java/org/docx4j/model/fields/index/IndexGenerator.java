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
package org.docx4j.model.fields.index;

import java.math.BigInteger;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.FieldInstruction;
import org.docx4j.model.pagination.Paginate;
import org.docx4j.model.pagination.PaginateSettings;
import org.docx4j.model.pagination.PaginationMap;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.toc.TocHelper;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTColumns;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.CTTabStop;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.STTabJc;
import org.docx4j.wml.STTabTlc;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPr.Type;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tabs;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * INDEX from the document's XE marks (CR-067 phase 3), the shape measured against Word 365's update of
 * 2026-10-28 (CR-067 section 14.2): one paragraph per entry in the Index 1, 2 and 3 styles (a parent entry
 * without pages of its own still listed), the entries sorted as Word sorts them (a collation that puts digits
 * before letters, ignores case and accents in the order and lists a lower-case entry before its upper-case twin),
 * {@code \e} between an entry and its pages (", "), {@code \l} between pages (", "), {@code \g} in a
 * {@code \r} range (an en dash), a {@code \t} cross-reference after the pages or, without pages, after
 * {@code \k} (". ", or {@code \e} where that is given); {@code \b} and {@code \i} page numbers bold and italic;
 * {@code \h} letter headings in the Index Heading style; {@code \r} run-in sub-entries; {@code \p} a letter range;
 * {@code \f} an index type; {@code \c} columns by continuous section breaks around the index.
 *
 * <p>Page numbers come from a {@link Pager}: {@link #paginating(WordprocessingMLPackage)} lays the document out
 * with docx4j-export-fo (as the TOC generator does); a caller with its own view of the pages supplies one. The
 * field's result is written in Word's shape: the begin, instruction and separate stay in their paragraph, the
 * entries follow as paragraphs, the end closes a paragraph of its own.</p>
 *
 * @since 17.3.5
 */
public final class IndexGenerator {

	private static final Logger log = LoggerFactory.getLogger(IndexGenerator.class);

	/** The page a paragraph starts on (from 1), or null where unknown (the mark is then listed without a page). */
	public interface Pager {
		Integer pageOf(P p);
	}

	private static final ObjectFactory factory = Context.getWmlObjectFactory();
	private static final Collator COLLATOR = Collator.getInstance(Locale.ENGLISH);
	static final String EN_DASH = "–";

	private final WordprocessingMLPackage pkg;
	private final Pager pager;

	public IndexGenerator(WordprocessingMLPackage pkg, Pager pager) {
		this.pkg = pkg;
		this.pager = pager;
	}

	/** A pager from docx4j-export-fo's layout, or null where that module is not on the classpath. */
	public static Pager paginating(WordprocessingMLPackage pkg) throws Docx4JException {
		if (!Docx4J.pdfViaFO()) return null;
		PaginationMap map = Paginate.compute(pkg, new PaginateSettings());
		List<P> paragraphs = Paginate.bodyParagraphs(pkg.getMainDocumentPart());
		List<String> keys = Paginate.keys(paragraphs);
		final Map<P, Integer> pages = new IdentityHashMap<P, Integer>();
		for (int i = 0; i < paragraphs.size(); i++) pages.put(paragraphs.get(i), map.getPage(keys.get(i)));
		return new Pager() {
			@Override public Integer pageOf(P p) {
				return pages.get(p);
			}
		};
	}

	// ---------------------------------------------------------------- the marks

	/** One XE mark. */
	static final class Mark {
		String[] path;
		Integer page, rangeEnd;
		boolean bold, italic;
		String xref, type;
	}

	/** The document's XE marks in order, with their pages. */
	List<Mark> marks() {
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		@SuppressWarnings("unchecked")
		List<P> paragraphs = (List<P>) (List<?>) TocHelper.getAllElementsFromObject(mdp.getJaxbElement().getBody(), P.class);
		// a bookmark's first and last paragraph, by name in lower case (for \r)
		Map<String, P[]> bookmarks = new HashMap<String, P[]>();
		Map<BigInteger, String> open = new HashMap<BigInteger, String>();
		for (P p : paragraphs) {
			for (Object o : p.getContent()) {
				Object u = XmlUtils.unwrap(o);
				if (u instanceof CTBookmark) {
					CTBookmark b = (CTBookmark) u;
					if (b.getName() == null) continue;
					String name = b.getName().toLowerCase(Locale.ROOT);
					open.put(b.getId(), name);
					bookmarks.put(name, new P[] { p, p });
				} else if (u instanceof CTMarkupRange && !(u instanceof CTBookmark)) {
					String name = open.get(((CTMarkupRange) u).getId());
					if (name != null && bookmarks.containsKey(name)) bookmarks.get(name)[1] = p;
				}
			}
		}
		List<Mark> out = new ArrayList<Mark>();
		for (P p : paragraphs) {
			for (String instr : instructions(p)) {
				FieldInstruction fi = FieldInstruction.parse(instr);
				if (!"XE".equals(fi.getName()) || fi.getArgument() == null) continue;
				Mark m = new Mark();
				m.path = fi.getArgument().split(":");
				for (int i = 0; i < m.path.length; i++) m.path[i] = m.path[i].trim();
				m.bold = fi.hasSwitch("\\b");
				m.italic = fi.hasSwitch("\\i");
				m.xref = fi.getSwitchValue("\\t");
				m.type = fi.getSwitchValue("\\f");
				String range = fi.getSwitchValue("\\r");
				if (range != null && bookmarks.containsKey(range.toLowerCase(Locale.ROOT))) {
					P[] b = bookmarks.get(range.toLowerCase(Locale.ROOT));
					m.page = pager == null ? null : pager.pageOf(b[0]);
					m.rangeEnd = pager == null ? null : pager.pageOf(b[1]);
					if (m.rangeEnd != null && m.page != null && m.rangeEnd.equals(m.page)) m.rangeEnd = null;
				} else if (m.xref == null) {
					m.page = pager == null ? null : pager.pageOf(p);
				}
				out.add(m);
			}
		}
		return out;
	}

	/** The instructions of the fields beginning in the paragraph (outer level), each the text up to its separate or end. */
	static List<String> instructions(P p) {
		List<String> out = new ArrayList<String>();
		int depth = 0;
		StringBuilder sb = null;
		for (Object o : p.getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (!(u instanceof R)) continue;
			for (Object c : ((R) u).getContent()) {
				Object v = XmlUtils.unwrap(c);
				if (v instanceof FldChar) {
					STFldCharType t = ((FldChar) v).getFldCharType();
					if (t == STFldCharType.BEGIN) {
						depth++;
						if (depth == 1) sb = new StringBuilder();
					} else if (t == STFldCharType.SEPARATE) {
						if (depth == 1 && sb != null) { out.add(sb.toString()); sb = null; }
					} else if (t == STFldCharType.END) {
						if (depth == 1 && sb != null) { out.add(sb.toString()); sb = null; }
						depth--;
					}
				} else if (v instanceof Text && depth == 1 && sb != null && c instanceof JAXBElement
						&& "instrText".equals(((JAXBElement<?>) c).getName().getLocalPart())) {
					sb.append(((Text) v).getValue());
				}
			}
		}
		return out;
	}

	// ---------------------------------------------------------------- the tree

	static final class Node {
		final String text;
		final Map<String, Node> children = new HashMap<String, Node>();
		/** page to {bold, italic} */
		final TreeMap<Integer, boolean[]> pages = new TreeMap<Integer, boolean[]>();
		/** range start to {end, bold, italic} */
		final TreeMap<Integer, int[]> ranges = new TreeMap<Integer, int[]>();
		final List<String> xrefs = new ArrayList<String>();
		boolean unpaged; // a mark whose page is unknown: listed without a page

		Node(String text) {
			this.text = text;
		}

		Node child(String part) {
			Node n = children.get(part);
			if (n == null) children.put(part, n = new Node(part));
			return n;
		}

		List<Node> sortedChildren() {
			List<Node> out = new ArrayList<Node>(children.values());
			Collections.sort(out, ORDER);
			return out;
		}

		boolean hasPagesOrXref() {
			return !pages.isEmpty() || !ranges.isEmpty() || !xrefs.isEmpty();
		}
	}

	static final Comparator<Node> ORDER = new Comparator<Node>() {
		@Override public int compare(Node a, Node b) {
			return COLLATOR.compare(a.text, b.text);
		}
	};

	Node tree(FieldInstruction index, List<Mark> marks) {
		Node root = new Node("");
		String type = index.getSwitchValue("\\f");
		for (Mark m : marks) {
			if (type == null ? m.type != null : !type.equalsIgnoreCase(m.type)) continue;
			Node n = root;
			for (String part : m.path) n = n.child(part);
			if (m.xref != null) {
				if (!n.xrefs.contains(m.xref)) n.xrefs.add(m.xref);
			} else if (m.page == null) {
				n.unpaged = true;
			} else if (m.rangeEnd != null) {
				n.ranges.put(m.page, new int[] { m.rangeEnd, m.bold ? 1 : 0, m.italic ? 1 : 0 });
			} else {
				boolean[] f = n.pages.get(m.page);
				if (f == null) n.pages.put(m.page, new boolean[] { m.bold, m.italic });
				else { f[0] |= m.bold; f[1] |= m.italic; }
			}
		}
		return root;
	}

	// ---------------------------------------------------------------- the paragraphs

	/** A piece of an entry line: text with its page-number formatting. */
	static final class Seg {
		final String text;
		final boolean bold, italic;
		Seg(String text, boolean bold, boolean italic) {
			this.text = text;
			this.bold = bold;
			this.italic = italic;
		}
	}

	/** The entry paragraphs for the instruction (section paragraphs for {@code \c} included). */
	public List<Object> generate(FieldInstruction index, SectPr enclosing) {
		Node root = tree(index, marks());
		String e = value(index, "\\e", ", "), l = value(index, "\\l", ", "), g = value(index, "\\g", EN_DASH);
		String k = index.getSwitchValue("\\k");
		if (k == null) k = index.getSwitchValue("\\e") == null ? ". " : e;
		String h = index.getSwitchValue("\\h");
		String p = index.getSwitchValue("\\p");
		boolean runIn = index.hasSwitch("\\r");
		int tabPos = tabPosition(enclosing);
		ensureStyles();

		List<Object> out = new ArrayList<Object>();
		String heading = null;
		for (Node top : root.sortedChildren()) {
			if (top.text.isEmpty()) continue;
			char first = top.text.charAt(0);
			if (p != null && !inLetterRange(first, p)) continue;
			if (h != null) {
				String hd = headingLetter(first);
				if (!hd.equals(heading)) {
					heading = hd;
					out.add(paragraph("IndexHeading", true, tabPos, Collections.singletonList(new Seg(h.trim().length() == 1 && Character.isLetter(h.trim().charAt(0)) ? hd : h, false, false))));
				}
			}
			if (runIn) {
				List<Seg> segs = new ArrayList<Seg>(line(top, e, l, g, k));
				List<Node> flat = new ArrayList<Node>();
				flatten(top, flat);
				if (!flat.isEmpty()) {
					segs.add(new Seg(top.hasPagesOrXref() ? "; " : ": ", false, false));
					for (int i = 0; i < flat.size(); i++) {
						if (i > 0) segs.add(new Seg("; ", false, false));
						segs.addAll(line(flat.get(i), e, l, g, k));
					}
				}
				out.add(paragraph("Index1", false, tabPos, segs));
			} else {
				render(top, 1, e, l, g, k, tabPos, out);
			}
		}
		String c = index.getSwitchValue("\\c");
		if (c != null && enclosing != null) {
			int columns = 1;
			try { columns = Integer.parseInt(c.trim()); } catch (NumberFormatException x) { /* one column */ }
			// the section before the index closes with the enclosing properties as they were; the index's own section
			// starts continuous with the columns; the enclosing section then continues continuous (measured)
			out.add(0, sectionParagraph(XmlUtils.deepCopy(enclosing), null));
			SectPr indexSect = XmlUtils.deepCopy(enclosing);
			setContinuous(indexSect);
			CTColumns cols = factory.createCTColumns();
			if (columns > 1) cols.setNum(BigInteger.valueOf(columns));
			cols.setSpace(BigInteger.valueOf(720));
			indexSect.setCols(cols);
			out.add(sectionParagraph(indexSect, null));
			setContinuous(enclosing);
		}
		return out;
	}

	private static void setContinuous(SectPr sectPr) {
		Type t = factory.createSectPrType();
		t.setVal("continuous");
		sectPr.setType(t);
	}

	private static P sectionParagraph(SectPr sectPr, Object unused) {
		P p = factory.createP();
		PPr pPr = factory.createPPr();
		pPr.setSectPr(sectPr);
		p.setPPr(pPr);
		return p;
	}

	private void render(Node n, int level, String e, String l, String g, String k, int tabPos, List<Object> out) {
		out.add(paragraph("Index" + Math.min(level, 9), false, tabPos, line(n, e, l, g, k)));
		for (Node child : n.sortedChildren()) render(child, level + 1, e, l, g, k, tabPos, out);
	}

	private static void flatten(Node n, List<Node> out) {
		for (Node child : n.sortedChildren()) {
			out.add(child);
			flatten(child, out);
		}
	}

	/** An entry's line: its text, its pages, its cross-references. */
	static List<Seg> line(Node n, String e, String l, String g, String k) {
		List<Seg> segs = new ArrayList<Seg>();
		segs.add(new Seg(n.text, false, false));
		List<Seg> pages = new ArrayList<Seg>();
		TreeMap<Integer, Object> ordered = new TreeMap<Integer, Object>();
		for (Map.Entry<Integer, boolean[]> p : n.pages.entrySet()) ordered.put(p.getKey(), p.getValue());
		for (Map.Entry<Integer, int[]> r : n.ranges.entrySet()) ordered.put(r.getKey(), r.getValue());
		for (Map.Entry<Integer, Object> p : ordered.entrySet()) {
			if (!pages.isEmpty()) pages.add(new Seg(l, false, false));
			if (p.getValue() instanceof boolean[]) {
				boolean[] f = (boolean[]) p.getValue();
				pages.add(new Seg(String.valueOf(p.getKey()), f[0], f[1]));
			} else {
				int[] r = (int[]) p.getValue();
				pages.add(new Seg(p.getKey() + g + r[0], r[1] == 1, r[2] == 1));
			}
		}
		if (!pages.isEmpty()) {
			segs.add(new Seg(e, false, false));
			segs.addAll(pages);
		}
		for (String x : n.xrefs) {
			segs.add(new Seg(pages.isEmpty() ? k : l, false, false));
			segs.add(new Seg(x, false, false));
		}
		return merge(segs);
	}

	/** Adjacent segments of one formatting joined. */
	private static List<Seg> merge(List<Seg> segs) {
		List<Seg> out = new ArrayList<Seg>();
		for (Seg s : segs) {
			if (s.text.isEmpty()) continue;
			Seg last = out.isEmpty() ? null : out.get(out.size() - 1);
			if (last != null && last.bold == s.bold && last.italic == s.italic) out.set(out.size() - 1, new Seg(last.text + s.text, s.bold, s.italic));
			else out.add(s);
		}
		return out;
	}

	/** The heading an entry sorts under: its first letter upper-cased, accents stripped ("éclair" under E, measured); else the character itself. */
	static String headingLetter(char first) {
		if (!Character.isLetter(first)) return String.valueOf(first);
		String plain = java.text.Normalizer.normalize(String.valueOf(first), java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		return (plain.isEmpty() ? String.valueOf(first) : plain).toUpperCase(Locale.ENGLISH);
	}

	static boolean inLetterRange(char first, String range) {
		String r = range.trim().toLowerCase(Locale.ROOT);
		if (!Character.isLetter(first)) return false;
		char c = Character.toLowerCase(first);
		int dash = r.indexOf('-');
		if (dash < 0) return r.length() > 0 && c == r.charAt(0);
		char from = r.charAt(0), to = r.charAt(r.length() - 1);
		return c >= from && c <= to;
	}

	private static String value(FieldInstruction index, String sw, String dflt) {
		String v = index.getSwitchValue(sw);
		return v == null ? dflt : v;
	}

	static int tabPosition(SectPr enclosing) {
		if (enclosing == null) return 9350;
		try {
			return new PageDimensions(enclosing).getWritableWidthTwips();
		} catch (RuntimeException e) {
			return 9350;
		}
	}

	static P paragraph(String style, boolean keepNext, int tabPos, List<Seg> segs) {
		P p = factory.createP();
		PPr pPr = factory.createPPr();
		PPrBase.PStyle ps = factory.createPPrBasePStyle();
		ps.setVal(style);
		pPr.setPStyle(ps);
		if (keepNext) pPr.setKeepNext(new BooleanDefaultTrue());
		Tabs tabs = factory.createTabs();
		CTTabStop tab = factory.createCTTabStop();
		tab.setVal(STTabJc.RIGHT);
		tab.setLeader(STTabTlc.DOT);
		tab.setPos(BigInteger.valueOf(tabPos));
		tabs.getTab().add(tab);
		pPr.setTabs(tabs);
		p.setPPr(pPr);
		for (Seg s : segs) {
			R r = factory.createR();
			RPr rPr = factory.createRPr();
			if (s.bold) { rPr.setB(new BooleanDefaultTrue()); rPr.setBCs(new BooleanDefaultTrue()); }
			if (s.italic) { rPr.setI(new BooleanDefaultTrue()); rPr.setICs(new BooleanDefaultTrue()); }
			rPr.setNoProof(new BooleanDefaultTrue());
			r.setRPr(rPr);
			Text t = factory.createText();
			t.setValue(s.text);
			if (s.text.startsWith(" ") || s.text.endsWith(" ")) t.setSpace("preserve");
			r.getContent().add(t);
			p.getContent().add(r);
		}
		return p;
	}

	// ---------------------------------------------------------------- the styles

	static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	static final String[][] STYLES = {
			{ "Index1", "<w:style " + W + " w:type=\"paragraph\" w:styleId=\"Index1\"><w:name w:val=\"index 1\"/><w:basedOn w:val=\"Normal\"/><w:next w:val=\"Normal\"/><w:autoRedefine/><w:uiPriority w:val=\"99\"/><w:semiHidden/><w:unhideWhenUsed/><w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/><w:ind w:left=\"220\" w:hanging=\"220\"/></w:pPr></w:style>" },
			{ "Index2", "<w:style " + W + " w:type=\"paragraph\" w:styleId=\"Index2\"><w:name w:val=\"index 2\"/><w:basedOn w:val=\"Normal\"/><w:next w:val=\"Normal\"/><w:autoRedefine/><w:uiPriority w:val=\"99\"/><w:semiHidden/><w:unhideWhenUsed/><w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/><w:ind w:left=\"440\" w:hanging=\"220\"/></w:pPr></w:style>" },
			{ "Index3", "<w:style " + W + " w:type=\"paragraph\" w:styleId=\"Index3\"><w:name w:val=\"index 3\"/><w:basedOn w:val=\"Normal\"/><w:next w:val=\"Normal\"/><w:autoRedefine/><w:uiPriority w:val=\"99\"/><w:semiHidden/><w:unhideWhenUsed/><w:pPr><w:spacing w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/><w:ind w:left=\"660\" w:hanging=\"220\"/></w:pPr></w:style>" },
			{ "IndexHeading", "<w:style " + W + " w:type=\"paragraph\" w:styleId=\"IndexHeading\"><w:name w:val=\"index heading\"/><w:basedOn w:val=\"Normal\"/><w:next w:val=\"Index1\"/><w:uiPriority w:val=\"99\"/><w:semiHidden/><w:unhideWhenUsed/><w:rPr><w:rFonts w:asciiTheme=\"majorHAnsi\" w:eastAsiaTheme=\"majorEastAsia\" w:hAnsiTheme=\"majorHAnsi\" w:cstheme=\"majorBidi\"/><w:b/><w:bCs/></w:rPr></w:style>" },
	};

	/** Word's index styles (as Word 365 adds them), where the document lacks them. */
	void ensureStyles() {
		StyleDefinitionsPart sdp = pkg.getMainDocumentPart().getStyleDefinitionsPart();
		if (sdp == null || sdp.getJaxbElement() == null) return;
		for (String[] s : STYLES) {
			if (sdp.getStyleById(s[0]) != null) continue;
			try {
				sdp.getJaxbElement().getStyle().add((Style) XmlUtils.unmarshalString(s[1]));
			} catch (Exception e) {
				log.warn("Could not add style " + s[0] + ": " + e.getMessage());
			}
		}
	}

	// ---------------------------------------------------------------- the fields

	/** An INDEX field found in the body: its begin paragraph and runs. */
	static final class Field {
		P begin;
		int beginRun, separateRun = -1, endRun = -1;
		String instr;
	}

	/**
	 * Regenerates every INDEX field in the main document part's body (a field in a table or a content control is
	 * left as it is).
	 *
	 * @return the number of fields regenerated
	 */
	public int update() throws Docx4JException {
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		List<Object> body = mdp.getContent();
		int count = 0;
		for (int i = 0; i < body.size(); i++) {
			Object u = XmlUtils.unwrap(body.get(i));
			if (!(u instanceof P)) continue;
			P p = (P) u;
			Field f = indexField(p);
			if (f == null) continue;
			FieldInstruction instr = FieldInstruction.parse(f.instr);
			SectPr enclosing = enclosingSectPr(body, i, mdp);
			List<Object> content = p.getContent();
			// the stale result: the runs after the separate (or the instruction) in this paragraph ...
			int from = f.separateRun >= 0 ? f.separateRun + 1 : f.beginRun + 1;
			if (f.separateRun < 0) {
				// skip the instruction runs
				while (from < content.size() && !(XmlUtils.unwrap(content.get(from)) instanceof R && isFldChar((R) XmlUtils.unwrap(content.get(from)), null))) from++;
			}
			P endP;
			if (f.endRun >= 0) {
				// ... up to the end in the same paragraph: the end moves to a paragraph of its own
				R end = (R) XmlUtils.unwrap(content.get(f.endRun));
				for (int j = f.endRun; j >= from; j--) content.remove(j);
				endP = factory.createP();
				endP.getContent().add(end);
				end.setParent(endP);
				body.add(i + 1, endP);
			} else {
				// ... or across the paragraphs up to the one holding the end
				for (int j = content.size() - 1; j >= from; j--) content.remove(j);
				int endAt = -1, depth = 1;
				for (int j = i + 1; j < body.size() && endAt < 0; j++) {
					Object v = XmlUtils.unwrap(body.get(j));
					if (!(v instanceof P)) continue;
					List<Object> c = ((P) v).getContent();
					for (int r = 0; r < c.size(); r++) {
						Object w = XmlUtils.unwrap(c.get(r));
						if (!(w instanceof R)) continue;
						if (isFldChar((R) w, STFldCharType.BEGIN)) depth++;
						else if (isFldChar((R) w, STFldCharType.END) && --depth == 0) {
							for (int q = r - 1; q >= 0; q--) c.remove(q); // the stale result's tail
							endAt = j;
							break;
						}
					}
				}
				if (endAt < 0) {
					log.warn("INDEX field has no end; left as it is");
					continue;
				}
				for (int j = endAt - 1; j > i; j--) body.remove(j);
				endP = (P) XmlUtils.unwrap(body.get(i + 1));
			}
			if (f.separateRun < 0) content.add(fldChar(STFldCharType.SEPARATE));
			List<Object> generated = generate(instr, enclosing);
			for (Object g : generated) {
				if (g instanceof org.jvnet.jaxb.lang.Child) ((org.jvnet.jaxb.lang.Child) g).setParent(mdp.getJaxbElement().getBody());
			}
			body.addAll(i + 1, generated);
			i += generated.size() + 1;
			count++;
		}
		return count;
	}

	/** The section the paragraph at {@code index} is in: the first sectPr at or after it, else the body's. */
	static SectPr enclosingSectPr(List<Object> body, int index, MainDocumentPart mdp) {
		for (int j = index; j < body.size(); j++) {
			Object u = XmlUtils.unwrap(body.get(j));
			if (u instanceof P && ((P) u).getPPr() != null && ((P) u).getPPr().getSectPr() != null) return ((P) u).getPPr().getSectPr();
		}
		return mdp.getJaxbElement().getBody().getSectPr();
	}

	static Field indexField(P p) {
		List<Object> content = p.getContent();
		int depth = 0;
		Field f = null;
		StringBuilder sb = null;
		for (int i = 0; i < content.size(); i++) {
			Object u = XmlUtils.unwrap(content.get(i));
			if (!(u instanceof R)) continue;
			R r = (R) u;
			if (isFldChar(r, STFldCharType.BEGIN)) {
				depth++;
				if (depth == 1 && f == null) { sb = new StringBuilder(); f = new Field(); f.begin = p; f.beginRun = i; }
			} else if (isFldChar(r, STFldCharType.SEPARATE)) {
				if (depth == 1 && f != null && f.instr == null) { f.instr = sb.toString(); f.separateRun = i; if (!isIndex(f)) { f = null; } }
			} else if (isFldChar(r, STFldCharType.END)) {
				if (depth == 1 && f != null) {
					if (f.instr == null) { f.instr = sb.toString(); if (!isIndex(f)) f = null; }
					if (f != null) { f.endRun = i; return f; }
				}
				depth--;
			} else if (depth == 1 && f != null && f.instr == null) {
				for (Object c : r.getContent()) {
					if (c instanceof JAXBElement && "instrText".equals(((JAXBElement<?>) c).getName().getLocalPart())) sb.append(((Text) ((JAXBElement<?>) c).getValue()).getValue());
				}
			}
		}
		if (f != null && f.instr == null) return null; // a begin with no instruction completed here
		return f;
	}

	private static boolean isIndex(Field f) {
		return "INDEX".equals(FieldInstruction.parse(f.instr).getName());
	}

	static boolean isFldChar(R r, STFldCharType type) {
		for (Object c : r.getContent()) {
			Object u = XmlUtils.unwrap(c);
			if (u instanceof FldChar && (type == null || ((FldChar) u).getFldCharType() == type)) return true;
		}
		return false;
	}

	static R fldChar(STFldCharType type) {
		R r = factory.createR();
		FldChar fc = factory.createFldChar();
		fc.setFldCharType(type);
		r.getContent().add(fc);
		return r;
	}
}
