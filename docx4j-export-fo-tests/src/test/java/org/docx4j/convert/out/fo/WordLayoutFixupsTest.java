package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The DOM-level rules in WordLayoutFixups, on hand-written FO. */
public class WordLayoutFixupsTest {

	private static final String NS = "xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"";

	private static String flow(String blocks) {
		return "<fo:root " + NS + "><fo:page-sequence><fo:flow flow-name=\"xsl-region-body\">" + blocks
				+ "</fo:flow></fo:page-sequence></fo:root>";
	}

	@Test
	public void pageBreakParagraphMergedIntoNextBlock_mode15() {
		String in = flow("<fo:block space-before=\"0pt\">one</fo:block>"
				+ "<fo:block break-before=\"page\" white-space-treatment=\"preserve\"> </fo:block>"
				+ "<fo:block space-before=\"36pt\">two</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertFalse("empty page-break block still present", out.contains("preserve"));
		assertTrue("break not moved to the next block", out.contains("break-before=\"page\""));
		assertTrue(out.contains(">two<"));
		assertFalse("mode 15 must not keep the space-before after a hard break", out.contains("space-before.conditionality=\"retain\" space-before=\"36pt\"")
				|| out.replace(" ", "").contains("space-before=\"36pt\"space-before.conditionality=\"retain\""));
	}

	@Test
	public void pageBreakParagraphKeepsSpaceBefore_mode14() {
		String in = flow("<fo:block>one</fo:block>"
				+ "<fo:block break-before=\"page\"> </fo:block>"
				+ "<fo:block space-before=\"36pt\">two</fo:block>");
		String out = WordLayoutFixups.apply(in, 14);
		assertTrue(out.contains("space-before.conditionality=\"retain\""));
	}

	@Test
	public void firstBlockOfFlowRetainsSpaceBefore() {
		String in = flow("<fo:block space-before=\"36pt\">one</fo:block><fo:block space-before=\"36pt\">two</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertEquals("only the first block", 1, count(out, "space-before.conditionality=\"retain\""));
		assertTrue(out.indexOf("retain") < out.indexOf(">one<"));
	}

	@Test
	public void firstBlockInsideBidiContainerIsFound() {
		String in = flow("<fo:block-container writing-mode=\"rl-tb\"><fo:block space-before=\"12pt\">x</fo:block></fo:block-container>");
		String out = WordLayoutFixups.apply(in, 15);
		assertTrue(out.contains("space-before.conditionality=\"retain\""));
	}

	@Test
	public void tableCellEdges() {
		String cell = "<fo:table " + NS + "><fo:table-body><fo:table-row><fo:table-cell>"
				+ "<fo:block space-before=\"12pt\" space-after=\"6pt\">a</fo:block>"
				+ "<fo:block space-before=\"12pt\" space-after=\"6pt\">b</fo:block>"
				+ "</fo:table-cell></fo:table-row></fo:table-body></fo:table>";
		String out15 = WordLayoutFixups.apply(flow(cell), 15);
		assertEquals(1, count(out15, "space-before.conditionality=\"retain\""));
		assertEquals(1, count(out15, "space-after.conditionality=\"retain\""));
		String out14 = WordLayoutFixups.apply(flow(cell), 14);
		assertEquals(1, count(out14, "space-before.conditionality=\"retain\""));
		assertEquals("bottom spacing in cells is a mode 15 rule", 0, count(out14, "space-after.conditionality=\"retain\""));
	}

