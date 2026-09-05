package org.docx4j.convert.out.XSLFO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
 * Word's preferred table width (w:tblW) is a target, not just a cap: when the
 * columns sized from their content come to less than it, Word widens them, in
 * their own proportions, until the table is that wide.  docx4j left them at
 * their content widths, so a table Word drew 400pt wide came out 154pt wide with
 * its cell text wrapped (CR-001, table-indent probes).
 *
 * <p>The proportions are the columns' content where every cell is auto-width, and the
 * w:tblGrid where any cell declares a width of its own (measured on real documents).
 * w:tblLayout "fixed" is the exception to all of it: the grid is used as it stands.</p>
 *
 * Both FO pathways (the table FO is built in Java for each).
 */
public class TableWidthTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	/** A4 portrait with 1in margins: a 9026 twip (451.3pt) text column. */
	private static final String SECT_PR = "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
			+ "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr>";

	private static String cell(String width, String text) {
		return cell(width, "dxa", text);
	}

	private static String cell(String width, String type, String text) {
		return "<w:tc>" + (width == null ? "" : "<w:tcPr><w:tcW w:type=\"" + type + "\" w:w=\"" + width + "\"/></w:tcPr>")
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
		assertTrue("no fo:table", nl.getLength() > 0);
		return (Element) nl.item(0);
	}

	private static double pt(String length) {
		if (length == null || length.length() == 0) return Double.NaN;
		if (length.endsWith("pt")) return Double.parseDouble(length.substring(0, length.length() - 2));
		if (length.endsWith("in")) return Double.parseDouble(length.substring(0, length.length() - 2)) * 72;
		throw new IllegalArgumentException(length);
	}

	private static double[] columnsPt(org.w3c.dom.Document doc) {
		NodeList cols = doc.getElementsByTagNameNS("http://www.w3.org/1999/XSL/Format", "table-column");
		double[] out = new double[cols.getLength()];
		for (int i = 0; i < out.length; i++) {
			out[i] = pt(((Element) cols.item(i)).getAttribute("column-width"));
		}
		return out;
	}

	private void checkWidenedToPreferredWidth(int flags) throws Exception {
		// w:tblW 8000 dxa (400pt) over a 3000 twip grid and two auto cells whose text
		// is only a few points wide: Word gives the table its preferred width
		String wide = table("<w:tblW w:type=\"dxa\" w:w=\"8000\"/>",
				"<w:gridCol w:w=\"1500\"/><w:gridCol w:w=\"1500\"/>",
				cell(null, "one") + cell(null, "two"));
		org.w3c.dom.Document doc = fo(wide, flags);
		assertEquals(400.0, pt(foTable(doc).getAttribute("width")), 0.05);
		double[] cols = columnsPt(doc);
		assertEquals(2, cols.length);
		assertEquals(400.0, cols[0] + cols[1], 0.05);
		// "one" and "two" are all but the same width, so the columns come out even
		assertEquals(200.0, cols[0], 5.0);
		assertEquals(200.0, cols[1], 5.0);
	}

	private void checkContentProportionsKept(int flags) throws Exception {
		// the widening keeps the columns' content proportions (measured against Word:
		// content of 67.4 and 74.1pt in a 400pt table gave 190.7 and 209.3pt)
		String wide = table("<w:tblW w:type=\"dxa\" w:w=\"8000\"/>",
				"<w:gridCol w:w=\"1500\"/><w:gridCol w:w=\"1500\"/>",
				cell(null, "i") + cell(null, "wide content in the second column"));
		double[] cols = columnsPt(fo(wide, flags));
		assertEquals(400.0, cols[0] + cols[1], 0.05);
		assertTrue("the wordier column is the wider one: " + cols[0] + " / " + cols[1], cols[1] > cols[0] * 2);
	}

	private void checkDeclaredWidthsWidenOnTheGrid(int flags) throws Exception {
		// where the cells declare widths of their own - a w:tcW in "pct" is the common
		// case - Word lays the table out on its w:tblGrid, however little content a
		// column holds; the content proportions here would put nearly all of the table
		// in the second column
		String pct = table("<w:tblW w:type=\"dxa\" w:w=\"8000\"/>",
				"<w:gridCol w:w=\"1000\"/><w:gridCol w:w=\"2000\"/>",
				cell("1667", "pct", "") + cell("3333", "pct", "wide content in the second column"));
		double[] cols = columnsPt(fo(pct, flags));
		assertEquals(2, cols.length);
		assertEquals(400.0, cols[0] + cols[1], 0.05);
		assertEquals("the grid's 1/3", 133.3, cols[0], 0.1);
		assertEquals("the grid's 2/3", 266.7, cols[1], 0.1);
	}

	private void checkFixedLayoutKeepsTheGrid(int flags) throws Exception {
		// w:tblLayout fixed: the grid stands, whatever w:tblW asks for
		String fixed = table("<w:tblLayout w:type=\"fixed\"/><w:tblW w:type=\"dxa\" w:w=\"8000\"/>",
				"<w:gridCol w:w=\"1500\"/><w:gridCol w:w=\"1500\"/>",
				cell(null, "one") + cell(null, "two"));
		org.w3c.dom.Document doc = fo(fixed, flags);
		assertEquals(150.0, pt(foTable(doc).getAttribute("width")), 0.01);
		double[] cols = columnsPt(doc);
		assertEquals(75.0, cols[0], 0.01);
		assertEquals(75.0, cols[1], 0.01);
	}

	private void checkAutoWidthShrinksToContent(int flags) throws Exception {
		// w:tblW "auto" is no preferred width at all: the table stays at its content
		// width, as it did before
		String auto = table("<w:tblW w:type=\"auto\" w:w=\"0\"/>",
				"<w:gridCol w:w=\"1500\"/><w:gridCol w:w=\"1500\"/>",
				cell(null, "one") + cell(null, "two"));
		double width = pt(foTable(fo(auto, flags)).getAttribute("width"));
		assertTrue("content width, well under the 451.3pt column: " + width, width > 0 && width < 100);
	}

	private void checkPerCellWidthsKept(int flags) throws Exception {
		// a column with its own w:tcW keeps it; the surplus goes to the auto column
		String mixed = table("<w:tblW w:type=\"dxa\" w:w=\"8000\"/>",
				"<w:gridCol w:w=\"2000\"/><w:gridCol w:w=\"1000\"/>",
				cell("2000", "one") + cell(null, "two"));
		org.w3c.dom.Document doc = fo(mixed, flags);
		assertEquals(400.0, pt(foTable(doc).getAttribute("width")), 0.05);
		double[] cols = columnsPt(doc);
		assertEquals(100.0, cols[0], 0.01);
		assertEquals(300.0, cols[1], 0.05);
	}

	@Test
	public void widenedToPreferredWidthVisitor() throws Exception {
		checkWidenedToPreferredWidth(Docx4J.FLAG_NONE);
	}

	@Test
	public void widenedToPreferredWidthXslt() throws Exception {
		checkWidenedToPreferredWidth(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void contentProportionsKeptVisitor() throws Exception {
		checkContentProportionsKept(Docx4J.FLAG_NONE);
	}

	@Test
	public void contentProportionsKeptXslt() throws Exception {
		checkContentProportionsKept(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void declaredWidthsWidenOnTheGridVisitor() throws Exception {
		checkDeclaredWidthsWidenOnTheGrid(Docx4J.FLAG_NONE);
	}

	@Test
	public void declaredWidthsWidenOnTheGridXslt() throws Exception {
		checkDeclaredWidthsWidenOnTheGrid(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void fixedLayoutKeepsTheGridVisitor() throws Exception {
		checkFixedLayoutKeepsTheGrid(Docx4J.FLAG_NONE);
	}

	@Test
	public void fixedLayoutKeepsTheGridXslt() throws Exception {
		checkFixedLayoutKeepsTheGrid(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void autoWidthShrinksToContentVisitor() throws Exception {
		checkAutoWidthShrinksToContent(Docx4J.FLAG_NONE);
	}

	@Test
	public void autoWidthShrinksToContentXslt() throws Exception {
		checkAutoWidthShrinksToContent(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}

	@Test
	public void perCellWidthsKeptVisitor() throws Exception {
		checkPerCellWidthsKept(Docx4J.FLAG_NONE);
	}

	@Test
	public void perCellWidthsKeptXslt() throws Exception {
		checkPerCellWidthsKept(Docx4J.FLAG_EXPORT_PREFER_XSL);
	}
}
