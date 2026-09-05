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
		disregardBaselineShifts(doc);
		listLabelLines(doc);
		lineBoxAttributes(doc);
		anchorImages(doc);
		anchorTextBoxes(doc);
		mergePageBreakParagraphs(doc, compatibilityMode);
		applyContextualSpacing(doc);
		applyAutoSpacingBetweenListItems(doc);
		retainSpaceBeforeAtFlowStart(doc);
		retainSpacingAtCellEdges(doc, compatibilityMode);
		fixLists(doc);
		clipExactRows(doc);
		stripHints(doc);
	}

	// ------------------------------------------------------------ 0. superscripts

	/**
	 * A superscript or subscript does not make Word's line taller (measured:
	 * a footnote reference adds 0.5pt below a 13.8pt line, nothing above), while
	 * FOP grows the line box by the shift; XSL's line-height-shift-adjustment
	 * turns that off.
	 */
	static void disregardBaselineShifts(Document doc) {
		Element root = doc.getDocumentElement();
		if (root != null && isFo(root, "root")) {
			root.setAttribute("line-height-shift-adjustment", "disregard-shifts");
		}
	}

	// ------------------------------------------------------------ 0b. Word's line box

	/** Hints from XsltFOFunctions.applyLineBoxHints: the text box of the block's
	 *  lines and the baseline within it, in pt. */
	public static final String HINT_LINE_BOX = "docx4j-linebox";
	public static final String HINT_BASELINE = "docx4j-baseline";
	public static final String HINT_LINE_RULE = "docx4j-linerule";
	/** on a list item's first paragraph block: the label's natural ascent, which
	 *  joins the runs of the first line (WordLineLayoutManager) */
	public static final String HINT_LABEL_ASCENT = "docx4j-label-ascent";

	// ------------------------------------------------------------ 0a. list labels

	/**
	 * Word sizes a list item's first line from the number or bullet as well as
	 * the text, but only by the label's ascent, and without the auto multiple:
	 * a Symbol bullet on Calibri 11pt makes the line 16.04pt, not 15.44 (Symbol's
	 * ascent exceeds Calibri's by 0.59pt), while a Courier New "o" bullet, whose
	 * descent exceeds Calibri's, leaves it at 15.45 (both measured, CR-001 §6.10).  In FO the label is a
	 * separate block, so the label block is given the combined box and baseline
	 * (its height and baseline then match the body's first line) and the body
	 * block the label's natural ascent for the line manager to fold into its
	 * first line.
	 */
	static void listLabelLines(Document doc) {
		for (Element item : elements(doc, "list-item")) {
			Element labelEl = firstChildElement(item, "list-item-label");
			Element bodyEl = firstChildElement(item, "list-item-body");
			if (labelEl == null || bodyEl == null) continue;
			Element label = firstChildElement(labelEl, "block");
			if (label == null) continue;
			Element body = null;
			for (Element b : descendants(bodyEl, "block")) {
				if (b.getAttribute(HINT_LINE_BOX).length() > 0) { body = b; break; }
			}
			if (body == null) continue;
			double box = lengthPt(body.getAttribute(HINT_LINE_BOX));
			double base = lengthPt(body.getAttribute(HINT_BASELINE));
			double bodyLh = lengthPt(body.getAttribute("line-height"));
			if (box <= 0 || base <= 0) continue;
			String rule = body.getAttribute(HINT_LINE_RULE);
			if ("exact".equals(rule)) {
				label.setAttribute(HINT_LINE_BOX, body.getAttribute(HINT_LINE_BOX));
				label.setAttribute(HINT_BASELINE, body.getAttribute(HINT_BASELINE));
				label.setAttribute(HINT_LINE_RULE, rule);
				if (bodyLh > 0) label.setAttribute("line-height", body.getAttribute("line-height"));
				continue;
			}
			double size = 0;
			for (Element e = label; e != null && size <= 0; e = e.getParentNode() instanceof Element ? (Element) e.getParentNode() : null) {
				size = lengthPt(e.getAttribute("font-size"));
			}
			String family = label.getAttribute("font-family");
			String docFont = label.getAttribute(org.docx4j.fonts.RunFontSelector.HINT_FONT);
			if (size <= 0 || (family.length() == 0 && docFont.length() == 0)) continue;
			org.docx4j.fonts.WordLineMetrics.Metrics m = org.docx4j.fonts.WordLineMetrics.get(
					docFont.length() == 0 ? null : docFont,
					family.length() == 0 ? null : org.docx4j.fonts.PhysicalFonts.get(family));
			if (m.fallback) continue;
			double labelAscent = (m.winAscent + m.externalLeading) * size;
			double a = Math.max(labelAscent, base);
			double d = box - base;
			label.setAttribute(HINT_LINE_BOX, pt(a + d));
			label.setAttribute(HINT_BASELINE, pt(a));
			if (rule.length() > 0) label.setAttribute(HINT_LINE_RULE, rule);
			if ("atLeast".equals(rule)) {
				if (bodyLh > 0) label.setAttribute("line-height", body.getAttribute("line-height"));
			} else if (bodyLh > 0) {
				// the text line's leading under the paragraph's auto multiple; the label's
				// excess ascent is not multiplied (16.04 = 15.44 + 0.59 for the Symbol bullet)
				label.setAttribute("line-height", pt(a + d + (bodyLh - box)));
			}
			body.setAttribute(HINT_LABEL_ASCENT, pt(labelAscent));
		}
	}

	private static Element firstChildElement(Element parent, String localName) {
		for (Node c = parent.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c instanceof Element && localName.equals(c.getLocalName()) && FO_NS.equals(c.getNamespaceURI())) return (Element) c;
		}
		return null;
	}

	private static final String XMLNS = "http://www.w3.org/2000/xmlns/";

	/**
	 * When Word layout is on (the default; org.docx4j.fop.wordlayout's
	 * FopFactoryCustomizer names the namespace its ElementMapping registers),
	 * the line-box hints become docx4j:line-box / docx4j:baseline attributes on
	 * the block, which the Word line manager reads: each line is then Word's
	 * text box with the extra leading as glue below it, dropped at the bottom of
	 * a page.  With docx4j.convert.out.fo.wordLayout=false FOP would reject the
	 * attributes, so they are left out.
	 */
	static void lineBoxAttributes(Document doc) {
		String ns = extensionNamespace();
		boolean declared = false;
		// the runs' document fonts (RunFontSelector.HINT_FONT), for the line manager's per-run metrics
		for (Element span : elements(doc, "inline")) {
			String font = span.getAttribute(org.docx4j.fonts.RunFontSelector.HINT_FONT);
			if (font.length() == 0) continue;
			span.removeAttribute(org.docx4j.fonts.RunFontSelector.HINT_FONT);
			if (ns == null) continue;
			if (!declared) {
				doc.getDocumentElement().setAttributeNS(XMLNS, "xmlns:docx4j", ns);
				declared = true;
			}
			span.setAttributeNS(ns, "docx4j:font", font);
		}
		for (Element block : elements(doc, "block")) {
			String box = block.getAttribute(HINT_LINE_BOX);
			String baseline = block.getAttribute(HINT_BASELINE);
			String rule = block.getAttribute(HINT_LINE_RULE);
			String labelAscent = block.getAttribute(HINT_LABEL_ASCENT);
			block.removeAttribute(HINT_LINE_BOX);
			block.removeAttribute(HINT_BASELINE);
			block.removeAttribute(HINT_LINE_RULE);
			block.removeAttribute(HINT_LABEL_ASCENT);
			if (ns == null || box.length() == 0) continue;
			if (!declared) {
				doc.getDocumentElement().setAttributeNS(XMLNS, "xmlns:docx4j", ns);
				declared = true;
			}
			block.setAttributeNS(ns, "docx4j:line-box", box.endsWith("pt") ? box : box + "pt");
			if (baseline.length() > 0) block.setAttributeNS(ns, "docx4j:baseline", baseline.endsWith("pt") ? baseline : baseline + "pt");
			if (rule.length() > 0) block.setAttributeNS(ns, "docx4j:line-rule", rule);
			if (labelAscent.length() > 0) block.setAttributeNS(ns, "docx4j:label-ascent", labelAscent);
		}
	}

	/** The extension namespace a loaded FopFactoryCustomizer supports, or null. */
	static String extensionNamespace() {
		try {
			for (org.docx4j.convert.out.fo.renderers.FopFactoryCustomizer c
					: java.util.ServiceLoader.load(org.docx4j.convert.out.fo.renderers.FopFactoryCustomizer.class)) {
				String ns = c.extensionNamespace();
				if (ns != null) return ns;
			}
		} catch (java.util.ServiceConfigurationError e) {
			log.warn("FopFactoryCustomizer lookup failed: " + e.getMessage());
		}
		return null;
	}

	// ------------------------------------------------------------ 0a. anchored pictures

	public static final String HINT_ANCHOR = "docx4j-anchor";
	/** @since 17.0.5 */
	public static final String HINT_ANCHOR_W = "docx4j-anchor-w";
	/** @since 17.0.5 */
	public static final String HINT_ANCHOR_H = "docx4j-anchor-h";
	/** @since 17.0.5 */
	public static final String HINT_ANCHOR_X = "docx4j-anchor-x";
	/** @since 17.0.5 */
	public static final String HINT_ANCHOR_Y = "docx4j-anchor-y";
	/** @since 17.0.5 */
	public static final String HINT_ANCHOR_COL = "docx4j-anchor-col";
	/** @since 17.0.5 */
	public static final String HINT_ANCHOR_ML = "docx4j-anchor-ml";
	private static final String[] ANCHOR_HINTS = { HINT_ANCHOR, HINT_ANCHOR_W, HINT_ANCHOR_H,
			HINT_ANCHOR_X, HINT_ANCHOR_Y, "docx4j-anchor-dist", "docx4j-anchor-behind",
			HINT_ANCHOR_COL, HINT_ANCHOR_ML };

	/**
	 * Word positions an anchored picture (wp:anchor) relative to its paragraph,
	 * column or page, and wraps the text around it; WordXmlPictureE20 stamps
	 * that geometry on the fo:external-graphic (lengths in pt, x from the
	 * column's left edge, y "p:" from the paragraph's top or "page:" from the
	 * page top) and this turns it into what FOP can do:
	 * <ul>
	 * <li>square/tight/through wrap: an fo:float at the left or right edge
	 * (whichever the picture is nearer), padded so the picture sits where Word
	 * puts it, text on the other side only (Word flows text on both sides of a
	 * picture in the middle);</li>
	 * <li>top-and-bottom wrap: a block-container as tall as the picture at the
	 * paragraph's top;</li>
	 * <li>no wrap (behind or in front of text): an absolutely positioned
	 * block-container inside a zero-height one at the paragraph's top, so it
	 * takes no space; page-relative positions use fixed positioning.</li>
	 * </ul>
	 * A float is only used where docx4j.convert.out.fo.pictures.float allows it
	 * (FOConversionContext.FLOAT_PROPERTY); the picture is laid out top-and-bottom
	 * otherwise.
	 * The picture's block uses a tiny font and zero line-height so its top is
	 * exactly the container's top (FOP otherwise offsets it by the block font's
	 * ascender).  Floats only work in the main flow, so a wrapped picture inside
	 * a table, header, footer or footnote is laid out top-and-bottom instead,
	 * and one with a page-relative vertical position is fixed without wrapping.
	 */
	static void anchorImages(Document doc) {
		for (Element g : elements(doc, "external-graphic")) {
			String kind = g.getAttribute(HINT_ANCHOR);
			if (kind == null || kind.length() == 0) continue;
			try {
				anchorImage(doc, g, kind);
			} catch (RuntimeException e) {
				log.warn("Anchored picture left in the flow: " + e.getMessage(), e);
			}
			for (String hint : ANCHOR_HINTS) g.removeAttribute(hint);
		}
	}

	private static void anchorImage(Document doc, Element g, String kind) {
		double w = Double.parseDouble(g.getAttribute("docx4j-anchor-w"));
		double h = Double.parseDouble(g.getAttribute("docx4j-anchor-h"));
		double x = Double.parseDouble(g.getAttribute("docx4j-anchor-x"));
		double col = Double.parseDouble(g.getAttribute("docx4j-anchor-col"));
		double ml = Double.parseDouble(g.getAttribute("docx4j-anchor-ml"));
		String y = g.getAttribute("docx4j-anchor-y");
		boolean pageY = y.startsWith("page:");
		double off = Double.parseDouble(y.substring(y.indexOf(':') + 1));
		String[] dist = g.getAttribute("docx4j-anchor-dist").split(" ");
		double distL = Double.parseDouble(dist[0]), distR = Double.parseDouble(dist[1]),
				distB = Double.parseDouble(dist[3]);

		Element para = enclosingParagraph(g);
		if (para == null) return; // leave it inline

		// the picture at its extent (content-width/height carry rounded pixels)
		g.setAttribute("content-width", pt(w));
		g.setAttribute("content-height", pt(h));
		Element holder = doc.createElementNS(FO_NS, "fo:block");
		holder.setAttribute("font-size", "0.1pt");
		holder.setAttribute("line-height", "0pt");
		holder.appendChild(g); // moves it out of its run

		if (pageY) kind = "none"; // FOP cannot wrap text around a page-positioned object
		if ("square".equals(kind) && (!floatsAllowed(para) || !FOConversionContext.useFloats())) {
			kind = "topAndBottom";
		}

		Element wrapper;
		if ("square".equals(kind)) {
			boolean right = x + w / 2 > col / 2;
			wrapper = doc.createElementNS(FO_NS, "fo:float");
			wrapper.setAttribute("float", right ? "right" : "left");
			holder.setAttribute("padding-left", pt(right ? distL : Math.max(0, x)));
			holder.setAttribute("padding-right", pt(right ? Math.max(0, col - x - w) : distR));
			if (off > 0) holder.setAttribute("padding-top", pt(off));
			if (distB > 0) holder.setAttribute("padding-bottom", pt(distB));
			wrapper.appendChild(holder);
		} else if ("topAndBottom".equals(kind)) {
			wrapper = doc.createElementNS(FO_NS, "fo:block-container");
			wrapper.setAttribute("height", pt(Math.max(0, off) + h + distB));
			wrapper.setAttribute("start-indent", "0pt");
			wrapper.setAttribute("end-indent", "0pt");
			if (off > 0) holder.setAttribute("padding-top", pt(off));
			holder.setAttribute("start-indent", pt(Math.max(0, x)));
			wrapper.appendChild(holder);
		} else {
			wrapper = doc.createElementNS(FO_NS, "fo:block-container");
			wrapper.setAttribute("height", "0pt");
			wrapper.setAttribute("overflow", "visible");
			wrapper.setAttribute("start-indent", "0pt");
			wrapper.setAttribute("end-indent", "0pt");
			Element abs = doc.createElementNS(FO_NS, "fo:block-container");
			abs.setAttribute("absolute-position", pageY ? "fixed" : "absolute");
			abs.setAttribute("top", pt(off));
			abs.setAttribute("left", pt(pageY ? x + ml : x));
			abs.setAttribute("width", pt(w));
			abs.setAttribute("height", pt(h));
			abs.setAttribute("overflow", "visible");
			abs.appendChild(holder);
			wrapper.appendChild(abs);
		}
		para.insertBefore(wrapper, para.getFirstChild());
	}

	// ------------------------------------------------------------ 0c. text boxes

	/**
	 * A text box (VML w:pict/v:shape/v:textbox, or DrawingML wps:wsp/wps:txbx)
	 * reaches here as an fo:block-container carrying the same anchor hints a
	 * picture does (FOTextBoxes), still inside the run's fo:inline - which is not
	 * block-level content, so FOP would paint nothing.  It is moved to the start of
	 * its paragraph's block and placed as Word places it:
	 * <ul>
	 * <li>behind or in front of the text (or positioned relative to the page): an
	 * absolutely positioned container inside a zero-height one, so it takes no
	 * space in the flow;</li>
	 * <li>wrapped and as wide as the column: a container that reserves the box's
	 * height at the paragraph, indented to the box's x.  A narrower wrapped box is
	 * placed like the first case, since Word flows text beside it and we cannot
	 * (see FOTextBoxes).</li>
	 * </ul>
	 *
	 * @since 17.0.5
	 */
	static void anchorTextBoxes(Document doc) {
		for (Element box : elements(doc, "block-container")) {
			String kind = box.getAttribute(HINT_ANCHOR);
			if (kind == null || kind.length() == 0) continue;
			try {
				anchorTextBox(doc, box, kind);
			} catch (RuntimeException e) {
				log.warn("Text box left in the flow: " + e.getMessage(), e);
			}
			for (String hint : ANCHOR_HINTS) box.removeAttribute(hint);
		}
	}

	private static void anchorTextBox(Document doc, Element box, String kind) {
		double w = lengthPt(box.getAttribute(HINT_ANCHOR_W));
		double h = lengthPt(box.getAttribute(HINT_ANCHOR_H));
		double x = lengthPt(box.getAttribute(HINT_ANCHOR_X));
		double col = lengthPt(box.getAttribute(HINT_ANCHOR_COL));
		double ml = lengthPt(box.getAttribute(HINT_ANCHOR_ML));
		String y = box.getAttribute(HINT_ANCHOR_Y);
		boolean pageY = y.startsWith("page:");
		double off = lengthPt(y.substring(y.indexOf(':') + 1));

		Element para = enclosingBlock(box);
		if (para == null) {
			log.warn("No block to place a text box in; it will not be painted");
			return;
		}

		// a box narrow enough for Word to flow text beside it is placed where Word
		// puts it and takes no space: reserving its height would push the text below
		// it, and where several such boxes sit side by side (a planner laid out in
		// text boxes) that costs a page each.  A box that fills the column has no
		// text beside it in Word either, so it reserves its height.
		if ("square".equals(kind) && col > 0 && w < 0.6 * col) kind = "none";

		Element wrapper = doc.createElementNS(FO_NS, "fo:block-container");
		wrapper.setAttribute("start-indent", "0pt");
		wrapper.setAttribute("end-indent", "0pt");
		wrapper.setAttribute("overflow", "visible");
		if ("none".equals(kind) || pageY) {
			// out of the flow: the zero-height container takes no space
			wrapper.setAttribute("height", "0pt");
			box.setAttribute("absolute-position", pageY ? "fixed" : "absolute");
			box.setAttribute("top", pt(off));
			box.setAttribute("left", pt(pageY ? x + ml : x));
		} else {
			// wrapped: reserve the box's height where Word puts it.  The indent goes on
			// the wrapper, whose reference area the box then starts at (the box resets
			// the inherited indent for its own content).
			if (h > 0) wrapper.setAttribute("height", pt(h));
			if (off > 0) wrapper.setAttribute("padding-top", pt(off));
			wrapper.setAttribute("start-indent", pt(Math.max(0, x)));
		}
		box.getParentNode().removeChild(box);
		wrapper.appendChild(box);
		para.insertBefore(wrapper, para.getFirstChild());
	}

	/** The block the text box belongs to: the nearest ancestor fo:block. */
	private static Element enclosingBlock(Element el) {
		Node n = el.getParentNode();
		while (n instanceof Element) {
			Element e = (Element) n;
			if (isFo(e, "block")) return e;
			if (isFo(e, "flow") || isFo(e, "static-content") || isFo(e, "table-cell")) return null;
			n = n.getParentNode();
		}
		return null;
	}

	/** The paragraph's fo:block (the nearest ancestor stamped with the pstyle hint). */
	private static Element enclosingParagraph(Element el) {
		Node n = el.getParentNode();
		while (n instanceof Element) {
			Element e = (Element) n;
			if (isFo(e, "block") && e.hasAttribute(HINT_PSTYLE)) return e;
			if (isFo(e, "flow") || isFo(e, "static-content")) return null;
			n = n.getParentNode();
		}
		return null;
	}

	/** FOP lays out side floats only in the main flow's blocks. */
	private static boolean floatsAllowed(Element para) {
		Node n = para.getParentNode();
		while (n instanceof Element) {
			Element e = (Element) n;
			if (isFo(e, "flow")) return true;
			if (isFo(e, "table-cell") || isFo(e, "static-content") || isFo(e, "footnote-body")
					|| isFo(e, "float") || isFo(e, "block-container") || isFo(e, "inline-container")) return false;
			n = n.getParentNode();
		}
		return false;
	}

	private static String pt(double v) {
		String s = String.format(java.util.Locale.ROOT, "%.2f", v);
		if (s.endsWith("0")) s = s.substring(0, s.length() - 1);
		if (s.endsWith("0")) s = s.substring(0, s.length() - 1);
		if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
		if (s.equals("-0")) s = "0";
		return s + "pt";
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
			block.removeAttribute(HINT_LINE_BOX);
			block.removeAttribute(HINT_BASELINE);
			block.removeAttribute(HINT_LINE_RULE);
			block.removeAttribute(HINT_LABEL_ASCENT);
			block.removeAttribute(org.docx4j.fonts.RunFontSelector.HINT_FONT);
		}
		for (Element g : elements(doc, "external-graphic")) {
			for (String hint : ANCHOR_HINTS) g.removeAttribute(hint);
		}
		for (Element span : elements(doc, "inline")) {
			span.removeAttribute(org.docx4j.fonts.RunFontSelector.HINT_FONT);
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
		for (Element span : spanAllBlocks(doc)) autoSpacingAmong(span);
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
		for (Element span : spanAllBlocks(doc)) contextualSpacingAmong(span);
		for (Element cell : elements(doc, "table-cell")) contextualSpacingAmong(cell);
	}

	/** The blocks spanning all columns of a multi-column page-sequence (a merged
	 *  continuous section, ConversionSectionWrapperFactory): their children are
	 *  flow-level paragraphs too. */
	private static List<Element> spanAllBlocks(Document doc) {
		List<Element> out = new ArrayList<>();
		for (Element b : elements(doc, "block")) {
			if ("all".equals(b.getAttribute("span"))) out.add(b);
		}
		return out;
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
		if (isFo(el, "block")) {
			if (el.hasAttribute(HINT_PSTYLE)) return el;
			// a borders/shading container (Containerization) wraps its paragraphs in a
			// plain block: the first paragraph inside stands for it
			Element b = firstBlock(el);
			return (b != null && b.hasAttribute(HINT_PSTYLE)) ? b : null;
		}
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
