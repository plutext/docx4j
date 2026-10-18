/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */
package org.docx4j.model.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.junit.BeforeClass;
import org.junit.Test;

/** CR-046 phase 2: the statements the probe's token rows do not pin on their own. */
public class TextsPhase2Test {

	private static WordprocessingMLPackage probe, external;

	@BeforeClass
	public static void load() throws Exception {
		Path dir = ViewAgreementTest.resources().resolve("text-extraction/phase0");
		probe = WordprocessingMLPackage.load(dir.resolve("extraction-probe.docx").toFile());
		external = WordprocessingMLPackage.load(dir.resolve("extraction-probe-external.docm").toFile());
	}

	private static String text(WordprocessingMLPackage pkg, TextOptions options) {
		return PlainText.of(Texts.of(pkg, options));
	}

	/** The text from {@code from} up to {@code to}, {@code from} included. */
	private static String between(String text, String from, String to) {
		int i = text.indexOf(from);
		assertTrue("no '" + from + "'", i >= 0);
		int j = text.indexOf(to, i + from.length());
		assertTrue("no '" + to + "'", j >= 0);
		return text.substring(i, j);
	}

	private static List<TextBlock> blocks(TextResult result, TextBlock.Story story) {
		List<TextBlock> out = new ArrayList<TextBlock>();
		for (TextBlock b : result.blocks()) if (b.getStory() == story) out.add(b);
		return out;
	}

	private static List<TextRun> runs(TextResult result, TextRun.Kind kind) {
		List<TextRun> out = new ArrayList<TextRun>();
		for (TextBlock b : result.blocks()) for (TextRun r : b.getRuns()) if (r.getKind() == kind) out.add(r);
		return out;
	}

	private static String p(String text) {
		return "<w:p><w:r><w:t xml:space=\"preserve\">" + text + "</w:t></w:r></w:p>";
	}

	private static String tc(String props, String... paragraphs) {
		StringBuilder sb = new StringBuilder("<w:tc><w:tcPr>" + props + "</w:tcPr>");
		for (String s : paragraphs) sb.append(s.isEmpty() ? "<w:p/>" : p(s));
		return sb.append("</w:tc>").toString();
	}

	// --- tables (REQ-605) ----------------------------------------------------------------------------

	/** REQ-605: case 31 in the listed format, as TSV under tables: tsv, as a grid under tables: grid; a simple table tab-separated. */
	@Test
	public void tableForms() {
		String listed = text(probe, new TextOptions());
		// its one header cell spans the grid: written once, labelling nothing, the rows' cells a tab apart
		assertEquals("[table: listed format]\nTBL31_HEADER\nTBL31_CELL_P1; TBL31_CELL_P2\tTBL31_NESTED\n"
				+ "TBL31_MERGED\tTBL31_LAST_CELL\n", between(listed, "[table: listed format]\nTBL31", "After the table.\nCase 32"));
		// a block-level control in a cell, with Word's empty paragraph after it, is a simple table's cell
		assertEquals("SDT24_IN_CELL\n", between(listed, "SDT24_IN_CELL", "After the table.\nCase 25"));
		assertEquals("TBL31_HEADER\nTBL31_CELL_P1 TBL31_CELL_P2\tTBL31_NESTED\t\nTBL31_MERGED\tTBL31_LAST_CELL\n",
				between(text(probe, new TextOptions().tables(TextOptions.Tables.TSV)), "TBL31_HEADER", "After the table.\nCase 32"));
		assertEquals("Case 31: a table: header row, two-paragraph cell, nested table, merged cell, empty cell\n"
				+ "+---------------+------------------+-----------------+\n"
				+ "| TBL31_HEADER                                       |\n"
				+ "+===============+==================+=================+\n"
				+ "| TBL31_CELL_P1 | +--------------+ |                 |\n"
				+ "|               | | TBL31_NESTED | |                 |\n"
				+ "| TBL31_CELL_P2 | +--------------+ |                 |\n"
				+ "+---------------+------------------+-----------------+\n"
				+ "| TBL31_MERGED                     | TBL31_LAST_CELL |\n"
				+ "+---------------+------------------+-----------------+\n",
				between(text(probe, new TextOptions().tables(TextOptions.Tables.GRID)), "Case 31: a table", "After the table.\nCase 32"));
	}

	private static final String MERGED_TABLE = "<w:tbl><w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>"
			+ "<w:tr>" + tc("", "Name") + tc("", "Value") + "</w:tr>"
			+ "<w:tr>" + tc("<w:vMerge w:val=\"restart\"/>", "a") + tc("", "1") + "</w:tr>"
			+ "<w:tr>" + tc("<w:vMerge/>", "") + tc("", "2") + "</w:tr>"
			+ "<w:tr>" + tc("", "") + tc("", "3") + "</w:tr></w:tbl>" + p("After.");

