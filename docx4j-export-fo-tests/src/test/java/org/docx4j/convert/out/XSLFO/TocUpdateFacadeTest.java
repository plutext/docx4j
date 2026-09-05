package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.FopReflective;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.toc.TocFinder;
import org.docx4j.toc.TocGenerator;
import org.docx4j.toc.TocHelper;
import org.docx4j.wml.Br;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.Text;
import org.junit.Test;

/**
 * The ToC pathway in the Docx4J facade (17.0.6): Docx4J.updateToc, and
 * FLAG_EXPORT_UPDATE_TOC on toFO/toPDF.
 *
 * The page numbers come from laying the document out with FOP, which is why
 * this lives in docx4j-export-fo-tests.
 */
public class TocUpdateFacadeTest {

	private static final ObjectFactory factory = new ObjectFactory();

	/**
	 * A document with a page break before each heading, so which page a heading
	 * lands on isn't a matter of layout: the ToC is page 1, then one heading per
	 * page after it.  The ToC is generated without page numbers, and "Delta
	 * Chapter" is added after it was generated, so that an update has something
	 * to do: 3 entries and no page numbers become 4 entries with them.
	 */
	private static WordprocessingMLPackage createPkg() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		for (int i = 1; i < 10; i++) {
			mdp.getPropertyResolver().activateStyle(String.format(TocHelper.TOC_STYLE_MASK, i));
		}

		heading(mdp, "Heading1", "Alpha Chapter");   // page 2
		heading(mdp, "Heading1", "Beta Chapter");    // page 3
		heading(mdp, "Heading2", "Gamma Section");   // page 4

		new TocGenerator(pkg).generateToc(0, TocHelper.DEFAULT_TOC_INSTRUCTION, true); // no page numbers

		heading(mdp, "Heading1", "Delta Chapter");   // page 5; not in the ToC as generated

