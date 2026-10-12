/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.model.chart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.TablePart;
import org.docx4j.openpackaging.parts.WordprocessingML.EmbeddedPackagePart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.Test;
import org.pptx4j.pml.CTGraphicalObjectFrame;
import org.xlsx4j.model.SheetCells;
import org.xlsx4j.model.Tables;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.Worksheet;

/**
 * CR-036 phase 3's gate: charts made in a docx and a pptx by {@link ChartCreator}, against Word's
 * and PowerPoint's own insert of each type with its default data (the P3 and P4 probes), part by
 * part: in Office's own document, its chart deleted ({@link ChartCreator#delete}), ours made with
 * the data of Office's, the document saved and loaded again, and compared with Office's (loaded
 * and saved by docx4j the same way): the chart part (Office's random ids aside), the embedded
 * workbook (cells, table, sheet, styles, workbook, shared strings, parts), the drawing (ids
 * aside), the package's parts, relationships and content types.
 *
 * <p>{@code -Ddocx4j.chartCreate.write=<dir>} writes each document made, for the Office check.</p>
 */
public class ChartCreatorHostTest {

	private static final String WRITE = System.getProperty("docx4j.chartCreate.write");

	private static final String[] TYPES = { "column-clustered", "column-stacked", "column-stacked-100", "bar-clustered",
			"bar-stacked", "bar-stacked-100", "line", "line-markers", "pie", "doughnut", "area", "scatter" };

	static ChartType type(String name) {
		for (ChartType t : ChartType.values()) if (t.getTemplateName().equals(name)) return t;
		throw new IllegalArgumentException(name);
	}

	// ------------------------------------------------------------------ Word's (P3)

	@Test
	public void p3EveryType() throws Exception {
		List<String> failures = new ArrayList<String>();
		for (String t : TYPES) docx(failures, "p3-" + t, type(t));
		report(failures);
	}

	/** Word's North and South by Plan and Actual: the chart part (its workbook lost its table to the probe's edits) */
	@Test
	public void p3DataSet() throws Exception {
		WordprocessingMLPackage word = (WordprocessingMLPackage) ChartGate.load("authoring/p3-data-set.docx");
		String theirs = ChartGate.chartXml(ChartGate.chart(ChartGate.roundTrip(word)), true);
		WordprocessingMLPackage doc = (WordprocessingMLPackage) ChartGate.load("authoring/p3-data-set.docx");
		ChartCreator.delete(Charts.in(doc).get(0));
		R run = new ChartCreator(new Random(5)).inDocument(doc, dataSet());
		((P) doc.getMainDocumentPart().getContent().get(0)).getContent().add(run);
		write(doc, "p3-data-set.docx");
		List<String> failures = new ArrayList<String>();
		compare(failures, "p3-data-set chart", theirs, ChartGate.chartXml(ChartGate.chart(ChartGate.roundTrip(doc)), true));
		report(failures);
	}

	static ChartDefinition dataSet() {
		return ChartDefinition.of(ChartType.COLUMN_CLUSTERED).categories("North", "South").series("Plan", 1.5, 3.5)
				.series("Actual", 2.5, 4.5);
	}

	private static void docx(List<String> failures, String fixture, ChartType type) {
		try {
			WordprocessingMLPackage word = (WordprocessingMLPackage) ChartGate.roundTrip(ChartGate.load("authoring/" + fixture + ".docx"));
			ChartDefinition d = ChartCreatorTest.definition(ChartGate.workbook(ChartGate.chart(word)), type);

			WordprocessingMLPackage doc = (WordprocessingMLPackage) ChartGate.load("authoring/" + fixture + ".docx");
			ChartCreator.delete(Charts.in(doc).get(0));
			R run = new ChartCreator(new Random(fixture.hashCode())).inDocument(doc, d);
			// where Word's was: its paragraph, which the deletion left
			((P) doc.getMainDocumentPart().getContent().get(0)).getContent().add(run);
			write(doc, fixture + ".docx");
			WordprocessingMLPackage saved = (WordprocessingMLPackage) ChartGate.roundTrip(doc);
			host(failures, fixture, word, saved);
			compare(failures, fixture + " document", document(word), document(saved));
		} catch (Throwable e) {
			failures.add(fixture + ": " + e);
			e.printStackTrace();
		}
	}

