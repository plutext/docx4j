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

import java.io.File;
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
		return apply(foDocument, compatibilityMode, null);
	}

	/**
	 * @param hyphenation the document's hyphenation settings, which go on fo:root
	 *        for the line manager; null for a document which is not hyphenated.
	 * @since 17.0.6
	 */
	public static String apply(String foDocument, int compatibilityMode,
			org.docx4j.model.HyphenationSettings hyphenation) {
		try {
			Document doc = XmlUtils.getNewDocumentBuilder().parse(new InputSource(new StringReader(foDocument)));
			apply(doc, compatibilityMode, hyphenation);
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
		apply(doc, compatibilityMode, null);
	}

	/** @since 17.0.6 */
	public static void apply(Document doc, int compatibilityMode,
			org.docx4j.model.HyphenationSettings hyphenation) {
		disregardBaselineShifts(doc);
		combineLetterSpacing(doc);
		imageOnlyLineBox(doc);
		inlineLabelGaps(doc);
		listLabelLines(doc);
		lineBoxAttributes(doc, compatibilityMode, hyphenation);
		positionFrames(doc);
		anchorImages(doc);
		anchorTextBoxes(doc);
		anchorFloatingTables(doc);
		hoistFloats(doc);
		firstLineIndentAfterLeadingBlock(doc);
		reserveUnpaintablePictures(doc);
		columnBreaks(doc); // before emptyLineForBlockWithNoContent: what follows the break takes a line
		emptyLineForBlockWithNoContent(doc);
		emptyLineAfterLineBreak(doc);
		tocLeaderEndIndent(doc);
		leadingWhitespaceLeader(doc);
		containWhitespaceTreatment(doc);
		dropParagraphAfterNestedTable(doc);
		dropPageBreaksInTableCells(doc);
		mergePageBreakParagraphs(doc, compatibilityMode);
		applyContextualSpacing(doc);
		applyAutoSpacingBetweenListItems(doc);
		syncContainerSpacing(doc);
		mergeBorderContainers(doc);
		retainSpaceBeforeAtFlowStart(doc);
		spaceBeforePageNumber(doc);
		retainSpacingAtStaticContentEnd(doc);
		retainSpaceAfterInAlignedFlow(doc);
		retainSpacingAtCellEdges(doc, compatibilityMode);
		cellLineWidth(doc);
		nestedTableGridEdge(doc);
		fixLists(doc);
		listItemPageBreaks(doc); // after fixLists, which is what puts the item's space-before on the list-block
		blockForEmptyCell(doc);
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

	// ------------------------------------------------------------ 0a1. letter-spacing

	/**
	 * Word's character spacing (<code>w:spacing</code>) and its character scaling
	 * (<code>w:w</code>, reproduced as a measured letter space, &#xa7;4.6) are both
	 * carried as <code>letter-spacing</code>, on the run's own <code>fo:inline</code> and
	 * on the per-font selection inline inside it.  <code>letter-spacing</code> is an
	 * inherited property, so the inner value <em>replaced</em> the outer one where Word
	 * applies both.
	 *
	 * <p>Measured against Word 365 on a document whose every run carries
	 * <code>w:w="94"</code> and a <code>w:spacing</code>: the FO read
	 * <code>&lt;inline letter-spacing="-0.2pt"&gt;&lt;inline docx4j:font="Arial"
	 * letter-spacing="-0.244pt"&gt;</code>, and "All payments should be made by cash or
	 * Cheque." is 57.1..294.7 = 237.6pt in Word against our 57.0..302.7 = 245.7pt - +8.1pt,
	 * which is 0.2pt over the line's 38 characters.</p>
	 *
	 * <p>Each explicit value therefore becomes the sum of itself and the nearest
	 * ancestor's.  Working in document order makes that cumulative over any depth.</p>
	 *
	 * @since 17.0.6
	 */
	static void combineLetterSpacing(Document doc) {
		List<Element> scaled = new ArrayList<>();
		collectScaledLetterSpacing(doc.getDocumentElement(), scaled);
		for (Element el : scaled) {
			el.removeAttribute(HINT_SCALED_SPACING);
			double outer = 0;
			for (Node n = el.getParentNode(); n instanceof Element; n = n.getParentNode()) {
				Element ancestor = (Element) n;
				if (ancestor.hasAttribute("letter-spacing")) {
					outer = lengthPt(ancestor.getAttribute("letter-spacing"));
					break;
				}
			}
			if (outer == 0) continue;
			double own = lengthPt(el.getAttribute("letter-spacing"));
			el.setAttribute("letter-spacing", org.docx4j.fonts.WordLineMetrics.format(own + outer));
		}
	}

	/** RunFontSelector's mark on a span whose letter-spacing is w:w scaling alone. */
	public static final String HINT_SCALED_SPACING =
			org.docx4j.fonts.RunFontSelector.HINT_SCALED_LETTER_SPACING;

	private static void collectScaledLetterSpacing(Element el, List<Element> out) {
		if (el == null) return;
		if (el.hasAttribute(HINT_SCALED_SPACING)) out.add(el);
		for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) collectScaledLetterSpacing((Element) n, out);
		}
	}

	// ------------------------------------------------------------ 0a2. a line holding only a picture

	/**
	 * Word gives a line holding nothing but a picture the picture's height and no
	 * descent (&#xa7;2.4).  The line box the layout manager needs is written by
	 * XsltFOFunctions.applyBlockLineHeight, which sizes it from the <em>text</em> runs on
	 * the line - so a paragraph whose only content is an inline picture gets no line box
	 * at all, and the line manager then falls back to FOP's own line, which adds the
	 * paragraph font's descent below the picture.  Measured: a 67.5pt logo above a 14pt
	 * paragraph put the next baseline 4.8pt below Word's, on every such paragraph.
	 *
	 * <p>Only an <em>inline</em> picture counts: one that is about to be lifted into a
	 * positioned container (it carries the anchor hints) takes no line of its own, and
	 * that paragraph gets the paragraph mark's line instead
	 * ({@link #emptyLineForBlockWithNoContent}).</p>
	 *
	 * @since 17.0.6
	 */
	static void imageOnlyLineBox(Document doc) {
		for (Element block : elements(doc, "block")) {
			if (!block.hasAttribute(HINT_PSTYLE)) continue;
			// an exact line rule fixes the line's height whatever is on it, picture included
			if ("exact".equals(block.getAttribute(HINT_LINE_RULE))) continue;
			double height = inlineGraphicHeight(block);
			if (height <= 0) continue;
			// applyBlockLineHeight very often does write a line box for such a paragraph
			// - from the run the picture sits in, or from the paragraph mark - and the
			// rule used to skip exactly the paragraphs it was written for, leaving the
			// picture's line the paragraph's height *plus* the picture.  Word's line is
			// the picture's own height with no descent, so the larger of the two wins.
			// @since 17.0.6
			double box = lengthPt(block.getAttribute(HINT_LINE_BOX));
			if (height <= box) continue;
			// A line-spacing multiple still adds its own leading to the picture's line -
			// it does not multiply the picture.  Word's multiple m adds (m - 1) x the
			// paragraph font's *natural* pitch, not (m - 1) x the picture: measured on
			// the picture-header-cell golden, an 11pt paragraph (natural pitch 13.428pt)
			// at w:spacing line=276 lineRule=auto (m = 1.15) holding a 24pt picture puts
			// the next baseline 40.1pt below the previous one, against 26.1 for the same
			// picture on an exact 12pt line - so its line is 26.0pt = 24 + 0.15 x 13.428
			// (2.01), where cancelling the multiple outright gave 24.0 and 37.8.  An
			// exact line rule still clips the picture, and is left alone above.
			// @since 17.0.6
			double extraLeading = 0;
			double lineHeight = lengthPt(block.getAttribute("line-height"));
			if (box > 0 && lineHeight > box) extraLeading = lineHeight - box;
			block.setAttribute(HINT_LINE_BOX, org.docx4j.fonts.WordLineMetrics.format(height));
			block.setAttribute(HINT_BASELINE, org.docx4j.fonts.WordLineMetrics.format(height));
			block.setAttribute(HINT_LINE_RULE, "auto");
			// ... and the paragraph's line-spacing multiple does not apply to it: Word
			// gives the picture's line the picture's height, not 1.5 x it.  Left at the
			// text line-height, the extra leading is a fraction of the *picture*:
			// measured on a document whose 269.68pt diagrams sit in blocks of
			// line-box 13.428pt / line-height 20.142pt, the caption after each came out
			// 132.6pt below Word's (515.8 against 383.2), almost exactly half the
			// picture, and 88 Word pages came out as 105.  Only where there is a
			// multiple to cancel (line-height beyond the box): a single-spaced
			// paragraph's line-height is its box, and raising it grew a header holding
			// a picture by 10.3pt, which re-centred the picture in its cell 5.1pt below
			// Word's.  Only the multiple's *own* leading survives, as a line-height the
			// line manager reads as a factor over the (picture-sized) line box: its
			// default rule makes the line ascent + descent = the picture, then adds
			// box x (line-height / line-box - 1), which is exactly extraLeading here.
			// @since 17.0.6
			if (extraLeading > 0) {
				block.setAttribute("line-height",
						org.docx4j.fonts.WordLineMetrics.format(height + extraLeading));
			}
		}
	}

	/** The tallest inline graphic directly on this block's line, in points; 0 where the
	 *  block holds anything else that would be on the line, or no graphic at all. */
	private static double inlineGraphicHeight(Element el) {
		double[] state = { 0, 0 }; // tallest graphic, disqualified
		scanInlineGraphics(el, state);
		return state[1] != 0 ? 0 : state[0];
	}

	private static void scanInlineGraphics(Element el, double[] state) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength() && state[1] == 0; i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE) {
				String v = n.getNodeValue();
				if (v != null && v.trim().length() > 0) state[1] = 1; // text on the line too
				continue;
			}
			if (!(n instanceof Element)) continue;
			Element child = (Element) n;
			if (isFo(child, "block-container") || isFo(child, "float")) continue; // out of the flow
			if (isFo(child, "external-graphic") || isFo(child, "instream-foreign-object")) {
				/* An anchored picture is about to be lifted into a positioned container
				 * (anchorImages), so it is not on this line at all - Word floats it.  It
				 * used to disqualify the line, which cost the rule every paragraph
				 * holding an anchored picture *and* an inline one: measured on a document
				 * whose first body paragraph is [anchored 62.25pt logo][tab][inline
				 * 25.5pt logo], Word's first baseline is 63.1pt from the page top with a
				 * 29.2pt top margin - the paragraph is the inline picture's 25.5pt -
				 * where ours was 29.2, the picture plus the 13pt run's descent and line
				 * gap.  Where the anchored picture is the only one, the height stays 0
				 * and the rule still does nothing.  @since 17.0.6 */
				if (child.hasAttribute(HINT_ANCHOR)) continue;
				double h = lengthPt(child.getAttribute("content-height"));
				if (h <= 0) { // unsized
					state[1] = 1;
					return;
				}
				state[0] = Math.max(state[0], h);
				continue;
			}
			if (isFo(child, "inline") || isFo(child, "wrapper") || isFo(child, "basic-link")
					|| isFo(child, "bidi-override")) {
				scanInlineGraphics(child, state);
				continue;
			}
			/* A space leader reserves width and paints nothing, so it does not make
			 * Word's line taller than the picture on it: a tab, or the leading
			 * whitespace leadingWhitespaceLeader writes, in front of a logo.  Measured
			 * on a document whose first body paragraph is [1.7pt tab][bookmark][25.5pt
			 * logo]: Word's first baseline is 63.1 with a 29.2pt top margin, ie the
			 * paragraph is exactly the picture's 25.5pt, where ours was 29.2 - the
			 * picture plus the 13pt run's descent and line gap.  A leader that does
			 * paint (dots, a rule) still disqualifies the line.  @since 17.0.6 */
			if (isFo(child, "leader")
					&& (!child.hasAttribute("leader-pattern")
						|| "space".equals(child.getAttribute("leader-pattern")))) {
				continue;
			}
			/* A nested fo:block is not allowed through even when it paints nothing: the
			 * one XsltFOFunctions writes for a w:br carries
			 * linefeed-treatment="preserve" and a newline, and it is a line break, so the
			 * paragraph has a line the picture is not on.  Measured on the corpus
			 * document whose 400x311pt inline picture sits in a w:line="360"
			 * w:lineRule="auto" paragraph (J13's twin): letting it through changed
			 * nothing at all, because that paragraph's break is real.  @since 17.0.6 */
			state[1] = 1; // a painting leader, a page-number, a nested block, ...
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

	/** on an fo:leader standing in for a w:tab (XsltFOFunctions.tabToFO): the line
	 *  manager gives it its width.  @since 17.0.5 */
	public static final String HINT_TAB = "docx4j-tab";
	/** on the paragraph block of a paragraph holding a tab: its custom tab stops,
	 *  "pos:align:leader;..." in twips from the left margin.  @since 17.0.5 */
	public static final String HINT_TABS = "docx4j-tabs";
	/** with HINT_TABS: the default tab interval in twips (w:defaultTabStop). @since 17.0.5 */
	public static final String HINT_TAB_DEFAULT = "docx4j-tab-default";
	/** with HINT_TABS: "left:firstLineOffset" in twips, the paragraph's indents,
	 *  which put the tab stops and the running x in the same frame.  @since 17.0.5 */
	public static final String HINT_TAB_IND = "docx4j-tab-ind";

	private static final String[] TAB_HINTS = { HINT_TABS, HINT_TAB_DEFAULT, HINT_TAB_IND };

	/** on a table-of-contents entry's block (XsltFOFunctions.applyTocStopHint): the
	 *  entry's own right dot stop in twips from the left margin, which is where its
	 *  stretching leader ends.  @since 17.0.6 */
	public static final String HINT_TOC_STOP = "docx4j-toc-stop";

	/** on the block a {@code w:br w:type="column"} makes (BrWriter): where the section
	 *  has columns to go to, it is a column break and not a line break.  @since 17.0.6 */
	public static final String HINT_COLUMN_BREAK = "docx4j-colbreak";

	/** on the fo:leader which is the {@code w:suff} tab after an inline numbering label
	 *  (XsltFOFunctions.createInlineLabel): the label's number position to its text
	 *  position, from which the label's own measured width is taken.  @since 17.0.6 */
	public static final String HINT_LABEL_GAP = "docx4j-label-gap";

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
	/**
	 * The gap after an inline numbering label (&#xa7;2.8): Word's tab there takes the text
	 * to the level's text position, so what the leader has to be is that distance less the
	 * label's own width.  Measured on a centred TOC entry, Word's "1." at x=129.4 and its
	 * text at 147.5, 18.1pt apart, which is the level's 18pt hanging indent.
	 *
	 * <p>Here, rather than where the label is written, because the label's font is not
	 * settled until its block is finished.  Where it cannot be measured - no font, or a
	 * font FOP does not have - the leader stays at zero and the number abuts the text,
	 * which is nearer Word than the whole gap would be.</p>
	 *
	 * @since 17.0.6
	 */
	static void inlineLabelGaps(Document doc) {
		for (Element leader : elements(doc, "leader")) {
			String gap = leader.getAttribute(HINT_LABEL_GAP);
			if (gap.length() == 0) continue;
			leader.removeAttribute(HINT_LABEL_GAP);
			double gapPt = lengthPt(gap);
			Element block = enclosingBlock(leader);
			Node prev = leader.getPreviousSibling();
			if (block == null || !(prev instanceof Element)) continue;
			double width = textWidthPt(block, (Element) prev);
			if (width < 0) continue;
			leader.setAttribute("leader-length", pt(Math.max(0, gapPt - width)));
		}
	}

	/** The width of the text under this element, in the font each part of it is set in,
	 *  or -1 where any of it cannot be measured.  @since 17.0.6 */
	private static double textWidthPt(Element block, Element el) {
		String text = el.getTextContent();
		if (text == null || text.length() == 0) return 0;
		String family = null, size = null;
		for (Node p = el; p instanceof Element; p = p.getParentNode()) {
			Element e = (Element) p;
			if (family == null && e.getAttribute("font-family").length() > 0) {
				family = e.getAttribute("font-family");
			}
			if (size == null && e.getAttribute("font-size").length() > 0) {
				size = e.getAttribute("font-size");
			}
			if (e == block) break;
		}
		if (family == null || size == null) return -1;
		double sizePt = lengthPt(size);
		if (sizePt <= 0) return -1;
		org.docx4j.fonts.PhysicalFont pf = org.docx4j.fonts.PhysicalFonts.get(family);
		if (pf == null) return -1;
		try {
			double w = org.docx4j.fonts.TextMeasurer.widthPt(text, pf, sizePt);
			return w >= 0 ? w : -1;
		} catch (RuntimeException e) {
			return -1;
		}
	}

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

	/** Whether any block asks to be hyphenated (XsltFOFunctions.applyHyphenation):
	 *  only then are the document's hyphenation settings worth writing on fo:root.
	 *  @since 17.0.6 */
	private static boolean anyBlockHyphenates(Document doc) {
		for (Element block : elements(doc, "block")) {
			if ("true".equals(block.getAttribute("hyphenate"))) return true;
		}
		return false;
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
	 *
	 * fo:root also gets docx4j:space-shrink for a document Word lays out with a
	 * pre-2013 engine: measured over 190 Word goldens, a justified line's spaces
	 * are compressed (to as little as 0.76 of their natural width) in compatibility
	 * mode 15 only; in modes 11, 12 and 14, and where the setting is absent, Word
	 * never compresses them, so a word that does not fit at natural width goes to
	 * the next line.
	 */
	static void lineBoxAttributes(Document doc, int compatibilityMode) {
		lineBoxAttributes(doc, compatibilityMode, null);
	}

	/**
	 * @param hyphenation the document's hyphenation settings (w:hyphenationZone,
	 *        w:consecutiveHyphenLimit, w:doNotHyphenateCaps), which the line
	 *        manager applies to the paragraphs whose block carries
	 *        hyphenate="true"; null, or a document which does not hyphenate,
	 *        writes none of them.  @since 17.0.6
	 */
	static void lineBoxAttributes(Document doc, int compatibilityMode,
			org.docx4j.model.HyphenationSettings hyphenation) {
		String ns = extensionNamespace();
		boolean declared = false;
		Element root = doc.getDocumentElement();
		if (ns != null && root != null && isFo(root, "root")) {
			if (compatibilityMode < 15) {
				root.setAttributeNS(XMLNS, "xmlns:docx4j", ns);
				declared = true;
				root.setAttributeNS(ns, "docx4j:space-shrink", "0");
			}
			if (hyphenation != null && anyBlockHyphenates(doc)) {
				if (!declared) {
					root.setAttributeNS(XMLNS, "xmlns:docx4j", ns);
					declared = true;
				}
				root.setAttributeNS(ns, "docx4j:" + org.docx4j.fop.wordlayout.WordLayoutElementMapping.HYPHENATION_ZONE,
						Integer.toString(hyphenation.getZoneTwips()));
				if (hyphenation.getConsecutiveLimit() > 0) {
					root.setAttributeNS(ns, "docx4j:" + org.docx4j.fop.wordlayout.WordLayoutElementMapping.HYPHEN_LIMIT,
							Integer.toString(hyphenation.getConsecutiveLimit()));
				}
				if (hyphenation.isDoNotHyphenateCaps()) {
					root.setAttributeNS(ns, "docx4j:" + org.docx4j.fop.wordlayout.WordLayoutElementMapping.HYPHENATE_CAPS, "false");
				}
			}
		}
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
		// the tab leaders and, on their paragraph's block, the stops they are laid out
		// against (XsltFOFunctions.tabToFO / applyTabStopHints).  @since 17.0.5
		for (Element leader : elements(doc, "leader")) {
			String kind = leader.getAttribute(HINT_TAB);
			if (kind.length()==0) continue;
			leader.removeAttribute(HINT_TAB);
			if (ns == null) continue;
			if (!declared) {
				doc.getDocumentElement().setAttributeNS(XMLNS, "xmlns:docx4j", ns);
				declared = true;
			}
			leader.setAttributeNS(ns, "docx4j:tab", kind);
		}
		for (Element block : elements(doc, "block")) {
			String tabs = block.getAttribute(HINT_TABS);
			boolean hasTabs = block.hasAttribute(HINT_TABS);
			String tabDefault = block.getAttribute(HINT_TAB_DEFAULT);
			String tabInd = block.getAttribute(HINT_TAB_IND);
			for (String hint : TAB_HINTS) block.removeAttribute(hint);
			if (ns != null && hasTabs) {
				if (!declared) {
					doc.getDocumentElement().setAttributeNS(XMLNS, "xmlns:docx4j", ns);
					declared = true;
				}
				block.setAttributeNS(ns, "docx4j:tabs", tabs);
				block.setAttributeNS(ns, "docx4j:tab-default", tabDefault);
				block.setAttributeNS(ns, "docx4j:tab-ind", tabInd);
			}
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

	private static volatile List<org.docx4j.convert.out.fo.renderers.FopFactoryCustomizer> customizers;

	/** The extension namespace a loaded FopFactoryCustomizer supports, or null.
	 *  (The customizers are found once; each is still asked every time, since whether
	 *  it wants the attributes is a docx4j property the caller may change.) */
	public static String extensionNamespace() {
		List<org.docx4j.convert.out.fo.renderers.FopFactoryCustomizer> loaded = customizers;
		if (loaded == null) {
			loaded = new ArrayList<>();
			try {
				for (org.docx4j.convert.out.fo.renderers.FopFactoryCustomizer c
						: java.util.ServiceLoader.load(org.docx4j.convert.out.fo.renderers.FopFactoryCustomizer.class)) {
					loaded.add(c);
				}
			} catch (java.util.ServiceConfigurationError e) {
				log.warn("FopFactoryCustomizer lookup failed: " + e.getMessage());
			}
			customizers = loaded;
		}
		for (org.docx4j.convert.out.fo.renderers.FopFactoryCustomizer c : loaded) {
			String ns = c.extensionNamespace();
			if (ns != null) return ns;
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

	// ------------------------------------------------------------ 0e. text frames

	/** On a paragraph's fo:block (XsltFOFunctions.applyFrameHint): its w:framePr, as
	 *  {@code hAnchor:vAnchor:x:y:xAlign:yAlign:w:h:hRule:wrap:dropCap:hSpace:vSpace:lines}
	 *  with the lengths in twips.  @since 17.0.6 */
	public static final String HINT_FRAME = "docx4j-frame";

	/** docx4j.convert.out.fo.frames.position (default <b>true</b>): whether w:framePr is
	 *  honoured at all.  @since 17.0.6 */
	static boolean framesEnabled() {
		return isEnabled()
				&& org.docx4j.Docx4jProperties.getProperty("docx4j.convert.out.fo.frames.position", true);
	}

	/**
	 * {@code w:pPr/w:framePr}: Word's positioned text frames (ECMA-376 17.3.1.11).  A
	 * paragraph carrying one is not in the flow: Word puts it in a box {@code w:w} wide
	 * at {@code w:x} / {@code w:y} measured from what {@code w:hAnchor} /
	 * {@code w:vAnchor} name, and flows the body text past it.  docx4j laid such a
	 * paragraph out where it fell, which is the first divergence in four corpus
	 * documents and the whole of two; 53 documents of the three corpora carry 1,238 of
	 * them.
	 *
	 * <p>Measured against Word's own PDFs:</p>
	 * <ul>
	 * <li>{@code w:w=2926 w:h=748 w:hRule=exact w:vAnchor=page w:hAnchor=page w:x=8563
	 *     w:y=1702} on a page-margin 68.05pt document: Word draws the frame's text at
	 *     x=428.3 y=95.3, i.e. its box at 428.15 / 85.1 from the page's top left corner
	 *     - w:x and w:y exactly - where docx4j drew it in the flow at x=68.1 y=81.2.</li>
	 * <li>{@code w:framePr w:w=5281 w:hAnchor=text w:x=1441 w:y=3177} on a cover whose
	 *     page margins are all 0: Word's text is at (72.0, 169.5), which is the text
	 *     column's own left edge + 72.05 and the flow position + 158.85 - so an absent
	 *     w:vAnchor is "text", the offset being taken from where the paragraph would
	 *     have been.  docx4j had it at (0.0, 10.5).</li>
	 * </ul>
	 *
	 * <p>Consecutive paragraphs carrying the same {@code w:framePr} are one frame, as
	 * Word treats them, so they go into one container.</p>
	 *
	 * <p>What is implemented here is the <b>absolute</b> case: a frame anchored to the
	 * page or to the margin becomes an absolutely positioned block-container (the
	 * machinery {@link #anchorImages} uses for a picture Word positions).  A
	 * {@code w:vAnchor="text"} frame - Word wraps the body text around it, and its
	 * vertical position is relative to the paragraph it belongs to - is left in the flow,
	 * and so is {@code w:dropCap}: both need the float route of &#xa7;9.1 and both were
	 * measured a loss without it.</p>
	 *
	 * <p>The frame Word applies is the <b>effective</b> one: a style may carry a
	 * {@code w:framePr}, and its attributes inherit one at a time, so a paragraph stating
	 * only {@code w:w="3600"} keeps its style's anchors and {@code w:x}/{@code w:y} (see
	 * {@code StyleUtil.apply(CTFramePr, CTFramePr)} and
	 * {@code PropertyResolver.hasDirectPPrFormatting}, both of which lost it before
	 * 17.0.6).  On the letterhead above, that is what puts its address block at Word's
	 * (68.1, 102.1) rather than in the flow.</p>
	 *
	 * <p>On by default ({@code docx4j.convert.out.fo.frames.position=false} turns it
	 * off).  Corpus: three documents change and none falls - 0.804 -> 0.873 on one of 25
	 * absolute frames, 0.824 -> 0.960 and Word's page count on another, 0.840 -> 0.848 on
	 * a third.  The letterhead keeps its 20 of Word's 31 lines either way while its frames
	 * move to where Word draws them (median dy 58.2 -> 22.4); its residual is its
	 * page-anchored table, which {@link #anchorFloatingTables} declines to position
	 * because content precedes it.</p>
	 *
	 * @since 17.0.6
	 */
	static void positionFrames(Document doc) {
		if (!framesEnabled()) return;
		List<Element> framed = new ArrayList<Element>();
		for (Element block : elements(doc, "block")) {
			if (block.getAttribute(HINT_FRAME).length() == 0) continue;
			// only the outermost block of a frame: a block nested in another carrying the
			// same hint would be positioned a second time, inside its own container
			if (hasFramedAncestor(block)) {
				block.removeAttribute(HINT_FRAME);
				continue;
			}
			framed.add(block);
		}
		int i = 0;
		java.util.Map<Node, Double> reservedTo = new java.util.HashMap<Node, Double>();
		while (i < framed.size()) {
			Element first = framed.get(i);
			String spec = first.getAttribute(HINT_FRAME);
			// consecutive paragraphs carrying the same w:framePr are one frame
			List<Element> group = new ArrayList<Element>();
			group.add(first);
			int j = i + 1;
			while (j < framed.size()) {
				Element next = framed.get(j);
				if (!spec.equals(next.getAttribute(HINT_FRAME))) break;
				if (next.getParentNode() != first.getParentNode()) break;
				if (nextElementSibling(group.get(group.size() - 1)) != next) break;
				group.add(next);
				j++;
			}
			i = j;
			try {
				positionFrame(doc, group, spec, reservedTo);
			} catch (RuntimeException e) {
				log.warn("Text frame left in the flow: " + e.getMessage(), e);
			}
			for (Element block : group) block.removeAttribute(HINT_FRAME);
		}
	}

	/** Whether some ancestor of this block carries {@link #HINT_FRAME} too. */
	private static boolean hasFramedAncestor(Element block) {
		for (Node n = block.getParentNode(); n instanceof Element; n = n.getParentNode()) {
			if (((Element) n).getAttribute(HINT_FRAME).length() > 0) return true;
		}
		return false;
	}

	private static void positionFrame(Document doc, List<Element> group, String spec,
			java.util.Map<Node, Double> reservedTo) {
		String[] f = spec.split(":", -1);
		if (f.length < 11) return;
		String hAnchor = f[0], vAnchor = f[1], xAlign = f[4], yAlign = f[5];
		String hRule = f[8], wrap = f[9], dropCap = f[10];
		double x = twips(f[2]), y = twips(f[3]), w = twips(f[6]), h = twips(f[7]);
		double hSpace = f.length > 11 ? twips(f[11]) : 0;
		double vSpace = f.length > 12 ? twips(f[12]) : 0;
		int capLines = f.length > 13 ? intOrZero(f[13]) : 0;
		Element first = group.get(0);

		Element rb = regionBody(first);
		double marginLeft = 0, marginTop = 0, pageWidth = 0;
		if (rb != null && rb.getParentNode() instanceof Element) {
			Element spm = (Element) rb.getParentNode();
			pageWidth = lengthPt(spm.getAttribute("page-width"));
			marginLeft = lengthPt(spm.getAttribute("margin-left")) + lengthPt(rb.getAttribute("margin-left"));
			marginTop = lengthPt(spm.getAttribute("margin-top")) + lengthPt(rb.getAttribute("margin-top"));
		}
		double measure = pageWidth - marginLeft
				- (rb == null ? 0 : lengthPt(((Element) rb.getParentNode()).getAttribute("margin-right"))
						+ lengthPt(rb.getAttribute("margin-right")));

		// a drop cap is a frame set into the paragraph beside it, not a positioned box
		if (dropCap.length() > 0 && !"none".equals(dropCap)) {
			dropCapFrame(doc, group, dropCap, capLines, hSpace, marginLeft);
			return;
		}

		// the frame's left edge, from the page's own left edge
		double left;
		if ("page".equals(hAnchor)) left = x;
		else left = marginLeft + x;              // "margin", "text" (the column) and the default
		if (xAlign.length() > 0) {
			double frame = "page".equals(hAnchor) ? pageWidth : Math.max(0, measure);
			double base = "page".equals(hAnchor) ? 0 : marginLeft;
			if ("center".equals(xAlign)) left = base + Math.max(0, (frame - w) / 2);
			else if ("right".equals(xAlign) || "outside".equals(xAlign)) left = base + Math.max(0, frame - w);
			else left = base;                    // left, inside
		}

		Node parent = first.getParentNode();
		if (parent == null) return;
		Node after = group.get(group.size() - 1).getNextSibling();

		// A frame anchored to the page or to the margin is taken out of the flow into a
		// positioned container; a w:vAnchor="text" frame is positioned against the
		// paragraph it belongs to, with the body text running beside it, which is an
		// fo:float ({@link #floatFrame}).
		boolean absolute = ("page".equals(vAnchor) || "margin".equals(vAnchor))
				&& !"inline".equals(yAlign);
		if (!absolute) {
			floatFrame(doc, group, w, left - marginLeft, y, measure, hSpace, vSpace, wrap);
			return;
		}
		if (w <= 0) w = Math.max(0, measure - (left - marginLeft));
		if (w <= 0) return;
		{
			double top = "page".equals(vAnchor) ? y : marginTop + y;
			if (yAlign.length() > 0) {
				double frameTop = "page".equals(vAnchor) ? 0 : marginTop;
				top = frameTop;                  // top; centre and bottom need the page height
			}
			Element wrapper = doc.createElementNS(FO_NS, "fo:block-container");
			wrapper.setAttribute("height", "0pt");
			wrapper.setAttribute("overflow", "visible");
			wrapper.setAttribute("start-indent", "0pt");
			wrapper.setAttribute("end-indent", "0pt");
			Element abs = doc.createElementNS(FO_NS, "fo:block-container");
			abs.setAttribute("absolute-position", "fixed");
			abs.setAttribute("top", pt(top));
			abs.setAttribute("left", pt(left));
			abs.setAttribute("width", pt(w));
			if (h > 0 && "exact".equals(hRule)) abs.setAttribute("height", pt(h));
			abs.setAttribute("overflow", "visible");
			abs.setAttribute("start-indent", "0pt");
			abs.setAttribute("end-indent", "0pt");
			wrapper.appendChild(abs);
			reserveBand(parent, after, group, f[9], top, reservedTo);
			moveInto(abs, group);
			parent.insertBefore(wrapper, after); // insertBefore(w, null) appends
		}
	}

	/**
	 * {@code w:wrap}: whether the body text may run beside the frame.  {@code notBeside}
	 * and {@code none} say it may not, so Word skips the frame's band - the flow resumes
	 * below it.  The band is reproduced by leaving an invisible copy of the frame's own
	 * blocks where they were: they reserve exactly the height Word's frame occupies (FOP
	 * honours {@code visibility="hidden"} - the area keeps its size and paints nothing),
	 * which the frame's {@code w:h} does not give, since {@code w:hRule="auto"} is the
	 * common case.
	 *
	 * <p>Measured on the corpus letterhead of {@link #positionFrames}: its seven
	 * page-anchored frames tile the top of the page, and without the reservation the body
	 * text moved up 125pt - Word starts it at y=310.2, where the frames end.  With it the
	 * frames go where Word draws them <em>and</em> the flow keeps its place.</p>
	 *
	 * <p>{@code around}, {@code tight} and {@code through} - and the default,
	 * {@code auto} - do let text run beside the frame, so they reserve nothing.</p>
	 *
	 * <p>What Word skips is the <b>union</b> of the frames' bands, not the sum of their
	 * heights: the letterhead sets its frames out in pairs, two at {@code w:y=3743} and
	 * two at {@code w:y=4821}, and Word's flow steps over each pair once.  A frame whose
	 * top is not below the last one reserved in this flow therefore shares its band and
	 * reserves nothing (reserving both put the body text 58pt too low).</p>
	 */
	private static void reserveBand(Node parent, Node after, List<Element> group, String wrap,
			double top, java.util.Map<Node, Double> reservedTo) {
		if (!"notBeside".equals(wrap) && !"none".equals(wrap)) return;
		Double last = reservedTo.get(parent);
		if (last != null && top <= last.doubleValue()) return;
		reservedTo.put(parent, Double.valueOf(top));
		for (Element block : group) {
			Element copy = (Element) block.cloneNode(true);
			copy.setAttribute("visibility", "hidden");
			stripIds(copy);
			parent.insertBefore(copy, after);
		}
	}

	/** An id must not appear twice in the FO, so the invisible copy loses them all. */
	private static void stripIds(Element el) {
		el.removeAttribute("id");
		for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) stripIds((Element) n);
		}
	}

	/** Moves the blocks out of the flow and into the container, keeping their order. */
	private static void moveInto(Element container, List<Element> group) {
		for (Element block : group) {
			block.getParentNode().removeChild(block);
			block.setAttribute("start-indent", "0pt");
			block.setAttribute("end-indent", "0pt");
			container.appendChild(block);
		}
	}

	/** A twip string as points; 0 where it is absent or unparseable. */
	private static double twips(String v) {
		if (v == null || v.length() == 0) return 0;
		try {
			return Double.parseDouble(v.trim()) / 20d;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/** An integer string, or 0 where it is absent or unparseable. */
	private static int intOrZero(String v) {
		if (v == null || v.length() == 0) return 0;
		try {
			return Integer.parseInt(v.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/** A frame taking more than this share of the column has no room for text beside it,
	 *  so it is left in the flow - which is where the text after it goes in Word too.
	 *  The same cut as a floating table's ({@code TableWriter.FLOAT_MAX_SHARE}). */
	static final double FLOAT_MAX_SHARE = 0.6;

	/**
	 * A {@code w:vAnchor="text"} frame: Word draws it at the anchor paragraph's own
	 * position offset by {@code w:x} / {@code w:y}, and flows the text that follows
	 * <em>beside</em> it, {@code w:hSpace} / {@code w:vSpace} away.  That is an
	 * {@code fo:float} at the edge the frame is nearer, exactly as &#xa7;9.1 floats a
	 * picture Word wraps text around and &#xa7;6.8 a text-anchored floating table.
	 *
	 * <p>FOP gives a float the ipd of its content and ignores the padding of the block
	 * inside it, so - as for the floating table - the float holds a <b>one-row table
	 * whose columns are the offset from the column edge, the frame, and the gap to the
	 * text</b>: that reserves exactly the band Word keeps clear and puts the frame at
	 * {@code w:x} within it.  {@code w:y} is padding above the frame inside the float,
	 * and {@code w:vSpace} padding below it.  FOP anchors a side float to a line and
	 * drops one which has none, so the float goes inside the first block after the frame
	 * - the paragraph that now begins where the framed paragraph was, which is what Word
	 * measures {@code w:y} from.</p>
	 *
	 * <p><b>Left in the flow</b>, where the text follows the frame rather than running
	 * beside it, and which is what docx4j did with every text-anchored frame before
	 * 17.0.6:</p>
	 * <ul>
	 * <li>a frame with no {@code w:w} at all, which fills the rest of the measure - the
	 *     shape of all 499 frames of the corpus document that is the acid test here
	 *     (0.954 of Word's lines in the flow, 0.573 in a container of its own width);</li>
	 * <li>one over {@value #FLOAT_MAX_SHARE} of the column, where nothing useful fits
	 *     beside it (&#xa7;6.8's cut, measured there);</li>
	 * <li>{@code w:wrap="notBeside"} or {@code "none"}, which say no text may run beside
	 *     the frame: in the flow the frame already reserves its own band;</li>
	 * <li>one whose band falls outside the column, one in a table cell, a header, a
	 *     footer or a footnote, and one in a multi-column region - FOP lays out no side
	 *     float in any of those, and paints nothing at all for one (&#xa7;10);</li>
	 * <li>one which an {@code fo:block} inside an {@code fo:inline} follows - a line
	 *     break inside a run - since that combination throws in FOP
	 *     ({@link #hoistFloats}) and a float holding a table cannot be hoisted;</li>
	 * <li>one with no following block to anchor the float to.</li>
	 * </ul>
	 *
	 * @param w      the frame's width, {@code w:w}
	 * @param x      its left edge within the column
	 * @param y      {@code w:y}, the drop from the anchor paragraph's top
	 * @param measure the column's width
	 * @since 17.0.6
	 */
	private static void floatFrame(Document doc, List<Element> group, double w, double x,
			double y, double measure, double hSpace, double vSpace, String wrap) {
		if (!FOConversionContext.useFloats()) return;
		// no w:w: the frame fills the rest of the measure, so nothing runs beside it
		if (w <= 0 || measure <= 0) return;
		if (w > FLOAT_MAX_SHARE * measure) return;
		// no text may run beside it: in the flow the frame reserves its own band already
		if ("notBeside".equals(wrap) || "none".equals(wrap)) return;
		Element first = group.get(0), last = group.get(group.size() - 1);
		if (!floatsAllowed(first)) return;
		if (x < -0.5 || x + w > measure + 0.5) return;
		if (blockInsideInlineAfter(doc, last)) return;

		// the block the float anchors to: the first one after the frame that is not
		// itself framed (a following frame is moved out of the flow in its turn)
		Element anchor = null;
		for (Node n = last.getNextSibling(); n != null; n = n.getNextSibling()) {
			if (!(n instanceof Element)) continue;
			Element el = (Element) n;
			if (isFo(el, "block") && el.getAttribute(HINT_FRAME).length() == 0) anchor = el;
			else if (isFo(el, "block") || takesNoSpace(el)) continue;
			break;
		}
		if (anchor == null) return;

		double padLeft, padRight;
		if (x + w / 2 > measure / 2) {           // the frame is nearer the right edge
			padLeft = hSpace;
			padRight = Math.max(0, measure - x - w);
		} else {
			padLeft = Math.max(0, x);
			padRight = hSpace;
		}
		Element wrapper = doc.createElementNS(FO_NS, "fo:float");
		wrapper.setAttribute("float", x + w / 2 > measure / 2 ? "right" : "left");
		Element holder = doc.createElementNS(FO_NS, "fo:block");
		holder.setAttribute("start-indent", "0pt");
		holder.setAttribute("end-indent", "0pt");
		if (y > 0) holder.setAttribute("padding-top", pt(y));
		if (vSpace > 0) holder.setAttribute("padding-bottom", pt(vSpace));
		wrapper.appendChild(holder);
		Element cell = bandTable(doc, holder, padLeft, w, padRight);
		for (Element block : group) dropPagination(block);
		moveInto(cell, group);
		anchor.insertBefore(wrapper, anchor.getFirstChild());
	}

	/**
	 * The one-row fixed-layout table a float uses to reserve a band: columns of
	 * {@code before}, {@code width} and {@code after}, the middle cell returned for the
	 * content.  FOP takes a float's ipd from its content and ignores the padding of the
	 * block inside it, so the gaps have to be columns (&#xa7;6.8).
	 */
	private static Element bandTable(Document doc, Element holder, double before, double width,
			double after) {
		Element outer = doc.createElementNS(FO_NS, "fo:table");
		outer.setAttribute("table-layout", "fixed");
		outer.setAttribute("width", pt(before + width + after));
		outer.setAttribute("start-indent", "0pt");
		outer.setAttribute("end-indent", "0pt");
		holder.appendChild(outer);
		Element body = doc.createElementNS(FO_NS, "fo:table-body");
		Element row = doc.createElementNS(FO_NS, "fo:table-row");
		double[] widths = { before, width, after };
		Element content = null;
		for (int i = 0; i < widths.length; i++) {
			if (widths[i] <= 0) continue;
			Element col = doc.createElementNS(FO_NS, "fo:table-column");
			col.setAttribute("column-width", pt(widths[i]));
			outer.appendChild(col);
			Element cell = doc.createElementNS(FO_NS, "fo:table-cell");
			row.appendChild(cell);
			if (i == 1) content = cell;
			else {
				Element blank = doc.createElementNS(FO_NS, "fo:block");
				blank.setAttribute("font-size", "0.1pt");
				blank.setAttribute("line-height", "0pt");
				cell.appendChild(blank);
			}
		}
		outer.appendChild(body);
		body.appendChild(row);
		return content;
	}

	/**
	 * {@code w:dropCap}: the framed paragraph <em>is</em> the cap - Word puts the
	 * paragraph's first character(s) in a frame of their own and runs the first
	 * {@code w:lines} lines of the paragraph that follows beside it.  Word writes the
	 * enlarged size into the run itself ({@code w:sz}), so nothing here computes a font
	 * size: what has to be reproduced is the band, which is {@code w:lines} lines of the
	 * following paragraph's pitch by the cap's own advance width.
	 *
	 * <p>{@code w:dropCap="drop"} sets the cap into the text, so it is an
	 * {@code fo:float} at the start edge, the same machinery as {@link #floatFrame};
	 * {@code "margin"} hangs it in the margin, which is a positioned container at the
	 * column's left edge less the cap's width, taking no space.</p>
	 *
	 * <p><b>Not measured against a Word golden</b>: no document of the three corpora
	 * (1,238 frames in 53 of them) carries a {@code w:dropCap} at all, and no probe has a
	 * golden for one, so the geometry here is the rule as ECMA-376 17.3.1.11 states it
	 * and as Word's own markup implies, not a measurement.  It changes no corpus
	 * document.</p>
	 *
	 * @since 17.0.6
	 */
	private static void dropCapFrame(Document doc, List<Element> group, String dropCap,
			int capLines, double hSpace, double marginLeft) {
		Element first = group.get(0), last = group.get(group.size() - 1);
		if (!floatsAllowed(first)) return;

		Element anchor = null;
		for (Node n = last.getNextSibling(); n != null; n = n.getNextSibling()) {
			if (!(n instanceof Element)) continue;
			Element el = (Element) n;
			if (isFo(el, "block") && el.getAttribute(HINT_FRAME).length() == 0) anchor = el;
			else if (isFo(el, "block") || takesNoSpace(el)) continue;
			break;
		}
		if (anchor == null) return;

		// the cap's own advance, from the font its run is set in
		double width = 0;
		for (Element block : group) {
			NodeList inlines = block.getElementsByTagNameNS(FO_NS, "inline");
			double w = textWidthPt(block, inlines.getLength() > 0 ? (Element) inlines.item(0) : block);
			if (w < 0) return;                   // the font could not be measured
			width = Math.max(width, w);
		}
		if (width <= 0) return;

		boolean margin = "margin".equals(dropCap);
		if (!margin && (!FOConversionContext.useFloats() || blockInsideInlineAfter(doc, last))) {
			return;                              // no float to be had: leave it in the flow
		}

		// the band is w:lines lines of the paragraph the cap is set into
		double pitch = lengthPt(anchor.getAttribute("line-height"));
		if (capLines > 1 && pitch > 0) {
			for (Element block : group) block.setAttribute("line-height", pt(capLines * pitch));
		}
		for (Element block : group) dropPagination(block);

		if (margin) {
			// hung in the margin: it takes no space in the flow
			Element wrapper = doc.createElementNS(FO_NS, "fo:block-container");
			wrapper.setAttribute("height", "0pt");
			wrapper.setAttribute("overflow", "visible");
			wrapper.setAttribute("start-indent", "0pt");
			wrapper.setAttribute("end-indent", "0pt");
			Element abs = doc.createElementNS(FO_NS, "fo:block-container");
			abs.setAttribute("absolute-position", "absolute");
			abs.setAttribute("top", "0pt");
			abs.setAttribute("left", pt(-(width + hSpace)));
			abs.setAttribute("width", pt(width));
			abs.setAttribute("overflow", "visible");
			abs.setAttribute("start-indent", "0pt");
			abs.setAttribute("end-indent", "0pt");
			wrapper.appendChild(abs);
			moveInto(abs, group);
			insertAnchorWrapper(anchor, wrapper);
			return;
		}
		Element wrapper = doc.createElementNS(FO_NS, "fo:float");
		wrapper.setAttribute("float", "left");
		Element holder = doc.createElementNS(FO_NS, "fo:block");
		holder.setAttribute("start-indent", "0pt");
		holder.setAttribute("end-indent", "0pt");
		wrapper.appendChild(holder);
		Element cell = bandTable(doc, holder, 0, width, hSpace);
		moveInto(cell, group);
		anchor.insertBefore(wrapper, anchor.getFirstChild());
	}

	// ------------------------------------------------------------ 0f. floating tables

	/** On an fo:table (TableWriter): the grid edge's distance from the page's left edge.
	 *  @since 17.0.6 */
	public static final String HINT_TBLP_LEFT = "docx4j-tblp-left";
	/** On an fo:table: the top edge's distance from the page's top edge. @since 17.0.6 */
	public static final String HINT_TBLP_TOP = "docx4j-tblp-top";
	/** On an fo:table: "top height" of the box the table is aligned in, from the page's
	 *  top edge, where the docx gives a w:tblpYSpec rather than a w:tblpY. @since 17.0.6 */
	public static final String HINT_TBLP_FRAME = "docx4j-tblp-frame";
	/** With {@link #HINT_TBLP_FRAME}: before, center or after. @since 17.0.6 */
	public static final String HINT_TBLP_ALIGN = "docx4j-tblp-align";

	/** On an fo:table: "left" or "right", the edge a text-anchored floating table floats
	 *  to (TableWriter.floatBesideText).  @since 17.0.6 */
	public static final String HINT_TBLP_FLOAT = "docx4j-tblp-float";
	/** With {@link #HINT_TBLP_FLOAT}: the float holder's padding, left right top, which
	 *  is what puts the table where Word puts it.  @since 17.0.6 */
	public static final String HINT_TBLP_PAD = "docx4j-tblp-pad";

	/** On an fo:table: "true" where text could fit beside the table in its column, which
	 *  is the case in which Word's wrapping is worth reproducing.  @since 17.0.6 */
	public static final String HINT_TBLP_NARROW = "docx4j-tblp-narrow";

	private static final String[] TBLP_HINTS = { HINT_TBLP_LEFT, HINT_TBLP_TOP,
			HINT_TBLP_FRAME, HINT_TBLP_ALIGN, HINT_TBLP_FLOAT, HINT_TBLP_PAD,
			HINT_TBLP_NARROW };

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
	 * ascender).
	 * <p>Floats only work in the main flow - FOP reports "fo:float (on fo:table)
	 * isn't implemented" and paints nothing at all for one in a table cell - so
	 * where there is no float to be had a wrapped picture takes the text box's
	 * treatment ({@link #anchorTextBox}, §9.2): narrower than 60% of its measure
	 * (the cell's content width in a cell, the text column otherwise) it is
	 * positioned where Word puts it and takes no space, and wider than that it
	 * reserves its height as a top-and-bottom wrap does.  In the flow a picture is
	 * floated however wide it is, since Word wraps beside one which leaves any room
	 * at all (measured: text beside a 348pt picture on a 453.55pt column), until it
	 * leaves no room - over 90% of the measure - where Word puts the text below it
	 * and FOP would anchor the float to a line and paint the picture over the page
	 * edge.  A picture with a page-relative vertical position is fixed without
	 * wrapping.</p>
	 */
	static void anchorImages(Document doc) {
		// instream-foreign-object as well as external-graphic: since 17.0.6 a WMF/EMF
		// picture is drawn as SVG inside one of those (CR-011), and it anchors the same
		List<Element> graphics = elements(doc, "external-graphic");
		graphics.addAll(elements(doc, "instream-foreign-object"));
		for (Element g : graphics) {
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

		// in a multi-column section the anchor hints' "column" is the section's whole
		// text column; Word's is the column the object is anchored in (@since 17.0.6)
		double oneColumn = columnWidthPt(para);
		if (oneColumn > 0) col = oneColumn;

		// the picture at its extent (content-width/height carry rounded pixels)
		g.setAttribute("content-width", pt(w));
		g.setAttribute("content-height", pt(h));
		Element holder = doc.createElementNS(FO_NS, "fo:block");
		holder.setAttribute("font-size", "0.1pt");
		holder.setAttribute("line-height", "0pt");
		holder.appendChild(g); // moves it out of its run

		if (pageY) kind = "none"; // FOP cannot wrap text around a page-positioned object
		/* A picture Word draws *behind* the text (wp:anchor/@behindDoc="1") displaces
		 * nothing: its wrap element is not applied, the text runs over it, and it
		 * reserves no space.  Measured on a document whose 155.25pt logo carries
		 * wrapSquare and behindDoc="1": Word puts the caption beside it at x=85.0,
		 * where the float put ours at 249.3 - +164.3 = the picture's 155.25 plus its
		 * 9.0pt distL/distR - and moved a whole block from above the table to below it,
		 * everything after it +92.6pt.  In the body only: a header or footer region takes
		 * its height from what is in it, and a picture drawn behind that region's text is
		 * still what gives it that height - measured, letting a behindDoc anchor in a
		 * footer reserve nothing took a document's region-body margin-bottom from 73.7 to
		 * 50.2pt and cost it a page of 27.  @since 17.0.6 */
		if ("1".equals(g.getAttribute("docx4j-anchor-behind")) && floatsAllowed(para)) kind = "none";
		if ("square".equals(kind) && !FOConversionContext.useFloats()) {
			kind = "topAndBottom"; // the property asks for the picture to be in the flow
		} else if ("square".equals(kind) && oneColumn > 0) {
			/* A multi-column region: FOP paints no float there at all, and a reservation
			 * is charged to the column the anchor is in - where Word wraps the text
			 * beside the object inside its own column, or draws it in another column
			 * altogether.  Positioning it is the closer of the two: measured on a
			 * two-column page whose 186.75pt text box sits in a 213pt column, reserving
			 * its height cost a page and took line parity from 0.478 to 0.087, and on a
			 * landscape two-column document the reservation put the title at y=323.0
			 * against Word's 37.0.  @since 17.0.6 */
			kind = "none";
		} else if ("square".equals(kind)) {
			double measure = anchorMeasure(para, col);
			if (!floatsAllowed(para)) {
				// no float to be had here (a table cell, a header or footer, a footnote):
				// the §9.2 rule, positioned where Word puts it or reserving its height
				kind = (measure > 0 && w < 0.6 * measure) ? "none" : "topAndBottom";
			} else if (measure > 0 && w > 0.9 * measure) {
				// a picture with no room for text beside it: Word puts the text below it,
				// which is what the flow does anyway, and FOP would otherwise anchor the
				// float to a line and paint the picture over the page edge
				kind = "topAndBottom";
			}
		}
		/* A picture whose horizontal position puts it in a *later* column reserves
		 * nothing in the column its anchor is in: Word lays it out in the column it
		 * occupies, and the flow beside it is untouched.  Measured on a landscape
		 * two-column document (columns 360.675pt) whose 340.15 x 246.75pt picture is
		 * anchored at 406.0pt from the margin: Word draws it at x=448.5 in column 2 and
		 * keeps its title at y=37.0, where the 278.4pt reservation at the head of
		 * column 1 put ours at 323.0.  An absolutely positioned container is measured
		 * from the same origin, so it lands where Word puts it.  @since 17.0.6 */
		if (oneColumn > 0 && x >= oneColumn && !"none".equals(kind)) kind = "none";

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
		insertAnchorWrapper(para, wrapper);
	}

	/**
	 * Puts an anchored object's wrapper at the head of its paragraph, with the wrappers
	 * which take no space ahead of those which reserve height.
	 *
	 * <p>An absolutely positioned container is placed relative to its own zero-height
	 * wrapper - an fo:block-container is a reference area - so that wrapper has to sit at
	 * the paragraph's top for the docx's offset to mean what Word means by it.  Measured
	 * on a cell holding a full-width wrapped picture and a small one 10.6pt below the
	 * paragraph's top: with the reservation first, the small picture came out at 179.9
	 * where Word has it at 81.5, one reserved height (98.6pt) low.</p>
	 *
	 * @since 17.0.6
	 */
	private static void insertAnchorWrapper(Element para, Element wrapper) {
		Node at = para.getFirstChild();
		if (!takesNoSpace(wrapper)) {
			while (at instanceof Element && takesNoSpace((Element) at)) at = at.getNextSibling();
		}
		para.insertBefore(wrapper, at); // insertBefore(w, null) appends
	}

	/**
	 * The width a wrapped anchored object's width is judged against: the containing
	 * cell's content width where the object is in a table cell - Word measures a "column"
	 * position from the cell there, and the cell is what the object could have text
	 * beside it in - and the section's text column otherwise.
	 *
	 * @param para the paragraph's block
	 * @param col  the section's text column width, from the anchor hints
	 * @return the measure in points, or 0 where it is not known
	 * @since 17.0.6
	 */
	private static double anchorMeasure(Element para, double col) {
		Element cell = ancestorCell(para);
		if (cell == null) return col;
		double w = cellContentWidthPt(cell);
		return w > 0 ? w : col;
	}

	/** The nearest ancestor fo:table-cell, or null (stopping at the flow). */
	private static Element ancestorCell(Element el) {
		for (Node n = el.getParentNode(); n instanceof Element; n = n.getParentNode()) {
			Element e = (Element) n;
			if (isFo(e, "table-cell")) return e;
			if (isFo(e, "flow") || isFo(e, "static-content")) return null;
		}
		return null;
	}

	/**
	 * A cell's content width: the fo:table-column widths its grid slots cover, less its
	 * own padding.  The cell's column index is the number of grid columns the cells
	 * before it in the row occupy (fo:table-cell carries no column index of its own here).
	 */
	private static double cellContentWidthPt(Element cell) {
		Element table = ancestorTable(cell);
		Node row = cell.getParentNode();
		if (table == null || !(row instanceof Element)) return 0;
		List<Double> columns = new ArrayList<>();
		for (Node n = table.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && isFo((Element) n, "table-column")) {
				columns.add(lengthPt(((Element) n).getAttribute("column-width")));
			}
		}
		if (columns.isEmpty()) return 0;
		int index = 0;
		for (Node n = row.getFirstChild(); n != null && n != cell; n = n.getNextSibling()) {
			if (n instanceof Element && isFo((Element) n, "table-cell")) {
				index += spanned((Element) n);
			}
		}
		double w = 0;
		for (int i = index; i < index + spanned(cell) && i < columns.size(); i++) w += columns.get(i);
		return w - lengthPt(cell.getAttribute("padding-left")) - lengthPt(cell.getAttribute("padding-right"));
	}

	private static int spanned(Element cell) {
		String v = cell.getAttribute("number-columns-spanned");
		if (v == null || v.length() == 0) return 1;
		try {
			return Math.max(1, Integer.parseInt(v.trim()));
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	/**
	 * A floating table (w:tblPr/w:tblpPr) whose vertical position Word measures from the
	 * page or from the margin box is placed there, out of the flow: the fo:table goes into
	 * an absolutely positioned fo:block-container, as an anchored picture does
	 * ({@link #anchorImage}), so the flow closes up over it.
	 * The container is as wide as the table, so the table keeps its columns, cell margins
	 * and borders; its start-indent is reset, since the container carries the position.
	 *
	 * <p>Where the docx states a w:tblpYSpec (top, centre or bottom of the page or of the
	 * margin box) rather than a w:tblpY, the table's height is not known before layout, so
	 * the container is the whole box with display-align on it and FOP places the table
	 * inside it. Measured: a cover-page table with tblpYSpec="bottom" on an A4 page with a
	 * 70.9pt bottom margin has its last line at y=765.4, ie its bottom edge on the bottom
	 * margin.</p>
	 *
	 * <p>Only a table that <b>opens its section's flow</b> is moved - the cover page and
	 * the letterhead, which is what the rule is for. Word flows the text after a floating
	 * table around it, and where the table is as wide as the column that means below it;
	 * XSL-FO cannot express that, and the table's height is not known before layout, so a
	 * floating table with text after it stays in the flow, where the text at least follows
	 * it. Measured on two corpus documents whose page-anchored table sits mid-flow: Word
	 * puts the table at its w:tblpY (y=94.6, first line 111.1) and the next table below it
	 * (y=257.6); positioning ours and closing the flow over it drew the two on top of each
	 * other and cost both documents ~0.09 of line parity.</p>
	 *
	 * <p>A table inside a table cell, a header or a footer is likewise left where it is
	 * (an absolutely positioned container there would leave the cell's own geometry
	 * wrong, and Word's frame rules differ inside a cell).</p>
	 *
	 * @since 17.0.6
	 */
	static void anchorFloatingTables(Document doc) {
		for (Element tbl : elements(doc, "table")) {
			boolean positioned = tbl.hasAttribute(HINT_TBLP_LEFT)
					&& (tbl.hasAttribute(HINT_TBLP_TOP) || tbl.hasAttribute(HINT_TBLP_FRAME));
			try {
				if (positioned) {
					anchorFloatingTable(doc, tbl);
				} else if (tbl.hasAttribute(HINT_TBLP_FLOAT)) {
					floatFloatingTable(doc, tbl);
				}
			} catch (RuntimeException e) {
				log.warn("Floating table left in the flow: " + e.getMessage(), e);
			}
			for (String hint : TBLP_HINTS) tbl.removeAttribute(hint);
		}
	}

	/**
	 * A text-anchored floating table (the default {@code w:vertAnchor="text"}), which Word
	 * puts beside the text of the paragraph it is anchored to - the paragraph the w:tbl
	 * precedes: an fo:float inside that paragraph, holding a one-row table whose columns
	 * are the gap to the text, the table, and what is left of the column
	 * ({@link org.docx4j.convert.out.fo.TableWriter} measured them from {@code w:tblpX},
	 * {@code w:leftFromText} and {@code w:rightFromText}; {@code w:tblpY} is padding above
	 * the table).
	 *
	 * <p>The table's own indents are reset, since the one-row table carries the position.
	 * A float only lays out in the main flow, so a table in a cell, a header, a footer or
	 * a footnote is left where it is.</p>
	 *
	 * @since 17.0.6
	 */
	private static void floatFloatingTable(Document doc, Element tbl) {
		Node parent = tbl.getParentNode();
		if (!(parent instanceof Element) || !isFo((Element) parent, "flow")) return;
		// {@link #hoistFloats} would move this float to flow level, where FOP renders a
		// float holding a table as nothing at all (measured), losing the table; and left
		// where it is, the combination throws.  So the table stays in the flow instead.
		if (blockInsideInlineAfter(doc, tbl)) {
			log.debug("Floating table left in the flow: a line break inside a run follows it");
			return;
		}

		String[] pad = tbl.getAttribute(HINT_TBLP_PAD).trim().split("\\s+");
		if (pad.length < 3) return;
		double padLeft = lengthPt(pad[0]), padRight = lengthPt(pad[1]), padTop = lengthPt(pad[2]);
		double width = tableWidthPt(tbl);
		if (width <= 0) return;

		Element wrapper = doc.createElementNS(FO_NS, "fo:float");
		wrapper.setAttribute("float", tbl.getAttribute(HINT_TBLP_FLOAT));
		Element holder = doc.createElementNS(FO_NS, "fo:block");
		holder.setAttribute("start-indent", "0pt");
		holder.setAttribute("end-indent", "0pt");
		/* w:tblpY, the drop from the top of the anchor paragraph to the top of Word's
		 * frame, is padding on the block inside the float.  Measured on the
		 * table-floating probe (tblpY=1440): Word's anchor paragraph begins at y=111.7
		 * and its "float a" cell line at 186.8, ours at 186.6.  (What FOP will not do is
		 * leave the lines *above* the table full width: it anchors the float at the line
		 * it sits at, so the padding narrows them too - §10.) */
		if (padTop > 0) holder.setAttribute("padding-top", pt(padTop));
		wrapper.appendChild(holder);

		// FOP gives the float area the ipd of its content and ignores the padding of the
		// block in it (measured: a right float's padding-right does not move the table,
		// and its padding-left pushes it past the margin), so the gaps are columns of a
		// one-row table which holds the table itself: that reserves exactly the band Word
		// keeps clear, and puts the table at w:tblpX within it.
		Element outer = doc.createElementNS(FO_NS, "fo:table");
		outer.setAttribute("table-layout", "fixed");
		outer.setAttribute("width", pt(padLeft + width + padRight));
		outer.setAttribute("start-indent", "0pt");
		outer.setAttribute("end-indent", "0pt");
		Element body = doc.createElementNS(FO_NS, "fo:table-body");
		Element row = doc.createElementNS(FO_NS, "fo:table-row");
		holder.appendChild(outer);
		double[] widths = { padLeft, width, padRight };
		for (int i = 0; i < widths.length; i++) {
			if (widths[i] <= 0) continue;
			Element col = doc.createElementNS(FO_NS, "fo:table-column");
			col.setAttribute("column-width", pt(widths[i]));
			outer.appendChild(col);
			Element cell = doc.createElementNS(FO_NS, "fo:table-cell");
			row.appendChild(cell);
			if (i != 1) {
				Element blank = doc.createElementNS(FO_NS, "fo:block");
				blank.setAttribute("font-size", "0.1pt");
				blank.setAttribute("line-height", "0pt");
				cell.appendChild(blank);
			}
		}
		outer.appendChild(body);
		body.appendChild(row);

		// FOP anchors a side float to a line, and drops one which has no line to anchor
		// to (measured: a float holding the table as a direct child of the flow rendered
		// nothing at all), so it goes inside the paragraph the table is anchored to - the
		// one it precedes, which is the paragraph Word measures w:tblpY from.
		Element anchor = null;
		for (Node n = tbl.getNextSibling(); n != null; n = n.getNextSibling()) {
			if (!(n instanceof Element)) continue;
			if (isFo((Element) n, "block")) anchor = (Element) n;
			break;
		}
		if (anchor == null) return;

		parent.removeChild(tbl);
		tbl.setAttribute("start-indent", "0pt");
		tbl.setAttribute("end-indent", "0pt");
		cellFor(row, padLeft > 0 ? 1 : 0).appendChild(tbl);
		anchor.insertBefore(wrapper, anchor.getFirstChild());
	}

	private static void anchorFloatingTable(Document doc, Element tbl) {
		Node parent = tbl.getParentNode();
		if (!(parent instanceof Element) || !isFo((Element) parent, "flow")) return;
		boolean reserve = !opensThePage(tbl);
		if (reserve && !reservesItsBand(tbl)) return;

		double left = lengthPt(tbl.getAttribute(HINT_TBLP_LEFT));
		double width = tableWidthPt(tbl);

		Element abs = doc.createElementNS(FO_NS, "fo:block-container");
		abs.setAttribute("absolute-position", "fixed");
		abs.setAttribute("left", pt(left));
		if (width > 0) abs.setAttribute("width", pt(width));
		abs.setAttribute("overflow", "visible");
		if (tbl.hasAttribute(HINT_TBLP_TOP)) {
			abs.setAttribute("top", pt(lengthPt(tbl.getAttribute(HINT_TBLP_TOP))));
		} else {
			String[] frame = tbl.getAttribute(HINT_TBLP_FRAME).trim().split("\\s+");
			abs.setAttribute("top", pt(lengthPt(frame[0])));
			if (frame.length > 1) abs.setAttribute("height", pt(lengthPt(frame[1])));
			String align = tbl.getAttribute(HINT_TBLP_ALIGN);
			if (align.length() > 0) abs.setAttribute("display-align", align);
		}

		// the wrapper resets the flow's indents for the container; it is left to size
		// itself (its only child is out of the flow, so it takes no space).  A wrapper
		// declared height="0pt" - which is what an anchored picture's takes - produces
		// no area, and FOP then drops a positioned container inside it whenever the
		// wrapper is the first thing in the flow and a page break follows: a cover page
		// built from one floating table came out blank.
		Element wrapper = doc.createElementNS(FO_NS, "fo:block-container");
		wrapper.setAttribute("overflow", "visible");
		wrapper.setAttribute("start-indent", "0pt");
		wrapper.setAttribute("end-indent", "0pt");
		if (tbl.hasAttribute("break-before")) { // the wrapper is in the flow, the table is not
			wrapper.setAttribute("break-before", tbl.getAttribute("break-before"));
			tbl.removeAttribute("break-before");
		}
		wrapper.appendChild(abs);

		parent.insertBefore(wrapper, tbl);
		if (reserve) {
			// the band the table occupies in the flow, so what follows is pushed down
			Element copy = (Element) tbl.cloneNode(true);
			copy.setAttribute("visibility", "hidden");
			for (String hint : TBLP_HINTS) copy.removeAttribute(hint);
			copy.removeAttribute("break-before");
			stripIds(copy);
			parent.insertBefore(copy, tbl);
		}
		parent.removeChild(tbl);
		tbl.setAttribute("start-indent", "0pt");
		tbl.setAttribute("end-indent", "0pt");
		abs.appendChild(tbl);
	}

	/**
	 * Whether a page- or margin-anchored floating table which content <em>precedes</em>
	 * is positioned at its anchor with its band reserved in the flow by an invisible copy
	 * (&#xa7;9.5's {@code w:wrap} trick, which FOP honours: {@code visibility="hidden"}
	 * keeps the area's size and paints nothing), rather than being left in the flow as
	 * 17.0.5 and b2-batch18 left it.
	 *
	 * <p>Reserving the band is right only where the flow has <b>not already passed</b>
	 * the table's anchor - there Word must put what follows below the table, which is
	 * what the reservation does.  Where the anchor is above the flow, Word draws the
	 * table over ground the flow has already covered and the reservation is pure error.
	 * The fixups cannot know where the flow has reached, and the measured cut is the
	 * anchor's own place on the page:</p>
	 * <ul>
	 * <li>a corpus letterhead whose table is anchored at {@code tblpY=15027} - 751.4pt
	 *     down an 841.9pt page, with 16 paragraphs before it and the letter's body after
	 *     it - goes from 0.645 to <b>0.839</b> of Word's lines;</li>
	 * <li>three anchored in the top quarter fall: 203.3pt of a 792pt page 0.933 to 0.853,
	 *     66.7pt of a 1190.7pt page 0.830 to 0.801, and 94.6pt of an 841.9pt page 0.839
	 *     to 0.833.</li>
	 * </ul>
	 * <p>So the band is reserved only for a table anchored in the <b>lower half</b> of
	 * the page.  A <b>narrow</b> table is left in the flow whatever its anchor: Word runs
	 * the text beside it, and reserving the whole band pushes down text Word keeps level
	 * (measured on the table-floating-anchor probe, whose page 6 has a narrow table beside
	 * the text - reserving its band cost the probe 0.84 -> 0.83 of Word's lines).</p>
	 *
	 * <p>{@code docx4j.convert.out.fo.tables.reserveBand=false} leaves every such table in
	 * the flow.</p>
	 *
	 * @since 17.0.6
	 */
	private static boolean reservesItsBand(Element tbl) {
		if (!org.docx4j.Docx4jProperties.getProperty(
				"docx4j.convert.out.fo.tables.reserveBand", true)) {
			return false;
		}
		if ("true".equals(tbl.getAttribute(HINT_TBLP_NARROW))) return false;
		// a w:tblpYSpec gives the frame, not the table's top edge - the table's height is
		// not known before layout - so where its band falls is guesswork (measured: the
		// one corpus table with a tblpYSpec and content before it lost a line)
		if (!tbl.hasAttribute(HINT_TBLP_TOP)) return false;
		double top = lengthPt(tbl.getAttribute(HINT_TBLP_TOP));
		Element rb = regionBody(tbl);
		double pageHeight = rb != null && rb.getParentNode() instanceof Element
				? lengthPt(((Element) rb.getParentNode()).getAttribute("page-height")) : 0;
		return pageHeight > 0 && top > pageHeight / 2;
	}

	/**
	 * Whether this table is the first thing on its page: nothing but empty paragraphs and
	 * tables already taken out of the flow precedes it, back to a forced page break or to
	 * the start of the flow.
	 *
	 * <p>Word positions every page- or margin-anchored table at its anchor whatever
	 * precedes it, and flows the text around it - measured on the table-floating-anchor
	 * probe, where each such table follows a page break and Word puts the following
	 * paragraph at the top of the page (y=83.1) with the table below it (168.3 for
	 * {@code tblpY=3136}), while docx4j left the table in the flow at 108.9 and the
	 * paragraph under it at 192.4.  What cannot be reproduced is the wrapping: nothing
	 * flows beside an absolutely positioned container, so where the flow needs the band
	 * the table is drawn in, the two are drawn on top of each other.</p>
	 *
	 * <p>Hence the two cases in which the table is taken out of the flow: one which
	 * <b>opens its section</b> (the cover page, the letterhead), where the flow has
	 * nothing before it and Word starts the page with the frame in any case; and one
	 * which <b>opens a page</b> and is narrow enough for text to fit beside it, where
	 * Word puts the text that fits above the frame at the top of the page, as the flow
	 * does.  A full-width table which opens a page stays in the flow: Word can only put
	 * the following content below it, which is what the flow does anyway - measured on a
	 * corpus letter whose page-anchored full-width table ({@code tblpY=1891}, 97% of the
	 * column) has Word's next table below it at y=257.6, where positioning ours drew the
	 * two on top of each other and cost 0.08 of line parity.</p>
	 */
	private static boolean opensThePage(Element tbl) {
		boolean narrow = "true".equals(tbl.getAttribute(HINT_TBLP_NARROW));
		if ("page".equals(tbl.getAttribute("break-before"))) return narrow;
		for (Node n = tbl.getPreviousSibling(); n != null; n = n.getPreviousSibling()) {
			if (!(n instanceof Element)) continue;
			Element el = (Element) n;
			if (takesNoSpace(el)) continue;
			if (!isFo(el, "block") || !blankBlock(el)) return false;
			if ("page".equals(el.getAttribute("break-before"))
					|| "page".equals(el.getAttribute("break-after"))) {
				return narrow;
			}
		}
		return true; // it opens the section: the cover page, the letterhead
	}

	/** A container holding nothing but an absolutely positioned one: a floating table or
	 *  a picture already taken out of the flow, which the page's own layout ignores. */
	private static boolean takesNoSpace(Element el) {
		if (!isFo(el, "block-container")) return false;
		for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (!(n instanceof Element)) continue;
			if (!isFo((Element) n, "block-container")
					|| !((Element) n).hasAttribute("absolute-position")) {
				return false;
			}
		}
		return true;
	}

	/** A block with nothing on its lines: an empty paragraph (which already carries the
	 *  preserved space of &#xa7;2.5) counts as nothing before the table. */
	private static boolean blankBlock(Element el) {
		for (String name : new String[] { "external-graphic", "leader", "table", "page-number" }) {
			if (el.getElementsByTagNameNS(FO_NS, name).getLength() > 0) return false;
		}
		String text = el.getTextContent();
		return text == null || text.trim().length() == 0;
	}

	/**
	 * Whether an fo:block inside an fo:inline - how the visitor pathway emits a line break
	 * inside a run - comes after this element in document order.  That is the other half
	 * of the FOP float defect {@link #hoistFloats} works around.
	 */
	private static boolean blockInsideInlineAfter(Document doc, Element el) {
		return blockInsideInlineAfter(doc.getDocumentElement(), el, new boolean[1]);
	}

	private static boolean blockInsideInlineAfter(Element at, Element after, boolean[] seen) {
		if (at == after) seen[0] = true;
		else if (seen[0] && isFo(at, "block")) {
			Node parent = at.getParentNode();
			if (parent instanceof Element && isFo((Element) parent, "inline")) return true;
		}
		for (Node n = at.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && blockInsideInlineAfter((Element) n, after, seen)) return true;
		}
		return false;
	}

	private static Element cellFor(Element row, int index) {
		int i = 0;
		for (Node n = row.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && i++ == index) return (Element) n;
		}
		throw new IllegalStateException("no cell " + index);
	}

	/** The table's width: its own attribute, else the sum of its columns'. */
	private static double tableWidthPt(Element tbl) {
		double w = lengthPt(tbl.getAttribute("width"));
		if (w > 0) return w;
		double sum = 0;
		for (Node n = tbl.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && isFo((Element) n, "table-column")) {
				sum += lengthPt(((Element) n).getAttribute("column-width"));
			}
		}
		return sum;
	}

	// ------------------------------------------------------------ 0d. floats at flow level

	/**
	 * Move every fo:float to be a direct child of its fo:flow, immediately before the
	 * flow-level ancestor it sits in.
	 *
	 * <p>FOP throws NullPointerException from TraitSetter.setVisibility (called with the
	 * null curBlockArea of a BlockLayoutManager which produced no area) whenever a float
	 * nested in an fo:block shares a flow with an fo:block nested in an fo:inline - which
	 * is how a line break inside a run reaches the FO (BrWriter emits a block with
	 * linefeed-treatment="preserve"), so it is a common combination and the whole export
	 * fails.  The float is laid out correctly when it is a direct child of the flow.</p>
	 *
	 * <p>Only the floats which the crash is in prospect for are moved: it takes a block
	 * inside an inline <em>after</em> the float (measured - one before it lays out), and
	 * at flow level a float anchors just above its paragraph rather than at the
	 * paragraph's first line, which measures a little further from Word (the
	 * image-anchored probe fell from 86% to 76% when every float was moved, and a real
	 * document whose only such block precedes its floats regressed with it).  The XSLT
	 * pathway emits a line break as a sibling of the run's inlines rather than inside
	 * one, so it never needs this.</p>
	 *
	 * <p>A float inside a table cell, header/footer or footnote has no flow ancestor;
	 * those are left alone (FOP ignores them there in any case, and
	 * {@link #anchorImage} does not create them).</p>
	 *
	 * @since 17.0.6
	 */
	static void hoistFloats(Document doc) {
		List<Element> floats = elements(doc, "float");
		if (floats.isEmpty()) return;
		java.util.Map<Element, Integer> at = new java.util.HashMap<Element, Integer>();
		int lastBlockInsideInline = number(doc.getDocumentElement(), new int[1], at);
		if (lastBlockInsideInline < 0) return; // FOP copes with floats on their own
		for (Element fl : floats) {
			Integer position = at.get(fl);
			if (position == null || position > lastBlockInsideInline) continue;
			// FOP renders a float holding an fo:table at flow level as nothing at all
			// (measured), so such a float is never moved; a floating table which would
			// need this is left in the flow instead ({@link #floatFloatingTable}).
			if (widest(fl, "table") > 0) continue;
			Node child = fl;
			Node parent = fl.getParentNode();
			while (parent instanceof Element && !isFo((Element) parent, "flow")) {
				// never out of a cell, a header/footer or a footnote: the float would
				// leave the content it belongs to
				if (isFo((Element) parent, "table-cell") || isFo((Element) parent, "static-content")
						|| isFo((Element) parent, "footnote-body")) {
					child = fl;
					break;
				}
				child = parent;
				parent = parent.getParentNode();
			}
			if (!(parent instanceof Element) || child == fl) continue; // already at flow level
			parent.insertBefore(fl, child);
		}
	}

	/**
	 * Numbers the elements in document order, recording where each fo:float is.
	 *
	 * @return the position of the last fo:block inside an fo:inline - the shape a line
	 *         break inside a run produces, and the other half of the crash - or -1.
	 */
	private static int number(Element el, int[] counter, java.util.Map<Element, Integer> floats) {
		int last = -1;
		int position = counter[0]++;
		if (isFo(el, "float")) {
			floats.put(el, position);
		} else if (isFo(el, "block")) {
			Node parent = el.getParentNode();
			if (parent instanceof Element && isFo((Element) parent, "inline")) last = position;
		}
		for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) last = Math.max(last, number((Element) n, counter, floats));
		}
		return last;
	}

	/** The widest fo:external-graphic (content-width) or fo:table (width) at or under
	 *  this element; 0 where it holds none. */
	private static double widest(Element el, String name) {
		double w = 0;
		if (isFo(el, name)) {
			w = "table".equals(name) ? tableWidthPt(el) : lengthPt(el.getAttribute("content-width"));
		}
		for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) w = Math.max(w, widest((Element) n, name));
		}
		return w;
	}

	// ------------------------------------------------------------ 0e. pictures FOP cannot paint

	/** A 1x1 fully transparent PNG as a data: URI; FOP resolves those. */
	private static final String TRANSPARENT_PNG = "data:image/png;base64,"
			+ "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

	/** The executable BinaryPartAbstractImage.convertToPNG uses; unset by default. */
	private static final String CONVERTER_PROPERTY =
			"docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.ImageMagickExecutable";

	/** Pixels per inch the converter rasterises a metafile at. */
	private static final String CONVERTER_DENSITY_PROPERTY = "docx4j.convert.out.fo.pictures.convertDensity";

	/**
	 * Word draws every picture; FOP paints only the formats it has a loader for.  A
	 * picture it cannot paint is not just missing: FOP drops the viewport with it, so
	 * the space Word gives the picture collapses and everything below moves up.
	 *
	 * <p>The formats docx4j's dependencies cover are PNG, JPEG (baseline, progressive and
	 * CMYK alike), GIF, BMP, TIFF, EPS, SVG and WMF (that last through Batik).  EMF has a
	 * preloader but no loader, so FOP scales the viewport to the metafile's own aspect
	 * ratio and paints nothing; bytes that are no image at all (Word stores the server's
	 * 404 page when a linked picture cannot be fetched) have neither, and collapse.</p>
	 *
	 * <p>Each such picture is pointed at a transparent 1x1 PNG and scaled non-uniformly,
	 * which reserves exactly the extent the document declares - what Word's layout needs -
	 * and one line is logged for the document rather than an error per picture.  If
	 * {@value #CONVERTER_PROPERTY} names an ImageMagick/GraphicsMagick executable, a
	 * metafile is converted to PNG and painted instead.</p>
	 *
	 * @since 17.0.6
	 */
	/**
	 * A paragraph which begins with a <b>block-level child</b> - the wrapper an anchored
	 * picture, a text box or a floating table is put in - loses its first-line indent:
	 * FOP puts the inline content which follows into an anonymous block, which starts at
	 * the block's start-indent whatever {@code text-indent} says.
	 *
	 * <p>Measured on a corpus document whose "Dear Sir," paragraph is
	 * {@code <w:ind w:left="60" w:firstLine="360"/>} (3pt + 18pt) and begins with a
	 * {@code w:pict}: our FO carries both properties, Word draws the text at x=21.1 and we
	 * drew it at x=3.0.  The indent is therefore reserved by an {@code fo:leader} of
	 * exactly its width - which is what a leading tab and leading whitespace already are
	 * (&#xa7;4.4, &#xa7;4.5) - put at the head of the inline content, and the property is
	 * taken off the block so the following lines are not indented too.</p>
	 *
	 * <p>A hanging indent (a negative text-indent) is left alone: there is nothing to
	 * reserve, and FOP places the first line at the start-indent, which is where Word
	 * puts it.</p>
	 *
	 * @since 17.0.6
	 */
	static void firstLineIndentAfterLeadingBlock(Document doc) {
		for (Element block : elements(doc, "block")) {
			if (!block.hasAttribute(HINT_PSTYLE)) continue;
			double indent = lengthPt(block.getAttribute("text-indent"));
			if (indent <= 0) continue;
			Node at = block.getFirstChild();
			boolean leading = false;
			while (at instanceof Element && isBlockLevel((Element) at)) {
				leading = true;
				at = at.getNextSibling();
			}
			if (!leading || at == null) continue;
			block.removeAttribute("text-indent");
			Element leader = doc.createElementNS(FO_NS, "fo:leader");
			leader.setAttribute("leader-pattern", "space");
			leader.setAttribute("leader-length", pt(indent));
			block.insertBefore(leader, at);
		}
	}

	/** Whether this element is one FOP lays out as a block, so that the inline content
	 *  after it becomes an anonymous block of its own.  @since 17.0.6 */
	private static boolean isBlockLevel(Element el) {
		return isFo(el, "block") || isFo(el, "block-container") || isFo(el, "float")
				|| isFo(el, "table") || isFo(el, "list-block");
	}

	static void reserveUnpaintablePictures(Document doc) {
		java.util.Map<String, String> converted = new java.util.HashMap<String, String>();
		java.util.Map<String, Integer> reserved = new java.util.TreeMap<String, Integer>();
		for (Element g : elements(doc, "external-graphic")) {
			String src = g.getAttribute("src");
			if (src == null || !src.startsWith("file:")) continue; // data:/cid: are handler output
			File f = fileOf(src);
			if (f == null || !f.isFile()) continue;
			String format = sniff(f);
			if (format == null) continue; // FOP can paint it

			String png = converted.containsKey(src) ? converted.get(src) : convertToPng(f);
			converted.put(src, png);
			if (png != null) {
				g.setAttribute("src", png);
				continue;
			}
			g.setAttribute("src", TRANSPARENT_PNG);
			if (g.hasAttribute("content-width") && g.hasAttribute("content-height")) {
				g.setAttribute("scaling", "non-uniform"); // a 1x1 image would otherwise go square
			}
			Integer n = reserved.get(format);
			reserved.put(format, n == null ? 1 : n + 1);
		}
		if (!reserved.isEmpty()) {
			int total = 0;
			for (Integer n : reserved.values()) total += n;
			log.warn("FOP cannot paint " + total + " picture(s) in this document " + reserved
					+ "; their space is reserved but they are not drawn.  Set " + CONVERTER_PROPERTY
					+ " to convert them with ImageMagick/GraphicsMagick and draw them.");
		}
	}

	private static File fileOf(String src) {
		try {
			return new File(new java.net.URI(src));
		} catch (Exception e) {
			log.debug("Not a file URI: " + src);
			return null;
		}
	}

	/**
	 * @return null if FOP can paint the file, otherwise the format's name (for the log).
	 */
	private static String sniff(File f) {
		byte[] b = new byte[64];
		int n;
		try (java.io.InputStream is = new java.io.FileInputStream(f)) {
			n = is.readNBytes(b, 0, b.length);
		} catch (java.io.IOException e) {
			return "unreadable";
		}
		if (n >= 8 && b[0] == (byte) 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return null;
		if (n >= 3 && b[0] == (byte) 0xFF && b[1] == (byte) 0xD8 && b[2] == (byte) 0xFF) return null;
		if (n >= 4 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8') return null;
		if (n >= 2 && b[0] == 'B' && b[1] == 'M') return null;
		if (n >= 4 && ((b[0] == 'I' && b[1] == 'I' && b[2] == 42 && b[3] == 0)
				|| (b[0] == 'M' && b[1] == 'M' && b[2] == 0 && b[3] == 42))) return null; // TIFF
		if (n >= 4 && b[0] == '%' && b[1] == '!' && b[2] == 'P' && b[3] == 'S') return null; // EPS
		if (n >= 4 && b[0] == (byte) 0xC5 && b[1] == (byte) 0xD0 && b[2] == (byte) 0xD3
				&& b[3] == (byte) 0xC6) return null; // DOS EPS
		// WMF: Batik's loader, which FOP uses when it is on the classpath
		if (n >= 4 && b[0] == (byte) 0xD7 && b[1] == (byte) 0xCD && b[2] == (byte) 0xC6
				&& b[3] == (byte) 0x9A) return null;
		if (n >= 4 && b[0] == 1 && b[1] == 0 && b[2] == 9 && b[3] == 0) return null;
		int from = (n >= 3 && b[0] == (byte) 0xEF && b[1] == (byte) 0xBB && b[2] == (byte) 0xBF) ? 3 : 0;
		String head = new String(b, from, Math.max(0, n - from),
				java.nio.charset.StandardCharsets.ISO_8859_1).trim();
		if (head.startsWith("<?xml") || head.startsWith("<svg")) return null;
		if (n >= 44 && b[40] == ' ' && b[41] == 'E' && b[42] == 'M' && b[43] == 'F') return "EMF";
		if (head.regionMatches(true, 0, "<!doctype html", 0, 14) || head.regionMatches(true, 0, "<html", 0, 5)) {
			return "not an image (HTML)";
		}
		if (n == 0) return "empty";
		return "unrecognised";
	}

	/**
	 * @return the URI of a PNG the picture was converted to, or null if no converter is
	 *         configured or it could not do it.
	 */
	private static String convertToPng(File f) {
		String exe = org.docx4j.Docx4jProperties.getProperty(CONVERTER_PROPERTY);
		if (exe == null || exe.trim().length() == 0) return null;
		String density = org.docx4j.Docx4jProperties.getProperty(CONVERTER_DENSITY_PROPERTY, "300");
		File png = new File(f.getPath() + ".docx4j.png");
		try {
			if (!png.isFile() || png.length() == 0) {
				Process p = new ProcessBuilder(exe.trim(), "-density", density, "-units", "PixelsPerInch",
						f.getPath(), png.getPath()).redirectErrorStream(true).start();
				p.getInputStream().readAllBytes(); // don't let the pipe fill
				if (!p.waitFor(60, java.util.concurrent.TimeUnit.SECONDS)) {
					p.destroyForcibly();
					return null;
				}
			}
			if (png.isFile() && png.length() > 0 && sniff(png) == null) {
				return png.toURI().toURL().toString();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			log.warn("Could not convert " + f.getName() + " with " + exe + ": " + e.getMessage());
		}
		return null;
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
		double oneColumn = columnWidthPt(para); // the column, not the section (@since 17.0.6)
		if (oneColumn > 0) col = oneColumn;

		// a box narrow enough for Word to flow text beside it is placed where Word
		// puts it and takes no space: reserving its height would push the text below
		// it, and where several such boxes sit side by side (a planner laid out in
		// text boxes) that costs a page each.  A box that fills the column has no
		// text beside it in Word either, so it reserves its height - except in a
		// multi-column region, where a reservation is charged to the column the anchor
		// is in and Word wraps the text beside the box within its own column: measured,
		// reserving a 186.75pt box's height in a 213pt column cost a page and took line
		// parity from 0.478 to 0.087 (@since 17.0.6)
		if ("square".equals(kind) && oneColumn > 0) kind = "none";
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
			/* A negative offset puts the box out into the margin, which is where Word
			 * draws it; clamping it to zero moved the box and everything laid out with
			 * it back to the column edge.  Measured: a landscape planner whose text box
			 * is anchored at -41.0pt has Word's box content rect at 31.0..1141.7 - its
			 * border rect starting 48.2pt left of the column - where ours started at
			 * 72.0, a constant +41.0pt on every line of the page.  @since 17.0.6 */
			wrapper.setAttribute("start-indent", pt(x));
		}
		resetTextBox(box);
		box.getParentNode().removeChild(box);
		wrapper.appendChild(box);
		insertAnchorWrapper(para, wrapper);
	}

	/**
	 * A text box is laid out from its own edges, and nothing in it is paginated.
	 *
	 * <p><b>Alignment and indents.</b> The box's blocks would otherwise inherit the
	 * anchoring paragraph's text-align and indents, which are about the paragraph, not
	 * about the box.  Measured on a 222-page letter whose letterhead is a VML box
	 * anchored in a right-aligned cell paragraph: Word starts all seven of the box's
	 * lines at x=346.0, where each of ours was right-aligned inside the box, from 312.4
	 * to 438.9.  A paragraph of the box which states its own w:jc keeps it - that goes on
	 * its own block.</p>
	 *
	 * <p><b>Pagination.</b> Word paginates nothing inside a text box: it is a frame, not
	 * part of the flow.  FOP, given break-before="page" inside an absolutely positioned
	 * container, paints only the last container of a run of them - measured on three
	 * boxes in zero-height wrappers, only the third was drawn, and without the breaks all
	 * three were.  A 335-page mail merge of 2345 boxes, every paragraph of which carries
	 * w:pageBreakBefore, came out with one line a page against Word's nine (3167
	 * reference lines against our 335).</p>
	 *
	 * @since 17.0.6
	 */
	private static void resetTextBox(Element box) {
		box.setAttribute("text-align", "start");
		box.setAttribute("text-align-last", "relative");
		box.setAttribute("text-indent", "0pt");
		// start-indent and end-indent are the box's own inset (FOTextBoxes.createContainer)
		// and must not be reset here: they are what puts the text inside the shape.
		dropPagination(box);
	}

	/** Pagination properties FOP must not see inside a positioned container. */
	private static final String[] PAGINATION = { "break-before", "break-after",
			"keep-together", "keep-together.within-page", "keep-with-next",
			"keep-with-next.within-page", "keep-with-previous", "keep-with-previous.within-page" };

	private static void dropPagination(Element el) {
		for (String name : PAGINATION) el.removeAttribute(name);
		for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element) dropPagination((Element) n);
		}
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

	/** FOP lays out side floats only in the main flow's blocks, and not in a
	 *  multi-column region, where it drops them silently ({@link #columnCount}). */
	private static boolean floatsAllowed(Element para) {
		Node n = para.getParentNode();
		while (n instanceof Element) {
			Element e = (Element) n;
			if (isFo(e, "flow")) return columnCount(para) <= 1;
			if (isFo(e, "table-cell") || isFo(e, "static-content") || isFo(e, "footnote-body")
					|| isFo(e, "float") || isFo(e, "block-container") || isFo(e, "inline-container")) return false;
			n = n.getParentNode();
		}
		return false;
	}

	/**
	 * The number of columns the region body this element is laid out in has, from the
	 * page master its page-sequence names; 1 where it cannot be worked out.
	 *
	 * <p>FOP drops an {@code fo:float} in a multi-column region silently - it paints
	 * neither the float's content nor an indent for it (&#xa7;10) - so a wrapped picture
	 * or table there takes the no-float treatment ([&#xa7;9.1]) instead.  Measured on a
	 * landscape two-column document whose 87.75 x 48pt logo is a {@code wrapTight}
	 * anchored picture: {@code mutool draw -F trace} counts two images on Word's page 1
	 * and one on ours - the float was never painted at all.</p>
	 *
	 * @since 17.0.6
	 */
	static int columnCount(Element el) {
		Element rb = regionBody(el);
		if (rb == null) return 1;
		String n = rb.getAttribute("column-count");
		try {
			return n.length() == 0 ? 1 : Math.max(1, Integer.parseInt(n.trim()));
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	/**
	 * The width of one column of the region body this element is laid out in, in points:
	 * the body width less the gaps, divided by the column count.  0 where it cannot be
	 * worked out (so the caller keeps the section's text column, which is what the
	 * anchor hints carry).
	 *
	 * <p>Word measures a {@code relativeFrom="column"} offset, and decides what fits
	 * beside an object, in the column the object is anchored in - not in the section's
	 * whole text column.  It is what tells an object anchored in a <em>later</em> column
	 * from one in this one ([&#xa7;9.1]); the 60% and 90% share tests do not arise in a
	 * multi-column region, where a wrapped object is always positioned.</p>
	 *
	 * @since 17.0.6
	 */
	static double columnWidthPt(Element el) {
		Element rb = regionBody(el);
		if (rb == null) return 0;
		int columns = columnCount(el);
		if (columns <= 1) return 0;
		Node spmNode = rb.getParentNode();
		if (!(spmNode instanceof Element)) return 0;
		Element spm = (Element) spmNode;
		double width = lengthPt(spm.getAttribute("page-width"))
				- lengthPt(spm.getAttribute("margin-left")) - lengthPt(spm.getAttribute("margin-right"))
				- lengthPt(rb.getAttribute("margin-left")) - lengthPt(rb.getAttribute("margin-right"));
		if (width <= 0) return 0;
		double gap = lengthPt(rb.getAttribute("column-gap"));
		double col = (width - (columns - 1) * gap) / columns;
		return col > 0 ? col : 0;
	}

	/**
	 * A table-of-contents entry's stretching leader ends on the entry's own right dot
	 * stop, not on the paragraph's right indent.
	 *
	 * <p>{@code text-align-last="justify"} stretches the leader to the block's
	 * end-indent.  Word stretches it to the stop the tab reaches, and where that stop
	 * lies outside the text column Word lets the entry overhang the margin.  Measured on
	 * an A4 document with 72pt margins - a right edge at x=523.35 - whose TOC1 style
	 * declares a right dot stop at 9350 twips (x=539.5): all 308 of Word's entry lines
	 * end at 539.6 where ours ended at 523.3, and being 16.2pt short of Word's measure
	 * six entries took two lines where Word takes one.  9 documents of three corpora
	 * declare a TOC stop more than 2pt from their text column; in the rest the stop is
	 * the right edge and nothing changes.</p>
	 *
	 * <p>Only in the flow: inside a table cell the block's reference area is the cell,
	 * which the stop - measured from the page's left margin - says nothing about.</p>
	 *
	 * @since 17.0.6
	 */
	static void tocLeaderEndIndent(Document doc) {
		for (Element block : elements(doc, "block")) {
			String hint = block.getAttribute(HINT_TOC_STOP);
			if (hint == null || hint.length() == 0) continue;
			if (insideTableCell(block)) continue;
			double stopPt;
			try {
				stopPt = Integer.parseInt(hint) / 20d;
			} catch (NumberFormatException e) {
				continue;
			}
			double measure = textColumnWidthPt(block);
			if (measure <= 0) continue;
			double want = measure - stopPt;
			double have = lengthPt(block.getAttribute("end-indent"));
			if (want > have - 2) continue;
			/* Only ever *out*, never in.  A paragraph may declare several stops and this is
			 * the first of them; where it is well inside the text column the entry's dots
			 * still run to the right indent, because a later stop - or the right indent
			 * itself - is what the entry's tab reaches.  Measured: pulling the end-indent
			 * in fired on 72 blocks of one corpus document (end-indent="245.25pt" on a
			 * 490pt measure) and cost it 0.025 of line parity, where letting the line
			 * overhang costs nothing where the stop is inside. */
			// the entry may overhang the margin, but not run off the page
			double margin = pageMarginRightPt(block);
			block.setAttribute("end-indent", pt(Math.max(want, -margin)));
		}
	}

	/** The width of the section's text column, before any column division. */
	private static double textColumnWidthPt(Element el) {
		Element rb = regionBody(el);
		if (rb == null) return 0;
		Node spmNode = rb.getParentNode();
		if (!(spmNode instanceof Element)) return 0;
		Element spm = (Element) spmNode;
		return lengthPt(spm.getAttribute("page-width"))
				- lengthPt(spm.getAttribute("margin-left")) - lengthPt(spm.getAttribute("margin-right"))
				- lengthPt(rb.getAttribute("margin-left")) - lengthPt(rb.getAttribute("margin-right"));
	}

	/** The page master's own right margin, which is how far a line may overhang. */
	private static double pageMarginRightPt(Element el) {
		Element rb = regionBody(el);
		if (rb == null || !(rb.getParentNode() instanceof Element)) return 0;
		return lengthPt(((Element) rb.getParentNode()).getAttribute("margin-right"));
	}

	/** The fo:region-body of the page master this element's page-sequence names.
	 *
	 *  <p>Walked from the fo:layout-master-set's own children rather than looked up with
	 *  getElementsByTagNameNS: this is called once per anchored object, and a
	 *  document-wide scan is re-walked from scratch each time, since the pass that calls
	 *  it is moving elements about (Xerces invalidates its NodeList cache on every
	 *  change).  Measured on a 335-page mail merge of 2345 text boxes, the scan turned
	 *  the export from under 300s into over 600s.</p> */
	private static Element regionBody(Element el) {
		Element sequence = null;
		for (Node n = el; n instanceof Element; n = n.getParentNode()) {
			if (isFo((Element) n, "page-sequence")) { sequence = (Element) n; break; }
		}
		if (sequence == null || !(sequence.getParentNode() instanceof Element)) return null;
		Element masters = firstChildElement((Element) sequence.getParentNode(), "layout-master-set");
		if (masters == null) return null;
		String name = sequence.getAttribute("master-reference");
		for (int hop = 0; hop < 3 && name.length() > 0; hop++) {
			Element master = masterNamed(masters, "simple-page-master", name);
			if (master != null) return firstChildElement(master, "region-body");
			// a page-sequence-master: follow its first alternative
			Element sequenceMaster = masterNamed(masters, "page-sequence-master", name);
			if (sequenceMaster == null) return null;
			String next = null;
			for (String kind : new String[] { "conditional-page-master-reference",
					"single-page-master-reference", "repeatable-page-master-reference" }) {
				for (Element ref : descendants(sequenceMaster, kind)) {
					if (ref.getAttribute("master-reference").length() > 0) {
						next = ref.getAttribute("master-reference");
						break;
					}
				}
				if (next != null) break;
			}
			if (next == null) return null;
			name = next;
		}
		return null;
	}

	/** A child of fo:layout-master-set of this kind, with this master-name. */
	private static Element masterNamed(Element masters, String localName, String name) {
		for (Node n = masters.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (!(n instanceof Element)) continue;
			Element el = (Element) n;
			if (isFo(el, localName) && name.equals(el.getAttribute("master-name"))) return el;
		}
		return null;
	}

	static String pt(double v) {
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
	 *
	 * <p><b>Word's exact height is the whole row, borders included</b>, where FOP reads
	 * {@code height} on an {@code fo:table-row} as the cell's <em>content</em> height and
	 * advances to the next row by that plus the border it charges the cell - half of each
	 * collapsed border, all of a separate one.  So the height is reduced by that border
	 * allowance, and the block-container clipped to what is then left after the cell's
	 * padding.  Measured on the page-blank probe (32 rows of {@code w:trHeight w:val="400"
	 * w:hRule="exact"}, 0.5pt collapsed borders): Word's row pitch is 20.0pt (baselines
	 * 617.5 / 637.6 / 657.5 / 677.5) and docx4j's was 20.5 (630.3 / 650.8 / 671.3), 16pt
	 * over the page; FOP's area tree put the rows 20500mp apart for a 20000mp content
	 * height, and 20000mp apart once the height was 19500mp.  The same 0.5pt is the
	 * residual of the table-rowheight probe's two exact rows.  {@code w:hRule="atLeast"}
	 * rows are not touched: Word's own atLeast pitch tracks ours to 0.1pt.</p>
	 */
	static void clipExactRows(Document doc) {
		for (Element row : elements(doc, "table-row")) {
			String h = row.getAttribute(HINT_ROW_EXACT);
			row.removeAttribute(HINT_ROW_EXACT);
			if (h == null || h.length() == 0) continue;
			double heightPt = lengthPt(h);
			if (heightPt <= 0) continue;
			Element tbl = ancestorTable(row);
			// FOP charges a collapsed border half to each of the two cells it separates,
			// a separate border wholly to its own cell
			double share = tbl != null && "separate".equals(tbl.getAttribute("border-collapse")) ? 1 : 0.5;
			NodeList cells = row.getChildNodes();
			double content = heightPt;
			for (int i = 0; i < cells.getLength(); i++) {
				if (!(cells.item(i) instanceof Element) || !isFo((Element) cells.item(i), "table-cell")) continue;
				Element cell = (Element) cells.item(i);
				double borders = share * (lengthPt(cell.getAttribute("border-top-width"))
						+ lengthPt(cell.getAttribute("border-bottom-width")));
				double inner = heightPt - borders
						- lengthPt(cell.getAttribute("padding-top")) - lengthPt(cell.getAttribute("padding-bottom"));
				content = Math.min(content, heightPt - borders);
				Element container = doc.createElementNS(FO_NS, "fo:block-container");
				container.setAttribute("block-progression-dimension", org.docx4j.fonts.WordLineMetrics.format(Math.max(0.1, inner)));
				container.setAttribute("overflow", "hidden");
				while (cell.getFirstChild() != null) container.appendChild(cell.getFirstChild());
				cell.appendChild(container);
			}
			row.setAttribute("height", org.docx4j.fonts.WordLineMetrics.format(Math.max(0.1, content)));
		}
	}

	/**
	 * An {@code fo:table-cell} must hold at least one block: its content model is
	 * {@code marker* (%block;)+}, and FOP fails the whole export with
	 * "fo:table-cell is missing child elements" where it holds none.
	 *
	 * <p>A cell whose every paragraph is hidden text produces none: &#xa7;9.3's rule that
	 * a paragraph all of whose runs and whose mark are hidden leaves no line at all is
	 * right in the flow, but empties the cell.  Word prints the row with an empty cell -
	 * its height comes from the other cells - so an empty {@code fo:block}, which
	 * generates no line and so no height, is exactly what is wanted.  Measured: one
	 * document of a 103-document corpus has eleven such cells, and lost its whole
	 * export to them.</p>
	 *
	 * <p>{@code fo:flow} and {@code fo:static-content} have the same content model, and a
	 * document whose {@code w:docDefaults/w:rPrDefault/w:rPr} carries {@code w:vanish} -
	 * every run in it hidden - produces an empty flow.  FOP then fails the export, and
	 * the header/footer extent pre-pass, which runs the same pipeline over a trimmed
	 * copy, fails with it: measured, one corpus document was rendered with the
	 * half-page default extents that failure leaves behind, 35 pages for Word's 3.</p>
	 *
	 * @since 17.0.6
	 */
	static void blockForEmptyCell(Document doc) {
		blockForEmptyContainers(doc, "table-cell");
		blockForEmptyContainers(doc, "flow");
		blockForEmptyContainers(doc, "static-content");
	}

	private static void blockForEmptyContainers(Document doc, String localName) {
		for (Element cell : elements(doc, localName)) {
			boolean hasBlock = false;
			for (Node n = cell.getFirstChild(); n != null && !hasBlock; n = n.getNextSibling()) {
				if (!(n instanceof Element)) continue;
				Element child = (Element) n;
				hasBlock = isFo(child, "block") || isFo(child, "block-container")
						|| isFo(child, "table") || isFo(child, "list-block")
						|| isFo(child, "table-and-caption");
			}
			if (hasBlock) continue;
			cell.appendChild(doc.createElementNS(FO_NS, "fo:block"));
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
			block.removeAttribute(HINT_COLUMN_BREAK);
			block.removeAttribute(HINT_BREAK_RUN);
			block.removeAttribute(HINT_TOC_STOP);
			block.removeAttribute(HINT_FRAME);
			for (String hint : TAB_HINTS) block.removeAttribute(hint);
			block.removeAttribute(org.docx4j.fonts.RunFontSelector.HINT_FONT);
		}
		for (Element leader : elements(doc, "leader")) {
			leader.removeAttribute(HINT_TAB);
			leader.removeAttribute(HINT_LABEL_GAP);
		}
		for (Element g : elements(doc, "external-graphic")) {
			for (String hint : ANCHOR_HINTS) g.removeAttribute(hint);
		}
		for (Element tbl : elements(doc, "table")) {
			for (String hint : TBLP_HINTS) tbl.removeAttribute(hint);
			tbl.removeAttribute(HINT_CONTENT_SIZED);
			tbl.removeAttribute(HINT_GRID_SHIFT);
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
		for (Element flow : elements(doc, "flow")) contextualSpacingAmong(flow, false);
		for (Element span : spanAllBlocks(doc)) contextualSpacingAmong(span, false);
		for (Element cell : elements(doc, "table-cell")) contextualSpacingAmong(cell, true);
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

	/**
	 * @param cellEdges the container is a table cell, whose top and bottom edges are
	 *        themselves boundaries a contextual paragraph's space is dropped at.  The
	 *        pairwise loop below stops one short of the end, so a cell holding a
	 *        <em>single</em> paragraph was never examined at all - and
	 *        {@link #retainSpacingAtCellEdges} then pinned that paragraph's docDefaults
	 *        space-after to the cell bottom.  Measured on a compat-15 planner whose
	 *        cells hold one contextual paragraph each: Word's row pitch is 10.1pt (the
	 *        9.199pt line box plus w:trHeight 199) and docx4j's was 19.9pt, on every row
	 *        of 37 Word pages, which came out as 43.
	 */
	private static void contextualSpacingAmong(Element container, boolean cellEdges) {
		List<Element> paras = paragraphBlocks(container);
		/* Only a cell holding a *single* paragraph, which is the shape this was measured
		 * on.  Where the cell holds several, Word applies the last one's space-after at
		 * the cell bottom - there is no next paragraph for the "same style" test to be
		 * about: measured on a document whose cells end in a bulleted List Paragraph
		 * with w:contextualSpacing and w:after="200", Word's row pitch is 25.0pt and
		 * suppressing it gave us 19.9.  @since 17.0.6 */
		if (cellEdges && paras.size() == 1) {
			Element only = paras.get(0);
			if ("1".equals(only.getAttribute(HINT_CONTEXTUAL))) {
				only.setAttribute("space-before", "0pt");
				only.setAttribute("space-after", "0pt");
			}
		}
		for (int i = 0; i + 1 < paras.size(); i++) {
			Element a = paras.get(i), b = paras.get(i + 1);
			String sa = a.getAttribute(HINT_PSTYLE), sb = b.getAttribute(HINT_PSTYLE);
			/* Two paragraphs which state no w:pStyle are both of the default style, so
			 * they are "of the same style" and w:contextualSpacing pairs them.  The hint
			 * is "" for such a paragraph (XsltFOFunctions), and treating "" as "unknown"
			 * meant it never did: measured on a planner whose shaded Normal cells carry
			 * w:contextualSpacing with 10pt of docDefaults space-after, Word's grid of
			 * baselines is 93.9 / 103.0 / 112.3 / 121.5 where ours split into 93.6 /
			 * 102.8 / 112.0 against 92.8 / 102.0 / 111.2 - +9.9pt - and 37 Word pages
			 * came out as 39.  @since 17.0.6 */
			if (sa == null || sb == null || !sa.equals(sb)) continue;
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

	/**
	 * A borders/shading container (the preprocess {@code Containerization} puts
	 * adjacent paragraphs sharing a border or shading into one) is an fo:block built
	 * from the <em>first</em> paragraph's properties, its spacing included, wrapping
	 * the paragraphs themselves.  Space-before and space-after are combined by "larger
	 * of", so carrying them twice normally costs nothing - but where a rule above
	 * removes a paragraph's spacing (contextual spacing, HTML auto spacing between
	 * list items) the wrapper's copy survives and puts the gap back.  Measured: a
	 * planner whose shaded cells carry w:contextualSpacing with 10pt of docDefaults
	 * space-after had every row 9.5pt too tall - Word's row pitch 36.5 -> 46.6 -> 57.1,
	 * ours 35.7 -> 55.6 -> 75.3 - and 37 Word pages came out as 43.
	 *
	 * <p>The wrapper's spacing is therefore made to follow the paragraphs it holds:
	 * space-before from the first, space-after from the last.</p>
	 *
	 * @since 17.0.6
	 */
	static void syncContainerSpacing(Document doc) {
		for (Element wrapper : elements(doc, "block")) {
			if (wrapper.hasAttribute(HINT_PSTYLE)) continue;
			List<Element> paras = new ArrayList<>();
			boolean onlyParagraphs = true;
			NodeList children = wrapper.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node n = children.item(i);
				if (!(n instanceof Element)) continue;
				Element el = (Element) n;
				if (isFo(el, "block") && el.hasAttribute(HINT_PSTYLE)) paras.add(el);
				else { onlyParagraphs = false; break; }
			}
			if (!onlyParagraphs || paras.isEmpty()) continue;
			copySpace(paras.get(0), wrapper, "space-before");
			copySpace(paras.get(paras.size() - 1), wrapper, "space-after");
		}
	}

	private static void copySpace(Element from, Element to, String name) {
		if (!to.hasAttribute(name)) return; // the wrapper never had any
		if (from.hasAttribute(name)) to.setAttribute(name, from.getAttribute(name));
		else to.removeAttribute(name);
	}

	/**
	 * A run of consecutive paragraphs whose borders are identical is <b>one</b> box in
	 * Word: one top border and its {@code w:space} above the first paragraph, one bottom
	 * border and its space below the last, and nothing between them - a shading change
	 * inside the run does not open a second box.
	 *
	 * <p>The {@code Containerization} preprocess groups by border and then, <em>inside</em>
	 * that group, by shading, so the nesting is already right; what was wrong is that both
	 * containers are built from the same paragraph's properties, so the inner (shading) one
	 * repeated the outer's top and bottom borders and their padding.  With a 0.5pt border
	 * at {@code w:space="1"} that is 2 x (0.51 + 1) = <b>3.02pt</b> per shading change.
	 * Measured on a planner whose cells hold three identically bordered paragraphs in three
	 * different fills: Word's row pitch is 61.0 -&gt; 70.1 -&gt; 79.2 (9.1pt, the bare Arial
	 * 8pt line box, so Word adds nothing at the change) where docx4j went 61.3 -&gt; 70.5
	 * -&gt; 82.7, and the drift reached +68pt by the foot of page 1; 16 Word pages came out
	 * as 21.  33 documents of the corpus have a bordered wrapper directly wrapping another
	 * block.  A paragraph carrying both a border and shading of its own is the same shape
	 * with one paragraph in it, and was 3.02pt too tall for the same reason.</p>
	 *
	 * <p>The inner container therefore drops the border and padding the outer one already
	 * draws.  Its left and right borders stay: they are drawn outside the text either way,
	 * so they cost no width (&#xa7;3), and the outer box's own left/right border is in the
	 * same place.</p>
	 *
	 * @since 17.0.6
	 */
	static void mergeBorderContainers(Document doc) {
		for (Element inner : elements(doc, "block")) {
			if (inner.hasAttribute(HINT_PSTYLE)) continue; // a paragraph, not a container
			Node parent = inner.getParentNode();
			if (!(parent instanceof Element) || !isFo((Element) parent, "block")) continue;
			Element outer = (Element) parent;
			if (outer.hasAttribute(HINT_PSTYLE)) continue; // not a container either
			if (!sameEdge(outer, inner, "top") || !sameEdge(outer, inner, "bottom")) continue;
			for (String side : new String[] { "top", "bottom" }) {
				if (!inner.hasAttribute("border-" + side + "-width")) continue;
				for (String property : new String[] { "border-" + side + "-width",
						"border-" + side + "-style", "border-" + side + "-color",
						"padding-" + side }) {
					inner.removeAttribute(property);
				}
			}
		}
	}

	/** True where the two blocks state the same border on this side (both may state none). */
	private static boolean sameEdge(Element a, Element b, String side) {
		for (String property : new String[] { "border-" + side + "-width",
				"border-" + side + "-style", "border-" + side + "-color" }) {
			if (!a.getAttribute(property).equals(b.getAttribute(property))) return false;
		}
		return true;
	}

	// ------------------------------------------------------------ 0e. a line for every paragraph

	/**
	 * Word gives every paragraph a line, at the paragraph mark's font and size
	 * (&#xa7;2.5), whatever its runs came to.  A paragraph with no runs at all already
	 * gets one (XsltFOFunctions.createBlock writes a preserved space), but a paragraph
	 * whose runs produced no <em>inline</em> content did not: an fo:block whose only
	 * child is an empty fo:inline builds no line area in FOP, and one whose picture has
	 * been lifted into a positioned container is left with the container and an empty
	 * inline.  Measured: a document whose first body paragraph holds only a wrapNone
	 * anchored picture had every line 15.44pt - exactly that block's line-height - above
	 * Word's, and a table whose three spacer rows each hold one paragraph with an empty
	 * w:t lost 33.7pt of row height at the third line of the document.
	 *
	 * <p>Such a block gets the same preserved space the run-less case gets; it already
	 * carries the paragraph mark's font, line-height and line-box hints.  Positioned
	 * containers (an anchored picture or a text box) do not count as content, since
	 * Word gives the paragraph its line as well as placing the object.</p>
	 *
	 * @since 17.0.6
	 */
	static void emptyLineForBlockWithNoContent(Document doc) {
		for (Element block : elements(doc, "block")) {
			if (!block.hasAttribute(HINT_PSTYLE)) continue;
			if (!block.hasChildNodes()) continue; // no children at all: createBlock handled it
			if (producesContent(block)) continue;
			block.setAttribute("white-space-treatment", "preserve");
			block.appendChild(doc.createTextNode(" "));
		}
	}

	/**
	 * Word gives a {@code w:br} its line even where nothing follows it on that line.
	 *
	 * <p>The twin of {@link #emptyLineForBlockWithNoContent}, one level down: that rule
	 * is about a whole paragraph whose runs painted nothing, this one about a
	 * <em>line</em> inside a paragraph.  {@code BrWriter} writes a line break as a nested
	 * {@code fo:block line-height="0pt" linefeed-treatment="preserve"}, so where what
	 * follows the break is empty - an empty run, a field with no result, or simply
	 * another break - nothing sizes the new line, the 0pt line-height stands, and the
	 * line vanishes.  Measured on a paragraph reading
	 * {@code 555 test <br/> asfdsdfsdf <fld/> <br/> <fld/> <br/> <fld/> <br/> <fld/>}
	 * whose fields have no result: Word runs from y=200.7 to 293.9, six line boxes,
	 * where ours ran 200.5 to 247.3 - three lines, 46.5pt, lost.  748 such breaks in 50
	 * documents of three corpora.</p>
	 *
	 * <p>Such a break's own block gives up its 0pt line-height, so it measures the
	 * paragraph's line box like any other line.  Adding content to the new line instead -
	 * the preserved space the empty-<em>paragraph</em> case gets - does not work in both
	 * pathways: where the break's block is a direct child of the paragraph's, which is
	 * the XSLT pathway's shape, the space is an anonymous block of its own and the
	 * default white-space treatment (ignore-if-surrounding-linefeed) drops it against the
	 * linefeed the break itself is.</p>
	 *
	 * <p>A break immediately followed by <em>another</em> break is already right and is
	 * left alone: the postprocessor takes the 0pt line-height off the second of a
	 * contiguous pair, so the pair measures the two lines Word draws.</p>
	 *
	 * @since 17.0.6
	 */
	static void emptyLineAfterLineBreak(Document doc) {
		if (!org.docx4j.Docx4jProperties.getProperty(
				"docx4j.convert.out.fo.wordLayout.emptyLineAfterBreak", true)) return;
		for (Element block : elements(doc, "block")) {
			if (!block.hasAttribute(HINT_PSTYLE)) continue;
			List<Element> breaks = new ArrayList<>();
			if (!lineBreaksWithNothingAfter(block, breaks, new boolean[2])) continue;
			for (Element br : breaks) {
				br.removeAttribute("line-height");
			}
		}
	}

	/**
	 * Collects, in reverse document order, the line breaks of this block whose new line
	 * has nothing on it and which FOP would therefore give no line.
	 *
	 * <p>A break immediately followed by another break is already right: the postprocessor
	 * takes the 0pt line-height off the second of a contiguous pair, so the pair measures
	 * two lines as Word draws them.  It is a break whose new line holds something that
	 * paints <em>nothing</em> - an empty run, a field with no result - which is lost.</p>
	 *
	 * @param state [0] content painting on the current line has been seen;
	 *              [1] the break below this one already takes a line of its own
	 * @return true where any such break was found
	 */
	private static boolean lineBreaksWithNothingAfter(Element el, List<Element> breaks, boolean[] state) {
		boolean found = false;
		NodeList children = el.getChildNodes();
		for (int i = children.getLength() - 1; i >= 0; i--) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE) {
				if (n.getNodeValue() != null && n.getNodeValue().length() > 0) {
					state[0] = true;
					state[1] = false;
				}
				continue;
			}
			if (!(n instanceof Element)) continue;
			Element child = (Element) n;
			if (isFo(child, "block-container") || isFo(child, "float")) continue; // out of the flow
			if (isLineBreak(child)) {
				boolean zeroHeight = "0pt".equals(child.getAttribute("line-height"));
				if (!state[0] && !state[1] && zeroHeight) {
					breaks.add(child);
					found = true;
				}
				state[0] = false;      // a new line starts above this break
				state[1] = !zeroHeight || breaks.contains(child);
				continue;
			}
			if (isFo(child, "inline") || isFo(child, "basic-link") || isFo(child, "wrapper")
					|| isFo(child, "bidi-override")) {
				found |= lineBreaksWithNothingAfter(child, breaks, state);
				continue;
			}
			if (isFo(child, "block")) {
				// a nested block of any other kind is a break in its own right; whatever
				// it holds is not this line's
				state[0] = true;
				state[1] = false;
				continue;
			}
			state[0] = true; // external-graphic, leader, page-number, character, ...
			state[1] = false;
		}
		return found;
	}

	/** The nested block BrWriter writes for a {@code w:br} which is not a page break. */
	private static boolean isLineBreak(Element el) {
		return isFo(el, "block")
				&& "preserve".equals(el.getAttribute("linefeed-treatment"))
				&& !el.hasAttribute("break-before");
	}

	/**
	 * The whitespace a paragraph begins with, as a leader of exactly its width.
	 *
	 * <p>Word paints it (&#xa7;4.5) and the XSL-FO default treatment,
	 * ignore-if-surrounding-linefeed, deletes it, so
	 * {@link XsltFOFunctions#createBlockForPPr} writes
	 * {@code white-space-treatment="preserve"} on such a block.  That keeps the leading
	 * spaces - and also the space at every <em>line-break opportunity</em>, so every
	 * wrapped line starts one space to the right of Word's, and on a justified line that
	 * space is stretched too.  Measured on a document whose first body paragraph is
	 * justified and begins with ten literal spaces: Word's continuation lines all start at
	 * x=113.3 where ours ran 119.0 / 117.0 / 117.9 / 120.0 - a spread of up to 4.6pt, on 92
	 * of that document's 142 matched runs of lines and on six documents of a 103-document
	 * corpus.  Isolated on the same block in a probe FO, preserve buys the ten leading
	 * spaces (x0 144.7 against Word's 146.9, the default's 113.3) and costs one space,
	 * 2.8-3.0pt, at every wrap.</p>
	 *
	 * <p>An {@code fo:inline} carrying the property instead is not honoured - FOP reads it
	 * from the nearest ancestor block - so the whitespace itself becomes an
	 * {@code fo:leader} of its measured width, which is what a leading tab already is
	 * (&#xa7;4.4): it reserves the width, is not a break opportunity, and puts no glyphs in
	 * the PDF's text layer, where Word's own PDF has none either.  The block then goes back
	 * on the default treatment.  Where the width cannot be measured - no font, no size, or
	 * whitespace other than plain spaces - the property stands, which is 17.0.5's
	 * behaviour.</p>
	 *
	 * <p>The empty-paragraph placeholder (&#xa7;2.5), whose whole content is one space, is
	 * left alone: that space is the line, not an indent.</p>
	 *
	 * @since 17.0.6
	 */
	static void leadingWhitespaceLeader(Document doc) {
		for (Element block : elements(doc, "block")) {
			if (!"preserve".equals(block.getAttribute("white-space-treatment"))) continue;
			if (inFlowText(block).trim().length() == 0) continue; // the placeholder
			List<Node> at = new ArrayList<Node>();
			List<Integer> counts = new ArrayList<Integer>();
			boolean[] state = new boolean[] { true, false };
			scanLeadingWhitespace(block, state, at, counts);
			if (state[1] || at.isEmpty()) continue;
			List<Double> widths = new ArrayList<Double>();
			boolean ok = true;
			for (int i = 0; i < at.size() && ok; i++) {
				double w = spacesWidthPt(block, at.get(i), counts.get(i).intValue());
				if (w < 0) ok = false;
				else widths.add(Double.valueOf(w));
			}
			if (!ok) continue;
			for (int i = 0; i < at.size(); i++) {
				Node text0 = at.get(i);
				Element leader = doc.createElementNS(FO_NS, "fo:leader");
				leader.setAttribute("leader-pattern", "space");
				leader.setAttribute("leader-length",
						org.docx4j.fonts.WordLineMetrics.format(widths.get(i).doubleValue()));
				text0.getParentNode().insertBefore(leader, text0);
				text0.setNodeValue(text0.getNodeValue().substring(counts.get(i).intValue()));
			}
			block.removeAttribute("white-space-treatment");
		}
	}

	/** The block's own text: what an out-of-flow child (a positioned container, a float)
	 *  holds is not on this block's line. */
	private static String inFlowText(Element el) {
		StringBuilder sb = new StringBuilder();
		for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE) {
				if (n.getNodeValue() != null) sb.append(n.getNodeValue());
			} else if (n instanceof Element) {
				Element child = (Element) n;
				if (isFo(child, "block-container") || isFo(child, "float")) continue;
				sb.append(inFlowText(child));
			}
		}
		return sb.toString();
	}

	/** The width of n spaces in the font this text node is set in, or -1 where it cannot be
	 *  worked out.  The block is the outermost place to look for the font. */
	private static double spacesWidthPt(Element block, Node text, int count) {
		String family = null, size = null;
		for (Node p = text.getParentNode(); p instanceof Element; p = p.getParentNode()) {
			Element el = (Element) p;
			if (family == null && el.getAttribute("font-family").length() > 0) {
				family = el.getAttribute("font-family");
			}
			if (size == null && el.getAttribute("font-size").length() > 0) {
				size = el.getAttribute("font-size");
			}
			if (el == block) break;
		}
		if (family == null || size == null) return -1;
		double sizePt = lengthPt(size);
		if (sizePt <= 0) return -1;
		org.docx4j.fonts.PhysicalFont pf = org.docx4j.fonts.PhysicalFonts.get(family);
		if (pf == null) return -1;
		StringBuilder spaces = new StringBuilder(count);
		for (int i = 0; i < count; i++) spaces.append(' ');
		try {
			double w = org.docx4j.fonts.TextMeasurer.widthPt(spaces.toString(), pf, sizePt);
			return w > 0 ? w : -1;
		} catch (RuntimeException e) {
			return -1;
		}
	}

	/**
	 * The text nodes which begin with whitespace FOP would delete: at the start of the
	 * block, and after each nested block (a w:br).  Mirrors
	 * XsltFOFunctions.scanLeadingWhitespace, which decides whether the property is written.
	 *
	 * @param state [0] true while the next character would be "after a linefeed" for FOP;
	 *        [1] set where such whitespace is something other than plain spaces
	 */
	private static void scanLeadingWhitespace(Node n, boolean[] state,
			List<Node> at, List<Integer> counts) {
		NodeList children = n.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
				String v = child.getNodeValue();
				if (v == null || v.isEmpty()) continue;
				if (state[0] && isXmlWhitespace(v.charAt(0))) {
					int end = 0;
					while (end < v.length() && isXmlWhitespace(v.charAt(end))) end++;
					for (int j = 0; j < end; j++) {
						if (v.charAt(j) != ' ') state[1] = true;
					}
					at.add(child);
					counts.add(Integer.valueOf(end));
				}
				state[0] = false;
				continue;
			}
			if (!(child instanceof Element)) continue;
			Element el = (Element) child;
			if (isFo(el, "block-container") || isFo(el, "float")) continue;
			if (isFo(el, "inline") || isFo(el, "wrapper") || isFo(el, "basic-link")
					|| isFo(el, "bidi-override")) {
				scanLeadingWhitespace(el, state, at, counts);
				continue;
			}
			if (isFo(el, "block") || isFo(el, "list-block") || isFo(el, "table")) {
				state[0] = true;
				continue;
			}
			state[0] = false; // a leader, an external-graphic, a page-number, ...
		}
	}

	private static boolean isXmlWhitespace(char c) {
		return c == ' ' || c == '\t' || c == '\n' || c == '\r';
	}

	/**
	 * white-space-treatment is an <em>inherited</em> property, and FOP reads it from the
	 * nearest ancestor fo:block (XMLWhiteSpaceHandler takes it from currentBlock), so a
	 * paragraph which needs its own whitespace preserved - the empty-paragraph
	 * placeholder above, or a paragraph whose text begins with a space (&#xa7;4.5) - has
	 * to carry the property on its block.  Where that block is also a <em>container</em>
	 * - a paragraph whose objects were lifted out into positioned fo:block-containers (an
	 * anchored picture, a text box) - every block inside those containers inherits
	 * "preserve" and keeps its own leading whitespace.  Measured: one such paragraph
	 * enclosing forty positioned text boxes moved every continuation line inside them
	 * from x=72.0 to 74.2, exactly one 8pt Arimo space (0.2778em = 2.22pt), and the
	 * narrower measure re-broke the text; 174 blocks in 53 of 156 corpus documents.
	 *
	 * <p>So each out-of-flow child - never part of the paragraph's own line - is put back
	 * on the XSL-FO default.  A block inside one that really wants preserved whitespace
	 * states it for itself.</p>
	 *
	 * @since 17.0.6
	 */
	static void containWhitespaceTreatment(Document doc) {
		for (Element block : elements(doc, "block")) {
			if (!"preserve".equals(block.getAttribute("white-space-treatment"))) continue;
			NodeList children = block.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node n = children.item(i);
				if (!(n instanceof Element)) continue;
				Element child = (Element) n;
				if (!isFo(child, "block-container") && !isFo(child, "float")) continue;
				if (child.hasAttribute("white-space-treatment")) continue; // it decided for itself
				child.setAttribute("white-space-treatment", "ignore-if-surrounding-linefeed");
			}
		}
	}

	/** Whether this block's own children would give FOP something to put on a line.
	 *  Only an out-of-flow child - a positioned container holding an anchored picture
	 *  or a text box, or a float - is not counted; a nested fo:block (a w:br) makes
	 *  lines of its own, so it is. */
	private static boolean producesContent(Element el) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE) {
				if (n.getNodeValue() != null && n.getNodeValue().length() > 0) return true;
				continue;
			}
			if (!(n instanceof Element)) continue;
			Element child = (Element) n;
			if (isFo(child, "block-container") || isFo(child, "float")) {
				continue; // out of the flow: its own areas, not this block's line
			}
			if (isFo(child, "inline") || isFo(child, "basic-link") || isFo(child, "wrapper")
					|| isFo(child, "bidi-override")) {
				if (producesContent(child)) return true;
				continue;
			}
			return true; // external-graphic, leader, page-number, footnote, character, ...
		}
		return false;
	}

	// ------------------------------------------------------------ 1. hard page breaks

	/**
	 * Inside a table, Word applies w:pageBreakBefore to the <em>table</em>, not to the
	 * paragraph: a break on the paragraph that opens the table starts the table on a
	 * new page, and one anywhere else in the table is ignored.  FOP instead takes every
	 * break-before it finds in an fo:table-cell and breaks the table there.
	 *
	 * <p>Measured against Word's own PDFs: a mail-merge template with sixteen
	 * w:pageBreakBefore spread over the rows of one table came out as twelve pages
	 * against Word's four, the extra pages carrying one line each; a report with one on
	 * the first paragraph of each of two tables has five pages in Word, which are the
	 * two breaks taken at the tables.</p>
	 *
	 * <p>A {@code w:pageBreakBefore} on the opening paragraph is therefore moved to the
	 * fo:table, and any other break inside a cell is dropped.  A nested table cannot
	 * carry it (the break would land inside the outer table, which is the behaviour
	 * being removed), so there it is dropped as well.</p>
	 *
	 * <p><b>A {@code w:br w:type="page"} inside a cell is ignored outright</b>, wherever
	 * it stands and however many of them there are: Word paginates on the paragraph
	 * property, not on the break run.  Measured on {@code page-break-in-cell}, which
	 * varies the position and the count one at a time - a single break at the head of
	 * the first cell's first paragraph, two of them there, one in a later paragraph of
	 * the cell, one in a cell which is not the first, and one in the second row - and
	 * Word gives none of them a page: its table shares a page with the paragraph
	 * introducing it in every case, and its seventh and last page is the one
	 * {@code w:pageBreakBefore} opens.  17.0.6 promoted a single head break to the
	 * table and had eight pages.  A {@code w:br} reaches the FO as a block nested in the
	 * run's {@code fo:inline}, which is how it is told from a {@code w:pageBreakBefore}
	 * on the paragraph's own block.</p>
	 *
	 * @since 17.0.6
	 */
	static void dropPageBreaksInTableCells(Document doc) {
		for (Element cell : elements(doc, "table-cell")) {
			for (Element block : descendants(cell, "block")) {
				if (!"page".equals(block.getAttribute("break-before"))) continue;
				block.removeAttribute("break-before");
				if (block.hasAttribute(HINT_BREAK_RUN)) continue; // a w:br, which Word ignores
				Element table = ancestorTable(block);
				if (table != null && ancestorTable(table) == null && opensTable(table, block)) {
					table.setAttribute("break-before", "page");
				}
			}
		}
	}

	/** Whether this block is the first thing the table holds, descending through any
	 *  borders/shading wrapper the first cell's content is in. */
	private static boolean opensTable(Element table, Element block) {
		for (Element f = firstBlock(table); f != null; f = firstBlock(f)) {
			if (f == block) return true;
		}
		return false;
	}

	// -------------------------------------------------- 6. the width a cell line fits in

	/** "1" on an fo:table whose columns docx4j's content-based autofit pass sized
	 *  (TableWriter.applyTableCustomAttributes). */
	public static final String HINT_CONTENT_SIZED = "docx4j-content-sized";

	/** On an fo:table whose start-indent took the compatibility-mode-14 grid-edge shift
	 *  (TableWriter.applyStartIndent): the left cell margin it was moved back by.
	 *  @since 17.0.6 */
	public static final String HINT_GRID_SHIFT = "docx4j-grid-shift";

	/**
	 * Word applies the mode-14 grid-edge shift to a <em>top-level</em> table only.  A
	 * table nested in a w:tc has its grid edge on the containing cell's content edge,
	 * with its own cell margin added on top: measured on a mode-14 header (page margin
	 * 28.35pt, outer w:tblInd 108, cell margin 108), Word's clip for a nested table runs
	 * from 33.9 = 28.35 + 5.4 and its text lands at 39.1, where docx4j drew it at 34.0 -
	 * one cell margin left, on every cell of every nested table.  There were 45 of them
	 * across 11 corpus documents, and it was the first divergence in four.
	 *
	 * <p>Whether a table is nested is not known where the shift is computed - in the XSLT
	 * pathway the w:tbl reaching the table writer was unmarshalled on its own, so it has
	 * no parent - but it is plain here, which also keeps the two pathways identical.</p>
	 *
	 * @since 17.0.6
	 */
	static void nestedTableGridEdge(Document doc) {
		for (Element tbl : elements(doc, "table")) {
			if (!tbl.hasAttribute(HINT_GRID_SHIFT)) continue;
			double shift = lengthPt(tbl.getAttribute(HINT_GRID_SHIFT));
			tbl.removeAttribute(HINT_GRID_SHIFT);
			if (shift == 0 || !insideTableCell(tbl)) continue;
			double indent = lengthPt(tbl.getAttribute("start-indent"));
			tbl.setAttribute("start-indent", org.docx4j.fonts.WordLineMetrics.format(indent + shift));
		}
	}

	private static boolean insideTableCell(Element el) {
		for (Node n = el.getParentNode(); n instanceof Element; n = n.getParentNode()) {
			if (isFo((Element) n, "table-cell")) return true;
		}
		return false;
	}

	/**
	 * A column Word's autofit pass sized holds its widest cell content on one line,
	 * because that content is what set the width: measured on {@code table-autofit-wrap},
	 * Word's three columns are the widest content plus the cell margins (127.4 = 116.1 +
	 * 10.8) and each is drawn whole, the widest bleeding 0.4pt of its right margin.  FOP
	 * takes the cell's borders off the content width as well - half of each collapsed
	 * border, all of a separate one - so exactly those lines were re-broken: all three
	 * columns of that probe wrapped, and in {@code pbdr-space} a cell's "second line 0"
	 * (47.1pt of content in a 47.1pt column) came out on two lines.
	 *
	 * <p>The border allowance is given back as a smaller end padding, which leaves the
	 * text's start - the grid edge plus half the border plus the left cell margin (§6.2)
	 * - exactly where it was, and lets the content reach as far past the right cell
	 * margin as Word lets it.
	 *
	 * <p><b>A collapsed border costs a cell's text measure nothing, in a grid-sized cell
	 * as much as in a content-sized one.</b>  Measured on {@code table-cell-measure},
	 * whose every table holds one line in a column that line's own advance sized (its
	 * advance rounded up to a whole twip, plus two twips, plus the two 108-twip cell
	 * margins) and whose three rows give their end cell margin back nothing, half the
	 * border width and the whole border width: Word wraps <em>no</em> row of the
	 * collapsed 0.5, 1.5 and 3pt tables, where FOP's half-of-each-border charge wrapped
	 * the first row of the 0.5pt table and the first two of the others.  That settles
	 * H12 and generalises 17.0.6's content-sized rule, which the {@code table-fixed} and
	 * {@code table-cellspacing} goldens had appeared to contradict - their lines had no
	 * slack at all, so all they said was that <em>something</em> was charged.
	 *
	 * <p>With <b>separate</b> borders ({@code w:tblCellSpacing}) Word charges what FOP
	 * charges: the same probe's three cell-spacing tables wrap all three rows in Word
	 * and here alike, which is two whole border widths.  Nothing is given back there.
	 *
	 * <p>Until 17.0.6 a tenth of a point of the allowance was held back where the width
	 * came from the grid, because FOP's line measure ran that much narrow - its glyph
	 * advances were truncated to 1/1000 em rather than rounded.  They are rounded now
	 * ({@code org.docx4j.fop.fonts.WordGlyphWidths}), so the whole allowance is given
	 * back, and {@code table-fixed}, {@code table-cellspacing} and
	 * {@code table-cell-measure} all keep the lines Word keeps without it.
	 *
	 * @since 17.0.6
	 */
	static void cellLineWidth(Document doc) {
		for (Element cell : elements(doc, "table-cell")) {
			Element tbl = ancestorTable(cell);
			if (tbl == null) continue;
			// a separate border is Word's charge as well as FOP's; only a collapsed one
			// (which FOP charges half of to each of the two cells it separates) is free
			if ("separate".equals(tbl.getAttribute("border-collapse"))) continue;
			double give = 0.5 * (lengthPt(cell.getAttribute("border-left-width"))
					+ lengthPt(cell.getAttribute("border-right-width")));
			if (give <= 0) continue;
			// the end side is the one to take it from: the start padding places the text
			String end = "rl-tb".equals(writingMode(cell)) ? "padding-left" : "padding-right";
			double padding = lengthPt(cell.getAttribute(end));
			if (padding <= 0) continue;   // nothing to give back; FO padding cannot go negative
			cell.setAttribute(end, pt(Math.max(0, padding - give)));
		}
	}

	/** BrWriter's mark on the fo:block a {@code w:br w:type="page"} became, which tells
	 *  it from the break a {@code w:pageBreakBefore} puts on the paragraph's own block.
	 *  @since 17.0.6 */
	public static final String HINT_BREAK_RUN = "docx4j-break-run";

	/** The nearest writing-mode in force on this element, or null. */
	private static String writingMode(Element el) {
		for (Node n = el; n instanceof Element; n = n.getParentNode()) {
			String v = ((Element) n).getAttribute("writing-mode");
			if (v != null && v.length() > 0) return v;
		}
		return null;
	}

	/** The nearest ancestor fo:table of this element, or null. */
	private static Element ancestorTable(Element el) {
		for (Node n = el.getParentNode(); n instanceof Element; n = n.getParentNode()) {
			if (isFo((Element) n, "table")) return (Element) n;
		}
		return null;
	}

	/**
	 * A {@code w:br w:type="column"} in a section which <em>has</em> another column to go
	 * to is a column break, not the line break docx4j made of it until 17.0.6: Word takes
	 * the break there, and what follows it opens the next column (&#xa7;7.3).  The
	 * paragraph has already been divided at the break by
	 * {@code ConversionSectionWrapperFactory} (via {@code ColumnBreaks}), which leaves the
	 * break at the head of the half which follows it, so all that is left here is to move
	 * it off the inline it may sit in and onto that half's block.
	 *
	 * <p>Where the section has one column there is nowhere to break to - Word's own
	 * columns are the region body's - and the break stays the line break it was.  A break
	 * with content before it in its block is one such (nothing divided it), and is left
	 * alone as well.</p>
	 *
	 * <p>The half which follows the break takes a line even where the break ends the
	 * paragraph and nothing is left of it but its mark; that is
	 * {@link #emptyLineForBlockWithNoContent}'s doing, which is why this runs first.</p>
	 *
	 * @since 17.0.6
	 */
	static void columnBreaks(Document doc) {
		List<Element> breaks = new ArrayList<Element>();
		for (Element block : elements(doc, "block")) {
			if (block.getAttribute(HINT_COLUMN_BREAK).length() > 0) breaks.add(block);
		}
		for (Element block : breaks) {
			block.removeAttribute(HINT_COLUMN_BREAK);
			Element para = enclosingParagraph(block);
			if (para == null || columnCount(para) <= 1) continue;
			// not inside a table: a break there belongs to the table, as a page break
			// does (§3.3), and FOP would break the table where it found it
			if (insideTableCell(para)) continue;
			if (contentPrecedesInBlock(para, block)) continue;
			block.getParentNode().removeChild(block);
			String existing = para.getAttribute("break-before");
			if (existing.length() == 0 || "auto".equals(existing)) {
				para.setAttribute("break-before", "column");
			}
			// where the break ended the paragraph, all that is left of the half which
			// opens the column is its mark, which takes a line of its own (§7.3).  The
			// block was not empty when createBlock wrote it, so it has no placeholder of
			// its own and emptyLineForBlockWithNoContent leaves it alone.
			if (!para.hasChildNodes()) {
				para.setAttribute("white-space-treatment", "preserve");
				para.appendChild(doc.createTextNode(" "));
			}
		}
	}

	/** Whether anything which draws comes before this element in the block, in document
	 *  order.  (The element is a descendant of the block.)  @since 17.0.6 */
	private static boolean contentPrecedesInBlock(Element block, Element before) {
		return !scanUntil(block, before, new boolean[1]);
	}

	/** @return true where the scan reached {@code stop} without finding content; state[0]
	 *  records whether it has been reached. */
	private static boolean scanUntil(Node n, Element stop, boolean[] reached) {
		for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c == stop) {
				reached[0] = true;
				return true;
			}
			if (c.getNodeType() == Node.TEXT_NODE || c.getNodeType() == Node.CDATA_SECTION_NODE) {
				String v = c.getNodeValue();
				if (v != null && v.trim().length() > 0) return false;
				continue;
			}
			if (!(c instanceof Element)) continue;
			Element e = (Element) c;
			if (isFo(e, "inline") || isFo(e, "basic-link") || isFo(e, "wrapper")
					|| isFo(e, "bidi-override") || isFo(e, "block")) {
				if (!scanUntil(e, stop, reached)) return false;
				if (reached[0]) return true;
				continue;
			}
			if (isFo(e, "block-container") || isFo(e, "float")) continue; // out of the flow
			return false; // external-graphic, leader, page-number, ...
		}
		return true;
	}

	static void mergePageBreakParagraphs(Document doc, int compatibilityMode) {
		List<Element> empties = new ArrayList<>();
		for (Element block : elements(doc, "block")) {
			if ("page".equals(block.getAttribute("break-before")) && isEmpty(block)) {
				empties.add(block);
			}
		}
		for (Element empty : empties) {
			Element next = nextElementSibling(empty);
			// a container which takes no space - a floating table or a picture already
			// positioned out of the flow - takes the break as a block does: that is what
			// the page of a page-anchored floating table begins with, and leaving the
			// empty block there cost a line at the top of it (measured on the
			// table-floating-anchor probe: Word's first paragraph at y=83.1, docx4j's at
			// 108.6).  Not a table: measured on a corpus document whose page break is
			// followed by one, Word keeps that line, and dropping it lost a page.
			// @since 17.0.6
			if (next == null || !(isFo(next, "block") || takesNoSpace(next))) {
				// Nothing left in this section for the break to move to.  Where another
				// section follows, its own page-sequence starts a page anyway and Word
				// does not add one for the break as well: measured on the tab-toc-pageref
				// probe, whose third section ends "page break paragraph, section-break
				// paragraph" - Word 6 pages, and the section after it opens page 4 at the
				// top; ours had a page 4 holding nothing but that empty block.  At the end
				// of the document the page is Word's (the page-blank probe ends in a page
				// break and Word gives it a ninth page), so the break stays there.
				// @since 17.0.6
				if (next == null && empty.getParentNode() instanceof Element
						&& isFo((Element) empty.getParentNode(), "flow") && sectionFollows(empty)) {
					empty.removeAttribute("break-before");
				}
				continue;
			}
			// Every break costs a page boundary of its own, so where the break paragraph
			// is not the only thing between two pages it keeps its page rather than
			// being folded into what follows.  Two shapes, both measured on the
			// page-empty golden, where Word has 13 pages and folding gave 11:
			//   - the next block is itself a break paragraph with nothing on it (two
			//     break-only paragraphs, or two w:br in one paragraph, which PageBreak
			//     splits into two blocks): the material between the two breaks - here
			//     nothing but this paragraph's mark - is a page with nothing on it.
			//     Only where the next block is *empty*: a w:pageBreakBefore paragraph
			//     that follows a page break is already at the top of a page and Word
			//     adds none for it (page-empty's S6, and measured on two corpus
			//     documents whose Heading 1 style carries w:pageBreakBefore, where
			//     counting it cost two spurious pages of fourteen and one of seven);
			//   - this block opens the flow, i.e. a section break has just started a
			//     page: Word puts the mark on that page and the break makes another, so
			//     a typeless w:sectPr followed by a break-only paragraph is an empty
			//     page.  Merging moved the break onto the flow's first block, where FO
			//     ignores it.  Only where the block after it does not break the page on
			//     its own account: where the section's first paragraph holds a break
			//     with text after it, PageBreak has already split it in two and the
			//     second half carries the break, which is the one page Word gives
			//     (measured on a corpus document where counting both cost it Word's
			//     page count, 22 -> 23).
			// @since 17.0.6
			boolean nextAlreadyBreaks = "page".equals(next.getAttribute("break-before"));
			boolean nextBreaks = nextAlreadyBreaks && isFo(next, "block") && isEmpty(next);
			if (nextBreaks || (opensFlow(empty) && !nextAlreadyBreaks)) {
				if (!nextBreaks) next.setAttribute("break-before", "page");
				if (!empty.hasChildNodes()) {
					// FOP builds no page for a block with no area: give it the mark's line
					empty.setAttribute("white-space-treatment", "preserve");
					empty.appendChild(doc.createTextNode(" "));
				}
				continue;
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

	/** Whether this block is the first thing its fo:flow holds, so that the section's own
	 *  page has just been started for it.  (A continuous section is merged into the
	 *  page-sequence before it, so a flow start is always a page start.)  @since 17.0.6 */
	private static boolean opensFlow(Element block) {
		Node parent = block.getParentNode();
		Element child = block;
		while (parent instanceof Element) {
			for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
				if (!(n instanceof Element)) continue;
				if (n != child) return false;
				break;
			}
			if (isFo((Element) parent, "flow")) return true;
			child = (Element) parent;
			parent = parent.getParentNode();
		}
		return false;
	}

	/** Whether another fo:page-sequence - another Word section - follows the one this
	 *  block is in, so that a page is started for it whatever this block asks for.
	 *  @since 17.0.6 */
	private static boolean sectionFollows(Element block) {
		Node seq = block;
		while (seq instanceof Element && !isFo((Element) seq, "page-sequence")) {
			seq = seq.getParentNode();
		}
		if (!(seq instanceof Element)) return false;
		for (Node n = seq.getNextSibling(); n != null; n = n.getNextSibling()) {
			if (n instanceof Element && isFo((Element) n, "page-sequence")) return true;
		}
		return false;
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
			if (first != null && isAutoSpaceBefore(first)) {
				// HTML auto spacing (w:beforeAutospacing) is a margin, and a margin
				// collapses out at the top of the body: measured on a document whose
				// first paragraph carries it, every line of page 1 was exactly +14.0pt -
				// Word 73.5 / 86.7 / 98.2 / 109.7, docx4j 87.5 / 100.7 / 112.2 / 123.7.
				// An explicit w:spacing w:before is honoured there (the spacing-page-top
				// probe: 36pt before on the first paragraph of a document), so only the
				// automatic value goes.  @since 17.0.6
				first.setAttribute("space-before", "0pt");
			} else if (first != null && hasSpace(first, "space-before")) {
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

	/**
	 * A vertically aligned section counts its last paragraph's space-after as part of
	 * the block it aligns, so a bottom-aligned section's last line sits that much above
	 * the bottom margin.  {@code space-after.conditionality} defaults to discard at the
	 * end of a reference area, so FOP dropped it and put the last line on the margin.
	 *
	 * <p>Measured on {@code section-valign-bottom}, whose sections pair a last
	 * paragraph carrying 24pt of space-after with a control carrying none: Word's
	 * bottom-aligned pages close at y=743.7 and 767.5 - 23.8pt apart - where docx4j put
	 * both at 767.4, and its centre-aligned pair is 11.8pt apart, half of the same
	 * 24pt.  The controls already matched.  A section whose <em>first</em> paragraph
	 * carries 24pt of space-before is not moved by it (Word closes it at 767.5, the
	 * same as the control), so only the end is retained.</p>
	 *
	 * <p>Not settled, and not implemented: where the section's last block is a table,
	 * Word's aligned content also holds the empty paragraph a table must be followed
	 * by - its three table sections sit 15.7pt higher than ours, and 7.9pt for the
	 * centred one - but docx4j drops a paragraph whose only content is the
	 * {@code w:sectPr}, so there is nothing here to retain.</p>
	 *
	 * @since 17.0.6
	 */
	static void retainSpaceAfterInAlignedFlow(Document doc) {
		java.util.Set<String> aligned = verticallyAlignedMasters(doc);
		if (aligned.isEmpty()) return;
		for (Element seq : elements(doc, "page-sequence")) {
			if (!aligned.contains(seq.getAttribute("master-reference"))) continue;
			for (Element flow : descendants(seq, "flow")) {
				Element last = lastBlock(flow);
				if (last != null && hasSpace(last, "space-after")) {
					last.setAttribute("space-after.conditionality", "retain");
				}
			}
		}
	}

	/** The master names whose region-body is display-aligned other than at the top,
	 *  page-sequence-masters included through the masters they reference. */
	private static java.util.Set<String> verticallyAlignedMasters(Document doc) {
		java.util.Set<String> out = new java.util.HashSet<String>();
		for (Element spm : elements(doc, "simple-page-master")) {
			for (Element rb : descendants(spm, "region-body")) {
				String da = rb.getAttribute("display-align");
				if ("center".equals(da) || "after".equals(da)) {
					out.add(spm.getAttribute("master-name"));
					break;
				}
			}
		}
		if (out.isEmpty()) return out;
		for (Element psm : elements(doc, "page-sequence-master")) {
			for (Element ref : descendants(psm, "conditional-page-master-reference")) {
				if (out.contains(ref.getAttribute("master-reference"))) {
					out.add(psm.getAttribute("master-name"));
					break;
				}
			}
			for (Element ref : descendants(psm, "single-page-master-reference")) {
				if (out.contains(ref.getAttribute("master-reference"))) {
					out.add(psm.getAttribute("master-name"));
					break;
				}
			}
		}
		return out;
	}

	/** Whether this block's space-before is HTML auto spacing (w:beforeAutospacing),
	 *  which the hint records as "b" or "ba". */
	private static boolean isAutoSpaceBefore(Element block) {
		if (!hasSpace(block, "space-before")) return false;
		if (block.getAttribute(HINT_AUTOSPACING).contains("b")) return true;
		// a borders/shading wrapper carries the first paragraph's spacing (syncContainerSpacing)
		Element inner = firstBlock(block);
		return inner != null && inner != block && hasSpace(inner, "space-before")
				&& inner.getAttribute(HINT_AUTOSPACING).contains("b");
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

	/**
	 * OOXML requires a {@code w:p} after a {@code w:tbl} inside a {@code w:tc}, and Word
	 * gives that paragraph no line at all - the cell ends on the nested table.  docx4j
	 * writes it like any other empty paragraph (&#xa7;2.5), which is a whole line too
	 * many at the bottom of every cell holding a nested table.
	 *
	 * <p>Measured on a mode-14 first-page header whose outer row 1 holds three nested
	 * rows (baselines 23.5 / 33.8 / 43.9, pitch 10.2): Word's next outer row starts at
	 * 54.7, 10.8pt later, where docx4j went 43.7 -> 64.8.  The header table then ended
	 * 28.9pt low and the body started 35.7 to 37.0pt low, which turned Word's two pages
	 * into four.  Only the <em>cell-final</em> paragraph that follows the table: an
	 * empty paragraph anywhere else in a cell keeps its line, as Word gives it one.</p>
	 *
	 * @since 17.0.6
	 */
	static void dropParagraphAfterNestedTable(Document doc) {
		for (Element cell : elements(doc, "table-cell")) {
			Element last = null, previous = null;
			NodeList children = cell.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node n = children.item(i);
				if (!(n instanceof Element)) continue;
				previous = last;
				last = (Element) n;
			}
			if (last == null || previous == null) continue;
			if (!isFo(last, "block") || !last.hasAttribute(HINT_PSTYLE)) continue;
			if (!isFo(previous, "table")) continue;
			if (!isBlank(last)) continue;
			cell.removeChild(last);
		}
	}

	/**
	 * Whether this block would put nothing but whitespace on its line.
	 *
	 * <p>Empty inline wrappers do not count, and neither does whitespace-only text: the
	 * paragraph a nested table forces arrives here as an {@code fo:block} holding an
	 * empty {@code fo:inline} (the run whose only content was the paragraph mark) plus
	 * the preserved space {@link #emptyLineForBlockWithNoContent} appends to it, so a
	 * test that stopped at the first element child never fired on the shape it was
	 * written for.  Measured on the table-grid-edge-compat12 probe: Word's paragraph
	 * after a cell's nested table is at 283.3, 27.4pt below the nested row's baseline,
	 * where docx4j went to 294.8 - one 11.5pt line too many, twice over.</p>
	 *
	 * <p>Anything else - a graphic, a leader, a nested block or a positioned container -
	 * is content, and the block keeps its line.</p>
	 */
	private static boolean isBlank(Element el) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE) {
				if (n.getNodeValue() != null && n.getNodeValue().trim().length() > 0) return false;
				continue;
			}
			if (!(n instanceof Element)) continue;
			Element child = (Element) n;
			if (child.hasAttribute("id")) return false; // a bookmark's anchor: keep it
			if (isFo(child, "inline") || isFo(child, "basic-link") || isFo(child, "wrapper")
					|| isFo(child, "bidi-override")) {
				if (!isBlank(child)) return false;
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * A header's or footer's height, as Word measures it, includes the space-after of
	 * its last paragraph.  XSL-FO drops space at the end of a reference area, so the
	 * area-tree pre-pass which measures the two regions ({@link FOPAreaTreeHelper})
	 * measured one space-after short, and &#xa7;7's
	 * {@code max(top margin, header distance + header height)} then started the body
	 * that much too high (and ended it that much too low).
	 *
	 * <p>Measured on a document with {@code w:pgMar w:top="1440" w:header="709"} whose
	 * header and footer each hold two paragraphs with 10pt between them: our
	 * {@code region-before} extent was 58.867pt and {@code region-after} 32.362pt, each
	 * exactly the blocks' line boxes plus the <em>middle</em> 10pt.  Word's first body
	 * line is at y=113.6 where ours was 102.3, and Word's footer line at 722.6 where
	 * ours was 731.9 - 20.6pt more body on every one of 311 pages.</p>
	 *
	 * <p>Pinning the space rather than adding it in the helper keeps the two exporter
	 * pathways in step and needs no second source of truth for the value; the space is
	 * invisible either way, since a static-content is laid out from the region's top
	 * edge and nothing follows the last block.</p>
	 *
	 * @since 17.0.6
	 */
	/**
	 * A hard page break inside a numbered paragraph belongs to the paragraph, not to the
	 * block inside its {@code fo:list-item-body}: FOP lays the list block's space-before
	 * down on the page the break leaves, so Word's space above the heading is lost.
	 *
	 * <p>Measured on a document whose {@code Heading1} carries
	 * {@code <w:spacing w:before="360" w:after="240"/>} and whose first run is
	 * {@code <w:br w:type="page"/>}: Word's heading is at y=85.0 = the top margin (56.7)
	 * plus 18pt of space-before plus its ascent, where docx4j's block top was 56.75 - the
	 * top margin exactly - and every line of the page carried the -18.2.  The break moves
	 * to the {@code fo:list-block}, which then needs
	 * {@code space-before.conditionality="retain"}, since XSL-FO discards space at the
	 * start of a reference area (&#xa7;3.3: Word honours space-before at the top of a page
	 * after an <em>explicit</em> break, and drops it after an automatic one - and an
	 * automatic break never writes {@code break-before} here).</p>
	 *
	 * @since 17.0.6
	 */
	static void listItemPageBreaks(Document doc) {
		for (Element block : elements(doc, "block")) {
			String br = block.getAttribute("break-before");
			if (!"page".equals(br) && !"even-page".equals(br) && !"odd-page".equals(br)
					&& !"column".equals(br)) continue;
			Element listBlock = null;
			for (Node n = block.getParentNode(); n instanceof Element; n = n.getParentNode()) {
				Element e = (Element) n;
				if (isFo(e, "list-block")) { listBlock = e; break; }
				if (isFo(e, "flow") || isFo(e, "static-content") || isFo(e, "table-cell")
						|| isFo(e, "block-container")) break;
			}
			if (listBlock == null) continue;
			block.removeAttribute("break-before");
			listBlock.setAttribute("break-before", br);
			if (hasSpace(listBlock, "space-before")) {
				listBlock.setAttribute("space-before.conditionality", "retain");
			}
		}
	}

	/**
	 * A space immediately in front of a page number survives.
	 *
	 * <p>FOP treats the end of an {@code fo:inline} as a place where a trailing space
	 * can be collapsed away, so {@code <w:t xml:space="preserve">Seite </w:t>} followed
	 * by a {@code PAGE} field lost its space.  Measured against Word 365 in a corpus
	 * footer: Word's "Seite ii" runs 526.1..547.6 = 21.5pt and ours read "Seiteii" at
	 * 527.6..547.4 = 19.8 - exactly the 1.81pt an 8pt Carlito space is.</p>
	 *
	 * <p>A zero-width space after it, so that the space is no longer the last character,
	 * is what keeps it - the same workaround FldSimpleWriter already uses at the other
	 * end of a {@code fo:page-number-citation-last}, and it leaves the text a reader (or
	 * a text extractor) sees unchanged.  A no-break space would keep the space too, and
	 * measured the same geometry, but it <em>is</em> the text: a footer that already had
	 * its space came out as "Page\u00a01 / 3", which is not what Word wrote.</p>
	 *
	 * @since 17.0.6
	 */
	static void spaceBeforePageNumber(Document doc) {
		for (String name : new String[] { "page-number", "page-number-citation",
				"page-number-citation-last" }) {
			for (Element pn : elements(doc, name)) {
				Node text = previousTextNodeInBlock(pn);
				if (text == null) continue;
				String v = text.getNodeValue();
				if (v == null || v.length() == 0 || v.charAt(v.length() - 1) != ' ') continue;
				text.setNodeValue(v + '\u200b'); // zero-width space: the space is no longer last
			}
		}
	}

	/** The text node just before this element in document order, without leaving the
	 *  block it is in (so a page number at the start of a block picks nothing up). */
	private static Node previousTextNodeInBlock(Element el) {
		Node n = el;
		while (n != null) {
			Node prev = n.getPreviousSibling();
			if (prev == null) {
				Node parent = n.getParentNode();
				if (!(parent instanceof Element) || isFo((Element) parent, "block")
						|| isFo((Element) parent, "block-container")) return null;
				n = parent;
				continue;
			}
			Node deepest = prev;
			while (deepest.getLastChild() != null) deepest = deepest.getLastChild();
			if (deepest.getNodeType() == Node.TEXT_NODE) return deepest;
			n = prev;
		}
		return null;
	}

	static void retainSpacingAtStaticContentEnd(Document doc) {
		for (Element sc : elements(doc, "static-content")) {
			String flow = sc.getAttribute("flow-name");
			if (!flow.startsWith("xsl-region-before") && !flow.startsWith("xsl-region-after")) {
				continue; // the footnote separator is not a header
			}
			// An *empty* header or footer reserves nothing at all, and the header or
			// footer distance alone must not move the body (§7): a section with no
			// footer part still gets a region and a placeholder block, and pinning that
			// block's docDefaults space-after made the region 10pt tall, which pulled
			// the body up by the footer distance plus the space.  Measured: a document
			// whose 44 sectPr say w:bottom="0" w:footer="720" with no footerReference
			// spilled each section's last line onto a page of its own, 24 Word pages
			// coming out as 89.  @since 17.0.6
			if (!hasVisibleContent(sc)) continue;
			/* The same at the other end: space-before.conditionality also defaults to
			 * discard at the start of a reference area, so FOP drops the first
			 * paragraph's space-before where Word applies it.  Measured against Word
			 * 365: a header whose first block has w:before="66" (3.3pt) and a 8.004pt
			 * baseline, with w:header=426 (21.3pt), puts Word's first header baseline at
			 * 21.3 + 3.3 + 8.004 = 32.6 exactly, where ours was 28.3 - -4.3pt on all 16
			 * pages; a second document, whose header style carries w:before="153"
			 * (7.65pt), has Word at 59.5 and ours at 50.9.  @since 17.0.6 */
			Element first = firstBlock(sc);
			if (first != null && hasSpace(first, "space-before") && hasVisibleContent(first)) {
				first.setAttribute("space-before.conditionality", "retain");
			}
			Element last = lastBlock(sc);
			if (last != null && hasSpace(last, "space-after")) {
				last.setAttribute("space-after.conditionality", "retain");
			}
		}
	}

	/** Whether this region draws anything: text beyond white space, or a graphic, a
	 *  leader, a page number, a table.  @since 17.0.6 */
	private static boolean hasVisibleContent(Element el) {
		String text = el.getTextContent();
		if (text != null && text.trim().length() > 0) return true;
		for (String name : new String[] { "external-graphic", "instream-foreign-object",
				"leader", "page-number", "page-number-citation", "page-number-citation-last",
				"table" }) {
			if (el.getElementsByTagNameNS(FO_NS, name).getLength() > 0) return true;
		}
		return false;
	}

	static void retainSpacingAtCellEdges(Document doc, int compatibilityMode) {
		for (Element cell : elements(doc, "table-cell")) {
			List<Element> blocks = childBlocks(cell);
			if (blocks.isEmpty()) continue;
						Element first = blocks.get(0);
			if (first.getAttribute(HINT_AUTOSPACING).indexOf('b') >= 0) {
				/* ... except at the start of a *cell*, where Word's automatic spacing is
				 * zero for the first paragraph but the cell's reference area then eats
				 * the space Word does apply, so the retained conditionality below is what
				 * carries a stated w:before through. */
				first.setAttribute("space-before", "0pt"); // auto spacing is dropped at cell edges (measured)
			} else if (hasSpace(first, "space-before")) {
				first.setAttribute("space-before.conditionality", "retain");
			}
			Element last = blocks.get(blocks.size() - 1);
			if (last.getAttribute(HINT_AUTOSPACING).indexOf('a') >= 0) {
				last.setAttribute("space-after", "0pt");
			} else if (hasSpace(last, "space-after")) {
				// Word keeps a cell's last paragraph's space-after below mode 15 too:
				// measured on a mode-14 document whose cell paragraphs carry
				// w:spacing w:before="60" w:after="60" (3pt each), Word's row pitch is
				// 119.6 -> 137.6 -> 155.6 = 18.0pt = 3 + 11.5 + 3, where ours was
				// 110.7 -> 125.7 -> 139.2 (the space-before only) and the deficit grew
				// to -25.4pt by y=360 on page 1.  @since 17.0.6
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