	/** REQ-605: headers from the first row when no row is a w:tblHeader; a vertical merge's value on each row it covers; an empty cell left out. */
	@Test
	public void listedFormatAndGridSpans() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body(MERGED_TABLE).load();
		assertEquals("[table: listed format]\nName: a; Value: 1\nName: a; Value: 2\nValue: 3\nAfter.\n", text(pkg, new TextOptions()));
		assertEquals("+------+-------+\n"
				+ "| Name | Value |\n"
				+ "+======+=======+\n"
				+ "| a    | 1     |\n"
				+ "|      +-------+\n"
				+ "|      | 2     |\n"
				+ "+------+-------+\n"
				+ "|      | 3     |\n"
				+ "+------+-------+\n"
				+ "After.\n", text(pkg, new TextOptions().tables(TextOptions.Tables.GRID)));
		// a row whose one cell spans the grid is its text alone, a header row (labelling nothing) or a body row
		WordprocessingMLPackage headed = MiniDocx.body("<w:tbl><w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>"
				+ "<w:tr><w:trPr><w:tblHeader/></w:trPr>" + tc("", "H1") + tc("", "H2") + "</w:tr>"
				+ "<w:tr>" + tc("<w:gridSpan w:val=\"2\"/>", "wide") + "</w:tr></w:tbl>"
				+ "<w:tbl><w:tblGrid><w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/></w:tblGrid>"
				+ "<w:tr>" + tc("<w:gridSpan w:val=\"2\"/>", "only") + "</w:tr><w:tr>" + tc("", "x") + tc("", "y") + "</w:tr></w:tbl>").load();
		assertEquals("[table: listed format]\nwide\n[table: listed format]\nonly\nx\ty\n", text(headed, new TextOptions()));
	}

	private static String grid(int... widths) {
		StringBuilder sb = new StringBuilder("<w:tbl><w:tblGrid>");
		for (int w : widths) sb.append("<w:gridCol w:w=\"").append(w).append("\"/>");
		return sb.append("</w:tblGrid>").toString();
	}

	private static final String SPAN2 = "<w:gridSpan w:val=\"2\"/>", SPAN3 = "<w:gridSpan w:val=\"3\"/>",
			HEADER = "<w:trPr><w:tblHeader/></w:trPr>";

	/**
	 * REQ-605's listed format, as ruled 18 October 2026 (CR-046 phase 2): a cell is labelled by the first
	 * header with text over its grid columns, once a cell; header rows' texts joined " / "; a cell under a
	 * header with no text is its value alone; a one-cell row spanning the grid is its text alone; a table
	 * with no labels has its cells a tab apart.
	 */
	@Test
	public void listedFormatHeaders() throws Exception {
		String rows = "<w:tr>" + tc("", "P1", "P2") + tc("", "N") + tc("", "") + "</w:tr>"
				+ "<w:tr>" + tc(SPAN2, "M") + tc("", "L") + "</w:tr></w:tbl>";
		// one header a column; a cell spanning two headed columns takes the first's
		assertEquals("[table: listed format]\nA: P1; P2; B: N\nA: M; C: L\n", text(MiniDocx.body(grid(3000, 3000, 3000)
				+ "<w:tr>" + HEADER + tc("", "A") + tc("", "B") + tc("", "C") + "</w:tr>" + rows).load(), new TextOptions()));
		// a header spanning two columns labels each cell under it
		assertEquals("[table: listed format]\nAB: P1; P2; AB: N\nAB: M; C: L\n", text(MiniDocx.body(grid(3000, 3000, 3000)
				+ "<w:tr>" + HEADER + tc(SPAN2, "AB") + tc("", "C") + "</w:tr>" + rows).load(), new TextOptions()));
		// a group header over its sub-headers; a header with no text over the first column
		assertEquals("[table: listed format]\nRegion: North; Sales / 2024: 1; Sales / 2025: 2\n", text(MiniDocx.body(grid(3000, 3000, 3000)
				+ "<w:tr>" + HEADER + tc("<w:vMerge w:val=\"restart\"/>", "Region") + tc(SPAN2, "Sales") + "</w:tr>"
				+ "<w:tr>" + HEADER + tc("<w:vMerge/>", "") + tc("", "2024") + tc("", "2025") + "</w:tr>"
				+ "<w:tr>" + tc("", "North") + tc("", "1") + tc("", "2") + "</w:tr></w:tbl>").load(), new TextOptions()));
		assertEquals("[table: listed format]\nB: ab; C: c\nx; B: y; C: z\n", text(MiniDocx.body(grid(3000, 3000, 3000)
				+ "<w:tr>" + HEADER + tc("", "") + tc("", "B") + tc("", "C") + "</w:tr>"
				+ "<w:tr>" + tc(SPAN2, "ab") + tc("", "c") + "</w:tr>"
				+ "<w:tr>" + tc("", "x") + tc("", "y") + tc("", "z") + "</w:tr></w:tbl>").load(), new TextOptions()));
		// a title row over column headers, and a divider row in the body
		assertEquals("[table: listed format]\nTitle\nH1: a; H2: b\nPart B\nH1: c; H2: d\n", text(MiniDocx.body(grid(3000, 3000)
				+ "<w:tr>" + HEADER + tc(SPAN2, "Title") + "</w:tr>"
				+ "<w:tr>" + HEADER + tc("", "H1") + tc("", "H2") + "</w:tr>"
				+ "<w:tr>" + tc("", "a") + tc("", "b") + "</w:tr>"
				+ "<w:tr>" + tc(SPAN2, "Part B") + "</w:tr>"
				+ "<w:tr>" + tc("", "c") + tc("", "d") + "</w:tr></w:tbl>").load(), new TextOptions()));
		// every header row spans the grid: the body is unlabelled (no data row promoted), its cells a tab apart
		assertEquals("[table: listed format]\nTitle\na; a2\tb\n", text(MiniDocx.body(grid(3000, 3000, 3000)
				+ "<w:tr>" + HEADER + tc(SPAN3, "Title") + "</w:tr>"
				+ "<w:tr>" + tc("", "a", "a2") + tc("", "") + tc("", "b") + "</w:tr></w:tbl>").load(), new TextOptions()));
	}

	// --- math (REQ-604) --------------------------------------------------------------------------------

	/** REQ-604: math as LaTeX, $$...$$ for an m:oMathPara, $...$ inline; in markup an equation's tracked changes as its two forms. */
	@Test
	public void mathAsLatex() throws Exception {
		assertEquals("$$MTH32\\_A+\\frac{MTH32\\_B}{2}$$\n", between(text(probe, new TextOptions()), "$$MTH32", "Case 33"));
		WordprocessingMLPackage inline = MiniDocx.body("<w:p><w:r><w:t xml:space=\"preserve\">So </w:t></w:r><m:oMath><m:r><m:t>x</m:t></m:r>"
				+ "<m:sSup><m:e><m:r><m:t>y</m:t></m:r></m:e><m:sup><m:r><m:t>2</m:t></m:r></m:sup></m:sSup></m:oMath></w:p>").load();
		assertEquals("So $xy^{2}$\n", text(inline, new TextOptions()));

		WordprocessingMLPackage tracked = WordprocessingMLPackage.load(ViewAgreementTest.resources().resolve("tracked-changes-equations.docx").toFile());
		for (TextView view : new TextView[] { TextView.ACCEPTED, TextView.ORIGINAL }) {
			// the walk and the resolved copy agree on equations (REQ-406)
			WordprocessingMLPackage copy = WordprocessingMLPackage.load(ViewAgreementTest.resources().resolve("tracked-changes-equations.docx").toFile());
			if (view == TextView.ACCEPTED) org.docx4j.model.revisions.TrackedChanges.acceptAll(copy);
			else org.docx4j.model.revisions.TrackedChanges.rejectAll(copy);
			assertEquals(view.toString(), PlainText.of(Texts.ofResolved(copy, TextOptions.of(view))), text(tracked, TextOptions.of(view)));
		}
		// a structure inserted whole (the superscript) is in the accepted form only
		String line = "with units of metres per second:\n";
		String after = between(text(tracked, TextOptions.of(TextView.ACCEPTED)), line, "\nHeight").substring(line.length());
		String before = between(text(tracked, TextOptions.of(TextView.ORIGINAL)), line, "\nHeight").substring(line.length());
		assertTrue(after, after.startsWith("$$\\mathrm{m}\\mathrm{") && after.endsWith("}{\\mathrm{s}}^{\\mathrm{-1}}\\mathrm{.}$$"));
		assertFalse(before.contains("^{\\mathrm"));
		assertEquals(line + "{--" + before + "--}{++" + after + "++}", between(text(tracked, TextOptions.of(TextView.MARKUP)), line, "\nHeight"));
	}

	// --- metadata (REQ-515) ----------------------------------------------------------------------------

	/** REQ-515, REQ-801: the properties always in the metadata; in the text only under metadata: true, before the body; no statistics. */
	@Test
	public void metadata() throws Exception {
		TextResult r = Texts.of(external, new TextOptions());
		assertEquals(Boolean.TRUE, r.metadata().get(TextResult.FIELDS_UPDATE_ON_OPEN));
		assertEquals("APP52_COMPANY", ((Map<?, ?>) r.metadata().get(TextResult.APP)).get("Company"));
		assertEquals("CPR52_CUSTOM", ((Map<?, ?>) r.metadata().get(TextResult.CUSTOM)).get("ProbeCustom"));
		assertFalse(PlainText.of(r).contains("APP52_COMPANY"));
		String text = text(external, new TextOptions().metadata(true));
		assertTrue(text, text.startsWith("[application properties: Application: Microsoft Office Word; Company: APP52_COMPANY]\n"
				+ "[custom properties: ProbeCustom: CPR52_CUSTOM]\nCase 50"));
		assertTrue(text(probe, new TextOptions().metadata(true)).startsWith("[core properties: title: PRP38_TITLE; subject: PRP38_SUBJECT]\n"));

		WordprocessingMLPackage stats = MiniDocx.body(p("Body.")).packagePart("docProps/app.xml",
				"application/vnd.openxmlformats-officedocument.extended-properties+xml",
				"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties", "rIdApp",
				"<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\">"
						+ "<Template>Normal.dotm</Template><Pages>7</Pages><Words>1234</Words><Characters>5678</Characters></Properties>").load();
		Map<?, ?> app = (Map<?, ?>) Texts.of(stats, new TextOptions()).metadata().get(TextResult.APP);
		assertEquals("{Template=Normal.dotm}", app.toString());
	}

	// --- the glossary (REQ-501, REQ-516) -----------------------------------------------------------------

	/** REQ-501, REQ-516: the glossary only under include: glossary, last, after the endnotes. */
	@Test
	public void glossaryLast() {
		assertFalse(text(probe, new TextOptions()).contains("GLS54"));
		TextResult r = Texts.of(probe, new TextOptions().include(TextOptions.Include.GLOSSARY));
		List<TextBlock> glossary = blocks(r, TextBlock.Story.GLOSSARY);
		assertEquals(2, glossary.size());
		assertEquals("glossary:0/0", glossary.get(0).getAddress());
		assertEquals("GLS54_ENTRY_TWO", glossary.get(1).getText());
		List<TextBlock> all = r.blocks();
		assertEquals(glossary, all.subList(all.size() - 2, all.size()));
		assertTrue(PlainText.of(r).endsWith("[1] EN27_NOTE_TEXT\nGLS54_ENTRY_ONE\nGLS54_ENTRY_TWO\n"));
	}

	// --- images (REQ-514) -------------------------------------------------------------------------------

	/** REQ-514, REQ-707: the default describer's alt text, else the name; nothing for a decorative picture; the title never; provenance carried. */
	@Test
	public void imageDescriptions() throws Exception {
		String text = text(probe, new TextOptions().altText(true));
		assertEquals("Alt text and title: [image: IMG33_ALT_TEXT]\nDecorative: \nSVG, its PNG fallback in a:blip: [image: IMG33_SVG_ALT]\n",
				between(text, "Alt text and title:", "Case 34"));
		assertFalse(text.contains("IMG33_TITLE"));
		assertFalse(text(probe, new TextOptions()).contains("[image"));
		List<TextRun> images = runs(Texts.of(probe, new TextOptions()), TextRun.Kind.IMAGE);
		TextRun decorative = null;
		for (TextRun r : images) if (r.getImage().isDecorative()) decorative = r;
		assertNotNull(decorative);
		assertNull(decorative.getDescription());
		assertEquals("IMG33_DECORATIVE_DESCR", decorative.getImage().getDescr());
		for (TextRun r : images) if (r.getDescription() != null) assertEquals(ImageDescription.Provenance.AUTHOR, r.getDescription().getProvenance());

		// a host's describer, its text marked generated
		ImageDescriber captions = image -> image.isDecorative() ? null : ImageDescription.generated("a picture named " + image.getName());
		assertTrue(text(probe, new TextOptions().altText(true).imageDescriber(captions)).contains("Alt text and title: [image, generated: a picture named Picture 14]"));

		// VML: v:shape/@alt; a paragraph holding only a decorative picture is a decorative block
		WordprocessingMLPackage vml = MiniDocx.body("<w:p><w:r><w:pict><v:shape style=\"width:10pt;height:10pt\" alt=\"VML alt\"><v:imagedata r:id=\"rIdNone\"/></v:shape></w:pict></w:r></w:p>").load();
		assertEquals("[image: VML alt]\n", text(vml, new TextOptions().altText(true)));
		for (TextBlock b : Texts.of(probe, new TextOptions()).blocks()) assertFalse(b.getText(), b.isDecorative());
	}

	// --- charts, SmartArt, WordArt (REQ-511) --------------------------------------------------------------

	/** REQ-511: a chart's title and table as a block after its paragraph; SmartArt's lines in reading order; legacy WordArt a one-paragraph text box. */
	@Test
	public void chartsSmartArtWordArt() {
		TextResult r = Texts.of(probe, new TextOptions());
		TextBlock chart = null;
		for (TextBlock b : r.blocks()) if (b.getKind() == TextBlock.Kind.CHART) chart = b;
		assertNotNull(chart);
		assertTrue(chart.getAddress(), chart.getAddress().endsWith("/chart:0"));
		assertEquals(TextBlock.Story.MAIN, chart.getStory());
		assertEquals("CHT34_TITLE", chart.getChartTitle());
		// its description's fields (CR-047, REQ-706), and its note before the title in plain text only (REQ-707)
		assertEquals("clustered column", chart.getChart().getKind());
		assertEquals("CHT34_AXIS_TITLE", chart.getChart().getAxes().get(1).getTitle());
		assertFalse(chart.getText().contains("[chart"));
		assertEquals("[chart: clustered column; value axis: CHT34_AXIS_TITLE]\nCHT34_TITLE\n\tCHT34_SERIES_ONE\tCHT34_SERIES_TWO\n"
				+ "CHT34_CAT_A\t3\t4\nCHT34_CAT_B\t5\t2\n", between(PlainText.of(r), "[chart:", "Case 35"));
		assertEquals(Arrays.asList("", "CHT34_SERIES_ONE", "CHT34_SERIES_TWO"), chart.getChartHeader());
		assertEquals(Arrays.asList(Arrays.asList("CHT34_CAT_A", "3", "4"), Arrays.asList("CHT34_CAT_B", "5", "2")), chart.getChartRows());
		for (TextBlock b : Texts.of(probe, new TextOptions().charts(false)).blocks()) assertFalse(b.getKind() == TextBlock.Kind.CHART);

		List<TextBlock> smartArt = blocks(r, TextBlock.Story.SMARTART);
		assertEquals(3, smartArt.size());
		assertEquals(Arrays.asList("SMA35_ROOT", "SMA35_CHILD_ONE", "SMA35_CHILD_TWO"),
				Arrays.asList(smartArt.get(0).getText(), smartArt.get(1).getText(), smartArt.get(2).getText()));
		assertTrue(smartArt.get(2).getAddress(), smartArt.get(2).getAddress().endsWith("/smartart:0/2"));
		assertTrue(blocks(Texts.of(probe, new TextOptions().smartart(false)), TextBlock.Story.SMARTART).isEmpty());

		TextBlock wordArt = null;
		for (TextBlock b : blocks(r, TextBlock.Story.TEXT_BOX)) if (b.getText().equals("WA25_TEXTPATH")) wordArt = b;
		assertNotNull(wordArt);
		assertTrue(wordArt.getAddress(), wordArt.getAddress().endsWith("/textbox:0/0"));
	}

	// --- bound values (REQ-509) -------------------------------------------------------------------------

	private static final String STORE = "{11111111-2222-3333-4444-555555555555}";

	private static WordprocessingMLPackage bound(String body) throws Exception {
		return MiniDocx.body(body)
				.relationship("rIdXml", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXml", "../customXml/item1.xml")
				.entry("customXml/item1.xml", null, "<d xmlns=\"urn:t\"><a>Bound A</a><empty/></d>")
				.entry("customXml/_rels/item1.xml.rels", null, "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
						+ "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXmlProps\" Target=\"itemProps1.xml\"/></Relationships>")
				.entry("customXml/itemProps1.xml", "application/vnd.openxmlformats-officedocument.customXmlProperties+xml",
						"<ds:datastoreItem xmlns:ds=\"http://schemas.openxmlformats.org/officeDocument/2006/customXml\" ds:itemID=\"" + STORE + "\"/>")
				.packagePart("docProps/core.xml", "application/vnd.openxmlformats-package.core-properties+xml",
						"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties", "rIdCore",
						"<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"><dc:title>Core title</dc:title></cp:coreProperties>")
				.load();
	}

	private static String control(String type, String binding, boolean placeholder, String surface) {
		return "<w:p><w:sdt><w:sdtPr>" + (placeholder ? "<w:showingPlcHdr/>" : "") + binding + type + "</w:sdtPr><w:sdtContent>"
				+ "<w:r><w:t xml:space=\"preserve\">" + surface + "</w:t></w:r></w:sdtContent></w:sdt></w:p>";
	}

	private static String binding(String xpath, String store) {
		return "<w:dataBinding w:prefixMappings=\"xmlns:t='urn:t'\" w:xpath=\"" + xpath + "\" w:storeItemID=\"" + store + "\"/>";
	}

	/** REQ-509: the bound value where the binding resolves; the surface where it doesn't, for other kinds of control, and under bindings: surface; the placeholder where the value is empty; sdt.binding_differs. */
	@Test
	public void boundValues() throws Exception {
		TextResult r = Texts.of(probe, new TextOptions());
		assertTrue(PlainText.of(r).contains("The control: BND23_XML_VALUE\n"));
		assertTrue(r.warnings().toString(), r.warnings().contains(new TextWarning(TextWarning.SDT_BINDING_DIFFERS, blockWith(r, "BND23_XML_VALUE").getAddress(), null)));
		TextResult surface = Texts.of(probe, new TextOptions().bindings(TextOptions.Bindings.SURFACE));
		assertTrue(PlainText.of(surface).contains("The control: BND23_SURFACE_STALE\n"));
		assertTrue(surface.warnings().toString().contains(TextWarning.SDT_BINDING_DIFFERS));

		WordprocessingMLPackage pkg = bound(
				control("<w:text/>", binding("/t:d/t:a", STORE), false, "Bound A")                 // agrees: no warning
				+ control("<w:text/>", binding("/t:d/t:empty", STORE), true, "Click here")      // empty, a placeholder shown
				+ control("<w:text/>", binding("/t:d/t:missing", STORE), false, "Surface C")    // doesn't resolve
				+ control("", binding("/t:d/t:a", STORE), false, "Rich D")                      // rich text: the surface
				+ control("<w:text/>", binding("/ns0:coreProperties[1]/ns1:title[1]", "{6C3C8BC8-F283-45AE-878A-BAB7291924A1}")
						.replace("xmlns:t='urn:t'", "xmlns:ns0='http://schemas.openxmlformats.org/package/2006/metadata/core-properties' xmlns:ns1='http://purl.org/dc/elements/1.1/'"),
						false, "Old title"));                                                    // the core properties
		TextResult b = Texts.of(pkg, new TextOptions());
		assertEquals("Bound A\nClick here\nSurface C\nRich D\nCore title\n", PlainText.of(b));
		assertEquals(1, b.warnings().size());
		assertEquals(new TextWarning(TextWarning.SDT_BINDING_DIFFERS, "body/4", null), b.warnings().get(0));
	}

	private static TextBlock blockWith(TextResult result, String token) {
		for (TextBlock b : result.blocks()) if (b.getText().contains(token)) return b;
		throw new AssertionError("no block holds " + token);
	}

	// --- fields: update (REQ-506) -------------------------------------------------------------------------

	private static String complex(String instr, String cached, boolean locked) {
		return "<w:r><w:fldChar w:fldCharType=\"begin\"" + (locked ? " w:fldLock=\"1\"" : "") + "/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> " + instr + " </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>" + cached + "</w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	private static WordprocessingMLPackage fields() throws Exception {
		String body = "<w:p>" + complex("DOCPROPERTY Client", "old client", false) + "</w:p>"
				+ "<w:p><w:fldSimple w:instr=\" TITLE \\* Upper \"><w:r><w:t>old title</w:t></w:r></w:fldSimple></w:p>"
				+ "<w:p>" + complex("DOCVARIABLE v", "old var", false) + "</w:p>"
				+ "<w:p>" + complex("SEQ Figure", "9", false) + complex("SEQ Figure", "9", false) + complex("SEQ Figure \\r 7", "9", false)
				+ complex("SEQ Figure \\c", "9", false) + "</w:p>"
				+ "<w:p>" + complex("REF later \\h", "old ref", false) + "</w:p>"
				+ "<w:p>" + complex("QUOTE \"quoted words\"", "old quote", false) + "</w:p>"
				+ "<w:p>" + complex("DATE \\@ \"d MMMM yyyy\"", "1 January 2000", false) + "</w:p>"
				+ "<w:p>" + complex("DOCPROPERTY Client", "locked", true) + "</w:p>"
				+ "<w:p><w:bookmarkStart w:id=\"0\" w:name=\"later\"/><w:r><w:t>Target text</w:t></w:r><w:bookmarkEnd w:id=\"0\"/></w:p>";
		return MiniDocx.body(body)
				.part("settings.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml",
						"http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings", "rIdSettings",
						"<w:settings " + MiniDocx.NS + "><w:docVars><w:docVar w:name=\"v\" w:val=\"Var value\"/></w:docVars></w:settings>")
				.packagePart("docProps/core.xml", "application/vnd.openxmlformats-package.core-properties+xml",
						"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties", "rIdCore",
						"<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"><dc:title>Probe title</dc:title></cp:coreProperties>")
				.packagePart("docProps/custom.xml", "application/vnd.openxmlformats-officedocument.custom-properties+xml",
						"http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties", "rIdCustom",
						"<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/custom-properties\" xmlns:vt=\"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes\">"
								+ "<property fmtid=\"{D5CDD505-2E9C-101B-9397-08002B2CF9AE}\" pid=\"2\" name=\"Client\"><vt:lpwstr>Acme</vt:lpwstr></property></Properties>")
				.load();
	}

	/** REQ-506: cached results by default; under fields: update, the fields that need neither layout nor fetching recomputed, in the walk; DATE and a locked field never; the package unchanged. */
	@Test
	public void fieldsUpdate() throws Exception {
		WordprocessingMLPackage pkg = fields();
		assertEquals("old client\nold title\nold var\n9999\nold ref\nold quote\n1 January 2000\nlocked\nTarget text\n", text(pkg, new TextOptions()));
		String before = snapshot(pkg);
		TextResult r = Texts.of(pkg, new TextOptions().fields(TextOptions.Fields.UPDATE));
		assertEquals("Acme\nPROBE TITLE\nVar value\n1277\nTarget text\nquoted words\n1 January 2000\nlocked\nTarget text\n", PlainText.of(r));
		assertTrue(r.blocks().get(0).getFields().get(0).isUpdated());
		assertEquals("Acme", r.blocks().get(0).getFields().get(0).getResult());
		assertFalse(r.blocks().get(6).getFields().get(0).isUpdated());
		assertFalse(r.blocks().get(7).getFields().get(0).isUpdated());
		assertEquals(before, snapshot(pkg)); // REQ-204
	}

	/**
	 * REQ-506: a computed field's cached result is not read at all: a REF whose cached result holds a copy
	 * of a footnote reference and a nested field writes the bookmark's text, numbers no note and records
	 * no nested field
	 */
	@Test
	public void computedResultSuppressed() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body("<w:p><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r>"
				+ "<w:r><w:instrText xml:space=\"preserve\"> REF b \\h </w:instrText></w:r><w:r><w:fldChar w:fldCharType=\"separate\"/></w:r>"
				+ "<w:r><w:t>Old</w:t></w:r><w:r><w:footnoteReference w:id=\"1\"/></w:r><w:fldSimple w:instr=\" DOCPROPERTY Client \"/>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>"
				+ "<w:p><w:bookmarkStart w:id=\"0\" w:name=\"b\"/><w:r><w:t>Target</w:t></w:r><w:r><w:footnoteReference w:id=\"1\"/></w:r>"
				+ "<w:bookmarkEnd w:id=\"0\"/></w:p>")
				.footnotes("<w:footnote w:id=\"1\"><w:p><w:r><w:footnoteRef/></w:r><w:r><w:t xml:space=\"preserve\"> NOTE_ONE</w:t></w:r></w:p></w:footnote>").load();
		assertEquals("Old[1]\nTarget[1]\n[1] NOTE_ONE\n", text(pkg, new TextOptions()));
		TextResult r = Texts.of(pkg, new TextOptions().fields(TextOptions.Fields.UPDATE));
		assertEquals("Target\nTarget[1]\n[1] NOTE_ONE\n", PlainText.of(r));
		TextBlock first = r.blocks().get(0);
		for (TextRun run : first.getRuns()) assertNotEquals(TextRun.Kind.NOTE_REFERENCE, run.getKind());
		assertEquals(1, first.getFields().size());
		assertTrue(first.getFields().get(0).isUpdated());
		for (TextWarning w : r.warnings()) assertNotEquals(TextWarning.FIELD_NO_RESULT, w.getKind());
		// under field_codes, the REF's code but not the nested field's
		TextResult codes = Texts.of(pkg, new TextOptions().fields(TextOptions.Fields.UPDATE).fieldCodes(true));
		StringBuilder code = new StringBuilder();
		for (TextRun run : codes.blocks().get(0).getRuns()) if (run.getKind() == TextRun.Kind.FIELD_CODE) code.append(run.getText());
		assertEquals(" REF b \\h ", code.toString());
	}

	/** REQ-506 on a part's route, which has no first reading: a REF after its bookmark computed, one before it cached */
	@Test
	public void refOnThePartRoute() throws Exception {
		String ref = "<w:p><w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> REF b \\h </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>Old</w:t></w:r><w:r><w:fldChar w:fldCharType=\"end\"/></w:r></w:p>";
		String bookmark = "<w:p><w:bookmarkStart w:id=\"0\" w:name=\"b\"/><w:r><w:t>Target</w:t></w:r><w:bookmarkEnd w:id=\"0\"/></w:p>";
		TextOptions update = new TextOptions().fields(TextOptions.Fields.UPDATE);
		// the bookmark before any field is computed
		WordprocessingMLPackage after = MiniDocx.body(bookmark + ref).load();
		assertEquals("Target\nTarget\n", PlainText.of(Texts.of(after.getMainDocumentPart(), update)));
		WordprocessingMLPackage before = MiniDocx.body(ref + bookmark).load();
		assertEquals("Old\nTarget\n", PlainText.of(Texts.of(before.getMainDocumentPart(), update)));
		assertEquals("Target\nTarget\n", PlainText.of(Texts.of(before, update)));
	}

	// --- content.not_read and image.not_described (REQ-801) ----------------------------------------------

	private static boolean warned(TextResult r, String kind, String address, String partName) {
		return r.warnings().contains(new TextWarning(kind, address, null, null, partName));
	}

	/** REQ-801: content that can't be read contributes nothing and raises content.not_read, the part's name its selector, the anchor's address its address */
	@Test
	public void contentNotRead() throws Exception {
		// an embedded package that isn't one
		WordprocessingMLPackage object = MiniDocx.body("<w:p><w:r><w:t>Before</w:t></w:r><w:r><w:object><o:OLEObject Type=\"Embed\" ProgID=\"Excel.Sheet.12\" r:id=\"rIdE\"/></w:object></w:r></w:p>")
				.part("embeddings/broken.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
						"http://schemas.openxmlformats.org/officeDocument/2006/relationships/package", "rIdE",
						"not a zip".getBytes(java.nio.charset.StandardCharsets.UTF_8)).load();
		TextResult r = Texts.of(object, new TextOptions().embedded(true));
		assertTrue(r.warnings().toString(), warned(r, TextWarning.CONTENT_NOT_READ, "body/0", "/word/embeddings/broken.xlsx"));
		assertEquals("Before\n", PlainText.of(r));
		assertTrue(Texts.of(object, new TextOptions()).warnings().isEmpty()); // not read at all without embedded: true
		// a chart part and a SmartArt data part that can't be read
		String inline = "<w:p><w:r><w:t>Chart</w:t></w:r><w:r><w:drawing><wp:inline><wp:extent cx=\"1\" cy=\"1\"/><wp:docPr id=\"1\" name=\"C\"/><a:graphic>"
				+ "<a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/chart\"><c:chart xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\" r:id=\"rIdC\"/>"
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>"
				+ "<w:p><w:r><w:t>Diagram</w:t></w:r><w:r><w:drawing><wp:inline><wp:extent cx=\"1\" cy=\"1\"/><wp:docPr id=\"2\" name=\"D\"/><a:graphic>"
				+ "<a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/diagram\"><dgm:relIds xmlns:dgm=\"http://schemas.openxmlformats.org/drawingml/2006/diagram\" r:dm=\"rIdD\" r:lo=\"\" r:qs=\"\" r:cs=\"\"/>"
				+ "</a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>";
		WordprocessingMLPackage drawings = MiniDocx.body(inline)
				.part("charts/chart1.xml", "application/vnd.openxmlformats-officedocument.drawingml.chart+xml",
						"http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart", "rIdC",
						"<notAChart/>")
				.part("diagrams/data1.xml", "application/vnd.openxmlformats-officedocument.drawingml.diagramData+xml",
						"http://schemas.openxmlformats.org/officeDocument/2006/relationships/diagramData", "rIdD",
						"<notData/>").load();
		r = Texts.of(drawings, new TextOptions());
		assertTrue(r.warnings().toString(), warned(r, TextWarning.CONTENT_NOT_READ, "body/0", "/word/charts/chart1.xml"));
		assertTrue(r.warnings().toString(), warned(r, TextWarning.CONTENT_NOT_READ, "body/1", "/word/diagrams/data1.xml"));
		assertEquals("Chart\nDiagram\n", PlainText.of(r));
	}

	/** REQ-514, REQ-801: a describer that fails leaves the image undescribed and raises image.not_described, the image part's name its selector */
	@Test
	public void imageNotDescribed() {
		ImageDescriber failing = image -> {
			throw new IllegalStateException("the describing service is down");
		};
		TextResult r = Texts.of(probe, new TextOptions().altText(true).imageDescriber(failing));
		boolean found = false;
		for (TextWarning w : r.warnings()) {
			if (TextWarning.IMAGE_NOT_DESCRIBED.equals(w.getKind())) {
				found = true;
				assertTrue(w.getSelector(), w.getSelector().startsWith("/word/media/"));
			}
		}
		assertTrue(r.warnings().toString(), found);
		assertFalse(PlainText.of(r).contains("[image"));
	}

	/**
	 * REQ-506: FORMTEXT under fields: update as Word's update leaves it, Word's own save after F9 the
	 * oracle (text-extraction/formtext): its default text, or five en spaces where it has none; in a
	 * document protected for forms, what was typed
	 */
	@Test
	public void formTextAsWordUpdatesIt() throws Exception {
		Path dir = ViewAgreementTest.resources().resolve("text-extraction/formtext");
		WordprocessingMLPackage before = WordprocessingMLPackage.load(dir.resolve("cr046-formtext-unprotected.docx").toFile());
		WordprocessingMLPackage word = WordprocessingMLPackage.load(dir.resolve("cr046-formtext-unprotected-after-f9.docx").toFile());
		TextOptions update = new TextOptions().fields(TextOptions.Fields.UPDATE);
		String ours = PlainText.of(Texts.of(before, update));
		String words = PlainText.of(Texts.of(word, new TextOptions()));
		String a = "A: typed over a default: FT_DEFAULT\n", b = "B: typed, no default: \u2002\u2002\u2002\u2002\u2002\n";
		assertTrue(words, words.startsWith(a + b));
		assertTrue(ours, ours.startsWith(a + b)); // C, a formula, docx4j doesn't compute
		String typed = "A: typed over a default: FT_TYPED\nB: typed, no default: FT_TYPED_NO_DEFAULT\n";
		assertTrue(PlainText.of(Texts.of(before, new TextOptions())).startsWith(typed));
		WordprocessingMLPackage protectedForm = WordprocessingMLPackage.load(dir.resolve("cr046-formtext-protected-update-on-open.docx").toFile());
		assertTrue(PlainText.of(Texts.of(protectedForm, update)).startsWith(typed));
	}

	// --- altChunk (REQ-513) -------------------------------------------------------------------------------

	private static final String HTML = "<html><head><title>T</title><style>p{}</style></head><body><p>One &amp; two</p>"
			+ "<div>Three<br>Four</div><table><tr><td>a</td><td>b</td></tr></table><script>no()</script></body></html>";

	/** REQ-513: HTML read as blocks through jsoup where present, its tags stripped otherwise; text chunks as lines; RTF never; altchunks: false; one level. */
	@Test
	public void altChunks() throws Exception {
		List<String> stripped = new ArrayList<String>();
		for (TextBlock b : HtmlText.stripped(HTML, TextBlock.Story.MAIN, "c")) stripped.add(b.getText());
		assertEquals(Arrays.asList("One & two", "Three\nFour", "a\tb"), stripped);
		assertTrue(HtmlText.hasJsoup()); // a test dependency of docx4j-core-tests
		List<TextBlock> tidied = HtmlText.blocks(HTML, TextBlock.Story.MAIN, "c");
		assertEquals(3, tidied.size());
		assertEquals("One & two", tidied.get(0).getText());
		assertEquals("Three\nFour", tidied.get(1).getText());
		assertEquals(TextBlock.Kind.TABLE, tidied.get(2).getKind());
		assertEquals("a\tb", tidied.get(2).getText());

		String aFChunk = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/aFChunk";
		WordprocessingMLPackage pkg = MiniDocx.body(p("Before.") + "<w:altChunk r:id=\"rIdT\"/><w:altChunk r:id=\"rIdR\"/><w:altChunk r:id=\"rIdH\"/>" + p("After."))
				.part("t.txt", "text/plain", aFChunk, "rIdT", "line one\r\nline two\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8))
				.part("r.rtf", "application/rtf", aFChunk, "rIdR", "{\\rtf1 RTF text}".getBytes(java.nio.charset.StandardCharsets.UTF_8))
				.part("h.htm", "text/html", aFChunk, "rIdH", HTML.getBytes(java.nio.charset.StandardCharsets.UTF_8)).load();
		String before = snapshot(pkg);
		TextResult r = Texts.of(pkg, new TextOptions());
		assertEquals("Before.\nline one\nline two\nOne & two\nThree\nFour\na\tb\nAfter.\n", PlainText.of(r));
		assertEquals("[altchunk.not_expanded at body/2 (application/rtf)]", r.warnings().toString());
		assertEquals("body/1/altchunk:0/1", r.blocks().get(2).getAddress());
		TextResult off = Texts.of(pkg, new TextOptions().altchunks(false));
		assertEquals("Before.\nAfter.\n", PlainText.of(off));
		assertEquals(3, off.warnings().size());
		assertEquals("text/plain", off.warnings().get(0).getContentType());
		assertEquals(before, snapshot(pkg)); // REQ-204

		// the probe's document chunk holds an HTML chunk, which is not expanded (one level)
		TextResult probeResult = Texts.of(probe, new TextOptions());
		TextWarning nested = null;
		for (TextWarning w : probeResult.warnings()) if (w.getAddress() != null && w.getAddress().contains("/altchunk:0/")) nested = w;
		assertNotNull(probeResult.warnings().toString(), nested);
		assertEquals("text/html", nested.getContentType());
	}

	// --- embedded packages (REQ-512) ------------------------------------------------------------------

	/** REQ-512: objects as object runs with their ProgIDs; under embedded: true, each package read at its reference, one level deep. */
	@Test
	public void embeddedPackages() {
		List<String> progIds = new ArrayList<String>();
		for (TextRun r : runs(Texts.of(probe, new TextOptions()), TextRun.Kind.OBJECT)) progIds.add(r.getProgId());
		assertEquals(Arrays.asList("Excel.Sheet.12", "Word.Document.12", "Package", null, "PowerPoint.Show.12"), progIds);

		TextResult r = Texts.of(probe, new TextOptions().embedded(true));
		Map<String, String> texts = new LinkedHashMap<String, String>();
		for (TextBlock b : blocks(r, TextBlock.Story.EMBEDDED)) texts.put(b.getAddress().replaceFirst("^body/\\d+/", ""), b.getText());
		assertEquals("Probe", texts.get("object:0/sheet:0"));
		assertEquals("EMB39_XLSX_CELL", texts.get("object:0/sheet:0/table"));
		assertEquals("EMB40_DOCX_TEXT", texts.get("object:0/body/0"));
		assertEquals("OLE41_NATIVE_TEXT", texts.get("object:0/0"));
		assertEquals("EMB53_SLIDE_ONE", texts.get("object:0/slide:0/0"));
		assertEquals("EMB53_SLIDE_TWO", texts.get("object:0/slide:2/0"));
		String text = PlainText.of(r);
		assertFalse(text.contains("EMB40_NESTED_TEXT"));
		assertFalse(text.contains("EMB53_HIDDEN_SLIDE"));
		assertFalse(text.contains("EMB53_SPEAKER_NOTES"));
		assertFalse(text.contains("ACX42_CAPTION"));
		// a hidden slide is read as hidden text is
		assertTrue(text(probe, new TextOptions().embedded(true).hidden(true)).contains("EMB53_HIDDEN_SLIDE"));
	}

	/**
	 * REQ-512: an embedded workbook's hidden sheets (state hidden or veryHidden) are left out as hidden text
	 * is, and read under hidden: true, in workbook order, each preceded by its name; a sheet keeps its
	 * workbook index in its address either way.
	 */
	@Test
	public void hiddenSheets() throws Exception {
		org.docx4j.openpackaging.packages.SpreadsheetMLPackage xl = org.docx4j.openpackaging.packages.SpreadsheetMLPackage.createPackage();
		String[][] sheets = { { "Shown", null, "SHT_SHOWN" }, { "Hidden", "hidden", "SHT_HIDDEN" },
				{ "VeryHidden", "veryHidden", "SHT_VERY_HIDDEN" }, { "Last", null, "SHT_LAST" } };
		for (int i = 0; i < sheets.length; i++) {
			org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart ws = xl.createWorksheetPart(
					new org.docx4j.openpackaging.parts.PartName("/xl/worksheets/sheet" + (i + 1) + ".xml"), sheets[i][0], i + 1);
			org.xlsx4j.sml.Row row = new org.xlsx4j.sml.Row();
			row.setR(1L);
			org.xlsx4j.sml.Cell c = cell(org.xlsx4j.sml.STCellType.STR, sheets[i][2], 0);
			c.setR("A1");
			row.getC().add(c);
			ws.getJaxbElement().getSheetData().getRow().add(row);
			if (sheets[i][1] != null) {
				xl.getWorkbookPart().getJaxbElement().getSheets().getSheet().get(i)
						.setState(org.xlsx4j.sml.STSheetState.fromValue(sheets[i][1]));
			}
		}
		java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
		xl.save(bytes);
		WordprocessingMLPackage doc = MiniDocx.body("<w:p><w:r><w:t>Before</w:t></w:r><w:r><w:object><o:OLEObject Type=\"Embed\" ProgID=\"Excel.Sheet.12\" r:id=\"rIdE\"/></w:object></w:r></w:p>")
				.part("embeddings/book.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
						"http://schemas.openxmlformats.org/officeDocument/2006/relationships/package", "rIdE", bytes.toByteArray()).load();

		String shown = text(doc, new TextOptions().embedded(true));
		assertTrue(shown, shown.contains("SHT_SHOWN") && shown.contains("SHT_LAST"));
		assertFalse(shown, shown.contains("SHT_HIDDEN") || shown.contains("SHT_VERY_HIDDEN") || shown.contains("VeryHidden"));
		assertFalse(text(doc, new TextOptions().hidden(true)).contains("SHT_HIDDEN")); // nothing opened without embedded: true

		TextResult all = Texts.of(doc, new TextOptions().embedded(true).hidden(true));
		Map<String, String> texts = new LinkedHashMap<String, String>();
		for (TextBlock b : blocks(all, TextBlock.Story.EMBEDDED)) texts.put(b.getAddress().replaceFirst("^body/\\d+/", ""), b.getText());
		assertEquals(Arrays.asList("object:0/sheet:0", "object:0/sheet:0/table", "object:0/sheet:1", "object:0/sheet:1/table",
				"object:0/sheet:2", "object:0/sheet:2/table", "object:0/sheet:3", "object:0/sheet:3/table"), new ArrayList<String>(texts.keySet()));
		assertEquals("Hidden", texts.get("object:0/sheet:1"));
		assertEquals("SHT_HIDDEN", texts.get("object:0/sheet:1/table"));
		assertEquals("SHT_VERY_HIDDEN", texts.get("object:0/sheet:2/table"));
		// and without hidden: true the last sheet keeps its index
		Map<String, String> visible = new LinkedHashMap<String, String>();
		for (TextBlock b : blocks(Texts.of(doc, new TextOptions().embedded(true)), TextBlock.Story.EMBEDDED))
			visible.put(b.getAddress().replaceFirst("^body/\\d+/", ""), b.getText());
		assertEquals("SHT_LAST", visible.get("object:0/sheet:3/table"));
	}

	/** REQ-512: a workbook's cells as Excel displays them, by their number formats in the US locale; a formula's cached value. */
	@Test
	public void workbookCells() {
		org.xlsx4j.sml.CTStylesheet styles = new org.xlsx4j.sml.CTStylesheet();
		styles.setCellXfs(new org.xlsx4j.sml.CTCellXfs());
		for (long id : new long[] { 0, 14, 164, 4 }) {
			org.xlsx4j.sml.CTXf xf = new org.xlsx4j.sml.CTXf();
			xf.setNumFmtId(id);
			styles.getCellXfs().getXf().add(xf);
		}
		styles.setNumFmts(new org.xlsx4j.sml.CTNumFmts());
		org.xlsx4j.sml.CTNumFmt percent = new org.xlsx4j.sml.CTNumFmt();
		percent.setNumFmtId(164);
		percent.setFormatCode("0.00%");
		styles.getNumFmts().getNumFmt().add(percent);
		org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter df = new org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter(java.util.Locale.US);
		assertEquals("3.5", Embedded.display(cell(null, "3.5", 0), new ArrayList<org.xlsx4j.sml.CTRst>(), styles, df, false));
		assertEquals("3/15/23", Embedded.display(cell(null, "45000", 1), new ArrayList<org.xlsx4j.sml.CTRst>(), styles, df, false));
		assertEquals("25.60%", Embedded.display(cell(null, "0.256", 2), new ArrayList<org.xlsx4j.sml.CTRst>(), styles, df, false));
		assertEquals("1,234.50", Embedded.display(cell(null, "1234.5", 3), new ArrayList<org.xlsx4j.sml.CTRst>(), styles, df, false));
		assertEquals("TRUE", Embedded.display(cell(org.xlsx4j.sml.STCellType.B, "1", 0), new ArrayList<org.xlsx4j.sml.CTRst>(), styles, df, false));
		assertEquals("#N/A", Embedded.display(cell(org.xlsx4j.sml.STCellType.E, "#N/A", 0), new ArrayList<org.xlsx4j.sml.CTRst>(), styles, df, false));
		org.xlsx4j.sml.Cell formula = cell(org.xlsx4j.sml.STCellType.STR, "cached", 0);
		formula.setF(new org.xlsx4j.sml.CTCellFormula());
		formula.getF().setValue("A1&\"x\"");
		assertEquals("cached", Embedded.display(formula, new ArrayList<org.xlsx4j.sml.CTRst>(), styles, df, false));
		assertEquals(Arrays.asList(28, 12), Arrays.asList(Embedded.ref("AB12")[0], Embedded.ref("AB12")[1]));
	}

	private static org.xlsx4j.sml.Cell cell(org.xlsx4j.sml.STCellType t, String v, long s) {
		org.xlsx4j.sml.Cell c = new org.xlsx4j.sml.Cell();
		if (t != null) c.setT(t);
		c.setV(v);
		c.setS(s);
		return c;
	}

	// --- page markers and indents (REQ-708, REQ-709) --------------------------------------------------------

	/** REQ-708: [page n] at each w:lastRenderedPageBreak of the main story, under page_markers: true only. */
	@Test
	public void pageMarkers() throws Exception {
		WordprocessingMLPackage resaved = WordprocessingMLPackage.load(
				ViewAgreementTest.resources().resolve("text-extraction/extraction-probe.word-resave.docx").toFile());
		assertFalse(text(resaved, new TextOptions()).contains("[page "));
		String text = text(resaved, new TextOptions().pageMarkers(true));
		List<TextRun> markers = runs(Texts.of(resaved, new TextOptions()), TextRun.Kind.PAGE_MARKER);
		assertFalse(markers.isEmpty());
		for (int i = 0; i < markers.size(); i++) {
			assertEquals(i + 2, markers.get(i).getPageNumber());
			assertTrue(text.contains("[page " + (i + 2) + "]"));
		}
		assertFalse(text.contains("[page " + (markers.size() + 2) + "]"));
	}

	/** REQ-709: under layout: indent, four spaces a 720-twip tab stop of a paragraph's first-line indent; tabs kept; none in a table. */
	@Test
	public void layoutIndent() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body(
				"<w:p><w:pPr><w:ind w:left=\"720\"/></w:pPr><w:r><w:t>one stop</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:ind w:left=\"1440\" w:firstLine=\"720\"/></w:pPr><w:r><w:t>three</w:t></w:r></w:p>"
				+ "<w:p><w:pPr><w:ind w:left=\"1440\" w:hanging=\"720\"/></w:pPr><w:r><w:t>a</w:t><w:tab/><w:t>b</w:t></w:r></w:p>"
				+ p("none")
				+ "<w:tbl><w:tblGrid><w:gridCol w:w=\"2000\"/></w:tblGrid><w:tr><w:tc><w:p><w:pPr><w:ind w:left=\"720\"/></w:pPr><w:r><w:t>cell</w:t></w:r></w:p></w:tc></w:tr></w:tbl>")
				.part("styles.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml",
						"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles", "rIdStyles",
						"<w:styles " + MiniDocx.NS + "/>")
				.load();
		assertEquals("one stop\nthree\na\tb\nnone\ncell\n", text(pkg, new TextOptions()));
		assertEquals("    one stop\n            three\n    a\tb\nnone\ncell\n", text(pkg, new TextOptions().layout(TextOptions.Layout.INDENT)));
	}

	// --- REQ-204 ---------------------------------------------------------------------------------------

	private static TextOptions everything(TextView view) {
		return TextOptions.of(view).fields(TextOptions.Fields.UPDATE).embedded(true).metadata(true).altText(true)
				.include(TextOptions.Include.values()).tables(TextOptions.Tables.GRID).pageMarkers(true)
				.layout(TextOptions.Layout.INDENT).hidden(true).listLabels(true).fieldCodes(true);
	}

	/** REQ-204: with every option of phase 2 on, in every view, the probe's parts are as they were. */
	@Test
	public void packageUnchanged() throws Exception {
		WordprocessingMLPackage probe = WordprocessingMLPackage.load(ViewAgreementTest.resources().resolve("text-extraction/phase0/extraction-probe.docx").toFile());
		String before = snapshot(probe);
		for (TextView view : TextView.values()) Texts.of(probe, everything(view));
		assertEquals(before, snapshot(probe));
		String externalBefore = snapshot(external);
		Texts.of(external, new TextOptions().fields(TextOptions.Fields.UPDATE).embedded(true).metadata(true));
		assertEquals(externalBefore, snapshot(external));
	}

	/**
	 * Every part's content, namespace declarations included: the XML of a JAXB part, the bytes of a
	 * binary one, by part name.  Until CR-049 the declarations were left out, since XmlUtils' marshal
	 * methods left a {@code wml.Document}'s {@code mc:Ignorable} prefixes on the shared prefix mapper and
	 * what was marshalled after the main document part declared them.
	 */
	static String snapshot(WordprocessingMLPackage pkg) throws Exception {
		StringBuilder sb = new StringBuilder();
		List<String> names = new ArrayList<String>();
		for (org.docx4j.openpackaging.parts.PartName n : pkg.getParts().getParts().keySet()) names.add(n.getName());
		java.util.Collections.sort(names);
		for (String name : names) {
			Part part = pkg.getParts().get(new org.docx4j.openpackaging.parts.PartName(name));
			sb.append(name).append('\n');
			if (part instanceof JaxbXmlPart) sb.append(XmlUtils.marshaltoString(((JaxbXmlPart<?>) part).getJaxbElement(), true, false, ((JaxbXmlPart<?>) part).getJAXBContext()));
			else if (part instanceof BinaryPart) sb.append(Arrays.hashCode(((BinaryPart) part).getBytes()));
			if (part.getRelationshipsPart(false) != null) sb.append(XmlUtils.marshaltoString(part.getRelationshipsPart(false).getJaxbElement(), true, false, org.docx4j.jaxb.Context.jcRelationships));
			sb.append('\n');
		}
		// the declarations too: XmlUtils' marshal leaves the prefix mapper as it found it (CR-049), so
		// what a part declares no longer depends on what the thread marshalled before
		return sb.toString();
	}
}
