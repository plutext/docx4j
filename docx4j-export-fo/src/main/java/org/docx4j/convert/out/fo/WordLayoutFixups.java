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
		retainSpaceBeforeAtFlowStart(doc);
		retainSpacingAtCellEdges(doc, compatibilityMode);
		stripHints(doc);
	}

	/** Remove the hint attributes whether or not the rules ran (FOP must not see them). */
	public static void stripHints(Document doc) {
		for (Element block : elements(doc, "block")) {
			block.removeAttribute(HINT_PSTYLE);
			block.removeAttribute(HINT_CONTEXTUAL);
		}
	}

	// ------------------------------------------------------------ 0. contextual spacing

	/**
	 * w:contextualSpacing ("Don't add space between paragraphs of the same style"):
	 * a paragraph with it ignores its space-before when the previous paragraph has the
	 * same style, and its space-after when the next one has (ECMA-376 17.3.1.9).
	 * Neighbours are the consecutive paragraphs of a flow or of a table cell; a list
	 * item's paragraph is the block inside its list-item-body.
	 */
	static void applyContextualSpacing(Document doc) {
		for (Element flow : elements(doc, "flow")) contextualSpacingAmong(flow);
		for (Element cell : elements(doc, "table-cell")) contextualSpacingAmong(cell);
	}

	private static void contextualSpacingAmong(Element container) {
		List<Element> paras = new ArrayList<>();
		NodeList children = container.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (!(n instanceof Element)) continue;
			Element p = paragraphBlock((Element) n);
			if (p != null) paras.add(p);
		}
		for (int i = 0; i + 1 < paras.size(); i++) {
			Element a = paras.get(i), b = paras.get(i + 1);
			String sa = a.getAttribute(HINT_PSTYLE), sb = b.getAttribute(HINT_PSTYLE);
			if (sa == null || sa.length() == 0 || !sa.equals(sb)) continue;
			if ("1".equals(a.getAttribute(HINT_CONTEXTUAL))) a.setAttribute("space-after", "0pt");
			if ("1".equals(b.getAttribute(HINT_CONTEXTUAL))) b.setAttribute("space-before", "0pt");
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

	static void retainSpaceBeforeAtFlowStart(Document doc) {
		for (Element flow : elements(doc, "flow")) {
			Element first = firstBlock(flow);
			if (first != null && hasSpace(first, "space-before")) {
				first.setAttribute("space-before.conditionality", "retain");
			}
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
			if (hasSpace(first, "space-before")) {
				first.setAttribute("space-before.conditionality", "retain");
			}
			if (compatibilityMode >= 15) {
				Element last = blocks.get(blocks.size() - 1);
				if (hasSpace(last, "space-after")) {
					last.setAttribute("space-after.conditionality", "retain");
				}
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
