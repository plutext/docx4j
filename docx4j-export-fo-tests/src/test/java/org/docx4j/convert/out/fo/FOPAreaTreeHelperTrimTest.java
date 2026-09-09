package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.wrappers.ConversionSectionWrapperFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * The header/footer extent pre-pass measures a trimmed copy of the document, and
 * the page masters it measures must be the ones the real pass builds.
 */
public class FOPAreaTreeHelperTrimTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		return pkg;
	}

	/**
	 * A continuous section whose first paragraph breaks the page is its own
	 * page-sequence (ConversionSectionWrapperFactory.startsPage); the trimmed copy's
	 * fillers carried no such break, so the pre-pass merged it, named its masters
	 * differently, and the real masters kept the half-page defaults (a 179-page document
	 * came out as 1,429 pages).
	 */
	@Test
	public void aSectionWhichOpensWithAPageBreakStillDoesOnceTrimmed() throws Exception {
		String body = "<w:p><w:r><w:t>front matter</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:sectPr><w:pgNumType w:fmt=\"lowerRoman\"/><w:cols w:space=\"708\"/></w:sectPr></w:pPr></w:p>"
				+ "<w:p><w:pPr><w:pageBreakBefore/></w:pPr><w:r><w:t>1. Scope</w:t></w:r></w:p>"
				+ "<w:p><w:r><w:t>body</w:t></w:r></w:p>"
				+ "<w:sectPr><w:type w:val=\"continuous\"/><w:pgNumType w:start=\"1\"/><w:cols w:space=\"708\"/></w:sectPr>";
		int real = ConversionSectionWrapperFactory.process(pkg(body), false, false).getList().size();
		assertEquals("the real pass keeps the section apart", 2, real);

		WordprocessingMLPackage trimmed = pkg(body);
		FOPAreaTreeHelper.trimContent(trimmed);
		int prePass = ConversionSectionWrapperFactory.process(trimmed, false, false).getList().size();
		assertEquals("the pre-pass must build the same page-sequences", real, prePass);
	}

	/** And where nothing keeps them apart, the trimmed copy merges as the document does. */
	@Test
	public void aMergedRunIsStillMergedOnceTrimmed() throws Exception {
		String body = "<w:p><w:r><w:t>a</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:sectPr><w:cols w:space=\"708\"/></w:sectPr></w:pPr></w:p>"
				+ "<w:p><w:r><w:t>b</w:t></w:r></w:p>"
				+ "<w:sectPr><w:type w:val=\"continuous\"/><w:cols w:num=\"2\" w:space=\"708\"/></w:sectPr>";
		assertEquals(1, ConversionSectionWrapperFactory.process(pkg(body), false, false).getList().size());
		WordprocessingMLPackage trimmed = pkg(body);
		FOPAreaTreeHelper.trimContent(trimmed);
		assertEquals(1, ConversionSectionWrapperFactory.process(trimmed, false, false).getList().size());
	}
}
