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
 * The content-autofit column sizer reads the cell's own {@code w:tcMar}, not only the
 * table's {@code w:tblCellMar}.
 *
 * <p>Where a table sets {@code w:tblCellMar} 0 left and right and every cell overrides
 * with {@code w:tcMar} 30 twips, the columns were sized to the bare text width and the
 * cell writer then emitted 1.5pt of padding each side - so the very line that sized the
 * column no longer fitted.  Measured against Word 365: a cell whose one line is 68.9pt
 * wide got a 68.95pt column and a 65.95pt measure, and broke in two, where Word keeps it
 * on one line (70.8..149.5).  10 documents of three corpora set a {@code w:tcMar} where
 * the table's own margins are narrower.</p>
 *
 * @since 17.0.6
 */
public class CellMarginAutofitTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	private static final int[] FLAGS = { Docx4J.FLAG_NONE, Docx4J.FLAG_EXPORT_PREFER_XSL };

	private static String flagName(int flag) {
		return flag == Docx4J.FLAG_EXPORT_PREFER_XSL ? "XSL" : "visitor";
	}

	/** An autofit table whose cell margins are the table's 0 plus the cell's own. */
	private static String table(boolean tcMar) {
		String mar = tcMar
				? "<w:tcMar><w:left w:w=\"300\" w:type=\"dxa\"/><w:right w:w=\"300\" w:type=\"dxa\"/></w:tcMar>"
				: "";
		return "<w:tbl><w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/>"
				+ "<w:tblCellMar><w:left w:w=\"0\" w:type=\"dxa\"/><w:right w:w=\"0\" w:type=\"dxa\"/></w:tblCellMar>"
				+ "</w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"3000\"/></w:tblGrid>"
				+ "<w:tr><w:tc><w:tcPr><w:tcW w:w=\"0\" w:type=\"auto\"/>" + mar + "</w:tcPr>"
				+ "<w:p><w:r><w:t>Boite de reception x</w:t></w:r></w:p></w:tc></w:tr></w:tbl>";
	}

	private static double columnWidthPt(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		org.w3c.dom.Document doc = XmlUtils.getNewDocumentBuilder()
				.parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
		NodeList cols = doc.getElementsByTagNameNS(FO, "table-column");
		assertTrue("no fo:table-column", cols.getLength() > 0);
		String w = ((Element) cols.item(0)).getAttribute("column-width");
		return Double.parseDouble(w.replaceAll("[a-zA-Z]+$", ""));
	}

	/** The cell's own 2 x 15 twips (1.5pt) enter the column's width. */
	@Test
	public void theCellsOwnMarginsWidenTheColumn() throws Exception {
		for (int flag : FLAGS) {
			double without = columnWidthPt(table(false), flag);
			double with = columnWidthPt(table(true), flag);
			assertTrue(flagName(flag) + ": the column is " + with + "pt with w:tcMar and "
					+ without + "pt without; the cell's own margins were not counted",
					with - without > 2.5);
		}
	}
}
