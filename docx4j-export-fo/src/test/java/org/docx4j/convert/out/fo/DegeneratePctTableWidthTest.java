package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * A {@code w:tblW} in pct which resolves to less than one pair of Word's default cell
 * margins is not a width.  Measured on a corpus document stating
 * {@code w:tblW w:w="1" w:type="pct"} - 0.02% of the text column - with a
 * {@code w:tblCellMar} of 0: Word draws the table full width, where docx4j scaled its grid
 * to the percentage and drew four 0.05pt columns.  Of the corpora's 1,855 pct table
 * widths the next-smallest is in the 30-39% band.
 *
 * @since 17.1.1
 */
public class DegeneratePctTableWidthTest {

	private static String documentXML(int pct) {
		return "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:body>"
				+ "<w:tbl>"
				+ "<w:tblPr><w:tblW w:w=\"" + pct + "\" w:type=\"pct\"/>"
				+ "<w:tblCellMar><w:left w:w=\"0\" w:type=\"dxa\"/><w:right w:w=\"0\" w:type=\"dxa\"/></w:tblCellMar>"
				+ "</w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"3000\"/><w:gridCol w:w=\"3000\"/><w:gridCol w:w=\"3000\"/></w:tblGrid>"
				+ "<w:tr>"
				+ cell("Personal Protective Equipment") + cell("Quantity") + cell("Notes")
				+ "</w:tr>"
				+ "</w:tbl>"
				+ "<w:p/>"
				+ "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
				+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/>"
				+ "</w:sectPr>"
				+ "</w:body></w:document>";
	}

	private static String cell(String text) {
		return "<w:tc><w:tcPr><w:tcW w:w=\"0\" w:type=\"auto\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc>";
	}

	private static List<Double> columnWidthsPt(String fo) {
		List<Double> out = new ArrayList<>();
		Matcher m = Pattern.compile("column-width=\"([0-9.]+)pt\"").matcher(fo);
		while (m.find()) out.add(Double.parseDouble(m.group(1)));
		return out;
	}

	private static String convert(int pct) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString(documentXML(pct)));
		FOSettings settings = new FOSettings(pkg);
		settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(settings, baos, Docx4J.FLAG_NONE);
		return baos.toString("UTF-8");
	}

	/** 0.02% of a 468pt text column is 0.094pt: not a width.  The table is laid out as
	 *  if it stated none (its content autofit), and every column holds its word - the
	 *  narrowest, "Notes" with no cell margins, at some 26pt against the 0.03pt each
	 *  column got when the percentage was honoured. */
	@Test
	public void aPercentageBelowOnePairOfCellMarginsIsIgnored() throws Exception {
		List<Double> widths = columnWidthsPt(convert(1));
		assertEquals(3, widths.size());
		double sum = 0;
		for (double w : widths) {
			assertTrue("column of " + w + "pt", w > 20);
			sum += w;
		}
		assertTrue("table of " + sum + "pt", sum > 150);
	}

	/** The control: 100% is a width, and the grid is scaled to it - the 468pt column. */
	@Test
	public void aRealPercentageIsHonoured() throws Exception {
		List<Double> widths = columnWidthsPt(convert(5000));
		assertEquals(3, widths.size());
		double sum = 0;
		for (double w : widths) sum += w;
		assertEquals(468, sum, 1.0);
	}
}
