package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
 * Where a table sits, and how wide it is, as Word puts it (CR-001, measured on
 * real documents):
 *
 * <ul>
 * <li>Word places the first column's <em>text</em> at the text margin plus
 *   w:tblInd, so the grid edge is one left cell margin further back; docx4j put
 *   the grid edge at the indent and added the cell margin as padding, so every
 *   table's content was 5.4pt too far right and overflowed the right margin by
 *   the same;</li>
 * <li>a w:jc="center" table wider than the text column is centred by Word,
 *   overhanging both margins;</li>
 * <li>widths docx4j chose itself (the autofit pass) are kept inside the text
 *   column; a table's own w:tblGrid is left alone even when it is wider, because
 *   that is what Word does.</li>
 * </ul>
 *
 * Both FO pathways (the table FO is built in Java for each).
 */
public class TablePositionTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** A4 portrait with 1in margins: a 9026 twip (451.3pt) text column. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static final int COLUMN_TWIPS = 11906 - 2880;

	private static String cell(String width, String text) {
		return "<w:tc>" + (width == null ? "" : "<w:tcPr><w:tcW w:type=\"dxa\" w:w=\"" + width + "\"/></w:tcPr>")
				+ "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:tc>";
	}

	private static String table(String tblPrExtra, String grid, String cells) {
		return "<w:tbl><w:tblPr>" + tblPrExtra + "</w:tblPr><w:tblGrid>" + grid + "</w:tblGrid>"
				+ "<w:tr>" + cells + "</w:tr></w:tbl>";
	}

	private static org.w3c.dom.Document fo(String body, int flags) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document)XmlUtils.unmarshalString(
				"<w:document " + W + "><w:body>" + body + SECT_PR + "</w:body></w:document>"));
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setWmlPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(baos.toByteArray()));
	}

	private static Element foTable(org.w3c.dom.Document doc) {
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "table");
		assertNotNull(nl);
		org.junit.Assert.assertTrue("no fo:table", nl.getLength() > 0);
		return (Element) nl.item(0);
	}

	/** a length like "-5.4pt", "0in" or "451.3pt", in points */
	private static double pt(String length) {
		if (length == null || length.length() == 0) return Double.NaN;
		if (length.endsWith("pt")) return Double.parseDouble(length.substring(0, length.length() - 2));
		if (length.endsWith("in")) return Double.parseDouble(length.substring(0, length.length() - 2)) * 72;
		throw new IllegalArgumentException(length);
	}

	private static double startIndentPt(org.w3c.dom.Document doc) {
		return pt(foTable(doc).getAttribute("start-indent"));
	}

	private void checkGridEdge(int flags) throws Exception {
		// tblInd 0, default (108 twip) cell margins: Word's first cell text is at the
		// margin, so the grid edge is 5.4pt to the left of it
		String plain = table("", "<w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/>",
				cell(null, "one") + cell(null, "two"));
		assertEquals(-5.4, startIndentPt(fo(plain, flags)), 0.01);

		// tblInd 108: the cell margin cancels it, and the text is at the margin + 5.4pt
		String indented = table("<w:tblInd w:type=\"dxa\" w:w=\"108\"/>",
				"<w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/>", cell(null, "one") + cell(null, "two"));
		assertEquals(0.0, startIndentPt(fo(indented, flags)), 0.01);

		// an explicit cell margin is used in place of Word's default
		String wideMargin = table("<w:tblCellMar><w:left w:type=\"dxa\" w:w=\"288\"/></w:tblCellMar>",
				"<w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"2000\"/>", cell(null, "one") + cell(null, "two"));
		assertEquals(-14.4, startIndentPt(fo(wideMargin, flags)), 0.01);
	}

	private void checkCentredOverflow(int flags) throws Exception {
		// a 600pt (12000 twip) table centred on a 451.3pt column overhangs both margins
		String centred = table("<w:jc w:val=\"center\"/><w:tblLayout w:type=\"fixed\"/>",
				"<w:gridCol w:w=\"6000\"/><w:gridCol w:w=\"6000\"/>",
				cell("6000", "one") + cell("6000", "two"));
		org.w3c.dom.Document doc = fo(centred, flags);
		assertEquals("600pt table on a 451.3pt column", 600.0, pt(foTable(doc).getAttribute("width")), 0.01);
		assertEquals((COLUMN_TWIPS - 12000) / 40.0, startIndentPt(doc), 0.01); // half the overflow, negative
	}

	private void checkFitToPage(int flags) throws Exception {
		// w:tblW asks for 600pt of a 451.3pt column and the columns are auto, so the
		// autofit pass sizes them to 600pt; docx4j's own widths are fitted to the page
		String wide = table("<w:tblW w:type=\"dxa\" w:w=\"12000\"/>",
				"<w:gridCol w:w=\"6000\"/><w:gridCol w:w=\"6000\"/>",
				cell(null, "one two three four five six seven eight nine ten eleven twelve")
				+ cell(null, "alpha beta gamma delta epsilon zeta eta theta iota kappa lambda"));
		org.w3c.dom.Document doc = fo(wide, flags);
		assertEquals(COLUMN_TWIPS / 20.0, pt(foTable(doc).getAttribute("width")), 0.01);
		NodeList cols = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "table-column");
		assertEquals(2, cols.getLength());
		double sum = 0;
		for (int i = 0; i < cols.getLength(); i++) {
			sum += pt(((Element) cols.item(i)).getAttribute("column-width"));
		}
		assertEquals(COLUMN_TWIPS / 20.0, sum, 0.02);

		// a w:tblLayout of "fixed" is left to overflow, as Word leaves it
		String fixed = table("<w:tblLayout w:type=\"fixed\"/><w:tblW w:type=\"dxa\" w:w=\"12000\"/>",
				"<w:gridCol w:w=\"6000\"/><w:gridCol w:w=\"6000\"/>",
				cell("6000", "one") + cell("6000", "two"));
		assertEquals(600.0, pt(foTable(fo(fixed, flags)).getAttribute("width")), 0.01);

		// and so is the document's own w:tblGrid, even when it is wider than the column:
		// Word draws such a table overhanging the right margin at its grid width
		// (measured over the real-document corpus at 3% to 19% over)
		String grid = table("", "<w:gridCol w:w=\"6000\"/><w:gridCol w:w=\"6000\"/>",
				cell("6000", "one") + cell("6000", "two"));
		assertEquals(600.0, pt(foTable(fo(grid, flags)).getAttribute("width")), 0.01);
	}

	@Test
	public void gridEdgeVisitor() throws Exception {
		checkGridEdge(Docx4J.FLAG_NONE);
	}

	@Test
	public void gridEdgeXslt() throws Exception {
		checkGridEdge(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void centredOverflowVisitor() throws Exception {
		checkCentredOverflow(Docx4J.FLAG_NONE);
	}

	@Test
	public void centredOverflowXslt() throws Exception {
		checkCentredOverflow(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void fitToPageVisitor() throws Exception {
		checkFitToPage(Docx4J.FLAG_NONE);
	}

	@Test
	public void fitToPageXslt() throws Exception {
		checkFitToPage(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
