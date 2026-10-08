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

	/**
	 * Consecutive page breaks each cost a page, so a break paragraph followed by another
	 * empty one keeps its own (page-empty: Word 13 pages, docx4j had 11).
	 */
	@Test
	public void consecutiveBreakParagraphsEachKeepAPage() {
		String in = flow("<fo:block>one</fo:block>"
				+ "<fo:block break-before=\"page\"/>"
				+ "<fo:block break-before=\"page\"/>"
				+ "<fo:block>two</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertEquals("two page boundaries", 2, count(out, "break-before=\"page\""));
	}

	/**
	 * But a w:pageBreakBefore paragraph after a page break is already at the top of a
	 * page and Word adds none for it: only an <em>empty</em> break block counts as a
	 * second break.
	 */
	@Test
	public void pageBreakBeforeParagraphAfterABreakCostsNoPage() {
		String in = flow("<fo:block>one</fo:block>"
				+ "<fo:block break-before=\"page\"/>"
				+ "<fo:block break-before=\"page\">a heading whose style breaks before</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertEquals("one page boundary", 1, count(out, "break-before=\"page\""));
	}

	private static String listBlock(String breakBefore, String text) {
		return "<fo:list-block" + breakBefore + "><fo:list-item>"
				+ "<fo:list-item-label end-indent=\"label-end()\"><fo:block>2.</fo:block></fo:list-item-label>"
				+ "<fo:list-item-body start-indent=\"body-start()\"><fo:block>" + text + "</fo:block></fo:list-item-body>"
				+ "</fo:list-item></fo:list-block>";
	}

	/** The same for a numbered heading, which is an fo:list-block: corpus document 13347 had
	 *  a break-only paragraph and then "2. processen" in a style breaking before, and an empty
	 *  page between, which Word does not have.  @since 17.3.1 */
	@Test
	public void pageBreakBeforeListBlockAfterABreakCostsNoPage() {
		String in = flow("<fo:block>one</fo:block>"
				+ "<fo:block break-before=\"page\" white-space-treatment=\"preserve\"> </fo:block>"
				+ listBlock(" break-before=\"page\"", "a numbered heading whose style breaks before"));
		String out = WordLayoutFixups.apply(in, 15);
		assertEquals("one page boundary", 1, count(out, "break-before=\"page\""));
		assertFalse("the empty break block is gone", out.contains("white-space-treatment=\"preserve\"> </fo:block>"));
	}

	/** And a numbered paragraph which does not break takes the break, with no empty line
	 *  above it: the mark Word moves past the break takes no line there.  @since 17.3.1 */
	@Test
	public void aListBlockAfterABreakTakesIt() {
		String in = flow("<fo:block>one</fo:block>"
				+ "<fo:block break-before=\"page\"/>"
				+ listBlock("", "a numbered paragraph"));
		String out = WordLayoutFixups.apply(in, 15);
		assertEquals("one page boundary", 1, count(out, "break-before=\"page\""));
		int brk = out.indexOf("break-before=\"page\"");
		assertTrue("on the list-block", out.lastIndexOf("<fo:list-block", brk) > out.lastIndexOf("<fo:block>one", brk));
	}

	/** In compatibility mode 11 the mark keeps its line above a numbered paragraph (corpus
	 *  document 11657), so the empty block stays and keeps the break.  @since 17.3.1 */
	@Test
	public void aListBlockAfterABreakInMode11LeavesTheMarksLine() {
		String in = flow("<fo:block>one</fo:block>"
				+ "<fo:block break-before=\"page\"/>"
				+ listBlock("", "a numbered paragraph"));
		String out = WordLayoutFixups.apply(in, 11);
		assertEquals("one page boundary", 1, count(out, "break-before=\"page\""));
		int brk = out.indexOf("break-before=\"page\"");
		assertTrue("on the empty block, before the list-block", brk < out.indexOf("<fo:list-block"));
	}

	/** A paragraph after a break-only paragraph takes the break where the mark takes no line
	 *  (mode 15, the flag unstated).  @since 17.3.1 */
	@Test
	public void aBlockAfterABreakTakesIt() {
		String in = flow("<fo:block>one</fo:block>"
				+ "<fo:block break-before=\"page\" space-after=\"9pt\" white-space-treatment=\"preserve\"> </fo:block>"
				+ "<fo:block>Table des mati\u00e8res</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertEquals("one page boundary", 1, count(out, "break-before=\"page\""));
		assertFalse("the mark's block is gone", out.contains("space-after=\"9pt\""));
	}

	/** Where the mark takes a line at the top of the next page - mode 11 (corpus document
	 *  11657, its contents title 23pt down: the Normal mark's 14pt line and 9pt after) - the
	 *  empty block keeps the break and its line.  @since 17.3.1 */
	@Test
	public void aBlockAfterABreakInMode11LeavesTheMarksLine() {
		String in = flow("<fo:block>one</fo:block>"
				+ "<fo:block break-before=\"page\" space-after=\"9pt\"/>"
				+ "<fo:block>Table des mati\u00e8res</fo:block>");
		String out = WordLayoutFixups.apply(in, 11);
		assertEquals("one page boundary", 1, count(out, "break-before=\"page\""));
		int brk = out.indexOf("break-before=\"page\"");
		assertTrue("on the mark's block, before the title", brk < out.indexOf("Table des"));
		assertTrue("which has a line to give: " + out, out.contains("white-space-treatment=\"preserve\""));
	}

	/** ...and in any mode where the document states w:splitPgBreakAndParaMark (corpus
	 *  document 6693, mode 14).  @since 17.3.1 */
	@Test
	public void aBlockAfterABreakWhereTheFlagIsStatedLeavesTheMarksLine() throws Exception {
		org.docx4j.openpackaging.packages.WordprocessingMLPackage pkg
				= org.docx4j.openpackaging.packages.WordprocessingMLPackage.createPackage();
		org.docx4j.wml.CTSettings settings = pkg.getMainDocumentPart().getDocumentSettingsPart().getJaxbElement();
		if (settings.getCompat() == null) settings.setCompat(new org.docx4j.wml.CTCompat());
		settings.getCompat().setSplitPgBreakAndParaMark(new org.docx4j.wml.BooleanDefaultTrue());
		org.docx4j.model.CompatibilityOptions compat = org.docx4j.model.CompatibilityOptions.of(pkg);
		assertTrue("mode 15", compat.mode() == 15);
		String in = flow("<fo:block>one</fo:block>"
				+ "<fo:block break-before=\"page\" space-after=\"9pt\" white-space-treatment=\"preserve\"> </fo:block>"
				+ "<fo:block>ANNEXE I</fo:block>");
		String out = WordLayoutFixups.apply(in, compat, null);
		int brk = out.indexOf("break-before=\"page\"");
		assertTrue("the break stays on the mark's block: " + out, brk >= 0 && brk < out.indexOf("ANNEXE"));
		assertEquals("one page boundary", 1, count(out, "break-before=\"page\""));
	}

	/** Where a one-column stretch (a span="all" wrapper) meets the columns after it, the
	 *  space-after of its last paragraph and the space-before of the first block after it are
	 *  kept, as Word keeps them: FOP discards space at a span boundary (E21; corpus document
	 *  177, every column-2 line 29.7pt high).  @since 17.3.1 */
	@Test
	public void spaceAtASpanBoundaryIsRetained() {
		String in = flow("<fo:block span=\"all\"><fo:block>Title</fo:block>"
				+ "<fo:block space-after=\"8pt\" white-space-treatment=\"preserve\"> </fo:block></fo:block>"
				+ "<fo:block space-before=\"4.3pt\">In columns</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertTrue("the wrapper's last space-after: " + out, out.contains("space-after=\"8pt\" space-after.conditionality=\"retain\"")
				|| out.contains("space-after.conditionality=\"retain\" space-after=\"8pt\""));
		assertTrue("the next block's space-before: " + out, out.contains("space-before.conditionality=\"retain\""));
	}

	/** ...but not where the wrapper ends in a paragraph whose runs paint nothing - an INDEX
	 *  field's code with the section break, to which Word gives neither a line nor its space
	 *  (corpus documents 13459, 8695).  @since 17.3.1 */
	@Test
	public void aFieldCodeParagraphEndingASpanKeepsNoSpace() {
		String in = flow("<fo:block span=\"all\"><fo:block>Index</fo:block>"
				+ "<fo:block space-after=\"10pt\" white-space-treatment=\"preserve\"><fo:inline> </fo:inline></fo:block></fo:block>"
				+ "<fo:block>#</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertFalse("no retain on the field-code paragraph: " + out, out.contains("space-after.conditionality=\"retain\""));
	}

	private static String border(String edge, String w) {
		return " border-" + edge + "-style=\"solid\" border-" + edge + "-width=\"" + w + "\" border-" + edge + "-color=\"#000000\"";
	}

	private static String borderedTable(String tableAttrs, String row1Attrs, String cellTop, String row2Attrs, String cellBottom) {
		return "<fo:table border-collapse=\"collapse\" table-layout=\"fixed\" width=\"200pt\"" + tableAttrs + ">"
				+ "<fo:table-column column-width=\"200pt\"/><fo:table-body>"
				+ "<fo:table-row" + row1Attrs + "><fo:table-cell" + border("top", cellTop) + "><fo:block>row 1</fo:block></fo:table-cell></fo:table-row>"
				+ "<fo:table-row" + row2Attrs + "><fo:table-cell" + border("bottom", cellBottom) + "><fo:block>row 2</fo:block></fo:table-cell></fo:table-row>"
				+ "</fo:table-body></fo:table>";
	}

	/** Word stacks the whole of a table's outer top and bottom borders outside its rows, FOP's
	 *  collapsing model half: the table goes in a block padded by half of each, so it and its
	 *  border start that much lower (the table-outer-border-stack probe; corpus 9919).  The
	 *  table's page break moves to the block, ahead of the padding.  @since 17.3.1 */
	@Test
	public void aBorderedTableStandsInABlockPaddedByHalfItsOuterBorders() {
		String in = flow("<fo:block>before</fo:block>"
				+ borderedTable(border("top", "2.88pt") + border("bottom", "2.88pt") + " break-before=\"page\"", "", "2.88pt", "", "2.88pt")
				+ "<fo:block>after</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		int table = out.indexOf("<fo:table ");
		String block = out.substring(out.lastIndexOf("<fo:block ", table), table);
		assertTrue("a padded block around the table, with its break: " + out, block.endsWith(">")
				&& block.contains("padding-top=\"1.44pt\"") && block.contains("padding-bottom=\"1.44pt\"")
				&& block.contains("break-before=\"page\""));
		assertEquals("one break, on the block: " + out, 1, count(out, "break-before=\"page\""));
		assertTrue("the table closes the block: " + out, out.contains("</fo:table></fo:block><fo:block>after"));
	}

	/** The collapsed width is the widest of the table's border and its cells', at each edge.
	 *  @since 17.3.1 */
	@Test
	public void theOuterBorderIsTheWiderOfTableAndCell() {
		String out = WordLayoutFixups.apply(flow(borderedTable(border("top", "0.5pt") + border("bottom", "1pt"), "", "1.5pt", "", "0.5pt")), 15);
		assertTrue("top: the cell's 1.5pt; bottom: the table's 1pt: " + out,
				out.contains("padding-top=\"0.75pt\"") && out.contains("padding-bottom=\"0.5pt\""));
	}

	/** An exact row's height is the whole row, borders included, in Word, and FOP's pitch
	 *  already agrees: no padding at that edge; an at-least row is padded.  @since 17.3.1 */
	@Test
	public void anExactRowTakesNoPaddingAtItsEdge() {
		String out = WordLayoutFixups.apply(flow(borderedTable(border("top", "1pt") + border("bottom", "1pt"),
				" docx4j-row-exact=\"20pt\"", "1pt", "", "1pt")), 15);
		assertFalse("no top padding over an exact first row: " + out, out.contains("padding-top=\"0.5pt\""));
		assertTrue("the auto last row's: " + out, out.contains("padding-bottom=\"0.5pt\""));
		// a row at least a height is padded as an auto-height one is (table-outer-border-atleast)
		out = WordLayoutFixups.apply(flow(borderedTable(border("top", "1pt") + border("bottom", "1pt"),
				"", "1pt", " height=\"15pt\"", "1pt")), 15);
		assertTrue("the auto first row's: " + out, out.contains("padding-top=\"0.5pt\""));
		assertTrue("the at-least last row's: " + out, out.contains("padding-bottom=\"0.5pt\""));
	}

	/** A table in the separate model keeps its borders inside it, and one with no border
	 *  drawn at either edge is left where it is.  @since 17.3.1 */
	@Test
	public void separateAndBorderlessTablesAreLeftAlone() {
		String separate = flow(borderedTable(border("top", "1pt") + border("bottom", "1pt"), "", "1pt", "", "1pt")
				.replace("border-collapse=\"collapse\"", "border-collapse=\"separate\""));
		assertFalse(WordLayoutFixups.apply(separate, 15).contains("padding-top"));
		String none = flow(borderedTable("", "", "0pt", "", "0pt").replace("solid", "none"));
		assertFalse(WordLayoutFixups.apply(none, 15).contains("padding-"));
	}

	/** A break-only paragraph ending the document gives Word no further page from mode 12:
	 *  the mark after the break is the document's last line, on the page before it (the five
	 *  document-end-break probes in mode 15, and the pagebreak-paragraph probes' case F in
	 *  modes 12, 14 and 15).  The break goes, and the empty block with it.  @since 17.3.1 */
	@Test
	public void aBreakEndingTheDocumentCostsNoPage() {
		String in = flow("<fo:block>one</fo:block>"
				+ "<fo:block break-before=\"page\" white-space-treatment=\"preserve\"> </fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertEquals("no page boundary", 0, count(out, "break-before=\"page\""));
		assertFalse("the empty break block is gone", out.contains("white-space-treatment=\"preserve\"> </fo:block>"));
		assertTrue(out.contains(">one<"));
	}

	/** Below mode 12 Word gives the mark moved past a break a line on the next page (corpus
	 *  document 11657), so a break ending the document keeps its page.  @since 17.3.1 */
	@Test
	public void aBreakEndingTheDocumentInMode11KeepsItsPage() {
		String in = flow("<fo:block>one</fo:block>"
				+ "<fo:block break-before=\"page\" white-space-treatment=\"preserve\"> </fo:block>");
		String out = WordLayoutFixups.apply(in, 11);
		assertEquals("one page boundary", 1, count(out, "break-before=\"page\""));
	}

	/** A break paragraph which opens the flow keeps its page: the section break has
	 *  already started one, so the break makes another. */
	@Test
	public void breakParagraphOpeningAFlowKeepsItsPage() {
		String in = flow("<fo:block break-before=\"page\"/><fo:block>two</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertEquals("two page boundaries", 2, count(out, "break-before=\"page\""));
	}

	/**
	 * A picture-only paragraph's line is the picture, plus (multiple - 1) x the
	 * paragraph font's natural pitch - not (multiple - 1) x the picture (&#xa7;2.4).
	 * Measured on picture-header-cell: an 11pt paragraph (pitch 13.428pt) at
	 * w:line=276 auto with a 24pt picture has a 26.0pt line, not 24.0 and not 27.6.
	 */
	@Test
	public void pictureLineTakesTheMultiplesOwnLeading() {
		String in = flow("<fo:block docx4j-pstyle=\"\" docx4j-linebox=\"13.428pt\""
				+ " docx4j-baseline=\"10.474pt\" docx4j-linerule=\"auto\" line-height=\"15.442pt\">"
				+ "<fo:external-graphic content-height=\"24pt\" content-width=\"60pt\" src=\"x.png\"/>"
				+ "</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		// the line box is the picture (that is what says the line has no descent)
		assertTrue("line box is not the picture: " + out, out.contains("line-box=\"24pt\""));
		// and the line-height carries the picture plus 0.15 x 13.428 = 26.01pt
		assertTrue("the multiple's own leading was not added: " + out,
				out.contains("line-height=\"26.014pt\"") || out.contains("line-height=\"26.01pt\""));
	}

	/** A single-spaced picture-only paragraph gets no leading at all. */
	@Test
	public void singleSpacedPictureLineTakesNoLeading() {
		String in = flow("<fo:block docx4j-pstyle=\"\" docx4j-linebox=\"13.428pt\""
				+ " docx4j-baseline=\"10.474pt\" docx4j-linerule=\"auto\" line-height=\"13.428pt\">"
				+ "<fo:external-graphic content-height=\"24pt\" content-width=\"60pt\" src=\"x.png\"/>"
				+ "</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertTrue(out.contains("line-box=\"24pt\""));
		assertTrue("the line-height was raised: " + out, out.contains("line-height=\"13.428pt\""));
	}

	/**
	 * The paragraph a page break moves onto is first on its page and loses its
	 * space-before, in every compatibility mode.  Until 17.2.0 this asserted the
	 * opposite below mode 15, on the reading that w:suppressSpBfAfterPgBrk decided it;
	 * the four page-top-space-before goldens (CR-001 batch 43, M30) are identical across
	 * modes 12, 14 and 15 and with the flag stated, and Word drops the space in all of
	 * them - the heading's first line lands 2.16pt below an ordinary one, which is its
	 * border and the border's space, not 24pt of space-before.
	 */
	@Test
	public void pageBreakParagraphLosesSpaceBefore_everyMode() {
		String in = flow("<fo:block>one</fo:block>"
				+ "<fo:block break-before=\"page\"> </fo:block>"
				+ "<fo:block space-before=\"36pt\">two</fo:block>");
		for (int mode : new int[] { 12, 14, 15 }) {
			String out = WordLayoutFixups.apply(in, mode);
			assertTrue("mode " + mode + ": the break moved onto the next block: " + out,
					out.contains("break-before=\"page\"") && out.contains(">two<"));
			assertEquals("mode " + mode + ": nothing retains space-before after the break: "
					+ out, 0, count(out, "space-before.conditionality=\"retain\""));
		}
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
		// Word keeps a cell's last paragraph's space-after below mode 15 too: measured on
		// a mode-14 document whose cell paragraphs carry w:before=60 w:after=60, Word's
		// row pitch is 3 + 11.5 + 3 = 18.0pt where ours was 15.0.  @since 17.1.0
		assertEquals("bottom spacing in cells applies below mode 15 too", 1,
				count(out14, "space-after.conditionality=\"retain\""));
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

	/**
	 * Word's exact row height is the whole row, borders included, where FOP reads
	 * {@code height} as the cell's content height and adds the border it charges the
	 * cell on top - half of each collapsed border, all of a separate one.  So the
	 * height comes down by that allowance.  @since 17.1.0
	 */
	@Test
	public void exactRowHeightAllowsForTheBorderFopAdds() {
		String collapsed = flow("<fo:table border-collapse=\"collapse\"><fo:table-body>"
				+ "<fo:table-row height=\"20pt\" docx4j-row-exact=\"20pt\">"
				+ "<fo:table-cell border-top-width=\"0.5pt\" border-bottom-width=\"0.5pt\">"
				+ "<fo:block>a</fo:block></fo:table-cell>"
				+ "</fo:table-row></fo:table-body></fo:table>");
		String out = WordLayoutFixups.apply(collapsed, 15);
		assertTrue(out, out.contains("height=\"19.5pt\""));
		assertTrue(out, out.contains("block-progression-dimension=\"19.5pt\""));

		String separate = flow("<fo:table border-collapse=\"separate\"><fo:table-body>"
				+ "<fo:table-row height=\"20pt\" docx4j-row-exact=\"20pt\">"
				+ "<fo:table-cell border-top-width=\"0.5pt\" border-bottom-width=\"0.5pt\" padding-top=\"1pt\">"
				+ "<fo:block>a</fo:block></fo:table-cell>"
				+ "</fo:table-row></fo:table-body></fo:table>");
		out = WordLayoutFixups.apply(separate, 15);
		assertTrue(out, out.contains("height=\"19pt\""));
		assertTrue(out, out.contains("block-progression-dimension=\"18pt\""));
	}

	@Test
	public void zeroSpaceNeedsNoRetain() {
		String in = flow("<fo:block space-before=\"0in\">one</fo:block>");
		assertFalse(WordLayoutFixups.apply(in, 15).contains("retain"));
	}

	/** Where the renderer has fox:continuation-display-align (fork CR-013), a centred or
	 *  bottom-aligned cell's later parts are laid out from the top, as Word lays them out;
	 *  a top-aligned cell needs nothing, and without the hook nothing is written.
	 *  @since 17.3.1 */
	@Test
	public void brokenCellsContinueFromTheTop() throws Exception {
		String fo = flow("<fo:table><fo:table-body><fo:table-row>"
				+ "<fo:table-cell display-align=\"after\"><fo:block>a</fo:block></fo:table-cell>"
				+ "<fo:table-cell display-align=\"center\"><fo:block>b</fo:block></fo:table-cell>"
				+ "<fo:table-cell display-align=\"before\"><fo:block>c</fo:block></fo:table-cell>"
				+ "</fo:table-row></fo:table-body></fo:table>");
		String fox = org.docx4j.fonts.RunFontSelector.FOX_NS;

		org.w3c.dom.Document doc = parse(fo);
		WordLayoutFixups.continuationFromTop(doc, true);
		org.w3c.dom.NodeList cells = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "table-cell");
		assertEquals("after", "before", ((org.w3c.dom.Element) cells.item(0)).getAttributeNS(fox, "continuation-display-align"));
		assertEquals("center", "before", ((org.w3c.dom.Element) cells.item(1)).getAttributeNS(fox, "continuation-display-align"));
		assertFalse("before", ((org.w3c.dom.Element) cells.item(2)).hasAttributeNS(fox, "continuation-display-align"));

		doc = parse(fo);
		WordLayoutFixups.continuationFromTop(doc, false);
		cells = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "table-cell");
		assertFalse("no hook", ((org.w3c.dom.Element) cells.item(0)).hasAttributeNS(fox, "continuation-display-align"));
	}

	/**
	 * A numbered paragraph opening the document keeps its space-before, as a plain one does:
	 * its spacing is on the item body's block until fixLists moves it to the list-block, and
	 * the retain has to travel with it.  Corpus document 12301 (a numbered TOC Heading, 24pt
	 * before): Word's first line at 129.1, docx4j's at 103.5.  @since 17.3.1
	 */
	@Test
	public void numberedFirstParagraphRetainsItsSpaceBefore() throws Exception {
		String in = flow("<fo:list-block provisional-distance-between-starts=\"21.6pt\"><fo:list-item>"
				+ "<fo:list-item-label end-indent=\"label-end()\"><fo:block>1</fo:block></fo:list-item-label>"
				+ "<fo:list-item-body start-indent=\"body-start()\"><fo:block space-before=\"24pt\">Contents</fo:block>"
				+ "</fo:list-item-body></fo:list-item></fo:list-block>"
				+ "<fo:block space-before=\"12pt\">after</fo:block>");
		org.w3c.dom.Document doc = parse(WordLayoutFixups.apply(in, 15));
		org.w3c.dom.Element listBlock = (org.w3c.dom.Element) doc.getElementsByTagNameNS(
				"http://www.w3.org/1999/XSL/Format", "list-block").item(0);
		assertEquals("24pt", listBlock.getAttribute("space-before"));
		assertEquals("retain", listBlock.getAttribute("space-before.conditionality"));
	}

	/**
	 * The collapsed border FOP charges a cell is the wider of its own and its neighbour's, so
	 * that is what cellLineWidth gives back: table-cell-measure-neighbour's right cell, with no
	 * left border of its own beside a 1pt one, keeps Word's measure (17.3.1).
	 */
	@Test
	public void aNeighboursCollapsedBorderIsGivenBack() throws Exception {
		String cell = "<fo:table-cell padding-left=\"5.4pt\" padding-right=\"5.4pt\" %s><fo:block>x</fo:block></fo:table-cell>";
		String fo = flow("<fo:table border-collapse=\"collapse\"><fo:table-body><fo:table-row>"
				+ String.format(cell, "border-right-style=\"solid\" border-right-width=\"1pt\"")
				+ String.format(cell, "border-left-style=\"none\" border-left-width=\"0.48pt\"")
				+ "</fo:table-row></fo:table-body></fo:table>");
		org.w3c.dom.Document doc = parse(fo);
		WordLayoutFixups.cellLineWidth(doc);
		org.w3c.dom.NodeList cells = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "table-cell");
		assertEquals("its own 1pt", "4.9pt", ((org.w3c.dom.Element) cells.item(0)).getAttribute("padding-right"));
		assertEquals("the neighbour's 1pt, its own being none", "4.9pt",
				((org.w3c.dom.Element) cells.item(1)).getAttribute("padding-right"));
	}

	private static org.w3c.dom.Document parse(String fo) throws Exception {
		return org.docx4j.XmlUtils.getNewDocumentBuilder().parse(
				new org.xml.sax.InputSource(new java.io.StringReader(fo)));
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
		// the vertical offset: the float's own on a renderer with float-offset (fork CR-023,
		// 2.11-docx4j.6), else padding inside the float
		if (org.docx4j.convert.out.fo.FopCapabilities.has(
				org.docx4j.convert.out.fo.FopCapabilities.Capability.FLOAT_OFFSET)) {
			assertTrue("vertical offset from the paragraph top", out.contains("float-offset=\"36pt\""));
			assertFalse(out.contains("padding-top=\"36pt\""));
		} else {
			assertTrue("vertical offset from the paragraph top", out.contains("padding-top=\"36pt\""));
		}
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

	// ---- page breaks in list items (§3.3)

	private static String listItem(String bodyBlock) {
		return "<fo:list-block space-before=\"8pt\"><fo:list-item><fo:list-item-label><fo:block>1.</fo:block></fo:list-item-label>"
				+ "<fo:list-item-body>" + bodyBlock + "</fo:list-item-body></fo:list-item></fo:list-block>";
	}

	/** A hard break (w:br at the head of the paragraph, which arrives as the paragraph's own
	 *  w:pageBreakBefore) moves to the list-block and keeps the item's space-before at the
	 *  top of the page: Word measured at the top margin + 18pt + ascent. */
	@Test
	public void hardBreakInAListItemMovesToTheListBlockAndKeepsItsSpace() {
		String in = flow("<fo:block>one</fo:block>"
				+ listItem("<fo:block docx4j-pstyle=\"Heading1\" docx4j-list=\"1\" docx4j-break-direct=\"1\" break-before=\"page\">heading</fo:block>"));
		String out = WordLayoutFixups.apply(in, 15);
		String lb = out.substring(out.indexOf("<fo:list-block"), out.indexOf(">", out.indexOf("<fo:list-block")));
		assertTrue("break not moved to the list-block: " + lb, lb.contains("break-before=\"page\""));
		assertTrue("space-before not retained after a hard break: " + lb, lb.contains("space-before.conditionality=\"retain\""));
		assertEquals("the break must not stay on the inner block too", 1, count(out, "break-before=\"page\""));
		assertFalse("hint must not reach FOP", out.contains("docx4j-break-direct"));
	}

	/** A style's w:pageBreakBefore is not a hard break: the break still moves to the
	 *  list-block, but the space-before is discarded at the top of the page, as Word does
	 *  in every mode (mode-12 chapter headings at y=94.8 where ours sat at 102.8). */
	@Test
	public void styleBreakInAListItemGetsNoSpaceAtThePageTop() {
		String in = flow("<fo:block>one</fo:block>"
				+ listItem("<fo:block docx4j-pstyle=\"Heading1\" docx4j-list=\"1\" break-before=\"page\">heading</fo:block>"));
		String out = WordLayoutFixups.apply(in, 12);
		String lb = out.substring(out.indexOf("<fo:list-block"), out.indexOf(">", out.indexOf("<fo:list-block")));
		assertTrue("break not moved to the list-block: " + lb, lb.contains("break-before=\"page\""));
		assertFalse("a style's break must not retain the space: " + lb, lb.contains("space-before.conditionality=\"retain\""));
	}

	/** A column break is always a hard break (there is no paragraph property for one),
	 *  and keeps the item's space at the top of its column. */
	@Test
	public void columnBreakInAListItemKeepsItsSpace() {
		String in = flow("<fo:block>one</fo:block>"
				+ listItem("<fo:block docx4j-pstyle=\"Heading1\" docx4j-list=\"1\" break-before=\"column\">heading</fo:block>"));
		String out = WordLayoutFixups.apply(in, 15);
		String lb = out.substring(out.indexOf("<fo:list-block"), out.indexOf(">", out.indexOf("<fo:list-block")));
		assertTrue(lb, lb.contains("break-before=\"column\"") && lb.contains("space-before.conditionality=\"retain\""));
	}

	// ---- STYLEREF markers (§7)

	/** A retrieve-marker in the header asks for markers on every flow block of that
	 *  style: text markers, and label markers for the \n form; never in static content. */
	@Test
	public void styleRefMarkersOnEveryBlockOfTheStyle() {
		String in = "<fo:root " + NS + "><fo:page-sequence>"
				+ "<fo:static-content flow-name=\"xsl-region-before\"><fo:block docx4j-pstyle=\"Heading1\">"
				+ "<fo:inline><fo:retrieve-marker retrieve-class-name=\"docx4j-styleref-Heading1\" retrieve-position=\"first-starting-within-page\" retrieve-boundary=\"document\"/></fo:inline>"
				+ "<fo:retrieve-marker retrieve-class-name=\"docx4j-styleref-Heading1-n\" retrieve-position=\"first-starting-within-page\" retrieve-boundary=\"document\"/>"
				+ "</fo:block></fo:static-content>"
				+ "<fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:block docx4j-pstyle=\"Heading1\"><fo:inline>Scope</fo:inline><fo:footnote><fo:inline>1</fo:inline><fo:footnote-body><fo:block>note</fo:block></fo:footnote-body></fo:footnote></fo:block>"
				+ "<fo:block docx4j-pstyle=\"Normal\">body</fo:block>"
				+ "<fo:list-block><fo:list-item><fo:list-item-label><fo:block>2.</fo:block></fo:list-item-label>"
				+ "<fo:list-item-body><fo:block docx4j-pstyle=\"Heading1\" docx4j-list=\"1\">Terms</fo:block></fo:list-item-body></fo:list-item></fo:list-block>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
		String out = WordLayoutFixups.apply(in, 15);
		assertEquals("two text markers and two label markers", 4, count(out, "<fo:marker "));
		assertTrue(out.contains("marker-class-name=\"docx4j-styleref-Heading1\">Scope</fo:marker>"));
		assertTrue("the footnote's text is not the heading's: " + out, !out.contains("Scope note"));
		assertTrue(out.contains("marker-class-name=\"docx4j-styleref-Heading1\">Terms</fo:marker>"));
		assertTrue("the label marker carries the number", out.contains("marker-class-name=\"docx4j-styleref-Heading1-n\">2.</fo:marker>"));
		assertTrue("an unnumbered heading has an empty label marker", out.contains("marker-class-name=\"docx4j-styleref-Heading1-n\"/>")
				|| out.contains("marker-class-name=\"docx4j-styleref-Heading1-n\"></fo:marker>"));
		assertTrue("no marker in the static content", out.indexOf("<fo:marker ") > out.indexOf("</fo:static-content>"));
		assertTrue("the marker must be the block's initial child",
				out.contains("<fo:block><fo:marker ") || out.contains("<fo:block docx4j-list") || out.indexOf("<fo:marker ") < out.indexOf("Scope"));
		assertFalse("no hint left for FOP", out.contains("docx4j-pstyle"));
	}

	/** A cover title in a text box in a table cell is what Word's STYLEREF finds; only
	 *  static content and footnotes are out of bounds. */
	@Test
	public void styleRefMarkerInATextBoxInACell() {
		String in = "<fo:root " + NS + "><fo:page-sequence>"
				+ "<fo:static-content flow-name=\"xsl-region-before\"><fo:block>"
				+ "<fo:retrieve-marker retrieve-class-name=\"docx4j-styleref-Title1frontpage\" retrieve-position=\"first-starting-within-page\" retrieve-boundary=\"document\"/>"
				+ "</fo:block></fo:static-content>"
				+ "<fo:flow flow-name=\"xsl-region-body\">"
				+ "<fo:table><fo:table-body><fo:table-row><fo:table-cell><fo:block-container>"
				+ "<fo:block docx4j-pstyle=\"Title1frontpage\">Release Notes</fo:block>"
				+ "</fo:block-container></fo:table-cell></fo:table-row></fo:table-body></fo:table>"
				+ "<fo:block docx4j-pstyle=\"Normal\">body<fo:footnote><fo:inline>1</fo:inline><fo:footnote-body>"
				+ "<fo:block docx4j-pstyle=\"Title1frontpage\">not this</fo:block></fo:footnote-body></fo:footnote></fo:block>"
				+ "</fo:flow></fo:page-sequence></fo:root>";
		String out = WordLayoutFixups.apply(in, 15);
		assertEquals("the title in the cell's text box, not the footnote's", 1, count(out, "<fo:marker "));
		assertTrue(out.contains("marker-class-name=\"docx4j-styleref-Title1frontpage\">Release Notes</fo:marker>"));
	}

	/** The space in front of the field survives, as it does in front of a page number. */
	@Test
	public void spaceBeforeAStyleRefSurvives() {
		String in = "<fo:root " + NS + "><fo:page-sequence>"
				+ "<fo:static-content flow-name=\"xsl-region-before\"><fo:block>"
				+ "<fo:inline>Extarct Tool - HLD </fo:inline><fo:inline><fo:retrieve-marker retrieve-class-name=\"docx4j-styleref-Document_Version_Number\" retrieve-position=\"first-starting-within-page\" retrieve-boundary=\"document\"/></fo:inline>"
				+ "</fo:block></fo:static-content>"
				+ "<fo:flow flow-name=\"xsl-region-body\"><fo:block docx4j-pstyle=\"Document_Version_Number\">1.1</fo:block></fo:flow></fo:page-sequence></fo:root>";
		String out = WordLayoutFixups.apply(in, 15);
		assertTrue("zero-width space after the trailing space: " + out, out.contains("HLD \u200b</fo:inline>"));
	}

	// ---- columns and span-all parts (merged continuous sections)

	/** FOP loses a break-before on the first block inside a span="all" wrapper (the
	 *  narrower part of a merged continuous run, §7): a chapter heading which opens such
	 *  a part started mid-page.  The break moves onto the wrapper, where FOP takes it. */
	@Test
	public void aBreakOnTheFirstBlockOfASpanWrapperMovesToTheWrapper() {
		String in = flow("<fo:block>two columns</fo:block>"
				+ "<fo:block span=\"all\">"
				+ "<fo:block break-before=\"page\">1. Scope</fo:block>"
				+ "<fo:block>body</fo:block>"
				+ "</fo:block>");
		String out = WordLayoutFixups.apply(in, 12);
		int w = out.indexOf("span=\"all\"");
		String wrapper = out.substring(out.lastIndexOf("<fo:block", w), out.indexOf(">", w));
		assertTrue("break not on the wrapper: " + wrapper, wrapper.contains("break-before=\"page\""));
		assertEquals("one break in all", 1, count(out, "break-before=\"page\""));
		assertTrue(out.contains(">1. Scope<"));
	}

	/** The same where the part opens with a numbered heading, whose break
	 *  listItemPageBreaks has just moved onto its fo:list-block. */
	@Test
	public void aListBlockBreakAtTheHeadOfASpanWrapperMovesToTheWrapper() {
		String in = flow("<fo:block>two columns</fo:block>"
				+ "<fo:block span=\"all\">"
				+ listItem("<fo:block docx4j-pstyle=\"Heading1\" docx4j-list=\"1\" break-before=\"page\">Scope</fo:block>")
				+ "<fo:block>body</fo:block>"
				+ "</fo:block>");
		String out = WordLayoutFixups.apply(in, 12);
		int w = out.indexOf("span=\"all\"");
		String wrapper = out.substring(out.lastIndexOf("<fo:block", w), out.indexOf(">", w));
		assertTrue("break not on the wrapper: " + wrapper, wrapper.contains("break-before=\"page\""));
		assertEquals("one break in all", 1, count(out, "break-before=\"page\""));
	}


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

	/** A w:br's block ends its run's inline, and its ancestors' up to the paragraph; what
	 *  follows goes into copies of them (without their id), so that FOP keeps its spaces
	 *  (7733, ledger9 part B §5).  @since 17.3.1 */
	@Test
	public void textAfterALineBreakIsInInlinesOfItsOwn() throws Exception {
		String in = flow("<fo:block white-space-collapse=\"false\">"
				+ "<fo:inline id=\"run1\" font-size=\"8pt\"><fo:inline font-family=\"Arimo\">before"
				+ "<fo:block line-height=\"0pt\"/>after    spaces</fo:inline></fo:inline></fo:block>");
		org.w3c.dom.Document doc = org.docx4j.XmlUtils.getNewDocumentBuilder().parse(
				new org.xml.sax.InputSource(new java.io.StringReader(in)));
		WordLayoutFixups.splitInlinesAtLineBreaks(doc);
		org.w3c.dom.Element p = (org.w3c.dom.Element) doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "block").item(0);
		java.util.List<org.w3c.dom.Element> outer = new java.util.ArrayList<org.w3c.dom.Element>();
		for (org.w3c.dom.Node n = p.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n instanceof org.w3c.dom.Element) outer.add((org.w3c.dom.Element) n);
		}
		assertEquals("two runs at the paragraph's level", 2, outer.size());
		assertEquals("the first keeps its id", "run1", outer.get(0).getAttribute("id"));
		assertEquals("the copy has none", "", outer.get(1).getAttribute("id"));
		assertEquals("the copy keeps the run's size", "8pt", outer.get(1).getAttribute("font-size"));
		assertEquals("the first ends with the break", "block",
				outer.get(0).getFirstChild().getLastChild().getLocalName());
		assertEquals("before", outer.get(0).getTextContent());
		assertEquals("after    spaces", outer.get(1).getTextContent());
		assertEquals("Arimo", ((org.w3c.dom.Element) outer.get(1).getFirstChild()).getAttribute("font-family"));

		// a break with nothing after it leaves the run alone
		in = flow("<fo:block><fo:inline>text<fo:block line-height=\"0pt\"/></fo:inline></fo:block>");
		doc = org.docx4j.XmlUtils.getNewDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(in)));
		WordLayoutFixups.splitInlinesAtLineBreaks(doc);
		assertEquals(1, doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "inline").getLength());
	}

	/** Under an auto multiple below 1 the label is cut as the text is (CR-001 batch 53,
	 *  list-label-line-multiplier, 3493): Liberation Serif 11pt at w:line="180", whose line
	 *  applyLineBoxHints cuts to a 9.487pt box on a 7.108pt baseline, 3.162pt off the top.
	 *  The label "1." in the same font no longer stands its whole 10.27pt ascent over that
	 *  baseline: its line is the text's, and the line manager learns the cut ascent. */
	@Test
	public void listLabelIsCutWithItsLineBelowSingleSpacing() throws Exception {
		String in = flow("<fo:list-block><fo:list-item>"
				+ "<fo:list-item-label font-size=\"11pt\"><fo:block font-family=\"Liberation Serif\" docx4j-font=\"Times New Roman\" line-height=\"12.65pt\">1.</fo:block></fo:list-item-label>"
				+ "<fo:list-item-body><fo:block docx4j-linebox=\"9.487pt\" docx4j-baseline=\"7.108pt\" docx4j-linerule=\"auto\""
				+ " docx4j-autocut=\"3.162\" font-family=\"Liberation Serif\" font-size=\"11pt\" line-height=\"9.487pt\">text</fo:block>"
				+ "</fo:list-item-body></fo:list-item></fo:list-block>");
		org.w3c.dom.Document doc = org.docx4j.XmlUtils.getNewDocumentBuilder().parse(
				new org.xml.sax.InputSource(new java.io.StringReader(in)));
		WordLayoutFixups.listLabelLines(doc);
		org.w3c.dom.NodeList blocks = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "block");
		org.w3c.dom.Element label = (org.w3c.dom.Element) blocks.item(0);
		org.w3c.dom.Element text = (org.w3c.dom.Element) blocks.item(1);
		assertEquals("9.49pt", label.getAttribute(WordLayoutFixups.HINT_LINE_BOX));
		assertEquals("7.11pt", label.getAttribute(WordLayoutFixups.HINT_BASELINE));
		assertEquals("9.49pt", label.getAttribute("line-height"));
		assertEquals("7.11pt", text.getAttribute(WordLayoutFixups.HINT_LABEL_ASCENT));

		String out = WordLayoutFixups.apply(in, 15);
		assertFalse(out.contains("docx4j-autocut"));
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
	 * @since 17.1.0
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
	 * @since 17.1.0
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
	 * @since 17.1.0
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
	 * @since 17.1.0
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
	 * @since 17.1.0
	 */
	@Test
	public void aTextBoxDoesNotInheritTheParagraphsAlignment() {
		String out = WordLayoutFixups.apply(flow(textBox("<fo:block>Service de l'informatique</fo:block>")), 15);
		int box = out.indexOf("absolute-position");
		assertTrue("the box was not positioned: " + out, box > 0);
		String container = out.substring(out.lastIndexOf("<fo:block-container", box), out.indexOf('>', box));
		assertTrue("the box must reset text-align: " + container, container.contains("text-align=\"start\""));
		// the box keeps its own inset as start-indent (the hand-written FO here declares
		// none, so it is 0pt); what must not survive is the paragraph's text-indent
		assertTrue("and the indents: " + container, container.contains("text-indent=\"0pt\""));
	}

	/**
	 * Word paginates nothing inside a text box.  FOP, given break-before="page" inside
	 * an absolutely positioned container, paints only the last container of a run of
	 * them: measured on three boxes in zero-height wrappers, only the third was drawn,
	 * and without the breaks all three were.  A 335-page mail merge of 2345 boxes, every
	 * paragraph carrying w:pageBreakBefore, came out with one line a page against Word's
	 * nine.
	 *
	 * @since 17.1.0
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
	 * @since 17.1.0
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

	// ------------------------------------------------------------ 17.1.0, b2-batch20

	/**
	 * A space leader (a tab, or the leader leadingWhitespaceLeader writes) and an
	 * anchored picture are not on the paragraph's line, so a paragraph which also holds
	 * one inline picture is still a picture-only line.  Measured: a body paragraph of
	 * [anchored 62.25pt logo][1.7pt tab][inline 25.5pt logo] has Word's first baseline
	 * 25.5pt below the top margin, where ours was 29.2 - the picture plus the run's
	 * descent and line gap.
	 */
	@Test
	public void aTabAndAnAnchoredPictureDoNotSpoilAPictureOnlyLine() {
		String in = flow("<fo:block docx4j-pstyle=\"\" line-height=\"10.349pt\">"
				+ "<fo:external-graphic docx4j-anchor=\"none\" content-height=\"62.25pt\" content-width=\"40pt\" src=\"a.png\"/>"
				+ "<fo:leader leader-pattern=\"space\" leader-length=\"1.7pt\"/>"
				+ "<fo:inline id=\"bm\"/>"
				+ "<fo:inline font-size=\"13.0pt\"><fo:external-graphic content-height=\"25.5pt\" content-width=\"123.13pt\" src=\"b.jpeg\"/></fo:inline>"
				+ "</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertTrue("the inline picture must size the line: " + out, out.contains("line-box=\"25.5pt\""));
	}

	/** A dot leader does paint, so it still disqualifies the line. */
	@Test
	public void aDotLeaderStillDisqualifiesAPictureLine() {
		String in = flow("<fo:block docx4j-pstyle=\"\" line-height=\"10.349pt\">"
				+ "<fo:leader leader-pattern=\"dots\" leader-length=\"20pt\"/>"
				+ "<fo:external-graphic content-height=\"25.5pt\" content-width=\"123pt\" src=\"b.jpeg\"/>"
				+ "</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertFalse("a painting leader is on the line: " + out, out.contains("line-box=\"25.5pt\""));
	}

	/**
	 * letter-spacing is inherited, so the per-font inline's value (w:w character
	 * scaling) must be the sum of it and the run's own (w:spacing), not a replacement.
	 * Measured: a run of w:w="94" plus a w:spacing of -0.2pt ran 8.1pt long over a
	 * 38-character line.
	 */
	@Test
	public void letterSpacingOfNestedInlinesIsCumulative() {
		String in = flow("<fo:block docx4j-pstyle=\"\"><fo:inline letter-spacing=\"-0.2pt\">"
				+ "<fo:inline docx4j-font=\"Arial\" docx4j-scaled-spacing=\"1\""
				+ " letter-spacing=\"-0.244pt\">All</fo:inline>"
				+ "</fo:inline></fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertTrue("the two must combine: " + out, out.contains("letter-spacing=\"-0.444pt\""));
		assertFalse("the hint must be stripped: " + out, out.contains("docx4j-scaled-spacing"));

		// but a value the exporter repeats on a nested inline (the same w:spacing, no
		// w:w) carries no mark and must not be added to itself
		String same = flow("<fo:block docx4j-pstyle=\"\"><fo:inline letter-spacing=\"0.2pt\">"
				+ "<fo:inline docx4j-font=\"Arial\" letter-spacing=\"0.2pt\">All</fo:inline>"
				+ "</fo:inline></fo:block>");
		String outSame = WordLayoutFixups.apply(same, 15);
		assertFalse("an unmarked nested value must be left alone: " + outSame,
				outSame.contains("letter-spacing=\"0.4pt\""));
	}

	/**
	 * The same rule for a measured width factor (WidthFactors, CR-001 batch 42 item 3):
	 * it reaches the FO as the same marked letter-spacing that w:w does, so a Calibri
	 * Light run which also carries a w:spacing must end up with the sum.
	 */
	@Test
	public void aWidthFactorsLetterSpacingIsAddedToTheRunsOwnSpacing() {
		String in = flow("<fo:block docx4j-pstyle=\"\"><fo:inline letter-spacing=\"0.5pt\">"
				+ "<fo:inline docx4j-font=\"Calibri Light\" docx4j-scaled-spacing=\"1\""
				+ " letter-spacing=\"-0.069pt\">The quick brown fox</fo:inline>"
				+ "</fo:inline></fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		assertTrue("0.5 + (-0.069) = 0.431: " + out, out.contains("letter-spacing=\"0.431pt\""));
		assertFalse("the hint must be stripped: " + out, out.contains("docx4j-scaled-spacing"));
	}

	/** Word applies the first paragraph's space-before at the top of a header or
	 *  footer, where XSL-FO's default conditionality discards it. */
	@Test
	public void headerKeepsSpaceBeforeAtItsStart() {
		String in = "<fo:root " + NS + "><fo:page-sequence>"
				+ "<fo:static-content flow-name=\"xsl-region-before\">"
				+ "<fo:block space-before=\"3.3pt\" space-after=\"6pt\">Gezin</fo:block>"
				+ "</fo:static-content>"
				+ "<fo:flow flow-name=\"xsl-region-body\"><fo:block>b</fo:block></fo:flow>"
				+ "</fo:page-sequence></fo:root>";
		String out = WordLayoutFixups.apply(in, 15);
		assertEquals("space-before must be retained at the header's start", 1,
				count(out, "space-before.conditionality=\"retain\""));
		assertEquals(1, count(out, "space-after.conditionality=\"retain\""));
	}

	/** Two paragraphs which state no w:pStyle are both of the default style, so
	 *  w:contextualSpacing pairs them (the hint is "" for such a paragraph). */
	@Test
	public void contextualSpacingPairsTwoDefaultStyleParagraphs() {
		String in = flow("<fo:block docx4j-pstyle=\"\" docx4j-contextual=\"1\" space-after=\"10pt\">one</fo:block>"
				+ "<fo:block docx4j-pstyle=\"\" docx4j-contextual=\"1\" space-before=\"10pt\">two</fo:block>");
		String out = WordLayoutFixups.apply(in, 15);
		int one = out.indexOf(">one<"), two = out.indexOf(">two<");
		assertTrue("one: after suppressed", out.substring(out.lastIndexOf("<fo:block", one), one).contains("space-after=\"0pt\""));
		assertTrue("two: before suppressed", out.substring(out.lastIndexOf("<fo:block", two), two).contains("space-before=\"0pt\""));
	}

	/**
	 * At a cell's edges w:contextualSpacing drops the space only where the cell holds a
	 * single paragraph.  Where it holds several, there is no next paragraph for the
	 * "same style" test to be about and Word applies the last one's space-after:
	 * measured, Word's row pitch 25.0pt against the 19.9 suppressing it gave.
	 */
	@Test
	public void contextualSpacingAtCellEdgesOnlyForASingleParagraphCell() {
		String one = "<fo:table><fo:table-body><fo:table-row><fo:table-cell>"
				+ "<fo:block docx4j-pstyle=\"L\" docx4j-contextual=\"1\" space-before=\"10pt\" space-after=\"10pt\">only</fo:block>"
				+ "</fo:table-cell></fo:table-row></fo:table-body></fo:table>";
		String outOne = WordLayoutFixups.apply(flow(one), 15);
		assertTrue("a single contextual paragraph loses both: " + outOne,
				outOne.contains("space-before=\"0pt\"") && outOne.contains("space-after=\"0pt\""));

		String many = "<fo:table><fo:table-body><fo:table-row><fo:table-cell>"
				+ "<fo:block docx4j-pstyle=\"L\" docx4j-contextual=\"1\" space-before=\"10pt\" space-after=\"10pt\">a</fo:block>"
				+ "<fo:block docx4j-pstyle=\"L\" docx4j-contextual=\"1\" space-before=\"10pt\" space-after=\"10pt\">b</fo:block>"
				+ "</fo:table-cell></fo:table-row></fo:table-body></fo:table>";
		String outMany = WordLayoutFixups.apply(flow(many), 15);
		int b = outMany.indexOf(">b<");
		String last = outMany.substring(outMany.lastIndexOf("<fo:block", b), b);
		assertTrue("the last of several keeps its space-after: " + last,
				last.contains("space-after=\"10pt\""));
	}

	/** A row of one cell paragraph, which w:keepLines marks keep-together, in a row
	 *  w:cantSplit marks the same. */
	private static String cellParagraphRow() {
		return flow("<fo:table><fo:table-body>"
				+ "<fo:table-row keep-together.within-page=\"always\"><fo:table-cell>"
				+ "<fo:block docx4j-pstyle=\"\" keep-together.within-page=\"always\">two lines</fo:block>"
				+ "</fo:table-cell></fo:table-row></fo:table-body></fo:table>");
	}

	/**
	 * Below compatibility mode 15 Word divides a row inside a cell paragraph at any line
	 * (table-rowsplit-3: modes 11, 12 and 14 split 1+1, 1+2 and 2+1 with widow control
	 * on and with w:keepLines), so the cell gets widows and orphans of 1 and its
	 * paragraphs lose keep-together; w:cantSplit's keep on the row stays.  @since 17.3.1
	 */
	@Test
	public void cellParagraphsSplitBelowMode15() {
		String out = WordLayoutFixups.apply(cellParagraphRow(), 14);
		assertTrue("paragraph widows", out.matches("(?s).*<fo:block[^>]*widows=\"1\".*"));
		assertTrue("paragraph orphans", out.matches("(?s).*<fo:block[^>]*orphans=\"1\".*"));
		assertFalse("keepLines kept on the cell paragraph",
				out.matches("(?s).*<fo:block[^>]*keep-together.within-page.*"));
		assertTrue("cantSplit lost from the row",
				out.matches("(?s).*<fo:table-row[^>]*keep-together.within-page=\"always\".*"));
	}

	/** Not inside a container (a cell clipped to an exact row height, a turned cell),
	 *  which cannot be divided across pages anyway.  @since 17.3.1 */
	@Test
	public void cellParagraphsInAContainerKeepTheirWidows() {
		String in = flow("<fo:table><fo:table-body><fo:table-row><fo:table-cell>"
				+ "<fo:block-container block-progression-dimension=\"12pt\" overflow=\"hidden\">"
				+ "<fo:block docx4j-pstyle=\"\">clipped</fo:block></fo:block-container>"
				+ "</fo:table-cell></fo:table-row></fo:table-body></fo:table>");
		assertFalse(WordLayoutFixups.apply(in, 14).contains("widows"));
	}

	/** ...and from mode 15 they hold the paragraph together as Word does.  @since 17.3.1 */
	@Test
	public void cellParagraphsKeepTogetherFromMode15() {
		String out = WordLayoutFixups.apply(cellParagraphRow(), 15);
		assertFalse(out.contains("widows=\"1\""));
		assertTrue(out.matches("(?s).*<fo:block[^>]*keep-together.within-page=\"always\".*"));
	}

	/**
	 * An explicit start-indent in a text box is measured from the box's text area, as
	 * Word measures it (textbox-inset-stroke-list): moved in by the box's start inset;
	 * body-start() and the inherited start are left alone.  @since 17.3.1
	 */
	@Test
	public void textBoxStartIndentsAddTheInset() {
		String in = flow("<fo:block-container start-indent=\"8.7pt\" docx4j-textbox-start=\"8.7pt\">"
				+ "<fo:block docx4j-pstyle=\"\">plain</fo:block>"
				+ "<fo:list-block start-indent=\"0in\" provisional-distance-between-starts=\"18pt\">"
				+ "<fo:list-item><fo:list-item-label end-indent=\"label-end()\"><fo:block>1.</fo:block></fo:list-item-label>"
				+ "<fo:list-item-body start-indent=\"body-start()\"><fo:block>item</fo:block></fo:list-item-body>"
				+ "</fo:list-item></fo:list-block>"
				+ "<fo:block docx4j-pstyle=\"\" start-indent=\"36pt\">indented</fo:block>"
				+ "</fo:block-container>");
		String out = WordLayoutFixups.apply(in, 15);
		assertTrue("list-block", out.matches("(?s).*<fo:list-block[^>]*start-indent=\"8.7pt\".*"));
		assertTrue("indented paragraph", out.contains("start-indent=\"44.7pt\""));
		assertTrue("body-start() changed", out.contains("start-indent=\"body-start()\""));
		assertFalse("hint left", out.contains("docx4j-textbox-start"));
	}
}
