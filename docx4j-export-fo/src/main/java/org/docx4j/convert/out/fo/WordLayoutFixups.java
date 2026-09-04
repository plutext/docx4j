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
package org.docx4j.convert.out.fo;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.docx4j.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Whole-document adjustments to the generated XSL FO that reproduce Word's
 * page-level spacing rules, applied after both the XSLT and the visitor
 * pathway have produced the FO and before FOP sees it.  Each rule below was
 * measured against Word 365 output (CR-001 harness, spacing-page-top and
 * spacing-in-table probes):
 *
 * <ol>
 * <li><b>Hard page break.</b> A paragraph consisting only of a page break
 *   (w:br type="page") was emitted as an empty block carrying break-before,
 *   which put an empty line at the top of the new page and kept the next
 *   paragraph's space-before (it was no longer at the start of the page).
 *   Word shows no such line, and in compatibility mode 15 (Word 2013+) drops
 *   the space-before of the paragraph after a hard break; earlier modes keep
 *   it.  The empty block is removed and its break moved to the next block,
 *   with space-before.conditionality="retain" in modes below 15.</li>
 * <li><b>Top of the first page of a section.</b> Word applies the first
 *   paragraph's space-before there (measured: 36pt before on the first
 *   paragraph of a document is honoured).  XSL FO discards it, so the first
 *   block of each flow gets space-before.conditionality="retain".  (After a
 *   next-page section break Word measured 26pt for a 36pt space-before with
 *   a 10pt space-after on the section-break paragraph; the rule for that
 *   reduction is not yet established, so retain is an approximation there.)
 *   Natural page tops and pageBreakBefore keep the FO default, discard,
 *   which is what Word does.</li>
 * <li><b>Table cells.</b> Word applies a paragraph's space-before at the top
 *   of a cell, and (mode 15) its space-after at the bottom; FO discards both
 *   at the cell edges.  The first block of a cell gets
 *   space-before.conditionality="retain", the last space-after.conditionality
 *   ="retain" (mode 15 and later).</li>
 * </ol>
 *
 * Disable with docx4j property docx4j.convert.out.fo.wordLayoutFixups=false.
 *
 * @since 17.0.5
 */
public final class WordLayoutFixups {

	private static final Logger log = LoggerFactory.getLogger(WordLayoutFixups.class);

		public static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

		/** Hint attributes XsltFOFunctions puts on blocks for this pass (stripped here).  Plain
	 *  names, not namespaced: Xalan drops the namespace declaration when it copies the
	 *  fragment in the XSLT pathway, leaving an unbound prefix. */
		public static final String HINT_PSTYLE = "docx4j-pstyle";
	public static final String HINT_CONTEXTUAL = "docx4j-contextual";
	/** "b", "a" or "ba": which of the paragraph's spacings are HTML auto spacing. */
	public static final String HINT_AUTOSPACING = "docx4j-autospacing";
	/** "1" on the paragraph block of a list item. */
	public static final String HINT_LIST = "docx4j-list";

	/** docx4j.convert.out.fo.wordLayoutFixups (default true).  When false, no hints are
	 *  stamped and the pass is skipped entirely. */
	public static boolean isEnabled() {
		return org.docx4j.Docx4jProperties.getProperty("docx4j.convert.out.fo.wordLayoutFixups", true);
	}

	private WordLayoutFixups() {}

	/** Parse, fix, and re-serialise (without indentation, so white-space handling is unchanged). */
	public static String apply(String foDocument, int compatibilityMode) {
		try {
			Document doc = XmlUtils.getNewDocumentBuilder().parse(new InputSource(new StringReader(foDocument)));
			apply(doc, compatibilityMode);
			Transformer t = XmlUtils.getTransformerFactory().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "no");
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			StringWriter sw = new StringWriter(foDocument.length() + 1024);
			t.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (Exception e) {
			log.warn("Word layout fixups skipped: " + e.getMessage(), e);
			return foDocument;
		}
	}

			public static void apply(Document doc, int compatibilityMode) {
		mergePageBreakParagraphs(doc, compatibilityMode);
		applyContextualSpacing(doc);
		applyAutoSpacingBetweenListItems(doc);
		retainSpaceBeforeAtFlowStart(doc);
				retainSpacingAtCellEdges(doc, compatibilityMode);
		fixLists(doc);
		clipExactRows(doc);
		stripHints(doc);
	}

	/** "docx4j-row-exact" on a table-row (TrHeight): the row must be exactly that tall. */
	public static final String HINT_ROW_EXACT = "docx4j-row-exact";

	// ------------------------------------------------------------ 5. exact row heights

