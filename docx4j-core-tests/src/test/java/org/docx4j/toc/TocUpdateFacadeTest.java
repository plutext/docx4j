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
import java.util.ArrayList;
import java.util.List;
import org.docx4j.XmlUtils;
import org.docx4j.wml.P;
import org.docx4j.wml.SdtBlock;
import org.docx4j.toc.Toc;
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
 * @since 17.1.0
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

	/**
	 * A TOC written as a bare field whose "TOC Heading" paragraph sits before it (not inside a content
	 * control, as Word's gallery writes it): the update takes that heading into the control and reuses it,
	 * rather than generating a second one (measured 2026-10-29 on the Enterprise manual: "Contents" twice,
	 * with a page break between them). @since 17.3.5
	 */
	@Test
	public void bareFieldWithHeadingBefore() throws Exception {
		WordprocessingMLPackage pkg = createPkg();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		// unwrap the generated control: its heading and field paragraphs become top-level blocks
		List<Object> content = mdp.getContent();
		int at = -1;
		for (int i = 0; i < content.size(); i++) if (XmlUtils.unwrap(content.get(i)) instanceof SdtBlock) { at = i; break; }
		assertTrue(at >= 0);
		SdtBlock sdt = (SdtBlock) XmlUtils.unwrap(content.get(at));
		List<Object> inner = new ArrayList<Object>(sdt.getSdtContent().getContent());
		content.remove(at);
		content.addAll(at, inner);
		String before = mdp.getXML();
		assertEquals("one heading before", 1, occurrences(before, Toc.DEFAULT_TOC_HEADING + "</w:t>")); // not the gallery name "Table of Contents"
		assertFalse(before.contains("<w:sdt>"));

		assertTrue(Docx4J.updateToc(pkg, true));
		String after = mdp.getXML();
		assertEquals("still one heading: the existing one, reused", 1, occurrences(after, Toc.DEFAULT_TOC_HEADING + "</w:t>"));
		assertTrue("the new entry is there", after.contains("Gamma Chapter"));
		SdtBlock updated = null;
		for (Object o : content) if (XmlUtils.unwrap(o) instanceof SdtBlock) updated = (SdtBlock) XmlUtils.unwrap(o);
		assertTrue("the TOC is in a control now", updated != null);
		Object first = XmlUtils.unwrap(updated.getSdtContent().getContent().get(0));
		assertTrue("the heading is the control's first paragraph", first instanceof P
				&& mdp.getXML().indexOf(Toc.DEFAULT_TOC_HEADING) > mdp.getXML().indexOf("<w:sdt>"));
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