		@Test
	public void contextualSpacingBetweenSameStyle() {
		String in = flow("<fo:block docx4j-pstyle=\"A\" docx4j-contextual=\"1\" space-before=\"12pt\" space-after=\"12pt\">one</fo:block>"
				+ "<fo:block docx4j-pstyle=\"A\" docx4j-contextual=\"1\" space-before=\"12pt\" space-after=\"12pt\">two</fo:block>"
				+ "<fo:block docx4j-pstyle=\"B\" space-before=\"12pt\" space-after=\"12pt\">three</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertFalse("hints must be stripped", out.contains("docx4j-pstyle"));
		int one = out.indexOf(">one<"), two = out.indexOf(">two<"), three = out.indexOf(">three<");
		String b1 = out.substring(out.lastIndexOf("<fo:block", one), one);
		String b2 = out.substring(out.lastIndexOf("<fo:block", two), two);
		String b3 = out.substring(out.lastIndexOf("<fo:block", three), three);
		assertTrue("one: after suppressed (two has the same style)", b1.contains("space-after=\"0pt\""));
		assertTrue("two: before suppressed", b2.contains("space-before=\"0pt\""));
		assertTrue("two: after kept (three differs)", b2.contains("space-after=\"12pt\""));
		assertTrue("three untouched", b3.contains("space-before=\"12pt\"") && b3.contains("space-after=\"12pt\""));
	}

		@Test
	public void contextualSpacingEitherSide() {
		String in = flow("<fo:block docx4j-pstyle=\"A\" docx4j-contextual=\"1\" space-after=\"12pt\">one</fo:block>"
				+ "<fo:block docx4j-pstyle=\"A\" space-before=\"12pt\">two</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		int two = out.indexOf(">two<");
		assertTrue("neighbour's before dropped too", out.substring(out.lastIndexOf("<fo:block", two), two).contains("space-before=\"0pt\""));
	}

	@Test
	public void sectionStartSubtractsPreviousSpaceAfter() {
		String in = "<fo:root " + NS + ">"
				+ "<fo:page-sequence><fo:flow flow-name=\"xsl-region-body\"><fo:block space-before=\"36pt\">a</fo:block><fo:block space-after=\"10pt\">sect</fo:block></fo:flow></fo:page-sequence>"
				+ "<fo:page-sequence><fo:flow flow-name=\"xsl-region-body\"><fo:block space-before=\"36pt\">b</fo:block><fo:block space-after=\"20pt\">sect</fo:block></fo:flow></fo:page-sequence>"
				+ "<fo:page-sequence><fo:flow flow-name=\"xsl-region-body\"><fo:block space-before=\"6pt\">c</fo:block></fo:flow></fo:page-sequence>"
				+ "</fo:root>";
		String out = WordLayoutFixups.apply(in, 15);
		String a = out.substring(out.lastIndexOf("<fo:block", out.indexOf(">a<")), out.indexOf(">a<"));
		String b = out.substring(out.lastIndexOf("<fo:block", out.indexOf(">b<")), out.indexOf(">b<"));
		String c = out.substring(out.lastIndexOf("<fo:block", out.indexOf(">c<")), out.indexOf(">c<"));
		assertTrue("first page: full 36pt", a.contains("space-before=\"36pt\"") && a.contains("retain"));
		assertTrue("36 - 10 = 26", b.contains("space-before=\"26pt\"") && b.contains("retain"));
		assertTrue("6 - 20 -> 0", c.contains("space-before=\"0pt\"") && !c.contains("retain"));
	}

	@Test
	public void autoSpacingDroppedBetweenListItemsAndAtCellEdges() {
		String item = "<fo:list-block><fo:list-item><fo:list-item-label><fo:block>1.</fo:block></fo:list-item-label>"
				+ "<fo:list-item-body><fo:block docx4j-pstyle=\"N\" docx4j-list=\"1\" docx4j-autospacing=\"ba\" line-height=\"13.8pt\" space-before=\"14pt\" space-after=\"14pt\">%s</fo:block></fo:list-item-body></fo:list-item></fo:list-block>";
		String in = flow("<fo:block docx4j-pstyle=\"N\">plain</fo:block>" + String.format(item, "i1") + String.format(item, "i2") + "<fo:block docx4j-pstyle=\"N\">after</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		// spacing moved to the list-blocks: first keeps 14 before, 0 after; second 0 before, 14 after
		int i1 = out.indexOf(">i1<"), i2 = out.indexOf(">i2<");
		String lb1 = out.substring(out.lastIndexOf("<fo:list-block", i1), out.indexOf(">", out.lastIndexOf("<fo:list-block", i1)));
		String lb2 = out.substring(out.lastIndexOf("<fo:list-block", i2), out.indexOf(">", out.lastIndexOf("<fo:list-block", i2)));
		assertTrue(lb1, lb1.contains("space-before=\"14pt\"") && lb1.contains("space-after=\"0pt\""));
		assertTrue(lb2, lb2.contains("space-before=\"0pt\"") && lb2.contains("space-after=\"14pt\""));
		assertTrue("label block gets the body's line-height", out.contains("<fo:block line-height=\"13.8pt\">1.</fo:block>"));

		String cell = "<fo:table><fo:table-body><fo:table-row><fo:table-cell>"
				+ "<fo:block docx4j-autospacing=\"ba\" space-before=\"14pt\" space-after=\"14pt\">c1</fo:block>"
				+ "<fo:block docx4j-autospacing=\"ba\" space-before=\"14pt\" space-after=\"14pt\">c2</fo:block>"
				+ "</fo:table-cell></fo:table-row></fo:table-body></fo:table>";
		out = WordLayoutFixups.apply(flow(cell), 15);
		String c1 = out.substring(out.lastIndexOf("<fo:block", out.indexOf(">c1<")), out.indexOf(">c1<"));
		String c2 = out.substring(out.lastIndexOf("<fo:block", out.indexOf(">c2<")), out.indexOf(">c2<"));
		assertTrue(c1, c1.contains("space-before=\"0pt\"") && c1.contains("space-after=\"14pt\"") && !c1.contains("retain"));
		assertTrue(c2, c2.contains("space-after=\"0pt\"") && c2.contains("space-before=\"14pt\""));
	}

		@Test
	public void exactRowsClipTheirCells() {
		String in = flow("<fo:table><fo:table-body>"
				+ "<fo:table-row height=\"10pt\" docx4j-row-exact=\"10pt\"><fo:table-cell padding-top=\"1pt\"><fo:block>a</fo:block><fo:block>b</fo:block></fo:table-cell></fo:table-row>"
				+ "<fo:table-row height=\"30pt\"><fo:table-cell><fo:block>c</fo:block></fo:table-cell></fo:table-row>"
				+ "</fo:table-body></fo:table>");
		String out = WordLayoutFixups.apply(in, 15);
		assertFalse(out.contains("docx4j-row-exact"));
		assertEquals("only the exact row is wrapped", 1, count(out, "<fo:block-container"));
		assertTrue(out.contains("block-progression-dimension=\"9pt\"") && out.contains("overflow=\"hidden\""));
		assertTrue("both blocks inside the container", out.indexOf("<fo:block-container") < out.indexOf(">a<") && out.indexOf(">b<") < out.indexOf("</fo:block-container>"));
	}

	@Test
	public void zeroSpaceNeedsNoRetain() {
		String in = flow("<fo:block space-before=\"0in\">one</fo:block>");
		assertFalse(WordLayoutFixups.apply(in, 15).contains("retain"));
	}

	private static int count(String s, String sub) {
		int n = 0, i = 0;
		while ((i = s.indexOf(sub, i)) >= 0) { n++; i += sub.length(); }
		return n;
	}

	// ---- Phase 4: superscripts and anchored pictures

	@Test
	public void rootDisregardsBaselineShifts() {
		String out = WordLayoutFixups.apply(flow("<fo:block>x</fo:block>"), 15);
		assertTrue(out.contains("line-height-shift-adjustment=\"disregard-shifts\""));
	}

	private static String anchored(String kind, String x, String y, String extra) {
		return "<fo:block docx4j-pstyle=\"\" space-before=\"6pt\"><fo:inline>text</fo:inline>"
				+ "<fo:inline><fo:external-graphic src=\"x.png\" content-width=\"113px\" content-height=\"85px\""
				+ " docx4j-anchor=\"" + kind + "\" docx4j-anchor-w=\"113.39\" docx4j-anchor-h=\"85.04\""
				+ " docx4j-anchor-x=\"" + x + "\" docx4j-anchor-y=\"" + y + "\" docx4j-anchor-dist=\"9 9 0 0\""
				+ " docx4j-anchor-col=\"451.3\" docx4j-anchor-ml=\"72\"" + extra + "/></fo:inline></fo:block>";
	}

	@Test
	public void squareWrapAtTheRightMarginBecomesARightFloat() {
		String out = WordLayoutFixups.apply(flow(anchored("square", "337.91", "p:0", "")), 15);
		assertTrue("no float", out.contains("float=\"right\""));
		// the float is the paragraph block's first child, before the text
		assertTrue(out.indexOf("<fo:float") < out.indexOf(">text<"));
		// wrap distance on the text side; nothing between the picture and the margin
		assertTrue(out.contains("padding-left=\"9pt\""));
		assertTrue(out.contains("padding-right=\"0pt\""));
		// the picture at its extent, in a block whose font cannot move it
		assertTrue(out.contains("content-width=\"113.39pt\""));
		assertTrue(out.contains("font-size=\"0.1pt\"") && out.contains("line-height=\"0pt\""));
		assertFalse("hints not stripped", out.contains("docx4j-anchor"));
	}

	@Test
	public void squareWrapNearTheLeftEdgeBecomesALeftFloatPaddedToItsOffset() {
		String out = WordLayoutFixups.apply(flow(anchored("square", "72", "p:36", "")), 15);
		assertTrue(out.contains("float=\"left\""));
		assertTrue("offset from the column edge", out.contains("padding-left=\"72pt\""));
		assertTrue("wrap distance on the text side", out.contains("padding-right=\"9pt\""));
		assertTrue("vertical offset from the paragraph top", out.contains("padding-top=\"36pt\""));
	}

	@Test
	public void topAndBottomWrapIsABlockContainerAsTallAsThePicture() {
		String out = WordLayoutFixups.apply(flow(anchored("topAndBottom", "168.96", "p:0", "")), 15);
		assertFalse(out.contains("fo:float"));
		assertTrue(out.contains("height=\"85.04pt\""));
		assertTrue("centred by its offset", out.contains("start-indent=\"168.96pt\""));
		assertTrue(out.indexOf("<fo:block-container") < out.indexOf(">text<"));
	}

	@Test
	public void noWrapIsAbsolutelyPositionedFromTheParagraphTop() {
		String out = WordLayoutFixups.apply(flow(anchored("none", "72", "p:0", " docx4j-anchor-behind=\"1\"")), 15);
		assertTrue(out.contains("absolute-position=\"absolute\""));
		assertTrue(out.contains("height=\"0pt\""));
		assertTrue(out.contains("left=\"72pt\"") && out.contains("top=\"0pt\""));
	}

	@Test
	public void pageRelativePositionIsFixedOnThePage() {
		String out = WordLayoutFixups.apply(flow(anchored("square", "72", "page:100", "")), 15);
		assertFalse("cannot wrap around a page-positioned picture", out.contains("fo:float"));
		assertTrue(out.contains("absolute-position=\"fixed\""));
		assertTrue("left from the page edge", out.contains("left=\"144pt\"") && out.contains("top=\"100pt\""));
	}

	/**
	 * FOP throws on a side float followed by content that overflows the page
	 * (NoSuchElementException in LMiter.next), so docx4j.convert.out.fo.pictures
	 * .float=false lays wrapped pictures out in the flow instead.
	 */
	@Test
	public void picturesFloatPropertyOffLaysThemOutTopAndBottom() {
		String was = org.docx4j.Docx4jProperties.getProperties()
				.getProperty(FOConversionContext.FLOAT_PROPERTY);
		try {
			org.docx4j.Docx4jProperties.setProperty(FOConversionContext.FLOAT_PROPERTY, false);
			String out = WordLayoutFixups.apply(flow(anchored("square", "337.91", "p:0", "")), 15);
			assertFalse("no float when the property is off", out.contains("fo:float"));
			assertTrue(out.contains("<fo:block-container") && out.contains("height=\"85.04pt\""));
		} finally {
			if (was==null) {
				org.docx4j.Docx4jProperties.getProperties().remove(FOConversionContext.FLOAT_PROPERTY);
			} else {
				org.docx4j.Docx4jProperties.setProperty(FOConversionContext.FLOAT_PROPERTY, was);
			}
		}
		// and back on by default
		assertTrue(WordLayoutFixups.apply(flow(anchored("square", "337.91", "p:0", "")), 15)
				.contains("float=\"right\""));
	}

	/** A cell of the given column width holding one square-wrapped picture. */
	private static String cellWithPicture(String columnWidth, String pictureWidth) {
		return "<fo:table " + NS + " width=\"" + columnWidth + "\">"
				+ "<fo:table-column column-width=\"" + columnWidth + "\"/><fo:table-body><fo:table-row>"
				+ "<fo:table-cell>"
				+ anchored("square", "0", "p:0", "").replace("docx4j-anchor-w=\"113.39\"",
						"docx4j-anchor-w=\"" + pictureWidth + "\"")
				+ "</fo:table-cell></fo:table-row></fo:table-body></fo:table>";
	}

	/**
	 * FOP does not implement fo:float in a table ("fo:float (on fo:table)") and paints
	 * nothing for one, so a wrapped picture in a cell takes the text box's treatment
	 * (&#xa7;9.2): narrower than 60% of the cell it is positioned and takes no space.
	 */
	@Test
	public void narrowWrappedPictureInATableCellIsPositioned() {
		String out = WordLayoutFixups.apply(flow(cellWithPicture("300pt", "113.39")), 15);
		assertFalse("FOP has no floats inside tables", out.contains("fo:float"));
		assertTrue("positioned where Word puts it: " + out, out.contains("absolute-position=\"absolute\""));
		assertTrue("and taking no space", out.contains("height=\"0pt\""));
	}

	/** Wider than 60% of the cell, nothing fits beside it, so it reserves its height. */
	@Test
	public void wideWrappedPictureInATableCellReservesItsHeight() {
		String out = WordLayoutFixups.apply(flow(cellWithPicture("150pt", "113.39")), 15);
		assertFalse("FOP has no floats inside tables", out.contains("fo:float"));
		assertFalse("not positioned: " + out, out.contains("absolute-position"));
		assertTrue(out.contains("<fo:block-container") && out.contains("height=\"85.04pt\""));
	}

	/**
	 * A picture which leaves no room beside it - over 90% of the column - is not
	 * floated: Word puts the text below it, and FOP otherwise anchors the float to a
	 * line and paints the picture over the page edge (measured on a document whose two
	 * full-page pictures Word gives a page each, and which came out drawn on top of
	 * each other at the foot of one page).  A picture which does leave room is still
	 * floated: Word wraps beside a 348pt picture on a 453.55pt column.
	 */
	@Test
	public void aPictureFillingTheColumnIsNotFloated() {
		String out = WordLayoutFixups.apply(flow(anchored("square", "0", "p:0", "")
				.replace("docx4j-anchor-w=\"113.39\"", "docx4j-anchor-w=\"440\"")), 15);
		assertFalse("440pt of a 451.3pt column: " + out, out.contains("fo:float"));
		assertTrue(out.contains("<fo:block-container") && out.contains("height=\"85.04pt\""));
	}

	@Test
	public void aPictureWhichLeavesRoomBesideItIsStillFloated() {
		String out = WordLayoutFixups.apply(flow(anchored("square", "0", "p:0", "")
				.replace("docx4j-anchor-w=\"113.39\"", "docx4j-anchor-w=\"348\"")), 15);
		assertTrue("348pt of a 451.3pt column, which Word wraps beside: " + out,
				out.contains("fo:float"));
	}

	/** With no column widths to read, the section's text column is the measure. */
	@Test
	public void pictureInACellWithNoColumnWidthsUsesTheTextColumn() {
		String cell = "<fo:table " + NS + "><fo:table-body><fo:table-row><fo:table-cell>"
				+ anchored("square", "0", "p:0", "").replace("docx4j-anchor-w=\"113.39\"",
						"docx4j-anchor-w=\"400\"")
				+ "</fo:table-cell></fo:table-row></fo:table-body></fo:table>";
		String out = WordLayoutFixups.apply(flow(cell), 15);
		assertFalse("400pt is 89% of the 451.3pt column: " + out, out.contains("absolute-position"));
	}

	@Test
	public void hintsStrippedWhenNoParagraphBlockIsFound() {
		String in = flow("<fo:block><fo:external-graphic src=\"x.png\" docx4j-anchor=\"square\" docx4j-anchor-w=\"10\""
				+ " docx4j-anchor-h=\"10\" docx4j-anchor-x=\"0\" docx4j-anchor-y=\"p:0\" docx4j-anchor-dist=\"0 0 0 0\""
				+ " docx4j-anchor-col=\"400\" docx4j-anchor-ml=\"72\"/></fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertFalse(out.contains("docx4j-anchor"));
		assertFalse(out.contains("fo:float"));
	}

	// ---- Word's line box (org.docx4j.fop.wordlayout is part of docx4j-export-fo, on by default)

	@Test
	public void lineBoxHintsBecomeNamespacedAttributes() {
		String in = flow("<fo:block docx4j-pstyle=\"\" line-height=\"27.6pt\" docx4j-linebox=\"13.8pt\" docx4j-baseline=\"11.2pt\" docx4j-linerule=\"auto\">x</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertTrue(out.contains("xmlns:docx4j=\"http://docx4j.org/fop/word-layout\""));
		assertTrue(out.contains("docx4j:line-box=\"13.8pt\""));
		assertTrue(out.contains("docx4j:baseline=\"11.2pt\""));
		assertTrue(out.contains("docx4j:line-rule=\"auto\""));
		assertFalse("hints not stripped", out.contains("docx4j-linebox") || out.contains("docx4j-baseline"));
	}

	@Test
	public void lineBoxHintsDroppedWhenWordLayoutIsOff() {
		org.docx4j.Docx4jProperties.setProperty("docx4j.convert.out.fo.wordLayout", "false");
		try {
			String in = flow("<fo:block docx4j-pstyle=\"\" line-height=\"27.6pt\" docx4j-linebox=\"13.8pt\" docx4j-baseline=\"11.2pt\" docx4j-linerule=\"auto\">x</fo:block>");
			String out = WordLayoutFixups.apply(in, 15);
			assertFalse("FOP would reject the attributes without the ElementMapping", out.contains("docx4j:"));
			assertFalse(out.contains("docx4j-linebox"));
		} finally {
			org.docx4j.Docx4jProperties.setProperty("docx4j.convert.out.fo.wordLayout", "true");
		}
	}

	// ---- columns and span-all parts (merged continuous sections)

	@Test
	public void paragraphsInsideASpanAllBlockAreNeighbours() {
		String in = flow("<fo:block span=\"all\">"
				+ "<fo:block docx4j-pstyle=\"ListParagraph\" docx4j-contextual=\"1\" space-after=\"10pt\">a</fo:block>"
				+ "<fo:block docx4j-pstyle=\"ListParagraph\" docx4j-contextual=\"1\" space-after=\"10pt\">b</fo:block>"
				+ "</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertTrue("contextual spacing not applied inside the span block", out.contains("space-after=\"0pt\">a<"));
	}

	/** A list label's ascent joins the item's first line (CR-001 §6.10): a Symbol
	 *  bullet (ascent 11.06pt at 11pt) on Calibri (10.47 + 2.95) makes the label
	 *  block a 14.01pt box on an 11.06pt baseline, with the text line's 2.01pt of
	 *  leading; the body block learns the label's ascent for the line manager. */
	@Test
	public void listLabelAscentJoinsTheFirstLine() throws Exception {
		String body = "<fo:block docx4j-linebox=\"13.428pt\" docx4j-baseline=\"10.474pt\" docx4j-linerule=\"auto\""
				+ " font-family=\"Carlito Regular\" font-size=\"11pt\" line-height=\"15.442pt\">text</fo:block>";
		String in = flow("<fo:list-block><fo:list-item>"
				+ "<fo:list-item-label font-size=\"11pt\"><fo:block font-family=\"DejaVu Serif\" docx4j-font=\"Symbol\" line-height=\"15.497pt\">\u2022</fo:block></fo:list-item-label>"
				+ "<fo:list-item-body>" + body + "</fo:list-item-body>"
				+ "</fo:list-item></fo:list-block>");
		org.w3c.dom.Document doc = org.docx4j.XmlUtils.getNewDocumentBuilder().parse(
				new org.xml.sax.InputSource(new java.io.StringReader(in)));
		WordLayoutFixups.listLabelLines(doc);
		org.w3c.dom.NodeList blocks = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "block");
		org.w3c.dom.Element label = (org.w3c.dom.Element) blocks.item(0);
		org.w3c.dom.Element text = (org.w3c.dom.Element) blocks.item(1);
		assertEquals("14.01pt", label.getAttribute(WordLayoutFixups.HINT_LINE_BOX));
		assertEquals("11.06pt", label.getAttribute(WordLayoutFixups.HINT_BASELINE));
		assertEquals("auto", label.getAttribute(WordLayoutFixups.HINT_LINE_RULE));
		assertEquals("16.03pt", label.getAttribute("line-height"));
		assertEquals("11.06pt", text.getAttribute(WordLayoutFixups.HINT_LABEL_ASCENT));

		// a Courier New "o" (ascent 9.16pt) does not raise Calibri's line, and its
		// descent (3.30pt, more than Calibri's) is not counted either
		in = flow("<fo:list-block><fo:list-item>"
				+ "<fo:list-item-label font-size=\"11pt\"><fo:block font-family=\"Cousine\" docx4j-font=\"Courier New\" line-height=\"14.33pt\">o</fo:block></fo:list-item-label>"
				+ "<fo:list-item-body>" + body + "</fo:list-item-body>"
				+ "</fo:list-item></fo:list-block>");
		doc = org.docx4j.XmlUtils.getNewDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(in)));
		WordLayoutFixups.listLabelLines(doc);
		blocks = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "block");
		label = (org.w3c.dom.Element) blocks.item(0);
		assertEquals("13.43pt", label.getAttribute(WordLayoutFixups.HINT_LINE_BOX));
		assertEquals("10.47pt", label.getAttribute(WordLayoutFixups.HINT_BASELINE));
		assertEquals("15.44pt", label.getAttribute("line-height"));

		// through apply(): the hints become the extension's attributes or are stripped, never left as-is
		String out = WordLayoutFixups.apply(in, 15);
		assertFalse(out.contains("docx4j-label-ascent"));
		assertFalse(out.contains("docx4j-font"));
	}

	/**
	 * Word gives every paragraph a line at the paragraph mark's font and size, whatever
	 * its runs came to.  A paragraph whose runs produced no inline content - an empty
	 * w:t, or a run holding only an anchored picture, which is lifted into a positioned
	 * container - reached FOP as a block with nothing to put on a line, so it took no
	 * height at all.  Measured: three table spacer rows each holding one paragraph with
	 * an empty w:t lost 33.7pt of Word's row height, and a paragraph holding only a
	 * wrapNone anchored picture cost its whole 15.44pt line.
	 *
	 * @since 17.0.6
	 */
	@Test
	public void aParagraphWithNoInlineContentStillGetsALine() {
		String in = flow("<fo:block docx4j-pstyle=\"A\">one</fo:block>"
				+ "<fo:block docx4j-pstyle=\"A\"><fo:inline><fo:inline font-family=\"Tinos\"/></fo:inline></fo:block>"
				+ "<fo:block docx4j-pstyle=\"A\"><fo:block-container height=\"0pt\"><fo:block>x</fo:block></fo:block-container>"
				+ "<fo:inline/></fo:block>"
				+ "<fo:block docx4j-pstyle=\"A\">two</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertEquals("both empty paragraphs get the preserved space", 2,
				count(out, "white-space-treatment=\"preserve\""));
	}

	/** A paragraph which does produce content is left alone. */
	@Test
	public void aParagraphWithContentIsNotGivenAnExtraSpace() {
		String in = flow("<fo:block docx4j-pstyle=\"A\"><fo:inline>text</fo:inline></fo:block>"
				+ "<fo:block docx4j-pstyle=\"A\"><fo:inline><fo:external-graphic src=\"x.png\"/></fo:inline></fo:block>"
				+ "<fo:block docx4j-pstyle=\"A\"><fo:inline> </fo:inline></fo:block>"
				+ "<fo:block docx4j-pstyle=\"A\"><fo:inline><fo:leader/></fo:inline></fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertEquals(0, count(out, "white-space-treatment=\"preserve\""));
	}

	/**
	 * Inside a table, Word applies w:pageBreakBefore to the table: a break on the
	 * paragraph that opens the table starts the table on a new page, one anywhere else
	 * in it is ignored.  FOP breaks the table wherever it finds a break-before in a
	 * cell: a mail-merge template with sixteen of them spread over one table's rows
	 * came out as twelve pages against Word's four, while a report with one on the
	 * first paragraph of each of two tables has Word's five pages only because those
	 * two breaks are taken.
	 *
	 * @since 17.0.6
	 */
	@Test
	public void pageBreakBeforeInATableAppliesToTheTable() {
		String table = "<fo:table " + NS + "><fo:table-body>"
				+ "<fo:table-row><fo:table-cell>"
				+ "<fo:block docx4j-pstyle=\"A\" break-before=\"page\">first</fo:block>"
				+ "</fo:table-cell></fo:table-row>"
				+ "<fo:table-row><fo:table-cell>"
				+ "<fo:block docx4j-pstyle=\"A\" break-before=\"page\">later</fo:block>"
				+ "</fo:table-cell></fo:table-row>"
				+ "</fo:table-body></fo:table>";
		String out = WordLayoutFixups.apply(flow("<fo:block docx4j-pstyle=\"A\">before</fo:block>" + table), 15);
		assertEquals("the opening break moves to the table, the later one goes", 1,
				count(out, "break-before=\"page\""));
		assertTrue("the break must be on the fo:table",
				out.indexOf("break-before=\"page\"") < out.indexOf("<fo:table-body"));
		assertTrue(out.indexOf("break-before=\"page\"") > out.indexOf(">before<"));
	}

	/**
	 * A borders/shading container (Containerization) is built from its first paragraph's
	 * properties, spacing included.  Space is combined by "larger of", so that normally
	 * costs nothing - but where contextual spacing zeroes the paragraph's space-after,
	 * the wrapper's copy puts the gap back.  Measured: a planner whose shaded cells
	 * carry w:contextualSpacing with 10pt of docDefaults space-after had every row 9.5pt
	 * too tall, and 37 Word pages came out as 43.
	 *
	 * @since 17.0.6
	 */
	@Test
	public void containerWrapperFollowsItsParagraphsSpacing() {
		String wrapper = "<fo:block background-color=\"#eeeeee\" space-before=\"10pt\" space-after=\"10pt\">"
				+ "<fo:block docx4j-pstyle=\"A\" docx4j-contextual=\"1\" space-before=\"10pt\" space-after=\"10pt\">%s</fo:block>"
				+ "</fo:block>";
		String out = WordLayoutFixups.apply(flow(String.format(wrapper, "one") + String.format(wrapper, "two")), 15);

		int second = out.indexOf("<fo:block background-color", 1 + out.indexOf("<fo:block background-color"));
		String first = out.substring(out.indexOf("<fo:block background-color"), second);
		assertTrue("the wrapper keeps the first paragraph's space-before: " + first,
				first.contains("space-before=\"10pt\""));
		assertTrue("the wrapper must not keep a space-after the fixups removed: " + first,
				first.contains("space-after=\"0pt\""));
		String last = out.substring(second, out.indexOf("</fo:flow>"));
		assertTrue("nor a space-before: " + last, last.contains("space-before=\"0pt\""));
		assertTrue("but the last paragraph's space-after stands: " + last, last.contains("space-after=\"10pt\""));
	}

	// ------------------------------------------------------------------- empty cell

	/**
	 * An fo:table-cell must hold at least one block (content model
	 * marker* (%block;)+); FOP fails the whole export with "fo:table-cell is missing
	 * child elements" where it holds none.  A cell whose every paragraph is hidden text
	 * produces none - Word prints the row with the cell empty, its height coming from
	 * the other cells.  One document of a 103-document corpus has eleven such cells and
	 * lost its whole export to them.
	 *
	 * @since 17.0.6
	 */
	@Test
	public void anEmptyCellGetsABlock() {
		String table = "<fo:table " + NS + "><fo:table-body><fo:table-row>"
				+ "<fo:table-cell><fo:block>hello</fo:block></fo:table-cell>"
				+ "<fo:table-cell display-align=\"center\" padding-left=\"2.03mm\"/>"
				+ "</fo:table-row></fo:table-body></fo:table>";
		String out = WordLayoutFixups.apply(flow(table), 15);
		assertFalse("the empty cell is still empty: " + out, out.contains("padding-left=\"2.03mm\"/>"));
		assertEquals("one block per cell", 2, count(out, "<fo:block"));
	}

	@Test
	public void aCellHoldingOnlyAContainerIsLeftAlone() {
		String table = "<fo:table " + NS + "><fo:table-body><fo:table-row>"
				+ "<fo:table-cell><fo:block-container><fo:block>x</fo:block></fo:block-container></fo:table-cell>"
				+ "</fo:table-row></fo:table-body></fo:table>";
		String out = WordLayoutFixups.apply(flow(table), 15);
		assertEquals("a block-container is a %block; nothing to add", 1, count(out, "<fo:block>"));
	}

	// ------------------------------------------------------------------- text boxes

	/** An absolutely positioned text box, as FOTextBoxes writes it before the fixups. */
	private static String textBox(String blocks) {
		return "<fo:block " + NS + " text-align=\"right\" start-indent=\"20pt\" docx4j-pstyle=\"A\">"
				+ "<fo:block-container docx4j-anchor=\"none\" docx4j-anchor-w=\"180pt\""
				+ " docx4j-anchor-h=\"30pt\" docx4j-anchor-x=\"100pt\" docx4j-anchor-col=\"451pt\""
				+ " docx4j-anchor-ml=\"72pt\" docx4j-anchor-y=\"para:90pt\">"
				+ blocks + "</fo:block-container></fo:block>";
	}

	/**
	 * Word lays a text box out from the box's own edges: its paragraphs inherit neither
	 * the anchoring paragraph's w:jc nor its indents.  Measured on a 222-page letter
	 * whose letterhead box is anchored in a right-aligned cell paragraph: Word starts
	 * all seven of its lines at x=346.0, where ours ran from 312.4 to 438.9.
	 *
	 * @since 17.0.6
	 */
	@Test
	public void aTextBoxDoesNotInheritTheParagraphsAlignment() {
		String out = WordLayoutFixups.apply(flow(textBox("<fo:block>Service de l'informatique</fo:block>")), 15);
		int box = out.indexOf("absolute-position");
		assertTrue("the box was not positioned: " + out, box > 0);
		String container = out.substring(out.lastIndexOf("<fo:block-container", box), out.indexOf('>', box));
		assertTrue("the box must reset text-align: " + container, container.contains("text-align=\"start\""));
		assertTrue("and the indents: " + container, container.contains("start-indent=\"0pt\""));
	}

	/**
	 * Word paginates nothing inside a text box.  FOP, given break-before="page" inside
	 * an absolutely positioned container, paints only the last container of a run of
	 * them: measured on three boxes in zero-height wrappers, only the third was drawn,
	 * and without the breaks all three were.  A 335-page mail merge of 2345 boxes, every
	 * paragraph carrying w:pageBreakBefore, came out with one line a page against Word's
	 * nine.
	 *
	 * @since 17.0.6
	 */
	@Test
	public void aTextBoxIsNotPaginated() {
		String out = WordLayoutFixups.apply(flow(
				textBox("<fo:block break-before=\"page\" keep-with-next.within-page=\"always\">x</fo:block>")), 15);
		assertFalse("a page break inside a text box loses the box: " + out, out.contains("break-before"));
		assertFalse(out.contains("keep-with-next"));
	}

	// ------------------------------------------------------------------- flow start

	/**
	 * HTML auto spacing (w:beforeAutospacing) is a margin, and a margin collapses out at
	 * the top of the body: measured on a document whose first paragraph carries it,
	 * every line of page 1 was exactly +14.0pt (Word 73.5 / 86.7 / 98.2, docx4j 87.5 /
	 * 100.7 / 112.2).  An explicit w:spacing w:before is honoured there - the
	 * spacing-page-top probe measured 36pt on the first paragraph of a document - so
	 * only the automatic value goes.
	 *
	 * @since 17.0.6
	 */
	@Test
	public void autoSpaceBeforeGoesAtTheStartOfAFlow() {
		String out = WordLayoutFixups.apply(flow(
				"<fo:block docx4j-pstyle=\"A\" docx4j-autospacing=\"ba\" space-before=\"14pt\" space-after=\"14pt\">one</fo:block>"
				+ "<fo:block docx4j-pstyle=\"A\" docx4j-autospacing=\"ba\" space-before=\"14pt\">two</fo:block>"), 15);
		String first = out.substring(out.indexOf("<fo:block", out.indexOf("<fo:flow")), out.indexOf(">one<"));
		assertTrue("auto space-before must go at the top of the flow: " + first,
				first.contains("space-before=\"0pt\""));
		assertFalse("and must not be retained: " + first, first.contains("conditionality"));
	}

	@Test
	public void anExplicitSpaceBeforeIsStillRetainedAtTheStartOfAFlow() {
		String out = WordLayoutFixups.apply(flow(
				"<fo:block docx4j-pstyle=\"A\" space-before=\"36pt\">one</fo:block>"), 15);
		assertTrue(out.contains("space-before.conditionality=\"retain\""));
	}

	// ------------------------------------------------------------------- floats

	private static final String MASTERS =
			"<fo:layout-master-set><fo:simple-page-master master-name=\"m\" page-width=\"595.3pt\""
			+ " page-height=\"841.9pt\" margin-left=\"85.05pt\" margin-right=\"85.05pt\">"
			+ "<fo:region-body column-count=\"1\" margin-left=\"0mm\" margin-right=\"0mm\"/>"
			+ "</fo:simple-page-master></fo:layout-master-set>";

	/** A whole page-sequence, so that the fixups can work out the measure (425.2pt). */
	private static String page(String blocks) {
		return "<fo:root " + NS + ">" + MASTERS
				+ "<fo:page-sequence master-reference=\"m\"><fo:flow flow-name=\"xsl-region-body\">"
				+ blocks + "</fo:flow></fo:page-sequence></fo:root>";
	}

	/** FOP renders a float holding an fo:table at flow level as nothing at all
	 *  (measured), so such a float is never moved there. */
	@Test
	public void aFloatHoldingATableIsNeverHoisted() {
		String out = WordLayoutFixups.apply(page(
				"<fo:block><fo:float float=\"left\"><fo:block>"
				+ "<fo:table width=\"460pt\"><fo:table-body><fo:table-row><fo:table-cell>"
				+ "<fo:block>x</fo:block></fo:table-cell></fo:table-row></fo:table-body></fo:table>"
				+ "</fo:block></fo:float></fo:block>"
				+ "<fo:block><fo:inline><fo:block>y</fo:block></fo:inline></fo:block>"), 15);
		assertFalse("a float holding a table renders nothing at flow level: " + out,
				out.contains("<fo:flow flow-name=\"xsl-region-body\"><fo:float"));
	}

	/** Hoisting a float to flow level must never take it out of the cell, header or
	 *  footnote it belongs to - it would be painted somewhere else entirely. */
	@Test
	public void aFloatInACellIsNeverHoistedOutOfIt() {
		String out = WordLayoutFixups.apply(page(
				"<fo:table><fo:table-body><fo:table-row><fo:table-cell>"
				+ "<fo:block><fo:float float=\"left\"><fo:block>f</fo:block></fo:float></fo:block>"
				+ "</fo:table-cell></fo:table-row></fo:table-body></fo:table>"
				+ "<fo:block><fo:inline><fo:block>y</fo:block></fo:inline></fo:block>"), 15);
		assertTrue("the float must stay inside the cell: " + out,
				out.indexOf("<fo:float") > out.indexOf("<fo:table-cell"));
		assertFalse(out.contains("<fo:flow flow-name=\"xsl-region-body\"><fo:float"));
	}
}