	/**
	 * FOP grows a row to its content whatever its height says; Word keeps an
	 * "exact" row at its height and draws the overflow over the following rows.
	 * Wrap each cell's content in a block-container of that height with
	 * overflow="hidden": the overflow is clipped rather than drawn, but the row and
	 * everything below it are where Word puts them.
	 */
	static void clipExactRows(Document doc) {
		for (Element row : elements(doc, "table-row")) {
			String h = row.getAttribute(HINT_ROW_EXACT);
			row.removeAttribute(HINT_ROW_EXACT);
			if (h == null || h.length() == 0) continue;
			double heightPt = lengthPt(h);
			if (heightPt <= 0) continue;
			NodeList cells = row.getChildNodes();
			for (int i = 0; i < cells.getLength(); i++) {
				if (!(cells.item(i) instanceof Element) || !isFo((Element) cells.item(i), "table-cell")) continue;
				Element cell = (Element) cells.item(i);
				double inner = heightPt - lengthPt(cell.getAttribute("padding-top")) - lengthPt(cell.getAttribute("padding-bottom"))
						- lengthPt(cell.getAttribute("border-top-width")) - lengthPt(cell.getAttribute("border-bottom-width"));
				Element container = doc.createElementNS(FO_NS, "fo:block-container");
				container.setAttribute("block-progression-dimension", org.docx4j.fonts.WordLineMetrics.format(Math.max(0.1, inner)));
				container.setAttribute("overflow", "hidden");
				while (cell.getFirstChild() != null) container.appendChild(cell.getFirstChild());
				cell.appendChild(container);
			}
		}
	}

	/** Remove the hint attributes whether or not the rules ran (FOP must not see them). */
	public static void stripHints(Document doc) {
		for (Element block : elements(doc, "block")) {
			block.removeAttribute(HINT_PSTYLE);
			block.removeAttribute(HINT_CONTEXTUAL);
			block.removeAttribute(HINT_AUTOSPACING);
			block.removeAttribute(HINT_LIST);
		}
	}

	// ------------------------------------------------------------ 0b. auto spacing in lists

	/**
	 * Word drops HTML auto spacing between consecutive list items (measured:
	 * 14pt before the first item and after the last, 0 between items), the way
	 * contextual spacing works.
	 */
	static void applyAutoSpacingBetweenListItems(Document doc) {
		for (Element flow : elements(doc, "flow")) autoSpacingAmong(flow);
		for (Element cell : elements(doc, "table-cell")) autoSpacingAmong(cell);
	}

	private static void autoSpacingAmong(Element container) {
		List<Element> paras = paragraphBlocks(container);
		for (int i = 0; i + 1 < paras.size(); i++) {
			Element a = paras.get(i), b = paras.get(i + 1);
			if (!"1".equals(a.getAttribute(HINT_LIST)) || !"1".equals(b.getAttribute(HINT_LIST))) continue;
			boolean aAuto = a.getAttribute(HINT_AUTOSPACING).indexOf('a') >= 0;
			boolean bAuto = b.getAttribute(HINT_AUTOSPACING).indexOf('b') >= 0;
			if (aAuto && bAuto) {
				a.setAttribute("space-after", "0pt");
				b.setAttribute("space-before", "0pt");
			}
		}
	}

	// ------------------------------------------------------------ 4. lists

	/**
	 * docx4j puts the paragraph's properties on the block inside
	 * fo:list-item-body, where FOP does not apply space-before/space-after (the
	 * list showed no spacing at all: measured 13.4pt before a list item with 14pt
	 * auto spacing, against Word's 27.8).  Move them to the fo:list-block, which
	 * is the flow-level object (one list item per list-block in docx4j's output),
	 * and give the label block the body's line-height so both sit on one line.
	 */
	static void fixLists(Document doc) {
		for (Element listBlock : elements(doc, "list-block")) {
			Element body = null, label = null;
			for (Element b : descendants(listBlock, "list-item-body")) { body = firstBlock(b); break; }
			for (Element l : descendants(listBlock, "list-item-label")) { label = firstBlock(l); break; }
			if (body == null) continue;
			for (String name : new String[] { "space-before", "space-after",
					"space-before.conditionality", "space-after.conditionality" }) {
				if (body.hasAttribute(name)) {
					listBlock.setAttribute(name, body.getAttribute(name));
					body.removeAttribute(name);
				}
			}
			if (label != null && !label.hasAttribute("line-height") && body.hasAttribute("line-height")) {
				label.setAttribute("line-height", body.getAttribute("line-height"));
				if (body.hasAttribute("font-size")) label.setAttribute("font-size", body.getAttribute("font-size"));
			}
		}
	}

