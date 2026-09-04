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
	public void zeroSpaceNeedsNoRetain() {
		String in = flow("<fo:block space-before=\"0in\">one</fo:block>");
		assertFalse(WordLayoutFixups.apply(in, 15).contains("retain"));
	}

	private static int count(String s, String sub) {
		int n = 0, i = 0;
		while ((i = s.indexOf(sub, i)) >= 0) { n++; i += sub.length(); }
		return n;
	}
}