		return pkg;
	}

	/** A page break, then the heading, then a line of body text. */
	private static void heading(MainDocumentPart mdp, String style, String text) {

		P pageBreak = factory.createP();
		R r = factory.createR();
		Br br = factory.createBr();
		br.setType(STBrType.PAGE);
		r.getContent().add(br);
		pageBreak.getContent().add(r);
		mdp.getContent().add(pageBreak);

		mdp.addStyledParagraphOfText(style, text);
		mdp.addParagraphOfText("Body text."); // deliberately doesn't repeat the heading
	}

	private static SdtBlock tocSdt(WordprocessingMLPackage pkg) {
		TocFinder finder = new TocFinder();
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), finder);
		return finder.getTocSDT();
	}

	/**
	 * The page number in the ToC entry for this heading: the last w:t in the
	 * entry paragraph which holds a number (the paragraph also holds the heading
	 * text, and the PAGEREF field instruction).
	 *
	 * @return null if there is no entry for that heading; "" if the entry has no
	 * page number
	 */
	private static String pageNumberOf(WordprocessingMLPackage pkg, String entryText) {

		SdtBlock sdt = tocSdt(pkg);
		assertNotNull("no ToC content control", sdt);

		for (Object o : sdt.getSdtContent().getContent()) {
			Object unwrapped = XmlUtils.unwrap(o);
			if (!(unwrapped instanceof P)) continue;

			List<Object> texts = TocHelper.getAllElementsFromObject(unwrapped, Text.class);
			StringBuilder sb = new StringBuilder();
			for (Object t : texts) {
				sb.append(((Text)t).getValue());
			}
			if (!sb.toString().contains(entryText)) continue;

			for (int i = texts.size() - 1; i >= 0; i--) {
				String value = ((Text)texts.get(i)).getValue();
				if (value.matches("\\d+")) return value;
			}
			return "";
		}
		return null;
	}

	private static String fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		return export(pkg, flags, FOSettings.INTERNAL_FO_MIME);
	}

	/**
	 * FOP's area tree: what is actually on the contents page, the PAGEREF fields
	 * (fo:page-number-citation) resolved to the pages they point at.
	 */
	private static String areaTree(WordprocessingMLPackage pkg, int flags) throws Exception {
		return export(pkg, flags, "application/X-fop-areatree");
	}

	private static String export(WordprocessingMLPackage pkg, int flags, String mime) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(mime);
		FopReflective.invokeFORendererApacheFOP(foSettings);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return baos.toString("UTF-8");
	}

	/**
	 * The rendered contents line for this heading: from its first occurrence
	 * (the ToC is page 1, so the entry comes before the heading itself) to the
	 * end of that line.
	 */
	private static String tocLine(String areaTree, String firstWordOfHeading) {
		int from = areaTree.indexOf("<word>" + firstWordOfHeading + "</word>");
		assertTrue("'" + firstWordOfHeading + "' not in the area tree", from > 0);
		int to = areaTree.indexOf("</lineArea>", from);
		return areaTree.substring(from, to);
	}

	private static int occurrences(String haystack, String needle) {
		int count = 0;
		for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + 1)) count++;
		return count;
	}

	@Test
	public void flagUpdatesEntriesAndPageNumbers() throws Exception {

		WordprocessingMLPackage pkg = createPkg();

		// as generated: 3 entries, and no page numbers in them
		assertEquals("", pageNumberOf(pkg, "Alpha Chapter"));
		assertEquals(null, pageNumberOf(pkg, "Delta Chapter"));

		String at = areaTree(pkg, Docx4J.FLAG_EXPORT_UPDATE_TOC);

		// the package was updated in place
		assertEquals("2", pageNumberOf(pkg, "Alpha Chapter"));
		assertEquals("3", pageNumberOf(pkg, "Beta Chapter"));
		assertEquals("4", pageNumberOf(pkg, "Gamma Section"));
		assertEquals("5", pageNumberOf(pkg, "Delta Chapter"));

		// and it is the updated ToC which was rendered: the new entry is on the
		// contents page, with the page its heading is on
		assertTrue(tocLine(at, "Alpha").contains("<word>2</word>"));
		assertTrue(tocLine(at, "Delta").contains("<word>5</word>"));
	}

	@Test
	public void flagCombinesWithTheXsltPathway() throws Exception {

		WordprocessingMLPackage pkg = createPkg();

		// the flag must not be read as "neither of the export pathways"
		String fo = fo(pkg, Docx4J.FLAG_EXPORT_UPDATE_TOC | Docx4J.FLAG_EXPORT_PREFER_XSL);

		assertEquals("2", pageNumberOf(pkg, "Alpha Chapter"));
		assertEquals("5", pageNumberOf(pkg, "Delta Chapter"));
		assertEquals("the new entry is in the FO, and so is its heading", 2, occurrences(fo, "Delta Chapter"));
	}

	@Test
	public void withoutTheFlagTheTocIsLeftAlone() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		String fo = fo(pkg, Docx4J.FLAG_NONE);

		assertEquals("", pageNumberOf(pkg, "Alpha Chapter"));
		assertEquals(null, pageNumberOf(pkg, "Delta Chapter"));

		// only the heading, no ToC entry for it: this is what the flag is for
		assertEquals(1, occurrences(fo, "Delta Chapter"));

		// NB the page numbers in the PDF come from FOP resolving the PAGEREF
		// fields (fo:page-number-citation), not from the numbers cached in the
		// docx - so an un-updated ToC still prints the right numbers for the
		// entries it does have
		assertTrue(fo.contains("page-number-citation"));
	}

	@Test
	public void updateTocDirectly() throws Exception {

		WordprocessingMLPackage pkg = createPkg();

		assertTrue(Docx4J.updateToc(pkg)); // with page numbers

		assertEquals("2", pageNumberOf(pkg, "Alpha Chapter"));
		assertEquals("5", pageNumberOf(pkg, "Delta Chapter"));
	}

	@Test
	public void updateTocSkippingPageNumbers() throws Exception {

		WordprocessingMLPackage pkg = createPkg();

		assertTrue(Docx4J.updateToc(pkg, true));

		// the new entry is there, but no page numbers were calculated
		assertEquals("", pageNumberOf(pkg, "Delta Chapter"));
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

		// and the export still happens
		assertTrue(fo(pkg, Docx4J.FLAG_EXPORT_UPDATE_TOC).contains("Alpha Chapter"));
	}
}