	// ------------------------------------------------------------ 0. contextual spacing

	/**
	 * w:contextualSpacing ("Don't add space between paragraphs of the same style"):
	 * no space between two paragraphs of the same style when either has it
	 * (ECMA-376 17.3.1.9 describes the flagged paragraph's own spacing; Word also
	 * drops the neighbour's, measured).
	 * Neighbours are the consecutive paragraphs of a flow or of a table cell; a list
	 * item's paragraph is the block inside its list-item-body.
	 */
	static void applyContextualSpacing(Document doc) {
		for (Element flow : elements(doc, "flow")) contextualSpacingAmong(flow);
		for (Element cell : elements(doc, "table-cell")) contextualSpacingAmong(cell);
	}

		/** The paragraph blocks of a flow or cell, in order (one per flow-level child that is a paragraph). */
	private static List<Element> paragraphBlocks(Element container) {
		List<Element> paras = new ArrayList<>();
		NodeList children = container.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (!(n instanceof Element)) continue;
			Element p = paragraphBlock((Element) n);
			if (p != null) paras.add(p);
		}
		return paras;
	}

	private static void contextualSpacingAmong(Element container) {
		List<Element> paras = paragraphBlocks(container);
		for (int i = 0; i + 1 < paras.size(); i++) {
			Element a = paras.get(i), b = paras.get(i + 1);
						String sa = a.getAttribute(HINT_PSTYLE), sb = b.getAttribute(HINT_PSTYLE);
			if (sa == null || sa.length() == 0 || !sa.equals(sb)) continue;
			// Measured (Word 365): the gap is zero when EITHER paragraph has it, not
			// just the side the spec's wording suggests: a contextual paragraph followed
			// by a non-contextual one of the same style with 12pt before got no gap.
			if ("1".equals(a.getAttribute(HINT_CONTEXTUAL)) || "1".equals(b.getAttribute(HINT_CONTEXTUAL))) {
				a.setAttribute("space-after", "0pt");
				b.setAttribute("space-before", "0pt");
			}
		}
	}

	/** The paragraph block this flow-level element stands for: itself, the block in a
	 *  list item's body, or the block inside a bidi block-container; null for tables etc. */
	private static Element paragraphBlock(Element el) {
		if (isFo(el, "block")) return el.hasAttribute(HINT_PSTYLE) ? el : null;
		if (isFo(el, "table")) return null;
		if (isFo(el, "list-block")) {
			for (Element body : descendants(el, "list-item-body")) {
				Element b = firstBlock(body);
				if (b != null) return b;
			}
			return null;
		}
		if (isFo(el, "block-container")) {
			Element b = firstBlock(el);
			return (b != null && b.hasAttribute(HINT_PSTYLE)) ? b : null;
		}
		return null;
	}

	private static List<Element> descendants(Element parent, String localName) {
		NodeList nl = parent.getElementsByTagNameNS(FO_NS, localName);
		List<Element> out = new ArrayList<>(nl.getLength());
		for (int i = 0; i < nl.getLength(); i++) out.add((Element) nl.item(i));
		return out;
	}

	// ------------------------------------------------------------ 1. hard page breaks

	static void mergePageBreakParagraphs(Document doc, int compatibilityMode) {
		List<Element> empties = new ArrayList<>();
		for (Element block : elements(doc, "block")) {
			if ("page".equals(block.getAttribute("break-before")) && isEmpty(block)) {
				empties.add(block);
			}
		}
		for (Element empty : empties) {
			Element next = nextElementSibling(empty);
			if (next == null || !isFo(next, "block")) {
				continue; // last thing in the flow: the break has nothing to move to; keep it
			}
			if (!next.hasAttribute("break-before") || "auto".equals(next.getAttribute("break-before"))) {
				next.setAttribute("break-before", "page");
			}
			if (compatibilityMode < 15 && hasSpace(next, "space-before")) {
				next.setAttribute("space-before.conditionality", "retain");
			}
			empty.getParentNode().removeChild(empty);
		}
	}

	// ------------------------------------------------------------ 2. flow start

		/**
	 * At the top of the first page of a section, Word applies the first paragraph's
	 * space-before reduced by the space-after of the last paragraph of the previous
	 * section (measured: 36pt before after a section-break paragraph with 0 / 10 /
	 * 20pt after gave 36 / 26 / 16pt; 6pt before after 20pt after gave 0).  On the
	 * first page of the document there is no previous paragraph, so the full value
	 * applies.  FO would discard it, hence conditionality="retain".
	 */
	static void retainSpaceBeforeAtFlowStart(Document doc) {
		double prevAfter = 0;
		for (Element flow : elements(doc, "flow")) {
			Element first = firstBlock(flow);
			if (first != null && hasSpace(first, "space-before")) {
				double before = Math.max(0, lengthPt(first.getAttribute("space-before")) - prevAfter);
				if (before > 0) {
					first.setAttribute("space-before", org.docx4j.fonts.WordLineMetrics.format(before));
					first.setAttribute("space-before.conditionality", "retain");
				} else {
					first.setAttribute("space-before", "0pt");
				}
			}
			Element last = lastBlock(flow);
			prevAfter = (last == null || !hasSpace(last, "space-after")) ? 0 : lengthPt(last.getAttribute("space-after"));
		}
	}

	/** The last fo:block in document order under this element, not descending into tables. */
	private static Element lastBlock(Element parent) {
		NodeList children = parent.getChildNodes();
		for (int i = children.getLength() - 1; i >= 0; i--) {
			Node n = children.item(i);
			if (!(n instanceof Element)) continue;
			Element el = (Element) n;
			if (isFo(el, "table")) return null;
			if (isFo(el, "block")) return el;
			Element inner = lastBlock(el);
			if (inner != null) return inner;
		}
		return null;
	}

	/** An FO length in points; 0 if unparseable. */
	static double lengthPt(String v) {
		if (v == null) return 0;
		v = v.trim();
		try {
			if (v.endsWith("pt")) return Double.parseDouble(v.substring(0, v.length() - 2));
			if (v.endsWith("in")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72;
			if (v.endsWith("mm")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72 / 25.4;
			if (v.endsWith("cm")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72 / 2.54;
			if (v.endsWith("px")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 0.75;
			return Double.parseDouble(v);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/** The first fo:block in document order under this element, not descending into tables. */
	private static Element firstBlock(Element parent) {
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (!(n instanceof Element)) continue;
			Element el = (Element) n;
			if (isFo(el, "table")) return null;
			if (isFo(el, "block")) return el;
			Element inner = firstBlock(el); // block-container (bidi), list-block, list-item, list-item-body
			if (inner != null) return inner;
		}
		return null;
	}

	// ------------------------------------------------------------ 3. table cells

	static void retainSpacingAtCellEdges(Document doc, int compatibilityMode) {
		for (Element cell : elements(doc, "table-cell")) {
			List<Element> blocks = childBlocks(cell);
			if (blocks.isEmpty()) continue;
						Element first = blocks.get(0);
			if (first.getAttribute(HINT_AUTOSPACING).indexOf('b') >= 0) {
				first.setAttribute("space-before", "0pt"); // auto spacing is dropped at cell edges (measured)
			} else if (hasSpace(first, "space-before")) {
				first.setAttribute("space-before.conditionality", "retain");
			}
			Element last = blocks.get(blocks.size() - 1);
			if (last.getAttribute(HINT_AUTOSPACING).indexOf('a') >= 0) {
				last.setAttribute("space-after", "0pt");
			} else if (compatibilityMode >= 15 && hasSpace(last, "space-after")) {
				last.setAttribute("space-after.conditionality", "retain");
			}
		}
	}

	// ------------------------------------------------------------ helpers

	private static List<Element> elements(Document doc, String localName) {
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, localName);
		List<Element> out = new ArrayList<>(nl.getLength());
		for (int i = 0; i < nl.getLength(); i++) out.add((Element) nl.item(i));
		return out;
	}

	private static boolean isFo(Element el, String localName) {
		return FO_NS.equals(el.getNamespaceURI()) && localName.equals(el.getLocalName());
	}

	private static List<Element> childBlocks(Element parent) {
		List<Element> out = new ArrayList<>();
		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n instanceof Element && isFo((Element) n, "block")) out.add((Element) n);
		}
		return out;
	}

	private static Element nextElementSibling(Element el) {
		Node n = el.getNextSibling();
		while (n != null && !(n instanceof Element)) n = n.getNextSibling();
		return (Element) n;
	}

	/** No element children and no non-whitespace text. */
	private static boolean isEmpty(Element el) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n instanceof Element) return false;
			if (n.getNodeType() == Node.TEXT_NODE && n.getNodeValue().trim().length() > 0) return false;
		}
		return true;
	}

	/** Has the attribute with a value that is not zero. */
	private static boolean hasSpace(Element el, String name) {
		String v = el.getAttribute(name);
		if (v == null || v.length() == 0) return false;
		String num = v.replaceAll("[a-zA-Z%]+$", "");
		try {
			return Double.parseDouble(num) != 0;
		} catch (NumberFormatException e) {
			return true;
		}
	}
}
