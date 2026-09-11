package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ConversionFeatures;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.model.pagination.Paginate;
import org.docx4j.model.pagination.PaginateSettings;
import org.docx4j.model.pagination.PaginationAreaTreeHandler;
import org.docx4j.model.pagination.PaginationMap;
import org.docx4j.model.pagination.RunText;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTTrackChange;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RunDel;
import org.docx4j.wml.RunIns;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.junit.Test;

/**
 * CR-012: which page each paragraph starts on, from FOP's area tree, and the
 * w:lastRenderedPageBreak markers written from that, at paragraph boundaries (phase 1)
 * and inside paragraphs that span pages (phase 2).  Lives in this module because it
 * needs FOP (and in this package: under JPMS a test can't join the core module's package).
 *
 * <p>The fixture's page boundaries are forced by page breaks, so which page a paragraph
 * lands on is not a matter of layout; only the long paragraph at the end runs over pages
 * on its own, and its assertions are about where FOP broke it rather than on which
 * character.</p>
 */
public class PaginateTest {

	private static final ObjectFactory factory = new ObjectFactory();

	private static final String SENTENCE = "The quick brown fox jumps over the lazy dog. ";

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
		mdp.addParagraphOfText(repeat(SENTENCE, 300).trim());
		mdp.addParagraphOfText("Omega");
		return pkg;
	}

	private static String repeat(String s, int n) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) sb.append(s);
		return sb.toString();
	}

	private static void pageBreak(MainDocumentPart mdp) {
		P p = factory.createP();
		p.getContent().add(pageBreakRun());
		mdp.getContent().add(p);
	}

	private static R pageBreakRun() {
		R r = factory.createR();
		Br br = factory.createBr();
		br.setType(STBrType.PAGE);
		r.getContent().add(br);
		return r;
	}

	private static R run(String text) {
		R r = factory.createR();
		Text t = factory.createText();
		t.setValue(text);
		t.setSpace("preserve");
		r.getContent().add(t);
		return r;
	}

	private static List<P> paragraphs(WordprocessingMLPackage pkg) {
		return Paginate.bodyParagraphs(pkg.getMainDocumentPart());
	}

	private static boolean isMarker(Object o) {
		return XmlUtils.unwrap(o) instanceof R.LastRenderedPageBreak;
	}

	/** Whether the paragraph's first run starts with the marker. */
	private static boolean startsWithMarker(P p) {
		List<R> runs = RunText.runs(p);
		return !runs.isEmpty() && !runs.get(0).getContent().isEmpty() && isMarker(runs.get(0).getContent().get(0));
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

	private static int markersIn(P p) {
		int n = 0;
		for (R r : RunText.runs(p)) {
			for (Object o : r.getContent()) if (isMarker(o)) n++;
		}
		return n;
	}

	/** The paragraph's text in the offset model (deleted runs excluded). */
	private static String textOf(P p) {
		StringBuilder sb = new StringBuilder();
		for (R r : RunText.runs(p)) RunText.appendText(r, sb);
		return sb.toString();
	}

	/** The offsets in the paragraph's text at which its markers stand. */
	private static List<Integer> markerOffsets(P p) {
		List<Integer> offsets = new ArrayList<Integer>();
		int pos = 0;
		for (R r : RunText.runs(p)) {
			for (Object o : r.getContent()) {
				if (isMarker(o)) offsets.add(pos);
				pos += RunText.length(o);
			}
		}
		return offsets;
	}

	// ---------------------------------------------------------------- phase 1

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
		String text = textOf(paragraphs(pkg).get(7));
		for (int i = 0; i < breaks.length; i++) {
			assertTrue("break offsets ascend from inside the text", breaks[i] > (i == 0 ? 0 : breaks[i - 1]));
			assertTrue("inside the text", breaks[i] < text.length());
			// FOP breaks the plain sentences at spaces: the new page starts a word
			assertEquals("a word starts at " + breaks[i] + " in " + map, ' ', text.charAt(breaks[i] - 1));
			assertTrue(text.charAt(breaks[i]) != ' ');
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
		// paragraph's last page; the long paragraph carries its own boundaries inside
		assertTrue("Beta", startsWithMarker(paragraphs.get(2)));
		assertTrue("the cell paragraph", startsWithMarker(paragraphs.get(5)));
		assertFalse("Alpha is on page 1", startsWithMarker(paragraphs.get(0)));
		assertFalse("Beta body continues page 2", startsWithMarker(paragraphs.get(3)));
		assertFalse("Gamma continues page 3", startsWithMarker(paragraphs.get(6)));
		assertFalse("Omega continues the long paragraph's page", startsWithMarker(paragraphs.get(8)));
		int inside = map.getBreaks(paragraphs.get(7).getParaId()).length;
		assertTrue(inside >= 1);
		assertEquals(inside, markersIn(paragraphs.get(7)));
		assertEquals(2 + inside, markersIn(pkg));
	}

	@Test
	public void paginatingAgainReplacesTheMarkers() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		PaginationMap first = Paginate.paginate(pkg, null);
		List<String> keys = Paginate.keys(paragraphs(pkg));
		int markers = markersIn(pkg);

		PaginationMap again = Paginate.paginate(pkg, null);
		assertEquals("ids are kept", keys, again.getKeys());
		assertEquals("the same pages", first.getPageIndexMap(), again.getPageIndexMap());
		assertEquals("markers replaced, not added", markers, markersIn(pkg));
	}

	@Test
	public void markersAndIdsSurviveSaveAndDoNotMoveThePages() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		PaginationMap map = Paginate.paginate(pkg, null);
		int markers = markersIn(pkg);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.save(pkg, baos, Docx4J.FLAG_SAVE_ZIP_FILE);
		WordprocessingMLPackage reloaded = Docx4J.load(new ByteArrayInputStream(baos.toByteArray()));

		assertEquals(markers, markersIn(reloaded));
		assertEquals(map.getKeys(), Paginate.keys(paragraphs(reloaded)));

		PaginationMap after = Paginate.compute(reloaded, null);
		assertEquals(map.getPageIndexMap(), after.getPageIndexMap());
		assertEquals(map.getPageCount(), after.getPageCount());
		String longKey = map.getKeys().get(7);
		assertEquals("the split runs lay out as the one did", map.getBreaks(longKey).length, after.getBreaks(longKey).length);
	}

	@Test
	public void applyWithoutAssigningIdsPairsByPosition() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		PaginationMap map = Paginate.compute(pkg, null);
		int expected = 2 + map.getBreaks("P8").length;
		assertEquals(expected, Paginate.applyLastRenderedPageBreaks(pkg, map, false));
		for (P p : paragraphs(pkg)) assertNull(p.getParaId());

		// and with ids: the transient keys pair with the paragraphs the ids are then assigned to
		assertEquals(expected, Paginate.applyLastRenderedPageBreaks(pkg, map));
		for (P p : paragraphs(pkg)) assertNotNull(p.getParaId());
		assertEquals(expected, markersIn(pkg));
	}

	@Test
	public void lineBreaksOffGivesParagraphBoundariesOnly() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		Paginate.paginate(pkg, new PaginateSettings().setLineBreaks(Boolean.FALSE));
		assertEquals(2, markersIn(pkg));
		assertEquals(0, markersIn(paragraphs(pkg).get(7)));
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
	public void explicitBreaksPutTheMarkerWhereWordDoes() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		mdp.addParagraphOfText("Alpha");                                        // 0  page 1
		pageBreak(mdp);                                                         // 1  break only: its break ends page 1
		mdp.addParagraphOfText("Charlie");                                      // 2  page 2: marker
		P d = mdp.addParagraphOfText("Delta then break");                       // 3  page 2
		d.getContent().add(pageBreakRun());
		mdp.addParagraphOfText("Echo");                                         // 4  page 3: marker
		P f = mdp.addParagraphOfText("Foxtrot after break");                    // 5  break then text: the break ends
		f.getContent().add(0, pageBreakRun());                                  //    page 3, the marker precedes the text
		mdp.addParagraphOfText("Golf");                                         // 6  page 4
		pageBreak(mdp);                                                         // 7  two breaks in a row: the first ends
		pageBreak(mdp);                                                         // 8  page 4; the second's run opens page 5
		mdp.addParagraphOfText("Juliet");                                       // 9  page 6: marker

		PaginationMap map = Paginate.paginate(pkg, null);
		List<P> paragraphs = paragraphs(pkg);
		assertEquals(map.toString(), 6, map.getPageCount());
		int[] expected = { 0, 0, 1, 0, 1, 1, 0, 0, 1, 1 };
		for (int i = 0; i < expected.length; i++) {
			assertEquals("paragraph " + i + " in " + map, expected[i], markersIn(paragraphs.get(i)));
		}
		// Foxtrot: after the break, before the text; the second break-only: before its break
		List<Object> foxtrot = new ArrayList<Object>();
		for (R r : RunText.runs(paragraphs.get(5))) foxtrot.addAll(r.getContent());
		assertTrue(XmlUtils.unwrap(foxtrot.get(0)) instanceof Br);
		assertTrue(isMarker(foxtrot.get(1)));
		assertTrue(startsWithMarker(paragraphs.get(8)));
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
		assertFalse(fo(pkg, false).contains(" id=\"" + PaginationAreaTreeHandler.FO_RUN_ID_PREFIX));

		List<P> paragraphs = paragraphs(pkg);
		paragraphs.get(0).setParaId("1234ABCD");
		String fo = fo(pkg, true);
		assertTrue(fo, fo.contains(" id=\"p-1234ABCD\""));
		assertTrue("its run, at offset 0", fo.contains(" id=\"r-1234ABCD-0\""));
		assertEquals("no id for a paragraph without a paraId", 1, occurrences(fo, " id=\"p-"));
		assertEquals("nor for its runs", 1, occurrences(fo, " id=\"r-"));
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

	/** The area tree names the paragraphs and runs by prod-id: the ids round-trip through FOP. */
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
		assertTrue(areaTree.contains("prod-id=\"r-1234ABCD-0\""));

		PaginationMap map = Paginate.parse(baos.toByteArray());
		assertEquals(Integer.valueOf(1), map.getPageIndex("1234ABCD"));
	}

	// ---------------------------------------------------------------- phase 2: inside paragraphs

	/** The in-paragraph markers of a paragraph of plain runs: at the map's offsets, each
	 *  opening a run, the text around them intact. */
	private static void assertMarkersInside(P p, PaginationMap map, String textBefore) {

		String key = p.getParaId();
		int[] breaks = map.getBreaks(key);
		assertTrue("spans pages: " + map, breaks.length >= 1);
		assertEquals("the text is intact", textBefore, textOf(p));
		List<Integer> offsets = markerOffsets(p);
		assertEquals("one marker per boundary: " + map, breaks.length, offsets.size());
		for (int i = 0; i < breaks.length; i++) {
			assertEquals(breaks[i], offsets.get(i).intValue());
		}
		for (R r : RunText.runs(p)) {
			for (int i = 0; i < r.getContent().size(); i++) {
				if (isMarker(r.getContent().get(i))) {
					assertEquals("a marker opens its run", 0, i);
					Object next = (r.getContent().size() > 1 ? XmlUtils.unwrap(r.getContent().get(1)) : null);
					if (next instanceof Text) {
						assertFalse("before the first character of the line", ((Text) next).getValue().startsWith(" "));
					}
				}
			}
		}
	}

	@Test
	public void aLongParagraphIsMarkedAtEachPageBoundary_theRunSplit() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		List<P> paragraphs = paragraphs(pkg);
		P p = paragraphs.get(7);
		String text = textOf(p);
		assertEquals("one run to start with", 1, RunText.runs(p).size());
		R original = RunText.runs(p).get(0);
		original.setRPr(factory.createRPr());
		original.getRPr().setB(factory.createBooleanDefaultTrue());

		PaginationMap map = Paginate.paginate(pkg, null);
		assertMarkersInside(p, map, text);
		List<R> runs = RunText.runs(p);
		assertEquals("split once per boundary", 1 + map.getBreaks(p.getParaId()).length, runs.size());
		for (R r : runs) {
			assertNotNull("the run properties are copied", r.getRPr());
			assertNotNull(r.getRPr().getB());
			Text t = (Text) XmlUtils.unwrap(r.getContent().get(r == runs.get(0) ? 0 : 1));
			assertEquals("preserve", t.getSpace());
		}
		// the map says the same on a second pass over the split runs
		PaginationMap again = Paginate.compute(pkg, null);
		assertEquals(map.getBreaks(p.getParaId()).length, again.getBreaks(p.getParaId()).length);
		assertEquals(map.getBreaks(p.getParaId())[0], again.getBreaks(p.getParaId())[0]);
	}

	@Test
	public void aBoundaryAtARunBoundaryNeedsNoSplit_hyperlinkRunsIncluded() throws Exception {

		// a paragraph of many hyperlink runs: the boundary falls between or inside them
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		P p = factory.createP();
		for (int i = 0; i < 400; i++) {
			P.Hyperlink h = factory.createPHyperlink();
			h.setAnchor("top");
			h.getContent().add(run("link " + i + " text "));
			p.getContent().add(h);
		}
		mdp.getContent().add(p);
		String text = textOf(p);

		PaginationMap map = Paginate.paginate(pkg, null);
		assertMarkersInside(p, map, text);
		for (R r : RunText.runs(p)) {
			if (!r.getContent().isEmpty() && isMarker(r.getContent().get(0))) {
				assertTrue("the marker's run is in its hyperlink", r.getParent() instanceof P.Hyperlink);
				assertTrue(((P.Hyperlink) r.getParent()).getContent().contains(r));
			}
		}
	}

	@Test
	public void tabsAndFieldsInTheParagraphDoNotShiftTheMarkers() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		P p = factory.createP();
		for (int i = 0; i < 250; i++) {
			R r = run("item " + i);
			r.getContent().add(factory.createRTab(factory.createRTab()));
			p.getContent().add(r);
			// a complex PAGE field: its result is what FOP prints, its code takes no characters
			p.getContent().add(fldChar(org.docx4j.wml.STFldCharType.BEGIN));
			R instr = factory.createR();
			Text code = factory.createText();
			code.setValue(" PAGE ");
			instr.getContent().add(factory.createRInstrText(code));
			p.getContent().add(instr);
			p.getContent().add(fldChar(org.docx4j.wml.STFldCharType.SEPARATE));
			p.getContent().add(run("1"));
			p.getContent().add(fldChar(org.docx4j.wml.STFldCharType.END));
			p.getContent().add(run(" and then some words to fill the line "));
		}
		mdp.getContent().add(p);
		String text = textOf(p);

		PaginationMap map = Paginate.paginate(pkg, null);
		int[] breaks = map.getBreaks(p.getParaId());
		assertTrue(map.toString(), breaks.length >= 2);
		assertEquals(text, textOf(p));
		assertEquals(breaks.length, markersIn(p));
		for (R r : RunText.runs(p)) {
			for (Object o : r.getContent()) {
				if (isMarker(o)) {
					Object first = XmlUtils.unwrap(r.getContent().get(0));
					assertTrue("never in a field code", !(r.getContent().size() > 1
							&& r.getContent().get(1) instanceof jakarta.xml.bind.JAXBElement
							&& "instrText".equals(((jakarta.xml.bind.JAXBElement<?>) r.getContent().get(1)).getName().getLocalPart())));
					assertTrue(first == XmlUtils.unwrap(o));
				}
			}
		}
		for (int b : breaks) {
			assertTrue("at a word start: '" + text.substring(Math.max(0, b - 8), Math.min(text.length(), b + 8)) + "'",
					text.charAt(b) != ' ');
		}
	}

	private static R fldChar(org.docx4j.wml.STFldCharType type) {
		R r = factory.createR();
		org.docx4j.wml.FldChar fc = factory.createFldChar();
		fc.setFldCharType(type);
		r.getContent().add(factory.createRFldChar(fc));
		return r;
	}

	@Test
	public void aTableRowSplitAcrossPagesIsMarkedInTheCell() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		Tbl tbl = factory.createTbl();
		Tr tr = factory.createTr();
		Tc tc = factory.createTc();
		P cell = mdp.createParagraphOfText(repeat(SENTENCE, 300).trim());
		tc.getContent().add(cell);
		tr.getContent().add(tc);
		tbl.getContent().add(tr);
		mdp.getContent().add(tbl);
		mdp.addParagraphOfText("after the table");
		String text = textOf(cell);

		PaginationMap map = Paginate.paginate(pkg, null);
		assertMarkersInside(cell, map, text);
		assertEquals("the paragraph after shares the last page", 0, markersIn(paragraphs(pkg).get(1)));
	}

	@Test
	public void hyphenationDoesNotCorruptTheText() throws Exception {

		// long words, justified, with automatic hyphenation: FOP may break a word at a
		// page boundary with a hyphen it added, which must not shift the marker
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		org.docx4j.wml.CTSettings settings = mdp.getDocumentSettingsPart(true).getJaxbElement();
		settings.setAutoHyphenation(factory.createBooleanDefaultTrue());
		P p = mdp.addParagraphOfText(repeat("Extraordinarily incomprehensibilities characteristically ", 400).trim());
		PPr pPr = factory.createPPr();
		org.docx4j.wml.Jc jc = factory.createJc();
		jc.setVal(org.docx4j.wml.JcEnumeration.BOTH);
		pPr.setJc(jc);
		p.setPPr(pPr);
		String text = textOf(p);

		PaginationMap map = Paginate.paginate(pkg, null);
		int[] breaks = map.getBreaks(p.getParaId());
		assertTrue(map.toString(), breaks.length >= 2);
		assertEquals("the text is intact", text, textOf(p));
		assertEquals(breaks.length, markersIn(p));
		for (int b : breaks) {
			assertTrue("never a hyphen the document has not got, nor a space, at " + b, text.charAt(b) != ' ');
			assertTrue(b > 0 && b < text.length());
		}
	}

	// ---------------------------------------------------------------- phase 2: the accepted view

	private static RunDel del(String text) {
		RunDel d = factory.createRunDel();
		d.setId(java.math.BigInteger.ONE);
		d.setAuthor("t");
		R r = factory.createR();
		org.docx4j.wml.DelText dt = factory.createDelText();
		dt.setValue(text);
		dt.setSpace("preserve");
		r.getContent().add(dt);
		d.getCustomXmlOrSmartTagOrSdt().add(r);
		return d;
	}

	@Test
	public void deletedTextTakesNoSpaceAndGetsNoMarker() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		PaginationMap plain = Paginate.compute(pkg, null);

		// pages of deleted text before the long paragraph, and inside it
		List<P> paragraphs = paragraphs(pkg);
		P gamma = paragraphs.get(6);
		gamma.getContent().add(del(repeat(SENTENCE, 400)));
		P longP = paragraphs.get(7);
		longP.getContent().add(0, del(repeat("deleted words ", 500)));
		String text = textOf(longP);

		PaginationMap map = Paginate.paginate(pkg, null);
		assertEquals("the deleted text is not laid out", plain.getPageCount(), map.getPageCount());
		assertEquals(plain.getBreaks("P8").length, map.getBreaks(longP.getParaId()).length);
		assertMarkersInside(longP, map, text);
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), new TraversalUtil.CallbackImpl() {
			private boolean inDel = false;
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof R.LastRenderedPageBreak) assertFalse("a marker inside w:del", inDel);
				return null;
			}
			@Override
			public void walkJAXBElements(Object parent) {
				boolean was = inDel;
				if (parent instanceof RunDel) inDel = true;
				super.walkJAXBElements(parent);
				inDel = was;
			}
		});
	}

	@Test
	public void insertedTextCounts() throws Exception {

		WordprocessingMLPackage pkg = createPkg();
		P longP = paragraphs(pkg).get(7);
		RunIns ins = factory.createRunIns();
		ins.setId(java.math.BigInteger.ONE);
		ins.setAuthor("t");
		ins.getCustomXmlOrSmartTagOrSdt().add(run(repeat("inserted words ", 40)));
		longP.getContent().add(0, ins);
		String text = textOf(longP);

		PaginationMap map = Paginate.paginate(pkg, null);
		assertMarkersInside(longP, map, text);
	}

	@Test
	public void aParagraphWhoseMarkIsDeletedIsOneWithTheNext() throws Exception {

		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		P first = mdp.addParagraphOfText(repeat(SENTENCE, 150).trim());
		PPr pPr = factory.createPPr();
		ParaRPr rPr = factory.createParaRPr();
		CTTrackChange del = factory.createCTTrackChange();
		del.setId(java.math.BigInteger.ONE);
		del.setAuthor("t");
		rPr.setDel(del);
		pPr.setRPr(rPr);
		first.setPPr(pPr);
		P second = mdp.addParagraphOfText(repeat(SENTENCE, 150).trim());
		mdp.addParagraphOfText("after");

		PaginationMap map = Paginate.paginate(pkg, null);
		assertTrue("keyed by the first", map.contains(first.getParaId()));
		assertFalse("the second is part of it", map.contains(second.getParaId()));
		int[] breaks = map.getBreaks(first.getParaId());
		assertTrue(map.toString(), breaks.length >= 1);
		String joined = textOf(first) + textOf(second);
		assertEquals(breaks.length, markersIn(first) + markersIn(second));
		int firstLength = textOf(first).length();
		for (int b : breaks) {
			assertEquals("a word starts there", ' ', joined.charAt(b - 1));
			if (b >= firstLength) assertTrue("a boundary in the second paragraph's text is marked there",
					markersIn(second) >= 1);
		}
		assertEquals("'after' shares the last page", 0, markersIn(paragraphs(pkg).get(2)));
	}
}
