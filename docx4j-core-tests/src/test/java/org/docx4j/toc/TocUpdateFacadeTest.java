package org.docx4j.toc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.junit.Test;

/**
 * Docx4J.updateToc and FLAG_EXPORT_UPDATE_TOC, in the part of their behaviour
 * which doesn't need docx4j-export-fo: updating the entries without page
 * numbers (which is what the HTML export does), a document with no ToC, and
 * asking for page numbers when nothing can calculate them.
 *
 * The page-numbering behaviour is tested in docx4j-export-fo-tests
 * (TocUpdateFacadeTest there).
 *
 * @since 17.0.6
 */
public class TocUpdateFacadeTest {

	/** ToC generated over two headings; a third is added afterwards. */
	private static WordprocessingMLPackage createPkg() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		for (int i = 1; i < 10; i++) {
			mdp.getPropertyResolver().activateStyle(String.format(TocHelper.TOC_STYLE_MASK, i));
		}

		mdp.addStyledParagraphOfText("Heading1", "Alpha Chapter");
		mdp.addParagraphOfText("Body text.");
		mdp.addStyledParagraphOfText("Heading1", "Beta Chapter");
		mdp.addParagraphOfText("Body text.");

		new TocGenerator(pkg).generateToc(0, TocHelper.DEFAULT_TOC_INSTRUCTION, true);

		mdp.addStyledParagraphOfText("Heading1", "Gamma Chapter"); // not in the ToC as generated
		mdp.addParagraphOfText("Body text.");

		return pkg;
	}

	private static String html(WordprocessingMLPackage pkg, int flags) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, baos, flags);
		return baos.toString("UTF-8");
	}

	private static int occurrences(String haystack, String needle) {
		int count = 0;
		for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + 1)) count++;
		return count;
	}

	@Test
	public void updateTocWithoutPageNumbers() throws Exception {

		WordprocessingMLPackage pkg = createPkg();

		assertTrue(Docx4J.updateToc(pkg, true));

		assertTrue("the new heading is now a ToC entry",
				pkg.getMainDocumentPart().getXML().contains("Gamma Chapter"));
	}

	@Test
	public void htmlExportHonoursTheFlag() throws Exception {

		// without the flag, the ToC in the HTML is the stale one: the heading
		// only, no entry for it
		assertEquals(1, occurrences(html(createPkg(), Docx4J.FLAG_NONE), "Gamma Chapter"));

		// with it, the entry is there too (no page numbers: HTML has no pages,
		// so no FO/FOP rendering is involved)
		WordprocessingMLPackage pkg = createPkg();
		assertEquals(2, occurrences(html(pkg, Docx4J.FLAG_EXPORT_UPDATE_TOC), "Gamma Chapter"));

		// and the package was updated in place
		assertTrue(pkg.getMainDocumentPart().getXML().contains("Gamma Chapter"));
	}

	@Test
	public void noTocInTheDocument() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		mdp.addStyledParagraphOfText("Heading1", "Alpha Chapter");
		int contentSize = mdp.getContent().size();

		assertFalse(Docx4J.updateToc(pkg, true));
		assertFalse("no ToC: don't go looking for page numbers either", Docx4J.updateToc(pkg));
		assertEquals("document unchanged", contentSize, mdp.getContent().size());

		// the export still happens
		assertTrue(html(pkg, Docx4J.FLAG_EXPORT_UPDATE_TOC).contains("Alpha Chapter"));
	}

	/**
	 * Page numbers need something which can paginate the document: without
	 * docx4j-export-fo (as here), say so rather than silently omitting them.
	 */
	@Test
	public void pageNumbersWithoutAPaginator() throws Exception {

		try {
			Docx4J.updateToc(createPkg());
			fail("expected a TocException");
		} catch (TocException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("docx4j-export-fo"));
		}
	}
}