	// ------------------------------------------------------------------ PowerPoint's (P4)

	@Test
	public void p4EveryType() throws Exception {
		List<String> failures = new ArrayList<String>();
		for (String t : TYPES) pptx(failures, "p4-" + t, type(t));
		report(failures);
	}

	@Test
	public void p4DataSet() throws Exception {
		PresentationMLPackage pp = (PresentationMLPackage) ChartGate.load("authoring/p4-data-set.pptx");
		String theirs = ChartGate.chartXml(ChartGate.chart(ChartGate.roundTrip(pp)), true);
		PresentationMLPackage deck = (PresentationMLPackage) ChartGate.load("authoring/p4-data-set.pptx");
		ChartRef c = Charts.in(deck).get(0);
		CTGraphicalObjectFrame f = (CTGraphicalObjectFrame) XmlUtils.unwrap(c.getAnchor());
		long x = f.getXfrm().getOff().getX(), y = f.getXfrm().getOff().getY(), cx = f.getXfrm().getExt().getCx(),
				cy = f.getXfrm().getExt().getCy();
		ChartCreator.delete(c);
		new ChartCreator(new Random(6)).inSlide((SlidePart) c.getHost(), dataSet(), x, y, cx, cy);
		write(deck, "p4-data-set.pptx");
		List<String> failures = new ArrayList<String>();
		compare(failures, "p4-data-set chart", theirs, ChartGate.chartXml(ChartGate.chart(ChartGate.roundTrip(deck)), true));
		report(failures);
	}

	private static void pptx(List<String> failures, String fixture, ChartType type) {
		try {
			PresentationMLPackage pp = (PresentationMLPackage) ChartGate.roundTrip(ChartGate.load("authoring/" + fixture + ".pptx"));
			ChartDefinition d = ChartCreatorTest.definition(ChartGate.workbook(ChartGate.chart(pp)), type);

			PresentationMLPackage deck = (PresentationMLPackage) ChartGate.load("authoring/" + fixture + ".pptx");
			ChartRef c = Charts.in(deck).get(0);
			CTGraphicalObjectFrame f = (CTGraphicalObjectFrame) XmlUtils.unwrap(c.getAnchor());
			long x = f.getXfrm().getOff().getX(), y = f.getXfrm().getOff().getY(), cx = f.getXfrm().getExt().getCx(),
					cy = f.getXfrm().getExt().getCy();
			ChartCreator.delete(c);
			new ChartCreator(new Random(fixture.hashCode())).inSlide((SlidePart) c.getHost(), d, x, y, cx, cy);
			write(deck, fixture + ".pptx");
			PresentationMLPackage saved = (PresentationMLPackage) ChartGate.roundTrip(deck);
			host(failures, fixture, pp, saved);
			compare(failures, fixture + " slide", slide(pp), slide(saved));
		} catch (Throwable e) {
			failures.add(fixture + ": " + e);
			e.printStackTrace();
		}
	}

	// ------------------------------------------------------------------ the comparison

