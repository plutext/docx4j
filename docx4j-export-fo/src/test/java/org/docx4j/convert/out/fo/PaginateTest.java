package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ConversionFeatures;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.model.pagination.Paginate;
import org.docx4j.model.pagination.PaginationAreaTreeHandler;
import org.docx4j.model.pagination.PaginationMap;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Br;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.Test;

/**
 * CR-012 phase 1: which page each paragraph starts on, from FOP's area tree, and the
 * w:lastRenderedPageBreak markers written from that.  Lives in this module because it
 * needs FOP (and in this package: under JPMS a test can't join the core module's package).
 *
 * <p>The fixture's page boundaries are forced by page breaks, so which page a paragraph
 * lands on is not a matter of layout; only the long paragraph at the end runs over pages
 * on its own, and its assertions are relative.</p>
 */
public class PaginateTest {

	private static final ObjectFactory factory = new ObjectFactory();

	/*
	 * page 1: "Alpha" (P1), a page-break paragraph (P2)
	 * page 2: "Beta" (P3), "Beta body" (P4), a page-break paragraph (P5)
	 * page 3: a table whose cell holds "Cell" (P6), "Gamma" (P7), a long paragraph (P8) running on
	 * page 3+n: "Omega" (P9)
	 */
	private static WordprocessingMLPackage createPkg() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();

		mdp.addParagraphOfText("Alpha");
		pageBreak(mdp);
		mdp.addParagraphOfText("Beta");
		mdp.addParagraphOfText("Beta body");
		pageBreak(mdp);

		Tbl tbl = factory.createTbl();
		Tr tr = factory.createTr();
		Tc tc = factory.createTc();
		tc.getContent().add(mdp.createParagraphOfText("Cell"));
		tr.getContent().add(tc);
		tbl.getContent().add(tr);
		mdp.getContent().add(tbl);

