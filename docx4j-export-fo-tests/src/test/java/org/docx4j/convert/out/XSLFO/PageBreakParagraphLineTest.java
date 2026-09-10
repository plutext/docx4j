package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * A paragraph holding only a page break keeps the line before its break, at the foot of
 * the page it is on, and the paragraph mark sizes that line (&#xa7;3.3).  Where the page
 * has no room left for it, the line goes to the next page and the break to the one after,
 * which is a page with nothing on it.
 *
 * <p>Measured on a 7-page corpus document whose break-only paragraph carries a 28pt mark
 * ({@code w:sz="56"}, the Title style's, on {@code w:line="240"}): its page 4 ends 17.5pt
 * short of the margin, the 34.18pt line does not fit, and Word's page 5 is empty with the
 * next paragraph opening page 6.  Folding the break paragraph into what follows, as
 * 17.1.0 did, lost the line and the page.</p>
 *
 * <p>The page here is filled with 28 exact 24pt lines (672pt of the A4 body's 697.9), so
 * 25.9pt remain: an 11pt mark's 13.43pt line fits and costs nothing, a 28pt mark's
 * 34.18pt line does not and costs a page.</p>
 *
 * <p>The rule is off by default (property
 * {@code docx4j.convert.out.fo.wordLayout.pageBreakParagraphLine}): over the corpora the
 * line tips a page whose content is already a few points fuller than Word's into an empty
 * page more often than it gives the page Word has, and the one measurement behind it is
 * that 28pt mark; the {@code page-break-line} probe is with Word.  These tests turn it on.</p>
 *
 * @since 17.1.1
 */
public class PageBreakParagraphLineTest extends AbstractXSLFOTest {

	private static final String PROPERTY = "docx4j.convert.out.fo.wordLayout.pageBreakParagraphLine";
	private String saved;

	@org.junit.Before
	public void turnOn() {
		saved = org.docx4j.Docx4jProperties.getProperty(PROPERTY);
		org.docx4j.Docx4jProperties.setProperty(PROPERTY, "true");
	}

	@org.junit.After
	public void restore() {
		org.docx4j.Docx4jProperties.setProperty(PROPERTY, saved == null ? "false" : saved);
	}

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	/** 28 paragraphs of exactly 24pt each: 672pt of the 697.9pt body. */
	private static String filler() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 28; i++) {
			sb.append("<w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"0\" w:line=\"480\" w:lineRule=\"exact\"/></w:pPr>"
					+ "<w:r><w:t>line " + (i + 1) + "</w:t></w:r></w:p>");
		}
		return sb.toString();
	}

	private static String breakOnly(int markHalfPoints) {
		return "<w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"0\" w:line=\"240\" w:lineRule=\"auto\"/>"
				+ "<w:rPr><w:sz w:val=\"" + markHalfPoints + "\"/></w:rPr></w:pPr>"
				+ "<w:r><w:rPr><w:sz w:val=\"" + markHalfPoints + "\"/></w:rPr><w:br w:type=\"page\"/></w:r></w:p>";
	}

	private static final String LAST = "<w:p><w:r><w:t>last</w:t></w:r></w:p>";

	private int pages(String body, int flags) throws Exception {
		return areaTree(pkg(body), flags).getElementsByTagName("pageViewport").getLength();
	}

	private void assertPages(String what, int expected, String body) throws Exception {
		assertEquals(what + " (visitor)", expected, pages(body, Docx4J.FLAG_NONE));
		assertEquals(what + " (XSLT)", expected, pages(body, Docx4J.FLAG_EXPORT_PREFER_XSL));
	}

	/** An 11pt mark's line (13.43pt) fits in the 25.9pt left: two pages, nothing empty. */
	@Test
	public void aLineWhichFitsCostsNothing() throws Exception {
		assertPages("break paragraph with an 11pt mark at a page with 25.9pt left", 2,
				filler() + breakOnly(22) + LAST);
	}

	/** A 28pt mark's line (34.18pt) does not fit: it goes to page 2, its break to page 3. */
	@Test
	public void aLineWhichDoesNotFitCostsAPage() throws Exception {
		assertPages("break paragraph with a 28pt mark at a page with 25.9pt left", 3,
				filler() + breakOnly(56) + LAST);
	}

	/** With the property off (the default) the break paragraph is folded into what
	 *  follows, as 17.1.0 did, and the 28pt mark's line costs nothing. */
	@Test
	public void offByDefault() throws Exception {
		org.docx4j.Docx4jProperties.setProperty(PROPERTY, "false");
		assertPages("break paragraph with a 28pt mark, property off", 2,
				filler() + breakOnly(56) + LAST);
	}

	/** The same 28pt break paragraph with room for it: two pages. */
	@Test
	public void aTallLineWithRoomCostsNothing() throws Exception {
		assertPages("break paragraph with a 28pt mark on a page with room", 2,
				"<w:p><w:r><w:t>first</w:t></w:r></w:p>" + breakOnly(56) + LAST);
	}
}