	/** The chart, its workbook and the package's parts, ours against Office's */
	private static void host(List<String> failures, String fixture, OpcPackage office, OpcPackage ours) throws Exception {
		Chart theirs = ChartGate.chart(office), mine = ChartGate.chart(ours);
		// ours, not Office's left behind: one chart part, fresh ids
		int n = 0;
		for (Part p : ours.getParts().getParts().values()) if (p instanceof Chart) n++;
		assertEquals(fixture + " chart parts", 1, n);
		assertTrue(fixture + " fresh ids", !ChartGate.chartXml(theirs, false).equals(ChartGate.chartXml(mine, false)));
		compare(failures, fixture + " chart", ChartGate.chartXml(theirs, true), ChartGate.chartXml(mine, true));
		compare(failures, fixture + " chart rels", rels(theirs), rels(mine));
		SpreadsheetMLPackage a = ChartGate.workbook(theirs), b = ChartGate.workbook(mine);
		compare(failures, fixture + " cells", ChartGate.cells(a, true), ChartGate.cells(b, true));
		compare(failures, fixture + " shared strings", strings(a), strings(b));
		compare(failures, fixture + " table", table(a), table(b));
		compare(failures, fixture + " sheet", sheet(a), sheet(b));
		compare(failures, fixture + " styles", sml(a.getWorkbookPart().getStylesPart().getJaxbElement()),
				sml(b.getWorkbookPart().getStylesPart().getJaxbElement()));
		// the template is Word's workbook: PowerPoint's differs only by where Excel's window last was
		compare(failures, fixture + " workbook", window(guids(sml(a.getWorkbookPart().getJaxbElement()))),
				window(guids(sml(b.getWorkbookPart().getJaxbElement()))));
		compare(failures, fixture + " workbook parts", parts(a), parts(b));
		compare(failures, fixture + " parts", parts(office), parts(ours));
	}

	/** The parts, each with its content type, and the content types' defaults */
	static String parts(OpcPackage pkg) {
		Map<String, String> m = new TreeMap<String, String>();
		for (Map.Entry<PartName, Part> e : pkg.getParts().getParts().entrySet()) {
			m.put(e.getKey().getName(), pkg.getContentTypeManager().getContentType(e.getKey()));
		}
		ContentTypeManager ctm = pkg.getContentTypeManager();
		return m + "\ndefaults " + new TreeSet<String>(ctm.getDefaultContentType().keySet());
	}

	/** A part's relationships: id, type and target, in id order (Office writes them last first) */
	private static String rels(Part p) {
		TreeMap<String, String> m = new TreeMap<String, String>();
		for (Relationship r : p.getRelationshipsPart().getRelationships().getRelationship()) {
			m.put(r.getId(), r.getType() + " " + r.getTarget());
		}
		return m.toString();
	}

	private static String strings(SpreadsheetMLPackage pkg) throws Exception {
		TreeSet<String> s = new TreeSet<String>();
		for (CTRst r : pkg.getWorkbookPart().getSharedStrings().getContents().getSi()) s.add(r.getT().getValue());
		return s.toString();
	}

	/** Sheet1's table entire, its uids aside */
	private static String table(SpreadsheetMLPackage pkg) throws Exception {
		TablePart t = Tables.of(SheetCells.of(pkg, "Sheet1").getPart()).get(0);
		return guids(sml(t.getJaxbElement())) + "\n" + t.getPartName().getName();
	}

	/** Sheet1 but its cells (compared by text), its uid aside */
	private static String sheet(SpreadsheetMLPackage pkg) throws Exception {
		Worksheet ws = SheetCells.of(pkg, "Sheet1").getPart().getContents();
		return guids(sml(ws)).replaceAll("(?s)<sheetData>.*</sheetData>", "<sheetData/>");
	}

	private static String sml(Object o) {
		return XmlUtils.marshaltoString(o, true, true, org.xlsx4j.jaxb.Context.jcSML);
	}

	private static String window(String xml) {
		return xml.replaceAll("xWindow=\"\\d+\" yWindow=\"\\d+\"", "xWindow=\"X\" yWindow=\"Y\"");
	}

	private static String guids(String xml) {
		return xml.replaceAll("\\{[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}\\}", "{GUID}");
	}

