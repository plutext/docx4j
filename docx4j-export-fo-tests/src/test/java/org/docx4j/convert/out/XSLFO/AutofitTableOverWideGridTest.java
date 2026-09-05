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
 * An over-wide w:tblGrid is left alone only where the table states a width of its own.
 *
 * <p>&#xa7;6.5 of the layout rules says Word draws a table whose grid is wider than the
 * text column at its grid width, overhanging the right margin - measured on grids 3% to
 * 19% over.  That rule was reaching <em>autofit</em> tables too, whose grid is only the
 * width Word cached from its last layout: with w:tblW "auto" and no w:tblLayout Word
 * re-runs its content-based autofit and clamps the result to the text column.  Measured
 * over the real-document corpus, such grids run 1.4 to 2.7 times the column (one 956pt
 * grid on a 453.6pt column, which Word drew 505.3pt wide), and docx4j painted half the
 * document past the page edge and lost 7 of Word's 15 pages.</p>
 *
 * @since 17.0.6
 */
public class AutofitTableOverWideGridTest extends AbstractXSLFOTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";
	private static final String FO = "http://www.w3.org/1999/XSL/Format";

	/** A4 portrait, 1in margins: the text column is 9026 twips = 451.3pt. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final double COLUMN_PT = (11906 - 1440 - 1440) / 20d;

	private static String cell(String width, String text) {
		return "<w:tc><w:tcPr><w:tcW w:w=\"" + width + "\"/></w:tcPr>"
				+ "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc>";
	}

	/** three equal columns of the given width; every cell states its grid width */
	private static String table(String tblPr, String col) {
		return "<w:tbl><w:tblPr>" + tblPr + "</w:tblPr>"
				+ "<w:tblGrid><w:gridCol w:w=\"" + col + "\"/><w:gridCol w:w=\"" + col + "\"/><w:gridCol w:w=\"" + col + "\"/></w:tblGrid>"
				+ "<w:tr>" + cell(col, "one") + cell(col, "two") + cell(col, "three") + "</w:tr>"
				+ "</w:tbl>";
	}

	/** grid 18000 twips = 900pt, twice the text column */
	private static final String AUTOFIT = table("<w:tblW w:w=\"0\" w:type=\"auto\"/>", "6000");
	/** grid 9600 twips = 480pt, 6% over: Word draws that at its grid width */
	private static final String AUTOFIT_JUST_OVER = table("<w:tblW w:w=\"0\" w:type=\"auto\"/>", "3200");
	private static final String FIXED_LAYOUT = table("<w:tblW w:w=\"0\" w:type=\"auto\"/><w:tblLayout w:type=\"fixed\"/>", "6000");
	private static final String PREFERRED_WIDTH = table("<w:tblW w:w=\"18000\" w:type=\"dxa\"/>", "6000");

	private static WordprocessingMLPackage pkg(String tbl) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + tbl + SECT_PR + "</w:body></w:document>"));
		return pkg;
	}

	private org.w3c.dom.Document fo(String tbl, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg(tbl));
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return w3cDomDocumentFromByteArray(baos.toByteArray());
	}

	/** the sum of the fo:table-column widths, in points */
	private double tableWidthPt(org.w3c.dom.Document doc) {
		NodeList cols = doc.getElementsByTagNameNS(FO, "table-column");
		assertEquals(3, cols.getLength());
		double total = 0;
		for (int i = 0; i < cols.getLength(); i++) {
			String w = ((Element)cols.item(i)).getAttribute("column-width");
			assertTrue("no column-width", w.endsWith("pt"));
			total += Double.parseDouble(w.substring(0, w.length() - 2));
		}
		return total;
	}

	private void check(int flags) throws Exception {

		double autofit = tableWidthPt(fo(AUTOFIT, flags));
		assertTrue("an autofit table's over-wide grid must be fitted to the text column, was "
				+ autofit + "pt of " + COLUMN_PT, autofit <= COLUMN_PT + 0.5);

		double justOver = tableWidthPt(fo(AUTOFIT_JUST_OVER, flags));
		assertTrue("a grid a few per cent over the column is Word's own layout and stands, was "
				+ justOver + "pt", justOver > COLUMN_PT + 20);

		assertTrue("a fixed-layout table keeps its grid",
				tableWidthPt(fo(FIXED_LAYOUT, flags)) > COLUMN_PT * 1.5);

		assertTrue("a table with a preferred width of its own keeps its grid",
				tableWidthPt(fo(PREFERRED_WIDTH, flags)) > COLUMN_PT * 1.5);
	}

	@Test
	public void visitor() throws Exception {
		check(Docx4J.FLAG_NONE);
	}

	@Test
	public void xslt() throws Exception {
		check(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
