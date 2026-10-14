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
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.writer.AbstractTableWriter;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblWidth;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A table whose own {@code w:tblW} contradicts its {@code w:tblGrid} is laid out at its
 * width, the cells' widths scaled to it (CR-041 phase 2), as Word lays it out: measured on
 * Word's re-saves of the table-align-survey and table-align-p0 probes (cases F6, F7, F11,
 * F12, W1, W4, W6, W9).  The grid stands where it agrees with the width, where the cells
 * agree with the width and the grid is wider (F8), in a table nested in a cell, for a
 * percentage in a section of several columns, and with
 * {@code docx4j.convert.out.fo.tables.refitStaleGrid=false}.  Both FO pathways.
 */
public class TableStatedWidthTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait with 1in margins: a 9026 twip (451.3pt) text column. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String pathway(int flags) {
		return flags == Docx4J.FLAG_NONE ? "visitor: " : "XSLT: ";
	}

	private static String cell(String tcW, String text) {
		return "<w:tc>" + (tcW == null ? "" : "<w:tcPr>" + tcW + "</w:tcPr>")
				+ "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc>";
	}

	private static String dxa(int w) {
		return "<w:tcW w:type=\"dxa\" w:w=\"" + w + "\"/>";
	}

	private static String pct(int w) {
		return "<w:tcW w:type=\"pct\" w:w=\"" + w + "\"/>";
	}

	/** A one-row table; {@code tcW} null for a cell stating no width. */
	private static String table(String tblPr, int[] grid, String[] tcW) {
		StringBuilder sb = new StringBuilder("<w:tbl><w:tblPr>" + tblPr + "</w:tblPr><w:tblGrid>");
		for (int g : grid) sb.append("<w:gridCol w:w=\"" + g + "\"/>");
		sb.append("</w:tblGrid><w:tr>");
		for (int i = 0; i < tcW.length; i++) sb.append(cell(tcW[i], "c" + i));
		return sb.append("</w:tr></w:tbl>").toString();
	}

	private static String tblW(int w, String type) {
		return "<w:tblW w:w=\"" + w + "\" w:type=\"" + type + "\"/>";
	}

	private static final String FIXED = "<w:tblLayout w:type=\"fixed\"/>";

	private static org.w3c.dom.Document fo(WordprocessingMLPackage pkg, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static WordprocessingMLPackage pkg(String body, String sectPr, int compatMode) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getDocumentSettingsPart()
				.setWordCompatSetting("compatibilityMode", Integer.toString(compatMode));
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "<w:p/>" + sectPr + "</w:body></w:document>"));
		return pkg;
	}

	/** The n-th fo:table's column widths in twips (0 the first table, 1 one nested in it). */
	private static double[] columns(String body, int flags) throws Exception {
		return columns(fo(pkg(body, SECT_PR, 15), flags), 0);
	}

	private static double[] columns(org.w3c.dom.Document doc, int n) {
		NodeList tables = doc.getElementsByTagNameNS(FO_NS, "table");
		assertTrue("fo:table " + n, tables.getLength() > n);
		Element tbl = (Element) tables.item(n);
		java.util.List<Double> out = new java.util.ArrayList<>();
		for (org.w3c.dom.Node c = tbl.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c instanceof Element && "table-column".equals(c.getLocalName())) {
				out.add(twips(((Element) c).getAttribute("column-width")));
			}
		}
		double[] a = new double[out.size()];
		for (int i = 0; i < a.length; i++) a[i] = out.get(i);
		return a;
	}

	private static double twips(String length) {
		double v = Double.parseDouble(length.substring(0, length.length() - 2));
		if (length.endsWith("pt")) return v * 20;
		if (length.endsWith("in")) return v * 1440;
		if (length.endsWith("mm")) return v * 1440 / 25.4;
		throw new IllegalArgumentException(length);
	}

	private static double sum(double[] a) {
		double s = 0;
		for (double v : a) s += v;
		return s;
	}

	private static void assertColumns(String message, double[] expected, double[] actual, double tolerance) {
		assertEquals(message + ": columns", expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(message + ": column " + (i + 1), expected[i], actual[i], tolerance);
		}
	}

	/** F6: a 9026-twip table over a grid of 3 x 2000 its cells repeat - TblFactory's grid
	 *  under a width set afterwards.  Word: 3008 / 3009 / 3009.  docx4j drew 6000. */
	@Test
	public void aGridItsWidthContradictsGivesWay() throws Exception {
		String body = table(tblW(9026, "dxa"), new int[] { 2000, 2000, 2000 },
				new String[] { dxa(2000), dxa(2000), dxa(2000) });
		for (int flags : FLAGS) {
			assertColumns(pathway(flags), new double[] { 3009, 3009, 3008 }, columns(body, flags), 1.5);
		}
	}

	/** F7: the cells agree with the width, the grid does not.  Word: 3008 / 3009 / 3009. */
	@Test
	public void cellsAgreeingWithTheWidthOverAShortGrid() throws Exception {
		String body = table(tblW(9026, "dxa"), new int[] { 2000, 2000, 2000 },
				new String[] { dxa(3009), dxa(3009), dxa(3009) });
		for (int flags : FLAGS) {
			assertEquals(pathway(flags), 9026, sum(columns(body, flags)), 1);
		}
	}

	/** W1 and W6: the cells' proportions, not the grid's.  Cells of 3000 / 2000 / 1000 over
	 *  a grid of 1000 / 2000 / 3000 under 9026 twips: Word 4513 / 3009 / 1504, autofit
	 *  and fixed alike. */
	@Test
	public void theCellsProportionsNotTheGrids() throws Exception {
		for (String layout : new String[] { "", FIXED }) {
			String body = table(tblW(9026, "dxa") + layout, new int[] { 1000, 2000, 3000 },
					new String[] { dxa(3000), dxa(2000), dxa(1000) });
			for (int flags : FLAGS) {
				assertColumns(pathway(flags) + (layout.isEmpty() ? "autofit" : "fixed"),
						new double[] { 4513, 3009, 1504 }, columns(body, flags), 1.5);
			}
		}
	}

	/** W9: percentage cells are percentages of the table's width.  Cells of 20 / 30 / 50%
	 *  over a grid of 3 x 2000 under 9026 twips: Word 1805 / 2708 / 4513. */
	@Test
	public void percentageCellsAreOfTheTablesWidth() throws Exception {
		String body = table(tblW(9026, "dxa"), new int[] { 2000, 2000, 2000 },
				new String[] { pct(1000), pct(1500), pct(2500) });
		for (int flags : FLAGS) {
			assertColumns(pathway(flags), new double[] { 1805, 2708, 4513 }, columns(body, flags), 1.5);
		}
	}

	/** F12: a 100% fixed-layout table over a grid of 3 x 2000 its cells repeat: the whole
	 *  column (Word 3005 / 3006 / 3006), and below mode 15 the column and both cell
	 *  margins (Word's re-saved mode-14 100% grids sum to 9243). */
	@Test
	public void aFixedPercentageTableTakesItsPercentage() throws Exception {
		String body = table(tblW(5000, "pct") + FIXED, new int[] { 2000, 2000, 2000 },
				new String[] { dxa(2000), dxa(2000), dxa(2000) });
		for (int flags : FLAGS) {
			assertEquals(pathway(flags) + "mode 15", 9026, sum(columns(fo(pkg(body, SECT_PR, 15), flags), 0)), 1);
			assertEquals(pathway(flags) + "mode 14", 9242, sum(columns(fo(pkg(body, SECT_PR, 14), flags), 0)), 1);
		}
	}

	/** W4: no column below its content.  Three 2500-twip cells holding one long word each,
	 *  under a 4000-twip w:tblW: Word gives each the word (3 x 2243), wider than the table
	 *  asked for, not 3 x 1333. */
	@Test
	public void noColumnBelowItsContent() throws Exception {
		String word = "<w:tc><w:tcPr>" + dxa(2500) + "</w:tcPr><w:p><w:r><w:t>Antidisestablishment</w:t></w:r></w:p></w:tc>";
		String body = "<w:tbl><w:tblPr>" + tblW(4000, "dxa") + "</w:tblPr><w:tblGrid>"
				+ "<w:gridCol w:w=\"2500\"/><w:gridCol w:w=\"2500\"/><w:gridCol w:w=\"2500\"/></w:tblGrid>"
				+ "<w:tr>" + word + word + word + "</w:tr></w:tbl>";
		for (int flags : FLAGS) {
			double[] cols = columns(body, flags);
			assertEquals(pathway(flags), 3, cols.length);
			assertTrue(pathway(flags) + "each column holds its word: " + cols[0], cols[0] > 1600 && cols[0] < 2500);
			assertEquals(pathway(flags) + "equal columns", cols[0], cols[2], 2);
		}
	}

	/** F8: the cells agree with the table's width and the grid is wider - the shape Word
	 *  kept in five of the corpora's six tables: the grid stands. */
	@Test
	public void aWiderGridTheCellsDoNotRepeatStands() throws Exception {
		String body = table(tblW(6000, "dxa"), new int[] { 3009, 3009, 3009 },
				new String[] { dxa(2000), dxa(2000), dxa(2000) });
		for (int flags : FLAGS) {
			assertColumns(pathway(flags), new double[] { 3009, 3009, 3009 }, columns(body, flags), 1);
		}
	}

	/** A grid within 1% of the width is the table's layout, as before (and fixed layout's
	 *  grid with it). */
	@Test
	public void aGridWithinOnePerCentStands() throws Exception {
		for (String layout : new String[] { "", FIXED }) {
			String body = table(tblW(9026, "dxa") + layout, new int[] { 2990, 2990, 2990 },
					new String[] { dxa(2990), dxa(2990), dxa(2990) });
			for (int flags : FLAGS) {
				assertColumns(pathway(flags), new double[] { 2990, 2990, 2990 }, columns(body, flags), 1);
			}
		}
	}

	/** A full-width one-cell table holding the given table, its cell margins Word's 108
	 *  twips, stated (containingCellWidthTwips reads the table's own w:tblCellMar). */
	private static String nested(String inner) {
		return "<w:tbl><w:tblPr>" + tblW(5000, "pct") + "<w:tblCellMar><w:left w:w=\"108\" w:type=\"dxa\"/>"
				+ "<w:right w:w=\"108\" w:type=\"dxa\"/></w:tblCellMar></w:tblPr><w:tblGrid><w:gridCol w:w=\"9026\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr>" + dxa(9026) + "</w:tcPr><w:p/>" + inner + "<w:p/></w:tc></w:tr></w:tbl>";
	}

	/** A table nested in a cell answers to the cell: an absolute width its grid falls short
	 *  of leaves the grid standing, as Word keeps the corpora's nested grids (the only
	 *  Word-written grids short of a stated width are four nested tables'). */
	@Test
	public void aNestedTableKeepsItsGrid() throws Exception {
		String body = nested(table(tblW(5000, "dxa"), new int[] { 2000, 2000 }, new String[] { dxa(2000), dxa(2000) }));
		for (int flags : FLAGS) {
			assertColumns(pathway(flags), new double[] { 2000, 2000 }, columns(fo(pkg(body, SECT_PR, 15), flags), 1), 1);
		}
	}

	/** A nested 100% table whose grid is its cell's content (9026 less two 108-twip
	 *  margins), short of 100% of the page: its grid.  The visitor pathway only: the XSLT
	 *  pathway's w:tbl has no parent, so it resolves a nested percentage against the page
	 *  (AbstractTableWriter.containingCellWidthTwips, 17.2.0) and scales the grid to it,
	 *  as it did before. */
	@Test
	public void aNestedPercentageTableKeepsItsGrid() throws Exception {
		String body = nested(table(tblW(5000, "pct"), new int[] { 4405, 4405 }, new String[] { dxa(4405), dxa(4405) }));
		assertColumns(pathway(Docx4J.FLAG_NONE), new double[] { 4405, 4405 },
				columns(fo(pkg(body, SECT_PR, 15), Docx4J.FLAG_NONE), 1), 1);
	}

	/** A percentage in a section of three columns is of the section's column, which this
	 *  writer does not resolve: the grid stands.  Measured on a corpus document (A3
	 *  landscape, three columns 567 twips apart, mode 14) whose three fixed 98% tables have
	 *  a 6693-twip grid their cells repeat - 98% of the column and its cell margins - and
	 *  which, resolved against the page's text column, came out three times too wide. */
	@Test
	public void aPercentageInSeveralColumnsKeepsItsGrid() throws Exception {
		String sectPr = "<w:sectPr><w:pgSz w:w=\"23814\" w:h=\"16840\" w:orient=\"landscape\"/>"
				+ "<w:pgMar w:top=\"1134\" w:right=\"1418\" w:bottom=\"1196\" w:left=\"1418\"/>"
				+ "<w:cols w:num=\"3\" w:space=\"567\"/></w:sectPr>";
		String body = table(tblW(4900, "pct") + FIXED, new int[] { 1339, 1339, 1339, 1338, 1338 },
				new String[] { dxa(1339), dxa(1339), dxa(1339), dxa(1338), dxa(1338) });
		for (int flags : FLAGS) {
			assertEquals(pathway(flags), 6693, sum(columns(fo(pkg(body, sectPr, 14), flags), 0)), 1);
		}
	}

	/** Below mode 15 a percentage is of the column and the edge cells' margins - the first
	 *  cell's left, the last cell's right - not twice the left.  A corpus table (mode 14)
	 *  whose own cell margins are 0 and whose first cell states 113 twips each side: 4943
	 *  pct of a 9498-twip column over Word's grid of 3937 / 5569 = 9506 (the column and 113,
	 *  at 98.86%, is 9501).  Twice the left margin read that grid as 1.1% short of the
	 *  percentage and laid the table out 107 twips wider. */
	@Test
	public void aPercentageOnTheEdgeCellsMarginsKeepsWordsGrid() throws Exception {
		String sectPr = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1204\" w:bottom=\"1440\" w:left=\"1204\"/></w:sectPr>";
		String first = "<w:tcW w:type=\"pct\" w:w=\"2071\"/><w:tcMar><w:left w:w=\"113\" w:type=\"dxa\"/>"
				+ "<w:right w:w=\"113\" w:type=\"dxa\"/></w:tcMar>";
		String body = table(tblW(4943, "pct") + "<w:tblCellMar><w:left w:w=\"0\" w:type=\"dxa\"/>"
				+ "<w:right w:w=\"0\" w:type=\"dxa\"/></w:tblCellMar>", new int[] { 3937, 5569 },
				new String[] { first, pct(2929) });
		for (int flags : FLAGS) {
			assertColumns(pathway(flags), new double[] { 3937, 5569 }, columns(fo(pkg(body, sectPr, 14), flags), 0), 1);
		}
	}

	/** The reporter's case: TblFactory writes w:tblW auto with a grid and cells of the
	 *  given width; code that then sets w:tblW gets F6.  A table built in code, never
	 *  saved. */
	@Test
	public void aTableBuiltInCodeTakesTheWidthItWasGiven() throws Exception {
		for (int flags : FLAGS) {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
			Tbl tbl = TblFactory.createTable(2, 3, 2000);
			TblWidth w = new TblWidth();
			w.setW(BigInteger.valueOf(9026));
			w.setType("dxa");
			tbl.getTblPr().setTblW(w);
			pkg.getMainDocumentPart().getContent().add(tbl);
			assertEquals(pathway(flags), 9026, sum(columns(fo(pkg, flags), 0)), 1);
		}
	}

	/** docx4j.convert.out.fo.tables.refitStaleGrid=false draws the grid as written. */
	@Test
	public void theSwitchKeepsTheGrid() throws Exception {
		String body = table(tblW(9026, "dxa"), new int[] { 2000, 2000, 2000 },
				new String[] { dxa(2000), dxa(2000), dxa(2000) });
		Docx4jProperties.setProperty(AbstractTableWriter.REFIT_STALE_GRID, false);
		try {
			for (int flags : FLAGS) {
				assertEquals(pathway(flags), 6000, sum(columns(body, flags)), 1);
			}
		} finally {
			Docx4jProperties.setProperty(AbstractTableWriter.REFIT_STALE_GRID, true);
		}
	}
}
