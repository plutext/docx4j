package org.docx4j.convert.out.common.wrappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.ConversionSectionWrappers;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.docx4j.wml.SdtBlock;
import org.junit.Test;

/**
 * A continuous section with another column count is merged into its
 * neighbour's page-sequence; the sequence takes the larger count and the
 * narrower part is wrapped in a container the FO exporter spans across all
 * columns (Getting Started guide: one column, a two-column samples list, one
 * column again).
 */
public class ContinuousSectionColumnsTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static String p(String text) {
		return "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p>";
	}

	@Test
	public void narrowerPartOfAMergedSequenceIsWrappedForSpanning() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ p("one column a") + p("one column b")
				+ "<w:p><w:pPr><w:sectPr><w:cols w:space=\"708\"/></w:sectPr></w:pPr></w:p>"
				+ p("two columns a") + p("two columns b")
				+ "<w:p><w:pPr><w:sectPr><w:type w:val=\"continuous\"/><w:cols w:num=\"2\" w:space=\"708\"/></w:sectPr></w:pPr></w:p>"
				+ p("last section")
				+ "<w:sectPr><w:cols w:space=\"708\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		ConversionSectionWrappers wrappers = ConversionSectionWrapperFactory.process(pkg, false, false);
		List<ConversionSectionWrapper> list = wrappers.getList();
		assertEquals(2, list.size());
		ConversionSectionWrapper merged = list.get(0);
		assertEquals("the merged sequence takes the larger count", 2, merged.getPageDimensions().getColsNum());
		Object first = XmlUtils.unwrap(merged.getContent().get(0));
		assertTrue("one-column part not wrapped: " + first.getClass(), first instanceof SdtBlock);
		SdtBlock sdt = (SdtBlock)first;
		assertEquals(ConversionSectionWrapperFactory.TAG_SPAN_ALL + "=1", sdt.getSdtPr().getTag().getVal());
		assertEquals("its paragraphs; the empty sectPr paragraph gets no line in Word",
				2, sdt.getSdtContent().getContent().size());
		// the two-column part (its two paragraphs) follows unwrapped
		assertEquals(3, merged.getContent().size());
		for (int i = 1; i < 3; i++) {
			assertTrue(XmlUtils.unwrap(merged.getContent().get(i)) instanceof org.docx4j.wml.P);
		}
		assertEquals(1, list.get(1).getPageDimensions().getColsNum());
	}

	/**
	 * The page masters can only carry one set of margins, so the merged sequence
	 * takes the first section's (Word starts the page with those) and each part
	 * with others carries the difference as indents.  Until 17.0.5 the sequence
	 * took the last section's margins and applied them to all of the content.
	 */
	@Test
	public void eachMergedPartKeepsItsOwnMargins() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ p("two columns a")
				+ "<w:p><w:pPr><w:sectPr><w:pgMar w:top=\"1440\" w:right=\"567\" w:bottom=\"1440\" w:left=\"567\"/>"
				+ "<w:cols w:num=\"2\" w:space=\"708\"/></w:sectPr></w:pPr></w:p>"
				+ p("one column")
				+ "<w:sectPr><w:type w:val=\"continuous\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1134\" w:bottom=\"1440\" w:left=\"1134\"/>"
				+ "<w:cols w:space=\"708\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		List<ConversionSectionWrapper> list = ConversionSectionWrapperFactory.process(pkg, false, false).getList();
		assertEquals(1, list.size());
		ConversionSectionWrapper merged = list.get(0);
		assertEquals("the first section's left margin", 567,
				merged.getPageDimensions().getPgMar().getLeft().intValue());
		assertEquals(567, merged.getPageDimensions().getPgMar().getRight().intValue());

		// the two-column part is the reference (the larger count, the sequence's
		// margins), so only the second part is wrapped, twice
		assertEquals(2, merged.getContent().size());
		Object last = XmlUtils.unwrap(merged.getContent().get(1));
		assertTrue("the narrower part not wrapped: " + last.getClass(), last instanceof SdtBlock);
		SdtBlock span = (SdtBlock)last;
		assertEquals(ConversionSectionWrapperFactory.TAG_SPAN_ALL + "=1", span.getSdtPr().getTag().getVal());
		SdtBlock indent = (SdtBlock)XmlUtils.unwrap(span.getSdtContent().getContent().get(0));
		assertEquals("the difference from the sequence's margins, in twips",
				ConversionSectionWrapperFactory.TAG_INDENT + "=567,567", indent.getSdtPr().getTag().getVal());
	}

	/**
	 * The gap between the columns has to come from the same section as the count.
	 * The wrapper for a run of merged continuous sections is built from the sectPr
	 * that <em>ends</em> the sequence - in the common case the one-column section
	 * after the columns - and until 17.0.5 only its column count was overridden, so
	 * the gap was that section's (often Word's 708 twip default) rather than the
	 * one the columns actually have.
	 *
	 * @since 17.0.5
	 */
	@Test
	public void theColumnGapComesFromTheSectionWithTheColumns() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ p("two columns a") + p("two columns b")
				+ "<w:p><w:pPr><w:sectPr><w:type w:val=\"continuous\"/>"
				+ "<w:cols w:num=\"2\" w:space=\"708\" w:equalWidth=\"0\">"
				+ "<w:col w:w=\"5313\" w:space=\"240\"/><w:col w:w=\"5219\"/></w:cols>"
				+ "</w:sectPr></w:pPr></w:p>"
				+ p("one column")
				+ "<w:sectPr><w:cols w:space=\"708\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		ConversionSectionWrapper merged =
				ConversionSectionWrapperFactory.process(pkg, false, false).getList().get(0);
		assertEquals(2, merged.getPageDimensions().getColsNum());
		assertEquals("w:col[1]/@w:space of the two-column section, not w:cols/@w:space of the last",
				240, merged.getPageDimensions().getColsSpacing());
	}

	/**
	 * Word gives no line to the paragraph mark carrying a section break when that
	 * paragraph is empty, so it must produce no block: as the last block of a flow,
	 * an empty one which does not fit starts a page carrying only the running header
	 * (four documents of a real-document corpus gained one to three pages each), and
	 * mid-page it costs a line height at every break.
	 *
	 * @since 17.0.5
	 */
	@Test
	public void anEmptyParagraphCarryingASectionBreakIsDropped() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ p("first section")
				+ "<w:p><w:pPr><w:sectPr><w:cols w:space=\"708\"/></w:sectPr></w:pPr></w:p>"
				+ p("second section")
				+ "<w:sectPr><w:cols w:space=\"708\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		List<ConversionSectionWrapper> list = ConversionSectionWrapperFactory.process(pkg, false, false).getList();
		assertEquals(2, list.size());
		assertEquals("only the paragraph with text", 1, list.get(0).getContent().size());
		assertEquals(1, list.get(1).getContent().size());
	}

	/** A section break on a paragraph which has content of its own still renders it. */
	@Test
	public void aSectionBreakOnANonEmptyParagraphKeepsIt() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ p("first section")
				+ "<w:p><w:pPr><w:sectPr><w:cols w:space=\"708\"/></w:sectPr></w:pPr>"
				+ "<w:r><w:t>last line of the first section</w:t></w:r></w:p>"
				+ p("second section")
				+ "<w:sectPr><w:cols w:space=\"708\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		List<ConversionSectionWrapper> list = ConversionSectionWrapperFactory.process(pkg, false, false).getList();
		assertEquals(2, list.size());
		assertEquals(2, list.get(0).getContent().size());
	}

	/** A section which is nothing but an empty section-break paragraph keeps it:
	 *  a flow with no block at all is invalid FO. */
	@Test
	public void aSectionWithNothingElseKeepsIt() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ "<w:p><w:pPr><w:sectPr><w:cols w:space=\"708\"/></w:sectPr></w:pPr></w:p>"
				+ p("second section")
				+ "<w:sectPr><w:cols w:space=\"708\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		List<ConversionSectionWrapper> list = ConversionSectionWrapperFactory.process(pkg, false, false).getList();
		assertEquals(2, list.size());
		assertEquals(1, list.get(0).getContent().size());
	}

	@Test
	public void sectionsWithTheSameCountAreLeftAlone() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ p("a")
				+ "<w:p><w:pPr><w:sectPr><w:type w:val=\"continuous\"/><w:cols w:space=\"708\"/></w:sectPr></w:pPr></w:p>"
				+ p("b")
				+ "<w:sectPr><w:cols w:space=\"708\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		ConversionSectionWrapper merged = ConversionSectionWrapperFactory.process(pkg, false, false).getList().get(0);
		for (Object o : merged.getContent()) {
			assertTrue(!(XmlUtils.unwrap(o) instanceof SdtBlock));
		}
	}

	// ---- a continuous section which starts a page anyway (§7)

	/**
	 * Where the paragraph opening a continuous section breaks the page itself
	 * (w:pageBreakBefore, its own or its style's), Word starts a page there and the
	 * break is a next-page one as far as layout goes: the section keeps its own
	 * page-sequence - its page master, its numbering, its headers - instead of being
	 * merged into the one before it.  Measured on a 179-page document whose body
	 * section (continuous, w:pgNumType w:start="1", opened by a Heading 1 which
	 * breaks before) ran its folios on from the roman front matter: 7, 8, 9... for
	 * Word's viii, 1, 2...
	 */
	@Test
	public void aContinuousSectionWhoseFirstParagraphBreaksThePageIsNotMerged() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ p("front matter")
				+ "<w:p><w:pPr><w:sectPr><w:pgNumType w:fmt=\"lowerRoman\"/><w:cols w:space=\"708\"/></w:sectPr></w:pPr></w:p>"
				+ "<w:p><w:pPr><w:pageBreakBefore/></w:pPr><w:r><w:t>1. Scope</w:t></w:r></w:p>"
				+ p("body")
				+ "<w:sectPr><w:type w:val=\"continuous\"/><w:pgNumType w:start=\"1\"/><w:cols w:space=\"708\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		List<ConversionSectionWrapper> list = ConversionSectionWrapperFactory.process(pkg, false, false).getList();
		assertEquals("two page-sequences", 2, list.size());
		assertEquals("lowerRoman", list.get(0).getPageNumberInformation().getPageFormat());
		assertEquals("the body restarts at 1", 1, list.get(1).getPageNumberInformation().getPageStart());
		assertEquals(2, list.get(1).getContent().size());
	}

	/** The break may be the paragraph style's rather than the paragraph's own. */
	@Test
	public void aStylesPageBreakBeforeCountsToo() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getStyle().add(
				(org.docx4j.wml.Style)XmlUtils.unmarshalString(
						"<w:style " + W + " w:type=\"paragraph\" w:styleId=\"Chapter\"><w:name w:val=\"Chapter\"/>"
						+ "<w:pPr><w:pageBreakBefore/></w:pPr></w:style>"));
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ p("front matter")
				+ "<w:p><w:pPr><w:sectPr><w:cols w:space=\"708\"/></w:sectPr></w:pPr></w:p>"
				+ "<w:p><w:pPr><w:pStyle w:val=\"Chapter\"/></w:pPr><w:r><w:t>1. Scope</w:t></w:r></w:p>"
				+ "<w:sectPr><w:type w:val=\"continuous\"/><w:cols w:space=\"708\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		assertEquals(2, ConversionSectionWrapperFactory.process(pkg, false, false).getList().size());
	}

	/** A break-only paragraph is not a paragraph which starts the page: Word puts its
	 *  mark on the page the section break opens and the break makes another, which
	 *  merging (and WordLayoutFixups.mergePageBreakParagraphs) reproduces. */
	@Test
	public void aBreakOnlyParagraphDoesNotStopTheMerge() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ p("a")
				+ "<w:p><w:pPr><w:sectPr><w:cols w:space=\"708\"/></w:sectPr></w:pPr></w:p>"
				+ "<w:p><w:pPr><w:pageBreakBefore/></w:pPr></w:p>"
				+ p("b")
				+ "<w:sectPr><w:type w:val=\"continuous\"/><w:cols w:space=\"708\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		assertEquals(1, ConversionSectionWrapperFactory.process(pkg, false, false).getList().size());
	}

	/**
	 * A merged sequence is built on the sectPr which ends it, so a section restarting
	 * the page numbering which runs into a continuous section lost its restart: the
	 * start and the format now come from the first part to declare them.
	 */
	@Test
	public void aMergedSequenceNumbersItsPagesAsItsFirstPartDoes() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ p("one column")
				+ "<w:p><w:pPr><w:sectPr><w:pgNumType w:fmt=\"lowerRoman\" w:start=\"5\"/><w:cols w:space=\"708\"/></w:sectPr></w:pPr></w:p>"
				+ p("two columns")
				+ "<w:sectPr><w:type w:val=\"continuous\"/><w:cols w:num=\"2\" w:space=\"708\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		List<ConversionSectionWrapper> list = ConversionSectionWrapperFactory.process(pkg, false, false).getList();
		assertEquals(1, list.size());
		assertEquals(5, list.get(0).getPageNumberInformation().getPageStart());
		assertEquals("lowerRoman", list.get(0).getPageNumberInformation().getPageFormat());
	}

	/** Where two merged parts both have the sequence's column count but differ in
	 *  their gap, the part with the most content supplies it (it used to be the
	 *  sequence's own section whenever that had the count). */
	@Test
	public void theColumnGapComesFromTheColumnPartWithTheMostContent() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ p("a") + p("b") + p("c") + p("d") + p("e")
				+ "<w:p><w:pPr><w:sectPr><w:cols w:num=\"2\" w:space=\"284\"/></w:sectPr></w:pPr></w:p>"
				+ p("f")
				+ "<w:sectPr><w:type w:val=\"continuous\"/><w:cols w:num=\"2\" w:space=\"708\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		ConversionSectionWrapper merged = ConversionSectionWrapperFactory.process(pkg, false, false).getList().get(0);
		assertEquals(2, merged.getPageDimensions().getColsNum());
		assertEquals(284, merged.getPageDimensions().getColsSpacing());
	}

	/** Summed over the parts which share a gap, not the single largest part: several
	 *  short two-column stretches at one gap outweigh one longer stretch at another. */
	@Test
	public void theColumnGapIsWeighedAcrossThePartsSharingIt() throws Exception {
		String twoCols146 = "<w:p><w:pPr><w:sectPr><w:type w:val=\"continuous\"/><w:cols w:num=\"2\" w:space=\"146\"/></w:sectPr></w:pPr></w:p>";
		String twoCols428 = "<w:p><w:pPr><w:sectPr><w:type w:val=\"continuous\"/><w:cols w:num=\"2\" w:space=\"428\"/></w:sectPr></w:pPr></w:p>";
		String oneCol = "<w:p><w:pPr><w:sectPr><w:type w:val=\"continuous\"/><w:cols w:space=\"720\"/></w:sectPr></w:pPr></w:p>";
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>"
				+ p("short one") + twoCols146
				+ p("x") + oneCol
				+ p("a longer part") + twoCols428
				+ p("x") + oneCol
				+ p("short two") + twoCols146
				+ p("x") + oneCol
				+ p("short three") + twoCols146
				+ p("x")
				+ "<w:sectPr><w:type w:val=\"continuous\"/><w:cols w:space=\"720\"/></w:sectPr>"
				+ "</w:body></w:document>"));
		ConversionSectionWrapper merged = ConversionSectionWrapperFactory.process(pkg, false, false).getList().get(0);
		assertEquals(2, merged.getPageDimensions().getColsNum());
		assertEquals("three short parts at 146 outweigh one long part at 428", 146, merged.getPageDimensions().getColsSpacing());
	}
}