		mdp.addParagraphOfText("Gamma");

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 300; i++) sb.append("The quick brown fox jumps over the lazy dog. ");
		mdp.addParagraphOfText(sb.toString().trim());

		mdp.addParagraphOfText("Omega");
		return pkg;
	}

	private static void pageBreak(MainDocumentPart mdp) {
		P p = factory.createP();
		R r = factory.createR();
		Br br = factory.createBr();
		br.setType(STBrType.PAGE);
		r.getContent().add(br);
		p.getContent().add(r);
		mdp.getContent().add(p);
	}

	private static List<P> paragraphs(WordprocessingMLPackage pkg) {
		return Paginate.bodyParagraphs(pkg.getMainDocumentPart());
	}

	/** Whether the paragraph's first run starts with the marker. */
	private static boolean startsWithMarker(P p) {
		for (Object o : p.getContent()) {
			o = XmlUtils.unwrap(o);
			if (o instanceof R) {
				List<Object> content = ((R) o).getContent();
				return !content.isEmpty() && XmlUtils.unwrap(content.get(0)) instanceof R.LastRenderedPageBreak;
			}
		}
		return false;
	}

	private static int markersIn(WordprocessingMLPackage pkg) {
		final int[] n = { 0 };
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof R.LastRenderedPageBreak) n[0]++;
				return null;
			}
		});
		return n[0];
	}

	@Test
	public void eachParagraphHasItsPage() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		PaginationMap map = Paginate.compute(pkg, null);

		assertEquals("keys P<n>, in document order, for paragraphs without a paraId",
				"[P1, P2, P3, P4, P5, P6, P7, P8, P9]", map.getKeys().toString());
		assertEquals(map.toString(), Integer.valueOf(1), map.getPageIndex("P1"));
		assertEquals(map.toString(), Integer.valueOf(2), map.getPageIndex("P3"));
		assertEquals(map.toString(), Integer.valueOf(2), map.getPageIndex("P4"));
		assertEquals("a paragraph in a table cell", Integer.valueOf(3), map.getPageIndex("P6"));
		assertEquals(map.toString(), Integer.valueOf(3), map.getPageIndex("P7"));
		assertEquals(map.toString(), Integer.valueOf(3), map.getPageIndex("P8"));

		int[] breaks = map.getBreaks("P8");
		assertTrue("the long paragraph spans pages: " + map, breaks.length >= 1);
		for (int i = 0; i < breaks.length; i++) {
			assertTrue("break offsets ascend from inside the text", breaks[i] > (i == 0 ? 0 : breaks[i - 1]));
		}
		assertEquals(Integer.valueOf(3 + breaks.length), map.getLastPageIndex("P8"));
		assertEquals(map.getLastPageIndex("P8"), map.getPageIndex("P9"));
		assertEquals(map.getPageIndex("P9").intValue(), map.getPageCount());
		assertEquals("no numbering restarts: page number is page index",
				map.getPageIndex("P9"), map.getPage("P9"));

		for (P p : paragraphs(pkg)) {
			assertNull("compute alone leaves the document alone", p.getParaId());
		}
		assertEquals(0, markersIn(pkg));
	}

	@Test
	public void paginateWritesAMarkerWhereANewPageStarts() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		PaginationMap map = Paginate.paginate(pkg, null);
		List<P> paragraphs = paragraphs(pkg);

		// ids assigned first, so the map is keyed by them
		Set<String> ids = new HashSet<String>();
		for (P p : paragraphs) {
			assertNotNull("paraId assigned", p.getParaId());
			assertTrue(p.getParaId(), p.getParaId().matches("[0-9A-F]{8}"));
			assertTrue("below 0x80000000", Long.parseLong(p.getParaId(), 16) < 0x80000000L);
			assertEquals("textId paired", p.getParaId(), p.getTextId());
			assertTrue("unique", ids.add(p.getParaId()));
		}
		assertEquals(Paginate.keys(paragraphs), map.getKeys());
		assertTrue(pkg.getMainDocumentPart().getJaxbElement().getIgnorable().contains("w14"));

		// Beta (page 2) and the cell paragraph (page 3) begin pages; Omega shares the long
		// paragraph's last page; nothing inside the long paragraph in this phase
		assertTrue("Beta", startsWithMarker(paragraphs.get(2)));
		assertTrue("the cell paragraph", startsWithMarker(paragraphs.get(5)));
		assertFalse("Alpha is on page 1", startsWithMarker(paragraphs.get(0)));
		assertFalse("Beta body continues page 2", startsWithMarker(paragraphs.get(3)));
		assertFalse("Gamma continues page 3", startsWithMarker(paragraphs.get(6)));
		assertFalse("Omega continues the long paragraph's page", startsWithMarker(paragraphs.get(8)));
		assertEquals(2, markersIn(pkg));
	}

	@Test
	public void paginatingAgainReplacesTheMarkers() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		Paginate.paginate(pkg, null);
		List<String> keys = Paginate.keys(paragraphs(pkg));

		PaginationMap again = Paginate.paginate(pkg, null);
		assertEquals("ids are kept", keys, again.getKeys());
		assertEquals("markers replaced, not added", 2, markersIn(pkg));
	}

	@Test
	public void markersAndIdsSurviveSaveAndDoNotMoveThePages() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		PaginationMap map = Paginate.paginate(pkg, null);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.save(pkg, baos, Docx4J.FLAG_SAVE_ZIP_FILE);
		WordprocessingMLPackage reloaded = Docx4J.load(new ByteArrayInputStream(baos.toByteArray()));

		assertEquals(2, markersIn(reloaded));
		assertEquals(map.getKeys(), Paginate.keys(paragraphs(reloaded)));

		PaginationMap after = Paginate.compute(reloaded, null);
		assertEquals(map.getPageIndexMap(), after.getPageIndexMap());
		assertEquals(map.getPageCount(), after.getPageCount());
	}

	@Test
	public void applyWithoutAssigningIdsPairsByPosition() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		PaginationMap map = Paginate.compute(pkg, null);
		assertEquals(2, Paginate.applyLastRenderedPageBreaks(pkg, map, false));
		for (P p : paragraphs(pkg)) assertNull(p.getParaId());

		// and with ids: the transient keys pair with the paragraphs the ids are then assigned to
		assertEquals(2, Paginate.applyLastRenderedPageBreaks(pkg, map));
		for (P p : paragraphs(pkg)) assertNotNull(p.getParaId());
		assertEquals(2, markersIn(pkg));
	}

	@Test
	public void aMapForAnotherDocumentStateIsRefused() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		PaginationMap map = Paginate.compute(pkg, null);
		pkg.getMainDocumentPart().addParagraphOfText("added since");
		try {
			Paginate.applyLastRenderedPageBreaks(pkg, map);
			fail("expected an IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			assertTrue(expected.getMessage(), expected.getMessage().contains("different document state"));
		}
	}

	@Test
	public void duplicateParaIdsDoNotBreakTheLayout() throws Exception {

		// FOP requires ids to be unique; a copy-pasted paragraph can share a paraId
		WordprocessingMLPackage pkg = createPkg();
		List<P> paragraphs = paragraphs(pkg);
		paragraphs.get(0).setParaId("0000ABCD");
		paragraphs.get(2).setParaId("0000ABCD");

		PaginationMap map = Paginate.compute(pkg, null);
		assertEquals("[0000ABCD, P2, P3, P4, P5, P6, P7, P8, P9]", map.getKeys().toString());
		assertEquals(Integer.valueOf(1), map.getPageIndex("0000ABCD"));
		assertEquals(Integer.valueOf(2), map.getPageIndex("P3"));
		assertEquals("compute restores the duplicate it keyed P3", "0000ABCD", paragraphs.get(2).getParaId());

		Paginate.paginate(pkg, null);
		assertFalse("paginate gives the duplicate its own id",
				paragraphs.get(0).getParaId().equals(paragraphs.get(2).getParaId()));
		assertEquals("0000ABCD", paragraphs.get(0).getParaId());
	}

	/**
	 * Word lays a page break out as the last line of its page, and puts the marker in the
	 * first run of the next page; the export moves a break at the head of a paragraph in
	 * front of its block (or onto the next block), so the map has such paragraphs a page
	 * later, or not at all.  The markers still land where Word's do.
	 */
	@Test
	public void explicitBreaksPutTheMarkerOnTheNextParagraphWithContent() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		mdp.addParagraphOfText("Alpha");                                        // 0  page 1
		pageBreak(mdp);                                                         // 1  break only
		mdp.addParagraphOfText("Charlie");                                      // 2  page 2: marker
		P d = mdp.addParagraphOfText("Delta then break");                       // 3  page 2
		d.getContent().add(pageBreakRun());
		mdp.addParagraphOfText("Echo");                                         // 4  page 3: marker
		P f = mdp.addParagraphOfText("Foxtrot after break");                    // 5  break then text: page 4, but the
		f.getContent().add(0, pageBreakRun());                                  //    marker inside the run is phase 2
		mdp.addParagraphOfText("Golf");                                         // 6  page 4
		pageBreak(mdp);                                                         // 7  two breaks in a row
		pageBreak(mdp);                                                         // 8
		mdp.addParagraphOfText("Juliet");                                       // 9  page 6: marker

		PaginationMap map = Paginate.paginate(pkg, null);
		List<P> paragraphs = paragraphs(pkg);
		assertEquals(map.toString(), 6, map.getPageCount());
		boolean[] expected = { false, false, true, false, true, false, false, false, false, true };
		for (int i = 0; i < expected.length; i++) {
			assertEquals("paragraph " + i + " in " + map, expected[i], startsWithMarker(paragraphs.get(i)));
		}
	}

	private static R pageBreakRun() {
		R r = factory.createR();
		Br br = factory.createBr();
		br.setType(STBrType.PAGE);
		r.getContent().add(br);
		return r;
	}

	@Test
	public void anEmptyParagraphStartingAPageGetsARunForTheMarker() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		mdp.addParagraphOfText("Alpha");
		pageBreak(mdp);
		mdp.getContent().add(factory.createP());   // empty, page 2
		mdp.addParagraphOfText("Beta");

		Paginate.paginate(pkg, null);
		List<P> paragraphs = paragraphs(pkg);
		assertTrue(startsWithMarker(paragraphs.get(2)));
		assertFalse(startsWithMarker(paragraphs.get(3)));
		assertEquals(1, markersIn(pkg));
	}

	@Test
	public void theFeatureIsOffByDefault_andTheIdsGoOnlyWhereItIsOn() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		assertFalse(fo(pkg, false).contains(" id=\"" + PaginationAreaTreeHandler.FO_ID_PREFIX));

		List<P> paragraphs = paragraphs(pkg);
		paragraphs.get(0).setParaId("1234ABCD");
		String fo = fo(pkg, true);
		assertTrue(fo, fo.contains(" id=\"p-1234ABCD\""));
		assertEquals("no id for a paragraph without a paraId", 1, occurrences(fo, " id=\"p-"));
	}

	private static String fo(WordprocessingMLPackage pkg, boolean paragraphIds) throws Exception {
		FOSettings settings = new FOSettings(pkg);
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		if (paragraphIds) settings.getFeatures().add(ConversionFeatures.PP_FO_PARAGRAPH_IDS);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, Docx4J.FLAG_EXPORT_PREFER_NONXSL);
		return baos.toString("UTF-8");
	}

	private static int occurrences(String haystack, String needle) {
		int count = 0;
		for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + 1)) count++;
		return count;
	}

	/** The area tree names the paragraphs by prod-id: the id round-trips through FOP. */
	@Test
	public void theAreaTreeCarriesTheIds() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		paragraphs(pkg).get(0).setParaId("1234ABCD");
		FOSettings settings = new FOSettings(pkg);
		settings.setApacheFopMime(Paginate.MIME_FOP_AREA_TREE);
		settings.getFeatures().add(ConversionFeatures.PP_FO_PARAGRAPH_IDS);
		org.docx4j.convert.out.FopReflective.invokeFORendererApacheFOP(settings);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, Docx4J.FLAG_EXPORT_PREFER_NONXSL);
		String areaTree = baos.toString("UTF-8");
		assertTrue(areaTree.substring(0, Math.min(areaTree.length(), 4000)),
				areaTree.contains("prod-id=\"p-1234ABCD\""));

		PaginationMap map = Paginate.parse(baos.toByteArray());
		assertEquals(Integer.valueOf(1), map.getPageIndex("1234ABCD"));
	}
}
