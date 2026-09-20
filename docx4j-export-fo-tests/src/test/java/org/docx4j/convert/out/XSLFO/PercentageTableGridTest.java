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
package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A table which states a <b>percentage</b> width its {@code w:tblGrid} sums to is laid out
 * on that grid, as one stating an absolute width is.
 *
 * <p>What the percentage is a percentage <em>of</em>, below compatibility mode 15, is the
 * <b>grid edge</b> - the text column widened by one cell margin at each end, which is where
 * the grid sits there ({@code TableWriter.autofitGridAllowanceTwips}), not the text column
 * itself. Measured on a corpus document's landscape five-column floating table,
 * {@code w:tblW w:w="5205" w:type="pct"} (104.1%) on a 12960-twip column with 108-twip cell
 * margins: 104.1% of the column is 13491 twips and 104.1% of the grid edge's 13176 is
 * <b>13716</b>, which is its {@code w:tblGrid} 4875/2208/1975/1624/3034 to the twip - and
 * Word's own re-save of that document leaves the grid untouched, so that is the layout Word
 * uses. Read against the column the grid missed by 1.7%, the content pass ran, and it gave
 * 4794/1722/1722/2269/2984 - two columns 20% and 13% narrow and one 40% wide - which cost the
 * document two of Word's thirteen pages.
 *
 * @since 17.2.0
 */
public class PercentageTableGridTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait, 1in margins: the text column is 9026 twips. */
	private static final int COLUMN_TWIPS = 11906 - 1440 - 1440;

	/** Word's default left cell margin, which the grid edge is one of at each end. */
	private static final int CELL_MARGIN_TWIPS = 108;

	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	/** 104.1%, as the corpus document states. */
	private static final int PCT = 5205;

	/** The grid is that percentage of the grid edge, which is what Word's own grid is. */
	private static final int GRID_TOTAL = (int) ((long) (COLUMN_TWIPS + 2 * CELL_MARGIN_TWIPS) * PCT / 5000);

	/**
	 * Three columns summing to {@link #GRID_TOTAL}, of quite different widths, with cells
	 * whose content would size them very differently: only the grid can produce the
	 * asserted widths.
	 */
	private static String table(String tblW) {
		int a = GRID_TOTAL / 2, b = GRID_TOTAL / 3, c = GRID_TOTAL - a - b;
		return "<w:tbl><w:tblPr>" + tblW + "</w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"" + a + "\"/><w:gridCol w:w=\"" + b + "\"/>"
				+ "<w:gridCol w:w=\"" + c + "\"/></w:tblGrid>"
				+ "<w:tr>"
				+ "<w:tc><w:tcPr><w:tcW w:w=\"0\" w:type=\"auto\"/></w:tcPr><w:p><w:r><w:t>a</w:t></w:r></w:p></w:tc>"
				+ "<w:tc><w:tcPr><w:tcW w:w=\"0\" w:type=\"auto\"/></w:tcPr><w:p><w:r><w:t>b</w:t></w:r></w:p></w:tc>"
				+ "<w:tc><w:tcPr><w:tcW w:w=\"0\" w:type=\"auto\"/></w:tcPr><w:p><w:r><w:t>c</w:t></w:r></w:p></w:tc>"
				+ "</w:tr></w:tbl>";
	}

	private static WordprocessingMLPackage pkg(String tblW) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		// the rule above is the below-mode-15 one (the grid edge as the percentage's base);
		// createPackage writes mode 15 since 17.2.0, so pin the mode the corpus document had
		pkg.getMainDocumentPart().getDocumentSettingsPart().setCompatibilityMode(14);
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + table(tblW) + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private org.w3c.dom.Document fo(String tblW, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg(tblW));
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	/** the fo:table-column widths, in twips */
	private int[] columnsTwips(org.w3c.dom.Document doc) {
		NodeList cols = doc.getElementsByTagNameNS(FO, "table-column");
		assertEquals(3, cols.getLength());
		int[] out = new int[cols.getLength()];
		for (int i = 0; i < cols.getLength(); i++) {
			String w = ((Element) cols.item(i)).getAttribute("column-width");
			assertTrue("no column-width", w.endsWith("pt"));
			out[i] = (int) Math.round(Double.parseDouble(w.substring(0, w.length() - 2)) * 20);
		}
		return out;
	}

	private void checkGridIsKept(int flags) throws Exception {
		int[] cols = columnsTwips(fo("<w:tblW w:w=\"" + PCT + "\" w:type=\"pct\"/>", flags));
		int a = GRID_TOTAL / 2, b = GRID_TOTAL / 3;
		assertEquals("column 1 is the grid's", a, cols[0], 1);
		assertEquals("column 2 is the grid's", b, cols[1], 1);
		assertEquals("the table is the grid's width", GRID_TOTAL, cols[0] + cols[1] + cols[2], 3);
	}

	@Test
	public void percentageGridIsKeptVisitorPathway() throws Exception {
		checkGridIsKept(Docx4J.FLAG_NONE);
	}

	@Test
	public void percentageGridIsKeptXsltPathway() throws Exception {
		checkGridIsKept(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/**
	 * The rule is that the grid sums to the stated width, not that the width is a
	 * percentage: a percentage the grid does <em>not</em> sum to leaves the table to the
	 * content pass, which sizes these one-letter cells nothing like the grid.
	 */
	@Test
	public void aGridWhichDoesNotSumToThePercentageIsNotAuthoritative() throws Exception {
		int[] cols = columnsTwips(fo("<w:tblW w:w=\"2500\" w:type=\"pct\"/>", Docx4J.FLAG_NONE));
		assertTrue("the content pass sizes it, not the grid",
				Math.abs(cols[0] + cols[1] + cols[2] - GRID_TOTAL) > GRID_TOTAL / 10);
	}
}