	/** The main document, Word's random drawing ids aside */
	private static String document(WordprocessingMLPackage pkg) {
		String xml = XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, true);
		return xml.replaceAll("<wp:docPr id=\"\\d+\"", "<wp:docPr id=\"ID\"").replaceAll("(anchorId|editId)=\"[0-9A-F]{8}\"", "$1=\"ID\"")
				.replaceAll(" r:id=\"rId\\d+\"", " r:id=\"rId\"");
	}

	/** The chart's slide, PowerPoint's random ids aside */
	private static String slide(PresentationMLPackage pkg) throws Exception {
		Part host = Charts.in(pkg).get(0).getHost();
		String xml = XmlUtils.marshaltoString(((SlidePart) host).getJaxbElement(), true, true, org.pptx4j.jaxb.Context.jcPML);
		return guids(xml).replaceAll("modId([^>]*) val=\"\\d+\"", "modId$1 val=\"ID\"").replaceAll(" r:id=\"rId\\d+\"", " r:id=\"rId\"");
	}

	private static void compare(List<String> failures, String what, String expected, String actual) {
		if (expected.equals(actual)) return;
		int i = 0;
		while (i < Math.min(expected.length(), actual.length()) && expected.charAt(i) == actual.charAt(i)) i++;
		int from = Math.max(0, i - 200);
		failures.add(what + " differs at " + i + ":\n  office: ..." + expected.substring(from, Math.min(expected.length(), i + 300))
				+ "\n  docx4j: ..." + actual.substring(from, Math.min(actual.length(), i + 300)));
	}

	private static void report(List<String> failures) {
		if (!failures.isEmpty()) fail(failures.size() + " differ:\n" + String.join("\n\n", failures));
	}

	static void write(OpcPackage pkg, String name) throws Exception {
		if (WRITE == null) return;
		File dir = new File(WRITE);
		dir.mkdirs();
		pkg.save(new File(dir, name));
	}

	// ------------------------------------------------------------------ two charts; a chart deleted

	/** Word's two charts: the second's parts numbered 2, its workbook Microsoft_Excel_Worksheet1, "Chart 2" */
	@Test
	public void p3TwoCharts() throws Exception {
		WordprocessingMLPackage word = (WordprocessingMLPackage) ChartGate.roundTrip(ChartGate.load("authoring/p3-two-charts.docx"));
		WordprocessingMLPackage doc = (WordprocessingMLPackage) ChartGate.load("authoring/p3-two-charts.docx");
		List<ChartRef> theirs = Charts.in(doc);
		// Word's two: a clustered column, a line
		ChartType[] types = { ChartType.COLUMN_CLUSTERED, ChartType.LINE };
		List<ChartDefinition> defs = new ArrayList<ChartDefinition>();
		for (ChartRef c : theirs) defs.add(ChartCreatorTest.definition(ChartGate.workbook((Chart) c.getChartPart()), types[defs.size()]));
		for (ChartRef c : theirs) ChartCreator.delete(c);
		ChartCreator cc = new ChartCreator(new Random(2));
		List<Object> body = doc.getMainDocumentPart().getContent();
		int n = 0;
		for (Object o : body) {
			if (o instanceof P && n < defs.size() && ((P) o).getContent().isEmpty()) ((P) o).getContent().add(cc.inDocument(doc, defs.get(n++)));
		}
		assertEquals(2, n);
		write(doc, "p3-two-charts.docx");
		WordprocessingMLPackage saved = (WordprocessingMLPackage) ChartGate.roundTrip(doc);
		List<String> failures = new ArrayList<String>();
		compare(failures, "p3-two-charts parts", parts(word), parts(saved));
		compare(failures, "p3-two-charts document", document(word), document(saved));
		List<ChartRef> ours = Charts.in(saved);
		for (int i = 0; i < 2; i++) {
			compare(failures, "p3-two-charts chart " + (i + 1), ChartGate.chartXml((Chart) Charts.in(word).get(i).getChartPart(), true),
					ChartGate.chartXml((Chart) ours.get(i).getChartPart(), true));
			compare(failures, "p3-two-charts chart rels " + (i + 1), rels(Charts.in(word).get(i).getChartPart()), rels(ours.get(i).getChartPart()));
		}
		report(failures);
	}

	/** Word's chart deleted: the document's parts and content types are those of Word's document with no chart */
	@Test
	public void deletedAsWordsNoChart() throws Exception {
		WordprocessingMLPackage none = (WordprocessingMLPackage) ChartGate.roundTrip(ChartGate.load("authoring/p3-no-chart.docx"));
		for (String t : new String[] { "column-clustered", "pie" }) {
			WordprocessingMLPackage doc = (WordprocessingMLPackage) ChartGate.load("authoring/p3-" + t + ".docx");
			ChartCreator.delete(Charts.in(doc).get(0));
			write(doc, "deleted-p3-" + t + ".docx");
			WordprocessingMLPackage saved = (WordprocessingMLPackage) ChartGate.roundTrip(doc);
			assertEquals(t, parts(none).replaceAll("docProps/[^,}]*", ""), parts(saved).replaceAll("docProps/[^,}]*", ""));
			assertEquals(0, Charts.in(saved).size());
			assertTrue(t, ((P) saved.getMainDocumentPart().getContent().get(0)).getContent().isEmpty());
		}
		// a slide's, and a sheet's
		PresentationMLPackage deck = (PresentationMLPackage) ChartGate.load("authoring/p4-pie.pptx");
		ChartRef c = Charts.in(deck).get(0);
		ChartCreator.delete(c);
		write(deck, "deleted-p4-pie.pptx");
		PresentationMLPackage savedDeck = (PresentationMLPackage) ChartGate.roundTrip(deck);
		assertEquals(0, Charts.in(savedDeck).size());
		assertTrue(!parts(savedDeck).contains("xlsx") && !parts(savedDeck).contains("/ppt/charts/"));
		for (Object o : ((SlidePart) savedDeck.getParts().get(c.getHost().getPartName())).getJaxbElement().getCSld().getSpTree()
				.getSpOrGrpSpOrGraphicFrame()) {
			assertTrue(!(XmlUtils.unwrap(o) instanceof CTGraphicalObjectFrame));
		}
		SpreadsheetMLPackage wb = (SpreadsheetMLPackage) ChartGate.load("authoring/p1-pie.xlsx");
		ChartCreator.delete(Charts.in(wb).get(0));
		SpreadsheetMLPackage savedWb = (SpreadsheetMLPackage) ChartGate.roundTrip(wb);
		assertEquals(0, Charts.in(savedWb).size());
		assertTrue(!parts(savedWb).contains("/xl/charts/"));
	}

	// ------------------------------------------------------------------ new documents; settings; reproducible

	/**
	 * In a docx and a pptx docx4j made: the chart's data edited with the editor, settings set, a
	 * second chart; dates on a docx's column chart give a date axis
	 */
	@Test
	public void inNewDocuments() throws Exception {
		WordprocessingMLPackage doc = WordprocessingMLPackage.createPackage();
		ChartCreator cc = new ChartCreator(new Random(4));
		ChartRef sales = cc.inNewParagraph(doc, dataSet().title("Sales").valueAxis(a -> a.title("Units").numberFormat("0.0")));
		ChartRef days = cc.inNewParagraph(doc, ChartDefinition.of(ChartType.COLUMN_CLUSTERED)
				.dates("d-mmm", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 6), LocalDate.of(2026, 1, 7)).series("Visits", 120, 135, 128)
				.dataLabels(true));
		cc.inNewParagraph(doc, ChartDefinition.of(ChartType.PIE).categories("North", "South", "East", "West", "Central")
				.series("Plan", 10, 11, 12, 13, 14).series("Actual", 12, 13, 14, 15, 16).legend(LegendPosition.RIGHT));
		try (ChartDataEditor e = ChartDataEditor.of((Chart) sales.getChartPart())) {
			e.addCategory("East", 1.0, 2.0);
		}
		ChartSettings.setLegend((Chart) sales.getChartPart(), LegendPosition.TOP);
		write(doc, "new-document.docx");
		WordprocessingMLPackage saved = (WordprocessingMLPackage) ChartGate.roundTrip(doc);
		List<ChartRef> charts = Charts.in(saved);
		assertEquals(3, charts.size());
		assertEquals("Chart 3", charts.get(2).getName());
		assertEquals("/word/embeddings/Microsoft_Excel_Worksheet2.xlsx",
				charts.get(2).getChartPart().getRelationshipsPart().getPart(
						((Chart) charts.get(2).getChartPart()).getContents().getExternalData().getId()).getPartName().getName());
		ChartModel m = charts.get(0).read();
		assertEquals("Sales", m.getTitle().getText());
		assertEquals(3, m.getGroups().get(0).getSeries().get(0).getValues().size());
		assertEquals("A1:C4", Tables.of(SheetCells.of(ChartGate.workbook((Chart) charts.get(0).getChartPart()), "Sheet1").getPart()).get(0)
				.getContents().getRef());
		assertEquals(ChartAxis.Kind.DATE, charts.get(1).read().getAxes().get(0).getKind());
		// the pie's points: Word's ids, numbered 2 idx + 1, the second series' too
		String pie = ChartGate.chartXml((Chart) charts.get(2).getChartPart(), false);
		assertTrue(pie.contains("{00000009-"));
		assertTrue(docPrsUnique(saved));
		assertNotNull(days);

		PresentationMLPackage deck = PresentationMLPackage.createPackage();
		SlidePart slide = PresentationMLPackage.createSlidePart(deck.getMainPresentationPart(),
				(org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart) deck.getParts().get(new PartName("/ppt/slideLayouts/slideLayout1.xml")),
				new PartName("/ppt/slides/slide1.xml"));
		CTGraphicalObjectFrame f1 = cc.inSlide(slide, dataSet().title("Sales"), 1270000, 1270000, ChartCreator.DEFAULT_WIDTH,
				ChartCreator.DEFAULT_HEIGHT);
		cc.inSlide(slide, ChartDefinition.of(ChartType.LINE_MARKERS).categories("Q1", "Q2", "Q3").series("Visits", 120, 135, 128),
				1270000, 4600000, ChartCreator.DEFAULT_WIDTH, ChartCreator.DEFAULT_HEIGHT);
		ChartRef r1 = ChartRef.of(slide, f1);
		try (ChartDataEditor e = ChartDataEditor.of((Chart) r1.getChartPart())) {
			e.addSeries("Forecast", 2.0, 3.0);
		}
		write(deck, "new-presentation.pptx");
		PresentationMLPackage savedDeck = (PresentationMLPackage) ChartGate.roundTrip(deck);
		List<ChartRef> slideCharts = Charts.in(savedDeck);
		assertEquals(2, slideCharts.size());
		assertEquals(3, slideCharts.get(0).read().getGroups().get(0).getSeries().size());
		assertTrue(!slideCharts.get(0).getName().equals(slideCharts.get(1).getName()));
	}

	/** Every drawing's docPr id in the main document distinct */
	private static boolean docPrsUnique(WordprocessingMLPackage pkg) {
		java.util.regex.Matcher m = java.util.regex.Pattern.compile("<wp:docPr id=\"(\\d+)\"")
				.matcher(XmlUtils.marshaltoString(pkg.getMainDocumentPart().getJaxbElement(), true, false));
		java.util.Set<String> seen = new java.util.HashSet<String>();
		while (m.find()) if (!seen.add(m.group(1))) return false;
		return true;
	}

	/** The same seed, the same document */
	@Test
	public void seededIdsReproduce() throws Exception {
		String[] a = made(9), b = made(9), c = made(10);
		for (int i = 0; i < a.length; i++) assertEquals(a[i], b[i]);
		assertTrue(!a[0].equals(c[0]));
	}

	private static String[] made(long seed) throws Exception {
		WordprocessingMLPackage doc = WordprocessingMLPackage.createPackage();
		new ChartCreator(new Random(seed)).inNewParagraph(doc, ChartDefinition.of(ChartType.PIE).categories("A", "B").series("S", 1, 2));
		WordprocessingMLPackage saved = (WordprocessingMLPackage) ChartGate.roundTrip(doc);
		Chart c = ChartGate.chart(saved);
		SpreadsheetMLPackage wb = ChartGate.workbook(c);
		return new String[] { ChartGate.chartXml(c, false),
				XmlUtils.marshaltoString(saved.getMainDocumentPart().getJaxbElement(), true, false),
				sml(Tables.of(SheetCells.of(wb, "Sheet1").getPart()).get(0).getJaxbElement()), sml(wb.getWorkbookPart().getJaxbElement()) };
	}

	/**
	 * The embedded workbook's author and times are docx4j.properties' (the template's are the
	 * probe's): by default none, and docx4j's save's lastModifiedBy; with docx4j.dc.creator.value
	 * and docx4j.dcterms.write, that author and the current time.  Its part is typed by the xlsx
	 * default.
	 */
	@Test
	public void workbookAuthorAndTimes() throws Exception {
		SpreadsheetMLPackage wb = madeWorkbook();
		org.docx4j.docProps.core.CoreProperties cp = wb.getDocPropsCorePart().getContents();
		assertEquals(null, cp.getCreator());
		assertEquals(null, cp.getCreated());
		assertEquals(null, cp.getModified());
		assertTrue(cp.getLastModifiedBy() == null || cp.getLastModifiedBy().startsWith("docx4j"));

		java.util.Properties props = org.docx4j.Docx4jProperties.getProperties();
		Object creator = props.get("docx4j.dc.creator.value"), times = props.get("docx4j.dcterms.write");
		try {
			props.setProperty("docx4j.dc.creator.value", "A Writer");
			props.setProperty("docx4j.dcterms.write", "true");
			long before = java.time.Instant.now().getEpochSecond();
			cp = madeWorkbook().getDocPropsCorePart().getContents();
			assertEquals("A Writer", cp.getCreator().getContent().get(0));
			assertTrue(cp.getCreated() instanceof org.docx4j.docProps.core.dc.terms.W3CDTF);
			long created = java.time.Instant.parse(cp.getCreated().getContent().get(0)).getEpochSecond();
			assertTrue(created >= before && created <= java.time.Instant.now().getEpochSecond());
			assertNotNull(cp.getModified());
		} finally {
			restore(props, "docx4j.dc.creator.value", creator);
			restore(props, "docx4j.dcterms.write", times);
		}
	}

	private static SpreadsheetMLPackage madeWorkbook() throws Exception {
		WordprocessingMLPackage doc = WordprocessingMLPackage.createPackage();
		new ChartCreator(new Random(1)).inNewParagraph(doc, dataSet());
		WordprocessingMLPackage saved = (WordprocessingMLPackage) ChartGate.roundTrip(doc);
		Chart c = ChartGate.chart(saved);
		EmbeddedPackagePart part = (EmbeddedPackagePart) c.getRelationshipsPart().getPart(c.getContents().getExternalData().getId());
		assertTrue(saved.getContentTypeManager().getDefaultContentType().containsKey("xlsx"));
		assertTrue(!saved.getContentTypeManager().getOverrideContentType().containsKey(part.getPartName().getURI()));
		return (SpreadsheetMLPackage) part.extractPackage();
	}

	static void restore(java.util.Properties props, String key, Object value) {
		if (value == null) props.remove(key);
		else props.put(key, value);
	}

	@Test
	public void refusals() throws Exception {
		WordprocessingMLPackage doc = WordprocessingMLPackage.createPackage();
		ChartCreator cc = new ChartCreator(new Random(1));
		try {
			cc.inDocument(doc, ChartDefinition.of(ChartType.PIE).categories("A"));
			fail("no series");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("no series"));
		}
		// nothing made by the refused call
		assertEquals(0, Charts.in(doc).size());
		assertTrue(!parts(doc).contains("/word/charts/") && !parts(doc).contains("xlsx"));
	}
}
