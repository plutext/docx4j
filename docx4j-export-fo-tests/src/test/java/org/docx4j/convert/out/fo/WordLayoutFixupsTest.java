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

	@Test
	public void wrappedPictureInATableCellIsLaidOutTopAndBottom() {
		String cell = "<fo:table " + NS + "><fo:table-body><fo:table-row><fo:table-cell>"
				+ anchored("square", "0", "p:0", "") + "</fo:table-cell></fo:table-row></fo:table-body></fo:table>";
		String out = WordLayoutFixups.apply(flow(cell), 15);
		assertFalse("FOP has no floats inside tables", out.contains("fo:float"));
		assertTrue(out.contains("<fo:block-container") && out.contains("height=\"85.04pt\""));
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
}
