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
		assertEquals("its paragraphs and the sectPr paragraph", 3, sdt.getSdtContent().getContent().size());
		// the two-column part (two paragraphs and its sectPr paragraph) follows unwrapped
		assertEquals(4, merged.getContent().size());
		for (int i = 1; i < 4; i++) {
			assertTrue(XmlUtils.unwrap(merged.getContent().get(i)) instanceof org.docx4j.wml.P);
		}
		assertEquals(1, list.get(1).getPageDimensions().getColsNum());
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
}
