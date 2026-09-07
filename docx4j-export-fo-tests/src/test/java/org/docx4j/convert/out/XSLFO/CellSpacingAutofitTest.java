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
 * {@code w:tblCellSpacing} is charged against a <b>grid</b> column width, not against a
 * content-autofit one (CR-001 &#xa7;6.6).
 *
 * <p>FOP's separate border model takes one gap per column where Word takes a whole gap
 * and half of each outer one, so docx4j gives the extra half back by taking the spacing
 * off each column - which is right for a grid width, since Word's grid includes the gaps.
 * A content-autofit width is built from the measured content plus {@code w:tblCellMar}
 * and never had a gap in it, so taking one out charged the spacing a second time.
 *
 * <p>Measured on a letterhead with {@code w:tblW auto}, {@code <w:tblCellSpacing w:w="15"/>},
 * {@code w:tblCellMar} 15 twips and one 2799-twip grid column: the autofit width 2736tw
 * (136.8pt) became a 136.05pt column and, less the cell's own 0.75pt of padding either
 * side, a 134.57pt measure, where the cell's line needs 135.3pt and is one line in Word
 * (377.4..512.5).  136.8 less the 1.5pt of {@code border-separation} is exactly 135.3.
 *
 * @since 17.1.0
 */
public class CellSpacingAutofitTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/**
	 * One cell of short text in a table with cell spacing and cell margins.
	 *
	 * @param tblW the w:tblW element, or "" for none (which is w:type="auto")
	 */
	private static String body(String tblW) {
		return body(tblW, "<w:tcW w:w=\"0\" w:type=\"auto\"/>");
	}

	private static String body(String tblW, String tcW) {
		return "<w:document " + W + "><w:body><w:tbl><w:tblPr>"
				+ tblW
				+ "<w:tblCellSpacing w:w=\"15\" w:type=\"dxa\"/>"
				+ "<w:tblCellMar>"
				+ "<w:top w:w=\"0\" w:type=\"dxa\"/><w:left w:w=\"15\" w:type=\"dxa\"/>"
				+ "<w:bottom w:w=\"0\" w:type=\"dxa\"/><w:right w:w=\"15\" w:type=\"dxa\"/>"
				+ "</w:tblCellMar></w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"2799\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr>" + tcW + "</w:tcPr>"
				+ "<w:p><w:r><w:t>DIVISION OF ENTERPRISE SERVICES</w:t></w:r></w:p>"
				+ "</w:tc></w:tr></w:tbl>"
				+ "<w:p/>"
				+ "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
				+ "</w:sectPr></w:body></w:document>";
	}

	private org.w3c.dom.Document fo(String tblW, int flags) throws Exception {
		return fo(tblW, "<w:tcW w:w=\"0\" w:type=\"auto\"/>", flags);
	}

	private org.w3c.dom.Document fo(String tblW, String tcW, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(body(tblW, tcW)));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	private static double columnWidthPt(org.w3c.dom.Document doc) {
		NodeList cols = doc.getElementsByTagNameNS(FO, "table-column");
		assertTrue("no fo:table-column", cols.getLength() > 0);
		return WordLayoutFixupsAccess.lengthPt(((Element) cols.item(0)).getAttribute("column-width"));
	}

	private static double tableWidthPt(org.w3c.dom.Document doc) {
		NodeList tables = doc.getElementsByTagNameNS(FO, "table");
		assertTrue("no fo:table", tables.getLength() > 0);
		return WordLayoutFixupsAccess.lengthPt(((Element) tables.item(0)).getAttribute("width"));
	}

	/** the column must not be narrower than the table less one gap either side */
	private void check(int flags) throws Exception {
		org.w3c.dom.Document doc = fo("", flags);
		double table = tableWidthPt(doc);
		double column = columnWidthPt(doc);
		assertTrue("test setup: no autofit table width", table > 0);
		// the 15tw of cell spacing is 0.75pt; the column may lose the border-separation
		// (2 x 0.75pt) but not that plus another 0.75
		assertTrue("a content-autofit column loses the cell spacing twice:"
				+ " table " + table + "pt, column " + column + "pt",
				column >= table - 1.5 - 0.01);
	}

	@Test
	public void anAutofitColumnIsNotChargedTheSpacingTwiceVisitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void anAutofitColumnIsNotChargedTheSpacingTwiceXslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	/** the control: a grid-sized table still gives the extra half-gap back. */
	@Test
	public void aGridSizedColumnStillLosesTheSpacing() throws Exception {
		org.w3c.dom.Document doc = fo("<w:tblW w:w=\"2799\" w:type=\"dxa\"/>",
				"<w:tcW w:w=\"2799\" w:type=\"dxa\"/>", Docx4J.FLAG_NONE);
		double column = columnWidthPt(doc);
		// 2799tw = 139.95pt, less the 15tw gap = 139.2pt
		assertTrue("a grid column keeps the give-back: " + column + "pt",
				Math.abs(column - 139.2) < 0.2);
	}

	/** {@code WordLayoutFixups.lengthPt} is package-private to its own package. */
	static final class WordLayoutFixupsAccess {
		static double lengthPt(String v) {
			if (v == null || v.length() == 0) return 0;
			if (v.endsWith("pt")) return Double.parseDouble(v.substring(0, v.length() - 2));
			if (v.endsWith("mm")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72 / 25.4;
			if (v.endsWith("in")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72;
			if (v.endsWith("cm")) return Double.parseDouble(v.substring(0, v.length() - 2)) * 72 / 2.54;
			return Double.parseDouble(v);
		}
	}
}
