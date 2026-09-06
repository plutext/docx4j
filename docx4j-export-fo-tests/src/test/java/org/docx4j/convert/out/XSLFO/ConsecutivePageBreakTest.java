package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * Every hard page break costs a page boundary of its own, so consecutive breaks leave
 * empty pages between them (&#xa7;3.3).
 *
 * <p>Measured on the {@code page-empty} golden, where Word has 13 pages and docx4j had
 * 11.  Word gives a page with nothing on it to</p>
 * <ul>
 * <li>an empty paragraph carrying a typeless {@code w:sectPr} followed by a paragraph
 *     holding only a page break - the section break opens the page, the break paragraph's
 *     mark is the whole of it, and its break opens the next;</li>
 * <li>two {@code w:br w:type="page"} in one paragraph;</li>
 * <li>two paragraphs each holding one.</li>
 * </ul>
 *
 * <p>A {@code w:pageBreakBefore} paragraph after a page break is <b>not</b> one of them:
 * it is already at the top of a page and Word adds none for it (measured on two corpus
 * documents whose Heading 1 style carries it, where counting it cost two spurious pages
 * of fourteen and one of seven).</p>
 *
 * @since 17.0.6
 */
public class ConsecutivePageBreakTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static WordprocessingMLPackage pkg(String body) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private static final String BREAK_ONLY = "<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>";
	private static final String FIRST = "<w:p><w:r><w:t>first</w:t></w:r></w:p>";
	private static final String LAST = "<w:p><w:r><w:t>last</w:t></w:r></w:p>";

	private int pages(String body, int flags) throws Exception {
		return areaTree(pkg(body), flags).getElementsByTagName("pageViewport").getLength();
	}

	private void assertPages(String what, int expected, String body) throws Exception {
		assertEquals(what + " (visitor)", expected, pages(body, Docx4J.FLAG_NONE));
		assertEquals(what + " (XSLT)", expected, pages(body, Docx4J.FLAG_EXPORT_PREFER_XSL));
	}

	/** One break: two pages, no empty one. */
	@Test
	public void oneBreak() throws Exception {
		assertPages("one break-only paragraph", 2, FIRST + BREAK_ONLY + LAST);
	}

	/** Two break-only paragraphs: the page between them is Word's empty page. */
	@Test
	public void twoBreakOnlyParagraphs() throws Exception {
		assertPages("two break-only paragraphs", 3, FIRST + BREAK_ONLY + BREAK_ONLY + LAST);
	}

	/** Two w:br in one paragraph: the same. */
	@Test
	public void twoBreaksInOneParagraph() throws Exception {
		assertPages("two breaks in one paragraph", 3,
				FIRST + "<w:p><w:r><w:br w:type=\"page\"/><w:br w:type=\"page\"/></w:r></w:p>" + LAST);
	}

	/**
	 * A w:pageBreakBefore paragraph after a page break is already at the top of a page:
	 * Word adds none for it.
	 */
	@Test
	public void pageBreakBeforeAfterABreakCostsNothing() throws Exception {
		assertPages("break then w:pageBreakBefore", 2, FIRST + BREAK_ONLY
				+ "<w:p><w:pPr><w:pageBreakBefore/></w:pPr><w:r><w:t>last</w:t></w:r></w:p>");
	}

	/**
	 * A break-only paragraph opening a section: the section break has started the page
	 * already, so the break makes another and the section's first page is empty.
	 */
	@Test
	public void breakOpeningASection() throws Exception {
		assertPages("typeless sectPr then a break-only paragraph", 3,
				FIRST
				+ "<w:p><w:pPr><w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
				+ "</w:sectPr></w:pPr></w:p>"
				+ BREAK_ONLY + LAST);
	}
}
